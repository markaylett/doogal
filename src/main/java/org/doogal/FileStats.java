package org.doogal;

import java.io.File;

final class FileStats {
	private final File file;
	private final long lastModified;
	private final long length;

	FileStats(File file) {
		// Works for non-existent files.
		this.file = file;
		this.lastModified = file.lastModified();
		this.length = file.length();
	}

	final boolean hasFileChanged() {
		return lastModified != file.lastModified() || length != file.length();
	}
}