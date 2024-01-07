package com.core.match.fix.orders;

import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixConstants.ExecType;
import com.core.fix.msgs.FixConstants.OrdStatus;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.match.fix.FIXAttributes;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.order.AbstractOrder;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchPriceUtils;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;

import java.nio.ByteBuffer;

import static com.core.fix.msgs.FixConstants.*;
import static com.core.fix.msgs.FixConstants.CxlRejResponseTo;
import static com.core.fix.msgs.FixConstants.Side;

/**
 * Created by jgreco on 2/15/16.
 */
public class FIXOrderOutput<T extends AbstractOrder<T> & FIXAttributes> {
    protected final ByteBuffer temp = ByteBuffer.allocate(128);

    protected final FixStore store;
    private final FIXQtyMode qtyMode;

    protected final AccountService<Account> accounts;
    protected final TraderService<Trader> traders;
    protected final SecurityService<BaseSecurity> securities;

    protected final FixTag account;
    protected final FixTag side;
    protected final FixTag orderQty;
    protected final FixTag symbol;
    protected final FixTag price;
    protected final FixTag tif;
    protected final FixTag clOrdId;
    protected final FixTag origClOrdId;
    protected final FixTag orderId;
    protected final FixTag execId;
    protected final FixTag execType;
    protected final FixTag ordStatus;
    protected final FixTag leavesQty;
    protected final FixTag cumQty;
    protected final FixTag avgPx;
    protected final FixTag text;
    protected final FixTag lastPx;
    protected final FixTag lastQty;
    protected final FixTag transactTime;
    protected final FixTag cxlRejReason;
    protected final FixTag cxlRejResponseTo;
    protected final FixTag securityID;
    protected final FixTag securityIDSource;
    public FIXOrderOutput(FixParser parser,
                          FixStore store,
                          FIXQtyMode qtyMode,
                          TraderService<Trader> traders,
                          AccountService<Account> accounts,
                          SecurityService<BaseSecurity> securities) {
        this.store = store;
        this.qtyMode = qtyMode;

        this.traders = traders;
        this.accounts = accounts;
        this.securities = securities;

        this.account = parser.createWriteOnlyFIXTag(FixTags.Account);
        this.side = parser.createWriteOnlyFIXTag(FixTags.Side);
        this.orderQty = parser.createWriteOnlyFIXTag(FixTags.OrderQty);
        this.symbol = parser.createWriteOnlyFIXTag(FixTags.Symbol);
        this.price = parser.createWriteOnlyFIXTag(FixTags.Price);
        this.clOrdId = parser.createWriteOnlyFIXTag(FixTags.ClOrdID);
        this.origClOrdId = parser.createWriteOnlyFIXTag(FixTags.OrigClOrdID);
        this.tif = parser.createWriteOnlyFIXTag(FixTags.TimeInForce);
        this.orderId = parser.createWriteOnlyFIXTag(FixTags.OrderID);
        this.execId = parser.createWriteOnlyFIXTag(FixTags.ExecID);
        this.execType = parser.createWriteOnlyFIXTag(FixTags.ExecType);
        this.ordStatus = parser.createWriteOnlyFIXTag(FixTags.OrdStatus);
        this.leavesQty = parser.createWriteOnlyFIXTag(FixTags.LeavesQty);
        this.cumQty = parser.createWriteOnlyFIXTag(FixTags.CumQty);
        this.avgPx = parser.createWriteOnlyFIXTag(FixTags.AvgPx);
        this.text = parser.createWriteOnlyFIXTag(FixTags.Text);
        this.lastPx = parser.createWriteOnlyFIXTag(FixTags.LastPx);
        this.lastQty = parser.createWriteOnlyFIXTag(FixTags.LastShares);
        this.transactTime = parser.createWriteOnlyFIXTag(FixTags.TransactTime);
        this.cxlRejReason = parser.createWriteOnlyFIXTag(FixTags.CxlRejectReason);
        this.cxlRejResponseTo = parser.createWriteOnlyFIXTag(FixTags.CxlRejResponseTo);
        this.securityID = parser.createWriteOnlyFIXTag(FixTags.SecurityID);
        this.securityIDSource = parser.createWriteOnlyFIXTag(FixTags.SecurityIDSource);
    }

