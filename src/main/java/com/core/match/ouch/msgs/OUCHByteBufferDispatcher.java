package com.core.match.ouch.msgs;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
 */
public class OUCHByteBufferDispatcher extends OUCHBaseDispatcher implements com.core.connector.ByteBufferDispatcher {
    private final OUCHMessages msgs;

    public OUCHByteBufferDispatcher(OUCHMessages msgs) {
        this.msgs = msgs;
    }

    @Override
    public boolean dispatch(ByteBuffer buffer) {
        char msgType = (char)buffer.get(buffer.position() + 0);
        switch (msgType) {
            case 'T':
                return dispatchOUCHTradeConfirmation(msgs.getOUCHTradeConfirmationEvent(buffer));
            case 'O':
                return dispatchOUCHOrder(msgs.getOUCHOrderEvent(buffer));
            case 'X':
                return dispatchOUCHCancel(msgs.getOUCHCancelEvent(buffer));
            case 'U':
                return dispatchOUCHReplace(msgs.getOUCHReplaceEvent(buffer));
            case 'A':
                return dispatchOUCHAccepted(msgs.getOUCHAcceptedEvent(buffer));
            case 'C':
                return dispatchOUCHCanceled(msgs.getOUCHCanceledEvent(buffer));
            case 'M':
                return dispatchOUCHReplaced(msgs.getOUCHReplacedEvent(buffer));
            case 'I':
                return dispatchOUCHCancelRejected(msgs.getOUCHCancelRejectedEvent(buffer));
            case 'J':
                return dispatchOUCHRejected(msgs.getOUCHRejectedEvent(buffer));
            case 'E':
                return dispatchOUCHFill(msgs.getOUCHFillEvent(buffer));
            default:
                return false;
        }
    }
}
