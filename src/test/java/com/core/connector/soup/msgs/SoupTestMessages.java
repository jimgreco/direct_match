package com.core.connector.soup.msgs;

import java.nio.ByteBuffer;

public class SoupTestMessages implements SoupMessages {
    public <T> T get(Class<T> cls, ByteBuffer buffer) {
        if (cls.equals(SoupDebugEvent.class)) {
            return (T)getSoupDebugEvent(buffer);
        }
        if (cls.equals(SoupLoginAcceptedEvent.class)) {
            return (T)getSoupLoginAcceptedEvent(buffer);
        }
        if (cls.equals(SoupLoginRejectedEvent.class)) {
            return (T)getSoupLoginRejectedEvent(buffer);
        }
        if (cls.equals(SoupSequencedDataEvent.class)) {
            return (T)getSoupSequencedDataEvent(buffer);
        }
        if (cls.equals(SoupServerHeartbeatEvent.class)) {
            return (T)getSoupServerHeartbeatEvent(buffer);
        }
        if (cls.equals(SoupEndOfSessionEvent.class)) {
            return (T)getSoupEndOfSessionEvent(buffer);
        }
        if (cls.equals(SoupLoginRequestEvent.class)) {
            return (T)getSoupLoginRequestEvent(buffer);
        }
        if (cls.equals(SoupUnsequencedDataEvent.class)) {
            return (T)getSoupUnsequencedDataEvent(buffer);
        }
        if (cls.equals(SoupClientHeartbeatEvent.class)) {
            return (T)getSoupClientHeartbeatEvent(buffer);
        }
        if (cls.equals(SoupLogoutRequestEvent.class)) {
            return (T)getSoupLogoutRequestEvent(buffer);
        }
        return null;
    }

    @Override
    public SoupDebugCommand getSoupDebugCommand() {
		SoupDebugByteBufferMessage msg = new SoupDebugByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupDebugCommand getSoupDebugCommand(ByteBuffer buffer) {
		SoupDebugByteBufferMessage msg = new SoupDebugByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupDebugEvent getSoupDebugEvent(ByteBuffer buffer) {
		SoupDebugByteBufferMessage msg = new SoupDebugByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupLoginAcceptedCommand getSoupLoginAcceptedCommand() {
		SoupLoginAcceptedByteBufferMessage msg = new SoupLoginAcceptedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupLoginAcceptedCommand getSoupLoginAcceptedCommand(ByteBuffer buffer) {
		SoupLoginAcceptedByteBufferMessage msg = new SoupLoginAcceptedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupLoginAcceptedEvent getSoupLoginAcceptedEvent(ByteBuffer buffer) {
		SoupLoginAcceptedByteBufferMessage msg = new SoupLoginAcceptedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupLoginRejectedCommand getSoupLoginRejectedCommand() {
		SoupLoginRejectedByteBufferMessage msg = new SoupLoginRejectedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupLoginRejectedCommand getSoupLoginRejectedCommand(ByteBuffer buffer) {
		SoupLoginRejectedByteBufferMessage msg = new SoupLoginRejectedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupLoginRejectedEvent getSoupLoginRejectedEvent(ByteBuffer buffer) {
		SoupLoginRejectedByteBufferMessage msg = new SoupLoginRejectedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupSequencedDataCommand getSoupSequencedDataCommand() {
		SoupSequencedDataByteBufferMessage msg = new SoupSequencedDataByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupSequencedDataCommand getSoupSequencedDataCommand(ByteBuffer buffer) {
		SoupSequencedDataByteBufferMessage msg = new SoupSequencedDataByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupSequencedDataEvent getSoupSequencedDataEvent(ByteBuffer buffer) {
		SoupSequencedDataByteBufferMessage msg = new SoupSequencedDataByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupServerHeartbeatCommand getSoupServerHeartbeatCommand() {
		SoupServerHeartbeatByteBufferMessage msg = new SoupServerHeartbeatByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupServerHeartbeatCommand getSoupServerHeartbeatCommand(ByteBuffer buffer) {
		SoupServerHeartbeatByteBufferMessage msg = new SoupServerHeartbeatByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupServerHeartbeatEvent getSoupServerHeartbeatEvent(ByteBuffer buffer) {
		SoupServerHeartbeatByteBufferMessage msg = new SoupServerHeartbeatByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupEndOfSessionCommand getSoupEndOfSessionCommand() {
		SoupEndOfSessionByteBufferMessage msg = new SoupEndOfSessionByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupEndOfSessionCommand getSoupEndOfSessionCommand(ByteBuffer buffer) {
		SoupEndOfSessionByteBufferMessage msg = new SoupEndOfSessionByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupEndOfSessionEvent getSoupEndOfSessionEvent(ByteBuffer buffer) {
		SoupEndOfSessionByteBufferMessage msg = new SoupEndOfSessionByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupLoginRequestCommand getSoupLoginRequestCommand() {
		SoupLoginRequestByteBufferMessage msg = new SoupLoginRequestByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupLoginRequestCommand getSoupLoginRequestCommand(ByteBuffer buffer) {
		SoupLoginRequestByteBufferMessage msg = new SoupLoginRequestByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupLoginRequestEvent getSoupLoginRequestEvent(ByteBuffer buffer) {
		SoupLoginRequestByteBufferMessage msg = new SoupLoginRequestByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupUnsequencedDataCommand getSoupUnsequencedDataCommand() {
		SoupUnsequencedDataByteBufferMessage msg = new SoupUnsequencedDataByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupUnsequencedDataCommand getSoupUnsequencedDataCommand(ByteBuffer buffer) {
		SoupUnsequencedDataByteBufferMessage msg = new SoupUnsequencedDataByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupUnsequencedDataEvent getSoupUnsequencedDataEvent(ByteBuffer buffer) {
		SoupUnsequencedDataByteBufferMessage msg = new SoupUnsequencedDataByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupClientHeartbeatCommand getSoupClientHeartbeatCommand() {
		SoupClientHeartbeatByteBufferMessage msg = new SoupClientHeartbeatByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupClientHeartbeatCommand getSoupClientHeartbeatCommand(ByteBuffer buffer) {
		SoupClientHeartbeatByteBufferMessage msg = new SoupClientHeartbeatByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupClientHeartbeatEvent getSoupClientHeartbeatEvent(ByteBuffer buffer) {
		SoupClientHeartbeatByteBufferMessage msg = new SoupClientHeartbeatByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public SoupLogoutRequestCommand getSoupLogoutRequestCommand() {
		SoupLogoutRequestByteBufferMessage msg = new SoupLogoutRequestByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public SoupLogoutRequestCommand getSoupLogoutRequestCommand(ByteBuffer buffer) {
		SoupLogoutRequestByteBufferMessage msg = new SoupLogoutRequestByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public SoupLogoutRequestEvent getSoupLogoutRequestEvent(ByteBuffer buffer) {
		SoupLogoutRequestByteBufferMessage msg = new SoupLogoutRequestByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }
}
