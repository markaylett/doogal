package org.doogal;

import static org.doogal.Utility.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.Date;

import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.maven.doxia.module.apt.AptParser;
import org.apache.maven.doxia.module.xhtml.XhtmlSinkFactory;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;

final class Export {

	private static String getFirst(InternetHeaders header, String name) {
		final String[] values = header.getHeader(name);
		return null == values || 0 == values.length ? null : values[0];
	}

	private static String getTitle(InternetHeaders header, File file) {
		String s = getFirst(header, "Title");
		if (null == s) {
			s = getFirst(header, "Subject");
			if (null == s) {
				s = getFirst(header, "Name");
				if (null == s)
					getId(file);
			}
		}
		return s;
	}

	private static void convert(SharedState state, String title,
			String[] authors, Date date, Reader contents, String outName)
			throws IOException, ParseException {

		final Parser parser = new AptParser();

		final SinkFactory sinkFactory = new XhtmlSinkFactory();
		final Sink sink = sinkFactory.createSink(new File(state.getOutgoing()),
				outName);
		try {
			parser.parse(contents, new HeaderSink(title, authors, date, sink));
		} finally {
			sink.close();
		}
	}

	static void exec(SharedState state, Term term) throws Exception {

		final IndexReader reader = state.getIndexReader();
		final File file = firstFile(reader, state.getData(), term);
		if (null == file) {
			System.err.println("no such document");
			return;
		}

		final InputStream is = new FileInputStream(file);
		try {

			String inFormat = null;

			final InternetHeaders header = new InternetHeaders(is);
			final String type = getFirst(header, "Content-Type");
			if (null != type) {
				final ContentType ct = new ContentType(type);
				if ("text".equalsIgnoreCase(ct.getPrimaryType()))
					inFormat = ct.getSubType().toLowerCase();
			}

			final String title = getTitle(header, file);
			final String name = toName(title);

			if ("wiki".equalsIgnoreCase(inFormat)) {
				final Reader contents = newBufferedReader(is);
				convert(state, getTitle(header, file), header
						.getHeader("author"), new Date(file.lastModified()),
						contents, name + ".html");
			} else
				copyFile(file, new File(state.getOutgoing(), name + ".txt"));

		} finally {
			is.close();
		}
		state.addRecent(state.getLocal(getId(file)));
	}
}
