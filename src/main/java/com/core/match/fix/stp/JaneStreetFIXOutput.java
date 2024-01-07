package com.core.match.fix.stp;

import com.core.fix.FixParser;
import com.core.fix.FixWriter;
import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixMsgTypes;
import com.core.fix.msgs.FixTags;
import com.core.fix.store.FixStore;
import com.core.fix.tags.FixTag;
import com.core.match.fix.orders.FIXOrderOutput;
import com.core.match.fix.orders.FIXQtyMode;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchBondMath;
import com.core.match.util.MatchPriceUtils;
import com.core.match.util.MessageUtils;
import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.TimeUtils;
import com.core.util.TradeDateUtils;

import java.nio.ByteBuffer;

import static com.core.fix.msgs.FixConstants.Side.Buy;
import static com.core.fix.msgs.FixConstants.Side.Sell;
import static com.core.fix.msgs.FixConstants.TimeInForce.Day;
import static com.core.fix.msgs.FixConstants.TimeInForce.IOC;

/**
 * Created by jgreco on 2/27/16.
 */
public class JaneStreetFIXOutput extends FIXOrderOutput<FIXSTPOrder> {
    protected final TradeDateUtils tradeDateUtils;

    protected final FixTag commission;
    protected final FixTag commissionType;
    protected final FixTag tradeDate;
    protected final FixTag settlementDate;
    protected final FixTag netMoney;

    public JaneStreetFIXOutput(FixParser parser,
                               FixStore store,
                               TraderService<Trader> traders,
                               AccountService<Account> accounts,
                               SecurityService<BaseSecurity> securities) {
        super(parser, store, FIXQtyMode.Notional, traders, accounts, securities);

        tradeDateUtils = new TradeDateUtils(MessageUtils.zoneID(), MatchConstants.SESSION_ROLLOVER_TIME);

        tradeDate = parser.createWriteOnlyFIXTag(FixTags.TradeDate);
        settlementDate = parser.createWriteOnlyFIXTag(FixTags.SettlementDate);
        netMoney = parser.createWriteOnlyFIXTag(FixTags.NetMoney);
        commission = parser.createWriteOnlyFIXTag(FixTags.Commission);
        commissionType = parser.createWriteOnlyFIXTag(FixTags.CommissionType);
    }

    @Override
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

        //22 and 48 for cusip

        if(msg.hasSecurity() &&  securities.getBond(msg.getSecurity()) != null){
            Bond security=securities.getBond(msg.getSecurity());
            fix.writeChar(securityIDSource, FixConstants.SecurityIDSource.CUSIP);
            temp.clear();
            BinaryUtils.copy(temp, security.getCUSIP()).flip();
            fix.writeString(securityID, temp);
        }


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



    @Override
    protected void writeExecutionReport(long time,
                                        FIXSTPOrder order,
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
        fix.writeChar(side, order.isBuy() ? Buy : Sell);

        // 55=Symbol*
        Bond security = securities.getBond(order.getSecurityID());
        if(security==null){
            return;
        }
        fix.writeString(symbol, security.getName());

        //22 and 48 for cusip

        fix.writeChar(securityIDSource, FixConstants.SecurityIDSource.CUSIP);

        temp.clear();
        BinaryUtils.copy(temp, security.getCUSIP()).flip();
        fix.writeString(securityID, temp);

        // 58=Text (Rejects only)

        // 59=TimeInForce
        fix.writeChar(tif, order.isIOC() ? IOC : Day);

        // 60=TransactTime*
        fix.writeDateTime(transactTime, time);

        // 150=ExecType*
        fix.writeChar(execType, execTypeVal);

        // 151=LeavesQty*
        if (execTypeVal == FixConstants.ExecType.Canceled) {
            fix.writeChar(leavesQty, '0');
        }
        else {
            int leavesQtyVal = internalToExternalQty(order.getQty() - order.getCumQty());
            fix.writeNumber(leavesQty, leavesQtyVal);
        }

        // Jane St specific tags
        if (lastQtyVal > 0 && security.isBond() && trader != null) {
            Account account = accounts.get(trader.getAccountID());
            Bond bond = (Bond) security;

            long commissionValue = (long)Math.ceil(MatchPriceUtils.toQtyRoundLot(lastQtyVal) * account.getCommission());
            fix.writePrice(commission, commissionValue, MatchConstants.IMPLIED_DECIMALS) ;
            fix.writeChar(commissionType, FixConstants.CommissionType.Absolute);

            double netMoneyDbl = MatchBondMath.getNetMoney(bond, order.isBuy(), lastPrice, lastQtyVal, account.getCommission());
            long netMoneyRounded = Math.round(netMoneyDbl * 100);
            fix.writePrice(netMoney, netMoneyRounded, 2);

            int settlementDateNum = TimeUtils.toDateInt(bond.getSettlementDate());
            fix.writeNumber(settlementDate, settlementDateNum);

            int tradeDateNum = TimeUtils.toDateInt(tradeDateUtils.getTradeDate(time));
            fix.writeNumber(tradeDate, tradeDateNum);
        }

        store.finalizeBusinessMessage();
    }
}
