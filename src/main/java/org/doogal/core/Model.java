package org.doogal.core;

import static org.doogal.core.Utility.join;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
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

    final PrintWriter out;
    final PrintWriter err;
    final Log log;
    private final Environment env;
    private final Repo repo;
    private final IdentityMap identityMap;
    private final Recent recent;
    private SharedState state;
    private Pager pager;

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

    private final Pager browse() throws IOException {

        final int max = state.maxDoc();
        final int n = Math.min(state.numDocs(), Constants.MAX_RESULTS);
        final IdentityResults results = new IdentityResults();

        int i = Math.abs(RAND.nextInt(max));
        while (results.size() < n) {
            final int j = i++ % max;
            if (!state.isDeleted(j)) {
                final Document doc = state.doc(j);
                final String id = doc.get("id");
                final int lid = state.getLocal(id);
                results.add(id, Utility.toString(lid, doc));
            }
        }
        return new Pager(results, out);
    }

    @SuppressWarnings("unchecked")
    private final Pager keys() throws IOException, ParseException {
        final Collection<String> col = state.getIndexReader().getFieldNames(
                FieldOption.ALL);
        final String[] arr = col.toArray(new String[col.size()]);
        Arrays.sort(arr);
        return new Pager(new ArrayResults(arr), out);
    }

    private final Pager more(Term term) throws EvalException, IOException {
        final TermDocs docs = state.getIndexReader().termDocs(term);
        if (!docs.next())
            throw new EvalException("no such document");
        final MoreLikeThis mlt = new MoreLikeThis(state.getIndexReader());
        mlt.setFieldNames(FIELDS);
        mlt.setMinDocFreq(1);
        mlt.setMinTermFreq(1);
        final Query query = mlt.like(docs.doc());
        return new Pager(new SearchResults(state, query), out);
    }

    private final Pager search(String s) throws IOException, ParseException {
        final QueryParser parser = new MultiFieldQueryParser(FIELDS,
                new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse(s);
        return new Pager(new SearchResults(state, query), out);
    }

    private final Pager values(final String s) throws IOException,
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
        return new Pager(new ArrayResults(arr), out);
    }

    Model(PrintWriter out, PrintWriter err, Log log, Environment env, Repo repo)
            throws EvalException, IOException {
        this.out = out;
        this.err = err;
        this.log = log;
        this.env = env;
        this.repo = repo;
        identityMap = new IdentityMap();
        recent = new Recent();
        state = null;
        // Avoid null pager.
        setPager(new Pager(ListResults.EMPTY, out));
    }

    public final void close() {
        try {
            setPager(null);
        } catch (IOException e) {
            log.error(e.getLocalizedMessage());
        }
        if (null != state) {
            try {
                state.release();
            } catch (IOException e) {
                log.error(e.getLocalizedMessage());
            }
            state = null;
        }
    }

    final void setPager(Pager p) throws IOException {
        if (null != pager) {
            pager.close();
            pager = null;
        }
        pager = p;
    }

    final void update() throws IOException {
        if (null != state && !state.isCurrent()) {
            state.release();
            state = null;
        }
        if (null == state)
            state = new SharedState(log, env, repo, identityMap, recent);
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
                log.info("archiving...");
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
                setPager(browse());
                pager.execList();
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
                log.info("deleting...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Delete.exec(state, term);
                } else
                    Delete.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("delete doc...")
            public final void exec(Object... args) throws Exception {
                log.info("deleting...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Delete.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = pager.terms();
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
                pager.execGoto(n);
                pager.execList();
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
                log.info("importing...");
                Import.exec(state);
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
                log.info("indexing...");
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
                setPager(keys());
                pager.execList();
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
                pager.execList();
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
                setPager(more(getTerm()));
                pager.execList();
            }

            @SuppressWarnings("unused")
            @Synopsis("more [doc]")
            public final void exec(String s) throws EvalException, IOException {
                setPager(more(getTerm(s)));
                pager.execList();
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
                log.info("new document...");
                New.exec(state);
            }

            @SuppressWarnings("unused")
            @Synopsis("new [template]")
            public final void exec(String s) throws Exception {
                log.info("new document...");
                New.exec(state, s);
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
                pager.execNext();
                pager.execList();
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
                log.info("opening...");
                Open.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            @Synopsis("open [doc]")
            public final void exec(String s) throws Exception {
                log.info("opening...");
                Open.exec(state, getTerm(s));
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
                pager.execPrev();
                pager.execList();
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
                log.info("publishing...");
                Publish.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                log.info("publishing...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Publish.exec(state, term);
                } else
                    Publish.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("publish [doc...]")
            public final void exec(Object... args) throws Exception {
                log.info("publishing...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Publish.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Publish.exec(state, term);
                }
            }
        };
    }

    @Builtin("quit")
    public final Command newQuit() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "exit application";
            }

            @SuppressWarnings("unused")
            @Synopsis("quit")
            public final void exec() throws ExitException {
                log.info("exiting...");
                throw new ExitException();
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
                setPager(new Pager(recent.asResults(state), out));
                pager.execList();
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
                log.info(String.format("searching for '%s'...\n", last));
                setPager(search(last));
                pager.execList();
            }

            public final void exec(String s) throws IOException, ParseException {
                setPager(search(s));
                pager.execList();
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
                setPager(new Pager(new ArrayResults(arr), out));
                pager.execList();
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
                log.info("tidying...");
                Tidy.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                log.info("tidying...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Tidy.exec(state, term);
                } else
                    Tidy.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("tidy [doc...]")
            public final void exec(Object... args) throws Exception {
                log.info("tidying...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Tidy.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Tidy.exec(state, term);
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
                setPager(values(s));
                pager.execList();
            }
        };
    }
}
