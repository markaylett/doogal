package org.doogal.swing;

import static org.doogal.core.Constants.LARGE_FONT;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

final class HtmlPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JLabel title;
    private final JEditorPane editor;

    HtmlPanel() {
        super(new BorderLayout());

        title = new JLabel();
        editor = new JEditorPane();

        title.setFont(new Font("Dialog", Font.BOLD, LARGE_FONT));
        editor.setEditable(false);

        final HTMLEditorKit kit = new HTMLEditorKit();
        editor.setEditorKit(kit);

        final Document doc = kit.createDefaultDocument();
        editor.setDocument(doc);

        add(title, BorderLayout.NORTH);
        add(editor, BorderLayout.CENTER);
    }

    final void setPage(String title, File path) throws IOException,
            MalformedURLException {
        this.title.setText(title);
        editor.setPage(path.toURI().toURL());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public final void run() {
                try {
                    final JFrame f = new JFrame("HtmlPanel");
                    final HtmlPanel panel = new HtmlPanel();
                    final JScrollPane scrollPane = new JScrollPane(panel);
                    f.getContentPane().add(scrollPane, BorderLayout.CENTER);

                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    final Dimension d = f.getToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                    f.setVisible(true);
                    panel.setPage("Test", new File(
                            "u:/doc/doogal/html/index.html"));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
