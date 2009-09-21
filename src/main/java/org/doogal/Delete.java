package org.doogal;

import static org.doogal.Utility.getId;
import static org.doogal.Utility.listFiles;
import static org.doogal.Utility.renameFile;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;

final class Delete {

	static void exec(final SharedState state, Term term) throws Exception {
		final IndexReader reader = state.getIndexReader();
		listFiles(reader, state.getData(), term, new Predicate<File>() {
			public final boolean call(File file) throws Exception {
				final File trash = new File(state.getTrash(), file.getName());
				trash.delete();
				renameFile(file, trash);
				state.removeRecent(state.getLocal(getId(file)));
				return true;
			}
		});

		final IndexWriter writer = new IndexWriter(state.getIndex(),
				new StandardAnalyzer(), false,
				IndexWriter.MaxFieldLength.LIMITED);
		try {
			writer.deleteDocuments(term);
		} finally {
			writer.optimize();
			writer.close();
		}
	}
}
