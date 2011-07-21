/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc.mbeans;

/**
 * no-op version of the stats module.
 * 
 */
public class StatsNoOp extends Stats {

	public void addGeneralError() { /* no-op */ }
	
	public void addMethodCallSuccess(String methodName) { /* no-op */ }
	
	public void addMethodCallError(String methodName) { /* no-op */ }
	
	public void resetMethodCallStats(String methodName) { /* no-op */ }

}
