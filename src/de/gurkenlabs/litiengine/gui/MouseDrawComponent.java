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

public class MouseDrawComponent extends ImageComponent {
  private double brushSize = 2;
  private BufferedImage drawingSpace;
  private Color drawingColor = Color.WHITE;

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

  @Override
  public void render(Graphics2D g) {
    super.render(g);
    g.drawImage(this.drawingSpace, (int) this.getX(), (int) this.getY(), null);
  }

  public void setBrushSize(double newSize) {
    this.brushSize = newSize;
  }

  public Color getDrawingColor() {
    return this.drawingColor;
  }

  public void setDrawingColor(Color color) {
    this.drawingColor = color;
  }

  @Override
  public void mouseDragged(MouseEvent e) {
    super.mouseDragged(e);

    double brushX = e.getX();
    double brushY = e.getY();
    Graphics2D g = (Graphics2D) this.drawingSpace.getGraphics();
    int brushXInt = (int) (brushX - this.getX() - this.brushSize * 1 / 2);
    int brushYInt = (int) (brushY - this.getY() - this.brushSize * 1 / 2);
    int brushSizeInt = (int) (this.brushSize);
    g.setColor(this.getDrawingColor());
    if (SwingUtilities.isLeftMouseButton(e)) {
      g.setComposite(AlphaComposite.SrcOver);
      g.fillRect(brushXInt, brushYInt, brushSizeInt, brushSizeInt);
    } else if (SwingUtilities.isRightMouseButton(e)) {
      g.setComposite(AlphaComposite.Clear);
      g.fillRect(
          brushXInt - brushSizeInt / 2,
          brushYInt - brushSizeInt / 2,
          brushSizeInt * 2,
          brushSizeInt * 2);
    }
  }

  public void clearDrawingSpace() {
    Graphics2D g = (Graphics2D) this.drawingSpace.getGraphics();
    g.setComposite(AlphaComposite.Clear);
    g.fillRect(0, 0, (int) this.getWidth(), (int) this.getHeight());
  }

  public BufferedImage getDrawingSpace() {
    return this.drawingSpace;
  }
}
