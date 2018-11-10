package org.hum.socks.v2.compoment;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

import org.hum.socks.v2.common.Configuration;
import org.hum.socks.v2.common.SocksException;
import org.hum.socks.v2.protocol.HostEntity;
import org.hum.socks.v2.protocol.SocksFactory;
import org.hum.socks.v2.protocol.SocksProtocol;
import org.hum.socks.v2.serialization.Serialaztion;
import org.hum.socks.v2.serialization.SimpleSerialaztion;

/**
 * 浏览器连接处理器
 * 
 * <pre>
 *   每当一个浏览器的请求过来时，由BrowserConnector负责处理。
 * </pre>
 * 
 * @author huming
 */
public class BrowserConnector implements Runnable {

	private Socket browserSocket;
	private Socket serverSocket;
	private HostEntity remoteHostDomain;
	private Serialaztion serialaztion = new SimpleSerialaztion();

	public BrowserConnector(Socket socket) {
		// 1.与浏览器进行Socks协议握手
		try {
			SocksProtocol socksProtocol = SocksFactory.newSocks(socket); // 根据浏览器请求，选择协议版本解析（目前只实现了socks5协议）
			remoteHostDomain = socksProtocol.parse(); // 通过socks协议解析出浏览器要访问的实际地址
		} catch (IOException e1) {
			new SocksException("build connection of browser error", e1);
		}
		// 2.握手成功后，对远端SocksServer发起连接，准备让其代理传输数据
		try {
			this.browserSocket = socket;
			this.serverSocket = new Socket(Configuration.SERVER_HOST, Configuration.SOCKS_CLIENT_LISTENING_PORT);
		} catch (IOException e) {
			new SocksException("connect server occured exception!", e);
		}
		// 3.告知SocksServer访问目标地址
		try {
			DataOutputStream sockServerOutputStream = new DataOutputStream(serverSocket.getOutputStream());
			sockServerOutputStream.write(serialaztion.serialize(remoteHostDomain));
			sockServerOutputStream.flush();
		} catch (Exception e) {
			new SocksException("connect server occured exception!", e);
		}
	}

	@Override
	public void run() {
		try {
			// pipe1 : browser -----> socks_client
			ShadowThreadPool.execute(new PipeChannel(browserSocket.getInputStream(), serverSocket.getOutputStream()));
		} catch (IOException e) {
			throw new SocksException("pipe1 occured excpetion", e);
		}
		try {
			// pipe4 : socks_client -----> browser
			ShadowThreadPool.execute(new PipeChannel(serverSocket.getInputStream(), browserSocket.getOutputStream()));
		} catch (IOException e) {
			throw new SocksException("pipe4 occured excpetion", e);
		}
	}
}
