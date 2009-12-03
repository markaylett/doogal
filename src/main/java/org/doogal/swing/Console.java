package org.doogal.swing;

import static org.doogal.core.Utility.printResource;
import static org.doogal.swing.SwingUtil.postWindowClosingEvent;
import static org.doogal.swing.SwingUtil.setEmacsKeyMap;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.Map;

import javax.mail.internet.ParseException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.WindowConstants;

import org.apache.commons.logging.Log;
import org.doogal.core.AsyncDoogal;
import org.doogal.core.Controller;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.ExitException;
import org.doogal.core.PromptDoogal;
import org.doogal.core.Repo;
import org.doogal.core.SyncDoogal;
import org.doogal.core.command.Command;
import org.doogal.core.table.TableType;
import org.doogal.core.util.EvalException;
import org.doogal.core.util.Shellwords;
import org.doogal.core.util.StandardLog;
import org.doogal.core.view.PrintView;
import org.doogal.core.view.View;

public final class Console extends JPanel implements Doogal {

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

    private final void setPrompt(boolean b) {
        prompt.setEnabled(b);
        if (b) {
            prompt.requestFocus();
            prompt.setBackground(Color.black);
        } else
            prompt.setBackground(Color.darkGray);
    }

    Console() throws Exception {
        super(new BorderLayout());

        history = new History();

        console = new JTextArea();
        console.setMargin(new Insets(5, 5, 5, 5));
        console.setFont(new Font("Monospaced", Font.PLAIN, SMALL_FONT));
        console.setForeground(Color.green);
        console.setBackground(Color.black);

        console.setDoubleBuffered(true);
        console.setEditable(false);
        console.setFocusable(false);
        console.setLineWrap(true);

        prompt = new JTextField();
        prompt.setMargin(new Insets(5, 5, 5, 5));
        prompt.setFont(new Font("Monospaced", Font.PLAIN, BIG_FONT));
        prompt.setForeground(Color.green);
        prompt.setCaretColor(Color.green);

        setPrompt(false);
        setEmacsKeyMap(prompt, history);

        add(new JScrollPane(console), BorderLayout.CENTER);
        add(prompt, BorderLayout.SOUTH);

        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(new TextAreaStream(console,
                true), true);
        final Log log = new StandardLog(out, out);
        final View view = new PrintView(out, log);
        final Controller controller = new Controller() {
            public final void exit(boolean interact) throws ExitException {
                if (!interact)
                    throw new ExitException();
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        log.info("exiting...");
                        postWindowClosingEvent(getFrame(Console.this));
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

        final Repo repo = new Repo(env.getRepo());
        repo.init();
        doogal = new AsyncDoogal(log, new PromptDoogal(controller,
                new SyncDoogal(env, view, controller, repo)));

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

    public final void destroy() {
        doogal.destroy();
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

    public final void setSelection(TableType type, Object... args) {
        doogal.setSelection(type, args);
    }

    public final void clearSelection() {
        doogal.clearSelection();
    }

    public final Map<String, Command> getBuiltins() {
        return doogal.getBuiltins();
    }

    private static void run() throws Exception {
        final JFrame f = new JFrame("Doogal");
        final Console c = new Console();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public final void windowClosing(WindowEvent ev) {
                c.destroy();
            }
        });
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        f.setContentPane(c);

        final Dimension d = f.getToolkit().getScreenSize();
        f.setSize(d.width / 2, d.height / 2);
        f.setVisible(true);
        c.config();
    }

    public static void main(String[] args) {

        System.setProperty("line.separator", "\n");
        EventQueue.invokeLater(new Runnable() {

            public final void run() {
                try {
                    try {
                        UIManager.setLookAndFeel(UIManager
                                .getSystemLookAndFeelClassName());
                    } catch (final Exception e) {
                    }
                    Console.run();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
