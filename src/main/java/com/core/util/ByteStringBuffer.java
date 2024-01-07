package com.core.util;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class ByteStringBuffer {
	private final ByteBuffer buffer;

	public ByteStringBuffer( int size )
	{
		buffer = ByteBuffer.allocate(size);		
	}
	
	public ByteStringBuffer()
	{
		this(4096);
	}

    public boolean isEmpty() {
    	return buffer.position() == 0 ; 
    }
    
    public ByteStringBuffer clear() {
        buffer.clear();
        return this;
    }

    public ByteBuffer getUnderlyingBuffer() {
        buffer.flip();
        return buffer;
    }

    public ByteBuffer getByteBuffer() {
        return buffer;
    }

    public ByteStringBuffer add(long num) {
        TextUtils.writeNumber(buffer, num);
        return this;
    }

    public ByteStringBuffer add(boolean b) {
        TextUtils.writeBool(buffer, b);
        return this;
    }

    public ByteStringBuffer add(short b) {
        TextUtils.writeShort(buffer, b);
        return this;
    }

    public ByteStringBuffer add(String str) {
        if (str == null) {
            BinaryUtils.copy(buffer, "<null>");
            return this;
        }

        BinaryUtils.copy(buffer, str);
        return this;
    }

    public ByteStringBuffer add(byte[] str) {
        buffer.put(str);
        return this;
    }

    public ByteStringBuffer add(ByteBuffer buf) {
        if (buf == null) {
            BinaryUtils.copy(buffer, "<null>");
            return this;
        }

        int position = buf.position();
        buffer.put(buf);
        buf.position(position);
        return this;
    }

    public ByteStringBuffer addNL() {
        buffer.put((byte) '\n');
        return this;
    }

    public ByteStringBuffer addComma() {
        buffer.put((byte)',');
        return this;
    }
    public ByteStringBuffer addColon() {
        buffer.put((byte)':');
        return this;
    }

    @Override
	public String toString() {
        int position = buffer.position();

        buffer.flip();
        String str = BinaryUtils.toString(buffer);

        buffer.position(position);
        buffer.limit(buffer.capacity());

        return str;
    }

    public ByteStringBuffer addPrice(long price, int impliedDecimals) {
        PriceUtils.writePrice(buffer, price, impliedDecimals);
        return this;
    }
}
