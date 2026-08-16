package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptManager;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.*;
import java.util.List;

/**
 * Inline script attachment and property inspector for map environment scripts.
 */
public final class EnvironmentScriptInspectorPanel extends JPanel {
  private final DefaultListModel<ScriptBinding> bindingsModel = new DefaultListModel<>();
  private final JList<ScriptBinding> bindings = new JList<>(this.bindingsModel);
  private final JComboBox<ScriptDefinition> availableScripts = new JComboBox<>();
  private final JCheckBox enabled = new JCheckBox("Enabled");
  private final DefaultTableModel parameters = new DefaultTableModel(new Object[]{"Property", "Value"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
  };
  private final JTable parameterTable = new JTable(this.parameters);
  private final JButton newScriptButton;
  private final JButton addButton;
  private final JButton removeButton;
  private final JButton openButton;
  private final JButton upButton;
  private final JButton downButton;

  private static final String CARD_EMPTY = "empty";
  private static final String CARD_CONTENT = "content";
  private static final String PARAM_EMPTY = "empty";
  private static final String PARAM_NONE = "none";
  private static final String PARAM_TABLE = "table";

  private final CardLayout mainCardLayout = new CardLayout();
  private final JPanel mainContainer = new JPanel(this.mainCardLayout);

  private final CardLayout paramCardLayout = new CardLayout();
  private final JPanel paramContainer = new JPanel(this.paramCardLayout);

  private final JLabel scriptsHeaderLabel = new JLabel("Attached Scripts (0)");

  private IMap dataSource;
  private boolean updating;

  public EnvironmentScriptInspectorPanel() {
    this.setLayout(new BorderLayout(0, Style.SPACE_SMALL));
    this.setOpaque(false);

    this.availableScripts.setRenderer((list, value, index, selected, focused) -> {
      JLabel label = new JLabel();
      if (value == null) {
        label.setText("<No compatible environment scripts>");
        label.setForeground(Style.mutedText());
        label.setIcon(Icons.API_16);
      } else {
        label.setText(displayName(value));
        label.setIcon(Icons.SYMBOL_METHOD_16);
      }
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : (value == null ? Style.mutedText() : list.getForeground()));
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.availableScripts.addActionListener(event -> this.updateButtonStates());

    this.newScriptButton = Style.iconButton(Icons.SCRIPT_16);
    this.newScriptButton.setToolTipText("Create a new environment script and attach it");
    this.newScriptButton.addActionListener(event -> this.createNewScript());

    this.addButton = Style.iconButton(Icons.ADD_16);
    this.addButton.setToolTipText("Attach selected environment script");
    this.addButton.addActionListener(event -> this.addSelectedScript());

    this.removeButton = Style.iconButton(Icons.DELETE_16);
    this.removeButton.setToolTipText("Remove selected script binding");
    this.removeButton.addActionListener(event -> this.removeSelectedScript());

    this.openButton = Style.iconButton(Icons.PENCIL_16);
    this.openButton.setToolTipText("Open selected script in editor");
    this.openButton.addActionListener(event -> this.openSelectedScript());

    this.upButton = Style.iconButton(Icons.LIFT_16);
    this.upButton.setToolTipText("Move up");
    this.upButton.addActionListener(event -> this.moveSelectedScript(-1));

    this.downButton = Style.iconButton(Icons.LOWER_16);
    this.downButton.setToolTipText("Move down");
    this.downButton.addActionListener(event -> this.moveSelectedScript(1));

    JButton helpButton = Style.iconButton(Icons.API_16);
    helpButton.setToolTipText("Explore script events and API guide");
    helpButton.addActionListener(event -> ScriptEventExplorerDialog.showDialog());

    JPanel toolButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 2, 0));
    toolButtons.setOpaque(false);
    toolButtons.add(this.newScriptButton);
    toolButtons.add(this.addButton);
    toolButtons.add(this.removeButton);
    toolButtons.add(this.openButton);
    toolButtons.add(this.upButton);
    toolButtons.add(this.downButton);
    toolButtons.add(helpButton);

    JPanel picker = new JPanel(new BorderLayout(Style.SPACE_SMALL, 0));
    picker.setOpaque(false);
    picker.add(this.availableScripts, BorderLayout.CENTER);
    picker.add(toolButtons, BorderLayout.EAST);
    this.add(picker, BorderLayout.NORTH);

    this.bindings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.bindings.setVisibleRowCount(4);
    this.bindings.setCellRenderer((list, value, index, selected, focused) -> {
      ScriptDefinition definition = definition(value == null ? null : value.getScript());
      JLabel label = new JLabel((value != null && value.isEnabled() ? "" : "(disabled) ")
          + (definition == null ? value == null ? "" : value.getScript() : displayName(definition)));
      label.setIcon(Icons.SYMBOL_METHOD_16);
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
      label.setFont(list.getFont());
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.bindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) this.bindSelection();
    });

