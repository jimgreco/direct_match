package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

public interface OUCHMessages {
    OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand();
    OUCHTradeConfirmationCommand getOUCHTradeConfirmationCommand(ByteBuffer buffer);
    OUCHTradeConfirmationEvent getOUCHTradeConfirmationEvent(ByteBuffer buffer);
    OUCHOrderCommand getOUCHOrderCommand();
    OUCHOrderCommand getOUCHOrderCommand(ByteBuffer buffer);
    OUCHOrderEvent getOUCHOrderEvent(ByteBuffer buffer);
    OUCHCancelCommand getOUCHCancelCommand();
    OUCHCancelCommand getOUCHCancelCommand(ByteBuffer buffer);
    OUCHCancelEvent getOUCHCancelEvent(ByteBuffer buffer);
    OUCHReplaceCommand getOUCHReplaceCommand();
    OUCHReplaceCommand getOUCHReplaceCommand(ByteBuffer buffer);
    OUCHReplaceEvent getOUCHReplaceEvent(ByteBuffer buffer);
    OUCHAcceptedCommand getOUCHAcceptedCommand();
    OUCHAcceptedCommand getOUCHAcceptedCommand(ByteBuffer buffer);
    OUCHAcceptedEvent getOUCHAcceptedEvent(ByteBuffer buffer);
    OUCHCanceledCommand getOUCHCanceledCommand();
    OUCHCanceledCommand getOUCHCanceledCommand(ByteBuffer buffer);
    OUCHCanceledEvent getOUCHCanceledEvent(ByteBuffer buffer);
    OUCHReplacedCommand getOUCHReplacedCommand();
    OUCHReplacedCommand getOUCHReplacedCommand(ByteBuffer buffer);
    OUCHReplacedEvent getOUCHReplacedEvent(ByteBuffer buffer);
    OUCHCancelRejectedCommand getOUCHCancelRejectedCommand();
    OUCHCancelRejectedCommand getOUCHCancelRejectedCommand(ByteBuffer buffer);
    OUCHCancelRejectedEvent getOUCHCancelRejectedEvent(ByteBuffer buffer);
    OUCHRejectedCommand getOUCHRejectedCommand();
    OUCHRejectedCommand getOUCHRejectedCommand(ByteBuffer buffer);
    OUCHRejectedEvent getOUCHRejectedEvent(ByteBuffer buffer);
    OUCHFillCommand getOUCHFillCommand();
    OUCHFillCommand getOUCHFillCommand(ByteBuffer buffer);
    OUCHFillEvent getOUCHFillEvent(ByteBuffer buffer);
} 
