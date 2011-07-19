/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import static org.jboss.netty.channel.Channels.pipeline;

import java.util.concurrent.TimeUnit;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import org.jboss.netty.handler.timeout.ReadTimeoutHandler;
import org.jboss.netty.util.HashedWheelTimer;
import org.jboss.netty.util.Timer;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcResponse;

public class ProtobufClientPipelineFactory implements ChannelPipelineFactory {
	private long readTimeout = 30;
	private TimeUnit readTimeoutUnit = TimeUnit.SECONDS;

	public ProtobufClientPipelineFactory(long readTimeout, TimeUnit readTimeoutUnit) {
		this.readTimeout = readTimeout;
		this.readTimeoutUnit = readTimeoutUnit;
	}
	
	/**
	 * defaults to 30 second read timeout.
	 */
	public ProtobufClientPipelineFactory() {
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = pipeline();
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(RpcResponse.getDefaultInstance()));

        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        Timer timer = new HashedWheelTimer();
        p.addLast("timeout", new ReadTimeoutHandler(timer, readTimeout, readTimeoutUnit));
        p.addLast("rpcHandler", new RpcClientHandler());

        return p;
	}

}
