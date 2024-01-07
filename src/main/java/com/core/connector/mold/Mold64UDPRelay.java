package com.core.connector.mold;

import com.core.app.AppConstructor;
import com.core.app.CommandException;
import com.core.app.Param;
import com.core.app.UniversalApplication;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.connector.AfterMessageListener;
import com.core.connector.Connector;
import com.core.util.file.FileFactory;
import com.core.util.log.Log;
import com.core.util.store.FileIndexedStore;
import com.core.util.udp.UDPSocketFactory;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class Mold64UDPRelay extends UniversalApplication implements AfterMessageListener {
    private final FileIndexedStore file;
    private final Mold64UDPRequestHandler handler;

    @AppConstructor
    public Mold64UDPRelay(FileFactory fileFactory,
                          UDPSocketFactory udpSocketFactory,
                          Connector connector,
                          Log log,
                          @Param(name = "Name") String name,
                          @Param(name = "Host") String host,
                          @Param(name = "Intf") String requestIntf,
                          @Param(name = "EventPort") short eventPort,
                          @Param(name = "RequestPort") short requestPort) throws IOException {
        super(log);

        this.file = new FileIndexedStore(log, fileFactory, name);

        handler = new Mold64UDPRequestHandler(
                udpSocketFactory,
                log,
                file,
                requestIntf,
                host,
                requestPort,
                eventPort);
        connector.addSessionSourceListener(handler);
        connector.addAfterListener(this);
    }

    @Override
    protected void onActive() {
        try {
            handler.open();
        } catch (IOException e) {
            throw new CommandException(e);
        }
    }

    @Override
    protected void onPassive() {
        handler.close();
    }

    @Override
    public void onAfterMessage(ByteBuffer rawBuffer) {
        try {
            rawBuffer.mark();
            file.add(rawBuffer);
            rawBuffer.reset();
        } catch (Exception e) {
            log.error(log.log().add(e));
        }
    }

    @Override
    public void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister) {

    }

    @Override
    public void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater) {

    }
}
