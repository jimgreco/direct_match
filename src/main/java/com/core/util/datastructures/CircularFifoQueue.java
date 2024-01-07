package com.core.util.datastructures;

import java.lang.reflect.Array;
import java.util.Iterator;

/**
 * Created by johnlevidy on 6/2/15.
 */
public class CircularFifoQueue<T>
{
    private int next = 0;
    int sizeSinceReset = 0;
    int beginIterator = 0;
    final T[] underlyingArray;

    @SuppressWarnings("unchecked")
	public CircularFifoQueue( int maxSize, ClassFactory<T> factory)
    {
    	// TODO: Straighten this out
        this.underlyingArray = ( T[] )Array.newInstance(factory.getClazz(), maxSize);
        for( int i = 0; i < maxSize; i++ )
        {
            this.underlyingArray[i] = factory.newInstance();
        }
    }

    public void reset()
    {
        sizeSinceReset = 0;
    }

    // returns a reference to the item to manipulate
    public T add()
    {
        sizeSinceReset = Math.min(underlyingArray.length, sizeSinceReset+1);
        return underlyingArray[next++%underlyingArray.length];
    }

    public Iterator<T> iterator()
    {
        if( underlyingArray[next%underlyingArray.length] != null )
        {
            beginIterator = ( next- sizeSinceReset )%underlyingArray.length;
        }
        return myIterator;
    }

    private final Iterator<T> myIterator = new Iterator<T>() {

        @Override
        public boolean hasNext() {
            return CircularFifoQueue.this.sizeSinceReset > 0;
        }

        @Override
        public T next() {
            CircularFifoQueue.this.sizeSinceReset--;
            return CircularFifoQueue.this.underlyingArray[beginIterator++%CircularFifoQueue.this.underlyingArray.length];
        }
    };
}
