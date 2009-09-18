package org.doogal;

import java.io.IOException;
import java.io.PrintWriter;

import static org.doogal.Constants.*;

final class Pager {
	private final Results results;
	private int start;
	private int end;

	Pager(Results results) throws IOException {
		this.results = results;
		this.start = 0;
		this.end = Math.min(results.size(), start + PAGE_SIZE);
	}

	public final void close() throws IOException {
		results.close();
	}

	public final void execGoto(String n) throws IOException {
		final int i = Math.max(Integer.valueOf(n) - 1, 0);
		if (i * PAGE_SIZE < results.size())
			start = i * PAGE_SIZE;
		else
			System.err.println("no such page");
	}

	public final void execList() throws IOException {

		end = Math.min(results.size(), start + PAGE_SIZE);

		if (0 < results.size()) {
			final int page = 1 + start / PAGE_SIZE;
			final int total = 1 + (results.size() - 1) / PAGE_SIZE;
			System.out.println("page " + page + " of " + total + ":");
		} else
			System.out.println("no results");

		final PrintWriter out = new PrintWriter(System.out);
		try {
			for (int i = start; i < end; i++)
				results.print(out, i);
		} finally {
			out.flush();
		}
	}

	public final void execNext() throws IOException {
		if (start + PAGE_SIZE < results.size())
			start += PAGE_SIZE;
	}

	public final void execPrev() throws IOException {
		start = Math.max(0, start - PAGE_SIZE);
	}
}
