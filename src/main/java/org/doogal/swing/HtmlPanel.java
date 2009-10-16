package org.doogal.swing;

import static org.doogal.core.Constants.TINY_FONT;
import static org.doogal.core.Utility.*;
import static org.doogal.swing.SwingUtil.nextScrollPage;
import static org.doogal.swing.SwingUtil.prevScrollPage;
import static org.doogal.swing.SwingUtil.setScrollPage;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Highlighter;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.StyleSheet;

import org.doogal.core.table.TableType;
import org.doogal.core.util.WriteOnce;

final class HtmlPanel extends JPanel implements ViewPanel {

    private static final long serialVersionUID = 1L;
    private final JTextPane textPane;
    private final JScrollPane scrollPane;
    private final JTextField find;
    private final JLabel matches;

    private static final Highlighter.HighlightPainter FIND_HIGHLIGHT_PAINTER = new DefaultHighlighter.DefaultHighlightPainter(
            Color.yellow);

    private final void find(Pattern pattern) {
        final WriteOnce<Integer> once = new WriteOnce<Integer>();
        final Highlighter highlighter = textPane.getHighlighter();
        highlighter.removeAllHighlights();

        int matches = 0;

        final HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        for (final HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.CONTENT); it
                .isValid(); it.next())
            try {
                final String fragment = doc.getText(it.getStartOffset(), it
                        .getEndOffset()
                        - it.getStartOffset());
                final Matcher matcher = pattern.matcher(fragment);
                while (matcher.find()) {
                    highlighter.addHighlight(it.getStartOffset()
                            + matcher.start(), it.getStartOffset()
                            + matcher.end(), FIND_HIGHLIGHT_PAINTER);
                    once.set(it.getStartOffset());
                    ++matches;
                }
            } catch (final BadLocationException ex) {
            }
        if (!once.isEmpty())
            textPane.setCaretPosition(once.get());
        this.matches.setText(String.format("%d matches", matches));
    }

    private final void find(String pattern) {
        if (null == pattern || 0 == pattern.length()) {
            final Highlighter highlighter = textPane.getHighlighter();
            highlighter.removeAllHighlights();
            matches.setText("");
        } else
            find(Pattern.compile(pattern, Pattern.CASE_INSENSITIVE));
    }

    HtmlPanel() throws IOException {
        super(new BorderLayout());

        textPane = new JTextPane();
        find = new JTextField();
        matches = new JLabel();

        textPane.setEditable(false);
        textPane.setFocusable(false);
        textPane.addHyperlinkListener(new HyperlinkListener() {

            public final void hyperlinkUpdate(HyperlinkEvent ev) {
                if (ev.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
                    final JEditorPane pane = (JEditorPane) ev.getSource();
                    if (ev instanceof HTMLFrameHyperlinkEvent) {
                        final HTMLFrameHyperlinkEvent evt = (HTMLFrameHyperlinkEvent) ev;
                        final HTMLDocument doc = (HTMLDocument) pane
                                .getDocument();
                        doc.processHTMLFrameHyperlinkEvent(evt);
                    } else if (Desktop.isDesktopSupported())
                        try {
                            Desktop.getDesktop().browse(ev.getURL().toURI());
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                }
            }
        });

        final HTMLEditorKit kit = new HTMLEditorKit() {
            private static final long serialVersionUID = 1L;

            @Override
            public final Document createDefaultDocument() {
                final HTMLDocument doc = (HTMLDocument) super
                        .createDefaultDocument();
                // Load synchronously.
                doc.setAsynchronousLoadPriority(-1);
                return doc;
            }
        };
        
        final StyleSheet styleSheet = kit.getStyleSheet();
        final InputStream is = getClass().getResourceAsStream("/doogal.css");
        try {
            styleSheet.loadRules(newBufferedReader(is), null);
        } finally {
            is.close();
        }
        
        textPane.setEditorKit(kit);

        final Document doc = kit.createDefaultDocument();
        textPane.setDocument(doc);

        find.setColumns(16);
        find.setFont(new Font("Dialog", Font.PLAIN, TINY_FONT));
        find.setMargin(new Insets(2, 2, 2, 2));
        find.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                find(find.getText());
            }
        });

        final JLabel label = new JLabel("Quick Find: ");
        label.setLabelFor(find);

        final JButton clear = new JButton("Clear");
        clear.setMargin(new Insets(1, 5, 0, 5));
        clear.setFont(new Font("Dialog", Font.PLAIN, 9));
        clear.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent e) {
                find.setText("");
                find("");
            }
        });

        final JPanel findPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        findPanel.add(label);
        findPanel.add(find);
        findPanel.add(clear);
        findPanel.add(matches);

        scrollPane = new JScrollPane(textPane,
                ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        scrollPane.setFocusable(false);
        final JScrollBar scrollBar = scrollPane.getVerticalScrollBar();
        scrollBar.setBlockIncrement(scrollBar.getBlockIncrement() * 20);
        scrollBar.setUnitIncrement(scrollBar.getUnitIncrement() * 20);

        add(scrollPane, BorderLayout.CENTER);
        add(findPanel, BorderLayout.SOUTH);
    }

    public final void close() throws IOException {
    }

    public final void setPage(int n) {
        setScrollPage(scrollPane.getVerticalScrollBar(), n);
    }

    public final void nextPage() {
        nextScrollPage(scrollPane.getVerticalScrollBar());
    }

    public final void prevPage() {
        prevScrollPage(scrollPane.getVerticalScrollBar());
    }

    public final void setVisible() {

    }

    public final TableType getType() {
        return TableType.ALIAS;
    }

    public final Object[] getSelection() {
        return new Object[0];
    }

    final void setPage(String title, File path) throws IOException,
            MalformedURLException {

        final Document doc = textPane.getDocument();
        doc.putProperty(Document.TitleProperty, title);
        // Clearing stream forces refresh.
        doc.putProperty(Document.StreamDescriptionProperty, null);
        textPane.setPage(path.toURI().toURL());
        find(find.getText());
    }

    final JScrollBar getVerticalScrollBar() {
        return scrollPane.getVerticalScrollBar();
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public final void run() {
                try {
                    final JFrame f = new JFrame("HtmlPanel");
                    final HtmlPanel panel = new HtmlPanel();
                    f.setContentPane(panel);

                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    final Dimension d = f.getToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                    f.setVisible(true);
                    panel.setPage("Test", new File(
                            "c:/tmp/ice_release_procedure.html"));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
