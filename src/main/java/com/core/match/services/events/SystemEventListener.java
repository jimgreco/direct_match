package com.core.match.services.events;

/**
 * Created by jgreco on 6/15/15.
 */
public interface SystemEventListener {
    void onOpen(long timestamp);
    void onClose(long timestamp);
}
