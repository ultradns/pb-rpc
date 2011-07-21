/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.ultra.pbrpc.mbeans.Stats;
import biz.neustar.ultra.pbrpc.mbeans.StatsNoOp;

public class RpcServer {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcServer.class);
	private ArrayList<SocketAddress> addresses = new ArrayList<SocketAddress>();
	private ServiceRegistry serviceRegistry = new ServiceRegistry();
	private ServerChannelFactory serverChannelFactory = new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
	private ServerBootstrap bootstrap = null;
	private ChannelGroup serverChannels = new DefaultChannelGroup();
	private Stats statsBean = new StatsNoOp();
	
	
	
	public RpcServer(Integer port) {
		this.addresses.add(new InetSocketAddress(port));
	}
	
	public RpcServer(ArrayList<SocketAddress> socketAddresses) {
		this.addresses = socketAddresses;
	}

	/**
	 * Register a service to be exposed over PB-RPC
	 * @param service the service to expose
	 */
	public void registerService(Service service) {
		serviceRegistry.add(service);
	}
	
	/**
	 * Set a custom channel factory, this should be done before calling start()
	 * @param serverChannelFactory netty server channel factory
	 */
	public void setChannelFactory(ServerChannelFactory serverChannelFactory) {
		this.serverChannelFactory = serverChannelFactory;
	}
	
	/**
	 * Enable statistics collection for exposure as an MBean
	 * This is useful for monitoring with minimal performance impact
	 */
	public void collectStats() {
		try {
			MBeanServer mbs = ManagementFactory.getPlatformMBeanServer(); 
			ObjectName name = new ObjectName("biz.neustar.ultra.pbrpc:type=Stats"); 
			statsBean = new Stats();
		    mbs.registerMBean(statsBean, name);
		} catch (Exception e) {
			LOGGER.error("Problem registering Stats MBean: {}", e);
		}
	}
	
	protected Stats getStatsBean() {
		return statsBean;
	}
	
	/**
	 * Start the PB-RPC Server
	 */
	public synchronized void start() {
		// Configure the server.
		bootstrap = new ServerBootstrap(serverChannelFactory);
		
		// Set up the event pipeline factory.
	    bootstrap.setPipelineFactory(
	    		new ProtobufServerPipelineFactory(serviceRegistry, serverChannels, statsBean));
	    bootstrap.setOption("reuseAddress", true);
	    bootstrap.setOption("child.tcpNoDelay", true);
	    bootstrap.setOption("child.keepAlive", true);
	      
	    // Bind and start to accept incoming connections.
	    for (SocketAddress socketAddress : addresses) { // bind to the given ports
	    	Channel channel = bootstrap.bind(socketAddress);
	    	if (!channel.isBound()) {
	    		shutdown();
	    		throw new RuntimeException("Not bound correctly to address: " + socketAddress);
	    	}
	    	serverChannels.add(channel);
	    }
	}
	
	/**
	 * Shutdown the PB-RPC Server and close all channels
	 */
	public synchronized void shutdown() {
        // Shut down all thread pools to exit.
		serverChannels.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}
}
