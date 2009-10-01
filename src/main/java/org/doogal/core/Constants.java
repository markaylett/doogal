package org.doogal.core;

public final class Constants {
    private Constants() {
    }

    // (remove-hook 'kill-buffer-query-functions
    // 'server-kill-buffer-query-function)
    public static final String DEFAULT_EDITOR = "emacsclient.exe";
    public static final int MAX_RESULTS = 200;
    public static final int PAGE_SIZE = 15;
    public static final String PROMPT = "doogal> ";
}
