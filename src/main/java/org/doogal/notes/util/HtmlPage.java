package org.doogal.notes.util;

import java.io.File;

import net.jcip.annotations.Immutable;

@Immutable
public final class HtmlPage {
    private final int id;
    private final String title;
    private final File path;

    public HtmlPage(int id, String title, File path) {
        this.id = id;
        this.title = title;
        this.path = path;
    }

    public final int getId() {
        return id;
    }

    public final String getTitle() {
        return title;
    }

    public final File getPath() {
        return path;
    }
}
