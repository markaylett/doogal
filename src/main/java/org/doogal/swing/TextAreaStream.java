package org.doogal.swing;

import java.awt.EventQueue;
import java.awt.Rectangle;
import java.io.IOException;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

final class TextAreaStream extends OutputStream {

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
