package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import de.gurkenlabs.litiengine.scripting.ScriptBindingCodec;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptManager;
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.util.List;

/**
 * Inline script attachment and property inspector for map environment scripts.
 */
public final class EnvironmentScriptInspectorPanel extends AbstractScriptBindingsPanel<IMap> {

  public EnvironmentScriptInspectorPanel() {
    super();
  }

  @Override
  protected List<ScriptBinding> readBindings(IMap source) {
    if (source == null) return List.of();
    try {
      return ScriptBindingCodec.decode(source.getStringValue(ScriptManager.BINDINGS_PROPERTY, null));
    } catch (IllegalArgumentException e) {
      return List.of();
    }
  }

  @Override
  protected void persistBindings(IMap source, List<ScriptBinding> bindings) {
    if (source == null) return;
    UndoManager.instance().mapChanging(source);
    if (bindings == null || bindings.isEmpty()) {
      source.removeProperty(ScriptManager.BINDINGS_PROPERTY);
    } else {
      source.setValue(ScriptManager.BINDINGS_PROPERTY, ScriptBindingCodec.encode(bindings));
    }
    UndoManager.instance().mapChanged(source);
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
    return "Select a script above and click <b>'+'</b><br>to bind map-level objectives & cinematics.";
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
