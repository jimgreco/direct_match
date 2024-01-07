package com.core.util.datastructures;

import com.core.util.datastructures.contracts.AbstractLinkable;
import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

public class DMCollectionsTest
{
	private class Temp extends AbstractLinkable<Temp>
    {
        private final String str;

        public Temp(String str)
        {
            this.str = str;
        }

        public String getStr()
        {
            return this.str;
        }
    }
	
	@Test
    public void testLinkedList()
    {
        DMLinkedList<Temp> list = new DMLinkedList<>();
        list.add(new Temp("A"));
        list.add(new Temp("B"));
        list.add(new Temp("C"));
        list.add(new Temp("D"));

        Iterator<Temp> iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("B", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("C", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("D", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());

        list.clear();
        Assert.assertFalse(iter.hasNext());

        list.add(new Temp("A"));
        iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());
    }
	

	@Test
    public void testStack()
    {
        DMStack<Temp> list = new DMStack<>();
        list.add(new Temp("A"));
        list.add(new Temp("B"));
        list.add(new Temp("C"));
        list.add(new Temp("D"));

        Iterator<Temp> iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("D", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("C", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("B", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());

        Temp tmp = list.remove();
        // make sure the D got removed andt hat the iterator still works
        Assert.assertEquals("D", tmp.getStr());

        
        iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("C", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("B", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());
        
        list.clear();
        Assert.assertFalse(iter.hasNext());

        list.add(new Temp("A"));
        iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());
    }
	
	@Test
    public void testQueue()
    {
        DMQueue<Temp> list = new DMQueue<>();
        list.add(new Temp("A"));
        list.add(new Temp("B"));
        list.add(new Temp("C"));
        list.add(new Temp("D"));

        Iterator<Temp> iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("B", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("C", iter.next().getStr());
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("D", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());

        list.clear();
        Assert.assertFalse(iter.hasNext());

        list.add(new Temp("A"));
        iter = list.iterator();
        Assert.assertTrue(iter.hasNext());
        Assert.assertEquals("A", iter.next().getStr());
        Assert.assertFalse(iter.hasNext());


    }
}
