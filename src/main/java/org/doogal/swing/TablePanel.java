package org.doogal.swing;

import static org.doogal.swing.SwingUtil.*;

import java.awt.BorderLayout;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.Map;

import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;

import org.doogal.core.table.TableType;
import org.doogal.core.util.Size;

final class TablePanel extends JPanel implements ViewPanel {

    private static final long serialVersionUID = 1L;
    private final Map<String, Action> actions;
    private final JTable table;
    private final JScrollPane scrollPane;

    TablePanel(final Map<String, Action> actions) {
        super(new BorderLayout());

        this.actions = actions;
        table = new JTable(new TableAdapter());
        scrollPane = new JScrollPane(table);
        scrollPane.setFocusable(false);
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
                        setVisible();
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
                        if (null != name) {
                            final Action action = actions.get(name);
                            action.actionPerformed(new ActionEvent(ev
                                    .getSource(), ev.getID(), (String) action
                                    .getValue(Action.NAME)));
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

        add(scrollPane, BorderLayout.CENTER);
    }

    public final void close() throws IOException {
    }

    public final void setPage(int n) {
        setScrollPage(scrollPane.getVerticalScrollBar(), n);
    }

    public final void nextPage() {
        nextScrollPage(scrollPane.getVerticalScrollBar());
    }

    public final void prevPage() {
        prevScrollPage(scrollPane.getVerticalScrollBar());
    }

    public final void setVisible() {
        if (!table.getSelectionModel().isSelectionEmpty()) {
            final TableAdapter tableModel = (TableAdapter) table
                    .getModel();
            final String[] names = tableModel.getType()
                    .getActions();
            for (int i = 0; i < names.length; ++i)
                actions.get(names[i]).setEnabled(true);
        }
    }
    
    public final TableType getType() {
        final TableAdapter tableModel = (TableAdapter) table.getModel();
        return tableModel.getType();
    }

    public final Object[] getSelection() {
        final int[] rows = table.getSelectedRows();
        final Object[] ids = new Object[rows.length];
        for (int i = 0; i < rows.length; ++i)
            ids[i] = table.getValueAt(rows[i], 0);
        return ids;
    }

    final void setModel(TableModel model) {
        table.setModel(model);
        setRowSorter(table);
    }
}
