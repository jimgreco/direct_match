package com.core.match.fix;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 2/15/16.
 */
public interface FIXAttributes {
    ByteBuffer getClOrdID();
    boolean hasClOrdID();
    boolean isReplaced();
    double getNotional();
    boolean isIOC();
}
