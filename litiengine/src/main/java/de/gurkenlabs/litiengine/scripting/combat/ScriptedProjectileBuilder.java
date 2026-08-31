package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import java.awt.geom.Point2D;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/// Fluent builder to configure and spawn [ScriptedProjectile] instances.
public final class ScriptedProjectileBuilder {
  private final Environment environment;
  private String spritePrefix = "projectile";
  private Point2D origin = new Point2D.Double();
  private double angle = 0;
  private double speed = 200;
  private int damage = 10;
  private double maxDistance = 1000;
  private boolean pierce = false;
  private boolean collideWithEnvironment = true;
  private double width = 16;
  private double height = 16;
  private double collisionWidth = 16;
  private double collisionHeight = 16;
  private double splashRadius = 0;
  private int splashDamage = 0;
  private ICombatEntity sourceEntity;
  private BiConsumer<ICombatEntity, ScriptedProjectile> onHitEntity;
  private Consumer<ScriptedProjectile> onHitEnvironment;
  private Consumer<ScriptedProjectile> onExpire;

  public ScriptedProjectileBuilder(Environment environment) {
    this.environment = environment;
  }

  public ScriptedProjectileBuilder sprite(String spritePrefix) {
    this.spritePrefix = spritePrefix == null ? "projectile" : spritePrefix;
    return this;
  }

  public ScriptedProjectileBuilder from(Point2D origin) {
    this.origin = Objects.requireNonNull(origin);
    return this;
  }

  public ScriptedProjectileBuilder from(double x, double y) {
    this.origin = new Point2D.Double(x, y);
    return this;
  }

  public ScriptedProjectileBuilder angle(double angleDegrees) {
    this.angle = angleDegrees;
    return this;
  }

  public ScriptedProjectileBuilder towards(Point2D target) {
    Objects.requireNonNull(target);
    this.angle = GeometricUtilities.calcRotationAngleInDegrees(this.origin, target);
    return this;
  }

  public ScriptedProjectileBuilder towards(double targetX, double targetY) {
    return this.towards(new Point2D.Double(targetX, targetY));
  }

  public ScriptedProjectileBuilder speed(double speed) {
    this.speed = Math.max(0, speed);
    return this;
  }

  public ScriptedProjectileBuilder damage(int damage) {
    this.damage = Math.max(0, damage);
    return this;
  }

  public ScriptedProjectileBuilder maxDistance(double distance) {
    this.maxDistance = Math.max(0, distance);
    return this;
  }

  public ScriptedProjectileBuilder pierce(boolean pierce) {
    this.pierce = pierce;
    return this;
  }

  public ScriptedProjectileBuilder collideWithEnvironment(boolean collide) {
    this.collideWithEnvironment = collide;
    return this;
  }

  public ScriptedProjectileBuilder size(double width, double height) {
    this.width = Math.max(1, width);
    this.height = Math.max(1, height);
    return this;
  }

  public ScriptedProjectileBuilder collisionBox(double width, double height) {
    this.collisionWidth = Math.max(1, width);
    this.collisionHeight = Math.max(1, height);
    return this;
  }

  public ScriptedProjectileBuilder splash(double radius, int splashDamage) {
    this.splashRadius = Math.max(0, radius);
    this.splashDamage = Math.max(0, splashDamage);
    return this;
  }

  public ScriptedProjectileBuilder source(ICombatEntity source) {
    this.sourceEntity = source;
    return this;
  }

  public ScriptedProjectileBuilder onHitEntity(BiConsumer<ICombatEntity, ScriptedProjectile> onHit) {
    this.onHitEntity = onHit;
    return this;
  }

  public ScriptedProjectileBuilder onHitEnvironment(Consumer<ScriptedProjectile> onHit) {
    this.onHitEnvironment = onHit;
    return this;
  }

  public ScriptedProjectileBuilder onExpire(Consumer<ScriptedProjectile> onExpire) {
    this.onExpire = onExpire;
    return this;
  }

  public ScriptedProjectile spawn() {
    Environment targetEnv = this.environment != null ? this.environment : Game.world().environment();
    ScriptedProjectile projectile = new ScriptedProjectile(
        this.spritePrefix,
        this.origin,
        this.angle,
        this.speed,
        this.damage,
        this.maxDistance,
        this.pierce,
        this.collideWithEnvironment,
        this.splashRadius,
        this.splashDamage,
        this.sourceEntity,
        this.onHitEntity,
        this.onHitEnvironment,
        this.onExpire,
        targetEnv);
    projectile.setSize(this.width, this.height);
    projectile.setCollisionBoxWidth(this.collisionWidth);
    projectile.setCollisionBoxHeight(this.collisionHeight);
    if (targetEnv != null) {
      targetEnv.add(projectile);
    }
    return projectile;
  }
}
