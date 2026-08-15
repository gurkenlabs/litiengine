package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.ScriptWorkspacePanel.ScriptKind;
import de.gurkenlabs.utiliti.view.components.UI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Dedicated dialog for configuring project-wide {@code GameScript}s, initial startup settings,
 * and providing guidance on game lifecycle scripting.
 */
public class GameScriptsDialog extends JDialog {
  private static GameScriptsDialog instance;

  private final JComboBox<ScriptItem> startupScriptCombo = new JComboBox<>();
  private final JComboBox<String> startupMapCombo = new JComboBox<>();
  private final DefaultTableModel bindingsTableModel;
  private final JTable bindingsTable;
  private final List<ScriptBinding> bindingsList = new ArrayList<>();

  public static void showDialog() {
    if (instance == null) {
      instance = new GameScriptsDialog();
    }
    instance.refreshData();
    instance.setVisible(true);
    instance.toFront();
    instance.requestFocus();
  }

  private GameScriptsDialog() {
    this.setTitle("Configure Game Scripts & Startup");
    this.setModalityType(ModalityType.APPLICATION_MODAL);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setSize(680, 480);
    this.setMinimumSize(new Dimension(550, 380));
    this.setLocationRelativeTo(null);
    this.setLayout(new BorderLayout(0, 0));
    this.getContentPane().setBackground(Style.COLOR_BG);

    // Top Header Banner
    JPanel bannerPanel = new JPanel(new BorderLayout(12, 0));
    bannerPanel.setBackground(Style.COLOR_SURFACE);
    bannerPanel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDER),
        new EmptyBorder(10, 16, 10, 16)
    ));

    JLabel bannerIcon = new JLabel(Icons.SCRIPT_16);

    JPanel bannerText = new JPanel(new GridLayout(0, 1, 0, 2));
    bannerText.setOpaque(false);
    JLabel bannerTitle = new JLabel("Game Script & Startup Configuration");
    bannerTitle.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 13f));
    bannerTitle.setForeground(Style.COLOR_TEXT);

    JLabel bannerDesc = new JLabel("<html><b>Game Script</b> is the global entry point for your game. It runs on boot to load initial maps, manage persistent state, play soundtracks, and handle global hotkeys.</html>");
    bannerDesc.setFont(Style.getDefaultFont().deriveFont(10.5f));
    bannerDesc.setForeground(Style.COLOR_SUBTEXT);

    bannerText.add(bannerTitle);
    bannerText.add(bannerDesc);

    bannerPanel.add(bannerIcon, BorderLayout.WEST);
    bannerPanel.add(bannerText, BorderLayout.CENTER);
    this.add(bannerPanel, BorderLayout.NORTH);

    // Center Content Panel
    JPanel centerPanel = new JPanel(new BorderLayout(0, 10));
    centerPanel.setOpaque(false);
    centerPanel.setBorder(new EmptyBorder(12, 16, 10, 16));

    // Section 1: Startup Settings
    JPanel startupSection = new JPanel(new BorderLayout(0, 6));
    startupSection.setOpaque(false);
    startupSection.add(createSectionHeader("STARTUP SETTINGS"), BorderLayout.NORTH);

    JPanel formGrid = new JPanel(new GridBagLayout());
    formGrid.setOpaque(false);
    GridBagConstraints gbc = new GridBagConstraints();
    gbc.insets = new Insets(3, 4, 3, 4);
    gbc.anchor = GridBagConstraints.WEST;
    gbc.fill = GridBagConstraints.HORIZONTAL;

    // Row 0: Primary Game Script
    gbc.gridx = 0; gbc.gridy = 0; gbc.weightx = 0.0;
    JLabel scriptLabel = new JLabel("Primary Game Script:");
    scriptLabel.setFont(Style.getDefaultFont());
    scriptLabel.setForeground(Style.COLOR_TEXT);
    formGrid.add(scriptLabel, gbc);

    gbc.gridx = 1; gbc.gridy = 0; gbc.weightx = 1.0;
    this.startupScriptCombo.setFont(Style.getDefaultFont());
    formGrid.add(this.startupScriptCombo, gbc);

    gbc.gridx = 2; gbc.gridy = 0; gbc.weightx = 0.0;
    JButton newScriptBtn = new JButton("New Script...", Icons.ADD_16);
    newScriptBtn.setToolTipText("Create a new GameScript in the Script Workspace");
    newScriptBtn.addActionListener(e -> {
      this.dispose();
      UI.getScriptWorkspacePanel().createScript(ScriptKind.GAME);
    });
    formGrid.add(newScriptBtn, gbc);

    // Row 1: Initial Map
    gbc.gridx = 0; gbc.gridy = 1; gbc.weightx = 0.0;
    JLabel mapLabel = new JLabel("Initial Map (Fallback):");
    mapLabel.setFont(Style.getDefaultFont());
    mapLabel.setForeground(Style.COLOR_TEXT);
    formGrid.add(mapLabel, gbc);

    gbc.gridx = 1; gbc.gridy = 1; gbc.weightx = 1.0;
    this.startupMapCombo.setFont(Style.getDefaultFont());
    formGrid.add(this.startupMapCombo, gbc);

    gbc.gridx = 2; gbc.gridy = 1; gbc.weightx = 0.0;
    JButton guideBtn = new JButton("Guide...", Icons.DOCUMENTATION_16);
    guideBtn.setToolTipText("Open Architecture & Getting Started Guide");
    guideBtn.addActionListener(e -> {
      this.dispose();
      ScriptEventExplorerDialog.showGuide();
    });
    formGrid.add(guideBtn, gbc);

    startupSection.add(formGrid, BorderLayout.CENTER);
    centerPanel.add(startupSection, BorderLayout.NORTH);

    // Section 2: Active Game Scripts Table
    JPanel bindingsSection = new JPanel(new BorderLayout(0, 6));
    bindingsSection.setOpaque(false);
    bindingsSection.add(createSectionHeader("ACTIVE GAME SCRIPTS"), BorderLayout.NORTH);

    this.bindingsTableModel = new DefaultTableModel(new Object[]{"Script Name / ID", "Enabled"}, 0) {
      @Override public boolean isCellEditable(int row, int column) { return column == 1; }
      @Override public Class<?> getColumnClass(int columnIndex) { return columnIndex == 1 ? Boolean.class : String.class; }
    };

    this.bindingsTable = new JTable(this.bindingsTableModel);
    this.bindingsTable.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.bindingsTable.setBackground(Style.COLOR_BG);
    this.bindingsTable.setForeground(Style.COLOR_TEXT);
    this.bindingsTable.setSelectionBackground(Style.COLOR_SELECTION_INACTIVE);
    this.bindingsTable.setFont(Style.getDefaultFont());
    this.bindingsTable.setShowGrid(false);
    this.bindingsTable.getColumnModel().getColumn(1).setMaxWidth(70);

    if (this.bindingsTable.getTableHeader() != null) {
      this.bindingsTable.getTableHeader().setBackground(Style.COLOR_SURFACE);
      this.bindingsTable.getTableHeader().setForeground(Style.COLOR_SUBTEXT);
      this.bindingsTable.getTableHeader().setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 10.5f));
      this.bindingsTable.getTableHeader().setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDER));
    }

    JScrollPane tableScroll = new JScrollPane(this.bindingsTable);
    tableScroll.setBorder(BorderFactory.createLineBorder(Style.COLOR_BORDER));
    tableScroll.getViewport().setBackground(Style.COLOR_BG);
    tableScroll.setPreferredSize(new Dimension(400, 130));
    bindingsSection.add(tableScroll, BorderLayout.CENTER);

    JPanel tableActions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 4));
    tableActions.setOpaque(false);
    JButton addBindingBtn = new JButton("Add Binding", Icons.ADD_16);
    addBindingBtn.addActionListener(e -> this.addBinding());
    JButton removeBindingBtn = new JButton("Remove", Icons.DELETE_16);
    removeBindingBtn.addActionListener(e -> this.removeSelectedBinding());
    JButton editScriptBtn = new JButton("Open in Editor", Icons.PENCIL_16);
    editScriptBtn.addActionListener(e -> this.openSelectedScript());

    tableActions.add(addBindingBtn);
    tableActions.add(removeBindingBtn);
    tableActions.add(editScriptBtn);
    bindingsSection.add(tableActions, BorderLayout.SOUTH);

    centerPanel.add(bindingsSection, BorderLayout.CENTER);
    this.add(centerPanel, BorderLayout.CENTER);

    // Bottom Action Buttons
    JPanel bottomBar = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 8));
    bottomBar.setBackground(Style.COLOR_SURFACE);
    bottomBar.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, Style.COLOR_BORDER));

    JButton saveBtn = new JButton("Apply & Save", Icons.SAVE_16);
    saveBtn.addActionListener(e -> {
      this.applyChanges();
      this.dispose();
    });

    JButton cancelBtn = new JButton("Cancel");
    cancelBtn.addActionListener(e -> this.dispose());

    bottomBar.add(saveBtn);
    bottomBar.add(cancelBtn);
    this.add(bottomBar, BorderLayout.SOUTH);
  }

  private static JPanel createSectionHeader(String title) {
    JPanel header = new JPanel(new BorderLayout(0, 2));
    header.setOpaque(false);
    JLabel label = new JLabel(title);
    label.setFont(Style.getDefaultFont().deriveFont(Font.BOLD, 10.5f));
    label.setForeground(Style.COLOR_SUBTEXT);
    header.add(label, BorderLayout.NORTH);
    header.add(new JSeparator(), BorderLayout.SOUTH);
    return header;
  }

  private void refreshData() {
    this.startupScriptCombo.removeAllItems();
    this.startupScriptCombo.addItem(new ScriptItem("(None / Auto-Detect)", null));

    if (Game.scripts() != null) {
      for (ScriptDefinition def : Game.scripts().getDefinitions()) {
        if (def.getHost() == ScriptHostType.GAME) {
          this.startupScriptCombo.addItem(new ScriptItem(def.getName() != null ? def.getName() : def.getId(), def.getId()));
        }
      }
    }

    this.startupMapCombo.removeAllItems();
    this.startupMapCombo.addItem("(Auto-load first map)");
    if (Editor.instance().getMapComponent() != null) {
      for (TmxMap map : Editor.instance().getMapComponent().getMaps()) {
        this.startupMapCombo.addItem(map.getName());
      }
    }

    // Refresh active bindings
    this.bindingsList.clear();
    while (this.bindingsTableModel.getRowCount() > 0) {
      this.bindingsTableModel.removeRow(0);
    }

    if (Game.scripts() != null && Game.scripts().getGameBindings() != null) {
      for (ScriptBinding binding : Game.scripts().getGameBindings()) {
        this.bindingsList.add(new ScriptBinding(binding.getScript(), binding.isEnabled()));
        this.bindingsTableModel.addRow(new Object[]{binding.getScript(), binding.isEnabled()});
      }
    }

    // Select current primary startup script if present in bindings
    if (!this.bindingsList.isEmpty()) {
      String firstScript = this.bindingsList.get(0).getScript();
      for (int i = 0; i < this.startupScriptCombo.getItemCount(); i++) {
        ScriptItem item = this.startupScriptCombo.getItemAt(i);
        if (item != null && firstScript.equals(item.id)) {
          this.startupScriptCombo.setSelectedIndex(i);
          break;
        }
      }
    }
  }

  private void addBinding() {
    ScriptItem selected = (ScriptItem) this.startupScriptCombo.getSelectedItem();
    if (selected != null && selected.id != null) {
      for (ScriptBinding b : this.bindingsList) {
        if (selected.id.equals(b.getScript())) {
          return; // Already added
        }
      }
      ScriptBinding newBinding = new ScriptBinding(selected.id, true);
      this.bindingsList.add(newBinding);
      this.bindingsTableModel.addRow(new Object[]{selected.id, true});
    } else {
      String input = JOptionPane.showInputDialog(this, "Enter Game Script ID or Class:", "Add Game Script", JOptionPane.PLAIN_MESSAGE);
      if (input != null && !input.isBlank()) {
        ScriptBinding newBinding = new ScriptBinding(input.trim(), true);
        this.bindingsList.add(newBinding);
        this.bindingsTableModel.addRow(new Object[]{input.trim(), true});
      }
    }
  }

  private void removeSelectedBinding() {
    int row = this.bindingsTable.getSelectedRow();
    if (row >= 0 && row < this.bindingsList.size()) {
      this.bindingsList.remove(row);
      this.bindingsTableModel.removeRow(row);
    }
  }

  private void openSelectedScript() {
    int row = this.bindingsTable.getSelectedRow();
    if (row >= 0 && row < this.bindingsList.size()) {
      String scriptId = this.bindingsList.get(row).getScript();
      if (Game.scripts() != null) {
        for (ScriptDefinition def : Game.scripts().getDefinitions()) {
          if (scriptId.equals(def.getId()) || scriptId.equals(def.getName())) {
            this.dispose();
            UI.getScriptWorkspacePanel().open(def);
            return;
          }
        }
      }
    }
  }

  private void applyChanges() {
    // Update enabled states from table
    for (int i = 0; i < this.bindingsTableModel.getRowCount() && i < this.bindingsList.size(); i++) {
      Boolean enabled = (Boolean) this.bindingsTableModel.getValueAt(i, 1);
      this.bindingsList.get(i).setEnabled(enabled != null && enabled);
    }

    // Ensure selected primary script is added if not present
    ScriptItem primary = (ScriptItem) this.startupScriptCombo.getSelectedItem();
    if (primary != null && primary.id != null) {
      boolean found = false;
      for (ScriptBinding b : this.bindingsList) {
        if (primary.id.equals(b.getScript())) {
          found = true;
          break;
        }
      }
      if (!found) {
        this.bindingsList.add(0, new ScriptBinding(primary.id, true));
      }
    }

    if (Game.scripts() != null) {
      Game.scripts().setGameBindings(this.bindingsList);
    }
    if (Editor.instance().getGameFile() != null) {
      Editor.instance().getGameFile().getGameScripts().clear();
      Editor.instance().getGameFile().getGameScripts().addAll(this.bindingsList);
      Editor.instance().save(false);
    }
  }

  private record ScriptItem(String label, String id) {
    @Override
    public String toString() {
      return label;
    }
  }
}
