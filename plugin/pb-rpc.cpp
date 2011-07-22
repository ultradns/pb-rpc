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

#include <string>
#include <vector>
#include <tr1/memory>
#include <google/protobuf/compiler/code_generator.h>
#include <google/protobuf/descriptor.h>
#include <google/protobuf/compiler/plugin.h>
#include <google/protobuf/stubs/common.h>

#include <google/protobuf/io/printer.h>
#include <google/protobuf/io/zero_copy_stream.h>

/// java
#include <google/protobuf/compiler/java/java_generator.h>
#include "java_helpers.h"
#include "strutil.h"

#include "java_service.h"


using google::protobuf::FileDescriptor;
using google::protobuf::compiler::GeneratorContext;
using google::protobuf::compiler::ParseGeneratorParameter;
using google::protobuf::internal::scoped_ptr;

using std::string;
using std::vector;
using std::pair;

using google::protobuf::compiler::java::JavaPackageToDir;
using google::protobuf::compiler::java::FileJavaPackage;
using google::protobuf::compiler::java::FileClassName;

namespace pbrpc {
  

class PbRpcService : public google::protobuf::compiler::CodeGenerator {
 public:
   enum Language {
     NONE,
     JAVA,
     CPP,
     PYTHON
   };

  PbRpcService(Language language) : language_(language) {}
  
  
  bool Generate(const FileDescriptor* file,
                const std::string& parameter,
                GeneratorContext* generator_context,
                std::string* error) const;
                
  void GenerateJavaServiceInsertion(const FileDescriptor* file,
                                    GeneratorContext* generator_context) const;
                                    
 protected:
   Language language_;
};


void PbRpcService::GenerateJavaServiceInsertion(const FileDescriptor* file, 
                                                GeneratorContext* generator_context) const {
                                                  
  // for right now only support services in java
  std::string package_dir = JavaPackageToDir(FileJavaPackage(file));

  std::vector<string> all_files;

  std::string java_filename = package_dir;
  java_filename += FileClassName(file);
  java_filename += ".java";
  fprintf(stderr, "Found: %s\n", java_filename.c_str());
  
  scoped_ptr<google::protobuf::io::ZeroCopyOutputStream> output(
    generator_context->OpenForInsert(java_filename, "outer_class_scope"));
  google::protobuf::io::Printer printer(output.get(), '$');

  for (int index = 0; index < file->service_count(); ++index) {
    // output java service definition
    java::ServiceGenerator serviceGenerator(file->service(index));
    serviceGenerator.Generate(&printer);
  }
}


bool PbRpcService::Generate(const FileDescriptor* file,
                            const std::string& parameter,
                            GeneratorContext* generator_context,
                            std::string* error) const {

  if (file->service_count() > 0) {
    
    switch (language_) {
      case JAVA:
        fprintf(stderr, "Generating Java Service\n");
        GenerateJavaServiceInsertion(file, generator_context);
        break;
      case CPP:
        fprintf(stderr, "Generating C++ Service (not supported yet)\n");
        break;
      case PYTHON:
        fprintf(stderr, "Generating Python Service (not supported yet)\n");
        break;
      default:
        break;
    }

#ifdef DEBUG    
    vector<pair<string, string> > options;
    ParseGeneratorParameter(parameter, &options);
    fprintf(stderr, "options.size: %lu\n", options.size());
    for (uint32_t i = 0; i < options.size(); i++) {
      fprintf(stderr, "first: %s\n", options[i].first.c_str());
      fprintf(stderr, "second: %s\n", options[i].second.c_str());
    }
#endif
  }
  return true;
}

} // end namespace pbrpc

int main(int argc, char* argv[]) {
  
  const char kJavaPbRpc[] = "java-pb-rpc";
  const char kCppPbRpc[] = "cpp-pb-rpc";
  const char kPythonPbRpc[] = "py-pb-rpc";
  
  pbrpc::PbRpcService::Language lang = pbrpc::PbRpcService::NONE;
  if (google::protobuf::HasSuffixString(argv[0], kJavaPbRpc)) {
    lang = pbrpc::PbRpcService::JAVA;
  } else if (google::protobuf::HasSuffixString(argv[0], kCppPbRpc)) {
    lang = pbrpc::PbRpcService::CPP;
  } else if (google::protobuf::HasSuffixString(argv[0], kPythonPbRpc)) {
    lang = pbrpc::PbRpcService::PYTHON;
  }
  
  pbrpc::PbRpcService generator(lang);
  return google::protobuf::compiler::PluginMain(argc, argv, &generator);
}
