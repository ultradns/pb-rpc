/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
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

import biz.neustar.ultra.service.example.AnotherServiceMessage.AnotherService;
import biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest;
import biz.neustar.ultra.service.example.ExampleRequestMessage.NestedItem;
import biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse;
import biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService;

public class RpcClientServerTest {
	static final LocalAddress address = new LocalAddress("1");
	static final ArrayList<SocketAddress> serverAddresses = new ArrayList<SocketAddress>();
	
	static {
		serverAddresses.add(address);
	}

	@Test
	public void testClientServerPath() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		Thread.sleep(100);
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
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
	
	// test non-existant service
	@Test(expected=ExecutionException.class)
	public void testInvalidService() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		Thread.sleep(100);
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		/* */
		AnotherService.Stub exClient = AnotherService.newStub(rpcClient);
		
		ExampleRequest.Builder req = ExampleRequest.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		String testId = "TESTING";
		item.setValue(testId);
		req.setItem(item);
		String something = "nothing";
		req.setSomething(something);
		
		Future<ExampleResponse> resp = exClient.doSomething(req.build());
		try {
			assertEquals(something + testId, resp.get().getItem());
			/* */
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
}
