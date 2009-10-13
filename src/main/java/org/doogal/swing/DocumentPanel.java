package org.doogal.swing;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;

import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.text.Document;
import javax.swing.text.html.HTMLEditorKit;

final class DocumentPanel extends JPanel {
    private static final long serialVersionUID = 1L;
    private final JEditorPane editor = new JEditorPane();

    DocumentPanel() {
        super(new BorderLayout());

        editor.setEditable(false);
        final HTMLEditorKit kit = new HTMLEditorKit();
        editor.setEditorKit(kit);

        final Document doc = kit.createDefaultDocument();
        editor.setDocument(doc);

        add(editor, BorderLayout.CENTER);
    }

    final void setPage(File file) throws IOException, MalformedURLException {
        editor.setPage(file.toURI().toURL());
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public final void run() {
                try {
                    final JFrame f = new JFrame("DocumentPanel");
                    final DocumentPanel panel = new DocumentPanel();
                    final JScrollPane scrollPane = new JScrollPane(panel);
                    f.getContentPane().add(scrollPane, BorderLayout.CENTER);

                    f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

                    final Dimension d = f.getToolkit().getScreenSize();
                    f.setSize(d.width / 2, d.height / 2);
                    f.setVisible(true);
                    panel.setPage(new File("u:/doc/doogal/html/index.html"));
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
