package org.hum.socks.v4;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.Unpooled;
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
		System.out.println("SocksConnectHandler.read.enter, " + request.host() + ":" + request.port());
		bootstrap.group(ctx.channel().eventLoop()).channel(NioSocketChannel.class);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 10000);
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.handler(new PipeHanlder(ctx));
		bootstrap.connect(request.host(), request.port()).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				// 如果连接成功后面讲请求转发过去；如果失败，则通过Socks协议告知客户端失败
				if (!future.isSuccess()) {
					ctx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.FAILURE, SocksAddressType.IPv4));
					if (ctx.channel().isActive()) {
						ctx.channel().writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
					}
				} else {
					future.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4)).addListener(new ChannelFutureListener() {
						@Override
						public void operationComplete(ChannelFuture channelFuture) {
							// TODO
						}
					});
				}
			}
		});
	}
	
	private static class PipeHanlder extends ChannelInboundHandlerAdapter {
		
		private ChannelHandlerContext outCtx;
		
		public PipeHanlder(ChannelHandlerContext outCtx) {
			this.outCtx = outCtx;
		}

	    @Override
	    public void channelActive(ChannelHandlerContext ctx) throws Exception {
	    		System.out.println("PipeHandler active");
	    }

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    		System.out.println("pipe.read=" + msg);
	        outCtx.writeAndFlush(msg);
	    }
	}
}