package org.hum.socks.v6.common;

import java.util.Arrays;

import org.hum.socks.v6.common.model.FullByteMessage;
import org.hum.socks.v6.common.util.Utils;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

public class DecryptPipeChannelHandler2 extends SimpleChannelInboundHandler<FullByteMessage> {

	@SuppressWarnings("unused")
	private String name;
	private Channel pipeChannel;

	public DecryptPipeChannelHandler2(String name, Channel channel) {
		this.name = name;
		this.pipeChannel = channel;
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullByteMessage msg) throws Exception {
		if (pipeChannel.isActive()) {
			byte[] decrypt = Utils.decrypt(msg.datas);
			System.out.println("[dec][channel" + ctx.hashCode() + "][" + decrypt.length + "]" + Arrays.toString(decrypt));
			pipeChannel.writeAndFlush(Unpooled.wrappedBuffer(decrypt));
		}
	}
}
