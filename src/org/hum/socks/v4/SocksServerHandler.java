package org.hum.socks.v4;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.socks.SocksAuthResponse;
import io.netty.handler.codec.socks.SocksAuthScheme;
import io.netty.handler.codec.socks.SocksAuthStatus;
import io.netty.handler.codec.socks.SocksCmdRequest;
import io.netty.handler.codec.socks.SocksCmdRequestDecoder;
import io.netty.handler.codec.socks.SocksCmdType;
import io.netty.handler.codec.socks.SocksInitResponse;
import io.netty.handler.codec.socks.SocksRequest;

public class SocksServerHandler extends SimpleChannelInboundHandler<SocksRequest> {

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, SocksRequest msg) throws Exception {
		System.out.println("SocksServerHandler.channelRead, msg=" + msg);
		switch (msg.requestType()) {
		case INIT:
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.writeAndFlush(new SocksInitResponse(SocksAuthScheme.NO_AUTH));
			break;
		case AUTH:
			ctx.pipeline().addFirst(new SocksCmdRequestDecoder());
			ctx.writeAndFlush(new SocksAuthResponse(SocksAuthStatus.SUCCESS));
			break;
		case CMD:
			SocksCmdRequest socksCmdRequest = (SocksCmdRequest) msg;
			if (socksCmdRequest.cmdType() == SocksCmdType.CONNECT) {
				// 进行socks握手完成后，下一步就要开始pipe模式(addLast和remove调换位置后就不行，为什么？ XXX)
				ctx.pipeline().addLast(new SocksConnectHandler());
				// socks握手部分差不多了，因此可以删除这个channel，握手结束在SocksConnectHandler里
				ctx.pipeline().remove(this);
				// 将msg继续往下传(其实就是交给了SocksConnecthandler处理)
				ctx.fireChannelRead(socksCmdRequest);
			} else {
				ctx.close();
			}
			break;
		case UNKNOWN:
			ctx.close();
			break;
		default:
			break;
		}
	}
}
