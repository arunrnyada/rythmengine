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

import com.greenlaw110.rythm.internal.CodeBuilder;
import com.greenlaw110.rythm.internal.IContext;
import com.greenlaw110.rythm.internal.IParser;
import com.greenlaw110.rythm.internal.Keyword;
import com.greenlaw110.rythm.internal.dialect.Rythm;
import com.greenlaw110.rythm.internal.parser.ParserBase;
import com.greenlaw110.rythm.internal.parser.RemoveLeadingLineBreakAndSpacesParser;
import com.greenlaw110.rythm.utils.S;
import com.greenlaw110.rythm.utils.TextBuilder;
import com.stevesoft.pat.Regex;

/**
 * Parse @tag [return-type] tagname(Type var,...) {template...}
 */
public class DefTagParser extends KeywordParserFactory {

    private static class DefTagToken extends BlockToken {
        String tagName;
        String signature;
        String retType;
        CodeBuilder.InlineTag tag;

        public DefTagToken(String tagName, String retType, String signature, String body, IContext context) {
            super("", context);
            this.retType = retType;
            this.tagName = tagName;
            this.signature = signature;
            tag = ctx.getCodeBuilder().defTag(tagName, retType, signature, body);
        }

        @Override
        public void openBlock() {
        }

        @Override
        public String closeBlock() {
            ctx.getCodeBuilder().endTag(tag);
            return "";
        }
    }

    @Override
    public Keyword keyword() {
        return Keyword.TAG;
    }

    public IParser create(IContext ctx) {
        return new RemoveLeadingLineBreakAndSpacesParser(ctx) {
            public TextBuilder go() {
                Regex r = reg(dialect());
                if (!r.search(remain())) {
                    raiseParseException("Error parsing @def, correct usage: @def tagName([arguments...])");
                }
                step(r.stringMatched().length());
                String retType = r.stringMatched(3);
                String tagName = r.stringMatched(6);
                String signature = r.stringMatched(7);
                if (null != retType && !"void".equals(retType)) {
                    r = new Regex("^(\\s*((?@{})))");
                    if (!r.search("{" + ctx().getRemain())) {
                        this.raiseParseException("code blocked expected after @def tag");
                    }
                    String s = r.stringMatched(1);
                    int curLine = ctx().currentLine();
                    ctx().step(s.length() - 1);
                    while (ctx().peek() != '}') ctx().step(-1);
                    s = r.stringMatched(2);
                    s = s.substring(1); // strip left "{"
                    s = s.substring(0, s.length() - 1); // strip right "}"
                    String[] lines = s.split("[\\n\\r]+");
                    int len = lines.length;
                    StringBuilder sb = new StringBuilder(s.length() * 2);
                    String lastLine = "";
                    for (int i = 0; i < len; ++i) {
                        String line = lines[i];
                        if (!S.isEmpty(line)) lastLine = line;
                        sb.append(line).append(" //line: ").append(curLine++).append("\n");
                    }
                    if (!lastLine.trim().endsWith(";")) sb.append(";");
                    return new DefTagToken(tagName, retType, signature, sb.toString(), ctx());
                }
                return new DefTagToken(tagName, retType, signature, null, ctx());
            }
        };
    }

    @Override
    protected String patternStr() {
        return "^%s%s\\s+(([_a-zA-Z][\\w_$]*(\\s*((?@<>)|(?@[])))?)\\s+)?([_a-zA-Z][\\w_$]*)\\s*((?@()))\\s*{\\s*\\r*\\n*";
    }

    public static void main(String[] args) {
        DefTagParser tp = new DefTagParser();
        Regex r = tp.reg(Rythm.INSTANCE);
        String s = "@tag Map<String, Map<String, Map<String, Long>>> myTag(String x, Map<String, Map<String, Map<String, Long>>> y) {\\n y.name: x\\n}";
        p(s, r, 9);
    }

}
