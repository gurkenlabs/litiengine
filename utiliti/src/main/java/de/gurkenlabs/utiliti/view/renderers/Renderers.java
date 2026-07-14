package de.gurkenlabs.utiliti.view.renderers;

import java.awt.Graphics2D;

public class Renderers {
  private static final IEditorRenderer[] editorRenderers = {
    new GridRenderer(),
    new MapObjectsRenderer(),
    new SelectionRenderer(),
    new FocusRenderer(),
    new NewObjectAreaRenderer(),
    new MouseSelectAreaRenderer()
  };

  private Renderers() {}

  public static void render(Graphics2D g) {
    for (IEditorRenderer renderer : editorRenderers) {
      renderer.render(g);
    }
  }

  public static <T> T get(Class<? extends T> cls) {
    for (IEditorRenderer ent : editorRenderers) {
      if (cls.isInstance(ent)) {
        return cls.cast(ent);
      }
    }

    return null;
  }
}
