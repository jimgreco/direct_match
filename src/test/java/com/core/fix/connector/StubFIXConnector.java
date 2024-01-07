package com.core.fix.connector;

import com.core.connector.AllCommandsClearedListener;
import com.core.fix.FixMessage;
import com.core.fix.FixParser;
import com.core.fix.msgs.FixConstants;
import com.core.fix.tags.StubFixTag;
import com.core.match.fix.FixMachine;
import com.core.match.fix.FixStateMachine;
import com.core.util.BinaryUtils;
import com.gs.collections.impl.list.mutable.FastList;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 1/4/15.
 */
public class StubFIXConnector implements FixConnector, AllCommandsClearedListener {
    private final List<FixMessage> messages = new FastList<>();
    private int falseAtSeqNum;
    private FIXConnectionListener connectionListener;
    private FIXConnectorListener listener;

    public void setReturnValueFalseAtSeqNum(int seqNum) {
        this.falseAtSeqNum = seqNum;
    }

    public FixMessage get() {
        return messages.size() > 0 ? messages.remove(0) : null;
    }

    public FixMessage get(int index) {
        return index < messages.size() ? messages.get(index) : null;
    }

    @Override
    public void resendComplete() {

    }

    @Override
    public void addListener(FIXConnectorListener listener) {
        this.listener = listener;
    }

    @Override
    public void init(FixParser parser, FixMachine machine) {

    }

    public void connect() {
        connectionListener.onConnect();
    }

    public void disconnect() {
        connectionListener.onDisconnect();
    }

    @Override
    public void open() {

    }

    @Override
    public void close() throws IOException {

    }

    @Override
    public boolean send(ByteBuffer msg) {
        FixMessage fixMessage = new FixMessage();

        String e = BinaryUtils.toString(msg);
        e = e.replace((char)FixConstants.SOH, '|');
        System.out.println("TX " + e);

        String[] keyVal = e.split("\\|");
        for (String s : keyVal) {
            String[] split = s.split("=");
            StubFixTag stubFixTag = new StubFixTag(Integer.parseInt(split[0]));
            stubFixTag.setValue(split[1]);
            fixMessage.addTag(stubFixTag);
        }

        messages.add(fixMessage);
        return fixMessage.getSeqNum() != falseAtSeqNum;
    }

    @Override
    public void onAllCommandsCleared() {

    }

	@Override
	public void addConnectionListener(FIXConnectionListener listener)
	{
		this.connectionListener = listener;
	}
}
