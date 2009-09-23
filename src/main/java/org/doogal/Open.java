package org.doogal;

import static org.doogal.Utility.firstFile;
import static org.doogal.Utility.getId;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

final class Open {

    static void exec(SharedState state, Term term) throws Exception {

        final IndexReader reader = state.getIndexReader();
        final File file = firstFile(reader, state.getData(), term);
        if (null == file) {
            System.err.println("no such document");
            return;
        }
        final FileStats stats = new FileStats(file);

        final Process p = new ProcessBuilder(state.getEditor(), file
                .getAbsolutePath()).start();
        if (0 != p.waitFor()) {
            System.err.println("editor returned error");
            return;
        }

        final String id = getId(file);
        if (stats.hasFileChanged()) {
            System.out.println("indexing document...");
            final IndexWriter writer = new IndexWriter(state.getIndex(),
                    new StandardAnalyzer(), false,
                    IndexWriter.MaxFieldLength.LIMITED);
            try {
                if (file.exists())
                    Rfc822.updateDocument(writer, state.getData(), file);
                else
                    writer.deleteDocuments(new Term("id", id));
            } finally {
                writer.optimize();
                writer.close();
            }
        }
        state.addRecent(state.getLocal(id));
    }
}
