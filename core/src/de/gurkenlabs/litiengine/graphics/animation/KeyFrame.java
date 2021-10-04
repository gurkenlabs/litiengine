package de.gurkenlabs.litiengine.graphics.animation;

/**
 * The {@code Keyframe} class defines the relation between a particular sprite index and its
 * animation duration.
 */
public class KeyFrame {
  private int duration;
  private int sprite;

  KeyFrame(final int duration, final int sprite) {
    this.duration = duration;
    this.sprite = sprite;
  }

  public int getDuration() {
    return this.duration;
  }

  public int getSpriteIndex() {
    return this.sprite;
  }

  public void setDuration(final int duration) {
    this.duration = duration;
  }

  public void setSprite(final int sprite) {
    this.sprite = sprite;
  }
}
