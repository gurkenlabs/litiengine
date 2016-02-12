package de.gurkenlabs.litiengine.graphics.animation;

import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class Animation implements IUpdateable, ILaunchable {
  private final int DEFAULT_FRAME_DURATION = 120;
  private final String name;
  private final Spritesheet spritesheet;
  private final List<KeyFrame> keyframes;
  private final boolean loop;

  private int frameDuration = this.DEFAULT_FRAME_DURATION;
  private boolean playing;
  private KeyFrame currentFrame;
  private long elapsedTicks;

  public Animation(final String name, final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this.name = name;
    this.spritesheet = spritesheet;
    this.loop = loop;
    this.keyframes = new ArrayList<KeyFrame>();
    this.initKeyFrames(keyFrameDurations);
    if (this.getKeyframes().size() == 0) {
      System.out.println("No keyframes defined for animation " + this.getName());
    }

    Game.getLoop().registerForUpdate(this);
  }

  public KeyFrame getCurrentKeyFrame() {
    return this.currentFrame;
  }

  public int getFrameDuration() {
    return this.frameDuration;
  }

  public List<KeyFrame> getKeyframes() {
    return this.keyframes;
  }

  public String getName() {
    return this.name;
  }

  public Spritesheet getSpritesheet() {
    return this.spritesheet;
  }

  public boolean isLoop() {
    return this.loop;
  }

  public boolean isPlaying() {
    return this.keyframes.size() > 0 && this.playing;
  }

  public void setFrameDuration(final int frameDuration) {
    this.frameDuration = frameDuration;

    // TODO: update frame durations
  }

  @Override
  public void start() {
    this.elapsedTicks = 0;
    this.playing = true;
    if (this.getKeyframes().size() == 0) {
      return;
    }

    this.currentFrame = this.getKeyframes().get(0);
  }

  @Override
  public void terminate() {
    this.elapsedTicks = 0;
    this.playing = false;
    if (this.getKeyframes().size() == 0) {
      return;
    }

    this.currentFrame = this.getKeyframes().get(0);
  }

  @Override
  public void update(final IGameLoop loop) {
    // do nothing if the animation is not playing of the current keyframe is not
    // finished
    if (!this.isPlaying() || loop.convertToMs(++this.elapsedTicks) < this.currentFrame.getDuration()) {
      return;
    }

    // if we are not looping and the last keyframe is finished, we terminate the
    // animation
    if (!this.isLoop() && this.isLastKeyFrame()) {
      this.terminate();
      return;
    }

    // make sure, we stay inside the keyframe list
    final int newFrameIndex = (this.getKeyframes().indexOf(this.currentFrame) + 1) % this.getKeyframes().size();
    this.currentFrame = this.getKeyframes().get(newFrameIndex);
    this.elapsedTicks = 0;
  }

  private void initKeyFrames(final int[] keyFrames) {

    // if no keyframes are specified, the animation takes in the whole
    // spritesheet as animation and uses the DEFAULT_FRAME_DURATION for each
    // keyframe
    if (keyFrames.length == 0) {
      for (int i = 0; i < this.getSpritesheet().getTotalNumberOfSprites(); i++) {
        this.keyframes.add(i, new KeyFrame(this.getFrameDuration(), i));
      }

      return;
    }

    for (int i = 0; i < keyFrames.length; i++) {
      this.keyframes.add(i, new KeyFrame(keyFrames[i], i));
    }
  }

  private boolean isLastKeyFrame() {
    return this.getKeyframes().indexOf(this.currentFrame) == this.getKeyframes().size() - 1;
  }
}
