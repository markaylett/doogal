package org.doogal;

import static org.doogal.Utility.join;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.IndexReader.FieldOption;
import org.apache.lucene.queryParser.MultiFieldQueryParser;
import org.apache.lucene.queryParser.ParseException;
import org.apache.lucene.queryParser.QueryParser;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.similar.MoreLikeThis;

final class Session {
    private static final String[] FIELDS = { "subject", "title", "contents" };
    private static final Random RAND = new Random();
    private final Environment env;
    private final Repo repo;
    private final IdentityMap identityMap;
    private final Recent recent;
    private SharedState state;
    private Pager pager;

    private final Term getTerm(String value) throws IdentityException {
        return Character.isDigit(value.charAt(0)) ? new Term("id", identityMap
                .getGlobal(value)) : new Term("name", value);
    }

    private final Term getTerm() throws IdentityException {
        final String id = recent.top();
        if (null == id)
            throw new IdentityException("no such identifier");
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
        return new Pager(results);
    }

    @SuppressWarnings("unchecked")
    private final Pager keys() throws IOException, ParseException {
        final Collection<String> col = state.getIndexReader().getFieldNames(
                FieldOption.ALL);
        final String[] arr = col.toArray(new String[col.size()]);
        Arrays.sort(arr);
        return new Pager(new ArrayResults(arr));
    }

    private final Pager more(Term term) throws IdentityException, IOException {
        final TermDocs docs = state.getIndexReader().termDocs(term);
        if (!docs.next())
            throw new IdentityException("no such document");
        final MoreLikeThis mlt = new MoreLikeThis(state.getIndexReader());
        mlt.setFieldNames(FIELDS);
        mlt.setMinDocFreq(1);
        mlt.setMinTermFreq(1);
        final Query query = mlt.like(docs.doc());
        return new Pager(new SearchResults(state, query));
    }

    private final Pager search(String s) throws CorruptIndexException,
            IOException, ParseException {
        final QueryParser parser = new MultiFieldQueryParser(FIELDS,
                new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse(s);
        return new Pager(new SearchResults(state, query));
    }

    private final Pager values(final String s) throws IOException,
            ParseException {
        final QueryParser parser = new QueryParser(s, new StandardAnalyzer());
        parser.setAllowLeadingWildcard(true);
        final Query query = parser.parse("*");
        final Set<String> values = new TreeSet<String>();
        state.search(query, new HitCollector() {
            @Override
            public final void collect(int doc, float score) {
                try {
                    final Document d = state.doc(doc);
                    final Field[] fs = d.getFields(s);
                    for (int i = 0; i < fs.length; ++i)
                        values.add(fs[i].stringValue());
                } catch (final CorruptIndexException e) {
                    System.err.println(e.getLocalizedMessage());
                } catch (final IOException e) {
                    System.err.println(e.getLocalizedMessage());
                }
            }
        });
        final String[] arr = values.toArray(new String[values.size()]);
        return new Pager(new ArrayResults(arr));
    }

    Session(Environment env, Repo repo) throws CorruptIndexException,
            IdentityException, IOException {
        this.env = env;
        this.repo = repo;
        identityMap = new IdentityMap();
        recent = new Recent();
        state = null;
        // Avoid null pager.
        setPager(new Pager(ListResults.EMPTY));
    }

    final void close() throws IOException {
        setPager(null);
        if (null != state) {
            state.release();
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

    final void update() throws CorruptIndexException, IOException {
        if (null != state && !state.isCurrent()) {
            state.release();
            state = null;
        }
        if (null == state)
            state = new SharedState(env, repo, identityMap, recent);
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
                System.out.println("archiving...");
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
                System.out.println("deleting...");
                Delete.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("delete doc...")
            public final void exec(Object... args) throws Exception {
                System.out.println("deleting...");
                for (final Object arg : args)
                    Delete.exec(state, getTerm(arg.toString()));
            }
        };
    }

    @Builtin("export")
    public final Command newExport() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "export to outgoing";
            }

            @SuppressWarnings("unused")
            public final void exec() throws Exception {
                System.out.println("exporting...");
                Export.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                System.out.println("exporting...");
                if ("*".equals(s)) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Export.exec(state, term);
                } else
                    Export.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("export [doc...]")
            public final void exec(Object... args) throws Exception {
                System.out.println("exporting...");
                boolean glob = false;
                for (final Object arg : args) {
                    final String s = arg.toString();
                    if ("*".equals(s))
                        glob = true;
                    else
                        Export.exec(state, getTerm(arg.toString()));
                }
                if (glob) {
                    final Collection<Term> terms = pager.terms();
                    for (final Term term : terms)
                        Export.exec(state, term);
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
            public final void exec(String n) throws IOException {
                pager.execGoto(n);
                pager.execList();
            }
        };
    }

    @Builtin("import")
    public final Command newImport() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "import from incoming";
            }

            @SuppressWarnings("unused")
            @Synopsis("import")
            public final void exec() throws Exception {
                System.out.println("importing...");
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
                System.out.println("indexing...");
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
            public final void exec() throws IdentityException, IOException {
                setPager(more(getTerm()));
                pager.execList();
            }

            @SuppressWarnings("unused")
            @Synopsis("more [doc]")
            public final void exec(String s) throws IdentityException,
                    IOException {
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
                System.out.println("new document...");
                New.exec(state);
            }

            @SuppressWarnings("unused")
            @Synopsis("new [template]")
            public final void exec(String s) throws Exception {
                System.out.println("new document...");
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
                System.out.println("opening...");
                Open.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            @Synopsis("open [doc]")
            public final void exec(String s) throws Exception {
                System.out.println("opening...");
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

    @Builtin("quit")
    public final Command newQuit() {
        return new AbstractBuiltin() {
            public final String getDescription() {
                return "exit application";
            }

            @SuppressWarnings("unused")
            @Synopsis("quit")
            public final void exec() throws ExitException {
                System.out.println("exiting...");
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
            public final void exec() throws CorruptIndexException,
                    IdentityException, IOException {
                setPager(new Pager(recent.asResults(state)));
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
            public final void exec() throws CorruptIndexException, IOException,
                    ParseException {
                System.out.printf("searching for '%s'...\n", last);
                setPager(search(last));
                pager.execList();
            }

            public final void exec(String s) throws CorruptIndexException,
                    IOException, ParseException {
                setPager(search(s));
                pager.execList();
                last = s;
            }

            @SuppressWarnings("unused")
            @Synopsis("search [expr...]")
            public final void exec(Object... args)
                    throws CorruptIndexException, IOException, ParseException {
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
                setPager(new Pager(new ArrayResults(arr)));
                pager.execList();
            }

            @SuppressWarnings("unused")
            @Synopsis("set name")
            public final void exec(String name) throws IOException,
                    NameException, ResetException {
                env.reset(name);
            }

            @SuppressWarnings("unused")
            @Synopsis("set name value")
            public final void exec(String name, String value)
                    throws IOException, NameException, ResetException {
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
                System.out.println("tidying...");
                Tidy.exec(state, getTerm());
            }

            @SuppressWarnings("unused")
            public final void exec(String s) throws Exception {
                System.out.println("tidying...");
                Tidy.exec(state, getTerm(s));
            }

            @SuppressWarnings("unused")
            @Synopsis("tidy [doc...]")
            public final void exec(Object... args) throws Exception {
                System.out.println("tidying...");
                for (final Object arg : args)
                    Tidy.exec(state, getTerm(arg.toString()));
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
            public final void exec(String s) throws Exception {
                setPager(values(s));
                pager.execList();
            }
        };
    }
}
