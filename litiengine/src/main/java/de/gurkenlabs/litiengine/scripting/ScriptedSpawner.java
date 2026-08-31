package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.RenderType;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

/// Fluent builder for spawning typed entities directly into an environment from scripts.
public final class ScriptedSpawner {
  private final Environment environment;

  public ScriptedSpawner(Environment environment) {
    this.environment = Objects.requireNonNull(environment, "Environment must not be null.");
  }

  /// Starts building a [Creature] to spawn.
  ///
  /// @param spritePrefix The animation sprite prefix.
  /// @return A creature builder.
  public CreatureBuilder creature(String spritePrefix) {
    return new CreatureBuilder(this.environment, spritePrefix);
  }

  /// Starts building a [Prop] to spawn.
  ///
  /// @param spriteSheet The spritesheet name.
  /// @return A prop builder.
  public PropBuilder prop(String spriteSheet) {
    return new PropBuilder(this.environment, spriteSheet);
  }

  /// Starts building an entity of arbitrary type via reflection.
  ///
  /// @param type The concrete type, which must declare a no-argument constructor that reflection is permitted to open.
  /// @param <T> The entity type.
  /// @return A builder using reflective construction.
  public <T extends IEntity> EntityBuilder<T> entity(Class<T> type) {
    Objects.requireNonNull(type, "Entity class must not be null.");
    return new EntityBuilder<>(this.environment, () -> {
      try {
        var ctor = type.getDeclaredConstructor();
        ctor.setAccessible(true);
        return ctor.newInstance();
      } catch (Exception e) {
        throw new IllegalArgumentException("Could not instantiate entity class " + type.getName() + " with a no-arg constructor.", e);
      }
    });
  }

  /// Starts building an entity supplied by a factory.
  ///
  /// @param factory The factory invoked for each [EntityBuilder#spawn()] call.
  /// @param <T> The entity type.
  /// @return A builder using the factory.
  public <T extends IEntity> EntityBuilder<T> entity(Supplier<T> factory) {
    return new EntityBuilder<>(this.environment, factory);
  }

  /// Starts configuring an existing entity instance to spawn.
  ///
  /// @param entity The entity to configure and add.
  /// @param <T> The entity type.
  /// @return A builder that returns the supplied instance.
  public <T extends IEntity> EntityBuilder<T> entity(T entity) {
    Objects.requireNonNull(entity, "Entity must not be null.");
    return new EntityBuilder<>(this.environment, () -> entity);
  }

  /// Configures an entity before adding it to an environment.
  ///
  /// Builders are reusable when their factory creates a fresh entity for every invocation.
  ///
  /// @param <T> The entity type.
  public static class EntityBuilder<T extends IEntity> {
    protected final Environment env;
    protected final Supplier<T> factory;
    protected Point2D location = new Point2D.Double(0, 0);
    protected String name;
    protected String[] tags;
    protected RenderType renderType;
    protected Integer health;
    protected Consumer<T> customizer;

    /// Creates an entity builder.
    ///
    /// @param env The destination environment.
    /// @param factory The entity factory.
    public EntityBuilder(Environment env, Supplier<T> factory) {
      this.env = Objects.requireNonNull(env);
      this.factory = Objects.requireNonNull(factory);
    }

    /// Sets the spawn coordinates.
    ///
    /// @param x The map x-coordinate.
    /// @param y The map y-coordinate.
    /// @return This builder.
    public EntityBuilder<T> at(double x, double y) {
      this.location = new Point2D.Double(x, y);
      return this;
    }

    /// Sets the spawn location.
    ///
    /// @param location The location in map coordinates; `null` leaves it unchanged.
    /// @return This builder.
    public EntityBuilder<T> at(Point2D location) {
      if (location != null) this.location = location;
      return this;
    }

    /// Sets the entity name.
    ///
    /// @param name The name to assign.
    /// @return This builder.
    public EntityBuilder<T> withName(String name) {
      this.name = name;
      return this;
    }

