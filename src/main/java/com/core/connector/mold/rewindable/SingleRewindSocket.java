package com.core.connector.mold.rewindable;

import com.core.connector.mold.Mold64UDPPacket;
import com.core.util.datastructures.contracts.Linkable;
import com.core.util.log.Log;
import com.core.util.pool.Poolable;
import com.core.util.store.IndexedStore;
import com.core.util.tcp.TCPClientSocket;
import com.core.util.tcp.TCPClientSocketListener;
import com.core.util.tcp.TCPServerSocket;

import java.nio.ByteBuffer;

/**
 * Created by hli on 6/1/16.
 */
public class SingleRewindSocket implements Poolable<SingleRewindSocket> , Linkable<SingleRewindSocket>, TCPClientSocketListener {
    private final Log log;
    private final IndexedStore store;
    private final ByteBuffer requestBuffer = ByteBuffer.allocate(8);
    private final ByteBuffer rewindMessage = ByteBuffer.allocate(Mold64UDPPacket.MTU_SIZE);

    private boolean rewinding;
    private long rewindSequence;
    private long rewindStartSequence;
    private SingleRewindSocket next;
    private TCPServerSocket rewindServerSocket;

    public SingleRewindSocket(Log log, IndexedStore store, TCPServerSocket rewindServerSocket){
        this.log=log;
        this.store= store;
        this.rewindServerSocket=rewindServerSocket;
    }


    @Override
    public void onConnect(TCPClientSocket clientSocket) {

    }

    @Override
    public void onDisconnect(TCPClientSocket clientSocket) {
        clear();
        rewindServerSocket.closeClient(clientSocket);

    }

    public void onReadAvailable(TCPClientSocket clientSocket) {
        requestBuffer.clear();
        if(clientSocket.read(requestBuffer)) {
            if(rewinding) {
                //Shouldnt really happen
                clientSocket.close();
                return;
            }
            requestBuffer.flip();
            if(requestBuffer.remaining() < 8) {
                log.info(log.log().add("Ignoring log in attempt because invalid login message."));
                clientSocket.close();
                return; // not enough bytes available
            }
            rewindSequence = requestBuffer.getLong();
            if(rewindSequence > store.getCurrentIndex()) {
                log.info(log.log().add("Ignoring log in attempt because requested sequence is greater than last seen. Request was for ").add(rewindSequence).add(" but max seen was ").add(store.getCurrentIndex()));
                clientSocket.close();
                return;
            }
            rewinding = true;
            log.info(log.log().add("Beginning rewind to client from sequence ").add(rewindSequence));
            rewindStartSequence = rewindSequence;
            rewind(clientSocket);
        }
    }

    @Override
    public void onWriteAvailable(TCPClientSocket clientSocket) {
        rewind(clientSocket);
    }

    @Override
    public void onWriteUnavailable(TCPClientSocket clientSocket) {

    }


    public void rewind(TCPClientSocket socket) {
        if(!rewinding) {
            return;
        }
        while(socket.canWrite()) {
            if(rewindSequence > store.getCurrentIndex() + 1) {
                // We're done with all messages for now.
                // close the socket connection
                log.info(log.log().add("Rewound ").add(rewindSequence - rewindStartSequence).add(" messages to client"));
                socket.close();
                return;
            }

            rewindMessage.clear();
            rewindMessage.position(4); // Save room for the size of the message
            store.get(rewindSequence, rewindMessage);
            rewindMessage.putInt(0, rewindMessage.position() - 4); // size of message = current position - start index
            rewindMessage.flip();
            socket.write(rewindMessage);
            rewindSequence++;
        }
    }

    @Override
    public SingleRewindSocket next() {
        return next;
    }

    @Override
    public void setNext(SingleRewindSocket next) {
        this.next=next;
    }

    @Override
    public void clear() {
        requestBuffer.clear();
        rewindMessage.clear();
        rewinding=false;
        rewindSequence=0;
        rewindStartSequence= 0;

    }
}
