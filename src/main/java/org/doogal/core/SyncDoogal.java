package org.doogal.core;

import static org.doogal.core.Utility.whileLine;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
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

import javax.mail.internet.ParseException;

import org.doogal.core.view.View;

public final class SyncDoogal implements Doogal {
    private final View view;
    private final Controller controller;
    private final Model model;
    private final Map<String, Command> commands;
    private final int[] maxNames;
    private boolean interact;

    private static void printHelp(String cmd, Command value,
            final PrintWriter out) throws Exception {
        out.println("NAME");
        out.printf(" s - %s\n", cmd, value.getDescription());
        out.println();
        out.println("SYNOPSIS");
        for (final Method method : value.getClass().getMethods()) {
            final Synopsis synopsis = method.getAnnotation(Synopsis.class);
            if (null != synopsis)
                out.printf(" %s\n", synopsis.value());
        }
        out.println();
        out.println("DESCRIPTION");
        whileLine(cmd + ".txt", new Predicate<String>() {
            public final boolean call(String arg) {
                if (0 == arg.length())
                    out.println();
                else
                    out.printf(" %s\n", arg);
                return true;
            }

        });
        out.println();
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

    private SyncDoogal(final View view, final Controller controller,
            final Model model) throws IllegalAccessException,
            InvocationTargetException {
        this.view = view;
        this.controller = controller;
        this.model = model;
        this.commands = new TreeMap<String, Command>();
        this.maxNames = new int[Type.values().length];
        this.interact = true;

        final Method[] methods = Model.class.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            final Method method = methods[i];
            final Builtin builtin = method.getAnnotation(Builtin.class);
            if (null != builtin)
                put(builtin.value(), (Command) method.invoke(model));
        }
        commands.put("alias", new AbstractBuiltin() {
            public final String getDescription() {
                return "alias an existing command";
            }

            @SuppressWarnings("unused")
            public final void exec() throws EvalException, IOException {

                final List<String> ls = new ArrayList<String>();

                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.ALIAS == entry.getValue().getType())
                        ls.add(toHelp(entry.getKey(), entry.getValue()));

                final DataSet dataSet = new ListSet(ls);
                view.setDataSet(dataSet);
                view.showPage();
            }

            @SuppressWarnings("unused")
            @Synopsis("alias [hint]")
            public final void exec(String hint) throws EvalException,
                    IOException {

                hint = hint.toLowerCase();

                final List<String> ls = new ArrayList<String>();

                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.ALIAS == entry.getValue().getType()
                            && entry.getKey().startsWith(hint))
                        ls.add(toHelp(entry.getKey(), entry.getValue()));

                final DataSet dataSet = new ListSet(ls);
                view.setDataSet(dataSet);
                view.showPage();
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

                    public final void exec() throws EvalException {
                        final boolean orig = interact;
                        interact = false;
                        try {
                            SyncDoogal.this.eval(name, toks.toArray());
                        } finally {
                            interact = orig;
                        }
                    }

                    public final void exec(Object... args) throws EvalException {
                        final Object[] all = new Object[toks.size()
                                + args.length];
                        int i = 0;
                        for (final Object tok : toks)
                            all[i++] = tok;
                        System
                                .arraycopy(args, 0, all, toks.size(),
                                        args.length);
                        final boolean orig = interact;
                        interact = false;
                        try {
                            SyncDoogal.this.eval(name, all);
                        } finally {
                            interact = orig;
                        }
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
            public final void exec(String name) throws EvalException {
                final Command cmd = commands.get(name);
                if (null == cmd || Type.ALIAS != cmd.getType())
                    throw new EvalException("unknown alias");

                commands.remove(name);
                setMaxName();
            }
        });
        put("help", new AbstractBuiltin() {
            public final String getDescription() {
                return "list commands with help";
            }

            @SuppressWarnings("unused")
            public final void exec() throws EvalException, IOException {

                final List<String> ls = new ArrayList<String>();

                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.BUILTIN == entry.getValue().getType())
                        ls.add(toHelp(entry.getKey(), entry.getValue()));

                final DataSet dataSet = new ListSet(ls);
                view.setDataSet(dataSet);
                view.showPage();
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

                if (1 == ls.size()) {
                    printHelp(last, commands.get(last), view.getOut());
                    return;
                }

                final DataSet dataSet = new ListSet(ls);
                view.setDataSet(dataSet);
                view.showPage();
            }
        });

