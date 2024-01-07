package com.core.match.db.jdbc;

import java.sql.SQLException;

/**
 * Created by jgreco on 12/27/14.
 */
public interface FieldWriter<T> {
    void write(T message, JDBCEventQueueItem item, long queueSeqNum, BatchPreparedStatement statement) throws SQLException;
}
