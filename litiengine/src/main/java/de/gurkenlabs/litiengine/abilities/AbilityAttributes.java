package de.gurkenlabs.litiengine.abilities;

import de.gurkenlabs.litiengine.attributes.Attribute;

public class AbilityAttributes {
  private final Attribute<Integer> cooldown;
  private final Attribute<Integer> duration;
  private final Attribute<Integer> impact;
  private final Attribute<Integer> impactAngle;
  private final Attribute<Integer> range;
  private final Attribute<Integer> value;

  AbilityAttributes(final AbilityInfo info) {
    this.cooldown = new Attribute<>(info.cooldown());
    this.range = new Attribute<>(info.range());
    this.impact = new Attribute<>(info.impact());
    this.duration = new Attribute<>(info.duration());
    this.value = new Attribute<>(info.value());
    this.impactAngle = new Attribute<>(info.impactAngle());
  }

  public Attribute<Integer> cooldown() {
    return this.cooldown;
  }

  public Attribute<Integer> duration() {
    return this.duration;
  }

  public Attribute<Integer> impact() {
    return this.impact;
  }

  public Attribute<Integer> impactAngle() {
    return this.impactAngle;
  }

  public Attribute<Integer> range() {
    return this.range;
  }

  public Attribute<Integer> value() {
    return this.value;
  }
}
