package org.hum.socks.v2.compoment;

import java.io.IOException;
import java.net.Socket;

import org.hum.socks.v2.common.Configure;
import org.hum.socks.v2.common.SocksException;

/**
 * 浏览器连接处理器
 * <pre>
 *   每当一个浏览器的请求过来时，由BrowserConnector负责处理。
 * </pre>
 * @author huming
 */
public class BrowserConnector implements Runnable {

	private Socket browserSocket;
	private Socket serverSocket;
	
	public BrowserConnector(Socket socket) {
		this.browserSocket = socket;
		try {
			this.serverSocket = new Socket(Configure.SERVER_HOST, Configure.SERVER_PORT);
		} catch (IOException e) {
			new SocksException("connect server occured exception!", e);
		}
	}
	
	@Override
	public void run() {
		try {
			ShadowThreadPool.execute(new PipeChannel(browserSocket.getInputStream(), serverSocket.getOutputStream()));
			ShadowThreadPool.execute(new PipeChannel(serverSocket.getInputStream(), browserSocket.getOutputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
