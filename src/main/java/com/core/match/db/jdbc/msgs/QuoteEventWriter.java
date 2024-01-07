package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.helpers.SecurityWriter;
import com.core.match.msgs.MatchQuoteByteBufferMessage;
import com.core.match.msgs.MatchQuoteEvent;
import com.core.match.msgs.MatchQuoteListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class QuoteEventWriter implements
        FieldWriter<MatchQuoteEvent>,
        JDBCEventQueueDequeueListener,
        MatchQuoteListener {
    private final Log log;
    private final CommonEventWriter commonWriter;
    private final SecurityWriter securityWriter;
    private final JDBCEventQueue eventQueue;
    private final BatchPreparedStatement runningStatement;
    private final JDBCFieldsService service;

    public QuoteEventWriter(Log log,
                            BatchPreparedStatement preparedStatement,
                            JDBCFieldsService service,
                            CommonEventWriter commonWriter,
                            SecurityWriter securityWriter,
                            JDBCEventQueue eventQueue) {
        this.log = log;
        this.service = service;
        this.commonWriter = commonWriter;
        this.securityWriter = securityWriter;
        this.eventQueue = eventQueue;
        this.runningStatement = preparedStatement;
    }

    @Override
    public void write(MatchQuoteEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        JDBCFieldsService.JDBCSecurity security = service.getSecurity(message.getSecurityID());
        commonWriter.write(message, item, queueSeqNum, statement);
        securityWriter.write(security, item, queueSeqNum, statement);
        statement.setString(DBFieldEnum.venue.getColumnIndex(), String.valueOf(message.getVenueCode()));
        statement.setLong(DBFieldEnum.best_bid.getColumnIndex(), message.getBidPrice());
        statement.setLong(DBFieldEnum.best_offer.getColumnIndex(), message.getOfferPrice());

    }

    @Override
    public void onMatchQuote(MatchQuoteEvent msg) {
        MatchQuoteByteBufferMessage event = new MatchQuoteByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchQuoteEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
