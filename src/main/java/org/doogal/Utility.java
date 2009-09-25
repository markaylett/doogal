package org.doogal;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.nio.channels.FileChannel;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

final class Utility {
    private static final DateFormat df = new SimpleDateFormat("dd-MMM-yy");

    private static final class FirstPredicate<T> implements Predicate<T> {
        T first = null;

        public final boolean call(T obj) {
            this.first = obj;
            return false; // First only.
        }
    }

    private static File append(File file, int w, int i) {
        file = new File(file, String.format("%0" + w + "d", i));
        if (!file.exists())
            file.mkdir();
        return file;
    }

    private Utility() {
    }

    static boolean eachLine(String name, Predicate<String> pred)
            throws Exception {
        final InputStream is = Utility.class.getClassLoader()
                .getResourceAsStream(name);
        if (null == is)
            return false;
        try {
            final BufferedReader reader = new BufferedReader(
                    new InputStreamReader(is));
            for (;;) {
                final String line = reader.readLine();
                if (null == line)
                    break;
                pred.call(line);
            }
        } finally {
            is.close();
        }
        return true;
    }

    static boolean printResource(String name, final PrintWriter out)
            throws Exception {
        return eachLine(name, new Predicate<String>() {
            public final boolean call(String arg) {
                out.println(arg);
                return true;
            }
        });
    }

    static void copyFile(File from, File to) throws IOException {
        final FileChannel is = new FileInputStream(from).getChannel();
        try {
            final FileChannel os = new FileOutputStream(to).getChannel();
            try {
                os.transferFrom(is, 0, is.size());
            } finally {
                os.close();
            }
        } finally {
            is.close();
        }
    }

    static void renameFile(File from, File to) throws IOException {
        if (!from.renameTo(to)) {
            boolean done = false;
            try {
                copyFile(from, to);
                done = true;
            } finally {
                // All or nothing.
                if (done)
                    from.delete();
                else
                    to.delete();
            }
        }
    }

    static boolean isEmpty(String s) {
        return null == s || 0 == s.trim().length();
    }

    static String join(Object... args) {
        final StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (final Object arg : args) {
            if (first)
                first = false;
            else
                sb.append(' ');
            sb.append(arg);
        }
        return sb.toString();
    }

    static String newId() {
        return UUID.randomUUID().toString();
    }

    static File getPath(File dir, Document doc) {
        return new File(dir, doc.get("path"));
    }

    static String getId(File file) {
        final String name = file.getName();
        final int n = name.lastIndexOf('.');
        return -1 == n ? name : name.substring(0, n);
    }

    static String getRelativePath(File dir, File file) throws IOException {

        String prefix = dir.getCanonicalPath();
        if (File.separatorChar != prefix.charAt(prefix.length() - 1))
            prefix = prefix + File.separatorChar;

        String path = file.getCanonicalPath();
        if (path.startsWith(prefix))
            path = path.substring(prefix.length());

        return path;
    }

    static String getPath(String dir, String name) {
        if (File.separatorChar != dir.charAt(dir.length() - 1))
            dir = dir + File.separatorChar;
        return dir + name;
    }

    static boolean ignore(File file) {
        final String name = file.getName().toLowerCase();
        return !name.endsWith(".txt");
    }

    static boolean listFiles(File dir, Predicate<File> pred) throws Exception {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getPath());
        if (!dir.isDirectory())
            return pred.call(dir);

        for (final File f : dir.listFiles())
            if (!listFiles(f, pred))
                return false;
        return true;
    }

    static boolean listDocuments(IndexReader reader, Term term,
            Predicate<Document> pred) throws Exception {
        final TermDocs docs = reader.termDocs(term);
        try {
            while (docs.next()) {
                final Document doc = reader.document(docs.doc());
                if (!pred.call(doc))
                    return false;
            }
        } finally {
            docs.close();
        }
        return true;
    }

    static boolean listFiles(IndexReader reader, File dir, Term term,
            Predicate<File> pred) throws Exception {
        final TermDocs docs = reader.termDocs(term);
        try {
            while (docs.next()) {
                final Document doc = reader.document(docs.doc());
                if (!pred.call(new File(dir, doc.get("path"))))
                    return false;
            }
        } finally {
            docs.close();
        }
        return true;
    }

    static Document firstDocument(IndexReader reader, Term term)
            throws Exception {
        final FirstPredicate<Document> pred = new FirstPredicate<Document>();
        listDocuments(reader, term, pred);
        return pred.first;
    }

    static File firstFile(IndexReader reader, File dir, Term term)
            throws Exception {
        final FirstPredicate<File> pred = new FirstPredicate<File>();
        listFiles(reader, dir, term, pred);
        return pred.first;
    }

    static BufferedReader newBufferedReader(InputStream in)
            throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(in, "UTF-8"));
    }

    static BufferedWriter newBufferedWriter(OutputStream out)
            throws UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
    }

    static File subdir(File file) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        file = append(file, 4, cal.get(Calendar.YEAR));
        file = append(file, 2, cal.get(Calendar.MONTH) + 1);
        return append(file, 2, cal.get(Calendar.DAY_OF_MONTH));
    }

    static String toName(String s) {
        final StringBuilder sb = new StringBuilder();
        final int len = s.length();
        boolean ws = false;
        for (int i = 0; i < len; ++i) {
            final char ch = s.charAt(i);
            if (Character.isLetterOrDigit(ch)) {
                sb.append(Character.toLowerCase(ch));
                ws = false;
            } else if ('_' == ch || Character.isWhitespace(ch))
                if (!ws) {
                    sb.append('_');
                    ws = true;
                }
        }
        return sb.toString();
    }

    static String toString(int id, Document doc) {

        String modified = doc.get("modified");
        try {
            // 14-Sep-09
            modified = df.format(DateTools.stringToDate(modified));
        } catch (final ParseException e) {
            // 20090914
            modified = " " + modified.substring(0, 8);
        }

        String display = doc.get("title");
        final String name = doc.get("name");

        if (null == display) {
            display = doc.get("subject");
            if (null == display) {
                display = name;
                if (null == display)
                    display = "Untitled";
            }
        }

        return null == name ? String.format("%5d %s %s", id, modified, display)
                : String.format("%5d %s %s [%s]", id, modified, display, name);
    }
}
