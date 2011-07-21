/**
 * Copyright 2000-2011 NeuStar, Inc. All rights reserved.
 * NeuStar, the Neustar logo and related names and logos are registered
 * trademarks, service marks or tradenames of NeuStar, Inc. All other
 * product names, company names, marks, logos and symbols may be trademarks
 * of their respective owners.
 */

package biz.neustar.ultra.pbrpc;

import static org.jboss.netty.channel.Channels.pipeline;

import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.protobuf.ProtobufDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufEncoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import org.jboss.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;

import biz.neustar.ultra.pbrpc.generated.RpcMessage.RpcRequest;
import biz.neustar.ultra.pbrpc.mbeans.Stats;

public class ProtobufServerPipelineFactory implements ChannelPipelineFactory {
	private ServiceRegistry serviceRegistry = new ServiceRegistry();
	private ChannelGroup serverChannels;
	private Stats statsBean;
	
	public ProtobufServerPipelineFactory(ServiceRegistry serviceRegistry, 
			ChannelGroup serverChannels, Stats statsBean) {
		this.serviceRegistry = serviceRegistry;
		this.serverChannels = serverChannels;
		this.statsBean = statsBean;
	}
	
	@Override
	public ChannelPipeline getPipeline() throws Exception {
		ChannelPipeline p = pipeline();
        p.addLast("frameDecoder", new ProtobufVarint32FrameDecoder());
        p.addLast("protobufDecoder", new ProtobufDecoder(RpcRequest.getDefaultInstance()));

        p.addLast("frameEncoder", new ProtobufVarint32LengthFieldPrepender());
        p.addLast("protobufEncoder", new ProtobufEncoder());
        p.addLast("rpcHandler", new RpcServerHandler(serviceRegistry, serverChannels, statsBean));
        return p;
	}

}
