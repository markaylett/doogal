package org.doogal;

import static org.doogal.Constants.PAGE_SIZE;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import org.apache.lucene.document.Document;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;

final class Recent {
	final List<Integer> list;

	Recent() {
		list = new LinkedList<Integer>();
	}

	final void add(int id) {
		final int i = list.indexOf(id);
		if (0 <= i)
			list.remove(i);
		else if (PAGE_SIZE <= list.size())
			list.remove(list.size() - 1);
		list.add(0, id);
	}

	final void remove(int id) {
		final int i = list.indexOf(id);
		if (0 <= i)
			list.remove(i);
	}

	final String[] toArray(SharedState state) throws IdentityException,
			IOException {
		final String[] arr = new String[list.size()];
		int i = 0;
		for (final Integer id : list) {
			final Term term = new Term("id", state.getGlobal(id));
			final TermDocs docs = state.termDocs(term);
			try {
				if (docs.next()) {
					final Document doc = state.doc(docs.doc());
					arr[i++] = Utility.toString(id, doc);
				}
			} finally {
				docs.close();
			}
		}
		return arr;
	}

	final int top() {
		return list.isEmpty() ? 0 : list.get(0);
	}
}
