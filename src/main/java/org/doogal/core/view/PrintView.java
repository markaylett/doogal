package org.doogal.core.view;

import static org.doogal.core.domain.Constants.PAGE_SIZE;
import static org.doogal.core.table.TableUtil.printTable;

import java.awt.Desktop;
import java.io.IOException;
import java.io.PrintWriter;

import org.apache.commons.logging.Log;
import org.doogal.core.table.Table;
import org.doogal.core.util.EvalException;
import org.doogal.core.util.HtmlPage;

public final class PrintView extends AbstractView {

    private int start;
    private int end;

    public PrintView(PrintWriter out, Log log) throws IOException {
        super(out, log);
    }

    public final void setPage(int n) throws EvalException, IOException {
        final int i = Math.max(n - 1, 0) * PAGE_SIZE;
        if (null == table || table.getRowCount() <= i)
            throw new EvalException("no such page");
        start = i;
    }

    public final void nextPage() throws EvalException, IOException {
        if (null != table && start + PAGE_SIZE < table.getRowCount())
            start += PAGE_SIZE;
    }

    public final void prevPage() throws EvalException, IOException {
        if (null != table)
            start = Math.max(0, start - PAGE_SIZE);
    }

    public final void setHtml(HtmlPage html) {
        if (Desktop.isDesktopSupported())
            try {
                Desktop.getDesktop().browse(html.getPath().toURI());
            } catch (final IOException e) {
                e.printStackTrace();
            }
    }

    @Override
    public final void setTable(Table table) throws IOException {
        super.setTable(table);
        start = 0;
        end = null == table ? 0 : Math.min(table.getRowCount(), start
                + PAGE_SIZE);
    }

    public final void refresh() throws EvalException, IOException {

        if (null == table) {
            out.println("no table");
            return;
        }

        end = Math.min(table.getRowCount(), start + PAGE_SIZE);

        printTable(table, start, end, out);

        if (0 < table.getRowCount()) {
            final int page = 1 + start / PAGE_SIZE;
            final int total = 1 + (table.getRowCount() - 1) / PAGE_SIZE;
            out.printf("page %d of %d", page, total);
            if (page < total)
                out.println(" more...");
            else
                out.println(" end");
        } else
            out.println("no results");

        out.println();
    }
}
