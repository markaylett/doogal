package org.doogal.notes.core;

import static org.doogal.notes.core.Utility.openContents;
import static org.doogal.notes.domain.Constants.PAGE_SIZE;
import static org.doogal.notes.table.TableUtil.printTable;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.StringReader;

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
import org.doogal.notes.domain.Constants;
import org.doogal.notes.domain.Summary;
import org.doogal.notes.table.AbstractTable;
import org.doogal.notes.table.DocumentTable;
import org.doogal.notes.table.SummaryTable;
import org.doogal.notes.view.View;

final class SearchTable extends AbstractTable implements DocumentTable {

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

    SearchTable(View view, SharedState state, Query query) throws IOException {
        this.view = view;
        this.state = state;
        this.query = query.rewrite(state.getIndexReader());
        // Collect first page.
        totalHits = Math.min(fetch(PAGE_SIZE), Constants.MAX_RESULTS);
        state.retain();
    }

    public final void destroy() {
        try {
            state.release();
        } catch (final IOException e) {
            view.getLog().error("destroy() failed", e);
        }
    }

    public final int getRowCount() {
        return totalHits;
    }

    public final Object getValueAt(int rowIndex, int columnIndex)
            throws IOException {

        if (hits.length <= rowIndex) {
            view.getLog().info("fetching documents...");
            fetch(totalHits);
        }

        final Document doc = state.doc(hits[rowIndex].doc);
        final int id = state.getLocal(doc.get("id"));
        return getValueAt(new Summary(id, doc), columnIndex);
    }

    public final int size() {
        return totalHits;
    }

    public final String peek(Term term, PrintWriter out) throws IOException {

        final Document doc = find(term);
        if (null == doc)
            return null;
        final String id = doc.get("id");
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
        out.println("best of document:");
        final SummaryTable table = new SummaryTable();
        table.add(new Summary(state.getLocal(id), doc));
        printTable(table, 0, 1, out);
        out.println();
        if (0 < ls.length) {
            for (int j = 0; j < ls.length; ++j) {
                final String line = ls[j].trim();
                if (0 < line.length())
                    out.println(line);
            }
            out.println();
        }
        return id;
    }

    public final Summary getSummary(int i) throws IOException {
        if (hits.length <= i) {
            view.getLog().info("fetching documents...");
            fetch(totalHits);
        }

        final Document doc = state.doc(hits[i].doc);
        final int id = state.getLocal(doc.get("id"));
        return new Summary(id, doc);
    }
}
