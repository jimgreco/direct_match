package com.core.match.msgs;

public interface MatchSecurityEvent extends com.core.match.msgs.MatchCommonEvent {
    MatchSecurityCommand toCommand();

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

    java.nio.ByteBuffer getName();
    int getNameLength();
    String getNameAsString();
    boolean hasName();

    java.nio.ByteBuffer getCUSIP();
    int getCUSIPLength();
    String getCUSIPAsString();
    boolean hasCUSIP();

    int getMaturityDate();
    java.time.LocalDate getMaturityDateAsDate();
    boolean hasMaturityDate();

    long getCoupon();
    String getCouponAs32nd();
    double getCouponAsDouble();
    boolean hasCoupon();

    char getType();
    boolean hasType();

    int getIssueDate();
    java.time.LocalDate getIssueDateAsDate();
    boolean hasIssueDate();

    byte getCouponFrequency();
    boolean hasCouponFrequency();

    long getTickSize();
    String getTickSizeAs32nd();
    double getTickSizeAsDouble();
    boolean hasTickSize();

    int getLotSize();
    double getLotSizeAsQty();
    boolean hasLotSize();

    java.nio.ByteBuffer getBloombergID();
    int getBloombergIDLength();
    String getBloombergIDAsString();
    boolean hasBloombergID();

    byte getNumLegs();
    boolean hasNumLegs();

    short getLeg1ID();
    boolean hasLeg1ID();

    short getLeg2ID();
    boolean hasLeg2ID();

    short getLeg3ID();
    boolean hasLeg3ID();

    int getLeg1Size();
    boolean hasLeg1Size();

    int getLeg2Size();
    boolean hasLeg2Size();

    int getLeg3Size();
    boolean hasLeg3Size();

    short getUnderlyingID();
    boolean hasUnderlyingID();

    long getReferencePrice();
    String getReferencePriceAs32nd();
    double getReferencePriceAsDouble();
    boolean hasReferencePrice();
} 
