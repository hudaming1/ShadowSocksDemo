package org.hum.socks.v2_1;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpMethod;

public class HttpForwardServerHandler extends SimpleChannelInboundHandler<FullHttpRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, FullHttpRequest msg) throws Exception {
		String host = HttpUtil.parseHost(msg.uri());
		short port = HttpUtil.parsePort(msg.uri());
		if (msg.method() == HttpMethod.CONNECT) {
			ctx.channel().writeAndFlush(Unpooled.wrappedBuffer("HTTP/1.1 200 Connection established\r\n\r\n".getBytes()));
		}
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(ctx.channel().eventLoop());
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new FowardChannelHandler(ctx));
			}
		});
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		bootstrap.connect(host, port).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				future.channel().write(msg);
			}
		});
	}
}

class FowardChannelHandler extends ChannelInboundHandlerAdapter {
	private ChannelHandlerContext ctx;

	public FowardChannelHandler(ChannelHandlerContext ctx) {
		this.ctx = ctx;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		this.ctx.writeAndFlush(msg);
		ctx.fireChannelRead(msg);
	}
}

class HttpUtil {

	public static String filterHttpHeaderString(String uri) {
		if (uri.startsWith("http://") || uri.startsWith("https://")) {
			return uri.split("://")[1];
		}
		return uri;
	}

	public static String parseHost(String uri) {
		return filterHttpHeaderString(uri).split(":")[0];
	}

	public static short parsePort(String uri) {
		uri = filterHttpHeaderString(uri);
		String[] sArr = uri.split(":");
		if (sArr.length == 1) {
			return 80;
		}
		try {
			return Short.parseShort(uri.split(":")[sArr.length - 1]);
		} catch (NumberFormatException c) {
			return 80;
		}
	}
}