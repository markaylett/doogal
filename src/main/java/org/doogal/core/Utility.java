package org.doogal.core;

import static org.doogal.core.util.FileUtil.newBufferedReader;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.channels.FileChannel;
import java.util.Calendar;
import java.util.TimeZone;
import java.util.UUID;

import javax.mail.MessagingException;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.doogal.core.util.FileStats;
import org.doogal.core.util.Predicate;

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

    static boolean edit(String editor, File path, PrintWriter err)
            throws InterruptedException, IOException {
        final FileStats stats = new FileStats(path);
        final Process p = new ProcessBuilder(editor, path.getAbsolutePath())
                .start();
        if (0 != p.waitFor()) {
            // Dump error stream to output.
            final BufferedReader reader = newBufferedReader(p.getErrorStream());
            for (;;) {
                final String line = reader.readLine();
                if (null == line)
                    break;
                err.println(line);
            }
            throw new IOException("editor failed");
        }
        return stats.hasFileChanged();
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

    static File copyTempFile(File from, File dir, String id) throws IOException {
        final File tmp = File.createTempFile(id + "-", ".txt", dir);
        if (from.canRead()) {
            boolean done = false;
            try {
                copyFile(from, tmp);
                done = true;
            } finally {
                if (!done)
                    tmp.delete();
            }
        }
        return tmp;
    }

    static File copyTempFile(File from, File dir) throws IOException {
        return copyTempFile(from, dir, getId(from));
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

    static File subdir(File file) {
        final Calendar cal = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
        file = append(file, 4, cal.get(Calendar.YEAR));
        file = append(file, 2, cal.get(Calendar.MONTH) + 1);
        return append(file, 2, cal.get(Calendar.DAY_OF_MONTH));
    }

    static String toFileName(String s) {
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

    static File findEditor() {
        final String[] editors = new String[] { "emacsclient", "vi", "gedit",
                "TextMate", "TextPad.exe", "wordpad.exe", "notepad.exe" };
        final File[] found = new File[editors.length];
        final String[] dirs = System.getenv("PATH").split(File.pathSeparator);
        for (int i = 0; i < dirs.length; ++i) {
            final File dir = new File(dirs[i]);
            if (dir.isDirectory())
                for (int j = 0; j < editors.length; ++j) {
                    final File editor = new File(dir, editors[j]);
                    if (editor.exists() && null == found[j])
                        found[j] = editor;
                }
        }
        int i = 0;
        for (; i < found.length; ++i)
            if (null != found[i])
                break;
        return found[i];
    }
}
