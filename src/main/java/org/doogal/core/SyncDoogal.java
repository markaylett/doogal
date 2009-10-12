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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import javax.mail.internet.ParseException;

import org.doogal.core.table.PairTable;
import org.doogal.core.table.TableType;
import org.doogal.core.view.LastRefreshView;
import org.doogal.core.view.View;

public final class SyncDoogal implements Doogal {
    private final LastRefreshView view;
    private final Controller controller;
    private final Model model;
    private final Map<String, Command> commands;

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

    private final void addCommands() throws IllegalAccessException,
            IllegalArgumentException, InvocationTargetException {
        final Method[] methods = Model.class.getMethods();
        for (int i = 0; i < methods.length; ++i) {
            final Method method = methods[i];
            final Builtin builtin = method.getAnnotation(Builtin.class);
            if (null != builtin)
                commands.put(builtin.value(), (Command) method.invoke(model));
        }
        commands.put("alias", new AbstractBuiltin() {
            public final String getDescription() {
                return "alias an existing command";
            }

            @SuppressWarnings("unused")
            public final void exec() throws EvalException, IOException {

                final PairTable table = new PairTable(TableType.ALIAS, "alias",
                        "description");
                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.ALIAS == entry.getValue().getType())
                        table.add(entry.getKey(), entry.getValue()
                                .getDescription());

                view.setTable(table);
                view.showPage();
            }

            @SuppressWarnings("unused")
            @Synopsis("alias [hint]")
            public final void exec(String hint) throws EvalException,
                    IOException {

                hint = hint.toLowerCase();

                final PairTable table = new PairTable(TableType.ALIAS, "alias",
                        "description");
                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.ALIAS == entry.getValue().getType()
                            && entry.getKey().startsWith(hint))
                        table.add(entry.getKey(), entry.getValue()
                                .getDescription());

                view.setTable(table);
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
                commands.put(alias, new AbstractAlias() {
                    public final String getDescription() {
                        return value;
                    }

                    public final void exec() throws EvalException {
                        SyncDoogal.this.eval(name, toks.toArray());
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
                        SyncDoogal.this.eval(name, all);
                    }
                });
            }
        });
        commands.put("unalias", new AbstractBuiltin() {
            public final String getDescription() {
                return "remove an alias";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                if (TableType.ALIAS == view.getType()) {
                    final Object[] args = model.getArgs();
                    if (null != args && 0 < args.length) {
                        exec(args[0].toString());
                        return;
                    }
                }
                // Only when context arguments are set.
                throw new EvalException("unknown command");
            }

            @Synopsis("unalias name")
            public final void exec(String name) throws EvalException {
                final Command cmd = commands.get(name);
                if (null == cmd || Type.ALIAS != cmd.getType())
                    throw new EvalException("unknown alias");

                commands.remove(name);
                view.refresh(TableType.ALIAS);
            }
        });
        commands.put("help", new AbstractBuiltin() {
            public final String getDescription() {
                return "list commands with help";
            }

            @Override
            public final String getLargeIcon() {
                return "/Help24.gif";
            }

            @Override
            public final String getSmallIcon() {
                return "/Help16.gif";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {

                if (TableType.BUILTIN == view.getType()) {
                    final Object[] args = model.getArgs();
                    if (null != args && 0 < args.length) {
                        exec(args[0].toString());
                        return;
                    }
                }

                final PairTable table = new PairTable(TableType.BUILTIN,
                        "command", "description");
                final List<String> ls = new ArrayList<String>();

                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.BUILTIN == entry.getValue().getType())
                        table.add(entry.getKey(), entry.getValue()
                                .getDescription());

                view.setTable(table);
                view.showPage();
            }

            @Synopsis("help [hint]")
            public final void exec(String hint) throws Exception {

                hint = hint.toLowerCase();

                final PairTable table = new PairTable(TableType.BUILTIN,
                        "command", "description");

                String last = null;
                for (final Entry<String, Command> entry : commands.entrySet())
                    if (Type.BUILTIN == entry.getValue().getType()
                            && entry.getKey().startsWith(hint)) {
                        table.add(entry.getKey(), entry.getValue()
                                .getDescription());
                        last = entry.getKey();
                    }

                if (1 == table.getRowCount()) {
                    printHelp(last, commands.get(last), view.getOut());
                    return;
                }

                view.setTable(table);
                view.showPage();
            }
        });
        commands.put("exit", new AbstractBuiltin() {
            public final String getDescription() {
                return "exit application";
            }

            @SuppressWarnings("unused")
            @Synopsis("exit")
            public final void exec() throws ExitException {
                controller.exit(true);
            }
        });
    }

    public SyncDoogal(Environment env, View view, Controller controller,
            Repo repo) throws EvalException, IllegalAccessException,
            InvocationTargetException, IOException {
        this.view = new LastRefreshView(view, this);
        this.controller = controller;
        model = new Model(env, this.view, repo);
        commands = new TreeMap<String, Command>();
        addCommands();
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
            view.setLast(cmd, args);

        } catch (final NoSuchMethodException e) {
            view.getLog().error("invalid arguments");
        } catch (final InvocationTargetException e) {
            final Throwable t = e.getCause();
            if (t instanceof ExitException)
                throw (ExitException) t;
            view.getLog().error(t.getLocalizedMessage());
        } catch (final Exception e) {
            view.getLog().error(e.getLocalizedMessage());
        }
    }

    public final void eval() throws EvalException {
    }

    public final void batch(Reader reader) throws EvalException, IOException,
            ParseException {
        Shellwords.parse(reader, this);
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

    public final void setArgs(Object... args) {
        model.setArgs(args);
    }

    public final Map<String, Command> getBuiltins() {
        final Map<String, Command> map = new HashMap<String, Command>();

        for (final Entry<String, Command> entry : commands.entrySet())
            if (Type.BUILTIN == entry.getValue().getType())
                map.put(entry.getKey(), entry.getValue());

        return map;
    }
}
