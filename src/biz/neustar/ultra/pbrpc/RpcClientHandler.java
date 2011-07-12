/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.Future;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelEvent;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcPayload;
import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcRequest;
import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcResponse;

import com.google.protobuf.Descriptors;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;

public class RpcClientHandler extends SimpleChannelUpstreamHandler {
	private static final Logger LOGGER = LoggerFactory.getLogger(RpcClient.class);
	private volatile Channel channel;
	private PayloadHelper payloadHelper = new PayloadHelper();
	private String callerId = UUID.randomUUID().toString();
	private Random rand = new Random();
	private static final int MAX_CALLBACKS = 5000; // what's reasonable?
	// this could grow unbounded, need an LRU
	private Map<Long, com.google.protobuf.RpcCallback<RpcResponse>> callbackMap = 
		Collections.synchronizedMap(
				new LinkedHashMap<Long, com.google.protobuf.RpcCallback<RpcResponse>>(MAX_CALLBACKS, .75F, false));
	
	
	
	public void setCallerId(String callerId) {
		this.callerId = callerId;
	}
	
	
	public <T extends Message> Future<T> callMethod(Descriptors.MethodDescriptor method, 
			Message request,
            T responsePrototype) {

		final FutureCallback<T> futureCallback = new FutureCallback<T>();
		callMethod(method, request, responsePrototype, futureCallback);
		return futureCallback;
	}

	
	public <T extends Message> void callMethod(Descriptors.MethodDescriptor method, 
			Message request,
            final T responsePrototype,
            final com.google.protobuf.RpcCallback<T> done) {

		RpcRequest.Builder reqBuilder = RpcRequest.newBuilder();
		reqBuilder.setCallerId(callerId);
		reqBuilder.setMethodName(method.getName());
		// assign the request id
		reqBuilder.setRequestId(rand.nextInt());
		reqBuilder.setServiceId(method.getService().getFullName());
		RpcPayload.Builder payloadBuilder = RpcPayload.newBuilder();
		payloadBuilder.setData(request.toByteString());
		payloadHelper.setCrc(payloadBuilder);
		reqBuilder.setPayload(payloadBuilder.build());
		
		// create an adapter to convert from the RpcRequest to the actual message.
		callbackMap.put(reqBuilder.getRequestId(), 
			new com.google.protobuf.RpcCallback<RpcResponse>() {
				@SuppressWarnings("unchecked")
				@Override
				public void run(RpcResponse rpcResp) {
					try {
						done.run((T) responsePrototype.toBuilder().mergeFrom(
								rpcResp.getPayload().getData()).build());
					} catch (InvalidProtocolBufferException e) {
						throw new RuntimeException(e);
					}
				}
		});

		channel.write(reqBuilder.build());
	}
	
	@Override
    public void handleUpstream(
            ChannelHandlerContext ctx, ChannelEvent e) throws Exception {
        if (e instanceof ChannelStateEvent) {
        	LOGGER.info(e.toString());
        }
        super.handleUpstream(ctx, e);
    }

    @Override
    public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
            throws Exception {
        channel = e.getChannel();
        super.channelOpen(ctx, e);
    }

    @Override
    public void messageReceived(
            ChannelHandlerContext ctx, final MessageEvent e) {
    	
    	RpcResponse response = (RpcResponse) e.getMessage();
    	if (callbackMap.containsKey(response.getRequestId())) {
	    	com.google.protobuf.RpcCallback<RpcResponse> callback = callbackMap.get(response.getRequestId());
	    	if (callback != null && payloadHelper.verify(response.getPayload())) {
	    		callback.run(response);
	    	}
	    	callbackMap.remove(response.getRequestId());
    	}
    }

    @Override
    public void exceptionCaught(
            ChannelHandlerContext ctx, ExceptionEvent e) {
    	LOGGER.warn(
                "Unexpected exception from downstream.",
                e.getCause());
        e.getChannel().close();
    }
}
