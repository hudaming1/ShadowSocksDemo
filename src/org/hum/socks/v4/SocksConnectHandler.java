package org.hum.socks.v4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelOption;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.socks.SocksAddressType;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdResponse;
import io.netty.handler.codec.socks.SocksCmdStatus;

public class SocksConnectHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private final Bootstrap bootstrap = new Bootstrap();

	@Override
	protected void channelRead0(final ChannelHandlerContext ctx, final SocksCmdRequest request) throws Exception {
		System.out.println("SocksHandler.enter");
		final Channel browser2Server = ctx.channel();
		bootstrap.group(browser2Server.eventLoop()).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new PipeHanlder(browser2Server));
		bootstrap.connect(request.host(), request.port()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture future) throws Exception {
				// 如果连接成功后面讲请求转发过去；如果失败，则通过Socks协议告知客户端失败
				if (!future.isSuccess()) {
					browser2Server.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));
					if (browser2Server.isActive()) {
						browser2Server.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
					}
				} else {
					browser2Server.writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4)).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture channelFuture) {
							browser2Server.pipeline().addLast(new PipeHanlder(future.channel()));
						}
					});
				}
			}
		});
	}
	
	private static class PipeHanlder extends ChannelInboundHandlerAdapter {
		
		private Channel outChannel;
		
		public PipeHanlder(Channel channel) {
			this.outChannel = channel;
		}

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    		ctx.fireChannelActive();
	    }

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    		if (outChannel.isActive()) {
	    			outChannel.writeAndFlush(msg);
	    		}
	    }
	}
}