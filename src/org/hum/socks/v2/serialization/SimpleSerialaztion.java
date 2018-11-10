package org.hum.socks.v2.serialization;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;

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
	public HostEntity deserilize(byte[] bytes) {
		// TODO Auto-generated method stub
		return null;
	}
}
