package org.doogal.notes.swing;

import static org.doogal.notes.domain.Constants.LARGE_FONT;
import static org.doogal.notes.domain.Constants.MEDIUM_FONT;
import static org.doogal.notes.domain.Constants.PROMPT;
import static org.doogal.notes.swing.SwingUtil.setEmacsKeyMap;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;

import javax.mail.internet.ParseException;
import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import org.apache.commons.logging.Log;
import org.doogal.notes.util.EvalException;
import org.doogal.notes.util.Interpreter;
import org.doogal.notes.util.Shellwords;

final class CommandPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final Interpreter interp;
    private final Log log;
    private final History history;
    private final JTextField textField;

    CommandPanel(Interpreter interp, Log log) {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));

        this.interp = interp;
        this.log = log;
        history = new History();
        textField = new JTextField();

        final JLabel label = new JLabel(PROMPT);
        label.setFont(new Font("Dialog", Font.BOLD, LARGE_FONT));

        textField.setMargin(new Insets(2, 2, 2, 2));
        textField.setFont(new Font("Monospaced", Font.PLAIN, MEDIUM_FONT));

        textField.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final String s = textField.getText();
                history.add(s);
                final Reader reader = new StringReader(s);
                textField.setText("");
                try {
                    Shellwords.parse(reader, CommandPanel.this.interp);
                } catch (final EvalException e) {
                    CommandPanel.this.log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    CommandPanel.this.log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    CommandPanel.this.log.error(e.getLocalizedMessage());
                }
            }
        });

        label.setLabelFor(textField);

        add(label, BorderLayout.WEST);
        add(textField, BorderLayout.CENTER);

        setPrompt(false);
        setEmacsKeyMap(textField, history);
    }

    final void setPrompt(boolean b) {
        textField.setEnabled(b);
        textField.setEditable(b);
        if (b)
            textField.requestFocus();
    }
}
