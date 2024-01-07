package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.msgs.MatchSystemEventByteBufferMessage;
import com.core.match.msgs.MatchSystemEventEvent;
import com.core.match.msgs.MatchSystemEventListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by hli on 10/5/15.
 */
public class SystemEventWriter implements
        FieldWriter<MatchSystemEventEvent>,
        JDBCEventQueueDequeueListener,
        MatchSystemEventListener {
    private final Log log;
    private final CommonEventWriter commonWriter;
    private final JDBCEventQueue eventQueue;
    private final BatchPreparedStatement runningStatement;

    public SystemEventWriter(Log log,
                             BatchPreparedStatement preparedStatement,
                             CommonEventWriter commonWriter,
                             JDBCEventQueue eventQueue) {
        this.commonWriter=commonWriter;
        this.eventQueue=eventQueue;
        this.log=log;
        this.runningStatement= preparedStatement;
    }

    @Override
    public void write(MatchSystemEventEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        commonWriter.write(message, item, queueSeqNum, statement);
        statement.setString(DBFieldEnum.text.getColumnIndex(),String.valueOf(message.getEventType()));

    }

    @Override
    public void onMatchSystemEvent(MatchSystemEventEvent msg) {
        MatchSystemEventByteBufferMessage event = new MatchSystemEventByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchSystemEventEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }

    }
}
