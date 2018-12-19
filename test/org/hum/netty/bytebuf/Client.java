package org.hum.netty.bytebuf;

import java.io.IOException;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;

public class Client {

	public static void main(String[] args) throws IOException {
		Bootstrap bootStrap = new Bootstrap();
		bootStrap.group(new NioEventLoopGroup());
		bootStrap.channel(NioSocketChannel.class);
		bootStrap.handler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
			}
		});
		bootStrap.connect("127.0.0.1", 22222).addListener(new ChannelFutureListener() {
			@Override
			public void operationComplete(ChannelFuture future) throws Exception {
				if (!future.isSuccess()) {
					System.out.println("failed");
					return;
				}
				Channel channel = future.channel();
				int size = 3040;
				ByteBuf byteBuf = Unpooled.buffer(size + 4);
				byteBuf.writeInt(size);
				for (int i = 0; i < size; i++) {
					byteBuf.writeByte(i);
				}
				System.out.println("out" + byteBuf.capacity());
				channel.writeAndFlush(byteBuf);
			}
		});

		System.in.read();
	}
}
