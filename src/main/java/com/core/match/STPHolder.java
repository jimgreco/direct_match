package com.core.match;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.services.order.Order;
import com.core.util.BinaryUtils;

import java.nio.ByteBuffer;

public class STPHolder<T extends Order<T>>
{
	private int accumulatedQty = 0;
	private double accumulatedNotional = 0;
	private final ByteBuffer clOrdID = ByteBuffer.allocate(MatchConstants.CLORDID_LENGTH);
	private long longClOrdID;
	private short traderID; 
	private short securityID;
	private boolean buy;
	private int id;

	public void addFill(T order, ByteBuffer bufferClOrdID, long clOrdID, MatchFillEvent event)
	{
		if (getOrderID() != order.getID()) {
			clear();
		}

		if (bufferClOrdID != null) this.setClOrdId(bufferClOrdID);
		else this.longClOrdID = clOrdID;
		this.buy = order.isBuy();
		this.id = order.getID();
		this.securityID = order.getSecurityID();
		this.traderID = order.getTraderID();

		accumulatedNotional += (event.getPriceAsDouble() * event.getQty());
		accumulatedQty += (event.getQty());
	}

	public long getLongClOrdId()
	{
		return this.longClOrdID; 
	}
	
	public ByteBuffer getClOrdId()
	{
		return this.clOrdID;
	}
	
	public void setClOrdId( ByteBuffer id )
	{
		this.clOrdID.clear();
		BinaryUtils.copy(this.clOrdID, id);
		this.clOrdID.flip();
	}
	
	public int getAccumulatedQty()
	{
		return accumulatedQty;
	}

	public double getAveragePrice()
	{
		return accumulatedNotional / accumulatedQty;
	}

	public void clear()
	{
		this.accumulatedNotional = 0;
		this.accumulatedQty = 0;
		this.clOrdID.clear();
		this.buy = false;
		this.id = 0;
		this.securityID = 0;
		this.traderID = 0;
	}

	public short getSecurityID()
	{
		return securityID;
	}

	public short getTraderID()
	{
		return traderID;
	}

	public boolean isBuy()
	{
		return buy;
	}

	public int getOrderID()
	{
		return id;
	}
}
