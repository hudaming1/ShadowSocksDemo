package org.hum.socks.v6.common;

import java.util.Arrays;

import org.hum.socks.v6.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class EncryptPipeChannelHandler extends ChannelInboundHandlerAdapter {

	private String name;
	private Channel pipeChannel;

	public EncryptPipeChannelHandler(String name, Channel channel) {
		this.name = name;
		this.pipeChannel = channel;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (pipeChannel.isActive()) {
				ByteBuf bytebuff = (ByteBuf) msg;
				if (!bytebuff.hasArray()) {
					int len = bytebuff.readableBytes();
					byte[] arr = new byte[len];
					bytebuff.getBytes(0, arr);
					try {
						long id = System.currentTimeMillis();
//						System.out.println("[" + id + "][before-enc][" + len + "]" + Arrays.toString(arr));
						byte[] encrypt = Utils.encrypt(arr);
						len = encrypt.length;
						System.out.println("[" + id + "][after-enc][" + encrypt.length + "]" + Arrays.toString(encrypt));
//						ByteBuf buf = Unpooled.buffer(encrypt.length + 4); // +4是int长度
//						buf.writeInt(encrypt.length);
//						buf.writeBytes(encrypt);
//						System.out.println("[" + id + "][enc][" + encrypt.length + "][" + buf.capacity() + "]" + Arrays.toString(encrypt));
//						pipeChannel.writeAndFlush(buf);
						pipeChannel.writeAndFlush(Unpooled.wrappedBuffer(encrypt));
					} catch (Exception e) {
						System.out.println(name + " error, len=" + len);
						e.printStackTrace();
					}
				}
			}
		} finally {
			ReferenceCountUtil.release(msg);
		}
	}
}
