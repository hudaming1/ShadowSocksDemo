package org.hum.socks.v5;

import java.io.IOException;

import org.hum.socks.v5.localserver.LocalServerStart;
import org.hum.socks.v5.proxyserver.ProxyServerStart;

public class MainV5 {

	public static void main(final String[] args) throws IOException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				LocalServerStart.main(args);
				System.out.println("local-server started");
			}
		}).start();
		
		ProxyServerStart.main(args);
		System.out.println("proxy-sever started");
		System.in.read();
	}
}
