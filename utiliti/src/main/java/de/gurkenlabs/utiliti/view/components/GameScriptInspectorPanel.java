package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.utiliti.controller.ScriptBindingTarget;

/** Assignment and property editor for scripts owned by the game lifecycle. */
public final class GameScriptInspectorPanel extends AbstractScriptBindingsPanel<ScriptBindingTarget.Game> {
  public void bindGame() {
    this.bind(new ScriptBindingTarget.Game());
  }

  @Override
  protected ScriptBindingTarget getBindingTarget(ScriptBindingTarget.Game source) {
    return source;
  }

  @Override
  protected boolean isScriptCompatible(ScriptDefinition definition, ScriptBindingTarget.Game source) {
    return definition != null && definition.getHost() == ScriptHostType.GAME;
  }

  @Override
  protected void createNewScript() {
    if (UI.getScriptWorkspacePanel() == null) return;
    ScriptDefinition created = UI.getScriptWorkspacePanel().createScript(ScriptWorkspacePanel.ScriptKind.GAME);
    if (created != null) {
      this.refreshAvailableScripts();
      this.availableScripts.setSelectedItem(created);
      this.addSelectedScript();
    }
  }

  @Override
  protected String getEmptyStateTitle() {
    return "No Game Scripts Attached";
  }

  @Override
  protected String getEmptyStateHint() {
    return "Choose a game script above or create a new one.";
  }

  @Override
  protected String getNoCompatibleScriptsText() {
    return "<No compatible game scripts>";
  }

  @Override
  protected String getNewScriptTooltip() {
    return "Create and attach a game script";
  }

  @Override
  protected ScriptHostType getSupportedHostType() {
    return ScriptHostType.GAME;
  }
}
