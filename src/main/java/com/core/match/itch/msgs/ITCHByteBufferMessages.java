package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public class ITCHByteBufferMessages implements ITCHMessages {
    private final ByteBuffer temp = ByteBuffer.allocateDirect(1500);

    private final ITCHSystemByteBufferMessage cmdSystem = new ITCHSystemByteBufferMessage(); 
    private final ITCHSystemByteBufferMessage eventSystem = new ITCHSystemByteBufferMessage(); 
    private final ITCHSecurityByteBufferMessage cmdSecurity = new ITCHSecurityByteBufferMessage(); 
    private final ITCHSecurityByteBufferMessage eventSecurity = new ITCHSecurityByteBufferMessage(); 
    private final ITCHOrderByteBufferMessage cmdOrder = new ITCHOrderByteBufferMessage(); 
    private final ITCHOrderByteBufferMessage eventOrder = new ITCHOrderByteBufferMessage(); 
    private final ITCHOrderCancelByteBufferMessage cmdOrderCancel = new ITCHOrderCancelByteBufferMessage(); 
    private final ITCHOrderCancelByteBufferMessage eventOrderCancel = new ITCHOrderCancelByteBufferMessage(); 
    private final ITCHOrderExecutedByteBufferMessage cmdOrderExecuted = new ITCHOrderExecutedByteBufferMessage(); 
    private final ITCHOrderExecutedByteBufferMessage eventOrderExecuted = new ITCHOrderExecutedByteBufferMessage(); 
    private final ITCHTradeByteBufferMessage cmdTrade = new ITCHTradeByteBufferMessage(); 
    private final ITCHTradeByteBufferMessage eventTrade = new ITCHTradeByteBufferMessage(); 
 
    @Override
    public ITCHSystemCommand getITCHSystemCommand() {
        temp.clear();
        return cmdSystem.wrapCommand(temp);
    }

    @Override
    public ITCHSystemCommand getITCHSystemCommand(ByteBuffer buffer) {
        return cmdSystem.wrapCommand(buffer);
    }

    @Override
    public ITCHSystemEvent getITCHSystemEvent(ByteBuffer buffer) {
        return eventSystem.wrapEvent(buffer);
    }
    @Override
    public ITCHSecurityCommand getITCHSecurityCommand() {
        temp.clear();
        return cmdSecurity.wrapCommand(temp);
    }

    @Override
    public ITCHSecurityCommand getITCHSecurityCommand(ByteBuffer buffer) {
        return cmdSecurity.wrapCommand(buffer);
    }

    @Override
    public ITCHSecurityEvent getITCHSecurityEvent(ByteBuffer buffer) {
        return eventSecurity.wrapEvent(buffer);
    }
    @Override
    public ITCHOrderCommand getITCHOrderCommand() {
        temp.clear();
        return cmdOrder.wrapCommand(temp);
    }

    @Override
    public ITCHOrderCommand getITCHOrderCommand(ByteBuffer buffer) {
        return cmdOrder.wrapCommand(buffer);
    }

    @Override
    public ITCHOrderEvent getITCHOrderEvent(ByteBuffer buffer) {
        return eventOrder.wrapEvent(buffer);
    }
    @Override
    public ITCHOrderCancelCommand getITCHOrderCancelCommand() {
        temp.clear();
        return cmdOrderCancel.wrapCommand(temp);
    }

    @Override
    public ITCHOrderCancelCommand getITCHOrderCancelCommand(ByteBuffer buffer) {
        return cmdOrderCancel.wrapCommand(buffer);
    }

    @Override
    public ITCHOrderCancelEvent getITCHOrderCancelEvent(ByteBuffer buffer) {
        return eventOrderCancel.wrapEvent(buffer);
    }
    @Override
    public ITCHOrderExecutedCommand getITCHOrderExecutedCommand() {
        temp.clear();
        return cmdOrderExecuted.wrapCommand(temp);
    }

    @Override
    public ITCHOrderExecutedCommand getITCHOrderExecutedCommand(ByteBuffer buffer) {
        return cmdOrderExecuted.wrapCommand(buffer);
    }

    @Override
    public ITCHOrderExecutedEvent getITCHOrderExecutedEvent(ByteBuffer buffer) {
        return eventOrderExecuted.wrapEvent(buffer);
    }
    @Override
    public ITCHTradeCommand getITCHTradeCommand() {
        temp.clear();
        return cmdTrade.wrapCommand(temp);
    }

    @Override
    public ITCHTradeCommand getITCHTradeCommand(ByteBuffer buffer) {
        return cmdTrade.wrapCommand(buffer);
    }

    @Override
    public ITCHTradeEvent getITCHTradeEvent(ByteBuffer buffer) {
        return eventTrade.wrapEvent(buffer);
    }
}
