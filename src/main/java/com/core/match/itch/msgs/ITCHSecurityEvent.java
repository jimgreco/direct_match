package com.core.match.itch.msgs;

public interface ITCHSecurityEvent extends ITCHCommonEvent {
    ITCHSecurityCommand toCommand();

    java.nio.ByteBuffer getName();
    int getNameLength();
    String getNameAsString();
    boolean hasName();

    char getSecurityType();
    boolean hasSecurityType();

    long getCoupon();
    String getCouponAs32nd();
    double getCouponAsDouble();
    boolean hasCoupon();

    int getMaturityDate();
    java.time.LocalDate getMaturityDateAsDate();
    boolean hasMaturityDate();

    java.nio.ByteBuffer getSecurityReference();
    int getSecurityReferenceLength();
    String getSecurityReferenceAsString();
    boolean hasSecurityReference();

    char getSecurityReferenceSource();
    boolean hasSecurityReferenceSource();
} 
