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
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Http代理服务类
 * <pre>
 * 	1.启动监听端口
 * 	2.读取并解析浏览器请求报文
 * 	3.根据读出的IP和端口与远端服务器建立连接（管道1：将目标服务器响应数据转发给浏览器）
 * 	4.建立管道2：监听浏览器请求数据，转发给目标服务器
 *  +--------+          +---------+           +----------+	
 *  |        | ---1---> |         | ----2---> |          |
 *  | 浏览器  |          |  Proxy  |           |  Remote  |
 *  |        | <--4---- |         | <---3---- |          |
 *  +--------+          +---------+           +----------+
 * </pre>
 * @author huming
 */
public class HttpProxy implements Runnable {

	private static final int LISTENNING_PORT = 5432;
	private static final ExecutorService ExecutorService = Executors.newFixedThreadPool(200);
	private static final SimpleDateFormat SimpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
	private Socket browserSocket;
	private Socket remoteSocket;

	public HttpProxy(Socket client) {
		this.browserSocket = client;
	}

	/** 已连接到请求的服务器 */
	private static final String ConnectedLine = "HTTP/1.1 200 Connection established\r\n\r\n";
	/** 内部错误 */
	private static final String ServerErrorLine = "HTTP/1.1 500 Connection FAILED\r\n\r\n";
	
	private static final AtomicInteger counter = new AtomicInteger(0);

	@Override
	public void run() {
		try {
			System.out.println("accept request" + counter.getAndIncrement());
			// 读取Http请求
			InputStream browserInputStream = browserSocket.getInputStream();
			OutputStream browserOutputStream = browserSocket.getOutputStream();
			// 从客户端流数据中读取头部，获得请求主机和端口
			HttpHeader header = HttpHeader.readHeader(browserInputStream);
			if (header == null) {
				System.err.println("can't parse header");
				return;
			}
			
			// 如果没解析出请求请求地址和端口，则返回错误信息
			if (header.getHost() == null || header.getPort() == null) {
				System.err.println("can't parse host or port. header=" + header);
				browserOutputStream.write(ServerErrorLine.getBytes());
				browserOutputStream.flush();
				return;
			}
			
			System.out.println(header);

			// 连接目标主机
			remoteSocket = new Socket(header.getHost(), Integer.parseInt(header.getPort()));
			System.out.println("connect " + header.getHost());
			InputStream serverInputStream = remoteSocket.getInputStream();
			OutputStream serverOutputStream = remoteSocket.getOutputStream();
			
			if (header.getMethod().equals(HttpHeader.METHOD_CONNECT)) {
				// 将已联通信号返回给请求页面(https才会有这么一步)
				browserOutputStream.write(ConnectedLine.getBytes());
				browserOutputStream.flush();
			} else {
				// http请求需要将请求头部也转发出去
				serverOutputStream.write(header.toString().getBytes());
				serverOutputStream.flush();
			}
			
			// 新开一个线程将返回的数据转发给客户端，串行会出问题 remote -> browser
			Thread forwardThread = new DataSendThread(serverInputStream, browserOutputStream);
			forwardThread.start();
			
			// 读取客户端请求过来的数据转发给服务器 browserSocket -> server
			readForwardDate(browserInputStream, serverOutputStream);
			// 等待向客户端转发的线程结束
			forwardThread.join(); 
		} catch (Exception e) {
			e.printStackTrace();
			if (!browserSocket.isOutputShutdown()) {
				// 如果还可以返回错误状态的话，返回内部错误
				try {
					browserSocket.getOutputStream().write(ServerErrorLine.getBytes());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} finally {
			try {
				if (browserSocket != null) {
					browserSocket.close();
					System.out.println("browserSocket close1");
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			if (remoteSocket != null) {
				try {
					remoteSocket.close();
					System.out.println("server close 1");
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			System.out.println("request" + counter.getAndDecrement() + " finish");
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
					buffer = new byte[4096];
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			try {
				remoteSocket.close();// 尝试关闭远程服务器连接，中断转发线程的读阻塞状态
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
						buffer = new byte[4096];
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}
	
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
