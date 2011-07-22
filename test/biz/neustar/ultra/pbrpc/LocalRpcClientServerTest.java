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

import static org.junit.Assert.assertEquals;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;

import biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest;
import biz.neustar.ultra.service.example.ExampleRequestMessage.NestedItem;
import biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse;
import biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService;

public class LocalRpcClientServerTest {


	@Test
	public void testClientServerPath() throws InterruptedException, ExecutionException {
		final LocalRpcServer rpcServer = new LocalRpcServer();
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createLocalRpcClient(rpcServer);
		
		/* */
		ExampleService.Stub exClient = ExampleService.newStub(rpcClient);
		
		ExampleRequest.Builder req = ExampleRequest.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		String testId = "TESTING";
		item.setValue(testId);
		req.setItem(item);
		String something = "nothing";
		req.setSomething(something);
		
		Future<ExampleResponse> resp = exClient.getSomething(req.build());
		assertEquals(something + testId, resp.get().getItem());
		/* */
		
		rpcClient.shutdown();
		rpcServer.shutdown();
	}
}
