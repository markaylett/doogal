package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

final class Recent {
    final List<String> ids;

    Recent() {
        ids = new LinkedList<String>();
    }

    final void add(String id) {
        final int i = ids.indexOf(id);
        if (0 <= i)
            ids.remove(i);
        else if (PAGE_SIZE <= ids.size())
            ids.remove(ids.size() - 1);
        ids.add(0, id);
    }

    final void remove(String id) {
        final int i = ids.indexOf(id);
        if (0 <= i)
            ids.remove(i);
    }

    final DocumentSet asDataSet(SharedState state) throws EvalException,
            IOException {
        final SummarySet docSet = new SummarySet();
        for (final String id : ids) {
            final Term term = new Term("id", id);
            final TermDocs docs = state.termDocs(term);
            try {
                if (docs.next()) {
                    final int lid = state.getLocal(id);
                    final Document doc = state.doc(docs.doc());
                    docSet.add(new Summary(lid, doc));
                }
            } finally {
                docs.close();
            }
        }
        return docSet;
    }

    final String top() {
        return ids.isEmpty() ? null : ids.get(0);
    }
}
