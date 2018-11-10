package org.hum.socks.v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.hum.socks.v2.compoment.BrowserConnector;
import org.hum.socks.v2.compoment.ShadowThreadPool;

public class SocksClient {
	
	private static final int LISTENING_PORT = 1080;

	public static void main(String[] args) throws IOException {
		
		// 1.开启ServerSocket监听，等待浏览器请求
		ServerSocket serverSocket = new ServerSocket(LISTENING_PORT);
		
		while (true) {
			// 2.倘若有浏览器请求，新建Connector处理。
			Socket socket = serverSocket.accept();
			ShadowThreadPool.execute(new BrowserConnector(socket));
		}
	}
}
