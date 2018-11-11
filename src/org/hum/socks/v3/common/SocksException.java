package org.hum.socks.v3.common;

public class SocksException extends RuntimeException {

	private static final long serialVersionUID = 550585563263705046L;

	public SocksException() {
		super();
	}

	public SocksException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public SocksException(String message, Throwable cause) {
		super(message, cause);
	}

	public SocksException(String message) {
		super(message);
	}

	public SocksException(Throwable cause) {
		super(cause);
	}
	
}
