package de.gurkenlabs.litiengine.scripting.combat;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.physics.Collision;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/// A projectile entity created dynamically via scripting that moves, impacts targets, and handles collision.
public class ScriptedProjectile extends Creature implements IUpdateable {
  private final Environment environment;
  private final ICombatEntity sourceEntity;
  private final double speed;
  private final int damage;
  private final double maxDistance;
  private final boolean pierce;
  private final boolean collideWithEnvironment;
  private final double splashRadius;
  private final int splashDamage;
  private final BiConsumer<ICombatEntity, ScriptedProjectile> onHitEntity;
  private final Consumer<ScriptedProjectile> onHitEnvironment;
  private final Consumer<ScriptedProjectile> onExpire;
  private final Set<ICombatEntity> hitTargets = new HashSet<>();
  private double traveledDistance;

  public ScriptedProjectile(
      String spritePrefix,
      Point2D origin,
      double angle,
      double speed,
      int damage,
      double maxDistance,
      boolean pierce,
      boolean collideWithEnvironment,
      double splashRadius,
      int splashDamage,
      ICombatEntity sourceEntity,
      BiConsumer<ICombatEntity, ScriptedProjectile> onHitEntity,
      Consumer<ScriptedProjectile> onHitEnvironment,
      Consumer<ScriptedProjectile> onExpire,
      Environment environment) {
    super(spritePrefix);
    this.environment = environment;
    this.setLocation(origin);
    this.setSize(16, 16);
    this.setCollisionBoxWidth(16);
    this.setCollisionBoxHeight(16);
    this.setAngle((float) angle);
    this.speed = speed;
    this.damage = damage;
    this.maxDistance = maxDistance > 0 ? maxDistance : Double.MAX_VALUE;
    this.pierce = pierce;
    this.collideWithEnvironment = collideWithEnvironment;
    this.splashRadius = Math.max(0, splashRadius);
    this.splashDamage = Math.max(0, splashDamage);
    this.sourceEntity = sourceEntity;
    this.onHitEntity = onHitEntity;
    this.onHitEnvironment = onHitEnvironment;
    this.onExpire = onExpire;
  }

  public ScriptedProjectile(
      String spritePrefix,
      Point2D origin,
      double angle,
      double speed,
      int damage,
      double maxDistance,
      boolean pierce,
      boolean collideWithEnvironment,
      ICombatEntity sourceEntity,
      BiConsumer<ICombatEntity, ScriptedProjectile> onHitEntity,
      Consumer<ScriptedProjectile> onHitEnvironment,
      Environment environment) {
    this(spritePrefix, origin, angle, speed, damage, maxDistance, pierce, collideWithEnvironment, 0, 0, sourceEntity, onHitEntity, onHitEnvironment, null, environment);
  }

  public ScriptedProjectile(
      String spritePrefix,
      Point2D origin,
      double angle,
      double speed,
      int damage,
      double maxDistance,
      boolean pierce,
      boolean collideWithEnvironment,
      ICombatEntity sourceEntity,
      BiConsumer<ICombatEntity, ScriptedProjectile> onHitEntity,
      Consumer<ScriptedProjectile> onHitEnvironment) {
    this(spritePrefix, origin, angle, speed, damage, maxDistance, pierce, collideWithEnvironment, 0, 0, sourceEntity, onHitEntity, onHitEnvironment, null, null);
  }

  public ICombatEntity getSourceEntity() {
    return this.sourceEntity;
  }

  public double getSpeed() {
    return this.speed;
  }

  public int getDamage() {
    return this.damage;
  }

  public double getTraveledDistance() {
    return this.traveledDistance;
  }

  public double getMaxDistance() {
    return this.maxDistance;
  }

  public boolean isPierce() {
    return this.pierce;
  }

  public double getSplashRadius() {
    return this.splashRadius;
  }

  public int getSplashDamage() {
    return this.splashDamage;
  }

  @Override
  public void update() {
    Environment env = this.getEnvironment() != null ? this.getEnvironment() : (this.environment != null ? this.environment : Game.world().environment());
    if (env == null || this.isDead()) {
      return;
    }

    double deltaSeconds = Game.loop() != null && Game.loop().getDeltaTime() > 0
        ? Game.loop().getDeltaTime() / 1000.0
        : (Game.loop() != null && Game.loop().getTickRate() > 0 ? 1.0 / Game.loop().getTickRate() : 1.0 / 60.0);
    deltaSeconds = Math.min(deltaSeconds, 0.1);
    double step = this.speed * deltaSeconds;
    Point2D nextLoc = de.gurkenlabs.litiengine.util.geom.GeometricUtilities.project(this.getLocation(), this.getAngle(), step);
    this.setLocation(nextLoc);
    this.traveledDistance += step;

    if (this.traveledDistance >= this.maxDistance) {
      if (this.onExpire != null) {
        this.onExpire.accept(this);
      }
      env.remove(this);
      return;
    }

    if (this.collideWithEnvironment && Game.physics().collides(this.getCollisionBox(), Collision.STATIC)) {
      if (this.onHitEnvironment != null) {
        this.onHitEnvironment.accept(this);
      }
      this.triggerSplashDamage(env);
      env.remove(this);
      return;
    }

    Collection<ICombatEntity> targets = env.findCombatEntities(this.getBoundingBox(), e -> e != this && e != this.sourceEntity && !e.isDead() && !this.hitTargets.contains(e));
    for (ICombatEntity target : targets) {
      this.hitTargets.add(target);
      if (this.damage > 0) {
        target.hit(this.damage);
      }
      if (this.onHitEntity != null) {
        this.onHitEntity.accept(target, this);
      }
      if (this.splashRadius > 0) {
        this.triggerSplashDamage(env);
      }
      if (!this.pierce) {
        env.remove(this);
        return;
      }
    }
  }

  private void triggerSplashDamage(Environment env) {
    if (this.splashRadius <= 0 || this.splashDamage <= 0) return;
    Ellipse2D splashArea = new Ellipse2D.Double(
        this.getCenter().getX() - this.splashRadius,
        this.getCenter().getY() - this.splashRadius,
        this.splashRadius * 2,
        this.splashRadius * 2);
    Collection<ICombatEntity> splashTargets = env.findCombatEntities(splashArea, e -> e != this && e != this.sourceEntity && !e.isDead() && !this.hitTargets.contains(e));
    for (ICombatEntity splashTarget : splashTargets) {
      splashTarget.hit(this.splashDamage);
    }
  }
}
