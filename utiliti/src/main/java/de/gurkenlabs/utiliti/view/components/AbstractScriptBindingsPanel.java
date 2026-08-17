package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.BindingState;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.EntityBindingState;
import de.gurkenlabs.utiliti.controller.ScriptBindingService;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
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
import java.awt.datatransfer.StringSelection;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.LinkedHashMap;
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
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;

/**
 * Reusable abstract inspector panel for inspecting, attaching, reordering, and configuring script bindings.
 *
 * @param <T> The target data source type (e.g. IMapObject or IMap)
 */
public abstract class AbstractScriptBindingsPanel<T> extends JPanel {
  protected static final String CARD_EMPTY = "empty";
  protected static final String CARD_CONTENT = "content";
  protected static final String CARD_INVALID = "invalid";
  protected static final String PARAM_EMPTY = "empty";
  protected static final String PARAM_NONE = "none";
  protected static final String PARAM_TABLE = "table";

  protected final DefaultListModel<ScriptBinding> bindingsModel = new DefaultListModel<>();
  protected final JList<ScriptBinding> bindings = new JList<>(this.bindingsModel);
  private final DefaultListModel<ScriptBinding> inheritedModel = new DefaultListModel<>();
  private final JList<ScriptBinding> inheritedBindings = new JList<>(this.inheritedModel);
  protected final JComboBox<ScriptDefinition> availableScripts = new JComboBox<>();
  protected final JTextField scriptSearch = UI.createSearchTextField("Search compatible scripts...");
  protected final JCheckBox enabled = new JCheckBox("Enabled");
  protected final DefaultTableModel parameters = new DefaultTableModel(new Object[] {"Property", "Value"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
  };
  private final java.util.Map<String, ParameterDescriptor> parameterDescriptors = new LinkedHashMap<>();
  private final Set<String> explicitParameterNames = new LinkedHashSet<>();
  protected final JTable parameterTable = new JTable(this.parameters) {
    @Override public TableCellEditor getCellEditor(int row, int column) {
      return column == 1 ? createParameterEditor(row) : super.getCellEditor(row, column);
    }
  };
  protected final JButton newScriptButton;
  protected final JButton addButton;
  protected final JButton removeButton;
  protected final JButton openButton;
  protected final JButton upButton;
  protected final JButton downButton;
  private final JButton overrideButton;

  protected final CardLayout mainCardLayout = new CardLayout();
  protected final JPanel mainContainer = new JPanel(this.mainCardLayout);

  protected final CardLayout paramCardLayout = new CardLayout();
  protected final JPanel paramContainer = new JPanel(this.paramCardLayout);

  protected final JLabel scriptsHeaderLabel = new JLabel("Attached Scripts (0)");
  private final JLabel invalidMessage = new JLabel();
  private final javax.swing.JTextArea invalidRawValue = new javax.swing.JTextArea();
  private final ScriptBindingService bindingService = ScriptBindingService.instance();
  private List<ScriptDefinition> compatibleScripts = List.of();
  private boolean bindingStateEditable;

  protected T currentSource;
  protected ScriptBindingTarget currentTarget;
  protected boolean updating;

  protected AbstractScriptBindingsPanel() {
    this.newScriptButton = Style.iconButton(Icons.SCRIPT_16);
    this.addButton = Style.iconButton(Icons.ADD_16);
    this.removeButton = Style.iconButton(Icons.DELETE_16);
    this.openButton = Style.iconButton(Icons.PENCIL_16);
    this.upButton = Style.iconButton(Icons.LIFT_16);
    this.downButton = Style.iconButton(Icons.LOWER_16);
    this.overrideButton = new JButton("Override", Icons.COPY_16);
    this.initUI();
    this.bindingService.addChangeListener(this::bindingChanged);
  }

  private void initUI() {
    this.setLayout(new BorderLayout(0, Style.SPACE_SMALL));
    this.setOpaque(false);
    this.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

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
    this.scriptSearch.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
      @Override public void insertUpdate(javax.swing.event.DocumentEvent event) { filterAvailableScripts(); }
      @Override public void removeUpdate(javax.swing.event.DocumentEvent event) { filterAvailableScripts(); }
      @Override public void changedUpdate(javax.swing.event.DocumentEvent event) { filterAvailableScripts(); }
    });

    this.newScriptButton.setToolTipText(this.getNewScriptTooltip());
    this.newScriptButton.addActionListener(event -> this.createNewScript());

