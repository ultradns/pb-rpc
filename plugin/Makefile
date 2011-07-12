

PRODUCT=protoc-gen-java-pb-rpc
FLAGS+=-O2 -Wall -I. -I$(PROTOBUF_INC_DIR) 

ifdef PROTOBUF_SRC_DIR
 FLAGS+= -I$(PROTOBUF_SRC_DIR)/src/google/protobuf/compiler/java
 FLAGS+= -I$(PROTOBUF_SRC_DIR)/src/google/protobuf/stubs
else
 FLAGS+= -Iinternal
endif


LDFLAGS+=-L$(PROTOBUF_LIB_DIR) -lprotobuf -lprotoc

SRC:=$(patsubst ./%,%,$(shell find . -name "*.cpp"))
OBJS=$(SRC:.cpp=.o)

all: prereqs $(PRODUCT) 

%.o : %.cpp
	@-mkdir -p $(dir $@)
	$(CXX) $(FLAGS) $(INCPATH) -c $< -o $@

$(PRODUCT) : $(OBJS) force
	$(CXX) $(OBJS) -o $(PRODUCT) $(LDFLAGS)
	cp $(PRODUCT) protoc-gen-cpp-pb-rpc
	cp $(PRODUCT) protoc-gen-py-pb-rpc

prereqs: force
ifndef PROTOBUF_LIB_DIR
	$(error "define: PROTOBUF_LIB_DIR")
endif
ifndef PROTOBUF_INC_DIR
	$(error "define: PROTOBUF_INC_DIR")
endif


clean: force
	-rm -rf *.o $(PRODUCT)

force:

.phony : force
