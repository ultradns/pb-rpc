/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

public interface Service {

	java.lang.String getId();
	
	com.google.protobuf.Descriptors.ServiceDescriptor getDescriptorForType();
 
	
	com.google.protobuf.Message callMethod(com.google.protobuf.Descriptors.MethodDescriptor method,
			com.google.protobuf.Message request);
	
	com.google.protobuf.Message getRequestPrototype(
			com.google.protobuf.Descriptors.MethodDescriptor method);
	
	com.google.protobuf.Message getResponsePrototype(
			com.google.protobuf.Descriptors.MethodDescriptor method);
}
