package com.core.match.db.jdbc.securities;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.db.jdbc.DatabaseAdaptor;
import com.core.match.msgs.MatchConstants;
import com.core.match.services.quote.Quote;
import com.core.match.services.quote.VenueQuoteService;
import com.core.match.services.security.*;
import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.core.util.time.TimeSource;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class SecuritiesDefinitionDrop extends MatchApplication implements TimerHandler {
    private final SecurityService securities;
    private final VenueQuoteService quotes;
    private DatabaseAdaptor databaseAdaptor;
    private Connector connector;
    private String INSERT_QUERY=" insert into daily_securities (session,timestamp,security,bid,offer,mid,ratio1,ratio2,ratio3) values " +
            "(?,?,?,?,?,?,?,?,?)";
    private final Map<Short,Double> securityPriceCache;
    private final TimeSource timeSource;
    private final boolean bloombergAsDataSource;

    @AppConstructor
    public SecuritiesDefinitionDrop(TimeSource timeSource,
                                    Connector connector,
                                    SecurityService securityService,
                                    Dispatcher dispatcher,
                                    Log log,
                                    TimerService timerService,
                                    @Param(name = "Driver") String driver,
                                    @Param(name = "ConnectionString") String connectionString,
                                    @Param(name="Minutes") int interval,
                                    @Param(name="UseBBG") boolean useBBGAsSource
                                    ) {
        super(log);
        securities = securityService;
        this.timeSource=timeSource;
        this.connector=connector;
        this.bloombergAsDataSource=useBBGAsSource;
        quotes = new VenueQuoteService(securityService);
        databaseAdaptor =new DatabaseAdaptor(driver,connectionString,log,INSERT_QUERY,"securities_definition");
        dispatcher.subscribe(quotes);
        dispatcher.subscribe(securities);
        long nowPlus = TimeUnit.MINUTES.toNanos(interval);
        log.info(log.log().add("Time now is ").add(timeSource.getTimestamp()).add(" will update daily_securities db in ").add(interval).add( " minutes"));
        timerService.scheduleTimer(nowPlus,this);
        securityPriceCache=new UnifiedMap<>();
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {

    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        log.info(log.log().add("Writing to daily_securities table"));
        String session = connector.getSession();
        long now = timeSource.getTimestamp();
        Iterator<BaseSecurity> secIter = securities.iterator();
        while (secIter.hasNext()) {
            BaseSecurity security = secIter.next();
            SecurityDefinitionDAO secDefDao= new SecurityDefinitionDAO();
            if (security.isBond()) {
                Quote quote = bloombergAsDataSource? quotes.getQuote(MatchConstants.Venue.Bloomberg, security.getID()):quotes.getQuote(MatchConstants.Venue.InteractiveData, security.getID());
                SecurityDefinitionDAOBuilder.build((Bond)security,quote,session,now,secDefDao);
                securityPriceCache.put(secDefDao.id,secDefDao.mid);
            } else {
                if (security.isSpread()) {
                    MultiLegSecurity multiLegSecurity = (MultiLegSecurity) security;
                    SecurityDefinitionDAOBuilder.buildDiscreteSpread(multiLegSecurity,session,now,securityPriceCache,secDefDao);
                } else {
                    MultiLegSecurity multiLegSecurity = (MultiLegSecurity) security;
                    SecurityDefinitionDAOBuilder.buildDiscreteButterFly(multiLegSecurity,session,now,securityPriceCache,secDefDao);;
                }
            }
            try {
                commitToDataBase(secDefDao);
            } catch (SQLException e) {
                log.error(log.log().add("Exception writing securities.").add(e.getMessage()));
            }
        }
    }

    private void commitToDataBase(SecurityDefinitionDAO sd) throws SQLException {
            PreparedStatement preparedStatement=databaseAdaptor.getPreparedStatement();
            Timestamp timestamp=new Timestamp(sd.nano/ TimeUtils.NANOS_PER_MILLI);
            preparedStatement.setString(1,sd.session);
            preparedStatement.setTimestamp(2,timestamp);
            preparedStatement.setString(3,sd.security);
            preparedStatement.setDouble(4,sd.bid);
            preparedStatement.setDouble(5,sd.ask);
            preparedStatement.setDouble(6,sd.mid);
            preparedStatement.setDouble(7,sd.ratio1);
            preparedStatement.setDouble(8,sd.ratio2);
            preparedStatement.setDouble(9,sd.ratio3);
            preparedStatement.addBatch();
            databaseAdaptor.executeAndCommitToDB();
        //Write To DB
    }

}
