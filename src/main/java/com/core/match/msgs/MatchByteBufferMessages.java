package com.core.match.msgs;

import java.nio.ByteBuffer;

public class MatchByteBufferMessages implements MatchMessages {
    private final ByteBuffer temp = ByteBuffer.allocateDirect(1500);

    private final MatchContributorByteBufferMessage cmdContributor = new MatchContributorByteBufferMessage(); 
    private final MatchContributorByteBufferMessage eventContributor = new MatchContributorByteBufferMessage(); 
    private final MatchTraderByteBufferMessage cmdTrader = new MatchTraderByteBufferMessage(); 
    private final MatchTraderByteBufferMessage eventTrader = new MatchTraderByteBufferMessage(); 
    private final MatchSystemEventByteBufferMessage cmdSystemEvent = new MatchSystemEventByteBufferMessage(); 
    private final MatchSystemEventByteBufferMessage eventSystemEvent = new MatchSystemEventByteBufferMessage(); 
    private final MatchAccountByteBufferMessage cmdAccount = new MatchAccountByteBufferMessage(); 
    private final MatchAccountByteBufferMessage eventAccount = new MatchAccountByteBufferMessage(); 
    private final MatchSecurityByteBufferMessage cmdSecurity = new MatchSecurityByteBufferMessage(); 
    private final MatchSecurityByteBufferMessage eventSecurity = new MatchSecurityByteBufferMessage(); 
    private final MatchOrderByteBufferMessage cmdOrder = new MatchOrderByteBufferMessage(); 
    private final MatchOrderByteBufferMessage eventOrder = new MatchOrderByteBufferMessage(); 
    private final MatchClientOrderRejectByteBufferMessage cmdClientOrderReject = new MatchClientOrderRejectByteBufferMessage(); 
    private final MatchClientOrderRejectByteBufferMessage eventClientOrderReject = new MatchClientOrderRejectByteBufferMessage(); 
    private final MatchOrderRejectByteBufferMessage cmdOrderReject = new MatchOrderRejectByteBufferMessage(); 
    private final MatchOrderRejectByteBufferMessage eventOrderReject = new MatchOrderRejectByteBufferMessage(); 
    private final MatchCancelByteBufferMessage cmdCancel = new MatchCancelByteBufferMessage(); 
    private final MatchCancelByteBufferMessage eventCancel = new MatchCancelByteBufferMessage(); 
    private final MatchClientCancelReplaceRejectByteBufferMessage cmdClientCancelReplaceReject = new MatchClientCancelReplaceRejectByteBufferMessage(); 
    private final MatchClientCancelReplaceRejectByteBufferMessage eventClientCancelReplaceReject = new MatchClientCancelReplaceRejectByteBufferMessage(); 
    private final MatchCancelReplaceRejectByteBufferMessage cmdCancelReplaceReject = new MatchCancelReplaceRejectByteBufferMessage(); 
    private final MatchCancelReplaceRejectByteBufferMessage eventCancelReplaceReject = new MatchCancelReplaceRejectByteBufferMessage(); 
    private final MatchReplaceByteBufferMessage cmdReplace = new MatchReplaceByteBufferMessage(); 
    private final MatchReplaceByteBufferMessage eventReplace = new MatchReplaceByteBufferMessage(); 
    private final MatchFillByteBufferMessage cmdFill = new MatchFillByteBufferMessage(); 
    private final MatchFillByteBufferMessage eventFill = new MatchFillByteBufferMessage(); 
    private final MatchInboundByteBufferMessage cmdInbound = new MatchInboundByteBufferMessage(); 
    private final MatchInboundByteBufferMessage eventInbound = new MatchInboundByteBufferMessage(); 
    private final MatchOutboundByteBufferMessage cmdOutbound = new MatchOutboundByteBufferMessage(); 
    private final MatchOutboundByteBufferMessage eventOutbound = new MatchOutboundByteBufferMessage(); 
    private final MatchQuoteByteBufferMessage cmdQuote = new MatchQuoteByteBufferMessage(); 
    private final MatchQuoteByteBufferMessage eventQuote = new MatchQuoteByteBufferMessage(); 
    private final MatchMiscRejectByteBufferMessage cmdMiscReject = new MatchMiscRejectByteBufferMessage(); 
    private final MatchMiscRejectByteBufferMessage eventMiscReject = new MatchMiscRejectByteBufferMessage(); 
 
