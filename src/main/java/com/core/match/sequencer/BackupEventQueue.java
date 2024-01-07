package com.core.match.sequencer;

import com.core.connector.mold.LinkableByteBuffer;
import com.core.match.msgs.*;
import com.core.sequencer.BackupQueue;
import com.core.sequencer.BackupQueueListener;
import com.core.util.datastructures.DMQueue;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * Created by hli on 5/11/16.
 */
public class BackupEventQueue implements BackupQueue, BackupEventQueueController {
    private final DMQueue<LinkableByteBuffer> receivedQueue;
    private final ObjectPool<LinkableByteBuffer> bufferObjectPool;
    private BackupQueueListener backupQueueListener;

    private final Log logger;
    private final MatchMessages matchMessages;

    public BackupEventQueue(Log logger, MatchMessages matchMessages) throws IOException {
        this.logger = logger;
        this.matchMessages=matchMessages;
        receivedQueue = new DMQueue<>();
        bufferObjectPool=new ObjectPool<>(logger,"backupQueue",LinkableByteBuffer::new,1000);
    }

    @Override
    public void addBackupQueueListener(BackupQueueListener listener){
        this.backupQueueListener=listener;
    }


    public void add(ByteBuffer inputToCopy) {
        LinkableByteBuffer linkedByteBuffer = bufferObjectPool.create();
        linkedByteBuffer.copy(inputToCopy);
        receivedQueue.add(linkedByteBuffer);
    }

    @Override
    public void verifyOrderEvent(MatchOrderEvent receivedEvent) {

        LinkableByteBuffer backupMessage=receivedQueue.remove();
        MatchOrderEvent backupGeneratedMsg= matchMessages.getMatchOrderEvent(backupMessage.getValue());
        if(backupGeneratedMsg.getOrderID()!=receivedEvent.getOrderID() ||
                backupGeneratedMsg.getExternalOrderID()!=receivedEvent.getExternalOrderID() ||
                backupGeneratedMsg.getInBook() != receivedEvent.getInBook()
                ){
            throw new  IllegalStateException("Inconsistent Backup on Order Event");
        }
        bufferObjectPool.delete(backupMessage);
    }

    @Override
    public void verifyReplaceEvent(MatchReplaceEvent receivedEvent) {
        LinkableByteBuffer backupMessage=receivedQueue.remove();

        MatchReplaceEvent backupGeneratedMsg= matchMessages.getMatchReplaceEvent(backupMessage.getValue());
        if(     backupGeneratedMsg.getQty()!=receivedEvent.getQty() ||
                backupGeneratedMsg.getExternalOrderID()!=receivedEvent.getExternalOrderID() ||
                backupGeneratedMsg.getInBook() != receivedEvent.getInBook()){
            throw new  IllegalStateException("Inconsistent backup on Replace Event");
        }
        bufferObjectPool.delete(backupMessage);
    }

    @Override
    public void verifyCancelEvent(MatchCancelEvent receivedEvent) {
        LinkableByteBuffer backupMessage=receivedQueue.remove();
        MatchCancelEvent backupGeneratedMsg= matchMessages.getMatchCancelEvent(backupMessage.getValue());
        if( backupGeneratedMsg.getOrderID()!=receivedEvent.getOrderID() ){
            throw new  IllegalStateException("Inconsistent backup on Cancel Event");
        }
        bufferObjectPool.delete(backupMessage);
    }

    @Override
    public void verifyFillEvent(MatchFillEvent receivedEvent) {
        LinkableByteBuffer backupMessage=receivedQueue.remove();

        MatchFillEvent backupGeneratedMsg= matchMessages.getMatchFillEvent(backupMessage.getValue());
        if(backupGeneratedMsg.getOrderID()!=(receivedEvent.getOrderID()) ||
                backupGeneratedMsg.getLastFill()!=receivedEvent.getLastFill() ||
                backupGeneratedMsg.getOrderID()!=receivedEvent.getOrderID() ||
                backupGeneratedMsg.getPassive()!=(receivedEvent.getPassive()) ||
                backupGeneratedMsg.getMatchID()!=(receivedEvent.getMatchID()) ||
                backupGeneratedMsg.getPrice()!=(receivedEvent.getPrice()) ||
                backupGeneratedMsg.getQty()!=(receivedEvent.getQty()) ||
                backupGeneratedMsg.getInBook() != receivedEvent.getInBook()){
            throw new  IllegalStateException("Inconsistent backup on fill event");
        }
        bufferObjectPool.delete(backupMessage);
    }

    @Override
    public void sendRemaining() {
        while(receivedQueue.size() >0){
            logger.error(logger.log().add("BACKUP SEQUENCER HAS MESSAGES...SENDING TO LISTENER"));
            LinkableByteBuffer curr=receivedQueue.remove();
            backupQueueListener.sendRemainingQueueMessages(curr.getValue());

        }
    }
}
