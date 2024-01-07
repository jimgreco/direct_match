package com.core.connector.soup;

import com.core.connector.BaseCommandSender;
import com.core.connector.soup.msgs.SoupByteBufferMessages;
import com.core.connector.soup.msgs.SoupUnsequencedDataCommand;
import com.core.util.log.Log;

import java.nio.ByteBuffer;

/**
 * Created by jgreco on 7/27/15.
 */
public class SoupCommandSender extends BaseCommandSender {
    private final SoupClientConnector connector;
    private final SoupByteBufferMessages messages;

    public SoupCommandSender(Log log, String name, SoupAppConnector connector) {
        super(log, name);

        this.messages = new SoupByteBufferMessages();
        this.connector = connector.getClientConnector();
    }

    @Override
    protected void onCommandCleared() {

    }


    @Override
    protected void onActive() {
        // nothing to do...
    }

    @Override
    protected void onPassive() {
        
    }

    @Override
    protected void onInit() {
        // we send the message on every add
    }

    @Override
    public void add(ByteBuffer buffer) {
        SoupUnsequencedDataCommand cmd = messages.getSoupUnsequencedDataCommand();
        cmd.setMessage(buffer);
        connector.send(cmd);
    }

    @Override
    protected boolean onSend() {
        return true;
    }

    @Override
    public String getDestinationAddress() {
        return connector.getTargetHost();
    }

    @Override
    public boolean canWrite() {
        return connector.isLoggedIn();
    }
}
