package org.doogal;

import static org.doogal.Utility.getId;
import static org.doogal.Utility.ignore;
import static org.doogal.Utility.listFiles;
import static org.doogal.Utility.newId;
import static org.doogal.Utility.renameFile;
import static org.doogal.Utility.subdir;

import java.io.File;
import java.io.IOException;

import javax.mail.MessagingException;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

final class Import {

    private static File importFile(SharedState state, File from)
            throws IOException {

        File to = subdir(state.getData());
        final String id = newId();
        to = new File(to, id + ".txt");
        renameFile(from, to);
        return to;
    }

    static void exec(final SharedState state) throws Exception {
        final IndexWriter writer = new IndexWriter(state.getIndex(),
                new StandardAnalyzer(), false,
                IndexWriter.MaxFieldLength.LIMITED);
        try {
            listFiles(new File(state.getIncoming()), new Predicate<File>() {
                public final boolean call(File file) throws IOException,
                        MessagingException {
                    if (ignore(file))
                        return true;
                    file = importFile(state, file);
                    final String id = getId(file);
                    final int lid = state.getLocal(getId(file));
                    state.log.info(String
                            .format("indexing document %d...", lid));
                    Rfc822.addDocument(writer, state.getData(), file);
                    state.addRecent(id);
                    return true;
                }
            });
        } finally {
            writer.optimize();
            writer.close();
        }
    }
}
