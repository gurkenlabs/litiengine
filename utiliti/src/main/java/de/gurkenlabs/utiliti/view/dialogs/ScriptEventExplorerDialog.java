package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.JavaCodeSnippetPane;
import de.gurkenlabs.utiliti.view.components.ScriptWorkspacePanel.ScriptKind;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

/** Interactive explorer and cheat sheet for script lifecycle events, combat primitives, and engine APIs. */
public final class ScriptEventExplorerDialog extends JDialog {
  private static ScriptEventExplorerDialog instance;

  private final List<ScriptEventItem> allItems = new ArrayList<>();
  private final DefaultListModel<ScriptEventItem> listModel = new DefaultListModel<>();
  private final JList<ScriptEventItem> eventList = new JList<>(this.listModel);
  private final JTextField searchField = new JTextField(20);
  private final JComboBox<String> categoryFilter = new JComboBox<>();

  private final JLabel titleLabel = new JLabel("Select an event or API");
  private final JLabel hostBadge = new JLabel("");
  private final JTextArea descriptionArea = new JTextArea();
  private final JavaCodeSnippetPane codeArea = new JavaCodeSnippetPane();
  private final JButton insertButton;
  private final JButton copyButton;
  private final JLabel statusLabel = new JLabel(" ");
  private final JTabbedPane tabbedPane = new JTabbedPane();

  public static synchronized void showDialog() {
    if (instance == null) {
      instance = new ScriptEventExplorerDialog();
    }
    instance.tabbedPane.setSelectedIndex(0);
    instance.setVisible(true);
    instance.toFront();
    instance.requestFocus();
  }

  public static synchronized void showGuide() {
    if (instance == null) {
      instance = new ScriptEventExplorerDialog();
    }
    instance.tabbedPane.setSelectedIndex(1);
    instance.setVisible(true);
    instance.toFront();
    instance.requestFocus();
  }

