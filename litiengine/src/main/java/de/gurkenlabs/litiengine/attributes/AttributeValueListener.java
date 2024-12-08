package de.gurkenlabs.litiengine.attributes;

import java.util.EventListener;

@FunctionalInterface
public interface AttributeValueListener extends EventListener {
  void valueChanged();
}
