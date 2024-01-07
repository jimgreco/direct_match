package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.SQLUtils;
import com.core.match.db.jdbc.helpers.SecurityWriter;
import com.core.match.msgs.MatchOrderRejectByteBufferMessage;
import com.core.match.msgs.MatchOrderRejectEvent;
import com.core.match.msgs.MatchOrderRejectListener;
import com.core.match.services.account.Account;
import com.core.match.services.trader.Trader;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class OrderRejectEventWriter implements
		MatchOrderRejectListener,
		JDBCEventQueueDequeueListener,
		FieldWriter<MatchOrderRejectEvent>
{
	private final Log log;
	private final JDBCFieldsService service;
	private final CommonEventWriter commonWriter;
	private final SecurityWriter securityWriter;
	private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement runningStatement;

	public OrderRejectEventWriter(Log log, BatchPreparedStatement preparedStatement,
			JDBCFieldsService service,
			CommonEventWriter commonWriter,
			SecurityWriter securityWriter,
			JDBCEventQueue eventQueue)
	{
		this.log = log;
		this.service = service;
		this.commonWriter = commonWriter;
		this.securityWriter = securityWriter;
		this.eventQueue = eventQueue;
		this.runningStatement = preparedStatement;
	}

	@Override
    public void write(MatchOrderRejectEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        Trader trader = service.getTrader( message.getTraderID() );
        if( trader != null )
        {
	        Account account = service.getAccount(trader.getID());
	        statement.setString(DBFieldEnum.account.getColumnIndex(), account != null ? account.getName() : null);
	        statement.setString(DBFieldEnum.trader.getColumnIndex(), trader.getName());
        }
        JDBCFieldsService.JDBCSecurity security = service.getSecurity(message.getSecurityID());
        String clordid = message.hasClOrdID() ? SQLUtils.getClOrdIdAsString(message.getClOrdID()) : null;
        String text = message.hasText() ? message.getTextAsString() : null;

        commonWriter.write(message, item, queueSeqNum, statement);
        securityWriter.write(security, item, queueSeqNum, statement);

        statement.setBoolean(DBFieldEnum.buy.getColumnIndex(), message.getBuy());
        statement.setString(DBFieldEnum.security.getColumnIndex(), security != null ? security.getName() : null);
        statement.setString(DBFieldEnum.clordid.getColumnIndex(), clordid);
        statement.setString(DBFieldEnum.text.getColumnIndex(), text);

    }

	@Override
	public void onMatchOrderReject(MatchOrderRejectEvent msg)
	{
        MatchOrderRejectByteBufferMessage event = new MatchOrderRejectByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, null);
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
		try
		{
			write((MatchOrderRejectEvent)item.getEvent(), item, queueSeqNum, runningStatement);
			runningStatement.addBatch();
		}
		catch (SQLException e)
		{
			log.error(log.log().add(e));
		}
	}
}
