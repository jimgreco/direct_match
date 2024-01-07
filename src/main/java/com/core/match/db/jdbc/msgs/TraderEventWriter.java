package com.core.match.db.jdbc.msgs;

import java.sql.SQLException;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.msgs.MatchTraderByteBufferMessage;
import com.core.match.msgs.MatchTraderEvent;
import com.core.match.msgs.MatchTraderListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

/**
 * Created by hli on 10/5/15.
 */
public class TraderEventWriter implements
        FieldWriter<MatchTraderEvent>,
        JDBCEventQueueDequeueListener,
        MatchTraderListener {
    private final Log log;
    private final CommonEventWriter commonWriter;
    private final JDBCEventQueue eventQueue;
    private final BatchPreparedStatement runningStatement;

    public TraderEventWriter(Log log,
                             BatchPreparedStatement preparedStatement,
                             CommonEventWriter commonWriter,
                             JDBCEventQueue eventQueue) {
        this.commonWriter=commonWriter;
        this.eventQueue=eventQueue;
        this.log=log;
        this.runningStatement= preparedStatement;
    }

    @Override
    public void write(MatchTraderEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        commonWriter.write(message, item, queueSeqNum, statement);
        statement.setString(DBFieldEnum.trader.getColumnIndex(), message.getNameAsString());
//TODO: add fat finger qty limit for each security. need to update db
    }

    @Override
    public void onMatchTrader(MatchTraderEvent msg) {
        MatchTraderByteBufferMessage event = new MatchTraderByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchTraderEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
