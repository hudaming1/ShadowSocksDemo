package org.hum.socks.v5.common.codec;

import java.util.List;

import org.hum.socks.v5.common.Constant;
import org.hum.socks.v5.common.model.ProxyPreparedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ProxyPreparedMessageDecoder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		ProxyPreparedMessage msg = new ProxyPreparedMessage();
		int magicNumber = in.readInt();
		if (magicNumber != Constant.MAGIC_NUMBER) {
			return ;
		}
		msg.setMagicNuml(magicNumber);
		msg.setCode(in.readInt());
		out.add(msg);
	}
}
