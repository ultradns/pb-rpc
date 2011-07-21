/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc.mbeans;

import java.util.Map;

public interface StatsMBean {
	Map<String, Long> getMethodCallCount();
	Map<String, Long> getMethodCallSuccessCount();
	Map<String, Long> getMethodCallErrorCount();
	Map<String, Long> getMethodCallSlowestTimeMs();
	Map<String, Long> getMethodCallAverageTimeMs();
	
	Long getMethodCallCount(String methodName);
	Long getMethodCallSuccessCount(String methodName);
	Long getMethodCallErrorCount(String methodName);
	Long getMethodCallSlowestTimeMs(String methodName);
	Long getMethodCallAverageTimeMs(String methodName);
	
	void resetMethodCallStats(String methodName);
	void resetAllMethodCallStats();
	
	Long getGeneralErrorCount();
}
