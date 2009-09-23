package org.doogal;

import static org.doogal.Utility.ignore;
import static org.doogal.Utility.listFiles;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

final class Index {

    private static void indexRepo(final Repo repo, final IndexWriter writer)
            throws Exception {

        listFiles(repo.getData(), new Predicate<File>() {
            public final boolean call(File file) {
                if (ignore(file))
                    return true;
                try {
                    Rfc822.addDocument(writer, repo.getData(), file);
                } catch (final Exception e) {
                    System.err.println("Error: " + file + ": " + e);
                }
                return true;
            }
        });
    }

    static void exec(Repo repo) throws Exception {
        final IndexWriter writer = new IndexWriter(repo.getIndex(),
                new StandardAnalyzer(), true,
                IndexWriter.MaxFieldLength.LIMITED);
        try {
            indexRepo(repo, writer);
        } finally {
            writer.optimize();
            writer.close();
        }
    }
}
