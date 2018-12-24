package org.hum.socks.v6.proxyserver;

import org.hum.socks.v6.common.Constant;
import org.hum.socks.v6.io.codec.IODecoder;
import org.hum.socks.v6.io.codec.ProxyConnectMessageCodec.ProxyConnectMessageDecorder;
import org.hum.socks.v6.io.codec.ProxyPreparedMessageCodec.ProxyPreparedMessageEncoder;
import org.hum.socks.v6.io.codec.model.ProxyConnectMessage;
import org.hum.socks.v6.io.codec.model.ProxyPreparedMessage;
import org.hum.socks.v6.io.handler.DecryptPipeChannelHandler;
import org.hum.socks.v6.io.handler.EncryptPipeChannelHandler;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;
import io.netty.util.concurrent.GenericFutureListener;

public class ServerPipeChannelHandler extends SimpleChannelInboundHandler<ProxyConnectMessage> {

	private final Bootstrap bootstrap = new Bootstrap();
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, ProxyConnectMessage msg) throws Exception {
		// 交换数据完成
		ctx.pipeline().remove(ProxyConnectMessageDecorder.class);
		final Channel localServerChannel = ctx.channel();
		bootstrap.group(localServerChannel.eventLoop()).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		// pipe3: 读remote并向localServer写（从remote到localServer）
		bootstrap.handler(new EncryptPipeChannelHandler("server.pipe3", localServerChannel));
		// server与remote建立连接
		bootstrap.connect(msg.getHost(), msg.getPort()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture remoteChannelFuture) throws Exception {
				// pipe2: 读localServer并向remote写（从localServer到remote）
				localServerChannel.pipeline().addLast(new IODecoder());
				localServerChannel.pipeline().addLast(new DecryptPipeChannelHandler("server.pipe2", remoteChannelFuture.channel()));
				localServerChannel.pipeline().addLast(new ProxyPreparedMessageEncoder());
				// 告知localserver，proxy已经准备好 (XXX 能用ByteBuf替换吗)
				localServerChannel.writeAndFlush(new ProxyPreparedMessage(Constant.MAGIC_NUMBER, ProxyPreparedMessage.SUCCESS)).addListener(new GenericFutureListener<Future<? super Void>>() {
					@Override
					public void operationComplete(Future<? super Void> future) throws Exception {
						localServerChannel.pipeline().remove(ProxyPreparedMessageEncoder.class);
					}
				}); 
				// socks协议壳已脱，因此后面转发只需要靠pipe_handler即可，因此删除SocksConnectHandler
				localServerChannel.pipeline().remove(ServerPipeChannelHandler.this);
			}
		});
	}
}
