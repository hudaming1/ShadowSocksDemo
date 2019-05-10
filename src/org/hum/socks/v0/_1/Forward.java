package org.hum.socks.v0._1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class Forward implements Runnable {

	private String name;
	private Socket inputSocket;
	private Socket outputSocket;
	private byte[] buffer = new byte[4096];

	public Forward(String name, Socket inputSocket, Socket outSocket) {
		this.name = name;
		this.inputSocket = inputSocket;
		this.outputSocket = outSocket;
	}

	@Override
	public void run() {
		int len = 0;
		try {
			InputStream inputStream = inputSocket.getInputStream();
			OutputStream outputStream = outputSocket.getOutputStream();
			while ((len = inputStream.read(buffer)) != -1) {
				if (len > 0) {
					outputStream.write(buffer, 0, len);
					outputStream.flush();
				}
				if (inputSocket.isClosed() || outputSocket.isClosed()) {
					break;
				}
			}
		} catch (Exception ce) {
			System.out.println("channel[" + name + "] is error....");
			ce.printStackTrace();
		} finally {
			if (!inputSocket.isClosed()) {
				try {
					inputSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			if (!outputSocket.isClosed()) {
				try {
					outputSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
}