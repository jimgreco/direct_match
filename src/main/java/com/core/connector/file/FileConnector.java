package com.core.connector.file;

import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatSource;
import com.core.connector.BaseConnector;
import com.core.connector.ByteBufferDispatcher;
import com.core.connector.Connector;
import com.core.util.ByteStringBuffer;
import com.core.util.file.FileFactory;
import com.core.util.time.TimerHandler;
import com.core.util.time.TimerService;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * User: jgreco
 */
public class FileConnector extends BaseConnector implements Connector, TimerHandler, HeartbeatSource {
    private final Path path;
    private final ByteBuffer readBuffer = ByteBuffer.allocateDirect(4096);
    private final TimerService timerService;


    private FileChannel file;
    private int nextMsgSeq = 1;

    public FileConnector(@SuppressWarnings("unused") FileFactory fileFactory, ByteBufferDispatcher dispatcher, TimerService timerService, String fname) {
        super(dispatcher);
        this.path = FileSystems.getDefault().getPath(fname);

        this.timerService = timerService;
        this.timerService.scheduleTimer(100, this);
    }

    public boolean poll() {
        try {
            int read = file.read(readBuffer);

            if (read == -1) {
                // done
                return false;
            }
            else if (read > 0) {
                readBuffer.flip();

                while(readBuffer.hasRemaining()) {
                    if (readBuffer.remaining() < 2) {
                        break;
                    }

                    short length = readBuffer.getShort(readBuffer.position());

                    if (readBuffer.remaining() < 2 + length) {
                        break;
                    }

                    ByteBuffer slice = readBuffer.slice();
                    slice.position(slice.position() + 2);
                    slice.limit(slice.position() + length);

                    dispatchMessage(slice);
                    dispatchMessageGroupComplete(++nextMsgSeq);

                    readBuffer.position(readBuffer.position() + 2 + length);
                }

                if (readBuffer.hasRemaining()) {
                    readBuffer.compact();
                }
                else {
                    readBuffer.clear();
                }
                return true;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    @Override
    public void open() throws IOException {
        file = FileChannel.open(path, StandardOpenOption.READ);
    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public long getCurrentSeq() {
        return nextMsgSeq - 1;
    }

    @Override
    public ByteStringBuffer status() {
        return null;
    }

    @Override
    public String getSession() {
        // TODO: Need to get filename to extract session
        return "TODO";
    }

    @Override
    public void onTimer(int internalTimerID, int referenceData) {
        if (poll()) {
            timerService.scheduleTimer(100, this);
        }
    }

    @Override
    public void onHeartbeatRegister(HeartbeatFieldRegister register) {

    }

    @Override
    public void onHeartbeatUpdate(HeartbeatFieldUpdater register) {

    }
}
