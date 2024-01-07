package com.core.connector.mold.rewindable;

import com.core.util.Factory;
import com.core.util.log.Log;
import com.core.util.pool.ObjectPool;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPServerSocket;
import com.gs.collections.impl.map.mutable.UnifiedMap;

import java.util.Map;

/**
 * Created by hli on 6/1/16.
 */
public class RewinderSocketPool {
    private Log logger ;
    private IndexedStore store;
    private  final int capacity;
    ObjectPool<SingleRewindSocket> rewindHandlerObjectPool;

    public RewinderSocketPool(Log logger , IndexedStore store,int capacity,TCPServerSocket rewindServerSocket){
        this.store = store;
        this.logger = logger;
        this.capacity=capacity;
        rewindHandlerObjectPool = new ObjectPool<>(logger, "RewinderConnectionPool", new Factory<SingleRewindSocket>() {
            @Override
            public SingleRewindSocket create() {
                return new SingleRewindSocket(logger, store,rewindServerSocket);
            }
        },capacity);
    }


    public SingleRewindSocket create(TCPClientSocket clientSocket){
        clientSocket.enableRead(true);
        SingleRewindSocket socket= rewindHandlerObjectPool.create();
        return socket;
    }

}
