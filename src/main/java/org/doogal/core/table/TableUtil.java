package org.doogal.core.table;

import static org.doogal.core.domain.Constants.DATE_FORMAT;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

public final class TableUtil {

    private static String getStringAt(Table table, int rowIndex, int columnIndex)
            throws IOException {

        Object value = table.getValueAt(rowIndex, columnIndex);

        final Class<?> clazz = table.getColumnClass(columnIndex);
        if (Date.class.isAssignableFrom(clazz))
            value = DATE_FORMAT.format(value);

        return value.toString();
    }

    private TableUtil() {

    }

    public static void printTable(Table table, int start, int end,
            PrintWriter out) throws IOException {
        final int rowCount = end - start;
        final int[] max = new int[table.getColumnCount()];
        final String[] head = new String[table.getColumnCount()];
        final String[][] body = new String[rowCount][table.getColumnCount()];
        for (int i = 0; i < rowCount; ++i)
            for (int j = 0; j < table.getColumnCount(); ++j) {
                final String value = getStringAt(table, start + i, j);
                max[j] = Math.max(max[j], value.length());
                body[i][j] = value;
            }
        final StringBuilder hf = new StringBuilder(" ");
        final StringBuilder bf = new StringBuilder(" ");
        int width = 0;
        for (int j = 0; j < table.getColumnCount(); ++j) {
            head[j] = table.getColumnName(j);
            max[j] = Math.max(max[j], head[j].length());
            width += max[j];

            if (0 < hf.length()) {
                ++width;
                hf.append(' ');
            }
            hf.append("%-");
            hf.append(max[j]);
            hf.append('s');

            if (0 < bf.length())
                bf.append(' ');
            if (Number.class.isAssignableFrom(table.getColumnClass(j))) {
                bf.append('%');
                bf.append(max[j]);
            } else if (j < table.getColumnCount() - 1) {
                bf.append("%-");
                bf.append(max[j]);
            } else
                // No padding if last column and left-justified.
                bf.append("%");
            bf.append('s');
        }

        hf.append('\n');
        bf.append('\n');

        out.printf(hf.toString(), (Object[]) head);
        out.print(' ');
        for (int i = 0; i < width; ++i)
            out.print('-');
        out.println();
        for (int i = 0; i < rowCount; ++i)
            out.printf(bf.toString(), (Object[]) body[i]);
    }
}
