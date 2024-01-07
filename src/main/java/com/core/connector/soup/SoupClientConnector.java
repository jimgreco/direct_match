package com.core.connector.soup;

import com.core.connector.soup.msgs.SoupUnsequencedDataCommand;

/**
 * Created by jgreco on 6/30/15.
 */
public interface SoupClientConnector extends SoupCommonConnector {
    void send(SoupUnsequencedDataCommand cmd);
    String getSession();
    String getTargetHost();

}
