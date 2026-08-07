package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.SoundSource;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.UndoManager;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.DefaultTableModel;

/** Inline script attachment and exported-property inspector shared by all entity map-object types. */
public final class ScriptBindingsInspectorPanel extends PropertyPanel {
  private final DefaultListModel<ScriptBinding> bindingsModel = new DefaultListModel<>();
  private final JList<ScriptBinding> bindings = new JList<>(this.bindingsModel);
  private final JComboBox<ScriptDefinition> availableScripts = new JComboBox<>();
  private final JCheckBox enabled = new JCheckBox("Enabled");
  private final DefaultTableModel parameters = new DefaultTableModel(new Object[] {"Property", "Value"}, 0) {
    @Override public boolean isCellEditable(int row, int column) { return column == 1; }
  };
  private final JTable parameterTable = new JTable(this.parameters);
  private boolean updating;

  public ScriptBindingsInspectorPanel() {
    super("panel_scriptBindings", Icons.API_16);
    this.setLayout(new BorderLayout(0, Style.SPACE_SMALL));

    this.availableScripts.setRenderer((list, value, index, selected, focused) -> new JLabel(displayName(value)));
    JButton add = Style.iconButton(Icons.ADD_16);
    add.setToolTipText("Attach selected script");
    add.addActionListener(event -> this.addSelectedScript());
    JPanel picker = new JPanel(new BorderLayout(Style.SPACE_SMALL, 0));
    picker.setOpaque(false);
    picker.add(this.availableScripts, BorderLayout.CENTER);
    picker.add(add, BorderLayout.EAST);
    this.add(picker, BorderLayout.NORTH);

    this.bindings.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    this.bindings.setVisibleRowCount(4);
    this.bindings.setCellRenderer((list, value, index, selected, focused) -> {
      ScriptDefinition definition = definition(value == null ? null : value.getScript());
      JLabel label = new JLabel((value != null && value.isEnabled() ? "" : "(disabled) ")
        + (definition == null ? value == null ? "" : value.getScript() : displayName(definition)));
      label.setOpaque(true);
      label.setBackground(selected ? list.getSelectionBackground() : list.getBackground());
      label.setForeground(selected ? list.getSelectionForeground() : list.getForeground());
      return label;
    });
    this.bindings.addListSelectionListener(event -> {
      if (!event.getValueIsAdjusting()) this.bindSelection();
    });
    this.bindings.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent event) {
        if (event.getClickCount() == 2) openSelectedScript();
      }
    });

    JPanel details = new JPanel(new BorderLayout(0, Style.SPACE_SMALL));
    details.setOpaque(false);
    details.add(this.enabled, BorderLayout.NORTH);
    details.add(new JScrollPane(this.parameterTable), BorderLayout.CENTER);
    JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT, new JScrollPane(this.bindings), details);
    UI.configureSplitPane(split);
    split.setResizeWeight(0.42);
    split.setDividerLocation(82);
    split.setPreferredSize(new Dimension(0, 210));
    this.add(split, BorderLayout.CENTER);

    JButton open = new JButton("Open");
    open.addActionListener(event -> this.openSelectedScript());
    JButton up = new JButton("Up");
    up.addActionListener(event -> this.moveSelectedScript(-1));
    JButton down = new JButton("Down");
    down.addActionListener(event -> this.moveSelectedScript(1));
    JButton remove = new JButton("Remove");
    remove.addActionListener(event -> this.removeSelectedScript());
    JPanel actions = new JPanel(new FlowLayout(FlowLayout.RIGHT, Style.SPACE_SMALL, 0));
    actions.setOpaque(false);
    actions.add(open);
    actions.add(up);
    actions.add(down);
    actions.add(remove);
    this.add(actions, BorderLayout.SOUTH);

    this.enabled.addActionListener(event -> {
      if (this.updating || this.bindings.getSelectedValue() == null) return;
      this.bindings.getSelectedValue().setEnabled(this.enabled.isSelected());
      this.persist();
      this.bindings.repaint();
    });
    this.parameters.addTableModelListener(event -> {
      if (!this.updating) this.applyParameterValues();
    });
  }

  @Override
  protected void clearControls() {
    this.updating = true;
    try {
      this.bindingsModel.clear();
      this.parameters.setRowCount(0);
      this.availableScripts.removeAllItems();
      this.enabled.setSelected(false);
    } finally {
      this.updating = false;
    }
  }

  @Override
  protected void setControlValues(IMapObject mapObject) {
    this.updating = true;
    try {
      this.bindingsModel.clear();
      ScriptBindingCodec.decode(mapObject.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null))
        .forEach(this.bindingsModel::addElement);
      this.refreshAvailableScripts(mapObject);
      if (!this.bindingsModel.isEmpty()) this.bindings.setSelectedIndex(0);
      else this.parameters.setRowCount(0);
    } catch (IllegalArgumentException ignored) {
      this.bindingsModel.clear();
      this.parameters.setRowCount(0);
    } finally {
      this.updating = false;
    }
    this.bindSelection();
  }

  private void refreshAvailableScripts(IMapObject mapObject) {
    List<ScriptDefinition> definitions = Editor.instance().getGameFile().getScripts().stream()
      .filter(definition -> definition.getHost() == ScriptHostType.ENTITY)
      .filter(definition -> compatible(definition, mapObject))
      .sorted(Comparator.comparing(ScriptBindingsInspectorPanel::displayName, String.CASE_INSENSITIVE_ORDER))
      .toList();
    this.availableScripts.setModel(new DefaultComboBoxModel<>(definitions.toArray(ScriptDefinition[]::new)));
  }

  private void addSelectedScript() {
    ScriptDefinition definition = (ScriptDefinition) this.availableScripts.getSelectedItem();
    if (definition == null || this.getDataSource() == null) return;
    ScriptBinding binding = new ScriptBinding(definition.getId());
    binding.setOrder(this.bindingsModel.size());
    this.bindingsModel.addElement(binding);
    this.bindings.setSelectedIndex(this.bindingsModel.size() - 1);
    this.persist();
  }

  private void removeSelectedScript() {
    int index = this.bindings.getSelectedIndex();
    if (index < 0) return;
    this.bindingsModel.remove(index);
    for (int i = 0; i < this.bindingsModel.size(); i++) this.bindingsModel.get(i).setOrder(i);
    this.persist();
    if (!this.bindingsModel.isEmpty()) this.bindings.setSelectedIndex(Math.min(index, this.bindingsModel.size() - 1));
    else this.bindSelection();
  }

  private void moveSelectedScript(int delta) {
    int index = this.bindings.getSelectedIndex();
    int target = index + delta;
    if (index < 0 || target < 0 || target >= this.bindingsModel.size()) return;
    ScriptBinding binding = this.bindingsModel.remove(index);
    this.bindingsModel.add(target, binding);
    for (int i = 0; i < this.bindingsModel.size(); i++) this.bindingsModel.get(i).setOrder(i);
    this.bindings.setSelectedIndex(target);
    this.persist();
  }

  private void bindSelection() {
    this.updating = true;
    try {
      this.parameters.setRowCount(0);
      ScriptBinding binding = this.bindings.getSelectedValue();
      this.enabled.setEnabled(binding != null);
      this.enabled.setSelected(binding != null && binding.isEnabled());
      if (binding == null) return;
      Set<String> names = new LinkedHashSet<>();
      var discovered = Editor.instance().getProjectCodeIntegration().getScriptDefinitions().stream()
        .filter(candidate -> candidate.id().equals(binding.getScript())).findFirst().orElse(null);
      if (discovered != null) discovered.properties().forEach(property -> names.add(property.name()));
      de.gurkenlabs.litiengine.Game.scripts().getPropertyMetadata(binding.getScript())
        .forEach(property -> names.add(property.name()));
      names.addAll(binding.getParameters().keySet());
      for (String name : names) {
        this.parameters.addRow(new Object[] {name, binding.getParameters().getOrDefault(name, "")});
      }
    } finally {
      this.updating = false;
    }
  }

  private void applyParameterValues() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    if (binding == null) return;
    binding.getParameterValues().clear();
    for (int row = 0; row < this.parameters.getRowCount(); row++) {
      String name = Objects.toString(this.parameters.getValueAt(row, 0), "").trim();
      if (!name.isEmpty()) binding.setParameter(name, Objects.toString(this.parameters.getValueAt(row, 1), ""));
    }
    this.persist();
  }

  private void persist() {
    IMapObject mapObject = this.getDataSource();
    if (mapObject == null || this.updating) return;
    List<ScriptBinding> result = new ArrayList<>();
    for (int i = 0; i < this.bindingsModel.size(); i++) result.add(this.bindingsModel.get(i));
    String encoded = ScriptBindingCodec.encode(result);
    UndoManager.instance().mapObjectChanging(mapObject);
    if (result.isEmpty()) mapObject.removeProperty(MapObjectProperty.SCRIPT_BINDINGS);
    else mapObject.setValue(MapObjectProperty.SCRIPT_BINDINGS, encoded);
    UndoManager.instance().mapObjectChanged(mapObject);
  }

  private void openSelectedScript() {
    ScriptBinding binding = this.bindings.getSelectedValue();
    ScriptDefinition definition = definition(binding == null ? null : binding.getScript());
    if (definition != null) UI.openScript(definition);
  }

  private static ScriptDefinition definition(String id) {
    if (id == null || Editor.instance().getGameFile() == null) return null;
    return Editor.instance().getGameFile().getScripts().stream().filter(candidate -> id.equals(candidate.getId())).findFirst().orElse(null);
  }

  private static String displayName(ScriptDefinition definition) {
    if (definition == null) return "";
    return definition.getName() == null || definition.getName().isBlank() ? definition.getId() : definition.getName();
  }

  static boolean compatible(ScriptDefinition definition, IMapObject mapObject) {
    if (definition == null || mapObject == null || definition.getHost() != ScriptHostType.ENTITY) return false;
    if (definition.getTargetType() == null || definition.getTargetType().isBlank()) return true;
    Class<?> entityType = resolveEntityType(mapObject);
    if (entityType == null) return false;
    try {
      ClassLoader loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
      if (loader == null) loader = ScriptBindingsInspectorPanel.class.getClassLoader();
      return Class.forName(definition.getTargetType(), false, loader).isAssignableFrom(entityType);
    } catch (ClassNotFoundException | LinkageError ignored) {
      return false;
    }
  }

  private static Class<?> resolveEntityType(IMapObject mapObject) {
    String implementation = mapObject.getStringValue(MapObjectProperty.IMPLEMENTATION, null);
    if (implementation != null) {
      var discovered = Editor.instance().getProjectCodeIntegration().getDefinitions().stream()
        .filter(definition -> implementation.equals(definition.id())).findFirst().orElse(null);
      if (discovered != null) {
        try {
          return Class.forName(discovered.className(), false, Editor.instance().getProjectCodeIntegration().getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
          // Fall back to the engine map-object type.
        }
      }
    }
    MapObjectType type = MapObjectType.get(mapObject.getType());
    if (type == null) return IEntity.class;
    return switch (type) {
      case COLLISIONBOX -> CollisionBox.class;
      case EMITTER -> Emitter.class;
      case LIGHTSOURCE -> LightSource.class;
      case PROP -> Prop.class;
      case CREATURE -> Creature.class;
      case SOUNDSOURCE -> SoundSource.class;
      case SPAWNPOINT -> Spawnpoint.class;
      case TRIGGER -> Trigger.class;
      case STATICSHADOW -> StaticShadow.class;
      case AREA -> null;
    };
  }
}
