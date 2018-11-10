package org.hum.socks.v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.hum.socks.v2.common.Configuration;
import org.hum.socks.v2.common.Logger;
import org.hum.socks.v2.compoment.BrowserConnector;
import org.hum.socks.v2.compoment.ShadowThreadPool;

@SuppressWarnings("resource")
public class SocksServer {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(Configuration.SOCKS_SERVER_LISTENING_PORT);
		Logger.log("server start, listening on port:" + Configuration.SOCKS_SERVER_LISTENING_PORT);
		
		while (true) {
			Socket socket = serverSocket.accept();
			ShadowThreadPool.execute(new BrowserConnector(socket));
			Logger.log("accept socket connected");
		}
	}
}
