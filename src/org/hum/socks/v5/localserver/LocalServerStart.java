package org.hum.socks.v5.localserver;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socks.SocksInitRequestDecoder;
import io.netty.handler.codec.socks.SocksMessageEncoder;

public class LocalServerStart {

	public static void main(String[] args) {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup(Runtime.getRuntime().availableProcessors()));
		serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new SocksInitRequestDecoder());
				ch.pipeline().addLast(new SocksMessageEncoder());
				ch.pipeline().addLast(new SocksServerHandler());
			}
		});
		serverBootstrap.bind(1080);
	}
}
