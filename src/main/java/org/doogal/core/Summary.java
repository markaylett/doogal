package org.doogal.core;

import java.util.Date;

final class Summary {
    private final int id;
    private final Date modified;
    private final String title;
    Summary(int id, Date modified, String title) {
        this.id = id;
        this.modified = modified;
        this.title = title;
    }
    final int getId() {
        return id;
    }
    final Date getModified() {
        return modified;
    }
    final String getTitle() {
        return title;
    }
}
