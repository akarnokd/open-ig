/*
 * Copyright 2008-present, David Karnok & Contributors
 * The file is part of the Open Imperium Galactica project.
 *
 * The code should be distributed under the LGPL license.
 * See http://www.gnu.org/licenses/lgpl.html for details.
 */

package hu.openig.editors.ce;

import hu.openig.core.Pair;

import java.io.IOException;
import java.io.StreamTokenizer;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

/**
 * Tools for the campaign editor.
 * @author akarnokd, 2012.11.02.
 */
public final class CETools {
    /** Utility class. */
    private CETools() { }
    /**
     * Parse a filter definition string. The format is as follows:<br>
     * "Exact words" "including:colons" id:100 name:fighter*
     * @param filterStr the filter string
     * @return the list of fields and patterns to check
     */
    public static List<Pair<String, Pattern>> parseFilter(String filterStr) {
        List<Pair<String, Pattern>> result = new ArrayList<>();

        StreamTokenizer st = new StreamTokenizer(new StringReader(filterStr));
        st.slashSlashComments(false);
        st.slashStarComments(false);
        st.lowerCaseMode(true);
        st.wordChars('*', '*');
        st.wordChars('?', '?');
        st.wordChars('.', '.');
        st.wordChars('@', '@');
        st.wordChars('-', '-');
        st.wordChars('_', '_');
        st.quoteChar('"');

        List<String> tokens = new ArrayList<>();
        try {
            while (true) {
                int tok = st.nextToken();
                if (tok == StreamTokenizer.TT_EOF) {
                    break;
                } else
                if (tok == StreamTokenizer.TT_WORD || tok == '"') {
                    tokens.add(st.sval);
                } else {
                    tokens.add(String.valueOf((char)tok));
                }
            }
        } catch (IOException ex) {
            // ignored
        }
        for (int i = 0; i < tokens.size(); i++) {
            String key = tokens.get(i);
            if (i < tokens.size() - 1 && tokens.get(i + 1).equals(":")) {
                if (i < tokens.size() - 2) {
                    result.add(Pair.of(key, wildcardToRegex(tokens.get(i + 2))));
                    i += 2;
                } else {
                    result.add(Pair.of(key, wildcardToRegex("")));
                }
            } else {
                result.add(Pair.of("", wildcardToRegex(key)));
            }
        }

        return result;
    }
    /** Escape characters. */
    private static final String WR_ESCAPE = ".|+(){}[]^$\\";
    /**
     * Convert a wildcard representation into regular expression.
     * @param wildcard the wildcard string
     * @return the regular expression
     */
    public static Pattern wildcardToRegex(String wildcard) {
        StringBuilder result = new StringBuilder();
        result.append("\\b");
        for (int i = 0; i < wildcard.length(); i++) {
            char c = wildcard.charAt(i);
            if (c == '*') {
                result.append(".*");
            } else
            if (c == '?') {
                result.append('.');
            } else
            if (WR_ESCAPE.indexOf(c) >= 0) {
                result.append('\\').append(c);
            } else {
                result.append(c);
            }
        }
        result.append("\\b");
        return Pattern.compile(result.toString(), Pattern.CASE_INSENSITIVE);
    }
}
