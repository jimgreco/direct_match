package com.core.match.db.jdbc.securities;


public class SecurityDefinitionDAO {
    String session;
    long nano;
    String security;
    char type;
    double bid;
    double ask;
    double mid;
    double ratio1;
    double ratio2;
    double ratio3;
    short id;

    public void clear() {
        session=null;
        security=null;
        nano=0;
        type='0';
        bid=0;
        ask=0;
        mid=0;
        ratio1=0;
        ratio2=0;
        ratio3=0;
        id=0;
    }
}
