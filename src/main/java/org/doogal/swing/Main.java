package org.doogal.swing;

import static org.doogal.core.Utility.printResource;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.Writer;

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
import org.doogal.core.Factory;
import org.doogal.core.Interpreter;
import org.doogal.core.Shellwords;
import org.doogal.core.StandardLog;

public final class Main extends JPanel {
    private final Interpreter doogal;
    private static final class TextAreaWriter extends Writer {

        private final JTextArea textArea;

        TextAreaWriter(final JTextArea textArea) {
            this.textArea = textArea;
        }

        @Override
        public final void flush() {
        }

        @Override
        public final void close() {
        }

        @Override
        public final void write(char[] cbuf, int off, int len)
                throws IOException {
            textArea.append(new String(cbuf, off, len));
            try {
                textArea.scrollRectToVisible(new Rectangle(0, textArea
                        .getLineEndOffset(textArea.getLineCount()), 1, 1));
            } catch (BadLocationException e) {
                e.printStackTrace();
            }
        }
    }

    private static final long serialVersionUID = 1L;

    private static final int FONT_SIZE = 12;

    private final JTextArea display = new JTextArea();
    private final JTextField command = new JTextField(32);

    Main() throws Exception {
        super(new BorderLayout());

        display.setEditable(false);
        display.setLineWrap(true);
        display.setMargin(new Insets(5, 5, 5, 5));
        display.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        final JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(display);
        add(scrollPanel, BorderLayout.CENTER);
        add(command, BorderLayout.SOUTH);
        
        final PrintWriter out = new PrintWriter(new TextAreaWriter(display));
        final Log log = new StandardLog(out, out);
        final Environment env = new Environment();

        printResource("motd.txt", out);
        doogal = Factory.newDoogal(out, out, log, env);

        command.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final Reader reader = new StringReader(command.getText());
                command.setText("");
                try {
                    Shellwords.parse(reader, doogal);
                } catch (EvalException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    final void append(String line) {
        display.append(line + "\n");
        try {
            display.scrollRectToVisible(new Rectangle(0, display
                    .getLineEndOffset(display.getLineCount()), 1, 1));
        } catch (final BadLocationException e) {
        }
    }

    final void setFocus() {
        command.requestFocusInWindow();
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            public final void run() {
                try {
                    final Main m = new Main();
                    final JFrame f = new JFrame("Doogal");
                    f.setLayout(new BorderLayout());
                    f.add(m, BorderLayout.CENTER);
                    f.pack();
                    f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    f.setVisible(true);
                    m.setFocus();
                    final Dimension d = Toolkit.getDefaultToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                } catch (Exception e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }
        });
    }
}
