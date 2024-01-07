package com.core.app;

import com.core.app.heartbeats.HeartBeatFieldIDEnum;
import com.core.app.heartbeats.HeartbeatApp;
import com.core.app.heartbeats.HeartbeatBooleanField;
import com.core.app.heartbeats.HeartbeatConstants;
import com.core.app.heartbeats.HeartbeatField;
import com.core.app.heartbeats.HeartbeatFieldRegister;
import com.core.app.heartbeats.HeartbeatFieldUpdater;
import com.core.app.heartbeats.HeartbeatNumberField;
import com.core.app.heartbeats.HeartbeatStringField;
import com.core.connector.AllCommandsClearedListener;
import com.core.connector.CommandSender;
import com.core.util.ByteStringBuffer;
import com.core.util.log.Log;

import java.nio.ByteBuffer;
import java.util.List;

/**
 * Created by jgreco on 6/19/15.
 */
public abstract class UniversalApplication implements Application {
    private final ByteStringBuffer status = new ByteStringBuffer();
    private final CommandSender sender;
    protected final Log log;

    private HeartbeatApp fieldRegister;
    private HeartbeatNumberField contribID;
    private HeartbeatNumberField seqNum;
    private HeartbeatNumberField lastSeenSeqNum;
    private HeartbeatBooleanField canSend;
    private HeartbeatBooleanField active;
    private HeartbeatBooleanField caughtUp;
    private HeartbeatBooleanField canWrite;
    private HeartbeatStringField contrib;
    private HeartbeatStringField host;

    public abstract void onAddHeartbeatFields(HeartbeatFieldRegister fieldRegister);
    public abstract void onUpdateHeartbeatFields(HeartbeatFieldUpdater fieldUpdater);

    protected void addStatusDetails(@SuppressWarnings("unused") ByteStringBuffer statusUpdate) { }
    protected void beforeActive() { }
    protected void beforePassive() { }
    protected void onActive() { }
    protected void onPassive() { }

    public UniversalApplication(Log log) {
        this(log, null);
    }

    public UniversalApplication(Log log, CommandSender sender) {
        this.log = log;
        this.sender = sender;
    }

    @Override
    public void onHeartbeatRegister(HeartbeatFieldRegister fieldRegister) {
        fieldRegister.addStringField("App", HeartBeatFieldIDEnum.Type).set(this.getClass().getSimpleName());

        this.fieldRegister = (HeartbeatApp)fieldRegister;

        if (sender != null) {
            contrib = fieldRegister.addStringField("App", HeartBeatFieldIDEnum.Contrib);
            contribID = fieldRegister.addNumberField("App", HeartBeatFieldIDEnum.ContribId);
            seqNum = fieldRegister.addNumberField("App", HeartBeatFieldIDEnum.SeqNum);
            lastSeenSeqNum = fieldRegister.addNumberField("App", HeartBeatFieldIDEnum.LastSeenSeqNum);
            canSend = fieldRegister.addBoolField("App", HeartBeatFieldIDEnum.CanSend);
            active = fieldRegister.addBoolField("App", HeartBeatFieldIDEnum.Active);
            caughtUp = fieldRegister.addBoolField("App", HeartBeatFieldIDEnum.CaughtUp);
            canWrite = fieldRegister.addBoolField("App", HeartBeatFieldIDEnum.CanWrite);
            host=fieldRegister.addStringField("App",HeartBeatFieldIDEnum.SenderHost);
        }

        onAddHeartbeatFields(fieldRegister);
    }

    @Override
    public void setDebug(@Param(name = "Debug") boolean debug) {
        log.setDebug(debug);
    }

    @Override
    public final ByteStringBuffer status() {
        onHeartbeatUpdate(fieldRegister);

        status.clear();

        addStatusDetails(status);

        String lastCategory = "-----";
        List<HeartbeatField> fields = fieldRegister.getFields();
        for (int i=0; i<fields.size(); i++) {
            HeartbeatField field = fields.get(i);

            String category = field.getCategory();
            if (!category.equals(lastCategory)) {
                status.add(category);
                status.add("\n\r");
                lastCategory = category;
            }

            status.add("  ");
            status.add(field.getName());
            status.add(": ");
            switch (field.getType()) {
                case HeartbeatConstants.NUMBER_TYPE:
                    status.add(((HeartbeatNumberField)field).get());
                    break;
                case HeartbeatConstants.STRING_TYPE:
                    status.add(((HeartbeatStringField) field).get());
                    break;
                case HeartbeatConstants.BOOLEAN_TYPE:
                    status.add(((HeartbeatBooleanField) field).get());
                    break;
                default:
                    break;
            }
            status.add("\n\r");
        }

        return status;
    }

    @Override
    public void onHeartbeatUpdate(HeartbeatFieldUpdater fieldUpdater) {
        if (sender != null) {
            contrib.set(sender.getName());
            contribID.set(  sender.getContribID());
            seqNum.set(sender.getContribSeqNum());
            lastSeenSeqNum.set(sender.getLastSeenContribSeqNum());
            canSend.set(sender.canSend() );
            active.set(sender.isActive() );
            caughtUp.set(sender.isCaughtUp());
            canWrite.set(sender.canWrite());
            host.set(sender.getDestinationAddress());
        }

        onUpdateHeartbeatFields(fieldUpdater);
    }

    @Override
    public final void setActive() {
        log.info(log.log().add("setActive(): Before Active"));
        beforeActive();

        if (sender != null) {
            log.info(log.log().add("setActive(): Command Sender"));
            sender.setActive();
        }

        log.info(log.log().add("setActive(): On Active"));
        try {
            onActive();
        }
        catch (Exception e) {
            log.error(log.log().add("setActive(): Error in onActive() call"));
            try {
                if (sender != null) {
                    sender.setPassive();
                }
            }
            catch (Exception ignored) {}
            throw e;
        }

        log.info(log.log().add("setActive(): Complete"));
    }

    @Override
    public final void setPassive() {
        log.info(log.log().add("setPassive(): Before Passive"));
        beforePassive();

        if (sender != null) {
            log.info(log.log().add("setPassive(): Command Sender"));
            sender.setPassive();
        }

        log.info(log.log().add("setPassive(): Passive"));
        onPassive();

        log.info(log.log().add("setPassive(): Complete"));
    }

    protected void send(ByteBuffer buffer) {
        init();
        add(buffer);
        send();
    }

    protected void add(ByteBuffer buffer) {
        if (sender != null) {
            sender.add(buffer);
        }
    }

    protected void init() {
        if (sender != null) {
            sender.init();
        }
    }

    protected void send() {
        if (sender != null) {
            sender.send();
        }
    }

    protected boolean canSend() {
        return sender != null && sender.canSend();
    }

    protected boolean isActive() {
        return sender.isActive();
    }

    protected boolean isPassive() {
        return !isActive();
    }

    protected short getContribID() {
        return sender != null ? sender.getContribID() : 0;
    }

    protected void addAllCommandsClearedListener(AllCommandsClearedListener listener) {
        sender.addAllCommandsClearedListener(listener);
    }
}
