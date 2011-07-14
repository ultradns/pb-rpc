/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.Message;
import com.google.protobuf.RpcCallback;

public class LocalRpcClient extends RpcClient {
	private LocalRpcServer rpcServer;
	@Override
	public void shutdown() {
		// TODO Auto-generated method stub
	}

	@Override
	public <T extends Message> Future<T> call(MethodDescriptor method,
			Message request, T responsePrototype) {

		final FutureCallback<T> futureCallback = new FutureCallback<T>();
		try {
			call(method, request, responsePrototype, futureCallback);
		} catch (Throwable ex) {
			futureCallback.setExecutionException(new ExecutionException(ex));
		}
		return futureCallback;
	}

	@Override
	public <T extends Message> void call(final MethodDescriptor method,
			final Message request, final T responsePrototype, final RpcCallback<T> done) {
		Thread rpcCall = new Thread(new Runnable() {
			@Override
			public void run() {
				Service service = rpcServer.getServiceRegistry().get(method.getService().getFullName());
				@SuppressWarnings("unchecked")
				T response = (T) service.callMethod(method, request);
				done.run(response);
			}
		});
		rpcCall.start();
	}

	public void setLocalRpcServer(LocalRpcServer rpcServer) {
		this.rpcServer = rpcServer;
	}
}
