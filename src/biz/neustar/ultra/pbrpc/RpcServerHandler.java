/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
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

import com.google.protobuf.Descriptors.MethodDescriptor;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class RpcServerHandler extends SimpleChannelUpstreamHandler {
    private static final Logger logger = LoggerFactory.getLogger(RpcServerHandler.class);
    private ServiceRegistry serviceRegistry = new ServiceRegistry();
    private PayloadHelper payloadHelper = new PayloadHelper();
    private ChannelGroup serverChannels;
    
    public RpcServerHandler(ServiceRegistry serviceRegistry, ChannelGroup serverChannels) {
    	super();
    	if (serviceRegistry != null) {
    		this.serviceRegistry = serviceRegistry;
    	}
    	this.serverChannels = serverChannels;
    }
    
    @Override
    public void handleUpstream(ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
            //logger.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
    	RpcRequest rpcReq = (RpcRequest) e.getMessage();
    	Service service = serviceRegistry.get(rpcReq.getServiceId());
    	
    	RpcResponse.Builder rpcResponse = RpcResponse.newBuilder()
    		.setRequestId(rpcReq.getRequestId())
    		.setTraceId(rpcReq.getTraceId());
    	
    	if (service != null) {
    		MethodDescriptor method = service.getDescriptorForType().findMethodByName(rpcReq.getMethodName());
    		Message reqMsg = service.getRequestPrototype(method);
    		
    		try {
				Message result = service.callMethod(method, 
						reqMsg.newBuilderForType().mergeFrom(rpcReq.getPayload().getData()).build());
				
				RpcPayload.Builder payload = RpcPayload.newBuilder();
				payload.setData(result.toByteString());
				payloadHelper.setCrc(payload);
				rpcResponse.setPayload(payload);
				
			} catch (InvalidProtocolBufferException e1) {
				RpcError.Builder error = RpcError.newBuilder()
	    			.setType(RpcError.Type.BAD_REQUEST)
	    			.setMessage(String.format("Service (%s) Unknown", rpcReq.getServiceId()));
	    		rpcResponse.setError(error);			
			}
    	} else {
    		RpcError.Builder error = RpcError.newBuilder()
    			.setType(RpcError.Type.BAD_REQUEST)
    			.setMessage(String.format("Service (%s) Unknown", rpcReq.getServiceId()));
    		rpcResponse.setError(error);
    	}
    	e.getChannel().write(rpcResponse.build());
    }
   

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
        logger.warn("Unexpected exception from downstream.", e.getCause());
        e.getChannel().close();
    }

    @Override
    public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
    	serverChannels.add(ctx.getChannel());
    }
}
