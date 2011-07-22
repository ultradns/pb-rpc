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

#ifndef GOOGLE_PROTOBUF_COMPILER_JAVA_SERVICE_H__
#define GOOGLE_PROTOBUF_COMPILER_JAVA_SERVICE_H__

#include <map>
#include <google/protobuf/descriptor.h>

namespace google {
namespace protobuf {
namespace io {
  class Printer;             // printer.h
}
}
}

namespace pbrpc {
namespace java {
  

class ServiceGenerator {
 public:
  explicit ServiceGenerator(const google::protobuf::ServiceDescriptor* descriptor);
  ~ServiceGenerator();

  void Generate(google::protobuf::io::Printer* printer);

 private:

  void GenerateGetId(google::protobuf::io::Printer* printer);

  // Generate the getDescriptorForType() method.
  void GenerateGetDescriptorForType(google::protobuf::io::Printer* printer);

  void GenerateNonBlockingInterface(google::protobuf::io::Printer* printer);

  // Generate abstract method declarations for all methods.
  void GenerateAbstractMethods(google::protobuf::io::Printer* printer);

  // Generate the implementation of Service.callMethod().
  void GenerateCallMethod(google::protobuf::io::Printer* printer);

  // Generate the implementation of BlockingService.callBlockingMethod().
  void GenerateCallBlockingMethod(google::protobuf::io::Printer* printer);

  // Generate the implementations of Service.get{Request,Response}Prototype().
  enum RequestOrResponse { REQUEST, RESPONSE };
  void GenerateGetPrototype(RequestOrResponse which, google::protobuf::io::Printer* printer);

  // Generate a stub implementation of the service.
  void GenerateStub(google::protobuf::io::Printer* printer);

  // Generate a method signature, possibly abstract, without body or trailing
  // semicolon.
  enum IsAbstract { IS_ABSTRACT, IS_CONCRETE };
  void GenerateMethodSignature(google::protobuf::io::Printer* printer,
                               const google::protobuf::MethodDescriptor* method,
                               IsAbstract is_abstract);
                               
  void GenerateClientCallbackMethodSignature(google::protobuf::io::Printer* printer,
                                             const google::protobuf::MethodDescriptor* method,
                                             IsAbstract is_abstract);
                                             
  void GenerateClientFutureMethodSignature(google::protobuf::io::Printer* printer,
                                           const google::protobuf::MethodDescriptor* method,
                                           IsAbstract is_abstract);



  const google::protobuf::ServiceDescriptor* descriptor_;

  GOOGLE_DISALLOW_EVIL_CONSTRUCTORS(ServiceGenerator);
};

}  // namespace rpc
}  // namespace pb

#endif  // NET_PROTO2_COMPILER_JAVA_SERVICE_H__


