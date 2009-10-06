package org.doogal.core;

import static org.doogal.core.Constants.PAGE_SIZE;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.doogal.core.table.DocumentTable;
import org.doogal.core.table.SummaryTable;

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

    final DocumentTable asTable(SharedState state) throws EvalException,
            IOException {
        final SummaryTable table = new SummaryTable();
        for (final String id : ids) {
            final Term term = new Term("id", id);
            final TermDocs docs = state.termDocs(term);
            try {
                if (docs.next()) {
                    final int lid = state.getLocal(id);
                    final Document doc = state.doc(docs.doc());
                    table.add(new Summary(lid, doc));
                }
            } finally {
                docs.close();
            }
        }
        return table;
    }

    final String top() {
        return ids.isEmpty() ? null : ids.get(0);
    }
}
