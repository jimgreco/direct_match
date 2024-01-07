package com.core.util.math;

import java.time.LocalDate;

public class BondMath {
	private static final double ERROR_TOLERANCE = .00001;
	private static final int PAYMENT_FREQUENCY = 2;
    private static final int PRINCIPAL = 100;
    private static final double PERCENT_MULTIPLIER = 100.0;
    private static final double APPROX_DAYS_PER_YEAR = 365.25;
    private static final int MAX_TRIES = 100;

    public static double getDV01(double coupon,
                                  LocalDate maturityDate,
                                  LocalDate previousPaymentDate,
                                  LocalDate nextPaymentDate,
                                  int paymentsRemaining,
                                  double currentPrice,
                                  LocalDate settlementDate)
    {
        double yield = getYield(
                coupon,
                maturityDate,
                previousPaymentDate,
                nextPaymentDate,
                paymentsRemaining,
                currentPrice,
                settlementDate);

        // might be some overlap with getYield
        double duration = getDuration(
                coupon,
                previousPaymentDate,
                nextPaymentDate,
                paymentsRemaining,
                yield,
                settlementDate,
                currentPrice);

        double dirtyPrice = getDirtyPrice(currentPrice, coupon, previousPaymentDate, nextPaymentDate, settlementDate);
        return duration * dirtyPrice;
    }

    public static double getAccruedInterest(double coupon, LocalDate previousPaymentDate, LocalDate nextPaymentDate, LocalDate settlementDate) {
        double couponPayment = coupon / PAYMENT_FREQUENCY;
        long prevPaymentEpochDays = previousPaymentDate.toEpochDay();
        long daysInPeriod = nextPaymentDate.toEpochDay() - prevPaymentEpochDays;
        double pctIntoPeriod = 1.0 * (settlementDate.toEpochDay() - prevPaymentEpochDays) / daysInPeriod;
        return pctIntoPeriod * couponPayment;
    }
    
    public static double getDirtyPrice(double cleanPrice, double coupon, LocalDate prevPaymentDate, LocalDate nextPaymentDate, LocalDate settlementDate) {
        double accruedInterest = getAccruedInterest(coupon, prevPaymentDate, nextPaymentDate, settlementDate);
        return cleanPrice + accruedInterest;
    }

    public static double getMarkupMarkdownPrice(boolean buy, double cleanPrice, double coupon, LocalDate prevPaymentDate, LocalDate nextPaymentDate, LocalDate settlementDate, double commission) {
        double dirtyPrice = getDirtyPrice(cleanPrice, coupon, prevPaymentDate, nextPaymentDate, settlementDate);
        if (buy) {
            dirtyPrice += commission / 10000;
        }
        else {
            dirtyPrice -= commission / 10000;
        }
        return dirtyPrice;
    }

    public static double getDuration(double coupon,
								  LocalDate previousPaymentDate,
								  LocalDate nextPaymentDate,
								  int paymentsRemaining,
                                  double yield,
								  LocalDate settlementDate,
                                  double price) {
		double couponPayment = coupon / PAYMENT_FREQUENCY;

		// calc accrued interest
		long prevPaymentEpochDays = previousPaymentDate.toEpochDay();
		double pctIntoPeriod = 1.0 * (settlementDate.toEpochDay() - prevPaymentEpochDays) / (nextPaymentDate.toEpochDay() - prevPaymentEpochDays);
        double paymentFrac = 1.0 - pctIntoPeriod;

		return calculateDuration(paymentsRemaining, couponPayment, paymentFrac, yield, price);
	}

    public static double getYield(double coupon,
                                  LocalDate maturityDate,
                                  LocalDate previousPaymentDate,
                                  LocalDate nextPaymentDate,
                                  int paymentsRemaining,
                                  double currentPrice,
                                  LocalDate settlementDate) {
        double couponPayment = coupon / PAYMENT_FREQUENCY;

        // calc accrued interest
        long prevPaymentEpochDays = previousPaymentDate.toEpochDay();
        double pctIntoPeriod = 1.0 * (settlementDate.toEpochDay() - prevPaymentEpochDays) / (nextPaymentDate.toEpochDay() - prevPaymentEpochDays);
        double paymentFrac = 1.0 - pctIntoPeriod;
        double accruedInterest = pctIntoPeriod * couponPayment;

        // dirty price = target price
        double lastPrice = PRINCIPAL;
        double lastYield = coupon / PERCENT_MULTIPLIER;
        double yield = approxYTM(coupon, maturityDate, currentPrice, settlementDate) / PERCENT_MULTIPLIER;

        int bailout = 0;
        while (true) {
            if (bailout++ == MAX_TRIES) {
                break;
            }

            double price = calcPriceOptimized(paymentsRemaining, couponPayment, paymentFrac, yield) - accruedInterest;
            if (Math.abs(price - currentPrice) < ERROR_TOLERANCE) {
                break;
            }

            if (yield - lastYield == 0) {
                break;
            }

            // Newton method for finding zeros
            double derivative = (price - lastPrice) / (yield - lastYield);
            lastYield = yield;
            lastPrice = price;
            yield = yield - (price - currentPrice) / derivative;
        }

        return PERCENT_MULTIPLIER * yield;
    }

    private static double calcPriceOptimized(int numPayments,
                                             double couponPayment,
                                             double paymentFrac,
                                             double yield) {

        double yieldPerPeriod = yield / PAYMENT_FREQUENCY;
        double innerTerm = 1 + yieldPerPeriod;
        double pow1 = Math.pow(innerTerm, numPayments);
        double pow2 = Math.pow(innerTerm, 1 - paymentFrac) / pow1;
        double price = pow1 - 1;
        price *= couponPayment;
        price *= pow2;
        price /= yieldPerPeriod;
        price += pow2 * PRINCIPAL;
        return price;
    }

	public static double calculateDuration( int numPayments, double couponPayment, double paymentFrac, double yield, double price ) {
        price += (1 - paymentFrac) * couponPayment;
        yield /= PERCENT_MULTIPLIER;

        double yieldPerPeriod = yield / PAYMENT_FREQUENCY;
		double innerTerm = 1 + yieldPerPeriod;
		double timeWeightedPrice = 0;

		double ithPaymentFrac = 0;
		double ithYearFrac = 0;

		for (int i=0; i<numPayments; i++) {
			ithPaymentFrac = i + paymentFrac;
			ithYearFrac = ithPaymentFrac / PAYMENT_FREQUENCY;
			double discountedPayment = couponPayment * Math.pow(innerTerm, -ithPaymentFrac);
			timeWeightedPrice += ithYearFrac * discountedPayment;
		}

		timeWeightedPrice += PRINCIPAL * ithYearFrac * Math.pow(innerTerm, -ithPaymentFrac);
		return timeWeightedPrice / price;
	}

    public static double approxYTM(double coupon, LocalDate maturityDate, double price, LocalDate settlementDate) {
		double approxYearFrac = approxYearFrac(settlementDate, maturityDate);
		return PERCENT_MULTIPLIER * ((coupon + (PRINCIPAL - price) / approxYearFrac) / ((PRINCIPAL + price) / PAYMENT_FREQUENCY));
	}

	public static double approxYearFrac(LocalDate start, LocalDate end) {
		return (end.toEpochDay() - start.toEpochDay()) / APPROX_DAYS_PER_YEAR;
	}
}
