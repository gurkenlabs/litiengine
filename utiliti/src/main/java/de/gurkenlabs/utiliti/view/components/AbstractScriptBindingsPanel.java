package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.dialogs.ScriptEventExplorerDialog;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/**
 * Reusable abstract inspector panel for inspecting, attaching, reordering, and configuring script bindings.
 *
 * @param <T> The target data source type (e.g. IMapObject or IMap)
 */
public abstract class AbstractScriptBindingsPanel<T> extends JPanel {
  protected static final String CARD_EMPTY = "empty";
  protected static final String CARD_CONTENT = "content";
  protected static final String PARAM_EMPTY = "empty";
  protected static final String PARAM_NONE = "none";
  protected static final String PARAM_TABLE = "table";

  protected final DefaultListModel<ScriptBinding> bindingsModel = new DefaultListModel<>();
  protected final JList<ScriptBinding> bindings = new JList<>(this.bindingsModel);
  protected final JComboBox<ScriptDefinition> availableScripts = new JComboBox<>();
  protected final JCheckBox enabled = new JCheckBox("Enabled");
  protected final DefaultTableModel parameters = new DefaultTableModel(new Object[] {"Property", "Value"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
  };
  protected final JTable parameterTable = new JTable(this.parameters);
  protected final JButton newScriptButton;
  protected final JButton addButton;
  protected final JButton removeButton;
  protected final JButton openButton;
  protected final JButton upButton;
  protected final JButton downButton;

  protected final CardLayout mainCardLayout = new CardLayout();
  protected final JPanel mainContainer = new JPanel(this.mainCardLayout);

  protected final CardLayout paramCardLayout = new CardLayout();
  protected final JPanel paramContainer = new JPanel(this.paramCardLayout);

  protected final JLabel scriptsHeaderLabel = new JLabel("Attached Scripts (0)");

  protected T currentSource;
  protected boolean updating;

  protected AbstractScriptBindingsPanel() {
    this.newScriptButton = Style.iconButton(Icons.SCRIPT_16);
    this.addButton = Style.iconButton(Icons.ADD_16);
    this.removeButton = Style.iconButton(Icons.DELETE_16);
    this.openButton = Style.iconButton(Icons.PENCIL_16);
    this.upButton = Style.iconButton(Icons.LIFT_16);
    this.downButton = Style.iconButton(Icons.LOWER_16);
    this.initUI();
  }

  private void initUI() {
    this.setLayout(new BorderLayout(0, Style.SPACE_SMALL));
    this.setOpaque(false);

    this.availableScripts.setRenderer((list, value, index, selected, focused) -> {
      JLabel label = new JLabel();
      if (value == null) {
        label.setText(this.getNoCompatibleScriptsText());
        label.setForeground(Style.mutedText());
        label.setIcon(Icons.API_16);
      } else {
        label.setText(this.formatAvailableScriptLabel(value));
        label.setIcon(Icons.SYMBOL_METHOD_16);
      }
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : (value == null ? Style.mutedText() : list.getForeground()));
      label.setFont(list.getFont());
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.availableScripts.addActionListener(event -> this.updateButtonStates());

    this.newScriptButton.setToolTipText(this.getNewScriptTooltip());
    this.newScriptButton.addActionListener(event -> this.createNewScript());

    this.addButton.setToolTipText("Attach selected script");
    this.addButton.addActionListener(event -> this.addSelectedScript());

    this.removeButton.setToolTipText("Remove selected script binding (Delete)");
    this.removeButton.addActionListener(event -> this.removeSelectedScript());

    this.openButton.setToolTipText("Open selected script in editor");
    this.openButton.addActionListener(event -> this.openSelectedScript());

    this.upButton.setToolTipText("Move up");
    this.upButton.addActionListener(event -> this.moveSelectedScript(-1));

    this.downButton.setToolTipText("Move down");
    this.downButton.addActionListener(event -> this.moveSelectedScript(1));

    JButton helpButton = Style.iconButton(Icons.API_16);
    helpButton.setToolTipText("Explore script events and API cheat sheet");
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
          + (definition == null ? (value == null ? "" : value.getScript()) : displayName(definition)));
      label.setIcon(Icons.SYMBOL_METHOD_16);
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
      label.setFont(list.getFont());
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.bindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        this.bindSelection();
        this.updateButtonStates();
      }
    });
    this.bindings.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) openSelectedScript();
      }
    });
    this.bindings.addKeyListener(new java.awt.event.KeyAdapter() {
      @Override public void keyPressed(java.awt.event.KeyEvent e) {
        if (e.getKeyCode() == java.awt.event.KeyEvent.VK_DELETE || e.getKeyCode() == java.awt.event.KeyEvent.VK_BACK_SPACE) {
          removeSelectedScript();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
          openSelectedScript();
        } else if (e.getKeyCode() == java.awt.event.KeyEvent.VK_SPACE) {
          if (bindings.getSelectedValue() != null) {
            enabled.setSelected(!enabled.isSelected());
            bindings.getSelectedValue().setEnabled(enabled.isSelected());
            persist();
            bindings.repaint();
          }
        }
      }
    });

    JPopupMenu popup = new JPopupMenu();
    JMenuItem openItem = new JMenuItem("Open Script", Icons.PENCIL_16);
    openItem.addActionListener(event -> this.openSelectedScript());
    JMenuItem removeItem = new JMenuItem("Remove Binding", Icons.DELETE_16);
    removeItem.addActionListener(event -> this.removeSelectedScript());
    JMenuItem upItem = new JMenuItem("Move Up", Icons.LIFT_16);
    upItem.addActionListener(event -> this.moveSelectedScript(-1));
    JMenuItem downItem = new JMenuItem("Move Down", Icons.LOWER_16);
    downItem.addActionListener(event -> this.moveSelectedScript(1));
    popup.add(openItem);
    popup.addSeparator();
    popup.add(removeItem);
    popup.addSeparator();
    popup.add(upItem);
    popup.add(downItem);
    this.bindings.setComponentPopupMenu(popup);

    this.parameterTable.setRowHeight(Style.TREE_ROW_HEIGHT);
    this.parameterTable.setShowGrid(false);
    this.parameterTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.parameterTable.getColumnModel().getColumn(0).setPreferredWidth(100);
    this.parameterTable.getColumnModel().getColumn(1).setPreferredWidth(120);

    JPanel emptyCard = this.createEmptyStatePanel();

    JPanel scriptsPanel = new JPanel(new BorderLayout(0, 2));
    scriptsPanel.setOpaque(false);
    this.scriptsHeaderLabel.setFont(this.scriptsHeaderLabel.getFont().deriveFont(Font.BOLD, 11f));
    this.scriptsHeaderLabel.setForeground(Style.mutedText());
    scriptsPanel.add(this.scriptsHeaderLabel, BorderLayout.NORTH);
    scriptsPanel.add(new JScrollPane(this.bindings), BorderLayout.CENTER);

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
    this.paramContainer.add(this.createParamEmptyPanel("Select an attached script above to configure its properties."), PARAM_EMPTY);
    this.paramContainer.add(this.createParamEmptyPanel("No @ScriptProperty parameters defined in this script."), PARAM_NONE);
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
    JLabel title = new JLabel(this.getEmptyStateTitle());
    title.setFont(title.getFont().deriveFont(Font.BOLD, 12f));
    title.setForeground(Style.text());
    panel.add(title, gbc);

    gbc.gridy = 2;
    gbc.insets = new Insets(2, 0, 0, 0);
    JLabel hint = new JLabel("<html><center style='color:#969eb9;'>" + this.getEmptyStateHint() + "</center></html>");
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

  protected void updateButtonStates() {
    int selectedIndex = this.bindings.getSelectedIndex();
    boolean hasSelection = selectedIndex >= 0;
    boolean hasAvailable = this.availableScripts.getSelectedItem() != null;
    int count = this.bindingsModel.size();

    this.scriptsHeaderLabel.setText("Attached Scripts (" + count + ")");
    this.newScriptButton.setEnabled(this.currentSource != null && Editor.instance().getGameFile() != null);
    this.addButton.setEnabled(hasAvailable);
    this.removeButton.setEnabled(hasSelection);
    this.openButton.setEnabled(hasSelection);
    this.upButton.setEnabled(hasSelection && selectedIndex > 0);
    this.downButton.setEnabled(hasSelection && selectedIndex < count - 1);
  }

  public T getDataSource() {
    return this.currentSource;
  }

  public void bind(T source) {
    this.currentSource = source;
    this.updating = true;
    try {
      this.bindingsModel.clear();
      if (source != null) {
        List<ScriptBinding> loaded = this.readBindings(source);
        if (loaded != null) {
          loaded.forEach(this.bindingsModel::addElement);
        }
        this.refreshAvailableScripts(source);
        if (!this.bindingsModel.isEmpty()) this.bindings.setSelectedIndex(0);
        else this.parameters.setRowCount(0);
      } else {
        this.parameters.setRowCount(0);
        this.availableScripts.removeAllItems();
        this.enabled.setSelected(false);
      }
    } finally {
      this.updating = false;
    }
    this.bindSelection();
    this.updateButtonStates();
  }

  public void refreshAvailableScripts() {
    this.refreshAvailableScripts(this.currentSource);
  }

  public void refreshAvailableScripts(T source) {
    List<ScriptDefinition> definitions = Editor.instance().getGameFile() != null && Editor.instance().getGameFile().getScripts() != null
        ? Editor.instance().getGameFile().getScripts().stream()
            .filter(definition -> definition.getHost() == this.getSupportedHostType())
            .filter(definition -> this.isScriptCompatible(definition, source))
            .sorted(Comparator.comparing(AbstractScriptBindingsPanel::displayName, String.CASE_INSENSITIVE_ORDER))
            .toList()
        : List.of();
    this.availableScripts.setModel(new DefaultComboBoxModel<>(definitions.toArray(ScriptDefinition[]::new)));
    this.updateButtonStates();
  }

  protected void addSelectedScript() {
    ScriptDefinition definition = (ScriptDefinition) this.availableScripts.getSelectedItem();
    if (definition == null || this.currentSource == null) return;
    ScriptBinding binding = new ScriptBinding(definition.getId());
    binding.setOrder(this.bindingsModel.size());
    this.bindingsModel.addElement(binding);
    this.bindings.setSelectedIndex(this.bindingsModel.size() - 1);
    this.persist();
    this.updateButtonStates();
  }

  protected void removeSelectedScript() {
    int index = this.bindings.getSelectedIndex();
    if (index < 0) return;
    this.bindingsModel.remove(index);
    for (int i = 0; i < this.bindingsModel.size(); i++) this.bindingsModel.get(i).setOrder(i);
    this.persist();
    if (!this.bindingsModel.isEmpty()) this.bindings.setSelectedIndex(Math.min(index, this.bindingsModel.size() - 1));
    else this.bindSelection();
    this.updateButtonStates();
  }

  protected void moveSelectedScript(int delta) {
    int index = this.bindings.getSelectedIndex();
    int target = index + delta;
    if (index < 0 || target < 0 || target >= this.bindingsModel.size()) return;
    ScriptBinding binding = this.bindingsModel.remove(index);
    this.bindingsModel.add(target, binding);
    for (int i = 0; i < this.bindingsModel.size(); i++) this.bindingsModel.get(i).setOrder(i);
    this.bindings.setSelectedIndex(target);
    this.persist();
    this.updateButtonStates();
  }

  protected void openSelectedScript() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    if (binding == null) return;
    ScriptDefinition definition = definition(binding.getScript());
    if (definition != null && UI.getScriptWorkspacePanel() != null) {
      UI.getScriptWorkspacePanel().open(definition);
    }
  }

  protected void bindSelection() {
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
      if (binding == null) {
        this.enabled.setEnabled(false);
        this.enabled.setSelected(false);
        this.paramCardLayout.show(this.paramContainer, PARAM_EMPTY);
        return;
      }
      this.enabled.setEnabled(true);
      this.enabled.setSelected(binding.isEnabled());

      Set<String> names = new LinkedHashSet<>();
      ScriptDefinition definition = definition(binding.getScript());
      var discovered = definition == null ? null : Editor.instance().getProjectCodeIntegration().getScriptDefinitions().stream()
          .filter(d -> d.id().equals(definition.getId())).findFirst().orElse(null);
      if (discovered != null) discovered.properties().forEach(property -> names.add(property.name()));
      Game.scripts().getPropertyMetadata(binding.getScript()).forEach(property -> names.add(property.name()));
      names.addAll(binding.getParameters().keySet());

      for (String name : names) {
        this.parameters.addRow(new Object[] {name, binding.getParameters().getOrDefault(name, "")});
      }

      if (this.parameters.getRowCount() == 0) {
        this.paramCardLayout.show(this.paramContainer, PARAM_NONE);
      } else {
        this.paramCardLayout.show(this.paramContainer, PARAM_TABLE);
      }
    } finally {
      this.updating = false;
    }
  }

  protected void applyParameterValues() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    if (binding == null) return;
    binding.getParameterValues().clear();
    for (int row = 0; row < this.parameters.getRowCount(); row++) {
      String name = Objects.toString(this.parameters.getValueAt(row, 0), "").trim();
      if (!name.isEmpty()) binding.setParameter(name, Objects.toString(this.parameters.getValueAt(row, 1), ""));
    }
    this.persist();
  }

  protected void persist() {
    if (this.currentSource == null) return;
    List<ScriptBinding> list = new ArrayList<>();
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      list.add(this.bindingsModel.get(i));
    }
    this.persistBindings(this.currentSource, list);
  }

  protected String formatAvailableScriptLabel(ScriptDefinition definition) {
    return displayName(definition);
  }

  public static String displayName(ScriptDefinition definition) {
    if (definition == null) return "";
    if (definition.getName() != null && !definition.getName().isBlank()) return definition.getName();
    if (definition.getId() != null && !definition.getId().isBlank()) return definition.getId();
    return definition.getSource() == null ? "" : definition.getSource();
  }

  public static ScriptDefinition definition(String id) {
    if (id == null || id.isBlank() || Editor.instance().getGameFile() == null) return null;
    return Editor.instance().getGameFile().getScripts().stream()
        .filter(d -> id.equals(d.getId()) || id.equals(d.getName()) || id.equals(d.getSource()))
        .findFirst().orElse(null);
  }

  protected abstract List<ScriptBinding> readBindings(T source);
  protected abstract void persistBindings(T source, List<ScriptBinding> bindings);
  protected abstract boolean isScriptCompatible(ScriptDefinition definition, T source);
  protected abstract void createNewScript();
  protected abstract String getEmptyStateTitle();
  protected abstract String getEmptyStateHint();
  protected abstract String getNoCompatibleScriptsText();
  protected abstract String getNewScriptTooltip();
  protected abstract ScriptHostType getSupportedHostType();
}
