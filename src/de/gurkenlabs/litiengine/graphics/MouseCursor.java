package de.gurkenlabs.litiengine.graphics;

import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Mouse;

/**
 * The visual representation of the <code>Mouse</code> in the LITIengine.<br>
 * It controls the appearance of the rendered cursor and allows to specify offsets from the actual mouse location.
 * 
 * @see Mouse
 */
public final class MouseCursor implements IRenderable {
  private Image image;
  private AffineTransform transform;
  private int offsetX;
  private int offsetY;

  private boolean visible;
  
  public MouseCursor() {
    this.visible = true;
  }

  @Override
  public void render(Graphics2D g) {
    if (this.isVisible() && (Input.mouse().isGrabMouse() || Game.window().isFocusOwner())) {
      final Point2D locationWithOffset = new Point2D.Double(Input.mouse().getLocation().getX() - this.getOffsetX(), Input.mouse().getLocation().getY() - this.getOffsetY());
      ImageRenderer.renderTransformed(g, this.getImage(), locationWithOffset, this.getTransform());
    }
  }

  public Image getImage() {
    return this.image;
  }

  public AffineTransform getTransform() {
    return this.transform;
  }

  public int getOffsetX() {
    return this.offsetX;
  }

  public int getOffsetY() {
    return this.offsetY;
  }

  /**
   * Determines whether the cursor is currently visible (and will thereby be rendered),
   * by checking the <code>visible</code> flag and whether the specified cursor image is null.
   * 
   * @return True if the cursor is currently visible; otherwise false.
   */
  public boolean isVisible() {
    return this.visible && this.getImage() != null;
  }

  public void set(final Image img) {
    this.image = img;
    if (this.image != null) {
      this.setOffsetX(-(this.image.getWidth(null) / 2));
      this.setOffsetY(-(this.image.getHeight(null) / 2));
    } else {
      this.setOffsetX(0);
      this.setOffsetY(0);
    }
  }

  public void set(final Image img, final int offsetX, final int offsetY) {
    this.set(img);
    this.setOffset(offsetX, offsetY);
  }

  public void setOffset(final int x, final int y) {
    this.setOffsetX(x);
    this.setOffsetY(y);
  }

  public void setOffsetX(final int cursorOffsetX) {
    this.offsetX = cursorOffsetX;
  }

  public void setOffsetY(final int cursorOffsetY) {
    this.offsetY = cursorOffsetY;
  }

  public void setTransform(AffineTransform transform) {
    this.transform = transform;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }
}
