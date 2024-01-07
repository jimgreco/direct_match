package com.core.match.services.contributor;

import com.core.match.msgs.MatchContributorEvent;

/**
 * Created by jgreco on 10/6/14.
 */
public interface ContributorServiceListener<T extends Contributor> {
    void onContributor(T contributor, MatchContributorEvent msg, boolean isNew);
}
