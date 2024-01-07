package com.core.sequencer;

import com.core.GenericTest;
import com.core.util.log.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by jgreco on 10/17/16.
 */
public class CoreSequencerContributorServiceTest extends GenericTest {
    private TestContributorService service;

    @Before
    public void before() {
        service = new TestContributorService(log);
    }

    @Test
    public void testAddContributor() {
        Assert.assertEquals(0, service.size());

        service.addContributor("FOO01");
        Assert.assertEquals(0, service.getSeqNum(1));
        Assert.assertEquals(1, service.size());
    }

    @Test
    public void testIncSeqNum() {
        short c1 = service.addContributor("FOO01");
        short c2 = service.addContributor("BAR01");

        Assert.assertEquals(0, service.getSeqNum(c1));
        service.incSeqNum(c1);
        Assert.assertEquals(1, service.getSeqNum(c1));
        service.incSeqNum(c2);
        service.incSeqNum(c2);
        service.incSeqNum(c2);
        service.incSeqNum(c1);
        Assert.assertEquals(2, service.getSeqNum(c1));
        Assert.assertEquals(3, service.getSeqNum(c2));
    }

    @Test
    public void testSetSeqNum() {
        short c1 = service.addContributor("FOO01");
        short c2 = service.addContributor("BAR01");

        service.incSeqNum(c1);
        service.setSeqNum(2, 3);

        Assert.assertEquals(1, service.getSeqNum(c1));
        Assert.assertEquals(3, service.getSeqNum(c2));
    }

    @Test
    public void testGetOrAdd() {
        short c1 = service.getOrAddContributor("FOO01");
        service.incSeqNum(c1);
        Assert.assertEquals(c1, service.getOrAddContributor("FOO01"));
        Assert.assertEquals(1, service.size());
    }

    private class TestContributorService extends CoreSequencerContributorService {
        public TestContributorService(Log log) {
            super(log);
        }
    }
}
