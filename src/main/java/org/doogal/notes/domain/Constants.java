package org.doogal.notes.domain;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

public final class Constants {
    private Constants() {
    }

    // (remove-hook 'kill-buffer-query-functions
    // 'server-kill-buffer-query-function)
    public static final String DEFAULT_EDITOR = "emacsclient.exe";
    public static final int MAX_RESULTS = 200;
    public static final int PAGE_SIZE = 12;
    public static final String PROMPT = "doogal> ";
    public static final DateFormat DATE_FORMAT = new SimpleDateFormat(
            "dd-MMM-yy");
    public static final int TINY_FONT = 9;
    public static final int SMALL_FONT = 12;
    public static final int MEDIUM_FONT = 14;
    public static final int LARGE_FONT = 16;
}
