package com.core.match.msgs;

public interface MatchQuoteEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchQuoteCommand toCommand();

    char getMsgType();
    boolean hasMsgType();

    short getContributorID();
    boolean hasContributorID();

    int getContributorSeq();
    boolean hasContributorSeq();

    long getTimestamp();
    java.time.LocalDateTime getTimestampAsTime();
    boolean hasTimestamp();

    short getSecurityID();
    boolean hasSecurityID();

    long getBidPrice();
    String getBidPriceAs32nd();
    double getBidPriceAsDouble();
    boolean hasBidPrice();

    long getOfferPrice();
    String getOfferPriceAs32nd();
    double getOfferPriceAsDouble();
    boolean hasOfferPrice();

    char getVenueCode();
    boolean hasVenueCode();

    long getSourceTimestamp();
    java.time.LocalDateTime getSourceTimestampAsTime();
    boolean hasSourceTimestamp();
} 
