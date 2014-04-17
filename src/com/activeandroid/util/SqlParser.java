
package com.activeandroid.util;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;


public class SqlParser {

    public final static int STATE_NONE          = 0;
    public final static int STATE_STRING        = 1;
    public final static int STATE_COMMENT       = 2;
    public final static int STATE_COMMENT_BLOCK = 3;

    public static List<String> parse(final InputStream stream) throws IOException {

        final BufferedInputStream buffer = new BufferedInputStream(stream);
        final List<String> commands = new ArrayList<String>();
        final StringBuffer sb = new StringBuffer();

        try {
            final Tokenizer tokenizer = new Tokenizer(buffer);
            int state = STATE_NONE;

            while (tokenizer.hasNext()) {
                final char c = (char) tokenizer.next();

                if (state == STATE_COMMENT_BLOCK) {
                    if (tokenizer.skip("*/")) {
                        state = STATE_NONE;
                    }
                    continue;

                } else if (state == STATE_COMMENT) {
                    if (isNewLine(c)) {
                        state = STATE_NONE;
                    }
                    continue;

                } else if (state == STATE_NONE && tokenizer.skip("/*")) {
                    state = STATE_COMMENT_BLOCK;
                    continue;

                } else if (state == STATE_NONE && tokenizer.skip("--")) {
                    state = STATE_COMMENT;
                    continue;

                } else if (state == STATE_NONE && c == ';') {
                    final String command = sb.toString().trim();
                    commands.add(command);
                    sb.setLength(0);
                    continue;

                } else if (state == STATE_NONE && c == '\'') {
                    state = STATE_STRING;

                } else if (state == STATE_STRING && c == '\'') {
                    state = STATE_NONE;

                }

                if (state == STATE_NONE || state == STATE_STRING) {
                    if (state == STATE_NONE && isWhitespace(c)) {
                        if (sb.length() > 0 && sb.charAt(sb.length() - 1) != ' ') {
                            sb.append(' ');
                        }
                    } else {
                        sb.append(c);
                    }
                }
            }

        } finally {
            IOUtils.closeQuietly(buffer);
        }

        if (sb.length() > 0) {
            commands.add(sb.toString().trim());
        }

        return commands;
    }

    private static boolean isNewLine(final char c) {
        return c == '\r' || c == '\n';
    }

    private static boolean isWhitespace(final char c) {
        return c == '\r' || c == '\n' || c == '\t' || c == ' ';
    }
}
