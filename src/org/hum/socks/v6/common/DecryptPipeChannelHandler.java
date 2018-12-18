package org.hum.socks.v6.common;

import java.util.Arrays;

import org.hum.socks.v6.common.util.Utils;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

public class DecryptPipeChannelHandler extends ChannelInboundHandlerAdapter {

	private String name;
	private Channel pipeChannel;

	public DecryptPipeChannelHandler(String name, Channel channel) {
		this.name = name;
		this.pipeChannel = channel;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
		try {
			if (pipeChannel.isActive()) {
				ByteBuf bytebuff = (ByteBuf) msg;
				if (!bytebuff.hasArray()) {
					int len = bytebuff.readInt();
					byte[] arr = new byte[len];
					try {
						bytebuff.getBytes(4, arr); // skip length bytes
						System.out.println("[dec]" + len + ":" + Arrays.toString(arr));
						byte[] decrypt = Utils.decrypt(arr);
						pipeChannel.writeAndFlush(Unpooled.wrappedBuffer(decrypt));
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
