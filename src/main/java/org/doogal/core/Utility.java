package org.doogal.core;

import static org.doogal.core.Constants.DATE_FORMAT;

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
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.doogal.core.table.Table;

public final class Utility {

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

    static boolean whileLine(String name, Predicate<String> pred)
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
                if (null == line || !pred.call(line))
                    break;
            }
        } finally {
            is.close();
        }
        return true;
    }

    public static boolean printResource(String name, final PrintWriter out)
            throws Exception {
        return whileLine(name, new Predicate<String>() {
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

    static InputStream openContents(File file) throws IOException {
        final InputStream is = new FileInputStream(file);
        boolean done = false;
        try {
            // Skip headers.
            new InternetHeaders(is);
            done = true;
        } catch (final MessagingException e) {
            throw new IOException(e.getLocalizedMessage());
        } finally {
            if (!done)
                is.close();
        }
        return is;
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

    static boolean whileFile(File dir, Predicate<File> pred) throws Exception {
        if (!dir.exists())
            throw new FileNotFoundException(dir.getPath());
        if (!dir.isDirectory())
            return pred.call(dir);

        for (final File f : dir.listFiles())
            if (!whileFile(f, pred))
                return false;
        return true;
    }

    static void whileDocument(IndexReader reader, Term term,
            Predicate<Document> pred) throws Exception {
        final TermDocs docs = reader.termDocs(term);
        try {
            while (docs.next()) {
                final Document doc = reader.document(docs.doc());
                if (!pred.call(doc))
                    break;
            }
        } finally {
            docs.close();
        }
    }

    static void whileFile(IndexReader reader, File dir, Term term,
            Predicate<File> pred) throws Exception {
        final TermDocs docs = reader.termDocs(term);
        try {
            while (docs.next()) {
                final Document doc = reader.document(docs.doc());
                if (!pred.call(new File(dir, doc.get("path"))))
                    break;
            }
        } finally {
            docs.close();
        }
    }

    static Document firstDocument(IndexReader reader, Term term)
            throws Exception {
        final FirstPredicate<Document> pred = new FirstPredicate<Document>();
        whileDocument(reader, term, pred);
        return pred.first;
    }

    static File firstFile(IndexReader reader, File dir, Term term)
            throws Exception {
        final FirstPredicate<File> pred = new FirstPredicate<File>();
        whileFile(reader, dir, term, pred);
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

    private static String getStringAt(Table table, int rowIndex, int columnIndex)
            throws IOException {
        final Object value = table.getValueAt(rowIndex, columnIndex);
        if (Date.class.isAssignableFrom(table.getColumnClass(columnIndex)))
            return DATE_FORMAT.format(value);

        return value.toString();
    }

    public static void printTable(Table table, int start, int end,
            PrintWriter out) throws IOException {
        final int rowCount = end - start;
        final int[] max = new int[table.getColumnCount()];
        final String[] head = new String[table.getColumnCount()];
        final String[][] body = new String[rowCount][table.getColumnCount()];
        for (int i = 0; i < rowCount; ++i)
            for (int j = 0; j < table.getColumnCount(); ++j) {
                final String value = getStringAt(table, start + i, j);
                max[j] = Math.max(max[j], value.length());
                body[i][j] = value;
            }
        final StringBuilder hf = new StringBuilder(" ");
        final StringBuilder bf = new StringBuilder(" ");
        int width = 0;
        for (int j = 0; j < table.getColumnCount(); ++j) {
            head[j] = table.getColumnName(j);
            max[j] = Math.max(max[j], head[j].length());
            width += max[j];

            if (0 < hf.length()) {
                ++width;
                hf.append(' ');
            }
            hf.append("%-");
            hf.append(max[j]);
            hf.append('s');

            if (0 < bf.length())
                bf.append(' ');
            if (Number.class.isAssignableFrom(table.getColumnClass(j))) {
                bf.append('%');
                bf.append(max[j]);
            } else if (j < table.getColumnCount() - 1) {
                bf.append("%-");
                bf.append(max[j]);
            } else
                // No padding if last column and left-aligned.
                bf.append("%");
            bf.append('s');
        }

        hf.append('\n');
        bf.append('\n');

        out.printf(hf.toString(), (Object[]) head);
        out.print(' ');
        for (int i = 0; i < width; ++i)
            out.print('-');
        out.println();
        for (int i = 0; i < rowCount; ++i)
            out.printf(bf.toString(), (Object[]) body[i]);
    }
}
