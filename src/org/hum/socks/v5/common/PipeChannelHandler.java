package org.hum.socks.v5.common;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class PipeChannelHandler extends ChannelInboundHandlerAdapter {
	
	private String name;
	private Channel pipeChannel;
	
	public PipeChannelHandler(String name, Channel channel) {
		this.name = name;
		this.pipeChannel = channel;
	}

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
    		if (pipeChannel.isActive()) {
    			System.out.println(name + " transfer message[" + System.nanoTime() + "]");
    			pipeChannel.writeAndFlush(msg);
    		}
    }
}
