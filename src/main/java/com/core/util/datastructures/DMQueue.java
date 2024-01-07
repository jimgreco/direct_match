package com.core.util.datastructures;

import com.core.util.datastructures.contracts.Linkable;

public class DMQueue<T extends Linkable<T>> extends DMLinkedList<T>
{	
	public T peek()
	{
		return this.root;
	}

	public T remove()
	{
		if( this.root == null)
		{
			return null;
		}
		T oldRoot = this.root;
		this.root = this.root.next();
		size--;
		return oldRoot;
	}
}
