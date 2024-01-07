package com.core.match.db.jdbc;

import com.core.app.AppConstructor;
import com.core.app.Exposed;
import com.core.app.Param;
import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.connector.Connector;
import com.core.connector.Dispatcher;
import com.core.match.MatchApplication;
import com.core.match.db.jdbc.helpers.OrderWriter;
import com.core.match.db.jdbc.helpers.SecurityWriter;
import com.core.match.db.jdbc.msgs.AccountEventWriter;
import com.core.match.db.jdbc.msgs.CancelEventWriter;
import com.core.match.db.jdbc.msgs.CancelReplaceRejectEventWriter;
import com.core.match.db.jdbc.msgs.ClientCancelReplaceRejectEventWriter;
import com.core.match.db.jdbc.msgs.ClientOrderRejectEventWriter;
import com.core.match.db.jdbc.msgs.CommonEventWriter;
import com.core.match.db.jdbc.msgs.ContributorEventWriter;
import com.core.match.db.jdbc.msgs.FillEventWriter;
import com.core.match.db.jdbc.msgs.InboundEventWriter;
import com.core.match.db.jdbc.msgs.OrderEventWriter;
import com.core.match.db.jdbc.msgs.OrderRejectEventWriter;
import com.core.match.db.jdbc.msgs.OutboundEventWriter;
import com.core.match.db.jdbc.msgs.QuoteEventWriter;
import com.core.match.db.jdbc.msgs.ReplaceEventWriter;
import com.core.match.db.jdbc.msgs.SecurityEventWriter;
import com.core.match.db.jdbc.msgs.SystemEventWriter;
import com.core.match.db.jdbc.msgs.TraderEventWriter;
import com.core.match.msgs.*;
import com.core.match.services.account.Account;
import com.core.match.services.account.AccountService;
import com.core.match.services.order.OrderService;
import com.core.match.services.quote.VenueQuoteService;
import com.core.match.services.security.BaseSecurity;
import com.core.match.services.security.SecurityService;
import com.core.match.services.trader.Trader;
import com.core.match.services.trader.TraderService;
import com.core.util.log.Log;
import com.gs.collections.impl.map.mutable.primitive.CharObjectHashMap;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Created by jgreco on 12/27/14.
 */
public class JDBCWriter extends MatchApplication {
    private final JDBCEventQueue eventQueue;
    private final CharObjectHashMap<JDBCEventQueueDequeueListener> writerMap;
    private final Connection dbConnection;

    private final BatchPreparedStatement batchPreparedStatement;
    private final VenueQuoteService venueQuoteService;
    private HeartbeatNumberField currSeqNum;
    private HeartbeatNumberField lastDbCommitSeqNum;
    private final Connector connector;

    @AppConstructor
    public JDBCWriter(
            Log log,
            Connector connector,
            Dispatcher dispatcher,
            SecurityService<BaseSecurity> securityService,
            AccountService<Account> accounts,
            TraderService<Trader> traders,
            @Param(name = "Driver") String driver,
            @Param(name = "ConnectionString") String connectionString,
            @Param(name = "BatchSize") int batchSize,
            @Param(name = "BatchTimerMillis") int batchTimerMillis) throws SQLException, ClassNotFoundException {
        super(log);

        this.writerMap = new CharObjectHashMap<>();
        dbConnection = DriverManager.getConnection(connectionString);
        dbConnection.setAutoCommit(false);
        PreparedStatement preparedStatement= WriterUtilities.assemble(dbConnection);
        this.batchPreparedStatement=new BatchPreparedStatement(preparedStatement, log, dbConnection, batchSize);
        this.connector=connector;
        this.venueQuoteService = new VenueQuoteService(securityService);
        this.eventQueue = new JDBCEventQueue(venueQuoteService);

        OrderService<JDBCOrder> orders = OrderService.create(JDBCOrder.class, log, dispatcher);

        JDBCFieldsService fieldService = new JDBCFieldsService(orders, traders,securityService,accounts);

        CommonEventWriter commonEventWriter = new CommonEventWriter(connector,fieldService);

        SecurityWriter securityWriter = new SecurityWriter();
        OrderWriter orderWriter = new OrderWriter(fieldService, securityWriter);

        AccountEventWriter accountEventWriter = new AccountEventWriter(log, batchPreparedStatement, commonEventWriter, eventQueue);
        ContributorEventWriter contributorEventWriter = new ContributorEventWriter(log, batchPreparedStatement, commonEventWriter, eventQueue);
        SecurityEventWriter securityEventWriter = new SecurityEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, securityWriter, eventQueue);

