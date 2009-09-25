package org.doogal;

import java.io.File;
import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.HitCollector;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

final class SharedState {
    public final Log log;
    private final Environment env;
    private final Repo repo;
    private final IdentityMap identityMap;
    private final Recent recent;
    private final IndexSearcher searcher;
    private int refs;

    SharedState(Log log, Environment env, Repo repo, IdentityMap identityMap,
            Recent recent) throws IOException {
        this.env = env;
        this.log = log;
        this.repo = repo;
        this.identityMap = identityMap;
        this.recent = recent;
        searcher = new IndexSearcher(repo.getIndex().getAbsolutePath());
        refs = 1;
    }

    final String getEditor() {
        return env.getEditor();
    }

    final String getRepo() {
        return env.getRepo();
    }

    final String getIncoming() {
        return env.getIncoming();
    }

    final String getOutgoing() {
        return env.getOutgoing();
    }

    final String getTemplate() {
        return env.getTemplate();
    }

    final File getData() {
        return repo.getData();
    }

    final File getEtc() {
        return repo.getEtc();
    }

    final File getIndex() {
        return repo.getIndex();
    }

    final File getTrash() {
        return repo.getTrash();
    }

    final int getLocal(String uuid) {
        return identityMap.getLocal(uuid);
    }

    final String getGlobal(int local) throws EvalException {
        return identityMap.getGlobal(local);
    }

    final String getGlobal(String uuid) throws EvalException {
        return identityMap.getGlobal(uuid);
    }

    final void addRecent(String id) {
        recent.add(id);
    }

    final void removeRecent(String id) {
        recent.remove(id);
    }

    final IndexReader getIndexReader() {
        return searcher.getIndexReader();
    }

    final void search(Query query, HitCollector results) throws IOException {
        searcher.search(query, results);
    }

    final TermDocs termDocs(Term term) throws IOException {
        return searcher.getIndexReader().termDocs(term);
    }

    final Document doc(int i) throws IOException {
        return searcher.doc(i);
    }

    final boolean isCurrent() throws IOException {
        return searcher.getIndexReader().isCurrent();
    }

    final boolean isDeleted(int n) {
        return searcher.getIndexReader().isDeleted(n);
    }

    final int maxDoc() {
        return searcher.getIndexReader().maxDoc();
    }

    final int numDocs() {
        return searcher.getIndexReader().numDocs();
    }

    final void release() throws IOException {
        if (0 == --refs)
            searcher.close();
    }

    final void retain() {
        ++refs;
    }
}
