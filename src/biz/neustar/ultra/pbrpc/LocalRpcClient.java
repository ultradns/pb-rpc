/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcResponse;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;

public class LocalRpcClient extends RpcClient {
	private LocalRpcServer rpcServer;
	private ExecutorService executor = Executors.newCachedThreadPool();
	
	@Override
	public void shutdown() {
		executor.shutdownNow();
	}

	@Override
	public <T extends Message> Future<T> callMethod(final MethodDescriptor method,
			final Message request, final T responsePrototype) {

		return executor.submit(new Callable<T>(){
			@SuppressWarnings("unchecked")
			@Override
			public T call() throws Exception {
				try {
					Service service = rpcServer.getServiceRegistry().get(method.getService().getFullName());
					return (T) service.callMethod(method, request);
				} catch (Throwable ex) {
					throw new ExecutionException(ex);
				}
			}
		});
	}

	public void setLocalRpcServer(LocalRpcServer rpcServer) {
		this.rpcServer = rpcServer;
	}
}
