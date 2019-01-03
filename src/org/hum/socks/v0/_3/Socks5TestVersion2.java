package org.hum.socks.v0._3;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author huming
 */
public class Socks5TestVersion2 {
	
	static final ExecutorService ThreadPool = Executors.newFixedThreadPool(300);
	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	static final int BUFFER_SIZE = 40960;
	static final int LISTEN_PORT = 1080;
	static final int SOCKET_OPTION_SOTIMEOUT = 7000;
	static final long IDLE_TIME = 180000L; // 闲置超时5秒 
	static final AtomicInteger counter = new AtomicInteger(0);
	
	private static void log(String str) {
		System.out.println(sdf.format(new Date()) + "\t\t" + str);
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
//		ServerSocket server = new ServerSocket(LISTEN_PORT, 5, localIP);
		ServerSocket server = new ServerSocket(LISTEN_PORT);
		log("server start, listening port:" + LISTEN_PORT);
		
		while (true) {
			final Socket socket = server.accept();
			// socket.setSoTimeout(SOCKET_OPTION_SOTIMEOUT);
			log("accept client [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]");

			DataInputStream clientInputStream = new DataInputStream(socket.getInputStream());
			DataOutputStream clientOutputStream = new DataOutputStream(socket.getOutputStream());
			
			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						log("current socket-thread count:" + counter.incrementAndGet());
						byte[] buffer = new byte[BUFFER_SIZE];
						
						/**
						 * 接收第一次请求消息体
						 * <pre>
						 * 	协议格式如下(数字代表占用字节数)
						 * 	  +----+----------+----------+ 
						 *	  | VER| NMETHODS | METHODS  |
						 *	  +----+----------+----------+ 
						 *	  | 1  |    1     | 1 - 255  |
						 *	  +----+----------+----------+ 
						 * </pre>
						 */
						byte version = clientInputStream.readByte(); // 版本无非就是4/5，现在主流都是用5，我用IE6自带Socks试了一下，发现采用的协议是4
						byte methodCount = clientInputStream.readByte(); // 验证方式数量
						byte[] methods = new byte[methodCount]; // 支持验证方式集合，验证方式虽然很多，当常见的就是0和2，0代表无需验证，2代表用用户名密码验证；其他验证方式客户端基本不支持，例如Mac支持0和2验证，而一般浏览器仅支持0，也就是无需验证。
						clientInputStream.read(methods); 
						log(String.format("handshake-1: version:%s, methods:%s", version, Arrays.toString(methods)));
						/**
						 * 第一次响应消息体
						 * <pre>
						 * 	协议格式如下(数字代表占用字节数)
						 *    +----+--------+ 
						 *	  |VER | METHOD | 
						 *	  +----+--------+ 
						 *	  | 1　| 　 1　　|  
						 *	  +----+--------+ 
						 * </pre>
						 */
						clientOutputStream.writeByte(5); // 输出version
						clientOutputStream.writeByte(0); // 服务器从客户端支持的Methods选出一种自己支持的验证方式？后面的验证逻辑怎么通信的，目前还不清楚。
						clientOutputStream.flush();

						/**
						 * 第二次通信请求消息体
						 * <pre>
						 * 	协议格式如下(数字代表占用字节数)
						 *    +-----+-----+-------+------+----------+----------+ 
						 *	  | VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
						 *	  +-----+-----+-------+------+----------+----------+ 
						 *	  |  1  |  1  | X'00' |  1   | variable |    2     |
						 *	  +-----+-----+-------+------+----------+----------+ 
						 * </pre>
						 */
						version = clientInputStream.readByte(); // version每次通信都要带着
						byte command = clientInputStream.readByte(); // command代表本次通信需要进行什么操作，一般做翻墙代理只用Connect，也就是1
						byte rsv = clientInputStream.readByte(); // 保留字段，必须为0x00
						byte atype = clientInputStream.readByte(); // 请求地址类型：1-域名；3-IPv4；4-IPv6
						String serverHost = ""; // 请求目标地址，依靠atype读取
						if (atype == 1) { 
							// 如果atype是IPv4，则按照IP地址拼读
							serverHost = clientInputStream.read() + "." + clientInputStream.read() + "." + clientInputStream.read() + "." + clientInputStream.read();
						} else if (atype == 3) {
							// 如果atype是域名，则第一个字节代表域名长度，后面变长根据长度读取即可
							int domainLength = clientInputStream.read();
							byte[] domainBytes = new byte[domainLength];
							clientInputStream.read(domainBytes);
							serverHost = new String(domainBytes, "utf-8");
						} else {
							System.err.println("error"); // 作为Demo，我没有实现IPv6协议
						}
						short serverPort = clientInputStream.readShort(); // 读取端口号
						log(String.format("handshake-2: version:%s, command:%s, rsv:%s, atype:%s, remoteServer:%s:%s", version, command, rsv, atype, serverHost, serverPort));

						// 和目标服务器建立连接 
						Socket targetServer = null;
						try {
							targetServer = new Socket(serverHost, serverPort);
						} catch (ConnectException ce) {
							log(serverHost + ":" + serverPort + " connected exception!");
							ce.printStackTrace();
						}
						// targetServer.setSoTimeout(SOCKET_OPTION_SOTIMEOUT);
						byte[] addressArray = socket.getInetAddress().getAddress();
						int targetPort = socket.getPort();

						/**
						 * 第二次通信响应消息体
						 * <pre>
						 * 	协议格式如下(数字代表占用字节数)
						 * 	 +-----+-----+-------+------+----------+----------+
						 *	 | VER | REP |  RSV  | ATYP | BND.ADDR | BND.PORT |
						 *	 +-----+-----+-------+------+----------+----------+
						 *	 |  1  |  1  | X'00' |  1   | Variable |    2     |
						 *	 +-----+-----+-------+------+----------+----------+
						 * </pre>
						 */
						byte[] outBytes = new byte[10];
						outBytes[0] = 0x05; // version
						outBytes[1] = 0x00; // 返回状态码（0-代表连接成功，非0代表失败）
						outBytes[2] = 0x00; // RSV保留字段，固定传0
						outBytes[3] = 0x01; // atype返回代理服务器IP，因atype是1，下面地址就传IP地址即可
						outBytes[4] = addressArray[0]; // ip_1
						outBytes[5] = addressArray[1]; // ip_2
						outBytes[6] = addressArray[2]; // ip_3
						outBytes[7] = addressArray[3]; // ip_4
						outBytes[8] = (byte) ((targetPort & 0xFF00) >> 8); // 输出端口高位
						outBytes[9] = (byte) (targetPort & 0x00FF); // 输出端口低位
						clientOutputStream.write(outBytes);
						clientOutputStream.flush();
						log("response byte-length : " + outBytes.length);

						/**
						 * 打开传输管道，实现转发功能：
						 * 	1.SocksServer读取本地请求
						 * 	2.SocksServer转发到目标服务器
						 * 	3.SocksServer读取目标服务器的响应
						 * 	4.SocksServer将目标服务器响应转发给Local
						 * <pre>
						 * 	+-------+            +-------------+          +--------+
						 *  | Local |  <---1---  | SocksServer | ---2---> | Remote |
						 *  +-------+            +-------------+          +--------+
						 *     ↑                    |      ↑                   |  
						 *     |                    |      |                   |
						 *     +---------4----------+      +--------3----------+  
						 * </pre>
						 */
						final InputStream serverInputStream = targetServer.getInputStream();
						final OutputStream serverOutputStream = targetServer.getOutputStream();
						
						// pipe2
						ThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								byte[] buffer = new byte[BUFFER_SIZE];
								int length = 0;
								long lastReadTime = 0L;
								while (length >= 0) {
									try {
										length = serverInputStream.read(buffer);
										lastReadTime = System.currentTimeMillis();
									} catch (InterruptedIOException e) {
										if ((System.currentTimeMillis() - lastReadTime) >= (IDLE_TIME - 1000)) {
											log(Thread.currentThread().getName() + " exit");
										}
										length = 0;
									} catch (IOException e) {
										length = -1;
									}
									try {
										if (length > 0) {
											clientOutputStream.write(buffer, 0, length);
											clientOutputStream.flush();
										}
									} catch (IOException e) {
										e.printStackTrace();
									}
								}
								try {
									clientOutputStream.close();
									log("clientOutputStream closed");
								} catch (Exception ce) {
									ce.printStackTrace();
								}
								try {
									serverInputStream.close();
									log("serverInputStream closed");
								} catch (Exception ce) {
									ce.printStackTrace();
								}
							}
						});
						
						int length = 0;
						long lastReadTime = 0L;
						// pipe1
						while (length >= 0) {
							try {
								length = clientInputStream.read(buffer);
								lastReadTime = System.currentTimeMillis();
							} catch (InterruptedIOException ce) {
								if ((System.currentTimeMillis() - lastReadTime) >= (IDLE_TIME - 1000)) {
									log(Thread.currentThread().getName() + " exit");
								}
								length = 0;
							} catch (IOException ce) {
								length = -1;
							}
							try {
								if (length > 0) {
									serverOutputStream.write(buffer, 0, length);
									serverOutputStream.flush();
								}
							} catch (IOException ce) {
								ce.printStackTrace();
							}
						}

						try {
							clientInputStream.close();
							log("clientInputStream closed");
						} catch (Exception ce) {
							ce.printStackTrace();
						}
						try {
							serverOutputStream.close();
							log("serverOutputStream closed");
						} catch (Exception ce) {
							ce.printStackTrace();
						}
							
					} catch (Exception ce) {
						System.out.println(Thread.currentThread().getName() + " error");
						ce.printStackTrace();
					} finally {
						log("close server socket, current socket-thread count:" + counter.decrementAndGet());
					}
				}
			});
		}
	}
	
	static void logBuffer(byte[] buffer) {
		try {
			String line = null;
			ByteArrayInputStream bis = new ByteArrayInputStream(buffer);
			BufferedReader br = new BufferedReader(new InputStreamReader(bis));
			while ((line = br.readLine()) != null) {
				log(line);
			}
		} catch (Exception ce) {
			ce.printStackTrace();
		}
	}
}
