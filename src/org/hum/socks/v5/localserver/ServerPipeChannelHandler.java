package org.hum.socks.v5.localserver;

import org.hum.socks.v5.common.Constant;
import org.hum.socks.v5.common.ProxyConnectMessage;

import io.netty.bootstrap.Bootstrap;
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

public class ServerPipeChannelHandler extends SimpleChannelInboundHandler<SocksCmdRequest> {

	private final String PROXY_HOST = "127.0.0.1";
	private final int PROXY_PORT = 1081;
	
	@Override
	protected void channelRead0(final ChannelHandlerContext browserCtx, final SocksCmdRequest msg) throws Exception {
		Bootstrap bootstrap = new Bootstrap();
		bootstrap.group(browserCtx.channel().eventLoop());
		bootstrap.channel(NioSocketChannel.class);
		bootstrap.handler(new ProxyConnectChannelHanlder(browserCtx.channel()));
		bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
		bootstrap.option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000);
		bootstrap.connect(PROXY_HOST, PROXY_PORT).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(final ChannelFuture proxyChannelFuture) throws Exception {
				// 将ip和port输出到proxy-server
				ProxyConnectMessage connectMsg = new ProxyConnectMessage(Constant.MAGIC_NUMBER, msg.host().length(), msg.host(), (short) msg.port());
				proxyChannelFuture.channel().writeAndFlush(connectMsg).addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						// 与proxy-server握手完成后，告知browser socks协议结束，后面可以开始发送真正数据了
						browserCtx.channel().writeAndFlush(new SocksCmdResponse(SocksCmdStatus.SUCCESS, SocksAddressType.IPv4));
						// browser <-> proxy
						browserCtx.pipeline().addLast(new ProxyConnectChannelHanlder(proxyChannelFuture.channel()));
						browserCtx.pipeline().remove(ServerPipeChannelHandler.this);
					}
				});
			}
		});
	}
	
	private static final class ProxyConnectChannelHanlder extends ChannelInboundHandlerAdapter {
		
		private Channel pipeChannel;
		
		public ProxyConnectChannelHanlder(Channel channel) {
			this.pipeChannel = channel;
		}

	    @Override
	    public void channelRead(ChannelHandlerContext proxyCtx, Object msg) throws Exception {
	    		if (pipeChannel.isActive()) {
	    			pipeChannel.writeAndFlush(msg);
	    		}
	    }
	}
}
