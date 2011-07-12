/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pb-rpc;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.jboss.netty.channel.local.DefaultLocalClientChannelFactory;
import org.jboss.netty.channel.local.DefaultLocalServerChannelFactory;
import org.jboss.netty.channel.local.LocalAddress;
import org.junit.Before;
import org.junit.Test;

import biz.neustar.ultra.service.ZoneSearchCriteriaMessage.ZoneSearchCriteria;
import biz.neustar.ultra.service.zvs.GetZonesRequestMessage.GetZonesRequest;
import biz.neustar.ultra.service.zvs.GetZonesResponseMessage.GetZonesResponse;

import static org.junit.Assert.*;

public class RpcClientServerTest {


	@Test
	public void testClientServerPath() throws InterruptedException, ExecutionException {
		ArrayList<SocketAddress> serverAddresses = new ArrayList<SocketAddress>();
		
		LocalAddress address = new LocalAddress("1");
		serverAddresses.add(address);
		final RpcServer rpcServer = new RpcServer(serverAddresses);
		rpcServer.setChannelFactory(new DefaultLocalServerChannelFactory());
		
		rpcServer.registerService(new ZVSImpl());
	
		rpcServer.start();
		Thread.sleep(100);
		
		RpcClient rpcClient = (new RpcClientFactory("test caller id")).createRpcClient(
				new DefaultLocalClientChannelFactory(), address);
		
		/* */
		biz.neustar.ultra.service.zvs.ZoneVersionServiceMessage.ZoneVersionService.Stub zvsClient = 
			biz.neustar.ultra.service.zvs.ZoneVersionServiceMessage.ZoneVersionService.newStub(rpcClient);
		
		GetZonesRequest.Builder req = GetZonesRequest.newBuilder();
		ZoneSearchCriteria.Builder srchCriteria = ZoneSearchCriteria.newBuilder();
		String testId = "TESTING";
		srchCriteria.setAccountId(testId);
		req.setCriteria(srchCriteria);
		Future<GetZonesResponse> resp = zvsClient.getZones(req.build());
		assertEquals(1, resp.get().getZoneCount());
		assertEquals(testId, resp.get().getZone(0).getAccountId());
		/* */
		
		rpcClient.shutdown();
		rpcServer.shutdown();
	}
}
