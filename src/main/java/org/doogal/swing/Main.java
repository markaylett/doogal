package org.doogal.swing;

import static org.doogal.core.Constants.PROMPT;
import static org.doogal.core.Utility.printResource;
import static org.doogal.swing.SwingUtil.newScrollPane;
import static org.doogal.swing.SwingUtil.parentFrame;
import static org.doogal.swing.SwingUtil.postWindowClosingEvent;
import static org.doogal.swing.SwingUtil.setEmacsKeyMap;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.net.URL;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.mail.internet.ParseException;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import org.apache.commons.logging.Log;
import org.doogal.core.AsyncDoogal;
import org.doogal.core.Command;
import org.doogal.core.Controller;
import org.doogal.core.Doogal;
import org.doogal.core.Environment;
import org.doogal.core.EvalException;
import org.doogal.core.ExitException;
import org.doogal.core.Repo;
import org.doogal.core.Shellwords;
import org.doogal.core.Size;
import org.doogal.core.StandardLog;
import org.doogal.core.SyncDoogal;
import org.doogal.core.table.DocumentTable;
import org.doogal.core.table.SummaryTable;
import org.doogal.core.table.Table;
import org.doogal.core.table.TableType;
import org.doogal.core.view.AbstractView;
import org.doogal.core.view.View;

public final class Main extends JPanel implements Doogal {

    private static final long serialVersionUID = 1L;
    private static final int SMALL_FONT = 12;
    private static final int MEDIUM_FONT = 14;
    private static final int LARGE_FONT = 16;

    private final History history;
    private final JTextArea console;
    private final JTextField prompt;
    private final Doogal doogal;

    private final Map<String, Action> actions;
    private final Set<Action> context;

    private boolean closed = false;

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

    private final void setPrompt(boolean b) {
        prompt.setEnabled(b);
        prompt.setEditable(b);
        if (b)
            prompt.requestFocus();
    }

    Main() throws Exception {
        super(new BorderLayout());

        history = new History();

        final JTable jtable = new JTable(new TableAdapter());
        jtable.setFocusable(false);
        jtable.setDefaultRenderer(Size.class, new DefaultTableCellRenderer() {

            private static final long serialVersionUID = 1L;

            {
                setHorizontalAlignment(SwingConstants.RIGHT);
            }

            @Override
            protected final void setValue(Object value) {
                super.setValue(value.toString());
            }
        });

        jtable.getSelectionModel().addListSelectionListener(
                new ListSelectionListener() {
                    public final void valueChanged(ListSelectionEvent e) {
                        if (closed)
                            return;
                        final ListSelectionModel model = (ListSelectionModel) e
                                .getSource();
                        if (!model.isSelectionEmpty()) {
                            final TableAdapter tableModel = (TableAdapter) jtable
                                    .getModel();
                            final String[] names = tableModel.getType()
                                    .getActions();
                            for (int i = 0; i < names.length; ++i)
                                actions.get(names[i]).setEnabled(true);
                            final int[] rows = jtable.getSelectedRows();
                            final Object[] args = new Object[rows.length];
                            for (int i = 0; i < rows.length; ++i)
                                args[i] = jtable.getValueAt(rows[i], 0);
                            setArgs(args);
                        }
                    }
                });

        jtable.addMouseListener(new MouseAdapter() {
            private final void setSelection(int index) {
                final ListSelectionModel selectionModel = jtable
                        .getSelectionModel();
                if (!selectionModel.isSelectedIndex(index))
                    selectionModel.setSelectionInterval(index, index);
            }

            private final JPopupMenu newMenu() {
                final TableAdapter model = (TableAdapter) jtable.getModel();
                final String[] names = model.getType().getActions();
                if (0 < names.length) {
                    final JPopupMenu menu = new JPopupMenu();
                    for (int i = 0; i < names.length; ++i) {
                        final Action action = actions.get(names[i]);
                        menu.add(new JMenuItem(action));
                    }
                    return menu;
                }
                return null;
            }

            private final void showPopup(MouseEvent e) {
                if (e.isPopupTrigger()) {
                    final Point p = e.getPoint();
                    final int index = jtable.rowAtPoint(p);
                    if (-1 != index) {
                        setSelection(index);
                        final JPopupMenu menu = newMenu();
                        if (null != menu)
                            menu.show(e.getComponent(), e.getX(), e.getY());
                    }
                }
            }

            @Override
            public final void mouseClicked(MouseEvent ev) {
                if (SwingUtilities.isLeftMouseButton(ev)
                        && 2 == ev.getClickCount()) {
                    final Point p = ev.getPoint();
                    final int index = jtable.rowAtPoint(p);
                    if (-1 != index) {
                        final TableAdapter model = (TableAdapter) jtable
                                .getModel();
                        final String name = model.getType().getAction();
                        if (null != name)
                            try {
                                eval(name);
                            } catch (final EvalException e) {
                                e.printStackTrace();
                            }
                    }
                }
            }

            @Override
            public final void mousePressed(MouseEvent e) {
                showPopup(e);
            }

            @Override
            public final void mouseReleased(MouseEvent e) {
                showPopup(e);
            }
        });

        console = new JTextArea();
        console.setMargin(new Insets(5, 5, 5, 5));
        console.setFont(new Font("Monospaced", Font.PLAIN, SMALL_FONT));

        console.setEditable(false);
        console.setFocusable(false);
        console.setLineWrap(true);

        final JPanel promptPanel = new JPanel();
        promptPanel.setBorder(BorderFactory.createEmptyBorder(2, 2, 2, 2));
        promptPanel.setLayout(new BorderLayout());

        final JLabel label = new JLabel(PROMPT);
        label.setFont(new Font("Dialog", Font.BOLD, LARGE_FONT));
        prompt = new JTextField();
        prompt.setMargin(new Insets(2, 2, 2, 2));
        prompt.setFont(new Font("Monospaced", Font.PLAIN, MEDIUM_FONT));

        label.setLabelFor(prompt);

        promptPanel.add(label, BorderLayout.WEST);
        promptPanel.add(prompt, BorderLayout.CENTER);

        setPrompt(false);
        setEmacsKeyMap(prompt, history);

        final JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT,
                newScrollPane(jtable), newScrollPane(console));
        splitPane.setOneTouchExpandable(true);
        splitPane.setDividerLocation(getToolkit().getScreenSize().height / 4);

