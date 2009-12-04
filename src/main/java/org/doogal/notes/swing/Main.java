package org.doogal.notes.swing;

import static org.doogal.notes.core.Utility.printResource;
import static org.doogal.notes.domain.Constants.SMALL_FONT;
import static org.doogal.notes.swing.SwingUtil.parentFrame;
import static org.doogal.notes.swing.SwingUtil.postWindowClosingEvent;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.mail.internet.ParseException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.table.TableModel;

import org.apache.commons.logging.Log;
import org.doogal.notes.command.Command;
import org.doogal.notes.core.AsyncDoogal;
import org.doogal.notes.core.Controller;
import org.doogal.notes.core.Doogal;
import org.doogal.notes.core.Environment;
import org.doogal.notes.core.ExitException;
import org.doogal.notes.core.PromptDoogal;
import org.doogal.notes.core.Repo;
import org.doogal.notes.core.SyncDoogal;
import org.doogal.notes.table.DocumentTable;
import org.doogal.notes.table.SummaryTable;
import org.doogal.notes.table.Table;
import org.doogal.notes.table.TableType;
import org.doogal.notes.util.EvalException;
import org.doogal.notes.util.HtmlPage;
import org.doogal.notes.util.StandardLog;
import org.doogal.notes.view.AbstractView;
import org.doogal.notes.view.View;

public final class Main extends JPanel implements Doogal {

    private static final long serialVersionUID = 1L;

    private final Map<String, Action> actions;
    private final Set<Action> context;

    private final CommandPanel command;
    private final JTabbedPane tabbedPane;
    private final JTextArea console;

    private final Doogal doogal;

    private boolean destroyed = false;

    private static TableModel newTableModel(Table table) throws IOException {
        if (null != table && table instanceof DocumentTable) {
            final DocumentTable from = (DocumentTable) table;
            final SummaryTable to = new SummaryTable();
            for (int i = 0; i < from.getRowCount(); ++i)
                to.add(from.getSummary(i));
            table = to;
        }
        return new TableAdapter(table);
    }

