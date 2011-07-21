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

public abstract class FutureCallback<T, P> implements Future<T> {
	private volatile boolean done = false;
	private volatile boolean cancelled = false;
	
	private static class Result<V> {
		Result(V value) {
			this.value = value;
		}
		Result(ExecutionException executionException) {
			this.executionException = executionException;
		}
		V value = null;
		ExecutionException executionException = null;
	}
	
	private ArrayBlockingQueue<Result<T>> value = new ArrayBlockingQueue<Result<T>>(1);
	
	
	public abstract T run(P parameter) throws ExecutionException;
	
	protected void process(P parameter) {
		Result<T> result = new Result<T>(new ExecutionException(new RuntimeException("Unknown Problem")));
		try {
			result = new Result<T>(run(parameter));
		} catch (ExecutionException executionException) {
			result = new Result<T>(executionException);
		} finally {
			done = true;
			value.offer(result);
		}
	}

	
	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public boolean isDone() {
		return done;
	}
	

	@Override
	public T get() throws InterruptedException, ExecutionException {
		Result<T> result = value.take();
		value.offer(result);
		if (result.executionException != null) {
			throw result.executionException;
		}
		return result.value;
	}

	/**
	 * @returns the value or null if no result is available
	 */
	@Override
	public T get(long timeout, TimeUnit unit) 
			throws InterruptedException, ExecutionException, TimeoutException {		
		Result<T> result = value.poll(timeout, unit);
		if (result == null) {
			throw new TimeoutException();
		}
		value.offer(result);
		if (result.executionException != null) {
			throw result.executionException;
		}
		return result.value;
	}
}
