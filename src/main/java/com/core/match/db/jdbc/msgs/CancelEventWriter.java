package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.helpers.OrderWriter;
import com.core.match.msgs.MatchCancelByteBufferMessage;
import com.core.match.msgs.MatchCancelEvent;
import com.core.match.msgs.MatchCancelListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class CancelEventWriter implements
        MatchCancelListener,
        JDBCEventQueueDequeueListener,
        FieldWriter<MatchCancelEvent> {
    private final Log log;
    private final JDBCFieldsService service;
    private final CommonEventWriter commonWriter;
    private final OrderWriter orderWriter;
    private final JDBCEventQueue eventQueue;
    private final BatchPreparedStatement runningStatement;

    public CancelEventWriter(Log log,
                             BatchPreparedStatement preparedStatement,
                             JDBCFieldsService service,
                             CommonEventWriter commonWriter,
                             OrderWriter orderWriter,
                             JDBCEventQueue eventQueue) {
        this.log = log;
        this.service = service;
        this.commonWriter = commonWriter;
        this.orderWriter = orderWriter;
        this.eventQueue = eventQueue;
        this.runningStatement = preparedStatement;
    }

    @Override
    public void write(MatchCancelEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {

        commonWriter.write(message, item, queueSeqNum, statement);
        orderWriter.write(item.getOrder(), item, queueSeqNum, statement);

        if (item.getOrder() == null) {
        	log.error(log.log().add("WTF. OrderId=").add(message.getOrderID()).add(" does not exist"));
        }
        else
        {
        	statement.setString(DBFieldEnum.orig_clordid.getColumnIndex(), item.getOrder().getOrigClOrdId());
        }
    }

    @Override
    public void onMatchCancel(MatchCancelEvent msg) {
        MatchCancelByteBufferMessage event = new MatchCancelByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, service.getOrder(msg.getOrderID()));
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchCancelEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
