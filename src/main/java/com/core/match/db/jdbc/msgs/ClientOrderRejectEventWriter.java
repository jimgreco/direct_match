package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.SQLUtils;
import com.core.match.msgs.MatchClientOrderRejectByteBufferMessage;
import com.core.match.msgs.MatchClientOrderRejectEvent;
import com.core.match.msgs.MatchClientOrderRejectListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class ClientOrderRejectEventWriter implements
        MatchClientOrderRejectListener,
        JDBCEventQueueDequeueListener,
        FieldWriter<MatchClientOrderRejectEvent> {
    private final Log log;
    private final CommonEventWriter commonWriter;
    private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement preparedStatement;

    public ClientOrderRejectEventWriter(Log log,
                                        BatchPreparedStatement preparedStatement,
                                        CommonEventWriter commonWriter,
                                        JDBCEventQueue eventQueue) {
        this.log = log;
        this.commonWriter = commonWriter;
        this.eventQueue = eventQueue;
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void write(MatchClientOrderRejectEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        String security = message.hasSecurity() ? message.getSecurityAsString() : null;
        String trader = message.hasTrader() ? message.getTraderAsString() : null;
        String clordid = message.hasClOrdID() ? SQLUtils.getClOrdIdAsString(message.getClOrdID()) : null;
        String text = message.hasText() ? message.getTextAsString() : null;

        commonWriter.write(message, item, queueSeqNum, statement);

        statement.setString( DBFieldEnum.trader.getColumnIndex(), trader);
        statement.setBoolean(DBFieldEnum.buy.getColumnIndex(), message.getBuy());
        statement.setString(DBFieldEnum.security.getColumnIndex(), security);
        statement.setString(DBFieldEnum.clordid.getColumnIndex(), clordid);
        statement.setString(DBFieldEnum.text.getColumnIndex(), text);

    }

    @Override
    public void onMatchClientOrderReject(MatchClientOrderRejectEvent msg) {
        MatchClientOrderRejectByteBufferMessage event = new MatchClientOrderRejectByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchClientOrderRejectEvent)item.getEvent(), item, queueSeqNum, preparedStatement);
            preparedStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
