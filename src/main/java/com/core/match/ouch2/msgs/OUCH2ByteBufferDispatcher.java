package com.core.match.ouch2.msgs;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
 */
public class OUCH2ByteBufferDispatcher extends OUCH2BaseDispatcher implements com.core.connector.ByteBufferDispatcher {
    private final OUCH2Messages msgs;

    public OUCH2ByteBufferDispatcher(OUCH2Messages msgs) {
        this.msgs = msgs;
    }

    @Override
    public boolean dispatch(ByteBuffer buffer) {
        char msgType = (char)buffer.get(buffer.position() + 0);
        switch (msgType) {
            case 'T':
                return dispatchOUCH2TradeConfirmation(msgs.getOUCH2TradeConfirmationEvent(buffer));
            case 'O':
                return dispatchOUCH2Order(msgs.getOUCH2OrderEvent(buffer));
            case 'X':
                return dispatchOUCH2Cancel(msgs.getOUCH2CancelEvent(buffer));
            case 'U':
                return dispatchOUCH2Replace(msgs.getOUCH2ReplaceEvent(buffer));
            case 'A':
                return dispatchOUCH2Accepted(msgs.getOUCH2AcceptedEvent(buffer));
            case 'C':
                return dispatchOUCH2Canceled(msgs.getOUCH2CanceledEvent(buffer));
            case 'M':
                return dispatchOUCH2Replaced(msgs.getOUCH2ReplacedEvent(buffer));
            case 'I':
                return dispatchOUCH2CancelRejected(msgs.getOUCH2CancelRejectedEvent(buffer));
            case 'J':
                return dispatchOUCH2Rejected(msgs.getOUCH2RejectedEvent(buffer));
            case 'E':
                return dispatchOUCH2Fill(msgs.getOUCH2FillEvent(buffer));
            default:
                return false;
        }
    }
}
