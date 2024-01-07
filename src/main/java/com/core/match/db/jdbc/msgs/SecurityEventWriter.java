package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.SQLUtils;
import com.core.match.db.jdbc.helpers.SecurityWriter;
import com.core.match.msgs.MatchSecurityByteBufferMessage;
import com.core.match.msgs.MatchSecurityEvent;
import com.core.match.msgs.MatchSecurityListener;
import com.core.match.services.security.SecurityType;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class SecurityEventWriter implements FieldWriter<MatchSecurityEvent>, MatchSecurityListener, JDBCEventQueueDequeueListener
{
	private final JDBCFieldsService service;
	private final CommonEventWriter commonWriter;
	private final SecurityWriter securityWriter;
	private final JDBCEventQueue eventQueue;

	private final BatchPreparedStatement runningStatement;

	private final Log log;

	public SecurityEventWriter(Log log, BatchPreparedStatement preparedStatement, JDBCFieldsService service, CommonEventWriter commonWriter, SecurityWriter securityWriter, JDBCEventQueue eventQueue)
	{
		this.log = log;
		this.service = service;
		this.commonWriter = commonWriter;
		this.securityWriter = securityWriter;
		this.eventQueue = eventQueue;
		this.runningStatement = preparedStatement;
	}

	@Override
	public void onMatchSecurity(MatchSecurityEvent msg)
	{
	    MatchSecurityByteBufferMessage event = new MatchSecurityByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
		try
		{
			write((MatchSecurityEvent)item.getEvent(), item, queueSeqNum, runningStatement);
			runningStatement.addBatch();
		}
		catch (SQLException e)
		{
			log.error(log.log().add(e));
		}
	}

	@Override
	public void write(MatchSecurityEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException
	{
		// common
		commonWriter.write(message, item, queueSeqNum, statement);

		// security
		JDBCFieldsService.JDBCSecurity security = service.getSecurity(message.getSecurityID());
		securityWriter.write(security, item, queueSeqNum, statement);

		statement.setLong(DBFieldEnum.tick_size.getColumnIndex(), message.getTickSize());
		statement.setInt(DBFieldEnum.lot_size.getColumnIndex(),message.getLotSize());
        statement.setString(DBFieldEnum.security_type.getColumnIndex(),String.valueOf(message.getType()));

        // id
        if(message.getType()== SecurityType.BOND.getValue()){
            statement.setString(DBFieldEnum.cusip.getColumnIndex(), message.getCUSIPAsString());
            statement.setDate(DBFieldEnum.issue_date.getColumnIndex(), SQLUtils.localdateToDate(message.getIssueDateAsDate()));
        }
	}
}
