package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupByteBufferMessages implements SoupMessages {
    private final ByteBuffer temp = ByteBuffer.allocateDirect(1500);

    private final SoupDebugByteBufferMessage cmdDebug = new SoupDebugByteBufferMessage(); 
    private final SoupDebugByteBufferMessage eventDebug = new SoupDebugByteBufferMessage(); 
    private final SoupLoginAcceptedByteBufferMessage cmdLoginAccepted = new SoupLoginAcceptedByteBufferMessage(); 
    private final SoupLoginAcceptedByteBufferMessage eventLoginAccepted = new SoupLoginAcceptedByteBufferMessage(); 
    private final SoupLoginRejectedByteBufferMessage cmdLoginRejected = new SoupLoginRejectedByteBufferMessage(); 
    private final SoupLoginRejectedByteBufferMessage eventLoginRejected = new SoupLoginRejectedByteBufferMessage(); 
    private final SoupSequencedDataByteBufferMessage cmdSequencedData = new SoupSequencedDataByteBufferMessage(); 
    private final SoupSequencedDataByteBufferMessage eventSequencedData = new SoupSequencedDataByteBufferMessage(); 
    private final SoupServerHeartbeatByteBufferMessage cmdServerHeartbeat = new SoupServerHeartbeatByteBufferMessage(); 
    private final SoupServerHeartbeatByteBufferMessage eventServerHeartbeat = new SoupServerHeartbeatByteBufferMessage(); 
    private final SoupEndOfSessionByteBufferMessage cmdEndOfSession = new SoupEndOfSessionByteBufferMessage(); 
    private final SoupEndOfSessionByteBufferMessage eventEndOfSession = new SoupEndOfSessionByteBufferMessage(); 
    private final SoupLoginRequestByteBufferMessage cmdLoginRequest = new SoupLoginRequestByteBufferMessage(); 
    private final SoupLoginRequestByteBufferMessage eventLoginRequest = new SoupLoginRequestByteBufferMessage(); 
    private final SoupUnsequencedDataByteBufferMessage cmdUnsequencedData = new SoupUnsequencedDataByteBufferMessage(); 
    private final SoupUnsequencedDataByteBufferMessage eventUnsequencedData = new SoupUnsequencedDataByteBufferMessage(); 
    private final SoupClientHeartbeatByteBufferMessage cmdClientHeartbeat = new SoupClientHeartbeatByteBufferMessage(); 
    private final SoupClientHeartbeatByteBufferMessage eventClientHeartbeat = new SoupClientHeartbeatByteBufferMessage(); 
    private final SoupLogoutRequestByteBufferMessage cmdLogoutRequest = new SoupLogoutRequestByteBufferMessage(); 
    private final SoupLogoutRequestByteBufferMessage eventLogoutRequest = new SoupLogoutRequestByteBufferMessage(); 
 
    @Override
    public SoupDebugCommand getSoupDebugCommand() {
        temp.clear();
        return cmdDebug.wrapCommand(temp);
    }

    @Override
    public SoupDebugCommand getSoupDebugCommand(ByteBuffer buffer) {
        return cmdDebug.wrapCommand(buffer);
    }

    @Override
    public SoupDebugEvent getSoupDebugEvent(ByteBuffer buffer) {
        return eventDebug.wrapEvent(buffer);
    }
    @Override
    public SoupLoginAcceptedCommand getSoupLoginAcceptedCommand() {
        temp.clear();
        return cmdLoginAccepted.wrapCommand(temp);
    }

    @Override
    public SoupLoginAcceptedCommand getSoupLoginAcceptedCommand(ByteBuffer buffer) {
        return cmdLoginAccepted.wrapCommand(buffer);
    }

    @Override
    public SoupLoginAcceptedEvent getSoupLoginAcceptedEvent(ByteBuffer buffer) {
        return eventLoginAccepted.wrapEvent(buffer);
    }
    @Override
    public SoupLoginRejectedCommand getSoupLoginRejectedCommand() {
        temp.clear();
        return cmdLoginRejected.wrapCommand(temp);
    }

    @Override
    public SoupLoginRejectedCommand getSoupLoginRejectedCommand(ByteBuffer buffer) {
        return cmdLoginRejected.wrapCommand(buffer);
    }

    @Override
    public SoupLoginRejectedEvent getSoupLoginRejectedEvent(ByteBuffer buffer) {
        return eventLoginRejected.wrapEvent(buffer);
    }
    @Override
    public SoupSequencedDataCommand getSoupSequencedDataCommand() {
        temp.clear();
        return cmdSequencedData.wrapCommand(temp);
    }

    @Override
    public SoupSequencedDataCommand getSoupSequencedDataCommand(ByteBuffer buffer) {
        return cmdSequencedData.wrapCommand(buffer);
    }

    @Override
    public SoupSequencedDataEvent getSoupSequencedDataEvent(ByteBuffer buffer) {
        return eventSequencedData.wrapEvent(buffer);
    }
    @Override
    public SoupServerHeartbeatCommand getSoupServerHeartbeatCommand() {
        temp.clear();
        return cmdServerHeartbeat.wrapCommand(temp);
    }

    @Override
    public SoupServerHeartbeatCommand getSoupServerHeartbeatCommand(ByteBuffer buffer) {
        return cmdServerHeartbeat.wrapCommand(buffer);
    }

    @Override
    public SoupServerHeartbeatEvent getSoupServerHeartbeatEvent(ByteBuffer buffer) {
        return eventServerHeartbeat.wrapEvent(buffer);
    }
    @Override
    public SoupEndOfSessionCommand getSoupEndOfSessionCommand() {
        temp.clear();
        return cmdEndOfSession.wrapCommand(temp);
    }

    @Override
    public SoupEndOfSessionCommand getSoupEndOfSessionCommand(ByteBuffer buffer) {
        return cmdEndOfSession.wrapCommand(buffer);
    }

    @Override
    public SoupEndOfSessionEvent getSoupEndOfSessionEvent(ByteBuffer buffer) {
        return eventEndOfSession.wrapEvent(buffer);
    }
    @Override
    public SoupLoginRequestCommand getSoupLoginRequestCommand() {
        temp.clear();
        return cmdLoginRequest.wrapCommand(temp);
    }

    @Override
    public SoupLoginRequestCommand getSoupLoginRequestCommand(ByteBuffer buffer) {
        return cmdLoginRequest.wrapCommand(buffer);
    }

    @Override
    public SoupLoginRequestEvent getSoupLoginRequestEvent(ByteBuffer buffer) {
        return eventLoginRequest.wrapEvent(buffer);
    }
    @Override
    public SoupUnsequencedDataCommand getSoupUnsequencedDataCommand() {
        temp.clear();
        return cmdUnsequencedData.wrapCommand(temp);
    }

    @Override
    public SoupUnsequencedDataCommand getSoupUnsequencedDataCommand(ByteBuffer buffer) {
        return cmdUnsequencedData.wrapCommand(buffer);
    }

    @Override
    public SoupUnsequencedDataEvent getSoupUnsequencedDataEvent(ByteBuffer buffer) {
        return eventUnsequencedData.wrapEvent(buffer);
    }
    @Override
    public SoupClientHeartbeatCommand getSoupClientHeartbeatCommand() {
        temp.clear();
        return cmdClientHeartbeat.wrapCommand(temp);
    }

    @Override
    public SoupClientHeartbeatCommand getSoupClientHeartbeatCommand(ByteBuffer buffer) {
        return cmdClientHeartbeat.wrapCommand(buffer);
    }

    @Override
    public SoupClientHeartbeatEvent getSoupClientHeartbeatEvent(ByteBuffer buffer) {
        return eventClientHeartbeat.wrapEvent(buffer);
    }
    @Override
    public SoupLogoutRequestCommand getSoupLogoutRequestCommand() {
        temp.clear();
        return cmdLogoutRequest.wrapCommand(temp);
    }

    @Override
    public SoupLogoutRequestCommand getSoupLogoutRequestCommand(ByteBuffer buffer) {
        return cmdLogoutRequest.wrapCommand(buffer);
    }

    @Override
    public SoupLogoutRequestEvent getSoupLogoutRequestEvent(ByteBuffer buffer) {
        return eventLogoutRequest.wrapEvent(buffer);
    }
}
