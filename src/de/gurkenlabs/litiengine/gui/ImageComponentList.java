package de.gurkenlabs.litiengine.gui;

import java.awt.Graphics2D;
import java.awt.Image;
import java.util.ArrayList;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.sound.Sound;

public class ImageComponentList extends GuiComponent {

  private final Spritesheet background;
  private final ArrayList<ImageComponent> cells;
  private ArrayList<Image> images;
  private final int rows, columns;
  private final double rowHeight, columnWidth, xOffset, yOffset;

  public ImageComponentList(final int x, final int y, final int width, final int height, final int rows, final int columns, final ArrayList<Image> images, final Spritesheet background, final Sound hoverSound) {
    super(x, y, width, height);
    if (images != null) {
      this.images = images;
    } else {
      this.images = new ArrayList<Image>();
      this.images.add(null);
    }

    this.background = background;
    this.cells = new ArrayList<>();
    this.rows = rows;
    this.columns = columns;
    if (this.getRows() == 1) {
      this.rowHeight = this.getHeight();
    } else {
      this.rowHeight = (this.getHeight() * 9 / 10) / (this.getRows());
    }
    if (this.getColumns() == 1) {
      this.columnWidth = this.getWidth();
    } else {
      this.columnWidth = (this.getWidth() * 9 / 10) / (this.getColumns());
    }
    this.xOffset = (this.getWidth() * 1 / 10) / (this.getColumns() - 1);
    this.yOffset = (this.getHeight() * 1 / 10) / (this.getRows() - 1);

    int imageCount = -1;

    for (int j = 0; j < this.getRows(); j++) {
      for (int i = 0; i < this.getColumns(); i++) {
        Image img;
        if (imageCount < this.getImages().size() - 1) {
          imageCount++;
          img = this.getImages().get(imageCount);
        } else {
          img = null;
        }
        final ImageComponent cell = new ImageComponent(this.getX() + i * (this.columnWidth + this.xOffset), this.getY() + j * (this.rowHeight + this.yOffset) , this.columnWidth, this.rowHeight, this.getBackground(), "", img, hoverSound);
        this.cells.add(cell);

      }
    }

    this.getComponents().addAll(0, this.cells);

  }

  public Spritesheet getBackground() {
    return this.background;
  }

  public ArrayList<ImageComponent> getCellComponents() {
    return this.cells;
  }

  public int getColumns() {
    return this.columns;
  }

  public ArrayList<Image> getImages() {
    return this.images;
  }

  public int getRows() {
    return this.rows;
  }

  @Override
  public void render(final Graphics2D g) {
    for (final ImageComponent bg : this.getCellComponents()) {
      bg.render(g);
    }
  }

  @Override
  protected void initializeComponents() {

  }

}
