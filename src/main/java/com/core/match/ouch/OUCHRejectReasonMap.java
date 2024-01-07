package com.core.match.ouch;

import com.core.match.msgs.MatchConstants;
import com.core.match.ouch.msgs.OUCHConstants;

/**
 * Created by jgreco on 7/11/15.
 */
public class OUCHRejectReasonMap {
    public static char matchToOUCH(char reason) {
        switch (reason) {
            case MatchConstants.OrderRejectReason.InvalidAccount:
                return OUCHConstants.RejectReason.InvalidAccount;
            case MatchConstants.OrderRejectReason.InvalidTrader:
                return OUCHConstants.RejectReason.InvalidTrader;
            case MatchConstants.OrderRejectReason.InvalidSecurity:
                return OUCHConstants.RejectReason.InvalidSecurity;
            case MatchConstants.OrderRejectReason.InvalidPrice:
                return OUCHConstants.RejectReason.InvalidPrice;
            case MatchConstants.OrderRejectReason.InvalidQuantity:
                return OUCHConstants.RejectReason.InvalidQuantity;
            // invalid side (client only)
            // invalid hidden qty (not implemented)
            // invalid tif (not implemented)

            // duplicate clordid (client only)
            // unknown clordid (client only)
            case MatchConstants.OrderRejectReason.UnknownOrderID:
                return OUCHConstants.RejectReason.TooLateToCancelOrModify;

            case MatchConstants.OrderRejectReason.AccountDisabled:
                return OUCHConstants.RejectReason.AccountDisabled;
            case MatchConstants.OrderRejectReason.TraderDisabled:
                return OUCHConstants.RejectReason.TraderDisabled;
            case MatchConstants.OrderRejectReason.SecurityDisabled:
                return OUCHConstants.RejectReason.SecurityDisabled;

            case MatchConstants.OrderRejectReason.TradingSystemClosed:
                return OUCHConstants.RejectReason.TradingSystemClosed;
            // risk violation (client only)
            default:
                return OUCHConstants.RejectReason.InternalError;
        }
    }
}
