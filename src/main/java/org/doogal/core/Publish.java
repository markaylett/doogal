package org.doogal.core;

import static org.doogal.core.Utility.firstFile;
import static org.doogal.core.Utility.getId;
import static org.doogal.core.Utility.toName;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.Date;

import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.xhtml.XhtmlSinkFactory;
import org.apache.maven.doxia.parser.AbstractTextParser;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;

final class Publish {

    private static final class PlainParser extends AbstractTextParser {

        public final void parse(Reader source, Sink sink) throws ParseException {
            final BufferedReader reader = new BufferedReader(source);
            try {
                sink.head();
                sink.head_();
                sink.body();
                sink.paragraph();
                for (;;) {
                    final String line = reader.readLine();
                    if (null == line)
                        break;
                    sink.text(line);
                    sink.lineBreak();
                }
                sink.paragraph_();
                sink.body_();
            } catch (final IOException e) {
                throw new ParseException(e.getLocalizedMessage());
            } finally {
                sink.close();
            }
        }

    };

    private static String getFirst(InternetHeaders headers, String name) {
        final String[] values = headers.getHeader(name);
        return null == values || 0 == values.length ? null : values[0];
    }

    private static String getTitle(InternetHeaders headers, String def) {
        String s = getFirst(headers, "Name");
        if (null == s) {
            s = getFirst(headers, "Title");
            if (null == s) {
                s = getFirst(headers, "Subject");
                if (null == s)
                    s = def;
            }
        }
        return s;
    }

    // Content-Type: text/apt

    private static boolean isApt(InternetHeaders headers)
            throws javax.mail.internet.ParseException {
        final String type = getFirst(headers, "Content-Type");
        if (null != type) {
            final ContentType ct = new ContentType(type);
            if ("text".equalsIgnoreCase(ct.getPrimaryType())
                    && ("apt".equalsIgnoreCase(ct.getSubType()) || "wiki"
                            .equalsIgnoreCase(ct.getSubType())))
                return true;
        }
        return false;
    }

    private static void convert(SharedState state, String title,
            String[] authors, Date date, Reader contents, Parser parser,
            String outName) throws IOException, ParseException {

        final SinkFactory factory = new XhtmlSinkFactory();
        final Sink xhtml = factory.createSink(new File(state.getHtml()),
                outName);
        try {
            parser.parse(contents, new HeaderSink(title, authors, date, xhtml));
        } finally {
            xhtml.close();
        }
    }

    static void exec(SharedState state, Term term) throws Exception {

        final IndexReader reader = state.getIndexReader();
        final File file = firstFile(reader, state.getData(), term);
        if (null == file)
            throw new EvalException("no such document");

        final String id = getId(file);
        final InputStream is = new FileInputStream(file);
        try {

            final InternetHeaders headers = new InternetHeaders(is);
            final String title = getTitle(headers, id);
            final String name = toName(title);

            final Reader contents = new InputStreamReader(is, "UTF-8");
            final Parser parser = isApt(headers) ? new AptParser()
                    : new PlainParser();
            convert(state, title, headers.getHeader("author"), new Date(file
                    .lastModified()), contents, parser, name + ".html");

        } finally {
            is.close();
        }
        state.addRecent(id);
    }
}
