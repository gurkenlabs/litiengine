package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.GradleScriptProjectSupport;
import de.gurkenlabs.utiliti.controller.IntellijIntegration;
import de.gurkenlabs.utiliti.controller.ProjectCodeIntegration;
import de.gurkenlabs.utiliti.controller.ProjectLaunchRequest;
import de.gurkenlabs.utiliti.controller.ProjectLaunchCancelledException;
import de.gurkenlabs.utiliti.controller.ProjectLaunchPhase;
import de.gurkenlabs.utiliti.controller.ProjectSession;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import de.gurkenlabs.utiliti.controller.ScriptSourcePaths;
import de.gurkenlabs.utiliti.controller.ScriptTemplateFactory;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.debug.JdiScriptDebuggerBackend;
import de.gurkenlabs.utiliti.controller.debug.ScriptBreakpoint;
import de.gurkenlabs.utiliti.controller.debug.ScriptBreakpointStore;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebuggerBackend;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.dialogs.GameScriptsDialog;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.lang.model.SourceVersion;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.ListModel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreePath;

/** First-class central workspace for project scripts. */
public final class ScriptWorkspacePanel extends JPanel {
  private static final Logger log = Logger.getLogger(ScriptWorkspacePanel.class.getName());
  private static final int BOTTOM_PANEL_HEIGHT = 190;
  static final String DEFAULT_SCRIPT_NAME = "NewScript";
  private final DefaultMutableTreeNode scriptsRoot = new DefaultMutableTreeNode("Scripts");
  private final DefaultTreeModel scriptsModel = new DefaultTreeModel(this.scriptsRoot);
  private final StyledTree scripts = new StyledTree(this.scriptsModel);
  private final JTextField search = createSearchTextField("Search scripts...");
  private final DefaultMutableTreeNode globalsRoot = new DefaultMutableTreeNode("Globals & APIs");
  private final DefaultTreeModel globalsTreeModel = new DefaultTreeModel(this.globalsRoot);
  private final StyledTree globalsTree = new StyledTree(this.globalsTreeModel);
  private final JTextField globalsSearch = createSearchTextField("Search APIs & events...");

