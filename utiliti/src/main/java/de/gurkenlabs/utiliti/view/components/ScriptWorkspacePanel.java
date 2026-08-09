package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.GradleScriptProjectSupport;
import de.gurkenlabs.utiliti.controller.IntellijIntegration;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
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
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
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
import javax.swing.tree.TreePath;

/** First-class central workspace for project scripts. */
public final class ScriptWorkspacePanel extends JPanel {
  private static final int BOTTOM_PANEL_HEIGHT = 190;

  private final DefaultMutableTreeNode scriptsRoot = new DefaultMutableTreeNode("Scripts");
  private final DefaultTreeModel scriptsModel = new DefaultTreeModel(this.scriptsRoot);
  private final JTree scripts = new JTree(this.scriptsModel);
  private final JTextField search = new JTextField();
  private final DefaultMutableTreeNode outlineRoot = new DefaultMutableTreeNode("Outline");
  private final DefaultTreeModel outlineModel = new DefaultTreeModel(this.outlineRoot);
  private final JTree outline = new JTree(this.outlineModel);
  private final JTabbedPane tabs = new JTabbedPane();
  private final JPanel mainEditorArea = new JPanel(new BorderLayout());
  private final DefaultTableModel problemsModel = new DefaultTableModel(
    new Object[] {"Severity", "File", "Line", "Message"}, 0) {
      @Override public boolean isCellEditable(int row, int column) { return false; }
    };
  private final JTable problems = new JTable(this.problemsModel);
  private final JTextArea output = new JTextArea();
  private final JLabel status = new JLabel(" ");
  private final JLabel caretStatus = new JLabel(" ");
  private final JPanel conflictBar = new JPanel(new BorderLayout(8, 0));
  private final JLabel conflictMessage = new JLabel();
  private final Map<String, ScriptTab> openTabs = new LinkedHashMap<>();
  private final Timer externalChangeTimer = new Timer(900, event -> this.checkExternalChanges());
  private MonacoScriptEditor monaco;
  private ScriptTab monacoTab;
  private ScriptTab conflictTab;
  private Consumer<ScriptDefinition> selectionListener = ignored -> {};

  public ScriptWorkspacePanel() {
    super(new BorderLayout());
    this.setBackground(Style.background());
    this.add(this.createConflictBar(), BorderLayout.NORTH);

    JSplitPane outlineAndGlobals = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.createOutline(), this.createGlobalsPanel());
    UI.configureSplitPane(outlineAndGlobals);
    outlineAndGlobals.setResizeWeight(0.5);

