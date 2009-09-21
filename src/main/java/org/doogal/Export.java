package org.doogal;

import static org.doogal.Utility.*;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
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

	static void convert(SharedState state, InternetHeaders header,
			Reader contents, String outName) throws IOException, ParseException {

		final Parser parser = new AptParser();

		final SinkFactory sinkFactory = new XhtmlSinkFactory();
		final Sink sink = sinkFactory.createSink(new File(state.getOutgoing()),
				outName);
		try {
			parser.parse(contents, new HeaderSink(header, sink));
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

			final InternetHeaders header = new InternetHeaders(is);
			final String[] types = header.getHeader("Content-Type");

			String inFormat = null;
			if (null != types && 0 < types.length) {
				final ContentType type = new ContentType(types[0]);
				if ("text".equalsIgnoreCase(type.getPrimaryType()))
					inFormat = type.getSubType().toLowerCase();
			}

			copyFile(file, new File(state.getOutgoing(), file.getName()));
			if ("wiki".equalsIgnoreCase(inFormat)) {
				final Reader contents = newBufferedReader(is);
				convert(state, header, contents, getId(file) + ".html");
			}

		} finally {
			is.close();
		}
	}
}