    this.parameterTable.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.parameterTable.setShowGrid(false);
    this.parameterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.parameterTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    this.parameterTable.getColumnModel().getColumn(1).setPreferredWidth(120);

    // Empty state card
    JPanel emptyCard = createEmptyStatePanel();

    // Top section: Attached scripts
    JPanel scriptsPanel = new JPanel(new BorderLayout(0, 2));
    scriptsPanel.setOpaque(false);
    this.scriptsHeaderLabel.setFont(this.scriptsHeaderLabel.getFont().deriveFont(Font.BOLD, 11f));
    this.scriptsHeaderLabel.setForeground(Style.mutedText());
    scriptsPanel.add(this.scriptsHeaderLabel, BorderLayout.NORTH);
    scriptsPanel.add(new JScrollPane(this.bindings), BorderLayout.CENTER);

    // Bottom section: Parameters
    JPanel details = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    details.setOpaque(false);

    JPanel detailsHeader = new JPanel(new BorderLayout());
    detailsHeader.setOpaque(false);
    detailsHeader.add(this.enabled, BorderLayout.WEST);
    JLabel propTitle = new JLabel("Script Properties");
    propTitle.setFont(propTitle.getFont().deriveFont(Font.BOLD, 11f));
    propTitle.setForeground(Style.mutedText());
    detailsHeader.add(propTitle, BorderLayout.EAST);
    details.add(detailsHeader, BorderLayout.NORTH);

