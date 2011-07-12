/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest;
import biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse;


public class ExampleServiceImpl extends biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService {

	@Override
	public ExampleResponse getSomething(ExampleRequest request) {
		ExampleResponse.Builder resp = ExampleResponse.newBuilder();
		resp.setItem(request.getSomething() + request.getItem().getValue());
		return resp.build();
	}

}
