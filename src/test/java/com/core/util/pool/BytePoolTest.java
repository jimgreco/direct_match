package com.core.util.pool;

import org.junit.Test;

/**
 * Created by jgreco on 8/11/15.
 */
public class BytePoolTest {
    @SuppressWarnings("static-method")
	@Test
    public void testCreate() {
        BytePool pool = new BytePool(5, 10);
        pool.create();
        pool.create();
    }
}
