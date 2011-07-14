/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;



public class LocalRpcServer {
	private ServiceRegistry serviceRegistry = new ServiceRegistry();


	public void registerService(Service service) {
		serviceRegistry.add(service);
	}
	
	public ServiceRegistry getServiceRegistry() {
		return serviceRegistry;
	}
	
	public synchronized void start() {
	}
	
	public synchronized void shutdown() {
	}
}
