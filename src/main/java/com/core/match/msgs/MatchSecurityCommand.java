package com.core.match.msgs;

public interface MatchSecurityCommand extends com.core.match.msgs.MatchCommonCommand {
    void copy(MatchSecurityEvent cmd);
    MatchSecurityEvent toEvent();

    void setSecurityID(short val);

    void setName(java.nio.ByteBuffer val);
    void setName(String val);

    void setCUSIP(java.nio.ByteBuffer val);
    void setCUSIP(String val);

    void setMaturityDate(int val);
    void setMaturityDateAsDate(java.time.LocalDate val);

    void setCoupon(long val);
    void setCoupon(double val);

    void setType(char val);

    void setIssueDate(int val);
    void setIssueDateAsDate(java.time.LocalDate val);

    void setCouponFrequency(byte val);

    void setTickSize(long val);
    void setTickSize(double val);

    void setLotSize(int val);

    void setBloombergID(java.nio.ByteBuffer val);
    void setBloombergID(String val);

    void setNumLegs(byte val);

    void setLeg1ID(short val);

    void setLeg2ID(short val);

    void setLeg3ID(short val);

    void setLeg1Size(int val);

    void setLeg2Size(int val);

    void setLeg3Size(int val);

    void setUnderlyingID(short val);

    void setReferencePrice(long val);
    void setReferencePrice(double val);
}
