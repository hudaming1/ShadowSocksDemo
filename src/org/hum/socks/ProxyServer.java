package org.hum.socks;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.hum.socks.util.Utils;

/**
 * @author huming
 */
public class ProxyServer {

	static final ExecutorService ThreadPool = Executors.newFixedThreadPool(300);
	static final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
	static final int BUFFER_SIZE = 4096;
	static final int LISTEN_PORT = 1080;
	static final int SOCKET_OPTION_SOTIMEOUT = 7000;
	static final long IDLE_TIME = 180000L; // 闲置超时5秒

	private static void log(String str) {
		System.out.println("[socks-server][" + Thread.currentThread().getName() + "]\t\t" + sdf.format(new Date()) + "\t\t" + str);
	}

	@SuppressWarnings("resource")
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(LISTEN_PORT);
		log("server start, listening port:" + LISTEN_PORT);

		while (true) {
			final Socket socket = server.accept();
			log("accept client [" + socket.getInetAddress().getHostAddress() + ":" + socket.getPort() + "]");

			ThreadPool.execute(new Runnable() {
				@Override
				public void run() {
					try {
						final DataInputStream clientSocksInputStream = new DataInputStream(socket.getInputStream());
						final DataOutputStream clientSocksOutputStream = new DataOutputStream(socket.getOutputStream());
						byte[] buffer = new byte[BUFFER_SIZE];
						
						int magicNumber = clientSocksInputStream.readInt();
						if (magicNumber != ProxyClient.MAGIC_NUMBER) {
							socket.close();
							return ;
						}
						String remoteHost = null;
						Short remotePort = null;
						byte[] remoteHostBytes = new byte[clientSocksInputStream.readInt()];
						clientSocksInputStream.read(remoteHostBytes);
						remoteHost = new String(remoteHostBytes);
						remotePort = clientSocksInputStream.readShort();
						
						Socket remoteSocket = new Socket(remoteHost, remotePort);

						final InputStream remoteInputStream = remoteSocket.getInputStream();
						final OutputStream remoteOutputStream = remoteSocket.getOutputStream();

						// pipe
						ThreadPool.execute(new Runnable() {
							@Override
							public void run() {
								byte[] buffer = new byte[BUFFER_SIZE];
								int length = 0;
								long lastReadTime = 0L;
								while (length >= 0) {
									length = 0;
									try {
										// client <---read--- server
										/**
										length = clientSocksInputStream.read(buffer);
										lastReadTime = System.currentTimeMillis();
										**/
										byte[] beforeDecrpytByte = Utils.readBytes(clientSocksInputStream);
										length = beforeDecrpytByte.length;
										buffer = Utils.decrypt(beforeDecrpytByte);
										lastReadTime = System.currentTimeMillis();
									} catch (InterruptedIOException e) {
										if ((System.currentTimeMillis() - lastReadTime) >= (IDLE_TIME - 1000)) {
											log(Thread.currentThread().getName() + " exit");
										}
										length = 0;
									} catch (IOException ignore) {
										// length = -1;
//										ignore.printStackTrace();
									}
									try {
										if (length > 0) {
//											remoteOutputStream.write(Utils.decrypt(buffer), 0, length);
											remoteOutputStream.write(buffer, 0, buffer.length);
											remoteOutputStream.flush();
										}
									} catch (IOException e) {
										// e.printStackTrace();
										length = -1;
									}
								}
								try {
									clientSocksInputStream.close();
									log("clientSocksInputStream closed");
								} catch (Exception ce) {
									ce.printStackTrace();
								}
								try {
									remoteOutputStream.close();
									log("remoteOutputStream closed");
								} catch (Exception ce) {
									ce.printStackTrace();
								}
							}
						});

						int length = 0;
						long lastReadTime = 0L;
						// pipe (server <-> remote)
						while (length >= 0) {
							try {
								// server ----(read)----> remote
								length = remoteInputStream.read(buffer);
								lastReadTime = System.currentTimeMillis();
							} catch (InterruptedIOException ce) {
								if ((System.currentTimeMillis() - lastReadTime) >= (IDLE_TIME - 1000)) {
									log(Thread.currentThread().getName() + " exit");
								}
								length = 0;
							} catch (IOException ignore) {
								// length = -1;
//								ignore.printStackTrace();
							}
							try {
								if (length > 0) {
									// server ----(write)-----> client
									byte[] bb = new byte[length];
									System.arraycopy(buffer, 0, bb, 0, length);
									byte[] bytes = Utils.encrypt(bb);
									clientSocksOutputStream.writeInt(bytes.length);
									clientSocksOutputStream.write(bytes, 0, bytes.length);
									// log("reponse to client, length=" + bytes.length + ", datas=" + java.util.Arrays.toString(bytes));
									clientSocksOutputStream.flush();
								}
							} catch (IOException ce) {
								// ce.printStackTrace();
								length = -1;
							}
						}

						try {
							remoteInputStream.close();
							log("remoteInputStream closed");
						} catch (Exception ce) {
							ce.printStackTrace();
						}
						try {
							clientSocksOutputStream.close();
							log("clientSocksOutputStream closed");
						} catch (Exception ce) {
							ce.printStackTrace();
						}

					} catch (Exception ce) {
						System.out.println(Thread.currentThread().getName() + " error");
						ce.printStackTrace();
					} finally {
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
