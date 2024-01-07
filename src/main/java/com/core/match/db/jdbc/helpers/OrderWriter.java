package com.core.match.db.jdbc.helpers;

import java.sql.SQLException;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.JDBCOrder;
import com.core.match.db.jdbc.msgs.DBFieldEnum;
import com.core.match.services.account.Account;
import com.core.match.services.trader.Trader;
import com.core.util.PriceUtils;

/**
 * Created by jgreco on 12/28/14.
 */
public class OrderWriter implements FieldWriter<JDBCOrder> {
    private final JDBCFieldsService service;
    private final SecurityWriter securityWriter;

    public OrderWriter(JDBCFieldsService service,
                       SecurityWriter securityWriter) {
        this.service = service;
        this.securityWriter = securityWriter;
    }

    @Override
    public void write(JDBCOrder order, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        if (order != null) {

            Trader trader = service.getTrader(order.getTraderID());

            Account account=null;
            if( trader!=null){
                account= service.getAccount(trader.getAccountID());
                //TODO: saves the fat finger fields for each security for the trader
                statement.setString(DBFieldEnum.trader.getColumnIndex(), trader.getName());
                //TODO: max display qty
            }
            JDBCFieldsService.JDBCSecurity security = service.getSecurity(order.getSecurityID());

            if(security!=null) {
                statement.setDate(DBFieldEnum.maturity_date.getColumnIndex(), security.getMaturityDate());
                statement.setString(DBFieldEnum.cusip.getColumnIndex(), security.getCusip());
                securityWriter.write(security, item, queueSeqNum, statement);
            }

            if (item.getQuote() != null) {
                statement.setString(DBFieldEnum.venue.getColumnIndex(), String.valueOf(item.getQuote().getVenue()));
                statement.setLong(DBFieldEnum.best_bid.getColumnIndex(), item.getQuote().getBidPrice());
                statement.setLong(DBFieldEnum.best_offer.getColumnIndex(), item.getQuote().getOfferPrice());
            }
            statement.setInt(DBFieldEnum.order_id.getColumnIndex(), order.getID());
            statement.setBoolean(DBFieldEnum.buy.getColumnIndex(), order.isBuy());
            statement.setInt(DBFieldEnum.qty.getColumnIndex(), order.getQty());

            statement.setLong(DBFieldEnum.price.getColumnIndex(), order.getPrice());
            statement.setString(DBFieldEnum.price32.getColumnIndex(), PriceUtils.to32ndPrice(order.getPrice(), 9));
            statement.setString(DBFieldEnum.clordid.getColumnIndex(), order.getClOrdID());
            statement.setInt(DBFieldEnum.cum_qty.getColumnIndex(), order.getCumQty());

            if(account!=null){
                statement.setString(DBFieldEnum.account.getColumnIndex(), account.getName());
            }

        }
	}
}
