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
import de.gurkenlabs.utiliti.controller.ScriptSourcePaths;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.controller.debug.JdiScriptDebuggerBackend;
import de.gurkenlabs.utiliti.controller.debug.ScriptBreakpoint;
import de.gurkenlabs.utiliti.controller.debug.ScriptBreakpointStore;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebuggerBackend;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  private final JTree scripts = UI.createStyledTree(this.scriptsModel);
  private final JTextField search = createSearchTextField("Search scripts...");
  private final DefaultMutableTreeNode outlineRoot = new DefaultMutableTreeNode("Outline");
  private final DefaultTreeModel outlineModel = new DefaultTreeModel(this.outlineRoot);
  private final JTree outline = UI.createStyledTree(this.outlineModel);
  private final DefaultMutableTreeNode globalsRoot = new DefaultMutableTreeNode("Globals & APIs");
  private final DefaultTreeModel globalsTreeModel = new DefaultTreeModel(this.globalsRoot);
  private final JTree globalsTree = UI.createStyledTree(this.globalsTreeModel);
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
  private final JPanel conflictBar = new JPanel(new BorderLayout(8, 0));
  private final JLabel conflictMessage = new JLabel();
  private final Map<String, ScriptTab> openTabs = new LinkedHashMap<>();
  private final Map<ScriptDefinition, Path> projectSourcePaths = new IdentityHashMap<>();
  private final Map<String, ScriptDefinition> projectSourceDefinitions = new LinkedHashMap<>();
  private final Map<String, ScriptDefinition> navigatedProjectDefinitions = new LinkedHashMap<>();
  private final Map<String, Path> navigatedProjectSources = new LinkedHashMap<>();
  private final Timer externalChangeTimer = new Timer(900, event -> this.checkExternalChanges());
  private MonacoScriptEditor monaco;
  private ScriptTab monacoTab;
  private ScriptTab conflictTab;
  private final ScriptDebuggerPanel debuggerPanel = new ScriptDebuggerPanel();
  private final List<ScriptBreakpoint> breakpoints = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final Timer breakpointSyncTimer = new Timer(300, e -> this.syncBreakpoints());
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

  public ScriptWorkspacePanel() {
    super(new BorderLayout());
    this.setBackground(Style.background());
    this.add(this.createConflictBar(), BorderLayout.NORTH);

    JTabbedPane sidebarTabs = new JTabbedPane(JTabbedPane.TOP);
    sidebarTabs.putClientProperty("JTabbedPane.noContentBorder", Boolean.TRUE);
    sidebarTabs.putClientProperty("JTabbedPane.hasFullBorder", Boolean.FALSE);
    sidebarTabs.putClientProperty("JTabbedPane.contentInsets", new java.awt.Insets(0, 0, 0, 0));
    sidebarTabs.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(0, 0, 0, 0));
    sidebarTabs.putClientProperty("JTabbedPane.tabType", "underlined");
    sidebarTabs.putClientProperty("JTabbedPane.showTabSeparators", Boolean.TRUE);
    sidebarTabs.putClientProperty("JTabbedPane.tabHeight", 28);
    sidebarTabs.putClientProperty("JTabbedPane.tabInsets", new java.awt.Insets(2, 10, 2, 10));
    sidebarTabs.putClientProperty("JTabbedPane.underlineColor", Style.accent());
    sidebarTabs.putClientProperty("JTabbedPane.underlineHeight", 2);
    sidebarTabs.putClientProperty("JTabbedPane.selectedBackground", Style.surface());
    sidebarTabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    sidebarTabs.setBackground(Style.background());
    sidebarTabs.addTab("Outline", Icons.SYMBOL_GROUP_16, this.createOutline());
    sidebarTabs.addTab("Globals & APIs", Icons.API_16, this.createGlobalsPanel());

    JSplitPane explorer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.createScriptExplorer(), sidebarTabs);
    UI.configureSplitPane(explorer);
    explorer.setBackground(Style.COLOR_BG);
    explorer.setResizeWeight(0.55);
    explorer.setMinimumSize(new Dimension(235, 0));
    explorer.setPreferredSize(new Dimension(265, 0));

    this.problems.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
    this.problems.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.problems.setShowGrid(false);
    this.problems.setIntercellSpacing(new Dimension(0, 0));
    this.problems.setBackground(Style.COLOR_BG);
    this.problems.setForeground(Style.text());
    this.problems.setSelectionBackground(Style.sceneRowSelected());
    this.problems.setSelectionForeground(Color.WHITE);
    this.problems.setFont(Style.getDefaultFont());
    this.problems.setOpaque(false);

    if (this.problems.getTableHeader() != null) {
      this.problems.getTableHeader().setBackground(Style.COLOR_BG);
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
    this.tabs.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()));
    this.tabs.setBackground(Style.background());

    this.mainEditorArea.add(this.tabs, BorderLayout.NORTH);

    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setBackground(Style.COLOR_BG);
    statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    Font statusBarFont = new Font(
        Style.FONTNAME_CONSOLE,
        Font.PLAIN,
        Math.max(10, Math.round(11 * Editor.preferences().getUiScale())));
    this.status.setFont(statusBarFont);
    this.status.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    this.status.setForeground(Style.mutedText());
    this.caretStatus.setFont(statusBarFont);
    this.caretStatus.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    this.caretStatus.setForeground(Style.mutedText());
    JPanel rightStatus = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 0));
    rightStatus.setOpaque(false);
    rightStatus.add(this.status);
    JLabel mcpBadge = StatusBar.createMcpBadge();
    rightStatus.add(mcpBadge);
    statusBar.add(this.caretStatus, BorderLayout.WEST);
    statusBar.add(rightStatus, BorderLayout.EAST);
    this.mainEditorArea.add(statusBar, BorderLayout.SOUTH);

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, this.mainEditorArea);
    UI.configureSplitPane(split);
    split.setResizeWeight(0.0);
    split.setDividerLocation(265);
    this.add(split, BorderLayout.CENTER);

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
      });
    });

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
    if (this.externalChangeTimer != null) {
      this.externalChangeTimer.stop();
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
  }

  public void refreshScripts() {
    String selectedId = this.selectedDefinition() == null ? null : this.selectedDefinition().getId();
    this.scriptsRoot.removeAllChildren();
    this.repairProjectScriptDefinitions();
    this.refreshProjectSourceDocuments();
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

      Editor.instance().getGameFile().getScripts().stream()
        .filter(definition -> query.isEmpty() || displayName(definition).toLowerCase(Locale.ROOT).contains(query)
          || Objects.toString(definition.getSource(), "").toLowerCase(Locale.ROOT).contains(query)
          || Objects.toString(definition.getImplementation(), "").toLowerCase(Locale.ROOT).contains(query))
        .sorted(Comparator.comparing(ScriptWorkspacePanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .forEach(definition -> {
          if (this.isProjectSource(definition)) this.insertProjectSourceNode(definition);
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

  public void open(ScriptDefinition definition) {
    if (definition == null) return;
    String key = documentKey(definition);
    ScriptTab tab = this.openTabs.computeIfAbsent(key, ignored -> {
      Path projectSource = this.projectSourcePaths.get(definition);
      ScriptTab created = new ScriptTab(definition, projectSource, projectSource != null);
      this.tabs.addTab(displayName(definition), Icons.SCRIPT_16, created, definition.getSource());
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
      if (discovered.sourcePath() == null) continue;
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
    String name = source == null ? "" : source.getFileName().toString().toLowerCase(Locale.ROOT);
    if (name.endsWith(".groovy")) return "groovy";
    if (name.endsWith(".kt")) return "kotlin";
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
    UndoManager.instance().recordChanges();
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

  public void reloadActive() {
    ScriptTab tab = this.activeTab();
    if (tab == null || !tab.save()) return;
    if (tab.projectSource) {
      this.rebuildProjectSource(tab);
      return;
    }
    this.appendOutput("Compiling " + displayName(tab.definition) + " ...");
    Editor.instance().reloadProjectCode();
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    Game.scripts().clearDiagnostics();
    boolean successful = Game.scripts().reload(tab.definition.getId());
    this.showDiagnostics(tab.definition);
    this.appendOutput(successful ? "Compilation successful; script reloaded." : "Compilation failed; previous generation kept active.");
    this.setStatus(successful ? "Compiled and reloaded " + displayName(tab.definition)
      : "Reload failed; the previous generation is still active", !successful);
  }

  private void rebuildProjectSource(ScriptTab tab) {
    if (!this.projectSourceBuildInProgress.compareAndSet(false, true)) {
      this.setStatus("A project source build is already running", true);
      return;
    }
    this.appendOutput("Building project implementation " + tab.definition.getImplementation() + " ...");
    Thread.ofVirtual().name("utiliti-project-script-build").start(() -> {
      try {
        var session = Editor.instance().buildProjectClasses();
        session.onOutput(this::appendOutput);
        var completed = new java.util.concurrent.CountDownLatch(1);
        session.onStateChanged(state -> {
          if (state == de.gurkenlabs.utiliti.controller.ProjectSession.State.EXITED
            || state == de.gurkenlabs.utiliti.controller.ProjectSession.State.FAILED) completed.countDown();
        });
        if (!session.isActive()) completed.countDown();
        completed.await();
        boolean successful = session.exitCode().orElse(-1) == 0;
        if (successful) {
          Editor.instance().reloadProjectCode();
          Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
          if (Editor.instance().getGameFile().getScripts().contains(tab.definition)) {
            Game.scripts().clearDiagnostics();
            successful = Game.scripts().reload(tab.definition.getId());
          }
        }
        boolean result = successful;
        SwingUtilities.invokeLater(() -> {
          this.refreshScripts();
          this.showDiagnostics(tab.definition);
          this.appendOutput(result ? "Project classes built and reloaded." : "Project build or script reload failed.");
          this.setStatus(result ? "Built and reloaded " + displayName(tab.definition)
            : "Build failed; the previous implementation is still active", !result);
        });
      } catch (InterruptedException e) {
        Thread.currentThread().interrupt();
        SwingUtilities.invokeLater(() -> this.setStatus("Project build was interrupted", true));
      } catch (Exception e) {
        log.log(Level.WARNING, "Could not build and reload project source", e);
        SwingUtilities.invokeLater(() -> this.setStatus("Could not build project: " + e.getMessage(), true));
      } finally {
        this.projectSourceBuildInProgress.set(false);
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

  public void openActiveExternally() {
    ScriptTab tab = this.activeTab();
    if (tab == null || tab.path == null) return;
    try {
      if (!Files.exists(tab.path)) tab.save();
      IntellijIntegration.open(projectRoot(), tab.path, tab.caretLine, tab.caretColumn);
    } catch (IOException e) {
      this.setStatus("Could not open external editor: " + e.getMessage(), true);
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
    this.setBackground(Style.COLOR_BG);
    this.scripts.setBackground(Style.COLOR_BG);
    this.scripts.setForeground(Style.text());
    this.outline.setBackground(Style.COLOR_BG);
    this.outline.setForeground(Style.text());
    this.problems.setBackground(Style.surface());
    this.problems.setForeground(Style.text());
    this.problems.setGridColor(Style.border());
    if (this.monaco != null) {
      Color background = Style.background();
      this.monaco.setTheme(background.getRed() + background.getGreen() + background.getBlue() < 384);
    }
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
    JButton compare = new JButton("Review in IntelliJ");
    compare.addActionListener(event -> this.openActiveExternally());
    actions.add(reload);
    actions.add(keep);
    actions.add(compare);
    this.conflictBar.add(actions, BorderLayout.EAST);
    this.conflictBar.setVisible(false);
    return this.conflictBar;
  }

  private JPanel createScriptExplorer() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setBorder(BorderFactory.createEmptyBorder(8, 8, 6, 8));

    JPanel header = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    header.setOpaque(false);

    JPanel titleRow = new JPanel(new BorderLayout(5, 0));
    titleRow.setOpaque(false);
    titleRow.add(sectionTitle("SCRIPTS"), BorderLayout.WEST);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 2, 0));
    actions.setOpaque(false);

    JButton addBtn = Style.iconButton(Icons.ADD_16);
    addBtn.setToolTipText("Add script");
    addBtn.addActionListener(event -> {
      JPopupMenu menu = createAddScriptPopupMenu();
      menu.show(addBtn, 0, addBtn.getHeight());
    });

    JButton dupBtn = Style.iconButton(Icons.COPY_16);
    dupBtn.setToolTipText("Duplicate selected script");
    dupBtn.addActionListener(event -> duplicateScript(selectedDefinition()));

    JButton deleteBtn = Style.iconButton(Icons.DELETE_16);
    deleteBtn.setToolTipText("Delete selected script");
    deleteBtn.addActionListener(event -> deleteScript(selectedDefinition()));

    JButton configGameBtn = Style.iconButton(Icons.SETTINGS_16);
    configGameBtn.setToolTipText("Configure Game Scripts & Startup Settings");
    configGameBtn.addActionListener(event -> de.gurkenlabs.utiliti.view.dialogs.GameScriptsDialog.showDialog());

    JButton guideBtn = Style.iconButton(Icons.DOCUMENTATION_16);
    guideBtn.setToolTipText("Open Scripting Architecture & Getting Started Guide");
    guideBtn.addActionListener(event -> de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog.showGuide());

    actions.add(addBtn);
    actions.add(dupBtn);
    actions.add(deleteBtn);
    actions.add(configGameBtn);
    actions.add(guideBtn);

    header.add(titleRow, BorderLayout.NORTH);
    header.add(actions, BorderLayout.SOUTH);
    panel.add(header, BorderLayout.NORTH);

    JPanel content = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    content.setBackground(Style.COLOR_BG);
    header.setBackground(Style.COLOR_BG);
    panel.setBackground(Style.COLOR_BG);
    RoundedSearchBox searchBox = new RoundedSearchBox(this.search, 0);
    searchBox.getClearButton().addActionListener(e -> {
      this.search.setText("");
      this.refreshScripts();
    });
    content.add(searchBox, BorderLayout.NORTH);
    this.scripts.setRootVisible(false);
    this.scripts.setShowsRootHandles(true);
    this.scripts.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.scripts.setBackground(Style.COLOR_BG);
    this.scripts.setOpaque(false);
    this.scripts.putClientProperty("JTree.lineStyle", "None");
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
      evt -> renameScript(selectedDefinition()),
      KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F2, 0),
      JComponent.WHEN_FOCUSED
    );

    content.add(createBorderlessScrollPane(this.scripts), BorderLayout.CENTER);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createOutline() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setBackground(Style.COLOR_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
    this.outline.setRootVisible(false);
    this.outline.setShowsRootHandles(true);
    this.outline.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.outline.setBackground(Style.COLOR_BG);
    this.outline.setOpaque(false);
    this.outline.putClientProperty("JTree.lineStyle", "None");
    this.outline.setCellRenderer(new OutlineTreeRenderer());
    this.outline.addTreeSelectionListener(event -> this.navigateToOutlineSelection());
    panel.add(createBorderlessScrollPane(this.outline), BorderLayout.CENTER);
    return panel;
  }

  private JPanel createGlobalsPanel() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setBackground(Style.COLOR_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));

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

    JPanel content = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    content.setBackground(Style.COLOR_BG);

    this.globalsTree.setRootVisible(false);
    this.globalsTree.setShowsRootHandles(true);
    this.globalsTree.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.globalsTree.setBackground(Style.COLOR_BG);
    this.globalsTree.setOpaque(false);
    this.globalsTree.putClientProperty("JTree.lineStyle", "None");
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

    content.add(createBorderlessScrollPane(this.globalsTree), BorderLayout.CENTER);
    panel.add(content, BorderLayout.CENTER);
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
    scroll.getViewport().setOpaque(false);
    scroll.getViewport().setBackground(Style.COLOR_BG);
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
      setToolTipText(Resources.strings().get("close"));
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

    JLabel label = new JLabel(displayName(tab.definition), Icons.SCRIPT_16, SwingConstants.LEADING);
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
    if (this.monaco != null && tab != null) {
      java.net.URI uri = tab.path == null ? java.net.URI.create("inmemory://script/" + tab.definition.getId()) : tab.path.toUri();
      this.monaco.closeModel(uri);
    }
    if (this.monacoTab == tab) {
      this.monacoTab = null;
    }
    this.openTabs.remove(tab.key);
    this.tabs.remove(tab);
    this.activeTabChanged();
  }

  private static JTextField createSearchTextField(String placeholder) {
    JTextField field = new JTextField() {
      @Override
      public void updateUI() {
        super.updateUI();
        setBorder(BorderFactory.createEmptyBorder());
        setOpaque(false);
        putClientProperty("JComponent.outline", "none");
      }

      @Override
      protected void paintBorder(Graphics g) {
        // The parent search box owns the only visible border.
      }
    };
    field.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, placeholder);
    field.setToolTipText(placeholder);
    field.setBorder(BorderFactory.createEmptyBorder());
    field.setOpaque(false);
    field.putClientProperty("JComponent.outline", "none");
    field.setFont(Style.getDefaultFont());
    return field;
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
      this.mainEditorArea.add(this.monaco, BorderLayout.CENTER);
      this.refreshTheme();
      this.mainEditorArea.revalidate();
      this.mainEditorArea.repaint();
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
    this.selectionListener.accept(definition);
    this.showDiagnostics(definition);
    this.refreshOutline(active);
    this.updateCaretStatus(active);
    this.refreshGlobals();
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
      this.projectDiagnostics.put(this.monacoTab.definition.getId(), new ArrayList<>(analysis.diagnostics()));
    }
    refreshProblemsTable();
  }

  public void refreshProblemsTable() {
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

  private void refreshOutline(ScriptTab tab) {
    this.outlineRoot.removeAllChildren();
    if (tab != null) {
      ScriptOutline.Symbol symbol = ScriptOutline.parse(tab.getText());
      if (symbol != null) this.outlineRoot.add(outlineNode(symbol));
    }
    this.outlineModel.reload();
    for (int row = 0; row < this.outline.getRowCount(); row++) this.outline.expandRow(row);
  }

  private void navigateToOutlineSelection() {
    ScriptTab tab = this.activeTab();
    Object selected = this.outline.getLastSelectedPathComponent();
    if (tab == null || !(selected instanceof DefaultMutableTreeNode node)
      || !(node.getUserObject() instanceof ScriptOutline.Symbol symbol) || symbol.line() < 0) return;
    if (this.monacoTab == tab && this.monaco != null && this.monaco.isReady()) {
      this.monaco.revealLine(symbol.line() + 1);
    }
  }

  private static DefaultMutableTreeNode outlineNode(ScriptOutline.Symbol symbol) {
    DefaultMutableTreeNode node = new DefaultMutableTreeNode(symbol);
    symbol.children().forEach(child -> node.add(outlineNode(child)));
    return node;
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

  private void insertScriptNode(ScriptDefinition definition) {
    DefaultMutableTreeNode parent = this.scriptsRoot;
    String relative = Objects.toString(definition.getSource(), "").replace('\\', '/');
    if (relative.startsWith("src/main/java/")) {
      relative = relative.substring("src/main/java/".length());
    } else if (relative.startsWith("src/main/groovy/")) {
      relative = relative.substring("src/main/groovy/".length());
    } else if (relative.startsWith("src/")) {
      relative = relative.substring("src/".length());
    } else if (relative.startsWith("scripts/")) {
      relative = relative.substring("scripts/".length());
    }
    String[] parts = relative.split("/");
    for (int i = 0; i < Math.max(0, parts.length - 1); i++) {
      if (parts[i].isBlank()) continue;
      parent = childFolder(parent, parts[i]);
    }
    parent.add(new DefaultMutableTreeNode(new ScriptTreeItem(displayName(definition), definition)));
  }

  private void insertProjectSourceNode(ScriptDefinition definition) {
    DefaultMutableTreeNode parent = childFolder(this.scriptsRoot, "Project Sources");
    String implementation = Objects.toString(definition.getImplementation(), definition.getId());
    String[] parts = implementation.split("\\.");
    for (int i = 0; i < Math.max(0, parts.length - 1); i++) {
      if (!parts[i].isBlank()) parent = childFolder(parent, parts[i]);
    }
    parent.add(new DefaultMutableTreeNode(new ScriptTreeItem(displayName(definition), definition)));
  }

  private static void compactEmptyFolders(DefaultMutableTreeNode node) {
    if (node == null) return;
    for (int i = 0; i < node.getChildCount(); i++) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
      compactEmptyFolders(child);
    }
    if (node.getUserObject() instanceof ScriptTreeItem item && item.definition() == null && !"Project Sources".equals(item.label())) {
      while (node.getChildCount() == 1) {
        DefaultMutableTreeNode onlyChild = (DefaultMutableTreeNode) node.getChildAt(0);
        if (onlyChild.getUserObject() instanceof ScriptTreeItem childItem && childItem.definition() == null) {
          String merged = item.label() + "." + childItem.label();
          item = new ScriptTreeItem(merged, null);
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

  private static DefaultMutableTreeNode childFolder(DefaultMutableTreeNode parent, String name) {
    Enumeration<?> children = parent.children();
    while (children.hasMoreElements()) {
      DefaultMutableTreeNode child = (DefaultMutableTreeNode) children.nextElement();
      if (child.getUserObject() instanceof ScriptTreeItem item && item.definition() == null && item.label().equals(name)) return child;
    }
    DefaultMutableTreeNode child = new DefaultMutableTreeNode(new ScriptTreeItem(name, null));
    parent.add(child);
    return child;
  }

  private ScriptDefinition selectedDefinition() {
    Object selected = this.scripts.getLastSelectedPathComponent();
    if (!(selected instanceof DefaultMutableTreeNode node) || !(node.getUserObject() instanceof ScriptTreeItem item)) return null;
    return item.definition();
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
    return createScript(kind, kind == ScriptKind.ENTITY ? Creature.class : null);
  }

  public ScriptDefinition createScript(ScriptKind kind, Class<?> targetClass) {
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
    String relPath = ScriptSourcePaths.create(Editor.instance().getProjectModel(), "java", className);
    Path source = resolveSource(relPath);
    if (source == null) return null;

    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), relPath);
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
      UndoManager.instance().recordChanges();
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
    if (source == null || source.isBlank()) return null;
    var matcher = Pattern.compile("(?m)^\\s*(?:public\\s+)?class\\s+([A-Za-z_$][\\w$]*)").matcher(source);
    return matcher.find() ? matcher.group(1) : null;
  }

  public static String extractFullyQualifiedClassName(String source) {
    if (source == null || source.isBlank()) return null;
    String simpleName = extractClassName(source);
    if (simpleName == null) return null;
    var pkgMatcher = Pattern.compile("(?m)^\\s*package\\s+([A-Za-z_$][\\w$.]*)\\s*;").matcher(source);
    if (pkgMatcher.find()) {
      String pkg = pkgMatcher.group(1).trim();
      if (!pkg.isEmpty()) {
        return pkg + "." + simpleName;
      }
    }
    return simpleName;
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

  public void deleteScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;
    if (this.isProjectSource(definition)) {
      this.setStatus("Project source files cannot be deleted from the script workspace", true);
      return;
    }
    int choice = JOptionPane.showConfirmDialog(this,
      "Are you sure you want to delete script '" + displayName(definition) + "'?\nThis will remove the file from disk.",
      "Delete Script", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) return;

    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    if (tab != null) closeTab(tab);

    Path file = resolveSource(definition.getSource());
    if (file != null && Files.exists(file)) {
      try {
        Files.delete(file);
      } catch (IOException e) {
        setStatus("Could not delete file: " + e.getMessage(), true);
      }
    }

    Editor.instance().getGameFile().getScripts().remove(definition);
    Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
    UndoManager.instance().recordChanges();
    UI.getAssetController().refresh();
    refreshScripts();
    setStatus("Deleted script " + displayName(definition), false);
  }

  public void closeTab(ScriptDefinition definition) {
    if (definition == null) return;
    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    if (tab != null) closeTab(tab);
  }

  public void reloadTab(ScriptDefinition definition) {
    if (definition == null) return;
    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    if (tab != null) tab.loadPreservingCaret();
  }

  public void duplicateScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;
    if (this.isProjectSource(definition)) {
      this.setStatus("Project source files cannot be duplicated from the script workspace", true);
      return;
    }
    int suffix = 1;
    String id;
    String className;
    Path source;
    String baseName = definition.getName() == null ? "Script" : definition.getName();
    do {
      className = baseName + "Copy" + (suffix == 1 ? "" : suffix);
      id = definition.getId() + "-copy" + (suffix == 1 ? "" : "-" + suffix);
      source = resolveSource(ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), className));
      suffix++;
    } while (source != null && (Files.exists(source) || scriptIdExists(id)));
    String newSourceRel = ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), className);
    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), newSourceRel);
    String implementation = (packageName != null && !packageName.isBlank())
        ? packageName + "." + className
        : className;

    ScriptDefinition dup = new ScriptDefinition(id, definition.getLanguage(),
      newSourceRel, implementation, definition.getHost());
    dup.setTargetType(definition.getTargetType());

    Path originalFile = resolveSource(definition.getSource());
    String content = "";
    if (originalFile != null && Files.exists(originalFile)) {
      try {
        content = Files.readString(originalFile);
        content = content.replaceFirst("(?m)^(\\s*public\\s+)?class\\s+\\w+", "$1class " + className);
      } catch (IOException ignored) {}
    } else {
      content = defaultSource(dup, className, ScriptKind.ENTITY);
    }

    try {
      Files.createDirectories(source.getParent());
      Files.writeString(source, content);
      Editor.instance().getGameFile().getScripts().add(dup);
      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      UndoManager.instance().recordChanges();
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
    JMenuItem entityScript = new JMenuItem("Entity Script...", Icons.SCRIPT_16);
    entityScript.addActionListener(e -> createScript(ScriptKind.ENTITY));

    JMenuItem gameScript = new JMenuItem("Game Script...", Icons.SCRIPT_16);
    gameScript.addActionListener(e -> createScript(ScriptKind.GAME));

    JMenuItem envScript = new JMenuItem("Environment Script...", Icons.SCRIPT_16);
    envScript.addActionListener(e -> createScript(ScriptKind.ENVIRONMENT));

    menu.add(entityScript);
    menu.add(gameScript);
    menu.add(envScript);
    return menu;
  }

  private void showTreeContextMenu(java.awt.event.MouseEvent e) {
    if (!e.isPopupTrigger()) return;
    int row = this.scripts.getClosestRowForLocation(e.getX(), e.getY());
    if (row >= 0) this.scripts.setSelectionRow(row);
    ScriptDefinition selected = selectedDefinition();

    JPopupMenu menu = new JPopupMenu();
    JMenu newSub = new JMenu("New Script");
    newSub.setIcon(Icons.ADD_16);
    JMenuItem entityScript = new JMenuItem("Entity Script...");
    entityScript.addActionListener(evt -> createScript(ScriptKind.ENTITY));
    JMenuItem gameScript = new JMenuItem("Game Script...");
    gameScript.addActionListener(evt -> createScript(ScriptKind.GAME));
    JMenuItem envScript = new JMenuItem("Environment Script...");
    envScript.addActionListener(evt -> createScript(ScriptKind.ENVIRONMENT));
    newSub.add(entityScript);
    newSub.add(gameScript);
    newSub.add(envScript);
    menu.add(newSub);

    if (selected != null) {
      menu.addSeparator();
      if (!this.isProjectSource(selected)) {
        JMenuItem dupItem = new JMenuItem("Duplicate Script", Icons.COPY_16);
        dupItem.addActionListener(evt -> duplicateScript(selected));
        menu.add(dupItem);

        JMenuItem renameItem = new JMenuItem("Rename Class...", Icons.RENAME_16);
        renameItem.addActionListener(evt -> renameScript(selected));
        menu.add(renameItem);

        JMenuItem deleteItem = new JMenuItem("Delete Script", Icons.DELETE_16);
        deleteItem.addActionListener(evt -> deleteScript(selected));
        menu.add(deleteItem);
      }

      JMenuItem openIdeItem = new JMenuItem("Open in IDE", Icons.EXTERNAL_16);
      openIdeItem.addActionListener(evt -> openActiveExternally());
      menu.add(openIdeItem);
    }
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void renameScript(ScriptDefinition definition) {
    if (definition == null) return;
    if (this.isProjectSource(definition)) {
      this.setStatus("Rename project classes with the project refactoring tools", true);
      return;
    }
    String currentName = displayName(definition);
    String input = (String) JOptionPane.showInputDialog(
        this,
        "Enter new class name:",
        "Rename Script",
        JOptionPane.QUESTION_MESSAGE,
        Icons.RENAME_16,
        null,
        currentName);
    if (input == null || input.isBlank() || input.trim().equals(currentName)) return;
    renameScript(definition, input.trim());
  }

  public void renameScript(ScriptDefinition definition, String newClassName) {
    if (definition == null || newClassName == null || newClassName.isBlank()) return;
    if (this.isProjectSource(definition)) return;
    String oldClassName = displayName(definition);
    if (oldClassName.equals(newClassName)) return;
    if (!newClassName.matches("[A-Za-z_$][\\w$]*")) {
      JOptionPane.showMessageDialog(this, "Invalid Java identifier name.", "Rename Error", JOptionPane.ERROR_MESSAGE);
      return;
    }

    Path oldPath = resolveSource(definition.getSource());
    String newSource = ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), newClassName);
    Path newPath = resolveSource(newSource);
    if (newPath == null) return;

    String currentText = "";
    ScriptTab tab = this.openTabs.get(this.documentKey(definition));
    if (tab != null) {
      currentText = tab.getText();
    } else if (oldPath != null && Files.exists(oldPath)) {
      try {
        currentText = Files.readString(oldPath);
      } catch (IOException ignored) {}
    }

    String updatedText = currentText
        .replaceAll("\\b" + Pattern.quote(oldClassName) + "\\b", java.util.regex.Matcher.quoteReplacement(newClassName))
        .replace("id = \"" + oldClassName + "\"", "id = \"" + newClassName + "\"");

    definition.setId(newClassName);
    definition.setName(newClassName);
    definition.setImplementation(newClassName);
    definition.setSource(newSource);

    try {
      if (newPath.getParent() != null) Files.createDirectories(newPath.getParent());
      Files.writeString(newPath, updatedText);
      if (oldPath != null && !oldPath.equals(newPath) && Files.exists(oldPath)) {
        Files.deleteIfExists(oldPath);
      }
    } catch (IOException error) {
      log.log(Level.SEVERE, "Failed to rename script file on disk", error);
      return;
    }

    if (tab != null) {
      this.openTabs.remove(tab.key);
      tab.key = this.documentKey(definition);
      this.openTabs.put(tab.key, tab);
      tab.setText(updatedText);
      tab.updateTabTitle();
      if (this.monacoTab == tab && this.monaco != null) {
        this.monaco.open(newPath, updatedText, definition);
      }
    }

    refreshScripts();
    selectTreeNode(newClassName);
    Editor.instance().save(true);
  }

  private boolean scriptIdExists(String id) {
    return Editor.instance().getGameFile().getScripts().stream().anyMatch(candidate -> candidate.getId().equals(id));
  }

  private static String defaultSource(ScriptDefinition definition, String className, ScriptKind kind) {
    String packageName = ScriptSourcePaths.derivePackageName(
        Editor.instance().getProjectModel(), definition.getSource());
    String packageHeader = (packageName != null && !packageName.isBlank()) ? "package " + packageName + ";\n\n" : "";

    if (kind == ScriptKind.GAME) {
      String base = "GameScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.GameScript" : "GameScript";
      return packageHeader
        + "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.input.Input;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n"
        + "import java.awt.event.KeyEvent;\n\n"
        + "/**\n"
        + " * Global game lifecycle script controller (entry point).\n"
        + " *\n"
        + " * <p>Responsibilities:\n"
        + " * <ul>\n"
        + " *   <li>Initialize persistent game state: {@code globals.put(\"score\", 0)}</li>\n"
        + " *   <li>Load starting map: {@code loadMap(\"map1\")}</li>\n"
        + " *   <li>Play background soundtracks: {@code playMusic(\"theme\")}</li>\n"
        + " *   <li>Register global inputs: pause, restart, hotkeys</li>\n"
        + " * </ul>\n"
        + " */\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.GAME)\n"
        + "public class " + className + " extends " + base + " {\n"
        + "  @Override\n"
        + "  public void onStarted() {\n"
        + "    // 1. Initialize persistent global variables across maps\n"
        + "    globals.put(\"score\", 0);\n"
        + "    globals.put(\"lives\", 3);\n\n"
        + "    // 2. Play background soundtrack (optional)\n"
        + "    // playMusic(\"bg_music\");\n\n"
        + "    // 3. Load initial map (if not already loaded by launcher/editor)\n"
        + "    if (Game.world().environment() == null) {\n"
        + "      // loadMap(\"level1\");\n"
        + "    }\n\n"
        + "    // 4. Global input shortcuts (e.g. Pause on ESC)\n"
        + "    Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, event -> {\n"
        + "      // Toggle pause or open menu\n"
        + "    });\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Global game-level update loop (runs continuously across all maps)\n"
        + "  }\n"
        + "}\n";
    }
    if (kind == ScriptKind.ENVIRONMENT) {
      String base = "EnvironmentScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.EnvironmentScript" : "EnvironmentScript";
      return packageHeader
        + "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.entities.*;\n"
        + "import de.gurkenlabs.litiengine.environment.Environment;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "/**\n"
        + " * Map environment script controller.\n"
        + " *\n"
        + " * <p>Responsibilities:\n"
        + " * <ul>\n"
        + " *   <li>Map initialization & wave spawning on {@code onLoaded()}</li>\n"
        + " *   <li>Objective tracking: {@code onEntityRemoved(IEntity)}</li>\n"
        + " *   <li>Level clear transitions & ambient cinematics</li>\n"
        + " * </ul>\n"
        + " */\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENVIRONMENT)\n"
        + "public class " + className + " extends " + base + " {\n"
        + "  @Override\n"
        + "  public void onLoaded() {\n"
        + "    // Map is loaded and active. Announce level start:\n"
        + "    context().ui().showBanner(\"LEVEL START\", \"Defeat all enemies!\", 2500);\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  protected void onEntityRemoved(IEntity entity) {\n"
        + "    // Check if level objective is complete\n"
        + "    var remainingMonsters = EntityQuery.in(environment(), Creature.class).alive().list();\n"
        + "    if (remainingMonsters.isEmpty()) {\n"
        + "      context().ui().showBanner(\"VICTORY\", \"Stage Cleared!\", 3000);\n"
        + "    }\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Map-level update logic\n"
        + "  }\n"
        + "}\n";
    }
    String targetType = definition.getTargetType() != null ? definition.getTargetType() : "de.gurkenlabs.litiengine.entities.Creature";
    String targetSimple = targetType.substring(targetType.lastIndexOf('.') + 1);
    String base = "Creature".equals(targetSimple)
        ? ("CreatureScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.CreatureScript" : "CreatureScript")
        : ("EntityScript<" + targetSimple + ">");
    return packageHeader
      + "import de.gurkenlabs.litiengine.*;\n"
      + "import " + targetType + ";\n"
      + "import de.gurkenlabs.litiengine.entities.*;\n"
      + "import de.gurkenlabs.litiengine.resources.*;\n"
      + "import de.gurkenlabs.litiengine.scripting.*;\n"
      + "import java.awt.Color;\n\n"
      + "/**\n"
      + " * Entity script controller for {@link " + targetSimple + "}.\n"
      + " *\n"
      + " * <p>Responsibilities:\n"
      + " * <ul>\n"
      + " *   <li>AI movement & navigation: {@code moveTowards(target)}</li>\n"
      + " *   <li>Combat abilities & projectiles: {@code createAbility()}, {@code spawnProjectile()}</li>\n"
      + " *   <li>Reactions: {@code onHit(event)}, {@code onDeath(entity, hitEvent)}</li>\n"
      + " * </ul>\n"
      + " */\n"
      + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENTITY, target = " + targetSimple + ".class)\n"
      + "public class " + className + " extends " + base + " {\n"
      + "  @Override\n"
      + "  public void onLoaded() {\n"
      + "    // Entity spawned and ready in the environment\n"
      + "  }\n\n"
      + "  @Override\n"
      + "  public void update() {\n"
      + "    if (isDead()) return;\n\n"
      + "    // Entity AI / movement logic\n"
      + "  }\n\n"
      + "  @Override\n"
      + "  protected void onHit(EntityHitEvent event) {\n"
      + "    // Display floating combat damage number\n"
      + "    context().ui().floatText(\"-\" + event.getDamage(), host(), Color.RED);\n"
      + "  }\n\n"
      + "  @Override\n"
      + "  protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {\n"
      + "    // Entity mortality handling\n"
      + "    remove();\n"
      + "  }\n"
      + "}\n";
  }

  static String synchronizeDeclaration(String source, ScriptDefinition definition) {
    if (source == null || source.isBlank() || definition == null) return source;
    String className = extractClassName(source);
    if (className == null || className.isBlank()) className = definition.getImplementation();

    String annotation = "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType." + definition.getHost()
      + (definition.getHost() == ScriptHostType.ENTITY && definition.getTargetType() != null
        ? ", target = " + definition.getTargetType() + ".class" : "") + ")";
    String updated = source.replaceFirst("(?s)@ScriptInfo\\s*\\(.*?\\)", Matcher.quoteReplacement(annotation));
    String base = scriptBase(definition, className);
    updated = updated.replaceFirst("(?m)(\\bclass\\s+[A-Za-z_$][\\w$]*\\s+extends\\s+)[\\w.$<>?]+",
      "$1" + Matcher.quoteReplacement(base));
    if (definition.getHost() == ScriptHostType.GAME) {
      return updated.replaceAll("\\bvoid\\s+onLoaded\\s*\\(", "void onStarted(")
        .replaceAll("\\bvoid\\s+onUnloaded\\s*\\(", "void onStopped(");
    }
    return updated.replaceAll("\\bvoid\\s+onStarted\\s*\\(", "void onLoaded(")
      .replaceAll("\\bvoid\\s+onStopped\\s*\\(", "void onUnloaded(");
  }

  private static String scriptBase(ScriptDefinition definition, String className) {
    String base = switch (definition.getHost()) {
      case GAME -> "GameScript";
      case ENVIRONMENT -> "EnvironmentScript";
      case ENTITY -> Creature.class.getName().equals(definition.getTargetType())
        ? "CreatureScript" : "EntityScript<" + Objects.requireNonNullElse(
          definition.getTargetType(), "de.gurkenlabs.litiengine.entities.IEntity") + ">";
    };

    String simpleBase = base.contains("<") ? base.substring(0, base.indexOf('<')) : base;
    if (className != null && className.equals(simpleBase)) {
      return "de.gurkenlabs.litiengine.scripting." + base;
    }
    return base;
  }

  private static String displayName(ScriptDefinition definition) {
    return definition.getName() == null || definition.getName().isBlank() ? definition.getId() : definition.getName();
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
      if (activeTab() == this) refreshOutline(this);
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
          this.definition.setImplementation(declaredFqcn);
          if (simpleClassName != null && !simpleClassName.isBlank() && !simpleClassName.equals(this.definition.getName())) {
            this.renameToClass(simpleClassName);
          }
        }

        if (this.path.getParent() != null) Files.createDirectories(this.path.getParent());
        Files.writeString(this.path, this.getText());
        this.loadedTime = Files.getLastModifiedTime(this.path);
        this.dirty = false;
        this.updateTabTitle();
        if (!this.projectSource) UndoManager.instance().recordChanges();
        return true;
      } catch (IOException e) {
        setStatus("Could not save source: " + e.getMessage(), true);
        return false;
      }
    }

    private void renameToClass(String newClassName) {
      Path oldPath = resolveSource(this.definition.getSource());

      String newSourceRel = ScriptSourcePaths.rename(
        this.definition.getSource(), this.definition.getLanguage(), newClassName);
      Path newPath = resolveSource(newSourceRel);

      if (oldPath != null && Files.exists(oldPath) && newPath != null && !oldPath.equals(newPath)) {
        try {
          if (newPath.getParent() != null) Files.createDirectories(newPath.getParent());
          Files.move(oldPath, newPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ignored) {}
      }

      this.definition.setId(newClassName);
      this.definition.setName(newClassName);
      this.definition.setImplementation(newClassName);
      this.definition.setSource(newSourceRel);
      this.path = newPath;
      String updatedText = ScriptWorkspacePanel.synchronizeDeclaration(this.getText(), this.definition);
      if (!Objects.equals(updatedText, this.getText())) {
        this.setText(updatedText);
      }

      ScriptWorkspacePanel.this.openTabs.remove(this.key);
      this.key = ScriptWorkspacePanel.this.documentKey(this.definition);
      ScriptWorkspacePanel.this.openTabs.put(this.key, this);

      Game.scripts().setDefinitions(Editor.instance().getGameFile().getScripts());
      UI.getAssetController().refresh();
      ScriptWorkspacePanel.this.refreshScripts();
      ScriptWorkspacePanel.this.selectTreeNode(newClassName);
      if (ScriptWorkspacePanel.this.monacoTab == this && ScriptWorkspacePanel.this.monaco != null) {
        ScriptWorkspacePanel.this.monaco.open(newPath, this.getText(), this.definition);
      }
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

  private record ScriptTreeItem(String label, ScriptDefinition definition) {
    @Override public String toString() { return this.label; }
  }

  private final class ScriptTreeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel();
    private final JLabel iconLabel = new JLabel();
    private final JLabel textLabel = new JLabel();

    ScriptTreeRenderer() {
      this.panel.setLayout(new javax.swing.BoxLayout(this.panel, javax.swing.BoxLayout.X_AXIS));
      this.panel.setOpaque(false);
      this.iconLabel.setOpaque(false);
      this.textLabel.setOpaque(false);
      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.textLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.textLabel.setFont(Style.getDefaultFont());
      this.panel.add(this.iconLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(4));
      this.panel.add(this.textLabel);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptTreeItem item) {
        this.textLabel.setText(item.label());
        javax.swing.Icon icon;
        if (item.definition() != null) {
          icon = Icons.SCRIPT_16;
        } else if ("Project Sources".equals(item.label())) {
          icon = Icons.FOLDER_OPEN_16;
        } else {
          icon = Icons.PACKAGE_16;
        }
        this.iconLabel.setIcon(icon);
        this.textLabel.setForeground(selected ? Color.WHITE : Style.text());
      } else {
        this.textLabel.setText(Objects.toString(value, ""));
        this.iconLabel.setIcon(null);
        this.textLabel.setForeground(selected ? Color.WHITE : Style.text());
      }
      this.panel.setOpaque(false);
      return this.panel;
    }
  }

  private static final class OutlineTreeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel();
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();

    OutlineTreeRenderer() {
      this.panel.setLayout(new javax.swing.BoxLayout(this.panel, javax.swing.BoxLayout.X_AXIS));
      this.panel.setOpaque(false);
      this.iconLabel.setOpaque(false);
      this.nameLabel.setOpaque(false);
      this.detailLabel.setOpaque(false);
      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.detailLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.panel.add(this.iconLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(4));
      this.panel.add(this.nameLabel);
      this.panel.add(this.detailLabel);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptOutline.Symbol symbol) {
        this.iconLabel.setIcon(switch (symbol.kind()) {
          case CLASS -> Icons.SYMBOL_CLASS_16;
          case METHOD -> Icons.SYMBOL_METHOD_16;
          case FIELD -> Icons.SYMBOL_FIELD_16;
          case GROUP -> Icons.SYMBOL_GROUP_16;
          default -> Icons.SYMBOL_DEPENDENCY_16;
        });

        this.nameLabel.setText(symbol.name());
        this.nameLabel.setFont(Style.getDefaultFont().deriveFont(
          symbol.kind() == ScriptOutline.Kind.CLASS || symbol.kind() == ScriptOutline.Kind.GROUP ? Font.BOLD : Font.PLAIN, 12f));

        if (symbol.detail() != null && !symbol.detail().isBlank()) {
          this.detailLabel.setText("  " + symbol.detail());
          this.detailLabel.setVisible(true);
        } else {
          this.detailLabel.setText("");
          this.detailLabel.setVisible(false);
        }

        if (selected) {
          this.nameLabel.setForeground(Color.WHITE);
          this.detailLabel.setForeground(new Color(200, 210, 225));
        } else {
          if (symbol.kind() == ScriptOutline.Kind.CLASS) {
            this.nameLabel.setForeground(Color.WHITE);
          } else if (symbol.kind() == ScriptOutline.Kind.GROUP) {
            this.nameLabel.setForeground(Style.mutedText());
          } else {
            this.nameLabel.setForeground(Style.text());
          }
          this.detailLabel.setForeground(new Color(130, 145, 165));
        }
      } else {
        this.nameLabel.setText(Objects.toString(value, ""));
        this.detailLabel.setText("");
        this.detailLabel.setVisible(false);
        this.iconLabel.setIcon(null);
      }
      this.panel.setOpaque(false);
      return this.panel;
    }
  }

  private static final class GlobalApiTreeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel();
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();

    GlobalApiTreeRenderer() {
      this.panel.setLayout(new javax.swing.BoxLayout(this.panel, javax.swing.BoxLayout.X_AXIS));
      this.panel.setOpaque(false);
      this.iconLabel.setOpaque(false);
      this.nameLabel.setOpaque(false);
      this.detailLabel.setOpaque(false);
      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.detailLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.panel.setBorder(BorderFactory.createEmptyBorder(0, 2, 0, 4));
      this.panel.add(this.iconLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(5));
      this.panel.add(this.nameLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(6));
      this.panel.add(this.detailLabel);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      if (value instanceof DefaultMutableTreeNode node) {
        Object userObj = node.getUserObject();
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
          this.nameLabel.setForeground(selected ? Color.WHITE : Style.COLOR_TEXT);

          this.detailLabel.setText(item.description());
          this.detailLabel.setFont(Style.getDefaultFont().deriveFont(10.5f));
          this.detailLabel.setForeground(selected ? new Color(200, 210, 240) : Style.COLOR_SUBTEXT);
          this.detailLabel.setVisible(true);

          this.panel.setToolTipText("<html><b>" + item.label() + "</b>: " + item.description() + "<br><code>" + item.snippet().replace("<", "&lt;").replace(">", "&gt;").replace("\n", "<br>") + "</code><br><i>Double-click to insert</i></html>");
        } else {
          // Group Category Node
          this.iconLabel.setIcon(expanded ? Icons.FOLDER_OPEN_16 : Icons.GROUP_16);
          this.nameLabel.setText(Objects.toString(userObj, ""));
          this.nameLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
          this.nameLabel.setForeground(selected ? Color.WHITE : Style.COLOR_SUBTEXT);
          this.detailLabel.setText("");
          this.detailLabel.setVisible(false);
          this.panel.setToolTipText(null);
        }
      }
      this.panel.setOpaque(false);
      return this.panel;
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