        OrderEventWriter orderEventWriter = new OrderEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, orderWriter, eventQueue);
        OrderRejectEventWriter orderRejectEventWriter = new OrderRejectEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, securityWriter, eventQueue);
        ClientOrderRejectEventWriter clientOrderRejectEventWriter = new ClientOrderRejectEventWriter(log, batchPreparedStatement, commonEventWriter, eventQueue);

        CancelEventWriter cancelEventWriter = new CancelEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, orderWriter, eventQueue);
        ReplaceEventWriter replaceEventWriter = new ReplaceEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, orderWriter, eventQueue);
        CancelReplaceRejectEventWriter cancelReplaceRejectEventWriter = new CancelReplaceRejectEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, orderWriter, eventQueue);
        ClientCancelReplaceRejectEventWriter clientCancelReplaceRejectEventWriter = new ClientCancelReplaceRejectEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, eventQueue);

        FillEventWriter fillEventWriter = new FillEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, orderWriter, eventQueue);

        InboundEventWriter inboundEventWriter = new InboundEventWriter(log, batchPreparedStatement, commonEventWriter, eventQueue);
        OutboundEventWriter outboundEventWriter = new OutboundEventWriter(log, batchPreparedStatement, commonEventWriter, eventQueue);

        TraderEventWriter traderEventWriter=new TraderEventWriter(log,batchPreparedStatement,commonEventWriter, eventQueue);
        SystemEventWriter systemEventWriter=new SystemEventWriter(log,batchPreparedStatement,commonEventWriter, eventQueue);
        QuoteEventWriter quoteEventWriter = new QuoteEventWriter(log, batchPreparedStatement, fieldService, commonEventWriter, securityWriter, eventQueue);

        writerMap.put(MatchConstants.Messages.Account, accountEventWriter);
        writerMap.put(MatchConstants.Messages.Contributor, contributorEventWriter);
        writerMap.put(MatchConstants.Messages.Security, securityEventWriter);
        writerMap.put(MatchConstants.Messages.Order, orderEventWriter);
        writerMap.put(MatchConstants.Messages.OrderReject, orderRejectEventWriter);
        writerMap.put(MatchConstants.Messages.ClientOrderReject, clientOrderRejectEventWriter);
        writerMap.put(MatchConstants.Messages.Cancel, cancelEventWriter);
        writerMap.put(MatchConstants.Messages.Replace, replaceEventWriter);
        writerMap.put(MatchConstants.Messages.CancelReplaceReject, cancelReplaceRejectEventWriter);
        writerMap.put(MatchConstants.Messages.ClientCancelReplaceReject, clientCancelReplaceRejectEventWriter);
        writerMap.put(MatchConstants.Messages.Fill, fillEventWriter);
        writerMap.put(MatchConstants.Messages.Inbound, inboundEventWriter);
        writerMap.put(MatchConstants.Messages.Outbound, outboundEventWriter);
        writerMap.put(MatchConstants.Messages.Trader, traderEventWriter);
        writerMap.put(MatchConstants.Messages.SystemEvent, systemEventWriter);
        writerMap.put(MatchConstants.Messages.Quote, quoteEventWriter);

        dispatcher.subscribe(venueQuoteService);
        dispatcher.subscribe(securityService);
        dispatcher.subscribe(orders);
        dispatcher.subscribe(fieldService);

        dispatcher.subscribe(commonEventWriter);
        dispatcher.subscribe(securityWriter);
        dispatcher.subscribe(orderWriter);

        dispatcher.subscribe(accountEventWriter);
        dispatcher.subscribe(contributorEventWriter);
        dispatcher.subscribe(securityEventWriter);

        dispatcher.subscribe(orderEventWriter);
        dispatcher.subscribe(orderRejectEventWriter);
        dispatcher.subscribe(clientOrderRejectEventWriter);

        dispatcher.subscribe(cancelEventWriter);
        dispatcher.subscribe(replaceEventWriter);
        dispatcher.subscribe(cancelReplaceRejectEventWriter);
        dispatcher.subscribe(clientCancelReplaceRejectEventWriter);

        dispatcher.subscribe(fillEventWriter);

        dispatcher.subscribe(inboundEventWriter);
        dispatcher.subscribe(outboundEventWriter);
        dispatcher.subscribe(traderEventWriter);
        dispatcher.subscribe(systemEventWriter);
        dispatcher.subscribe(quoteEventWriter);

        Thread t = new Thread(new JDBCEventQueueConsumer(eventQueue, writerMap, batchPreparedStatement, log, batchTimerMillis));
        t.setDaemon(true);
        t.start();
    }



    @Exposed(name="closeDBConnection")
    public void close() throws SQLException {
        dbConnection.close();
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister register) {
        currSeqNum=register.addNumberField("DB", HeartBeatFieldIDEnum.LastSeenSeqNum);
        lastDbCommitSeqNum=register.addNumberField("DB",HeartBeatFieldIDEnum.LastDBCommit);

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater register) {
        currSeqNum.set(connector.getCurrentSeq());
        lastDbCommitSeqNum.set(batchPreparedStatement.getInsertCount());
    }
}
