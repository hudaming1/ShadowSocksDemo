package org.hum.socks.v5.common.codec;

import org.hum.socks.v5.common.model.ProxyPreparedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProxyPreparedMessageEncoder extends MessageToByteEncoder<ProxyPreparedMessage> {
	@Override
	protected void encode(ChannelHandlerContext ctx, ProxyPreparedMessage msg, ByteBuf out) throws Exception {
		out.writeInt(msg.getMagicNuml());
		out.writeInt(msg.getCode());
	}
}