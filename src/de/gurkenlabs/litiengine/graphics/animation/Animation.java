package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.ILaunchable;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.resources.Resources;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * The {@code Animation} class keeps track of the current keyframe which is used to animate a visual
 * element. It iterates over all defined keyframes with respect to their duration and provides
 * information for the related {@code AnimationController} which keyframe should currently be
 * rendered.
 *
 * @see IAnimationController#getCurrent()
 */
public class Animation implements IUpdateable, ILaunchable {
  private final List<KeyFrameListener> listeners;
  /** The default frame duration in milliseconds. */
  public static final int DEFAULT_FRAME_DURATION = 120;

  private static final Logger log = Logger.getLogger(Animation.class.getName());

  private final List<KeyFrame> keyframes;
  private final String name;
  private Spritesheet spritesheet;

  private KeyFrame currentFrame;
  private long lastFrameUpdate;
  private KeyFrame firstFrame;
  private int frameDuration = DEFAULT_FRAME_DURATION;

  private boolean loop;
  private boolean paused;
  private boolean playing;

  /**
   * Initializes a new instance of the {@code Animation} class.
   *
   * @param spriteSheetName The name of the spritesheet used by this animation.
   * @param loop A flag indicating whether the animation should be looped or played only once.
   * @param randomizeStart A flag indicating whether this animation should choose a random keyframe
   *     to start.
   * @param keyFrameDurations The duration of each keyframe.
   */
  public Animation(
      final String spriteSheetName,
      final boolean loop,
      final boolean randomizeStart,
      final int... keyFrameDurations) {
    this(Resources.spritesheets().get(spriteSheetName), loop, randomizeStart, keyFrameDurations);
  }

