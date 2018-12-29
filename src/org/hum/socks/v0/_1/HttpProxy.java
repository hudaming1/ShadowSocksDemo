package org.hum.socks.v0._1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Http代理服务类
 * <pre>
 * 	1.
 * </pre>
 * @author huming
 */
public class HttpProxy implements Runnable {
	private Socket client;
	private Socket serverSocket;

	public HttpProxy(Socket client) {
		this.client = client;
	}

	/** 已连接到请求的服务器 */
	private static final String ConnectedLine = "HTTP/1.1 200 Connection established\r\n\r\n";
	/** 内部错误 */
	private static final String ServerErrorLine = "HTTP/1.1 500 Connection FAILED\r\n\r\n";

	@Override
	public void run() {
		try {
			// 读取Http请求
			InputStream clientInputStream = client.getInputStream();
			OutputStream clientOutputStream = client.getOutputStream();
			// 从客户端流数据中读取头部，获得请求主机和端口
			HttpHeader header = HttpHeader.readHeader(clientInputStream);
			
			// 如果没解析出请求请求地址和端口，则返回错误信息
			if (header.getHost() == null || header.getPort() == null) {
				System.err.println("can't parse host or port.");
				clientOutputStream.write(ServerErrorLine.getBytes());
				clientOutputStream.flush();
				return;
			}

			// 连接远程主机
			serverSocket = new Socket(header.getHost(), Integer.parseInt(header.getPort()));
			System.out.println("connect " + header.getHost());
			
			InputStream serverInputStream = serverSocket.getInputStream();
			OutputStream serverOutputStream = serverSocket.getOutputStream();
			// 新开一个线程将返回的数据转发给客户端，串行会出问题 server -> client
			Thread forwardThread = new DataSendThread(serverInputStream, clientOutputStream);
			forwardThread.start();
			
			if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
				// 将已联通信号返回给请求页面(https才会有这么一步)
				clientOutputStream.write(ConnectedLine.getBytes());
				clientOutputStream.flush();
			} else {
				// http请求需要将请求头部也转发出去
				System.out.println("heder;=" + header.toString());
				System.out.println("===============================");
				serverOutputStream.write(header.toString().getBytes());
				serverOutputStream.flush();
			}
			
			// 读取客户端请求过来的数据转发给服务器 client -> server
			readForwardDate(clientInputStream, serverOutputStream);
			// 等待向客户端转发的线程结束
			forwardThread.join();
		} catch (Exception e) {
			e.printStackTrace();
			if (!client.isOutputShutdown()) {
				// 如果还可以返回错误状态的话，返回内部错误
				try {
					client.getOutputStream().write(ServerErrorLine.getBytes());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (client != null) {
					client.close();
					System.out.println("client close1");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (serverSocket != null) {
				try {
					serverSocket.close();
					System.out.println("server close 1");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * 读取客户端发送过来的数据，发送给服务器端
	 * 
	 * @param clientSocketInputStream
	 * @param serverSocketOutputStream
	 */
	private void readForwardDate(InputStream clientSocketInputStream, OutputStream serverSocketOutputStream) {
		byte[] buffer = new byte[4096];
		try {
			int len;
			while ((len = clientSocketInputStream.read(buffer)) != -1) {
				if (len > 0) {
					serverSocketOutputStream.write(buffer, 0, len);
					serverSocketOutputStream.flush();
				}
				if (client.isClosed() || serverSocket.isClosed()) {
					System.out.println("socket closed");
					break;
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				serverSocket.close();// 尝试关闭远程服务器连接，中断转发线程的读阻塞状态
				System.out.println("server close2");
			} catch (IOException e1) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * 将服务器端返回的数据转发给客户端
	 * 
	 * @param serverInputStream
	 * @param clientOutputStream
	 */
	class DataSendThread extends Thread {
		private InputStream serverInputStream;
		private OutputStream clientOutputStream;

		DataSendThread(InputStream serverInputStream, OutputStream clientOutputStream) {
			this.serverInputStream = serverInputStream;
			this.clientOutputStream = clientOutputStream;
		}

		@Override
		public void run() {
			byte[] buffer = new byte[4096];
			try {
				int len;
				while ((len = serverInputStream.read(buffer)) != -1) {
					if (len > 0) {
						clientOutputStream.write(buffer, 0, len);
						clientOutputStream.flush();
					}
					if (client.isOutputShutdown() || serverSocket.isClosed()) {
						System.out.println("close socket");
						break;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	private static final int LISTENNING_PORT = 8002;
	private static final ExecutorService ExecutorService = Executors.newCachedThreadPool();
	private static final SimpleDateFormat SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");

	@SuppressWarnings("resource")
	public static void start() throws Exception {
		// 启动服务
		ServerSocket serverSocket = new ServerSocket(LISTENNING_PORT);
		System.out.println("Proxy Server Start At " + SimpleDateFormat.format(new Date()) + ", listening port:" + LISTENNING_PORT + "……");
		
		// 进入监听
		while (true) {
			Socket clientSocket = null;
			try {
				clientSocket = serverSocket.accept();
				ExecutorService.execute(new HttpProxy(clientSocket));
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
	public static void main(String[] args) throws Exception {
		HttpProxy.start();
	}
}
