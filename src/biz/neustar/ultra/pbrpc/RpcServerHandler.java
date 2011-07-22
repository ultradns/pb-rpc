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

package biz.neustar.ultra.pbrpc;

import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcError;
import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcPayload;
import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcRequest;
import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcResponse;
import biz.neustar.ultra.pbrpc.mbeans.Stats;

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class RpcServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(RpcServerHandler.class);
    private ServiceRegistry serviceRegistry = new ServiceRegistry();
    private PayloadHelper payloadHelper = new PayloadHelper();
    private ChannelGroup serverChannels;
    private Stats statsBean;
    
    public RpcServerHandler(ServiceRegistry serviceRegistry, 
    		ChannelGroup serverChannels, Stats statsBean) {
    	super();
    	if (serviceRegistry != null) {
    		this.serviceRegistry = serviceRegistry;
    	}
    	this.serverChannels = serverChannels;
    	this.statsBean = statsBean;
    }
    
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            //logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent msgEvent) {
    	Stats.Timing timing = Stats.Timing.start();
    	RpcRequest rpcReq = (RpcRequest) msgEvent.getMessage();
    	Service service = serviceRegistry.get(rpcReq.getServiceId());
    	
    	RpcResponse.Builder rpcResponse = RpcResponse.newBuilder()
    		.setRequestId(rpcReq.getRequestId())
    		.setTraceId(rpcReq.getTraceId());
    	
    	if (service != null) {
    		MethodDescriptor method = service.getDescriptorForType().findMethodByName(rpcReq.getMethodName());
    		Message reqMsg = service.getRequestPrototype(method);
    		
    		try {
    			Message.Builder builder = reqMsg.newBuilderForType().mergeFrom(rpcReq.getPayload().getData());
    			if (builder.isInitialized()) {
    				if (LOGGER.isDebugEnabled() && builder.getUnknownFields().asMap().size() > 0) {
    					LOGGER.debug("Unknown Fields found: {}", builder.getUnknownFields().asMap());
    				}
					Message result = service.callMethod(method, 
							builder.build());
					
					RpcPayload.Builder payload = RpcPayload.newBuilder();
					payload.setData(result.toByteString());
					payloadHelper.setCrc(payload);
					rpcResponse.setPayload(payload);
					statsBean.addMethodCallSuccess(method.getFullName(), timing);
    			} else {
    				rpcResponse.setError(errorBuilder(method.getFullName(), 
    						RpcError.Type.BAD_REQUEST, "Missing required Fields"));
    			}
			} catch (InvalidProtocolBufferException ex) {
				rpcResponse.setError(errorBuilder(method.getFullName(), 
						RpcError.Type.BAD_REQUEST, "Invalid Protocol Buffer: %s", ex));
			} catch (Exception ex) {
				rpcResponse.setError(errorBuilder(
						method.getFullName(), RpcError.Type.APPLICATION_ERROR, "Method Exception: %s", ex));
			}
    	} else {
    		rpcResponse.setError(
    				errorBuilder(RpcError.Type.BAD_REQUEST, "Service (%s) Unknown", rpcReq.getServiceId()));
    	}
    	
    	msgEvent.getChannel().write(rpcResponse.build());
    }
   
    protected RpcError errorBuilder(String methodName, RpcError.Type type, String fmt, Object... args) {
    	RpcError result = RpcError.newBuilder()
			.setType(type)
			.setMessage(String.format(fmt, args)).build();
    	statsBean.addMethodCallError(methodName);
    	return result;
    }
    
    protected RpcError errorBuilder(RpcError.Type type, String fmt, Object... args) {
    	RpcError result = RpcError.newBuilder()
			.setType(type)
			.setMessage(String.format(fmt, args)).build();
    	statsBean.addGeneralError();
    	return result;
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
    	LOGGER.warn("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
    	serverChannels.add(ctx.getChannel());
    }
}
