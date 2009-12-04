package org.doogal.notes.core;

import static org.doogal.notes.core.Utility.copyTempFile;
import static org.doogal.notes.core.Utility.edit;
import static org.doogal.notes.core.Utility.newId;
import static org.doogal.notes.core.Utility.subdir;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.doogal.notes.view.View;

final class New {

    static boolean exec(View view, SharedState state, String template)
            throws IOException, InterruptedException {

        final String id = newId();
        final File from = new File(state.getTemplate(), template + ".txt");
        final File tmp = copyTempFile(from, state.getTmp(), id);
        try {

            if (!edit(state.getEditor(), tmp, view.getOut())) {
                view.getLog().info("discarding...");
                return false;
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
        return true;
    }

    static boolean exec(View view, SharedState state)
            throws InterruptedException, IOException {
        return exec(view, state, "plain");
    }
}
