package com.core.match.sequencer;

import com.core.services.limit.LimitBook;


public interface LimitBookComparator
{
	boolean isFirstPriceAggressive(long price1, long price2);

	SequencerOrder getTopOfBook(LimitBook<SequencerOrder> book);
}
