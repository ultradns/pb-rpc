
/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */
package biz.neustar.ultra;

import static org.junit.Assert.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.junit.Test;

import com.google.protobuf.ByteString;

import biz.neustar.ultra.Rpc.RpcPayload;
import biz.neustar.ultra.Rpc.RpcRequest.Builder;

public class BigRpcTest {

	@Test
	public void runBigMessage() throws IOException {
		// todo generate 65 mb of data
		byte singleByte = (byte) 0xEF;
		ByteString.Output payloadValue = ByteString.newOutput();
		for (int i = 0; i < 68157440; i++) {
			payloadValue.write(singleByte);
		}
		System.out.println("Done generating 65 MB of data");
		
		RpcPayload.Builder payload = RpcPayload.newBuilder();
		payload.setData(payloadValue.toByteString());
		
		Builder msg = Rpc.RpcRequest.newBuilder();
		msg.setPayload(payload);
		
		Rpc.RpcRequest rpcRequest = msg.build();
		ByteArrayOutputStream serializedOutput = new ByteArrayOutputStream();
		rpcRequest.writeTo(serializedOutput);
		///// now deserialize
		Rpc.RpcRequest.Builder deserializer = Rpc.RpcRequest.newBuilder();
		deserializer.mergeFrom(serializedOutput.toByteArray());
		Rpc.RpcRequest rpcRequestOut = deserializer.build();
		assertNotNull(rpcRequestOut);
		assertEquals(68157440, rpcRequestOut.getPayload().getData().size());
	}
}
