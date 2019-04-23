package org.hum.socks.v6.httpserver;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

/**
 * A simple http proxy server.
 *
 * @author shuaicj 2017/09/21
 */
@Component
public class HttpProxyServer {

	@Value("${proxy.port}")
	private int port;
	@Autowired
	private ChannelInitializer<SocketChannel> channelInitializer;

	@PostConstruct
	public void start() {
		new Thread(() -> {
			EventLoopGroup bossGroup = new NioEventLoopGroup(1);
			EventLoopGroup workerGroup = new NioEventLoopGroup();
			try {
				ServerBootstrap b = new ServerBootstrap();
				b.group(bossGroup, workerGroup);
				b.channel(NioServerSocketChannel.class);
				b.handler(new LoggingHandler(LogLevel.DEBUG));
				b.childHandler(channelInitializer);
				b.bind(port).sync().channel().closeFuture().sync();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} finally {
				bossGroup.shutdownGracefully();
				workerGroup.shutdownGracefully();
			}
		}).start();
	}
}
