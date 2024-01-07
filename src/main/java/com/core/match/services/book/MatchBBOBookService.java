package com.core.match.services.book;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.core.services.bbo.BBOBook;
import com.core.services.bbo.BBOBookService;
import com.core.services.price.PriceLevelBookService;

/**
 * Created by jgreco on 1/26/15.
 */
public class MatchBBOBookService extends BBOBookService {
    public MatchBBOBookService(PriceLevelBookService bookService, SecurityService<BaseSecurity> securities) {
        super(bookService, MatchConstants.IMPLIED_DECIMALS, MatchConstants.QTY_MULTIPLIER);

        securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    addBook(security.getID(), security.getName());
                }
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if(isNew){
                    addBook(security.getID(),security.getName());
                }
            }

        });
    }

    public BBOBook get(BaseSecurity security) {
        return get(security.getID());
    }
}
