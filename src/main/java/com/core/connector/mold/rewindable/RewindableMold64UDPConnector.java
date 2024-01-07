package com.core.connector.mold.rewindable;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Set;

import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatSource;
import com.core.connector.BaseConnector;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.Connector;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.sequencer.BackupSessionListener;
import com.core.util.BinaryUtils;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.udp.ReadWriteUDPSocket;
import com.core.util.udp.SimpleReadWriteUDPSocket;
import com.core.util.udp.SingleReadUDPSocket;
import com.core.util.udp.UDPSocketFactory;
import com.core.util.udp.UDPSocketReadWriteListener;
import com.core.util.udp.WritableUDPSocket;

public class RewindableMold64UDPConnector extends BaseConnector implements Connector, UDPSocketReadWriteListener, TCPClientSocketListener, HeartbeatSource {

	private  BackupSessionListener sessionListener;
	private boolean isMulticastOpen;
	private boolean isRewindOpen;
	private ByteBuffer currentSession = ByteBuffer.allocate(Mold64UDPPacket.SESSION_LENGTH);
	private boolean sessionSet = false;
	private long nextExpectedSequenceNumber;
	private int nextRewoundMessageLength;
	private long rewindInitiatedTime;
	private long rewindInitiatedSequence;
	private final ByteBuffer rewindRequestMessage = ByteBuffer.allocate(8);
	private boolean processingMessage;
	private long maximumSequenceNumber;
	private TCPClientSocket rewindSocket;
	private int rewindLocationIndex;
	
	private final ByteStringBuffer statusBuffer = new ByteStringBuffer();
	private final ByteBuffer rewindBuffer = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
	private final SimpleReadWriteUDPSocket multicastReadSocket;
	private final Log logger;
	private final Mold64UDPPacket receivePacket;

	private final TCPSocketFactory tcpSocketFactory;
	private final MessageStore messageStore;
	private ArrayList<RewindLocation> rewindLocations;

	public RewindableMold64UDPConnector(Log logger, ByteBufferDispatcher dispatcher, UDPSocketFactory udpSocketFactory, TCPSocketFactory tcpSocketFactory, MessageStore messageStore, String multicastGroup, String multicastInterface, Set<RewindLocation> rewindLocations, short receivePort) throws IOException {
		super(dispatcher);
		this.logger = logger;
		this.tcpSocketFactory = tcpSocketFactory;

        if (rewindLocations.size()==0) {
            throw new RuntimeException("No rewind locations defined");
        }
		this.rewindLocations= new ArrayList<>(rewindLocations);
		rewindLocationIndex = 0;
		
		this.messageStore = messageStore;
		isMulticastOpen = false;
		isRewindOpen = false;
		nextExpectedSequenceNumber = 1;
		
		this.receivePacket = new Mold64UDPPacket(logger);
		multicastReadSocket = new SingleReadUDPSocket(udpSocketFactory.createReadWriteUDPSocket(this), multicastInterface, multicastGroup, receivePort);
		rewindSocket = tcpSocketFactory.createTCPClientSocket();
		rewindSocket.setListener(this);
	}
	
	@Override
	public void open() throws IOException {
		if(isMulticastOpen || isRewindOpen) {
			throw new IllegalStateException("Cannot open when already open");
		}
		isMulticastOpen = true;
		multicastReadSocket.open();
		multicastReadSocket.enableRead(true);
	}

	@Override
	public void close() throws IOException {
		multicastReadSocket.close();
		isMulticastOpen = false;
	}


	@Override
	public long getCurrentSeq() {
		return nextExpectedSequenceNumber;
	}

	@Override
	@Exposed(name = "status")
	public ByteStringBuffer status() {
		statusBuffer.clear();
		
		statusBuffer.add("Session: ").add(getSession()).addNL();
		statusBuffer.add("isCaughtUp: ").add(!isRewindOpen).addNL();
		statusBuffer.add("nextExpectedSequence: ").add(nextExpectedSequenceNumber).addNL();
		
		return statusBuffer;
	}

	@Exposed(name = "setRewindLocation")
	public void setRewindLocation(@Param(name = "Host")String host, @Param(name = "Port") int port, @Param(name = "IsSingleRewindHost") boolean singleRewindHost) {
		if(singleRewindHost){
			rewindLocations.clear();
		}
		RewindLocation rewindLocation = new RewindLocation(host, (short) port);
		rewindLocations.add(rewindLocation);
		rewindLocationIndex=rewindLocations.size()-1;
		logger.debug(logger.log().add("Added ").add(host).add(":").add(port).add(" as rewinder host:port."));

	}

