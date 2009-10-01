package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;

final class SearchResults implements Results {

    private final View view;
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

    SearchResults(View view, SharedState state, Query query) throws IOException {
        this.view = view;
        this.state = state;
        this.query = query;
        // Collect first page.
        totalHits = Math.min(fetch(PAGE_SIZE), Constants.MAX_RESULTS);
        state.retain();
    }

    public final void close() throws IOException {
        state.release();
    }

    public final String get(int i) throws IOException {

        if (hits.length <= i) {
            view.getLog().info("fetching documents...");
            fetch(totalHits);
        }

        final Document doc = state.doc(hits[i].doc);
        final int id = state.getLocal(doc.get("id"));
        return Utility.toString(id, doc);
    }

    public final Collection<Term> terms() throws IOException {
        if (hits.length < totalHits) {
            view.getLog().info("fetching documents...");
            fetch(totalHits);
        }
        final List<Term> ls = new ArrayList<Term>();
        for (int i = 0; i < hits.length; ++i) {
            final Document doc = state.doc(hits[i].doc);
            ls.add(new Term("id", doc.get("id")));
        }
        return ls;
    }

    public final int size() {
        return totalHits;
    }
}
