package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.AbilityExecution;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityQuery;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedAbilityBuilder;
import de.gurkenlabs.litiengine.scripting.combat.ScriptedProjectileBuilder;
import de.gurkenlabs.litiengine.scripting.ui.ScriptUiOverlay;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.logging.Logger;

/// Runtime services and binding values supplied to a script instance.
///
/// A context owns the input listeners, scheduled actions, UI elements, and other subscriptions
/// registered through it. [#close()] releases those resources when the script detaches or reloads.
///
/// @param <T> The type of object hosting the script.
/// @see ScriptInstance#attach(ScriptContext)
public final class ScriptContext<T> implements AutoCloseable {
  private final ScriptDefinition definition;
  private final ScriptBinding binding;
  private final T host;
  private final Subscriptions subscriptions = new Subscriptions();
  private final Logger logger;
  private ScriptUiOverlay uiOverlay;

  /// Creates a context for one attached script instance.
  ///
  /// @param definition The registered script definition.
  /// @param binding The binding that created the instance.
  /// @param host The object hosting the script.
  public ScriptContext(ScriptDefinition definition, ScriptBinding binding, T host) {
    this.definition = Objects.requireNonNull(definition);
    this.binding = Objects.requireNonNull(binding);
    this.host = Objects.requireNonNull(host);
    this.logger = Logger.getLogger("scripts." + definition.getId());
  }

  /// Returns the definition used to create the script.
  ///
  /// @return The script definition.
  public ScriptDefinition definition() { return this.definition; }

  /// Returns the binding applied to this instance.
  ///
  /// @return The script binding.
  public ScriptBinding binding() { return this.binding; }

  /// Returns the object hosting this script.
  ///
  /// @return The script host.
  public T host() { return this.host; }

  /// Returns a logger named for the script definition.
  ///
  /// @return The script logger.
  public Logger logger() { return this.logger; }

  /// Returns the game-wide script globals.
  ///
  /// @return The shared global registry.
  public ScriptGlobals globals() { return Game.scripts().globals(); }

  /// Returns the parameters configured by the binding.
  ///
  /// @return The binding parameters.
  public Map<String, String> parameters() { return this.binding.getParameters(); }

  /// Looks up a binding parameter.
  ///
  /// @param name The parameter name.
  /// @return The configured value, or `null` if absent.
  public String parameter(String name) { return this.binding.getParameters().get(name); }

  /// Looks up a binding parameter with a fallback.
  ///
  /// @param name The parameter name.
  /// @param defaultValue The value returned when the parameter is absent.
  /// @return The configured value or `defaultValue`.
  public String parameter(String name, String defaultValue) { return this.binding.getParameters().getOrDefault(name, defaultValue); }

  /// Returns the environment associated with this host.
  ///
  /// Entity hosts use their current environment; environment hosts return themselves.
  ///
  /// @return The associated environment, or `null` if neither the host nor the game world has one.
  public Environment environment() {
    if (this.host instanceof Environment environment) return environment;
    if (this.host instanceof IEntity entity) return entity.getEnvironment();
    return Game.world().environment();
  }

  /// Starts a fluent query against the current environment.
  ///
  /// @param type The entity type to query.
  /// @param <E> The entity type.
  /// @return A query over matching entities in the current environment.
  public <E> EntityQuery<E> entities(Class<? extends E> type) {
    Environment environment = this.environment();
    if (environment == null) return new EntityQuery<>(java.util.List.of());
    return environment.query(type);
  }

  /// Begins building a scripted ability executed by the current host creature.
  ///
  /// @param name The ability name.
  /// @return A builder for the new ability.
  /// @throws IllegalStateException if the host is not a creature.
  public ScriptedAbilityBuilder createAbility(String name) {
    if (!(this.host instanceof Creature creature)) {
      throw new IllegalStateException("The script host is not a Creature. Specify the executor explicitly via createAbility(executor, name).");
    }
    return new ScriptedAbilityBuilder(creature, name);
  }

  /// Begins building a scripted ability for a specific executor creature.
  ///
  /// @param executor The creature that will execute the ability.
  /// @param name The ability name.
  /// @return A builder for the new ability.
  public ScriptedAbilityBuilder createAbility(Creature executor, String name) {
    return new ScriptedAbilityBuilder(executor, name);
  }

