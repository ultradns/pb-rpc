// Generated by the protocol buffer compiler.  DO NOT EDIT!
// source: example_service.proto

package biz.neustar.ultra.service.example;

public final class ExampleServiceMessage {
  private ExampleServiceMessage() {}
  public static void registerAllExtensions(
      com.google.protobuf.ExtensionRegistry registry) {
  }
  
  public static com.google.protobuf.Descriptors.FileDescriptor
      getDescriptor() {
    return descriptor;
  }
  private static com.google.protobuf.Descriptors.FileDescriptor
      descriptor;
  static {
    java.lang.String[] descriptorData = {
      "\n\025example_service.proto\022\017service.example" +
      "\032\025example_request.proto\032\026example_respons" +
      "e.proto2c\n\016ExampleService\022Q\n\014getSomethin" +
      "g\022\037.service.example.ExampleRequest\032 .ser" +
      "vice.example.ExampleResponseB@\n!biz.neus" +
      "tar.ultra.service.exampleB\025ExampleServic" +
      "eMessage\200\001\000\210\001\000"
    };
    com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner assigner =
      new com.google.protobuf.Descriptors.FileDescriptor.InternalDescriptorAssigner() {
        public com.google.protobuf.ExtensionRegistry assignDescriptors(
            com.google.protobuf.Descriptors.FileDescriptor root) {
          descriptor = root;
          return null;
        }
      };
    com.google.protobuf.Descriptors.FileDescriptor
      .internalBuildGeneratedFileFrom(descriptorData,
        new com.google.protobuf.Descriptors.FileDescriptor[] {
          biz.neustar.ultra.service.example.ExampleRequestMessage.getDescriptor(),
          biz.neustar.ultra.service.example.ExampleResponseMessage.getDescriptor(),
        }, assigner);
  }
  
  public static abstract class ExampleService
      implements biz.neustar.ultra.pbrpc.Service {
    protected ExampleService() {}
    
    public abstract biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse getSomething(
        biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest request);
    
    public static final
        com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptor() {
      return biz.neustar.ultra.service.example.ExampleServiceMessage.getDescriptor().getServices().get(0);
    }
    public final com.google.protobuf.Descriptors.ServiceDescriptor
        getDescriptorForType() {
      return getDescriptor();
    }
    public final String getId() {
      	return getDescriptorForType().getFullName();
    }
    
    public final com.google.protobuf.Message callMethod(
        com.google.protobuf.Descriptors.MethodDescriptor method,
        com.google.protobuf.Message request) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.callMethod() given method descriptor for wrong " +
          "service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return this.getSomething((biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest)request);
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public final com.google.protobuf.Message
        getRequestPrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.getRequestPrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest.getDefaultInstance();
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public final com.google.protobuf.Message
        getResponsePrototype(
        com.google.protobuf.Descriptors.MethodDescriptor method) {
      if (method.getService() != getDescriptor()) {
        throw new java.lang.IllegalArgumentException(
          "Service.getResponsePrototype() given method " +
          "descriptor for wrong service type.");
      }
      switch(method.getIndex()) {
        case 0:
          return biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse.getDefaultInstance();
        default:
          throw new java.lang.AssertionError("Can't get here.");
      }
    }
    
    public interface NonBlockingInterface {
      public abstract void getSomething(
          biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest request,
          com.google.protobuf.RpcCallback<biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse> done);
      
      public abstract java.util.concurrent.Future<biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse> getSomething(
          biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest request);
    }
    
    public static Stub newStub(
        biz.neustar.ultra.pbrpc.RpcClient client) {
      return new Stub(client);
    }
    
    public static final class Stub implements biz.neustar.ultra.service.example.ExampleServiceMessage.ExampleService.NonBlockingInterface {
      private Stub(biz.neustar.ultra.pbrpc.RpcClient client) {
        this.client = client;
      }
      
      private final biz.neustar.ultra.pbrpc.RpcClient client;
      
      public biz.neustar.ultra.pbrpc.RpcClient getClient() {
        return client;
      }
      
      public  void getSomething(
          biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest request,
          com.google.protobuf.RpcCallback<biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse> done) {
        client.getClientHandler().callMethod(
          getDescriptor().getMethods().get(0),
          request,
          biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse.getDefaultInstance(),
          done);
      }
      
      public  java.util.concurrent.Future<biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse> getSomething(
          biz.neustar.ultra.service.example.ExampleRequestMessage.ExampleRequest request) {
        return client.getClientHandler().callMethod(
          getDescriptor().getMethods().get(0),
          request,
          biz.neustar.ultra.service.example.ExampleResponseMessage.ExampleResponse.getDefaultInstance());
      }
    }
    
  }
  
  // @@protoc_insertion_point(outer_class_scope)
}
