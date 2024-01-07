package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.helpers.OrderWriter;
import com.core.match.msgs.MatchFillByteBufferMessage;
import com.core.match.msgs.MatchFillEvent;
import com.core.match.msgs.MatchFillListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class FillEventWriter implements
        MatchFillListener,
        JDBCEventQueueDequeueListener,
        FieldWriter<MatchFillEvent> {
    private final Log log;
    private final JDBCFieldsService service;
    private final CommonEventWriter commonWriter;
    private final OrderWriter orderWriter;
    private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement runningStatement;

    public FillEventWriter(Log log,
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
    public void write(MatchFillEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        commonWriter.write(message, item, queueSeqNum, statement);
        orderWriter.write(item.getOrder(), item, queueSeqNum, statement);
        statement.setInt(DBFieldEnum.match_id.getColumnIndex(), message.getMatchID());
        statement.setInt(DBFieldEnum.fill_qty.getColumnIndex(), message.getQty());
        statement.setLong(DBFieldEnum.fill_price.getColumnIndex(), message.getPrice());
        statement.setBoolean(DBFieldEnum.last_fill.getColumnIndex(), message.getLastFill());

    }

    @Override
    public void onMatchFill(MatchFillEvent msg) {
        MatchFillByteBufferMessage event = new MatchFillByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, service.getOrder(msg.getOrderID()));
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchFillEvent)item.getEvent(), item, queueSeqNum, runningStatement);
            runningStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
