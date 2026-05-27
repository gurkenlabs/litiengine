package de.gurkenlabs.litiengine.graphics;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.input.Mouse;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;

/**
 * The visual representation of the {@code Mouse} in the LITIENGINE.<br>
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
    BLANK_CURSOR =
        Toolkit.getDefaultToolkit().createCustomCursor(cursorImg, new Point(0, 0), "blank cursor");

    final BufferedImage debugCursorImg = Imaging.getCompatibleImage(16, 16);
    Graphics2D g = debugCursorImg.createGraphics();
    g.setColor(Color.RED);
    g.drawLine(0, 0, 16, 16);
    g.drawLine(0, 0, 16, 0);
    g.drawLine(0, 0, 0, 16);
    g.dispose();

    DEBUG_CURSOR_IMAGE = debugCursorImg;
  }

  /** Initializes a new instance of the {@code MouseCursor} class. */
  public MouseCursor() {
    this.visible = true;
  }

  @Override
  public void render(Graphics2D g) {
    if (this.isVisible()) {
      final Point2D locationWithOffset =
          new Point2D.Double(
              Input.mouse().getLocation().getX() + this.getOffsetX(),
              Input.mouse().getLocation().getY() + this.getOffsetY());
      ImageRenderer.renderTransformed(g, this.getImage(), locationWithOffset, this.getTransform());
    }

    if (Game.config().debug().isRenderDebugMouse()) {
      ImageRenderer.render(g, DEBUG_CURSOR_IMAGE, Input.mouse().getLocation());
    }
  }

  /**
   * Gets the image currently used to render the cursor.
   *
   * @return the cursor image, or {@code null} if none is set
   */
  public Image getImage() {
    return this.image;
  }

  /**
   * Gets the affine transform applied to the cursor image when it is rendered.
   *
   * @return the cursor transform
   */
  public AffineTransform getTransform() {
    return this.transform;
  }

  /**
   * Gets the horizontal pixel offset from the actual mouse location to the rendered cursor.
   *
   * @return the horizontal offset
   */
  public int getOffsetX() {
    return this.offsetX;
  }

  /**
   * Gets the vertical pixel offset from the actual mouse location to the rendered cursor.
   *
   * @return the vertical offset
   */
  public int getOffsetY() {
    return this.offsetY;
  }

  /**
   * Determines whether the cursor is currently visible (and will thereby be rendered), by checking the {@code visible}
   * flag and whether the specified cursor image is null.
   *
   * @return True if the cursor is currently visible; otherwise false.
   */
  public boolean isVisible() {
    return this.visible && this.getImage() != null;
  }

  /**
   * Sets the cursor image, anchored at its top-left corner.
   *
   * @param img the cursor image
   */
  public void set(final Image img) {
    this.set(img, Align.LEFT, Valign.TOP);
  }

  /**
   * Sets the cursor image with the supplied pixel offsets. If a non-{@code null} image is set, the native system cursor is hidden; otherwise it is
   * restored (unless the mouse is grabbed).
   *
   * @param img     the cursor image; may be {@code null} to clear the cursor
   * @param offsetX the horizontal offset
   * @param offsetY the vertical offset
   */
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

  /**
   * Sets the cursor image, computing the pixel offset so that the supplied alignment is anchored to the actual mouse location.
   *
   * @param img    the cursor image
   * @param hAlign the horizontal alignment of the anchor
   * @param vAlign the vertical alignment of the anchor
   */
  public void set(final Image img, Align hAlign, Valign vAlign) {
    this.set(img, -hAlign.getValue(img.getWidth(null)), -vAlign.getValue(img.getHeight(null)));
  }

  /**
   * Sets the cursor offsets.
   *
   * @param x the horizontal offset
   * @param y the vertical offset
   */
  public void setOffset(final int x, final int y) {
    this.setOffsetX(x);
    this.setOffsetY(y);
  }

  /**
   * Sets the horizontal cursor offset.
   *
   * @param cursorOffsetX the horizontal offset
   */
  public void setOffsetX(final int cursorOffsetX) {
    this.offsetX = cursorOffsetX;
  }

  /**
   * Sets the vertical cursor offset.
   *
   * @param cursorOffsetY the vertical offset
   */
  public void setOffsetY(final int cursorOffsetY) {
    this.offsetY = cursorOffsetY;
  }

  /**
   * Sets the affine transform applied to the cursor image when it is rendered.
   *
   * @param transform the transform
   */
  public void setTransform(AffineTransform transform) {
    this.transform = transform;
  }

  /**
   * Sets whether the cursor is visible.
   *
   * @param visible {@code true} to show the cursor
   */
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  /**
   * Restores the native system cursor on the game window.
   */
  public void showDefaultCursor() {
    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setCursor(DEFAULT_CURSOR);
    }
  }

  /**
   * Hides the native system cursor by replacing it with an empty cursor on the game window.
   */
  public void hideDefaultCursor() {
    if (Game.window() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setCursor(BLANK_CURSOR);
    }
  }
}
