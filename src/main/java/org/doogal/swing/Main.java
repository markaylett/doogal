package org.doogal.swing;

import static org.doogal.core.Utility.printResource;

import java.awt.BorderLayout;
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
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.WindowConstants;
import javax.swing.text.DefaultEditorKit;
import javax.swing.text.JTextComponent;
import javax.swing.text.Keymap;

import org.apache.commons.logging.Log;
import org.doogal.core.AsyncDoogal;
import org.doogal.core.Controller;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;
import org.doogal.core.SyncDoogal;

public final class Main extends JPanel implements Doogal {

    private static final long serialVersionUID = 1L;
    private static final int FONT_SIZE = 12;

    private final History history;
    private final JTextArea display;
    private final JTextField command;
    private final Doogal doogal;

    private static Frame getFrame(Container c) {
        while (c != null) {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }
        return null;
    }

    private static void postWindowClosingEvent(Frame frame) {
        final WindowEvent windowClosingEvent = new WindowEvent(frame,
                WindowEvent.WINDOW_CLOSING);
        frame.getToolkit().getSystemEventQueue().postEvent(windowClosingEvent);
    }

    private void setEmacs(JTextComponent textPane) {

        final Map<String, Action> actions = new HashMap<String, Action>();
        final Action[] actionsArray = textPane.getActions();
        for (final Action a : actionsArray) {
            actions.put(a.getValue(Action.NAME).toString(), a);
        }

        final Keymap keymap = JTextComponent.addKeymap("emacs", textPane
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
                command.setText("");
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
                command.setText(history.next());
            }            
        };

        key = KeyStroke.getKeyStroke(KeyEvent.VK_N, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0);
        keymap.addActionForKeyStroke(key, action);

        action = new AbstractAction() {
            private static final long serialVersionUID = 1L;
            public final void actionPerformed(ActionEvent e) {
                command.setText(history.previous());
            }            
        };
        
        key = KeyStroke.getKeyStroke(KeyEvent.VK_P, Event.CTRL_MASK);
        keymap.addActionForKeyStroke(key, action);
        key = KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0);
        keymap.addActionForKeyStroke(key, action);
        
        textPane.setKeymap(keymap);
    }

    Main() throws Exception {
        super(new BorderLayout());

        history = new History();
        
        display = new JTextArea();
        display.setEditable(false);
        display.setLineWrap(true);
        display.setMargin(new Insets(5, 5, 5, 5));
        display.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        command = new JTextField(32);
        command.setEditable(false);
        setEmacs(command);

        final JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(display);
        add(scrollPanel, BorderLayout.CENTER);
        add(command, BorderLayout.SOUTH);

        final PrintWriter out = new PrintWriter(new TextAreaStream(display),
                true);
        final Log log = new StandardLog(out, out);
        final Environment env = new Environment();
        final Controller controller = new Controller() {
            public final void exit() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        postWindowClosingEvent(getFrame(Main.this));
                    }
                });
            }

            public final void ready() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        command.setEditable(true);
                        command.requestFocusInWindow();
                    }
                });
            }
        };
        doogal = new AsyncDoogal(log, SyncDoogal.newInstance(out, log, env,
                controller));

        command.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final String s = command.getText();
                history.add(s);
                final Reader reader = new StringReader(s);
                command.setText("");
                command.setEditable(false);
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
        final Main m = new Main();
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
                    Main.run();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
