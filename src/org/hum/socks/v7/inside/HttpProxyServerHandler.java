package org.hum.socks.v7.inside;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		System.out.println("uri:" + msg.uri());
		System.out.println(msg);
		System.out.println("======================================================");
		if (msg.method() == HttpMethod.CONNECT) {
			// ctx.channel().writeAndFlush(new DefaultHttpResponse(msg.protocolVersion(), HttpResponseStatus.OK));
			ctx.channel().writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection established\r\n\r\n".getBytes()));
		}
	}
}