    this.paramContainer.setOpaque(false);
    this.paramContainer.add(createParamEmptyPanel("Select an attached script above to configure its properties."), PARAM_EMPTY);
    this.paramContainer.add(createParamEmptyPanel("No @ScriptProperty parameters defined in this script."), PARAM_NONE);
    this.paramContainer.add(new JScrollPane(this.parameterTable), PARAM_TABLE);
    details.add(this.paramContainer, BorderLayout.CENTER);

    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, scriptsPanel, details);
    UI.configureSplitPane(split);
    split.setResizeWeight(0.45);
    split.setDividerLocation(90);
    split.setPreferredSize(new Dimension(0, 210));

    this.mainContainer.setOpaque(false);
    this.mainContainer.add(emptyCard, CARD_EMPTY);
    this.mainContainer.add(split, CARD_CONTENT);
    this.add(this.mainContainer, BorderLayout.CENTER);

    this.enabled.addActionListener(event -> {
      if (this.updating || this.bindings.getSelectedValue() == null) return;
      this.bindings.getSelectedValue().setEnabled(this.enabled.isSelected());
      this.persist();
      this.bindings.repaint();
    });

    this.parameters.addTableModelListener(event -> {
      if (!this.updating) this.applyParameterValues();
    });

    this.updateButtonStates();
  }

  private JPanel createEmptyStatePanel() {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(true);
    panel.setBackground(Style.surface());
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Style.border()),
        BorderFactory.createEmptyBorder(12, 12, 12, 12)));

    GridBagConstraints gbc = new GridBagConstraints();
    gbc.gridx = 0;
    gbc.gridy = 0;
    gbc.insets = new Insets(0, 0, 4, 0);
    gbc.anchor = GridBagConstraints.CENTER;

    JLabel iconLabel = new JLabel(Icons.API_16);
    panel.add(iconLabel, gbc);

    gbc.gridy = 1;
    JLabel title = new JLabel("No Environment Scripts Attached");
    title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
    title.setForeground(Style.text());
    panel.add(title, gbc);

    gbc.gridy = 2;
    gbc.insets = new Insets(2, 0, 0, 0);
    JLabel hint = new JLabel("<html><center style='color:#969eb9;'>Select a script above and click <b>'+'</b><br>to bind map-level objectives & cinematics.</center></html>");
    hint.setFont(hint.getFont().deriveFont(11f));
    panel.add(hint, gbc);

    gbc.gridy = 3;
    gbc.insets = new Insets(8, 0, 0, 0);
    JButton createBtn = new JButton("Create New Script", Icons.SCRIPT_16);
    createBtn.setFont(createBtn.getFont().deriveFont(11f));
    createBtn.addActionListener(event -> this.createNewScript());
    panel.add(createBtn, gbc);

    return panel;
  }

  private JPanel createParamEmptyPanel(String message) {
    JPanel panel = new JPanel(new GridBagLayout());
    panel.setOpaque(true);
    panel.setBackground(Style.surface());
    panel.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Style.border()),
        BorderFactory.createEmptyBorder(6, 6, 6, 6)));

    JLabel hint = new JLabel("<html><center style='color:#969eb9;'>" + message + "</center></html>");
    hint.setFont(hint.getFont().deriveFont(11f));
    panel.add(hint);

    return panel;
  }

  public void bind(IMap map) {
    this.dataSource = map;
    if (map == null) {
      this.clearControls();
      return;
    }
    this.updating = true;
    try {
      this.bindingsModel.clear();
      String encoded = map.getStringValue(ScriptManager.BINDINGS_PROPERTY, null);
      if (encoded != null && !encoded.isBlank()) {
        ScriptBindingCodec.decode(encoded).forEach(this.bindingsModel::addElement);
      }
      this.refreshAvailableScripts();
      if (!this.bindingsModel.isEmpty()) {
        this.bindings.setSelectedIndex(0);
      } else {
        this.parameters.setRowCount(0);
      }
    } catch (IllegalArgumentException ignored) {
      this.bindingsModel.clear();
      this.parameters.setRowCount(0);
    } finally {
      this.updating = false;
    }
    this.bindSelection();
  }

  private void clearControls() {
    this.updating = true;
    try {
      this.bindingsModel.clear();
      this.parameters.setRowCount(0);
      this.availableScripts.removeAllItems();
      this.enabled.setSelected(false);
    } finally {
      this.updating = false;
    }
    this.mainCardLayout.show(this.mainContainer, CARD_EMPTY);
    this.updateButtonStates();
  }

  public void refreshAvailableScripts() {
    if (Editor.instance().getGameFile() == null || Editor.instance().getGameFile().getScripts() == null) {
      this.availableScripts.setModel(new DefaultComboBoxModel<>());
      this.updateButtonStates();
      return;
    }
    List<ScriptDefinition> definitions = Editor.instance().getGameFile().getScripts().stream()
        .filter(definition -> definition.getHost() == ScriptHostType.ENVIRONMENT)
        .sorted(Comparator.comparing(EnvironmentScriptInspectorPanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .toList();
    this.availableScripts.setModel(new DefaultComboBoxModel<>(definitions.toArray(ScriptDefinition[]::new)));
    this.updateButtonStates();
  }

  private void createNewScript() {
    if (UI.getScriptWorkspacePanel() == null || this.dataSource == null) return;
    ScriptDefinition created = UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.ENVIRONMENT, null);
    if (created != null) {
      this.refreshAvailableScripts();
      this.availableScripts.setSelectedItem(created);
      this.addSelectedScript();
    }
  }

  private void addSelectedScript() {
    ScriptDefinition definition = (ScriptDefinition) this.availableScripts.getSelectedItem();
    if (definition == null || this.dataSource == null) return;
    ScriptBinding binding = new ScriptBinding(definition.getId());
    binding.setOrder(this.bindingsModel.size());
    this.bindingsModel.addElement(binding);
    this.bindings.setSelectedIndex(this.bindingsModel.size() - 1);
    this.persist();
    this.updateButtonStates();
  }

  private void removeSelectedScript() {
    int index = this.bindings.getSelectedIndex();
    if (index < 0) return;
    this.bindingsModel.remove(index);
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      this.bindingsModel.get(i).setOrder(i);
    }
    this.persist();
    if (!this.bindingsModel.isEmpty()) {
      this.bindings.setSelectedIndex(Math.min(index, this.bindingsModel.size() - 1));
    } else {
      this.bindSelection();
    }
    this.updateButtonStates();
  }

  private void moveSelectedScript(int delta) {
    int index = this.bindings.getSelectedIndex();
    int target = index + delta;
    if (index < 0 || target < 0 || target >= this.bindingsModel.size()) return;
    ScriptBinding binding = this.bindingsModel.remove(index);
    this.bindingsModel.add(target, binding);
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      this.bindingsModel.get(i).setOrder(i);
    }
    this.bindings.setSelectedIndex(target);
    this.persist();
    this.updateButtonStates();
  }

  private void bindSelection() {
    this.updating = true;
    try {
      this.parameters.setRowCount(0);
      if (this.bindingsModel.isEmpty()) {
        this.mainCardLayout.show(this.mainContainer, CARD_EMPTY);
        this.enabled.setEnabled(false);
        this.enabled.setSelected(false);
        return;
      }
      this.mainCardLayout.show(this.mainContainer, CARD_CONTENT);

      ScriptBinding binding = this.bindings.getSelectedValue();
      this.enabled.setEnabled(binding != null);
      this.enabled.setSelected(binding != null && binding.isEnabled());
      if (binding == null) {
        this.paramCardLayout.show(this.paramContainer, PARAM_EMPTY);
        return;
      }
      Set<String> names = new LinkedHashSet<>();
      var discovered = Editor.instance().getProjectCodeIntegration().getScriptDefinitions().stream()
          .filter(candidate -> candidate.id().equals(binding.getScript())).findFirst().orElse(null);
      if (discovered != null) {
        discovered.properties().forEach(property -> names.add(property.name()));
      }
      de.gurkenlabs.litiengine.Game.scripts().getPropertyMetadata(binding.getScript())
          .forEach(property -> names.add(property.name()));
      names.addAll(binding.getParameters().keySet());
      for (String name : names) {
        this.parameters.addRow(new Object[]{name, binding.getParameters().getOrDefault(name, "")});
      }

      if (this.parameters.getRowCount() == 0) {
        this.paramCardLayout.show(this.paramContainer, PARAM_NONE);
      } else {
        this.paramCardLayout.show(this.paramContainer, PARAM_TABLE);
      }
    } finally {
      this.updating = false;
    }
    this.updateButtonStates();
  }

  private void applyParameterValues() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    if (binding == null) return;
    binding.getParameterValues().clear();
    for (int row = 0; row < this.parameters.getRowCount(); row++) {
      String name = Objects.toString(this.parameters.getValueAt(row, 0), "").trim();
      if (!name.isEmpty()) {
        binding.setParameter(name, Objects.toString(this.parameters.getValueAt(row, 1), ""));
      }
    }
    this.persist();
  }

  private void persist() {
    if (this.dataSource == null || this.updating) return;
    List<ScriptBinding> result = new ArrayList<>();
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      result.add(this.bindingsModel.get(i));
    }
    String encoded = ScriptBindingCodec.encode(result);
    UndoManager.instance().mapChanging(this.dataSource);
    if (result.isEmpty()) {
      this.dataSource.removeProperty(ScriptManager.BINDINGS_PROPERTY);
    } else {
      this.dataSource.setValue(ScriptManager.BINDINGS_PROPERTY, encoded);
    }
    UndoManager.instance().mapChanged(this.dataSource);
    if (UI.getScriptWorkspacePanel() != null) {
      UI.getScriptWorkspacePanel().refreshProblemsTable();
    }
  }

  private void openSelectedScript() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    ScriptDefinition definition = definition(binding == null ? null : binding.getScript());
    if (definition != null) {
      UI.openScript(definition);
    }
  }

  private void updateButtonStates() {
    int selectedIndex = this.bindings.getSelectedIndex();
    boolean hasSelection = selectedIndex >= 0;
    boolean hasAvailable = this.availableScripts.getSelectedItem() != null;
    int count = this.bindingsModel.size();

    this.scriptsHeaderLabel.setText("Attached Scripts (" + count + ")");
    this.newScriptButton.setEnabled(this.dataSource != null && Editor.instance().getGameFile() != null);
    this.addButton.setEnabled(hasAvailable);
    this.removeButton.setEnabled(hasSelection);
    this.openButton.setEnabled(hasSelection);
    this.upButton.setEnabled(hasSelection && selectedIndex > 0);
    this.downButton.setEnabled(hasSelection && selectedIndex < count - 1);
  }

  private static ScriptDefinition definition(String id) {
    if (id == null || Editor.instance().getGameFile() == null) return null;
    return Editor.instance().getGameFile().getScripts().stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
  }

  private static String displayName(ScriptDefinition definition) {
    if (definition == null) return "(None)";
    if (definition.getName() != null && !definition.getName().isBlank()) {
      return definition.getName() + " (" + definition.getId() + ")";
    }
    return definition.getId();
  }
}
