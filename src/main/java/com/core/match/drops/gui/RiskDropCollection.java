package com.core.match.drops.gui;

import com.core.match.drops.DropCollection;
import com.core.match.drops.LinearCounter;
import com.core.match.drops.gui.msgs.GUIAccountRiskValues;
import com.core.match.msgs.MatchAccountEvent;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.account.AccountServiceListener;
import com.core.match.services.risk.RiskAccount;
import com.core.match.services.risk.RiskService;
import com.core.match.services.risk.RiskServiceListener;
import com.gs.collections.api.map.primitive.MutableShortObjectMap;
import com.gs.collections.impl.map.mutable.primitive.ShortObjectHashMap;

public class RiskDropCollection extends DropCollection implements AccountServiceListener<Account>, RiskServiceListener {
	private final MutableShortObjectMap<GUIAccountRiskValues> accountsByID = new ShortObjectHashMap<>();

	public RiskDropCollection(LinearCounter versionCounter, LinearCounter itemCounter, AccountService<Account> accounts,
			RiskService<RiskDropOrder> riskService) {
		super(versionCounter, itemCounter);

		accounts.addListener(this);
		riskService.addListener(this);
	}

	@Override
	public void onAccount(Account account, MatchAccountEvent msg, boolean isNew) {
		if (isNew) {
			GUIAccountRiskValues guiAccountRiskValues = new GUIAccountRiskValues(msg.getAccountID(), msg.getNameAsString());
			accountsByID.put(msg.getAccountID(), guiAccountRiskValues);

			addVersion(guiAccountRiskValues);
		}
	}

	@Override
	public void onAccountRiskUpdate(RiskAccount acct) {
		GUIAccountRiskValues guiAccountRiskValues = accountsByID.get(acct.getID());

		guiAccountRiskValues.setOrderExposure(Math.round(acct.getOpenBuyDV01() + acct.getOpenSellDV01()));

		// TODO fill these out when risk service supports it
		// guiAccountRiskValues.setTradeExposure(val);
		// guiAccountRiskValues.setGrossActivity(val);

		updateVersion(guiAccountRiskValues);
	}
}