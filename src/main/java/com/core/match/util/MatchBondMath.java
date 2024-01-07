package com.core.match.util;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchPrinter;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.util.PriceUtils;
import com.core.util.math.BondMath;

/**
 * Created by jgreco on 7/10/15.
 */
public class MatchBondMath {
    public static double getSignedDV01(Bond security, long price, int qty, boolean isBuyOrder) {
        double sign = isBuyOrder ? 1 : -1;
        return sign * getDV01(security,price,qty);

    }

    public static double getSignedDV01(MultiLegSecurity spread,int qty ,boolean isBuy){
        if(spread.getNumLegs()==2) {
            double dv01FrontLeg = MatchBondMath.getDV01(spread.getLeg1(), spread.getLeg1().getReferencePrice(), qty);
            double dv01SecondLeg = MatchBondMath.getDV01(spread.getLeg2(), spread.getLeg2().getReferencePrice(),(int) (qty *spread.getLeg2Size() / spread.getLeg1Size()));
            return Math.abs(dv01FrontLeg - dv01SecondLeg) * (isBuy ? 1 : -1);
        }
        else{
            double dv01FrontLeg = MatchBondMath.getDV01(spread.getLeg1(), spread.getLeg1().getReferencePrice(), (int)(qty * spread.getLeg1Size() / spread.getLeg2Size()));
            double dv01SecondLeg = MatchBondMath.getDV01(spread.getLeg2(), spread.getLeg2().getReferencePrice(), qty);
            double dv01ThirdLeg = MatchBondMath.getDV01(spread.getLeg2(), spread.getLeg3().getReferencePrice(),(int)(qty * spread.getLeg3Size() / spread.getLeg2Size()));
            return Math.abs(dv01SecondLeg-dv01FrontLeg - dv01ThirdLeg) * (isBuy ? 1 : -1);
        }
    }
    public static double getDV01(Bond security, long price, int qty) {
        double roundLotQty = MatchPriceUtils.toQtyRoundLot(qty);
        return roundLotQty * BondMath.getDV01(
                MatchPriceUtils.toDouble(security.getCoupon()),
                security.getMaturityDate(),
                security.getPreviousPaymentDate(),
                security.getNextPaymentDate(),
                security.getPaymentDates().length,
                MatchPriceUtils.toDouble(price),
                security.getSettlementDate()) ;
    }

// TODO: get DV01 for spreads

    public static double getNetMoney(Bond security, boolean buy, long price, int qty, long commission) {
        long fullQty = qty * MatchConstants.QTY_MULTIPLIER;
        double coupon = MatchPriceUtils.toDouble(security.getCoupon());
        double commissionDbl = MatchPriceUtils.toDouble(commission);
        double cleanPrice = MatchPriceUtils.toDouble(price);
        double markupMarkdown = BondMath.getMarkupMarkdownPrice(
                buy,
                cleanPrice,
                coupon,
                security.getPreviousPaymentDate(),
                security.getNextPaymentDate(),
                security.getSettlementDate(),
                commissionDbl);
        return fullQty * (markupMarkdown / 100);
    }
}
