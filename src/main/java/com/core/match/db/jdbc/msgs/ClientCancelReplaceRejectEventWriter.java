package com.core.match.db.jdbc.msgs;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCEventQueueDequeueListener;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueue;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.SQLUtils;
import com.core.match.msgs.MatchClientCancelReplaceRejectByteBufferMessage;
import com.core.match.msgs.MatchClientCancelReplaceRejectEvent;
import com.core.match.msgs.MatchClientCancelReplaceRejectListener;
import com.core.match.services.account.Account;
import com.core.match.services.trader.Trader;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;

import java.sql.SQLException;
import java.sql.Types;

/**
 * Created by jgreco on 12/28/14.
 */
public class ClientCancelReplaceRejectEventWriter implements
        MatchClientCancelReplaceRejectListener,
        JDBCEventQueueDequeueListener,
        FieldWriter<MatchClientCancelReplaceRejectEvent> {
    private final Log log;
    private final JDBCFieldsService service;
    private final CommonEventWriter commonWriter;
    private final JDBCEventQueue eventQueue;
	private final BatchPreparedStatement preparedStatement;

    public ClientCancelReplaceRejectEventWriter(Log log,
                                                BatchPreparedStatement preparedStatement,
                                                JDBCFieldsService service,
                                                CommonEventWriter commonWriter,
                                                JDBCEventQueue eventQueue) {
        this.log = log;
        this.service = service;
        this.commonWriter = commonWriter;
        this.eventQueue = eventQueue;
        this.preparedStatement = preparedStatement;
    }

    @Override
    public void write(MatchClientCancelReplaceRejectEvent message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {

        Trader trader = null;
        if (item.getOrder() != null) {
            trader = service.getTrader( item.getOrder().getTraderID() );
            Account account = service.getAccount(trader.getAccountID());
            String accountName = account != null  ? account.getName() : null;
            String traderName = trader != null ? trader.getName() : null;
            statement.setString(DBFieldEnum.account.getColumnIndex(), accountName);
            statement.setString(DBFieldEnum.trader.getColumnIndex(), traderName);
        }


        String clordid = message.hasClOrdID() ? SQLUtils.getClOrdIdAsString(message.getClOrdID()) : null;
        String origClOrdID = message.hasOrigClOrdID() ? SQLUtils.getClOrdIdAsString(message.getOrigClOrdID()) : null;
        String text = message.hasText() ? message.getTextAsString() : null;


        if (message.getOrderID() > 0) {
            statement.setInt(DBFieldEnum.order_id.getColumnIndex(), message.getOrderID());
        }
        else {
            statement.setNull(DBFieldEnum.order_id.getColumnIndex(), Types.INTEGER);
        }
        char reason=message.getReason();

        statement.setString(DBFieldEnum.clordid.getColumnIndex(), clordid);
        statement.setString(DBFieldEnum.orig_clordid.getColumnIndex(), origClOrdID);
        statement.setString(DBFieldEnum.text.getColumnIndex(), text);
        statement.setBoolean(DBFieldEnum.is_replaced.getColumnIndex(), message.getIsReplace());
        statement.setString(DBFieldEnum.reject_reason.getColumnIndex(), String.valueOf(reason));
        commonWriter.write(message, item, queueSeqNum, statement);


    }

    @Override
    public void onMatchClientCancelReplaceReject(MatchClientCancelReplaceRejectEvent msg) {
        MatchClientCancelReplaceRejectByteBufferMessage event = new MatchClientCancelReplaceRejectByteBufferMessage();
        event.wrapEvent(BinaryUtils.createCopy(msg.getRawBuffer()));
        eventQueue.add(event, service.getOrder(msg.getOrderID()));
    }

    @Override
    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum) {
        try {
            write((MatchClientCancelReplaceRejectEvent)item.getEvent(), item, queueSeqNum, preparedStatement);
            preparedStatement.addBatch();
        } catch (SQLException e) {
            log.error(log.log().add(e));
        }
    }
}
