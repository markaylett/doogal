package org.doogal.swing;

import java.awt.Container;
import java.awt.Event;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JTable;
import javax.swing.KeyStroke;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.doogal.core.table.TableType;

final class SwingUtil {
    private SwingUtil() {

    }

    static Frame parentFrame(Container c) {
        while (c != null) {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }
        return null;
    }

    static void postWindowClosingEvent(Frame frame) {
        final WindowEvent windowClosingEvent = new WindowEvent(frame,
                WindowEvent.WINDOW_CLOSING);
        frame.getToolkit().getSystemEventQueue().postEvent(windowClosingEvent);
    }

    static void setEmacsKeyMap(final JTextComponent comp, final History history) {

        final Map<String, Action> actions = new HashMap<String, Action>();
        final Action[] actionsArray = comp.getActions();
        for (final Action a : actionsArray)
            actions.put(a.getValue(Action.NAME).toString(), a);

        final Keymap keymap = JTextComponent.addKeymap("emacs", comp
                .getKeymap());

        Action action;
        KeyStroke key;

        action = actions.get(DefaultEditorKit.beginLineAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_A, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);

        action = actions.get(DefaultEditorKit.previousWordAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.ALT_MASK);
        keymap.addActionForKeyStroke(key, action);

        action = actions.get(DefaultEditorKit.backwardAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_B, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);

        key = KeyStroke.getKeyStroke(KeyEvent.VK_C, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public final void actionPerformed(ActionEvent e) {
                comp.setText("");
            }
        });

        action = actions.get(DefaultEditorKit.endLineAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_E, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);

        action = actions.get(DefaultEditorKit.nextWordAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.ALT_MASK);
        keymap.addActionForKeyStroke(key, action);

        action = actions.get(DefaultEditorKit.forwardAction);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_F, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);

        action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public final void actionPerformed(ActionEvent e) {
                comp.setText(history.next());
            }
        };

        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        keymap.addActionForKeyStroke(key, action);

        action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public final void actionPerformed(ActionEvent e) {
                comp.setText(history.prev());
            }
        };

        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        keymap.addActionForKeyStroke(key, action);

        comp.setKeymap(keymap);
    }

    static void setRowSorter(JTable table) {
        final TableModel model = table.getModel();
        table.setRowSorter(new TableRowSorter<TableModel>(model));
    }

    static void setScrollPage(JScrollBar scrollBar, int n) {
        int value = n * scrollBar.getBlockIncrement(1);
        value = Math.min(value, scrollBar.getMaximum());
        scrollBar.setValue(value);
    }

    static void nextScrollPage(JScrollBar scrollBar) {
        int value = scrollBar.getValue();
        value += scrollBar.getBlockIncrement(1);
        value = Math.min(value, scrollBar.getMaximum());
        scrollBar.setValue(value);
    }

    static void prevScrollPage(JScrollBar scrollBar) {
        int value = scrollBar.getValue();
        value -= scrollBar.getBlockIncrement(-1);
        value = Math.max(value, scrollBar.getMinimum());
        scrollBar.setValue(value);
    }

    static JPopupMenu newPopupMenu(TableType type, Map<String, Action> actions) {
        final String[] names = type.getActions();
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
}
