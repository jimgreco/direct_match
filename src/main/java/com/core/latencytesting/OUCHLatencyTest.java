package com.core.latencytesting;

import com.core.nio.SelectorService;
import com.core.testharness.ouch.IterationTimer;
import com.core.testharness.ouch.NullEventCounter;
import com.core.testharness.ouch.OuchTestClient;
import com.core.util.time.TimerHandler;
import static com.core.util.TimeUtils.NANOS_PER_MILLI;


import java.io.IOException;


public class OUCHLatencyTest {

	public static void append( StringBuilder builder, String str )
	{
		builder.append( str ).append("\n");
	}
	
	public static void append( StringBuilder builder, long l)
	{
		builder.append( l ).append("\n");
	}

	public static void main(String args[]) throws InterruptedException, IOException {
		if( args.length < 5 ) {
			System.out.println("Invalid usage - [ iterations (int), host (string), port (int), sendIOC, trader (string)");
			return;
		}

		int iterations = Integer.parseInt(args[0]);
		String host = args[1];
		int port = Integer.parseInt(args[2]);
		boolean sendIOC = Boolean.parseBoolean(args[3]);
        String trader = args[4];


        System.out.println("Orders to send: " + iterations);
		System.out.println(host + ":" + port);
		System.out.println("SendIOC: " + sendIOC);

		LatencyTestFactory testFactory= new LatencyTestFactory("VM01-1", "TEST01");
        SelectorService selectorService=testFactory.getSelectorService();
		OuchTestClient testClient = new OuchTestClient( host, port, "OUCHUN", "OUCHPW", 1000000, selectorService, testFactory.getTimeSource(), testFactory.getLogger(), new NullEventCounter());
		System.out.print("Waiting for login response...");
		System.out.println("Done.");

        TimerHandler timerHandler = new IterationTimer(iterations, 10000, sendIOC, trader, selectorService, testClient);
        selectorService.scheduleTimer(NANOS_PER_MILLI, timerHandler);
        selectorService.run();
    }
}

