package org.hum.socks.v2.compoment;

import java.io.IOException;
import java.net.Socket;

import org.hum.socks.v2.common.SocksException;
import org.hum.socks.v2.protocol.HostEntity;
import org.hum.socks.v2.serialization.Serialaztion;
import org.hum.socks.v2.serialization.SimpleSerialaztion;

public class ClientConnector implements Runnable {

	private Serialaztion serialaztion = new SimpleSerialaztion();
	private Socket socksClient;
	private Socket remoteSocket;
	
	public ClientConnector(Socket socksClient) {
		this.socksClient = socksClient;
		try {
			HostEntity hostEntity = serialaztion.deserilize(socksClient.getInputStream());
			this.remoteSocket = new Socket(hostEntity.getHost(), hostEntity.getPort());
		} catch (IOException e) {
			throw new SocksException("connect client occured exception!", e);
		}
	}
	
	@Override
	public void run() {
		try {
			// pipe2 : socks_client -----> socks_server
			ShadowThreadPool.execute(new PipeChannel(socksClient.getInputStream(), remoteSocket.getOutputStream()));
		} catch (IOException e) {
			throw new SocksException("pipe1 occured excpetion", e);
		}
		try {
			// pipe3 : socks_server -----> socks_client
			ShadowThreadPool.execute(new PipeChannel(remoteSocket.getInputStream(), socksClient.getOutputStream()));
		} catch (IOException e) {
			throw new SocksException("pipe4 occured excpetion", e);
		}
	}
}
