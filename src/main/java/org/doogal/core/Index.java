package org.doogal.core;

import static org.doogal.core.Utility.ignore;
import static org.doogal.core.Utility.whileFile;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.doogal.core.util.Predicate;

final class Index {

    private static void indexRepo(final Repo repo, final IndexWriter writer)
            throws Exception {

        whileFile(repo.getData(), new Predicate<File>() {
            public final boolean call(File file) throws IOException {
                if (!ignore(file))
                    Rfc822.addDocument(writer, repo.getData(), file);
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
