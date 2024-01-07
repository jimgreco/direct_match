package com.core.match.sequencer;

import com.core.sequencer.BackupQueueListener;
import com.core.util.NullLog;
import com.core.connector.mold.Mold64UDPPacket;
import com.core.match.msgs.*;
import com.core.util.BinaryUtils;
import com.core.util.log.Log;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Created by hli on 5/12/16.
 */
public class BackupEventQueueTest {

    private BackupEventQueue target;
    private Log log= new NullLog();
    private MatchMessages messages= new MatchTestMessages();
    private  StubBackupQueueListener  stubListener= new StubBackupQueueListener();


    @Before
    public void setup() throws IOException {
        target=new BackupEventQueue(log,messages);
        target.addBackupQueueListener(stubListener);
    }

    private ByteBuffer buildTestMessage(String input){
        ByteBuffer testMessage=ByteBuffer.allocate(input.length());


        testMessage.clear();
        BinaryUtils.copy(testMessage, input);
        testMessage.limit(input.length());
        testMessage.flip();
        return testMessage;
    }

    @Test
    public void add_anyMessage_addToBackupQueue(){
        //Arrange
        ByteBuffer testPayload=buildTestMessage("lalala");

        //Act
        target.add(testPayload);

        //Assert
        target.sendRemaining();
        Assert.assertEquals("lalala",stubListener.outputQueue.remove());
        Assert.assertEquals(0,stubListener.outputQueue.size());

    }
    @Test
    public void add_emptyMessage_addToBackupQueueAndDidNotDie(){
        //Arrange
        ByteBuffer testPayload=buildTestMessage("");

        //Act
        target.add(testPayload);

        //Assert
        target.sendRemaining();
        Assert.assertEquals("",stubListener.outputQueue.remove());
        Assert.assertEquals(0,stubListener.outputQueue.size());

    }

    @Test
    public void add_twoOrMoreMessage_addToBackupQueue(){
        //Arrange
        ByteBuffer testPayload=buildTestMessage("lalala");
        ByteBuffer testPayload2=buildTestMessage("foo");


        //Act
        target.add(testPayload);
        target.add(testPayload2);

        //Assert
        target.sendRemaining();
        Assert.assertEquals("lalala",stubListener.outputQueue.remove());
        Assert.assertEquals("foo",stubListener.outputQueue.remove());
        Assert.assertEquals(0,stubListener.outputQueue.size());
    }

    @Test
    public void add_OrderEventReceived_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setContributorSeq(3);
        orderMsg.setOrderID(1);
        orderMsg.setExternalOrderID(100);
        orderMsg.setQty(10);

        //Stream event received
        MatchOrderCommand streamOrderMsg = messages.getMatchOrderCommand();
        streamOrderMsg.setContributorSeq(3);
        streamOrderMsg.setOrderID(1);
        streamOrderMsg.setExternalOrderID(100);
        streamOrderMsg.setQty(10);
        MatchOrderEvent streamEvent = streamOrderMsg.toEvent();

        //Act
        target.add(orderMsg.getRawBuffer());

        //Assert
        target.verifyOrderEvent(streamEvent);
        Assert.assertEquals(0,stubListener.outputQueue.size());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyOrderEvent_OrderEventDoNotHaveSameOrderID_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setContributorSeq(3);
        orderMsg.setOrderID(2);
        orderMsg.setExternalOrderID(100);
        orderMsg.setQty(10);

        //Stream event received
        MatchOrderCommand streamOrderMsg = messages.getMatchOrderCommand();
        streamOrderMsg.setContributorSeq(3);
        streamOrderMsg.setOrderID(1);
        streamOrderMsg.setExternalOrderID(100);
        streamOrderMsg.setQty(10);
        MatchOrderEvent streamEvent = streamOrderMsg.toEvent();

        //Act
        target.add(orderMsg.getRawBuffer());
        target.verifyOrderEvent(streamEvent);

        //Assert
        Assert.assertEquals(1,stubListener.outputQueue.size());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyOrderEvent_OrderEventDoNotHaveSameDisplayOrderID_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setContributorSeq(3);
        orderMsg.setOrderID(1);
        orderMsg.setExternalOrderID(100);
        orderMsg.setQty(10);

        //Stream event received
        MatchOrderCommand streamOrderMsg = messages.getMatchOrderCommand();
        streamOrderMsg.setContributorSeq(3);
        streamOrderMsg.setOrderID(1);
        streamOrderMsg.setExternalOrderID(0);
        streamOrderMsg.setQty(10);
        MatchOrderEvent streamEvent = streamOrderMsg.toEvent();

        //Act
        target.add(orderMsg.getRawBuffer());
        target.verifyOrderEvent(streamEvent);


