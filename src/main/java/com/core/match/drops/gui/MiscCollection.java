package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUISecurity;
import com.core.match.drops.gui.msgs.GUIStatus;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.events.SystemEventListener;
import com.core.match.services.events.SystemEventService;
import com.core.match.services.security.*;
import com.core.util.TimeUtils;
import com.gs.collections.impl.map.mutable.primitive.ShortObjectHashMap;

/**
 * Created by jgreco on 2/20/16.
 */
public class MiscCollection extends DropCollection implements
        SystemEventListener,
        SecurityServiceListener<BaseSecurity> {
    private final GUIStatus event;
    private final ShortObjectHashMap<GUISecurity> idSecurityMap = new ShortObjectHashMap<>();

    public MiscCollection(SecurityService<BaseSecurity> securities, SystemEventService systems,
                          LinearCounter versionCounter,
                          LinearCounter itemCounter) {
        super(versionCounter, itemCounter);

        systems.addListener(this);
        securities.addListener(this);

        event = new GUIStatus(itemCounter.incVersion());
        addVersion(event);
    }

    @Override
    public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            GUISecurity securityItem = new GUISecurity(itemCounter.incVersion(), security.getName());
            updateSecurity(securityItem, security, msg.getTimestamp());

            idSecurityMap.put(security.getID(), securityItem);
            addVersion(securityItem);
        }
        else {
            GUISecurity securityItem = idSecurityMap.get(security.getID());
            updateSecurity(securityItem, security, msg.getTimestamp());

            updateVersion(securityItem);
        }
    }

    private void updateSecurity(GUISecurity security, BaseSecurity sec, long timestamp) {
        security.setTime(timestamp);
        security.setTickSize(sec.getTickSize());
        security.setLotSize(sec.getLotSize());
        security.setSecType(sec.getType().getName());

        if (sec.isBond()) {
            Bond bond = (Bond) sec;
            security.setCusip(bond.getCUSIP());
            security.setMatDate(TimeUtils.toDateInt(bond.getMaturityDate()));
            security.setCoupon(bond.getCoupon());
        }
        else {
            security.setCusip("");
            security.setMatDate(0);
            security.setCoupon(0);
        }
    }

    @Override
    public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
        if (isNew) {
            GUISecurity securityItem = new GUISecurity(itemCounter.incVersion(), security.getName());
            updateSecurity(securityItem, security, msg.getTimestamp());

            idSecurityMap.put(security.getID(), securityItem);
            addVersion(securityItem);
        }
        else {
            GUISecurity securityItem = idSecurityMap.get(security.getID());
            updateSecurity(securityItem, security, msg.getTimestamp());

            updateVersion(securityItem);
        }
    }

    @Override
    public void onOpen(long timestamp) {
        event.setEvent(MatchConstants.SystemEvent.Open);
        event.setTime(timestamp);
        updateVersion(event);
    }

    @Override
    public void onClose(long timestamp) {
        event.setEvent(MatchConstants.SystemEvent.Close);
        event.setTime(timestamp);
        updateVersion(event);
    }
}
