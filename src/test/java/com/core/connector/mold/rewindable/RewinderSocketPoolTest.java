package com.core.connector.mold.rewindable;

import com.core.util.NullLog;
import com.core.util.log.Log;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPServerSocket;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.mockito.Mockito.verify;

/**
 * Created by hli on 6/2/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class RewinderSocketPoolTest {
    private Log logger;

    @Mock
    private IndexedStore store;


    @Mock
    private TCPClientSocket clientSocket;

    @Mock
    private TCPServerSocket serverSocket;
    private int capacity=2;

    @Before
    public void setUp(){
        logger=new NullLog();
    }

    private RewinderSocketPool createTarget(){
        return new RewinderSocketPool(logger,store, capacity,serverSocket);
    }
    @Test
    public void create_incomingConnectionsLessThanMaxCapacity_enableReadAndStoreInMap()  {
        //Arrange
        RewinderSocketPool target=createTarget();

        //Act
        target.create(clientSocket);

        //Assert
        verify(clientSocket).enableRead(true);
    }




}