  private ScriptEventExplorerDialog() {
    this.setTitle("Script Events & Architecture Guide");
    this.setModalityType(ModalityType.MODELESS);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(900, 620);
    this.setMinimumSize(new Dimension(700, 480));
    this.setLocationRelativeTo(null);
    this.setLayout(new BorderLayout());

    this.populateCatalog();

    // Tab 1: Catalog
    JPanel catalogPanel = new JPanel(new BorderLayout(8, 8));
    catalogPanel.setBorder(BorderFactory.createEmptyBorder(6, 6, 6, 6));

    // Top Filter Bar
    JPanel topBar = new JPanel(new BorderLayout(8, 0));
    topBar.setBorder(BorderFactory.createEmptyBorder(4, 4, 4, 4));

    JPanel searchPanel = new JPanel(new BorderLayout(4, 0));
    JLabel searchIconLabel = new JLabel(Icons.SEARCH_16);
    searchPanel.add(searchIconLabel, BorderLayout.WEST);
    searchPanel.add(this.searchField, BorderLayout.CENTER);

    this.categoryFilter.addItem("All Categories");
    this.categoryFilter.addItem("Entity & Creature Lifecycle");
    this.categoryFilter.addItem("Environment Lifecycle");
    this.categoryFilter.addItem("Game Lifecycle");
    this.categoryFilter.addItem("Combat & Abilities");
    this.categoryFilter.addItem("Movement & Physics");
    this.categoryFilter.addItem("Sequences & Cinematics");
    this.categoryFilter.addItem("UI & HUD Overlays");
    this.categoryFilter.addItem("Entity Queries");

    this.categoryFilter.addActionListener(e -> this.filterList());
    this.searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) { filterList(); }
      @Override public void removeUpdate(DocumentEvent e) { filterList(); }
      @Override public void changedUpdate(DocumentEvent e) { filterList(); }
    });

    topBar.add(searchPanel, BorderLayout.CENTER);
    topBar.add(this.categoryFilter, BorderLayout.EAST);
    catalogPanel.add(topBar, BorderLayout.NORTH);

    // Left List
    this.eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.eventList.setCellRenderer(new DefaultListCellRenderer() {
      @Override
      public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
        JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        if (value instanceof ScriptEventItem item) {
          label.setText(item.name + " (" + item.category + ")");
          label.setIcon(Icons.SYMBOL_METHOD_16);
        }
        return label;
      }
    });

    this.eventList.addListSelectionListener(e -> {
      if (!e.getValueIsAdjusting()) {
        displayItem(this.eventList.getSelectedValue());
      }
    });

    JScrollPane listScroll = new JScrollPane(this.eventList);
    listScroll.setPreferredSize(new Dimension(300, 400));

    // Right Details View
    JPanel detailsPanel = new JPanel(new BorderLayout(0, 8));
    detailsPanel.setBorder(BorderFactory.createEmptyBorder(0, 4, 8, 8));

    JPanel headerPanel = new JPanel(new BorderLayout(8, 0));
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD, 15f));
    this.hostBadge.setForeground(Color.GRAY);
    headerPanel.add(this.titleLabel, BorderLayout.WEST);
    headerPanel.add(this.hostBadge, BorderLayout.EAST);
    detailsPanel.add(headerPanel, BorderLayout.NORTH);

    this.descriptionArea.setEditable(false);
    this.descriptionArea.setLineWrap(true);
    this.descriptionArea.setWrapStyleWord(true);
    this.descriptionArea.setOpaque(false);
    this.descriptionArea.setFont(this.descriptionArea.getFont().deriveFont(12.5f));
    this.descriptionArea.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));

    JScrollPane codeScroll = new JScrollPane(this.codeArea);
    codeScroll.setBorder(BorderFactory.createEmptyBorder());

    JPanel centerDetails = new JPanel(new BorderLayout(0, 4));
    centerDetails.add(this.descriptionArea, BorderLayout.NORTH);
    centerDetails.add(codeScroll, BorderLayout.CENTER);
    detailsPanel.add(centerDetails, BorderLayout.CENTER);

    // Bottom Action Buttons
    JPanel actionsBar = new JPanel(new BorderLayout());
    JPanel buttonGroup = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));

    this.insertButton = new JButton("Insert into Active Script", Icons.ADD_16);
    this.insertButton.addActionListener(e -> insertSelected());

    this.copyButton = new JButton("Copy Snippet", Icons.COPY_16);
    this.copyButton.addActionListener(e -> copySelected());

    buttonGroup.add(this.copyButton);
    buttonGroup.add(this.insertButton);

    actionsBar.add(this.statusLabel, BorderLayout.WEST);
    actionsBar.add(buttonGroup, BorderLayout.EAST);
    detailsPanel.add(actionsBar, BorderLayout.SOUTH);

    JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, listScroll, detailsPanel);
    splitPane.setDividerLocation(300);
    catalogPanel.add(splitPane, BorderLayout.CENTER);

    // Tab 2: Architecture & Getting Started Guide
    JPanel guidePanel = createGuidePanel();

    this.tabbedPane.addTab("Events & API Catalog", Icons.API_16, catalogPanel);
    this.tabbedPane.addTab("Architecture & Getting Started Guide", Icons.DOCUMENTATION_16, guidePanel);
    this.add(this.tabbedPane, BorderLayout.CENTER);

    this.filterList();
    if (!this.listModel.isEmpty()) {
      this.eventList.setSelectedIndex(0);
    }
  }

  private JPanel createGuidePanel() {
    JPanel panel = new JPanel(new BorderLayout(0, 8));
    panel.setBackground(Style.COLOR_BG);
    panel.setBorder(BorderFactory.createEmptyBorder(10, 14, 10, 14));

    JEditorPane guidePane = new JEditorPane();
    guidePane.setContentType("text/html");
    guidePane.setEditable(false);
    guidePane.setOpaque(false);
    guidePane.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
    guidePane.setFont(Style.getDefaultFont());

    String guideHtml = "<html><body style='font-family: sans-serif; font-size: 11pt; color: #c8d0f5; background-color: #121214; padding: 4px;'>"
        + "<h2 style='color: #ffffff; margin-top: 0px; margin-bottom: 6px; font-size: 15pt;'>Scripting Architecture Guide</h2>"
        + "<p style='color: #969eb9; margin-top: 0px; margin-bottom: 14px;'>In LITIengine, you can build full games using pure modular scripts. Scripts operate across three distinct tiers:</p>"

        // Card 1: GameScript
        + "<div style='background-color: #1e1e23; border: 1px solid #373740; padding: 10px 14px; margin-bottom: 12px; border-radius: 6px;'>"
        + "<div style='margin-bottom: 6px;'><span style='background-color: #24355a; color: #7aa2f7; font-size: 9pt; font-weight: bold; padding: 2px 8px; border-radius: 3px;'>TIER 1 &bull; GAME SCRIPT (GameScript)</span>"
        + " <b style='color: #ffffff; margin-left: 6px; font-size: 11pt;'>Global Game Entry Point</b></div>"
        + "<div style='color: #969eb9; margin-bottom: 6px;'><b>Role:</b> The root orchestrator (equivalent to <code>main()</code>). Boots on startup and runs continuously across all maps.</div>"
        + "<ul style='margin-top: 4px; margin-bottom: 4px; padding-left: 20px; color: #c8d0f5;'>"
        + "<li><b>Load Initial Map:</b> Call <code>loadMap(\"level1\")</code> on startup or when entering portals.</li>"
        + "<li><b>Manage Global State:</b> Store persistent progression across maps via <code>globals.put(\"score\", score)</code> or <code>globals.put(\"lives\", 3)</code>.</li>"
        + "<li><b>Background Soundtracks:</b> Play music with <code>playMusic(\"main_theme\")</code> and <code>stopMusic()</code>.</li>"
        + "<li><b>Global Hotkeys:</b> Register pause or restart inputs via <code>Input.keyboard().onKeyTyped(KeyEvent.VK_ESCAPE, ...)</code>.</li>"
        + "</ul></div>"

        // Card 2: EnvironmentScript
        + "<div style='background-color: #1e1e23; border: 1px solid #373740; padding: 10px 14px; margin-bottom: 12px; border-radius: 6px;'>"
        + "<div style='margin-bottom: 6px;'><span style='background-color: #1c3d2e; color: #9ece6a; font-size: 9pt; font-weight: bold; padding: 2px 8px; border-radius: 3px;'>TIER 2 &bull; ENVIRONMENT SCRIPT (EnvironmentScript)</span>"
        + " <b style='color: #ffffff; margin-left: 6px; font-size: 11pt;'>Map / Level Controller</b></div>"
        + "<div style='color: #969eb9; margin-bottom: 6px;'><b>Role:</b> Attached to a specific map. Active while that map is loaded; coordinates level objectives and cinematics.</div>"
        + "<ul style='margin-top: 4px; margin-bottom: 4px; padding-left: 20px; color: #c8d0f5;'>"
        + "<li><b>Level Start:</b> Show level titles via <code>context().ui().showBanner(\"STAGE 1\", \"Fight!\", 3000)</code>.</li>"
        + "<li><b>Objective Tracking:</b> Detect enemy defeats with <code>onEntityRemoved(entity)</code> to trigger victory transitions or unlock doors.</li>"
        + "<li><b>Camera Sequences:</b> Pan camera smoothly using <code>context().sequence().cameraPanTo(...)</code>.</li>"
        + "</ul></div>"

        // Card 3: CreatureScript
        + "<div style='background-color: #1e1e23; border: 1px solid #373740; padding: 10px 14px; margin-bottom: 4px; border-radius: 6px;'>"
        + "<div style='margin-bottom: 6px;'><span style='background-color: #42301c; color: #e0af68; font-size: 9pt; font-weight: bold; padding: 2px 8px; border-radius: 3px;'>TIER 3 &bull; CREATURE SCRIPT (CreatureScript)</span>"
        + " <b style='color: #ffffff; margin-left: 6px; font-size: 11pt;'>Entity Behaviors & AI</b></div>"
        + "<div style='color: #969eb9; margin-bottom: 6px;'><b>Role:</b> Attached to individual creatures, enemies, NPCs, or players to govern movement, combat, and interactions.</div>"
        + "<ul style='margin-top: 4px; margin-bottom: 4px; padding-left: 20px; color: #c8d0f5;'>"
        + "<li><b>AI Movement:</b> Move towards targets with <code>moveTowards(target)</code> or patrol with <code>moveInDirection(dir)</code>.</li>"
        + "<li><b>Combat & Abilities:</b> Build abilities via <code>createAbility(\"Spell\").cast()</code> or fire projectiles with <code>spawnProjectile()</code>.</li>"
        + "<li><b>Reactions:</b> Spawn floating damage numbers in <code>onHit(event)</code> and despawn in <code>onDeath(entity, hitEvent)</code>.</li>"
        + "</ul></div>"
        + "</body></html>";

    guidePane.setText(guideHtml);
    guidePane.setCaretPosition(0);
    JScrollPane guideScroll = new JScrollPane(guidePane);
    guideScroll.setBorder(BorderFactory.createEmptyBorder());
    guideScroll.getViewport().setBackground(Style.COLOR_BG);
    panel.add(guideScroll, BorderLayout.CENTER);

    // Quick Action Bar
    JPanel quickActions = new JPanel(new BorderLayout());
    quickActions.setOpaque(false);
    quickActions.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.COLOR_BORDER),
        BorderFactory.createEmptyBorder(8, 2, 2, 2)
    ));

    JLabel actionHint = new JLabel("Quick Actions:");
    actionHint.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 11.5f));
    actionHint.setForeground(Style.mutedText());
    quickActions.add(actionHint, BorderLayout.WEST);

    JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 6, 0));
    buttonsPanel.setOpaque(false);

    JButton createGameScriptBtn = new JButton("Game Script", Icons.ADD_16);
    createGameScriptBtn.setToolTipText("Create a new global GameScript entry point");
    createGameScriptBtn.addActionListener(e -> {
      this.dispose();
      UI.getScriptWorkspacePanel().createScript(ScriptKind.GAME);
    });

    JButton createEnvScriptBtn = new JButton("Map Script", Icons.ADD_16);
    createEnvScriptBtn.setToolTipText("Create a new map-level EnvironmentScript");
    createEnvScriptBtn.addActionListener(e -> {
      this.dispose();
      UI.getScriptWorkspacePanel().createScript(ScriptKind.ENVIRONMENT);
    });

    JButton createCreatureScriptBtn = new JButton("Creature Script", Icons.ADD_16);
    createCreatureScriptBtn.setToolTipText("Create a new CreatureScript behavior");
    createCreatureScriptBtn.addActionListener(e -> {
      this.dispose();
      UI.getScriptWorkspacePanel().createScript(ScriptKind.ENTITY, de.gurkenlabs.litiengine.entities.Creature.class);
    });

    JButton configGameBtn = new JButton("Game Scripts...", Icons.SETTINGS_16);
    configGameBtn.setToolTipText("Manage scripts attached to the game lifecycle");
    configGameBtn.addActionListener(e -> {
      this.dispose();
      GameScriptsDialog.showDialog();
    });

    buttonsPanel.add(createGameScriptBtn);
    buttonsPanel.add(createEnvScriptBtn);
    buttonsPanel.add(createCreatureScriptBtn);
    buttonsPanel.add(configGameBtn);
    quickActions.add(buttonsPanel, BorderLayout.EAST);

    panel.add(quickActions, BorderLayout.SOUTH);

    return panel;
  }

  private void filterList() {
    String search = this.searchField.getText() == null ? "" : this.searchField.getText().toLowerCase(Locale.ROOT).trim();
    String cat = (String) this.categoryFilter.getSelectedItem();
    if (cat == null) cat = "All Categories";

    this.listModel.clear();
    for (ScriptEventItem item : this.allItems) {
      boolean matchesCat = "All Categories".equals(cat) || item.category.equalsIgnoreCase(cat);
      boolean matchesSearch = search.isEmpty()
          || item.name.toLowerCase(Locale.ROOT).contains(search)
          || item.description.toLowerCase(Locale.ROOT).contains(search)
          || item.codeSnippet.toLowerCase(Locale.ROOT).contains(search);
      if (matchesCat && matchesSearch) {
        this.listModel.addElement(item);
      }
    }
    if (!this.listModel.isEmpty()) {
      this.eventList.setSelectedIndex(0);
    } else {
      this.displayItem(null);
    }
  }

  private void displayItem(ScriptEventItem item) {
    if (item == null) {
      this.titleLabel.setText("No selection");
      this.hostBadge.setText("");
      this.descriptionArea.setText("");
      this.codeArea.setCode("");
      this.insertButton.setEnabled(false);
      this.copyButton.setEnabled(false);
      return;
    }
    this.titleLabel.setText(item.name);
    this.hostBadge.setText(item.hostType);
    this.descriptionArea.setText(item.description);
    this.codeArea.setCode(item.codeSnippet);
    this.insertButton.setEnabled(true);
    this.copyButton.setEnabled(true);
    this.statusLabel.setText(" ");
  }

  private void copySelected() {
    ScriptEventItem item = this.eventList.getSelectedValue();
    if (item == null || item.codeSnippet == null) return;
    Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(item.codeSnippet), null);
    this.statusLabel.setText("Copied to clipboard!");
  }

  private void insertSelected() {
    ScriptEventItem item = this.eventList.getSelectedValue();
    if (item == null || item.codeSnippet == null) return;
    try {
      UI.getScriptWorkspacePanel().insertTextToActiveScript("\n" + item.codeSnippet + "\n");
      this.statusLabel.setText("Inserted into active script!");
    } catch (Exception e) {
      this.statusLabel.setText("Could not insert: " + e.getMessage());
    }
  }

  private void populateCatalog() {
    // 1. Entity & Creature Lifecycle
    this.allItems.add(new ScriptEventItem(
        "onLoaded()",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked once when the entity is loaded and initialized within the active game world environment.",
        """
        @Override
        public void onLoaded() {
          // Initialize entity state, attach components, or configure attributes
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "update()",
        "Entity & Creature Lifecycle",
        "All Scripts",
        "Invoked every game loop frame tick. Use for movement, AI behavior trees, cooldown timers, and condition checking.",
        """
        @Override
        public void update() {
          // Frame tick update logic
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onHit(EntityHitEvent event)",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked whenever this combat entity receives damage from an attack or projectile.\nParameters:\n- event.getDamage(): Damage amount received.\n- event.getSource(): Attacking entity or source.",
        """
        @Override
        protected void onHit(EntityHitEvent event) {
          int damage = event.getDamage();
          // Display hit reaction, floating text, or play sound
          context().ui().floatText("-" + damage, host(), java.awt.Color.RED);
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onDeath(ICombatEntity entity, EntityHitEvent hitEvent)",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked when the combat entity's hit points reach zero.",
        """
        @Override
        protected void onDeath(ICombatEntity entity, EntityHitEvent hitEvent) {
          // Play death animation, drop loot, or grant score
          remove();
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onCollision(CollisionEvent event)",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked when the collision box of this entity collides with static map geometry or another collision entity.",
        """
        @Override
        protected void onCollision(CollisionEvent event) {
          // Handle obstacle collision or bounce
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onInteract(IEntity source)",
        "Entity & Creature Lifecycle",
        "EntityScript",
        "Invoked when an interaction event is triggered by a player or another entity.",
        """
        @Override
        protected void onInteract(IEntity source) {
          // Open dialogue, trigger shop, or activate mechanism
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onMessage(String message, Object sender)",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked when a message event is dispatched to this entity.",
        """
        @Override
        protected void onMessage(String message, Object sender) {
          if ("alert".equals(message)) {
            // Respond to alert message
          }
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onUnloaded()",
        "Entity & Creature Lifecycle",
        "EntityScript / CreatureScript",
        "Invoked when the entity leaves the map environment or despawns.",
        """
        @Override
        public void onUnloaded() {
          // Cleanup subscriptions or timers
        }"""
    ));

    // 2. Environment Lifecycle
    this.allItems.add(new ScriptEventItem(
        "onLoaded() [Environment]",
        "Environment Lifecycle",
        "EnvironmentScript",
        "Invoked when this map environment becomes active in the game world.",
        """
        @Override
        public void onLoaded() {
          // Spawn initial monsters, trigger ambient music, or configure lighting
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onEntityAdded(IEntity entity)",
        "Environment Lifecycle",
        "EnvironmentScript",
        "Invoked whenever any new entity is spawned or added to this map environment.",
        """
        @Override
        protected void onEntityAdded(IEntity entity) {
          if (entity instanceof Creature creature) {
            // Track active enemies or configure spawned creature
          }
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onEntityRemoved(IEntity entity)",
        "Environment Lifecycle",
        "EnvironmentScript",
        "Invoked whenever an entity is removed or despawned from this map environment.",
        """
        @Override
        protected void onEntityRemoved(IEntity entity) {
          // Track wave progress or check if all enemies are defeated
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onCleared()",
        "Environment Lifecycle",
        "EnvironmentScript",
        "Invoked when the environment entities are cleared while the map remains active.",
        """
        @Override
        protected void onCleared() {
          // Reset map state
        }"""
    ));

    // 3. Game Lifecycle
    this.allItems.add(new ScriptEventItem(
        "onStarted()",
        "Game Lifecycle",
        "GameScript",
        "Invoked once when the game loop starts and all subsystems are online.",
        """
        @Override
        public void onStarted() {
          // Set window title, initialize global save data, or load initial map
          loadMap("level1");
        }"""
    ));

    this.allItems.add(new ScriptEventItem(
        "onStopped()",
        "Game Lifecycle",
        "GameScript",
        "Invoked when the game loop is shutting down.",
        """
        @Override
        public void onStopped() {
          // Save game progress or flush statistics
        }"""
    ));

    // 4. Combat & Abilities
    this.allItems.add(new ScriptEventItem(
        "createAbility(name)",
        "Combat & Abilities",
        "CreatureScript / ScriptContext",
        "Fluent builder to define and cast custom combat abilities without Java subclassing.",
        """
        var fireball = createAbility("Fireball")
          .range(200)
          .cooldown(1500)
          .onCast(execution -> {
            spawnProjectile()
              .from(host().getCenter())
              .towards(Game.world().camera().getFocus())
              .speed(300)
              .damage(25)
              .splash(35, 10)
              .spawn();
          });
        fireball.cast();"""
    ));

    this.allItems.add(new ScriptEventItem(
        "spawnProjectile()",
        "Combat & Abilities",
        "ScriptContext",
        "Fluent builder to launch dynamic projectiles with velocity, collision, splash damage, and callbacks.",
        """
        spawnProjectile()
          .from(host().getCenter())
          .towards(targetEntity.getCenter())
          .speed(400)
          .damage(30)
          .splash(40, 15)
          .pierce(false)
          .onHitEntity((target, proj) -> {
            context().ui().floatText("Hit!", target, java.awt.Color.ORANGE);
          })
          .spawn();"""
    ));

    // 5. Movement & Physics
    this.allItems.add(new ScriptEventItem(
        "moveTowards(target)",
        "Movement & Physics",
        "CreatureScript",
        "Moves the creature towards a destination point or target entity at its configured velocity.",
        """
        // Move towards a target entity or point
        moveTowards(targetPlayer);"""
    ));

    this.allItems.add(new ScriptEventItem(
        "moveInDirection(Direction)",
        "Movement & Physics",
        "CreatureScript",
        "Moves the creature in a cardinal direction (UP, DOWN, LEFT, RIGHT).",
        """
        moveInDirection(Direction.RIGHT);"""
    ));

    // 6. Sequences & Cinematics
    this.allItems.add(new ScriptEventItem(
        "ScriptSequence Cinematics",
        "Sequences & Cinematics",
        "ScriptContext",
        "Chains timed camera movements, screen shakes, zooms, and audio in a declarative sequence.",
        """
        context().sequence()
          .screenShake(10, 0, 30)
          .cameraZoom(1.5f, 500)
          .cameraPanTo(bossEntity, 60)
          .playSound("boss_roar")
          .then(() -> {
            context().ui().showBanner("BOSS ENCOUNTER", "Defeat the guardian!", 3000);
          })
          .play();"""
    ));

    // 7. UI & HUD Overlays
    this.allItems.add(new ScriptEventItem(
        "floatText()",
        "UI & HUD Overlays",
        "ScriptContext / ScriptUiOverlay",
        "Renders animated world-space floating combat numbers and status text above entities.",
        """
        context().ui().floatText("-45 CRIT!", host(), java.awt.Color.RED);"""
    ));

    this.allItems.add(new ScriptEventItem(
        "drawScreenText()",
        "UI & HUD Overlays",
        "ScriptContext / ScriptUiOverlay",
        "Renders persistent or timed screen-space HUD text for scores, timers, or health bars.",
        """
        context().ui().drawScreenText("Score: " + score, 20, 30, java.awt.Color.YELLOW);"""
    ));

    this.allItems.add(new ScriptEventItem(
        "showBanner()",
        "UI & HUD Overlays",
        "ScriptContext / ScriptUiOverlay",
        "Displays a cinematic title and subtitle banner across the center of the screen.",
        """
        context().ui().showBanner("LEVEL COMPLETED", "All enemies defeated", 3000);"""
    ));

    // 8. Entity Queries
    this.allItems.add(new ScriptEventItem(
        "EntityQuery.in(environment, ...)",
        "Entity Queries",
        "All Scripts",
        "Fluent spatial entity search and filtering across the active environment.",
        """
        var nearbyEnemies = EntityQuery.in(environment(), Creature.class)
          .within(host().getCenter(), 200)
          .alive()
          .list();"""
    ));
  }

  private static final class ScriptEventItem {
    final String name;
    final String category;
    final String hostType;
    final String description;
    final String codeSnippet;

    ScriptEventItem(String name, String category, String hostType, String description, String codeSnippet) {
      this.name = name;
      this.category = category;
      this.hostType = hostType;
      this.description = description;
      this.codeSnippet = codeSnippet;
    }

    @Override
    public String toString() {
      return this.name;
    }
  }
}