  /// Casts an ability on the current host creature by name.
  ///
  /// @param name The registered ability name.
  /// @return The ability execution, or `null` when the ability cannot be cast.
  /// @throws IllegalStateException if the host is not a creature.
  public AbilityExecution cast(String name) {
    if (!(this.host instanceof Creature creature)) {
      throw new IllegalStateException("The script host is not a Creature. Specify the executor explicitly via cast(executor, name).");
    }
    return creature.cast(name);
  }

  /// Casts an ability on a specific executor creature by name.
  ///
  /// @param executor The creature that will execute the ability.
  /// @param name The registered ability name.
  /// @return The ability execution, or `null` when the executor is absent or cannot cast it.
  public AbilityExecution cast(Creature executor, String name) {
    return executor != null ? executor.cast(name) : null;
  }

  /// Checks if an ability on the current host creature can currently be cast.
  ///
  /// @param name The registered ability name.
  /// @return `true` if the host is a creature and can cast the ability.
  public boolean canCast(String name) {
    return this.host instanceof Creature creature && creature.canCast(name);
  }

  /// Checks if an ability on a specific executor creature can currently be cast.
  ///
  /// @param executor The creature to check.
  /// @param name The registered ability name.
  /// @return `true` if the executor exists and can cast the ability.
  public boolean canCast(Creature executor, String name) {
    return executor != null && executor.canCast(name);
  }

  /// Checks if an ability on the current host creature is currently on cooldown.
  ///
  /// @param name The registered ability name.
  /// @return `true` if the host is a creature and the ability is cooling down.
  public boolean isOnCooldown(String name) {
    return this.host instanceof Creature creature && creature.isOnCooldown(name);
  }

  /// Checks if an ability on a specific executor creature is currently on cooldown.
  ///
  /// @param executor The creature to check.
  /// @param name The registered ability name.
  /// @return `true` if the executor exists and the ability is cooling down.
  public boolean isOnCooldown(Creature executor, String name) {
    return executor != null && executor.isOnCooldown(name);
  }

  /// Begins building and spawning a scripted projectile in the current environment.
  ///
  /// @return A projectile builder bound to the current environment.
  public ScriptedProjectileBuilder spawnProjectile() {
    return new ScriptedProjectileBuilder(this.environment());
  }

  /// Returns the scripted UI overlay service owned by this context.
  ///
  /// @return The lazily created overlay service.
  public synchronized ScriptUiOverlay ui() {
    if (this.uiOverlay == null) {
      this.uiOverlay = new ScriptUiOverlay();
      this.manage(this.uiOverlay);
    }
    return this.uiOverlay;
  }

  /// Returns the active camera from the game world.
  ///
  /// @return The active camera, or `null` if none is configured.
  public ICamera camera() {
    return Game.world().camera();
  }

  private ScriptInput scriptInput;

  /// Returns the managed input helper owned by this context.
  ///
  /// @return The lazily created input helper.
  public synchronized ScriptInput input() {
    if (this.scriptInput == null) {
      this.scriptInput = new ScriptInput(this);
    }
    return this.scriptInput;
  }

  /// Returns a fluent spawner for creating entities in the current environment.
  ///
  /// @return A spawner bound to the current environment.
  /// @throws IllegalStateException if no environment is active.
  public ScriptedSpawner spawner() {
    Environment environment = this.environment();
    if (environment == null) throw new IllegalStateException("No environment is currently active to spawn entities into.");
    return new ScriptedSpawner(environment);
  }

  /// Spawns a creature with the given sprite prefix at the specified coordinates.
  ///
  /// @param spritePrefix The animation sprite prefix.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @return The spawned creature.
  public Creature spawnCreature(String spritePrefix, double x, double y) {
    return this.spawner().creature(spritePrefix).at(x, y).spawn();
  }

  /// Spawns a creature with the given sprite prefix at the specified location.
  ///
  /// @param spritePrefix The animation sprite prefix.
  /// @param location The location in map coordinates.
  /// @return The spawned creature.
  public Creature spawnCreature(String spritePrefix, java.awt.geom.Point2D location) {
    return this.spawner().creature(spritePrefix).at(location).spawn();
  }

