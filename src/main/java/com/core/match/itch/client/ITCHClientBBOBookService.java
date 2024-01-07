package com.core.match.itch.client;

import com.core.match.itch.msgs.ITCHSecurityEvent;
import com.core.match.msgs.MatchConstants;
import com.core.services.bbo.BBOBookService;
import com.core.services.price.PriceLevelBookService;

/**
 * Created by jgreco on 7/7/15.
 */
public class ITCHClientBBOBookService extends BBOBookService implements ITCHClientSecurityServiceListener {
    public ITCHClientBBOBookService(PriceLevelBookService bookService, ITCHClientSecurityService securities) {
        super(bookService, MatchConstants.IMPLIED_DECIMALS, 1);

        securities.addListener(this);
    }

    @Override
    public void onSecurity(ITCHClientSecurity security, ITCHSecurityEvent msg, boolean isNew) {
        if (isNew) {
            addBook(security.getID(), security.getName());
        }
    }
}
