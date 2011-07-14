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
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;

public class RemoteRpcClient extends RpcClient {
	private String callerId = null;
	private volatile Channel channel;
	private volatile ClientBootstrap bootstrap;
	private RpcClientHandler handler;
	private ChannelFactory clientChannelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());

	
	public RemoteRpcClient() {
	}
	
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	
	public void setChannelFactory(ChannelFactory clientChannelFactory) {
		this.clientChannelFactory = clientChannelFactory;
	}
	
	
	public void start(String host, int port) {
		start(new InetSocketAddress(host, port));
	}
	
	public void start(SocketAddress address) {
		// Set up.
        bootstrap = new ClientBootstrap(clientChannelFactory);

        // Configure the event pipeline factory.
        bootstrap.setPipelineFactory(new ProtobufClientPipelineFactory());

        // Make a new connection.	
        ChannelFuture connectFuture = bootstrap.connect(address);

        // Wait until the connection is made successfully.
        channel = connectFuture.awaitUninterruptibly().getChannel();
        if (!connectFuture.isSuccess()) {
        	throw new RuntimeException(connectFuture.getCause());
        }

        // Get the handler instance to initiate the request.
        handler = channel.getPipeline().get(RpcClientHandler.class);
        handler.setCallerId(callerId);
	}
	
	
	public void shutdown() {
		// Close the connection.
        channel.close().awaitUninterruptibly();

        // Shut down all thread pools to exit.
        bootstrap.releaseExternalResources();
	}
	
	@Override
	public <T extends Message> Future<T> call(MethodDescriptor method,
			Message request, T responsePrototype) {
	
		return handler.callMethod(method, request, responsePrototype);
	}

	@Override
	public <T extends Message> void call(MethodDescriptor method,
			Message request, T responsePrototype, RpcCallback<T> done) {
		
		handler.callMethod(method, request, responsePrototype, done);
	}
}
