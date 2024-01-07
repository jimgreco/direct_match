package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHSecurityEvent;

/**
 * Created by jgreco on 7/6/15.
 */
public interface ITCHClientSecurityServiceListener {
    void onSecurity(ITCHClientSecurity security, ITCHSecurityEvent msg, boolean isNew);
}
