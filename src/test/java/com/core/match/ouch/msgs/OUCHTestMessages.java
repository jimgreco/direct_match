package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHTestMessages implements OUCHMessages {
    public <T> T get(Class<T> cls, ByteBuffer buffer) {
        if (cls.equals(OUCHTradeConfirmationEvent.class)) {
            return (T)getOUCHTradeConfirmationEvent(buffer);
        }
        if (cls.equals(OUCHOrderEvent.class)) {
            return (T)getOUCHOrderEvent(buffer);
        }
        if (cls.equals(OUCHCancelEvent.class)) {
            return (T)getOUCHCancelEvent(buffer);
        }
        if (cls.equals(OUCHReplaceEvent.class)) {
            return (T)getOUCHReplaceEvent(buffer);
        }
        if (cls.equals(OUCHAcceptedEvent.class)) {
            return (T)getOUCHAcceptedEvent(buffer);
        }
        if (cls.equals(OUCHCanceledEvent.class)) {
            return (T)getOUCHCanceledEvent(buffer);
        }
        if (cls.equals(OUCHReplacedEvent.class)) {
            return (T)getOUCHReplacedEvent(buffer);
        }
        if (cls.equals(OUCHCancelRejectedEvent.class)) {
            return (T)getOUCHCancelRejectedEvent(buffer);
        }
        if (cls.equals(OUCHRejectedEvent.class)) {
            return (T)getOUCHRejectedEvent(buffer);
        }
        if (cls.equals(OUCHFillEvent.class)) {
            return (T)getOUCHFillEvent(buffer);
        }
        return null;
    }

    @Override
    public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand() {
		OUCHTradeConfirmationByteBufferMessage msg = new OUCHTradeConfirmationByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand(ByteBuffer buffer) {
		OUCHTradeConfirmationByteBufferMessage msg = new OUCHTradeConfirmationByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHTradeConfirmationEvent getOUCHTradeConfirmationEvent(ByteBuffer buffer) {
		OUCHTradeConfirmationByteBufferMessage msg = new OUCHTradeConfirmationByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHOrderCommand getOUCHOrderCommand() {
		OUCHOrderByteBufferMessage msg = new OUCHOrderByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHOrderCommand getOUCHOrderCommand(ByteBuffer buffer) {
		OUCHOrderByteBufferMessage msg = new OUCHOrderByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHOrderEvent getOUCHOrderEvent(ByteBuffer buffer) {
		OUCHOrderByteBufferMessage msg = new OUCHOrderByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHCancelCommand getOUCHCancelCommand() {
		OUCHCancelByteBufferMessage msg = new OUCHCancelByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHCancelCommand getOUCHCancelCommand(ByteBuffer buffer) {
		OUCHCancelByteBufferMessage msg = new OUCHCancelByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHCancelEvent getOUCHCancelEvent(ByteBuffer buffer) {
		OUCHCancelByteBufferMessage msg = new OUCHCancelByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHReplaceCommand getOUCHReplaceCommand() {
		OUCHReplaceByteBufferMessage msg = new OUCHReplaceByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHReplaceCommand getOUCHReplaceCommand(ByteBuffer buffer) {
		OUCHReplaceByteBufferMessage msg = new OUCHReplaceByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHReplaceEvent getOUCHReplaceEvent(ByteBuffer buffer) {
		OUCHReplaceByteBufferMessage msg = new OUCHReplaceByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHAcceptedCommand getOUCHAcceptedCommand() {
		OUCHAcceptedByteBufferMessage msg = new OUCHAcceptedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHAcceptedCommand getOUCHAcceptedCommand(ByteBuffer buffer) {
		OUCHAcceptedByteBufferMessage msg = new OUCHAcceptedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHAcceptedEvent getOUCHAcceptedEvent(ByteBuffer buffer) {
		OUCHAcceptedByteBufferMessage msg = new OUCHAcceptedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHCanceledCommand getOUCHCanceledCommand() {
		OUCHCanceledByteBufferMessage msg = new OUCHCanceledByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHCanceledCommand getOUCHCanceledCommand(ByteBuffer buffer) {
		OUCHCanceledByteBufferMessage msg = new OUCHCanceledByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHCanceledEvent getOUCHCanceledEvent(ByteBuffer buffer) {
		OUCHCanceledByteBufferMessage msg = new OUCHCanceledByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHReplacedCommand getOUCHReplacedCommand() {
		OUCHReplacedByteBufferMessage msg = new OUCHReplacedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHReplacedCommand getOUCHReplacedCommand(ByteBuffer buffer) {
		OUCHReplacedByteBufferMessage msg = new OUCHReplacedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHReplacedEvent getOUCHReplacedEvent(ByteBuffer buffer) {
		OUCHReplacedByteBufferMessage msg = new OUCHReplacedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHCancelRejectedCommand getOUCHCancelRejectedCommand() {
		OUCHCancelRejectedByteBufferMessage msg = new OUCHCancelRejectedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHCancelRejectedCommand getOUCHCancelRejectedCommand(ByteBuffer buffer) {
		OUCHCancelRejectedByteBufferMessage msg = new OUCHCancelRejectedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHCancelRejectedEvent getOUCHCancelRejectedEvent(ByteBuffer buffer) {
		OUCHCancelRejectedByteBufferMessage msg = new OUCHCancelRejectedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHRejectedCommand getOUCHRejectedCommand() {
		OUCHRejectedByteBufferMessage msg = new OUCHRejectedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHRejectedCommand getOUCHRejectedCommand(ByteBuffer buffer) {
		OUCHRejectedByteBufferMessage msg = new OUCHRejectedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHRejectedEvent getOUCHRejectedEvent(ByteBuffer buffer) {
		OUCHRejectedByteBufferMessage msg = new OUCHRejectedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCHFillCommand getOUCHFillCommand() {
		OUCHFillByteBufferMessage msg = new OUCHFillByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCHFillCommand getOUCHFillCommand(ByteBuffer buffer) {
		OUCHFillByteBufferMessage msg = new OUCHFillByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCHFillEvent getOUCHFillEvent(ByteBuffer buffer) {
		OUCHFillByteBufferMessage msg = new OUCHFillByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }
}
