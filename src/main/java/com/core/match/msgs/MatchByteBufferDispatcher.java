package com.core.match.msgs;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 * THIS FILE IS AUTOGENERATED
 */
public class MatchByteBufferDispatcher extends MatchBaseDispatcher implements com.core.connector.ByteBufferDispatcher {
    private final MatchMessages msgs;

    public MatchByteBufferDispatcher(MatchMessages msgs) {
        this.msgs = msgs;
    }

    @Override
    public boolean dispatch(ByteBuffer buffer) {
        char msgType = (char)buffer.get(buffer.position() + 0);
        switch (msgType) {
            case 'C':
                return dispatchMatchContributor(msgs.getMatchContributorEvent(buffer));
            case 'T':
                return dispatchMatchTrader(msgs.getMatchTraderEvent(buffer));
            case 'E':
                return dispatchMatchSystemEvent(msgs.getMatchSystemEventEvent(buffer));
            case 'A':
                return dispatchMatchAccount(msgs.getMatchAccountEvent(buffer));
            case 'S':
                return dispatchMatchSecurity(msgs.getMatchSecurityEvent(buffer));
            case 'O':
                return dispatchMatchOrder(msgs.getMatchOrderEvent(buffer));
            case 'P':
                return dispatchMatchClientOrderReject(msgs.getMatchClientOrderRejectEvent(buffer));
            case 'Q':
                return dispatchMatchOrderReject(msgs.getMatchOrderRejectEvent(buffer));
            case 'X':
                return dispatchMatchCancel(msgs.getMatchCancelEvent(buffer));
            case 'Y':
                return dispatchMatchClientCancelReplaceReject(msgs.getMatchClientCancelReplaceRejectEvent(buffer));
            case 'Z':
                return dispatchMatchCancelReplaceReject(msgs.getMatchCancelReplaceRejectEvent(buffer));
            case 'U':
                return dispatchMatchReplace(msgs.getMatchReplaceEvent(buffer));
            case 'F':
                return dispatchMatchFill(msgs.getMatchFillEvent(buffer));
            case 'I':
                return dispatchMatchInbound(msgs.getMatchInboundEvent(buffer));
            case 'J':
                return dispatchMatchOutbound(msgs.getMatchOutboundEvent(buffer));
            case 'D':
                return dispatchMatchQuote(msgs.getMatchQuoteEvent(buffer));
            case 'M':
                return dispatchMatchMiscReject(msgs.getMatchMiscRejectEvent(buffer));
            default:
                return false;
        }
    }
}
