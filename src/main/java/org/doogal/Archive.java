package org.doogal;

import static org.doogal.Utility.getRelativePath;
import static org.doogal.Utility.ignore;
import static org.doogal.Utility.listFiles;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

final class Archive {
    static void exec(final Repo repo) throws Exception {

        final byte[] buf = new byte[1024];
        final String path = repo.getData().getAbsolutePath() + ".zip";

        final ZipOutputStream out = new ZipOutputStream(new FileOutputStream(
                path));
        try {

            listFiles(repo.getData(), new Predicate<File>() {
                public final boolean call(File file) throws IOException {
                    if (ignore(file))
                        return true;
                    final FileInputStream in = new FileInputStream(file);
                    try {

                        final String path = "data" + File.separatorChar
                                + getRelativePath(repo.getData(), file);
                        final ZipEntry entry = new ZipEntry(path.replace(
                                File.separatorChar, '/'));
                        entry.setTime(file.lastModified());
                        out.putNextEntry(entry);
                        for (;;) {
                            final int len = in.read(buf);
                            if (len <= 0)
                                break;
                            out.write(buf, 0, len);
                        }
                        out.closeEntry();

                    } finally {
                        in.close();
                    }
                    return true;
                }
            });
        } finally {
            out.close();
        }
    }
}
