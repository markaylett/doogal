package org.doogal.core.util;

import static org.doogal.core.util.FileUtil.newBufferedReader;

import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StreamTokenizer;
import java.util.ArrayList;
import java.util.List;

import javax.mail.internet.ParseException;

public final class Shellwords {
    private final StreamTokenizer tokeniser;
    private boolean eof;

    private static StreamTokenizer newTokenizer(Reader reader) {
        final StreamTokenizer st = new StreamTokenizer(reader);
        st.resetSyntax();
        st.commentChar('#');
        st.eolIsSignificant(true);
        // st.parseNumbers();
        st.quoteChar('"');
        st.quoteChar('\'');
        st.slashSlashComments(false);
        st.slashStarComments(false);
        st.whitespaceChars(0, ' ');
        st.wordChars('0', '9');
        st.wordChars('A', 'Z');
        st.wordChars('a', 'z');
        // FIXME: handle backslash escape sequences.
        final String punct = "!$%&()*+,-./:;<=>?@[\\]^_`{|}~\\";
        for (int i = 0; i < punct.length(); i++) {
            final char ch = punct.charAt(i);
            st.wordChars(ch, ch);
        }
        return st;
    }

    Shellwords(Reader reader) {
        tokeniser = newTokenizer(reader);
        eof = false;
    }

    final List<Object> readLine() throws EvalException, IOException,
            ParseException {

        // Parse the file
        final List<Object> toks = new ArrayList<Object>();
        boolean eol = false;
        do {
            final int token = tokeniser.nextToken();
            switch (token) {
            case StreamTokenizer.TT_NUMBER:
                toks.add(new Double(tokeniser.nval));
                break;
            case StreamTokenizer.TT_WORD:
                toks.add(tokeniser.sval);
                break;
            case '"':
                // Double-quoted string.
                toks.add(tokeniser.sval);
                break;
            case '\'':
                // Single-quoted string.
                toks.add(tokeniser.sval);
                break;
            case StreamTokenizer.TT_EOF:
                eof = true;
            case StreamTokenizer.TT_EOL:
                eol = true;
                break;
            default:
                // Unexpected character.
                final char ch = (char) tokeniser.ttype;
                throw new ParseException("unexpected character '" + ch + "'");
            }
        } while (!eol);
        return toks;
    }

    final boolean isEof() {
        return eof;
    }

    public static List<Object> readLine(Reader reader) throws EvalException,
            IOException, ParseException {
        return new Shellwords(reader).readLine();
    }

    public static void parse(Reader reader, Interpreter interp)
            throws EvalException, IOException, ParseException {
        final Shellwords sw = new Shellwords(reader);
        do {
            final List<Object> toks = sw.readLine();
            if (!toks.isEmpty()) {
                final String name = toks.get(0).toString();
                toks.remove(0);
                interp.eval(name, toks.toArray());
            } else
                interp.eval();
        } while (!sw.isEof());
    }

    public static void parse(InputStream in, Interpreter interp)
            throws EvalException, IOException, ParseException {
        parse(newBufferedReader(in), interp);
    }

    public static void main(String[] args) throws EvalException, IOException,
            ParseException {
        parse(System.in, new Interpreter() {
            public final void eval(String cmd, Object... args) {
                System.out.printf("[%s]\n", cmd);
                for (final Object arg : args)
                    System.out.printf("[%s]\n", arg);
            }

            public final void eval() {
            }
        });
    }
}
