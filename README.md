# pb-rpc #

pb-rpc is an RPC layer for [protocol buffers](http://code.google.com/apis/protocolbuffers/docs/overview.html).

This library is aimed at simplifying steps needed to expose and use a service using protocol buffers.

## Usage ##

### Compiling proto compiler plugin ###

Environment variables to set:
PROTOBUF_LIB_DIR=<protobuf lib dir> 
PROTOBUF_INC_DIR=<protobuf include dir> 
PROTOBUF_SRC_DIR=<protobuf source dir>
<pre>  
  make -C plugin 
</pre>

### Compiling proto files ###

<pre>
protoc --java_out=distdir --java-pb-rpc_out=distdir some_service.proto
</pre>

### Server ###

#### Create a service implementation ####
```java
public class ExampleServiceImpl extends biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService {
	@Override
	public ExampleResponse getSomething(ExampleRequest request) {
    return ExampleResponse.newBuilder().setItem("testing").build();
	}
}
```

#### Register Service ####
```java
  final RpcServer rpcServer = new RpcServer(8081);
	rpcServer.registerService(new ExampleServiceImpl());
	rpcServer.collectStats(); // (optional) collect stats and expose as an MBean
	rpcServer.start();
	/// eventually.. 
	rpcServer.shutdown();
```

## Client ###

```java
  RpcClient rpcClient = (new RpcClientFactory("123")).createRpcClient("127.0.0.1", 8081);
	ExampleService.Stub exClient = ExampleService.newStub(rpcClient);
	Future<ExampleResponse> resp = exClient.getSomething(
	  ExampleRequest.newBuilder().setSomething("nothing").build());
	resp.get().getItem();
	// eventually..
	rpcClient.shutdown();
```

## Credits ##

Based on work & ideas from: protobuf-rpc-pro, capnproto, netty-protobuf-rpc

Of course the great framework that is [Netty](http://www.jboss.org/netty)

## TODO ##

- C++ service generation plugin
- C++ client & service library
- Maybe Go and/or python..

## License ##

 Copyright (c) 2011 NeuStar, Inc.
 All rights reserved.

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 
