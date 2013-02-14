/* 
 * Copyright (C) 2013 The Rythm Engine project
 * Gelin Luo <greenlaw110(at)gmail.com>
 *
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/
package com.greenlaw110.rythm.internal.parser.build_in;

import com.greenlaw110.rythm.internal.IContext;
import com.greenlaw110.rythm.internal.IKeyword;
import com.greenlaw110.rythm.internal.IParser;
import com.greenlaw110.rythm.internal.Keyword;
import com.greenlaw110.rythm.internal.dialect.Rythm;
import com.greenlaw110.rythm.internal.parser.ParserBase;
import com.greenlaw110.rythm.utils.S;
import com.greenlaw110.rythm.utils.TextBuilder;
import com.stevesoft.pat.Regex;

import java.util.regex.Matcher;

/**
 * Parse @invoke("tagname", ...) {body}
 */
public class InvokeParser extends KeywordParserFactory {
    @Override
    public IKeyword keyword() {
        return Keyword.INVOKE;
    }

    @Override
    protected String patternStr() {
        return "(^%s(%s\\s*((?@()))\\s*)((\\.([_a-zA-Z][_a-zA-Z0-9]*)((?@())))*))";
    }

    @Override
    public IParser create(IContext ctx) {
        return new ParserBase(ctx) {
            @Override
            public TextBuilder go() {
                Regex r = reg(dialect());
                if (!r.search(remain())) {
                    raiseParseException("Error parsing @invoke statement. Correct usage: @invoke(\"tagname\", ...)");
                }
                String matched = r.stringMatched();
                step(matched.length());
                //boolean ignoreNonExistsTag = matched.indexOf("ignoreNonExistsTag") != -1;
                String invocation = r.stringMatched(3);
                invocation = S.stripBrace(invocation);
                // get tag name
                int pos = invocation.indexOf(",");
                String tagName, params;
                if (-1 == pos) {
                    tagName = invocation;
                    params = "";
                } else {
                    tagName = invocation.substring(0, pos);
                    params = invocation.substring(pos + 1);
                }
                String s = remain();
                Matcher m0 = InvokeTagParser.P_HEREDOC_SIMBOL.matcher(s);
                Matcher m1 = InvokeTagParser.P_STANDARD_BLOCK.matcher(s);
                if (m0.matches()) {
                    TextBuilder tb = InvokeTagParser.InvokeTagWithBodyToken.dynamicTagToken(tagName, params, r.stringMatched(4), ctx());
                    ctx().step(m0.group(1).length());
                    return tb;
                } else if (m1.matches()) {
                    TextBuilder tb = InvokeTagParser.InvokeTagWithBodyToken.dynamicTagToken(tagName, params, r.stringMatched(4), ctx());
                    ctx().step(m1.group(1).length());
                    return tb;
                } else {
                    return InvokeTagParser.InvokeTagWithBodyToken.dynamicTagToken(tagName, params, r.stringMatched(4), ctx());
                }
            }
        };
    }

    public static void main(String[] args) {
        Regex r = new InvokeParser().reg(Rythm.INSTANCE);
        //String s = "@invoke(\"hello.world\" + foo.bar(), foo.bar()).cache(\"1h\", foo).ignoreNonExistsTag() \nxyz";
        String s = "@invoke(\"testTagCacheTag\", foo.rint())";
        if (r.search(s)) {
            p(r, 6);
        }

        s = r.stringMatched(3);
        s = S.stripBrace(s);
        int pos = s.indexOf(",");
        System.out.println(s.substring(0, pos));
        System.out.println(s.substring(pos + 1));
    }
}
