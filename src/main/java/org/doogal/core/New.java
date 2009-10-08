package org.doogal.core;

import static org.doogal.core.Utility.copyTempFile;
import static org.doogal.core.Utility.newBufferedReader;
import static org.doogal.core.Utility.newId;
import static org.doogal.core.Utility.subdir;

import java.io.BufferedReader;
import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.doogal.core.view.View;

final class New {

    static void exec(View view, SharedState state, String template)
            throws Exception {

        final String id = newId();
        final File from = new File(state.getTemplate(), template + ".txt");
        final File tmp = copyTempFile(from, state.getTmp(), id);
        try {

            final FileStats stats = new FileStats(tmp);
            final Process p = new ProcessBuilder(state.getEditor(), tmp
                    .getAbsolutePath()).start();
            if (0 != p.waitFor()) {
                // Dump error stream to output.
                final BufferedReader err = newBufferedReader(p.getErrorStream());
                for (;;) {
                    final String line = err.readLine();
                    if (null == line)
                        break;
                    view.getOut().println(line);
                }
                throw new EvalException("editor failed");
            }

            if (!stats.hasFileChanged()) {
                view.getLog().info("discarding...");
                return;
            }

            view.getLog().info(
                    String.format("indexing document %d...\n", state
                            .getLocal(id)));
            final IndexWriter writer = new IndexWriter(state.getIndex(),
                    new StandardAnalyzer(), false,
                    IndexWriter.MaxFieldLength.LIMITED);
            try {
                final File dir = subdir(state.getData());
                final File file = new File(dir, id + ".txt");
                Tidy.tidy(tmp, file);
                Rfc822.addDocument(writer, state.getData(), file);
            } finally {
                writer.optimize();
                writer.close();
            }

        } finally {
            tmp.delete();
        }
        state.addRecent(id);
    }

    static void exec(View view, SharedState state) throws Exception {
        exec(view, state, "plain");
    }
}
