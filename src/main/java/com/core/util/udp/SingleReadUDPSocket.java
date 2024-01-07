package com.core.util.udp;

import java.io.IOException;
import java.nio.ByteBuffer;

public class SingleReadUDPSocket implements SimpleReadWriteUDPSocket {

	private final ReadWriteUDPSocket socket;
	private final String iface;
	private final String multicastGroup;
	private final short receivePort;
	private boolean hasJoined;
	

	public SingleReadUDPSocket(
			ReadWriteUDPSocket socket, 
			String iface, 
			String multicastGroup,
			short receivePort) {
				
		this.socket = socket;
		this.iface = iface;
		this.multicastGroup = multicastGroup;
		this.receivePort = receivePort;
	}
	
	@Override
	public void open() throws IOException {
		if(hasJoined) {
			throw new IOException("Cannot re-open() when joined to a multicast group");
		}
		
		socket.open();
		if(receivePort != 0) {
			socket.join(iface, multicastGroup, receivePort);
		} else {
			socket.join(iface, multicastGroup);
		}
		hasJoined = true;
	}

	@Override
	public void close() {
		socket.close();
		this.hasJoined = false;
	}

	@Override
	public boolean canWrite() {
		return false;
	}

	@Override
	public boolean write(ByteBuffer output) {
		throw new RuntimeException("Not implemented"); // TODO: implement
	}

	@Override
	public void enableRead(boolean val) {
		socket.enableRead(val);
	}

	@Override
	public boolean hasJoined() {
		return hasJoined;
	}

}
