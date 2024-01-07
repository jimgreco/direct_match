package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.helpers.OrderWriter;
import com.core.match.msgs.MatchCancelReplaceRejectByteBufferMessage;
import com.core.match.msgs.MatchCancelReplaceRejectEvent;
import com.core.match.msgs.MatchCancelReplaceRejectListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class CancelReplaceRejectEventWriter implements
        MatchCancelReplaceRejectListener,
        JDBCEventQueueDequeueListener,
        FieldWriter<MatchCancelReplaceRejectEvent> {
    private final Log log;
    private final JDBCFieldsService service;
    private final CommonEventWriter commonWriter;
    private final OrderWriter orderWriter;
    private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement preparedStatement;

    public CancelReplaceRejectEventWriter(Log log,
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
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void write(MatchCancelReplaceRejectEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {

        String origClordid = message.hasOrigClOrdID() ? message.getOrigClOrdIDAsString() : null;
        String text = message.hasText() ? message.getTextAsString() : null;
        char reason=message.getReason();

       commonWriter.write(message, item, queueSeqNum, statement);
       orderWriter.write(item.getOrder(), item, queueSeqNum, statement);

        statement.setString(DBFieldEnum.orig_clordid.getColumnIndex(), origClordid);
        statement.setString(DBFieldEnum.text.getColumnIndex(), text);
        statement.setBoolean(DBFieldEnum.is_replaced.getColumnIndex(),message.getIsReplace());
        statement.setString(DBFieldEnum.reject_reason.getColumnIndex(), String.valueOf(reason));

    }

    @Override
    public void onMatchCancelReplaceReject(MatchCancelReplaceRejectEvent msg) {
        MatchCancelReplaceRejectByteBufferMessage event = new MatchCancelReplaceRejectByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, service.getOrder(msg.getOrderID()));
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchCancelReplaceRejectEvent)item.getEvent(), item, queueSeqNum, preparedStatement);
            preparedStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
