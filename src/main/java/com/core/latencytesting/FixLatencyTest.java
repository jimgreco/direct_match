package com.core.latencytesting;

import com.core.match.msgs.MatchByteBufferMessages;
import com.core.nio.SelectorService;
import com.core.testharness.ouch.GenericLatencyMeasurer;
import com.core.testharness.ouch.IterationTimer;
import com.core.util.tcp.TCPSocketFactory;
import com.core.util.time.TimerHandler;

import java.io.IOException;

import static com.core.util.TimeUtils.NANOS_PER_MILLI;

/**
 * Created by hli on 4/17/16.
 */
public class FixLatencyTest {
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


        LatencyTestFactory testFactory= new LatencyTestFactory("VM03-1", "FIXCLIENT01");
        TCPSocketFactory socketFactory=testFactory.getSelectorService();
        System.out.print("Waiting for login response...");

        FIXLatencyClient fixLatencyClient=new FIXLatencyClient(testFactory.getLogger(),socketFactory,testFactory.getSelectorService(),testFactory.getTimeSource(),new MatchByteBufferMessages(),host,(short)port,4,"DMDEV","JIM01",trader );

        fixLatencyClient.setLatencyMeasurer(new GenericLatencyMeasurer(testFactory.getTimeSource(),testFactory.getLogger()));

        SelectorService selectorService=testFactory.getSelectorService();
        TimerHandler timerHandler = new IterationTimer(iterations, 10000, sendIOC, trader, selectorService, fixLatencyClient);
        selectorService.scheduleTimer(NANOS_PER_MILLI, timerHandler);
        selectorService.run();


    }
}
