package com.core.fix;

import com.core.fix.msgs.FixConstants;
import com.core.fix.msgs.FixTags;
import com.core.fix.tags.FixTag;
import com.core.fix.tags.InlineFixTag;
import com.core.util.BinaryUtils;
import com.core.util.PriceUtils;
import com.core.util.TextUtils;
import com.core.util.time.TimeSource;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class InlineFixWriter implements FixWriter {
    private final ByteBuffer emptyBuffer = ByteBuffer.wrap("NONE".getBytes());
    private final ByteBuffer timeBuffer = ByteBuffer.allocateDirect("20140522-00:19:36.324".length());
    private final ByteBuffer bodyBuffer = ByteBuffer.allocateDirect(10 * 1024);
    private final ByteBuffer temp = ByteBuffer.allocateDirect(256);

    private final TimeSource timeSource;

    private final FixTag bodyLengthTag;
    private final FixTag checkSumTag;
    private final FixTag beginStringTag;
    private final FixTag msgTypeTag;
    private final FixTag sendingTimeTag;
    private final FixTag senderCompIDTag;
    private final FixTag targetCompIDTag;
    private final FixTag msgSeqNumTag;
    private final FixTag possDupFlag;
    private final FixTag origSendingTime;

    private final byte[] fixVersion;
    private final byte[] senderCompId;
    private final byte[] targetCompId;

    private int msgTypeSize = 1;

    public InlineFixWriter(TimeSource timeSource, int version, String senderCompID, String targetCompID) {
        this.timeSource = timeSource;

        this.fixVersion = ("FIX.4." + version).getBytes();
        this.senderCompId = senderCompID.getBytes();
        this.targetCompId = targetCompID.getBytes();

        beginStringTag = new InlineFixTag(FixTags.BeginString);
        bodyLengthTag = new InlineFixTag(FixTags.BodyLength);
        checkSumTag = new InlineFixTag(FixTags.CheckSum);
        msgTypeTag = new InlineFixTag(FixTags.MsgType);
        sendingTimeTag = new InlineFixTag(FixTags.SendingTime);
        senderCompIDTag = new InlineFixTag(FixTags.SenderCompID);
        targetCompIDTag = new InlineFixTag(FixTags.TargetCompID);
        msgSeqNumTag = new InlineFixTag(FixTags.MsgSeqNum);
        possDupFlag = new InlineFixTag(FixTags.PossDupFlag);
        origSendingTime = new InlineFixTag(FixTags.OrigSendingTime);
    }

    @Override
    public void initFix(char msgType, int seqNum) {
        msgTypeSize = 1;
        bodyBuffer.clear();

        bodyBuffer.put(msgTypeTag.getTagString());
        bodyBuffer.put((byte)msgType);
        putSOH();

        writeRestOfBody(seqNum);
    }

    @Override
    public void initFix(String msgType, int seqNum) {
        msgTypeSize = 2;
        bodyBuffer.clear();

        bodyBuffer.put(msgTypeTag.getTagString());
        BinaryUtils.copy(bodyBuffer, msgType);
        putSOH();

        writeRestOfBody(seqNum);
    }

    private void writeRestOfBody(int seqNum) {
        bodyBuffer.put(sendingTimeTag.getTagString());
        TextUtils.writeDateTimeUTC(bodyBuffer, timeSource.getTimestamp());
        putSOH();

        bodyBuffer.put(msgSeqNumTag.getTagString());
        TextUtils.writeNumber(bodyBuffer, seqNum);
        putSOH();

        bodyBuffer.put(senderCompIDTag.getTagString());
        bodyBuffer.put(senderCompId);
        putSOH();

        bodyBuffer.put(targetCompIDTag.getTagString());
        bodyBuffer.put(targetCompId);
        putSOH();
    }

    @Override
    public ByteBuffer getFixBody() {
        bodyBuffer.flip();
        return bodyBuffer;
    }

    @Override
    public void buildFixMessage(ByteBuffer messageBuffer, ByteBuffer body, boolean resend) {
        int startPosition = messageBuffer.position();
        char resendMsgType = 0;
        char resendMsgType2 = 0;

        // bodyBuffer: 35=A|52=, length = 8
        if (resend) {
            int bodyPosition = body.position();
            int bodyLimit = body.limit();

            resendMsgType = (char)body.get(bodyPosition + 3);
            resendMsgType2 = (char)body.get(bodyPosition + 4);
            boolean twoLetterMsgType = resendMsgType2 != FixConstants.SOH;
            if (!twoLetterMsgType) {
                resendMsgType2 = 0;
            }

            msgTypeSize = twoLetterMsgType ? 2 : 1;
            int timeOffset = twoLetterMsgType ? 9 : 8;
            int newBodyStartPosition = twoLetterMsgType ? 6 : 5;

            // set the position/limit around the SendingTime
            body.position(bodyPosition + timeOffset);
            body.limit(body.position() + timeBuffer.capacity());

            // copy the sending time
            timeBuffer.clear();
            timeBuffer.put(body);
            timeBuffer.flip();

            // reset the position/limit to datetime
            body.position(bodyPosition + timeOffset);
            body.limit(body.position() + timeBuffer.capacity());

            // write the current time
            TextUtils.writeDateTimeUTC(body, timeSource.getTimestamp());

            // reset the position/limit to original
            body.position(bodyPosition + newBodyStartPosition);
            body.limit(bodyLimit);
        }

        int bodyLength = body.remaining();
        if (resend) {
            bodyLength += (msgTypeTag.getTagString().length + msgTypeSize + 1);
            bodyLength += (origSendingTime.getTagString().length + timeBuffer.capacity() + 1);
            bodyLength += (possDupFlag.getTagString().length + 1 + 1);
        }

        // 8=FIX.4.2
        messageBuffer.put(beginStringTag.getTagString());
        messageBuffer.put(fixVersion);
        messageBuffer.put(FixConstants.SOH);

        // 9=MsgLength
        messageBuffer.put(bodyLengthTag.getTagString());
        TextUtils.writeNumber(messageBuffer, bodyLength);
        messageBuffer.put(FixConstants.SOH);

        if (resend) {
            messageBuffer.put(msgTypeTag.getTagString());
            messageBuffer.put((byte)resendMsgType);
            if (resendMsgType2 > 0) {
                messageBuffer.put((byte)resendMsgType2);
            }
            messageBuffer.put(FixConstants.SOH);

            messageBuffer.put(origSendingTime.getTagString());
            messageBuffer.put(timeBuffer);
            messageBuffer.put(FixConstants.SOH);

            messageBuffer.put(possDupFlag.getTagString());
            messageBuffer.put((byte)'Y');
            messageBuffer.put(FixConstants.SOH);
        }

        // Body
        messageBuffer.put(body);

        // 10=Checksum
        int checkSum = 0;
        for (int i=startPosition; i<messageBuffer.position(); i++) {
            checkSum += messageBuffer.get(i);
        }
        checkSum %= 256;

        messageBuffer.put(checkSumTag.getTagString());
        TextUtils.writeNumberLeftPadded(messageBuffer, checkSum, 3, '0');
        messageBuffer.put(FixConstants.SOH);
    }

    @Override
    public void writeNumber(FixTag tag, long number) {
        bodyBuffer.put(tag.getTagString());
        TextUtils.writeNumber(bodyBuffer, number);
        putSOH();
    }

    @Override
    public void writeChar(FixTag tag, char c) {
        bodyBuffer.put(tag.getTagString());
        bodyBuffer.put((byte) c);
        putSOH();
    }

    @Override
    public void writePrice(FixTag tag, long price, int impliedDecimals) {
        bodyBuffer.put(tag.getTagString());
        PriceUtils.writePrice(bodyBuffer, price, impliedDecimals);
        putSOH();
    }

    @Override
    public void writeDateTime(FixTag tag, long time) {
        bodyBuffer.put(tag.getTagString());
        TextUtils.writeDateTimeUTC(bodyBuffer, time);
        putSOH();
    }

    @Override
    public void writeString(FixTag tag, String str) {
        if (str == null || str.length() == 0) {
            writeString(tag, (ByteBuffer)null);
            return;
        }

        temp.clear();
        BinaryUtils.copy(temp, str).flip();
        writeString(tag, temp);
    }

    @Override
    public void writeString(FixTag tag, ByteBuffer str) {
        if (str == null || str.remaining() == 0) {
            str = emptyBuffer;
        }

        str.mark();
        bodyBuffer.put(tag.getTagString());
        bodyBuffer.put(str);
        putSOH();
        str.reset();
    }

    private void putSOH() {
        bodyBuffer.put(FixConstants.SOH);
    }
}
