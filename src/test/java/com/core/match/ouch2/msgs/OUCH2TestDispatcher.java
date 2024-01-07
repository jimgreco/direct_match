package com.core.match.ouch2.msgs;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
 */
public class OUCH2TestDispatcher extends OUCH2BaseDispatcher {
    public boolean dispatch(OUCH2CommonCommand msg1) {
        OUCH2CommonEvent msg = (OUCH2CommonEvent)msg1;
        switch (msg.getMsgType()) {
            case 'T':
                return dispatchOUCH2TradeConfirmation((OUCH2TradeConfirmationEvent)msg);
            case 'O':
                return dispatchOUCH2Order((OUCH2OrderEvent)msg);
            case 'X':
                return dispatchOUCH2Cancel((OUCH2CancelEvent)msg);
            case 'U':
                return dispatchOUCH2Replace((OUCH2ReplaceEvent)msg);
            case 'A':
                return dispatchOUCH2Accepted((OUCH2AcceptedEvent)msg);
            case 'C':
                return dispatchOUCH2Canceled((OUCH2CanceledEvent)msg);
            case 'M':
                return dispatchOUCH2Replaced((OUCH2ReplacedEvent)msg);
            case 'I':
                return dispatchOUCH2CancelRejected((OUCH2CancelRejectedEvent)msg);
            case 'J':
                return dispatchOUCH2Rejected((OUCH2RejectedEvent)msg);
            case 'E':
                return dispatchOUCH2Fill((OUCH2FillEvent)msg);
            default:
                return false;
        }
    }

    public void setLastTimestamp(long timestamp) {
        this.lastTimestamp = timestamp; 
    }
}

