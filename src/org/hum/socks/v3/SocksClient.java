package org.hum.socks.v3;

import java.io.IOException;
import java.util.Arrays;

import org.hum.socks.v2.common.Logger;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.socksx.v5.DefaultSocks5InitialResponse;
import io.netty.handler.codec.socksx.v5.Socks5AuthMethod;

public class SocksClient {
	
	private static final int LISTENING_PORT = 1080;

	// test
	public static void main(String[] args) throws IOException, InterruptedException {
		EventLoopGroup bossGroup = new NioEventLoopGroup();
		EventLoopGroup workerGroup = new NioEventLoopGroup();
		ServerBootstrap bootstrap = new ServerBootstrap();
		bootstrap.group(bossGroup, workerGroup);
		bootstrap.channel(NioServerSocketChannel.class);
		bootstrap.childHandler(new ChannelInitializer<Channel>() {
			@Override
			protected void initChannel(Channel ch) throws Exception {
				ch.pipeline().addLast(new NettyServerHandler());
			}
		});
		ChannelFuture future = bootstrap.bind(LISTENING_PORT).sync();
		System.out.println("server listenining on port : " + LISTENING_PORT);
		future.channel().closeFuture().sync();
	}

	private static class NettyServerHandler extends ChannelInboundHandlerAdapter {

		public NettyServerHandler() {
		}
		
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    		ByteBuf buf = (ByteBuf) msg;
	    		// handshake-1  (request)
	    		byte version = buf.readByte();
	    		byte methodCount = buf.readByte(); // 验证方式数量
	    		byte[] methods = new byte[methodCount];
	    		buf.readBytes(methods);
	    		Logger.log(String.format("handshake-1: version:%s, methods:%s", version, Arrays.toString(methods)));
	    		// handshake-1  (response)
//	    		ByteArrayOutputStream bos = new ByteArrayOutputStream();
//	    		DataOutputStream dos = new DataOutputStream(bos);
//	    		dos.writeByte(version);
//	    		dos.writeByte(0);
	    		ctx.writeAndFlush(new DefaultSocks5InitialResponse(Socks5AuthMethod.NO_AUTH));
	    		Logger.log(String.format("handshake-1 over"));
	    		
	    }
	}

	private static class NettyServerHandler2 extends ChannelInboundHandlerAdapter {

		public NettyServerHandler2() {
		}
		
	    @Override
	    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
	    		System.out.println("handshake2 begin");
	    		ByteBuf buf = (ByteBuf) msg;
	    		byte[] bytes = new byte[buf.readableBytes()];
	    		buf.readBytes(bytes);
	    		// handshake-1  (request)
	    		byte version = buf.readByte();
	    		System.out.println("handshake2-version" + version);
	    }
	}
}
