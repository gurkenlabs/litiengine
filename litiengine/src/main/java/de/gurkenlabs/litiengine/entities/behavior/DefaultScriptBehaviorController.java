package de.gurkenlabs.litiengine.entities.behavior;

import de.gurkenlabs.litiengine.Game;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.scripting.EntityScriptController;
import de.gurkenlabs.litiengine.scripting.ScriptBinding;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * A default behavior controller that loads and manages entity scripts internally.
 *
 * <p>This controller encapsulates script bindings and delegates execution to an internal
 * {@link EntityScriptController}, allowing entity behavior to be driven dynamically by runtime scripts.
 *
 * @param <T> the type of controlled entity
 */
public class DefaultScriptBehaviorController<T extends IEntity> implements IBehaviorController {
  private final T entity;
  private final EntityScriptController<T> scriptController;
  private final List<ScriptBinding> loadedBindings = new ArrayList<>();

  public DefaultScriptBehaviorController(final T entity) {
    this(entity, List.of());
  }

  public DefaultScriptBehaviorController(final T entity, final String... scriptIds) {
    this(entity, createBindings(scriptIds));
  }

  public DefaultScriptBehaviorController(final T entity, final Collection<ScriptBinding> bindings) {
    this.entity = Objects.requireNonNull(entity, "Entity must not be null.");
    if (bindings != null) {
      for (ScriptBinding binding : bindings) {
        if (binding != null) {
          this.loadedBindings.add(new ScriptBinding(binding));
        }
      }
    }
    this.scriptController = new EntityScriptController<>(this.entity, this.loadedBindings);
  }

  @Override
  public T getEntity() {
    return this.entity;
  }

  /**
   * Returns the internal {@link EntityScriptController} managing the script instances.
   *
   * @return the script controller
   */
  public EntityScriptController<T> getScriptController() {
    return this.scriptController;
  }

  /**
   * Loads a script by its definition ID with default parameters.
   *
   * @param scriptId the script definition ID to load
   */
  public void loadScript(final String scriptId) {
    if (scriptId == null || scriptId.isBlank()) return;
    this.loadScript(new ScriptBinding(scriptId));
  }

  /**
   * Loads a script with specific bindings and configuration parameters.
   *
   * @param binding the script binding to load
   */
  public void loadScript(final ScriptBinding binding) {
    if (binding == null || binding.getScript() == null) return;
    this.loadedBindings.removeIf(b -> binding.getScript().equals(b.getScript()));
    this.loadedBindings.add(new ScriptBinding(binding));
    this.scriptController.setBindings(this.loadedBindings);
  }

  /**
   * Loads multiple script bindings.
   *
   * @param bindings the script bindings to load
   */
  public void loadScripts(final Collection<ScriptBinding> bindings) {
    if (bindings == null) return;
    for (ScriptBinding binding : bindings) {
      if (binding != null && binding.getScript() != null) {
        this.loadedBindings.removeIf(b -> binding.getScript().equals(b.getScript()));
        this.loadedBindings.add(new ScriptBinding(binding));
      }
    }
    this.scriptController.setBindings(this.loadedBindings);
  }

  /**
   * Unloads a script by its definition ID.
   *
   * @param scriptId the script definition ID to unload
   */
  public void unloadScript(final String scriptId) {
    if (scriptId == null) return;
    if (this.loadedBindings.removeIf(b -> scriptId.equals(b.getScript()))) {
      this.scriptController.setBindings(this.loadedBindings);
    }
  }

  /**
   * Unloads all scripts currently loaded by this behavior controller.
   */
  public void unloadAllScripts() {
    this.loadedBindings.clear();
    this.scriptController.setBindings(this.loadedBindings);
  }

  /**
   * Returns an unmodifiable list of script bindings currently loaded by this controller.
   *
   * @return the loaded script bindings
   */
  public List<ScriptBinding> getLoadedScripts() {
    return List.copyOf(this.loadedBindings);
  }

  @Override
  public void attach() {
    this.scriptController.attach();
  }

  @Override
  public void detach() {
    this.scriptController.detach();
  }

  @Override
  public void update() {
    if (!Game.scripts().isEnabled()) return;
    this.scriptController.update();
    try {
      this.updateBehavior();
    } catch (Exception e) {
      if (e instanceof RuntimeException re) throw re;
      throw new RuntimeException(e);
    }
  }

  protected void updateBehavior() throws Exception {}

  private static List<ScriptBinding> createBindings(final String[] scriptIds) {
    if (scriptIds == null || scriptIds.length == 0) return List.of();
    List<ScriptBinding> bindings = new ArrayList<>();
    for (String scriptId : scriptIds) {
      if (scriptId != null && !scriptId.isBlank()) {
        bindings.add(new ScriptBinding(scriptId));
      }
    }
    return bindings;
  }
}
