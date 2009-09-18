package org.doogal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import static org.doogal.Utility.*;

public final class Main implements Interpreter {
	private final Session session;
	private final Map<String, Command> commands;
	private final int[] maxNames;

	private static Pager helpPager(String cmd, Command value) throws Exception {
		final List<String> ls = new ArrayList<String>();
		ls.add("NAME");
		ls.add(String.format(" s - %s", cmd, value.getDescription()));
		ls.add("");
		ls.add("SYNOPSIS");
		for (final Method method : value.getClass().getMethods()) {
			final Synopsis synopsis = method.getAnnotation(Synopsis.class);
			if (null != synopsis)
				ls.add(" " + synopsis.value());
		}
		ls.add("");
		ls.add("DESCRIPTION");
		eachLine(cmd + ".txt", new Predicate<String>() {
			public final boolean call(String arg) throws Exception {
				ls.add(" " + arg);
				return true;
			}

		});
		return new Pager(new ListResults(ls));
	}

	private final void put(String name, Command value) {
		commands.put(name, value);
		maxNames[value.getType().ordinal()] = Math.max(maxNames[value.getType()
				.ordinal()], name.length());
	}

	private final void setMaxName() {
		for (final Type type : Type.values())
			maxNames[type.ordinal()] = 0;
		for (final Entry<String, Command> entry : commands.entrySet()) {
			final String name = entry.getKey();
			final Command value = entry.getValue();
			maxNames[value.getType().ordinal()] = Math.max(maxNames[value
					.getType().ordinal()], name.length());
		}
	}

	private final String toHelp(String cmd, Command value) {
		final int max = maxNames[value.getType().ordinal()];
		return String.format("%" + max + "s - %s", cmd, value.getDescription());
	}

	Main(Session session) throws IllegalAccessException,
			IllegalArgumentException, InvocationTargetException {
		this.session = session;
		this.commands = new TreeMap<String, Command>();
		this.maxNames = new int[Type.values().length];
		final Method[] methods = Session.class.getMethods();
		for (int i = 0; i < methods.length; ++i) {
			final Method method = methods[i];
			final Builtin builtin = method.getAnnotation(Builtin.class);
			if (null != builtin)
				put(builtin.value(), (Command) method.invoke(session));
		}
		commands.put("alias", new AbstractBuiltin() {
			public final String getDescription() {
				return "alias an existing command";
			}

			@SuppressWarnings("unused")
			public final void exec() throws IOException {

				final List<String> ls = new ArrayList<String>();

				for (final Entry<String, Command> entry : commands.entrySet())
					if (Type.ALIAS == entry.getValue().getType())
						ls.add(toHelp(entry.getKey(), entry.getValue()));

				final Pager pager = new Pager(new ListResults(ls));
				Main.this.session.setPager(pager);
				pager.execList();
			}

			@SuppressWarnings("unused")
			@Synopsis("alias [hint]")
			public final void exec(String hint) throws IOException {

				hint = hint.toLowerCase();

				final List<String> ls = new ArrayList<String>();

				for (final Entry<String, Command> entry : commands.entrySet())
					if (Type.ALIAS == entry.getValue().getType()
							&& entry.getKey().startsWith(hint))
						ls.add(toHelp(entry.getKey(), entry.getValue()));

				final Pager pager = new Pager(new ListResults(ls));
				Main.this.session.setPager(pager);
				pager.execList();
			}

			@SuppressWarnings("unused")
			@Synopsis("alias name value")
			public final void exec(String alias, final String value)
					throws Exception {
				final List<Object> toks = Shellwords.readLine(new StringReader(
						value));
				final String name = toks.get(0).toString();
				toks.remove(0);
				put(alias, new AbstractAlias() {
					public final String getDescription() {
						return String.format("alias for '%s'", value);
					}

					public final void exec() throws Exception {
						Main.this.eval(name, toks.toArray());
					}

					public final void exec(Object... args) throws Exception {
						final Object[] all = new Object[toks.size()
								+ args.length];
						int i = 0;
						for (final Object tok : toks)
							all[i++] = tok;
						System
								.arraycopy(args, 0, all, toks.size(),
										args.length);
						Main.this.eval(name, all);
					}
				});
			}
		});
		put("unalias", new AbstractBuiltin() {
			public final String getDescription() {
				return "remove an alias";
			}

			@SuppressWarnings("unused")
			@Synopsis("unalias name")
			public final void exec(String name) {
				final Command cmd = commands.get(name);
				if (null != cmd && Type.ALIAS == cmd.getType()) {
					commands.remove(name);
					setMaxName();
				} else
					System.err.println("no such alias");
			}
		});
		put("help", new AbstractBuiltin() {
			public final String getDescription() {
				return "list commands with help";
			}

			@SuppressWarnings("unused")
			public final void exec() throws IOException {

				final List<String> ls = new ArrayList<String>();

				for (final Entry<String, Command> entry : commands.entrySet())
					if (Type.BUILTIN == entry.getValue().getType())
						ls.add(toHelp(entry.getKey(), entry.getValue()));

				final Pager pager = new Pager(new ListResults(ls));
				Main.this.session.setPager(pager);
				pager.execList();
			}

			@SuppressWarnings("unused")
			@Synopsis("help [hint]")
			public final void exec(String hint) throws Exception {

				hint = hint.toLowerCase();

				final List<String> ls = new ArrayList<String>();

				String last = null;
				for (final Entry<String, Command> entry : commands.entrySet())
					if (Type.BUILTIN == entry.getValue().getType()
							&& entry.getKey().startsWith(hint)) {
						ls.add(toHelp(entry.getKey(), entry.getValue()));
						last = entry.getKey();
					}

				Pager pager = null;
				if (1 == ls.size())
					pager = helpPager(last, commands.get(last));
				else
					pager = new Pager(new ListResults(ls));

				Main.this.session.setPager(pager);
				pager.execList();
			}
		});
	}

