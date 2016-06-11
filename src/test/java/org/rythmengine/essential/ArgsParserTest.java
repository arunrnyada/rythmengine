/**
 * Copyright (C) 2013-2016 The Rythm Engine project
 * for LICENSE and other details see:
 * https://github.com/rythmengine/rythmengine
 */
package org.rythmengine.essential;

import org.rythmengine.TestBase;
import org.junit.Test;

import java.util.Collections;

/**
 * Test @args parser
 */
public class ArgsParserTest extends TestBase {
    @Test
    public void testSimple() {
        t = "@args String s = \"\", int i\ns:@__getClass(s).getName(),i:@__getClass(i)";
        s = r(t);
        eq("s:java.lang.String,i:int");
    }
    
    @Test
    public void testArray() {
        t = "@args int[] a1, String[] a2\na1:@__getClass(a1).isArray(),a2:@__getClass(a2).isArray()";
        // note cannnot use new int[]{} here
        s = r(t, new int[]{}, new String[]{});
        eq("a1:true,a2:true");
    }
    
    @Test
    public void testFullFormat() {
        t = "@args() {Map<String, String> m\n, List<Integer> l,}@(m instanceof Map),@(l instanceof List)";
        s = r(t, Collections.emptyMap(), Collections.emptyList());
        eq("true,true");
    }
    
    @Test
    public void testDefaultValue() {
        t = "@args() {String s = \"foo\", int i = 3}@s @i";
        s = r(t);
        eq("foo 3");
    }
    
    @Test
    public void testPositionHolder() {
        t = "@args String @1, int @2;@(1)_@2";
        s = r(t, "s", 0);
        eq("s_0");
    }
    
    @Test
    public void testLineBreaks() {
        t = "abc\n@args String @1\nxyz@1";
        s = r(t, "s");
        eq("abc\nxyzs");
        t = "abc@args(){String @1}xyz@1";
        s = r(t, "s");
        eq("abcxyzs");
        t = "abc\n@args(){String @1}\nxyz@1";
        s = r(t, "s");
        eq("abc\nxyzs");
        t = "abc\n@args(){\nString @1\n}\nxyz@1";
        s = r(t, "s");
        eq("abc\nxyzs");
        t = "abc\n\t@args(){\nString @1\n}\nxyz@1";
        s = r(t, "s");
        eq("abc\nxyzs");
    }
    
    @Test
    public void testWithBlankInFront() {
        t = "\n</style>\n  @args String _cur_page @// layout template parameter\n@_cur_page";
        s = r(t, "s");
        eq("\n</style>  \ns");
    }

    public static void main(String[] args) {
        run(ArgsParserTest.class);
    }
}
