package de.gurkenlabs.litiengine.attributes;

import java.util.EventListener;

/**
 * An interface for listening to changes in attribute modifiers. Classes that implement this interface can be used to handle events when an attribute
 * modifier changes.
 */
@FunctionalInterface
public interface AttributeModifierListener extends EventListener {
  /**
   * This method is called when an attribute modifier changes.
   */
  void modifierChanged();
}
