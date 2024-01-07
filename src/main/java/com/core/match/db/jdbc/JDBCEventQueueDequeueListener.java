package com.core.match.db.jdbc;

public interface JDBCEventQueueDequeueListener {

    public void onDequeue(JDBCEventQueueItem item, long queueSeqNum);
}
