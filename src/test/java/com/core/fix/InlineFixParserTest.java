package com.core.fix;

import com.core.fix.msgs.FixConstants;
import com.core.fix.tags.InlineFixTag;
import com.core.fix.tags.InlineRepeatingGroupFixTag;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.nio.ByteBuffer;

/**
 * User: jgreco
 */
public class InlineFixParserTest {
    private InlineFixParser parser;

    @Before
    public void setup() {
        FixConstants.SOH = (byte)'|';
        parser = new InlineFixParser();
    }

    @SuppressWarnings("static-method")
    @Test
    public void testParseTag() throws InvalidFixMessageException {
        ByteBuffer tag = ByteBuffer.wrap("35=Foo|34=Bar".getBytes());
        Assert.assertEquals(35, InlineFixParser.parseTag(tag));
    }

    @SuppressWarnings("static-method")
    @Test(expected = InvalidFixMessageException.class)
    public void testThrowExceptionOnInvalidTag() throws InvalidFixMessageException {
        ByteBuffer tag = ByteBuffer.wrap("35A=Foo|34=Bar".getBytes());
        InlineFixParser.parseTag(tag);
    }

    @SuppressWarnings("static-method")
    @Test
    public void testThrowExceptionOnNoEndOfTagFound() throws InvalidFixMessageException {
        ByteBuffer tag = ByteBuffer.wrap("35353523".getBytes());
        Assert.assertEquals(-1, InlineFixParser.parseTag(tag));
    }

    @SuppressWarnings("static-method")
	@Test
    public void testAdvance() throws InvalidFixMessageException {
        ByteBuffer tag = ByteBuffer.wrap("35=Foo|34=Bar|33=So|".getBytes());

        InlineFixParser.advanceToNextTag(tag);
        Assert.assertEquals(34, InlineFixParser.parseTag(tag));

        InlineFixParser.advanceToNextTag(tag);
        Assert.assertEquals(33, InlineFixParser.parseTag(tag));
    }

    @SuppressWarnings("static-method")
    @Test
    public void testAdvanceTag() throws InvalidFixMessageException {
        ByteBuffer tag = ByteBuffer.wrap("35=Foo|34=Bar|33=So|".getBytes());

        InlineFixParser.advanceToNextTag(tag);
        Assert.assertEquals(34, InlineFixParser.parseTag(tag));

        InlineFixParser.advanceToNextTag(tag);
        Assert.assertEquals(33, InlineFixParser.parseTag(tag));
    }

    @Test
    public void testParseCompleteMessage() throws InvalidFixMessageException {
        byte[] bytes = "8=FIX.4.2|35=Foo|34=Bar|33=So|10=200|".getBytes();
        ByteBuffer tag = ByteBuffer.wrap(bytes);
        Assert.assertEquals(bytes.length, parser.parse(tag));
        Assert.assertEquals(0, tag.position());
    }

    @Test
    public void testFailToParseMissingTrailer() throws InvalidFixMessageException {
        byte[] bytes = "8=FIX.4.2|35=Foo|34=Bar|33=So|".getBytes();
        ByteBuffer tag = ByteBuffer.wrap(bytes);
        Assert.assertEquals(0, parser.parse(tag));
        Assert.assertEquals(0, tag.position());
    }

    @Test
    public void testParseMultipleMessages() throws InvalidFixMessageException {
        byte[] bytes = "8=FIX.4.2|35=Foo|10=200|8=FIX.4.2|35=Bar|10=200|8=FIX".getBytes();
        ByteBuffer tag = ByteBuffer.wrap(bytes);
        Assert.assertEquals(24, parser.parse(tag));
        Assert.assertEquals(0, tag.position());

        tag.position(tag.position() + 24);
        Assert.assertEquals(24, parser.parse(tag));
        Assert.assertEquals(24, tag.position());

        tag.position(tag.position() + 24);
        Assert.assertEquals(0, parser.parse(tag));
        Assert.assertEquals(48, tag.position());
    }

    @Test
    public void testParseMessageValues() throws InvalidFixMessageException {
        InlineFixTag fixTag = (InlineFixTag)parser.createReadWriteFIXTag(35);

        ByteBuffer tag = ByteBuffer.wrap("8=FIX.4.2|35=Foo|10=200|8=FIX.4.2|35=Bar|10=200|8=FIX".getBytes());

        int bytes = parser.parse(tag);
        Assert.assertTrue(fixTag.isPresent());
        Assert.assertEquals("Foo", fixTag.getValueAsString());

        tag.position(tag.position() + bytes);
        bytes = parser.parse(tag);
        Assert.assertTrue(fixTag.isPresent());
        Assert.assertEquals("Bar", fixTag.getValueAsString());

        tag.position(tag.position() + bytes);
        bytes = parser.parse(tag);
        Assert.assertFalse(fixTag.isPresent());
        Assert.assertNull(fixTag.getValueAsString());
    }

    @Test
    public void testParseEmptyTag() throws InvalidFixMessageException {
        InlineFixTag fixTag = (InlineFixTag)parser.createReadWriteFIXTag(35);

        ByteBuffer tag = ByteBuffer.wrap("8=FIX.4.2|35=Foo|10=200|8=FIX.4.2|35=|10=200|8=FIX".getBytes());

        int bytes = parser.parse(tag);
        Assert.assertTrue(fixTag.isPresent());
        Assert.assertEquals("Foo", fixTag.getValueAsString());

        tag.position(tag.position() + bytes);
        bytes = parser.parse(tag);
        Assert.assertFalse(fixTag.isPresent());
        Assert.assertNull(fixTag.getValueAsString());

        tag.position(tag.position() + bytes);
        bytes = parser.parse(tag);
        Assert.assertFalse(fixTag.isPresent());
        Assert.assertNull(fixTag.getValueAsString());
    }

    @Test
    public void testParseRepeatingGroup() throws InvalidFixMessageException {
        InlineRepeatingGroupFixTag fixTag = (InlineRepeatingGroupFixTag)parser.createReadWriteFIXGroupTag(55);

        byte[] bytes = "8=FIX.4.2|35=Foo|34=Bar|55=10Y|56=FOO|55=5Y|57=BAR|10=200|".getBytes();
        ByteBuffer tag = ByteBuffer.wrap(bytes);
        Assert.assertEquals(bytes.length, parser.parse(tag));
        Assert.assertEquals(0, tag.position());

        Assert.assertEquals(2, fixTag.getNumGroups());
        Assert.assertEquals(0, fixTag.getGroup());
        Assert.assertEquals("10Y", fixTag.getValueAsString());

        Assert.assertEquals(2, fixTag.getNumGroups());
        Assert.assertEquals(1, fixTag.getGroup());
        Assert.assertEquals("5Y", fixTag.getValueAsString());

        Assert.assertEquals(2, fixTag.getNumGroups());
        Assert.assertEquals(2, fixTag.getGroup());
    }
}