  private final JTabbedPane tabs = new JTabbedPane() {
    @Override
    public Dimension getPreferredSize() {
      Dimension d = super.getPreferredSize();
      if (getTabCount() > 0) {
        java.awt.Rectangle bounds = getBoundsAt(0);
        if (bounds != null && bounds.height > 0) {
          return new Dimension(d.width, bounds.height);
        }
      }
      return d;
    }
  };
  private final JTabbedPane sidebarTabs;
  private final JPanel tabStrip;
  private final JPanel statusBar;
  private final JPanel mainEditorArea = new JPanel(new BorderLayout());
  private final DefaultTableModel problemsModel = new DefaultTableModel(
    new Object[] {"Severity", "File", "Line", "Message", "Diagnostic"}, 0) {
      @Override public boolean isCellEditable(int row, int column) { return false; }
    };
  private final JTable problems = new JTable(this.problemsModel);
  private final Map<String, Boolean> scriptErrorStates = new ConcurrentHashMap<>();
  private final Map<String, List<ScriptDiagnostic>> projectDiagnostics = new ConcurrentHashMap<>();
  private final JLabel status = new JLabel(" ");
  private final JLabel caretStatus = new JLabel(" ");
  private final JLabel scriptUsageStatusLabel = new JLabel(" ");
  private final JLabel scriptHealthIconLabel = new JLabel();
  private static final javax.swing.Icon CHECK_ICON = new javax.swing.Icon() {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(110, 195, 120));
        g2.drawOval(x + 1, y + 1, 13, 13);
        g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 4, y + 8, x + 7, y + 11);
        g2.drawLine(x + 7, y + 11, x + 11, y + 5);
      } finally {
        g2.dispose();
      }
    }

    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
  };
  private static final javax.swing.Icon ERROR_ICON = new javax.swing.Icon() {
    @Override
    public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setColor(new Color(235, 90, 90));
        g2.drawOval(x + 1, y + 1, 13, 13);
        g2.setStroke(new java.awt.BasicStroke(1.5f, java.awt.BasicStroke.CAP_ROUND, java.awt.BasicStroke.JOIN_ROUND));
        g2.drawLine(x + 5, y + 5, x + 10, y + 10);
        g2.drawLine(x + 10, y + 5, x + 5, y + 10);
      } finally {
        g2.dispose();
      }
    }

    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
  };
  private final JPanel conflictBar = new JPanel(new BorderLayout(8, 0));
  private final JLabel conflictMessage = new JLabel();
  private final Map<String, ScriptTab> openTabs = new LinkedHashMap<>();
  private final Map<ScriptDefinition, Path> projectSourcePaths = new IdentityHashMap<>();
  private final Map<String, ScriptDefinition> projectSourceDefinitions = new LinkedHashMap<>();
  private final Set<String> customCreatedPackages = new java.util.LinkedHashSet<>();
  private Map<String, Integer> scriptUsageCounts = Map.of();
  private final Map<String, ScriptDefinition> navigatedProjectDefinitions = new LinkedHashMap<>();
  private final Map<String, Path> navigatedProjectSources = new LinkedHashMap<>();
  private final Timer externalChangeTimer = new Timer(900, event -> this.checkExternalChanges());
  private MonacoScriptEditor monaco;
  private ScriptTab monacoTab;
  private ScriptTab conflictTab;
  private final ScriptDebuggerPanel debuggerPanel = new ScriptDebuggerPanel();
  private final List<ScriptBreakpoint> breakpoints = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final Timer breakpointSyncTimer = new Timer(300, e -> this.syncBreakpoints());
  private final Timer problemsRefreshDebounce = new Timer(200, e -> this.refreshProblemsTableNow());
  private final java.util.concurrent.ExecutorService breakpointSyncExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
  private final java.util.concurrent.atomic.AtomicBoolean projectSourceBuildInProgress =
    new java.util.concurrent.atomic.AtomicBoolean();
  private JdiScriptDebuggerBackend debugger;
  private String executionScriptId;
  private int executionLine;
  private List<ScriptDebugSnapshot.Variable> executionVariables = List.of();
  private volatile boolean projectLaunchPending;
  private volatile boolean debuggerLaunchFailed;
  private boolean restartRequested;
  private Consumer<ScriptDefinition> selectionListener = ignored -> {};
  private final JPanel editorHost = new JPanel(new BorderLayout());
  private final ScriptTypeBadge scriptContext = new ScriptTypeBadge();
  private final ScriptOverviewPanel overviewPanel;
  private final Consumer<ScriptBindingTarget> scriptBindingChangeListener = ignored ->
    SwingUtilities.invokeLater(this::scriptBindingsChanged);
  private final Consumer<UndoManager> undoStackChangeListener = ignored ->
    SwingUtilities.invokeLater(this::refreshActiveUsages);

  public ScriptWorkspacePanel() {


    super(new BorderLayout());
    this.problemsRefreshDebounce.setRepeats(false);
    this.setBackground(Style.background());
    this.add(this.createConflictBar(), BorderLayout.NORTH);

    this.overviewPanel = new ScriptOverviewPanel(
      this::navigateToUsage,
      this::revealLineInActiveTab
    );

    this.sidebarTabs = new JTabbedPane(JTabbedPane.TOP);
    sidebarTabs.putClientProperty("JTabbedPane.noContentBorder", Boolean.TRUE);
    sidebarTabs.putClientProperty("JTabbedPane.hasFullBorder", Boolean.FALSE);
    sidebarTabs.putClientProperty("JTabbedPane.contentInsets", new java.awt.Insets(0, 0, 0, 0));
    sidebarTabs.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(0, 4, 0, 4));
    sidebarTabs.putClientProperty("JTabbedPane.tabType", "underlined");
    sidebarTabs.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
    sidebarTabs.putClientProperty("JTabbedPane.tabHeight", 28);
    sidebarTabs.putClientProperty("JTabbedPane.tabInsets", new java.awt.Insets(2, 8, 2, 8));
    sidebarTabs.putClientProperty("JTabbedPane.underlineColor", Style.accent());
    sidebarTabs.putClientProperty("JTabbedPane.underlineHeight", 2);
    sidebarTabs.putClientProperty("JTabbedPane.selectedBackground", Style.surface());
    sidebarTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    sidebarTabs.setBackground(Style.background());

    sidebarTabs.addTab("Current Script", Icons.SYMBOL_GROUP_16, this.overviewPanel);
    sidebarTabs.addTab("Game API", Icons.API_16, this.createGlobalsPanel());

    JSplitPane explorer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.createScriptExplorer(), sidebarTabs);
    UI.configureSplitPane(explorer);
    explorer.setBackground(Style.background());
    explorer.setResizeWeight(0.48);
    explorer.setMinimumSize(new Dimension(235, 0));
    explorer.setPreferredSize(new Dimension(265, 0));

    this.problems.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    this.problems.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.problems.setShowGrid(false);
    this.problems.setIntercellSpacing(new Dimension(0, 0));
    this.problems.setBackground(Style.surface());
    this.problems.setForeground(Style.text());
    this.problems.setSelectionBackground(Style.sceneRowSelected());
    this.problems.setSelectionForeground(Color.WHITE);
    this.problems.setFont(Style.getDefaultFont());
    this.problems.setOpaque(false);

    if (this.problems.getTableHeader() != null) {
      this.problems.getTableHeader().setBackground(Style.surface());
      this.problems.getTableHeader().setForeground(Style.mutedText());
      this.problems.getTableHeader().setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
      this.problems.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
      javax.swing.table.TableCellRenderer defaultHeaderRenderer = this.problems.getTableHeader().getDefaultRenderer();
      this.problems.getTableHeader().setDefaultRenderer((table, value, isSelected, hasFocus, row, column) -> {
        Component c = defaultHeaderRenderer.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
        if (c instanceof JLabel label) {
          label.setHorizontalAlignment(SwingConstants.LEADING);
        }
        return c;
      });
    }

    this.problems.getColumnModel().getColumn(0).setPreferredWidth(85);
    this.problems.getColumnModel().getColumn(0).setCellRenderer(new ProblemSeverityRenderer());

    this.problems.getColumnModel().getColumn(1).setPreferredWidth(160);
    this.problems.getColumnModel().getColumn(1).setCellRenderer(new ProblemFileRenderer());

    this.problems.getColumnModel().getColumn(2).setPreferredWidth(65);
    this.problems.getColumnModel().getColumn(2).setCellRenderer(new ProblemLineRenderer());

    this.problems.getColumnModel().getColumn(3).setCellRenderer(new ProblemMessageRenderer());

    if (this.problems.getColumnModel().getColumnCount() > 4) {
      this.problems.getColumnModel().removeColumn(this.problems.getColumnModel().getColumn(4));
    }

    this.problems.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && this.problems.getSelectedRow() >= 0) {
        this.jumpToProblemRow(this.problems.getSelectedRow());
      }
    });

    this.problems.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        int row = problems.getSelectedRow();
        if (row >= 0) {
          jumpToProblemRow(row);
        }
      }
    });

    this.tabs.putClientProperty("JTabbedPane.noContentBorder", Boolean.TRUE);
    this.tabs.putClientProperty("JTabbedPane.hasFullBorder", Boolean.FALSE);
    this.tabs.putClientProperty("JTabbedPane.contentInsets", new java.awt.Insets(0, 0, 0, 0));
    this.tabs.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(0, 0, 0, 0));
    this.tabs.putClientProperty("JTabbedPane.tabType", "underlined");
    this.tabs.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
    this.tabs.putClientProperty("JTabbedPane.tabSeparatorsFullHeight", Boolean.FALSE);
    this.tabs.putClientProperty("JTabbedPane.tabHeight", 30);
    this.tabs.putClientProperty("JTabbedPane.tabInsets", new java.awt.Insets(3, 10, 3, 6));
    this.tabs.putClientProperty("JTabbedPane.underlineColor", Style.accent());
    this.tabs.putClientProperty("JTabbedPane.underlineHeight", 2);
    this.tabs.putClientProperty("JTabbedPane.selectedBackground", Style.surface());
    this.tabs.putClientProperty("JTabbedPane.tabAreaBackground", Style.background());
    this.tabs.setBorder(BorderFactory.createEmptyBorder());
    this.tabs.setBackground(Style.background());

    this.tabStrip = new JPanel(new BorderLayout());
    tabStrip.setBackground(Style.background());
    tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    tabStrip.add(this.tabs, BorderLayout.CENTER);

    JPanel tabTrailing = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 4));
    tabTrailing.setOpaque(false);
    this.scriptContext.setVisible(false);
    tabTrailing.add(this.scriptContext);
    tabStrip.add(tabTrailing, BorderLayout.EAST);
    this.mainEditorArea.add(tabStrip, BorderLayout.NORTH);

    this.editorHost.setBackground(Style.background());
    this.mainEditorArea.add(this.editorHost, BorderLayout.CENTER);

    this.statusBar = new JPanel(new BorderLayout());
    statusBar.setBackground(Style.background());
    statusBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    Font statusBarFont = new Font(
        Style.FONTNAME_CONSOLE,
        Font.PLAIN,
        Math.max(10, Math.round(11 * Editor.preferences().getUiScale())));
    this.status.setFont(statusBarFont);
    this.status.setForeground(Style.mutedText());
    this.caretStatus.setFont(statusBarFont);
    this.caretStatus.setForeground(Style.mutedText());
    this.scriptUsageStatusLabel.setFont(statusBarFont);
    this.scriptUsageStatusLabel.setForeground(Style.mutedText());
    this.scriptHealthIconLabel.setIcon(CHECK_ICON);

    JLabel mcpBadge = StatusBar.createMcpBadge();

    JPanel leftStatus = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
    leftStatus.setOpaque(false);
    leftStatus.add(mcpBadge);
    leftStatus.add(StatusBar.separator());
    leftStatus.add(this.caretStatus);

    JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    rightStatus.setOpaque(false);
    rightStatus.add(this.status);
    rightStatus.add(this.scriptUsageStatusLabel);
    rightStatus.add(this.scriptHealthIconLabel);

    statusBar.add(leftStatus, BorderLayout.WEST);
    statusBar.add(rightStatus, BorderLayout.EAST);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, this.mainEditorArea);
    UI.configureSplitPane(split);
    split.setResizeWeight(0.0);
    split.setDividerLocation(265);
    this.add(split, BorderLayout.CENTER);
    this.add(statusBar, BorderLayout.SOUTH);

    this.search.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { refreshScripts(); }
      @Override public void removeUpdate(DocumentEvent event) { refreshScripts(); }
      @Override public void changedUpdate(DocumentEvent event) { refreshScripts(); }
    });
    this.tabs.addChangeListener(event -> this.activeTabChanged());
    this.refreshTheme();

    Game.addGameListener(new de.gurkenlabs.litiengine.GameListener() {
      @Override
      public void terminated() {
        ScriptWorkspacePanel.this.close();
      }
    });

    Editor.instance().onLoaded(() -> {
      javax.swing.SwingUtilities.invokeLater(() -> {
        this.refreshScripts();
        if (UI.isScriptWorkspaceActive()) {
          this.focusOrOpenFirstScript();
        }
        this.refreshActiveUsages();
      });
    });

    ScriptBindingService.instance().addChangeListener(this.scriptBindingChangeListener);
    UndoManager.onUndoStackChanged(this.undoStackChangeListener);

    String savedBreakpoints = Editor.preferences().getScriptBreakpoints();
    if (savedBreakpoints != null && !savedBreakpoints.isBlank()) {
      this.breakpoints.addAll(ScriptBreakpointStore.decode(savedBreakpoints));
    }
  }



  public JComponent getProblemsComponent() {
    return new JScrollPane(this.problems);
  }

  public JComponent getDebuggerComponent() {
    return this.debuggerPanel;
  }

  DefaultMutableTreeNode getScriptsRoot() {
    return this.scriptsRoot;
  }

  public synchronized void close() {
    ScriptBindingService.instance().removeChangeListener(this.scriptBindingChangeListener);
    UndoManager.removeUndoStackChanged(this.undoStackChangeListener);
    if (this.externalChangeTimer != null) {
      this.externalChangeTimer.stop();
    }
    if (this.problemsRefreshDebounce != null) {
      this.problemsRefreshDebounce.stop();
    }
    for (ScriptTab tab : new ArrayList<>(this.openTabs.values())) {
      if (tab != null) {
        this.closeTab(tab);
      }
    }
    this.openTabs.clear();
    if (this.monaco != null) {
      this.monaco.close();
      this.monaco = null;
    }
    MonacoScriptEditor.shutdownCef();
  }

  @Override
  public void addNotify() {
    super.addNotify();
    this.externalChangeTimer.start();
    this.refreshScripts();
    if (UI.isScriptWorkspaceActive()) {
      this.focusOrOpenFirstScript();
    }
  }

  @Override
  public void removeNotify() {
    this.externalChangeTimer.stop();
    super.removeNotify();
  }

  public void focusOrOpenFirstScript() {
    if (Editor.instance().getGameFile() == null) return;
    List<ScriptDefinition> scriptDefs = Editor.instance().getGameFile().getScripts();
    if (scriptDefs == null || scriptDefs.isEmpty()) return;

    if (this.openTabs.isEmpty() || this.tabs.getSelectedIndex() < 0) {
      ScriptDefinition first = scriptDefs.getFirst();
      if (first != null) {
        this.open(first);
      }
    } else {
      ScriptTab active = activeTab();
      if (active != null && active.definition != null) {
        this.selectTreeNode(active.definition.getId());
      }
    }
  }

  public void onScriptSelected(Consumer<ScriptDefinition> listener) {
    this.selectionListener = listener == null ? ignored -> {} : listener;
    ScriptTab active = this.activeTab();
    this.selectionListener.accept(active == null ? null : active.definition);
  }

  public void refreshScripts() {
    String selectedId = this.selectedDefinition() == null ? null : this.selectedDefinition().getId();
    this.scriptsRoot.removeAllChildren();
    this.repairProjectScriptDefinitions();
    this.refreshProjectSourceDocuments();
    this.scriptUsageCounts = this.visibleUsageCounts();
    if (Editor.instance().getGameFile() != null) {
      String query = this.search.getText().strip().toLowerCase(Locale.ROOT);
      Set<String> gameScriptIds = Editor.instance().getGameFile().getScripts().stream()
          .map(d -> Objects.toString(d.getId(), ""))
          .collect(java.util.stream.Collectors.toSet());
      Set<String> gameScriptSources = Editor.instance().getGameFile().getScripts().stream()
          .map(d -> Objects.toString(d.getSource(), ""))
          .collect(java.util.stream.Collectors.toSet());
      Set<String> gameScriptImpls = Editor.instance().getGameFile().getScripts().stream()
          .map(d -> Objects.toString(d.getImplementation(), ""))
          .collect(java.util.stream.Collectors.toSet());

      if (query.isEmpty()) {
        List<String> diskPackages = ScriptSourcePaths.listPackageDirectories(Editor.instance().getProjectModel());
        Set<String> allPackages = new java.util.LinkedHashSet<>(diskPackages);
        allPackages.addAll(this.customCreatedPackages);
        allPackages.stream().sorted().forEach(this::insertPackageNode);
      }

      Editor.instance().getGameFile().getScripts().stream()
        .filter(definition -> query.isEmpty() || displayName(definition).toLowerCase(Locale.ROOT).contains(query)
          || Objects.toString(definition.getSource(), "").toLowerCase(Locale.ROOT).contains(query)
          || Objects.toString(definition.getImplementation(), "").toLowerCase(Locale.ROOT).contains(query))
        .sorted(Comparator.comparing(ScriptWorkspacePanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .forEach(definition -> {
          if (this.isProjectSource(definition) || (definition.getImplementation() != null && definition.getImplementation().contains("."))) this.insertProjectSourceNode(definition);
          else this.insertScriptNode(definition);
        });
      this.projectSourceDefinitions.values().stream()
        .filter(definition -> !gameScriptIds.contains(definition.getId())
            && !gameScriptSources.contains(definition.getSource())
            && !gameScriptImpls.contains(definition.getImplementation()))
        .filter(definition -> query.isEmpty()
          || displayName(definition).toLowerCase(Locale.ROOT).contains(query)
          || definition.getImplementation().toLowerCase(Locale.ROOT).contains(query))
        .sorted(Comparator.comparing(ScriptWorkspacePanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .forEach(this::insertProjectSourceNode);

      compactEmptyFolders(this.scriptsRoot);
    }
    this.scriptsModel.reload();
    for (int row = 0; row < this.scripts.getRowCount(); row++) this.scripts.expandRow(row);
    if (selectedId != null) {
      this.selectTreeNode(selectedId);
    } else if (UI.isScriptWorkspaceActive() || !this.openTabs.isEmpty()) {
      this.focusOrOpenFirstScript();
    }
    this.refreshGlobals();
    UI.refreshScriptInspectors();
  }

  private void refreshUsageIndicators() {
    this.scriptUsageCounts = this.visibleUsageCounts();
    this.scripts.treeDidChange();
    ScriptTab active = this.activeTab();
    ScriptDefinition definition = active == null ? null : active.definition;
    this.refreshActiveUsages();
    this.selectionListener.accept(definition);
  }

  private void scriptBindingsChanged() {
    this.reconcileOpenRuntimeTabs();
    this.refreshUsageIndicators();
  }

  private void reconcileOpenRuntimeTabs() {
    for (ScriptTab tab : List.copyOf(this.openTabs.values())) {
      if (tab.projectSource || tab.definition == null) continue;
      String expectedKey = this.documentKey(tab.definition);
      Path expectedPath = resolveSource(tab.definition.getSource());
      if (Objects.equals(tab.key, expectedKey) && Objects.equals(tab.path, expectedPath)) continue;
      ScriptTab conflicting = this.openTabs.get(expectedKey);
      if (conflicting != null && conflicting != tab) {
        this.closeTab(tab);
        continue;
      }
      this.openTabs.remove(tab.key, tab);
      tab.key = expectedKey;
      tab.path = expectedPath;
      this.openTabs.put(expectedKey, tab);
      tab.loadPreservingCaret();
    }
  }

  private Map<String, Integer> visibleUsageCounts() {
    if (Editor.instance().getGameFile() == null) return Map.of();
    Map<String, Integer> usageCounts = ScriptBindingService.instance().usageCounts();
    Map<String, Integer> result = new LinkedHashMap<>();
    for (ScriptDefinition definition : Editor.instance().getGameFile().getScripts()) {
      int count = usageCounts.getOrDefault(definition.getId(), 0);
      if (count > 0) result.put(definition.getId(), count);
    }
    return Map.copyOf(result);
  }

  public void open(ScriptDefinition definition) {
    if (definition == null) return;
    String key = documentKey(definition);
    ScriptTab tab = this.openTabs.computeIfAbsent(key, ignored -> {
      Path projectSource = this.projectSourcePaths.get(definition);
      ScriptTab created = new ScriptTab(definition, projectSource, projectSource != null);
      this.tabs.addTab(displayName(definition), Icons.getScriptIcon(definition), created, definition.getSource());
      this.tabs.setTabComponentAt(this.tabs.indexOfComponent(created), this.createTabHeader(created));
      return created;
    });
    this.tabs.setSelectedComponent(tab);
    this.selectTreeNode(definition.getId());
    this.activeTabChanged();
  }

  private void refreshProjectSourceDocuments() {
    this.projectSourcePaths.clear();
    if (Editor.instance().getGameFile() == null) return;
    Map<String, ScriptDefinition> registeredByImplementation = new LinkedHashMap<>();
    Map<String, ScriptDefinition> registeredById = new LinkedHashMap<>();
    for (ScriptDefinition definition : Editor.instance().getGameFile().getScripts()) {
      registeredByImplementation.put(definition.getImplementation(), definition);
      registeredById.put(definition.getId(), definition);
    }

    Map<String, ScriptDefinition> nextProjectDefinitions = new LinkedHashMap<>();
    List<ProjectCodeIntegration.ScriptClassDefinition> discoveredScripts =
      Editor.instance().getProjectCodeIntegration().getScriptDefinitions();
    Map<String, Long> discoveredIdCounts = discoveredScripts.stream().collect(
      java.util.stream.Collectors.groupingBy(
        ProjectCodeIntegration.ScriptClassDefinition::id, java.util.stream.Collectors.counting()));
    for (var discovered : discoveredScripts) {
      if (discovered.sourcePath() == null || !Files.isRegularFile(discovered.sourcePath())) continue;
      ScriptDefinition registered = registeredByImplementation.get(discovered.className());
      if (registered == null && discoveredIdCounts.getOrDefault(discovered.id(), 0L) == 1L) {
        registered = registeredById.get(discovered.id());
      }
      if (registered != null) {
        this.projectSourcePaths.put(registered, discovered.sourcePath());
        continue;
      }

      ScriptDefinition definition = this.projectSourceDefinitions.get(discovered.className());
      if (definition == null) {
        definition = new ScriptDefinition(
          discovered.id(), languageFor(discovered.sourcePath()), null,
          discovered.className(), discovered.host());
      }
      definition.setName(discovered.displayName());
      definition.setTargetType(discovered.targetType());
      nextProjectDefinitions.put(discovered.className(), definition);
      this.projectSourcePaths.put(definition, discovered.sourcePath());
    }
    for (Map.Entry<String, ScriptDefinition> entry : this.navigatedProjectDefinitions.entrySet()) {
      Path source = this.navigatedProjectSources.get(entry.getKey());
      if (source != null && Files.isRegularFile(source)) {
        nextProjectDefinitions.putIfAbsent(entry.getKey(), entry.getValue());
        this.projectSourcePaths.put(entry.getValue(), source);
      }
    }
    this.projectSourceDefinitions.clear();
    this.projectSourceDefinitions.putAll(nextProjectDefinitions);
    this.reconcileOpenProjectTabs();
  }

  private void reconcileOpenProjectTabs() {
    for (ScriptTab tab : List.copyOf(this.openTabs.values())) {
      Path source = this.projectSourcePaths.get(tab.definition);
      if (source == null) continue;
      String previousKey = tab.key;
      tab.projectSource = true;
      tab.path = source;
      tab.key = projectDocumentKey(source);
      if (!tab.key.equals(previousKey)) {
        ScriptTab existing = this.openTabs.get(tab.key);
        if (existing == null || existing == tab) {
          this.openTabs.remove(previousKey, tab);
          this.openTabs.put(tab.key, tab);
        } else {
          tab.key = previousKey;
        }
      }
    }
  }

  private void openProjectDefinition(MonacoScriptEditor.DefinitionTarget target) {
    if (target == null || target.path() == null || !Files.isRegularFile(target.path())) return;
    Path source = target.path().toAbsolutePath().normalize();
    ScriptDefinition definition = this.projectSourcePaths.entrySet().stream()
      .filter(entry -> source.equals(entry.getValue()))
      .map(Map.Entry::getKey)
      .findFirst().orElse(null);
    if (definition == null) {
      String implementation = target.className();
      if (implementation == null || implementation.isBlank()) {
        String filename = source.getFileName().toString();
        int extension = filename.lastIndexOf('.');
        implementation = extension > 0 ? filename.substring(0, extension) : filename;
      }
      String definitionKey = implementation;
      definition = this.navigatedProjectDefinitions.computeIfAbsent(definitionKey, className -> {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        ScriptDefinition created = new ScriptDefinition(
          className, languageFor(source), null, className, ScriptHostType.ENTITY);
        created.setName(simpleName);
        return created;
      });
      this.navigatedProjectSources.put(definitionKey, source);
      this.projectSourcePaths.put(definition, source);
      this.projectSourceDefinitions.putIfAbsent(definitionKey, definition);
      this.refreshScripts();
    }
    this.open(definition);
    if (this.monaco != null) this.monaco.revealPosition(target.line() + 1, target.column() + 1);
  }

  private static String languageFor(Path source) {
    return "java";
  }

  private boolean isProjectSource(ScriptDefinition definition) {
    return definition != null && this.projectSourcePaths.containsKey(definition);
  }

  private String documentKey(ScriptDefinition definition) {
    Path source = this.projectSourcePaths.get(definition);
    return source == null ? "runtime:" + definition.getId() : projectDocumentKey(source);
  }

  private static String projectDocumentKey(Path source) {
    return "project:" + source.toAbsolutePath().normalize();
  }

  public void saveActive() {
    ScriptTab tab = this.activeTab();
    if (tab != null && tab.save()) this.setStatus("Saved " + tab.path, false);
  }

  /** Applies inspector metadata to the active definition and its source declaration. */
  public void updateActiveMetadata(String name, ScriptHostType host, String targetType) {
    ScriptTab tab = this.activeTab();
    if (tab == null || host == null) return;
    if (tab.projectSource) {
      this.setStatus("Project script metadata is owned by its @ScriptInfo declaration", false);
      return;
    }
    ScriptDefinition definition = tab.definition;
    definition.setName(name == null || name.isBlank() ? definition.getId() : name.strip());
    definition.setHost(host);
    definition.setTargetType(host == ScriptHostType.ENTITY ? targetType : null);
    tab.synchronizeDeclaration();
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    recordUndoChanges();
    this.refreshScripts();
    this.selectTreeNode(definition.getId());
    this.selectionListener.accept(definition);
    this.setStatus("Updated script metadata; save to write the source declaration", false);
  }



  public void reloadActiveFromDisk() {
    ScriptTab tab = this.activeTab();
    if (tab == null) return;
    tab.load();
    this.setStatus("Reloaded " + tab.path, false);
  }

  public void formatActive() {
    if (this.monaco != null && this.monaco.isReady()) {
      this.monaco.triggerFormat();
      this.setStatus("Formatted active script", false);
    }
  }

  public void build() {
    this.reloadActive();
  }

  public void reloadActive() {
    if (!this.projectSourceBuildInProgress.compareAndSet(false, true)) {
      this.setStatus("A build is already in progress", true);
      return;
    }
    if (!this.saveAllScripts()) {
      this.projectSourceBuildInProgress.set(false);
      return;
    }
    ScriptTab tab = this.activeTab();
    String targetLabel = tab != null && tab.definition != null ? displayName(tab.definition) : "project scripts";
    this.appendOutput("Building " + targetLabel + " ...");
    this.setLaunchPhase(ProjectLaunchPhase.BUILDING);

    Thread.ofVirtual().name("utiliti-script-build").start(() -> {
      boolean successful = false;
      try {
        if (tab != null && tab.projectSource) {
          var session = Editor.instance().buildProjectClasses();
          session.onOutput(this::appendOutput);
          var completed = new java.util.concurrent.CountDownLatch(1);
          session.onStateChanged(state -> {
            if (state == de.gurkenlabs.utiliti.controller.ProjectSession.State.EXITED
                || state == de.gurkenlabs.utiliti.controller.ProjectSession.State.FAILED) {
              completed.countDown();
            }
          });
          if (!session.isActive()) completed.countDown();
          completed.await();
          successful = session.exitCode().orElse(-1) == 0;
          if (successful) {
            Editor.instance().reloadProjectCode();
            if (Editor.instance().getGameFile() != null) {
              Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
              if (Editor.instance().getGameFile().getScripts().contains(tab.definition)) {
                Game.scripts().clearDiagnostics();
                successful = Game.scripts().reload(tab.definition.getId());
              }
            }
          }
        } else {
          Editor.instance().reloadProjectCode();
          if (Editor.instance().getGameFile() != null) {
            Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
            Game.scripts().clearDiagnostics();
            if (tab != null && tab.definition != null) {
              successful = Game.scripts().reload(tab.definition.getId());
            } else {
              successful = true;
              for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
                if (!Game.scripts().reload(def.getId())) {
                  successful = false;
                }
              }
            }
          } else {
            successful = true;
          }
        }
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        successful = false;
        this.appendOutput("Build was interrupted.");
      } catch (Exception e) {
        log.log(Level.WARNING, "Could not build and reload scripts", e);
        successful = false;
        this.appendOutput("Build failed: " + e.getMessage());
      } finally {
        boolean result = successful;
        this.projectSourceBuildInProgress.set(false);
        SwingUtilities.invokeLater(() -> {
          this.setLaunchPhase(ProjectLaunchPhase.IDLE);
          this.refreshScripts();
          if (tab != null && tab.definition != null) {
            this.showDiagnostics(tab.definition);
          }
          this.appendOutput(result ? "Build successful; classes and scripts reloaded." : "Build failed; previous generation kept active.");
          this.setStatus(result ? "Built and reloaded " + targetLabel
              : "Build failed; the previous generation is still active", !result);
        });
      }
    });
  }

  /** Saves all open scripts and launches the project through its Gradle application run task. */
  public void runProject() {
    this.runProject(ProjectLaunchRequest.Mode.RUN);
  }

  public void debugProject() {
    this.runProject(ProjectLaunchRequest.Mode.DEBUG);
  }

  public void runProject(ProjectLaunchRequest.Mode mode) {
    if (this.projectLaunchPending) {
      this.setStatus("The project is already starting", false);
      return;
    }
    if (!this.saveAllScripts()) return;
    Editor.instance().prepareProjectLaunch();

    if (mode == ProjectLaunchRequest.Mode.DEBUG) {
      UI.showDebuggerTab();
      this.appendOutput("Saving project and preparing debugger...");
    } else {
      UI.showConsoleTab();
      this.appendOutput("Resolving Gradle project model and launching...");
    }

    this.projectLaunchPending = true;
    this.debuggerLaunchFailed = false;
    this.setLaunchPhase(ProjectLaunchPhase.SAVING);

    Thread.ofVirtual().name("utiliti-project-launch").start(() -> {
      try {
        if (Editor.instance().getCurrentResourceFile() != null) {
          Editor.instance().save(false);
        }

        this.setLaunchPhase(ProjectLaunchPhase.RESOLVING_MODEL);

        List<ScriptDefinition> debugDefinitions = mode == ProjectLaunchRequest.Mode.DEBUG
            ? Editor.instance().getGameFile().getScripts().stream().map(ScriptDefinition::new).toList()
            : List.of();

        ProjectSession session = Editor.instance().runProject(mode);
        session.onOutput(line -> SwingUtilities.invokeLater(() -> this.appendOutput(line)));
        session.onStateChanged(state -> {
          SwingUtilities.invokeLater(() -> this.projectStateChanged(session, state));
        });
        if (this.projectLaunchPending
            && Editor.instance().isProjectLaunchCancellationRequested()) {
          session.stop();
          this.finishProjectLaunch(ProjectLaunchPhase.CANCELLED);
          return;
        }
        if (mode == ProjectLaunchRequest.Mode.DEBUG) {
          this.setLaunchPhase(ProjectLaunchPhase.ATTACHING_DEBUGGER);
          this.attachDebugger(debugDefinitions);
        }
      } catch (ProjectLaunchCancelledException cancelled) {
        this.finishProjectLaunch(ProjectLaunchPhase.CANCELLED);
        SwingUtilities.invokeLater(() -> this.setStatus("Project launch cancelled", false));
      } catch (IOException error) {
        if (mode == ProjectLaunchRequest.Mode.DEBUG) {
          this.closeDebugger();
          Editor.instance().stopProject();
        }
        this.finishProjectLaunch(ProjectLaunchPhase.FAILED);
        SwingUtilities.invokeLater(() -> {
          this.appendOutput("Could not start project: " + error.getMessage());
          this.setStatus("Could not start project: " + error.getMessage(), true);
        });
      }
    });
  }

  private void setLaunchPhase(ProjectLaunchPhase phase) {
    UI.updateRunControlStates(phase);
    if (phase != null && !phase.displayText().isBlank()) {
      SwingUtilities.invokeLater(() -> this.setStatus(phase.displayText(), phase == ProjectLaunchPhase.FAILED));
    }
  }

  private void finishProjectLaunch(ProjectLaunchPhase phase) {
    SwingUtilities.invokeLater(() -> {
      this.projectLaunchPending = false;
      UI.updateRunControlStates(phase);
    });
  }

  private void attachDebugger(List<ScriptDefinition> debugDefinitions) {
    this.closeDebugger();
    JdiScriptDebuggerBackend backend = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void stateChanged(ScriptDebuggerBackend.State state, String detail) {
        SwingUtilities.invokeLater(() -> debuggerPanel.updateState(state, detail));
        if (state == ScriptDebuggerBackend.State.RUNNING || state == ScriptDebuggerBackend.State.PAUSED) {
          finishProjectLaunch(state == ScriptDebuggerBackend.State.PAUSED
              ? ProjectLaunchPhase.PAUSED : ProjectLaunchPhase.RUNNING);
        } else if (state == ScriptDebuggerBackend.State.FAILED) {
          if (ScriptWorkspacePanel.this.projectLaunchPending) {
            ScriptWorkspacePanel.this.debuggerLaunchFailed = true;
          }
          finishProjectLaunch(ProjectLaunchPhase.FAILED);
        }
      }

      @Override
      public void paused(ScriptDebugSnapshot snapshot) {
        SwingUtilities.invokeLater(() -> handleDebugSnapshot(snapshot));
      }
    });

    this.debuggerPanel.onResume(() -> backend.resume());
    this.debuggerPanel.onPause(() -> backend.pause());
    this.debuggerPanel.onStepOver(() -> backend.stepOver());
    this.debuggerPanel.onStepInto(() -> backend.stepInto());
    this.debuggerPanel.onStepOut(() -> backend.stepOut());
    this.debuggerPanel.onExpandVariable(variable -> {
      if (variable != null && variable.reference() != null) {
        Thread.ofVirtual().name("utiliti-debug-expand").start(() -> {
          List<ScriptDebugSnapshot.Variable> children = backend.expandVariable(variable.reference());
          SwingUtilities.invokeLater(() -> this.debuggerPanel.showVariableChildren(variable.reference(), children));
        });
      }
    });
    this.debuggerPanel.onStop(() -> {
      this.closeDebugger();
      Editor.instance().stopProject();
    });

    this.debugger = backend;
    int port = Editor.instance().getProjectDebugPort();
    try {
      backend.setBreakpoints(this.currentProjectBreakpoints());
      backend.attach("127.0.0.1", port, debugDefinitions);
    } catch (IOException e) {
      log.log(Level.WARNING, "Failed to attach debugger on port " + port, e);
      boolean cancelled = Editor.instance().isProjectLaunchCancellationRequested();
      this.debuggerLaunchFailed = !cancelled;
      if (cancelled) this.closeDebugger();
      Editor.instance().stopProject();
      this.finishProjectLaunch(cancelled ? ProjectLaunchPhase.CANCELLED : ProjectLaunchPhase.FAILED);
      SwingUtilities.invokeLater(() -> {
        if (cancelled) {
          this.setStatus("Project launch cancelled", false);
        } else {
          this.appendOutput("Debugger attach failed: " + e.getMessage());
          this.setStatus("Debugger attach failed: " + e.getMessage(), true);
        }
      });
    }
  }

  private void closeDebugger() {
    if (this.debugger != null) {
      try {
        this.debugger.close();
      } catch (Exception ignored) {}
      this.debugger = null;
    }
    SwingUtilities.invokeLater(() -> {
      this.handleDebugResumed();
      this.debuggerPanel.updateState(ScriptDebuggerBackend.State.DISCONNECTED, "Debugger disconnected");
    });
  }

  private void handleDebugSnapshot(ScriptDebugSnapshot snapshot) {
    ScriptDebugSnapshot.Frame top = snapshot == null || snapshot.frames().isEmpty() ? null : snapshot.frames().getFirst();
    this.debuggerPanel.showSnapshot(snapshot, top);
    if (snapshot != null && top != null) {
      ScriptDefinition def = definitionForClass(top.className());
      this.executionScriptId = def == null ? null : this.breakpointIdentity(def);
      this.executionLine = top.line();
      this.executionVariables = top.variables();
      if (def != null) {
        this.open(def);
        this.updateMonacoDebugState(def);
      }
    } else {
      this.handleDebugResumed();
    }
  }

  private void handleDebugResumed() {
    this.executionScriptId = null;
    this.executionLine = 0;
    this.executionVariables = List.of();
    this.debuggerPanel.showSnapshot(null, null);
    if (this.monacoTab != null && this.monacoTab.definition != null) {
      this.updateMonacoDebugState(this.monacoTab.definition);
    }
  }

  private ScriptDefinition definitionForClass(String className) {
    if (className == null || Editor.instance().getGameFile() == null) return null;
    ScriptDefinition runtimeDefinition = Editor.instance().getGameFile().getScripts().stream().filter(definition -> {
      String implementation = definition.getImplementation();
      return implementation != null && (implementation.equals(className) || className.startsWith(implementation + "$"));
    }).findFirst().orElse(null);
    if (runtimeDefinition != null) return runtimeDefinition;
    return this.projectSourcePaths.keySet().stream().filter(definition -> {
      String implementation = definition.getImplementation();
      return implementation != null && (implementation.equals(className) || className.startsWith(implementation + "$"));
    }).findFirst().orElse(null);
  }

  private void replaceBreakpoints(ScriptDefinition definition, List<Integer> lines) {
    if (definition == null) return;
    String project = this.projectKey();
    String scriptId = this.breakpointIdentity(definition);
    String source = Objects.toString(definition.getSource(), "");
    List<Integer> normalized = lines == null ? List.of() : lines.stream()
        .filter(line -> line != null && line > 0).distinct().sorted().toList();
    List<Integer> existing = this.breakpoints.stream()
        .filter(item -> item.project().equals(project) && item.scriptId().equals(scriptId) && item.source().equals(source))
        .map(ScriptBreakpoint::line).sorted().toList();
    if (existing.equals(normalized)) return;
    this.breakpoints.removeIf(item -> item.project().equals(project)
        && item.scriptId().equals(scriptId) && item.source().equals(source));
    normalized.forEach(line -> this.breakpoints.add(new ScriptBreakpoint(project, scriptId, source, line, true)));
    this.breakpointSyncTimer.restart();
    this.updateMonacoDebugState(definition);
  }

  private void syncBreakpoints() {
    String serialized = ScriptBreakpointStore.encode(this.breakpoints);
    List<ScriptBreakpoint> activeBreakpoints = this.currentProjectBreakpoints();
    ScriptDebuggerBackend current = this.debugger;
    this.breakpointSyncExecutor.execute(() -> {
      String previous = Editor.preferences().getScriptBreakpoints();
      if (!Objects.equals(previous, serialized)) {
        Editor.preferences().setScriptBreakpoints(serialized);
        Game.config().save();
      }
      if (current != null && current == this.debugger) current.setBreakpoints(activeBreakpoints);
    });
  }

  private List<ScriptBreakpoint> currentProjectBreakpoints() {
    String project = this.projectKey();
    return this.breakpoints.stream().filter(item -> item.project().equals(project)).toList();
  }

  private void updateMonacoDebugState(ScriptDefinition definition) {
    if (this.monaco == null || definition == null) return;
    String project = this.projectKey();
    String scriptId = this.breakpointIdentity(definition);
    String source = Objects.toString(definition.getSource(), "");
    List<Integer> lines = this.breakpoints.stream()
        .filter(item -> item.enabled() && item.project().equals(project)
            && item.scriptId().equals(scriptId) && item.source().equals(source))
        .map(ScriptBreakpoint::line).distinct().sorted().toList();
    int currentLine = scriptId.equals(this.executionScriptId) ? this.executionLine : 0;
    List<ScriptDebugSnapshot.Variable> variables = currentLine > 0 ? this.executionVariables : List.of();
    this.monaco.setDebugState(lines, currentLine, variables);
  }

  private String breakpointIdentity(ScriptDefinition definition) {
    if (Editor.instance().getGameFile() != null
        && Editor.instance().getGameFile().getScripts().stream().anyMatch(candidate -> candidate == definition)) {
      return definition.getId();
    }
    String implementation = definition.getImplementation();
    return implementation == null || implementation.isBlank() ? definition.getId() : implementation;
  }

  private String projectKey() {
    Path project = Editor.instance().getProjectPath();
    return project == null ? "" : project.toAbsolutePath().normalize().toString();
  }

  public void stopProject() {
    ProjectSession session = Editor.instance().getProjectSession();
    if (this.projectSourceBuildInProgress.get()) {
      this.setStatus("Cancelling project source build...", false);
      Editor.instance().stopProject();
      return;
    }
    if (this.projectLaunchPending) {
      this.setLaunchPhase(ProjectLaunchPhase.STOPPING);
      this.appendOutput("Cancelling project launch...");
      Editor.instance().stopProject();
      this.closeDebugger();
      return;
    }
    if (session == null || !session.isActive()) {
      this.setStatus("No project is running", false);
      return;
    }
    this.appendOutput("Stopping project...");
    Editor.instance().stopProject();
  }

  public void restartProject() {
    ProjectSession session = Editor.instance().getProjectSession();
    if (session == null || !session.isActive()) {
      this.runProject();
      return;
    }
    this.restartRequested = true;
    this.appendOutput("Restarting project...");
    Editor.instance().stopProject();
  }

  private boolean saveAllScripts() {
    for (ScriptTab tab : this.openTabs.values()) {
      if (tab.dirty && !tab.save()) {
        this.setStatus("Could not save " + displayName(tab.definition), true);
        return false;
      }
    }
    return true;
  }

  private void projectStateChanged(ProjectSession session, ProjectSession.State state) {
    switch (state) {
      case STARTING, BUILDING -> this.setLaunchPhase(ProjectLaunchPhase.BUILDING);
      case STARTING_GAME -> this.setLaunchPhase(
          Editor.instance().getProjectLaunchMode() == ProjectLaunchRequest.Mode.DEBUG
              ? ProjectLaunchPhase.ATTACHING_DEBUGGER : ProjectLaunchPhase.STARTING_GAME);
      case RUNNING -> {
        this.setStatus("Project is running", false);
        if (Editor.instance().getProjectLaunchMode() == ProjectLaunchRequest.Mode.RUN) {
          this.finishProjectLaunch(ProjectLaunchPhase.RUNNING);
        }
      }
      case STOPPING -> this.setLaunchPhase(ProjectLaunchPhase.STOPPING);
      case EXITED -> {
        if (this.debuggerLaunchFailed) {
          this.debuggerLaunchFailed = false;
          this.finishProjectLaunch(ProjectLaunchPhase.FAILED);
          this.restartRequested = false;
          return;
        }
        if (this.projectLaunchPending
            && Editor.instance().isProjectLaunchCancellationRequested()) {
          this.setStatus("Project launch cancelled", false);
          this.finishProjectLaunch(ProjectLaunchPhase.CANCELLED);
          this.restartRequested = false;
          return;
        }
        int exitCode = session.exitCode().orElse(-1);
        String failure = session.failureMessage().orElse(null);
        if (exitCode != 0 && failure != null) {
          this.appendOutput("Project launch failed: " + failure);
          this.setStatus("Gradle setup failed; see Console for recovery steps", true);
        } else {
          this.appendOutput("Project exited with code " + exitCode + ".");
          this.setStatus("Project exited with code " + exitCode, exitCode != 0);
        }
        this.finishProjectLaunch(exitCode == 0 ? ProjectLaunchPhase.IDLE : ProjectLaunchPhase.FAILED);
        if (this.restartRequested) {
          this.restartRequested = false;
          this.runProject();
        }
      }
      case FAILED -> {
        String failure = session.failureMessage().orElse("Project launch failed.");
        this.appendOutput(failure);
        this.setStatus(failure, true);
        this.finishProjectLaunch(ProjectLaunchPhase.FAILED);
        this.restartRequested = false;
      }
    }
  }

  public void configureProjectForIntellij() {
    Path root = projectRoot();
    if (root == null) {
      this.setStatus("Open a project before configuring IntelliJ support.", true);
      return;
    }
    try {
      GradleScriptProjectSupport.Result result = GradleScriptProjectSupport.configure(root);
      this.setStatus(result.configured() ? "Gradle is configured for Java scripts and IntelliJ."
        : String.join(" ", result.issues()), !result.configured());
    } catch (IOException error) {
      this.setStatus("Could not configure the Gradle project: " + error.getMessage(), true);
    }
  }

  public void refreshTheme() {
    this.setBackground(Style.background());
    this.scripts.setBackground(Style.background());
    this.scripts.setForeground(Style.text());
    this.problems.setBackground(Style.surface());
    this.problems.setForeground(Style.text());
    this.problems.setGridColor(Style.border());
    if (this.problems.getTableHeader() != null) {
      this.problems.getTableHeader().setBackground(Style.surface());
      this.problems.getTableHeader().setForeground(Style.text());
      this.problems.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    }
    if (this.overviewPanel != null) {
      this.overviewPanel.refreshTheme();
    }
    if (this.debuggerPanel != null) {
      this.debuggerPanel.refreshTheme();
    }
    if (this.sidebarTabs != null) {
      this.sidebarTabs.setBackground(Style.background());
      this.sidebarTabs.setForeground(Style.text());
      this.sidebarTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    }
    if (this.tabStrip != null) {
      this.tabStrip.setBackground(Style.background());
      this.tabStrip.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    }
    if (this.statusBar != null) {
      this.statusBar.setBackground(Style.background());
      this.statusBar.setBorder(BorderFactory.createEmptyBorder(2, 8, 2, 8));
    }
    if (this.tabs != null) {
      this.tabs.setBackground(Style.background());
      this.tabs.setForeground(Style.text());
    }
    if (this.monaco != null) {
      this.monaco.setTheme(Editor.preferences().getTheme() == Style.Theme.DARK);
    }
    this.caretStatus.setForeground(Style.mutedText());
    this.status.setForeground(Style.mutedText());
    this.scriptUsageStatusLabel.setForeground(Style.mutedText());
    this.globalsTree.setBackground(Style.background());
    this.globalsTree.setForeground(Style.text());
    this.repaint();
  }

  private JPanel createConflictBar() {
    this.conflictBar.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
      BorderFactory.createEmptyBorder(5, 10, 5, 8)));
    this.conflictBar.setBackground(Style.surface());
    this.conflictMessage.setForeground(Style.text());
    this.conflictBar.add(this.conflictMessage, BorderLayout.CENTER);
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
    actions.setOpaque(false);
    JButton reload = new JButton("Reload disk version");
    reload.addActionListener(event -> {
      if (this.conflictTab != null) this.conflictTab.loadPreservingCaret();
      this.hideConflict();
    });
    JButton keep = new JButton("Keep editor version");
    keep.addActionListener(event -> {
      if (this.conflictTab != null) this.conflictTab.acceptExternalVersion();
      this.hideConflict();
    });
    actions.add(reload);
    actions.add(keep);
    this.conflictBar.add(actions, BorderLayout.EAST);
    this.conflictBar.setVisible(false);
    return this.conflictBar;
  }

  private JPanel createScriptExplorer() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    RoundedSearchBox searchBox = new RoundedSearchBox(this.search, 0);
    searchBox.getClearButton().addActionListener(e -> {
      this.search.setText("");
      this.refreshScripts();
    });
    panel.add(searchBox, BorderLayout.NORTH);

    this.scripts.getSelectionModel().setSelectionMode(javax.swing.tree.TreeSelectionModel.SINGLE_TREE_SELECTION);
    this.scripts.setCellRenderer(new ScriptTreeRenderer());
    this.scripts.addTreeSelectionListener(event -> {
      ScriptDefinition definition = this.selectedDefinition();
      if (definition != null) this.open(definition);
    });

    this.scripts.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override public void mousePressed(java.awt.event.MouseEvent e) { showTreeContextMenu(e); }
      @Override public void mouseReleased(java.awt.event.MouseEvent e) { showTreeContextMenu(e); }
    });

    this.scripts.registerKeyboardAction(
      evt -> {
        Object selected = this.scripts.getLastSelectedPathComponent();
        if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptTreeItem item) {
          if (item.isPackage()) {
            renamePackage(item.packageName());
          } else if (item.definition() != null) {
            renameScript(item.definition());
          }
        }
      },
      KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0),
      JComponent.WHEN_FOCUSED
    );

    this.scripts.registerKeyboardAction(
      evt -> {
        Object selected = this.scripts.getLastSelectedPathComponent();
        if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptTreeItem item) {
          if (item.isPackage()) {
            deletePackage(item.packageName());
          } else if (item.definition() != null) {
            deleteScript(item.definition());
          }
        }
      },
      KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_DELETE, 0),
      JComponent.WHEN_FOCUSED
    );

    panel.add(StyledTree.createScrollPane(this.scripts), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createGlobalsPanel() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setOpaque(false);
    panel.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    JPanel searchRow = new JPanel(new BorderLayout(4, 0));
    searchRow.setOpaque(false);

    RoundedSearchBox searchBox = new RoundedSearchBox(this.globalsSearch, 0);
    searchBox.getClearButton().addActionListener(e -> {
      this.globalsSearch.setText("");
      this.refreshGlobals();
    });
    searchRow.add(searchBox, BorderLayout.CENTER);

    JButton exploreButton = Style.iconButton(Icons.API_16);
    exploreButton.setToolTipText("Open Script Events & API Explorer");
    exploreButton.addActionListener(e -> de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog.showDialog());
    searchRow.add(exploreButton, BorderLayout.EAST);

    panel.add(searchRow, BorderLayout.NORTH);

    this.globalsTree.setCellRenderer(new GlobalApiTreeRenderer());
    this.globalsTree.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override public void mouseClicked(java.awt.event.MouseEvent event) {
        if (event.getClickCount() == 2) {
          TreePath path = globalsTree.getSelectionPath();
          if (path != null && path.getLastPathComponent() instanceof DefaultMutableTreeNode node) {
            if (node.getUserObject() instanceof GlobalApiItem item) {
              insertTextToActiveScript(item.snippet());
            }
          }
        }
      }
    });

    this.globalsSearch.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) { refreshGlobals(); }
      @Override public void removeUpdate(DocumentEvent e) { refreshGlobals(); }
      @Override public void changedUpdate(DocumentEvent e) { refreshGlobals(); }
    });

    this.refreshGlobals();

    panel.add(StyledTree.createScrollPane(this.globalsTree), BorderLayout.CENTER);
    return panel;
  }


  private ScriptHostType getActiveHostType() {
    ScriptTab active = this.activeTab();
    if (active == null) return null;
    if (active.definition != null && active.definition.getHost() != null) {
      return active.definition.getHost();
    }
    String text = active.getText();
    if (text != null) {
      if (text.contains("extends GameScript") || text.contains("ScriptHostType.GAME")) {
        return ScriptHostType.GAME;
      }
      if (text.contains("extends EnvironmentScript") || text.contains("ScriptHostType.ENVIRONMENT")) {
        return ScriptHostType.ENVIRONMENT;
      }
      if (text.contains("extends CreatureScript") || text.contains("extends EntityScript") || text.contains("ScriptHostType.ENTITY")) {
        return ScriptHostType.ENTITY;
      }
    }
    return null;
  }

  public void refreshGlobals() {
    this.globalsRoot.removeAllChildren();
    String query = this.globalsSearch.getText() == null ? "" : this.globalsSearch.getText().toLowerCase(Locale.ROOT).trim();
    ScriptHostType hostType = this.getActiveHostType();

    if (hostType == ScriptHostType.GAME) {
      // --- GAME SCRIPT CONTEXT ---
      DefaultMutableTreeNode servicesGroup = new DefaultMutableTreeNode("Game APIs & Services");
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("globals", "globals.put(\"key\", value);", "Shared state across maps", "g"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("loadMap(...)", "loadMap(\"level1\");", "Transition / load starting map", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("playMusic(...)", "playMusic(\"main_theme\");", "Play background music track", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("stopMusic()", "stopMusic();", "Stop background soundtrack", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.world()", "Game.world()", "Map & world manager", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.loop()", "Game.loop()", "Main loop & frame updates", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.audio()", "Game.audio()", "Sound & music engine", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.physics()", "Game.physics()", "Collision & physics engine", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.graphics()", "Game.graphics()", "Render engine & camera", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Input.keyboard()", "Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, event -> {});", "Global keyboard hotkeys", "m"), query);
      if (servicesGroup.getChildCount() > 0) this.globalsRoot.add(servicesGroup);

      DefaultMutableTreeNode hooksGroup = new DefaultMutableTreeNode("Game Lifecycle Hooks");
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onStarted()", "\n  @Override\n  public void onStarted() {\n    // Game startup initialization\n  }\n", "Game boot & startup hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("update()", "\n  @Override\n  public void update() {\n    // Global game tick loop\n  }\n", "Global game tick loop", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onStopped()", "\n  @Override\n  public void onStopped() {\n    // Game shutdown cleanup\n  }\n", "Game shutdown hook", "hook"), query);
      if (hooksGroup.getChildCount() > 0) this.globalsRoot.add(hooksGroup);

      DefaultMutableTreeNode uiGroup = new DefaultMutableTreeNode("Global UI & Timers");
      addGlobalItemIfMatches(uiGroup, new GlobalApiItem("context().ui().showBanner(...)", "context().ui().showBanner(\"TITLE\", \"Subtitle\", 3000);", "Announcement banner", "u"), query);
      addGlobalItemIfMatches(uiGroup, new GlobalApiItem("context().ui().drawScreenText(...)", "context().ui().drawScreenText(\"SCORE: \" + score, 16, 24, Color.WHITE);", "Screen-space HUD text", "u"), query);
      addGlobalItemIfMatches(uiGroup, new GlobalApiItem("context().schedule(...)", "context().schedule(1000, () -> {});", "Managed delayed execution", "u"), query);
      if (uiGroup.getChildCount() > 0) this.globalsRoot.add(uiGroup);

    } else if (hostType == ScriptHostType.ENVIRONMENT) {
      // --- ENVIRONMENT SCRIPT CONTEXT ---
      DefaultMutableTreeNode servicesGroup = new DefaultMutableTreeNode("Environment APIs");
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("environment()", "environment()", "Active map environment", "e"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("globals", "globals", "Shared ScriptGlobals store", "g"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("context()", "context()", "Script context & properties", "c"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("EntityQuery", "EntityQuery.in(environment(), Creature.class)", "Fluent entity finder", "q"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.world()", "Game.world()", "Map & world entity manager", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.audio()", "Game.audio()", "Sound & music engine", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.graphics()", "Game.graphics()", "Render engine & camera", "m"), query);
      if (servicesGroup.getChildCount() > 0) this.globalsRoot.add(servicesGroup);

      DefaultMutableTreeNode hooksGroup = new DefaultMutableTreeNode("Environment Lifecycle Hooks");
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onLoaded()", "\n  @Override\n  public void onLoaded() {\n    // Map loaded and active\n  }\n", "Map loaded and active hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("update()", "\n  @Override\n  public void update() {\n    // Map tick loop\n  }\n", "Map tick loop", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onEntityAdded(entity)", "\n  @Override\n  protected void onEntityAdded(IEntity entity) {\n  }\n", "Environment entity spawned hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onEntityRemoved(entity)", "\n  @Override\n  protected void onEntityRemoved(IEntity entity) {\n    // Enemy defeated / removed hook\n  }\n", "Environment entity despawned hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onUnloaded()", "\n  @Override\n  public void onUnloaded() {\n  }\n", "Map unloading hook", "hook"), query);
      if (hooksGroup.getChildCount() > 0) this.globalsRoot.add(hooksGroup);

      DefaultMutableTreeNode actionsGroup = new DefaultMutableTreeNode("Map Actions & Cinematics");
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("context().ui().showBanner(...)", "context().ui().showBanner(\"STAGE 1\", \"Fight!\", 3000);", "Show announcement banner", "u"), query);
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("context().ui().drawScreenText(...)", "context().ui().drawScreenText(\"WAVE: 1\", 16, 24, Color.YELLOW);", "Screen-space HUD text", "u"), query);
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("cameraPanTo(target, ticks)", "context().sequence().cameraPanTo(targetEntity, 60);", "Cinematic camera pan", "u"), query);
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("cameraZoom(zoom, duration)", "context().sequence().cameraZoom(1.5f, 500);", "Cinematic camera zoom", "u"), query);
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("screenShake(strength, dur)", "context().sequence().screenShake(8.0f, 30, 20);", "Camera screen shake", "u"), query);
      addGlobalItemIfMatches(actionsGroup, new GlobalApiItem("context().schedule(...)", "context().schedule(2000, () -> {});", "Managed timer action", "u"), query);
      if (actionsGroup.getChildCount() > 0) this.globalsRoot.add(actionsGroup);

      // Named Map Entities on this map
      if (Game.world() != null && Game.world().environment() != null) {
        DefaultMutableTreeNode entitiesGroup = new DefaultMutableTreeNode("Named Map Entities");
        for (de.gurkenlabs.litiengine.entities.IEntity entity : Game.world().environment().getEntities()) {
          String name = entity.getName();
          if (name != null && !name.isBlank()) {
            String snippet = "environment().get(\"" + name + "\")";
            String typeName = entity.getClass().getSimpleName();
            String badge = switch (typeName) {
              case "Creature" -> "creature";
              case "Prop" -> "prop";
              case "Trigger" -> "trigger";
              case "Emitter" -> "emitter";
              default -> "entity";
            };
            addGlobalItemIfMatches(entitiesGroup, new GlobalApiItem(name, snippet, typeName + " on map", badge), query);
          }
        }
        if (entitiesGroup.getChildCount() > 0) this.globalsRoot.add(entitiesGroup);
      }

    } else if (hostType == ScriptHostType.ENTITY) {
      // --- ENTITY / CREATURE SCRIPT CONTEXT ---
      DefaultMutableTreeNode servicesGroup = new DefaultMutableTreeNode("Entity APIs");
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("host()", "host()", "Entity / Creature script instance", "h"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("environment()", "environment()", "Active map environment", "e"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("context()", "context()", "Script context & properties", "c"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("globals", "globals", "Shared ScriptGlobals store", "g"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("EntityQuery", "EntityQuery.in(environment(), Creature.class)", "Fluent entity finder", "q"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.audio()", "Game.audio()", "Sound & music engine", "m"), query);
      if (servicesGroup.getChildCount() > 0) this.globalsRoot.add(servicesGroup);

      DefaultMutableTreeNode hooksGroup = new DefaultMutableTreeNode("Entity Lifecycle Hooks");
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onLoaded()", "\n  @Override\n  public void onLoaded() {\n  }\n", "Entity loaded into map", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("update()", "\n  @Override\n  public void update() {\n  }\n", "Entity tick loop", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onHit(event)", "\n  @Override\n  protected void onHit(EntityHitEvent event) {\n    int damage = event.getDamage();\n  }\n", "Damage received hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onDeath(entity, hitEvent)", "\n  @Override\n  protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {\n    remove();\n  }\n", "Entity mortality hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onCollision(event)", "\n  @Override\n  protected void onCollision(CollisionEvent event) {\n  }\n", "Obstacle collision hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onInteract(source)", "\n  @Override\n  protected void onInteract(IEntity source) {\n  }\n", "Player interaction hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onMessage(msg, sender)", "\n  @Override\n  protected void onMessage(String message, Object sender) {\n  }\n", "Message received hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onUnloaded()", "\n  @Override\n  public void onUnloaded() {\n  }\n", "Entity unloaded hook", "hook"), query);
      if (hooksGroup.getChildCount() > 0) this.globalsRoot.add(hooksGroup);

      DefaultMutableTreeNode combatGroup = new DefaultMutableTreeNode("Combat & Actions");
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("createAbility(name)", "createAbility(\"Fireball\").range(200).cooldown(1000).onCast(exec -> {}).cast();", "Build & cast ability", "a"), query);
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("spawnProjectile()", "spawnProjectile().from(host().getCenter()).speed(300).damage(20).splash(30, 10).spawn();", "Launch projectile", "p"), query);
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("moveTowards(target)", "moveTowards(targetEntity);", "Move creature towards target", "m"), query);
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("floatText(text, entity, color)", "context().ui().floatText(\"-25\", host(), Color.RED);", "Floating combat text", "u"), query);
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("sendMessage(target, msg)", "sendMessage(targetEntity, \"alert\");", "Send entity message", "m"), query);
      addGlobalItemIfMatches(combatGroup, new GlobalApiItem("remove()", "remove();", "Despawn host entity", "m"), query);
      if (combatGroup.getChildCount() > 0) this.globalsRoot.add(combatGroup);

      // Named Map Entities
      if (Game.world() != null && Game.world().environment() != null) {
        DefaultMutableTreeNode entitiesGroup = new DefaultMutableTreeNode("Named Map Entities");
        for (de.gurkenlabs.litiengine.entities.IEntity entity : Game.world().environment().getEntities()) {
          String name = entity.getName();
          if (name != null && !name.isBlank()) {
            String snippet = "environment().get(\"" + name + "\")";
            String typeName = entity.getClass().getSimpleName();
            String badge = switch (typeName) {
              case "Creature" -> "creature";
              case "Prop" -> "prop";
              case "Trigger" -> "trigger";
              case "Emitter" -> "emitter";
              default -> "entity";
            };
            addGlobalItemIfMatches(entitiesGroup, new GlobalApiItem(name, snippet, typeName + " on map", badge), query);
          }
        }
        if (entitiesGroup.getChildCount() > 0) this.globalsRoot.add(entitiesGroup);
      }

    } else {
      // --- GENERAL / UNKNOWN CONTEXT ---
      DefaultMutableTreeNode servicesGroup = new DefaultMutableTreeNode("Globals & Services");
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("host()", "host()", "Entity script instance", "h"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("environment()", "environment()", "Active map environment", "e"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("context()", "context()", "Script context & properties", "c"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("globals", "globals", "Shared ScriptGlobals store", "g"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("Game.world()", "Game.world()", "Map & world entity manager", "m"), query);
      addGlobalItemIfMatches(servicesGroup, new GlobalApiItem("EntityQuery", "EntityQuery.in(environment(), Creature.class)", "Fluent entity finder", "q"), query);
      if (servicesGroup.getChildCount() > 0) this.globalsRoot.add(servicesGroup);

      DefaultMutableTreeNode hooksGroup = new DefaultMutableTreeNode("Lifecycle Hooks");
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onStarted()", "\n  @Override\n  public void onStarted() {\n  }\n", "Game startup hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onLoaded()", "\n  @Override\n  public void onLoaded() {\n  }\n", "Map/Entity loaded hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("update()", "\n  @Override\n  public void update() {\n  }\n", "Tick update loop", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onHit(event)", "\n  @Override\n  protected void onHit(EntityHitEvent event) {\n  }\n", "Damage received hook", "hook"), query);
      addGlobalItemIfMatches(hooksGroup, new GlobalApiItem("onDeath(entity, hitEvent)", "\n  @Override\n  protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {\n  }\n", "Entity mortality hook", "hook"), query);
      if (hooksGroup.getChildCount() > 0) this.globalsRoot.add(hooksGroup);
    }

    // Registered Global Variables (relevant for all contexts)
    if (Game.scripts() != null && Game.scripts().globals() != null && !Game.scripts().globals().getEntries().isEmpty()) {
      DefaultMutableTreeNode varsGroup = new DefaultMutableTreeNode("Global Variables (globals)");
      for (Map.Entry<String, Object> entry : Game.scripts().globals().getEntries().entrySet()) {
        String key = entry.getKey();
        Object val = entry.getValue();
        String typeName = val == null ? "Object" : val.getClass().getSimpleName();
        addGlobalItemIfMatches(varsGroup, new GlobalApiItem(key, "globals.get(\"" + key + "\")", "Global variable (" + typeName + ")", "g"), query);
      }
      if (varsGroup.getChildCount() > 0) this.globalsRoot.add(varsGroup);
    }

    this.globalsTreeModel.reload();

    // Expand top-level groups by default or when searching
    for (int i = 0; i < this.globalsTree.getRowCount(); i++) {
      this.globalsTree.expandRow(i);
    }
  }

  private static void addGlobalItemIfMatches(DefaultMutableTreeNode group, GlobalApiItem item, String query) {
    if (query.isEmpty()
        || item.label().toLowerCase(Locale.ROOT).contains(query)
        || item.description().toLowerCase(Locale.ROOT).contains(query)
        || item.snippet().toLowerCase(Locale.ROOT).contains(query)) {
      group.add(new DefaultMutableTreeNode(item));
    }
  }

  private static JScrollPane createBorderlessScrollPane(java.awt.Component view) {
    JScrollPane scroll = new JScrollPane(view);
    scroll.setBorder(BorderFactory.createEmptyBorder());
    scroll.setViewportBorder(null);
    scroll.setOpaque(false);
    scroll.getViewport().setOpaque(true);
    scroll.getViewport().setBackground(Style.background());
    return scroll;
  }


  public void insertTextToActiveScript(String text) {
    if (this.monaco != null && this.monaco.isReady() && text != null && !text.isEmpty()) {
      this.monaco.insertText(text);
    }
  }

  private record GlobalApiItem(String label, String snippet, String description, String badge) {}

  private static JLabel sectionTitle(String text) {
    JLabel title = new JLabel(text);
    title.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
    title.setForeground(Style.mutedText());
    return title;
  }

  private static final class TabCloseButton extends JButton {
    private boolean hovered;

    TabCloseButton(Runnable onClose) {
      setContentAreaFilled(false);
      setBorderPainted(false);
      setFocusPainted(false);
      setFocusable(false);
      setOpaque(false);
      setRolloverEnabled(true);
      setPreferredSize(new Dimension(18, 18));
      setMaximumSize(new Dimension(18, 18));
      setMinimumSize(new Dimension(18, 18));
      setToolTipText(Resources.strings().get("close", "Close"));
      addMouseListener(new MouseAdapter() {
        @Override public void mouseEntered(MouseEvent e) { hovered = true; repaint(); }
        @Override public void mouseExited(MouseEvent e) { hovered = false; repaint(); }
      });
      addActionListener(_ -> onClose.run());
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
        if (hovered || getModel().isPressed()) {
          g2.setColor(getModel().isPressed() ? new Color(255, 255, 255, 35) : new Color(255, 255, 255, 20));
          g2.fillRoundRect(0, 0, getWidth(), getHeight(), 4, 4);
        }
        g2.setColor(hovered ? Style.text() : Style.mutedText());
        g2.setStroke(new BasicStroke(1.25f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
        int cx = getWidth() / 2;
        int cy = getHeight() / 2;
        int r = 3;
        g2.drawLine(cx - r, cy - r, cx + r, cy + r);
        g2.drawLine(cx - r, cy + r, cx + r, cy - r);
      } finally {
        g2.dispose();
      }
    }
  }

  private JPanel createTabHeader(ScriptTab tab) {
    JPanel header = new JPanel(new BorderLayout(6, 0));
    header.setOpaque(false);
    header.setBorder(BorderFactory.createEmptyBorder(1, 2, 1, 2));

    JLabel label = new JLabel(displayName(tab.definition), Icons.getScriptIcon(tab.definition), SwingConstants.LEADING);
    label.setFont(Style.getDefaultFont().deriveFont(12f));
    label.setForeground(tab == activeTab() ? Style.text() : Style.mutedText());
    tab.title = label;

    TabCloseButton close = new TabCloseButton(() -> this.closeTab(tab));

    header.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        if (javax.swing.SwingUtilities.isMiddleMouseButton(e)) {
          closeTab(tab);
        } else if (javax.swing.SwingUtilities.isLeftMouseButton(e)) {
          tabs.setSelectedComponent(tab);
        }
      }
    });

    header.add(label, BorderLayout.CENTER);
    header.add(close, BorderLayout.EAST);
    return header;
  }

  private void closeTab(ScriptTab tab) {
    if (tab == null) return;
    if (this.monaco != null) {
      java.net.URI uri = tab.path == null ? java.net.URI.create("inmemory://script/" + tab.definition.getId()) : tab.path.toUri();
      this.monaco.closeModel(uri);
    }
    if (this.monacoTab == tab) {
      this.monacoTab = null;
    }
    this.openTabs.values().removeIf(t -> t == tab);
    this.openTabs.remove(tab.key);
    this.tabs.remove(tab);
    this.activeTabChanged();
  }

  private static JTextField createSearchTextField(String placeholder) {
    return UI.createSearchTextField(placeholder);
  }

  MonacoScriptEditor getMonaco() {
    return this.monaco;
  }

  private synchronized MonacoScriptEditor ensureMonaco() {
    if (this.monaco != null) {
      return this.monaco;
    }
    try {
      this.monaco = new MonacoScriptEditor();
      this.monaco.onChanged(text -> {
        if (this.monacoTab != null) this.monacoTab.setTextFromMonaco(text);
      });
      this.monaco.onSave(() -> {
        if (this.monacoTab != null && this.monacoTab.save()) {
          this.setStatus("Saved " + this.monacoTab.path, false);
        }
      });
      this.monaco.onAnalysis(this::showAnalysis);
      this.monaco.onDefinition(this::openProjectDefinition);
      this.monaco.onReady(this::activeTabChanged);
      this.monaco.onUnavailable(reason -> {
        this.setStatus("Monaco unavailable: " + reason, true);
      });
      this.monaco.onCursor(position -> {
        if (this.monacoTab != null) {
          this.monacoTab.caretLine = position.line() + 1;
          this.monacoTab.caretColumn = position.column() + 1;
          this.updateCaretStatus(this.monacoTab);
        }
      });
      this.monaco.onBreakpointsChanged(lines -> {
        if (this.monacoTab != null && this.monacoTab.definition != null) {
          this.replaceBreakpoints(this.monacoTab.definition, lines);
        }
      });
      this.monaco.onDebugCommand(command -> {
        if (this.debugger == null) return;
        switch (command) {
          case "resume" -> this.debugger.resume();
          case "stepOver" -> this.debugger.stepOver();
          case "stepInto" -> this.debugger.stepInto();
          case "stepOut" -> this.debugger.stepOut();
          default -> {}
        }
      });
      this.editorHost.add(this.monaco, BorderLayout.CENTER);
      this.refreshTheme();
      this.editorHost.revalidate();
      this.editorHost.repaint();
    } catch (IOException error) {
      this.monaco = null;
      this.setStatus("Monaco is unavailable: " + error.getMessage(), true);
    }
    return this.monaco;
  }

  private void activeTabChanged() {
    ScriptTab active = this.activeTab();
    for (int i = 0; i < this.tabs.getTabCount(); i++) {
      Component c = this.tabs.getComponentAt(i);
      if (c instanceof ScriptTab tabComponent && tabComponent.title != null) {
        tabComponent.title.setForeground(tabComponent == active ? Style.text() : Style.mutedText());
      }
    }
    if (active != null) {
      MonacoScriptEditor editor = this.ensureMonaco();
      if (editor != null && !editor.isUnavailable()) {
        this.monacoTab = active;
        editor.open(active.path, active.getText(), active.definition);
        if (editor.isReady()) editor.focusEditor();
        editor.notifyMoved();
        this.mainEditorArea.revalidate();
        this.mainEditorArea.repaint();
      }
    } else if (this.monaco != null) {
      this.monacoTab = null;
      if (!this.monaco.isUnavailable()) {
        this.monaco.open(null, "", null);
      }
    }
    ScriptDefinition definition = active == null ? null : active.definition;
    this.scriptContext.setText(scriptContext(definition));
    this.scriptContext.setIcon(scriptContextIcon(definition));
    this.scriptContext.setBadgeColor(scriptBadgeColor(definition));
    this.scriptContext.setVisible(definition != null);
    this.refreshActiveUsages();
    this.selectionListener.accept(definition);
    this.showDiagnostics(definition);
    this.updateCaretStatus(active);
    this.refreshGlobals();
  }

  private void refreshActiveUsages() {
    ScriptTab active = this.activeTab();
    if (this.overviewPanel != null && active != null) {
      ScriptOutline.Symbol symbol = ScriptOutline.parse(active.getText());
      List<ScriptBindingService.ScriptUsage> usages = (active.definition == null || active.definition.getId() == null) ? List.of()
          : ScriptUsagesPanel.displayableUsages(ScriptBindingService.instance().findUsages(active.definition.getId()));
      this.overviewPanel.bind(active.definition, symbol, usages);
    }
  }

  static boolean showsUsagesFor(ScriptDefinition definition) {
    return definition != null && definition.getHost() != ScriptHostType.GAME;
  }

  private void revealLineInActiveTab(int line) {
    if (line <= 0) return;
    if (this.monaco != null && this.monaco.isReady()) {
      this.monaco.revealLine(line);
      this.monaco.focusEditor();
    }
  }

  private void navigateToUsage(ScriptBindingService.ScriptUsage usage) {
    if (usage == null) return;
    switch (usage.target()) {
      case ScriptBindingTarget.Game ignored -> GameScriptsDialog.showDialog();
      case ScriptBindingTarget.Environment map -> {
        if (this.loadUsageMap(map.mapName())) UI.showMapProperties();
      }
      case ScriptBindingTarget.EntityInstance entity -> {
        if (!this.loadUsageMap(entity.mapName()) || Game.world().environment() == null) return;
        var object = Game.world().environment().getMap().getMapObject(entity.entityId());
        if (object != null) {
          Editor.instance().getMapComponent().setSelection(object, true);
          Editor.instance().getMapComponent().setFocus(object, true);
          UI.showObjectInspector();
        }
      }
      case ScriptBindingTarget.EntityType ignored -> {
        // Entity defaults are intentionally not exposed as script usages.
      }
    }
  }

  private boolean loadUsageMap(String mapName) {
    if (Editor.instance().getMapComponent() == null || mapName == null) return false;
    var map = Editor.instance().getMapComponent().getMaps().stream()
      .filter(candidate -> Objects.equals(candidate.getName(), mapName)).findFirst().orElse(null);
    if (map == null) return false;
    if (Game.world().environment() == null || Game.world().environment().getMap() != map) {
      Editor.instance().getMapComponent().loadEnvironment(map);
    }
    return true;
  }

  static String scriptContext(ScriptDefinition definition) {
    if (definition == null || definition.getHost() == null) return "";
    return switch (definition.getHost()) {
      case GAME -> "Game Script";
      case ENVIRONMENT -> "Map Script";
      case ENTITY -> "Entity Script · " + simpleName(definition.getTargetType());
    };
  }

  private static final Color BADGE_ENTITY = new Color(56, 189, 248); // Electric Cyan
  private static final Color BADGE_ENV = new Color(74, 222, 128); // Emerald Green (Map Script)
  private static final Color BADGE_GAME = new Color(251, 191, 36); // Amber Gold (Game Script)

  private static final class ScriptTypeBadge extends JLabel {
    private Color badgeColor;
    private Color bgColor;
    private Color borderColor;

    ScriptTypeBadge() {
      setOpaque(false);
      setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
      setIconTextGap(5);
      setBorder(BorderFactory.createEmptyBorder(3, 8, 3, 8));
      this.updateColors();
    }

    void setBadgeColor(Color color) {
      if (!Objects.equals(this.badgeColor, color)) {
        this.badgeColor = color;
        this.updateColors();
        repaint();
      }
    }

    private void updateColors() {
      Color accent = this.badgeColor != null ? this.badgeColor : Style.accent();
      this.bgColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 38);
      this.borderColor = new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 110);
    }

    @Override
    public Color getForeground() {
      return this.badgeColor != null ? this.badgeColor : Style.accent();
    }

    @Override
    protected void paintComponent(Graphics g) {
      Graphics2D g2 = (Graphics2D) g.create();
      try {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        int arc = Style.CORNER_RADIUS * 2;
        g2.setColor(this.bgColor);
        g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
        g2.setColor(this.borderColor);
        g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
      } finally {
        g2.dispose();
      }
      super.paintComponent(g);
    }
  }

  static Color scriptBadgeColor(ScriptDefinition definition) {
    if (definition == null || definition.getHost() == null) return Style.accent();
    return switch (definition.getHost()) {
      case ENTITY -> BADGE_ENTITY;
      case ENVIRONMENT -> BADGE_ENV;
      case GAME -> BADGE_GAME;
    };
  }

  static javax.swing.Icon scriptContextIcon(ScriptDefinition definition) {
    if (definition == null || definition.getHost() == null) return null;
    javax.swing.Icon baseIcon = switch (definition.getHost()) {
      case GAME -> Icons.PLAY_16;
      case ENVIRONMENT -> Icons.MAP_16;
      case ENTITY -> entityTypeIcon(definition.getTargetType());
    };
    return baseIcon != null ? new TintedBadgeIcon(baseIcon, null) : null;
  }

  static javax.swing.Icon entityTypeIcon(String targetType) {
    if (targetType == null || targetType.isBlank()) return Icons.ENTITY_16;
    String name = simpleName(targetType).toLowerCase(Locale.ROOT);
    return switch (name) {
      case "creature" -> Icons.CREATURE_16;
      case "prop" -> Icons.PROP_16;
      case "emitter" -> Icons.EMITTER_16;
      case "lightsource", "light" -> Icons.BULB_16;
      case "trigger" -> Icons.TRIGGER_16;
      case "spawnpoint" -> Icons.SPAWNPOINT_16;
      case "maparea", "area" -> Icons.MAPAREA_16;
      case "soundsource", "sound" -> Icons.SOUND_16;
      case "collisionbox" -> Icons.COLLISIONBOX_16;
      case "staticshadow" -> Icons.SHADOWBOX_16;
      default -> Icons.ENTITY_16;
    };
  }

  private record TintedBadgeIcon(javax.swing.Icon delegate, Color color) implements javax.swing.Icon {
    @Override public int getIconWidth() { return this.delegate.getIconWidth(); }
    @Override public int getIconHeight() { return this.delegate.getIconHeight(); }

    @Override public void paintIcon(Component component, Graphics graphics, int x, int y) {
      double scaleX = 1;
      double scaleY = 1;
      if (graphics instanceof Graphics2D graphics2D) {
        scaleX = Math.abs(graphics2D.getTransform().getScaleX());
        scaleY = Math.abs(graphics2D.getTransform().getScaleY());
      }
      int imageWidth = Math.max(1, (int) Math.ceil(getIconWidth() * scaleX));
      int imageHeight = Math.max(1, (int) Math.ceil(getIconHeight() * scaleY));
      BufferedImage image = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
      Graphics2D imageGraphics = image.createGraphics();
      try {
        imageGraphics.scale(scaleX, scaleY);
        this.delegate.paintIcon(component, imageGraphics, 0, 0);
        imageGraphics.setComposite(java.awt.AlphaComposite.SrcIn);
        imageGraphics.setColor(this.color != null ? this.color : (component != null ? component.getForeground() : Color.WHITE));
        imageGraphics.fillRect(0, 0, getIconWidth(), getIconHeight());
      } finally {
        imageGraphics.dispose();
      }
      graphics.drawImage(image, x, y, getIconWidth(), getIconHeight(), null);
    }
  }

  private static String simpleName(String name) {
    return name == null || name.isBlank() ? "Entity" : name.substring(name.lastIndexOf('.') + 1);
  }

  private ScriptTab activeTab() {
    return this.tabs.getSelectedComponent() instanceof ScriptTab tab ? tab : null;
  }

  private void checkExternalChanges() {
    for (ScriptTab tab : this.openTabs.values()) {
      if (!tab.changedOnDisk()) continue;
      if (!tab.dirty) {
        tab.loadPreservingCaret();
        this.setStatus("Reloaded externally changed " + tab.definition.getSource(), false);
      } else if (this.conflictTab != tab) {
        this.conflictTab = tab;
        this.conflictMessage.setText(displayName(tab.definition)
          + " changed both in utiLITI and on disk. Choose which version to keep.");
        this.conflictBar.setVisible(true);
        this.conflictBar.revalidate();
      }
    }
  }

  private void hideConflict() {
    this.conflictTab = null;
    this.conflictBar.setVisible(false);
  }

  private static Path projectRoot() {
    Path project = Editor.instance().getProjectPath();
    return project == null ? null : project.toAbsolutePath().normalize().getParent();
  }

  private void showDiagnostics(ScriptDefinition definition) {
    this.problemsModel.setRowCount(0);
    if (definition == null) return;
    Game.scripts().getDiagnostics().stream()
      .filter(diagnostic -> Objects.equals(diagnostic.scriptId(), definition.getId()))
      .forEach(diagnostic -> this.problemsModel.addRow(problemRow(diagnostic)));
  }

  private static Object[] problemRow(ScriptDiagnostic diagnostic) {
    String cleanFile = diagnostic.scriptId() != null && !diagnostic.scriptId().isBlank()
      ? diagnostic.scriptId() + ".java"
      : extractBasename(diagnostic.source());
    return new Object[] {
      diagnostic.severity(),
      cleanFile,
      diagnostic.line(),
      diagnostic.message(),
      diagnostic
    };
  }

  private static String extractBasename(String source) {
    if (source == null || source.isBlank()) return "Script.java";
    int lastSlash = Math.max(source.lastIndexOf('/'), source.lastIndexOf('\\'));
    return lastSlash < 0 ? source : source.substring(lastSlash + 1);
  }

  private void jumpToProblemRow(int row) {
    if (row < 0 || row >= this.problemsModel.getRowCount()) return;
    Object diagObj = this.problemsModel.getValueAt(row, 4);
    ScriptDiagnostic diag = diagObj instanceof ScriptDiagnostic d ? d : null;

    int line = 1;
    int column = 1;
    String scriptId = null;
    String sourceStr = null;

    if (diag != null) {
      line = Math.max(1, diag.line());
      column = Math.max(1, diag.column());
      scriptId = diag.scriptId();
      sourceStr = diag.source();
    } else {
      Object lineVal = this.problemsModel.getValueAt(row, 2);
      if (lineVal instanceof Integer i) line = Math.max(1, i);
      sourceStr = Objects.toString(this.problemsModel.getValueAt(row, 1), "");
    }

    ScriptDefinition target = null;
    if (Editor.instance().getGameFile() != null) {
      for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
        if ((scriptId != null && def.getId().equalsIgnoreCase(scriptId))
            || (sourceStr != null && (Objects.toString(def.getSource(), "").contains(sourceStr) || sourceStr.contains(def.getId())))) {
          target = def;
          break;
        }
      }
    }

    if (target != null) {
      this.open(target);
      if (this.monaco != null && this.monaco.isReady()) {
        this.monaco.revealPosition(line, column);
      }
    } else if (Editor.instance().getMapComponent() != null && Game.world().environment() != null) {
      de.gurkenlabs.litiengine.environment.tilemap.IMap map = Game.world().environment().getMap();
      if (map != null) {
        de.gurkenlabs.litiengine.environment.tilemap.IMapObject matchingObject = null;
        if (diag != null && diag.message() != null) {
          java.util.regex.Matcher m = java.util.regex.Pattern.compile("entity #(\\d+)").matcher(diag.message());
          if (m.find()) {
            int entityId = Integer.parseInt(m.group(1));
            matchingObject = map.getMapObject(entityId);
          }
        }
        if (matchingObject == null && scriptId != null) {
          for (de.gurkenlabs.litiengine.environment.tilemap.IMapObject obj : map.getMapObjects()) {
            String scriptBindings = obj.getStringValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.SCRIPT_BINDINGS);
            if (scriptBindings != null && scriptBindings.contains(scriptId)) {
              matchingObject = obj;
              break;
            }
          }
        }
        if (matchingObject != null) {
          Editor.instance().getMapComponent().setSelection(matchingObject, true);
          Editor.instance().getMapComponent().setFocus(matchingObject, true);
        }
      }
    }
  }

  private void showAnalysis(de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Analysis analysis) {
    if (this.monacoTab != null && this.monacoTab.definition != null) {
      String scriptId = this.monacoTab.definition.getId();
      this.projectDiagnostics.put(scriptId, new ArrayList<>(analysis.diagnostics()));
      if (analysis.diagnostics().isEmpty()) {
        Game.scripts().clearDiagnostics(scriptId);
      }
    }
    this.problemsRefreshDebounce.restart();
  }

  public void refreshProblemsTable() {
    this.problemsRefreshDebounce.stop();
    this.refreshProblemsTableNow();
  }

  private void refreshProblemsTableNow() {
    this.problemsModel.setRowCount(0);
    this.scriptErrorStates.clear();

    Map<String, List<ScriptDiagnostic>> allDiagnostics = new LinkedHashMap<>();
    for (Map.Entry<String, List<ScriptDiagnostic>> entry : this.projectDiagnostics.entrySet()) {
      allDiagnostics.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
    for (ScriptDiagnostic diag : Game.scripts().getDiagnostics()) {
      if (diag.message() != null && diag.message().contains("No script definition is registered for binding")) {
        if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
          java.util.regex.Matcher m = java.util.regex.Pattern.compile("entity #(\\d+)").matcher(diag.message());
          if (m.find()) {
            int entityId = Integer.parseInt(m.group(1));
            de.gurkenlabs.litiengine.environment.tilemap.IMapObject obj = Game.world().environment().getMap().getMapObject(entityId);
            if (obj == null) continue;
            String bindingsStr = obj.getStringValue(de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty.SCRIPT_BINDINGS);
            if (bindingsStr == null || diag.scriptId() == null || !bindingsStr.contains(diag.scriptId())) {
              continue;
            }
          }
        }
      }
      if (diag.scriptId() != null) {
        if (this.projectDiagnostics.containsKey(diag.scriptId())) {
          List<ScriptDiagnostic> currentList = this.projectDiagnostics.get(diag.scriptId());
          if (currentList != null && currentList.isEmpty() && diag.line() <= 0) {
            continue;
          }
        }
        List<ScriptDiagnostic> list = allDiagnostics.computeIfAbsent(diag.scriptId(), k -> new ArrayList<>());
        boolean exists = list.stream().anyMatch(d -> Objects.equals(d.message(), diag.message()) && d.line() == diag.line());
        if (!exists) {
          list.add(diag);
        }
      }
    }


    int totalCount = 0;
    int errorCount = 0;
    int warningCount = 0;
    for (Map.Entry<String, List<ScriptDiagnostic>> entry : allDiagnostics.entrySet()) {
      String scriptId = entry.getKey();
      List<ScriptDiagnostic> list = entry.getValue();
      if (list == null || list.isEmpty()) continue;

      boolean scriptHasError = false;
      for (ScriptDiagnostic diag : list) {
        this.problemsModel.addRow(problemRow(diag));
        totalCount++;
        if (diag.severity() == ScriptDiagnostic.Severity.ERROR) {
          scriptHasError = true;
          errorCount++;
        } else if (diag.severity() == ScriptDiagnostic.Severity.WARNING) {
          warningCount++;
        }
      }

      if (scriptHasError) {
        this.scriptErrorStates.put(scriptId, true);
      }
    }

    UI.updateProblemsStatus(warningCount, errorCount);
    this.scripts.repaint();
  }

  private void updateCaretStatus(ScriptTab tab) {
    if (tab == null) {
      this.caretStatus.setText(" ");
      return;
    }
    if (this.monacoTab == tab && this.monaco != null && this.monaco.isReady()) {
      this.caretStatus.setText("Ln " + tab.caretLine + ", Col " + tab.caretColumn + "    "
        + tab.definition.getLanguage().toUpperCase(Locale.ROOT));
      return;
    }
    this.caretStatus.setText(tab.definition.getLanguage().toUpperCase(Locale.ROOT));
  }

  private void setStatus(String message, boolean error) {
    this.status.setText(message);
    this.status.setForeground(error ? new Color(210, 80, 80) : new Color(100, 170, 110));
  }

  private static final Pattern JUL_HEADER_PATTERN = Pattern.compile(
      "^(?:Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)[a-z.]* \\d{1,2},? \\d{4} \\d{1,2}:\\d{2}:\\d{2} (?:AM|PM) .*",
      Pattern.CASE_INSENSITIVE
  );

  private String pendingLogHeader = null;

  private void appendOutput(String message) {
    if (message == null || message.isBlank()) return;

    String trimmed = message.trim();

    if (JUL_HEADER_PATTERN.matcher(trimmed).matches()) {
      if (this.pendingLogHeader != null) {
        log.info(this.pendingLogHeader);
      }
      this.pendingLogHeader = trimmed;
      return;
    }

    LevelAndMessage parsed = parseLevelAndMessage(trimmed);
    this.pendingLogHeader = null;
    log.log(parsed.level(), parsed.message());
  }

  private static LevelAndMessage parseLevelAndMessage(String raw) {
    if (raw == null || raw.isBlank()) return new LevelAndMessage(Level.INFO, raw);

    String trimmed = raw.trim();
    String upper = trimmed.toUpperCase(Locale.ROOT);

    Level level = Level.INFO;

    if (upper.contains("SCHWERWIEGEND:") || upper.contains("SEVERE:") || upper.startsWith("SEVERE") || upper.startsWith("ERROR") || upper.startsWith("FATAL")) {
      level = Level.SEVERE;
    } else if (upper.contains("WARNUNG:") || upper.contains("WARNING:") || upper.startsWith("WARNING") || upper.startsWith("WARN")) {
      level = Level.WARNING;
    } else if (upper.startsWith("AT ") || upper.contains("EXCEPTION") || upper.contains("ERROR:") || upper.contains("FAILED")) {
      level = Level.SEVERE;
    } else if (upper.contains("COULD NOT BE LOADED")) {
      level = Level.WARNING;
    }

    String cleaned = trimmed.replaceFirst("^(?i)(?:SEVERE|SCHWERWIEGEND|WARNING|WARNUNG|INFORMATION|INFO|CONFIG|FINE|FINER|FINEST)\\b\\s*:?\\s*", "");
    if (cleaned.isBlank()) cleaned = trimmed;

    return new LevelAndMessage(level, cleaned);
  }

  private record LevelAndMessage(Level level, String message) {}

  private void insertPackageNode(String packageName) {
    if (packageName == null || packageName.isBlank()) return;
    DefaultMutableTreeNode parent = this.scriptsRoot;
    String[] parts = packageName.split("\\.");
    String currentPkg = "";
    for (String part : parts) {
      if (part.isBlank()) continue;
      currentPkg = currentPkg.isEmpty() ? part : currentPkg + "." + part;
      parent = childFolder(parent, part, currentPkg);
    }
  }

  private void insertScriptNode(ScriptDefinition definition) {
    this.scriptsRoot.add(new DefaultMutableTreeNode(new ScriptTreeItem(displayName(definition), definition, null)));
  }

  private void insertProjectSourceNode(ScriptDefinition definition) {
    DefaultMutableTreeNode parent = this.scriptsRoot;
    String implementation = Objects.toString(definition.getImplementation(), definition.getId());
    String[] parts = implementation.split("\\.");
    String currentPkg = "";
    for (int i = 0; i < Math.max(0, parts.length - 1); i++) {
      if (!parts[i].isBlank()) {
        currentPkg = currentPkg.isEmpty() ? parts[i] : currentPkg + "." + parts[i];
        parent = childFolder(parent, parts[i], currentPkg);
      }
    }
    parent.add(new DefaultMutableTreeNode(new ScriptTreeItem(displayName(definition), definition, null)));
  }

  private static void compactEmptyFolders(DefaultMutableTreeNode node) {
    if (node == null) return;
    for (int i = 0; i < node.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
      compactEmptyFolders(child);
    }
    if (node.getUserObject() instanceof ScriptTreeItem item && item.isPackage()) {
      while (node.getChildCount() == 1) {
        DefaultMutableTreeNode onlyChild = (DefaultMutableTreeNode) node.getChildAt(0);
        if (onlyChild.getUserObject() instanceof ScriptTreeItem childItem && childItem.isPackage()) {
          String merged = item.label() + "." + childItem.label();
          item = new ScriptTreeItem(merged, null, childItem.packageName());
          node.setUserObject(item);
          node.removeAllChildren();
          List<DefaultMutableTreeNode> grandChildren = new ArrayList<>();
          for (int k = 0; k < onlyChild.getChildCount(); k++) {
            grandChildren.add((DefaultMutableTreeNode) onlyChild.getChildAt(k));
          }
          for (DefaultMutableTreeNode gc : grandChildren) {
            node.add(gc);
          }
        } else {
          break;
        }
      }
    }
  }

  private static DefaultMutableTreeNode childFolder(DefaultMutableTreeNode parent, String name, String fullPackage) {
    Enumeration<?> children = parent.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
      if (child.getUserObject() instanceof ScriptTreeItem item && item.isPackage() && item.label().equals(name)) return child;
    }
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ScriptTreeItem(name, null, fullPackage));
    parent.add(child);
    return child;
  }

  public String selectedPackage() {
    Object selected = this.scripts.getLastSelectedPathComponent();
    if (selected instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptTreeItem item) {
      if (item.isPackage()) return item.packageName();
      if (item.definition() != null && item.definition().getImplementation() != null) {
        int dot = item.definition().getImplementation().lastIndexOf('.');
        return dot > 0 ? item.definition().getImplementation().substring(0, dot) : null;
      }
    }
    return null;
  }

  public void selectPackageNode(String packageName) {
    if (packageName == null || packageName.isBlank()) return;
    Enumeration<?> nodes = this.scriptsRoot.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
      if (node.getUserObject() instanceof ScriptTreeItem item && item.isPackage()
          && (Objects.equals(packageName, item.packageName()) || Objects.equals(packageName, item.label()))) {
        TreePath path = new TreePath(node.getPath());
        this.scripts.setSelectionPath(path);
        this.scripts.scrollPathToVisible(path);
        return;
      }
    }
  }

  private ScriptDefinition selectedDefinition() {
    Object selected = this.scripts.getLastSelectedPathComponent();
    if (!(selected instanceof DefaultMutableTreeNode node) || !(node.getUserObject() instanceof ScriptTreeItem item)) return null;
    return item.definition();
  }

  public ScriptDefinition activeOrSelectedDefinition() {
    ScriptDefinition selected = selectedDefinition();
    if (selected != null) return selected;
    ScriptTab tab = activeTab();
    return tab != null ? tab.definition : null;
  }

  public void duplicateActiveOrSelected() {
    ScriptDefinition def = activeOrSelectedDefinition();
    if (def != null) duplicateScript(def);
  }

  public void deleteActiveOrSelected() {
    ScriptDefinition def = activeOrSelectedDefinition();
    if (def != null) deleteScript(def);
  }

  private void selectTreeNode(String id) {
    Enumeration<?> nodes = this.scriptsRoot.depthFirstEnumeration();
    while (nodes.hasMoreElements()) {
      DefaultMutableTreeNode node = (DefaultMutableTreeNode) nodes.nextElement();
      if (node.getUserObject() instanceof ScriptTreeItem item && item.definition() != null
        && Objects.equals(id, item.definition().getId())) {
        TreePath path = new TreePath(node.getPath());
        this.scripts.setSelectionPath(path);
        this.scripts.scrollPathToVisible(path);
        return;
      }
    }
  }

  public enum ScriptKind {
    ENTITY,
    GAME,
    ENVIRONMENT
  }

  public ScriptDefinition createScript() {
    return createScript(ScriptKind.ENTITY);
  }

  public ScriptDefinition createScript(ScriptKind kind) {
    return createScript(kind, kind == ScriptKind.ENTITY ? Creature.class : null, selectedPackage());
  }

  public ScriptDefinition createScript(ScriptKind kind, Class<?> targetClass) {
    return createScript(kind, targetClass, selectedPackage());
  }

  public ScriptDefinition createScript(ScriptKind kind, Class<?> targetClass, String targetPackage) {
    if (Editor.instance().getGameFile() == null || Editor.instance().getProjectPath() == null) return null;
    String targetType = targetClass != null ? targetClass.getName() : (kind == ScriptKind.ENTITY ? Creature.class.getName() : null);
    ScriptHostType hostType = switch (kind) {
      case GAME -> ScriptHostType.GAME;
      case ENVIRONMENT -> ScriptHostType.ENVIRONMENT;
      case ENTITY -> ScriptHostType.ENTITY;
    };

    String className = this.promptForNewScriptName(this.nextAvailableScriptName());
    if (className == null) return null;
    String id = className;
    String relPath = ScriptSourcePaths.create(Editor.instance().getProjectModel(), "java", targetPackage, className);
    Path source = resolveSource(relPath);
    if (source == null) return null;

    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), relPath);
    if (packageName == null || packageName.isBlank()) {
      packageName = targetPackage;
    }
    String implementation = (packageName != null && !packageName.isBlank())
        ? packageName + "." + className
        : className;

    ScriptDefinition definition = new ScriptDefinition(className, "java", relPath,
      implementation, hostType);
    definition.setName(className);
    if (targetType != null) {
      definition.setTargetType(targetType);
    }
    try {
      Files.createDirectories(source.getParent());
      Files.writeString(source, defaultSource(definition, className, kind));
      Editor.instance().getGameFile().getScripts().add(definition);
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      recordUndoChanges();
      UI.getAssetController().refresh();
      this.refreshScripts();
      this.open(definition);
      this.setStatus("Created " + definition.getSource(), false);
      return definition;
    } catch (IOException e) {
      this.setStatus("Could not create script: " + e.getMessage(), true);
      return null;
    }
  }

  private String nextAvailableScriptName() {
    int suffix = 1;
    while (true) {
      String candidate = suffix == 1 ? DEFAULT_SCRIPT_NAME : DEFAULT_SCRIPT_NAME + suffix;
      if (!this.scriptNameUnavailable(candidate)) return candidate;
      suffix++;
    }
  }

  private String promptForNewScriptName(String suggestion) {
    String candidate = suggestion;
    while (true) {
      JTextField nameField = new JTextField(candidate, 28);
      nameField.getAccessibleContext().setAccessibleName("Script name");
      JPanel prompt = new JPanel(new BorderLayout(0, 6));
      prompt.add(new JLabel("Name"), BorderLayout.NORTH);
      prompt.add(nameField, BorderLayout.CENTER);
      SwingUtilities.invokeLater(() -> {
        nameField.requestFocusInWindow();
        nameField.selectAll();
      });
      int result = JOptionPane.showConfirmDialog(
          this, prompt, "New Script", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, Icons.SCRIPT_16);
      if (result != JOptionPane.OK_OPTION) return null;

      candidate = nameField.getText() == null ? "" : nameField.getText().strip();
      String error = scriptNameValidationError(candidate, false);
      if (error == null) error = scriptNameValidationError(candidate, this.scriptNameUnavailable(candidate));
      if (error == null) return candidate;
      JOptionPane.showMessageDialog(this, error, "Invalid Script Name", JOptionPane.ERROR_MESSAGE);
    }
  }

  private boolean scriptNameUnavailable(String name) {
    Path source = resolveSource(ScriptSourcePaths.create(Editor.instance().getProjectModel(), "java", name));
    return source == null || Files.exists(source) || scriptIdExists(name);
  }

  static String scriptNameValidationError(String name, boolean unavailable) {
    if (name == null || name.isBlank()) return "Enter a script name.";
    if (!SourceVersion.isIdentifier(name) || SourceVersion.isKeyword(name)) {
      return "The script name must be a valid Java class name.";
    }
    if (unavailable) return "A script or source file with this name already exists.";
    return null;
  }

  public static String extractClassName(String source) {
    return ScriptSourcePaths.extractClassName(source);
  }

  public static String extractFullyQualifiedClassName(String source) {
    return ScriptSourcePaths.extractFullyQualifiedClassName(source);
  }

  private void repairProjectScriptDefinitions() {

    if (Editor.instance().getGameFile() == null) return;
    boolean changed = false;
    for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
      if (def == null) continue;
      Path path = resolveSource(def.getSource());
      if (path != null && Files.isRegularFile(path)) {
        try {
          String content = Files.readString(path);
          String fqcn = extractFullyQualifiedClassName(content);
          if (fqcn != null && !fqcn.isBlank() && !fqcn.equals(def.getImplementation())) {
            def.setImplementation(fqcn);
            changed = true;
          }
        } catch (Exception ignored) {
        }
      }
    }
    if (changed) {
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    }
  }

  private boolean scriptIdExists(String id) {
    return Editor.instance().getGameFile().getScripts().stream().anyMatch(candidate -> candidate.getId().equals(id));
  }

  public void deleteScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;

    Path file = this.projectSourcePaths.get(definition);
    if (file == null) file = resolveSource(definition.getSource());

    ScriptBindingService.ScriptMutationPlan plan = ScriptBindingService.instance().planDelete(definition.getId());
    int usageCount = plan.valid() ? plan.usages().usages().size() : 0;
    String assignmentWarning = usageCount == 0 ? "" : " and remove " + usageCount
      + (usageCount == 1 ? " assignment" : " assignments");
    int choice = JOptionPane.showConfirmDialog(this,
      "Are you sure you want to delete script '" + displayName(definition) + "'?\n"
        + "This will remove its source file" + assignmentWarning + ".",
      "Delete Script", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) return;

    Path fileToDelete = file;
    List<ScriptTab> matchingTabs = this.openTabs.values().stream()
      .filter(t -> t.definition == definition
          || (t.definition != null && Objects.equals(t.definition.getId(), definition.getId()))
          || (t.definition != null && definition.getImplementation() != null && Objects.equals(t.definition.getImplementation(), definition.getImplementation()))
          || (fileToDelete != null && fileToDelete.equals(t.path)))
      .toList();
    matchingTabs.forEach(this::closeTab);

    SourceFileMutation sourceMutation = null;
    try {
      if (file != null && Files.exists(file)) sourceMutation = SourceFileMutation.delete(file);
    } catch (IOException error) {
      this.setStatus("Could not prepare source deletion: " + error.getMessage(), true);
      return;
    }

    if (plan.valid()) {
      ScriptBindingService.MutationResult result = ScriptBindingService.instance()
        .execute(plan, ignored -> {}, sourceMutation);
      if (!result.success()) {
        this.setStatus(result.message(), true);
        return;
      }
    } else {
      if (sourceMutation != null) {
        try {
          sourceMutation.apply();
        } catch (RuntimeException error) {
          this.setStatus("Could not delete source file: " + error.getMessage(), true);
          return;
        }
      }
      Editor.instance().getGameFile().getScripts().removeIf(d ->
        d == definition
        || Objects.equals(d.getId(), definition.getId())
        || (definition.getImplementation() != null && Objects.equals(d.getImplementation(), definition.getImplementation()))
      );
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    }

    if (file != null) {
      try {
        Files.deleteIfExists(file);
      } catch (IOException ignored) {}
    }

    this.projectSourcePaths.remove(definition);
    if (definition.getImplementation() != null) {
      this.projectSourceDefinitions.remove(definition.getImplementation());
      this.navigatedProjectDefinitions.remove(definition.getImplementation());
      this.navigatedProjectSources.remove(definition.getImplementation());
    }
    this.projectSourceDefinitions.values().removeIf(d -> d == definition || Objects.equals(d.getId(), definition.getId()));

    if (Editor.instance().getProjectCodeIntegration() != null) {
      Editor.instance().getProjectCodeIntegration().reloadProject(Editor.instance().getProjectModel());
    }

    UI.getAssetController().refresh();
    refreshScripts();
    Editor.instance().save(false);
    setStatus("Deleted script " + displayName(definition), false);
  }

  public void closeTab(ScriptDefinition definition) {
    if (definition == null) return;
    List<ScriptTab> matchingTabs = this.openTabs.values().stream()
      .filter(t -> t.definition == definition
          || (t.definition != null && Objects.equals(t.definition.getId(), definition.getId()))
          || (t.definition != null && definition.getImplementation() != null && Objects.equals(t.definition.getImplementation(), definition.getImplementation())))
      .toList();
    matchingTabs.forEach(this::closeTab);
  }

  public void reloadTab(ScriptDefinition definition) {
    if (definition == null) return;
    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    if (tab != null) tab.loadPreservingCaret();
  }

  public void duplicateScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;
    Path originalFile = this.projectSourcePaths.get(definition);
    if (originalFile == null) originalFile = resolveSource(definition.getSource());

    int suffix = 1;
    String id;
    String className;
    Path source;
    String baseName = displayName(definition);
    do {
      className = baseName + "Copy" + (suffix == 1 ? "" : suffix);
      id = definition.getId() + "-copy" + (suffix == 1 ? "" : "-" + suffix);
      if (originalFile != null && originalFile.getParent() != null) {
        String newFileName = className + "." + languageFor(originalFile);
        source = originalFile.getParent().resolve(newFileName);
      } else {
        source = resolveSource(ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), className));
      }
      suffix++;
    } while (source != null && (Files.exists(source) || scriptIdExists(id)));

    String newSourceRel;
    if (originalFile != null && Editor.instance().getProjectPath() != null && source != null) {
      Path projectParent = Editor.instance().getProjectPath().getParent().toAbsolutePath().normalize();
      newSourceRel = projectParent.relativize(source.toAbsolutePath().normalize()).toString().replace('\\', '/');
    } else {
      newSourceRel = ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), className);
    }

    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), newSourceRel);
    String implementation = (packageName != null && !packageName.isBlank())
        ? packageName + "." + className
        : className;

    ScriptDefinition dup = new ScriptDefinition(id, definition.getLanguage(),
      newSourceRel, implementation, definition.getHost());
    dup.setTargetType(definition.getTargetType());

    String content = "";
    if (originalFile != null && Files.exists(originalFile)) {
      try {
        content = Files.readString(originalFile);
        String oldClassName = Objects.requireNonNullElse(extractClassName(content), displayName(definition));
        content = content
            .replaceAll("\\b" + Pattern.quote(oldClassName) + "\\b", java.util.regex.Matcher.quoteReplacement(className))
            .replace("id = \"" + definition.getId() + "\"", "id = \"" + id + "\"");
      } catch (IOException ignored) {}
    } else {
      content = defaultSource(dup, className, ScriptKind.ENTITY);
    }

    try {
      if (source != null && source.getParent() != null) {
        Files.createDirectories(source.getParent());
        Files.writeString(source, content);
      }
      Editor.instance().getGameFile().getScripts().add(dup);
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      recordUndoChanges();
      UI.getAssetController().refresh();
      refreshScripts();
      open(dup);
      setStatus("Duplicated script to " + dup.getSource(), false);
    } catch (IOException e) {
      setStatus("Could not duplicate script: " + e.getMessage(), true);
    }
  }

  private JPopupMenu createAddScriptPopupMenu() {
    JPopupMenu menu = new JPopupMenu();
    String targetPkg = selectedPackage();
    JMenuItem entityScript = new JMenuItem("Entity Script...", Icons.SCRIPT_ENTITY_16);
    entityScript.addActionListener(e -> createScript(ScriptKind.ENTITY, null, targetPkg));

    JMenuItem gameScript = new JMenuItem("Game Script...", Icons.SCRIPT_GAME_16);
    gameScript.addActionListener(e -> createScript(ScriptKind.GAME, null, targetPkg));

    JMenuItem envScript = new JMenuItem("Environment Script...", Icons.SCRIPT_ENVIRONMENT_16);
    envScript.addActionListener(e -> createScript(ScriptKind.ENVIRONMENT, null, targetPkg));

    menu.add(entityScript);
    menu.add(gameScript);
    menu.add(envScript);
    return menu;
  }

  private void showTreeContextMenu(java.awt.event.MouseEvent e) {
    if (!e.isPopupTrigger()) return;
    int row = this.scripts.getClosestRowForLocation(e.getX(), e.getY());
    if (row >= 0) this.scripts.setSelectionRow(row);
    Object selectedComponent = this.scripts.getLastSelectedPathComponent();
    ScriptTreeItem item = (selectedComponent instanceof DefaultMutableTreeNode node
        && node.getUserObject() instanceof ScriptTreeItem treeItem) ? treeItem : null;

    JPopupMenu menu = new JPopupMenu();
    String targetPkg = item != null && item.isPackage() ? item.packageName() : selectedPackage();

    JMenu newSub = new JMenu("New Script");
    newSub.setIcon(Icons.ADD_16);
    JMenuItem entityScript = new JMenuItem("Entity Script...");
    entityScript.addActionListener(evt -> createScript(ScriptKind.ENTITY, null, targetPkg));
    JMenuItem gameScript = new JMenuItem("Game Script...");
    gameScript.addActionListener(evt -> createScript(ScriptKind.GAME, null, targetPkg));
    JMenuItem envScript = new JMenuItem("Environment Script...");
    envScript.addActionListener(evt -> createScript(ScriptKind.ENVIRONMENT, null, targetPkg));
    newSub.add(entityScript);
    newSub.add(gameScript);
    newSub.add(envScript);
    menu.add(newSub);

    JMenuItem newPkgItem = new JMenuItem(item != null && item.isPackage() ? "New Subpackage..." : "New Package...", Icons.PACKAGE_16);
    newPkgItem.addActionListener(evt -> createPackage(targetPkg));
    menu.add(newPkgItem);

    if (item != null) {
      if (item.isPackage()) {
        menu.addSeparator();
        JMenuItem renamePkg = new JMenuItem("Rename Package...", Icons.RENAME_16);
        renamePkg.addActionListener(evt -> renamePackage(item.packageName()));
        menu.add(renamePkg);

        JMenuItem deletePkg = new JMenuItem("Delete Package", Icons.DELETE_16);
        deletePkg.addActionListener(evt -> deletePackage(item.packageName()));
        menu.add(deletePkg);
      } else if (item.definition() != null) {
        ScriptDefinition selected = item.definition();
        menu.addSeparator();
        JMenuItem dupItem = new JMenuItem("Duplicate Script", Icons.COPY_16);
        dupItem.addActionListener(evt -> duplicateScript(selected));
        menu.add(dupItem);

        JMenuItem renameItem = new JMenuItem("Rename Class...", Icons.RENAME_16);
        renameItem.addActionListener(evt -> renameScript(selected));
        menu.add(renameItem);

        JMenuItem movePkgItem = new JMenuItem("Move to Package...", Icons.PACKAGE_16);
        movePkgItem.addActionListener(evt -> moveScriptToPackage(selected));
        menu.add(movePkgItem);

        JMenuItem deleteItem = new JMenuItem("Delete Script", Icons.DELETE_16);
        deleteItem.addActionListener(evt -> deleteScript(selected));
        menu.add(deleteItem);

        if (showsUsagesFor(selected)) {
          JMenuItem usagesItem = new JMenuItem("Find Usages", Icons.SEARCH_16);
          usagesItem.addActionListener(evt -> open(selected));
          menu.add(usagesItem);
        }
      }
    }
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void createPackage(String parentPackage) {
    if (Editor.instance().getGameFile() == null || Editor.instance().getProjectPath() == null) return;
    String suggestion = parentPackage != null && !parentPackage.isBlank() ? parentPackage + "." : "";
    String input = (String) JOptionPane.showInputDialog(
        this,
        "Enter package name (e.g. 'com.example.game.combat' or 'combat'):",
        "New Package",
        JOptionPane.QUESTION_MESSAGE,
        Icons.PACKAGE_16,
        null,
        suggestion);
    if (input == null || input.isBlank()) return;
    String pkgName = input.trim();
    if (!pkgName.contains(".") && parentPackage != null && !parentPackage.isBlank()) {
      pkgName = parentPackage + "." + pkgName;
    }
    if (!ScriptSourcePaths.isValidPackage(pkgName)) {
      JOptionPane.showMessageDialog(this, "Invalid package name: " + pkgName, "Create Package Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    Path dir = ScriptSourcePaths.resolvePackageDirectory(Editor.instance().getProjectModel(), pkgName);
    if (dir == null) {
      this.setStatus("Could not resolve package directory for " + pkgName, true);
      return;
    }
    try {
      Files.createDirectories(dir);
      this.customCreatedPackages.add(pkgName);
      this.refreshScripts();
      this.selectPackageNode(pkgName);
      this.setStatus("Created package " + pkgName, false);
    } catch (IOException e) {
      this.setStatus("Could not create package directory: " + e.getMessage(), true);
    }
  }

  public void renamePackage(String oldPackage) {
    if (oldPackage == null || oldPackage.isBlank() || Editor.instance().getProjectPath() == null) return;
    String input = (String) JOptionPane.showInputDialog(
        this,
        "Enter new package name:",
        "Rename Package",
        JOptionPane.QUESTION_MESSAGE,
        Icons.RENAME_16,
        null,
        oldPackage);
    if (input == null || input.isBlank() || input.trim().equals(oldPackage)) return;
    String newPackage = input.trim();
    if (!ScriptSourcePaths.isValidPackage(newPackage)) {
      JOptionPane.showMessageDialog(this, "Invalid package name: " + newPackage, "Rename Package Error", JOptionPane.ERROR_MESSAGE);
      return;
    }
    executeRenamePackage(oldPackage, newPackage);
  }

  public void executeRenamePackage(String oldPackage, String newPackage) {
    if (oldPackage == null || newPackage == null || oldPackage.equals(newPackage)) return;
    if (!ScriptSourcePaths.isValidPackage(newPackage)) {
      setStatus("Invalid package name: " + newPackage, true);
      return;
    }

    Path sourceRoot = ScriptSourcePaths.selectSourceRoot(Editor.instance().getProjectModel());
    Path oldDir = ScriptSourcePaths.resolvePackageDirectory(Editor.instance().getProjectModel(), oldPackage);
    Path newDir = ScriptSourcePaths.resolvePackageDirectory(Editor.instance().getProjectModel(), newPackage);

    if (oldDir == null || !Files.isDirectory(oldDir)) {
      setStatus("Source package directory does not exist: " + oldPackage, true);
      return;
    }

    // Step 1: Preflight analysis & collision checking
    List<Path> sourceJavaFiles;
    try (Stream<Path> stream = Files.walk(oldDir)) {
      sourceJavaFiles = stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java")).toList();
    } catch (IOException e) {
      setStatus("Could not scan package files: " + e.getMessage(), true);
      return;
    }

    Map<Path, Path> moveMap = new LinkedHashMap<>();
    Map<Path, String> newContents = new LinkedHashMap<>();

    for (Path file : sourceJavaFiles) {
      Path relPath = oldDir.relativize(file);
      Path targetFile = newDir.resolve(relPath);
      moveMap.put(file, targetFile);

      if (!file.equals(targetFile) && Files.exists(targetFile) && !sourceJavaFiles.contains(targetFile)) {
        setStatus("Cannot rename package: Destination file '" + targetFile.getFileName() + "' already exists.", true);
        return;
      }

      try {
        String fileContent = Files.readString(file);
        String targetPkg;
        if (relPath.getParent() != null) {
          String sub = relPath.getParent().toString().replace('\\', '.').replace('/', '.');
          targetPkg = newPackage + "." + sub;
        } else {
          targetPkg = newPackage;
        }
        String updated = fileContent.replaceFirst("(?m)^\\s*package\\s+[a-zA-Z0-9_.]+\\s*;", "package " + targetPkg + ";");
        newContents.put(file, updated);
      } catch (IOException e) {
        setStatus("Could not read file during preflight: " + file, true);
        return;
      }
    }

    // Analyze import rewrites in all source files under sourceRoot
    Map<Path, String> importRewrites = new LinkedHashMap<>();
    if (sourceRoot != null && Files.isDirectory(sourceRoot)) {
      try (Stream<Path> stream = Files.walk(sourceRoot)) {
        var allJava = stream.filter(p -> Files.isRegularFile(p) && p.getFileName().toString().endsWith(".java")).toList();
        for (Path file : allJava) {
          if (sourceJavaFiles.contains(file)) continue;
          try {
            String text = Files.readString(file);
            String updated = text
                .replaceAll("(?m)^\\s*import\\s+" + Pattern.quote(oldPackage) + "\\.", "import " + newPackage + ".")
                .replaceAll("(?m)^\\s*import\\s+static\\s+" + Pattern.quote(oldPackage) + "\\.", "import static " + newPackage + ".");
            if (!updated.equals(text)) {
              importRewrites.put(file, updated);
            }
          } catch (IOException e) {
            setStatus("Could not analyze file for imports: " + file.getFileName(), true);
            return;
          }
        }
      } catch (IOException e) {
        setStatus("Could not scan source files: " + e.getMessage(), true);
        return;
      }
    }

    // Step 2: In-memory snapshot for transactional rollback
    Map<Path, byte[]> originalSnapshots = new LinkedHashMap<>();
    Set<Path> createdTargetFiles = new LinkedHashSet<>();
    try {
      for (Path file : sourceJavaFiles) {
        originalSnapshots.put(file, Files.readAllBytes(file));
      }
      for (Path file : importRewrites.keySet()) {
        originalSnapshots.put(file, Files.readAllBytes(file));
      }
    } catch (IOException e) {
      setStatus("Could not create rollback snapshot: " + e.getMessage(), true);
      return;
    }

    // Step 3: Apply mutation transactionally
    try {
      Files.createDirectories(newDir);
      for (Map.Entry<Path, Path> entry : moveMap.entrySet()) {
        Path src = entry.getKey();
        Path dst = entry.getValue();
        String updatedText = newContents.get(src);
        Files.createDirectories(dst.getParent());
        Files.writeString(dst, updatedText);
        createdTargetFiles.add(dst);
      }

      for (Path src : sourceJavaFiles) {
        if (!moveMap.containsValue(src)) {
          Files.deleteIfExists(src);
        }
      }
      deleteEmptyDirectoriesUpTo(oldDir, sourceRoot);

      for (Map.Entry<Path, String> entry : importRewrites.entrySet()) {
        Files.writeString(entry.getKey(), entry.getValue());
      }
    } catch (Exception error) {
      // Rollback all file changes
      for (Path created : createdTargetFiles) {
        try {
          Files.deleteIfExists(created);
        } catch (IOException ignored) {}
      }
      for (Map.Entry<Path, byte[]> snapshot : originalSnapshots.entrySet()) {
        try {
          Files.createDirectories(snapshot.getKey().getParent());
          Files.write(snapshot.getKey(), snapshot.getValue());
        } catch (IOException ignored) {}
      }
      setStatus("Could not rename package on disk, changes rolled back: " + error.getMessage(), true);
      return;
    }

    // Step 4: Update metadata
    if (Editor.instance().getGameFile() != null) {
      String oldSlash = oldPackage.replace('.', '/');
      String newSlash = newPackage.replace('.', '/');
      for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
        if (def == null) continue;
        String impl = def.getImplementation();
        if (impl != null) {
          if (impl.equals(oldPackage)) {
            def.setImplementation(newPackage);
          } else if (impl.startsWith(oldPackage + ".")) {
            def.setImplementation(newPackage + impl.substring(oldPackage.length()));
          }
        }
        String src = def.getSource();
        if (src != null && src.contains(oldSlash)) {
          def.setSource(src.replace(oldSlash, newSlash));
        }
      }
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    }

    Set<String> updatedCustom = new java.util.LinkedHashSet<>();
    for (String pkg : this.customCreatedPackages) {
      if (pkg.equals(oldPackage)) {
        updatedCustom.add(newPackage);
      } else if (pkg.startsWith(oldPackage + ".")) {
        updatedCustom.add(newPackage + pkg.substring(oldPackage.length()));
      } else {
        updatedCustom.add(pkg);
      }
    }
    this.customCreatedPackages.clear();
    this.customCreatedPackages.addAll(updatedCustom);

    if (oldDir != null && newDir != null) {
      for (ScriptTab tab : List.copyOf(this.openTabs.values())) {
        if (tab.path != null && tab.path.startsWith(oldDir)) {
          Path rel = oldDir.relativize(tab.path);
          Path next = newDir.resolve(rel);
          this.openTabs.remove(tab.key);
          tab.path = next;
          tab.key = this.documentKey(tab.definition);
          this.openTabs.put(tab.key, tab);
          tab.loadPreservingCaret();
        }
      }
    }

    if (Editor.instance().getProjectCodeIntegration() != null) {
      Editor.instance().getProjectCodeIntegration().reloadProject(Editor.instance().getProjectModel());
    }
    recordUndoChanges();
    Editor.instance().save(false);
    refreshScripts();
    selectPackageNode(newPackage);
    setStatus("Renamed package " + oldPackage + " to " + newPackage, false);
  }

  private static void deleteEmptyDirectoriesUpTo(Path start, Path boundary) {
    if (start == null) return;
    Path curr = start;
    while (curr != null && !curr.equals(boundary)) {
      try {
        if (Files.isDirectory(curr)) {
          try (var s = Files.list(curr)) {
            if (s.findAny().isPresent()) break;
          }
          Files.deleteIfExists(curr);
        }
      } catch (IOException ignored) {
        break;
      }
      curr = curr.getParent();
    }
  }

  public void deletePackage(String packageName) {
    this.deletePackage(packageName, false);
  }

  public boolean deletePackage(String packageName, boolean skipConfirmation) {
    if (packageName == null || packageName.isBlank() || Editor.instance().getProjectPath() == null) return false;
    Path dir = ScriptSourcePaths.resolvePackageDirectory(Editor.instance().getProjectModel(), packageName);
    if (dir == null || !Files.isDirectory(dir)) return false;

    List<ScriptDefinition> matching = new ArrayList<>();
    Set<Path> scriptPaths = new HashSet<>();
    if (Editor.instance().getGameFile() != null) {
      for (ScriptDefinition def : Editor.instance().getGameFile().getScripts()) {
        if (def == null) continue;
        String impl = def.getImplementation();
        if (impl != null && (impl.equals(packageName) || impl.startsWith(packageName + "."))) {
          matching.add(def);
          Path sp = this.projectSourcePaths.get(def);
          if (sp == null) sp = resolveSource(def.getSource());
          if (sp != null) scriptPaths.add(sp.toAbsolutePath().normalize());
        }
      }
    }

    List<Path> nonScriptFiles = new ArrayList<>();
    try (Stream<Path> stream = Files.walk(dir)) {
      nonScriptFiles = stream.filter(Files::isRegularFile)
          .map(p -> p.toAbsolutePath().normalize())
          .filter(p -> !scriptPaths.contains(p))
          .toList();
    } catch (IOException e) {
      setStatus("Could not inspect package directory: " + e.getMessage(), true);
      return false;
    }

    if (!nonScriptFiles.isEmpty()) {
      String sample = nonScriptFiles.stream()
          .limit(3)
          .map(p -> p.getFileName().toString())
          .collect(java.util.stream.Collectors.joining(", "));
      if (nonScriptFiles.size() > 3) sample += ", ...";
      if (!skipConfirmation && !GraphicsEnvironment.isHeadless()) {
        JOptionPane.showMessageDialog(
            this,
            "Cannot delete package '" + packageName + "': The directory contains " + nonScriptFiles.size()
                + " non-script file(s) (" + sample + ").\n"
                + "To prevent accidental deletion of unrelated project code, only empty packages or packages containing exclusively scripts can be deleted from the Script Explorer.",
            "Delete Package Denied",
            JOptionPane.ERROR_MESSAGE);
      }
      setStatus("Cannot delete package '" + packageName + "': contains non-script files (" + sample + ").", true);
      return false;
    }

    if (!skipConfirmation && !GraphicsEnvironment.isHeadless()) {
      int scriptCount = matching.size();
      int choice = JOptionPane.showConfirmDialog(
          this,
          "Are you sure you want to delete package '" + packageName + "'"
              + (scriptCount > 0 ? " and all " + scriptCount + " script(s) inside it" : "") + "?\n"
              + "This will delete the directory from disk.",
          "Delete Package",
          JOptionPane.YES_NO_OPTION,
          JOptionPane.WARNING_MESSAGE);
      if (choice != JOptionPane.YES_OPTION) return false;
    }

    try {
      try (Stream<Path> stream = Files.walk(dir)) {
        List<Path> pathsToDelete = stream.sorted(Comparator.reverseOrder()).toList();
        for (Path p : pathsToDelete) {
          Files.delete(p);
        }
      }
      deleteEmptyDirectoriesUpTo(dir.getParent(), ScriptSourcePaths.selectSourceRoot(Editor.instance().getProjectModel()));
    } catch (IOException e) {
      setStatus("Could not delete package directory: " + e.getMessage(), true);
      return false;
    }

    matching.forEach(this::closeTab);

    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getScripts().removeAll(matching);
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    }

    this.customCreatedPackages.removeIf(p -> p.equals(packageName) || p.startsWith(packageName + "."));
    if (Editor.instance().getProjectCodeIntegration() != null) {
      Editor.instance().getProjectCodeIntegration().reloadProject(Editor.instance().getProjectModel());
    }
    recordUndoChanges();
    Editor.instance().save(false);
    refreshScripts();
    setStatus("Deleted package " + packageName, false);
    return true;
  }

  public void moveScriptToPackage(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getProjectPath() == null) return;
    String currentPkg = "";
    if (definition.getImplementation() != null) {
      int dot = definition.getImplementation().lastIndexOf('.');
      if (dot > 0) currentPkg = definition.getImplementation().substring(0, dot);
    }
    String input = (String) JOptionPane.showInputDialog(
        this,
        "Enter destination package for '" + displayName(definition) + "':",
        "Move to Package",
        JOptionPane.QUESTION_MESSAGE,
        Icons.PACKAGE_16,
        null,
        currentPkg);
    if (input == null || input.isBlank() || input.trim().equals(currentPkg)) return;
    String targetPackage = input.trim();
    if (!ScriptSourcePaths.isValidPackage(targetPackage)) {
      if (!GraphicsEnvironment.isHeadless()) {
        JOptionPane.showMessageDialog(this, "Invalid package name: " + targetPackage, "Move Error", JOptionPane.ERROR_MESSAGE);
      }
      return;
    }
    executeMoveScriptToPackage(definition, targetPackage);
  }

  public boolean executeMoveScriptToPackage(ScriptDefinition definition, String targetPackage) {
    if (definition == null || targetPackage == null || Editor.instance().getProjectPath() == null) return false;
    if (!ScriptSourcePaths.isValidPackage(targetPackage)) {
      setStatus("Invalid package name: " + targetPackage, true);
      return false;
    }

    Path oldPath = this.projectSourcePaths.get(definition);
    if (oldPath == null) oldPath = resolveSource(definition.getSource());

    String className = null;
    if (oldPath != null && Files.isRegularFile(oldPath)) {
      try {
        className = extractClassName(Files.readString(oldPath));
      } catch (IOException ignored) {}
    }
    if (className == null && definition.getImplementation() != null) {
      int dot = definition.getImplementation().lastIndexOf('.');
      className = dot >= 0 ? definition.getImplementation().substring(dot + 1) : definition.getImplementation();
    }
    if (className == null && definition.getSource() != null) {
      String fn = Path.of(definition.getSource()).getFileName().toString();
      int ext = fn.lastIndexOf('.');
      className = ext > 0 ? fn.substring(0, ext) : fn;
    }
    if (className == null || className.isBlank()) {
      className = definition.getId();
    }

    String newSourceRel = ScriptSourcePaths.create(Editor.instance().getProjectModel(), definition.getLanguage(), targetPackage, className);
    Path newPath = resolveSource(newSourceRel);

    if (oldPath == null || !Files.isRegularFile(oldPath) || newPath == null) {
      setStatus("Could not resolve source paths for move.", true);
      return false;
    }

    if (!oldPath.equals(newPath) && Files.exists(newPath)) {
      setStatus("Cannot move script: Destination file '" + newPath.getFileName() + "' already exists in package '" + targetPackage + "'.", true);
      return false;
    }

    try {
      String fileContent = Files.readString(oldPath);
      String updated = fileContent.replaceFirst("(?m)^\\s*package\\s+[a-zA-Z0-9_.]+\\s*;", "package " + targetPackage + ";");
      if (!updated.contains("package " + targetPackage + ";")) {
        updated = "package " + targetPackage + ";\n\n" + updated;
      }
      Files.createDirectories(newPath.getParent());
      Files.writeString(newPath, updated);
      if (!oldPath.equals(newPath)) {
        Files.deleteIfExists(oldPath);
        deleteEmptyDirectoriesUpTo(oldPath.getParent(), ScriptSourcePaths.selectSourceRoot(Editor.instance().getProjectModel()));
      }

      definition.setSource(newSourceRel);
      definition.setImplementation(targetPackage + "." + className);
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());

      ScriptTab tab = this.openTabs.get(this.documentKey(definition));
      if (tab != null) {
        this.openTabs.remove(tab.key);
        tab.path = newPath;
        tab.key = this.documentKey(definition);
        this.openTabs.put(tab.key, tab);
        tab.setText(updated);
        tab.dirty = false;
      }

      if (Editor.instance().getProjectCodeIntegration() != null) {
        Editor.instance().getProjectCodeIntegration().reloadProject(Editor.instance().getProjectModel());
      }
      recordUndoChanges();
      Editor.instance().save(false);
      refreshScripts();
      if (tab != null) {
        open(definition);
      }
      setStatus("Moved script to package " + targetPackage, false);
      return true;
    } catch (IOException e) {
      setStatus("Could not move script: " + e.getMessage(), true);
      return false;
    }
  }

  public void renameScript(ScriptDefinition definition) {
    if (definition == null) return;
    String currentClassName = null;
    Path currentPath = this.projectSourcePaths.get(definition);
    if (currentPath == null) currentPath = resolveSource(definition.getSource());
    if (currentPath != null && Files.isRegularFile(currentPath)) {
      try {
        currentClassName = extractClassName(Files.readString(currentPath));
      } catch (IOException ignored) {}
    }
    if (currentClassName == null && definition.getImplementation() != null) {
      int dot = definition.getImplementation().lastIndexOf('.');
      currentClassName = dot >= 0 ? definition.getImplementation().substring(dot + 1) : definition.getImplementation();
    }
    if (currentClassName == null) {
      currentClassName = definition.getId();
    }

    String input = (String) JOptionPane.showInputDialog(
        this,
        "Enter new class name for '" + displayName(definition) + "':",
        "Rename Class",
        JOptionPane.QUESTION_MESSAGE,
        Icons.RENAME_16,
        null,
        currentClassName);
    if (input == null || input.isBlank() || input.trim().equals(currentClassName)) return;
    renameScript(definition, input.trim());
  }

  public boolean renameScript(ScriptDefinition definition, String newClassName) {
    return this.renameScript(definition, newClassName, null, true);
  }

  private boolean renameScript(ScriptDefinition definition, String newClassName, String sourceText,
                               boolean saveProject) {
    if (definition == null || newClassName == null || newClassName.isBlank()) return false;
    if (!newClassName.matches("[A-Za-z_$][\\w$]*")) {
      setStatus("Invalid Java identifier name: " + newClassName, true);
      return false;
    }

    Path oldPath = this.projectSourcePaths.get(definition);
    if (oldPath == null) oldPath = resolveSource(definition.getSource());
    Path newPath;
    String newSource;
    if (oldPath != null) {
      String newFileName = newClassName + "." + languageFor(oldPath);
      newPath = oldPath.getParent().resolve(newFileName);
      newSource = Editor.instance().getProjectPath() != null
          ? Editor.instance().getProjectPath().getParent().toAbsolutePath().normalize().relativize(newPath.toAbsolutePath().normalize()).toString().replace('\\', '/')
          : ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), newClassName);
    } else {
      newSource = ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), newClassName);
      newPath = resolveSource(newSource);
    }
    if (oldPath == null || !Files.isRegularFile(oldPath) || newPath == null) {
      this.setStatus("The script source file could not be resolved.", true);
      return false;
    }

    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    String currentText = sourceText;
    if (currentText == null && tab != null) currentText = tab.getText();
    if (currentText == null) {
      try {
        currentText = Files.readString(oldPath);
      } catch (IOException error) {
        this.setStatus("Could not read script source: " + error.getMessage(), true);
        return false;
      }
    }

    String oldId = definition.getId();
    String oldClassName = extractClassName(currentText);
    if (oldClassName == null) {
      if (definition.getImplementation() != null) {
        int dot = definition.getImplementation().lastIndexOf('.');
        oldClassName = dot >= 0 ? definition.getImplementation().substring(dot + 1) : definition.getImplementation();
      } else {
        oldClassName = definition.getId();
      }
    }

    if (Objects.equals(oldClassName, newClassName)) return true;

    if (!oldPath.equals(newPath) && Files.exists(newPath)) {
      this.setStatus("A source file named '" + newPath.getFileName() + "' already exists.", true);
      return false;
    }

    boolean idMatchesClassName = Objects.equals(oldId, oldClassName);
    ScriptBindingService.ScriptMutationPlan plan = idMatchesClassName
        ? ScriptBindingService.instance().planRename(oldId, newClassName)
        : null;
    if (plan != null && !plan.valid() && Editor.instance().getGameFile().getScripts().stream().anyMatch(d -> d != null && oldId.equals(d.getId()))) {
      this.setStatus(String.join(" ", plan.errors()), true);
      return false;
    }

    String updatedText = currentText
        .replaceAll("\\b" + Pattern.quote(oldClassName) + "\\b", java.util.regex.Matcher.quoteReplacement(newClassName));
    if (idMatchesClassName) {
      updatedText = updatedText.replace("id = \"" + oldId + "\"", "id = \"" + newClassName + "\"");
    }

    String implementation = extractFullyQualifiedClassName(updatedText);
    if (implementation == null || implementation.isBlank()) {
      String oldImplementation = Objects.toString(definition.getImplementation(), "");
      int packageSeparator = oldImplementation.lastIndexOf('.');
      implementation = packageSeparator < 0 ? newClassName
        : oldImplementation.substring(0, packageSeparator + 1) + newClassName;
    }

    SourceFileMutation sourceMutation;
    try {
      sourceMutation = SourceFileMutation.rename(oldPath, newPath, updatedText);
    } catch (IOException error) {
      this.setStatus("Could not prepare script rename: " + error.getMessage(), true);
      return false;
    }

    String renamedImplementation = implementation;
    boolean preserveCustomName = definition.getName() != null && !definition.getName().equals(oldClassName) && !definition.getName().equals(oldId);

    if (idMatchesClassName && plan.valid()) {
      ScriptBindingService.MutationResult result = ScriptBindingService.instance().execute(plan, renamed -> {
        if (!preserveCustomName) renamed.setName(newClassName);
        renamed.setImplementation(renamedImplementation);
        renamed.setSource(newSource);
      }, sourceMutation);
      if (!result.success()) {
        this.setStatus(result.message(), true);
        return false;
      }
    } else {
      try {
        sourceMutation.apply();
      } catch (RuntimeException error) {
        this.setStatus("Could not rename source file: " + error.getMessage(), true);
        return false;
      }
      if (!preserveCustomName) definition.setName(newClassName);
      definition.setImplementation(renamedImplementation);
      definition.setSource(newSource);
    }

    if (tab != null) {
      this.openTabs.remove(tab.key);
      tab.key = this.documentKey(definition);
      tab.path = newPath;
      this.openTabs.put(tab.key, tab);
      tab.setText(updatedText);
      tab.dirty = false;
      try {
        tab.loadedTime = Files.getLastModifiedTime(newPath);
      } catch (IOException ignored) {
        tab.loadedTime = null;
      }
      tab.updateTabTitle();
      if (this.monacoTab == tab && this.monaco != null) {
        this.monaco.open(newPath, updatedText, definition);
      }
    }

    refreshScripts();
    selectTreeNode(newClassName);
    if (Editor.instance().getProjectCodeIntegration() != null) {
      Editor.instance().getProjectCodeIntegration().reloadProject(Editor.instance().getProjectModel());
    }
    if (saveProject) Editor.instance().save(false);
    this.setStatus("Renamed script to " + newClassName, false);
    return true;
  }
  private static String defaultSource(ScriptDefinition definition, String className, ScriptKind kind) {
    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), definition.getSource());
    ScriptHostType hostType = switch (kind) {
      case GAME -> ScriptHostType.GAME;
      case ENVIRONMENT -> ScriptHostType.ENVIRONMENT;
      case ENTITY -> ScriptHostType.ENTITY;
    };
    return ScriptTemplateFactory.generateTemplate(definition.getId(), hostType, definition.getTargetType(), packageName, className);
  }

  static String synchronizeDeclaration(String source, ScriptDefinition definition) {
    return ScriptTemplateFactory.synchronizeDeclaration(source, definition);
  }

  private static void recordUndoChanges() {
    if (Game.world() != null && Game.world().environment() != null && Game.world().environment().getMap() != null) {
      UndoManager.instance().recordChanges();
    }
  }

  private static String displayName(ScriptDefinition definition) {
    return definition.getName() == null || definition.getName().isBlank() ? definition.getId() : definition.getName();
  }

  private static final class SourceFileMutation implements ScriptBindingService.MutationParticipant {
    private final Path oldPath;
    private final Path newPath;
    private final byte[] oldContent;
    private final byte[] newContent;

    private SourceFileMutation(Path oldPath, Path newPath, byte[] oldContent, byte[] newContent) {
      this.oldPath = oldPath;
      this.newPath = newPath;
      this.oldContent = oldContent;
      this.newContent = newContent;
    }

    private static SourceFileMutation delete(Path path) throws IOException {
      return new SourceFileMutation(path, null, Files.readAllBytes(path), null);
    }

    private static SourceFileMutation rename(Path oldPath, Path newPath, String content) throws IOException {
      return new SourceFileMutation(oldPath, newPath, Files.readAllBytes(oldPath),
        content.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void apply() {
      try {
        if (this.newPath == null) {
          Files.deleteIfExists(this.oldPath);
          return;
        }
        if (this.newPath.getParent() != null) Files.createDirectories(this.newPath.getParent());
        Files.write(this.newPath, this.newContent);
        if (!this.oldPath.equals(this.newPath)) Files.deleteIfExists(this.oldPath);
      } catch (IOException error) {
        throw new UncheckedIOException(error);
      }
    }

    @Override
    public void rollback() {
      try {
        if (this.oldPath.getParent() != null) Files.createDirectories(this.oldPath.getParent());
        Files.write(this.oldPath, this.oldContent);
        if (this.newPath != null && !this.oldPath.equals(this.newPath)) Files.deleteIfExists(this.newPath);
      } catch (IOException error) {
        throw new UncheckedIOException(error);
      }
    }
  }

  private final class ScriptTab extends JPanel {
    private final ScriptDefinition definition;
    private String key;
    private boolean projectSource;
    private String text = "";
    private Path path;
    private FileTime loadedTime;
    private boolean dirty;
    private int caretLine = 1;
    private int caretColumn = 1;
    private JLabel title;

    private ScriptTab(ScriptDefinition definition, Path projectPath, boolean projectSource) {
      this.definition = definition;
      this.projectSource = projectSource;
      this.path = projectPath != null ? projectPath : resolveSource(definition.getSource());
      this.key = projectSource ? projectDocumentKey(this.path) : "runtime:" + definition.getId();
      this.setPreferredSize(new Dimension(0, 0));
      this.setMinimumSize(new Dimension(0, 0));
      this.setMaximumSize(new Dimension(0, 0));
      this.setOpaque(false);
      this.load();
    }

    private String getText() {
      return this.text;
    }

    private void setText(String newText) {
      this.text = newText == null ? "" : newText;
    }

    private void synchronizeDeclaration() {
      if (this.projectSource) return;
      String updated = ScriptWorkspacePanel.synchronizeDeclaration(this.getText(), this.definition);
      if (!Objects.equals(updated, this.getText())) this.setText(updated);
      if (ScriptWorkspacePanel.this.monacoTab == this && ScriptWorkspacePanel.this.monaco != null) {
        ScriptWorkspacePanel.this.monaco.open(this.path, this.getText(), this.definition);
      }
    }

    private void setTextFromMonaco(String text) {
      this.text = text == null ? "" : text;
      this.dirty = true;
      this.updateTabTitle();
      if (activeTab() == this) refreshActiveUsages();
    }

    private void load() {
      try {
        if (this.path != null && Files.isRegularFile(this.path)) {
          this.text = Files.readString(this.path);
          this.repairGeneratedCreatureTarget();
          this.repairScriptImplementation();
          this.loadedTime = Files.getLastModifiedTime(this.path);
        } else {
          this.text = "";
          this.loadedTime = null;
        }
        this.dirty = false;
      } catch (IOException e) {
        setStatus("Could not load source: " + e.getMessage(), true);
      } finally {
        this.updateTabTitle();
      }
      if (ScriptWorkspacePanel.this.monacoTab == this && ScriptWorkspacePanel.this.monaco != null
        && ScriptWorkspacePanel.this.monaco.isReady()) {
        ScriptWorkspacePanel.this.monaco.open(this.path, this.getText(), this.definition);
      }
    }

    private void loadPreservingCaret() {
      this.load();
    }

    private boolean changedOnDisk() {
      if (this.path == null || !Files.isRegularFile(this.path)) return false;
      try {
        return this.loadedTime == null || !Files.getLastModifiedTime(this.path).equals(this.loadedTime);
      } catch (IOException ignored) {
        return false;
      }
    }

    private void acceptExternalVersion() {
      if (this.path == null || !Files.isRegularFile(this.path)) return;
      try {
        this.loadedTime = Files.getLastModifiedTime(this.path);
      } catch (IOException error) {
        setStatus("Could not inspect external source: " + error.getMessage(), true);
      }
    }

    private void repairGeneratedCreatureTarget() {
      if ("de.gurkenlabs.litiengine.entities.Entity".equals(this.definition.getTargetType())
        && this.getText().matches("(?s).*\\bextends\\s+CreatureScript\\b.*")) {
        this.definition.setTargetType(Creature.class.getName());
        Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      }
    }

    private void repairScriptImplementation() {
      if (this.definition == null) return;
      String currentText = this.getText();
      String declaredFqcn = ScriptWorkspacePanel.extractFullyQualifiedClassName(currentText);
      if (declaredFqcn != null && !declaredFqcn.isBlank()
          && !declaredFqcn.equals(this.definition.getImplementation())) {
        this.definition.setImplementation(declaredFqcn);
        Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      }
    }

    private boolean save() {
      if (this.path == null) return false;
      try {
        if (Files.exists(this.path) && this.loadedTime != null && !Files.getLastModifiedTime(this.path).equals(this.loadedTime)) {
          setStatus("Source changed outside utiLITI. Reload it before saving.", true);
          return false;
        }

        String currentText = this.getText();
        String declaredFqcn = ScriptWorkspacePanel.extractFullyQualifiedClassName(currentText);
        String simpleClassName = ScriptWorkspacePanel.extractClassName(currentText);
        if (!this.projectSource && declaredFqcn != null && !declaredFqcn.isBlank()) {
          if (simpleClassName != null && !simpleClassName.isBlank()
              && !simpleClassName.equals(this.definition.getName())) {
            if (!this.renameToClass(simpleClassName)) return false;
          } else {
            this.definition.setImplementation(declaredFqcn);
          }
        }

        if (this.path.getParent() != null) Files.createDirectories(this.path.getParent());
        Files.writeString(this.path, this.getText());
        this.loadedTime = Files.getLastModifiedTime(this.path);
        this.dirty = false;
        this.updateTabTitle();
        if (!this.projectSource) recordUndoChanges();
        return true;
      } catch (IOException e) {
        setStatus("Could not save source: " + e.getMessage(), true);
        return false;
      }
    }

    private boolean renameToClass(String newClassName) {
      return ScriptWorkspacePanel.this.renameScript(this.definition, newClassName, this.getText(), false);
    }

    private void updateTabTitle() {
      if (this.title != null) this.title.setText((this.dirty ? "* " : "") + displayName(this.definition));
    }
  }

  private static Path resolveSource(String source) {
    if (source == null || source.isBlank() || Editor.instance().getProjectPath() == null) return null;
    try {
      Path root = Editor.instance().getProjectModel() != null && Editor.instance().getProjectModel().projectRoot() != null
          ? Editor.instance().getProjectModel().projectRoot()
          : Editor.instance().getProjectPath().getParent().toAbsolutePath().normalize();
      Path configured = Path.of(source);
      Path resolved = (configured.isAbsolute() ? configured : root.resolve(configured)).toAbsolutePath().normalize();
      if (resolved.startsWith(root)) return resolved;
      Path fallbackRoot = Editor.instance().getProjectPath().getParent().toAbsolutePath().normalize();
      Path fallbackResolved = (configured.isAbsolute() ? configured : fallbackRoot.resolve(configured)).toAbsolutePath().normalize();
      return fallbackResolved.startsWith(fallbackRoot) ? fallbackResolved : null;
    } catch (InvalidPathException ignored) {
      return null;
    }
  }

  private record ScriptTreeItem(String label, ScriptDefinition definition, String packageName) {
    ScriptTreeItem(String label, ScriptDefinition definition) {
      this(label, definition, null);
    }
    public boolean isPackage() { return this.definition == null; }
    @Override public String toString() { return this.label; }
  }

  private final class ScriptTreeRenderer extends JPanel implements TreeCellRenderer {
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel usageLabel = new UsageBadge();

    ScriptTreeRenderer() {
      super(new BorderLayout(6, 0));
      this.setOpaque(false);
      this.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

      JPanel left = new JPanel();
      left.setLayout(new javax.swing.BoxLayout(left, javax.swing.BoxLayout.X_AXIS));
      left.setOpaque(false);

      this.iconLabel.setOpaque(false);
      this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
      this.iconLabel.setPreferredSize(new Dimension(18, 18));
      this.nameLabel.setOpaque(false);
      this.usageLabel.setOpaque(false);

      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.usageLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

      this.nameLabel.setFont(Style.getDefaultFont());

      left.add(this.iconLabel);
      left.add(javax.swing.Box.createHorizontalStrut(6));
      left.add(this.nameLabel);
      left.add(javax.swing.Box.createHorizontalStrut(6));
      left.add(this.usageLabel);

      this.add(left, BorderLayout.CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      DefaultMutableTreeNode node = value instanceof DefaultMutableTreeNode n ? n : null;
      Object userObj = node != null ? node.getUserObject() : null;

      if (userObj instanceof ScriptTreeItem item) {
        this.nameLabel.setText(item.label());
        javax.swing.Icon icon;
        if (item.definition() != null) {
          icon = Icons.getScriptIcon(item.definition());
          int usages = scriptUsageCounts.getOrDefault(item.definition().getId(), 0);
          this.usageLabel.setText(usages == 1 ? "1 use" : usages + " uses");
          this.usageLabel.setVisible(usages > 0);
          this.setToolTipText(usages > 0
            ? usages + (usages == 1 ? " script use" : " script uses") : null);
          this.nameLabel.setFont(tree.getFont().deriveFont(Font.PLAIN));
        } else if ("Project Sources".equals(item.label())) {
          icon = expanded ? Icons.FOLDER_OPEN_16 : Icons.GROUP_16;
          this.usageLabel.setVisible(false);
          this.setToolTipText(null);
          this.nameLabel.setFont(tree.getFont().deriveFont(Font.BOLD));
        } else {
          icon = Icons.PACKAGE_16;
          this.usageLabel.setVisible(false);
          this.setToolTipText(item.packageName() != null ? "Package: " + item.packageName() : null);
          this.nameLabel.setFont(tree.getFont().deriveFont(Font.PLAIN));
        }

        this.iconLabel.setIcon(icon);
      } else {
        this.nameLabel.setText(Objects.toString(value, ""));
        this.iconLabel.setIcon(null);
        this.usageLabel.setVisible(false);
        this.setToolTipText(null);
        this.nameLabel.setFont(tree.getFont().deriveFont(Font.PLAIN));
      }

      this.nameLabel.setForeground(Style.text());

      int level = node != null ? Math.max(0, node.getLevel() - 1) : 0;
      int depthInset = level * 16;
      int width = Math.max(100, tree.getWidth() - 20 - depthInset);
      int rowHeight = tree.getRowHeight() > 0 ? tree.getRowHeight() : (int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale());
      this.setOpaque(false);
      return this;
    }
  }

  private static final class UsageBadge extends JLabel {
    private UsageBadge() {
      this.setOpaque(false);
      this.setForeground(Style.mutedText());
      this.setFont(Style.getDefaultFont().deriveFont(10.5f));
      this.setBorder(BorderFactory.createEmptyBorder(1, 6, 1, 6));
    }

    @Override
    protected void paintComponent(Graphics graphics) {
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(Style.surface());
        g.fillRoundRect(0, 2, this.getWidth(), Math.max(0, this.getHeight() - 4), 10, 10);
      } finally {
        g.dispose();
      }
      super.paintComponent(graphics);
    }
  }

  private final class GlobalApiTreeRenderer extends JPanel implements TreeCellRenderer {
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();

    GlobalApiTreeRenderer() {
      super(new BorderLayout(6, 0));
      this.setOpaque(false);
      this.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 6));

      JPanel left = new JPanel();
      left.setLayout(new javax.swing.BoxLayout(left, javax.swing.BoxLayout.X_AXIS));
      left.setOpaque(false);


      this.iconLabel.setOpaque(false);
      this.iconLabel.setHorizontalAlignment(JLabel.CENTER);
      this.iconLabel.setPreferredSize(new Dimension(18, 18));
      this.nameLabel.setOpaque(false);
      this.detailLabel.setOpaque(false);

      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.detailLabel.setAlignmentY(Component.CENTER_ALIGNMENT);

      left.add(this.iconLabel);
      left.add(javax.swing.Box.createHorizontalStrut(6));
      left.add(this.nameLabel);
      left.add(javax.swing.Box.createHorizontalStrut(6));
      left.add(this.detailLabel);

      this.add(left, BorderLayout.CENTER);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      DefaultMutableTreeNode node = value instanceof DefaultMutableTreeNode n ? n : null;
      Object userObj = node != null ? node.getUserObject() : null;

      if (userObj instanceof GlobalApiItem item) {
        this.iconLabel.setIcon(switch (item.badge()) {
          case "h" -> Icons.SYMBOL_CLASS_16;
          case "e" -> Icons.SYMBOL_DEPENDENCY_16;
          case "c", "g" -> Icons.SYMBOL_FIELD_16;
          case "m", "hook", "a", "p" -> Icons.SYMBOL_METHOD_16;
          case "q" -> Icons.SEARCH_16;
          case "u" -> Icons.DOCUMENTATION_16;
          case "creature" -> Icons.CREATURE_16;
          case "prop" -> Icons.PROP_16;
          case "trigger" -> Icons.TRIGGER_16;
          case "emitter" -> Icons.EMITTER_16;
          default -> Icons.API_16;
        });

        this.nameLabel.setText(item.label());
        this.nameLabel.setFont(Style.getDefaultFont().deriveFont(Font.PLAIN, 11.5f));
        this.nameLabel.setForeground(Style.text());

        this.detailLabel.setText(item.description());
        this.detailLabel.setFont(Style.getDefaultFont().deriveFont(10.5f));
        this.detailLabel.setForeground(Style.mutedText());
        this.detailLabel.setVisible(true);

        this.setToolTipText("<html><b>" + item.label() + "</b>: " + item.description() + "<br><code>" + item.snippet().replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") + "</code><br><i>Double-click to insert</i></html>");
      } else {
        this.iconLabel.setIcon(expanded ? Icons.FOLDER_OPEN_16 : Icons.GROUP_16);
        this.nameLabel.setText(Objects.toString(userObj, ""));

        this.nameLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
        this.nameLabel.setForeground(Style.mutedText());
        this.detailLabel.setText("");
        this.detailLabel.setVisible(false);
        this.setToolTipText(null);
      }

      int level = node != null ? Math.max(0, node.getLevel() - 1) : 0;
      int depthInset = level * 16;
      int width = Math.max(100, tree.getWidth() - 20 - depthInset);
      int rowHeight = tree.getRowHeight() > 0 ? tree.getRowHeight() : (int) (Style.TREE_ROW_HEIGHT * Editor.preferences().getUiScale());
      this.setPreferredSize(new Dimension(width, rowHeight));
      this.setOpaque(false);

      return this;
    }
  }




  private static final class ProblemSeverityRenderer extends javax.swing.table.DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, "", isSelected, hasFocus, row, column);
      if (value instanceof ScriptDiagnostic.Severity severity) {
        if (severity == ScriptDiagnostic.Severity.ERROR) {
          setIcon(Icons.ERROR_16);
          setText("Error");
          setForeground(isSelected ? Color.WHITE : new Color(255, 110, 110));
        } else {
          setIcon(Icons.BULB_16);
          setText("Warning");
          setForeground(isSelected ? Color.WHITE : new Color(240, 200, 80));
        }
      } else {
        setIcon(null);
        setText(Objects.toString(value, ""));
      }
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
      return this;
    }
  }

  private static final class ProblemFileRenderer extends javax.swing.table.DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, Objects.toString(value, ""), isSelected, hasFocus, row, column);
      setIcon(Icons.SCRIPT_16);
      setFont(Style.getDefaultFont());
      setForeground(isSelected ? Color.WHITE : Style.text());
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
      return this;
    }
  }

  private static final class ProblemLineRenderer extends javax.swing.table.DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      String lineText = value instanceof Integer line ? "Ln " + line : Objects.toString(value, "");
      super.getTableCellRendererComponent(table, lineText, isSelected, hasFocus, row, column);
      setIcon(null);
      setFont(Style.getDefaultFont().deriveFont(11f));
      setForeground(isSelected ? Color.WHITE : Style.mutedText());
      setHorizontalAlignment(SwingConstants.RIGHT);
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
      return this;
    }
  }

  private static final class ProblemMessageRenderer extends javax.swing.table.DefaultTableCellRenderer {
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
      super.getTableCellRendererComponent(table, Objects.toString(value, ""), isSelected, hasFocus, row, column);
      setIcon(null);
      setFont(Style.getDefaultFont());
      setForeground(isSelected ? Color.WHITE : Style.text());
      setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
      return this;
    }
  }

}
