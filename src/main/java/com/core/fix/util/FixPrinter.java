package com.core.fix.util;

import com.core.fix.connector.FIXConnectorListener;
import com.core.fix.msgs.FixConstants;
import com.core.util.log.Log;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 1/5/15.
 */
public class FixPrinter implements FIXConnectorListener {
    private final Log log;

    public FixPrinter(Log log) {
        this.log = log;
    }

    @Override
    public void onFIXMessageSent(ByteBuffer msg) {
        if (log.isDebugEnabled()) {
            prepareFixForPrinting(msg);
            log.debug(log.log().add("TX ").add(msg));
            reverseFixPrinting(msg);
        }
    }

    @Override
    public void onFIXMessageRecv(ByteBuffer msg) {
        if (log.isDebugEnabled()) {
            prepareFixForPrinting(msg);
            log.debug(log.log().add("RX ").add(msg));
            reverseFixPrinting(msg);
        }
    }

    public static void prepareFixForPrinting(ByteBuffer fixBuffer) {
        for (int i=fixBuffer.position(); i<fixBuffer.limit(); i++) {
            byte b = fixBuffer.get(i);
            if (b == FixConstants.SOH) {
                fixBuffer.put(i, FixConstants.PRINTED_SOH);
            }
            else if (b == FixConstants.PRINTED_SOH) {
                fixBuffer.put(i, FixConstants.SOH);
            }
        }
    }

    public static void reverseFixPrinting(ByteBuffer fixBuffer) {
        for (int i=fixBuffer.position(); i<fixBuffer.limit(); i++) {
            byte b = fixBuffer.get(i);
            if (b == FixConstants.PRINTED_SOH) {
                fixBuffer.put(i, FixConstants.SOH);
            }
            else if (b == FixConstants.SOH) {
                fixBuffer.put(i, FixConstants.PRINTED_SOH);
            }
        }
    }
}
