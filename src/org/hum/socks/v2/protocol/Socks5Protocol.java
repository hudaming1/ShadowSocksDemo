package org.hum.socks.v2.protocol;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

import org.hum.socks.v2.common.Logger;
import org.hum.socks.v2.common.SocksException;

public class Socks5Protocol implements SocksProtocol {

	public static final int VERSION = 5;
	private Socket socket;
	private DataInputStream browserClientInputStream;
	private DataOutputStream browserClientOutputStream;

	public Socks5Protocol(Socket socket) {
		try {
			this.socket = socket;
			this.browserClientInputStream = new DataInputStream(socket.getInputStream());
			this.browserClientOutputStream = new DataOutputStream(socket.getOutputStream());
		} catch (IOException e) {
			throw new SocksException("IO Exception" + e);
		}
	}

	@Override
	public HostEntity parse() {
		try {
			// 与Client第一次握手
			handshake1st();
			
			// 二次握手
			return handshake2nd();
		} catch (IOException e) {
			throw new SocksException("parse socks protocol[" + VERSION + "] occured exception", e);
		}
	}
	
	private void handshake1st() throws IOException {
		
		/**
		 * 接收第一次请求消息体
		 * 
		 * <pre>
		 * 	协议格式如下(数字代表占用字节数)
		 * 	  +----+----------+----------+ 
		 *	  | VER| NMETHODS | METHODS  |
		 *	  +----+----------+----------+ 
		 *	  | 1  |    1     | 1 - 255  |
		 *	  +----+----------+----------+
		 * </pre>
		 */
		byte methodCount = browserClientInputStream.readByte(); // 验证方式数量
		byte[] methods = new byte[methodCount]; // 支持验证方式集合，验证方式虽然很多，当常见的就是0和2，0代表无需验证，2代表用用户名密码验证；其他验证方式客户端基本不支持，例如Mac支持0和2验证，而一般浏览器仅支持0，也就是无需验证。
		browserClientInputStream.read(methods);
		Logger.log(String.format("handshake-1: version:%s, methods:%s", VERSION, Arrays.toString(methods)));

		/**
		 * 第一次响应消息体
		 * 
		 * <pre>
		 * 	协议格式如下(数字代表占用字节数)
		 *    +----+--------+ 
		 *	  |VER | METHOD | 
		 *	  +----+--------+ 
		 *	  | 1　| 　 1　　|  
		 *	  +----+--------+
		 * </pre>
		 */
		browserClientOutputStream.writeByte(5); // 输出version
		browserClientOutputStream.writeByte(0); // 服务器从客户端支持的Methods选出一种自己支持的验证方式？后面的验证逻辑怎么通信的，目前还不清楚。
		browserClientOutputStream.flush();
	}
	
	private HostEntity handshake2nd() throws IOException {

		/**
		 * 第二次通信请求消息体
		 * 
		 * <pre>
		 * 	协议格式如下(数字代表占用字节数)
		 *    +-----+-----+-------+------+----------+----------+ 
		 *	  | VER | CMD |  RSV  | ATYP | DST.ADDR | DST.PORT |
		 *	  +-----+-----+-------+------+----------+----------+ 
		 *	  |  1  |  1  | X'00' |  1   | variable |    2     |
		 *	  +-----+-----+-------+------+----------+----------+
		 * </pre>
		 */
		byte version = browserClientInputStream.readByte(); // version每次通信都要带着
		byte command = browserClientInputStream.readByte(); // command代表本次通信需要进行什么操作，一般做翻墙代理只用Connect，也就是1
		byte rsv = browserClientInputStream.readByte(); // 保留字段，必须为0x00
		byte atype = browserClientInputStream.readByte(); // 请求地址类型：1-域名；3-IPv4；4-IPv6
		String targetRemoteHost = ""; // 请求目标地址，依靠atype读取
		if (atype == 1) {
			// 如果atype是IPv4，则按照IP地址拼读
			targetRemoteHost = browserClientInputStream.read() + "." + browserClientInputStream.read() + "." + browserClientInputStream.read() + "." + browserClientInputStream.read();
		} else if (atype == 3) {
			// 如果atype是域名，则第一个字节代表域名长度，后面变长根据长度读取即可
			int domainLength = browserClientInputStream.read();
			byte[] domainBytes = new byte[domainLength];
			browserClientInputStream.read(domainBytes);
			targetRemoteHost = new String(domainBytes, "utf-8");
		} else {
			throw new SocksException("can't parse atype[" + atype + "]");
		}
		short targetRemotePort = browserClientInputStream.readShort(); // 读取端口号
		Logger.log(String.format("handshake-2: version:%s, command:%s, rsv:%s, atype:%s, remoteServer:%s:%s", version, command, rsv, atype, targetRemoteHost, targetRemotePort));

		byte[] addressArray = socket.getInetAddress().getAddress();
		int targetPort = socket.getPort();

		/**
		 * 第二次通信响应消息体
		 * 
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
		browserClientOutputStream.write(outBytes);
		browserClientOutputStream.flush();
		
		// 最后返回要访问的目标地址+端口
		return new HostEntity(targetRemoteHost, targetRemotePort); 
	}
}
