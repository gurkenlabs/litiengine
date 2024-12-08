package de.gurkenlabs.litiengine.attributes;

import java.util.EventListener;

/**
 * A listener interface for receiving attribute value change events.
 * The class that is interested in processing an attribute value change event
 * implements this interface, and the object created with that class is registered
 * with a component using the component's `addAttributeValueListener` method.
 * When the attribute value change event occurs, that object's `valueChanged` method
 * is invoked.
 */
@FunctionalInterface
public interface AttributeValueListener extends EventListener {
  /**
   * Invoked when the value of an attribute has changed.
   */
  void valueChanged();
}
