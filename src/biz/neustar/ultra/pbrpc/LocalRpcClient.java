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

import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

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
