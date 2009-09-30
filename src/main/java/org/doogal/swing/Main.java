package org.doogal.swing;

import static org.doogal.core.Utility.printResource;

import java.awt.BorderLayout;
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
import javax.mail.internet.ParseException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;

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

    Main() throws Exception {
        super(new BorderLayout());

        display = new JTextArea();
        display.setEditable(false);
        display.setLineWrap(true);
        display.setMargin(new Insets(5, 5, 5, 5));
        display.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        command = new JTextField(32);
        command.setEditable(false);

        final JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(display);
        add(scrollPanel, BorderLayout.CENTER);
        add(command, BorderLayout.SOUTH);

        final PrintWriter out = new PrintWriter(new TextAreaStream(display),
                true);
        final Log log = new StandardLog(out, out);
        final Environment env = new Environment();
        final Controller controller = new Controller() {
            public final void close() {
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
        doogal = new AsyncDoogal(log, SyncDoogal.newInstance(out, log, env),
                controller);

        command.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final Reader reader = new StringReader(command.getText());
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

    public final void readConfig(File config) throws EvalException,
            IOException, ParseException {
        doogal.readConfig(config);
    }

    public final void readConfig() throws EvalException, IOException,
            ParseException {
        doogal.readConfig();
    }

    public final void setDefault(String def) {
        doogal.setDefault(def);
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
        f.setVisible(true);
        final Dimension d = f.getToolkit().getScreenSize();
        f.setSize(d.width / 2, d.height / 2);
        m.readConfig();
        m.setDefault("next");
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
