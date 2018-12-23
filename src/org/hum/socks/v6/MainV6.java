package org.hum.socks.v6;

import java.io.IOException;

import org.hum.socks.v6.localserver.LocalServerStart;
import org.hum.socks.v6.proxyserver.ProxyServerStart;

public class MainV6 {

	public static void main(final String[] args) throws IOException {
		new Thread(new Runnable() {
			@Override
			public void run() {
				LocalServerStart.main(args);
				System.out.println("local-server started");
			}
		}).start();
		
		ProxyServerStart.main(args);
		System.out.println("proxy-server started");
		
		System.in.read();
	}
}
