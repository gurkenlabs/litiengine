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
  public CreatureBuilder creature(String spritePrefix) {
    return new CreatureBuilder(this.environment, spritePrefix);
  }

  /// Starts building a [Prop] to spawn.
  public PropBuilder prop(String spriteSheet) {
    return new PropBuilder(this.environment, spriteSheet);
  }

  /// Starts building an entity of arbitrary type via reflection.
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
  public <T extends IEntity> EntityBuilder<T> entity(Supplier<T> factory) {
    return new EntityBuilder<>(this.environment, factory);
  }

  /// Starts configuring an existing entity instance to spawn.
  public <T extends IEntity> EntityBuilder<T> entity(T entity) {
    Objects.requireNonNull(entity, "Entity must not be null.");
    return new EntityBuilder<>(this.environment, () -> entity);
  }

  public static class EntityBuilder<T extends IEntity> {
    protected final Environment env;
    protected final Supplier<T> factory;
    protected Point2D location = new Point2D.Double(0, 0);
    protected String name;
    protected String[] tags;
    protected RenderType renderType;
    protected Integer health;
    protected Consumer<T> customizer;

    public EntityBuilder(Environment env, Supplier<T> factory) {
      this.env = Objects.requireNonNull(env);
      this.factory = Objects.requireNonNull(factory);
    }

    public EntityBuilder<T> at(double x, double y) {
      this.location = new Point2D.Double(x, y);
      return this;
    }

    public EntityBuilder<T> at(Point2D location) {
      if (location != null) this.location = location;
      return this;
    }

    public EntityBuilder<T> withName(String name) {
      this.name = name;
      return this;
    }

    public EntityBuilder<T> withTags(String... tags) {
      this.tags = tags;
      return this;
    }

    public EntityBuilder<T> withRenderType(RenderType renderType) {
      this.renderType = renderType;
      return this;
    }

    public EntityBuilder<T> withHealth(int health) {
      this.health = health;
      return this;
    }

    public EntityBuilder<T> configure(Consumer<T> customizer) {
      this.customizer = customizer;
      return this;
    }

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

  public static final class CreatureBuilder extends EntityBuilder<Creature> {
    private final String spritePrefix;

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

  public static final class PropBuilder extends EntityBuilder<Prop> {
    private final String spriteSheet;

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
