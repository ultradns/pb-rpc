/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc.mbeans;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class Stats implements StatsMBean {
	private static class MethodStats {
		final AtomicLong success = new AtomicLong(0);
		final AtomicLong error = new AtomicLong(0);
		public Long getTotal() { return success.get() + error.get(); }
		private static final int MAX_TIMES = 25; // only hold the last 25
		final ArrayBlockingQueue<Long> timesInMs = new ArrayBlockingQueue<Long>(MAX_TIMES);
	}
	
	private ConcurrentHashMap<String, MethodStats> methodCallStats = new ConcurrentHashMap<String, MethodStats>();
	private AtomicLong generalErrors = new AtomicLong(0);
	
	
	public static class Timing {
		private long start = 0;
		protected Timing() { start = System.currentTimeMillis(); }
		/**
		 * start timing some operation
		 * @return a Timing object that is pasted to the Stats collection of successful method calls
		 */
		public static Timing start() { return new Timing(); }
		/**
		 * stop timing
		 * @return the length of time between start and stop in ms
		 */
		public long stop() { return System.currentTimeMillis() - start; }
	}
	
	public void addGeneralError() {
		generalErrors.incrementAndGet();
	}
	
	public void addMethodCallSuccess(String methodName, Timing timing) {
		long timeInMs = timing.stop();
		MethodStats stats = new MethodStats();
		stats.success.set(1);
		stats.timesInMs.offer(timeInMs);
		MethodStats result = methodCallStats.putIfAbsent(methodName, stats);
		if (result != null) { // was already there, just update the count
			result.success.incrementAndGet();
			synchronized(result.timesInMs) { // hate to do this, but otherwise things could go wonky
				// pop off the head if it's full, we really want a ArrayNonBlockingQueue..
				if (result.timesInMs.remainingCapacity() == 0) {
					result.timesInMs.remove();
				}
				result.timesInMs.offer(timeInMs);
			}
		}
	}
	
	public void addMethodCallError(String methodName) {
		MethodStats stats = new MethodStats();
		stats.error.set(1);
		MethodStats result = methodCallStats.putIfAbsent(methodName, stats);
		if (result != null) { // was already there, just update the count
			result.error.incrementAndGet();
		}
	}
	
	@Override
	public void resetMethodCallStats(String methodName) {
		methodCallStats.remove(methodName);
	}
	
	@Override
	public void resetAllMethodCallStats() {
		methodCallStats.clear();
	}
	
	@Override
	public Map<String, Long> getMethodCallCount() {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map.Entry<String, MethodStats> entry : methodCallStats.entrySet()) {
			result.put(entry.getKey(), entry.getValue().getTotal());
		}
		return result;
	}

	@Override
	public Map<String, Long> getMethodCallSuccessCount() {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map.Entry<String, MethodStats> entry : methodCallStats.entrySet()) {
			result.put(entry.getKey(), entry.getValue().success.get());
		}
		return result;
	}

	@Override
	public Map<String, Long> getMethodCallErrorCount() {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map.Entry<String, MethodStats> entry : methodCallStats.entrySet()) {
			result.put(entry.getKey(), entry.getValue().error.get());
		}
		return result;
	}

	@Override
	public Map<String, Long> getMethodCallSlowestTimeMs() {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map.Entry<String, MethodStats> entry : methodCallStats.entrySet()) {
			result.put(entry.getKey(), getSlowestTime(entry.getValue()));
		}
		return result;
	}
	

	@Override
	public Map<String, Long> getMethodCallAverageTimeMs() {
		Map<String, Long> result = new HashMap<String, Long>();
		for (Map.Entry<String, MethodStats> entry : methodCallStats.entrySet()) {
			result.put(entry.getKey(), getAverageTime(entry.getValue()));
		}
		return result;
	}

	@Override
	public Long getGeneralErrorCount() {
		return this.generalErrors.get();
	}

	@Override
	public Long getMethodCallCount(String methodName) {
		MethodStats stats = methodCallStats.get(methodName);
		if (stats != null)
			return stats.getTotal();
		return null;
	}

	@Override
	public Long getMethodCallSuccessCount(String methodName) {
		MethodStats stats = methodCallStats.get(methodName);
		if (stats != null)
			return stats.success.get();
		return null;
	}

	@Override
	public Long getMethodCallErrorCount(String methodName) {
		MethodStats stats = methodCallStats.get(methodName);
		if (stats != null)
			return stats.success.get();
		return null;
	}

	@Override
	public Long getMethodCallSlowestTimeMs(String methodName) {
		MethodStats stats = methodCallStats.get(methodName);
		if (stats != null)
			return getSlowestTime(stats);
		return null;
	}

	@Override
	public Long getMethodCallAverageTimeMs(String methodName) {
		MethodStats stats = methodCallStats.get(methodName);
		if (stats != null)
			return getAverageTime(stats);
		return null;
	}
	
	protected long getSlowestTime(MethodStats methodStats) {
		long slowestTime = -1;
		for (long time : methodStats.timesInMs) {
			if (slowestTime == -1 || time < slowestTime) {
				slowestTime = time;
			}
		}
		return slowestTime;
	}
	
	protected long getAverageTime(MethodStats methodStats) {
		long avgTime = 0;
		long size = 0;
		for (long time : methodStats.timesInMs) {
			avgTime += time;
			size += 1;
		}
		if (size > 0)
			avgTime /= size;
		return avgTime;
	}
}
