package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHSecurityEvent;
import com.core.match.itch.msgs.ITCHSecurityListener;
import com.core.match.msgs.MatchConstants;
import com.core.services.StaticsServiceBase;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * Created by jgreco on 7/6/15.
 */
public class ITCHClientSecurityService extends StaticsServiceBase<ITCHClientSecurity> implements ITCHSecurityListener {
    private final List<ITCHClientSecurityServiceListener> listeners = new FastList<>();

    public ITCHClientSecurityService() {
        super(MatchConstants.STATICS_START_INDEX);
    }

    public void addListener(ITCHClientSecurityServiceListener listener) {
        listeners.add(listener);
    }

    @Override
    public void onITCHSecurity(ITCHSecurityEvent msg) {
        boolean isNew = size() == msg.getSecurityID() - MatchConstants.STATICS_START_INDEX;

        ITCHClientSecurity security;
        if (isNew) {
            security = new ITCHClientSecurity(msg.getSecurityID(), msg.getNameAsString());
            add(security);
        }
        else {
            security = get(msg.getSecurityID());
        }

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onSecurity(security, msg, isNew);
        }
    }
}
