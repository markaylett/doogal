package org.doogal.core;

import java.io.Closeable;
import java.io.IOException;

interface Pager extends Closeable {

    void setDataSet(DataSet dataSet) throws IOException;

    void setPage(String n) throws EvalException, IOException;

    void showPage() throws EvalException, IOException;

    void nextPage() throws EvalException, IOException;

    void prevPage() throws EvalException, IOException;

    DataSet getDataSet();
}
