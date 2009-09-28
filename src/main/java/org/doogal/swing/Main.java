package org.doogal.swing;

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
import java.io.Writer;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;

public final class Main extends JPanel {

    public final class TextAreaWriter extends Writer {

        private final JTextArea textArea;

        public TextAreaWriter(final JTextArea textArea) {
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
        }
    }

    private static final long serialVersionUID = 1L;

    private static final int FONT_SIZE = 12;

    private final JTextArea display = new JTextArea();
    private final JTextField command = new JTextField(32);

    Main() {
        super(new BorderLayout());

        display.setEditable(false);
        display.setLineWrap(true);
        display.setMargin(new Insets(5, 5, 5, 5));
        display.setFont(new Font("Monospaced", Font.PLAIN, FONT_SIZE));

        final JScrollPane scrollPanel = new JScrollPane();
        scrollPanel.setViewportView(display);
        add(scrollPanel, BorderLayout.CENTER);

        add(command, BorderLayout.SOUTH);

        command.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                append(command.getText());
                command.setText("");
            }
        });
    }

    final void append(String line) {
        display.append(line + "\n");
        try {
            display.scrollRectToVisible(new Rectangle(0, display
                    .getLineEndOffset(display.getLineCount()), 1, 1));
        } catch (BadLocationException e) {
        }
    }

    final void setFocus() {
        command.requestFocusInWindow();
    }

    public static void main(String[] args) {

        EventQueue.invokeLater(new Runnable() {

            public final void run() {

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
            }
        });
    }
}
