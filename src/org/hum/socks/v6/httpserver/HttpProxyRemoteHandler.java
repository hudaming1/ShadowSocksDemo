package org.hum.socks.v6.httpserver;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

/**
 * Handle data from remote.
 *
 * @author shuaicj 2017/09/21
 */
@Component
@Scope("prototype")
public class HttpProxyRemoteHandler extends ChannelInboundHandlerAdapter {

	@SuppressWarnings("unused")
	private final String id;
	private Channel clientChannel;
	private Channel remoteChannel;

	public HttpProxyRemoteHandler(String id, Channel clientChannel) {
		this.id = id;
		this.clientChannel = clientChannel;
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		this.remoteChannel = ctx.channel();
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) {
		clientChannel.writeAndFlush(msg); // just forward
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		flushAndClose(clientChannel);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable e) {
		flushAndClose(remoteChannel);
	}

	private void flushAndClose(Channel ch) {
		if (ch != null && ch.isActive()) {
			ch.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
		}
	}
}
