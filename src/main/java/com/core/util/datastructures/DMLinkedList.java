package com.core.util.datastructures;

import com.core.util.datastructures.contracts.Linkable;

import java.util.Iterator;

/**
 * Created by johnlevidy on 6/3/15.
 */
public class DMLinkedList<T extends Linkable<T>> implements Iterable<T> {
    protected T root = null;
    protected T tail = null;
    protected T next = null;
    protected int size;

    private final Iterator<T> iterator = new Iterator<T>() {
        @Override
        public boolean hasNext() {
            return next != null;
        }

        @Override
        public T next() {
            T tmp = next;
            next = next.next();
            return tmp;
        }
    };

    public void add(T toAdd)
    {
        if( root == null )
        {
            root = toAdd;
            next = root;
            tail = toAdd;
        }
        else
        {
            if( root == tail )
            {
                root.setNext(toAdd);
            }
            else
            {
                tail.setNext(toAdd);
            }
            tail = toAdd;
        }
        size++;
    }

    public void clear()
    {
        root = null;
        tail = null;
        next = root;
        size = 0;
    }

    @Override
	public Iterator<T> iterator()
    {
        return this.iterator;
    }

    public int size() {
        return size;
    }
}
