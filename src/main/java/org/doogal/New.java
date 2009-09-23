package org.doogal;

import static org.doogal.Utility.copyFile;
import static org.doogal.Utility.newId;
import static org.doogal.Utility.subdir;

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
            System.out.println("discarding...");
            file.delete();
            return;
        }

        final IndexWriter writer = new IndexWriter(state.getIndex(),
                new StandardAnalyzer(), false,
                IndexWriter.MaxFieldLength.LIMITED);
        final int lid = state.getLocal(id);
        try {
            System.out.printf("indexing document %d...\n", lid);
            Rfc822.addDocument(writer, state.getData(), file);
        } finally {
            writer.optimize();
            writer.close();
        }
        state.addRecent(lid);
    }

    static void exec(SharedState state) throws Exception {
        exec(state, "plain");
    }
}
