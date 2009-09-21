package org.doogal;

final class Constants {
	private Constants() {
	}

	// (remove-hook 'kill-buffer-query-functions
	// 'server-kill-buffer-query-function)
	static final String DEFAULT_EDITOR = "emacsclient.exe";
	static final int MAX_PAGE = 20;
	static final int PAGE_SIZE = 10;
}