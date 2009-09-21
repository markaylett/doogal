package org.doogal;

import static org.doogal.Constants.PAGE_SIZE;

import java.io.IOException;
import java.io.PrintWriter;

import org.apache.lucene.document.Document;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;

final class SearchResults implements Results {

	private final SharedState state;
	private final Query query;
	private final int totalHits;
	private ScoreDoc[] hits;

	private final int fetch(int numHits) throws IOException {
		final TopDocCollector collector = new TopDocCollector(numHits);
		state.search(query, collector);
		hits = collector.topDocs().scoreDocs;
		return collector.getTotalHits();
	}

	SearchResults(SharedState state, Query query) throws IOException {
		this.state = state;
		this.query = query;
		// Collect first page.
		totalHits = Math.min(fetch(PAGE_SIZE), Constants.PAGE_SIZE
				* Constants.MAX_PAGE);
		state.retain();
	}

	public final void close() throws IOException {
		state.release();
	}

	public final void print(PrintWriter out, int i) throws IOException {

		if (hits.length <= i) {
			System.out.println("fetching documents...");
			fetch(totalHits);
		}

		final Document doc = state.doc(hits[i].doc);
		final int id = state.getLocal(doc.get("id"));
		out.println(Utility.toString(id, doc));
	}

	public final int size() {
		return totalHits;
	}
}
