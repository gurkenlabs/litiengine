package de.gurkenlabs.litiengine.graphics.animation;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;

public class Animation implements IUpdateable, ILaunchable {
  public static final int DEFAULT_FRAME_DURATION = 120;
  private static final Logger log = Logger.getLogger(Animation.class.getName());
  private KeyFrame currentFrame;
  private long elapsedTicks;
  private KeyFrame firstFrame;
  private int frameDuration = DEFAULT_FRAME_DURATION;

  private final List<KeyFrame> keyframes;
  private final boolean loop;
  private final String name;
  private boolean paused;
  private boolean playing;
  private Spritesheet spritesheet;

  public Animation(final String spriteSheetName, final boolean loop, final boolean randomizeStart, final int... keyFrameDurations) {
    this(Resources.spritesheets().get(spriteSheetName), loop, randomizeStart, keyFrameDurations);
  }

  public Animation(final Spritesheet spritesheet, final boolean loop, final boolean randomizeStart, final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, randomizeStart, keyFrameDurations);
  }

  public Animation(final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, keyFrameDurations);
  }

  public Animation(final String name, final Spritesheet spritesheet, final boolean loop, final boolean randomizeStart, final int... keyFrameDurations) {
    this(name, spritesheet, loop, keyFrameDurations);

    if (randomizeStart && !this.keyframes.isEmpty()) {
      this.firstFrame = Game.random().chose(this.getKeyframes());
    }
  }

  public Animation(final String name, final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this.name = name;
    this.spritesheet = spritesheet;
    this.loop = loop;
    this.keyframes = new ArrayList<>();

    if (spritesheet == null) {
      log.log(Level.WARNING, "no spritesheet defined for animation {0}", this.getName());
      return;
    }

    this.initKeyFrames(keyFrameDurations);
    if (this.getKeyframes().isEmpty()) {
      log.log(Level.WARNING, "No keyframes defined for animation {0} (spitesheet: {1})", new Object[] { this.getName(), spritesheet.getName() });
    }
  }

  public KeyFrame getCurrentKeyFrame() {
    return this.currentFrame;
  }

  public List<KeyFrame> getKeyframes() {
    return this.keyframes;
  }

  /**
   * Gets to aggregated duration of all {@link KeyFrame}s in this animation.
   * 
   * @return The total duration of a single playback.
   */
  public int getTotalDuration() {
    int duration = 0;
    for (KeyFrame keyFrame : this.getKeyframes()) {
      duration += keyFrame.getDuration();
    }

    return duration;
  }

  public String getName() {
    return this.name;
  }

  public Spritesheet getSpritesheet() {
    // in case the previously sprite sheet was unloaded (removed from the loaded sprite sheets),
    // try to find an updated one by the name of the previously used sprite
    if (this.spritesheet != null && !this.spritesheet.isLoaded()) {
      this.spritesheet = Resources.spritesheets().get(this.spritesheet.getName());
      this.initKeyFrames();
    }

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
   *          The frameduration for all keyframes.
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

    Game.loop().attach(this);
  }

  public void restart() {
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
  public void update() {
    // do nothing if the animation is not playing of the current keyframe is not
    // finished
    if (!this.isPlaying() || Game.time().toMilliseconds(++this.elapsedTicks) < this.currentFrame.getDuration()) {
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

  private void initKeyFrames(final int... keyFrames) {
    if (this.getSpritesheet() == null) {
      return;
    }

    this.keyframes.clear();
    int[] keyFrameDurations = keyFrames;
    if (keyFrameDurations.length == 0) {
      // fallback to use custom keyframe durations if no specific durations are
      // defined
      keyFrameDurations = Resources.spritesheets().getCustomKeyFrameDurations(name);
    }

    // if no keyframes are specified, the animation takes in the whole
    // spritesheet as animation and uses the DEFAULT_FRAME_DURATION for each
    // keyframe
    if (keyFrameDurations.length == 0) {
      for (int i = 0; i < this.getSpritesheet().getTotalNumberOfSprites(); i++) {
        this.keyframes.add(i, new KeyFrame(this.frameDuration, i));
      }
    } else {
      for (int i = 0; i < keyFrameDurations.length; i++) {
        this.keyframes.add(i, new KeyFrame(keyFrameDurations[i], i));
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
