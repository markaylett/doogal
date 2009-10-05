package org.doogal.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.lucene.index.Term;

final class IdentityMap {
    private final Map<String, Integer> globalToLocal;
    private final Map<Integer, String> localToGlobal;
    private int next;

    private final int put(String uuid) {
        final int local = next++;
        globalToLocal.put(uuid, local);
        localToGlobal.put(local, uuid);
        return local;
    }

    IdentityMap() {
        globalToLocal = new HashMap<String, Integer>();
        localToGlobal = new HashMap<Integer, String>();
        next = 1;
    }

    final int getLocal(String uuid) {
        final Integer local = globalToLocal.get(uuid);
        return null == local ? put(uuid) : local;
    }

    final String getGlobal(int local) throws IOException {
        final String uuid = localToGlobal.get(local);
        if (null == uuid)
            throw new IOException("no such identifier");
        return uuid;
    }

    final String getGlobal(String local) throws IOException {
        return getGlobal(Integer.valueOf(local));
    }

    final Term getTerm(int local) throws IOException {
        return new Term("id", getGlobal(local));
    }

    final Term getTerm(String local) throws IOException {
        return new Term("id", getGlobal(local));
    }
}
