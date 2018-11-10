package org.hum.socks.v2.protocol;

public class HostEntity {

	private String host;
	private short port;
	
	public HostEntity() { }

	public HostEntity(String host, short port) {
		super();
		this.host = host;
		this.port = port;
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
		return "HostEntity [host=" + host + ", port=" + port + "]";
	}
}
