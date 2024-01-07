package com.core.match.db.jdbc.msgs;

import java.sql.Types;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hli on 9/30/15.
 */
@SuppressWarnings("boxing")
public enum DBFieldEnum {

    session(1),
    core_time(2) ,
    nanos(3)  ,
    sequence(4)  ,
    msg_type(5) ,
    contributor(6) ,
    contributor_seq(7)  ,
    order_id(8),
    account(9),
    trader(10) ,
    security(11),
    maturity_date(12),
    coupon(13),
    buy(14),
    qty(15),
    price(16),
    clordid(17),
    orig_clordid(18),
    match_id(19),
    fill_qty(20),
    fill_price(21),
    old_qty(22),
    old_price(23),
    bid_price(24),
    offer_price(25) ,
    text(26) ,
    cusip(27) ,
    security_type(28),
    source_contributor(29) ,
    cancel_on_disconnect(30),
    net_dv01_limit(31) ,
    fat_finger_dv01_limit(32),
    trade_date(33),
    issue_date(34),
    bloomberg_id(35) ,
    last_fill(36) ,
    max_display_qty(37),
    old_max_display_qty(38),
    cum_qty(39),
    passive(40) ,
    ioc(41),
    display_qty(42),
    is_replaced(43) ,
    reject_reason(44) ,
    fix_msg_type(45),
    tick_size(46),
    lot_size(47),
    price32(48),
    bid_price32(49),
    offer_price32(50),
    venue(51),
    best_bid(52),
    best_offer(53), restart_time(54);



    private final int index;

     DBFieldEnum(final int newIndex) {
        index = newIndex;
    }

    public int getColumnIndex() { return index; }

    public static Map<DBFieldEnum,Integer> getFieldEnumTypeMap () { return fieldEnumTypesMap; }

    private final static Map<DBFieldEnum,Integer> fieldEnumTypesMap;
    static {
        fieldEnumTypesMap=new HashMap<>();
        fieldEnumTypesMap.put(DBFieldEnum.session,Types.DATE);
        fieldEnumTypesMap.put(DBFieldEnum.core_time,Types.TIME);
        fieldEnumTypesMap.put(DBFieldEnum.nanos,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.sequence,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.msg_type,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.contributor,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.contributor_seq,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.order_id,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.account,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.trader,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.security,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.maturity_date,Types.DATE);
        fieldEnumTypesMap.put(DBFieldEnum.coupon,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.buy,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.price,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.clordid,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.orig_clordid,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.match_id,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.fill_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.fill_price,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.old_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.old_price,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.bid_price,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.offer_price,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.text,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.cusip,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.security_type,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.source_contributor,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.cancel_on_disconnect,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.net_dv01_limit,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.fat_finger_dv01_limit,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.trade_date,Types.DATE);
        fieldEnumTypesMap.put(DBFieldEnum.issue_date,Types.DATE);
        fieldEnumTypesMap.put(DBFieldEnum.bloomberg_id,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.last_fill,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.max_display_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.old_max_display_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.cum_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.passive,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.ioc,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.display_qty,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.is_replaced,Types.BIT);
        fieldEnumTypesMap.put(DBFieldEnum.reject_reason,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.fix_msg_type,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.tick_size,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.lot_size,Types.INTEGER);
        fieldEnumTypesMap.put(DBFieldEnum.price32,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.bid_price32,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.offer_price32,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.venue,Types.VARCHAR);
        fieldEnumTypesMap.put(DBFieldEnum.best_bid,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.best_offer,Types.BIGINT);
        fieldEnumTypesMap.put(DBFieldEnum.restart_time,Types.TIME);

    }

}