    public void writeClientCancelReplaceReject(T order, MatchClientCancelReplaceRejectEvent msg) {
        FixWriter writer = store.createMessage(FixMsgTypes.OrderCancelReject);

        // 1=Account
        if (order != null) {
            Trader trader = traders.get(order.getTraderID());
            if (trader != null) {
                writer.writeString(account, trader.getName());
            }
        }

        // 11=ClOrdID*
        writer.writeString(clOrdId, msg.getClOrdID());

        // 37=OrderID*
        if (msg.getOrderID() == 0) {
            writer.writeString(orderId, "NONE");
        }
        else {
            writer.writeNumber(orderId, msg.getOrderID());
        }

        // 39=OrdStatus*
        if (order != null) {
            if (order.getCumQty() > 0) {
                writer.writeChar(ordStatus, OrdStatus.PartiallyFilled);
            }
            else if (order.isReplaced()) {
                writer.writeChar(ordStatus, OrdStatus.Replaced);
            }
            else {
                writer.writeChar(ordStatus, OrdStatus.New);
            }
        }
        else {
            // These could also be filled...
            writer.writeChar(ordStatus, OrdStatus.Canceled);
        }

        // 41=OrigClOrdID*
        if (msg.hasOrigClOrdID()) {
            writer.writeString(origClOrdId, msg.getOrigClOrdID());
        }
        else if (order != null && order.hasClOrdID()) {
            writer.writeString(origClOrdId, order.getClOrdID());
        }

        // 58=Text
        if (msg.hasText()) {
            writer.writeString(text, msg.getText());
        }

        // 60=TransactTime*
        writer.writeDateTime(transactTime, msg.getTimestamp());

        // 102=CxlRejReason
        if (msg.getReason() != 0) {
            writer.writeChar(cxlRejReason, msg.getReason());
        }

        // 434=CxlRejResponseTo*
        if (msg.getIsReplace()) {
            writer.writeChar(cxlRejResponseTo, CxlRejResponseTo.OrderCancelReplaceRequest);
        }
        else {
            writer.writeChar(cxlRejResponseTo, CxlRejResponseTo.OrderCancelRequest);
        }

        store.finalizeBusinessMessage();
    }

    public void writeCancelReplaceReject(T order, MatchCancelReplaceRejectEvent msg) {
        FixWriter writer = store.createMessage(FixMsgTypes.OrderCancelReject);

        // 1=Account
        if (order != null) {
            Trader trader = traders.get(order.getTraderID());
            if (trader != null) {
                writer.writeString(account, trader.getName());
            }
        }

        // 11=ClOrdID*
        writer.writeString(clOrdId, msg.getClOrdID());

        // 37=OrderID*
        if (msg.getOrderID() == 0) {
            writer.writeString(orderId, "NONE");
        }
        else {
            writer.writeNumber(orderId, msg.getOrderID());
        }

        // 39=OrdStatus*
        if (order != null) {
            if (order.getCumQty() > 0) {
                writer.writeChar(ordStatus, OrdStatus.PartiallyFilled);
            }
            else if (order.isReplaced()) {
                writer.writeChar(ordStatus, OrdStatus.Replaced);
            }
            else {
                writer.writeChar(ordStatus, OrdStatus.New);
            }
        }
        else {
            // These could also be filled...
            writer.writeChar(ordStatus, OrdStatus.Canceled);
        }

        // 41=OrigClOrdID*
        if (msg.hasOrigClOrdID()) {
            writer.writeString(origClOrdId, msg.getOrigClOrdID());
        }
        else if (order != null && order.hasClOrdID()) {
            writer.writeString(origClOrdId, order.getClOrdID());
        }

        // 58=Text
        writer.writeString(text, msg.getText());

        // 60=TransactTime*
        writer.writeDateTime(transactTime, msg.getTimestamp());

        // 102=CxlRejReason
        if (order == null) {
            writer.writeChar(cxlRejReason, CxlRejectReason.TooLateToCancel);
            // TODO: Should we fix?
            // if (clOrdIdStore.seenClOrdID(msg.getOrigClOrdID())) {
            //    writer.writeChar(cxlRejReason, CxlRejectReason.TooLateToCancel);
            // }
            // else {
            //    writer.writeChar(cxlRejReason, CxlRejectReason.UnknownOrder);
            // }
        }
        else {
            writer.writeChar(cxlRejReason, CxlRejectReason.BrokerOption);
        }

        // 434=CxlRejResponseTo*
        if (msg.getIsReplace()) {
            writer.writeChar(cxlRejResponseTo, CxlRejResponseTo.OrderCancelReplaceRequest);
        }
        else {
            writer.writeChar(cxlRejResponseTo, CxlRejResponseTo.OrderCancelRequest);
        }

        store.finalizeBusinessMessage();
    }

