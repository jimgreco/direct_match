package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.msgs.MatchContributorByteBufferMessage;
import com.core.match.msgs.MatchContributorEvent;
import com.core.match.msgs.MatchContributorListener;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class ContributorEventWriter implements FieldWriter<MatchContributorEvent>, MatchContributorListener, JDBCEventQueueDequeueListener
{
	private final Log log;
	private final CommonEventWriter commonWriter;
	private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement preparedStatement;

	public ContributorEventWriter(Log log, BatchPreparedStatement preparedStatement, CommonEventWriter commonWriter, JDBCEventQueue eventQueue)
	{
		this.log = log;
		this.commonWriter = commonWriter;
		this.eventQueue = eventQueue;
		this.preparedStatement = preparedStatement;
	}

	@Override
	public void onMatchContributor(MatchContributorEvent msg)
	{
	    MatchContributorByteBufferMessage event = new MatchContributorByteBufferMessage();
	    event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
	    eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
		try
		{
			write((MatchContributorEvent)item.getEvent(), item, queueSeqNum, preparedStatement);

			preparedStatement.addBatch();
		}
		catch (SQLException e)
		{
			log.error(log.log().add(e));
		}
	}

	@Override
	public void write(MatchContributorEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException
	{

		commonWriter.write(message, item, queueSeqNum, statement);
		statement.setString(DBFieldEnum.source_contributor.getColumnIndex(), message.getNameAsString());
		statement.setString(DBFieldEnum.text.getColumnIndex(), message.getNameAsString());
		statement.setBoolean(DBFieldEnum.cancel_on_disconnect.getColumnIndex(), message.getCancelOnDisconnect());

	}
}
