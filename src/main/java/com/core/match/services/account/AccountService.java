package com.core.match.services.account;

import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchAccountListener;
import com.core.match.msgs.MatchConstants;
import com.core.services.StaticsFactory;
import com.core.services.StaticsServiceBase;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * User: jgreco
 */
public class AccountService<T extends Account> extends StaticsServiceBase<T> implements
        MatchAccountListener{

    private final StaticsFactory<T> factory;
    private final List<AccountServiceListener<T>> listeners = new FastList<>();
    
    public static AccountService<Account> create() {
        return new AccountService<>(Account::new);
    }

    public AccountService(StaticsFactory<T> factory) {
        super(MatchConstants.STATICS_START_INDEX);
        this.factory = factory;
    }

    @Override
    public void onMatchAccount(MatchAccountEvent msg) {
        boolean isNew = false;
        T account = get(msg.getAccountID());
        if (account == null) {
            account = factory.create(msg.getAccountID(), msg.getNameAsString());
            add(account);
            isNew = true;
        }

        account.setNetDV01Limit(msg.getNetDV01Limit());
        if(msg.hasSSGMID()){
            account.setStateStreetInternalID(msg.getSSGMIDAsString());
        }
        account.setNetting(msg.getNettingClearing());
        account.setCommission(msg.getCommission());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onAccount(account, msg, isNew);
        }
    }

    public void addListener(AccountServiceListener<T> listener) {
        listeners.add(listener);
    }
}