    @Override
    public MatchContributorCommand getMatchContributorCommand() {
        temp.clear();
        return cmdContributor.wrapCommand(temp);
    }

    @Override
    public MatchContributorCommand getMatchContributorCommand(ByteBuffer buffer) {
        return cmdContributor.wrapCommand(buffer);
    }

    @Override
    public MatchContributorEvent getMatchContributorEvent(ByteBuffer buffer) {
        return eventContributor.wrapEvent(buffer);
    }
    @Override
    public MatchTraderCommand getMatchTraderCommand() {
        temp.clear();
        return cmdTrader.wrapCommand(temp);
    }

    @Override
    public MatchTraderCommand getMatchTraderCommand(ByteBuffer buffer) {
        return cmdTrader.wrapCommand(buffer);
    }

    @Override
    public MatchTraderEvent getMatchTraderEvent(ByteBuffer buffer) {
        return eventTrader.wrapEvent(buffer);
    }
    @Override
    public MatchSystemEventCommand getMatchSystemEventCommand() {
        temp.clear();
        return cmdSystemEvent.wrapCommand(temp);
    }

    @Override
    public MatchSystemEventCommand getMatchSystemEventCommand(ByteBuffer buffer) {
        return cmdSystemEvent.wrapCommand(buffer);
    }

    @Override
    public MatchSystemEventEvent getMatchSystemEventEvent(ByteBuffer buffer) {
        return eventSystemEvent.wrapEvent(buffer);
    }
    @Override
    public MatchAccountCommand getMatchAccountCommand() {
        temp.clear();
        return cmdAccount.wrapCommand(temp);
    }

    @Override
    public MatchAccountCommand getMatchAccountCommand(ByteBuffer buffer) {
        return cmdAccount.wrapCommand(buffer);
    }

    @Override
    public MatchAccountEvent getMatchAccountEvent(ByteBuffer buffer) {
        return eventAccount.wrapEvent(buffer);
    }
    @Override
    public MatchSecurityCommand getMatchSecurityCommand() {
        temp.clear();
        return cmdSecurity.wrapCommand(temp);
    }

    @Override
    public MatchSecurityCommand getMatchSecurityCommand(ByteBuffer buffer) {
        return cmdSecurity.wrapCommand(buffer);
    }

    @Override
    public MatchSecurityEvent getMatchSecurityEvent(ByteBuffer buffer) {
        return eventSecurity.wrapEvent(buffer);
    }
    @Override
    public MatchOrderCommand getMatchOrderCommand() {
        temp.clear();
        return cmdOrder.wrapCommand(temp);
    }

    @Override
    public MatchOrderCommand getMatchOrderCommand(ByteBuffer buffer) {
        return cmdOrder.wrapCommand(buffer);
    }

    @Override
    public MatchOrderEvent getMatchOrderEvent(ByteBuffer buffer) {
        return eventOrder.wrapEvent(buffer);
    }
    @Override
    public MatchClientOrderRejectCommand getMatchClientOrderRejectCommand() {
        temp.clear();
        return cmdClientOrderReject.wrapCommand(temp);
    }

    @Override
    public MatchClientOrderRejectCommand getMatchClientOrderRejectCommand(ByteBuffer buffer) {
        return cmdClientOrderReject.wrapCommand(buffer);
    }

    @Override
    public MatchClientOrderRejectEvent getMatchClientOrderRejectEvent(ByteBuffer buffer) {
        return eventClientOrderReject.wrapEvent(buffer);
    }
    @Override
    public MatchOrderRejectCommand getMatchOrderRejectCommand() {
        temp.clear();
        return cmdOrderReject.wrapCommand(temp);
    }

    @Override
    public MatchOrderRejectCommand getMatchOrderRejectCommand(ByteBuffer buffer) {
        return cmdOrderReject.wrapCommand(buffer);
    }

    @Override
    public MatchOrderRejectEvent getMatchOrderRejectEvent(ByteBuffer buffer) {
        return eventOrderReject.wrapEvent(buffer);
    }
    @Override
    public MatchCancelCommand getMatchCancelCommand() {
        temp.clear();
        return cmdCancel.wrapCommand(temp);
    }

    @Override
    public MatchCancelCommand getMatchCancelCommand(ByteBuffer buffer) {
        return cmdCancel.wrapCommand(buffer);
    }

