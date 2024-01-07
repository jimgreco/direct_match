package com.core.match.monitors.account;

import java.text.DecimalFormat;

import com.core.match.msgs.MatchConstants;
import com.core.match.util.MatchPriceUtils;

/**
 * Created by jgreco on 11/13/15.
 */
public class StatBase {
    short id;
    int rejsN;
    int sendQ;
    int fillQ;
    int outStdingQ;
    int ordrN;
    int crplN;
    int filsN;
    int cnlsN;
    boolean enabled;

    private final DecimalFormat qtyFormatter = new DecimalFormat("#,###.#");

    public boolean isEnabled() {
        return enabled;
    }

    public void incOrders() {
        ordrN++;
    }

    public void incReplaces() {
        crplN++;
    }

    public void incCancels() {
        cnlsN++;
    }

    public void incFills() {
        filsN++;
    }

    public void addSendQty(int qty) {
        sendQ +=qty;
    }

    public void addQtyOutstanding(int qty) {
        outStdingQ +=qty;
    }

    public short getId() {
        return id;
    }

    public int getRejsN() {
        return rejsN;
    }

    public String getSendQ() {
        return qtyFormatter.format((double) sendQ / MatchConstants.QTY_MULTIPLIER);
    }

    public String getFillQ() {
        return qtyFormatter.format(MatchPriceUtils.toQtyRoundLot(fillQ));
    }

    public String getOutStdingQ() {
        return qtyFormatter.format(MatchPriceUtils.toQtyRoundLot(outStdingQ));
    }

    public int getOrdrN() {
        return ordrN;
    }

    public int getCrplN() {
        return crplN;
    }

    public int getFilsN() {
        return filsN;
    }

    public int getCnlsN() {
        return cnlsN;
    }

    public void addFilledQty(int i) {
        fillQ +=i;
    }

    public void incRejectOrder() {
        rejsN++;
    }
}
