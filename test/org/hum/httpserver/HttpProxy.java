package org.hum.httpserver;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import org.hum.socks.v0._1.Forward;

public class HttpProxy {

	/** 已连接到请求的服务器 */
	private static final String ConnectedLine = "HTTP/1.1 200 Connection established\r\n\r\n";
	/** 内部错误 */
	private static final String ServerErrorLine = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(8002);
		while (true) {
			try {
				Socket browserSocket = server.accept();

				new Thread(new Runnable() {

					@Override
					public void run() {
						try {
							InputStream browserInputStream = browserSocket.getInputStream();
							OutputStream browserOutputStream = browserSocket.getOutputStream();

							HttpRequest request = WebHelper.parse(browserInputStream);
							System.out.println("======================================");
							System.out.println(request.getFullRequest());
							System.out.println("======================================");
							// 如果没解析出请求请求地址和端口，则返回错误信息
							if (request == null || request.getHost() == null || request.getHost().trim().isEmpty() || request.getPort() == null) {
								System.err.println("can't parse host or port.");
								browserOutputStream.write(ServerErrorLine.getBytes());
								browserOutputStream.flush();
								return ;
							}

							Socket remoteServer = new Socket(request.getHost(), request.getPort());
							System.out.println("connect " + request.getHost());

							// server -> browser
							Thread thread = new Thread(new Forward("1", remoteServer, browserSocket));
							thread.start();

							if ("CONNECT".equals(request.getMethod())) {
								browserOutputStream.write(ConnectedLine.getBytes());
								browserOutputStream.flush();
							}

							// browser -> remote
							new Thread(new Forward("2", browserSocket, remoteServer)).start();
							thread.join();
						} catch (Exception ce) {
							ce.printStackTrace();
						}
					}
				}).start();

			} catch (Exception ce) {
				ce.printStackTrace();
			}
		}
	}
}
