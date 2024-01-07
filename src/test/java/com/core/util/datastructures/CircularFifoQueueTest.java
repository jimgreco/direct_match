package com.core.util.datastructures;

import org.junit.Assert;
import org.junit.Test;

import java.util.Iterator;

/**
 * Created by johnlevidy on 6/2/15.
 */
public class CircularFifoQueueTest {
    
    @SuppressWarnings("static-method")
	@Test
    public void testBasicInsertionAndIteration()
    {

        CircularFifoQueue<StringBuilder> queue = new CircularFifoQueue<>(5, new ClassFactory<StringBuilder>(StringBuilder.class) {
            @Override
            public StringBuilder newInstance() {
                return new StringBuilder(" ");
            }
        });
        queue.add().setCharAt(0, 'A');
        queue.add().setCharAt(0, 'B');

        String[] expectedResults = { "A", "B" };
        assertEquals(queue.iterator(), expectedResults);
    }

    @SuppressWarnings("static-method")
	@Test
    public void testWrapping()
    {
        CircularFifoQueue<StringBuilder> queue = new CircularFifoQueue<>(5, new ClassFactory<StringBuilder>(StringBuilder.class) {
            @Override
            public StringBuilder newInstance() {
                return new StringBuilder(" ");
            }
        });

        queue.add().setCharAt(0, 'A');
        queue.add().setCharAt(0, 'B');
        queue.add().setCharAt(0, 'C');
        queue.add().setCharAt(0, 'D');
        queue.add().setCharAt(0, 'E');
        queue.add().setCharAt(0, 'F');
        queue.add().setCharAt(0, 'G');

        String[] expectedResults = { "C", "D", "E", "F", "G" };
        assertEquals(queue.iterator(), expectedResults);

        queue.reset();
        assertEquals(queue.iterator(), new String[0]);


        queue.add().setCharAt(0, 'A');
        String[] results = { "A" };
        assertEquals(queue.iterator(), results );

    }

    public static void assertEquals( Iterator<StringBuilder> iterator, String[] expected )
    {

        for( int j = 0; j < expected.length; j++ )
        {
            Assert.assertEquals(expected[j], iterator.next().toString());
        }
    }
}
