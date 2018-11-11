package org.hum.socks.v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicInteger;

import org.hum.socks.v2.common.Configuration;
import org.hum.socks.v2.common.Logger;
import org.hum.socks.v2.compoment.BrowserConnector;
import org.hum.socks.v2.compoment.ShadowThreadPool;

@SuppressWarnings("resource")
public class SocksClient {

	private static AtomicInteger counter = new AtomicInteger();
	
	public static void main(String[] args) throws IOException {

		// 1.开启ServerSocket监听，等待浏览器请求
		ServerSocket serverSocket = new ServerSocket(Configuration.SOCKS_CLIENT_LISTENING_PORT);
		Logger.log("client start, listening on port:" + Configuration.SOCKS_CLIENT_LISTENING_PORT);

		while (true) {
			try {
				// 2.倘若有浏览器请求，新建Connector处理。
				Socket socket = serverSocket.accept();
				Logger.log("accept socket connected:" + counter.getAndIncrement());
				ShadowThreadPool.execute(new BrowserConnector(socket));
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}
	}
}
