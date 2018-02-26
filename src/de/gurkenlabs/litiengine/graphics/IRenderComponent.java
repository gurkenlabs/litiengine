package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.IInitializable;

public interface IRenderComponent extends IInitializable {

  public void fadeIn(int ms);

  public void fadeOut(int ms);

  public Image getCursorImage();

  public void onFpsChanged(Consumer<Integer> fpsConsumer);

  public void onRendered(Consumer<Graphics2D> renderedConsumer);

  public void render(IRenderable screen);

  public void setCursor(Image image);

  public void setCursor(Image image, int offsetX, int offsetY);

  public void setCursorOffset(int x, int y);

  public void setCursorOffsetX(int cursorOffsetX);

  public void setCursorOffsetY(int cursorOffsetY);

  public void takeScreenshot();
}
