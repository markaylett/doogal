package org.doogal.swing;

import static org.doogal.core.Utility.printResource;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;

import javax.mail.internet.ParseException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.apache.commons.logging.Log;
import org.doogal.core.AsyncDoogal;
import org.doogal.core.Controller;
import org.doogal.core.DataSet;
import org.doogal.core.DocumentSet;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;
import org.doogal.core.Summary;
import org.doogal.core.SyncDoogal;
import org.doogal.core.view.AbstractView;
import org.doogal.core.view.View;

public final class Scratch extends JPanel implements Doogal {

    private static final long serialVersionUID = 1L;
    private static final int SMALL_FONT = 12;
    private static final int BIG_FONT = 14;

    private final History history;
    private final JTextArea console;
    private final JTextField prompt;
    private final Doogal doogal;

    private static Frame getFrame(Container c) {
        while (c != null) {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }
        return null;
    }

    private static Component newScrollPane(Component view) {
        final JScrollPane scrollPane = new JScrollPane();
        scrollPane.setViewportView(view);
        return scrollPane;
    }

    private static TableModel newTableModel(DataSet dataSet) throws IOException {
        TableModel model;
        if (null == dataSet)
            model = new DefaultTableModel();
        else if (dataSet instanceof DocumentSet) {
            final DocumentSet docSet = (DocumentSet) dataSet;
            final Summary[] ls = new Summary[docSet.size()];
            for (int i = 0; i < ls.length; ++i)
                ls[i] = docSet.getSummary(i);
            model = new DocumentModel(ls);
        } else {
            final String[] ls = new String[dataSet.size()];
            for (int i = 0; i < ls.length; ++i)
                ls[i] = dataSet.get(i);
            model = new DataModel(ls);
        }
        return model;
    }

    private static void postWindowClosingEvent(Frame frame) {
        final WindowEvent windowClosingEvent = new WindowEvent(frame,
                WindowEvent.WINDOW_CLOSING);
        frame.getToolkit().getSystemEventQueue().postEvent(windowClosingEvent);
    }

    private final void setPrompt(boolean b) {
        prompt.setEnabled(b);
        if (b)
            prompt.requestFocus();
    }

    private final void setEmacs() {

        final Map<String, Action> actions = new HashMap<String, Action>();
        final Action[] actionsArray = prompt.getActions();
        for (final Action a : actionsArray)
            actions.put(a.getValue(Action.NAME).toString(), a);

        final Keymap keymap = JTextComponent.addKeymap("emacs", prompt
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
                prompt.setText("");
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
                prompt.setText(history.next());
            }
        };

        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        keymap.addActionForKeyStroke(key, action);

        action = new AbstractAction() {
            private static final long serialVersionUID = 1L;

            public final void actionPerformed(ActionEvent e) {
                prompt.setText(history.prev());
            }
        };

        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        keymap.addActionForKeyStroke(key, action);

        prompt.setKeymap(keymap);
    }

    Scratch() throws Exception {
        super(new BorderLayout());

        history = new History();

        final JTable table = new JTable();

        console = new JTextArea();
        console.setMargin(new Insets(5, 5, 5, 5));
        console.setFont(new Font("Monospaced", Font.PLAIN, SMALL_FONT));

        console.setEditable(false);
        console.setFocusable(false);
        console.setLineWrap(true);

        prompt = new JTextField();
        prompt.setMargin(new Insets(5, 5, 5, 5));
        prompt.setFont(new Font("Monospaced", Font.PLAIN, BIG_FONT));

        setPrompt(false);
        setEmacs();

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                newScrollPane(console), newScrollPane(table));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getToolkit().getScreenSize().height / 4);

        add(splitPane, BorderLayout.CENTER);
        add(prompt, BorderLayout.SOUTH);

        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(new TextAreaStream(console),
                true);
        final Log log = new StandardLog(out, out);
        final View view = new AbstractView(out, log) {
            public final void setPage(String n) throws EvalException,
                    IOException {
            }

            public final void showPage() throws EvalException, IOException {
            }

            public final void nextPage() throws EvalException, IOException {
            }

            public final void prevPage() throws EvalException, IOException {
            }

            @Override
            public final void setDataSet(final DataSet dataSet)
                    throws IOException {
                super.setDataSet(dataSet);
                final TableModel model = newTableModel(dataSet);
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        table.setModel(model);
                    }
                });
            }
        };
        final Controller controller = new Controller() {
            public final void exit(boolean interact) throws ExitException {
                if (!interact)
                    throw new ExitException();
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        log.info("exiting...");
                        postWindowClosingEvent(getFrame(Scratch.this));
                    }
                });
            }

            public final void ready() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        setPrompt(true);
                    }
                });
            }
        };
        doogal = new AsyncDoogal(log, SyncDoogal.newInstance(env, view,
                controller));

        prompt.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final String s = prompt.getText();
                history.add(s);
                final Reader reader = new StringReader(s);
                prompt.setText("");
                setPrompt(false);
                try {
                    Shellwords.parse(reader, doogal);
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });

        printResource("motd.txt", out);
    }

    public final void close() throws IOException {
        doogal.close();
    }

    public final void eval(String cmd, Object... args) throws EvalException {
        doogal.eval(cmd, args);
    }

    public final void eval() throws EvalException {
        doogal.eval();
    }

    public final void batch(Reader reader) throws EvalException, IOException,
            ParseException {
        doogal.batch(reader);
    }

    public final void batch(File file) throws EvalException, IOException,
            ParseException {
        doogal.batch(file);
    }

    public final void config() throws EvalException, IOException,
            ParseException {
        doogal.config();
    }

    private static void run() throws Exception {
        final JFrame f = new JFrame("Doogal");
        final Scratch m = new Scratch();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public final void windowClosing(WindowEvent ev) {
                try {
                    m.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        f.setLayout(new BorderLayout());
        f.add(m, BorderLayout.CENTER);
        f.pack();

        final Dimension d = f.getToolkit().getScreenSize();
        f.setSize(d.width / 2, d.height / 2);
        f.setVisible(true);
        m.config();
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            public final void run() {
                try {
                    Scratch.run();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
