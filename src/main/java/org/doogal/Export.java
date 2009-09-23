package org.doogal;

import static org.doogal.Utility.copyFile;
import static org.doogal.Utility.firstFile;
import static org.doogal.Utility.getId;
import static org.doogal.Utility.newBufferedReader;
import static org.doogal.Utility.toName;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;

import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.xhtml.XhtmlSinkFactory;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;

final class Export {

    private static String getFirst(InternetHeaders headers, String name) {
        final String[] values = headers.getHeader(name);
        return null == values || 0 == values.length ? null : values[0];
    }

    private static String getTitle(InternetHeaders headers, String def) {
        String s = getFirst(headers, "Title");
        if (null == s) {
            s = getFirst(headers, "Subject");
            if (null == s) {
                s = getFirst(headers, "Name");
                if (null == s)
                    s = def;
            }
        }
        return s;
    }

    // Content-Type: text/wiki

    private static boolean isWiki(InternetHeaders headers)
            throws javax.mail.internet.ParseException {
        final String type = getFirst(headers, "Content-Type");
        if (null != type) {
            final ContentType ct = new ContentType(type);
            if ("text".equalsIgnoreCase(ct.getPrimaryType())
                    && "wiki".equalsIgnoreCase(ct.getSubType()))
                return true;
        }
        return false;
    }

    private static void convert(SharedState state, String title,
            String[] authors, Date date, Reader contents, String outName)
            throws IOException, ParseException {

        final Parser parser = new AptParser();

        final SinkFactory sinkFactory = new XhtmlSinkFactory();
        final Sink sink = sinkFactory.createSink(new File(state.getOutgoing()),
                outName);
        try {
            parser.parse(contents, new HeaderSink(title, authors, date, sink));
        } finally {
            sink.close();
        }
    }

    static void exec(SharedState state, Term term) throws Exception {

        final IndexReader reader = state.getIndexReader();
        final File file = firstFile(reader, state.getData(), term);
        if (null == file) {
            System.err.println("no such document");
            return;
        }

        final String id = getId(file);
        final InputStream is = new FileInputStream(file);
        try {

            final InternetHeaders headers = new InternetHeaders(is);
            final String title = getTitle(headers, id);
            final String name = toName(title);

            if (isWiki(headers)) {
                final Reader contents = newBufferedReader(is);
                convert(state, title, headers.getHeader("author"), new Date(
                        file.lastModified()), contents, name + ".html");
            } else
                copyFile(file, new File(state.getOutgoing(), name + ".txt"));

        } finally {
            is.close();
        }
        state.addRecent(state.getLocal(id));
    }
}
