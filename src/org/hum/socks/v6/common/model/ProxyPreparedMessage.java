package org.hum.socks.v6.common.model;

public class ProxyPreparedMessage {

	public static final int SUCCESS = 1;
	private int magicNuml;
	private int code;
	
	public ProxyPreparedMessage() { }

	public ProxyPreparedMessage(int magicNuml, int code) {
		this.magicNuml = magicNuml;
		this.code = code;
	}

	public int getMagicNuml() {
		return magicNuml;
	}

	public void setMagicNuml(int magicNuml) {
		this.magicNuml = magicNuml;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	@Override
	public String toString() {
		return "ProxyPreparedMessage [magicNuml=" + magicNuml + ", code=" + code + "]";
	}
}
