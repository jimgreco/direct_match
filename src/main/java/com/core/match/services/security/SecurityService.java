package com.core.match.services.security;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchSecurityListener;
import com.core.match.util.MessageUtils;
import com.core.services.StaticsServiceBase;
import com.core.util.BinaryUtils;
import com.core.util.HolidayCalendar;
import com.core.util.TimeUtils;
import com.core.util.TradeDateUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;
import com.gs.collections.api.tuple.Pair;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.UnifiedMap;
import com.gs.collections.impl.tuple.Tuples;

import java.nio.ByteBuffer;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * User: jgreco
 */
public class SecurityService<T extends BaseSecurity> extends StaticsServiceBase<BaseSecurity> implements
		MatchSecurityListener {
	private final TradeDateUtils tradeDateUtils = new TradeDateUtils(MessageUtils.zoneID(), MatchConstants.SESSION_ROLLOVER_TIME);
    private final Map<ByteBuffer, BaseSecurity> securitiesByCUSIP = new UnifiedMap<ByteBuffer, BaseSecurity>();
	private final List<SecurityServiceListener<BaseSecurity>> listeners = new FastList<>();
	private final HolidayCalendar calendar;
	private final TimeSource source;

	public static SecurityService<BaseSecurity> create(Log log, TimeSource source) {
        return new SecurityService<>(log, source);
	}

	private SecurityService(Log log, TimeSource source) {
		super (MatchConstants.STATICS_START_INDEX);
		this.source = source;
		this.calendar = new HolidayCalendar(log);
	}

	public void addListener(SecurityServiceListener<BaseSecurity> listener) {
		listeners.add(listener);
	}

	@Override
	public void onMatchSecurity(MatchSecurityEvent msg) {
		boolean isNew = false;
		BaseSecurity security = get(msg.getSecurityID());
		if (security == null) {
			if (msg.getType()==MatchConstants.SecurityType.DiscreteButterfly || msg.getType()==MatchConstants.SecurityType.DiscreteSpread ||  msg.getType()==MatchConstants.SecurityType.Roll){
				security = new MultiLegSecurity(msg.getSecurityID(), msg.getNameAsString());
			}
			else {
				security = new Bond(msg.getSecurityID(), msg.getNameAsString());
			}
			add(security);
			isNew = true;
		}

		security.setTickSize(msg.getTickSize());
		security.setLotSize(msg.getLotSize());
		security.setType(SecurityType.lookup(msg.getType()));

		if (msg.getType()==MatchConstants.SecurityType.DiscreteButterfly || msg.getType()==MatchConstants.SecurityType.DiscreteSpread ||  msg.getType()==MatchConstants.SecurityType.Roll) {
			MultiLegSecurity multiLegSecurity = (MultiLegSecurity) security;
			multiLegSecurity.setNumberOfLegs(msg.getNumLegs());
			multiLegSecurity.setLeg1Size(msg.getLeg1Size());
			multiLegSecurity.setLeg2Size(msg.getLeg2Size());
			Bond secLeg1 = (Bond) get(msg.getLeg1ID());
			Bond secLeg2 = (Bond) get(msg.getLeg2ID());
			multiLegSecurity.setLeg1(secLeg1);
			multiLegSecurity.setLeg2(secLeg2);

			if (security.getType() == SecurityType.DISCRETE_BUTTERFLY) {
				Bond secLeg3 = (Bond) get(msg.getLeg3ID());;
				multiLegSecurity.setLeg3(secLeg3);
				multiLegSecurity.setLeg3Size(msg.getLeg3Size());
			}

			ByteBuffer cusipBuffer = BinaryUtils.createCopy(msg.getCUSIP());
			securitiesByCUSIP.put(cusipBuffer, multiLegSecurity);

			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).onMultiLegSecurityInstrument(multiLegSecurity, msg, security.getType(), isNew);
			}
		}
		else {
			Bond bond=(Bond)security;
			bond.category = security.getType() != null ? security.getType().getCategory() : SecurityCategory.BOND;

			bond.maturityDate = msg.getMaturityDateAsDate();
			bond.issueDate = msg.getIssueDateAsDate();
			bond.couponFrequency = msg.getCouponFrequency();

			LocalDate settlementDate = getNextValidSettlementDate(tradeDateUtils.getTradeDate(this.source.getTimestamp()));
			if (settlementDate.isBefore(bond.getIssueDate())) {
				settlementDate = bond.getIssueDate();
			}
			bond.settlementDate = settlementDate;

			Pair<LocalDate[], LocalDate> paymentDates = getPaymentDatesAndPreviousPayment(bond.getMaturityDate(), bond.getSettlementDate(), bond.getCouponFrequency());
			bond.paymentDates = paymentDates.getOne();
			bond.previousPaymentDate = paymentDates.getTwo();

			bond.coupon = msg.getCoupon();
			bond.cusip = msg.getCUSIPAsString();
			bond.setReferencePrice(msg.getReferencePrice());

			ByteBuffer cusipBuffer = BinaryUtils.createCopy(msg.getCUSIP());
			securitiesByCUSIP.put(cusipBuffer, bond);

			try {
				bond.term = Integer.parseInt(security.getName().replace("Y", ""));
			} catch (NumberFormatException e) {
				bond.term = 0;
			}
			for (int i = 0; i < listeners.size(); i++) {
				listeners.get(i).onBond(bond, msg, security.getType(), isNew);
			}
		}

	}

	private static Pair<LocalDate[], LocalDate> getPaymentDatesAndPreviousPayment(LocalDate maturityDate, LocalDate settlementDate, int couponFrequency) {
		List<LocalDate> payments = new FastList<>();
		
		LocalDate iterDate = maturityDate; 
		int monthsToSubtract = TimeUtils.MONTHS_PER_YEAR / couponFrequency;
		
		// while settlement date is < iterdate
		while( settlementDate.compareTo(iterDate) < 0 )
		{
			payments.add(0, iterDate);
			iterDate = TimeUtils.subtractMonthsMaintainRelativeDate(iterDate, monthsToSubtract); 
		}
		LocalDate[] arr = new LocalDate[payments.size()];
		return Tuples.pair(payments.toArray(arr), iterDate);
	}

	private LocalDate getNextValidSettlementDate(LocalDate settlementDate) {
		settlementDate = settlementDate.plusDays(1);
		while (!calendar.isTradingDate(settlementDate)) {
            settlementDate = settlementDate.plusDays(1);
		}
		return settlementDate;
	}


    public BaseSecurity getByCUSIP(ByteBuffer cusip) {
        return securitiesByCUSIP.get(cusip);
    }

    public Bond getBond(short ID){
        BaseSecurity baseSecurity=get(ID);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.BOND)){
            return (Bond)baseSecurity;
        }
        return null;
    }
    public MultiLegSecurity getMultiLegSecurityInstrument(short ID){
        BaseSecurity baseSecurity=get(ID);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.MULTI_LEG)){
            return (MultiLegSecurity)baseSecurity;
        }
        return null;
    }

    public Bond getBond(ByteBuffer name){
        BaseSecurity baseSecurity=get(name);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.BOND)){
            return (Bond)baseSecurity;
        }
        return null;
    }
    public MultiLegSecurity getMultiLegSecurityInstrument(ByteBuffer name){
        BaseSecurity baseSecurity=get(name);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.MULTI_LEG)){
            return (MultiLegSecurity)baseSecurity;
        }
        return null;
    }

    public Bond getBond(String name){
        BaseSecurity baseSecurity=get(name);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.BOND)){
            return (Bond)baseSecurity;
        }
        return null;
    }
    public MultiLegSecurity getMultiLegSecurityInstrument(String name){
        BaseSecurity baseSecurity=get(name);
        if(baseSecurity!=null && baseSecurity.getType().getCategory().equals(SecurityCategory.MULTI_LEG)){
            return (MultiLegSecurity)baseSecurity;
        }
        return null;
    }

    public boolean isMultiLegSecurity(String name) {
        return getMultiLegSecurityInstrument(name) != null;
    }

	public boolean isBond(String name) {
        return getBond(name) != null;
    }

	public boolean isBond(short securityID) {
		return getBond(securityID) != null;
	}
}
