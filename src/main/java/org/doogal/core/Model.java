package org.doogal.core;

import static org.doogal.core.Utility.join;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

final class Model implements Closeable {
    private static final class WrapException extends RuntimeException {
        private static final long serialVersionUID = 1L;

        WrapException(Throwable t) {
            super(t);
        }
    }

    private static final String[] FIELDS = { "subject", "title", "contents" };
    private static final Random RAND = new Random();

    private final Environment env;
    private final View view;
    private final Repo repo;
    private final IdentityMap identityMap;
    private final Recent recent;
    private SharedState state;

    private final Term getTerm(String value) throws EvalException {
        return Character.isDigit(value.charAt(0)) ? new Term("id", identityMap
                .getGlobal(value)) : new Term("name", value);
    }

    private final Term getTerm() throws EvalException {
        final String id = recent.top();
        if (null == id)
            throw new EvalException("no such identifier");
        return new Term("id", id);
    }

    private final DocumentSet browse() throws IOException {

        final int max = state.maxDoc();
        final int n = Math.min(state.numDocs(), Constants.MAX_RESULTS);
        final IdentitySet docSet = new IdentitySet();

        int i = Math.abs(RAND.nextInt(max));
        while (docSet.size() < n) {
            final int j = i++ % max;
            if (!state.isDeleted(j)) {
                final Document doc = state.doc(j);
                final String id = doc.get("id");
                final int lid = state.getLocal(id);
                docSet.add(id, Utility.toString(lid, doc));
            }
        }
        return docSet;
    }

    @SuppressWarnings("unchecked")
    private final DataSet keys() throws IOException, ParseException {
        final Collection<String> col = state.getIndexReader().getFieldNames(
                FieldOption.ALL);
        final String[] arr = col.toArray(new String[col.size()]);
        Arrays.sort(arr);
        return new ArraySet(arr);
    }

    private final DocumentSet more(Term term) throws EvalException, IOException {
        final TermDocs docs = state.getIndexReader().termDocs(term);
        if (!docs.next())
            throw new EvalException("no such document");
        final MoreLikeThis mlt = new MoreLikeThis(state.getIndexReader());
        mlt.setFieldNames(FIELDS);
        mlt.setMinDocFreq(1);
        mlt.setMinTermFreq(1);
        final Query query = mlt.like(docs.doc());
        return new SearchSet(view, state, query);
    }