    public void writeClientOrderReject(MatchClientOrderRejectEvent msg, long execSuffix) {
        FixWriter fix = store.createMessage(FixMsgTypes.ExecutionReport);

        // 1=Account*
        if (msg.hasTrader()) {
            fix.writeString(account, msg.getTrader());
        }

        // 6=AvgPx*
        fix.writeChar(avgPx, '0');

        // 11=ClOrdID*
        if (msg.hasClOrdID()) {
            fix.writeString(clOrdId, msg.getClOrdID());
        }
        else {
            fix.writeString(clOrdId, "NONE");
        }

        // 14=CumQty*
        fix.writeChar(cumQty, '0');

        // 17=ExecID*
        temp.clear();
        temp.put((byte) 'R');
        TextUtils.writeNumber(temp, execSuffix).flip();
        fix.writeString(execId, temp);

        // 20=ExecTransType*
        //if (minorVersion < 3) {
        //    fix.writeChar(execTransType, FixConstants.ExecTransType.New);
        //}

        // 31=LastPx (reject)
        // 32=LastQty (reject)

        // 37=OrderID*
        fix.writeString(orderId, "NONE");

        // 38=OrderQty (reject)

        // 39=OrdStatus*
        fix.writeChar(ordStatus, FixConstants.OrdStatus.Rejected);

        // 41=OrigClOrdID (reject)
        // 44=Price

        // 54=Side*
        fix.writeChar(side, msg.getBuy() ? FixConstants.Side.Buy : FixConstants.Side.Sell);

        // 55=Symbol*
        fix.writeString(symbol, msg.getSecurity());

        // 58=Text
        if (msg.hasText()) {
            fix.writeString(text, msg.getText());
        }

        // 59=TimeInForce (reject)

        // 60=TransactTime*
        fix.writeDateTime(transactTime, msg.getTimestamp());

        // 111=MaxFloor (reject)

        // 150=ExecType*
        fix.writeChar(execType, FixConstants.ExecType.Rejected);

        // 151=LeavesQty*
        fix.writeChar(leavesQty, '0');

        store.finalizeBusinessMessage();
    }

    public void writeOrderReject(MatchOrderRejectEvent msg, long execSuffix) {
        FixWriter fix = store.createMessage(FixMsgTypes.ExecutionReport);

        // 1=Account*
        if (msg.hasTraderID()) {
            Trader trader = traders.get(msg.getTraderID());
            fix.writeString(account, trader.getName());
        }

        // 6=AvgPx*
        fix.writeChar(avgPx, '0');

        // 11=ClOrdID*
        if (msg.hasClOrdID()) {
            fix.writeString(clOrdId, msg.getClOrdID());
        }
        else {
            fix.writeString(clOrdId, "NONE");
        }

        // 14=CumQty*
        fix.writeChar(cumQty, '0');

        // 17=ExecID*
        temp.clear();
        temp.put((byte) 'R');
        TextUtils.writeNumber(temp, execSuffix).flip();
        fix.writeString(execId, temp);

        // 20=ExecTransType
        //if (minorVersion < 3) {
        //    fix.writeChar(execTransType, FixConstants.ExecTransType.New);
        //}

        // 31=LastPx (Not a fill)
        // 32=LastQty (Not a fill)

        // 37=OrderID*
        fix.writeString(orderId, "NONE");

        // 38=OrderQty (reject)

        // 39=OrdStatus
        fix.writeChar(ordStatus, OrdStatus.Rejected);

        // 41=OrigClOrdID (reject)
        // 44=Price (reject)

        // 54=Side*
        fix.writeChar(side, msg.getBuy() ? Side.Buy : Side.Sell);

        // 55=Symbol
        if (!securities.isValid(msg.getSecurityID())) {
            fix.writeString(symbol, "NONE");
        }
        else {
            BaseSecurity security = securities.get(msg.getSecurityID());
            fix.writeString(symbol, security.getName());
        }

        // 58=Text
        if (msg.hasText()) {
            fix.writeString(text, msg.getText());
        }

        // 59=TimeInForce (reject)

        // 60=TransactTime
        fix.writeDateTime(transactTime, msg.getTimestamp());

        // 111=MaxFloor (reject)

        // 150=ExecType*
        fix.writeChar(execType, ExecType.Rejected);

        // 151=LeavesQty*
        fix.writeChar(leavesQty, '0');

        store.finalizeBusinessMessage();
    }

    public void writeAcceptedExecutionReport(long time,
                                             T order,
                                             long execSuffix) {
        writeExecutionReport(time, order, FixMsgTypes.NewOrderSingle, execSuffix, ExecType.New, OrdStatus.New, 0, 0, null);
    }

    public void writeCanceledExecutionReport(long time,
                                             T order,
                                             long execSuffix,
                                             ByteBuffer origClOrdID) {
        writeExecutionReport(time, order, FixMsgTypes.OrderCancelRequest, execSuffix, ExecType.Canceled, OrdStatus.Canceled, 0, 0, origClOrdID);
    }

