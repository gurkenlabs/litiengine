package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;

/**
 * Inline script attachment and property inspector for map environment scripts.
 */
public final class EnvironmentScriptInspectorPanel extends AbstractScriptBindingsPanel<IMap> {

  public EnvironmentScriptInspectorPanel() {
    super();
  }

  @Override
  protected ScriptBindingTarget getBindingTarget(IMap source) {
    return source == null || source.getName() == null || source.getName().isBlank()
      ? null : new ScriptBindingTarget.Environment(source.getName());
  }

  @Override
  protected boolean isScriptCompatible(ScriptDefinition definition, IMap source) {
    return definition != null && definition.getHost() == ScriptHostType.ENVIRONMENT;
  }

  @Override
  protected void createNewScript() {
    if (UI.getScriptWorkspacePanel() == null || this.currentSource == null) return;
    ScriptDefinition created = UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.ENVIRONMENT);
    if (created != null) {
      this.refreshAvailableScripts(this.currentSource);
      this.availableScripts.setSelectedItem(created);
      this.addSelectedScript();
    }
  }

  @Override
  protected String getEmptyStateTitle() {
    return "No Environment Scripts Attached";
  }

  @Override
  protected String getEmptyStateHint() {
    return "Select a script above and click <b>'+'</b><br>to attach map-level objectives and cinematics.";
  }

  @Override
  protected String getNoCompatibleScriptsText() {
    return "<No compatible environment scripts>";
  }

  @Override
  protected String getNewScriptTooltip() {
    return "Create a new environment script and attach it";
  }

  @Override
  protected ScriptHostType getSupportedHostType() {
    return ScriptHostType.ENVIRONMENT;
  }
}
