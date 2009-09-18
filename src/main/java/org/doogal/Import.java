package org.doogal;

import static org.doogal.Utility.*;

import java.io.File;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.index.IndexWriter;

final class Import {

	private static File importFile(SharedState state, File from)
			throws Exception {

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
			listFiles(new File(state.getInbox()), new Predicate<File>() {
				public final boolean call(File file) {
					if (ignore(file))
						return true;
					try {
						file = importFile(state, file);
						final int id = state.getLocal(getId(file));
						System.out.printf("indexing document %d...\n", id);
						Rfc822.addDocument(writer, state.getData(), file);
						state.addRecent(id);
					} catch (final Exception e) {
						System.err.println("Error: " + file + ": " + e);
					}
					return true;
				}
			});
		} finally {
			writer.optimize();
			writer.close();
		}
	}
}
