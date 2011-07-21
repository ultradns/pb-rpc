/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import com.google.protobuf.UnknownFieldSet;
import com.google.protobuf.UnknownFieldSet.Field;

import biz.neustar.ultra.service.example.AnotherServiceMessage.AnotherService;
import biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest;
import biz.neustar.ultra.service.example.ExampleRequestMessage.NestedItem;
import biz.neustar.ultra.service.example.ExampleResponseMessage;
import biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse;
import biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService;

public class RpcClientServerTest {
	static final LocalAddress address = new LocalAddress("1");
	static final ArrayList<SocketAddress> serverAddresses = new ArrayList<SocketAddress>();
	static final int port = 9697;
	static final String localHost = "127.0.0.1";
	static {
		serverAddresses.add(address);
	}

	@Test
	public void testClientServerPath() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		
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
		try {
			assertEquals(something + testId, resp.get().getItem());
			// no stats so these should be empty
			assertEquals((Long) 0L, rpcServer.getStatsBean().getGeneralErrorCount());
			assertCallStats(rpcServer, null, null, null, "service.example.ExampleService.getSomething");
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	@Test
	public void testFullClientServerPath() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(port);
		rpcServer.registerService(new ExampleServiceImpl());
		rpcServer.start();
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(localHost, port);
		
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
		try {
			assertEquals(something + testId, resp.get().getItem());
			// no stats so these should be empty
			assertEquals((Long) 0L, rpcServer.getStatsBean().getGeneralErrorCount());
			assertCallStats(rpcServer, null, null, null, "service.example.ExampleService.getSomething");
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	@Test(expected=ExecutionException.class)
	public void testClientNoHangOnClose() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		
		rpcServer.start();
		
		final RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		ExampleService exService = mock(ExampleService.class);
		when(exService.getSomething(any(ExampleRequest.class))).thenAnswer(new Answer<ExampleResponse>() {
		     public ExampleResponse answer(InvocationOnMock invocation) throws Throwable {
		    	 rpcClient.shutdown();
		    	 return ExampleResponse.newBuilder().setItem("here").build();
		     }
		 });
		rpcServer.registerService(exService);
		
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
		try {
			resp.get().getItem();
			// no stats so these should be empty
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	@Test
	public void testClientReconnect() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		
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
		try {
			assertEquals(something + testId, resp.get().getItem());
		} finally {
			rpcServer.shutdown();
		}
		
		// restart the server
		rpcServer.start();
		
		Future<ExampleResponse> resp2 = exClient.getSomething(req.build());
		try {
			assertEquals(something + testId, resp2.get().getItem());
		} finally {
			rpcServer.shutdown();
			rpcClient.shutdown();
		}
	}
	
	@Test
	public void testThreadedClientReconnect() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		/* */
		final ExampleService.Stub exClient = ExampleService.newStub(rpcClient);
		
		final ExampleRequest.Builder req = ExampleRequest.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		final String testId = "TESTING";
		item.setValue(testId);
		req.setItem(item);
		final String something = "nothing";
		req.setSomething(something);
		
		Future<ExampleResponse> resp = exClient.getSomething(req.build());
		try {
			assertEquals(something + testId, resp.get().getItem());
		} finally {
			rpcServer.shutdown();
		}
		
		// restart the server
		rpcServer.start();
		Thread thread1 = new Thread(new Runnable(){
			@Override
			public void run() {
				Future<ExampleResponse> resp = exClient.getSomething(req.build());
				try {
					assertEquals(something + testId, resp.get().getItem());
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue(false);
				}
			}			
		});
		
		
		Thread thread2 = new Thread(new Runnable(){
			@Override
			public void run() {
				Future<ExampleResponse> resp = exClient.getSomething(req.build());
				try {
					assertEquals(something + testId, resp.get().getItem());
				} catch (Exception e) {
					e.printStackTrace();
					assertTrue(false);
				}
			}			
		});
		
		
		try {
			thread1.start();
			thread2.start();
			thread1.join();
			thread2.join();
		} finally {
			rpcServer.shutdown();
			rpcClient.shutdown();
		}
	}
	
	// test non-existant service
	@Test(expected=ExecutionException.class)
	public void testInvalidService() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
		rpcServer.collectStats();
		rpcServer.start();
		
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
			assertEquals((Long) 1L, rpcServer.getStatsBean().getGeneralErrorCount());
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	@Test
	public void testInvalidMessage() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
	
		rpcServer.start();
		
		RpcClient rpcClient = new RpcClientFactory("test caller id").createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		// send it the response, which it doesnt expect.
		ExampleResponse.Builder req = ExampleResponse.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		String testId = "TESTING";
		item.setValue(testId);
		
		UnknownFieldSet unknowns = UnknownFieldSet.newBuilder()
			.addField(255, Field.newBuilder().addFixed32(99).build())
			.build();
		ExampleResponse methodReq = req.setUnknownFields(unknowns).build();
		Future<ExampleResponse> resp = rpcClient.callMethod(ExampleService.getDescriptor().getMethods().get(0), 
				methodReq, ExampleResponseMessage.ExampleResponse.getDefaultInstance());
		
		try {
			assertEquals("", resp.get().getItem());
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	
	@Test(expected=TimeoutException.class)
	public void testCallTimeout() throws InterruptedException, ExecutionException, TimeoutException, BrokenBarrierException {
		final RpcServer rpcServer = new RpcServer(9697);
		final CyclicBarrier barrier = new CyclicBarrier(2);
		ExampleService service = mock(ExampleServiceImpl.class);
		when(service.getSomething(any(ExampleRequest.class))).thenAnswer(new Answer<ExampleResponse>() {
			public ExampleResponse answer(InvocationOnMock invocation) throws Throwable {
				barrier.await();
				return ExampleResponse.newBuilder().build();
			}
		});
		rpcServer.registerService(service);	
		rpcServer.start();
		
		RemoteRpcClient rpcClient = new RemoteRpcClient();
		rpcClient.setCallerId("test caller id");
		rpcClient.setReadTimeout(2, TimeUnit.MINUTES);
		rpcClient.start("127.0.0.1", 9697);
		
		ExampleService.Stub exClient = ExampleService.newStub(rpcClient);
		ExampleRequest.Builder req = ExampleRequest.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		String testId = "TESTING";
		item.setValue(testId);
		req.setItem(item);
		String something = "nothing";
		req.setSomething(something);
		
		Future<ExampleResponse> resp = exClient.getSomething(req.build());
		try {
			resp.get(1, TimeUnit.MILLISECONDS);
		} finally {
			barrier.await();
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	@Test(expected=ExecutionException.class)
	public void tesServerMethodException() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		
		AnotherService anotherService = mock(AnotherService.class);
		final String anotherResp = "another";	
		when(anotherService.doSomething(any(ExampleRequest.class))).thenAnswer(new Answer<ExampleResponse>() {
		     public ExampleResponse answer(InvocationOnMock invocation) throws Throwable {
		    	 throw new RuntimeException("nothing");
		     }
		 });
		rpcServer.registerService(anotherService);
		
		rpcServer.collectStats();
		rpcServer.start();
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		/* */
		
		ExampleRequest.Builder req = ExampleRequest.newBuilder();
		
		NestedItem.Builder item = NestedItem.newBuilder();
		String testId = "TESTING";
		item.setValue(testId);
		req.setItem(item);
		String something = "nothing";
		req.setSomething(something);
		
		AnotherService.Stub exClient2 = AnotherService.newStub(rpcClient);
		Future<ExampleResponse> resp2 = exClient2.doSomething(req.build());
		try {
			assertEquals(anotherResp, resp2.get().getItem());
		} finally {
			// make sure there is 1 error
			assertEquals(1, rpcServer.getStatsBean().getMethodCallCount().size());
			assertCallStats(rpcServer, 1L, 0L, 1L, "service.example.AnotherService.doSomething");
			
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}

	@Test
	public void testClientServerStats() throws InterruptedException, ExecutionException {
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		rpcServer.registerService(new ExampleServiceImpl());
		
		AnotherService anotherService = mock(AnotherService.class);
		final String anotherResp = "another";
		when(anotherService.doSomething(any(ExampleRequest.class)))
			.thenReturn(ExampleResponse.newBuilder().setItem(anotherResp).build());
		rpcServer.registerService(anotherService);
		
		rpcServer.collectStats();
		rpcServer.start();
		
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
		AnotherService.Stub exClient2 = AnotherService.newStub(rpcClient);
		Future<ExampleResponse> resp2 = exClient2.doSomething(req.build());
		try {
			assertEquals(something + testId, resp.get().getItem());
			assertEquals(anotherResp, resp2.get().getItem());
			// make sure there are 2 successes
			assertEquals(2, rpcServer.getStatsBean().getMethodCallCount().size());
			assertCallStats(rpcServer, 1L, 1L, 0L, "service.example.AnotherService.doSomething");	
			assertCallStats(rpcServer, 1L, 1L, 0L, "service.example.ExampleService.getSomething");
		} finally {
			rpcClient.shutdown();
			rpcServer.shutdown();
		}
	}
	
	protected void assertCallStats(RpcServer rpcServer, Long count, Long successes, Long errors, String methodName) {
		assertCallCount(rpcServer, count, methodName);
		assertSuccessCount(rpcServer, successes, methodName);
		assertErrorCount(rpcServer, errors, methodName);
	}
	
	protected void assertCallCount(RpcServer rpcServer, Long count, String methodName) {
		assertEquals(count, rpcServer.getStatsBean().getMethodCallCount().get(methodName));
	}
	
	protected void assertErrorCount(RpcServer rpcServer, Long count, String methodName) {
		assertEquals(count, rpcServer.getStatsBean().getMethodCallErrorCount().get(methodName));
	}
	
	protected void assertSuccessCount(RpcServer rpcServer, Long count, String methodName) {
		assertEquals(count, rpcServer.getStatsBean().getMethodCallSuccessCount().get(methodName));
	}
}
