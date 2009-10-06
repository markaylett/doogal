package org.doogal.core;

import static org.doogal.core.Utility.firstDocument;
import static org.doogal.core.Utility.openContents;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.doogal.core.view.View;

final class Peek {
    static void exec(View view, SharedState state, Term term) throws Exception {

        String id = view.peek(term);
        if (null != id) {
            state.addRecent(id);
            return;
        }

        final IndexReader reader = state.getIndexReader();
        final Document doc = firstDocument(reader, term);
        if (null == doc)
            throw new EvalException("no such document");

        id = doc.get("id");
        final InputStream is = openContents(new File(state.getData(), doc
                .get("path")));
        try {
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    is, "UTF-8"));
            view.getOut().println("head of document:");
            view.getOut().println(
                    new Summary(state.getLocal(id), doc).toString());
            view.getOut().println();
            int i = 0;
            while (i < 10) {
                String line = in.readLine();
                if (null == line)
                    break;
                line = line.trim();
                if (0 < line.length()) {
                    view.getOut().println(line.trim());
                    ++i;
                }
            }
            if (0 < i)
                view.getOut().println();
        } finally {
            is.close();
        }
        state.addRecent(id);
    }
}
