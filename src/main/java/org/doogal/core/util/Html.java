package org.doogal.core.util;

import java.io.File;

import net.jcip.annotations.Immutable;

@Immutable
public final class Html {
    private final String title;
    private final File path;

    public Html(String title, File path) {
        this.title = title;
        this.path = path;
    }

    public final String getTitle() {
        return title;
    }

    public final File getPath() {
        return path;
    }
}
