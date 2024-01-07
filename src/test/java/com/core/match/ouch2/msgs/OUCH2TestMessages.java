package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2TestMessages implements OUCH2Messages {
    public <T> T get(Class<T> cls, ByteBuffer buffer) {
        if (cls.equals(OUCH2TradeConfirmationEvent.class)) {
            return (T)getOUCH2TradeConfirmationEvent(buffer);
        }
        if (cls.equals(OUCH2OrderEvent.class)) {
            return (T)getOUCH2OrderEvent(buffer);
        }
        if (cls.equals(OUCH2CancelEvent.class)) {
            return (T)getOUCH2CancelEvent(buffer);
        }
        if (cls.equals(OUCH2ReplaceEvent.class)) {
            return (T)getOUCH2ReplaceEvent(buffer);
        }
        if (cls.equals(OUCH2AcceptedEvent.class)) {
            return (T)getOUCH2AcceptedEvent(buffer);
        }
        if (cls.equals(OUCH2CanceledEvent.class)) {
            return (T)getOUCH2CanceledEvent(buffer);
        }
        if (cls.equals(OUCH2ReplacedEvent.class)) {
            return (T)getOUCH2ReplacedEvent(buffer);
        }
        if (cls.equals(OUCH2CancelRejectedEvent.class)) {
            return (T)getOUCH2CancelRejectedEvent(buffer);
        }
        if (cls.equals(OUCH2RejectedEvent.class)) {
            return (T)getOUCH2RejectedEvent(buffer);
        }
        if (cls.equals(OUCH2FillEvent.class)) {
            return (T)getOUCH2FillEvent(buffer);
        }
        return null;
    }

    @Override
    public OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand() {
		OUCH2TradeConfirmationByteBufferMessage msg = new OUCH2TradeConfirmationByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand(ByteBuffer buffer) {
		OUCH2TradeConfirmationByteBufferMessage msg = new OUCH2TradeConfirmationByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2TradeConfirmationEvent getOUCH2TradeConfirmationEvent(ByteBuffer buffer) {
		OUCH2TradeConfirmationByteBufferMessage msg = new OUCH2TradeConfirmationByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2OrderCommand getOUCH2OrderCommand() {
		OUCH2OrderByteBufferMessage msg = new OUCH2OrderByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2OrderCommand getOUCH2OrderCommand(ByteBuffer buffer) {
		OUCH2OrderByteBufferMessage msg = new OUCH2OrderByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2OrderEvent getOUCH2OrderEvent(ByteBuffer buffer) {
		OUCH2OrderByteBufferMessage msg = new OUCH2OrderByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2CancelCommand getOUCH2CancelCommand() {
		OUCH2CancelByteBufferMessage msg = new OUCH2CancelByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2CancelCommand getOUCH2CancelCommand(ByteBuffer buffer) {
		OUCH2CancelByteBufferMessage msg = new OUCH2CancelByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2CancelEvent getOUCH2CancelEvent(ByteBuffer buffer) {
		OUCH2CancelByteBufferMessage msg = new OUCH2CancelByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2ReplaceCommand getOUCH2ReplaceCommand() {
		OUCH2ReplaceByteBufferMessage msg = new OUCH2ReplaceByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2ReplaceCommand getOUCH2ReplaceCommand(ByteBuffer buffer) {
		OUCH2ReplaceByteBufferMessage msg = new OUCH2ReplaceByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2ReplaceEvent getOUCH2ReplaceEvent(ByteBuffer buffer) {
		OUCH2ReplaceByteBufferMessage msg = new OUCH2ReplaceByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2AcceptedCommand getOUCH2AcceptedCommand() {
		OUCH2AcceptedByteBufferMessage msg = new OUCH2AcceptedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2AcceptedCommand getOUCH2AcceptedCommand(ByteBuffer buffer) {
		OUCH2AcceptedByteBufferMessage msg = new OUCH2AcceptedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2AcceptedEvent getOUCH2AcceptedEvent(ByteBuffer buffer) {
		OUCH2AcceptedByteBufferMessage msg = new OUCH2AcceptedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2CanceledCommand getOUCH2CanceledCommand() {
		OUCH2CanceledByteBufferMessage msg = new OUCH2CanceledByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2CanceledCommand getOUCH2CanceledCommand(ByteBuffer buffer) {
		OUCH2CanceledByteBufferMessage msg = new OUCH2CanceledByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2CanceledEvent getOUCH2CanceledEvent(ByteBuffer buffer) {
		OUCH2CanceledByteBufferMessage msg = new OUCH2CanceledByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2ReplacedCommand getOUCH2ReplacedCommand() {
		OUCH2ReplacedByteBufferMessage msg = new OUCH2ReplacedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2ReplacedCommand getOUCH2ReplacedCommand(ByteBuffer buffer) {
		OUCH2ReplacedByteBufferMessage msg = new OUCH2ReplacedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2ReplacedEvent getOUCH2ReplacedEvent(ByteBuffer buffer) {
		OUCH2ReplacedByteBufferMessage msg = new OUCH2ReplacedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand() {
		OUCH2CancelRejectedByteBufferMessage msg = new OUCH2CancelRejectedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand(ByteBuffer buffer) {
		OUCH2CancelRejectedByteBufferMessage msg = new OUCH2CancelRejectedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2CancelRejectedEvent getOUCH2CancelRejectedEvent(ByteBuffer buffer) {
		OUCH2CancelRejectedByteBufferMessage msg = new OUCH2CancelRejectedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2RejectedCommand getOUCH2RejectedCommand() {
		OUCH2RejectedByteBufferMessage msg = new OUCH2RejectedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2RejectedCommand getOUCH2RejectedCommand(ByteBuffer buffer) {
		OUCH2RejectedByteBufferMessage msg = new OUCH2RejectedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2RejectedEvent getOUCH2RejectedEvent(ByteBuffer buffer) {
		OUCH2RejectedByteBufferMessage msg = new OUCH2RejectedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public OUCH2FillCommand getOUCH2FillCommand() {
		OUCH2FillByteBufferMessage msg = new OUCH2FillByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public OUCH2FillCommand getOUCH2FillCommand(ByteBuffer buffer) {
		OUCH2FillByteBufferMessage msg = new OUCH2FillByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public OUCH2FillEvent getOUCH2FillEvent(ByteBuffer buffer) {
		OUCH2FillByteBufferMessage msg = new OUCH2FillByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }
}
