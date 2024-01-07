package com.core.match.services.account;

import com.core.match.msgs.MatchAccountEvent;

/**
 * User: jgreco
 */
public interface AccountServiceListener<T extends Account> {
    void onAccount(T account, MatchAccountEvent msg, boolean isNew);
}
