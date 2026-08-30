package de.gurkenlabs.litiengine.graphics.animation;

import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.ImageEffect;
import de.gurkenlabs.litiengine.graphics.ImageRenderer;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.util.Collection;
import java.util.List;

public interface IAnimationController extends IUpdateable {

  /// Adds the specified animation listener to receive events and callbacks when animation playbacks are started and
  /// finished.
  ///
  /// @param listener
  /// The listener to add.
  public void addListener(AnimationListener listener);

  /// Removes the specified animation listener.
  ///
  /// @param listener
  /// The listener to remove.
  public void removeListener(AnimationListener listener);

  /// Add the specified `Animation` to this controller instance.
  ///
  /// Animations with the same name will be replaced by this method.
  ///
  /// @param animation
  /// The animation to add.
  /// @see #remove(Animation)
  /// @see #hasAnimation(String)
  /// @see #clear()
  public void add(Animation animation);

  /// Removes the specified `Animation` from this controller instance.
  ///
  /// @param animation
  /// The animation to remove.
  /// @see #add(Animation)
  /// @see #hasAnimation(String)
  /// @see #clear()
  public void remove(Animation animation);

  /// Remove all `Animation`s from the `AnimationController`.
  public void clear();

  /// Gets all `Animation` instances managed by this controller.
  ///
  /// @return All `Animation` instances.
  public Collection<Animation> getAll();

  /// Gets the `Animation` instance with the specified name from this controller.
  ///
  /// The name of an `Animation` is case sensitive.
  ///
  /// @param animationName
  /// The name of the animation.
  /// @return The animation with the specified name or null if no such animation is managed by this controller.
  /// @see #getCurrent()
  /// @see #getDefault()
  /// @see #hasAnimation(String)
  public Animation get(String animationName);

  /// Gets the currently active `Animation` of this controller.
  ///
  /// The current active animation provides the current image that is being rendered by consumers of this controller (e.g.
  /// the render engine or any explicit, custom render mechanism).
  ///
  /// @return The currently active animation.
  /// @see #getDefault()
  /// @see #get(String)
  /// @see RenderEngine#renderEntity(java.awt.Graphics2D, de.gurkenlabs.litiengine.entities.IEntity)
  public Animation getCurrent();

  /// Gets the default `Animation` of this controller.
  ///
  /// This animation is played when no other animation is currently active.
  ///
  /// @return The default animation of this controller.
  /// @see #getCurrent()
  /// @see #get(String)
  /// @see #setDefault(Animation)
  public Animation getDefault();

  /// Determines whether this controller has an `Animation` with the specified name.
  ///
  /// The name of an `Animation` is case sensitive.
  ///
  /// @param animationName
  /// The name of the animation.
  /// @return True if this controller contains an `Animation` with the specified name; otherwise false.
  /// @see #add(Animation)
  /// @see #remove(Animation)
  public boolean hasAnimation(String animationName);

  /// Determines whether this controller is currently playing an `Animation` with the specified name.
  ///
  /// The name of an `Animation` is case sensitive.
  ///
  /// @param animationName
  /// The name of the animation.
  /// @return True if this controller is currently playing the `Animation` with the specified name.
  /// @see #getCurrent()
  public boolean isPlaying(String animationName);

  /// Plays the `Animation` with the specified name.
  ///
  /// Does nothing if this controller doesn't contain an `Animation` with the specified name.
  ///
  /// This method also publishes the "played" event to all subscribed `AnimationListener` instances.
  ///
  /// @param animationName
  /// The name of the `Animation` to be played.
  /// @see AnimationListener#played(Animation)
  /// @see #getCurrent()
  public void play(final String animationName);

  /// Sets the specified `Animation` as default for this controller.
  ///
  /// @param animation
  /// The animation to be set as default.
  /// @see #getDefault()
  public void setDefault(Animation animation);

  /// Gets the current sprite (keyframe) of the currently active animation of this controller.
  ///
  /// The implementation of this method applies all registered `ImageEffects`.
  ///
  /// @return The current sprite of the current animation with applied effects; or null, if this controller is currently
  /// disabled.
  /// @see #getCurrent()
  /// @see Animation#getCurrentKeyFrame()
  /// @see #isEnabled()
  public BufferedImage getCurrentImage();

  /// Gets the current sprite scaled by the specified dimensions of the currently active animation of this controller.
  ///
  /// The implementation of this method applies all registered `ImageEffects`.
  ///
  /// @param width
  /// The width of the image.
  /// @param height
  /// The height of the image.
  /// @return The current sprite of the current animation scaled by the defined dimensions with applied effects; or null,
  /// if this controller is currently disabled.
  /// @see #getCurrent()
  /// @see #getCurrentImage()
  /// @see Animation#getCurrentKeyFrame()
  /// @see #isEnabled()
  public BufferedImage getCurrentImage(int width, int height);

  /// Gets the `AffineTransform` instance assigned to this controller that can be used to externally transform the
  /// current image when rendering it with the `ImageRenderer`.
  ///
  /// @return The `AffineTransform` instance assigned to this controller or null.
  /// @see AffineTransform
  /// @see ImageRenderer#renderTransformed(java.awt.Graphics2D, java.awt.Image, AffineTransform)
  /// @see #setAffineTransform(AffineTransform)
  /// @see #getCurrentImage()
  public AffineTransform getAffineTransform();

  /// Sets the `AffineTransform` instance for this controller that can be used to externally transform the current
  /// image when rendering it with the `ImageRenderer`.
  ///
  /// @param affineTransform
  /// The `AffineTransform` instance for this controller.
  /// @see AffineTransform
  /// @see #getAffineTransform()
  public void setAffineTransform(AffineTransform affineTransform);

  /// Adds the specified `ImageEffect` to be applied when the current image is retrieved from this controller.
  ///
  /// @param effect
  /// The image effect to be added.
  public void add(ImageEffect effect);

  /// Removes the specified `ImageEffect` from this controller.
  ///
  /// @param effect
  /// The image effect to be removed.
  public void remove(ImageEffect effect);

  /// Gets all image effects assigned to this controller.
  ///
  /// @return All image effects of this controller.
  /// @see #add(ImageEffect)
  /// @see #remove(ImageEffect)
  public List<ImageEffect> getImageEffects();

  /// Determines whether this controller is currently enabled.
  ///
  /// @return True if this controller is enabled; otherwise false.
  public boolean isEnabled();

  /// Sets a flag that defines whether this controller is enabled or not.
  ///
  /// @param enabled
  /// True if the controller should be enabled; otherwise false.
  public void setEnabled(boolean enabled);
}
