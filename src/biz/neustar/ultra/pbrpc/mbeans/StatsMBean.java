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
