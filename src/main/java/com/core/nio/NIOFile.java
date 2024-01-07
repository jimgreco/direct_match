package com.core.nio;

import com.core.util.file.File;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * User: jgreco
 */
class NIOFile implements File {
    private final FileChannel channel;

    public NIOFile(String fileName, String mode) throws IOException {
        if (mode.equals("rw")) {
            RandomAccessFile randomAccessFile = new RandomAccessFile(fileName, mode);
            randomAccessFile.setLength(0);
            this.channel = randomAccessFile.getChannel();
        }
        else if (mode.equals("w")) {
            this.channel = new FileOutputStream(fileName, false).getChannel();
        }
        else {
            this.channel = new FileInputStream(fileName).getChannel();
        }
    }

    @Override
    public long size() {
        try {
            return channel.size();
        } catch (IOException e) {
            return -1;
        }
    }

    @Override
    public int write(ByteBuffer buf, long offset) throws IOException {
        int i = 0;
        int bytes = 0;
        while (buf.hasRemaining()) {
            bytes += channel.write(buf, offset + bytes);
            if (i++ > 100) {
                // just in case
                return bytes;
            }
        }
        return bytes;
    }

    @Override
    public int write(ByteBuffer buf) throws IOException {
        int i = 0;
        int bytes = 0;
        while (buf.hasRemaining()) {
            bytes += channel.write(buf);
            if (i++ > 100) {
                // just in case
                return bytes;
            }
        }
        return bytes;
    }

    @Override
    public int read(ByteBuffer buffer, long offset) throws IOException {
        return channel.read(buffer, offset);
    }

    @Override
    public int read(ByteBuffer buffer) throws IOException {
        return channel.read(buffer);
    }
}