    JSplitPane explorer = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.createScriptExplorer(), outlineAndGlobals);
    UI.configureSplitPane(explorer);
    explorer.setResizeWeight(0.35);
    explorer.setDividerLocation(0.35);
    explorer.setMinimumSize(new Dimension(235, 0));
    explorer.setPreferredSize(new Dimension(265, 0));

    JTabbedPane bottomTabs = new JTabbedPane();
    this.problems.getSelectionModel().addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting() && this.problems.getSelectedRow() >= 0) {
        Object lineVal = this.problemsModel.getValueAt(this.problems.getSelectedRow(), 2);
        if (lineVal instanceof Integer line && line > 0 && this.monaco != null && this.monaco.isReady()) {
          this.monaco.revealLine(line);
        }
      }
    });
    bottomTabs.addTab("Problems", new JScrollPane(this.problems));
    this.output.setEditable(false);
    this.output.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 12));
    bottomTabs.addTab("Output", new JScrollPane(this.output));
    bottomTabs.setMinimumSize(new Dimension(0, 110));
    bottomTabs.setPreferredSize(new Dimension(0, BOTTOM_PANEL_HEIGHT));

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

    JSplitPane editorSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, this.mainEditorArea, bottomTabs);
    UI.configureSplitPane(editorSplit);
    editorSplit.setResizeWeight(1.0);
    editorSplit.addComponentListener(new ComponentAdapter() {
      private boolean initialized;

      @Override public void componentResized(ComponentEvent event) {
        if (!this.initialized && editorSplit.getHeight() > BOTTOM_PANEL_HEIGHT) {
          editorSplit.setDividerLocation(editorSplit.getHeight() - BOTTOM_PANEL_HEIGHT);
          this.initialized = true;
        }
      }
    });

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, explorer, editorSplit);
    UI.configureSplitPane(split);
    split.setResizeWeight(0.0);
    split.setDividerLocation(265);
    this.add(split, BorderLayout.CENTER);

    JPanel statusBar = new JPanel(new BorderLayout());
    statusBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()));
    this.status.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    this.caretStatus.setBorder(BorderFactory.createEmptyBorder(3, 10, 3, 10));
    this.caretStatus.setForeground(Style.mutedText());
    statusBar.add(this.status, BorderLayout.CENTER);
    statusBar.add(this.caretStatus, BorderLayout.EAST);
    this.add(statusBar, BorderLayout.SOUTH);

    this.search.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent event) { refreshScripts(); }
      @Override public void removeUpdate(DocumentEvent event) { refreshScripts(); }
      @Override public void changedUpdate(DocumentEvent event) { refreshScripts(); }
    });
    this.tabs.addChangeListener(event -> this.activeTabChanged());
    this.refreshTheme();
  }

  @Override
  public void addNotify() {
    super.addNotify();
    this.externalChangeTimer.start();
  }

  @Override
  public void removeNotify() {
    this.externalChangeTimer.stop();
    super.removeNotify();
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
    if (selectedId != null) this.selectTreeNode(selectedId);
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
    this.setBackground(Style.background());
    this.scripts.setBackground(Style.background());
    this.scripts.setForeground(Style.text());
    this.outline.setBackground(Style.background());
    this.outline.setForeground(Style.text());
    this.problems.setBackground(Style.surface());
    this.problems.setForeground(Style.text());
    this.problems.setGridColor(Style.border());
    this.output.setBackground(Style.background());
    this.output.setForeground(Style.text());
    this.output.setCaretColor(Style.accent());
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
    content.setBackground(Style.background());
    header.setBackground(Style.background());
    panel.setBackground(Style.background());
    this.search.setFont(Style.getDefaultFont());
    this.search.putClientProperty("JTextField.placeholderText", "Search scripts...");
    RoundedSearchBox searchBox = new RoundedSearchBox(this.search, 200);
    content.add(searchBox, BorderLayout.NORTH);
    this.scripts.setRootVisible(false);
    this.scripts.setShowsRootHandles(true);
    this.scripts.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.scripts.setBackground(Style.background());
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

    content.add(createBorderlessScrollPane(this.scripts), BorderLayout.CENTER);
    panel.add(content, BorderLayout.CENTER);
    return panel;
  }

  private JPanel createOutline() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setBackground(Style.background());
    panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
    panel.add(sectionTitle("OUTLINE"), BorderLayout.NORTH);
    this.outline.setRootVisible(false);
    this.outline.setShowsRootHandles(true);
    this.outline.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.outline.setBackground(Style.background());
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
    panel.setBackground(Style.background());
    panel.setBorder(BorderFactory.createEmptyBorder(6, 8, 8, 8));
    panel.add(sectionTitle("GLOBALS & APIS"), BorderLayout.NORTH);

    this.refreshGlobals();

    JList<GlobalApiItem> list = new JList<>(this.globalsModel);
    list.setCellRenderer(new GlobalApiRenderer());
    list.setFixedCellHeight(26);
    list.setBackground(Style.background());
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
    scroll.getViewport().setBackground(new Color(0, 0, 0, 0));
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
    return new Object[] {diagnostic.severity(), diagnostic.source(), diagnostic.line(), diagnostic.message()};
  }

  private void showAnalysis(de.gurkenlabs.litiengine.scripting.ScriptLanguageService.Analysis analysis) {
    this.problemsModel.setRowCount(0);
    analysis.diagnostics().forEach(diagnostic -> this.problemsModel.addRow(problemRow(diagnostic)));
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

  private void appendOutput(String message) {
    if (!this.output.getText().isEmpty()) this.output.append(System.lineSeparator());
    this.output.append(message);
    this.output.setCaretPosition(this.output.getDocument().getLength());
  }

  private void insertScriptNode(ScriptDefinition definition) {
    DefaultMutableTreeNode parent = this.scriptsRoot;
    String relative = Objects.toString(definition.getSource(), "").replace('\\', '/');
    relative = relative.replaceFirst("^(?:.*?/)?src/main/(?:java|groovy)/", "");
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
    if (Editor.instance().getGameFile() == null || Editor.instance().getProjectPath() == null) return;
    String prefix = switch (kind) {
      case GAME -> "GameScript";
      case ENVIRONMENT -> "EnvironmentScript";
      case ENTITY -> "CreatureScript";
    };
    ScriptHostType hostType = switch (kind) {
      case GAME -> ScriptHostType.GAME;
      case ENVIRONMENT -> ScriptHostType.ENVIRONMENT;
      case ENTITY -> ScriptHostType.ENTITY;
    };
    String targetType = kind == ScriptKind.ENTITY ? Creature.class.getName() : null;

    int suffix = 1;
    String id;
    String className;
    Path source;
    do {
      className = suffix == 1 ? prefix : prefix + suffix;
      id = className;
      source = resolveSource("src/main/java/" + className + ".java");
      suffix++;
    } while (source != null && (Files.exists(source) || scriptIdExists(id)));
    if (source == null) return;

    ScriptDefinition definition = new ScriptDefinition(className, "java", "src/main/java/" + className + ".java",
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
      source = resolveSource("src/main/java/" + className + ".java");
      suffix++;
    } while (source != null && (Files.exists(source) || scriptIdExists(id)));
    if (source == null) return;

    ScriptDefinition dup = new ScriptDefinition(id, "java", "src/main/java/" + className + ".java",
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

      JMenuItem deleteItem = new JMenuItem("Delete Script", Icons.DELETE_16);
      deleteItem.addActionListener(evt -> deleteScript(selected));
      menu.add(deleteItem);

      JMenuItem openIdeItem = new JMenuItem("Open in IDE", Icons.EXTERNAL_16);
      openIdeItem.addActionListener(evt -> openActiveExternally());
      menu.add(openIdeItem);
    }
    menu.show(e.getComponent(), e.getX(), e.getY());
  }

  private boolean scriptIdExists(String id) {
    return Editor.instance().getGameFile().getScripts().stream().anyMatch(candidate -> candidate.getId().equals(id));
  }

  private static String defaultSource(ScriptDefinition definition, String className, ScriptKind kind) {
    if (kind == ScriptKind.GAME) {
      return "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.GAME)\n"
        + "public class " + className + " extends GameScript {\n"
        + "  @Override\n"
        + "  protected void onStarted() {\n"
        + "    // The game loop is active.\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Game-level script logic.\n"
        + "  }\n"
        + "}\n";
    }
    if (kind == ScriptKind.ENVIRONMENT) {
      return "import de.gurkenlabs.litiengine.*;\n"
        + "import de.gurkenlabs.litiengine.environment.Environment;\n"
        + "import de.gurkenlabs.litiengine.resources.*;\n"
        + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
        + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENVIRONMENT)\n"
        + "public class " + className + " extends EnvironmentScript {\n"
        + "  @Override\n"
        + "  protected void onLoaded() {\n"
        + "    // Map / Environment loaded.\n"
        + "  }\n\n"
        + "  @Override\n"
        + "  public void update() {\n"
        + "    // Environment-level script logic.\n"
        + "  }\n"
        + "}\n";
    }
    return "import de.gurkenlabs.litiengine.*;\n"
      + "import de.gurkenlabs.litiengine.entities.Creature;\n"
      + "import de.gurkenlabs.litiengine.resources.*;\n"
      + "import de.gurkenlabs.litiengine.scripting.*;\n\n"
      + "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType.ENTITY, target = Creature.class)\n"
      + "public class " + className + " extends CreatureScript {\n"
      + "  @Override\n"
      + "  protected void onLoaded() {\n"
      + "    // Creature and environment ready.\n"
      + "  }\n\n"
      + "  @Override\n"
      + "  protected void update() {\n"
      + "    // Entity-level script logic.\n"
      + "  }\n"
      + "}\n";
  }

  static String synchronizeDeclaration(String source, ScriptDefinition definition) {
    if (source == null || source.isBlank() || definition == null) return source;
    String annotation = "@ScriptInfo(id = \"" + definition.getId() + "\", host = ScriptHostType." + definition.getHost()
      + (definition.getHost() == ScriptHostType.ENTITY && definition.getTargetType() != null
        ? ", target = " + definition.getTargetType() + ".class" : "") + ")";
    String updated = source.replaceFirst("(?s)@ScriptInfo\\s*\\(.*?\\)", Matcher.quoteReplacement(annotation));
    String base = scriptBase(definition);
    updated = updated.replaceFirst("(?m)(\\bclass\\s+[A-Za-z_$][\\w$]*\\s+extends\\s+)[\\w.$<>?]+",
      "$1" + Matcher.quoteReplacement(base));
    if (definition.getHost() == ScriptHostType.GAME) {
      return updated.replaceAll("\\bvoid\\s+onLoaded\\s*\\(", "void onStarted(")
        .replaceAll("\\bvoid\\s+onUnloaded\\s*\\(", "void onStopped(");
    }
    return updated.replaceAll("\\bvoid\\s+onStarted\\s*\\(", "void onLoaded(")
      .replaceAll("\\bvoid\\s+onStopped\\s*\\(", "void onUnloaded(");
  }

  private static String scriptBase(ScriptDefinition definition) {
    return switch (definition.getHost()) {
      case GAME -> "GameScript";
      case ENVIRONMENT -> "EnvironmentScript";
      case ENTITY -> Creature.class.getName().equals(definition.getTargetType())
        ? "CreatureScript" : "EntityScript<" + Objects.requireNonNullElse(
          definition.getTargetType(), "de.gurkenlabs.litiengine.entities.IEntity") + ">";
    };
  }

  private static String displayName(ScriptDefinition definition) {
    return definition.getName() == null || definition.getName().isBlank() ? definition.getId() : definition.getName();
  }

  private final class ScriptTab extends JPanel {
    private final ScriptDefinition definition;
    private String text = "";
    private final Path path;
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

      String ext = ".java";
      String subfolder = "src/main/java/";
      String newSourceRel = subfolder + newClassName + ext;
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
      if (this.title != null) this.title.setText((this.dirty ? "● " : "") + displayName(this.definition));
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

  private static final class ScriptTreeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    private final JLabel iconLabel = new JLabel();
    private final JLabel textLabel = new JLabel();

    ScriptTreeRenderer() {
      this.panel.setOpaque(false);
      this.textLabel.setFont(Style.getDefaultFont());
      this.panel.add(this.iconLabel);
      this.panel.add(this.textLabel);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptTreeItem item) {
        this.textLabel.setText(item.label());
        this.iconLabel.setIcon(item.definition() != null ? Icons.SCRIPT_16 : Icons.SYMBOL_GROUP_16);
      } else {
        this.textLabel.setText(Objects.toString(value, ""));
        this.iconLabel.setIcon(null);
      }
      this.panel.setOpaque(selected);
      this.panel.setBackground(selected ? Style.selection() : Style.background());
      this.textLabel.setForeground(selected ? Color.WHITE : Style.text());
      return this.panel;
    }
  }

  private static final class OutlineTreeRenderer implements TreeCellRenderer {
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 4, 0));
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel detailLabel = new JLabel();

    OutlineTreeRenderer() {
      this.panel.setOpaque(false);
      this.nameLabel.setFont(Style.getDefaultFont());
      this.detailLabel.setFont(Style.getDefaultFont().deriveFont(11f));
      this.panel.add(this.iconLabel);
      this.panel.add(this.nameLabel);
      this.panel.add(this.detailLabel);
    }

    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded,
                                                   boolean leaf, int row, boolean focused) {
      if (value instanceof DefaultMutableTreeNode node && node.getUserObject() instanceof ScriptOutline.Symbol symbol) {
        this.iconLabel.setIcon(switch (symbol.kind()) {
          case CLASS -> Icons.SYMBOL_CLASS_16;
          case GROUP -> Icons.SYMBOL_GROUP_16;
          case FIELD -> Icons.SYMBOL_FIELD_16;
          case METHOD -> Icons.SYMBOL_METHOD_16;
          case DEPENDENCY -> Icons.SYMBOL_DEPENDENCY_16;
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
      this.panel.setOpaque(selected);
      this.panel.setBackground(selected ? Style.selection() : Style.background());
      return this.panel;
    }
  }

  private static final class GlobalApiRenderer implements javax.swing.ListCellRenderer<GlobalApiItem> {
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
    private final JLabel iconLabel = new JLabel();
    private final JLabel nameLabel = new JLabel();
    private final JLabel descLabel = new JLabel();

    GlobalApiRenderer() {
      this.panel.setOpaque(false);
      this.panel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      this.nameLabel.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11f));
      this.descLabel.setFont(Style.getDefaultFont().deriveFont(11f));
      this.panel.add(this.iconLabel);
      this.panel.add(this.nameLabel);
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
      this.panel.setOpaque(isSelected);
      this.panel.setBackground(isSelected ? Style.selection() : Style.background());
      this.nameLabel.setForeground(isSelected ? Color.WHITE : Style.text());
      this.descLabel.setForeground(isSelected ? new Color(200, 210, 225) : Style.mutedText());
      return this.panel;
    }
  }

}
