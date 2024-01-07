package com.core.util.datastructures;

import java.util.Iterator;

/**
 * Created by johnlevidy on 6/1/15.
 */
public class ArraySliceIterator<T> implements Iterator<T>
{
    private final T[] underlyingArray;
    private int currentIndex = 0;
    private int boundary = 0;

    public ArraySliceIterator(T[] underlyingArray)
    {
        this.underlyingArray = underlyingArray;
    }

    public void setBoundary( int boundary )
    {
        this.boundary = boundary;
    }

    public void reset()
    {
        this.currentIndex = 0;
    }

    public int size()
    {
    	return this.underlyingArray.length; 
    }
    
    @Override
    public boolean hasNext() {
        return currentIndex < underlyingArray.length && currentIndex < boundary;
    }

    @Override
    public T next() {
        if( hasNext() ) {
            // TODO: make this less stupid
            return underlyingArray[currentIndex++];
        }
        return null;
    }
}
