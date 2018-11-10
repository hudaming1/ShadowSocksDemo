package org.hum.socks.v2.serialization;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.hum.socks.v2.common.SocksException;
import org.hum.socks.v2.protocol.HostEntity;

public class SimpleSerialaztion implements Serialaztion {

	private final int MAGIC_NUMBER = 19237;
	
	@Override
	public byte[] serialize(HostEntity hostEntity) throws IOException {
		byte[] hostBytes = hostEntity.getHost().getBytes();
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		DataOutputStream dos = new DataOutputStream(bos);
		dos.writeInt(MAGIC_NUMBER);
		dos.writeInt(hostBytes.length);
		dos.write(hostBytes);
		dos.writeShort(hostEntity.getPort());
		return bos.toByteArray();
	}

	@Override
	public HostEntity deserilize(InputStream inputStream) throws IOException {
		DataInputStream dis = new DataInputStream(inputStream);
		int magicNumber = dis.readInt();
		if (magicNumber != MAGIC_NUMBER) {
			throw new SocksException("unknown magic_number:" + magicNumber);
		}
		byte[] hostBytes = new byte[dis.readInt()];
		dis.read(hostBytes, 0, hostBytes.length);
		short port = dis.readShort();
		return new HostEntity(new String(hostBytes, "utf-8"), port);
	}
}
