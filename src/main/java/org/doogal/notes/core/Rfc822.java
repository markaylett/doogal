package org.doogal.notes.core;

import static org.doogal.notes.core.Utility.getId;
import static org.doogal.notes.core.Utility.getRelativePath;
import static org.doogal.notes.core.Utility.isEmpty;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import javax.mail.Header;
import javax.mail.MessagingException;
import javax.mail.internet.HeaderTokenizer;
import javax.mail.internet.InternetHeaders;
import javax.mail.internet.ParseException;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

final class Rfc822 {

    private static Collection<String> splitRfc822(String value, String delims)
            throws ParseException {
        final Collection<String> toks = new ArrayList<String>();
        final HeaderTokenizer ht = new HeaderTokenizer(value, delims, true);
        for (;;) {
            final HeaderTokenizer.Token tok = ht.next();
            if (HeaderTokenizer.Token.EOF == tok.getType())
                break;
            toks.add(tok.getValue());
        }
        return toks;
    }

    private static String addStandard(Document doc, File dir, File file)
            throws IOException {
        final String id = getId(file);
        doc.add(new Field("id", id, Field.Store.YES, Field.Index.NOT_ANALYZED));
        doc.add(new Field("modified", DateTools.timeToString(file
                .lastModified(), DateTools.Resolution.MINUTE), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        doc.add(new Field("path", getRelativePath(dir, file), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
        return id;
    }

    @SuppressWarnings("unchecked")
    private static void addFields(Document doc, InputStream is)
            throws MessagingException {
        final InternetHeaders headers = new InternetHeaders(is);
        final List<Header> l = Collections.<Header> list(headers
                .getAllHeaders());
        for (final Header h : l) {
            final String name = h.getName().toLowerCase();
            // Ignore reserved names.
            if ("id".equals(name) || "modified".equals(name)
                    || "path".equals(name))
                continue;
            final String value = h.getValue();
            if (isEmpty(value))
                continue;
            if ("subject".equals(name) || "title".equals(name))
                doc.add(new Field(name, value, Field.Store.YES,
                        Field.Index.ANALYZED));
            else {

                // FIXME: the following search returns no results unless a
                // wild-card is appended:
                // > s content-type:text/plain

                final Collection<String> toks = splitRfc822(value,
                        HeaderTokenizer.RFC822);
                for (final String tok : toks)
                    doc.add(new Field(name, tok.toLowerCase(), Field.Store.YES,
                            Field.Index.NOT_ANALYZED));
            }
        }
    }

    private static void addContentLength(Document doc, FileInputStream is)
            throws IOException {
        final FileChannel channel = is.getChannel();
        final long l = channel.size() - channel.position();
        doc.removeFields("content-length");
        doc.add(new Field("content-length", String.valueOf(l), Field.Store.YES,
                Field.Index.NOT_ANALYZED));
    }

    static final void addDocument(IndexWriter writer, File dir, File file)
            throws IOException {
        final Document doc = new Document();
        final FileInputStream is = new FileInputStream(file);
        try {
            addStandard(doc, dir, file);
            addFields(doc, is);
            addContentLength(doc, is);
            doc.add(new Field("contents", new InputStreamReader(is)));
            writer.addDocument(doc);
        } catch (final MessagingException e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            is.close();
        }
    }

    static final void updateDocument(IndexWriter writer, File dir, File file)
            throws IOException {
        final Document doc = new Document();
        final FileInputStream is = new FileInputStream(file);
        try {
            final String id = addStandard(doc, dir, file);
            addFields(doc, is);
            addContentLength(doc, is);
            doc.add(new Field("contents", new InputStreamReader(is)));
            writer.updateDocument(new Term("id", id), doc);
        } catch (final MessagingException e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            is.close();
        }
    }
}
