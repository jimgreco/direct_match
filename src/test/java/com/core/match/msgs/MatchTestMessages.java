package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchTestMessages implements MatchMessages {
    public <T> T get(Class<T> cls, ByteBuffer buffer) {
        if (cls.equals(MatchContributorEvent.class)) {
            return (T)getMatchContributorEvent(buffer);
        }
        if (cls.equals(MatchTraderEvent.class)) {
            return (T)getMatchTraderEvent(buffer);
        }
        if (cls.equals(MatchSystemEventEvent.class)) {
            return (T)getMatchSystemEventEvent(buffer);
        }
        if (cls.equals(MatchAccountEvent.class)) {
            return (T)getMatchAccountEvent(buffer);
        }
        if (cls.equals(MatchSecurityEvent.class)) {
            return (T)getMatchSecurityEvent(buffer);
        }
        if (cls.equals(MatchOrderEvent.class)) {
            return (T)getMatchOrderEvent(buffer);
        }
        if (cls.equals(MatchClientOrderRejectEvent.class)) {
            return (T)getMatchClientOrderRejectEvent(buffer);
        }
        if (cls.equals(MatchOrderRejectEvent.class)) {
            return (T)getMatchOrderRejectEvent(buffer);
        }
        if (cls.equals(MatchCancelEvent.class)) {
            return (T)getMatchCancelEvent(buffer);
        }
        if (cls.equals(MatchClientCancelReplaceRejectEvent.class)) {
            return (T)getMatchClientCancelReplaceRejectEvent(buffer);
        }
        if (cls.equals(MatchCancelReplaceRejectEvent.class)) {
            return (T)getMatchCancelReplaceRejectEvent(buffer);
        }
        if (cls.equals(MatchReplaceEvent.class)) {
            return (T)getMatchReplaceEvent(buffer);
        }
        if (cls.equals(MatchFillEvent.class)) {
            return (T)getMatchFillEvent(buffer);
        }
        if (cls.equals(MatchInboundEvent.class)) {
            return (T)getMatchInboundEvent(buffer);
        }
        if (cls.equals(MatchOutboundEvent.class)) {
            return (T)getMatchOutboundEvent(buffer);
        }
        if (cls.equals(MatchQuoteEvent.class)) {
            return (T)getMatchQuoteEvent(buffer);
        }
        if (cls.equals(MatchMiscRejectEvent.class)) {
            return (T)getMatchMiscRejectEvent(buffer);
        }
        return null;
    }

    @Override
    public MatchContributorCommand getMatchContributorCommand() {
		MatchContributorByteBufferMessage msg = new MatchContributorByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchContributorCommand getMatchContributorCommand(ByteBuffer buffer) {
		MatchContributorByteBufferMessage msg = new MatchContributorByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchContributorEvent getMatchContributorEvent(ByteBuffer buffer) {
		MatchContributorByteBufferMessage msg = new MatchContributorByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchTraderCommand getMatchTraderCommand() {
		MatchTraderByteBufferMessage msg = new MatchTraderByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchTraderCommand getMatchTraderCommand(ByteBuffer buffer) {
		MatchTraderByteBufferMessage msg = new MatchTraderByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchTraderEvent getMatchTraderEvent(ByteBuffer buffer) {
		MatchTraderByteBufferMessage msg = new MatchTraderByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchSystemEventCommand getMatchSystemEventCommand() {
		MatchSystemEventByteBufferMessage msg = new MatchSystemEventByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchSystemEventCommand getMatchSystemEventCommand(ByteBuffer buffer) {
		MatchSystemEventByteBufferMessage msg = new MatchSystemEventByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchSystemEventEvent getMatchSystemEventEvent(ByteBuffer buffer) {
		MatchSystemEventByteBufferMessage msg = new MatchSystemEventByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchAccountCommand getMatchAccountCommand() {
		MatchAccountByteBufferMessage msg = new MatchAccountByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchAccountCommand getMatchAccountCommand(ByteBuffer buffer) {
		MatchAccountByteBufferMessage msg = new MatchAccountByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchAccountEvent getMatchAccountEvent(ByteBuffer buffer) {
		MatchAccountByteBufferMessage msg = new MatchAccountByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchSecurityCommand getMatchSecurityCommand() {
		MatchSecurityByteBufferMessage msg = new MatchSecurityByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchSecurityCommand getMatchSecurityCommand(ByteBuffer buffer) {
		MatchSecurityByteBufferMessage msg = new MatchSecurityByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchSecurityEvent getMatchSecurityEvent(ByteBuffer buffer) {
		MatchSecurityByteBufferMessage msg = new MatchSecurityByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchOrderCommand getMatchOrderCommand() {
		MatchOrderByteBufferMessage msg = new MatchOrderByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchOrderCommand getMatchOrderCommand(ByteBuffer buffer) {
		MatchOrderByteBufferMessage msg = new MatchOrderByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchOrderEvent getMatchOrderEvent(ByteBuffer buffer) {
		MatchOrderByteBufferMessage msg = new MatchOrderByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchClientOrderRejectCommand getMatchClientOrderRejectCommand() {
		MatchClientOrderRejectByteBufferMessage msg = new MatchClientOrderRejectByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchClientOrderRejectCommand getMatchClientOrderRejectCommand(ByteBuffer buffer) {
		MatchClientOrderRejectByteBufferMessage msg = new MatchClientOrderRejectByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchClientOrderRejectEvent getMatchClientOrderRejectEvent(ByteBuffer buffer) {
		MatchClientOrderRejectByteBufferMessage msg = new MatchClientOrderRejectByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchOrderRejectCommand getMatchOrderRejectCommand() {
		MatchOrderRejectByteBufferMessage msg = new MatchOrderRejectByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchOrderRejectCommand getMatchOrderRejectCommand(ByteBuffer buffer) {
		MatchOrderRejectByteBufferMessage msg = new MatchOrderRejectByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchOrderRejectEvent getMatchOrderRejectEvent(ByteBuffer buffer) {
		MatchOrderRejectByteBufferMessage msg = new MatchOrderRejectByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchCancelCommand getMatchCancelCommand() {
		MatchCancelByteBufferMessage msg = new MatchCancelByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchCancelCommand getMatchCancelCommand(ByteBuffer buffer) {
		MatchCancelByteBufferMessage msg = new MatchCancelByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchCancelEvent getMatchCancelEvent(ByteBuffer buffer) {
		MatchCancelByteBufferMessage msg = new MatchCancelByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchClientCancelReplaceRejectCommand getMatchClientCancelReplaceRejectCommand() {
		MatchClientCancelReplaceRejectByteBufferMessage msg = new MatchClientCancelReplaceRejectByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchClientCancelReplaceRejectCommand getMatchClientCancelReplaceRejectCommand(ByteBuffer buffer) {
		MatchClientCancelReplaceRejectByteBufferMessage msg = new MatchClientCancelReplaceRejectByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchClientCancelReplaceRejectEvent getMatchClientCancelReplaceRejectEvent(ByteBuffer buffer) {
		MatchClientCancelReplaceRejectByteBufferMessage msg = new MatchClientCancelReplaceRejectByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchCancelReplaceRejectCommand getMatchCancelReplaceRejectCommand() {
		MatchCancelReplaceRejectByteBufferMessage msg = new MatchCancelReplaceRejectByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchCancelReplaceRejectCommand getMatchCancelReplaceRejectCommand(ByteBuffer buffer) {
		MatchCancelReplaceRejectByteBufferMessage msg = new MatchCancelReplaceRejectByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchCancelReplaceRejectEvent getMatchCancelReplaceRejectEvent(ByteBuffer buffer) {
		MatchCancelReplaceRejectByteBufferMessage msg = new MatchCancelReplaceRejectByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchReplaceCommand getMatchReplaceCommand() {
		MatchReplaceByteBufferMessage msg = new MatchReplaceByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchReplaceCommand getMatchReplaceCommand(ByteBuffer buffer) {
		MatchReplaceByteBufferMessage msg = new MatchReplaceByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchReplaceEvent getMatchReplaceEvent(ByteBuffer buffer) {
		MatchReplaceByteBufferMessage msg = new MatchReplaceByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchFillCommand getMatchFillCommand() {
		MatchFillByteBufferMessage msg = new MatchFillByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchFillCommand getMatchFillCommand(ByteBuffer buffer) {
		MatchFillByteBufferMessage msg = new MatchFillByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchFillEvent getMatchFillEvent(ByteBuffer buffer) {
		MatchFillByteBufferMessage msg = new MatchFillByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchInboundCommand getMatchInboundCommand() {
		MatchInboundByteBufferMessage msg = new MatchInboundByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchInboundCommand getMatchInboundCommand(ByteBuffer buffer) {
		MatchInboundByteBufferMessage msg = new MatchInboundByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchInboundEvent getMatchInboundEvent(ByteBuffer buffer) {
		MatchInboundByteBufferMessage msg = new MatchInboundByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchOutboundCommand getMatchOutboundCommand() {
		MatchOutboundByteBufferMessage msg = new MatchOutboundByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchOutboundCommand getMatchOutboundCommand(ByteBuffer buffer) {
		MatchOutboundByteBufferMessage msg = new MatchOutboundByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchOutboundEvent getMatchOutboundEvent(ByteBuffer buffer) {
		MatchOutboundByteBufferMessage msg = new MatchOutboundByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchQuoteCommand getMatchQuoteCommand() {
		MatchQuoteByteBufferMessage msg = new MatchQuoteByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchQuoteCommand getMatchQuoteCommand(ByteBuffer buffer) {
		MatchQuoteByteBufferMessage msg = new MatchQuoteByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchQuoteEvent getMatchQuoteEvent(ByteBuffer buffer) {
		MatchQuoteByteBufferMessage msg = new MatchQuoteByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }

    @Override
    public MatchMiscRejectCommand getMatchMiscRejectCommand() {
		MatchMiscRejectByteBufferMessage msg = new MatchMiscRejectByteBufferMessage();
		msg.wrapCommand(ByteBuffer.allocate(1500));
		return msg;
    }

    @Override
    public MatchMiscRejectCommand getMatchMiscRejectCommand(ByteBuffer buffer) {
		MatchMiscRejectByteBufferMessage msg = new MatchMiscRejectByteBufferMessage();
		msg.wrapCommand(buffer);
		return msg;
    }

    @Override
    public MatchMiscRejectEvent getMatchMiscRejectEvent(ByteBuffer buffer) {
		MatchMiscRejectByteBufferMessage msg = new MatchMiscRejectByteBufferMessage();
		msg.wrapEvent(buffer);
		return msg;
    }
}
