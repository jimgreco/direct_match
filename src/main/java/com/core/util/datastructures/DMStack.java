package com.core.util.datastructures;

import com.core.util.datastructures.contracts.Linkable;

public class DMStack<T extends Linkable<T>> extends DMLinkedList<T>
{
	public T peek()
	{
		return this.root;
	}

	public T remove()
	{
		if( this.root == null ) return root;
		T toReturn = this.root;
		this.root = this.root.next();
		next = this.root;
		size--;
		return toReturn;
	}
	
	@Override
	public void add(T toAdd)
    {
		// add to the front
        if( this.root == null )
        {
            this.root = toAdd;
            next = this.root;
            tail = toAdd;
        }
        else
        {
        	toAdd.setNext(this.root	);
        	this.root = toAdd;
        	next = this.root;
        }

		size++;
    }
}
