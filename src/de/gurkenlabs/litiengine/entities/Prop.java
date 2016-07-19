package de.gurkenlabs.litiengine.entities;

import java.awt.Dimension;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;

public class Prop extends CollisionEntity {
  private String spritePath;

  public Prop(Point2D location, String spritesheetName, Dimension dimension) {
    super();
    this.spritePath = spritesheetName;
    this.setSize(dimension.width, dimension.height);
    this.setLocation(location);
    this.setAnimationController(new PropAnimationController(this));
  }

  public String getSpritePath() {
    return this.spritePath;
  }
}