    public void writeReplacedExecutionReport(long time,
                                             T order,
                                             long execSuffix,
                                             ByteBuffer origClOrdID) {
        char ordStatus = order.getCumQty() > 0 ? OrdStatus.PartiallyFilled : OrdStatus.Replaced;
        writeExecutionReport(time, order, FixMsgTypes.OrderCancelReplaceRequest, execSuffix, ExecType.Replace, ordStatus, 0, 0, origClOrdID);
    }

    public void writeFilledExecutionReport(long time,
                                           T order,
                                           long execSuffix,
                                           int execQty,
                                           long execPrice) {
        char ordStatus = order.isFilled() ? OrdStatus.Filled : OrdStatus.PartiallyFilled;
        writeExecutionReport(time, order, 'X', execSuffix, ExecType.Trade, ordStatus, execQty, execPrice, null);
    }

    protected void writeExecutionReport(long time,
                                        T order,
                                        char execPrefix,
                                        long matchID,
                                        char execTypeVal,
                                        char ordStatusVal,
                                        int lastQtyVal,
                                        long lastPrice,
                                        ByteBuffer origClOrdIdVal) {
        FixWriter fix = store.createMessage(FixMsgTypes.ExecutionReport);

        // 1=Account
        Trader trader = traders.get(order.getTraderID());
        if (trader != null) {
            fix.writeString(account, trader.getName());
        }

        // 6=AvgPx*
        if (order.getCumQty() == 0) {
            fix.writeChar(avgPx, '0');
        }
        else {
            double avgPxVal = order.getNotional() / order.getCumQty();
            fix.writePrice(avgPx, MatchPriceUtils.toLong(avgPxVal), MatchConstants.IMPLIED_DECIMALS);
        }

        // 11=ClOrdID*
        if (order.hasClOrdID()) {
            fix.writeString(clOrdId, order.getClOrdID());
        }
        else {
            fix.writeString(clOrdId, "NONE");
        }

        // 14=CumQty*
        int cumQtyVal = internalToExternalQty(order.getCumQty());
        fix.writeNumber(cumQty, cumQtyVal);

        // 17=ExecID*
        temp.clear();
        temp.put((byte) execPrefix);
        TextUtils.writeNumber(temp, matchID).flip();
        fix.writeString(execId, temp);

        // 20=ExecTransType*
        //if (minorVersion < 3) {
        //    fix.writeChar(execTransType, FixConstants.ExecTransType.New);
        //}

        // 31=LastPx
        // 32=LastQty
        if (lastQtyVal > 0) {
            int execQtyVal = internalToExternalQty(lastQtyVal);
            fix.writePrice(lastPx, lastPrice, MatchConstants.IMPLIED_DECIMALS);
            fix.writeNumber(lastQty, execQtyVal);
        }

        // 37=OrderID*
        fix.writeNumber(orderId, order.getID());

        // 38=OrderQty
        int orderQtyVal = internalToExternalQty(order.getQty());
        fix.writeNumber(orderQty, orderQtyVal);

        // 39=OrdStatus*
        fix.writeChar(ordStatus, ordStatusVal);

        // 41=OrigClOrdID
        if (origClOrdIdVal != null && origClOrdIdVal.hasRemaining()) {
            fix.writeString(origClOrdId, origClOrdIdVal);
        }

        // 44=Price
        fix.writePrice(price, order.getPrice(), MatchConstants.IMPLIED_DECIMALS);

        // 54=Side*
        fix.writeChar(side, order.isBuy() ? Side.Buy : Side.Sell);

        // 55=Symbol*

        Bond security = securities.getBond(order.getSecurityID());
        fix.writeString(symbol, security.getName());





        // 58=Text (Rejects only)

        // 59=TimeInForce
        fix.writeChar(tif, order.isIOC() ? TimeInForce.IOC : TimeInForce.Day);

        // 60=TransactTime*
        fix.writeDateTime(transactTime, time);

        // 150=ExecType*
        fix.writeChar(execType, execTypeVal);

        // 151=LeavesQty*
        if (execTypeVal == ExecType.Canceled) {
            fix.writeChar(leavesQty, '0');
        }
        else {
            int leavesQtyVal = internalToExternalQty(order.getQty() - order.getCumQty());
            fix.writeNumber(leavesQty, leavesQtyVal);
        }

        store.finalizeBusinessMessage();
    }

    protected int internalToExternalQty(int qty) {
        if (qtyMode == FIXQtyMode.Notional) {
            return qty * MatchConstants.QTY_MULTIPLIER;
        }
        else if (qtyMode == FIXQtyMode.RoundLot) {
            return qty / (1000000 / MatchConstants.QTY_MULTIPLIER);
        }
        throw new RuntimeException("Unknown QtyMode");
    }
}
