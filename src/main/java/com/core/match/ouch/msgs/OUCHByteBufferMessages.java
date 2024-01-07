package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public class OUCHByteBufferMessages implements OUCHMessages {
    private final ByteBuffer temp = ByteBuffer.allocateDirect(1500);

    private final OUCHTradeConfirmationByteBufferMessage cmdTradeConfirmation = new OUCHTradeConfirmationByteBufferMessage(); 
    private final OUCHTradeConfirmationByteBufferMessage eventTradeConfirmation = new OUCHTradeConfirmationByteBufferMessage(); 
    private final OUCHOrderByteBufferMessage cmdOrder = new OUCHOrderByteBufferMessage(); 
    private final OUCHOrderByteBufferMessage eventOrder = new OUCHOrderByteBufferMessage(); 
    private final OUCHCancelByteBufferMessage cmdCancel = new OUCHCancelByteBufferMessage(); 
    private final OUCHCancelByteBufferMessage eventCancel = new OUCHCancelByteBufferMessage(); 
    private final OUCHReplaceByteBufferMessage cmdReplace = new OUCHReplaceByteBufferMessage(); 
    private final OUCHReplaceByteBufferMessage eventReplace = new OUCHReplaceByteBufferMessage(); 
    private final OUCHAcceptedByteBufferMessage cmdAccepted = new OUCHAcceptedByteBufferMessage(); 
    private final OUCHAcceptedByteBufferMessage eventAccepted = new OUCHAcceptedByteBufferMessage(); 
    private final OUCHCanceledByteBufferMessage cmdCanceled = new OUCHCanceledByteBufferMessage(); 
    private final OUCHCanceledByteBufferMessage eventCanceled = new OUCHCanceledByteBufferMessage(); 
    private final OUCHReplacedByteBufferMessage cmdReplaced = new OUCHReplacedByteBufferMessage(); 
    private final OUCHReplacedByteBufferMessage eventReplaced = new OUCHReplacedByteBufferMessage(); 
    private final OUCHCancelRejectedByteBufferMessage cmdCancelRejected = new OUCHCancelRejectedByteBufferMessage(); 
    private final OUCHCancelRejectedByteBufferMessage eventCancelRejected = new OUCHCancelRejectedByteBufferMessage(); 
    private final OUCHRejectedByteBufferMessage cmdRejected = new OUCHRejectedByteBufferMessage(); 
    private final OUCHRejectedByteBufferMessage eventRejected = new OUCHRejectedByteBufferMessage(); 
    private final OUCHFillByteBufferMessage cmdFill = new OUCHFillByteBufferMessage(); 
    private final OUCHFillByteBufferMessage eventFill = new OUCHFillByteBufferMessage(); 
 
    @Override
    public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand() {
        temp.clear();
        return cmdTradeConfirmation.wrapCommand(temp);
    }

    @Override
    public OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand(ByteBuffer buffer) {
        return cmdTradeConfirmation.wrapCommand(buffer);
    }

    @Override
    public OUCHTradeConfirmationEvent getOUCHTradeConfirmationEvent(ByteBuffer buffer) {
        return eventTradeConfirmation.wrapEvent(buffer);
    }
    @Override
    public OUCHOrderCommand getOUCHOrderCommand() {
        temp.clear();
        return cmdOrder.wrapCommand(temp);
    }

    @Override
    public OUCHOrderCommand getOUCHOrderCommand(ByteBuffer buffer) {
        return cmdOrder.wrapCommand(buffer);
    }

    @Override
    public OUCHOrderEvent getOUCHOrderEvent(ByteBuffer buffer) {
        return eventOrder.wrapEvent(buffer);
    }
    @Override
    public OUCHCancelCommand getOUCHCancelCommand() {
        temp.clear();
        return cmdCancel.wrapCommand(temp);
    }

    @Override
    public OUCHCancelCommand getOUCHCancelCommand(ByteBuffer buffer) {
        return cmdCancel.wrapCommand(buffer);
    }

    @Override
    public OUCHCancelEvent getOUCHCancelEvent(ByteBuffer buffer) {
        return eventCancel.wrapEvent(buffer);
    }
    @Override
    public OUCHReplaceCommand getOUCHReplaceCommand() {
        temp.clear();
        return cmdReplace.wrapCommand(temp);
    }

    @Override
    public OUCHReplaceCommand getOUCHReplaceCommand(ByteBuffer buffer) {
        return cmdReplace.wrapCommand(buffer);
    }

    @Override
    public OUCHReplaceEvent getOUCHReplaceEvent(ByteBuffer buffer) {
        return eventReplace.wrapEvent(buffer);
    }
    @Override
    public OUCHAcceptedCommand getOUCHAcceptedCommand() {
        temp.clear();
        return cmdAccepted.wrapCommand(temp);
    }

    @Override
    public OUCHAcceptedCommand getOUCHAcceptedCommand(ByteBuffer buffer) {
        return cmdAccepted.wrapCommand(buffer);
    }

    @Override
    public OUCHAcceptedEvent getOUCHAcceptedEvent(ByteBuffer buffer) {
        return eventAccepted.wrapEvent(buffer);
    }
    @Override
    public OUCHCanceledCommand getOUCHCanceledCommand() {
        temp.clear();
        return cmdCanceled.wrapCommand(temp);
    }

    @Override
    public OUCHCanceledCommand getOUCHCanceledCommand(ByteBuffer buffer) {
        return cmdCanceled.wrapCommand(buffer);
    }

    @Override
    public OUCHCanceledEvent getOUCHCanceledEvent(ByteBuffer buffer) {
        return eventCanceled.wrapEvent(buffer);
    }
    @Override
    public OUCHReplacedCommand getOUCHReplacedCommand() {
        temp.clear();
        return cmdReplaced.wrapCommand(temp);
    }

    @Override
    public OUCHReplacedCommand getOUCHReplacedCommand(ByteBuffer buffer) {
        return cmdReplaced.wrapCommand(buffer);
    }

    @Override
    public OUCHReplacedEvent getOUCHReplacedEvent(ByteBuffer buffer) {
        return eventReplaced.wrapEvent(buffer);
    }
    @Override
    public OUCHCancelRejectedCommand getOUCHCancelRejectedCommand() {
        temp.clear();
        return cmdCancelRejected.wrapCommand(temp);
    }

    @Override
    public OUCHCancelRejectedCommand getOUCHCancelRejectedCommand(ByteBuffer buffer) {
        return cmdCancelRejected.wrapCommand(buffer);
    }

    @Override
    public OUCHCancelRejectedEvent getOUCHCancelRejectedEvent(ByteBuffer buffer) {
        return eventCancelRejected.wrapEvent(buffer);
    }
    @Override
    public OUCHRejectedCommand getOUCHRejectedCommand() {
        temp.clear();
        return cmdRejected.wrapCommand(temp);
    }

    @Override
    public OUCHRejectedCommand getOUCHRejectedCommand(ByteBuffer buffer) {
        return cmdRejected.wrapCommand(buffer);
    }

    @Override
    public OUCHRejectedEvent getOUCHRejectedEvent(ByteBuffer buffer) {
        return eventRejected.wrapEvent(buffer);
    }
    @Override
    public OUCHFillCommand getOUCHFillCommand() {
        temp.clear();
        return cmdFill.wrapCommand(temp);
    }

    @Override
    public OUCHFillCommand getOUCHFillCommand(ByteBuffer buffer) {
        return cmdFill.wrapCommand(buffer);
    }

    @Override
    public OUCHFillEvent getOUCHFillEvent(ByteBuffer buffer) {
        return eventFill.wrapEvent(buffer);
    }
}
