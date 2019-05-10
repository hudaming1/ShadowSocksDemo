package org.hum;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

public class ServerTest {

	// http://localhost:8011/mpush/sms/mengwang/test2?spid=1123&mtmsgid=12123213
	public static void main(String[] args) throws IOException {
		ServerSocket server = new ServerSocket(8011);
		Socket socket = server.accept();
		BufferedReader br = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		String line = null;
		while ((line = br.readLine()) != null) {
			System.out.println("=====================");
			System.out.println(line);
		}
	}
}
