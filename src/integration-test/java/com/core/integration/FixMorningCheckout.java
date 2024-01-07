package com.core.integration;

import com.core.latencytesting.QuickFixApp;
import com.core.testharness.ouch.CountDownLatchEventCounter;
import com.core.testharness.ouch.EventCounter;
import com.core.testharness.ouch.LatencyMeasurer;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import quickfix.ConfigError;
import quickfix.SessionID;
import quickfix.SessionNotFound;
import quickfix.SessionSettings;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Created by hli on 11/11/15.
 */
public class FixMorningCheckout {

    private static QuickFixApp quickFixApp;
    private  static SessionID sessionID;
    private static String trader="LLIU";
    private String security= "3Y";

    @BeforeClass
    public static void setUp() throws ConfigError, InterruptedException {

        sessionID = new SessionID("FIX.4.4", "JIM01", "DMDEV");

        SessionSettings settings = new SessionSettings();
        // general
        settings.setString(sessionID, "StartTime", "00:05:00");
        settings.setString(sessionID, "EndTime", "23:55:00");
        settings.setBool(sessionID, "UseLocalTime", true);
        settings.setBool(sessionID, "CheckLatency", false);

        settings.setBool(sessionID, "UseDataDictionary", true);
        settings.setString(sessionID, "FileStorePath", "/tmp");

        // client
        settings.setString(sessionID, "ConnectionType", "initiator");
        settings.setLong(sessionID, "HeartBtInt", 30);
        settings.setLong(sessionID, "ReconnectInterval", 160);
        settings.setBool(sessionID, "ResetSeqNumFlag", true);


        // order entry
        settings.setString(sessionID, "SocketConnectHost", "10.9.11.27");
        settings.setLong(sessionID, "SocketConnectPort", 10223);
        settings.setBool(sessionID, "ResetOnLogout", true);
        settings.setBool(sessionID, "ResetOnDisconnect", true) ;


        EventCounter counter = new CountDownLatchEventCounter();
        quickFixApp = new QuickFixApp(settings,counter);
        quickFixApp.addLatencyMeasurer(new NullLatencyMeasurer());
        quickFixApp.setAccount(trader);
        Thread thread = new Thread(() -> {
            try {
                quickFixApp.start();
            } catch (ConfigError configError) {
                configError.printStackTrace();
            }
        });
        thread.start();
        //What we are trying to do here is to reset the seq num on both the inbound and outbound to 1 so that we can start sending orders
        InputStream is=null;
        try {
            URL url = new URL("http://dev03.directmatchx.com:8005/JIM01A/resetSeqNumForTest_DoNotExecute?callback=JSON_CALLBACK");
             is = url.openStream();

        } catch (MalformedURLException e) {


        } catch (IOException e) {

        }
        Thread.sleep(5000);

    }
    @AfterClass
    public static void tearDown(){
        quickFixApp.stop();
        quickFixApp.onLogout(sessionID);

    }

    @Test
    public void sendNewOrder_allFields_receiveAccept() throws SessionNotFound, InterruptedException {

        quickFixApp.expectAccepted(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, 1, security, 100);
        quickFixApp.waitAccept();
        assertTrue(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));

    }

    @Test
    public void sendNewOrder_invalidSecurity_receiveReject() throws SessionNotFound, InterruptedException {
        quickFixApp.expectRejected(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, 1, "10G", 100);
        quickFixApp.waitReject();
        assertFalse(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));
        assertTrue(quickFixApp.getFixRejectMessage(String.valueOf(orderid)));
    }

    @Test
    public void sendNewOrder_negQty_receiveReject() throws SessionNotFound, InterruptedException {
        quickFixApp.expectRejected(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, -1, security, 100);
        quickFixApp.waitReject();
        assertFalse(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));
        assertTrue(quickFixApp.getFixRejectMessage(String.valueOf(orderid)));
    }

    @Test
    public void sendCancel_any_getCancelAccept() throws SessionNotFound, InterruptedException {
        quickFixApp.expectAccepted(1);

        long orderid=quickFixApp.sendOrder(sessionID, true, 1, security, 100);
        quickFixApp.waitAccept();
        assertTrue(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));

        Boolean result=quickFixApp.sendCancel(sessionID, orderid);
        quickFixApp.waitCancel();
        assertTrue(result);
    }


    @Test
    public void sendReplace_replaceQty_replaceAccept() throws SessionNotFound, InterruptedException {
        double price=100.0;
        int updatedQty=25;
        quickFixApp.expectAccepted(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, 1, security, 100);

        quickFixApp.waitAccept();
        assertTrue(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));
        quickFixApp.expectReplaced(1);
        long replaceID=quickFixApp.sendReplace(sessionID, orderid, updatedQty, price);
        quickFixApp.waitReplace();
        assertTrue(quickFixApp.getFixReplaceAccept(String.valueOf(replaceID)));

    }

    @Test
    public void sendReplace_dv01RiskViolation_replaceRejected() throws SessionNotFound, InterruptedException {
        double price=100.0;
        int updatedQty=1000000000;
        quickFixApp.expectAccepted(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, 1, security, 100);

        quickFixApp.waitAccept();
        assertTrue(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));
        quickFixApp.expectCancelReject(1);
        long replaceID=quickFixApp.sendReplace(sessionID, orderid, updatedQty, price);
        quickFixApp.waitCancelReject();
        assertFalse(quickFixApp.getFixReplaceAccept(String.valueOf(replaceID)));
        assertTrue(quickFixApp.getFixCancelRejectMessage(String.valueOf(replaceID)));

    }

    @Test
    public void sendReplace_replacePrice_replaceAccepted() throws SessionNotFound, InterruptedException {
        double updatedPrice=99;

        quickFixApp.expectAccepted(1);
        long orderid=quickFixApp.sendOrder(sessionID, true, 1, security, 100);

        quickFixApp.waitAccept();
        assertTrue(quickFixApp.getFixAcceptMessage(String.valueOf(orderid)));
        quickFixApp.expectReplaced(1);
        long replaceID=quickFixApp.sendReplace(sessionID, orderid, 1, updatedPrice);
        quickFixApp.waitReplace();
        assertTrue(quickFixApp.getFixReplaceAccept(String.valueOf(replaceID)));

    }


    static class NullLatencyMeasurer implements LatencyMeasurer {
        public NullLatencyMeasurer() {
        }

        @Override
        public void start(long id) {
        }

        @Override
        public void stop(long id) {
        }
    }
}
