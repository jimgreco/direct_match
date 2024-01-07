package com.core.connector.mold;



import com.core.util.datastructures.contracts.Linkable;
import com.core.util.pool.Poolable;

import java.nio.ByteBuffer;

/**
 * Created by hli on 9/22/15.
 */
public class LinkableByteBuffer implements Poolable<LinkableByteBuffer>, Linkable<LinkableByteBuffer>{
    private ByteBuffer value;
    private LinkableByteBuffer next;

    public LinkableByteBuffer() {
        this(50);
    }

    public LinkableByteBuffer(int size) {
        value = ByteBuffer.allocate(size);
    }

    public void copy(ByteBuffer buffer) {
        if (value.capacity() < buffer.remaining()) {
            value = ByteBuffer.allocate(Math.max(buffer.remaining(), 2 * value.capacity()));
        }

        value.clear();
        value.put(buffer);
        value.flip();
    }

    @Override
    public void clear() {
        value.clear();
    }


    @Override
    public LinkableByteBuffer next() {
        return next;
    }

    @Override
    public void setNext(LinkableByteBuffer next) {
        this.next=next;
    }
    public void setValue(ByteBuffer byteBuffer){
        this.value=byteBuffer;
    }

    public ByteBuffer getValue() {
        return value;
    }
}
