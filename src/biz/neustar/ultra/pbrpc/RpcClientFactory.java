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
