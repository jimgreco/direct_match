package com.core.match.monitors.account;

import com.core.match.util.MatchPriceUtils;

import java.text.DecimalFormat;
import java.util.ArrayList;

/**
 * Created by hli on 10/23/15.
 */
class AccountStats extends StatBase {
    private final DecimalFormat dv01Formatter = new DecimalFormat("#,###");

    String acct;
    double netDv01Lmt;
    double filNetDV01;
    double maxDV01Exp;
    boolean ficc;

    public String getAcct() {
        return acct;
    }

    public String getNetDv01Lmt() {
        return dv01Formatter.format(netDv01Lmt);
    }

    public String getFilNetDV01() {
        return dv01Formatter.format(filNetDV01);
    }

    public String getMaxDV01Exp() {
        return dv01Formatter.format(maxDV01Exp);
    }

    public void setMaxDV01Exp(double maxDV01Exp) {
        this.maxDV01Exp = maxDV01Exp;
    }

    public void setFilNetDV01(double filNetDV01) {
        this.filNetDV01 = filNetDV01;
    }

    public boolean isFICC() {
        return ficc;
    }
}
