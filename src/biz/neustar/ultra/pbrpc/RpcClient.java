/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.concurrent.Future;

import com.google.protobuf.Descriptors;
import com.google.protobuf.Message;


public abstract class RpcClient {
	private String callerId = null;
		
	public RpcClient() {
	}
	
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	
	public String getCallerId() {
		return callerId;
	}
	
	public abstract void shutdown();
	
	public abstract <T extends Message> Future<T> callMethod(final Descriptors.MethodDescriptor method, 
			final Message request,
            final T responsePrototype);
	
}
