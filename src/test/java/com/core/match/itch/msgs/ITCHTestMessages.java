package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public class ITCHTestMessages implements ITCHMessages {
    public <T> T get(Class<T> cls, ByteBuffer buffer) {
        if (cls.equals(ITCHSystemEvent.class)) {
            return (T)getITCHSystemEvent(buffer);
        }
        if (cls.equals(ITCHSecurityEvent.class)) {
            return (T)getITCHSecurityEvent(buffer);
        }
        if (cls.equals(ITCHOrderEvent.class)) {
            return (T)getITCHOrderEvent(buffer);
        }
        if (cls.equals(ITCHOrderCancelEvent.class)) {
            return (T)getITCHOrderCancelEvent(buffer);
        }
        if (cls.equals(ITCHOrderExecutedEvent.class)) {
            return (T)getITCHOrderExecutedEvent(buffer);
        }
        if (cls.equals(ITCHTradeEvent.class)) {
            return (T)getITCHTradeEvent(buffer);
        }
        return null;
    }

    @Override
    public ITCHSystemCommand getITCHSystemCommand() {
		ITCHSystemByteBufferMessage msg = new ITCHSystemByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHSystemCommand getITCHSystemCommand(ByteBuffer buffer) {
		ITCHSystemByteBufferMessage msg = new ITCHSystemByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHSystemEvent getITCHSystemEvent(ByteBuffer buffer) {
		ITCHSystemByteBufferMessage msg = new ITCHSystemByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public ITCHSecurityCommand getITCHSecurityCommand() {
		ITCHSecurityByteBufferMessage msg = new ITCHSecurityByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHSecurityCommand getITCHSecurityCommand(ByteBuffer buffer) {
		ITCHSecurityByteBufferMessage msg = new ITCHSecurityByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHSecurityEvent getITCHSecurityEvent(ByteBuffer buffer) {
		ITCHSecurityByteBufferMessage msg = new ITCHSecurityByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public ITCHOrderCommand getITCHOrderCommand() {
		ITCHOrderByteBufferMessage msg = new ITCHOrderByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHOrderCommand getITCHOrderCommand(ByteBuffer buffer) {
		ITCHOrderByteBufferMessage msg = new ITCHOrderByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHOrderEvent getITCHOrderEvent(ByteBuffer buffer) {
		ITCHOrderByteBufferMessage msg = new ITCHOrderByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public ITCHOrderCancelCommand getITCHOrderCancelCommand() {
		ITCHOrderCancelByteBufferMessage msg = new ITCHOrderCancelByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHOrderCancelCommand getITCHOrderCancelCommand(ByteBuffer buffer) {
		ITCHOrderCancelByteBufferMessage msg = new ITCHOrderCancelByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHOrderCancelEvent getITCHOrderCancelEvent(ByteBuffer buffer) {
		ITCHOrderCancelByteBufferMessage msg = new ITCHOrderCancelByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public ITCHOrderExecutedCommand getITCHOrderExecutedCommand() {
		ITCHOrderExecutedByteBufferMessage msg = new ITCHOrderExecutedByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHOrderExecutedCommand getITCHOrderExecutedCommand(ByteBuffer buffer) {
		ITCHOrderExecutedByteBufferMessage msg = new ITCHOrderExecutedByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHOrderExecutedEvent getITCHOrderExecutedEvent(ByteBuffer buffer) {
		ITCHOrderExecutedByteBufferMessage msg = new ITCHOrderExecutedByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public ITCHTradeCommand getITCHTradeCommand() {
		ITCHTradeByteBufferMessage msg = new ITCHTradeByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public ITCHTradeCommand getITCHTradeCommand(ByteBuffer buffer) {
		ITCHTradeByteBufferMessage msg = new ITCHTradeByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public ITCHTradeEvent getITCHTradeEvent(ByteBuffer buffer) {
		ITCHTradeByteBufferMessage msg = new ITCHTradeByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }
}
