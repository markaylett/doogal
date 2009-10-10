package org.doogal.core;

import static org.doogal.core.Constants.DEFAULT_EDITOR;
import static org.doogal.core.Utility.getPath;

import java.io.File;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import org.doogal.core.table.PairTable;
import org.doogal.core.table.Table;

public final class Environment {
    private static interface Accessor {
        void reset() throws EvalException;

        void set(Object value) throws EvalException;

        Object get();
    }

    private final Map<String, Accessor> env;
    private final String repo;
    private String editor;
    private String template;
    private String html;
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

    private final String defaultHtml() {
        return getPath(repo, "html");
    }

    private final String defaultInbox() {
        return getPath(repo, "inbox");
    }

    private final String defaultTemplate() {
        return getPath(repo, "template");
    }

    public Environment() {
        env = new TreeMap<String, Accessor>();
        editor = defaultEditor();
        repo = defaultRepo();
        template = null;
        html = null;
        inbox = null;

        env.put("editor", new Accessor() {

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
        env.put("repo", new Accessor() {

            public final Object get() {
                return getRepo();
            }

            public final void reset() throws EvalException {
                throw new EvalException("read-only variable");
            }

            public final void set(Object value) throws EvalException {
                throw new EvalException("read-only variable");
            }
        });
        env.put("html", new Accessor() {

            public final Object get() {
                return getHtml();
            }

            public final void reset() {
                html = null;
            }

            public final void set(Object value) {
                html = value.toString();
            }
        });
        env.put("inbox", new Accessor() {

            public final Object get() {
                return getInbox();
            }

            public final void reset() {
                inbox = null;
            }

            public final void set(Object value) {
                inbox = value.toString();
            }
        });
        env.put("template", new Accessor() {

            public final Object get() {
                return getTemplate();
            }

            public final void reset() {
                template = null;
            }

            public final void set(Object value) {
                template = value.toString();
            }
        });
    }

    final void reset(String name) throws EvalException {
        final Accessor acc = env.get(name);
        if (null == acc)
            throw new EvalException("no such name");
        acc.reset();
    }

    final void set(String name, Object value) throws EvalException {
        final Accessor acc = env.get(name);
        if (null == acc)
            throw new EvalException("no such name");
        acc.set(value);
    }

    final Object get(String name) throws EvalException {
        final Accessor acc = env.get(name);
        if (null == acc)
            throw new EvalException("no such name");
        return acc.get();
    }

    final String getEditor() {
        return editor;
    }

    public final String getRepo() {
        return repo;
    }

    final String getHtml() {
        return null == html ? defaultHtml() : html;
    }

    final String getInbox() {
        return null == inbox ? defaultInbox() : inbox;
    }

    final String getTemplate() {
        return null == template ? defaultTemplate() : template;
    }

    final Table asTable() {
        final PairTable table = new PairTable("name", "value", null, null);
        for (final Entry<String, Accessor> entry : env.entrySet())
            table.add(entry.getKey(), entry.getValue().get().toString());
        return table;
    }
}