package org.doogal.swing;

import static org.doogal.core.Utility.printResource;

import java.awt.AWTEvent;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.mail.internet.ParseException;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

import org.apache.commons.logging.Log;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Factory;
import org.doogal.core.Interpreter;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;

public final class Main extends JPanel {

    private static final long serialVersionUID = 1L;
    private static final int FONT_SIZE = 12;

    private static Frame findFrame(Container c) {
        while (c != null) {
            if (c instanceof Frame)
                return (Frame) c;
            c = c.getParent();
        }
        return null;
    }

    private static void postWindowClosingEvent(Frame frame) {
        WindowEvent windowClosingEvent = new WindowEvent(frame,
                WindowEvent.WINDOW_CLOSING);
        frame.getToolkit().getSystemEventQueue().postEvent(windowClosingEvent);
    }

    private static final class TextAreaStream extends OutputStream {

        private final JTextArea textArea;
        private final StringBuilder sb;

        private final void append(String s) {
            textArea.append(s);
            try {
                final int lc = textArea.getLineCount();
                textArea.scrollRectToVisible(new Rectangle(0, textArea
                        .getLineEndOffset(lc - 1), 1, 1));
            } catch (final BadLocationException e) {
                e.printStackTrace();
            }
        }

        TextAreaStream(final JTextArea textArea) {
            this.textArea = textArea;
            this.sb = new StringBuilder();
        }

        @Override
        public final void flush() {
        }

        @Override
        public final void close() {
        }

        @Override
        public final void write(int b) throws IOException {

            if ('\r' == b)
                return;

            sb.append((char) b);
            if ('\n' == b) {

                final String s = sb.toString();
                sb.setLength(0);

                if (EventQueue.isDispatchThread())
                    append(s);
                else
                    EventQueue.invokeLater(new Runnable() {
                        public final void run() {
                            append(s);
                        }
                    });
            }
        }
    }

    private final ExecutorService executor;
    private final JTextArea display;
    private final JTextField command;
    private Interpreter doogal;

    Main() {
        super(new BorderLayout());

        this.executor = Executors.newSingleThreadExecutor();
        this.display = new JTextArea();
        this.command = new JTextField(32);
        this.doogal = null;

        enableEvents(AWTEvent.WINDOW_EVENT_MASK);

        display.setEditable(false);
        display.setLineWrap(true);
        display.setMargin(new Insets(5, 5, 5, 5));
        display.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        final JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(display);
        add(scrollPanel, BorderLayout.CENTER);
        add(command, BorderLayout.SOUTH);

        executor.execute(new Runnable() {
            public final void run() {
                try {
                    init();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public final void close() {
        executor.shutdown();
        try {
            executor.awaitTermination(5, TimeUnit.SECONDS);
        } catch (final InterruptedException e) {
            e.printStackTrace();
        }
        if (!executor.isShutdown())
            executor.shutdownNow();
    }

    final void init() throws Exception {

        final PrintWriter out = new PrintWriter(new TextAreaStream(display),
                true);
        final Log log = new StandardLog(out, out);
        final Environment env = new Environment();

        printResource("motd.txt", out);
        doogal = Factory.newDoogal(out, out, log, env);

        command.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final Reader reader = new StringReader(command.getText());
                command.setText("");
                command.setEnabled(false);
                command.setEditable(false);
                try {
                    Shellwords.parse(reader, new Interpreter() {
                        public final void close() {

                        }

                        public final void eval(final String cmd,
                                final Object... args) throws EvalException {
                            executor.execute(new Runnable() {
                                public final void run() {
                                    try {
                                        doogal.eval(cmd, args);
                                    } catch (final ExitException e) {
                                        try {
                                            doogal.close();
                                        } catch (final IOException e1) {
                                            e1.printStackTrace();
                                        }
                                        EventQueue.invokeLater(new Runnable() {
                                            public final void run() {
                                                postWindowClosingEvent(findFrame(Main.this));
                                            }
                                        });
                                    } catch (final EvalException e) {
                                        e.printStackTrace();
                                    } finally {
                                        EventQueue.invokeLater(new Runnable() {
                                            public final void run() {
                                                setReady(true);
                                            }
                                        });
                                    }
                                }
                            });
                        }

                        public final void eval() throws EvalException {
                            executor.execute(new Runnable() {
                                public final void run() {
                                    try {
                                        doogal.eval("next");
                                    } catch (final EvalException e) {
                                        e.printStackTrace();
                                    } finally {
                                        EventQueue.invokeLater(new Runnable() {
                                            public final void run() {
                                                setReady(true);
                                            }
                                        });
                                    }
                                }
                            });
                        }
                    });
                } catch (final EvalException e) {
                    e.printStackTrace();
                } catch (final IOException e) {
                    e.printStackTrace();
                } catch (final ParseException e) {
                    e.printStackTrace();
                }
            }
        });

        EventQueue.invokeLater(new Runnable() {
            public final void run() {
                setReady(true);
            }
        });
    }

    final void setReady(boolean ready) {
        command.setEditable(ready);
        if (ready)
            command.requestFocusInWindow();
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            public final void run() {
                try {
                    final JFrame f = new JFrame("Doogal");
                    final Main m = new Main();
                    f.addWindowListener(new WindowAdapter() {
                        public final void windowClosing(WindowEvent e) {
                            m.close();
                        }
                    });
                    f
                            .setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                    f.setLayout(new BorderLayout());
                    f.add(m, BorderLayout.CENTER);
                    f.pack();
                    f.setVisible(true);
                    m.setReady(false);
                    final Dimension d = f.getToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