    this.addButton.setToolTipText("Attach selected script");
    this.addButton.addActionListener(event -> this.addSelectedScript());

    this.removeButton.setToolTipText("Remove selected script (Delete)");
    this.removeButton.addActionListener(event -> this.removeSelectedScript());

    this.openButton.setToolTipText("Open selected script in editor");
    this.openButton.addActionListener(event -> this.openSelectedScript());

    this.upButton.setToolTipText("Move up");
    this.upButton.addActionListener(event -> this.moveSelectedScript(-1));

    this.downButton.setToolTipText("Move down");
    this.downButton.addActionListener(event -> this.moveSelectedScript(1));

    this.overrideButton.setToolTipText("Create an editable instance override from the selected inherited script");
    this.overrideButton.addActionListener(event -> this.overrideSelectedInheritedBinding());

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
    JPanel pickerStack = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    pickerStack.setOpaque(false);
    RoundedSearchBox searchBox = new RoundedSearchBox(this.scriptSearch, 0);
    searchBox.getClearButton().addActionListener(event -> {
      this.scriptSearch.setText("");
      filterAvailableScripts();
    });
    pickerStack.add(searchBox, BorderLayout.NORTH);
    pickerStack.add(picker, BorderLayout.CENTER);
    this.add(pickerStack, BorderLayout.NORTH);

