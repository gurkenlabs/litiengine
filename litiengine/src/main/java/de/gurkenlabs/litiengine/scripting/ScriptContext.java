package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityQuery;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedAbilityBuilder;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedProjectileBuilder;
import de.gurkenlabs.litiengine.scripting.ui.ScriptUiOverlay;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/** Runtime services and binding values supplied to a script instance. */
public final class ScriptContext<T> implements AutoCloseable {
  private final ScriptDefinition definition;
  private final ScriptBinding binding;
  private final T host;
  private final Subscriptions subscriptions = new Subscriptions();
  private final Logger logger;
  private ScriptUiOverlay uiOverlay;

  ScriptContext(ScriptDefinition definition, ScriptBinding binding, T host) {
    this.definition = Objects.requireNonNull(definition);
    this.binding = Objects.requireNonNull(binding);
    this.host = Objects.requireNonNull(host);
    this.logger = Logger.getLogger("scripts." + definition.getId());
  }

  public ScriptDefinition definition() { return this.definition; }
  public ScriptBinding binding() { return this.binding; }
  public T host() { return this.host; }
  public Logger logger() { return this.logger; }
  public ScriptGlobals globals() { return Game.scripts().globals(); }
  public Map<String, String> parameters() { return this.binding.getParameters(); }
  public String parameter(String name) { return this.binding.getParameters().get(name); }
  public String parameter(String name, String defaultValue) { return this.binding.getParameters().getOrDefault(name, defaultValue); }

  public Environment environment() {
    if (this.host instanceof Environment environment) return environment;
    if (this.host instanceof IEntity entity) return entity.getEnvironment();
    return Game.world().environment();
  }

  /** Starts a fluent query against the current environment. */
  public <E> EntityQuery<E> entities(Class<? extends E> type) {
    Environment environment = this.environment();
    if (environment == null) return new EntityQuery<>(java.util.List.of());
    return environment.query(type);
  }

  /** Begins building a scripted ability executed by the current host creature. */
  public ScriptedAbilityBuilder createAbility(String name) {
    if (!(this.host instanceof Creature creature)) {
      throw new IllegalStateException("The script host is not a Creature. Specify the executor explicitly via createAbility(executor, name).");
    }
    return new ScriptedAbilityBuilder(creature, name);
  }

  /** Begins building a scripted ability for a specific executor creature. */
  public ScriptedAbilityBuilder createAbility(Creature executor, String name) {
    return new ScriptedAbilityBuilder(executor, name);
  }

  /** Begins building and spawning a scripted projectile in the current environment. */
  public ScriptedProjectileBuilder spawnProjectile() {
    return new ScriptedProjectileBuilder(this.environment());
  }

  /** Returns the scripted UI overlay service owned by this context. */
  public synchronized ScriptUiOverlay ui() {
    if (this.uiOverlay == null) {
      this.uiOverlay = new ScriptUiOverlay();
      this.manage(this.uiOverlay);
    }
    return this.uiOverlay;
  }

  /** Adds a registration that will be released when the script is detached or reloaded. */
  public <S extends Subscription> S manage(S subscription) {
    return this.subscriptions.add(subscription);
  }

  /** Owns an arbitrary resource and invokes its release action when the script detaches or reloads. */
  public <R> R manage(R resource, Consumer<? super R> release) {
    Objects.requireNonNull(release);
    this.manage(() -> release.accept(resource));
    return resource;
  }

  /** Registers a listener and automatically removes it when the script detaches or reloads. */
  public <L> L listen(Consumer<? super L> add, Consumer<? super L> remove, L listener) {
    Objects.requireNonNull(add);
    Objects.requireNonNull(remove);
    Objects.requireNonNull(listener);
    add.accept(listener);
    this.manage(listener, remove);
    return listener;
  }

  /** Schedules a cancellable action on the game loop. */
  public Subscription schedule(int delay, Runnable action) {
    Objects.requireNonNull(action);
    int id = Game.loop().perform(delay, action);
    return this.manage(() -> Game.loop().removeAction(id));
  }

  /** Creates an ordered, cancellable sequence of actions and delays owned by this context. */
  public ScriptSequence sequence() {
    return new ScriptSequence(this);
  }

  @Override
  public void close() {
    this.subscriptions.close();
  }
}
