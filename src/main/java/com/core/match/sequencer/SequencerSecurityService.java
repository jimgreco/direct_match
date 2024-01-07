package com.core.match.sequencer;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchSecurityListener;
import com.core.sequencer.CoreSequencerBaseService;
import com.core.util.log.Log;
import com.gs.collections.impl.list.mutable.primitive.CharArrayList;
import com.gs.collections.impl.list.mutable.primitive.IntArrayList;
import com.gs.collections.impl.list.mutable.primitive.LongArrayList;

class SequencerSecurityService extends CoreSequencerBaseService implements MatchSecurityListener
{
	private final LongArrayList tickSizes = new LongArrayList();
	private final IntArrayList lotSizes = new IntArrayList();
	private final CharArrayList securityTypes = new CharArrayList();
	private final IntArrayList minSpreadSizes = new IntArrayList();

	private final SequencerBookService books;

	public SequencerSecurityService(Log log, SequencerBookService books) {
		super(log, "Securities");

		this.books = books;
	}

	public short addSecurity(String name, long tickSize, int lotSize, char securityType,int minSpreadSize) {
		tickSizes.add(tickSize);
		lotSizes.add(lotSize);
		securityTypes.add(securityType);
		minSpreadSizes.add(minSpreadSize*lotSize);
		books.addBook();
		return (short)add(name);
	}

	public long getTickSize(int securityID)
	{
		return tickSizes.get(getIndex(securityID));
	}

	public int getLotSize(int securityID)
	{
		return lotSizes.get(getIndex(securityID));
	}

	@Override
	public void onMatchSecurity(MatchSecurityEvent msg) {
		short id = msg.getSecurityID();
		long tickSize = msg.getTickSize();
		int lotSize = msg.getLotSize();
		char type= msg.getType();
		int minimumSpreadSize = msg.getLeg1Size()*lotSize;


		int index = getIndex(id);

		if (isValid(id)) {
			// updating an old
			tickSizes.set(index, tickSize);
			lotSizes.set(index, lotSize);
			securityTypes.set(index,type);
			minSpreadSizes.set(index,minimumSpreadSize);
		}
		else if (index != size()) {
			// index must be one greater than last
			log.error(log.log().add("Invalid SecurityID. Expected=").add(size() + MatchConstants.STATICS_START_INDEX)
					.add(", Recv=").add(id));
		}
		else {
			// adding a new security
			addSecurity(msg.getNameAsString(), tickSize, lotSize, type,minimumSpreadSize);
		}
	}

	public char getType(short securityID) {
		return securityTypes.get(getIndex(securityID));
	}

	public int getMinimumSize(short securityID) {
		return minSpreadSizes.get(getIndex(securityID) );
	}
}
