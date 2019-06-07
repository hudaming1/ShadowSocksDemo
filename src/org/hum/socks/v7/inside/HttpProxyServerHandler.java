package org.hum.socks.v7.inside;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpRequestEncoder;

public class HttpProxyServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		String host = HttpUtil.parseHost(msg.uri());
		short port = HttpUtil.parsePort(msg.uri());
		System.out.println("====================" + host + ":" + port + "==========================");
		if (msg.method() == HttpMethod.CONNECT) {
			ctx.channel().writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection established\r\n\r\n".getBytes()));
		}
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(ctx.channel().eventLoop());
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				// ch.pipeline().addLast(new PrepareConnectChannelHandler(ctx));
			}
		});
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		bootstrap.connect("localhost", 5433).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture proxyServerChannelFuture) throws Exception {
				// 将ip和port输出到proxy-server
				proxyServerChannelFuture.channel().pipeline().addLast(new HttpRequestEncoder());
				proxyServerChannelFuture.channel().writeAndFlush(msg).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						
					}
				});
			}
		});
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