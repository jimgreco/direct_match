package com.core.util.log;

import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.time.TimeSource;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class SystemOutLog implements Log, Logger {
    private final TimeSource timeSource;

    private final byte[] array = new byte[20 * 1024];

    private final int timePosition;
    private final int levelPosition;
    private final int textPosition;

    private int position;
    private boolean debug;

    public SystemOutLog(String coreName, String name, TimeSource timeSource) {
        this(coreName, name, timeSource, 10);
    }

    public SystemOutLog(String coreName, String name, TimeSource timeSource, int contributorSize) {
        this.timeSource = timeSource;

        StringBuilder b = new StringBuilder();

        timePosition = 0;
        b.append("HH:mm:ss.SSS ");

        levelPosition = b.length();
        b.append("LEVEL");

        b.append(" ");
        b.append(coreName);
        b.append(" ");
        b.append(name);
        for (int i=name.length(); i<contributorSize; i++) {
            b.append(" ");
        }
        b.append(" ");
        textPosition = b.length();

        byte[] startTemplate = b.toString().getBytes();
        System.arraycopy(startTemplate, 0, array, 0, startTemplate.length);
    }

    @Override
    public boolean isDebugEnabled() {
        return debug;
    }

    @Override
    public void setDebug(boolean debug) {
        this.debug = debug;
    }

    @Override
    public void debug(Logger log) {
        if (debug) {
            print("DEBUG");
        }
    }

    @Override
    public void info(Logger log) {
        print("INFO");
    }

    @Override
    public void warn(Logger log) {
        print("WARN");
    }

    @Override
    public void error(Logger log) {
        print("ERROR");
    }

    @Override
    public Logger log() {
        this.position = textPosition;
        return this;
    }

    @Override
    public Logger add(boolean b) {
        if (checkSize(5)) {
            position += TextUtils.writeBool(array, position, b);
        }
        return this;
    }

    @Override
    public Logger add(char b) {
        if (checkSize(1)) {
            add((byte)b);
        }
        return this;
    }

    @Override
    public Logger add(byte b) {
        if (checkSize(1)) {
            array[position++] = b;
        }
        return this;
    }

    @Override
    public Logger add(String str) {
        if (str == null) {
            str = "null";
        }

        if (checkSize(str.length())) {
            for (int i=0; i<str.length(); i++) {
                array[position++] = (byte)str.charAt(i);
            }
        }
        return this;
    }

    @Override
    public Logger add(ByteBuffer buf) {
        if (buf == null) {
            return add("null");
        }

        int size = buf.remaining();
        if (checkSize(size)) {
            buf.mark();
            buf.get(array, position, size);
            buf.reset();
            position += size;
        }
        return this;
    }

    @Override
    public Logger addAsHex(ByteBuffer buf) {
        if (buf == null) {
            return add("null");
        }

        int c = 0;
        int remaining = buf.remaining();
        if (remaining % 16 != 0) {
            remaining += 16 - (remaining % 16);
        }

        add('\n');
        for (int i=buf.position(); i<buf.position() + remaining; i++) {
            if (i < buf.limit()) {
                byte b = buf.get(i);
                add(BinaryUtils.ASCII[(b >> 4) & 0xF]);
                add(BinaryUtils.ASCII[b & 0xF]);
            }
            else {
                add(' ');
                add(' ');
            }
            add(' ');

            if (++c % 16 == 0) {
                add(' ');
                add(' ');

                for (int j=i-15; j<=i; j++) {
                    if (j < buf.limit()) {
                        byte b = buf.get(j);
                        if (b >= ' ' && b < '~') {
                            add(b);
                        }
                        else {
                            add('.');
                        }
                    }
                    else {
                        add(' ');
                    }
                }

                add('\n');
            }
        }

        return this;
    }

    @Override
    public Logger add(byte[] bytes) {
        if (bytes == null) {
            return add("null");
        }

        if (checkSize(bytes.length)) {
            System.arraycopy(bytes, 0, array, position, bytes.length);
            position += bytes.length;
        }
        return this;
    }

    @Override
    public Logger add(long i) {
        if (checkSize(100)) {
            position += TextUtils.writeNumber(array, position, i);
        }
        return this;
    }

    @Override
    public Logger add(Throwable e) {
        if (e == null) {
            return add("null");
        }

        error(this);

        error(log().add(e.toString()));

        StackTraceElement[] stackTrace = e.getStackTrace();
        for (StackTraceElement stackTraceElement : stackTrace) {
            error(log().add(stackTraceElement.toString()));
        }

        return log().add("EXCEPTION END");
    }

    private void print(String level) {
        TextUtils.writeTimeUTC(array, timePosition, timeSource.getTimestamp());

        for (int i=0; i<level.length(); i++) {
            array[levelPosition + i] = (byte)level.charAt(i);
        }

        for (int i=level.length(); i<5; i++) {
            array[levelPosition + i] = (byte)' ';
        }

        add('\n');
        System.out.write(array, 0, position);
    }

    private boolean checkSize(int len) {
        return position + len < array.length;
    }
}
