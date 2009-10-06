package org.doogal.core;

import static org.doogal.core.Utility.firstFile;
import static org.doogal.core.Utility.getId;

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
        final FileStats stats = new FileStats(file);

        final Process p = new ProcessBuilder(state.getEditor(), file
                .getAbsolutePath()).start();
        if (0 != p.waitFor())
            throw new EvalException("editor error");

        final String id = getId(file);
        if (stats.hasFileChanged()) {
            view.getLog().info("indexing document...");
            final IndexWriter writer = new IndexWriter(state.getIndex(),
                    new StandardAnalyzer(), false,
                    IndexWriter.MaxFieldLength.LIMITED);
            try {
                if (file.exists()) {
                    Tidy.exec(state.getTmp(), file);
                    Rfc822.updateDocument(writer, state.getData(), file);
                } else
                    writer.deleteDocuments(new Term("id", id));
            } finally {
                writer.optimize();
                writer.close();
            }
        } else
            view.getLog().info("no change...");
        state.addRecent(id);
    }
}
