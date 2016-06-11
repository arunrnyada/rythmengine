/**
 * Copyright (C) 2013-2016 The Rythm Engine project
 * for LICENSE and other details see:
 * https://github.com/rythmengine/rythmengine
 */
package org.rythmengine.internal.parser.build_in;

import org.rythmengine.Sandbox;
import org.rythmengine.exception.ParseException;
import org.rythmengine.internal.ICaretParserFactory;
import org.rythmengine.internal.IContext;
import org.rythmengine.internal.IDialect;
import org.rythmengine.internal.IParserFactory;
import org.rythmengine.logger.ILogger;
import org.rythmengine.logger.Logger;
import com.stevesoft.pat.Regex;

public abstract class CaretParserFactoryBase implements ICaretParserFactory {

    protected final static ILogger logger = Logger.get(IParserFactory.class);

    public String getCaret(IDialect dialect) {
        return dialect.a();
    }

    public static void raiseParseException(IContext ctx, String msg, Object... args) {
        throw new ParseException(ctx.getEngine(), ctx.getTemplateClass(), ctx.currentLine(), msg, args);
    }

    public static void checkRestrictedClass(IContext ctx, String s) {
        if (org.rythmengine.Rythm.insideSandbox()) {
            String s0 = Sandbox.hasAccessToRestrictedClasses(ctx.getEngine(), s);
            if (null != s0) {
                raiseParseException(ctx, "Access to restricted class [%s] is blocked in sandbox mode", s0);
            }
        }
    }

    // -- for testing purpose
    public static void p(int i, Regex r) {
        if (0 == i) {
            System.out.println(i + ": " + r.stringMatched());
        } else {
            System.out.println(i + ": " + r.stringMatched(i));
        }
    }

    public static void p(String s, Regex r) {
        if (r.search(s)) p(r);
    }

    public static void p(String s, Regex r, int max) {
        if (r.search(s)) p(r, max);
    }

    public static void p(Regex r, int max) {
        for (int i = 0; i < max; ++i) {
            p(i, r);
        }
    }

    public static void p(Regex r) {
        p(r, 6);
    }
}
