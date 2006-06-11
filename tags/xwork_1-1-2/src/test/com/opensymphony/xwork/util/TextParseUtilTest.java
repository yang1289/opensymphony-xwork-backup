package com.opensymphony.xwork.util;

import com.opensymphony.xwork.XWorkTestCase;

import java.util.List;
import java.util.HashSet;
import java.util.Arrays;

/**
 * User: plightbo
 * Date: Aug 3, 2005
 * Time: 5:41:36 AM
 */
public class TextParseUtilTest extends XWorkTestCase {
    public void testTranslateVariables() {
        OgnlValueStack stack = new OgnlValueStack();

        Object s = TextParseUtil.translateVariables("foo: ${{1, 2, 3}}, bar: ${1}", stack);
        assertEquals("foo: [1, 2, 3], bar: 1", s);

        s = TextParseUtil.translateVariables("foo: ${#{1 : 2, 3 : 4}}, bar: ${1}", stack);
        assertEquals("foo: {1=2, 3=4}, bar: 1", s);

        s = TextParseUtil.translateVariables("foo: 1}", stack);
        assertEquals("foo: 1}", s);

        s = TextParseUtil.translateVariables("foo: {1}", stack);
        assertEquals("foo: {1}", s);

        s = TextParseUtil.translateVariables("foo: ${1", stack);
        assertEquals("foo: ${1", s);

        s =  TextParseUtil.translateVariables('$', "${{1, 2, 3}}", stack, Object.class);
        assertTrue("List not returned when parsing a 'pure' list", s instanceof List);

        s =  TextParseUtil.translateVariables('$', "${1} two ${3}", stack, Object.class);
        assertEquals("1 two 3", s);

        s = TextParseUtil.translateVariables('$', "count must be between ${123} and ${456}, current value is ${98765}.", stack, Object.class);
        assertEquals("count must be between 123 and 456, current value is 98765.", s);
    }

    public void testCommaDelimitedStringToSet() {
        assertEquals(0, TextParseUtil.commaDelimitedStringToSet("").size());
        assertEquals(new HashSet(Arrays.asList(new String[] { "foo", "bar", "tee" })),
                TextParseUtil.commaDelimitedStringToSet(" foo, bar,tee"));
    }
}