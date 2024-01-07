package com.core.fix.util;

import com.core.fix.FixParser;
import com.core.fix.InvalidFixMessageException;
import com.core.fix.tags.FixTag;
import com.core.fix.tags.GroupedStubFixTag;
import com.core.fix.tags.StubFixTag;
import com.gs.collections.impl.map.mutable.primitive.IntObjectHashMap;

import java.nio.ByteBuffer;

/**
 * Created by johnlevidy on 6/3/15.
 */
public class TestFixParser implements FixParser{
	private final IntObjectHashMap<FixTag> map = new IntObjectHashMap<>();
	
    @Override
    public int parse(ByteBuffer buffer) throws InvalidFixMessageException {
        return 0;
    }

    @Override
    public FixTag createReadWriteFIXTag(int id) {
        return createTag(id);
    }

    @Override
    public FixTag createWriteOnlyFIXTag(int id) {
        return createTag(id);
    }

    @Override
    public FixTag createReadWriteFIXGroupTag(int id) {
        FixTag stubFixTag = map.get(id);
        if( stubFixTag == null )
        {
            stubFixTag = new GroupedStubFixTag(id);
        }
        map.put(id, stubFixTag);
        return stubFixTag;
    }

    private FixTag createTag(int id) {
        FixTag stubFixTag = map.get(id);
        if( stubFixTag == null )
        {
            stubFixTag = new StubFixTag(id);
            ((StubFixTag)stubFixTag).setValue("");
        }
        map.put(id, stubFixTag);
        return stubFixTag;
    }

    public void writeTag(int tagID, String value)
    {
        FixTag fixTag = map.get(tagID);
        if (fixTag.getClass().equals(StubFixTag.class)) {
            ((StubFixTag) fixTag).setValue(value);
        }
        else if (fixTag.getClass().equals(GroupedStubFixTag.class)) {
            ((GroupedStubFixTag) fixTag).setValue(value);
        }
    }
    
    public void writeTag(int tagId, char value)
    {
    	writeTag(tagId, new String(new char[] { value }));
    }
    
    public void writeTag(int tagId, int value)
    {
        writeTag(tagId, Integer.toString(value));
    }
}
