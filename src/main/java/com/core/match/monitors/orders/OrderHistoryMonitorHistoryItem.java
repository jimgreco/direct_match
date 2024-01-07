package com.core.match.monitors.orders;

import com.core.match.msgs.MatchConstants;
import com.core.match.util.MatchPriceUtils;

/**
 * Created by jgreco on 12/7/15.
 */
public class OrderHistoryMonitorHistoryItem {
    String timestamp;

    char msgType;
    String contributor;
    int qty;
    int cumQty;
    long price;
    String clOrdID;

    int execQty;
    long execPrice;

    char rejectReason;
    String text;

    public String getTimestamp() {
        return timestamp;
    }

    public String getMsgType() {
        return MatchConstants.Messages.toString(msgType);
    }

    public String getContributor() {
        return contributor;
    }

    public String getRejectReason() {
        return rejectReason != 0 ? MatchConstants.OrderRejectReason.toString(rejectReason) : "";
    }

    public String getClOrdID() {
        return clOrdID;
    }

    public String getText() {
        return text;
    }

    public String getPrice32() {
        return MatchPriceUtils.to32ndPrice(price);
    }

    public double getPrice() {
        return MatchPriceUtils.toDouble(price);
    }

    public double getQty() {
        return MatchPriceUtils.toQtyRoundLot(qty);
    }

    public double getCumQty() {
        return MatchPriceUtils.toQtyRoundLot(cumQty);
    }

    public String getExecPrice32() {
        return MatchPriceUtils.to32ndPrice(execPrice);
    }

    public double getExecPrice() {
        return MatchPriceUtils.toDouble(execPrice);
    }

    public double getExecQty() {
        return MatchPriceUtils.toQtyRoundLot(execQty);
    }
}
