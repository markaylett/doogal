package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;
import static org.doogal.core.Utility.openContents;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.mail.MessagingException;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocCollector;
import org.apache.lucene.search.highlight.Formatter;
import org.apache.lucene.search.highlight.Highlighter;
import org.apache.lucene.search.highlight.QueryScorer;
import org.apache.lucene.search.highlight.TokenGroup;

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

    private final Document find(Term term) throws IOException {
        for (int i = 0; i < hits.length; ++i) {
            final Document doc = state.doc(hits[i].doc);
            if (term.text().equals(doc.get(term.field())))
                return doc;
        }
        return null;
    }
    
    SearchResults(View view, SharedState state, Query query) throws IOException {
        this.view = view;
        this.state = state;
        this.query = query.rewrite(state.getIndexReader());
        // Collect first page.
        totalHits = Math.min(fetch(PAGE_SIZE), Constants.MAX_RESULTS);
        state.retain();
    }

    public final void close() throws IOException {
        state.release();
    }

    public final void what(Term term, PrintWriter out) throws IOException,
            MessagingException {

        final Document doc = find(term);
        if (null == doc)
            return;
        final Highlighter hl = new Highlighter(new Formatter() {
            public final String highlightTerm(String originalText,
                    TokenGroup tokenGroup) {
                return originalText;
            }
        }, new QueryScorer(query));

        final StringBuilder sb = new StringBuilder();
        final File file = new File(state.getData(), doc.get("path"));
        final InputStream is = openContents(file);
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"));
            for (;;) {
                String line = in.readLine();
                if (null == line)
                    break;
                line = line.trim();
                if (0 < line.length()) {
                    sb.append(line);
                    sb.append('\n');
                }
            }
        } finally {
            is.close();
        }

        final TokenStream ts = new StandardAnalyzer().tokenStream("contents",
                new StringReader(sb.toString()));
        final String[] ls = hl.getBestFragments(ts, sb.toString(), 3);
        out.println("highlights:");
        out.println(Utility.toString(state.getLocal(doc.get("id")), doc));
        out.println();
        if (0 < ls.length) {
            for (int j = 0; j < ls.length; ++j)
                out.println(ls[j]);
            out.println();
        }
    }

    public final Collection<Term> getTerms() throws IOException {
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

    public final String get(int i) throws IOException {

        if (hits.length <= i) {
            view.getLog().info("fetching documents...");
            fetch(totalHits);
        }

        final Document doc = state.doc(hits[i].doc);
        final int id = state.getLocal(doc.get("id"));
        return Utility.toString(id, doc);
    }

    public final int size() {
        return totalHits;
    }
}
