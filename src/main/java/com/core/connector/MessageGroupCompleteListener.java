package com.core.connector;

/**
 * Created by jgreco on 6/14/15.
 */
public interface MessageGroupCompleteListener {
    void onMessageGroupComplete(long nextSeqNum);
}
