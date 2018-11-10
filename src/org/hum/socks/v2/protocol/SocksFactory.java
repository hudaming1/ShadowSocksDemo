package org.hum.socks.v2.protocol;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;

import org.hum.socks.v2.common.SocksException;

public class SocksFactory {

	public static SocksProtocol newSocks(Socket socket) throws IOException {
		DataInputStream dis = new DataInputStream(socket.getInputStream());
		byte version = dis.readByte();
		if (version == 5) {
			return new Socks5Protocol(socket);
		}
		throw new SocksException("unsupport socks version:" + version);
	}
}
