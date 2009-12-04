package org.doogal.notes.core;

import static org.doogal.notes.core.Utility.ignore;
import static org.doogal.notes.core.Utility.whileFile;

import java.io.File;
import java.io.IOException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.store.LockObtainFailedException;
import org.doogal.core.util.UnaryPredicate;

final class Index {

    private static void indexRepo(final Repo repo, final IndexWriter writer) throws IOException {

        whileFile(repo.getData(), new UnaryPredicate<File>() {
            public final boolean call(File file) throws IOException {
                if (!ignore(file))
                    Rfc822.addDocument(writer, repo.getData(), file);
                return true;
            }
        });
    }

    static void exec(Repo repo) throws IOException {
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
