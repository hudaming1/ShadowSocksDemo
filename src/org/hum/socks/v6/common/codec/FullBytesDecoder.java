package org.hum.socks.v6.common.codec;

import java.util.List;

import org.hum.socks.v6.common.codec.FullBytesDecoder.State;
import org.hum.socks.v6.common.model.FullByteMessage;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ReplayingDecoder;

public class FullBytesDecoder extends ReplayingDecoder<State> {

	enum State {
		INIT, SUCCESS, FAILURE
	}

	public FullBytesDecoder() {
		super(State.INIT);
	}

	@Override
	protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
		switch (state()) {
		case INIT:
			int len = in.readInt();
			FullByteMessage msg = new FullByteMessage();
			msg.datas = new byte[len];
			in.readBytes(msg.datas);
			System.out.println("decode.len=" + msg.datas.length);
			out.add(msg);
			checkpoint(State.SUCCESS);
			break;
		case SUCCESS:
            int readableBytes = actualReadableBytes();
            if (readableBytes > 0) {
                out.add(in.readRetainedSlice(readableBytes));
            }
			break;
		case FAILURE:
			break;
		}
	}
}