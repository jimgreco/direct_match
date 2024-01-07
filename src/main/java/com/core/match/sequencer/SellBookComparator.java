package com.core.match.sequencer;

import com.core.services.limit.LimitBook;

public class SellBookComparator implements LimitBookComparator
{	
	@Override
	public SequencerOrder getTopOfBook(LimitBook<SequencerOrder> book)
	{
		return book.getBestBid(); 
	}

	@Override
	public boolean isFirstPriceAggressive(long price1, long price2)
	{
		return price1 > price2;
	}
}
