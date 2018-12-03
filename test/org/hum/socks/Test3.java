package org.hum.socks;

import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;

public class Test3 implements Runnable {

	private int bufferSize = 2048;

	@Override
	public void run() {
		try {
			Selector selector = Selector.open();
			ServerSocketChannel socketChannel = ServerSocketChannel.open();
			socketChannel.socket().bind(new InetSocketAddress(1080));
			socketChannel.configureBlocking(false);
			socketChannel.register(selector, SelectionKey.OP_ACCEPT);
			System.out.println("Server[" + socketChannel + "] started .... port:1080");
			listener(selector);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void listener(Selector in_selector) {
		try {
			while (true) {
				in_selector.select();
				Iterator<SelectionKey> iterator = in_selector.selectedKeys().iterator();
				while (iterator.hasNext()) {
					SelectionKey selectionKey = iterator.next();
					if (selectionKey.isAcceptable()) {
						ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();
						serverSocketChannel.accept().configureBlocking(false).register(in_selector, SelectionKey.OP_READ);
						System.out.println("accepted [" + serverSocketChannel + "]");
					} else if (selectionKey.isReadable()) {
						SocketChannel clientChannel = (SocketChannel) selectionKey.channel();
						ByteBuffer receiveBuf = ByteBuffer.allocate(bufferSize);
						int len = clientChannel.read(receiveBuf);
						System.out.println(len);
						receiveBuf.flip();
						byte version = receiveBuf.get();
						System.out.println("version:" + version);
						byte methodCount = receiveBuf.get();
						byte[] methods = new byte[methodCount];
						receiveBuf.put(methods);
						System.out.println("methods:" + Arrays.toString(methods));
						
						// response
						ByteBuffer sendBuf = ByteBuffer.allocate(bufferSize);
						sendBuf.put((byte) 5);
						sendBuf.put((byte) 0);
						receiveBuf.flip();
						clientChannel.write(sendBuf);
						System.out.println("response - 1");

						// handshake-2
						receiveBuf.clear();
						receiveBuf.flip();
						len = clientChannel.read(receiveBuf);
						System.out.println("len3=" + len);
						version = receiveBuf.get();
						System.out.println("handshake2-version:" + version);

					} else if (selectionKey.isConnectable()) {
						System.out.println("connected");
					}
					iterator.remove();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Thread(new Test3()).start();
	}
}