    Main() throws IllegalAccessException, InvocationTargetException,
            IOException {
        super(new BorderLayout());

        actions = new HashMap<String, Action>();
        context = new HashSet<Action>();

        tabbedPane = new JTabbedPane();
        console = new JTextArea();

        console.setMargin(new Insets(5, 5, 5, 5));
        console.setFont(new Font("Monospaced", Font.PLAIN, SMALL_FONT));

        console.setEditable(false);
        console.setFocusable(false);
        console.setLineWrap(true);

        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(new TextAreaStream(console,
                false), true);
        final Log log = new StandardLog(out, out);

        command = new CommandPanel(this, log);

        final TablePanel tablePanel = new TablePanel(actions);

        tabbedPane.setFocusable(false);
        tabbedPane.setTabPlacement(SwingConstants.BOTTOM);
        tabbedPane.add("Table", tablePanel);
        tabbedPane.addChangeListener(new ChangeListener() {
            public final void stateChanged(ChangeEvent e) {
                for (final Action action : context)
                    action.setEnabled(false);
                final ViewPanel vp = (ViewPanel) tabbedPane
                        .getSelectedComponent();
                vp.setVisible();
            }
        });

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                tabbedPane, new JScrollPane(console));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getToolkit().getScreenSize().height / 4);
        // Weighted to top panel.
        splitPane.setResizeWeight(0.7);

        add(command, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        final View view = new AbstractView(out, log) {

            public final void setPage(final int n) {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        final ViewPanel vp = (ViewPanel) tabbedPane
                                .getSelectedComponent();
                        try {
                            vp.setPage(n);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            public final void nextPage() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        final ViewPanel vp = (ViewPanel) tabbedPane
                                .getSelectedComponent();
                        try {
                            vp.nextPage();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            public final void prevPage() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        final ViewPanel vp = (ViewPanel) tabbedPane
                                .getSelectedComponent();
                        try {
                            vp.prevPage();
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            public final void setHtml(final HtmlPage html) {
                EventQueue.invokeLater(new Runnable() {
                    public final int indexOfTab(int id) {
                        final int n = tabbedPane.getTabCount();
                        for (int i = 1; i < n; i++) {
                            final HtmlPanel panel = (HtmlPanel) tabbedPane
                                    .getComponentAt(i);
                            if (id == panel.getPage().getId())
                                return i;
                        }
                        return -1;
                    }

                    public final void run() {
                        try {
                            int i = indexOfTab(html.getId());
                            if (-1 == i) {
                                i = tabbedPane.getTabCount();
                                final HtmlPanel panel = new HtmlPanel(actions,
                                        html);
                                tabbedPane.add(html.getTitle(), panel);
                                tabbedPane.setTabComponentAt(i, new TabPanel(
                                        tabbedPane));
                            } else {
                                final HtmlPanel panel = (HtmlPanel) tabbedPane
                                        .getComponentAt(i);
                                panel.setPage(html);
                                tabbedPane.setTitleAt(i, html.getTitle());
                            }
                            tabbedPane.setSelectedIndex(i);
                        } catch (final Exception e) {
                            e.printStackTrace();
                        }
                    }
                });
            }

            @Override
            public final void setTable(final Table table) throws IOException {
                super.setTable(table);
                final TableModel model = newTableModel(table);
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        if (destroyed)
                            return;
                        for (final Action action : context)
                            action.setEnabled(false);
                        tablePanel.setModel(model);
                        clearSelection();
                        tabbedPane.setSelectedIndex(0);
                    }
                });
            }

            public final void refresh() {
            }
        };
        final Controller controller = new Controller() {
            public final void exit(boolean interact) {
                if (!interact)
                    throw new ExitException();
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        log.info("exiting...");
                        postWindowClosingEvent(parentFrame(Main.this));
                    }
                });
            }

            public final void ready() {
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        command.setPrompt(true);
                    }
                });
            }
        };

        final Repo repo = new Repo(env.getRepo());
        repo.init();
        doogal = new AsyncDoogal(log, new PromptDoogal(controller,
                new SyncDoogal(env, view, controller, repo)));

        printResource("motd.txt", out);

        final Map<String, Command> builtins = doogal.getBuiltins();
        for (final Entry<String, Command> entry : builtins.entrySet()) {
            final Command command = entry.getValue();
            actions.put(entry.getKey(), new AbstractAction() {
                private static final long serialVersionUID = 1L;

                {
                    final String key = entry.getKey();
                    final String name = ""
                            + Character.toUpperCase(key.charAt(0))
                            + key.substring(1);
                    putValue(Action.NAME, name);
                    putValue(Action.SHORT_DESCRIPTION, command.getDescription());

                    final String largePath = command.getLargeIcon();
                    if (null != largePath) {
                        final URL largeUrl = getClass().getResource(largePath);
                        if (largeUrl != null)
                            putValue(Action.LARGE_ICON_KEY, new ImageIcon(
                                    largeUrl));
                    }

                    final String smallPath = command.getSmallIcon();
                    if (null != smallPath) {
                        final URL smallUrl = getClass().getResource(smallPath);
                        if (smallUrl != null)
                            putValue(Action.SMALL_ICON, new ImageIcon(smallUrl));
                    }
                }

                public final void actionPerformed(ActionEvent ev) {
                    try {
                        eval(entry.getKey());
                    } catch (final EvalException e) {
                        e.printStackTrace();
                    }
                }
            });

            for (final TableType type : TableType.values()) {
                if (null != type.getAction()
                        && !"help".equals(type.getAction())) {
                    final Action action = actions.get(type.getAction());
                    if (null != action)
                        context.add(action);
                }
                for (final String name : type.getActions()) {
                    final Action action = actions.get(name);
                    if (null != action && !"help".equals(name))
                        context.add(action);
                }
            }
            context.remove("help");
            for (final Action action : context)
                action.setEnabled(false);
        }
    }

    public final void destroy() {
        // Avoid multiple destroy from multiple clicks.
        if (!destroyed) {
            destroyed = true;
            // TODO: close each view panel.
            doogal.destroy();
        }
    }

    public final void eval(String cmd, Object... args) {
        command.setPrompt(false);
        console.setText("");
        final ViewPanel vp = (ViewPanel) tabbedPane.getSelectedComponent();
        doogal.setSelection(vp.getType(), vp.getSelection());
        doogal.eval(cmd, args);
    }

    public final void eval() {
        command.setPrompt(false);
        console.setText("");
        doogal.eval();
    }

    public final void batch(Reader reader) throws IOException, ParseException {
        command.setPrompt(false);
        console.setText("");
        doogal.batch(reader);
    }

    public final void batch(File file) throws IOException, ParseException {
        command.setPrompt(false);
        console.setText("");
        doogal.batch(file);
    }

    public final void config() throws IOException, ParseException {
        command.setPrompt(false);
        console.setText("");
        doogal.config();
    }

    public final void setSelection(TableType type, Object... args) {
        doogal.setSelection(type, args);
    }

    public final void clearSelection() {
        doogal.clearSelection();
    }

    public final Map<String, Command> getBuiltins() {
        return doogal.getBuiltins();
    }

    final Map<String, Action> getActions() {
        return actions;
    }

    private static void run() throws IllegalAccessException,
            InvocationTargetException, IOException, ParseException {
        final JFrame f = new JFrame("Doogal");
        final Main m = new Main();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public final void windowClosing(WindowEvent ev) {
                m.destroy();
            }
        });
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Map<String, Action> actions = m.getActions();

        final JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.add(new JMenuItem(actions.get("new")));
        file.add(new JMenuItem(actions.get("open")));
        file.add(new JMenuItem(actions.get("publish")));
        file.add(new JMenuItem(actions.get("import")));
        file.add(new JMenuItem(actions.get("exit")));

        final JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(new JMenuItem(actions.get("delete")));
        edit.add(new JMenuItem(actions.get("peek")));
        edit.add(new JMenuItem(actions.get("tidy")));

        final JMenu search = new JMenu("Search");
        search.setMnemonic(KeyEvent.VK_S);
        search.add(new JMenuItem(actions.get("browse")));
        search.add(new JMenuItem(actions.get("more")));
        search.add(new JMenuItem(actions.get("recent")));
        search.add(new JMenuItem(actions.get("names")));

        final JMenu tools = new JMenu("Tools");
        tools.setMnemonic(KeyEvent.VK_T);
        tools.add(new JMenuItem(actions.get("alias")));
        tools.add(new JMenuItem(actions.get("archive")));
        tools.add(new JMenuItem(actions.get("index")));
        tools.add(new JMenuItem(actions.get("set")));

        final JMenu help = new JMenu("Help");
        help.setMnemonic(KeyEvent.VK_H);
        help.add(new JMenuItem(actions.get("help")));

        final JMenuBar mb = new JMenuBar();
        mb.add(file);
        mb.add(edit);
        mb.add(search);
        mb.add(tools);
        mb.add(help);

        f.setJMenuBar(mb);
        final JToolBar toolBar = new JToolBar();
        toolBar.add(actions.get("new"));
        toolBar.add(actions.get("open"));
        toolBar.add(actions.get("publish"));
        toolBar.add(actions.get("import"));
        toolBar.add(actions.get("delete"));
        toolBar.add(actions.get("peek"));
        toolBar.add(actions.get("browse"));
        toolBar.add(actions.get("more"));
        toolBar.add(actions.get("recent"));
        toolBar.add(actions.get("index"));
        toolBar.add(actions.get("set"));
        toolBar.add(actions.get("help"));

        f.setLayout(new BorderLayout());
        f.add(toolBar, BorderLayout.PAGE_START);
        f.add(m, BorderLayout.CENTER);

        final URL url = Main.class.getResource("/doogal32.png");
        f.setIconImage(new ImageIcon(url).getImage());

        final Dimension d = f.getToolkit().getScreenSize();
        f.setSize(d.width / 2, d.height / 2);
        f.setVisible(true);
        m.config();
    }

    public static void main(String[] args) {

        System.setProperty("line.separator", "\n");
        EventQueue.invokeLater(new Runnable() {

            public final void run() {
                try {
                    Main.run();
                } catch (final Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }
}
