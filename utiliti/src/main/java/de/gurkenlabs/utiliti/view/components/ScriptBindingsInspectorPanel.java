package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;
import de.gurkenlabs.utiliti.controller.ScriptBindingTypeResolver;

/**
 * Inline script attachment and exported-property inspector shared by all entity map-object types.
 */
public final class ScriptBindingsInspectorPanel extends AbstractScriptBindingsPanel<IMapObject> {

  public ScriptBindingsInspectorPanel() {
    super();
  }

  @Override
  protected ScriptBindingTarget getBindingTarget(IMapObject source) {
    if (source == null || Editor.instance().getGameFile() == null) return null;
    var map = Editor.instance().getGameFile().getMaps().stream()
      .filter(candidate -> candidate.getMapObject(source.getId()) == source).findFirst().orElse(null);
    return map == null || map.getName() == null || map.getName().isBlank()
      ? null : new ScriptBindingTarget.EntityInstance(map.getName(), source.getId());
  }

  @Override
  protected boolean isScriptCompatible(ScriptDefinition definition, IMapObject source) {
    return definition != null
        && definition.getHost() == ScriptHostType.ENTITY
        && compatible(definition, source);
  }

  @Override
  protected void createNewScript() {
    if (UI.getScriptWorkspacePanel() == null || this.currentSource == null) return;
    Class<?> targetClass = resolveEntityType(this.currentSource);
    ScriptDefinition created = UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.ENTITY, targetClass);
    if (created != null) {
      this.refreshAvailableScripts(this.currentSource);
      this.availableScripts.setSelectedItem(created);
      this.addSelectedScript();
    }
  }

  @Override
  protected String formatAvailableScriptLabel(ScriptDefinition definition) {
    String targetTag = definition.getTargetType() != null && !definition.getTargetType().isBlank()
        ? " [" + definition.getTargetType().substring(definition.getTargetType().lastIndexOf('.') + 1) + "]"
        : "";
    return displayName(definition) + targetTag;
  }

  @Override
  protected String getEmptyStateTitle() {
    return "No Scripts Attached";
  }

  @Override
  protected String getEmptyStateHint() {
    return "Select a script above and click <b>'+'</b><br>to attach behavior and parameters.";
  }

  @Override
  protected String getNoCompatibleScriptsText() {
    return "<No compatible scripts in project>";
  }

  @Override
  protected String getNewScriptTooltip() {
    return "Create a new entity script and attach it";
  }

  @Override
  protected ScriptHostType getSupportedHostType() {
    return ScriptHostType.ENTITY;
  }

  static boolean compatible(ScriptDefinition definition, IMapObject mapObject) {
    if (definition == null || definition.getTargetType() == null || definition.getTargetType().isBlank()) return true;
    Class<?> objectType = resolveEntityType(mapObject);
    if (objectType == null) return true;
    try {
      ClassLoader loader = mapObject.getClass().getClassLoader();
      if (loader == null) loader = Thread.currentThread().getContextClassLoader();
      if (loader == null) loader = ScriptBindingsInspectorPanel.class.getClassLoader();
      Class<?> target = Class.forName(definition.getTargetType(), false, loader);
      return target.isAssignableFrom(objectType);
    } catch (Throwable ignored) {
      return definition.getTargetType().equals(objectType.getName()) || definition.getTargetType().endsWith("." + objectType.getSimpleName());
    }
  }

  public static Class<?> resolveEntityType(IMapObject mapObject) {
    return ScriptBindingTypeResolver.resolve(mapObject);
  }
}
