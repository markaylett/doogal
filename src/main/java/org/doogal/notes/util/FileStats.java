package org.doogal.notes.util;

import java.io.File;

public final class FileStats {
    private final File file;
    private final long lastModified;
    private final long length;

    public FileStats(File file) {
        // Works for non-existent files.
        this.file = file;
        lastModified = file.lastModified();
        length = file.length();
    }

    public final boolean hasFileChanged() {
        return lastModified != file.lastModified() || length != file.length();
    }
}