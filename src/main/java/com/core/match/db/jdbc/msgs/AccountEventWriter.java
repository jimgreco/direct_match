package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.msgs.MatchAccountByteBufferMessage;
import com.core.match.msgs.MatchAccountEvent;
import com.core.match.msgs.MatchAccountListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class AccountEventWriter implements
        FieldWriter<MatchAccountEvent>,
        JDBCEventQueueDequeueListener,
        MatchAccountListener {
    private final Log log;
    private final CommonEventWriter commonWriter;
    private final JDBCEventQueue eventQueue;
    private final BatchPreparedStatement runningStatement;

    public AccountEventWriter(Log log,
                              BatchPreparedStatement preparedStatement,
                              CommonEventWriter commonWriter,
                              JDBCEventQueue eventQueue) {
        this.log = log;
        this.commonWriter = commonWriter;
        this.eventQueue = eventQueue;
        this.runningStatement = preparedStatement;
    }

    @Override
    public void onMatchAccount(MatchAccountEvent msg) {
        MatchAccountByteBufferMessage event = new MatchAccountByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchAccountEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }

    @Override
    public void write(MatchAccountEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        commonWriter.write(message, item, queueSeqNum, statement);
        statement.setString(DBFieldEnum.account.getColumnIndex(), message.getNameAsString());
        statement.setInt(DBFieldEnum.net_dv01_limit.getColumnIndex(), message.getNetDV01Limit());

    }
}
