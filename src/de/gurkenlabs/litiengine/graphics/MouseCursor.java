package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Mouse;
import de.gurkenlabs.litiengine.util.Imaging;

/**
 * The visual representation of the <code>Mouse</code> in the LITIengine.<br>
 * It controls the appearance of the rendered cursor and allows to specify offsets from the actual mouse location.
 * 
 * @see Mouse
 */
public final class MouseCursor implements IRenderable {

  private static final Cursor DEFAULT_CURSOR = new Cursor(Cursor.DEFAULT_CURSOR);
  private static final Cursor BLANK_CURSOR;
  private static final Image DEBUG_CURSOR_IMAGE;

  private Image image;
  private AffineTransform transform;
  private int offsetX;
  private int offsetY;

  private boolean visible;
  static {
    final BufferedImage cursorImg = Imaging.getCompatibleImage(16, 16);
    BLANK_CURSOR = Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

    final BufferedImage debugCursorImg = Imaging.getCompatibleImage(16, 16);
    Graphics2D g = debugCursorImg.createGraphics();
    g.setColor(Color.RED);
    g.drawLine(0, 0, 16, 16);
    g.drawLine(0, 0, 16, 0);
    g.drawLine(0, 0, 0, 16);
    g.dispose();

    DEBUG_CURSOR_IMAGE = debugCursorImg;
  }

  /**
   * Instantiates a new instance of the <code>MouseCursor</code> class.
   */
  public MouseCursor() {
    this.visible = true;
  }

  @Override
  public void render(Graphics2D g) {
    if (this.isVisible()) {
      final Point2D locationWithOffset = new Point2D.Double(Input.mouse().getLocation().getX() + this.getOffsetX(), Input.mouse().getLocation().getY() + this.getOffsetY());
      ImageRenderer.renderTransformed(g, this.getImage(), locationWithOffset, this.getTransform());
    }

    if (Game.config().debug().isRenderDebugMouse()) {
      ImageRenderer.render(g, DEBUG_CURSOR_IMAGE, Input.mouse().getLocation());
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
    this.set(img, Align.LEFT, Valign.TOP);
  }

  public void set(final Image img, final int offsetX, final int offsetY) {
    this.image = img;
    this.setOffset(offsetX, offsetY);

    if (this.getImage() != null) {
      hideDefaultCursor();
      return;
    }

    if (!Input.mouse().isGrabMouse()) {
      showDefaultCursor();
    }
  }

  public void set(final Image img, Align hAlign, Valign vAlign) {
    this.set(img, -hAlign.getValue(img.getWidth(null)), -vAlign.getValue(img.getHeight(null)));
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

  public void showDefaultCursor() {
    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setCursor(DEFAULT_CURSOR);
    }
  }

  public void hideDefaultCursor() {
    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setCursor(BLANK_CURSOR);
    }
  }
}