	@Override
	public String getSession() {
		return BinaryUtils.toString(currentSession);
	}

	public void setSessionListener(BackupSessionListener sessionListener){
		this.sessionListener = sessionListener;
	}

	@Override
	public void onWriteAvailable(WritableUDPSocket clientSocket) {
		// TODO Auto-generated method stub
		logger.debug(logger.log().add("multicast socket write available"));
	}

	@Override
	public void onWriteUnavailable(WritableUDPSocket clientSocket) {
		// TODO Auto-generated method stub
		logger.debug(logger.log().add("multicast socket write unavailable"));
	}

	@Override
	public void onDatagram(ReadWriteUDPSocket clientSocket, ByteBuffer datagram, InetSocketAddress addr) {
		if(!receivePacket.wrap(datagram)) {
			logger.error(logger.log().add("Datagram unpacking failed. Received ").add(datagram.remaining()).add(" bytes in packet header"));
			return; // Throw away the packet
		}
		
		ByteBuffer packetSession = receivePacket.getSession();
		if(sessionSet && !BinaryUtils.compare(currentSession, packetSession)) {
			logger.error(logger.log().add("Received packet for invalid session. Expected '").add(BinaryUtils.toString(currentSession)).add("' but got '").add(BinaryUtils.toString(packetSession)).add("'"));
			return; // Throw away the packet
		}

		if(!sessionSet) {
			currentSession.clear();
			currentSession.put(packetSession);
			currentSession.flip();
			sessionSet = true;
			String session = getSession();
			logger.info(logger.log().add("New session: ").add(session));
			dispatchSession(session);
			if(sessionListener !=null){
				sessionListener.onSessionStarted(session);
			}
		}
		
		long sequenceNumber = receivePacket.getStreamSeq();

		if (!receivePacket.hasMessages() && sequenceNumber > nextExpectedSequenceNumber) { // heartbeat is the next message
            logger.error(logger.log()
                    .add("HEARTBEAT GAP! Received seqNum greater than expected. Got ")
                    .add(sequenceNumber)
                    .add(" but expected ")
                    .add(nextExpectedSequenceNumber));
            if(sequenceNumber > maximumSequenceNumber) {
                maximumSequenceNumber = sequenceNumber; // last message received
            }
            try {
                beginRewind();
            }
            catch(IOException ioException) {
                logger.error(logger.log().add("IO error while processing message: ").add(ioException));
            }
		}

		while (receivePacket.hasMessages()) {
			ByteBuffer message = receivePacket.getMessage();
			if(message == null) {
				break;
			}
			try {
				processMessage(sequenceNumber, message);
			} catch(IOException ioException) {
				logger.error(logger.log().add("IO error while processing message: ").add(ioException));
				throw new UncheckedIOException(ioException);
			}
			sequenceNumber++;
		}
	}

	private void processMessage(long sequenceNumber, ByteBuffer message) throws IOException {
        if(sequenceNumber < nextExpectedSequenceNumber) {
            logger.error(logger.log().add("Received sequence number lower than expected. Got ").add(sequenceNumber).add(" but expected ").add(nextExpectedSequenceNumber));
            return; // swallow the message
        }

		if(sequenceNumber > nextExpectedSequenceNumber) {
			logger.error(logger.log().add("GAP! Received sequence number greater than expected. Got ").add(sequenceNumber).add(" but expected ").add(nextExpectedSequenceNumber));
			messageStore.putMessage(sequenceNumber, message);
			if(sequenceNumber > maximumSequenceNumber) {
				maximumSequenceNumber = sequenceNumber;
			}

			beginRewind();
			return; // swallow the message
		}

		dispatchMessage(message);
		nextExpectedSequenceNumber++;

	}

	private void beginRewind() throws IOException {
		if(isRewindOpen) {
			return; // already rewinding
		}

		RewindLocation location = rewindLocations.get(rewindLocationIndex);
		logger.info(logger.log().add("Opening rewind connection to ").add(location.getHost()).add(":").add(location.getPort()));
		rewindSocket.connect(location.getHost(), location.getPort());
		rewindLocationIndex = (rewindLocationIndex + 1) % rewindLocations.size();
		messageStore.setMaxSequence(maximumSequenceNumber);
		isRewindOpen = true;
	}

