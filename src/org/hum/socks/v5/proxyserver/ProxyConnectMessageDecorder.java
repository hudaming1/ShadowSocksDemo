package org.hum.socks.v5.proxyserver;

import java.util.List;

import org.hum.socks.v5.common.Constant;
import org.hum.socks.v5.common.ProxyConnectMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class ProxyConnectMessageDecorder extends ByteToMessageDecoder {

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		ProxyConnectMessage message = new ProxyConnectMessage();
		message.setMagicNum(in.readInt());
		// XXX 如果这里不做判断，不知道为什么，会收到奇怪的消息（总之必须应该不是local-server发送过来的），所以我只能判断并丢弃了。
		if (message.getMagicNum() != Constant.MAGIC_NUMBER) {
			return ;
		}
		message.setHostLen(in.readInt());
		byte[] bytes = new byte[message.getHostLen()];
		in.readBytes(bytes);
		message.setHost(new String(bytes));
		message.setPort(in.readShort());
		System.out.println("receive local-server message: " + message);
		out.add(message);
	}
}
