package org.doogal;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Collections;
import java.util.List;

final class ListResults implements Results {

	private final List<String> ls;

	ListResults(List<String> ls) {
		this.ls = ls;
	}

	public final void close() throws IOException {
	}

	public final void print(PrintWriter out, int i) throws IOException {
		final String s = ls.get(i);
		if (0 == s.length())
			out.println();
		else
			out.println(" " + s);
	}

	public final int size() {
		return ls.size();
	}

	public static final ListResults EMPTY = new ListResults(Collections
			.<String> emptyList());
}
