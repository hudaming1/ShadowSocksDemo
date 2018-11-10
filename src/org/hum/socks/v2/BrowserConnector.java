package org.hum.socks.v2;

import java.net.Socket;

public class BrowserConnector implements Runnable {

	private Socket socket;
	
	public BrowserConnector(Socket socket) {
		this.socket = socket;
	}
	
	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
}
