package com.core.match.db.jdbc.msgs;

import com.core.connector.Connector;
import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.msgs.MatchCommonEvent;
import com.core.match.msgs.MatchConstants;
import com.core.match.util.MessageUtils;
import com.core.util.TimeUtils;
import com.core.util.TradeDateUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Created by jgreco on 12/28/14.
 */
public class CommonEventWriter implements FieldWriter<MatchCommonEvent>
{
	protected final Connector connector;
	protected final JDBCFieldsService service;
	private final TradeDateUtils tradeDateUtils = new TradeDateUtils(MessageUtils.zoneID(), MatchConstants.SESSION_ROLLOVER_TIME);
	private Timestamp startTimeStamp;

	public CommonEventWriter( Connector connector, JDBCFieldsService service)
	{
		this.connector = connector;
		this.service = service;
	}

	@Override
	public void write(MatchCommonEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement preparedStatement) throws SQLException
	{
		String session = connector.getSession();

		// TODO: Need to account for previous day's session

		JDBCFieldsService.CommonContributor contributor = service.getContributor(message.getContributorID());

		if (queueSeqNum == 1)
		{
			startTimeStamp=new Timestamp(message.getTimestamp()/TimeUtils.NANOS_PER_MILLI);
		}

		LocalDateTime tstamp = message.getTimestampAsTime();
		Timestamp timestamp=new Timestamp(message.getTimestamp()/TimeUtils.NANOS_PER_MILLI);

		timestamp.setNanos(tstamp.getNano());
		Date tradeDate = new Date(getSessionDate(message.getTimestamp()).getTime());
		preparedStatement.setString(DBFieldEnum.session.getColumnIndex(), session);
		preparedStatement.setTimestamp(DBFieldEnum.core_time.getColumnIndex(), timestamp);
		preparedStatement.setTimestamp(DBFieldEnum.restart_time.getColumnIndex(), startTimeStamp);


		preparedStatement.setLong(DBFieldEnum.nanos.getColumnIndex(), message.getTimestamp());
		preparedStatement.setInt(DBFieldEnum.sequence.getColumnIndex(), (int) queueSeqNum);
		preparedStatement.setString(DBFieldEnum.msg_type.getColumnIndex(), message.getMsgName());
		preparedStatement.setString(DBFieldEnum.contributor.getColumnIndex(), contributor.getName());
		preparedStatement.setInt(DBFieldEnum.contributor_seq.getColumnIndex(), message.getContributorSeq());
		preparedStatement.setDate(DBFieldEnum.trade_date.getColumnIndex(), tradeDate);
	}

    private java.util.Date getSessionDate(long nanos) {
		LocalDate tradeDate = tradeDateUtils.getTradeDate(nanos);
		return Date.from(tradeDate.atStartOfDay().atZone(MessageUtils.zoneID()).toInstant());
    }
}
