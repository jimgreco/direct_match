package com.core.printer;

import com.core.app.AppConstructor;
import com.core.app.Param;
import com.core.app.UniversalApplication;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.AfterMessageListener;
import com.core.connector.Connector;
import com.core.util.file.File;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class BinaryPrinter extends UniversalApplication implements AfterMessageListener {
    private final File file;
    private final ByteBuffer lengthBuffer = ByteBuffer.allocateDirect(2);

    @AppConstructor
    public BinaryPrinter(FileFactory fileFactory,
                         Connector connector,
                         Log log,
                         @Param(name="file") String fileName) throws IOException {
        super(log);
        this.file = fileFactory.createFile(fileName, "w");

        connector.addAfterListener(this);
    }

    @Override
    public void onAfterMessage(ByteBuffer message) {
        try {
            lengthBuffer.clear();
            lengthBuffer.putShort((short) message.remaining());
            lengthBuffer.flip();
            file.write(lengthBuffer);

            int position = message.position();
            file.write(message);
            message.position(position);
        } catch (IOException e) {
            log.error(log.log().add("Error writing message to file\n").add(e.getMessage()));
        }
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {

    }
}
