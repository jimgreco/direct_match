package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

public interface OUCH2Messages {
    OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand();
    OUCH2TradeConfirmationCommand getOUCH2TradeConfirmationCommand(ByteBuffer buffer);
    OUCH2TradeConfirmationEvent getOUCH2TradeConfirmationEvent(ByteBuffer buffer);
    OUCH2OrderCommand getOUCH2OrderCommand();
    OUCH2OrderCommand getOUCH2OrderCommand(ByteBuffer buffer);
    OUCH2OrderEvent getOUCH2OrderEvent(ByteBuffer buffer);
    OUCH2CancelCommand getOUCH2CancelCommand();
    OUCH2CancelCommand getOUCH2CancelCommand(ByteBuffer buffer);
    OUCH2CancelEvent getOUCH2CancelEvent(ByteBuffer buffer);
    OUCH2ReplaceCommand getOUCH2ReplaceCommand();
    OUCH2ReplaceCommand getOUCH2ReplaceCommand(ByteBuffer buffer);
    OUCH2ReplaceEvent getOUCH2ReplaceEvent(ByteBuffer buffer);
    OUCH2AcceptedCommand getOUCH2AcceptedCommand();
    OUCH2AcceptedCommand getOUCH2AcceptedCommand(ByteBuffer buffer);
    OUCH2AcceptedEvent getOUCH2AcceptedEvent(ByteBuffer buffer);
    OUCH2CanceledCommand getOUCH2CanceledCommand();
    OUCH2CanceledCommand getOUCH2CanceledCommand(ByteBuffer buffer);
    OUCH2CanceledEvent getOUCH2CanceledEvent(ByteBuffer buffer);
    OUCH2ReplacedCommand getOUCH2ReplacedCommand();
    OUCH2ReplacedCommand getOUCH2ReplacedCommand(ByteBuffer buffer);
    OUCH2ReplacedEvent getOUCH2ReplacedEvent(ByteBuffer buffer);
    OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand();
    OUCH2CancelRejectedCommand getOUCH2CancelRejectedCommand(ByteBuffer buffer);
    OUCH2CancelRejectedEvent getOUCH2CancelRejectedEvent(ByteBuffer buffer);
    OUCH2RejectedCommand getOUCH2RejectedCommand();
    OUCH2RejectedCommand getOUCH2RejectedCommand(ByteBuffer buffer);
    OUCH2RejectedEvent getOUCH2RejectedEvent(ByteBuffer buffer);
    OUCH2FillCommand getOUCH2FillCommand();
    OUCH2FillCommand getOUCH2FillCommand(ByteBuffer buffer);
    OUCH2FillEvent getOUCH2FillEvent(ByteBuffer buffer);
} 
