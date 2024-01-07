package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public class OUCH2ByteBufferMessages implements OUCH2Messages {
    private final ByteBuffer temp = ByteBuffer.allocateDirect(1500);

    private final OUCH2TradeConfirmationByteBufferMessage cmdTradeConfirmation = new OUCH2TradeConfirmationByteBufferMessage(); 
    private final OUCH2TradeConfirmationByteBufferMessage eventTradeConfirmation = new OUCH2TradeConfirmationByteBufferMessage(); 
    private final OUCH2OrderByteBufferMessage cmdOrder = new OUCH2OrderByteBufferMessage(); 
    private final OUCH2OrderByteBufferMessage eventOrder = new OUCH2OrderByteBufferMessage(); 
    private final OUCH2CancelByteBufferMessage cmdCancel = new OUCH2CancelByteBufferMessage(); 
    private final OUCH2CancelByteBufferMessage eventCancel = new OUCH2CancelByteBufferMessage(); 
    private final OUCH2ReplaceByteBufferMessage cmdReplace = new OUCH2ReplaceByteBufferMessage(); 
    private final OUCH2ReplaceByteBufferMessage eventReplace = new OUCH2ReplaceByteBufferMessage(); 
    private final OUCH2AcceptedByteBufferMessage cmdAccepted = new OUCH2AcceptedByteBufferMessage(); 
    private final OUCH2AcceptedByteBufferMessage eventAccepted = new OUCH2AcceptedByteBufferMessage(); 
    private final OUCH2CanceledByteBufferMessage cmdCanceled = new OUCH2CanceledByteBufferMessage(); 
    private final OUCH2CanceledByteBufferMessage eventCanceled = new OUCH2CanceledByteBufferMessage(); 
    private final OUCH2ReplacedByteBufferMessage cmdReplaced = new OUCH2ReplacedByteBufferMessage(); 
    private final OUCH2ReplacedByteBufferMessage eventReplaced = new OUCH2ReplacedByteBufferMessage(); 
    private final OUCH2CancelRejectedByteBufferMessage cmdCancelRejected = new OUCH2CancelRejectedByteBufferMessage(); 
    private final OUCH2CancelRejectedByteBufferMessage eventCancelRejected = new OUCH2CancelRejectedByteBufferMessage(); 
    private final OUCH2RejectedByteBufferMessage cmdRejected = new OUCH2RejectedByteBufferMessage(); 
    private final OUCH2RejectedByteBufferMessage eventRejected = new OUCH2RejectedByteBufferMessage(); 
    private final OUCH2FillByteBufferMessage cmdFill = new OUCH2FillByteBufferMessage(); 
    private final OUCH2FillByteBufferMessage eventFill = new OUCH2FillByteBufferMessage(); 
 
    @Override
    public OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand() {
        temp.clear();
        return cmdTradeConfirmation.wrapCommand(temp);
    }

    @Override
    public OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand(ByteBuffer buffer) {
        return cmdTradeConfirmation.wrapCommand(buffer);
    }

    @Override
    public OUCH2TradeConfirmationEvent getOUCH2TradeConfirmationEvent(ByteBuffer buffer) {
        return eventTradeConfirmation.wrapEvent(buffer);
    }
    @Override
    public OUCH2OrderCommand getOUCH2OrderCommand() {
        temp.clear();
        return cmdOrder.wrapCommand(temp);
    }

    @Override
    public OUCH2OrderCommand getOUCH2OrderCommand(ByteBuffer buffer) {
        return cmdOrder.wrapCommand(buffer);
    }

    @Override
    public OUCH2OrderEvent getOUCH2OrderEvent(ByteBuffer buffer) {
        return eventOrder.wrapEvent(buffer);
    }
    @Override
    public OUCH2CancelCommand getOUCH2CancelCommand() {
        temp.clear();
        return cmdCancel.wrapCommand(temp);
    }

    @Override
    public OUCH2CancelCommand getOUCH2CancelCommand(ByteBuffer buffer) {
        return cmdCancel.wrapCommand(buffer);
    }

    @Override
    public OUCH2CancelEvent getOUCH2CancelEvent(ByteBuffer buffer) {
        return eventCancel.wrapEvent(buffer);
    }
    @Override
    public OUCH2ReplaceCommand getOUCH2ReplaceCommand() {
        temp.clear();
        return cmdReplace.wrapCommand(temp);
    }

    @Override
    public OUCH2ReplaceCommand getOUCH2ReplaceCommand(ByteBuffer buffer) {
        return cmdReplace.wrapCommand(buffer);
    }

    @Override
    public OUCH2ReplaceEvent getOUCH2ReplaceEvent(ByteBuffer buffer) {
        return eventReplace.wrapEvent(buffer);
    }
    @Override
    public OUCH2AcceptedCommand getOUCH2AcceptedCommand() {
        temp.clear();
        return cmdAccepted.wrapCommand(temp);
    }

    @Override
    public OUCH2AcceptedCommand getOUCH2AcceptedCommand(ByteBuffer buffer) {
        return cmdAccepted.wrapCommand(buffer);
    }

    @Override
    public OUCH2AcceptedEvent getOUCH2AcceptedEvent(ByteBuffer buffer) {
        return eventAccepted.wrapEvent(buffer);
    }
    @Override
    public OUCH2CanceledCommand getOUCH2CanceledCommand() {
        temp.clear();
        return cmdCanceled.wrapCommand(temp);
    }

    @Override
    public OUCH2CanceledCommand getOUCH2CanceledCommand(ByteBuffer buffer) {
        return cmdCanceled.wrapCommand(buffer);
    }

    @Override
    public OUCH2CanceledEvent getOUCH2CanceledEvent(ByteBuffer buffer) {
        return eventCanceled.wrapEvent(buffer);
    }
    @Override
    public OUCH2ReplacedCommand getOUCH2ReplacedCommand() {
        temp.clear();
        return cmdReplaced.wrapCommand(temp);
    }

    @Override
    public OUCH2ReplacedCommand getOUCH2ReplacedCommand(ByteBuffer buffer) {
        return cmdReplaced.wrapCommand(buffer);
    }

    @Override
    public OUCH2ReplacedEvent getOUCH2ReplacedEvent(ByteBuffer buffer) {
        return eventReplaced.wrapEvent(buffer);
    }
    @Override
    public OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand() {
        temp.clear();
        return cmdCancelRejected.wrapCommand(temp);
    }

    @Override
    public OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand(ByteBuffer buffer) {
        return cmdCancelRejected.wrapCommand(buffer);
    }

    @Override
    public OUCH2CancelRejectedEvent getOUCH2CancelRejectedEvent(ByteBuffer buffer) {
        return eventCancelRejected.wrapEvent(buffer);
    }
    @Override
    public OUCH2RejectedCommand getOUCH2RejectedCommand() {
        temp.clear();
        return cmdRejected.wrapCommand(temp);
    }

    @Override
    public OUCH2RejectedCommand getOUCH2RejectedCommand(ByteBuffer buffer) {
        return cmdRejected.wrapCommand(buffer);
    }

    @Override
    public OUCH2RejectedEvent getOUCH2RejectedEvent(ByteBuffer buffer) {
        return eventRejected.wrapEvent(buffer);
    }
    @Override
    public OUCH2FillCommand getOUCH2FillCommand() {
        temp.clear();
        return cmdFill.wrapCommand(temp);
    }

    @Override
    public OUCH2FillCommand getOUCH2FillCommand(ByteBuffer buffer) {
        return cmdFill.wrapCommand(buffer);
    }

    @Override
    public OUCH2FillEvent getOUCH2FillEvent(ByteBuffer buffer) {
        return eventFill.wrapEvent(buffer);
    }
}
