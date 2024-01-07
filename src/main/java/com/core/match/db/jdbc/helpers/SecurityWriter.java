package com.core.match.db.jdbc.helpers;

import com.core.match.db.jdbc.BatchPreparedStatement;
import com.core.match.db.jdbc.JDBCEventQueueItem;
import com.core.match.db.jdbc.FieldWriter;
import com.core.match.db.jdbc.JDBCFieldsService;
import com.core.match.db.jdbc.msgs.DBFieldEnum;
import com.core.util.PriceUtils;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/28/14.
 */
public class SecurityWriter implements FieldWriter<JDBCFieldsService.JDBCSecurity> {
    @Override
    public void write(JDBCFieldsService.JDBCSecurity security, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException {
        if (security != null) {
            statement.setString(DBFieldEnum.security.getColumnIndex(), security.getName());
            statement.setDate(DBFieldEnum.maturity_date.getColumnIndex(), security.getMaturityDate());
            statement.setLong(DBFieldEnum.coupon.getColumnIndex(), security.getCoupon());
            statement.setLong(DBFieldEnum.bid_price.getColumnIndex(), security.getBidPrice());
            statement.setLong(DBFieldEnum.offer_price.getColumnIndex(), security.getOfferPrice());
            statement.setString(DBFieldEnum.bid_price32.getColumnIndex(), PriceUtils.to32ndPrice(security.getBidPrice(), 9));
            statement.setString(DBFieldEnum.offer_price32.getColumnIndex(), PriceUtils.to32ndPrice(security.getOfferPrice(),9));
        }
    }
}
