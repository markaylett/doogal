package org.doogal.core;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.lucene.document.DateTools;
import org.apache.lucene.document.Document;

public final class Summary {
    private static final DateFormat df = new SimpleDateFormat("dd-MMM-yy");
    private final int id;
    private final Date modified;
    private final String display;

    Summary(int id, Date modified, String display) {
        this.id = id;
        this.modified = modified;
        this.display = display;
    }

    Summary(int id, Document doc) throws IOException {

        this.id = id;
        try {
            this.modified = DateTools.stringToDate(doc.get("modified"));
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
        return String.format("%5d %s %s", id, df.format(modified), display);
    }

    public final int getId() {
        return id;
    }

    public final Date getModified() {
        return modified;
    }

    public final String getDisplay() {
        return display;
    }
}
