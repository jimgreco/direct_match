package com.core.fix;

import com.core.fix.tags.FixTagCreator;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public interface FixParser extends FixTagCreator {
    int parse(ByteBuffer buffer) throws InvalidFixMessageException;
}
