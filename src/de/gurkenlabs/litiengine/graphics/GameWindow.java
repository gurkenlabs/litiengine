package de.gurkenlabs.litiengine.graphics;

import java.awt.Dimension;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.geom.Point2D;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.gui.screens.Resolution;

public interface GameWindow {

  public Rectangle getBounds();

  public RenderComponent getRenderComponent();

  public Dimension getResolution();

  public Point2D getCenter();

  public float getResolutionScale();

  public Point getScreenLocation();

  public String getTitle();

  public void init();

  public boolean isFocusOwner();

  public void setIconImage(Image image);

  public void setTitle(String string);

  public void setResolution(Resolution res);

  public void onResolutionChanged(Consumer<Dimension> resolutionConsumer);
}
