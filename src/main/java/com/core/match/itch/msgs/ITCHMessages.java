package com.core.match.itch.msgs;

import java.nio.ByteBuffer;

public interface ITCHMessages {
    ITCHSystemCommand getITCHSystemCommand();
    ITCHSystemCommand getITCHSystemCommand(ByteBuffer buffer);
    ITCHSystemEvent getITCHSystemEvent(ByteBuffer buffer);
    ITCHSecurityCommand getITCHSecurityCommand();
    ITCHSecurityCommand getITCHSecurityCommand(ByteBuffer buffer);
    ITCHSecurityEvent getITCHSecurityEvent(ByteBuffer buffer);
    ITCHOrderCommand getITCHOrderCommand();
    ITCHOrderCommand getITCHOrderCommand(ByteBuffer buffer);
    ITCHOrderEvent getITCHOrderEvent(ByteBuffer buffer);
    ITCHOrderCancelCommand getITCHOrderCancelCommand();
    ITCHOrderCancelCommand getITCHOrderCancelCommand(ByteBuffer buffer);
    ITCHOrderCancelEvent getITCHOrderCancelEvent(ByteBuffer buffer);
    ITCHOrderExecutedCommand getITCHOrderExecutedCommand();
    ITCHOrderExecutedCommand getITCHOrderExecutedCommand(ByteBuffer buffer);
    ITCHOrderExecutedEvent getITCHOrderExecutedEvent(ByteBuffer buffer);
    ITCHTradeCommand getITCHTradeCommand();
    ITCHTradeCommand getITCHTradeCommand(ByteBuffer buffer);
    ITCHTradeEvent getITCHTradeEvent(ByteBuffer buffer);
} 
