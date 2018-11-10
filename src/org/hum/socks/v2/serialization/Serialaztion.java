package org.hum.socks.v2.serialization;

import java.io.IOException;

import org.hum.socks.v2.protocol.HostEntity;

public interface Serialaztion {

	public byte[] serialize(HostEntity hostEntity) throws IOException;
	
	public HostEntity deserilize(byte[] bytes) throws IOException;
}
