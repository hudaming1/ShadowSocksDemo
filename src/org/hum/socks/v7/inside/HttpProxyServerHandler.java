package org.hum.socks.v7.inside;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		String host = HttpUtil.parseHost(msg.uri());
		short port = HttpUtil.parsePort(msg.uri());
		System.out.println("====================" + host + ":" + port + "==========================");
		System.out.println(msg);
		if (msg.method() == HttpMethod.CONNECT) {
			ctx.channel().writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection established\r\n\r\n".getBytes()));
		}
	}
}

class HttpUtil {
	public static String parseHost(String uri) {
		return uri.split(":")[0];
	}
	
	public static short parsePort(String uri) {
		String[] sArr = uri.split(":");
		if (sArr.length == 1) {
			return 80;
		}
		return Short.parseShort(uri.split(":")[1]);
	}
}