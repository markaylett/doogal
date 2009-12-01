package org.doogal.core.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public final class FileUtil {
    private FileUtil() {

    }

    public static BufferedReader newBufferedReader(InputStream in)
            throws UnsupportedEncodingException {
        return new BufferedReader(new InputStreamReader(in, "UTF-8"));
    }

    public static BufferedWriter newBufferedWriter(OutputStream out)
            throws UnsupportedEncodingException {
        return new BufferedWriter(new OutputStreamWriter(out, "UTF-8"));
    }
}
