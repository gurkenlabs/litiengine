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
import de.gurkenlabs.utiliti.controller.UndoManager;
import java.util.List;

/**
 * Inline script attachment and exported-property inspector shared by all entity map-object types.
 */
public final class ScriptBindingsInspectorPanel extends AbstractScriptBindingsPanel<IMapObject> {

  public ScriptBindingsInspectorPanel() {
    super();
  }

  @Override
  protected List<ScriptBinding> readBindings(IMapObject source) {
    if (source == null) return List.of();
    try {
      return ScriptBindingCodec.decode(source.getStringValue(MapObjectProperty.SCRIPT_BINDINGS, null));
    } catch (IllegalArgumentException e) {
      return List.of();
    }
  }

  @Override
  protected void persistBindings(IMapObject source, List<ScriptBinding> bindings) {
    if (source == null) return;
    UndoManager.instance().mapObjectChanging(source);
    if (bindings == null || bindings.isEmpty()) {
      source.removeProperty(MapObjectProperty.SCRIPT_BINDINGS);
    } else {
      source.setValue(MapObjectProperty.SCRIPT_BINDINGS, ScriptBindingCodec.encode(bindings));
    }
    UndoManager.instance().mapObjectChanged(source);
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
    return "Select a script above and click <b>'+'</b><br>to bind behaviors & parameters.";
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

  static Class<?> resolveEntityType(IMapObject mapObject) {
    if (mapObject == null || mapObject.getType() == null) return IEntity.class;
    String implementation = mapObject.getStringValue(MapObjectProperty.IMPLEMENTATION, null);
    if (implementation != null && !implementation.isBlank()) {
      var discovered = de.gurkenlabs.utiliti.controller.Editor.instance().getProjectCodeIntegration().getDefinitions().stream()
          .filter(d -> implementation.equals(d.id())).findFirst().orElse(null);
      if (discovered != null) {
        try {
          return Class.forName(discovered.className(), false,
              de.gurkenlabs.utiliti.controller.Editor.instance().getProjectCodeIntegration().getClassLoader());
        } catch (ClassNotFoundException | LinkageError ignored) {
          // Fall back to map object type
        }
      }
    }
    MapObjectType type = MapObjectType.get(mapObject.getType());
    if (type == null) return IEntity.class;
    return switch (type) {
      case PROP -> Prop.class;
      case CREATURE -> Creature.class;
      case LIGHTSOURCE -> LightSource.class;
      case TRIGGER -> Trigger.class;
      case SPAWNPOINT -> Spawnpoint.class;
      case COLLISIONBOX -> CollisionBox.class;
      case STATICSHADOW -> StaticShadow.class;
      case SOUNDSOURCE -> SoundSource.class;
      case EMITTER -> Emitter.class;
      default -> IEntity.class;
    };
  }
}
