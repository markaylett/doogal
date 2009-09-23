package org.doogal;

import java.util.HashMap;
import java.util.Map;

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

    final String getGlobal(int local) throws IdentityException {
        final String uuid = localToGlobal.get(local);
        if (null == uuid)
            throw new IdentityException("no such identifier");
        return uuid;
    }

    final String getGlobal(String uuid) throws IdentityException {
        return getGlobal(Integer.valueOf(uuid));
    }
}
