package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.function.Consumer;

import de.gurkenlabs.core.IInitializable;
import de.gurkenlabs.litiengine.gui.screens.IScreen;

public interface IRenderComponent extends IInitializable{

  public void onFpsChanged(Consumer<Integer> fpsConsumer);

  public void onRendered(Consumer<Graphics2D> renderedConsumer);

  public void setCursor(Image image);

  public void setCursorOffsetX(int cursorOffsetX);

  public void setCursorOffsetY(int cursorOffsetY);
  
  public void render(IRenderable screen);
  
  public void takeScreenshot();
}
