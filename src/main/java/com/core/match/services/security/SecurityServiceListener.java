package com.core.match.services.security;

import com.core.match.msgs.MatchSecurityEvent;

/**
 * User: jgreco
 */
public interface SecurityServiceListener<T extends BaseSecurity> {
    void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew);
    void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew);
}