        //Assert
        Assert.assertEquals(1,stubListener.outputQueue.size());

    }

    @Test(expected = IllegalStateException.class)
    public void verifyOrderEvent_OrderEventDoNotHaveSameDisplayQty_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchOrderCommand orderMsg = messages.getMatchOrderCommand();
        orderMsg.setContributorSeq(3);
        orderMsg.setOrderID(2);
        orderMsg.setExternalOrderID(100);
        orderMsg.setQty(10);

        //Stream event received
        MatchOrderCommand streamOrderMsg = messages.getMatchOrderCommand();
        streamOrderMsg.setContributorSeq(3);
        streamOrderMsg.setOrderID(1);
        streamOrderMsg.setExternalOrderID(100);
        streamOrderMsg.setQty(10);
        MatchOrderEvent streamEvent = streamOrderMsg.toEvent();

        //Act
        target.add(orderMsg.getRawBuffer());

        //Assert
        target.verifyOrderEvent(streamEvent);
        Assert.assertEquals(1,stubListener.outputQueue.size());

    }

    //////////


    @Test
    public void verifyReplaceEvent_identicalReplaceEventReceived_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchReplaceCommand replaceCmd = messages.getMatchReplaceCommand();
        replaceCmd.setInBook(true);
        replaceCmd.setExternalOrderID(100);
        replaceCmd.setQty(10);

        //Stream event received
        MatchReplaceCommand streamReplaceCmd = messages.getMatchReplaceCommand();
        streamReplaceCmd.setInBook(true);
        streamReplaceCmd.setExternalOrderID(100);
        streamReplaceCmd.setQty(10);
        MatchReplaceEvent streamEvent = streamReplaceCmd.toEvent();

        //Act
        target.add(replaceCmd.getRawBuffer());
        target.verifyReplaceEvent(streamEvent);

        //Assert
        Assert.assertEquals(0,stubListener.outputQueue.size());

    }

    @Test(expected = IllegalStateException.class)
    public void verifyReplaceEvent_replaceEventDoNotHaveSameDispOrderID_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchReplaceCommand replaceCmd = messages.getMatchReplaceCommand();
        replaceCmd.setInBook(true);
        replaceCmd.setExternalOrderID(100);
        replaceCmd.setQty(10);

        //Stream event received
        MatchReplaceCommand streamReplaceCmd = messages.getMatchReplaceCommand();
        streamReplaceCmd.setInBook(true);
        streamReplaceCmd.setExternalOrderID(99);
        streamReplaceCmd.setQty(10);
        MatchReplaceEvent streamEvent = streamReplaceCmd.toEvent();

        //Act
        target.add(replaceCmd.getRawBuffer());
        target.verifyReplaceEvent(streamEvent);

        //Assert
        Assert.assertEquals(1,stubListener.outputQueue.size());
    }

    @Test(expected = IllegalStateException.class)
    public void verifyReplaceEvent_replaceEventDoNotHaveSamety_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchReplaceCommand replaceCmd = messages.getMatchReplaceCommand();
        replaceCmd.setInBook(true);
        replaceCmd.setExternalOrderID(100);
        replaceCmd.setQty(10);

        //Stream event received
        MatchReplaceCommand streamReplaceCmd = messages.getMatchReplaceCommand();
        streamReplaceCmd.setInBook(true);
        streamReplaceCmd.setExternalOrderID(100);
        streamReplaceCmd.setQty(11);
        MatchReplaceEvent streamEvent = streamReplaceCmd.toEvent();

        //Act
        target.add(replaceCmd.getRawBuffer());
        target.verifyReplaceEvent(streamEvent);

        //Assert
        Assert.assertEquals(1,stubListener.outputQueue.size());
    }

    @Test
    public void verifyCancelEvent_identicalCancelEventReceived_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchCancelCommand cancelCmd = messages.getMatchCancelCommand();
        cancelCmd.setOrderID(1);

        //Stream event received
        MatchCancelCommand streamCancelCmd = messages.getMatchCancelCommand();
        streamCancelCmd.setOrderID(1);
        MatchCancelEvent streamEvent = streamCancelCmd.toEvent();


        //Act
        target.add(cancelCmd.getRawBuffer());
        target.verifyCancelEvent(streamEvent);

        //Assert
        Assert.assertEquals(0,stubListener.outputQueue.size());

    }

    @Test(expected = IllegalStateException.class)
    public void verifyCancelEvent_replaceEventDoNotHaveSameOrderID_addToBackupQueue(){
        //Arrange
        //Generate the order generated
        MatchCancelCommand cancelCmd = messages.getMatchCancelCommand();
        cancelCmd.setOrderID(1);


        //Stream event received
        MatchCancelCommand streamCancelCmd = messages.getMatchCancelCommand();
        streamCancelCmd.setOrderID(3);

        MatchCancelEvent streamEvent = streamCancelCmd.toEvent();

        //Act
        target.add(cancelCmd.getRawBuffer());
        target.verifyCancelEvent(streamEvent);

        //Assert
        Assert.assertEquals(1,stubListener.outputQueue.size());
    }

    private class StubBackupQueueListener implements BackupQueueListener {
        private ByteBuffer tmp=ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);
        public Queue<String> outputQueue=new LinkedList<>();

        @Override
        public void sendRemainingQueueMessages(ByteBuffer byteBuffer) {
            byte[] slice = Arrays.copyOfRange(byteBuffer.array(), 0, byteBuffer.limit());
            outputQueue.add(new String(slice));
        }
    }
}