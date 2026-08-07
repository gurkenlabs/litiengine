package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Entity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.SoundSource;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.EntityScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/** Script metadata inspector used by the central source workspace. */
public final class ScriptInspectorPanel extends JPanel {
  private final JTextField name = readOnlyField();
  private final JTextField id = readOnlyField();
  private final JTextField language = readOnlyField();
  private final JComboBox<ScriptHostType> host = new JComboBox<>(ScriptHostType.values());
  private final JComboBox<TargetChoice> target = new JComboBox<>();
  private final JCheckBox defaultForType = new JCheckBox("Attach to this entity type by default");
  private final JCheckBox includeSubtypes = new JCheckBox("Include subtypes");
  private final JCheckBox staticTypeChecking = new JCheckBox("Static type checking");
  private final JTextField source = readOnlyField();
  private final JButton apply = new JButton("Apply metadata");
  private ScriptWorkspacePanel workspace;
  private ScriptDefinition definition;
  private boolean binding;

  public ScriptInspectorPanel() {
    super(new BorderLayout());
    JPanel header = new JPanel(new BorderLayout());
    header.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(0, 0, 1, 0, Style.border()),
      BorderFactory.createEmptyBorder(9, 10, 9, 10)));
    JLabel title = new JLabel("SCRIPT INFORMATION");
    title.setFont(title.getFont().deriveFont(java.awt.Font.BOLD));
    title.setForeground(Style.mutedText());
    header.add(title, BorderLayout.CENTER);
    this.add(header, BorderLayout.NORTH);

    JPanel fields = new JPanel(new GridBagLayout());
    fields.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    int row = 0;
    addRow(fields, row++, "Class Name", this.name);
    addRow(fields, row++, "ID", this.id);
    addRow(fields, row++, "Language", this.language);
    addRow(fields, row++, "Host", this.host);
    addRow(fields, row++, "Target type", this.target);
    addRow(fields, row++, "", this.defaultForType);
    addRow(fields, row++, "", this.includeSubtypes);
    addRow(fields, row++, "", this.staticTypeChecking);
    addRow(fields, row++, "Source", this.source);
    GridBagConstraints filler = new GridBagConstraints();
    filler.gridy = row;
    filler.weighty = 1;
    fields.add(new JPanel(), filler);
    this.name.setFocusable(false);
    this.id.setFocusable(false);
    this.language.setFocusable(false);
    this.host.setFocusable(false);
    this.target.setFocusable(false);
    this.defaultForType.setFocusable(false);
    this.includeSubtypes.setFocusable(false);
    this.staticTypeChecking.setFocusable(false);
    this.source.setFocusable(false);
    this.apply.setFocusable(false);

    JPanel actions = new JPanel(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, Style.SPACE_SMALL, Style.SPACE_SMALL));
    actions.setBorder(BorderFactory.createCompoundBorder(
      BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
      BorderFactory.createEmptyBorder(4, 6, 4, 6)));
    this.apply.addActionListener(event -> this.applyMetadata());
    JButton save = new JButton("Save", Icons.SAVE_16);
    save.setFocusable(false);
    save.addActionListener(event -> { if (this.workspace != null) this.workspace.saveActive(); });
    JButton reload = new JButton("Compile & reload", Icons.REWIND_16);
    reload.setFocusable(false);
    reload.addActionListener(event -> { if (this.workspace != null) this.workspace.reloadActive(); });
    JButton external = new JButton("Open in IDE", Icons.EXTERNAL_16);
    external.setFocusable(false);
    external.addActionListener(event -> { if (this.workspace != null) this.workspace.openActiveExternally(); });
    actions.add(this.apply);
    actions.add(save);
    actions.add(reload);
    actions.add(external);
    this.add(actions, BorderLayout.SOUTH);

    this.host.addActionListener(event -> {
      if (!this.binding) this.updateTargetEnabled();
    });
    this.target.addActionListener(event -> {
      if (!this.binding) this.refreshDefaultBinding();
    });
    this.defaultForType.addActionListener(event -> this.updateDefaultBindingEnabled());
    this.bind(null);
  }

  public void setWorkspace(ScriptWorkspacePanel workspace) {
    this.workspace = workspace;
  }

  public void bind(ScriptDefinition definition) {
    this.binding = true;
    try {
      this.definition = definition;
      this.refreshTargets();
      this.name.setText(value(definition == null ? null : (definition.getImplementation() == null || definition.getImplementation().isBlank() ? definition.getName() : definition.getImplementation())));
      this.id.setText(value(definition == null ? null : definition.getId()));
      this.language.setText(value(definition == null ? null : definition.getLanguage()));
      this.host.setSelectedItem(definition == null ? ScriptHostType.ENTITY : definition.getHost());
      this.selectTarget(definition == null ? null : definition.getTargetType());
      this.refreshDefaultBinding();
      this.source.setText(value(definition == null ? null : definition.getSource()));
      this.staticTypeChecking.setSelected(definition != null && this.workspace != null
        && this.workspace.isStaticTypeCheckingEnabled());
      boolean enabled = definition != null;
      this.host.setEnabled(enabled);
      this.apply.setEnabled(enabled);
      this.staticTypeChecking.setEnabled(enabled && "groovy".equalsIgnoreCase(definition.getLanguage()));
      this.updateTargetEnabled();
    } finally {
      this.binding = false;
    }
  }

  private void applyMetadata() {
    if (this.workspace == null || this.definition == null) return;
    String previousTarget = this.definition.getTargetType();
    ScriptHostType selectedHost = (ScriptHostType) this.host.getSelectedItem();
    TargetChoice selectedTarget = (TargetChoice) this.target.getSelectedItem();
    String targetType = selectedHost == ScriptHostType.ENTITY && selectedTarget != null ? selectedTarget.className() : null;
    updateDefaultBinding(Editor.instance().getGameFile(), this.definition.getId(), previousTarget, targetType,
      selectedHost == ScriptHostType.ENTITY && this.defaultForType.isSelected(), this.includeSubtypes.isSelected());
    this.workspace.updateActiveMetadata(this.name.getText(), selectedHost, targetType);
    this.workspace.setStaticTypeCheckingEnabled(this.staticTypeChecking.isSelected());
    de.gurkenlabs.litiengine.Game.scripts().setEntityBindings(Editor.instance().getGameFile().getEntityScripts());
  }

  private void refreshTargets() {
    String selected = this.target.getSelectedItem() instanceof TargetChoice choice ? choice.className() : null;
    this.target.removeAllItems();
    Map<String, TargetChoice> choices = new LinkedHashMap<>();
    addChoice(choices, "Any entity", IEntity.class);
    addChoice(choices, "Entity", Entity.class);
    addChoice(choices, "Creature", Creature.class);
    addChoice(choices, "Prop", Prop.class);
    addChoice(choices, "Trigger", Trigger.class);
    addChoice(choices, "Emitter", Emitter.class);
    addChoice(choices, "Light source", LightSource.class);
    addChoice(choices, "Sound source", SoundSource.class);
    addChoice(choices, "Spawn point", Spawnpoint.class);
    addChoice(choices, "Static shadow", StaticShadow.class);
    addChoice(choices, "Collision box", CollisionBox.class);
    Editor.instance().getProjectCodeIntegration().getDefinitions().forEach(projectType -> choices.putIfAbsent(
      projectType.className(), new TargetChoice(projectType.displayName(), projectType.className())));
    choices.values().forEach(this.target::addItem);
    this.selectTarget(selected);
  }

  private static void addChoice(Map<String, TargetChoice> choices, String label, Class<?> type) {
    choices.put(type.getName(), new TargetChoice(label, type.getName()));
  }

  private void selectTarget(String className) {
    if (className == null) return;
    for (int index = 0; index < this.target.getItemCount(); index++) {
      if (Objects.equals(className, this.target.getItemAt(index).className())) {
        this.target.setSelectedIndex(index);
        return;
      }
    }
    this.target.addItem(new TargetChoice(simpleName(className), className));
    this.target.setSelectedIndex(this.target.getItemCount() - 1);
  }

  private void updateTargetEnabled() {
    this.target.setEnabled(this.definition != null && this.host.getSelectedItem() == ScriptHostType.ENTITY);
    this.defaultForType.setEnabled(this.target.isEnabled());
    this.updateDefaultBindingEnabled();
  }

  private void updateDefaultBindingEnabled() {
    this.includeSubtypes.setEnabled(this.defaultForType.isEnabled() && this.defaultForType.isSelected());
  }

  private void refreshDefaultBinding() {
    TargetChoice selectedTarget = (TargetChoice) this.target.getSelectedItem();
    String targetType = selectedTarget == null ? null : selectedTarget.className();
    EntityScriptBinding configured = this.definition == null || targetType == null ? null
      : Editor.instance().getGameFile().getEntityScripts().stream()
        .filter(candidate -> Objects.equals(targetType, candidate.getTargetType()))
        .filter(candidate -> candidate.getScripts().stream()
          .anyMatch(script -> Objects.equals(this.definition.getId(), script.getScript())))
        .findFirst().orElse(null);
    this.defaultForType.setSelected(configured != null);
    this.includeSubtypes.setSelected(configured == null || configured.isInherited());
    this.updateDefaultBindingEnabled();
  }

  static void updateDefaultBinding(de.gurkenlabs.litiengine.resources.ResourceBundle bundle, String scriptId,
      String previousTarget, String targetType, boolean enabled, boolean inherited) {
    if (bundle == null || scriptId == null) return;
    bundle.getEntityScripts().stream()
      .filter(candidate -> Objects.equals(previousTarget, candidate.getTargetType()))
      .forEach(candidate -> candidate.getScripts().removeIf(script -> Objects.equals(scriptId, script.getScript())));
    bundle.getEntityScripts().removeIf(candidate -> candidate.getScripts().isEmpty());
    if (!enabled || targetType == null || targetType.isBlank()) return;
    EntityScriptBinding target = bundle.getEntityScripts().stream()
      .filter(candidate -> Objects.equals(targetType, candidate.getTargetType()) && candidate.isInherited() == inherited)
      .findFirst().orElseGet(() -> {
        EntityScriptBinding created = new EntityScriptBinding(targetType);
        created.setInherited(inherited);
        bundle.getEntityScripts().add(created);
        return created;
      });
    if (target.getScripts().stream().noneMatch(script -> Objects.equals(scriptId, script.getScript()))) {
      ScriptBinding binding = new ScriptBinding(scriptId);
      binding.setOrder(target.getScripts().size());
      target.getScripts().add(binding);
    }
  }

  private static JTextField readOnlyField() {
    JTextField field = new JTextField();
    field.setEditable(false);
    return field;
  }

  private static void addRow(JPanel panel, int row, String label, java.awt.Component field) {
    GridBagConstraints labelConstraints = new GridBagConstraints();
    labelConstraints.gridx = 0;
    labelConstraints.gridy = row;
    labelConstraints.anchor = GridBagConstraints.EAST;
    labelConstraints.insets = new Insets(3, 0, 3, 8);
    panel.add(new JLabel(label), labelConstraints);
    GridBagConstraints fieldConstraints = new GridBagConstraints();
    fieldConstraints.gridx = 1;
    fieldConstraints.gridy = row;
    fieldConstraints.weightx = 1;
    fieldConstraints.fill = GridBagConstraints.HORIZONTAL;
    fieldConstraints.insets = new Insets(3, 0, 3, 0);
    panel.add(field, fieldConstraints);
  }

  private static String value(Object value) {
    return value == null ? "" : value.toString();
  }

  private static String simpleName(String className) {
    int separator = className.lastIndexOf('.');
    return separator < 0 ? className : className.substring(separator + 1);
  }

  private record TargetChoice(String label, String className) {
    @Override public String toString() { return this.label; }
  }
}
