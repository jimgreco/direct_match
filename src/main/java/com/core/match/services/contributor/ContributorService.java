package com.core.match.services.contributor;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchContributorListener;
import com.core.services.StaticsFactory;
import com.core.services.StaticsServiceBase;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

/**
 * User: jgreco
 */
public class ContributorService<T extends Contributor> extends StaticsServiceBase<T> implements
        MatchContributorListener {
    private final StaticsFactory<T> factory;
    private final List<ContributorServiceListener<T>> listeners = new FastList<>();

    public static ContributorService<Contributor> create() {
        return new ContributorService<>(Contributor::new);
    }

    public ContributorService(StaticsFactory<T> factory) {
        super(MatchConstants.STATICS_START_INDEX);
        this.factory = factory;
    }

    public void addListener(ContributorServiceListener<T> listener) {
        listeners.add(listener);
    }

    @Override
    public void onMatchContributor(MatchContributorEvent msg) {
        boolean isNew = false;
        T contributor = get(msg.getSourceContributorID());
        if (contributor == null) {
            isNew = true;
            contributor = factory.create(msg.getSourceContributorID(), msg.getNameAsString());
            add(contributor);
        }
        contributor.setCancelOnDisconnect(msg.getCancelOnDisconnect());

        for (int i=0; i<listeners.size(); i++) {
            listeners.get(i).onContributor(contributor, msg, isNew);
        }
    }
}
