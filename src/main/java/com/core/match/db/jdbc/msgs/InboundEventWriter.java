package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.msgs.MatchInboundByteBufferMessage;
import com.core.match.msgs.MatchInboundEvent;
import com.core.match.msgs.MatchInboundListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class InboundEventWriter implements FieldWriter<MatchInboundEvent>, MatchInboundListener, JDBCEventQueueDequeueListener
{
	private final Log log;
	private final CommonEventWriter commonWriter;
	private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement runningStatement;

	public InboundEventWriter(Log log, BatchPreparedStatement preparedStatement, CommonEventWriter commonWriter, JDBCEventQueue eventQueue)
	{
		this.log = log;
		this.commonWriter = commonWriter;
		this.eventQueue = eventQueue;
		this.runningStatement = preparedStatement;
	}

	@Override
	public void write(MatchInboundEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException
	{
		commonWriter.write(message, item, queueSeqNum, statement);
		statement.setString(DBFieldEnum.fix_msg_type.getColumnIndex(), new String(new char[] { message.getFixMsgType() }));
	}

	@Override
	public void onMatchInbound(MatchInboundEvent msg)
	{
        MatchInboundByteBufferMessage event = new MatchInboundByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
		try
		{
			write((MatchInboundEvent)item.getEvent(), item, queueSeqNum, runningStatement);
			runningStatement.addBatch();
		}
		catch (SQLException e)
		{
			log.error(log.log().add(e));
		}
	}
}
