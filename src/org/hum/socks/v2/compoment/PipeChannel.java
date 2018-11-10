package org.hum.socks.v2.compoment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 通信管道，负责将TCP字节流从一端(inputStream)传输到另一端(outputStream)
 * 
 * @author huming
 */
public class PipeChannel implements Runnable {

	private InputStream inputStream;
	private OutputStream outputStream;
	private final int IDLE_TIMEOUT = 5000; // 读空闲时间5秒

	public PipeChannel(InputStream inputStream, OutputStream outputStream) {
		this.inputStream = inputStream;
		this.outputStream = outputStream;
	}

	@Override
	public void run() {
		int length = 0;
		byte[] buffer = new byte[4096];
		long lastReadTime = System.currentTimeMillis();
		while (length >= 0) {
			// 从管道一端读取流
			try {
				length = inputStream.read(buffer);
				lastReadTime = System.currentTimeMillis();
			} catch (IOException e) {
				if ((System.currentTimeMillis() - lastReadTime) >= IDLE_TIMEOUT) {
					length = -1;
				}
			}
			// 将流输出到对端
			if (length > 0) {
				try {
					outputStream.write(buffer, 0, length);
					outputStream.flush();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 关闭管道
		try {
			_destroy();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private void _destroy() throws IOException {
		if (inputStream != null) {
			inputStream.close();
		}
		if (outputStream != null) {
			outputStream.close();
		}
	}
}