  /**
   * Initializes a new instance of the {@code Animation} class.
   *
   * @param spritesheet The spritesheet used by this animation.
   * @param loop A flag indicating whether the animation should be looped or played only once.
   * @param randomizeStart A flag indicating whether this animation should choose a random keyframe
   *     to start.
   * @param keyFrameDurations The duration of each keyframe.
   */
  public Animation(
      final Spritesheet spritesheet,
      final boolean loop,
      final boolean randomizeStart,
      final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, randomizeStart, keyFrameDurations);
  }

  /**
   * Initializes a new instance of the {@code Animation} class.
   *
   * @param spritesheet The spritesheet used by this animation.
   * @param loop A flag indicating whether the animation should be looped or played only once.
   * @param keyFrameDurations The duration of each keyframe.
   */
  public Animation(
      final Spritesheet spritesheet, final boolean loop, final int... keyFrameDurations) {
    this(spritesheet.getName(), spritesheet, loop, keyFrameDurations);
  }

  /**
   * Initializes a new instance of the {@code Animation} class.
   *
   * @param name The name of this animation.
   * @param spritesheet The spritesheet used by this animation.
   * @param loop A flag indicating whether the animation should be looped or played only once.
   * @param randomizeStart A flag indicating whether this animation should choose a random keyframe
   *     to start.
   * @param keyFrameDurations The duration of each keyframe.
   */
  public Animation(
      final String name,
      final Spritesheet spritesheet,
      final boolean loop,
      final boolean randomizeStart,
      final int... keyFrameDurations) {
    this(name, spritesheet, loop, keyFrameDurations);

    if (randomizeStart && !this.keyframes.isEmpty()) {
      this.firstFrame = Game.random().choose(this.getKeyframes());
    }
  }

  /**
   * Initializes a new instance of the {@code Animation} class.
   *
   * @param name The name of this animation.
   * @param spritesheet The spritesheet used by this animation.
   * @param loop A flag indicating whether the animation should be looped or played only once.
   * @param keyFrameDurations The duration of each keyframe.
   */
  public Animation(
      final String name,
      final Spritesheet spritesheet,
      final boolean loop,
      final int... keyFrameDurations) {
    this.name = name;
    this.spritesheet = spritesheet;
    this.loop = loop;
    this.keyframes = new ArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();

    if (spritesheet == null) {
      log.log(Level.WARNING, "no spritesheet defined for animation {0}", this.getName());
      return;
    }

    this.initKeyFrames(keyFrameDurations);
    if (this.getKeyframes().isEmpty()) {
      log.log(
          Level.WARNING,
          "No keyframes defined for animation {0} (spitesheet: {1})",
          new Object[] {this.getName(), spritesheet.getName()});
    }
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

  /**
   * Gets the name of this animation.
   *
   * @return The name of this animation.
   */
  public String getName() {
    return this.name;
  }

  public Spritesheet getSpritesheet() {
    // in case the previously sprite sheet was unloaded (removed from the loaded sprite sheets),
    // try to find an updated one by the name of the previously used sprite
    if (this.spritesheet != null && !this.spritesheet.isLoaded()) {
      log.log(
          Level.INFO,
          "Reloading spritesheet {0} for animation {1}",
          new Object[] {this.spritesheet.getName(), this.getName()});
      this.spritesheet = Resources.spritesheets().get(this.spritesheet.getName());
      this.initKeyFrames();
    }

    return this.spritesheet;
  }

  /**
   * Gets a value indicating whether this animation intended to loop.
   *
   * @return True if this animation will loop; otherwise false.
   */
  public boolean isLooping() {
    return this.loop;
  }

  /**
   * Gets a value indicating whether this animation is currently paused.
   *
   * @return True if this animation is currently pause; otherwise false.
   */
  public boolean isPaused() {
    return this.paused;
  }

  /**
   * Gets a value indicating whether this animation is currently playing.
   *
   * @return True if this animation is currently playing; otherwise false.
   */
  public boolean isPlaying() {
    return !this.paused && !this.keyframes.isEmpty() && this.playing;
  }

  /**
   * Pauses the playback of this animation.
   *
   * @see #isPaused()
   * @see #unpause()
   */
  public void pause() {
    this.paused = true;
  }

  /**
   * Un-pauses the playback of this animation.
   *
   * @see #isPaused()
   * @see #pause()
   */
  public void unpause() {
    this.paused = false;
  }

  /**
   * Sets the frame duration for all keyframes in this animation to the specified value.
   *
   * @param frameDuration The frameduration for all keyframes.
   */
  public void setDurationForAllKeyFrames(final int frameDuration) {
    this.frameDuration = frameDuration;

    for (final KeyFrame keyFrame : this.getKeyframes()) {
      keyFrame.setDuration(this.frameDuration);
    }
  }

  /**
   * Sets the specified durations for the keyframes at the index of the defined arguments.
   *
   * <p>e.g.: If this animation defines 5 keyframes, the caller of this method can provide 5
   * individual durations for each keyframe.
   *
   * @param keyFrameDurations The durations to be set on the keyframes of this animation.
   */
  public void setKeyFrameDurations(final int... keyFrameDurations) {
    if (keyFrameDurations.length == 0) {
      return;
    }

    for (int i = 0; i < this.getKeyframes().size(); i++) {
      this.getKeyframes().get(i).setDuration(keyFrameDurations[i]);
    }
  }

  /**
   * Gets an array of this animation's keyframe durations by streaming the keyframe list and mapping
   * the durations to an int array.
   *
   * @return An array of this animation's keyframe durations.
   */
  public int[] getKeyFrameDurations() {
    return this.getKeyframes().stream().mapToInt(KeyFrame::getDuration).toArray();
  }

  /**
   * Sets the looping behavior for this animation.
   *
   * @param loop if true, restart the animation infinitely after its last frame.
   */
  public void setLooping(boolean loop) {
    this.loop = loop;
  }

  public void onKeyFrameChanged(KeyFrameListener listener) {
    this.listeners.add(listener);
  }

  public void removeKeyFrameListener(final KeyFrameListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void start() {
    this.playing = true;
    if (this.getKeyframes().isEmpty()) {
      return;
    }

    this.restart();
  }

  /** Restarts this animation at its first frame. */
  public void restart() {
    this.currentFrame = this.firstFrame;
    this.lastFrameUpdate = Game.loop().getTicks();
  }

  @Override
  public void terminate() {
    this.playing = false;
    if (this.getKeyframes().isEmpty()) {
      return;
    }

    this.currentFrame = this.getKeyframes().get(0);
  }

  @Override
  public void update() {
    // do nothing if the animation is not playing or the current keyframe is not finished
    if (!this.isPlaying()
        || Game.time().since(this.lastFrameUpdate) < this.getCurrentKeyFrame().getDuration()) {
      return;
    }

    // if we are not looping and the last keyframe is finished, we terminate the animation
    if (!this.isLooping() && this.isLastKeyFrame()) {
      this.terminate();
      return;
    }

    // make sure, we stay inside the keyframe list
    final int newFrameIndex =
        (this.getKeyframes().indexOf(this.currentFrame) + 1) % this.getKeyframes().size();
    final KeyFrame previousFrame = this.currentFrame;
    this.currentFrame = this.getKeyframes().get(newFrameIndex);

    for (KeyFrameListener listener : this.listeners) {
      listener.currentFrameChanged(previousFrame, this.currentFrame);
    }

    this.lastFrameUpdate = Game.loop().getTicks();
  }

  KeyFrame getCurrentKeyFrame() {
    return this.currentFrame;
  }

  List<KeyFrame> getKeyframes() {
    return this.keyframes;
  }

  /**
   * Initializes the animation key frames by the specified durations.
   *
   * <p>The amount of keyframes is defined by the available sprites of the assigned {@code
   * Spritesheet}. For each sprite, a new keyframe will be initialized.
   *
   * <p>If no custom durations are specified, the {@link #DEFAULT_FRAME_DURATION} will be used for
   * each frame.
   *
   * @param durations The custom durations for each keyframe.
   */
  private void initKeyFrames(final int... durations) {
    if (this.getSpritesheet() == null) {
      return;
    }

    this.keyframes.clear();
    int[] keyFrameDurations = durations;
    if (keyFrameDurations.length == 0) {
      // fallback to use custom keyframe durations if no specific durations are defined
      keyFrameDurations = Resources.spritesheets().getCustomKeyFrameDurations(name);
    }

    // if no keyframes are specified, the animation takes in the whole
    // spritesheet as animation and uses the DEFAULT_FRAME_DURATION for each keyframe
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
