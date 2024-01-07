package com.core.match.ouch.client;

import com.core.match.ouch.msgs.OUCHAcceptedEvent;
import com.core.match.ouch.msgs.OUCHAcceptedListener;
import com.core.match.ouch.msgs.OUCHCancelRejectedEvent;
import com.core.match.ouch.msgs.OUCHCancelRejectedListener;
import com.core.match.ouch.msgs.OUCHCanceledEvent;
import com.core.match.ouch.msgs.OUCHCanceledListener;
import com.core.match.ouch.msgs.OUCHFillEvent;
import com.core.match.ouch.msgs.OUCHFillListener;
import com.core.match.ouch.msgs.OUCHRejectedEvent;
import com.core.match.ouch.msgs.OUCHRejectedListener;
import com.core.match.ouch.msgs.OUCHReplacedEvent;
import com.core.match.ouch.msgs.OUCHReplacedListener;
import com.core.match.ouch.msgs.OUCHTradeConfirmationEvent;
import com.core.match.ouch.msgs.OUCHTradeConfirmationListener;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.FastList;
import com.gs.collections.impl.map.mutable.primitive.LongObjectHashMap;

import java.util.List;

/**
 * Created by jgreco on 8/16/15.
 */
public class OUCHClientOrderService implements
        OUCHAcceptedListener,
        OUCHReplacedListener,
        OUCHCanceledListener,
        OUCHRejectedListener,
        OUCHCancelRejectedListener,
        OUCHFillListener,
        OUCHTradeConfirmationListener
{
    private final Log log;
    private final LongObjectHashMap<OUCHClientOrder> orders = new LongObjectHashMap<>();
    private final List<OUCHClientOrderServiceListener> listeners = new FastList<>();

    public OUCHClientOrderService(Log log) {
        this.log = log;
    }

    public void addListener(OUCHClientOrderServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onOUCHAccepted(OUCHAcceptedEvent msg) {
        if(log.isDebugEnabled()){
            log.info(log.log().add("RX ACCEPT: ").add(msg.getClOrdID()));

        }
        OUCHClientOrder order = new OUCHClientOrder(msg);
        orders.put(msg.getClOrdID(), order);
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHAccepted(msg, order);
        }
    }

    @Override
    public void onOUCHReplaced(OUCHReplacedEvent msg) {
        if(log.isDebugEnabled()){
            log.debug(log.log().add("RX REPLACE FOR: ").add(msg.getOldClOrdId()).add(" with: ").add(msg.getClOrdID()));
        }
        OUCHClientOrder order = orders.remove(msg.getOldClOrdId());
        if (order == null) {
            log.error(log.log().add("UNKNOWN REPLACE. CRITICAL ERROR!"));
            return;
        }
        
        orders.put(msg.getClOrdID(), order);

        order.replaced(msg.getClOrdID(), msg.getQty(), msg.getPrice());
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHReplaced(msg, order);
        }
    }

    @Override
    public void onOUCHCanceled(OUCHCanceledEvent msg) {
        if(log.isDebugEnabled()){
            log.debug(log.log().add("RX CANCEL: ").add(msg.getClOrdID()));
        }
        OUCHClientOrder order = orders.remove(msg.getClOrdID());
        if (order == null) {
            log.error(log.log().add("UNKNOWN CANCEL. CRITICAL ERROR!"));
            return;
        }

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHCanceled(msg, order);
        }
    }

    @Override
    public void onOUCHCancelRejected(OUCHCancelRejectedEvent msg) {
        OUCHClientOrder order = orders.get(msg.getClOrdID());

        if (order == null) {
            log.error(log.log().add("UNKNOWN CANCEL REJECT. CRITICAL ERROR!"));
            return;
        }
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHCancelRejected(msg, order);
        }
    }

    @Override
    public void onOUCHRejected(OUCHRejectedEvent msg) {
        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHRejected(msg);
        }
    }

    @Override
    public void onOUCHFill(OUCHFillEvent msg) {
        OUCHClientOrder order = orders.get(msg.getClOrdID());
        if (order == null) {
            log.error(log.log().add("UNKNOWN FILL. CRITICAL ERROR!"));
            return;
        }

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHFill(msg, order);
        }
    }

    @Override
    public void onOUCHTradeConfirmation(OUCHTradeConfirmationEvent msg) {

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onOUCHTradeConfirm(msg);
        }
    }
}
