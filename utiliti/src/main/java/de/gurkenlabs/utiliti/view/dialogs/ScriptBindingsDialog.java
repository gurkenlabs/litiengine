package de.gurkenlabs.utiliti.view.dialogs;

import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

/** Ordered, inline editor for script bindings and their typed parameters. */
public final class ScriptBindingsDialog extends JDialog {
  private final DefaultListModel<ScriptBinding> model = new DefaultListModel<>();
  private final JList<ScriptBinding> bindings = new JList<>(this.model);
  private final JComboBox<ScriptDefinition> script = new JComboBox<>();
  private final JCheckBox enabled = new JCheckBox("Enabled");
  private final DefaultTableModel parameters = new DefaultTableModel(new Object[] {"Parameter", "Value"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
  };
  private final JTable parameterTable = new JTable(this.parameters);
  private final Consumer<String> saveAction;
  private boolean bindingInspector;

  private ScriptBindingsDialog(String encoded, ScriptHostType hostType, Consumer<String> saveAction) {
    this.saveAction = saveAction;
    this.setTitle(switch (hostType) {
      case GAME -> "Game scripts";
      case ENVIRONMENT -> "Environment scripts";
      case ENTITY -> "Entity scripts";
    });
    this.setModalityType(ModalityType.MODELESS);
    this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
    this.setLayout(new BorderLayout(8, 8));

    try {
      ScriptBindingCodec.decode(encoded).forEach(this.model::addElement);
    } catch (IllegalArgumentException ignored) {
      // The empty editor lets the user replace malformed legacy content.
    }
    this.bindings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.bindings.setCellRenderer((list, value, index, selected, focused) -> {
      JLabel label = new JLabel((index + 1) + ".  " + value.getScript());
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
      return label;
    });
    this.bindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) bindSelection();
    });

    JPanel left = new JPanel(new BorderLayout());
    left.add(new JScrollPane(this.bindings), BorderLayout.CENTER);
    JPanel listActions = new JPanel();
    JButton add = new JButton("+");
    add.addActionListener(event -> addBinding());
    JButton remove = new JButton("−");
    remove.addActionListener(event -> removeBinding());
    JButton up = new JButton("↑");
    up.addActionListener(event -> move(-1));
    JButton down = new JButton("↓");
    down.addActionListener(event -> move(1));
    listActions.add(add);
    listActions.add(remove);
    listActions.add(up);
    listActions.add(down);
    left.add(listActions, BorderLayout.SOUTH);

    JPanel inspector = new JPanel(new BorderLayout(6, 6));
    JPanel identity = new JPanel(new BorderLayout(8, 0));
    identity.add(this.script, BorderLayout.CENTER);
    identity.add(this.enabled, BorderLayout.EAST);
    inspector.add(identity, BorderLayout.NORTH);
    inspector.add(new JScrollPane(this.parameterTable), BorderLayout.CENTER);
    this.script.addActionListener(event -> { if (!this.bindingInspector) applyInspector(); });
    this.enabled.addActionListener(event -> { if (!this.bindingInspector) applyInspector(); });

    List<ScriptDefinition> available = Editor.instance().getGameFile().getScripts().stream()
      .filter(definition -> definition.getHost() == hostType)
      .sorted(Comparator.comparing(ScriptDefinition::getId)).toList();
    this.script.setModel(new DefaultComboBoxModel<>(available.toArray(ScriptDefinition[]::new)));
    this.script.setRenderer((list, value, index, selected, focused) -> new JLabel(value == null ? "" :
      (value.getName() == null || value.getName().isBlank() ? value.getId() : value.getName())));

    JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, left, inspector);
    split.setResizeWeight(0.32);
    split.setDividerLocation(280);
    this.add(split, BorderLayout.CENTER);

    JPanel footer = new JPanel();
    JButton cancel = new JButton("Cancel");
    cancel.addActionListener(event -> dispose());
    JButton save = new JButton("Save bindings");
    save.addActionListener(event -> save());
    footer.add(cancel);
    footer.add(save);
    this.add(footer, BorderLayout.SOUTH);

    if (!this.model.isEmpty()) this.bindings.setSelectedIndex(0);
    this.setPreferredSize(new Dimension(850, 540));
    this.pack();
    this.setLocationRelativeTo(null);
  }

  public static void open(String encoded, Consumer<String> saveAction) {
    open(encoded, ScriptHostType.ENTITY, saveAction);
  }

  public static void open(String encoded, ScriptHostType hostType, Consumer<String> saveAction) {
    SwingUtilities.invokeLater(() -> new ScriptBindingsDialog(encoded, hostType, saveAction).setVisible(true));
  }

  private void addBinding() {
    if (this.script.getItemCount() == 0) return;
    ScriptDefinition definition = (ScriptDefinition) this.script.getSelectedItem();
    if (definition == null) return;
    ScriptBinding binding = new ScriptBinding(definition.getId());
    binding.setOrder(this.model.size());
    this.model.addElement(binding);
    this.bindings.setSelectedIndex(this.model.size() - 1);
  }

  private void removeBinding() {
    int index = this.bindings.getSelectedIndex();
    if (index < 0) return;
    this.model.remove(index);
    normalizeOrder();
    if (!this.model.isEmpty()) this.bindings.setSelectedIndex(Math.min(index, this.model.size() - 1));
  }

  private void move(int delta) {
    int index = this.bindings.getSelectedIndex();
    int target = index + delta;
    if (index < 0 || target < 0 || target >= this.model.size()) return;
    ScriptBinding value = this.model.remove(index);
    this.model.add(target, value);
    normalizeOrder();
    this.bindings.setSelectedIndex(target);
  }

  private void bindSelection() {
    this.bindingInspector = true;
    try {
      ScriptBinding binding = this.bindings.getSelectedValue();
      this.parameters.setRowCount(0);
      if (binding == null) return;
      for (int i = 0; i < this.script.getItemCount(); i++) {
        if (this.script.getItemAt(i).getId().equals(binding.getScript())) this.script.setSelectedIndex(i);
      }
      this.enabled.setSelected(binding.isEnabled());
      Set<String> names = new LinkedHashSet<>();
      var discovered = Editor.instance().getProjectCodeIntegration().getScriptDefinitions().stream()
        .filter(candidate -> candidate.id().equals(binding.getScript())).findFirst().orElse(null);
      if (discovered != null) discovered.properties().forEach(property -> names.add(property.name()));
      de.gurkenlabs.litiengine.Game.scripts().getPropertyMetadata(binding.getScript())
        .forEach(property -> names.add(property.name()));
      names.addAll(binding.getParameters().keySet());
      for (String name : names) this.parameters.addRow(new Object[] {name, binding.getParameters().getOrDefault(name, "")});
    } finally {
      this.bindingInspector = false;
    }
  }

  private void applyInspector() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    ScriptDefinition definition = (ScriptDefinition) this.script.getSelectedItem();
    if (binding == null || definition == null) return;
    binding.setScript(definition.getId());
    binding.setEnabled(this.enabled.isSelected());
    binding.getParameterValues().clear();
    for (int row = 0; row < this.parameters.getRowCount(); row++) {
      String name = String.valueOf(this.parameters.getValueAt(row, 0));
      String value = String.valueOf(this.parameters.getValueAt(row, 1));
      if (!name.isBlank()) binding.setParameter(name, value);
    }
    this.bindings.repaint();
  }

  private void normalizeOrder() {
    for (int i = 0; i < this.model.size(); i++) this.model.get(i).setOrder(i);
  }

  private void save() {
    if (this.parameterTable.isEditing()) this.parameterTable.getCellEditor().stopCellEditing();
    applyInspector();
    List<ScriptBinding> result = new ArrayList<>();
    for (int i = 0; i < this.model.size(); i++) result.add(this.model.get(i));
    this.saveAction.accept(ScriptBindingCodec.encode(result));
    dispose();
  }
}