  /// Spawns a prop with the given spritesheet at the specified coordinates.
  ///
  /// @param spriteSheet The spritesheet name.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @return The spawned prop.
  public de.gurkenlabs.litiengine.entities.Prop spawnProp(String spriteSheet, double x, double y) {
    return this.spawner().prop(spriteSheet).at(x, y).spawn();
  }

  /// Spawns a prop with the given spritesheet at the specified location.
  ///
  /// @param spriteSheet The spritesheet name.
  /// @param location The location in map coordinates.
  /// @return The spawned prop.
  public de.gurkenlabs.litiengine.entities.Prop spawnProp(String spriteSheet, java.awt.geom.Point2D location) {
    return this.spawner().prop(spriteSheet).at(location).spawn();
  }

  /// Spawns an entity of the given type at the specified coordinates.
  ///
  /// @param entityType The concrete type, which must have a no-argument constructor.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @param <E> The entity type.
  /// @return The spawned entity.
  public <E extends IEntity> E spawn(Class<E> entityType, double x, double y) {
    return this.spawner().entity(entityType).at(x, y).spawn();
  }

  /// Spawns an entity of the given type at the specified location.
  ///
  /// @param entityType The concrete type, which must have a no-argument constructor.
  /// @param location The location in map coordinates.
  /// @param <E> The entity type.
  /// @return The spawned entity.
  public <E extends IEntity> E spawn(Class<E> entityType, java.awt.geom.Point2D location) {
    return this.spawner().entity(entityType).at(location).spawn();
  }

  /// Spawns the given entity at the specified coordinates.
  ///
  /// @param entity The entity to add to the environment.
  /// @param x The map x-coordinate.
  /// @param y The map y-coordinate.
  /// @param <E> The entity type.
  /// @return The supplied entity.
  public <E extends IEntity> E spawn(E entity, double x, double y) {
    return this.spawner().entity(entity).at(x, y).spawn();
  }

  /// Spawns the given entity at the specified location.
  ///
  /// @param entity The entity to add to the environment.
  /// @param location The location in map coordinates.
  /// @param <E> The entity type.
  /// @return The supplied entity.
  public <E extends IEntity> E spawn(E entity, java.awt.geom.Point2D location) {
    return this.spawner().entity(entity).at(location).spawn();
  }

  /// Adds a registration that will be released when the script is detached or reloaded.
  ///
  /// @param subscription The subscription to own.
  /// @param <S> The subscription type.
  /// @return The supplied subscription.
  public <S extends Subscription> S manage(S subscription) {
    return this.subscriptions.add(subscription);
  }

  /// Owns an arbitrary resource and invokes its release action when the script detaches or reloads.
  ///
  /// @param resource The resource to own.
  /// @param release The action that releases it.
  /// @param <R> The resource type.
  /// @return The supplied resource.
  public <R> R manage(R resource, Consumer<? super R> release) {
    Objects.requireNonNull(release);
    this.manage(() -> release.accept(resource));
    return resource;
  }

  /// Registers a listener and automatically removes it when the script detaches or reloads.
  ///
  /// @param add The operation that registers the listener.
  /// @param remove The operation that unregisters it.
  /// @param listener The listener instance.
  /// @param <L> The listener type.
  /// @return The supplied listener.
  public <L> L listen(Consumer<? super L> add, Consumer<? super L> remove, L listener) {
    Objects.requireNonNull(add);
    Objects.requireNonNull(remove);
    Objects.requireNonNull(listener);
    add.accept(listener);
    this.manage(listener, remove);
    return listener;
  }

  /// Schedules a cancellable action on the game loop.
  ///
  /// @param delay The delay in milliseconds.
  /// @param action The action to invoke.
  /// @return A subscription that cancels the scheduled action.
  public Subscription schedule(int delay, Runnable action) {
    Objects.requireNonNull(action);
    int id = Game.loop().perform(delay, action);
    return this.manage(() -> Game.loop().removeAction(id));
  }

  /// Creates an ordered, cancellable sequence of actions and delays owned by this context.
  ///
  /// @return A new sequence owned by this context.
  public ScriptSequence sequence() {
    return new ScriptSequence(this);
  }

  @Override
  public void close() {
    this.subscriptions.close();
  }
}

