package com.core.nio;

import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by jgreco on 1/21/15.
 */
public class NIOFileTest {
    @SuppressWarnings("static-method")
	@Test
    public void testWrite() throws IOException {
        NIOFile file = new NIOFile("FOO.BIN", "rw");

        ByteBuffer byteBuffer = ByteBuffer.allocateDirect(100);
        byte[] bytes = "FOOOOOOOOOO".getBytes();
        byteBuffer.put(bytes);
        byteBuffer.flip();

        file.write(byteBuffer);

        Assert.assertEquals(0, byteBuffer.remaining());
        Assert.assertEquals(bytes.length, file.size());
    }
}
