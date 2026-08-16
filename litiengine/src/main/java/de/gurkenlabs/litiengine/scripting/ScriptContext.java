package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
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

  /** Casts an ability on the current host creature by name. */
  public AbilityExecution cast(String name) {
    if (!(this.host instanceof Creature creature)) {
      throw new IllegalStateException("The script host is not a Creature. Specify the executor explicitly via cast(executor, name).");
    }
    return creature.cast(name);
  }

  /** Casts an ability on a specific executor creature by name. */
  public AbilityExecution cast(Creature executor, String name) {
    return executor != null ? executor.cast(name) : null;
  }

  /** Checks if an ability on the current host creature can currently be cast. */
  public boolean canCast(String name) {
    return this.host instanceof Creature creature && creature.canCast(name);
  }

  /** Checks if an ability on a specific executor creature can currently be cast. */
  public boolean canCast(Creature executor, String name) {
    return executor != null && executor.canCast(name);
  }

  /** Checks if an ability on the current host creature is currently on cooldown. */
  public boolean isOnCooldown(String name) {
    return this.host instanceof Creature creature && creature.isOnCooldown(name);
  }

  /** Checks if an ability on a specific executor creature is currently on cooldown. */
  public boolean isOnCooldown(Creature executor, String name) {
    return executor != null && executor.isOnCooldown(name);
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

  private ScriptInput scriptInput;

  /** Returns the managed input helper owned by this context. */
  public synchronized ScriptInput input() {
    if (this.scriptInput == null) {
      this.scriptInput = new ScriptInput(this);
    }
    return this.scriptInput;
  }

  /** Returns a fluent spawner for creating entities in the current environment. */
  public ScriptedSpawner spawner() {
    Environment environment = this.environment();
    if (environment == null) throw new IllegalStateException("No environment is currently active to spawn entities into.");
    return new ScriptedSpawner(environment);
  }

  /** Spawns a creature with the given sprite prefix at the specified coordinates. */
  public Creature spawnCreature(String spritePrefix, double x, double y) {
    return this.spawner().creature(spritePrefix).at(x, y).spawn();
  }

  /** Spawns a creature with the given sprite prefix at the specified location. */
  public Creature spawnCreature(String spritePrefix, java.awt.geom.Point2D location) {
    return this.spawner().creature(spritePrefix).at(location).spawn();
  }

  /** Spawns a prop with the given spritesheet at the specified coordinates. */
  public de.gurkenlabs.litiengine.entities.Prop spawnProp(String spriteSheet, double x, double y) {
    return this.spawner().prop(spriteSheet).at(x, y).spawn();
  }

  /** Spawns a prop with the given spritesheet at the specified location. */
  public de.gurkenlabs.litiengine.entities.Prop spawnProp(String spriteSheet, java.awt.geom.Point2D location) {
    return this.spawner().prop(spriteSheet).at(location).spawn();
  }

  /** Spawns an entity of the given type at the specified coordinates. */
  public <E extends IEntity> E spawn(Class<E> entityType, double x, double y) {
    return this.spawner().entity(entityType).at(x, y).spawn();
  }

  /** Spawns an entity of the given type at the specified location. */
  public <E extends IEntity> E spawn(Class<E> entityType, java.awt.geom.Point2D location) {
    return this.spawner().entity(entityType).at(location).spawn();
  }

  /** Spawns the given entity at the specified coordinates. */
  public <E extends IEntity> E spawn(E entity, double x, double y) {
    return this.spawner().entity(entity).at(x, y).spawn();
  }

  /** Spawns the given entity at the specified location. */
  public <E extends IEntity> E spawn(E entity, java.awt.geom.Point2D location) {
    return this.spawner().entity(entity).at(location).spawn();
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

