package com.core.util.log;

import com.core.util.BinaryUtils;
import com.core.util.TextUtils;
import com.core.util.file.File;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 * Don't use, in progress
 */
public class FileChannelLog implements Log {
    private final LoggerImpl logger = new LoggerImpl();
    private boolean debug;

    public FileChannelLog(@SuppressWarnings("unused") File file) {
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
        if (debug)
        {
        }
    }

    @Override
    public void info(Logger log) {

    }

    @Override
    public void warn(Logger log) {

    }

    @Override
    public void error(Logger log) {

    }

    @Override
    public Logger log() {
        return logger;
    }

    private class LoggerImpl implements Logger {
        private final ByteBuffer buffer = ByteBuffer.allocateDirect(4096);

        protected LoggerImpl() {}

		@Override
        public Logger add(byte[] b) {
            buffer.put(buffer);
            return this;
        }

        @Override
        public Logger add(ByteBuffer b) {
            buffer.put(b);
            return this;
        }

        @Override
        public Logger add(Throwable e) {
            BinaryUtils.copy(buffer, e.getMessage());
            buffer.put((byte)'\n');
            StackTraceElement[] stackTrace = e.getStackTrace();
            for (StackTraceElement el : stackTrace) {
                BinaryUtils.copy(buffer, el.getClassName());
                buffer.put((byte)'.');
                BinaryUtils.copy(buffer, el.getMethodName());

                buffer.put((byte)'(');
                if (el.isNativeMethod()) {
                    BinaryUtils.copy(buffer, "Native Method");
                }
                else if (el.getFileName() != null) {
                    BinaryUtils.copy(buffer, el.getFileName());
                    if (el.getLineNumber() >= 0) {
                        buffer.put((byte)':');
                        TextUtils.writeNumber(buffer, el.getLineNumber());
                    }
                }
                else {
                    BinaryUtils.copy(buffer, "Unknown Source");
                }
                buffer.put((byte)')');
            }
            return this;
        }

        @Override
        public Logger addAsHex(ByteBuffer buf) {
            return this;
        }

        @Override
        public Logger add(long l) {
            TextUtils.writeNumber(buffer, l);
            return this;
        }

        @Override
        public Logger add(boolean b) {
            TextUtils.writeBool(buffer, b);
            return this;
        }

        @Override
        public Logger add(char c) {
            buffer.put((byte) c);
            return this;
        }

        @Override
        public Logger add(byte b) {
            TextUtils.writeNumber(buffer, b);
            return this;
        }

        @Override
        public Logger add(String str) {
            BinaryUtils.copy(buffer, str);
            return this;
        }
    }
}
