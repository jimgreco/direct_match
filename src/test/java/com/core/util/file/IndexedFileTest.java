package com.core.util.file;

import com.core.nio.SelectorService;
import com.core.util.BinaryUtils;
import com.core.util.log.SystemOutLog;
import com.core.util.time.SystemTimeSource;
import org.junit.Assert;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class IndexedFileTest {
    @SuppressWarnings("static-method")
	@Test
    public void testIndexFile() throws IOException {
        SystemTimeSource timeSource = new SystemTimeSource();
        IndexedFile file = new IndexedFile(new SelectorService(new SystemOutLog("CORE03-1", "TEST", timeSource), timeSource), new SystemOutLog("CORE03-1", "FOO", timeSource), "BAR.BIN");

        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.clear();
        buffer.put("Item 1".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        buffer.put("Item 22".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        buffer.put("Item 333".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        Assert.assertEquals(0, file.read(-1, buffer));

        buffer.clear();
        Assert.assertEquals(6, file.read(0, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 1"));

        buffer.clear();
        Assert.assertEquals(7, file.read(1, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 22"));

        buffer.clear();
        Assert.assertEquals(8, file.read(2, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 333"));

        buffer.clear();
        Assert.assertEquals(0, file.read(3, buffer));
    }

    @SuppressWarnings("static-method")
	@Test
    public void testGetMultipleMessages() throws IOException {
        SystemTimeSource timeSource = new SystemTimeSource();
        IndexedFile file = new IndexedFile(new SelectorService(new SystemOutLog("CORE03-1", "TEST", timeSource), timeSource), new SystemOutLog("CORE03-1", "FOO", timeSource), "BAR.BIN");

        ByteBuffer buffer = ByteBuffer.allocate(32);
        buffer.clear();
        buffer.put("Item 1".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        buffer.put("Item 22".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        buffer.put("Item 333".getBytes());
        buffer.flip();
        file.write(buffer);

        buffer.clear();
        Assert.assertEquals(0, file.read(-1, 1, buffer));

        buffer.clear();
        Assert.assertEquals(6 + 7, file.read(0, 2, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 1Item 22"));

        buffer.clear();
        Assert.assertEquals(7 + 8, file.read(1, 2, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 22Item 333"));

        buffer.clear();
        Assert.assertEquals(7 + 8, file.read(1, 3, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 22Item 333"));

        buffer.clear();
        Assert.assertEquals(7 + 8, file.read(1, 10, buffer));
        buffer.flip();
        Assert.assertTrue(BinaryUtils.compare(buffer, "Item 22Item 333"));

        buffer.clear();
        Assert.assertEquals(0, file.read(3, 1, buffer));
    }
}
