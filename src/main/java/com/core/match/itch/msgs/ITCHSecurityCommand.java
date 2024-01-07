package com.core.match.itch.msgs;

public interface ITCHSecurityCommand extends ITCHCommonCommand {
    void copy(ITCHSecurityEvent cmd);
    ITCHSecurityEvent toEvent();

    void setName(java.nio.ByteBuffer val);
    void setName(String val);

    void setSecurityType(char val);

    void setCoupon(long val);
    void setCoupon(double val);

    void setMaturityDate(int val);
    void setMaturityDateAsDate(java.time.LocalDate val);

    void setSecurityReference(java.nio.ByteBuffer val);
    void setSecurityReference(String val);

    void setSecurityReferenceSource(char val);
}
