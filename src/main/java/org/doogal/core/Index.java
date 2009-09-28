package org.doogal.core;

import static org.doogal.core.Utility.ignore;
import static org.doogal.core.Utility.listFiles;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

final class Index {

    private static void indexRepo(final Repo repo, final IndexWriter writer)
            throws Exception {

        listFiles(repo.getData(), new Predicate<File>() {
            public final boolean call(File file) throws IOException,
                    MessagingException {
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
