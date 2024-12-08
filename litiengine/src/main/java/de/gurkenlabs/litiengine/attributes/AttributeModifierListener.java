package de.gurkenlabs.litiengine.attributes;

import java.util.EventListener;

@FunctionalInterface
public interface AttributeModifierListener extends EventListener {
  void modifierChanged();
}
