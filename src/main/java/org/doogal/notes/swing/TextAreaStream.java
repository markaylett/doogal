package org.doogal.notes.swing;

import static org.doogal.notes.domain.Constants.MAX_RESULTS;

import java.awt.EventQueue;
import java.io.OutputStream;

import javax.swing.JTextArea;
import javax.swing.text.BadLocationException;

final class TextAreaStream extends OutputStream {

    private static final int MAX_LINES = MAX_RESULTS * 10;

    private final JTextArea textArea;
    private final boolean scroll;
    private final StringBuilder sb;

    private final void append(String s) {
        textArea.append(s);
        try {
            int lc = textArea.getLineCount();
            if (MAX_LINES < lc) {
                textArea.replaceRange(null, 0, textArea.getLineEndOffset(lc
                        - MAX_LINES - 1));
                lc = MAX_LINES;
            }
            // Scroll to top.
            textArea.setCaretPosition(scroll ? textArea.getDocument()
                    .getLength() : 0);
        } catch (final BadLocationException e) {
            e.printStackTrace();
        }
    }

    TextAreaStream(final JTextArea textArea, boolean scroll) {
        this.textArea = textArea;
        this.scroll = scroll;
        sb = new StringBuilder();
    }

    @Override
    public final void flush() {
    }

    @Override
    public final void close() {
    }

    @Override
    public final void write(int b) {

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
