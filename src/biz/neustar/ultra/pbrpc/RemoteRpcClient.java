/**
 *  Copyright (c) 2011 NeuStar, Inc.
 *  All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 *  NeuStar, the Neustar logo and related names and logos are registered
 *  trademarks, service marks or tradenames of NeuStar, Inc. All other
 *  product names, company names, marks, logos and symbols may be trademarks
 *  of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFactory;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;

public class RemoteRpcClient extends RpcClient {
	private static final Logger LOGGER = LoggerFactory.getLogger(RemoteRpcClient.class);
	private String callerId = null;
	private volatile Channel channel;
	private volatile ClientBootstrap bootstrap;
	private RpcClientHandler handler;
	private ChannelFactory clientChannelFactory = new NioClientSocketChannelFactory(
            Executors.newCachedThreadPool(), Executors.newCachedThreadPool());
	private long readTimeout = 30;
	private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;
	private volatile boolean started = false;
	
	public RemoteRpcClient() {
	}
	
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	
	public void setChannelFactory(ChannelFactory clientChannelFactory) {
		this.clientChannelFactory = clientChannelFactory;
	}
	
	public void setReadTimeout(long readTimeout, TimeUnit readTimeoutUnit) {
		this.readTimeout = readTimeout;
		this.readTimeoutUnit = readTimeoutUnit;
	}
	
	
	public void start(String host, int port) {
		start(new InetSocketAddress(host, port));
	}
	
	public void start(SocketAddress address) {
		// Set up.
        bootstrap = new ClientBootstrap(clientChannelFactory);

        // Configure the event pipeline factory.
        bootstrap.setPipelineFactory(
        		new ProtobufClientPipelineFactory(readTimeout, readTimeoutUnit));
        // set the option so the reconnect can just call connect
        bootstrap.setOption("remoteAddress", address);
        connect();
        started = true;
	}
	
	protected synchronized void connect() {
		if (channel != null && channel.isConnected())
			return;
		
		if (channel != null && LOGGER.isDebugEnabled()) {
			LOGGER.debug("Reconnecting to: {}", bootstrap.getOption("remoteAddress"));
		}
		// Make a new connection.	
        ChannelFuture connectFuture = bootstrap.connect();

        // Wait until the connection is made successfully.
        Channel channel = connectFuture.awaitUninterruptibly().getChannel();
        if (!connectFuture.isSuccess()) {
        	throw new RuntimeException(connectFuture.getCause());
        }

        // Get the handler instance to initiate the request.
        handler = channel.getPipeline().get(RpcClientHandler.class);
        handler.setCallerId(callerId);
        this.channel = channel;
	}
	
	
	public void shutdown() {
		// Close the connection.
        channel.close().awaitUninterruptibly();

        // Shut down all thread pools to exit.
        bootstrap.releaseExternalResources();
	}
	
	@Override
	public <T extends Message> Future<T> callMethod(MethodDescriptor method,
			Message request, T responsePrototype) {
	
		if (!started) {
			throw new RuntimeException("RPC Client Not Started");
		}
		if (!channel.isConnected()) {
			LOGGER.info("Possible Reconnect to: {}", bootstrap.getOption("remoteAddress"));
			connect();
		}
		return handler.callMethod(method, request, responsePrototype);
	}
}
