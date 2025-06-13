package de.gurkenlabs.utiliti.view.renderers;

import java.awt.Graphics2D;

public interface IEditorRenderer {
  String getName();

  void render(Graphics2D g);
}
