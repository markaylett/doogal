package org.doogal.swing;

import static org.doogal.core.Constants.LARGE_FONT;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.BorderFactory;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultHighlighter;
import javax.swing.text.Document;
import javax.swing.text.Element;
import javax.swing.text.Highlighter;
import javax.swing.text.View;
import javax.swing.text.ViewFactory;
import javax.swing.text.html.HTML;
import javax.swing.text.html.HTMLDocument;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.HTMLFrameHyperlinkEvent;
import javax.swing.text.html.ParagraphView;

import org.doogal.core.util.WriteOnce;

final class HtmlPanel extends JPanel {
    private final class WrapParagraphView extends ParagraphView {
        private final int wrap;

        public WrapParagraphView(Element elem) {
            super(elem);
            final Dimension d = getToolkit().getScreenSize();
            wrap = d.width / 2;
        }

        @Override
        public final void layout(int width, int height) {
            super.layout(wrap, height);
        }

        @Override
        public final float getMinimumSpan(int axis) {
            return super.getPreferredSpan(axis);
        }
    }

    private static final long serialVersionUID = 1L;
    private final JLabel title;
    private final JTextPane textPane;

    private static final Highlighter.HighlightPainter FIND_HIGHLIGHT_PAINTER =
        new DefaultHighlighter.DefaultHighlightPainter(
            Color.yellow);

    private final void find(Pattern pattern) {
        final WriteOnce<Integer> once = new WriteOnce<Integer>();
        final Highlighter highlighter = textPane.getHighlighter();
        highlighter.removeAllHighlights();

        final HTMLDocument doc = (HTMLDocument) textPane.getDocument();
        for (final HTMLDocument.Iterator it = doc.getIterator(HTML.Tag.CONTENT); it
                .isValid(); it.next()) {
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
                }
            } catch (final BadLocationException ex) {
            }
        }
        if (!once.isEmpty())
            textPane.setCaretPosition(once.get());
    }

    HtmlPanel() {
        super(new BorderLayout());

        title = new JLabel();
        textPane = new JTextPane();

        title.setFont(new Font("Dialog", Font.BOLD, LARGE_FONT));
        title.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        textPane.setEditable(false);
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

            @Override
            public final ViewFactory getViewFactory() {
                final ViewFactory factory = super.getViewFactory();
                return new ViewFactory() {
                    public View create(Element elem) {
                        View view = factory.create(elem);
                        if (view instanceof ParagraphView)
                            view = new WrapParagraphView(elem);
                        return view;
                    }
                };
            }

        };
        textPane.setEditorKit(kit);

        final Document doc = kit.createDefaultDocument();
        textPane.setDocument(doc);

        final JTextField highlight = new JTextField();
        highlight.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                find(Pattern.compile(highlight.getText()));
            }
        });

        final JLabel label = new JLabel("highlight: ");
        label.setLabelFor(highlight);

        final JPanel center = new JPanel(new BorderLayout());
        center.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        center.setBackground(Color.white);
        center.add(title, BorderLayout.NORTH);
        center.add(textPane, BorderLayout.CENTER);

        final JPanel south = new JPanel(new BorderLayout());
        south.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        south.add(label, BorderLayout.WEST);
        south.add(highlight, BorderLayout.CENTER);

        add(SwingUtil.newVerticalScrollPane(center), BorderLayout.CENTER);
        add(south, BorderLayout.SOUTH);
    }

    final void setPage(String title, File path) throws IOException,
            MalformedURLException {
        this.title.setText(title);

        final Highlighter highlighter = textPane.getHighlighter();
        highlighter.removeAllHighlights();
        
        final Document doc = textPane.getDocument();
        doc.putProperty(Document.TitleProperty, title);
        // Clearing stream forces refresh.
        doc.putProperty(Document.StreamDescriptionProperty, null);
        textPane.setPage(path.toURI().toURL());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public final void run() {
                try {
                    final JFrame f = new JFrame("HtmlPanel");
                    final HtmlPanel panel = new HtmlPanel();
                    f.getContentPane().add(panel, BorderLayout.CENTER);

                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    final Dimension d = f.getToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                    f.setVisible(true);
                    panel.setPage("Test", new File("c:/tmp/index.html"));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