    private final DocumentSet search(String s) throws IOException, ParseException {
        final QueryParser parser = new MultiFieldQueryParser(FIELDS,
                new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse(s);
        return new SearchSet(view, state, query);
    }

    private final DataSet values(final String s) throws IOException,
            ParseException {
        final QueryParser parser = new QueryParser(s, new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse("*");
        final Set<String> values = new TreeSet<String>();
        try {
            state.search(query, new HitCollector() {
                @Override
                public final void collect(int doc, float score) {
                    try {
                        final Document d = state.doc(doc);
                        final Field[] fs = d.getFields(s);
                        for (int i = 0; i < fs.length; ++i)
                            values.add(fs[i].stringValue());
                    } catch (final IOException e) {
                        throw new WrapException(e);
                    }
                }
            });
        } catch (final WrapException e) {
            throw (IOException) e.getCause();
        }
        final String[] arr = values.toArray(new String[values.size()]);
        return new ArraySet(arr);
    }

    Model(Environment env, View view, Repo repo)
            throws EvalException, IOException {
        this.env = env;
        this.view = view;
        this.repo = repo;
        identityMap = new IdentityMap();
        recent = new Recent();
        state = null;
    }

    public final void close() {
        try {
            view.close();
        } catch (IOException e) {
            view.getLog().error(e.getLocalizedMessage());
        }
        if (null != state) {
            try {
                state.release();
            } catch (IOException e) {
                view.getLog().error(e.getLocalizedMessage());
            }
            state = null;
        }
    }

    final void update() throws IOException {
        if (null != state && !state.isCurrent()) {
            state.release();
            state = null;
        }
        if (null == state)
            state = new SharedState(env, repo, identityMap, recent);
    }

    final File getConfig() {
        return new File(repo.getEtc(), "doogal.conf");
    }

    @Builtin("archive")
    public final Command newArchive() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "create a backup archive";
            }

            @SuppressWarnings("unused")
            @Synopsis("archive")
            public final void exec() throws Exception {
                view.getLog().info("archiving...");
                Archive.exec(repo);
            }
        };
    }

    @Builtin("browse")
    public final Command newBrowse() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "browse random selection";
            }

            @SuppressWarnings("unused")
            @Synopsis("browse")
            public final void exec() throws Exception {
                view.setDataSet(browse());
                view.showPage();
            }
        };
    }

    @Builtin("delete")
    public final Command newDelete() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "delete a document";
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                view.getLog().info("deleting...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Delete.exec(state, term);
                } else
                    Delete.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("delete doc...")
            public final void exec(Object... args) throws Exception {
                view.getLog().info("deleting...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Delete.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Delete.exec(state, term);
                }
            }
        };
    }

    @Builtin("goto")
    public final Command newGoto() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "jump to page";
            }

            @SuppressWarnings("unused")
            @Synopsis("goto n")
            public final void exec(String n) throws EvalException, IOException {
                view.setPage(n);
                view.showPage();
            }
        };
    }

    @Builtin("import")
    public final Command newImport() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "import from inbox";
            }

            @SuppressWarnings("unused")
            @Synopsis("import")
            public final void exec() throws Exception {
                view.getLog().info("importing...");
                Import.exec(view, state);
            }
        };
    }

    @Builtin("index")
    public final Command newIndex() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "generate search index";
            }

            @SuppressWarnings("unused")
            @Synopsis("index")
            public final void exec() throws Exception {
                view.getLog().info("indexing...");
                Index.exec(repo);
            }
        };
    }

    @Builtin("keys")
    public final Command newKeys() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "field keys";
            }

            @SuppressWarnings("unused")
            @Synopsis("keys")
            public final void exec() throws Exception {
                view.setDataSet(keys());
                view.showPage();
            }
        };
    }

    @Builtin("list")
    public final Command newList() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "show current page";
            }

            @SuppressWarnings("unused")
            @Synopsis("list")
            public final void exec() throws IOException {
                view.showPage();
            }
        };
    }

    @Builtin("more")
    public final Command newMore() {
        return new AbstractBuiltin() {

            public final String getDescription() {
                return "more like this";
            }

            @SuppressWarnings("unused")
            public final void exec() throws EvalException, IOException {
                view.setDataSet(more(getTerm()));
                view.showPage();
            }

            @SuppressWarnings("unused")
            @Synopsis("more [doc]")
            public final void exec(String s) throws EvalException, IOException {
                view.setDataSet(more(getTerm(s)));
                view.showPage();
            }
        };
    }

    @Builtin("new")
    public final Command newNew() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "create a new document";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                view.getLog().info("new document...");
                New.exec(view, state);
            }

            @SuppressWarnings("unused")
            @Synopsis("new [template]")
            public final void exec(String s) throws Exception {
                view.getLog().info("new document...");
                New.exec(view, state, s);
            }
        };
    }

    @Builtin("next")
    public final Command newNext() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "move to next page";
            }

            @SuppressWarnings("unused")
            @Synopsis("next")
            public final void exec() throws IOException {
                view.nextPage();
                view.showPage();
            }
        };
    }

    @Builtin("open")
    public final Command newOpen() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "open existing document";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                view.getLog().info("opening...");
                Open.exec(view, state, getTerm());
            }

            @SuppressWarnings("unused")
            @Synopsis("open [doc]")
            public final void exec(String s) throws Exception {
                view.getLog().info("opening...");
                Open.exec(view, state, getTerm(s));
            }
        };
    }
    
    @Builtin("peek")
    public final Command newWhat() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "peek inside document";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                Peek.exec(view, state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                if ("*".equals(s)) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Peek.exec(view, state, term);
                } else
                    Peek.exec(view, state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("peek [doc...]")
            public final void exec(Object... args) throws Exception {
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Peek.exec(view, state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Peek.exec(view, state, term);
                }
            }
        };
    }

    @Builtin("previous")
    public final Command newPrev() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "move to previous page";
            }

            @SuppressWarnings("unused")
            @Synopsis("previous")
            public final void exec() throws IOException {
                view.prevPage();
                view.showPage();
            }
        };
    }

    @Builtin("publish")
    public final Command newPublish() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "publish to html";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                view.getLog().info("publishing...");
                Publish.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                view.getLog().info("publishing...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Publish.exec(state, term);
                } else
                    Publish.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("publish [doc...]")
            public final void exec(Object... args) throws Exception {
                view.getLog().info("publishing...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Publish.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Publish.exec(state, term);
                }
            }
        };
    }

    @Builtin("recent")
    public final Command newRecent() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "recently visited";
            }

            @SuppressWarnings("unused")
            @Synopsis("recent")
            public final void exec() throws EvalException, IOException {
                view.setDataSet(recent.asDataSet(state));
                view.showPage();
            }
        };
    }

    @Builtin("search")
    public final Command newSearch() {
        return new AbstractBuiltin() {
            private String last = "doogal";

            public final String getDescription() {
                return "search repository";
            }

            @SuppressWarnings("unused")
            public final void exec() throws IOException, ParseException {
                view.getLog().info(String.format("searching for '%s'...\n", last));
                view.setDataSet(search(last));
                view.showPage();
            }

            public final void exec(String s) throws IOException, ParseException {
                view.setDataSet(search(s));
                view.showPage();
                last = s;
            }

            @SuppressWarnings("unused")
            @Synopsis("search [expr...]")
            public final void exec(Object... args) throws IOException,
                    ParseException {
                exec(join(args));
            }
        };
    }

    @Builtin("set")
    public final Command newEnv() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "update configuration";
            }

            @SuppressWarnings("unused")
            @Synopsis("set")
            public final void exec() throws IOException {
                final String[] arr = env.toArray();
                view.setDataSet(new ArraySet(arr));
                view.showPage();
            }

            @SuppressWarnings("unused")
            @Synopsis("set name")
            public final void exec(String name) throws EvalException,
                    IOException {
                env.reset(name);
            }

            @SuppressWarnings("unused")
            @Synopsis("set name value")
            public final void exec(String name, String value)
                    throws EvalException, IOException {
                env.set(name, value);
            }
        };
    }

    @Builtin("tidy")
    public final Command newTidy() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "clean ascii and trim";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                view.getLog().info("tidying...");
                Tidy.exec(view, state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                view.getLog().info("tidying...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Tidy.exec(view, state, term);
                } else
                    Tidy.exec(view, state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("tidy [doc...]")
            public final void exec(Object... args) throws Exception {
                view.getLog().info("tidying...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Tidy.exec(view, state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = view.terms();
                    for (final Term term : terms)
                        Tidy.exec(view, state, term);
                }
            }
        };
    }

    @Builtin("values")
    public final Command newValues() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "field values";
            }

            @SuppressWarnings("unused")
            @Synopsis("values name")
            public final void exec(String s) throws IOException, ParseException {
                view.setDataSet(values(s));
                view.showPage();
            }
        };
    }
}
