package org.doogal;

import static org.doogal.Constants.DEFAULT_EDITOR;
import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import static org.doogal.Utility.*;

final class Environment {
	private static interface Accessor {
		void reset() throws ResetException;

		void set(Object value) throws ResetException;

		Object get();
	}

	private final Map<String, Accessor> env;
	private String editor;
	private String repo;
	private String template;
	private String inbox;

	private static String defaultEditor() {
		final String s = System.getenv("EDITOR");
		return null == s ? DEFAULT_EDITOR : s;
	}

	private static String defaultRepo() {
		String s = System.getenv("DOOGAL_REPO");
		if (null == s)
			s = System.getenv("DOOGAL_HOME");
		if (null == s)
			s = System.getProperty("user.home") + File.separator + ".doogal";
		return s;
	}

	private final String defaultInbox() {
		return getPath(repo, "inbox");
	}

	private final String defaultTemplate() {
		return getPath(repo, "template");
	}

	Environment() {
		this.env = new TreeMap<String, Accessor>();
		this.editor = defaultEditor();
		this.repo = defaultRepo();
		this.template = null;
		this.inbox = null;

		this.env.put("editor", new Accessor() {

			public final Object get() {
				return getEditor();
			}

			public final void reset() {
				editor = defaultEditor();
			}

			public final void set(Object value) {
				editor = value.toString();
			}
		});
		this.env.put("repo", new Accessor() {

			public final Object get() {
				return getRepo();
			}

			public final void reset() throws ResetException {
				repo = defaultRepo();
				throw new ResetException();
			}

			public final void set(Object value) throws ResetException {
				repo = value.toString();
				throw new ResetException();
			}
		});
		this.env.put("inbox", new Accessor() {

			public final Object get() {
				return getInbox();
			}

			public final void reset() throws ResetException {
				inbox = null;
			}

			public final void set(Object value) throws ResetException {
				inbox = value.toString();
			}
		});
		this.env.put("template", new Accessor() {

			public final Object get() {
				return getTemplate();
			}

			public final void reset() throws ResetException {
				template = null;
			}

			public final void set(Object value) throws ResetException {
				template = value.toString();
			}
		});
	}

	final void reset(String name) throws NameException, ResetException {
		final Accessor acc = env.get(name);
		if (null == acc)
			throw new NameException("no such name");
		acc.reset();
	}

	final void set(String name, Object value) throws NameException,
			ResetException {
		final Accessor acc = env.get(name);
		if (null == acc)
			throw new NameException("no such name");
		acc.set(value);
	}

	final Object get(String name) throws NameException {
		final Accessor acc = env.get(name);
		if (null == acc)
			throw new NameException("no such name");
		return acc.get();
	}

	final String getEditor() {
		return editor;
	}

	final String getRepo() {
		return repo;
	}

	final String getInbox() {
		return null == inbox ? defaultInbox() : inbox;
	}

	final String getTemplate() {
		return null == template ? defaultTemplate() : template;
	}

	final String[] toArray() {
		final String[] arr = new String[env.size()];
		int i = 0;
		for (final Entry<String, Accessor> entry : env.entrySet())
			arr[i++] = String.format("%s=%s", entry.getKey(), entry.getValue()
					.get().toString());
		return arr;
	}
}