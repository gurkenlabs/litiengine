package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.EntityListener;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityController;
import de.gurkenlabs.litiengine.environment.Environment;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Owns the script bindings of an entity and maps them onto the regular entity-controller lifecycle.
 *
 * <p>The controller is attached by the environment before {@link IEntity#loaded(Environment)} is fired. Script
 * instances are therefore created from the entity's loaded event, after its current environment is available.
 */
public final class EntityScriptController<T extends IEntity> implements IEntityController {
  /** Entity scripts are behavior orchestration and run after default movement controllers. */
  public static final int SCRIPT_UPDATE_PRIORITY = 100;
  private final T entity;
  private final EntityListener lifecycleListener;
  private List<ScriptBinding> explicitBindings;
  private List<ScriptBinding> defaultBindings = List.of();
  private List<ScriptBinding> bindings;
  private boolean controllerAttached;
  private boolean scriptsAttached;

  public EntityScriptController(T entity, Collection<ScriptBinding> bindings) {
    this.entity = Objects.requireNonNull(entity);
    this.explicitBindings = copyBindings(bindings);
    this.bindings = this.explicitBindings;
    this.lifecycleListener = new EntityListener() {
      @Override
      public void loaded(IEntity loaded, Environment environment) {
        if (controllerAttached && Game.scripts().isEnabled()) attachScripts();
      }

      @Override
      public void removed(IEntity removed, Environment environment) {
        detachScripts();
      }
    };
    this.entity.addListener(this.lifecycleListener);
  }

  @Override
  public void attach() {
    if (this.controllerAttached) return;
    this.controllerAttached = true;
    if (this.entity.isLoaded() && Game.scripts().isEnabled()) this.attachScripts();
  }

  @Override
  public void detach() {
    if (!this.controllerAttached) return;
    this.controllerAttached = false;
    this.detachScripts();
  }

  @Override
  public void update() {
    if (Game.scripts().isEnabled()) {
      Game.scripts().update(this.entity);
    }
  }

  @Override
  public int getUpdatePriority() {
    return SCRIPT_UPDATE_PRIORITY;
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  public List<ScriptBinding> getBindings() {
    return this.bindings;
  }

  /** Returns only bindings explicitly configured on this entity. */
  public List<ScriptBinding> getExplicitBindings() {
    return this.explicitBindings;
  }

  /** Replaces the ordered bindings and restarts this controller if it is active. */
  public void setBindings(Collection<ScriptBinding> bindings) {
    boolean restart = this.scriptsAttached;
    if (restart) this.detachScripts();
    this.explicitBindings = copyBindings(bindings);
    this.rebuildBindings();
    if (restart && this.controllerAttached && this.entity.isLoaded() && Game.scripts().isEnabled()) this.attachScripts();
  }

  /** Replaces inherited type-level bindings while retaining per-entity overrides. */
  public void setDefaultBindings(Collection<ScriptBinding> bindings) {
    boolean restart = this.scriptsAttached;
    if (restart) this.detachScripts();
    this.defaultBindings = copyBindings(bindings);
    this.rebuildBindings();
    if (restart && this.controllerAttached && this.entity.isLoaded() && Game.scripts().isEnabled()) this.attachScripts();
  }

  public boolean isAttached() {
    return this.scriptsAttached;
  }

  private void attachScripts() {
    if (this.scriptsAttached || !Game.scripts().isEnabled()) return;
    this.scriptsAttached = true;
    Game.scripts().attachAll(this.entity, this.bindings, true);
    if (Game.loop() != null) Game.loop().attach(this);
  }

  private void detachScripts() {
    if (!this.scriptsAttached) return;
    if (Game.loop() != null) Game.loop().detach(this);
    Game.scripts().detach(this.entity, true);
    this.scriptsAttached = false;
  }

  private static List<ScriptBinding> copyBindings(Collection<ScriptBinding> bindings) {
    return bindings == null ? List.of() : bindings.stream().filter(Objects::nonNull).map(ScriptBinding::new).toList();
  }

  private void rebuildBindings() {
    Map<String, ScriptBinding> merged = new LinkedHashMap<>();
    mergeInto(merged, this.defaultBindings);
    mergeInto(merged, this.explicitBindings);
    this.bindings = merged.values().stream().sorted(Comparator.comparingInt(ScriptBinding::getOrder)).toList();
  }

  private static void mergeInto(Map<String, ScriptBinding> merged, Collection<ScriptBinding> bindings) {
    for (ScriptBinding binding : bindings) {
      if (binding == null || binding.getScript() == null) continue;
      // A more specific or explicit binding replaces the earlier one and takes its stable ordering position.
      merged.remove(binding.getScript());
      merged.put(binding.getScript(), new ScriptBinding(binding));
    }
  }
}
