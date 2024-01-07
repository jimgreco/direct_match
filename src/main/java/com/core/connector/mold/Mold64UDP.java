package com.core.connector.mold;

import java.io.IOException;

/**
 * User: jgreco
 */
public interface Mold64UDP {
    void setListener(Mold64UDPListener listener);
    void open() throws IOException;
    void close();

    String getMulticastGroup();
}