	@SuppressWarnings("unchecked")
	public final void eval(String cmd, Object... args) throws ExitException,
			ResetException {
		try {
			cmd = cmd.toLowerCase();
			Command value = commands.get(cmd);
			if (null == value) {
				final List<Entry<String, Command>> fuzzy = new ArrayList<Entry<String, Command>>();
				for (final Entry<String, Command> entry : commands.entrySet())
					if (entry.getKey().startsWith(cmd))
						fuzzy.add(entry);
				if (fuzzy.isEmpty()) {
					System.err.println("unknown command");
					return;
				}
				if (1 < fuzzy.size()) {
					Collections.sort(fuzzy,
							new Comparator<Entry<String, Command>>() {
								public final int compare(
										Entry<String, Command> lhs,
										Entry<String, Command> rhs) {
									return lhs.getKey().compareTo(rhs.getKey());
								}
							});
					System.out.println("ambiguous command:");
					for (final Entry<String, Command> entry : fuzzy)
						System.out.println("  " + entry.getKey());
					return;
				}
				final Entry<String, Command> entry = fuzzy.get(0);
				cmd = entry.getKey();
				value = entry.getValue();
			}
			final List<Class> types = new ArrayList<Class>();
			for (final Object arg : args)
				types.add(arg.getClass());

			session.update();
			try {
				final Method m = value.getClass().getMethod("exec",
						types.toArray(new Class[types.size()]));
				m.invoke(value, args);
			} catch (final NoSuchMethodException e) {
				final Method m = value.getClass().getMethod("exec",
						Object[].class);
				m.invoke(value, (Object) args);
			}

		} catch (final NoSuchMethodException e) {
			System.err.println("invalid arguments");
		} catch (final InvocationTargetException e) {
			final Throwable t = e.getCause();
			if (t instanceof ExitException)
				throw (ExitException) t;
			if (t instanceof ResetException)
				throw (ResetException) t;
			System.err.println(t.getLocalizedMessage());
		} catch (final Exception e) {
			System.err.println(e.getLocalizedMessage());
		}
	}

	public final void eval() {
	}

	public static void main(String[] args) throws Exception {

		printResource("motd.txt");

		final Environment env = new Environment();
		for (;;) {
			final Repo repo = new Repo(env.getRepo());
			repo.init();
			final Session s = new Session(env, repo);
			try {
				final Main m = new Main(s);
				final File conf = new File(repo.getEtc(), "doogal.conf");
				if (conf.canRead()) {
					final FileReader reader = new FileReader(conf);
					try {
						Shellwords.readLine(reader, m);
					} finally {
						reader.close();
					}
				}
				prompt();
				Shellwords.readLine(System.in, new Interpreter() {
					public final void eval(String cmd, Object... args)
							throws ExitException, ResetException {
						m.eval(cmd, args);
						prompt();
					}

					public final void eval() throws ExitException,
							ResetException {
						m.eval("next");
						prompt();
					}
				});
			} catch (final ExitException e) {
				System.out.println("exiting...");
				break;
			} catch (final ResetException e) {
				System.out.println("resetting...");
			} finally {
				s.close();
			}
		}
	}
}
