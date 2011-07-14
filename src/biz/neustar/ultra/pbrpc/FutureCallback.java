/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.google.protobuf.RpcCallback;

public class FutureCallback<T> implements Future<T>, RpcCallback<T> {
	private volatile boolean done = false;
	private ArrayBlockingQueue<T> value = new ArrayBlockingQueue<T>(1);
	private volatile ExecutionException executionException = null;
	
	public void setExecutionException(ExecutionException executionException) {
		if (executionException != null) {
			this.executionException = executionException;
			if (value.isEmpty()) {
				run(null); // release anyone waiting
			}
		}
	}
	
	@Override
	public void run(T parameter) {
		value.offer(parameter);
		done = true;
	}

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	
	protected void checkForExecException() throws ExecutionException {
		ExecutionException execException = executionException;
		if (execException != null) {
			throw execException;
		}
	}

	@Override
	public T get() throws InterruptedException, ExecutionException {
		checkForExecException();
		synchronized(this) {
			T val = value.take();
			value.offer(val);
			checkForExecException();
			return val;
		}
	}

	/**
	 * @returns the value or null if the specified waiting time elapses before an element is available
	 */
	@Override
	public T get(long timeout, TimeUnit unit) 
			throws InterruptedException, ExecutionException, TimeoutException {
		checkForExecException();
		synchronized(this) {
			T val = value.poll(timeout, unit);
			value.offer(val);
			checkForExecException();
			return val;
		}
	}
}