    @Override
    public MatchCancelEvent getMatchCancelEvent(ByteBuffer buffer) {
        return eventCancel.wrapEvent(buffer);
    }
    @Override
    public MatchClientCancelReplaceRejectCommand getMatchClientCancelReplaceRejectCommand() {
        temp.clear();
        return cmdClientCancelReplaceReject.wrapCommand(temp);
    }

    @Override
    public MatchClientCancelReplaceRejectCommand getMatchClientCancelReplaceRejectCommand(ByteBuffer buffer) {
        return cmdClientCancelReplaceReject.wrapCommand(buffer);
    }

    @Override
    public MatchClientCancelReplaceRejectEvent getMatchClientCancelReplaceRejectEvent(ByteBuffer buffer) {
        return eventClientCancelReplaceReject.wrapEvent(buffer);
    }
    @Override
    public MatchCancelReplaceRejectCommand getMatchCancelReplaceRejectCommand() {
        temp.clear();
        return cmdCancelReplaceReject.wrapCommand(temp);
    }

    @Override
    public MatchCancelReplaceRejectCommand getMatchCancelReplaceRejectCommand(ByteBuffer buffer) {
        return cmdCancelReplaceReject.wrapCommand(buffer);
    }

    @Override
    public MatchCancelReplaceRejectEvent getMatchCancelReplaceRejectEvent(ByteBuffer buffer) {
        return eventCancelReplaceReject.wrapEvent(buffer);
    }
    @Override
    public MatchReplaceCommand getMatchReplaceCommand() {
        temp.clear();
        return cmdReplace.wrapCommand(temp);
    }

    @Override
    public MatchReplaceCommand getMatchReplaceCommand(ByteBuffer buffer) {
        return cmdReplace.wrapCommand(buffer);
    }

    @Override
    public MatchReplaceEvent getMatchReplaceEvent(ByteBuffer buffer) {
        return eventReplace.wrapEvent(buffer);
    }
    @Override
    public MatchFillCommand getMatchFillCommand() {
        temp.clear();
        return cmdFill.wrapCommand(temp);
    }

    @Override
    public MatchFillCommand getMatchFillCommand(ByteBuffer buffer) {
        return cmdFill.wrapCommand(buffer);
    }

    @Override
    public MatchFillEvent getMatchFillEvent(ByteBuffer buffer) {
        return eventFill.wrapEvent(buffer);
    }
    @Override
    public MatchInboundCommand getMatchInboundCommand() {
        temp.clear();
        return cmdInbound.wrapCommand(temp);
    }

    @Override
    public MatchInboundCommand getMatchInboundCommand(ByteBuffer buffer) {
        return cmdInbound.wrapCommand(buffer);
    }

    @Override
    public MatchInboundEvent getMatchInboundEvent(ByteBuffer buffer) {
        return eventInbound.wrapEvent(buffer);
    }
    @Override
    public MatchOutboundCommand getMatchOutboundCommand() {
        temp.clear();
        return cmdOutbound.wrapCommand(temp);
    }

    @Override
    public MatchOutboundCommand getMatchOutboundCommand(ByteBuffer buffer) {
        return cmdOutbound.wrapCommand(buffer);
    }

    @Override
    public MatchOutboundEvent getMatchOutboundEvent(ByteBuffer buffer) {
        return eventOutbound.wrapEvent(buffer);
    }
    @Override
    public MatchQuoteCommand getMatchQuoteCommand() {
        temp.clear();
        return cmdQuote.wrapCommand(temp);
    }

    @Override
    public MatchQuoteCommand getMatchQuoteCommand(ByteBuffer buffer) {
        return cmdQuote.wrapCommand(buffer);
    }

    @Override
    public MatchQuoteEvent getMatchQuoteEvent(ByteBuffer buffer) {
        return eventQuote.wrapEvent(buffer);
    }
    @Override
    public MatchMiscRejectCommand getMatchMiscRejectCommand() {
        temp.clear();
        return cmdMiscReject.wrapCommand(temp);
    }

    @Override
    public MatchMiscRejectCommand getMatchMiscRejectCommand(ByteBuffer buffer) {
        return cmdMiscReject.wrapCommand(buffer);
    }

    @Override
    public MatchMiscRejectEvent getMatchMiscRejectEvent(ByteBuffer buffer) {
        return eventMiscReject.wrapEvent(buffer);
    }
}
