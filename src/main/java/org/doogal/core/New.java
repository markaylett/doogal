package org.doogal.core;

import static org.doogal.core.Utility.copyFile;
import static org.doogal.core.Utility.newId;
import static org.doogal.core.Utility.subdir;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

final class New {

    static void exec(SharedState state, String template) throws Exception {
        File file = subdir(state.getData());
        final String id = newId();
        file = new File(file, id + ".txt");

        final File templ = new File(state.getTemplate(), template + ".txt");
        if (templ.canRead()) {
            boolean done = false;
            try {
                copyFile(templ, file);
                done = true;
            } finally {
                if (!done)
                    file.delete();
            }
        }

        final FileStats stats = new FileStats(file);

        final Process p = new ProcessBuilder(state.getEditor(), file
                .getAbsolutePath()).start();
        p.waitFor();

        if (!stats.hasFileChanged()) {
            state.log.info("discarding...");
            file.delete();
            return;
        }

        final IndexWriter writer = new IndexWriter(state.getIndex(),
                new StandardAnalyzer(), false,
                IndexWriter.MaxFieldLength.LIMITED);
        final int lid = state.getLocal(id);
        try {
            state.log.info(String.format("indexing document %d...\n", lid));
            Rfc822.addDocument(writer, state.getData(), file);
        } finally {
            writer.optimize();
            writer.close();
        }
        state.addRecent(id);
    }

    static void exec(SharedState state) throws Exception {
        exec(state, "plain");
    }
}
