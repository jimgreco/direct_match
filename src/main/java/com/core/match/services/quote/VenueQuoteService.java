package com.core.match.services.quote;

import com.core.match.msgs.MatchConstants;
import com.core.match.msgs.MatchQuoteEvent;
import com.core.match.msgs.MatchQuoteListener;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.Bond;
import com.core.match.services.security.MultiLegSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.security.SecurityServiceListener;
import com.core.match.services.security.SecurityType;
import com.gs.collections.impl.list.mutable.FastList;

import java.util.List;

import static com.core.match.msgs.MatchConstants.Venue.Bloomberg;
import static com.core.match.msgs.MatchConstants.Venue.InteractiveData;

/**
 * Created by jgreco on 12/25/14.
 */
public class VenueQuoteService implements MatchQuoteListener {
    private final static int BLOOMBERG_INDEX = 0;
    private final static int IDC_INDEX = 1;

    private final List<QuoteUpdateListener> listeners = new FastList<>();
    private final List<Venue> venues = new FastList<>();
    private final SecurityService<BaseSecurity> securities;

    public VenueQuoteService(final SecurityService<BaseSecurity> secs) {
        securities = secs;

        // TODO: hard coded, can use reflection
        venues.add(new Venue(MatchConstants.Venue.toString(Bloomberg)));
        venues.add(new Venue(MatchConstants.Venue.toString(InteractiveData)));

        securities.addListener(new SecurityServiceListener<BaseSecurity>() {
            @Override
            public void onBond(Bond security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    for (Venue venue : venues) {
                        venue.addSecurity(security);
                    }
                }
            }

            @Override
            public void onMultiLegSecurityInstrument(MultiLegSecurity security, MatchSecurityEvent msg, SecurityType type, boolean isNew) {
                if (isNew) {
                    for (Venue venue : venues) {
                        venue.addSecurity(security);
                    }
                }
            }


        });
    }

    @Override
    public void onMatchQuote(MatchQuoteEvent msg) {
        Venue venue = getVenueByCode(msg.getVenueCode());
        if (venue == null) {
            return;
        }

        QuoteUpdatedFlags flags = venue.update(
                msg.getSecurityID(),
                msg.getBidPrice(),
                msg.getOfferPrice(),
                msg.getTimestamp(),
                msg.getSourceTimestamp());

        if (flags.isFlagSet()) {
            Quote quote = venue.get(msg.getSecurityID());

            for (QuoteUpdateListener listener : listeners) {
                listener.onQuoteUpdate(quote, flags);
            }
        }
    }

    public void addListener(QuoteUpdateListener listener) {
        listeners.add(listener);
    }

    public Quote getQuote(char venue, Bond security) {
        return getQuote(venue, security.getID());
    }

    public Quote getQuote(char venueCode, int securityID) {
        if (!securities.isValid(securityID)) {
            return null;
        }

        Venue venue = getVenueByCode(venueCode);
        return venue != null ? venue.get(securityID) : null;
    }

    public long getCoreTime(char venueCode) {
        Venue venue = getVenueByCode(venueCode);
        return venue != null ? venue.getCoreTime() : 0;
    }

    public long getSourceTime(char venueCode) {
        Venue venue = getVenueByCode(venueCode);
        return venue != null ? venue.getSourceTime() : 0;
    }

    public int getUpdates(char venueCode) {
        Venue venue = getVenueByCode(venueCode);
        return venue != null ? venue.getUpdates() : 0;
    }

    private Venue getVenueByCode(char venueCode) {
        switch (venueCode) {
            case Bloomberg:
                return getVenueByIndex(BLOOMBERG_INDEX);
            case InteractiveData:
                return getVenueByIndex(IDC_INDEX);
        }
        return null;
    }

    private Venue getVenueByIndex(int index) {
        return venues.get(index);
    }
}
