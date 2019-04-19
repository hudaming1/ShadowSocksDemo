package org.hum.socks.v7.inside;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpRequestDecoder;
import io.netty.handler.codec.http.HttpRequestEncoder;

public class HttpProxyServer {

	private int port;
	private ServerBootstrap serverBootstrap;

	public HttpProxyServer(int port) {
		this.port = port;
		init();
	}

	public void init() {
		serverBootstrap = new ServerBootstrap();
		NioEventLoopGroup masterLoopGroup = new NioEventLoopGroup(1);
		NioEventLoopGroup slaveLoopGroup = new NioEventLoopGroup(8);
		serverBootstrap.group(masterLoopGroup, slaveLoopGroup);
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new HttpRequestDecoder());
				ch.pipeline().addLast(new HttpRequestEncoder());
			}
		});
	}

	public void start() {
		serverBootstrap.bind(port);
	}
}
