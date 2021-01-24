package de.gurkenlabs.utiliti.renderers;

import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class Renderers {
  private static final List<IEditorRenderer> editorRenderers;

  static {
    editorRenderers = new CopyOnWriteArrayList<>();
    editorRenderers.add(new GridRenderer());
    editorRenderers.add(new MapObjectsRenderer());
    editorRenderers.add(new SelectionRenderer());
    editorRenderers.add(new FocusRenderer());
    editorRenderers.add(new NewObjectAreaRenderer());
    editorRenderers.add(new MouseSelectAreaRenderer());
  }
  
  private Renderers() {
  }

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