    this.bindings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.bindings.setVisibleRowCount(4);
    this.bindings.setCellRenderer((list, value, index, selected, focused) -> {
      ScriptDefinition definition = definition(value == null ? null : value.getScript());
      boolean duplicate = value != null && countBindings(value.getScript()) > 1;
      String prefix = duplicate ? "⚠ Duplicate · " : "";
      if (value != null && this.isInherited(value.getScript())) prefix += "Override · ";
      if (value != null && !value.isEnabled()) prefix += "Disabled · ";
      JLabel label = new JLabel(prefix
          + (definition == null ? (value == null ? "" : value.getScript()) : displayName(definition)));
      label.setIcon(Icons.SYMBOL_METHOD_16);
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground()
        : duplicate || definition == null ? Style.COLOR_ORANGE : list.getForeground());
      label.setFont(list.getFont());
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.bindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) {
        if (this.bindings.getSelectedIndex() >= 0) this.inheritedBindings.clearSelection();
        this.bindSelection();
        this.updateButtonStates();
      }
    });

    this.inheritedBindings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.inheritedBindings.setVisibleRowCount(2);
    this.inheritedBindings.setCellRenderer((list, value, index, selected, focused) -> {
      ScriptDefinition definition = definition(value == null ? null : value.getScript());
      boolean overridden = value != null && this.containsBinding(value.getScript());
      JLabel label = new JLabel((overridden ? "Overridden · " : "Inherited · ")
        + (definition == null ? (value == null ? "" : value.getScript()) : displayName(definition)));
      label.setIcon(Icons.SYMBOL_DEPENDENCY_16);
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : Style.mutedText());
      label.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 4));
      return label;
    });
    this.inheritedBindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting() && this.inheritedBindings.getSelectedIndex() >= 0) {
        this.bindings.clearSelection();
        this.bindSelection();
        this.updateButtonStates();
      }
    });
    this.inheritedBindings.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) openBinding(inheritedBindings.getSelectedValue());
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
    JMenuItem removeItem = new JMenuItem("Remove Script", Icons.DELETE_16);
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

    JPanel scriptsPanel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    scriptsPanel.setOpaque(false);
    this.scriptsHeaderLabel.setFont(this.scriptsHeaderLabel.getFont().deriveFont(Font.BOLD, 11f));
    this.scriptsHeaderLabel.setForeground(Style.mutedText());
    JPanel explicitPanel = new JPanel(new BorderLayout(0, 2));
    explicitPanel.setOpaque(false);
    explicitPanel.add(this.scriptsHeaderLabel, BorderLayout.NORTH);
    explicitPanel.add(new JScrollPane(this.bindings), BorderLayout.CENTER);

    JPanel inheritedPanel = new JPanel(new BorderLayout(Style.SPACE_SMALL, 2));
    inheritedPanel.setOpaque(false);
    JLabel inheritedHeader = new JLabel("Inherited Defaults");
    inheritedHeader.setFont(inheritedHeader.getFont().deriveFont(Font.BOLD, 11f));
    inheritedHeader.setForeground(Style.mutedText());
    JPanel inheritedTitle = new JPanel(new BorderLayout());
    inheritedTitle.setOpaque(false);
    inheritedTitle.add(inheritedHeader, BorderLayout.WEST);
    inheritedTitle.add(this.overrideButton, BorderLayout.EAST);
    inheritedPanel.add(inheritedTitle, BorderLayout.NORTH);
    JScrollPane inheritedScroll = new JScrollPane(this.inheritedBindings);
    inheritedScroll.setPreferredSize(new Dimension(0, 58));
    inheritedPanel.add(inheritedScroll, BorderLayout.CENTER);
    inheritedPanel.setVisible(false);
    inheritedPanel.setName("inheritedBindingsPanel");
    scriptsPanel.add(inheritedPanel, BorderLayout.NORTH);
    scriptsPanel.add(explicitPanel, BorderLayout.CENTER);

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
    this.mainContainer.add(this.createInvalidStatePanel(), CARD_INVALID);
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

  private JPanel createInvalidStatePanel() {
    JPanel panel = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    panel.setOpaque(true);
    panel.setBackground(Style.surface());
    panel.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createLineBorder(Style.COLOR_RED),
      BorderFactory.createEmptyBorder(10, 10, 10, 10)));

    JLabel title = new JLabel("Script assignment data could not be read", Icons.ERROR_16, JLabel.LEADING);
    title.setFont(title.getFont().deriveFont(Font.BOLD));
    title.setForeground(Style.COLOR_RED);
    JPanel header = new JPanel(new BorderLayout(0, 3));
    header.setOpaque(false);
    header.add(title, BorderLayout.NORTH);
    this.invalidMessage.setForeground(Style.mutedText());
    header.add(this.invalidMessage, BorderLayout.CENTER);
    panel.add(header, BorderLayout.NORTH);

    this.invalidRawValue.setEditable(false);
    this.invalidRawValue.setLineWrap(true);
    this.invalidRawValue.setWrapStyleWord(true);
    this.invalidRawValue.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN, 11));
    panel.add(new JScrollPane(this.invalidRawValue), BorderLayout.CENTER);

    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, 4, 0));
    actions.setOpaque(false);
    JButton copy = new JButton("Copy Raw Value", Icons.COPY_16);
    copy.addActionListener(event -> java.awt.Toolkit.getDefaultToolkit().getSystemClipboard()
      .setContents(new StringSelection(this.invalidRawValue.getText()), null));
    JButton reset = new JButton("Reset Scripts", Icons.DELETE_16);
    reset.setToolTipText("Explicitly replace the invalid value with an empty script list");
    reset.addActionListener(event -> {
      if (this.currentTarget == null) return;
      int choice = javax.swing.JOptionPane.showConfirmDialog(this,
        "Replace the invalid script assignment data with an empty list?\nCopy the raw value first if it may be needed.",
        "Reset Invalid Scripts", javax.swing.JOptionPane.OK_CANCEL_OPTION,
        javax.swing.JOptionPane.WARNING_MESSAGE);
      if (choice == javax.swing.JOptionPane.OK_OPTION) {
        this.bindingService.resetInvalidBindings(this.currentTarget);
      }
    });
    actions.add(copy);
    actions.add(reset);
    panel.add(actions, BorderLayout.SOUTH);
    return panel;
  }

  protected void updateButtonStates() {
    int selectedIndex = this.bindings.getSelectedIndex();
    boolean hasSelection = selectedIndex >= 0;
    ScriptDefinition available = (ScriptDefinition) this.availableScripts.getSelectedItem();
    boolean hasAvailable = available != null && !this.containsBinding(available.getId());
    int count = this.bindingsModel.size();

    this.scriptsHeaderLabel.setText("Attached Scripts (" + count + ")");
    this.newScriptButton.setEnabled(this.bindingStateEditable && this.currentSource != null
      && Editor.instance().getGameFile() != null);
    this.addButton.setEnabled(this.bindingStateEditable && hasAvailable);
    this.removeButton.setEnabled(this.bindingStateEditable && hasSelection);
    this.openButton.setEnabled(this.bindingStateEditable && hasSelection);
    this.upButton.setEnabled(this.bindingStateEditable && hasSelection && selectedIndex > 0);
    this.downButton.setEnabled(this.bindingStateEditable && hasSelection && selectedIndex < count - 1);
    ScriptBinding inherited = this.inheritedBindings.getSelectedValue();
    this.overrideButton.setEnabled(this.bindingStateEditable && inherited != null
      && !this.containsBinding(inherited.getScript()));
    this.removeButton.setToolTipText(hasSelection && this.isInherited(this.bindings.getSelectedValue().getScript())
      ? "Reset instance override and restore the inherited script"
      : "Remove selected script (Delete)");
  }

  public T getDataSource() {
    return this.currentSource;
  }

  public void bind(T source) {
    this.currentSource = source;
    this.currentTarget = source == null ? null : this.getBindingTarget(source);
    this.bindingStateEditable = false;
    this.updating = true;
    try {
      this.bindingsModel.clear();
      this.inheritedModel.clear();
      if (source != null && this.currentTarget != null) {
        BindingState state = this.bindingService.getBindings(this.currentTarget);
        if (state instanceof BindingState.Invalid invalid) {
          this.invalidMessage.setText(invalid.error());
          this.invalidRawValue.setText(invalid.rawValue());
          this.mainCardLayout.show(this.mainContainer, CARD_INVALID);
          this.availableScripts.removeAllItems();
          this.parameters.setRowCount(0);
          this.updateButtonStates();
          return;
        }
        this.bindingStateEditable = true;
        ((BindingState.Valid) state).bindings().forEach(this.bindingsModel::addElement);
        this.refreshInheritedBindings();
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
    List<ScriptDefinition> candidates = this.currentTarget == null && Editor.instance().getGameFile() != null
      ? Editor.instance().getGameFile().getScripts().stream()
        .filter(Objects::nonNull)
        .filter(definition -> definition.getHost() == this.getSupportedHostType()).map(ScriptDefinition::new).toList()
      : this.currentTarget == null ? List.of() : this.bindingService.compatibleDefinitions(this.currentTarget);
    List<ScriptDefinition> definitions = candidates.stream()
        .filter(definition -> this.isScriptCompatible(definition, source))
        .sorted(Comparator.comparing(AbstractScriptBindingsPanel::displayName, String.CASE_INSENSITIVE_ORDER))
        .toList();
    this.compatibleScripts = definitions;
    this.filterAvailableScripts();
    this.updateButtonStates();
  }

  private void filterAvailableScripts() {
    String query = this.scriptSearch.getText() == null ? "" : this.scriptSearch.getText().strip().toLowerCase(java.util.Locale.ROOT);
    ScriptDefinition selected = this.availableScripts.getSelectedItem() instanceof ScriptDefinition definition ? definition : null;
    List<ScriptDefinition> filtered = this.compatibleScripts.stream()
      .filter(definition -> query.isEmpty()
        || displayName(definition).toLowerCase(java.util.Locale.ROOT).contains(query)
        || Objects.toString(definition.getId(), "").toLowerCase(java.util.Locale.ROOT).contains(query)
        || Objects.toString(definition.getImplementation(), "").toLowerCase(java.util.Locale.ROOT).contains(query))
      .toList();
    this.availableScripts.setModel(new DefaultComboBoxModel<>(filtered.toArray(ScriptDefinition[]::new)));
    if (selected != null) {
      filtered.stream().filter(definition -> Objects.equals(definition.getId(), selected.getId())).findFirst()
        .ifPresent(this.availableScripts::setSelectedItem);
    }
    this.updateButtonStates();
  }

  protected void addSelectedScript() {
    ScriptDefinition definition = (ScriptDefinition) this.availableScripts.getSelectedItem();
    if (definition == null || this.currentSource == null) return;
    if (this.containsBinding(definition.getId())) return;
    ScriptBinding binding = new ScriptBinding(definition.getId());
    int nextOrder = 0;
    for (int index = 0; index < this.bindingsModel.size(); index++) {
      nextOrder = Math.max(nextOrder, this.bindingsModel.get(index).getOrder() + 1);
    }
    binding.setOrder(nextOrder);
    this.bindingsModel.addElement(binding);
    this.bindings.setSelectedIndex(this.bindingsModel.size() - 1);
    this.persist();
    this.updateButtonStates();
  }

  protected void removeSelectedScript() {
    int index = this.bindings.getSelectedIndex();
    if (index < 0) return;
    this.bindingsModel.remove(index);
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
    this.openBinding(this.bindings.getSelectedValue());
  }

  private void openBinding(ScriptBinding binding) {
    if (binding == null) return;
    ScriptDefinition definition = definition(binding.getScript());
    if (definition != null) UI.openScript(definition);
  }

  private void refreshInheritedBindings() {
    this.inheritedModel.clear();
    if (this.currentTarget instanceof ScriptBindingTarget.EntityInstance entity) {
      EntityBindingState state = this.bindingService.getEntityBindingState(entity);
      state.inherited().stream().map(EntityBindingState.ResolvedBinding::binding)
        .forEach(this.inheritedModel::addElement);
    }
    java.awt.Container parent = this.inheritedBindings.getParent();
    while (parent != null && !"inheritedBindingsPanel".equals(parent.getName())) parent = parent.getParent();
    if (parent != null) parent.setVisible(!this.inheritedModel.isEmpty());
  }

  private void overrideSelectedInheritedBinding() {
    ScriptBinding inherited = this.inheritedBindings.getSelectedValue();
    if (inherited == null || this.containsBinding(inherited.getScript())) return;
    ScriptBinding override = new ScriptBinding(inherited);
    this.bindingsModel.addElement(override);
    this.bindings.setSelectedIndex(this.bindingsModel.size() - 1);
    this.persist();
  }

  private boolean isInherited(String scriptId) {
    for (int index = 0; index < this.inheritedModel.size(); index++) {
      if (Objects.equals(scriptId, this.inheritedModel.get(index).getScript())) return true;
    }
    return false;
  }

  protected void bindSelection() {
    this.updating = true;
    try {
      this.parameters.setRowCount(0);
      this.parameterDescriptors.clear();
      this.explicitParameterNames.clear();
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
          .filter(d -> d.id().equals(definition.getId())
            || d.className().equals(definition.getImplementation())).findFirst().orElse(null);
      if (discovered != null) discovered.properties().forEach(property -> {
        names.add(property.name());
        this.parameterDescriptors.put(property.name(),
          new ParameterDescriptor(property.type(), property.defaultValue(), property.min(), property.max()));
      });
      Game.scripts().getPropertyMetadata(binding.getScript()).forEach(property -> {
        names.add(property.name());
        this.parameterDescriptors.put(property.name(),
          new ParameterDescriptor(property.type(), property.defaultValue(), property.min(), property.max()));
      });
      names.addAll(binding.getParameters().keySet());
      this.explicitParameterNames.addAll(binding.getParameters().keySet());

      for (String name : names) {
        ParameterDescriptor descriptor = this.parameterDescriptors.get(name);
        String value = binding.getParameters().containsKey(name)
          ? binding.getParameters().get(name) : descriptor == null ? "" : descriptor.defaultValue();
        Object editorValue = descriptor != null
          && ("boolean".equals(descriptor.type()) || Boolean.class.getName().equals(descriptor.type()))
          ? Boolean.parseBoolean(value) : value;
        this.parameters.addRow(new Object[] {name, editorValue});
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
    Set<String> nextExplicitNames = new LinkedHashSet<>();
    for (int row = 0; row < this.parameters.getRowCount(); row++) {
      String name = Objects.toString(this.parameters.getValueAt(row, 0), "").trim();
      if (name.isEmpty()) continue;
      String value = Objects.toString(this.parameters.getValueAt(row, 1), "");
      ParameterDescriptor descriptor = this.parameterDescriptors.get(name);
      boolean explicit = this.explicitParameterNames.contains(name) || descriptor == null
        || !Objects.equals(value, descriptor.defaultValue());
      if (explicit) {
        binding.setParameter(name, value);
        nextExplicitNames.add(name);
      }
    }
    this.explicitParameterNames.clear();
    this.explicitParameterNames.addAll(nextExplicitNames);
    this.persist();
  }

  private TableCellEditor createParameterEditor(int row) {
    String name = Objects.toString(this.parameters.getValueAt(row, 0), "");
    ParameterDescriptor descriptor = this.parameterDescriptors.get(name);
    if (descriptor == null || descriptor.type() == null) return new javax.swing.DefaultCellEditor(new JTextField());
    String type = descriptor.type();
    if (type.equals("boolean") || type.equals(Boolean.class.getName())) {
      return new javax.swing.DefaultCellEditor(new JCheckBox());
    }
    if (isNumericType(type)) {
      return new NumericCellEditor(descriptor, Objects.toString(this.parameters.getValueAt(row, 1), "0"));
    }
    try {
      ClassLoader loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
      if (loader == null) loader = AbstractScriptBindingsPanel.class.getClassLoader();
      Class<?> propertyType = Class.forName(type, false, loader);
      if (propertyType.isEnum()) {
        JComboBox<String> choices = new JComboBox<>();
        for (Object constant : propertyType.getEnumConstants()) choices.addItem(constant.toString());
        return new javax.swing.DefaultCellEditor(choices);
      }
    } catch (ClassNotFoundException | LinkageError ignored) {
      // Unknown types deliberately remain editable as text and are preserved verbatim.
    }
    return new javax.swing.DefaultCellEditor(new JTextField());
  }

  private static boolean isNumericType(String type) {
    return type.equals("byte") || type.equals("short") || type.equals("int") || type.equals("long")
      || type.equals("float") || type.equals("double") || type.equals(Byte.class.getName())
      || type.equals(Short.class.getName()) || type.equals(Integer.class.getName()) || type.equals(Long.class.getName())
      || type.equals(Float.class.getName()) || type.equals(Double.class.getName());
  }

  private static boolean isIntegralType(String type) {
    return type.equals("byte") || type.equals("short") || type.equals("int") || type.equals("long")
      || type.equals(Byte.class.getName()) || type.equals(Short.class.getName())
      || type.equals(Integer.class.getName()) || type.equals(Long.class.getName());
  }

  protected void persist() {
    if (this.currentSource == null || this.currentTarget == null) return;
    List<ScriptBinding> list = new ArrayList<>();
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      list.add(this.bindingsModel.get(i));
    }
    ScriptBindingService.UpdateResult result = this.bindingService.updateBindings(this.currentTarget, list);
    if (!result.success()) {
      javax.swing.JOptionPane.showMessageDialog(this, result.message(), "Scripts",
        javax.swing.JOptionPane.ERROR_MESSAGE);
      this.bind(this.currentSource);
    }
  }

  private boolean containsBinding(String scriptId) {
    if (scriptId == null) return false;
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      if (scriptId.equals(this.bindingsModel.get(i).getScript())) return true;
    }
    return false;
  }

  private int countBindings(String scriptId) {
    int count = 0;
    for (int i = 0; i < this.bindingsModel.size(); i++) {
      if (Objects.equals(scriptId, this.bindingsModel.get(i).getScript())) count++;
    }
    return count;
  }

  private void bindingChanged(ScriptBindingTarget target) {
    if (!Objects.equals(this.currentTarget, target)) return;
    javax.swing.SwingUtilities.invokeLater(() -> this.bind(this.currentSource));
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
        .filter(Objects::nonNull)
        .filter(d -> id.equals(d.getId()) || id.equals(d.getName()) || id.equals(d.getSource()))
        .findFirst().orElse(null);
  }

  protected abstract ScriptBindingTarget getBindingTarget(T source);
  protected abstract boolean isScriptCompatible(ScriptDefinition definition, T source);
  protected abstract void createNewScript();
  protected abstract String getEmptyStateTitle();
  protected abstract String getEmptyStateHint();
  protected abstract String getNoCompatibleScriptsText();
  protected abstract String getNewScriptTooltip();
  protected abstract ScriptHostType getSupportedHostType();

  private record ParameterDescriptor(String type, String defaultValue, double min, double max) {
    private ParameterDescriptor {
      defaultValue = defaultValue == null ? "" : defaultValue;
    }
  }

  private static final class NumericCellEditor extends javax.swing.AbstractCellEditor implements TableCellEditor {
    private final javax.swing.JSpinner spinner;
    private final boolean integral;

    private NumericCellEditor(ParameterDescriptor descriptor, String value) {
      this.integral = isIntegralType(descriptor.type());
      double parsed;
      try {
        parsed = Double.parseDouble(value);
      } catch (NumberFormatException ignored) {
        parsed = 0;
      }
      double minimum = Double.isFinite(descriptor.min()) ? descriptor.min() : -Double.MAX_VALUE;
      double maximum = Double.isFinite(descriptor.max()) ? descriptor.max() : Double.MAX_VALUE;
      parsed = Math.max(minimum, Math.min(maximum, parsed));
      this.spinner = new javax.swing.JSpinner(new javax.swing.SpinnerNumberModel(
        parsed, minimum, maximum, this.integral ? 1.0 : 0.1));
    }

    @Override public Object getCellEditorValue() {
      Number value = (Number) this.spinner.getValue();
      return this.integral ? Long.toString(value.longValue()) : Double.toString(value.doubleValue());
    }

    @Override public java.awt.Component getTableCellEditorComponent(JTable table, Object value, boolean selected,
                                                                    int row, int column) {
      try {
        this.spinner.setValue(Double.parseDouble(Objects.toString(value, "0")));
      } catch (IllegalArgumentException ignored) {
        // Keep the safe value established by the descriptor.
      }
      return this.spinner;
    }
  }
}
