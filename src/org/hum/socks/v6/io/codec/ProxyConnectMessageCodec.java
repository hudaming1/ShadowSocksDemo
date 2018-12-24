package org.hum.socks.v6.io.codec;

import java.util.List;

import org.hum.socks.v6.common.Constant;
import org.hum.socks.v6.io.codec.model.ProxyConnectMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.MessageToByteEncoder;

public class ProxyConnectMessageCodec {

	public static class ProxyConnectMessageDecorder extends ByteToMessageDecoder {

		@Override
		protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
			ProxyConnectMessage message = new ProxyConnectMessage();
			message.setMagicNum(in.readInt());
			// XXX 如果这里不做判断，不知道为什么，会收到奇怪的消息（总之必须应该不是local-server发送过来的），所以我只能判断并丢弃了。
			// 好像是因为异步的原因，已经通过ack机制解决了这个问题
			if (message.getMagicNum() != Constant.MAGIC_NUMBER) {
				return;
			}
			message.setHostLen(in.readInt());
			byte[] bytes = new byte[message.getHostLen()];
			in.readBytes(bytes);
			message.setHost(new String(bytes));
			message.setPort(in.readShort());
			out.add(message);
		}
	}

	public static class ProxyConnectMessageEncoder extends MessageToByteEncoder<ProxyConnectMessage> {

		@Override
		protected void encode(ChannelHandlerContext ctx, ProxyConnectMessage message, ByteBuf out) throws Exception {
			out.writeInt(message.getMagicNum());
			out.writeInt(message.getHostLen());
			out.writeBytes(message.getHost().getBytes());
			out.writeShort(message.getPort());
		}
	}

}
