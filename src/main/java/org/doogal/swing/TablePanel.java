package org.doogal.swing;

import static org.doogal.swing.SwingUtil.newScrollPane;
import static org.doogal.swing.SwingUtil.setRowSorter;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.doogal.core.Doogal;
import org.doogal.core.EvalException;
import org.doogal.core.util.Size;

final class TablePanel extends JPanel implements ViewPanel {

    private static final long serialVersionUID = 1L;
    private final JTable table;

    TablePanel(final Doogal doogal, final Map<String, Action> actions) {
        super(new BorderLayout());

        table = new JTable(new TableAdapter());
        table.setFocusable(false);
        table.setDefaultRenderer(Size.class, new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected final void setValue(Object value) {
                super.setValue(value.toString());
            }
        });

        table.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public final void valueChanged(ListSelectionEvent e) {
                        final ListSelectionModel model = (ListSelectionModel) e
                                .getSource();
                        if (!model.isSelectionEmpty()) {
                            final TableAdapter tableModel = (TableAdapter) table
                                    .getModel();
                            final String[] names = tableModel.getType()
                                    .getActions();
                            for (int i = 0; i < names.length; ++i)
                                actions.get(names[i]).setEnabled(true);
                            final int[] rows = table.getSelectedRows();
                            final Object[] args = new Object[rows.length];
                            for (int i = 0; i < rows.length; ++i)
                                args[i] = table.getValueAt(rows[i], 0);
                            doogal.setSelection(tableModel.getType(), args);
                        }
                    }
                });

        table.addMouseListener(new MouseAdapter() {
            private final void setSelection(int index) {
                final ListSelectionModel selectionModel = table
                        .getSelectionModel();
                if (!selectionModel.isSelectedIndex(index))
                    selectionModel.setSelectionInterval(index, index);
            }

            private final JPopupMenu newMenu() {
                final TableAdapter model = (TableAdapter) table.getModel();
                final String[] names = model.getType().getActions();
                if (0 < names.length) {
                    final JPopupMenu menu = new JPopupMenu();
                    for (int i = 0; i < names.length; ++i) {
                        final Action action = actions.get(names[i]);
                        menu.add(new JMenuItem(action));
                    }
                    return menu;
                }
                return null;
            }

            private final void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    final Point p = e.getPoint();
                    final int index = table.rowAtPoint(p);
                    if (-1 != index) {
                        setSelection(index);
                        final JPopupMenu menu = newMenu();
                        if (null != menu)
                            menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public final void mouseClicked(MouseEvent ev) {
                if (SwingUtilities.isLeftMouseButton(ev)
                        && 2 == ev.getClickCount()) {
                    final Point p = ev.getPoint();
                    final int index = table.rowAtPoint(p);
                    if (-1 != index) {
                        final TableAdapter model = (TableAdapter) table
                                .getModel();
                        final String name = model.getType().getAction();
                        if (null != name)
                            try {
                                doogal.eval(name);
                            } catch (final EvalException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }

            @Override
            public final void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public final void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
        });

        add(newScrollPane(table), BorderLayout.CENTER);
    }

    public final void close() throws IOException {
    }

    public final void setPage(int n) throws EvalException, IOException {
    }

    public final void nextPage() throws EvalException, IOException {
    }

    public final void prevPage() throws EvalException, IOException {
    }

    final void setModel(TableModel model) {
        table.setModel(model);
        setRowSorter(table);
    }
}
