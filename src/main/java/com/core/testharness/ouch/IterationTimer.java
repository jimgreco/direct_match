package com.core.testharness.ouch;

import com.core.nio.SelectorService;
import com.core.util.time.TimerHandler;

import java.util.Random;

import static com.core.util.TimeUtils.NANOS_PER_MILLI;

public class IterationTimer implements TimerHandler
{
	private final GenericLatencyMeasurer orderToAcceptLatencies;
	private int lastOutstanding = 0;
	private int numOrders = 0;
	private int numWait = 0;
	private SelectorService select;
	private LatencyTestClient testClient;
	private final String trader;
	private final String[] securities = new String[] { "2Y", "3Y", "5Y", "7Y", "10Y", "30Y"};
	private final Random random = new Random();
	private boolean sendIOC;

	public IterationTimer(int numOrders,
						  int numWait,
						  boolean sendIOC,
						  String trader,
						  SelectorService select,
						  LatencyTestClient testClient)
	{
		this.trader = trader;
		this.numOrders = numOrders;
		this.sendIOC = sendIOC;
		this.numWait = numWait;
		this.select = select;
		this.testClient = testClient;
		this.orderToAcceptLatencies = testClient.getOrderAcceptLatencyMeasurer();

	}

	@Override
	public void onTimer(int internalTimerID, int referenceData) {
		if (!testClient.isLoggedIn()) {
			select.scheduleTimer(100 * NANOS_PER_MILLI, this);
			return;
		}

		if (orderToAcceptLatencies.getOutstanding() > 0) {
			select.scheduleTimer(10 * NANOS_PER_MILLI, this);
		} else {
			select.scheduleTimer(NANOS_PER_MILLI, this);
		}

		if (numOrders-- > 0) {
			testClient.sendNewOrder(
					securities[random.nextInt(securities.length)],
					false,
					random.nextInt(4) + 1,
					100.0,
					trader,
					sendIOC);
		}

		int numOutstanding = orderToAcceptLatencies.getOutstanding();
		if (lastOutstanding == numOutstanding) {
			numWait--;
		}
		lastOutstanding = numOutstanding;

		if (numOutstanding == 0 || numWait <= 0) {

			GenericLatencyMeasurer.LatencyStats acceptStats = orderToAcceptLatencies.measure();
			System.out.println("Order To Accept Latencies");

			printStatistic(acceptStats);

			System.exit(0);
		}
	}

	private void printStatistic(GenericLatencyMeasurer.LatencyStats stats){
		System.out.println("Measurements: " + stats.getMeasurements());
		System.out.println("Missing: " + stats.getOutstanding());
		System.out.println("Best: " + stats.getPercentileInMicros(0));
		System.out.println("10%: " + stats.getPercentileInMicros(0.10));
		System.out.println("20%: " + stats.getPercentileInMicros(0.2));
		System.out.println("25%: " + stats.getPercentileInMicros(0.25));
		System.out.println("30%: " + stats.getPercentileInMicros(0.30));
		System.out.println("40%: " + stats.getPercentileInMicros(0.40));
		System.out.println("50%: " + stats.getPercentileInMicros(0.50));
		System.out.println("60%: " + stats.getPercentileInMicros(0.6));
		System.out.println("70%: " + stats.getPercentileInMicros(0.7));
		System.out.println("80%: " + stats.getPercentileInMicros(0.8));
		System.out.println("90%: " + stats.getPercentileInMicros(0.90));
		System.out.println("95%: " + stats.getPercentileInMicros(0.95));
		System.out.println("99%: " + stats.getPercentileInMicros(0.99));
		System.out.println("99.5%: " + stats.getPercentileInMicros(0.995));
		System.out.println("99.9%: " + stats.getPercentileInMicros(0.999));
		System.out.println("Worst: " + stats.getPercentileInMicros(1));

		System.out.println("Mean: " + stats.getMean());
		System.out.println("Standard Deviation: " + stats.getStdDev());


		System.out.println();
		System.out.println("Done...");
	}

}
