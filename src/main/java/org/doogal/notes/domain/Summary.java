package org.doogal.notes.domain;

import static org.doogal.notes.domain.Constants.DATE_FORMAT;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import net.jcip.annotations.Immutable;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;

@Immutable
public final class Summary {
    private final int id;
    private final long size;
    private final Date modified;
    private final String display;

    Summary(int id, long size, Date modified, String display) {
        this.id = id;
        this.size = size;
        this.modified = modified;
        this.display = display;
    }

    public Summary(int id, Document doc) throws IOException {

        this.id = id;
        size = Long.parseLong(doc.get("content-length"));
        try {
            modified = DateTools.stringToDate(doc.get("modified"));
        } catch (final ParseException e) {
            throw new IOException(e.getLocalizedMessage());
        }

        String display = doc.get("title");
        final String name = doc.get("name");

        if (null == display) {
            display = doc.get("subject");
            if (null == display) {
                display = name;
                if (null == display)
                    display = "Untitled";
            }
        }

        this.display = null == name ? display : String.format("%s [%s]",
                display, name);
    }

    @Override
    public final String toString() {
        return String.format("%d %s %s", id, DATE_FORMAT.format(modified),
                display);
    }

    public final int getId() {
        return id;
    }

    public final long getSize() {
        return size;
    }

    public final Date getModified() {
        return modified;
    }

    public final String getDisplay() {
        return display;
    }
}
