package com.core.testharness.ouch;

/**
 * Created by hli on 4/17/16.
 */
public interface LatencyTestClient {
    long sendNewOrder(String security, Boolean isBuy, int qty, double price, String trader, boolean isIOC);

    boolean isLoggedIn();

    GenericLatencyMeasurer getOrderAcceptLatencyMeasurer();

}
