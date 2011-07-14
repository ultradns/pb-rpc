/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ServiceRegistry {
	// <Service Id / Service FullName> to <Service> map
	private Map<String, Service> serviceMap = new ConcurrentHashMap<String, Service>();

	public Service get(String serviceId) {
		return serviceMap.get(serviceId);
	}
	
	public void add(Service service) {
		serviceMap.put(service.getId(), service);
	}
	
	public void remove(Service service) {
		serviceMap.remove(service.getId());
	}
}
