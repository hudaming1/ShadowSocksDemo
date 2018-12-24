package org.hum.socks.v6.io.codec;

import java.util.List;

import org.hum.socks.v6.common.Constant;
import org.hum.socks.v6.io.codec.model.ProxyPreparedMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProxyPreparedMessageCodec {

	public static class ProxyPreparedMessageDecoder extends ByteToMessageDecoder {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			ProxyPreparedMessage msg = new ProxyPreparedMessage();
			int magicNumber = in.readInt();
			if (magicNumber != Constant.MAGIC_NUMBER) {
				return;
			}
			msg.setMagicNuml(magicNumber);
			msg.setCode(in.readInt());
			out.add(msg);
		}
	}

	public static class ProxyPreparedMessageEncoder extends MessageToByteEncoder<ProxyPreparedMessage> {
		@Override
		protected void encode(ChannelHandlerContext ctx, ProxyPreparedMessage msg, ByteBuf out) throws Exception {
			out.writeInt(msg.getMagicNuml());
			out.writeInt(msg.getCode());
		}
	}
}
