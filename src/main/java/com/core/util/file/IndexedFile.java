package com.core.util.file;

import com.core.util.log.Log;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class IndexedFile {
    private final Log log;

    private final ByteBuffer indexTemp = ByteBuffer.allocateDirect(8);

    private final File file;
    private final File index;
    private final String name;

    private int nextIndex;

    public IndexedFile(FileFactory fileFactory, Log log, String name) throws IOException {
        this.log = log;
        this.name = name;
        this.file = fileFactory.createFile(name + ".bin", "rw");
        this.index = fileFactory.createFile(name + ".idx", "rw");
    }

    public void write(ByteBuffer buffer) {
        try {// write the index
            indexTemp.clear();
            indexTemp.putLong(file.size());
            indexTemp.flip();
            index.write(indexTemp);

            // write the bytes
            int position = buffer.position();
            file.write(buffer);
            buffer.position(position);

            nextIndex++;
        } catch (Exception e) {
            log.error(log.log().add("Error writing file[").add(name).add("]").add(e));
        }
    }

    public int getNextIndex() {
        return nextIndex;
    }

    public int read(long i, ByteBuffer buffer) {
        return read(i, 1, buffer);
    }

    public int read(long readIndex, int numMessages, ByteBuffer buffer) {
        try {
            if (readIndex < 0)  {
                log.error(log.log().add("Invalid index ").add(readIndex).add(" for file[").add(name).add("]"));
                return 0;
            }
            if (numMessages <= 0) {
                log.error(log.log().add("Invalid number of messages ").add(numMessages).add(" for file[").add(name).add("]"));
                return 0;
            }

            long byteOffset0 = getByteOffset(readIndex);
            if (byteOffset0 == -1) {
                log.error(log.log().add("Start OffSet Neg"));

                return 0;
            }

            long byteOffsetN = getByteOffset(readIndex + numMessages);
            if (byteOffsetN == -1) {
                log.error(log.log().add("End OffSet Neg"));

                return 0;
            }

            int length = (int)Math.min(byteOffsetN - byteOffset0, buffer.remaining());

            if (length == 0) {
                log.error(log.log().add("Length=0"));

                return 0;
            }


            int oldLimit = buffer.limit();
            buffer.limit(buffer.position() + length);
            int bytesRead = file.read(buffer, byteOffset0);
            buffer.limit(oldLimit);

            return bytesRead;

        }
        catch (IOException e) {
            log.error(log.log().add("Error reading file[")
                    .add(name).add("] at index ")
                    .add(readIndex).add("\n").add(e));
            return 0;
        }
    }

    private long getByteOffset(long offsetIndex) throws IOException {
        long indexByteOffset = offsetIndex * 8;

        if (indexByteOffset >= this.index.size()) {
            return file.size();
        }

        indexTemp.clear();
        int bytesRead = this.index.read(indexTemp, indexByteOffset);
        indexTemp.flip();

        if (bytesRead < 8) {
            log.error(log.log().add("Invalid byte offset at ").add(offsetIndex).add(" for file[").add(name).add("]"));
            return -1;
        }

        long byteOffset = indexTemp.getLong();
        if (byteOffset == -1) {
            // nothing is stored
            return -1;
        }

        if (byteOffset < 0 || byteOffset > file.size() ) {
            log.error(log.log().add("Invalid byte offset at ").add(offsetIndex).add(" for file[").add(name).add("]"));
            return -1;
        }

        return byteOffset;
    }

    public void reset(int index) {
        nextIndex = index;
    }
}
