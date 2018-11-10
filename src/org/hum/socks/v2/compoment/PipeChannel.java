package org.hum.socks.v2.compoment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.SocketException;

import org.hum.socks.v2.common.Logger;

/**
 * 通信管道，负责将TCP字节流从一端(inputStream)传输到另一端(outputStream)
 * 
 * @author huming
 */
public class PipeChannel implements Runnable {

	private InputStream inputStream;
	private OutputStream outputStream;
	private final int IDLE_TIMEOUT = 1000; // 读空闲时间5秒

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
				} catch (SocketException ignore) {
					/**
					 * <pre>
					 * 	这里其实可以通过socket.isClosed判断，避免异常。
					 * 	但目前异常原因还不明确，length大于0说明有数据要给client，
					 * 	而socket为什么会关闭呢 但目前不影响使用，暂时先不研究了
					 * </pre>
					 */
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		// 关闭管道
		_destroy();
		Logger.log("destroy");
	}

	private void _destroy() {
		if (inputStream != null) {
			try {
				inputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if (outputStream != null) {
			try {
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
