package com.core.match.ouch.controller;

import com.core.match.STPHolder;
import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.OUCHOrder;
import com.core.match.ouch.msgs.OUCHConstants;
import com.core.match.ouch.msgs.OUCHTradeConfirmationCommand;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.match.util.MatchPriceUtils;
import com.core.util.PriceUtils;
import com.core.util.TimeUtils;

public class OuchTradeConfirmProcessor implements TradeConfirmProcessor {
    private OUCHAdapter adaptor;
    private int todayTradeDate;
    protected final TraderService<Trader> traders;
    private final SpreadPriceProvider spreadPriceProvider;

    public OuchTradeConfirmProcessor(int todayTradeDate, OUCHAdapter adaptor, SpreadPriceProvider spreadPriceProvider,TraderService traders){
        this.adaptor=adaptor;
        this.todayTradeDate=todayTradeDate;
        this.spreadPriceProvider=spreadPriceProvider;
        this.traders=traders;
    }


    @Override
    public void sendTradeConfirmation(long timestamp, int matchID, STPHolder<OUCHOrder> holder, BaseSecurity sec)
    {
        double referenceExecQty=holder.getAccumulatedQty();

        if(sec.isBond()){
            sendTradeConfirm(timestamp, matchID, holder, (Bond)sec,referenceExecQty,holder.getAveragePrice(),referenceExecQty);
        }else {
            MultiLegSecurity security=(MultiLegSecurity)sec;
            double leg1Price=PriceUtils.toDouble(spreadPriceProvider.getPrice(security.getLeg1(),holder.isBuy()),MatchConstants.IMPLIED_DECIMALS);
            double leg2Price=PriceUtils.toDouble(spreadPriceProvider.getPrice(security.getLeg2(),holder.isBuy()),MatchConstants.IMPLIED_DECIMALS);

            if(sec.isSpread()){
                double secondLegRatio=security.getLeg2Size()/security.getLeg1Size();
                double commisionSecondLeg=referenceExecQty*secondLegRatio;
                sendTradeConfirm(timestamp, matchID, holder, security.getLeg1(),referenceExecQty,  leg1Price,referenceExecQty);
                sendTradeConfirm(timestamp, matchID, holder, security.getLeg2(),commisionSecondLeg,leg2Price,commisionSecondLeg);
            }else{
                double leg3Price=PriceUtils.toDouble(spreadPriceProvider.getPrice(security.getLeg3(),holder.isBuy()),MatchConstants.IMPLIED_DECIMALS);

                double firstLegRatio=security.getLeg1Size()/security.getLeg2Size();
                double thirdLegRatio=security.getLeg3Size()/security.getLeg2Size();

                double relativeLeg1ExecQty=referenceExecQty*firstLegRatio;
                double relativeLeg3ExecQty=referenceExecQty*thirdLegRatio;

                sendTradeConfirm(timestamp, matchID, holder, security.getLeg1(),relativeLeg1ExecQty,leg1Price,relativeLeg1ExecQty);
                sendTradeConfirm(timestamp, matchID, holder, security.getLeg2(),referenceExecQty,leg2Price,referenceExecQty);
                sendTradeConfirm(timestamp, matchID, holder, security.getLeg3(),relativeLeg3ExecQty,leg3Price,relativeLeg3ExecQty);

            }
        }
        holder.clear();
    }

    private void sendTradeConfirm(long timestamp, int matchID, STPHolder<OUCHOrder> holder,Bond bond, double commision,double execPrice,double execQty) {
        OUCHTradeConfirmationCommand cmd=adaptor.getOUCHTradeConfirmationCommand();
        Trader trader = this.traders.get(holder.getTraderID());

        cmd.setClOrdID(holder.getLongClOrdId());
        cmd.setCommissionAmount(MatchPriceUtils.toQtyRoundLot((int)commision));
        cmd.setExecPrice(execPrice);
        cmd.setExecQty((int)(MatchConstants.QTY_MULTIPLIER*execQty));
        cmd.setMatchID(matchID);
        cmd.setSide(holder.isBuy() ? OUCHConstants.Side.Buy : OUCHConstants.Side.Sell);
        cmd.setSecurity(bond.getCUSIP());
        cmd.setSettlementDateAsDate(bond.getSettlementDate());
        cmd.setSettlementDate(TimeUtils.toDateInt(bond.getSettlementDate()));
        cmd.setTradeDate(todayTradeDate);
        cmd.setTradeTime(timestamp);
        cmd.setTrader(trader.getName());
        adaptor.send(cmd);

    }

}
