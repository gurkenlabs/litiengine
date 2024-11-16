package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.util.Imaging;
import java.awt.AlphaComposite;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;

/**
 * Represents a GUI component that supports freehand drawing using mouse input.
 */
public class MouseDrawComponent extends ImageComponent {
  /**
   * The size of the brush used for drawing.
   */
  private double brushSize = 2;

  /**
   * The canvas where the drawing is performed.
   */
  private final BufferedImage drawingSpace;

  /**
   * The current color of the brush.
   */
  private Color drawingColor = Color.WHITE;

  /**
   * Creates a new instance of the {@code MouseDrawComponent}.
   *
   * @param x           The x-coordinate of the component's position.
   * @param y           The y-coordinate of the component's position.
   * @param width       The width of the component.
   * @param height      The height of the component.
   * @param spritesheet The spritesheet associated with the component.
   * @param text        The text displayed on the component.
   * @param image       The image used for rendering the component.
   */
  public MouseDrawComponent(
    double x,
    double y,
    double width,
    double height,
    Spritesheet spritesheet,
    String text,
    Image image) {
    super(x, y, width, height, spritesheet, text, image);
    this.drawingSpace = Imaging.getCompatibleImage((int) width, (int) height);
  }

  /**
   * Renders the component and the current drawing on the graphics context.
   *
   * @param g The {@code Graphics2D} context used for rendering.
   */
  @Override
  public void render(Graphics2D g) {
    super.render(g);
    g.drawImage(this.drawingSpace, (int) getX(), (int) getY(), null);
  }

  /**
   * Sets the size of the brush used for drawing.
   *
   * @param newSize The new brush size.
   */
  public void setBrushSize(double newSize) {
    this.brushSize = newSize;
  }

  /**
   * Gets the size of the brush used for drawing.
   *
   * @return The current brush size.
   */
  public double getBrushSize() {
    return brushSize;
  }

  /**
   * Gets the current color of the brush.
   *
   * @return The current drawing color.
   */
  public Color getDrawingColor() {
    return this.drawingColor;
  }

  /**
   * Sets the color of the brush used for drawing.
   *
   * @param color The new drawing color.
   */
  public void setDrawingColor(Color color) {
    this.drawingColor = color;
  }

  /**
   * Handles mouse drag events to draw or erase on the component. Left mouse button draws with the current brush color, while right mouse button
   * clears the area.
   *
   * @param e The {@code MouseEvent} triggered by dragging the mouse.
   */
  @Override
  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);

    double brushX = e.getX();
    double brushY = e.getY();
    Graphics2D g = (Graphics2D) this.drawingSpace.getGraphics();
    int brushXInt = (int) (brushX - getX() - this.brushSize * 1 / 2);
    int brushYInt = (int) (brushY - getY() - this.brushSize * 1 / 2);
    int brushSizeInt = (int) (this.brushSize);
    g.setColor(getDrawingColor());
    if (SwingUtilities.isLeftMouseButton(e)) {
      g.fillRect(brushXInt, brushYInt, brushSizeInt, brushSizeInt);
    } else if (SwingUtilities.isRightMouseButton(e)) {
      g.clearRect(
        brushXInt - brushSizeInt / 2,
        brushYInt - brushSizeInt / 2,
        brushSizeInt * 2,
        brushSizeInt * 2);
    }
  }

  /**
   * Clears the entire drawing space, removing all drawings.
   */
  public void clearDrawingSpace() {
    drawingSpace.getGraphics().clearRect(0, 0, drawingSpace.getWidth(), drawingSpace.getHeight());
  }

  /**
   * Gets the current drawing space.
   *
   * @return The {@code BufferedImage} used for drawing.
   */
  public BufferedImage getDrawingSpace() {
    return this.drawingSpace;
  }
}
