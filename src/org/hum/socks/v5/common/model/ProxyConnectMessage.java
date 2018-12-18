package org.hum.socks.v5.common.model;

public class ProxyConnectMessage {

	private int magicNum;
	private int hostLen;
	private String host;
	private short port;
	
	public ProxyConnectMessage() { }
	
	public ProxyConnectMessage(int magicNum, int hostLen, String host, short port) {
		super();
		this.magicNum = magicNum;
		this.hostLen = hostLen;
		this.host = host;
		this.port = port;
	}

	public int getMagicNum() {
		return magicNum;
	}

	public void setMagicNum(int magicNum) {
		this.magicNum = magicNum;
	}

	public int getHostLen() {
		return hostLen;
	}

	public void setHostLen(int hostLen) {
		this.hostLen = hostLen;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public short getPort() {
		return port;
	}

	public void setPort(short port) {
		this.port = port;
	}

	@Override
	public String toString() {
		return "ProxyConnectMessage [magicNum=" + magicNum + ", hostLen=" + hostLen + ", host=" + host + ", port="
				+ port + "]";
	}
}
