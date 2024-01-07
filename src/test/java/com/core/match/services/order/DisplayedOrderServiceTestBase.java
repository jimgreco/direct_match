package com.core.match.services.order;

import com.core.match.GenericAppTest;
import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchFillCommand;
import com.core.match.msgs.MatchOrderCommand;
import com.core.match.msgs.MatchReplaceCommand;
import com.core.util.PriceUtils;

/**
 * Created by jgreco on 1/23/16.
 */
public abstract class DisplayedOrderServiceTestBase extends GenericAppTest<DisplayedOrder> {
    public DisplayedOrderServiceTestBase() {
        super(DisplayedOrder.class);
    }
}