        put("quit", new AbstractBuiltin() {
            public final String getDescription() {
                return "exit application";
            }

            @SuppressWarnings("unused")
            @Synopsis("quit")
            public final void exec() throws ExitException {
                controller.exit(interact);
            }
        });
    }

    public static final Doogal newInstance(Environment env, View view,
            Controller controller, Repo repo) throws EvalException,
            IllegalAccessException, InvocationTargetException, IOException {
        final Model model = new Model(env, view, repo);
        Doogal doogal = null;
        try {
            doogal = new SyncDoogal(view, controller, model);
        } finally {
            if (null == doogal)
                model.close();
        }
        // Now owns the session.
        return doogal;
    }

    public static Doogal newInstance(Environment env, View view,
            Controller controller) throws Exception {
        final Repo repo = new Repo(env.getRepo());
        repo.init();
        return newInstance(env, view, controller, repo);
    }

    public final void close() {
        model.close();
    }

    @SuppressWarnings("unchecked")
    public final void eval(String cmd, Object... args) throws EvalException {
        try {
            cmd = cmd.toLowerCase();
            Command value = commands.get(cmd);
            if (null == value) {
                final List<Entry<String, Command>> fuzzy = new ArrayList<Entry<String, Command>>();
                for (final Entry<String, Command> entry : commands.entrySet())
                    if (entry.getKey().startsWith(cmd))
                        fuzzy.add(entry);
                if (fuzzy.isEmpty())
                    throw new EvalException("unknown command");
                if (1 < fuzzy.size()) {
                    Collections.sort(fuzzy,
                            new Comparator<Entry<String, Command>>() {
                                public final int compare(
                                        Entry<String, Command> lhs,
                                        Entry<String, Command> rhs) {
                                    return lhs.getKey().compareTo(rhs.getKey());
                                }
                            });
                    final StringBuilder sb = new StringBuilder();
                    sb.append("ambiguous command:");
                    for (final Entry<String, Command> entry : fuzzy) {
                        sb.append(' ');
                        sb.append(entry.getKey());
                    }
                    throw new EvalException(sb.toString());
                }
                final Entry<String, Command> entry = fuzzy.get(0);
                cmd = entry.getKey();
                value = entry.getValue();
            }
            final List<Class> types = new ArrayList<Class>();
            for (final Object arg : args)
                types.add(arg.getClass());

            model.update();
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
            view.getLog().error("invalid arguments");
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            if (t instanceof ExitException) {
                // No ready prompt.
                interact = false;
                throw (ExitException) t;
            }
            view.getLog().error(t.getLocalizedMessage());
        } catch (final Exception e) {
            view.getLog().error(e.getLocalizedMessage());
        } finally {
            if (interact)
                controller.ready();
        }
    }

    public final void eval() throws EvalException {
        if (interact)
            eval("next");
    }

    public final void batch(Reader reader) throws EvalException, IOException,
            ParseException {
        final boolean orig = interact;
        interact = false;
        try {
            Shellwords.parse(reader, this);
        } finally {
            interact = orig;
            controller.ready();
        }
    }

    public final void batch(File file) throws EvalException, IOException,
            ParseException {
        if (file.canRead()) {
            final FileReader reader = new FileReader(file);
            try {
                batch(reader);
            } finally {
                reader.close();
            }
        } else
            controller.ready();
    }

    public final void config() throws EvalException, IOException,
            ParseException {
        batch(model.getConfig());
    }
}
