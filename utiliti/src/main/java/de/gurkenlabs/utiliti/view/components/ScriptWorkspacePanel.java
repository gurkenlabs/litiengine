package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.GradleScriptProjectSupport;
import de.gurkenlabs.utiliti.controller.IntellijIntegration;
import de.gurkenlabs.utiliti.controller.ProjectLaunchRequest;
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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.logging.Level;
import java.util.logging.Logger;
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

  private final DefaultMutableTreeNode scriptsRoot = new DefaultMutableTreeNode("Scripts");
  private final DefaultTreeModel scriptsModel = new DefaultTreeModel(this.scriptsRoot);
  private final JTree scripts = UI.createStyledTree(this.scriptsModel);
  private final JTextField search = new JTextField();
  private final DefaultMutableTreeNode outlineRoot = new DefaultMutableTreeNode("Outline");
  private final DefaultTreeModel outlineModel = new DefaultTreeModel(this.outlineRoot);
  private final JTree outline = UI.createStyledTree(this.outlineModel);
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
  private final Timer externalChangeTimer = new Timer(900, event -> this.checkExternalChanges());
  private MonacoScriptEditor monaco;
  private ScriptTab monacoTab;
  private ScriptTab conflictTab;
  private final ScriptDebuggerPanel debuggerPanel = new ScriptDebuggerPanel();
  private final List<ScriptBreakpoint> breakpoints = new java.util.concurrent.CopyOnWriteArrayList<>();
  private final Timer breakpointSyncTimer = new Timer(300, e -> this.syncBreakpoints());
  private final java.util.concurrent.ExecutorService breakpointSyncExecutor = java.util.concurrent.Executors.newSingleThreadExecutor();
  private JdiScriptDebuggerBackend debugger;
  private String executionScriptId;
  private int executionLine;
  private List<ScriptDebugSnapshot.Variable> executionVariables = List.of();
  private boolean projectLaunchPending;
  private boolean restartRequested;
  private Consumer<ScriptDefinition> selectionListener = ignored -> {};

  public ScriptWorkspacePanel() {
    super(new BorderLayout());
    this.setBackground(Style.background());
    this.add(this.createConflictBar(), BorderLayout.NORTH);

    JSplitPane explorer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.createOutline(), this.createGlobalsPanel());
    UI.configureSplitPane(explorer);
    explorer.setBackground(Style.COLOR_BG);
    explorer.setResizeWeight(0.5);
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

    try {
      this.monaco = new MonacoScriptEditor();
      this.monaco.onChanged(text -> {
        if (this.monacoTab != null) this.monacoTab.setTextFromMonaco(text);
      });
      this.monaco.onSave(() -> {
        if (this.monacoTab != null && this.monacoTab.save()) {
          this.setStatus("Saved " + this.monacoTab.definition.getSource(), false);
        }
      });
      this.monaco.onAnalysis(this::showAnalysis);
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
    } catch (IOException error) {
      this.monaco = null;
      this.setStatus("Monaco is unavailable: " + error.getMessage(), true);
    }

    this.tabs.putClientProperty("JTabbedPane.noContentBorder", Boolean.TRUE);
    this.tabs.putClientProperty("JTabbedPane.hasFullBorder", Boolean.FALSE);
    this.tabs.putClientProperty("JTabbedPane.contentInsets", new java.awt.Insets(0, 0, 0, 0));
    this.tabs.putClientProperty("JTabbedPane.tabAreaInsets", new java.awt.Insets(0, 0, 0, 0));

    this.mainEditorArea.add(this.tabs, BorderLayout.NORTH);
    if (this.monaco != null) {
      this.mainEditorArea.add(this.monaco, BorderLayout.CENTER);
    }

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
    statusBar.add(this.status, BorderLayout.WEST);
    statusBar.add(this.caretStatus, BorderLayout.EAST);
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
        this.focusOrOpenFirstScript();
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
    this.focusOrOpenFirstScript();
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
    if (Editor.instance().getGameFile() != null) {
      String query = this.search.getText().strip().toLowerCase(Locale.ROOT);
      Editor.instance().getGameFile().getScripts().stream()
        .filter(definition -> query.isEmpty() || displayName(definition).toLowerCase(Locale.ROOT).contains(query)
          || Objects.toString(definition.getSource(), "").toLowerCase(Locale.ROOT).contains(query))
        .sorted(Comparator.comparing(ScriptWorkspacePanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .forEach(this::insertScriptNode);
    }
    this.scriptsModel.reload();
    for (int row = 0; row < this.scripts.getRowCount(); row++) this.scripts.expandRow(row);
    if (selectedId != null) {
      this.selectTreeNode(selectedId);
    } else {
      this.focusOrOpenFirstScript();
    }
    this.refreshGlobals();
  }

  public void open(ScriptDefinition definition) {
    if (definition == null) return;
    ScriptTab tab = this.openTabs.computeIfAbsent(definition.getId(), ignored -> {
      ScriptTab created = new ScriptTab(definition);
      this.tabs.addTab(displayName(definition), Icons.SCRIPT_16, created, definition.getSource());
      this.tabs.setTabComponentAt(this.tabs.indexOfComponent(created), this.createTabHeader(created));
      return created;
    });
    this.tabs.setSelectedComponent(tab);
    this.selectTreeNode(definition.getId());
    this.activeTabChanged();
  }

  public void saveActive() {
    ScriptTab tab = this.activeTab();
    if (tab != null && tab.save()) this.setStatus("Saved " + tab.definition.getSource(), false);
  }

  /** Applies inspector metadata to the active definition and its source declaration. */
  public void updateActiveMetadata(String name, ScriptHostType host, String targetType) {
    ScriptTab tab = this.activeTab();
    if (tab == null || host == null) return;
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
    this.setStatus("Reloaded " + tab.definition.getSource(), false);
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

    if (mode == ProjectLaunchRequest.Mode.DEBUG) {
      UI.showDebuggerTab();
      this.appendOutput("Saving project and preparing debugger...");
    } else {
      UI.showConsoleTab();
      this.appendOutput("Resolving Gradle project model and launching...");
    }

    this.projectLaunchPending = true;
    UI.updateRunControlStates(true);

    Thread.ofVirtual().name("utiliti-project-launch").start(() -> {
      try {
        if (!this.saveAllScripts()) return;
        if (Editor.instance().getCurrentResourceFile() != null) {
          Editor.instance().save(false);
        }

        List<ScriptDefinition> debugDefinitions = mode == ProjectLaunchRequest.Mode.DEBUG
            ? Editor.instance().getGameFile().getScripts().stream().map(ScriptDefinition::new).toList()
            : List.of();

        ProjectSession session = Editor.instance().runProject(mode);
        session.onOutput(line -> SwingUtilities.invokeLater(() -> this.appendOutput(line)));
        session.onStateChanged(
            state -> SwingUtilities.invokeLater(() -> this.projectStateChanged(session, state)));
        if (mode == ProjectLaunchRequest.Mode.DEBUG) {
          this.attachDebugger(debugDefinitions);
        }
      } catch (IOException error) {
        if (mode == ProjectLaunchRequest.Mode.DEBUG) {
          this.closeDebugger();
          Editor.instance().stopProject();
        }
        SwingUtilities.invokeLater(() -> {
          this.appendOutput("Could not start project: " + error.getMessage());
          this.setStatus("Could not start project: " + error.getMessage(), true);
        });
      } finally {
        SwingUtilities.invokeLater(() -> {
          this.projectLaunchPending = false;
          UI.updateRunControlStates(false);
        });
      }
    });
  }

  private void attachDebugger(List<ScriptDefinition> debugDefinitions) {
    this.closeDebugger();
    JdiScriptDebuggerBackend backend = new JdiScriptDebuggerBackend(new ScriptDebuggerBackend.Listener() {
      @Override
      public void stateChanged(ScriptDebuggerBackend.State state, String detail) {
        SwingUtilities.invokeLater(() -> debuggerPanel.updateState(state, detail));
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
      backend.attach("127.0.0.1", port, debugDefinitions);
      backend.setBreakpoints(this.currentProjectBreakpoints());
    } catch (IOException e) {
      log.log(Level.WARNING, "Failed to attach debugger on port " + port, e);
      SwingUtilities.invokeLater(() -> {
        this.appendOutput("Debugger attach failed: " + e.getMessage());
        this.setStatus("Debugger attach failed: " + e.getMessage(), true);
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
      this.executionScriptId = def == null ? null : def.getId();
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
    return Editor.instance().getGameFile().getScripts().stream().filter(definition -> {
      String implementation = definition.getImplementation();
      return implementation != null && (implementation.equals(className) || className.startsWith(implementation + "$"));
    }).findFirst().orElse(null);
  }

  private void replaceBreakpoints(ScriptDefinition definition, List<Integer> lines) {
    if (definition == null) return;
    String project = this.projectKey();
    String source = Objects.toString(definition.getSource(), "");
    List<Integer> normalized = lines == null ? List.of() : lines.stream()
        .filter(line -> line != null && line > 0).distinct().sorted().toList();
    List<Integer> existing = this.breakpoints.stream()
        .filter(item -> item.project().equals(project) && item.scriptId().equals(definition.getId()) && item.source().equals(source))
        .map(ScriptBreakpoint::line).sorted().toList();
    if (existing.equals(normalized)) return;
    this.breakpoints.removeIf(item -> item.project().equals(project)
        && item.scriptId().equals(definition.getId()) && item.source().equals(source));
    normalized.forEach(line -> this.breakpoints.add(new ScriptBreakpoint(project, definition.getId(), source, line, true)));
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
    String source = Objects.toString(definition.getSource(), "");
    List<Integer> lines = this.breakpoints.stream()
        .filter(item -> item.enabled() && item.project().equals(project)
            && item.scriptId().equals(definition.getId()) && item.source().equals(source))
        .map(ScriptBreakpoint::line).distinct().sorted().toList();
    int currentLine = definition.getId().equals(this.executionScriptId) ? this.executionLine : 0;
    List<ScriptDebugSnapshot.Variable> variables = currentLine > 0 ? this.executionVariables : List.of();
    this.monaco.setDebugState(lines, currentLine, variables);
  }

  private String projectKey() {
    Path project = Editor.instance().getProjectPath();
    return project == null ? "" : project.toAbsolutePath().normalize().toString();
  }

  public void stopProject() {
    ProjectSession session = Editor.instance().getProjectSession();
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
      case STARTING -> this.setStatus("Starting project...", false);
      case RUNNING -> this.setStatus("Project is running", false);
      case STOPPING -> this.setStatus("Stopping project...", false);
      case EXITED -> {
        int exitCode = session.exitCode().orElse(-1);
        this.appendOutput("Project exited with code " + exitCode + ".");
        this.setStatus("Project exited with code " + exitCode, exitCode != 0);
        if (this.restartRequested) {
          this.restartRequested = false;
          this.runProject();
        }
      }
      case FAILED -> {
        this.appendOutput("Project launch failed.");
        this.setStatus("Project launch failed", true);
        this.restartRequested = false;
      }
    }
    UI.updateRunControlStates();
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

    JPanel header = new JPanel(new BorderLayout(5, 0));
    header.add(sectionTitle("SCRIPTS"), BorderLayout.WEST);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
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

    actions.add(addBtn);
    actions.add(dupBtn);
    actions.add(deleteBtn);
    header.add(actions, BorderLayout.EAST);
    panel.add(header, BorderLayout.NORTH);

    JPanel content = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    content.setBackground(Style.COLOR_BG);
    header.setBackground(Style.COLOR_BG);
    panel.setBackground(Style.COLOR_BG);
    this.search.setFont(Style.getDefaultFont());
    this.search.putClientProperty("JTextField.placeholderText", "Search scripts...");
    RoundedSearchBox searchBox = new RoundedSearchBox(this.search, 200);
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
    panel.add(sectionTitle("OUTLINE"), BorderLayout.NORTH);
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

  private DefaultListModel<GlobalApiItem> globalsModel = new DefaultListModel<>();

  private JPanel createGlobalsPanel() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setBackground(Style.COLOR_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
    panel.add(sectionTitle("GLOBALS & APIS"), BorderLayout.NORTH);

    this.refreshGlobals();

    JList<GlobalApiItem> list = UI.createStyledList(this.globalsModel);
    list.setCellRenderer(new GlobalApiRenderer());
    list.setFixedCellHeight(26);
    list.setBackground(Style.COLOR_BG);
    list.setOpaque(false);
    list.setSelectionBackground(Style.selection());

    list.addMouseListener(new java.awt.event.MouseAdapter() {
      @Override public void mouseClicked(java.awt.event.MouseEvent event) {
        if (event.getClickCount() == 2 && list.getSelectedValue() != null) {
          GlobalApiItem item = list.getSelectedValue();
          insertTextToActiveScript(item.snippet());
        }
      }
    });

    panel.add(createBorderlessScrollPane(list), BorderLayout.CENTER);
    return panel;
  }

  public void refreshGlobals() {
    this.globalsModel.clear();

    // 1. Builtin Globals & Services
    this.globalsModel.addElement(new GlobalApiItem("host()", "host()", "Entity / Creature script instance", "h"));
    this.globalsModel.addElement(new GlobalApiItem("environment()", "environment()", "Active map environment", "e"));
    this.globalsModel.addElement(new GlobalApiItem("context()", "context()", "Script context & properties", "c"));
    this.globalsModel.addElement(new GlobalApiItem("globals", "globals", "Shared ScriptGlobals store", "g"));
    this.globalsModel.addElement(new GlobalApiItem("Game.world()", "Game.world()", "Map & world entity manager", "m"));
    this.globalsModel.addElement(new GlobalApiItem("Game.loop()", "Game.loop()", "Main loop & frame updates", "m"));
    this.globalsModel.addElement(new GlobalApiItem("Game.audio()", "Game.audio()", "Sound & music engine", "m"));
    this.globalsModel.addElement(new GlobalApiItem("Game.physics()", "Game.physics()", "Collision & physics engine", "m"));
    this.globalsModel.addElement(new GlobalApiItem("Game.graphics()", "Game.graphics()", "Render engine & camera", "m"));
    this.globalsModel.addElement(new GlobalApiItem("EntityQuery", "EntityQuery.in(environment(), Creature.class)", "Fluent entity finder", "q"));

    // 2. Active Map Entities
    if (Game.world() != null && Game.world().environment() != null) {
      for (de.gurkenlabs.litiengine.entities.IEntity entity : Game.world().environment().getEntities()) {
        String name = entity.getName();
        String identifier = (name != null && !name.isBlank()) ? name : String.valueOf(entity.getMapId());
        String snippet = "environment().get(\"" + identifier + "\")";
        String typeName = entity.getClass().getSimpleName();
        String label = (name != null && !name.isBlank()) ? name : typeName + " #" + entity.getMapId();
        this.globalsModel.addElement(new GlobalApiItem(label, snippet, typeName + " on active map", "e"));
      }
    }

    // 3. Registered ScriptGlobals Entries
    if (Game.scripts() != null && Game.scripts().globals() != null) {
      for (Map.Entry<String, Object> entry : Game.scripts().globals().getEntries().entrySet()) {
        String key = entry.getKey();
        Object val = entry.getValue();
        String typeName = val == null ? "Object" : val.getClass().getSimpleName();
        this.globalsModel.addElement(new GlobalApiItem(key, "globals.get(\"" + key + "\")", "Global variable (" + typeName + ")", "g"));
      }
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

  private JPanel createTabHeader(ScriptTab tab) {
    JPanel header = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
    header.setOpaque(false);
    JLabel label = new JLabel(displayName(tab.definition), Icons.SCRIPT_16, SwingConstants.LEADING);
    label.setFont(Style.getDefaultFont());
    tab.title = label;
    JButton close = new JButton("×");
    close.setFont(Style.getDefaultFont().deriveFont(12f));
    close.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
    close.setContentAreaFilled(false);
    close.setFocusable(false);
    close.addActionListener(event -> this.closeTab(tab));
    header.add(label);
    header.add(close);
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
    this.openTabs.remove(tab.definition.getId());
    this.tabs.remove(tab);
    this.activeTabChanged();
  }

  private void activeTabChanged() {
    ScriptTab active = this.activeTab();
    if (active != null && this.monaco != null && !this.monaco.isUnavailable()) {
      this.monacoTab = active;
      this.monaco.open(active.path, active.getText(), active.definition);
      if (this.monaco.isReady()) this.monaco.focusEditor();
      this.monaco.notifyMoved();
      this.mainEditorArea.revalidate();
      this.mainEditorArea.repaint();
    } else if (active == null) {
      this.monacoTab = null;
      if (this.monaco != null && !this.monaco.isUnavailable()) {
        this.monaco.open(null, "", null);
      }
    }
    ScriptDefinition definition = active == null ? null : active.definition;
    this.selectionListener.accept(definition);
    this.showDiagnostics(definition);
    this.refreshOutline(active);
    this.updateCaretStatus(active);
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

    if (target == null && activeTab() != null) {
      target = activeTab().definition;
    }

    if (target != null) {
      this.open(target);
    }
    if (this.monaco != null && this.monaco.isReady()) {
      this.monaco.revealPosition(line, column);
    }
  }

  private void showAnalysis(de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Analysis analysis) {
    if (this.monacoTab != null && this.monacoTab.definition != null) {
      this.projectDiagnostics.put(this.monacoTab.definition.getId(), new ArrayList<>(analysis.diagnostics()));
    }
    refreshProblemsTable();
  }

  private void refreshProblemsTable() {
    this.problemsModel.setRowCount(0);
    this.scriptErrorStates.clear();

    Map<String, List<ScriptDiagnostic>> allDiagnostics = new LinkedHashMap<>();
    for (Map.Entry<String, List<ScriptDiagnostic>> entry : this.projectDiagnostics.entrySet()) {
      allDiagnostics.put(entry.getKey(), new ArrayList<>(entry.getValue()));
    }
    for (ScriptDiagnostic diag : Game.scripts().getDiagnostics()) {
      if (diag.scriptId() != null && !this.projectDiagnostics.containsKey(diag.scriptId())) {
        allDiagnostics.computeIfAbsent(diag.scriptId(), k -> new ArrayList<>()).add(diag);
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
    relative = relative.replaceFirst("^(?:.*?/)?(?:src/main|scripts)/(?:java|groovy)/", "");
    String[] parts = relative.split("/");
    for (int i = 0; i < Math.max(0, parts.length - 1); i++) {
      if (parts[i].isBlank()) continue;
      parent = childFolder(parent, parts[i]);
    }
    parent.add(new DefaultMutableTreeNode(new ScriptTreeItem(displayName(definition), definition)));
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

  public void createScript() {
    createScript(ScriptKind.ENTITY);
  }

  public void createScript(ScriptKind kind) {
    createScript(kind, kind == ScriptKind.ENTITY ? Creature.class : null);
  }

  public void createScript(ScriptKind kind, Class<?> targetClass) {
    if (Editor.instance().getGameFile() == null || Editor.instance().getProjectPath() == null) return;
    String targetType = targetClass != null ? targetClass.getName() : (kind == ScriptKind.ENTITY ? Creature.class.getName() : null);
    String targetSimple = targetClass != null ? targetClass.getSimpleName() : "Creature";
    String prefix = switch (kind) {
      case GAME -> "GameScript";
      case ENVIRONMENT -> "EnvironmentScript";
      case ENTITY -> "Creature".equals(targetSimple) ? "CreatureScript" : targetSimple + "Script";
    };
    ScriptHostType hostType = switch (kind) {
      case GAME -> ScriptHostType.GAME;
      case ENVIRONMENT -> ScriptHostType.ENVIRONMENT;
      case ENTITY -> ScriptHostType.ENTITY;
    };

    int suffix = 1;
    String id;
    String className;
    Path source;
    do {
      className = suffix == 1 ? prefix : prefix + suffix;
      id = className;
      source = resolveSource(ScriptSourcePaths.create("java", className));
      suffix++;
    } while (source != null && (Files.exists(source) || scriptIdExists(id)));
    if (source == null) return;

    ScriptDefinition definition = new ScriptDefinition(className, "java", ScriptSourcePaths.create("java", className),
      className, hostType);
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
    } catch (IOException e) {
      this.setStatus("Could not create script: " + e.getMessage(), true);
    }
  }

  public static String extractClassName(String source) {
    if (source == null || source.isBlank()) return null;
    var matcher = Pattern.compile("(?m)^\\s*(?:public\\s+)?class\\s+([A-Za-z_$][\\w$]*)").matcher(source);
    return matcher.find() ? matcher.group(1) : null;
  }

  public void deleteScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;
    int choice = JOptionPane.showConfirmDialog(this,
      "Are you sure you want to delete script '" + displayName(definition) + "'?\nThis will remove the file from disk.",
      "Delete Script", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
    if (choice != JOptionPane.YES_OPTION) return;

    ScriptTab tab = this.openTabs.get(definition.getId());
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

  public void duplicateScript(ScriptDefinition definition) {
    if (definition == null || Editor.instance().getGameFile() == null) return;
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
    if (source == null) return;

    ScriptDefinition dup = new ScriptDefinition(id, definition.getLanguage(),
      ScriptSourcePaths.rename(definition.getSource(), definition.getLanguage(), className),
      className, definition.getHost());
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
      JMenuItem dupItem = new JMenuItem("Duplicate Script", Icons.COPY_16);
      dupItem.addActionListener(evt -> duplicateScript(selected));
      menu.add(dupItem);

      JMenuItem renameItem = new JMenuItem("Rename Class...", Icons.RENAME_16);
      renameItem.addActionListener(evt -> renameScript(selected));
      menu.add(renameItem);

      JMenuItem deleteItem = new JMenuItem("Delete Script", Icons.DELETE_16);
      deleteItem.addActionListener(evt -> deleteScript(selected));
      menu.add(deleteItem);

      JMenuItem openIdeItem = new JMenuItem("Open in IDE", Icons.EXTERNAL_16);
      openIdeItem.addActionListener(evt -> openActiveExternally());
      menu.add(openIdeItem);
    }
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  public void renameScript(ScriptDefinition definition) {
    if (definition == null) return;
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
    ScriptTab tab = this.openTabs.get(definition.getId());
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
      this.openTabs.remove(oldClassName);
      this.openTabs.put(newClassName, tab);
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
    if (kind == ScriptKind.GAME) {
      String base = "GameScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.GameScript" : "GameScript";
      return "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "/**\n"
        + " * Global game lifecycle script controller.\n"
        + " * Access global game state via {@code globals.put(\"key\", value)}.\n"
        + " */\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.GAME)\n"
        + "public class " + className + " extends " + base + " {\n"
        + "  @Override\n"
        + "  public void onStarted() {\n"
        + "    // The game loop is active.\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Global game-level script logic.\n"
        + "  }\n"
        + "}\n";
    }
    if (kind == ScriptKind.ENVIRONMENT) {
      String base = "EnvironmentScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.EnvironmentScript" : "EnvironmentScript";
      return "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.environment.Environment;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "/**\n"
        + " * Map environment script controller.\n"
        + " * Use {@code environment()} to query entities, triggers, and map properties.\n"
        + " */\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENVIRONMENT)\n"
        + "public class " + className + " extends " + base + " {\n"
        + "  @Override\n"
        + "  public void onLoaded() {\n"
        + "    // Map / Environment loaded.\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Environment-level script logic.\n"
        + "  }\n"
        + "}\n";
    }
    String targetType = definition.getTargetType() != null ? definition.getTargetType() : "de.gurkenlabs.litiengine.entities.Creature";
    String targetSimple = targetType.substring(targetType.lastIndexOf('.') + 1);
    String base = "Creature".equals(targetSimple)
        ? ("CreatureScript".equals(className) ? "de.gurkenlabs.litiengine.scripting.CreatureScript" : "CreatureScript")
        : ("EntityScript<" + targetSimple + ">");
    return "import de.gurkenlabs.litiengine.*;\n"
      + "import " + targetType + ";\n"
      + "import de.gurkenlabs.litiengine.entities.*;\n"
      + "import de.gurkenlabs.litiengine.resources.*;\n"
      + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
      + "/**\n"
      + " * Entity script controller for {@link " + targetSimple + "}.\n"
      + " * Use {@code host()} to access entity attributes and behavior.\n"
      + " */\n"
      + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENTITY, target = " + targetSimple + ".class)\n"
      + "public class " + className + " extends " + base + " {\n"
      + "  @Override\n"
      + "  public void onLoaded() {\n"
      + "    // Entity and environment ready.\n"
      + "  }\n\n"
      + "  @Override\n"
      + "  public void update() {\n"
      + "    // Entity-level script logic.\n"
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
    private String text = "";
    private Path path;
    private FileTime loadedTime;
    private boolean dirty;
    private int caretLine = 1;
    private int caretColumn = 1;
    private JLabel title;

    private ScriptTab(ScriptDefinition definition) {
      this.definition = definition;
      this.path = resolveSource(definition.getSource());
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

    private boolean save() {
      if (this.path == null) return false;
      try {
        if (Files.exists(this.path) && this.loadedTime != null && !Files.getLastModifiedTime(this.path).equals(this.loadedTime)) {
          setStatus("Source changed outside utiLITI. Reload it before saving.", true);
          return false;
        }

        String currentText = this.getText();
        String declaredClass = ScriptWorkspacePanel.extractClassName(currentText);
        if (declaredClass != null && !declaredClass.isBlank() && !declaredClass.equals(this.definition.getImplementation())) {
          this.renameToClass(declaredClass);
        }

        if (this.path.getParent() != null) Files.createDirectories(this.path.getParent());
        Files.writeString(this.path, this.getText());
        this.loadedTime = Files.getLastModifiedTime(this.path);
        this.dirty = false;
        this.updateTabTitle();
        UndoManager.instance().recordChanges();
        return true;
      } catch (IOException e) {
        setStatus("Could not save source: " + e.getMessage(), true);
        return false;
      }
    }

    private void renameToClass(String newClassName) {
      String oldId = this.definition.getId();
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

      ScriptWorkspacePanel.this.openTabs.remove(oldId);
      ScriptWorkspacePanel.this.openTabs.put(newClassName, this);

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
      Path root = Editor.instance().getProjectPath().getParent().toAbsolutePath().normalize();
      Path configured = Path.of(source);
      Path resolved = (configured.isAbsolute() ? configured : root.resolve(configured)).toAbsolutePath().normalize();
      return resolved.startsWith(root) ? resolved : null;
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
        this.iconLabel.setIcon(item.definition() != null ? Icons.SCRIPT_16 : Icons.SYMBOL_GROUP_16);
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

  private static final class GlobalApiRenderer implements javax.swing.ListCellRenderer<GlobalApiItem> {
    private final JPanel panel = new JPanel();
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel descLabel = new JLabel();

    GlobalApiRenderer() {
      this.panel.setLayout(new javax.swing.BoxLayout(this.panel, javax.swing.BoxLayout.X_AXIS));
      this.panel.setOpaque(false);
      this.iconLabel.setOpaque(false);
      this.nameLabel.setOpaque(false);
      this.descLabel.setOpaque(false);
      this.iconLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.nameLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.descLabel.setAlignmentY(Component.CENTER_ALIGNMENT);
      this.panel.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 4));
      this.nameLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
      this.descLabel.setFont(Style.getDefaultFont().deriveFont(11f));
      this.panel.add(this.iconLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(6));
      this.panel.add(this.nameLabel);
      this.panel.add(javax.swing.Box.createHorizontalStrut(6));
      this.panel.add(this.descLabel);
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends GlobalApiItem> list, GlobalApiItem item, int index,
                                                   boolean isSelected, boolean cellHasFocus) {
      if (item != null) {
        this.nameLabel.setText(item.label());
        this.descLabel.setText(item.description());
        this.iconLabel.setIcon(switch (item.badge()) {
          case "h" -> Icons.SYMBOL_CLASS_16;
          case "e" -> Icons.SYMBOL_DEPENDENCY_16;
          case "c", "g" -> Icons.SYMBOL_FIELD_16;
          case "m" -> Icons.SYMBOL_METHOD_16;
          case "q" -> Icons.SEARCH_16;
          default -> Icons.SYMBOL_DEPENDENCY_16;
        });
      }
      this.panel.setOpaque(false);
      this.nameLabel.setForeground(isSelected ? Color.WHITE : Style.text());
      this.descLabel.setForeground(isSelected ? new Color(200, 210, 225) : Style.mutedText());
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
