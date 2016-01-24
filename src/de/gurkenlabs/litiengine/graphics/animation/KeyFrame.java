package de.gurkenlabs.litiengine.graphics.animation;

public class KeyFrame {
  private int duration;
  private int sprite;

  public KeyFrame(final int duration, final int sprite) {
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
