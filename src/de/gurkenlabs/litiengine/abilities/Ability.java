package de.gurkenlabs.litiengine.abilities;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.geom.Arc2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Point2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.effects.Effect;
import de.gurkenlabs.litiengine.abilities.effects.EffectArgument;
import de.gurkenlabs.litiengine.annotation.AbilityInfo;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

@AbilityInfo
public abstract class Ability implements IRenderable {
  private final List<Consumer<AbilityExecution>> abilityCastConsumer;
  private final AbilityAttributes attributes;

  private final List<Effect> effects;
  private final Creature executor;

  private String name;
  private String description;
  private boolean multiTarget;
  private CastType castType;
  private AbilityOrigin originType;

  private AbilityExecution currentExecution;
  private Point2D origin;

  /**
   * Instantiates a new ability.
   *
   * @param executor
   *          the executing entity
   */
  protected Ability(final Creature executor) {
    this.abilityCastConsumer = new CopyOnWriteArrayList<>();
    this.effects = new CopyOnWriteArrayList<>();

    final AbilityInfo info = this.getClass().getAnnotation(AbilityInfo.class);
    this.attributes = new AbilityAttributes(info);
    this.executor = executor;
    this.name = info.name();
    this.multiTarget = info.multiTarget();
    this.description = info.description();
    this.castType = info.castType();
    this.originType = info.origin();
  }

  public void addEffect(final Effect effect) {
    this.getEffects().add(effect);
  }

  public Shape calculateImpactArea() {
    return this.internalCalculateImpactArea(this.getExecutor().getAngle());
  }

  public Ellipse2D calculatePotentialImpactArea() {
    final int range = this.getAttributes().impact().get();
    final double arcX = this.getExecutor().getCollisionBox().getCenterX() - range * 0.5;
    final double arcY = this.getExecutor().getCollisionBox().getCenterY() - range * 0.5;

    return new Ellipse2D.Double(arcX, arcY, range, range);
  }

  public boolean canCast() {
    return !this.getExecutor().isDead() && (this.getCurrentExecution() == null || this.getCurrentExecution().getExecutionTicks() == 0 || Game.time().since(this.getCurrentExecution().getExecutionTicks()) >= this.getAttributes().cooldown().get());
  }

  /**
   * Casts the ability by the temporal conditions of the specified game loop and
   * the spatial circumstances of the specified environment. An ability execution
   * will be taken out that start applying all the effects of this ability.
   * 
   * @return An {@link AbilityExecution} object that wraps all information about
   *         this execution of the ability.
   */
  public AbilityExecution cast() {
    if (!this.canCast()) {
      return null;
    }
    this.currentExecution = new AbilityExecution(this);

    for (final Consumer<AbilityExecution> castConsumer : this.abilityCastConsumer) {
      castConsumer.accept(this.currentExecution);
    }

    return this.getCurrentExecution();
  }

  public AbilityAttributes getAttributes() {
    return this.attributes;
  }

  public CastType getCastType() {
    return this.castType;
  }

  public float getCooldownInSeconds() {
    return (float) (this.getAttributes().cooldown().get() * 0.001);
  }

  public AbilityExecution getCurrentExecution() {
    return this.currentExecution;
  }

  public String getDescription() {
    return this.description;
  }

  public Creature getExecutor() {
    return this.executor;
  }

  public String getName() {
    return this.name;
  }

  public AbilityOrigin getOriginType() {
    return this.originType;
  }

  public Point2D getOrigin() {
    switch (this.getOriginType()) {
    case COLLISIONBOX_CENTER:
      return new Point2D.Double(this.executor.getCollisionBox().getCenterX(), this.executor.getCollisionBox().getCenterY());
    case DIMENSION_CENTER:
      return this.executor.getCenter();
    case OFFSET:
      if (this.origin != null) {
        return new Point2D.Double(this.executor.getX() + this.origin.getX(), this.executor.getY() + this.origin.getY());
      }
      break;
    case LOCATION:
    default:
      break;
    }

    return this.executor.getLocation();
  }

  public float getRemainingCooldownInSeconds() {
    if (this.getCurrentExecution() == null || this.getExecutor() == null || this.getExecutor().isDead()) {
      return 0;
    }

    // calculate cooldown in seconds
    return (float) (!this.canCast() ? (this.getAttributes().cooldown().get() - Game.time().since(this.getCurrentExecution().getExecutionTicks())) * 0.001 : 0);
  }

  public boolean isActive() {
    return this.getCurrentExecution() != null && Game.time().since(this.getCurrentExecution().getExecutionTicks()) < this.getAttributes().duration().get();
  }

  public boolean isMultiTarget() {
    return this.multiTarget;
  }

  public void onCast(final Consumer<AbilityExecution> castConsumer) {
    if (!this.abilityCastConsumer.contains(castConsumer)) {
      this.abilityCastConsumer.add(castConsumer);
    }
  }

  public void onEffectApplied(final Consumer<EffectArgument> consumer) {
    for (final Effect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectApplied(effect, consumer);
    }
  }

  public void onEffectCeased(final Consumer<EffectArgument> consumer) {
    for (final Effect effect : this.getEffects()) {
      // registers to all effects and their follow up effects recursively
      this.onEffectCeased(effect, consumer);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.setColor(new Color(255, 255, 0, 100));
    Game.graphics().renderShape(g, this.calculateImpactArea());
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke(2f));
    g.setColor(new Color(255, 255, 0, 200));
    Game.graphics().renderOutline(g, this.calculateImpactArea());
    g.setStroke(oldStroke);
  }

  /**
   * Sets a custom offset from the executors map location as origin of this
   * ability.
   * 
   * @param origin
   *          The origin that defines the execution offset for this
   *          {@link Ability}.
   */
  public void setOrigin(final Point2D origin) {
    this.origin = origin;
  }

  public void setOriginType(AbilityOrigin originType) {
    this.originType = originType;
  }

  public List<Consumer<AbilityExecution>> getAbilityCastConsumer() {
    return this.abilityCastConsumer;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setMultiTarget(boolean multiTarget) {
    this.multiTarget = multiTarget;
  }

  public void setCastType(CastType castType) {
    this.castType = castType;
  }

  protected void setCurrentExecution(AbilityExecution ae) {
    this.currentExecution = ae;
  }

  protected List<Effect> getEffects() {
    return this.effects;
  }

  protected Shape internalCalculateImpactArea(final double angle) {
    final int impact = this.getAttributes().impact().get();
    final int impactAngle = this.getAttributes().impactAngle().get();
    final double arcX = this.getOrigin().getX() - impact * 0.5;
    final double arcY = this.getOrigin().getY() - impact * 0.5;

    // project
    final Point2D appliedRange = GeometricUtilities.project(new Point2D.Double(arcX, arcY), angle, this.getAttributes().range().get() * 0.5);
    final double start = angle - 90;
    if (impactAngle % 360 == 0) {
      return new Ellipse2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact);
    }

    return new Arc2D.Double(appliedRange.getX(), appliedRange.getY(), impact, impact, start, impactAngle, Arc2D.PIE);
  }

  private void onEffectApplied(final Effect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectApplied(consumer);

    for (final Effect followUp : effect.getFollowUpEffects()) {
      this.onEffectApplied(followUp, consumer);
    }
  }

  private void onEffectCeased(final Effect effect, final Consumer<EffectArgument> consumer) {
    effect.onEffectCeased(consumer);

    for (final Effect followUp : effect.getFollowUpEffects()) {
      this.onEffectCeased(followUp, consumer);
    }
  }
}
