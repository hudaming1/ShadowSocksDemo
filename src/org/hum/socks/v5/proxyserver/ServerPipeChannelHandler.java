package org.hum.socks.v5.proxyserver;

import org.hum.socks.v5.common.PipeChannelHandler;
import org.hum.socks.v5.common.ProxyConnectMessage;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;

public class ServerPipeChannelHandler extends SimpleChannelInboundHandler<ProxyConnectMessage> {

	private final Bootstrap bootstrap = new Bootstrap();
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProxyConnectMessage msg) throws Exception {
		// 交换数据完成
		ctx.pipeline().remove(ProxyConnectMessageDecorder.class);
		// 
		// ctx.pipeline().addLast(new PipeChannelHandler(channel));
		System.out.println("ServerPipeChannelHandler read message : " + msg);
		final Channel localServerChannel = ctx.channel();
		bootstrap.group(localServerChannel.eventLoop()).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		// pipe1: 读remote并向localServer写（从remote到localServer）
		bootstrap.handler(new PipeChannelHandler("server.pipe1", localServerChannel));
		// server与remote建立连接
		bootstrap.connect(msg.getHost(), msg.getPort()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture remoteChannelFuture) throws Exception {
				// pipe2: 读localServer并向remote写（从localServer到remote）
				localServerChannel.pipeline().addLast(new PipeChannelHandler("server.pipe2", remoteChannelFuture.channel()));
				System.out.println("add channel[" + System.nanoTime() + "]");
				// socks协议壳已脱，因此后面转发只需要靠pipehandler即可，因此删除SocksConnectHandler
				localServerChannel.pipeline().remove(ServerPipeChannelHandler.this);
			}
		});
	}
}
