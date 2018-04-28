package de.gurkenlabs.litiengine.sound;

import java.util.EventObject;

/**
 * This implementation is used for all events that need to pass a <code>Sound</code> object to their listeners.
 * 
 * @see Sound
 * @see EventObject
 */
public class SoundEvent extends EventObject {
  private static final long serialVersionUID = -2070316328855430839L;

  private final transient Sound sound;

  public SoundEvent(Object source, Sound sound) {
    super(source);
    this.sound = sound;
  }

  /**
   * Gets the related <code>Sound</code> instance.
   * 
   * @return The sound object.
   */
  public Sound getSound() {
    return this.sound;
  }

  @Override
  public String toString() {
    return super.toString() + "[sound=" + this.sound.getName() + "]";
  }
}
