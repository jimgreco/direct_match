package com.core.match.services.security;

import java.time.LocalDate;

/**
 * User: jgreco
 */
public class Bond extends BaseSecurity {
	byte couponFrequency;
	String cusip;
	LocalDate maturityDate;
	LocalDate issueDate;

	public void setSettlementDate(LocalDate settlementDate) {
		this.settlementDate = settlementDate;
	}

	LocalDate settlementDate;
	long coupon;
	SecurityCategory category;
	int term;
	LocalDate[] paymentDates;
	LocalDate previousPaymentDate;

	public long getReferencePrice() {
		return referencePrice;
	}

	public void setReferencePrice(long referencePrice) {
		this.referencePrice = referencePrice;
	}

	long referencePrice;


	public LocalDate getMaturityDate() {
		return this.maturityDate;
	}

	public LocalDate getIssueDate() {
		return this.issueDate;
	}

	public LocalDate getSettlementDate() {
		return this.settlementDate;
	}

	@Override
	public boolean isMultiLegInstrument() {
		return false;
	}

	@Override
	public boolean isBond() {
		return true;
	}

	@Override
	public boolean isSpread() {
		return false;
	}

	@Override
	public boolean isButterfly() {
		return false;
	}

	public Bond(short id, String name) {
		super(id, name);
	}

	public String getCUSIP() {
		return cusip;
	}

	public long getCoupon() {
		return coupon;
	}


	public SecurityCategory getCategory() {
		return category;
	}

	public int getTerm() {
		return term;
	}

	public byte getCouponFrequency() {
		return couponFrequency;
	}

	public LocalDate[] getPaymentDates() {
		return paymentDates;
	}

	public LocalDate getPreviousPaymentDate() {
		return previousPaymentDate;
	}

	public LocalDate getNextPaymentDate() {
		return paymentDates != null && paymentDates.length > 0 ? paymentDates[0] : null;
	}

	public void setMaturityDate(LocalDate maturityDate) {
		this.maturityDate = maturityDate;
	}

	public void setCoupon(long coupon) {
		this.coupon = coupon;
	}

	public void setCUSIP(String cusip) {
		this.cusip = cusip;
	}
}
