package com.core.match.sequencer;

import com.core.services.limit.LimitBook;

public class BuyBookComparator implements LimitBookComparator
{
	@Override
	public SequencerOrder getTopOfBook(LimitBook<SequencerOrder> book)
	{
		return book.getBestOffer();
	}

	@Override
	public boolean isFirstPriceAggressive(long price1, long price2)
	{
		return price1 < price2;
	}
}
