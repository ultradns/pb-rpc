/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.Executors;

import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ServerChannelFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;

public class RpcServer {
	private ArrayList<SocketAddress> addresses = new ArrayList<SocketAddress>();
	private ServiceRegistry serviceRegistry = new ServiceRegistry();
	private ServerChannelFactory serverChannelFactory = new NioServerSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
	private ServerBootstrap bootstrap = null;
	private ChannelGroup serverChannels = new DefaultChannelGroup();
	
	public RpcServer(Integer port) {
		this.addresses.add(new InetSocketAddress(port));
	}
	
	public RpcServer(ArrayList<SocketAddress> socketAddresses) {
		this.addresses = socketAddresses;
	}

	public void registerService(Service service) {
		serviceRegistry.add(service);
	}
	
	public void setChannelFactory(ServerChannelFactory serverChannelFactory) {
		this.serverChannelFactory = serverChannelFactory;
	}
	
	public synchronized void start() {
		// Configure the server.
		bootstrap = new ServerBootstrap(serverChannelFactory);
		
		// Set up the event pipeline factory.
	    bootstrap.setPipelineFactory(
	    		new ProtobufServerPipelineFactory(serviceRegistry, serverChannels));
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
	
	public synchronized void shutdown() {
        // Shut down all thread pools to exit.
		serverChannels.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}
}
