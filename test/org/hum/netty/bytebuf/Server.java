package org.hum.netty.bytebuf;

import java.util.Arrays;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.FixedRecvByteBufAllocator;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class Server {

	public static void main(String[] args) throws InterruptedException {
		ServerBootstrap serverBootstrap = new ServerBootstrap();
		serverBootstrap.group(new NioEventLoopGroup(1), new NioEventLoopGroup());
		serverBootstrap.channel(NioServerSocketChannel.class);
		serverBootstrap.option(ChannelOption.SO_KEEPALIVE, true).option(ChannelOption.RCVBUF_ALLOCATOR, new FixedRecvByteBufAllocator(65535));
		serverBootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new Decoder());
				ch.pipeline().addLast(new TestHandler());
			}
		});
		serverBootstrap.bind(22222).sync();
	}
	
	private static final class TestHandler extends ChannelInboundHandlerAdapter {

	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    		ByteBuf buf = (ByteBuf) msg;
	    		System.out.println("server-test-handler: size=" + buf.capacity());
	    		byte[] bytes = new byte[buf.readableBytes()];
	    		buf.readBytes(bytes);
	    		System.out.println(Arrays.toString(bytes));
	        ctx.fireChannelRead(msg);
	    }
	}
}
