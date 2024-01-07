package com.core.match.fairies.cancel;

import com.core.match.services.order.AbstractOrder;

/**
 * Created by jgreco on 8/11/15.
 */
public class CancelFairyOrder extends AbstractOrder<CancelFairyOrder> {
    private short contributorID;

    @Override
    public void clear() {
        super.clear();

        contributorID = 0;
    }

	public short getContributorID() {
        return contributorID;
    }

	public void setContributorID(short contributorID) {
        this.contributorID = contributorID;
    }
}