package com.core.match.db.jdbc;

import java.sql.SQLException;

import com.core.util.TimeUtils;
import com.core.util.log.Log;
import com.gs.collections.impl.map.mutable.primitive.CharObjectHashMap;

public class JDBCEventQueueConsumer implements Runnable {

    private final JDBCEventQueue eventQueue;
    private final CharObjectHashMap<JDBCEventQueueDequeueListener> writerMap;
    private final BatchPreparedStatement batchPreparedStatement;
    private final Log log;
    private final int batchTimerMillis;

    private JDBCEventQueueItem item;
    private int queueSeqNum;
    private long now;
    private long nextBatchTime;

    public JDBCEventQueueConsumer(
            JDBCEventQueue eventQueue,
            CharObjectHashMap<JDBCEventQueueDequeueListener> writerMap,
            BatchPreparedStatement batchPreparedStatement,
            Log log,
            int batchTimerMillis) {
        this.eventQueue = eventQueue;
        this.writerMap = writerMap;
        this.batchPreparedStatement = batchPreparedStatement;
        this.log = log;
        this.batchTimerMillis = batchTimerMillis;
    }

    @Override
    public void run() {
        nextBatchTime = getNextBatchTime();
        while (true) {
            item = eventQueue.poll();
            if (item != null) {
                // if there is an event in the queue then process it, and reschedule the batch timer if a batch is committed
                writerMap.get(item.getEvent().getMsgType()).onDequeue(item, ++queueSeqNum);
                if (batchPreparedStatement.getBatchSize()==0) {
                    nextBatchTime = getNextBatchTime();
                }
            } else {
                now = System.nanoTime();
                if (now>=nextBatchTime) {
                    // no batch was sent within the time allotted, so send the batch if there is one, then reset the timer
                    if (batchPreparedStatement.getBatchSize()>0) {
                        log.debug(log.log().add("JDBC commit on timer"));
                        try {
                            batchPreparedStatement.executeAndCommitToDB();
                        } catch (SQLException e) {
                            log.error(log.log().add("Error Writing to DB: ").add(e.getMessage()));
                        }
                    }
                    nextBatchTime = getNextBatchTime();
                } else {
                    // event queue is empty and the batch timer has not been reached yet, so wait
                    try {
                        Thread.sleep(Math.min(1000, nextBatchTime-now));
                    } catch (InterruptedException e) {
                        log.error(log.log().add("JDBC event queue interrupted: ").add(e.getMessage()));
                        break;
                    }
                }
            }
        }
    }
    
    private long getNextBatchTime() {
        return System.nanoTime() + batchTimerMillis * TimeUtils.NANOS_PER_MILLI;
    }
}
