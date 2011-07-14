/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.net.SocketAddress;

import org.jboss.netty.channel.ChannelFactory;

public class RpcClientFactory {
	private String callerId = null;
	
	public RpcClientFactory(String callerId) {
		this.callerId = callerId;
	}
	
	public RpcClient createRpcClient(String host, int port) {
		RemoteRpcClient client = new RemoteRpcClient();
		client.setCallerId(callerId);
		client.start(host, port);
		return client;
	}
	
	public RpcClient createRpcClient(ChannelFactory clientChannelFactory, SocketAddress address) {
		RemoteRpcClient client = new RemoteRpcClient();
		client.setCallerId(callerId);
		client.setChannelFactory(clientChannelFactory);
		client.start(address);
		return client;
	}
	
	public RpcClient createLocalRpcClient(LocalRpcServer rpcServer) {
		LocalRpcClient client = new LocalRpcClient();
		client.setLocalRpcServer(rpcServer);
		return client;
	}
}
