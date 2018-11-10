package org.hum.socks.v2;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import org.hum.socks.v2.common.Configuration;
import org.hum.socks.v2.common.Logger;
import org.hum.socks.v2.compoment.ClientConnector;
import org.hum.socks.v2.compoment.ShadowThreadPool;

@SuppressWarnings("resource")
public class SocksServer {

	public static void main(String[] args) throws IOException {
		ServerSocket serverSocket = new ServerSocket(Configuration.SOCKS_SERVER_LISTENING_PORT);
		Logger.log("server start, listening on port:" + Configuration.SOCKS_SERVER_LISTENING_PORT);

		while (true) {
			try {
				Socket socket = serverSocket.accept();
				Logger.log("accept socket connected");
				ShadowThreadPool.execute(new ClientConnector(socket));
			} catch (Exception ignore) {
				ignore.printStackTrace();
			}
		}
	}
}
