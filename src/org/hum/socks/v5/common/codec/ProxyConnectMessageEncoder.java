package org.hum.socks.v5.common.codec;

import org.hum.socks.v5.common.model.ProxyConnectMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProxyConnectMessageEncoder extends MessageToByteEncoder<ProxyConnectMessage> {

	@Override
	protected void encode(ChannelHandlerContext ctx, ProxyConnectMessage message, ByteBuf out) throws Exception {
		out.writeInt(message.getMagicNum());
		out.writeInt(message.getHostLen());
		out.writeBytes(message.getHost().getBytes());
		out.writeShort(message.getPort());
	}
}
