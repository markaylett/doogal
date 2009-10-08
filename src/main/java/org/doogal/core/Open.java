package org.doogal.core;

import static org.doogal.core.Utility.copyTempFile;
import static org.doogal.core.Utility.firstFile;
import static org.doogal.core.Utility.getId;
import static org.doogal.core.Utility.newBufferedReader;

import java.io.BufferedReader;
import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.doogal.core.view.View;

final class Open {

    static void exec(View view, SharedState state, Term term) throws Exception {

        final IndexReader reader = state.getIndexReader();
        final File file = firstFile(reader, state.getData(), term);
        if (null == file)
            throw new EvalException("no such document");

        final String id = getId(file);
        final File tmp = copyTempFile(file, state.getTmp());
        try {

            final FileStats stats = new FileStats(tmp);
            final Process p = new ProcessBuilder(state.getEditor(), tmp
                    .getAbsolutePath()).start();
            if (0 != p.waitFor()) {
                // Dump error stream to output.
                final BufferedReader err = newBufferedReader(p.getErrorStream());
                for (;;) {
                    final String line = err.readLine();
                    if (null == line )
                        break;
                    view.getOut().println(line);
                }
                throw new EvalException("editor failed");
            }

            if (stats.hasFileChanged()) {
                view.getLog().info("indexing document...");
                final IndexWriter writer = new IndexWriter(state.getIndex(),
                        new StandardAnalyzer(), false,
                        IndexWriter.MaxFieldLength.LIMITED);
                try {
                    if (file.exists()) {
                        Tidy.tidy(tmp, file);
                        Rfc822.updateDocument(writer, state.getData(), file);
                    } else
                        writer.deleteDocuments(new Term("id", id));
                } finally {
                    writer.optimize();
                    writer.close();
                }
            } else
                view.getLog().info("no change...");

        } finally {
            tmp.delete();
        }
        state.addRecent(id);
    }
}
