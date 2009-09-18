package org.doogal;

import java.io.IOException;
import java.io.PrintWriter;

interface Results {
	void close() throws IOException;

	void print(PrintWriter out, int i) throws IOException;

	int size();
}