	@Override
	public void onConnect(TCPClientSocket clientSocket) {
		if(!isRewindOpen) {
			logger.error(logger.log().add("Received rewind socket connect() when not rewinding"));
			clientSocket.close();
			return;
		}
		
		rewindInitiatedSequence = nextExpectedSequenceNumber;
		rewindInitiatedTime = System.nanoTime();
		
		logger.info(logger.log().add("Initiating rewind starting at ").add(nextExpectedSequenceNumber));
		rewindBuffer.clear();
		rewindRequestMessage.clear();
		rewindRequestMessage.putLong(nextExpectedSequenceNumber);
		rewindRequestMessage.flip();
		clientSocket.write(rewindRequestMessage);
		clientSocket.enableRead(true);
	}

	@Override
	public void onDisconnect(TCPClientSocket clientSocket) {
		isRewindOpen = false;
		try {
			rewindSocket = tcpSocketFactory.createTCPClientSocket(); // TODO creates garbage; how can we avoid??
			rewindSocket.setListener(this);
		
			long rewoundMessages = nextExpectedSequenceNumber - rewindInitiatedSequence - 1;
			if(rewoundMessages == 0) {
				logger.info(logger.log().add("No messages available from rewind source. Retrying rewind"));
				beginRewind();
				return;
			}
		
			logger.info(logger.log().add("Rewound ").add(rewoundMessages).add(" messages in ").add(System.nanoTime() - rewindInitiatedTime).add(" nanos"));
			processAvailableMessagesInStore();
			
		} catch (IOException e) {
			logger.error(logger.log().add("Error while creating socket: ").add(e));
			throw new UncheckedIOException(e);
		} 
	}
	
	@Override
	public void onReadAvailable(TCPClientSocket clientSocket) {
		try {
			while (clientSocket.readBytes(rewindBuffer) > 0) {
				rewindBuffer.flip();
				try {
					processMessagesFromRewindBuffer();
				} catch (IOException ioException) {
					logger.error(logger.log().add("IO error while rewinding message: ").add(ioException));
					throw new UncheckedIOException(ioException);
				}
				rewindBuffer.compact();
			}
			
			if(clientSocket.isConnected() && nextExpectedSequenceNumber >= maximumSequenceNumber) {
                logger.info(logger.log()
                        .add("Closing TCP GAP Fill. NextExpectedSeqNum=").add(nextExpectedSequenceNumber)
                        .add(", MaxSeqNum=").add(maximumSequenceNumber));
                // close TCP rewind
				clientSocket.close();
			}
			
		} catch (IOException e) {
			logger.error(logger.log().add(e));
			throw new UncheckedIOException(e);
		}
	}
	
	private void processAvailableMessagesInStore() throws IOException {
		while(messageStore.hasMessage(nextExpectedSequenceNumber)) {
			processMessage(nextExpectedSequenceNumber, messageStore.popMessage(nextExpectedSequenceNumber));
		}
	}

	private void processMessagesFromRewindBuffer() throws IOException {
		while(rewindBuffer.remaining() > 0) {
			if(processingMessage) {
				if(rewindBuffer.remaining() < nextRewoundMessageLength) {
					return; // not enough to read
				}
				
				// process message
				ByteBuffer messageBuffer = rewindBuffer.slice();
				messageBuffer.limit(nextRewoundMessageLength);
				processMessage(nextExpectedSequenceNumber, messageBuffer); // We know the sequence number must be what we expect as we are rewinding
				rewindBuffer.position(rewindBuffer.position() + nextRewoundMessageLength);
				processingMessage = false;
				
			} else {
				
				if(rewindBuffer.remaining() < 4) {
					return; // not enough to read
				}
				
				nextRewoundMessageLength = rewindBuffer.getInt();
				processingMessage = true;
				
			}	
		}
				
	}
	
	@Override
	public void onWriteAvailable(TCPClientSocket clientSocket) {
		// TODO Auto-generated method stub
		logger.debug(logger.log().add("rewind socket write available"));
	}

	@Override
	public void onWriteUnavailable(TCPClientSocket clientSocket) {
		// TODO Auto-generated method stub
		logger.debug(logger.log().add("rewind socket write unavailable"));
	}

	@Override
	public void onHeartbeatRegister(HeartbeatFieldRegister register) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {
		// TODO Auto-generated method stub
	}

	public void setNextExpectedSequenceNumber(int lastSentSequenceNumber) {
		this.nextExpectedSequenceNumber = lastSentSequenceNumber;
	}
}
