package org.hum;

import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

public class Test {
	public static void main(String[] args) throws Exception {
		RandomAccessFile aFile = new RandomAccessFile("d:/demo.txt", "rw");
		FileChannel channel = aFile.getChannel();
		ByteBuffer buffer = ByteBuffer.allocate(2048);
		int read = channel.read(buffer);
		while (read != -1) {
			System.out.println("Read count=" + read);
			buffer.flip();
			while (buffer.hasRemaining()) {
				System.out.print((char) buffer.get());
			}
			buffer.clear();
			read = channel.read(buffer);
		}
		aFile.close();
	}
}
