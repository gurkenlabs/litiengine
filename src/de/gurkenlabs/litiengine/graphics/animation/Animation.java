package de.gurkenlabs.litiengine.graphics.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import de.gurkenlabs.core.ILaunchable;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.Spritesheet;

public class Animation implements IUpdateable, ILaunchable {
  private static final int DEFAULT_FRAME_DURATION = 120;
  private KeyFrame currentFrame;
  private long elapsedTicks;
  private KeyFrame firstFrame;
  private int frameDuration = DEFAULT_FRAME_DURATION;

  private final List<KeyFrame> keyframes;
  private final boolean loop;
  private final String name;
  private boolean paused;
  private boolean playing;
  private final Spritesheet spritesheet;

  public Animation(final Spritesheet spritesheet, final boolean loop, final boolean randomizeStart, final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, randomizeStart, keyFrameDurations);
  }

  public Animation(final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, keyFrameDurations);
  }

  public Animation(final String name, final Spritesheet spritesheet, final boolean loop, final boolean randomizeStart, final int... keyFrameDurations) {
    this(name, spritesheet, loop, keyFrameDurations);

    if (randomizeStart && !this.keyframes.isEmpty()) {
      this.firstFrame = this.getKeyframes().get(new Random().nextInt(this.getKeyframes().size()));
    }
  }

  public Animation(final String name, final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this.name = name;
    this.spritesheet = spritesheet;
    this.loop = loop;
    this.keyframes = new ArrayList<>();

    if (spritesheet == null) {
      System.out.println("no spritesheet defined for animation " + name);
      return;
    }

    this.initKeyFrames(keyFrameDurations);
    if (this.getKeyframes().isEmpty()) {
      System.out.println("No keyframes defined for animation " + this.getName() + " (spitesheet: " + spritesheet.getName() + ")");
    }

    Game.getLoop().attach(this);
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

  public boolean isPaused() {
    return this.paused;
  }

  public boolean isPlaying() {
    return !this.paused && !this.keyframes.isEmpty() && this.playing;
  }

  public void pause() {
    this.paused = true;
  }

  /**
   * Sets the frame duration for all keyframes in this animation to the
   * specified value.
   *
   * @param frameDuration
   */
  public void setFrameDuration(final int frameDuration) {
    this.frameDuration = frameDuration;

    for (final KeyFrame keyFrame : this.getKeyframes()) {
      keyFrame.setDuration(this.frameDuration);
    }
  }

  public void setkeyFrameDurations(final int... keyFrameDurations) {
    if (keyFrameDurations.length == 0) {
      return;
    }

    for (int i = 0; i < this.getKeyframes().size(); i++) {
      this.getKeyframes().get(i).setDuration(keyFrameDurations[i]);
    }
  }

  @Override
  public void start() {
    this.elapsedTicks = 0;
    this.playing = true;
    if (this.getKeyframes().isEmpty()) {
      return;
    }

    this.currentFrame = this.firstFrame;
  }

  @Override
  public void terminate() {
    this.elapsedTicks = 0;
    this.playing = false;
    if (this.getKeyframes().isEmpty()) {
      return;
    }

    this.currentFrame = this.getKeyframes().get(0);
  }

  public void unpause() {
    this.paused = false;
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

  private void initKeyFrames(int[] keyFrames) {

    if (this.getSpritesheet() == null) {
      return;
    }

    if (keyFrames.length == 0) {
      // fallback to use custom keyframe durations if no specific durations are
      // defined
      keyFrames = Spritesheet.getCustomKeyFrameDurations(name);
    }

    // if no keyframes are specified, the animation takes in the whole
    // spritesheet as animation and uses the DEFAULT_FRAME_DURATION for each
    // keyframe
    if (keyFrames.length == 0) {
      for (int i = 0; i < this.getSpritesheet().getTotalNumberOfSprites(); i++) {
        this.keyframes.add(i, new KeyFrame(this.getFrameDuration(), i));
      }
    } else {
      for (int i = 0; i < keyFrames.length; i++) {
        this.keyframes.add(i, new KeyFrame(keyFrames[i], i));
      }
    }

    if (!this.keyframes.isEmpty()) {
      this.firstFrame = this.getKeyframes().get(0);
    }
  }

  private boolean isLastKeyFrame() {
    return this.getKeyframes().indexOf(this.currentFrame) == this.getKeyframes().size() - 1;
  }
}