    /// Sets tags to add to the entity.
    ///
    /// @param tags The tags; null entries are ignored.
    /// @return This builder.
    public EntityBuilder<T> withTags(String... tags) {
      this.tags = tags;
      return this;
    }

    /// Sets the entity's render layer.
    ///
    /// @param renderType The render type, or `null` to preserve the entity default.
    /// @return This builder.
    public EntityBuilder<T> withRenderType(RenderType renderType) {
      this.renderType = renderType;
      return this;
    }

    /// Sets current and maximum hit points when the entity supports combat.
    ///
    /// @param health The hit-point value.
    /// @return This builder.
    public EntityBuilder<T> withHealth(int health) {
      this.health = health;
      return this;
    }

    /// Sets an operation invoked after standard configuration but before insertion.
    ///
    /// @param customizer The customizer, or `null` for none.
    /// @return This builder.
    public EntityBuilder<T> configure(Consumer<T> customizer) {
      this.customizer = customizer;
      return this;
    }

    /// Creates, configures, and adds an entity to the environment.
    ///
    /// @return The spawned entity, or `null` if the factory returned `null`.
    public T spawn() {
      T instance = this.factory.get();
      if (instance == null) return null;
      instance.setLocation(this.location);
      if (this.name != null) instance.setName(this.name);
      if (this.tags != null) {
        for (String tag : this.tags) {
          if (tag != null) instance.addTag(tag);
        }
      }
      if (this.renderType != null) instance.setRenderType(this.renderType);
      if (this.health != null && instance instanceof ICombatEntity combat) {
        combat.getHitPoints().setMax(this.health);
        combat.getHitPoints().setValue(this.health);
      }
      if (this.customizer != null) {
        this.customizer.accept(instance);
      }
      this.env.add(instance);
      return instance;
    }
  }

  /// Type-preserving builder for creatures.
  public static final class CreatureBuilder extends EntityBuilder<Creature> {
    private final String spritePrefix;

    /// Creates a creature builder.
    ///
    /// @param env The destination environment.
    /// @param spritePrefix The animation sprite prefix.
    public CreatureBuilder(Environment env, String spritePrefix) {
      super(env, () -> new Creature(spritePrefix));
      this.spritePrefix = spritePrefix;
    }

    @Override
    public CreatureBuilder at(double x, double y) {
      super.at(x, y);
      return this;
    }

    @Override
    public CreatureBuilder at(Point2D location) {
      super.at(location);
      return this;
    }

    @Override
    public CreatureBuilder withName(String name) {
      super.withName(name);
      return this;
    }

    @Override
    public CreatureBuilder withTags(String... tags) {
      super.withTags(tags);
      return this;
    }

    @Override
    public CreatureBuilder withHealth(int health) {
      super.withHealth(health);
      return this;
    }

    @Override
    public CreatureBuilder configure(Consumer<Creature> customizer) {
      super.configure(customizer);
      return this;
    }
  }

  /// Type-preserving builder for props.
  public static final class PropBuilder extends EntityBuilder<Prop> {
    private final String spriteSheet;

    /// Creates a prop builder.
    ///
    /// @param env The destination environment.
    /// @param spriteSheet The spritesheet name.
    public PropBuilder(Environment env, String spriteSheet) {
      super(env, () -> new Prop(spriteSheet));
      this.spriteSheet = spriteSheet;
    }

    @Override
    public PropBuilder at(double x, double y) {
      super.at(x, y);
      return this;
    }

    @Override
    public PropBuilder at(Point2D location) {
      super.at(location);
      return this;
    }

    @Override
    public PropBuilder withName(String name) {
      super.withName(name);
      return this;
    }

    @Override
    public PropBuilder withTags(String... tags) {
      super.withTags(tags);
      return this;
    }

    @Override
    public PropBuilder configure(Consumer<Prop> customizer) {
      super.configure(customizer);
      return this;
    }
  }
}
