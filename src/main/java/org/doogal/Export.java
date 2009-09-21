package org.doogal;

import static org.doogal.Utility.firstFile;
import static org.doogal.Utility.getId;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.ContentType;
import javax.mail.internet.InternetHeaders;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.maven.doxia.parser.ParseException;
import org.apache.maven.doxia.parser.Parser;
import org.apache.maven.doxia.sink.Sink;
import org.apache.maven.doxia.sink.SinkFactory;
import org.codehaus.plexus.ContainerConfiguration;
import org.codehaus.plexus.DefaultContainerConfiguration;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.util.ReaderFactory;

final class Export {

	private static PlexusContainer newPlexusContainer(String repo)
			throws PlexusContainerException {
		final Map<String, String> context = new HashMap<String, String>();
		context.put("basedir", repo);

		final ContainerConfiguration config = new DefaultContainerConfiguration();
		config.setName("Doxia");
		config.setContext(context);

		return new DefaultPlexusContainer(config);
	}

	private static String extension(String format) {
		String ext;
		if ("apt".equalsIgnoreCase(format))
			ext = ".txt";
		else if ("confluence".equalsIgnoreCase(format))
			ext = ".txt";
		else if ("docbook".equalsIgnoreCase(format))
			ext = ".xml";
		else if ("fo".equalsIgnoreCase(format))
			ext = ".xml";
		else if ("itext".equalsIgnoreCase(format))
			ext = ".xml";
		else if ("latex".equalsIgnoreCase(format))
			ext = ".latex";
		else if ("rtf".equalsIgnoreCase(format))
			ext = ".rtf";
		else if ("twiki".equalsIgnoreCase(format))
			ext = ".txt";
		else if ("xdoc".equalsIgnoreCase(format))
			ext = ".xml";
		else if ("xhtml".equalsIgnoreCase(format))
			ext = ".html";
		else
			ext = "." + format;
		return ext;
	}

	static void convert(SharedState state, String inFormat, Reader in,
			String outFormat, String outName) throws ComponentLookupException,
			IOException, ParseException, PlexusContainerException {

		final PlexusContainer plexus = newPlexusContainer(state.getRepo());
		try {
			final Parser parser = (Parser) plexus.lookup(Parser.ROLE, inFormat);

			final SinkFactory sinkFactory = (SinkFactory) plexus.lookup(
					SinkFactory.ROLE, outFormat);
			final Sink sink = sinkFactory.createSink(new File(state
					.getOutgoing()), outName);
			try {
				parser.parse(in, new HeaderSink(sink));
			} finally {
				sink.close();
			}
		} finally {
			plexus.dispose();
		}
	}

	static void exec(SharedState state, String outFormat, Term term)
			throws Exception {

		final IndexReader reader = state.getIndexReader();
		final File file = firstFile(reader, state.getData(), term);
		if (null == file) {
			System.err.println("no such document");
			return;
		}

		final InputStream is = new FileInputStream(file);
		try {

			final InternetHeaders headers = new InternetHeaders(is);
			final String[] types = headers.getHeader("Content-Type");

			String inFormat = null;
			if (0 < types.length) {
				final ContentType type = new ContentType(types[0]);
				if ("text".equalsIgnoreCase(type.getPrimaryType()))
					inFormat = type.getSubType().toLowerCase();
			}
			if (null == inFormat)
				inFormat = "apt";

			final Reader body = ReaderFactory.newReader(is, "UTF-8");
			convert(state, inFormat, body, outFormat, getId(file)
					+ extension(outFormat));

		} finally {
			is.close();
		}
	}
}