        add(promptPanel, BorderLayout.NORTH);
        add(splitPane, BorderLayout.CENTER);

        final Environment env = new Environment();
        final PrintWriter out = new PrintWriter(new TextAreaStream(console),
                true);
        final Log log = new StandardLog(out, out);
        final View view = new AbstractView(out, log) {

            @Override
            public final void setTable(final Table table) throws IOException {
                super.setTable(table);
                final TableModel model = newTableModel(table);
                EventQueue.invokeLater(new Runnable() {
                    public final void run() {
                        if (closed)
                            return;
                        for (final Action action : context)
                            action.setEnabled(false);
                        jtable.setRowSorter(new TableRowSorter<TableModel>(
                                model));
                        jtable.setModel(model);
                        setArgs();
                    }
                });
            }

            public final void setPage(String n) throws EvalException,
                    IOException {
            }

            public final void showPage() throws EvalException, IOException {
                if (0 == table.getRowCount())
                    out.println("no results");
            }

            public final void nextPage() throws EvalException, IOException {
            }

            public final void prevPage() throws EvalException, IOException {
            }
        };
        final Controller controller = new Controller() {
            public final void exit(boolean interact) throws ExitException {
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
                        setPrompt(true);
                    }
                });
            }
        };

        final Repo repo = new Repo(env.getRepo());
        repo.init();
        doogal = new AsyncDoogal(log, new SyncDoogal(env, view, controller,
                repo));

        prompt.addActionListener(new ActionListener() {
            public final void actionPerformed(ActionEvent ev) {
                final String s = prompt.getText();
                history.add(s);
                final Reader reader = new StringReader(s);
                prompt.setText("");
                setPrompt(false);
                try {
                    Shellwords.parse(reader, Main.this);
                } catch (final EvalException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final IOException e) {
                    log.error(e.getLocalizedMessage());
                } catch (final ParseException e) {
                    log.error(e.getLocalizedMessage());
                }
            }
        });

        printResource("motd.txt", out);

        actions = new HashMap<String, Action>();
        context = new HashSet<Action>();

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

    public final void close() throws IOException {
        // Avoid multiple close from multiple clicks.
        if (!closed) {
            closed = true;
            doogal.close();
        }
    }

    public final void eval(String cmd, Object... args) throws EvalException {
        console.setText("");
        doogal.eval(cmd, args);
    }

    public final void eval() throws EvalException {
        console.setText("");
        doogal.eval();
    }

    public final void batch(Reader reader) throws EvalException, IOException,
            ParseException {
        console.setText("");
        doogal.batch(reader);
    }

    public final void batch(File file) throws EvalException, IOException,
            ParseException {
        console.setText("");
        doogal.batch(file);
    }

    public final void config() throws EvalException, IOException,
            ParseException {
        console.setText("");
        doogal.config();
    }

    public final void setArgs(Object... args) {
        doogal.setArgs(args);
    }

    public final Map<String, Command> getBuiltins() {
        return doogal.getBuiltins();
    }

    final Map<String, Action> getActions() {
        return actions;
    }

    private static void run() throws Exception {
        final JFrame f = new JFrame("Doogal");
        final Main m = new Main();
        f.addWindowListener(new WindowAdapter() {
            @Override
            public final void windowClosing(WindowEvent ev) {
                try {
                    m.close();
                } catch (final IOException e) {
                    e.printStackTrace();
                }
            }
        });
        f.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        final Map<String, Action> actions = m.getActions();

        final JMenu file = new JMenu("File");
        file.setMnemonic(KeyEvent.VK_F);
        file.add(new JMenuItem(actions.get("new")));
        file.add(new JMenuItem(actions.get("open")));
        file.add(new JMenuItem(actions.get("import")));
        file.add(new JMenuItem(actions.get("publish")));
        file.add(new JMenuItem(actions.get("exit")));

        final JMenu edit = new JMenu("Edit");
        edit.setMnemonic(KeyEvent.VK_E);
        edit.add(new JMenuItem(actions.get("delete")));
        edit.add(new JMenuItem(actions.get("peek")));
        edit.add(new JMenuItem(actions.get("tidy")));

        final JMenu search = new JMenu("Search");
        search.setMnemonic(KeyEvent.VK_S);
        search.add(new JMenuItem(actions.get("browse")));
        search.add(new JMenuItem(actions.get("search")));
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
        toolBar.add(actions.get("import"));
        toolBar.add(actions.get("publish"));
        toolBar.add(actions.get("delete"));
        toolBar.add(actions.get("peek"));
        toolBar.add(actions.get("browse"));
        toolBar.add(actions.get("search"));
        toolBar.add(actions.get("more"));
        toolBar.add(actions.get("recent"));
        toolBar.add(actions.get("set"));
        toolBar.add(actions.get("help"));

        f.setLayout(new BorderLayout());
        f.add(toolBar, BorderLayout.PAGE_START);
        f.add(m, BorderLayout.CENTER);
        f.pack();

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
