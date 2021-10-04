package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.Image;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class ImageComponentList extends GuiComponent {

  private final Spritesheet background;
  private final List<ImageComponent> cells;
  private List<Image> images;
  private double rowHeight;
  private double columnWidth;
  private final int rows;
  private final int columns;
  private double xOffset;
  private double yOffset;
  private boolean initialized;

  public ImageComponentList(
      final double x,
      final double y,
      final double width,
      final double height,
      final int rows,
      final int columns,
      final List<Image> images,
      final Spritesheet background) {
    super(x, y, width, height);
    if (images != null) {
      this.images = images;
    } else {
      this.images = new CopyOnWriteArrayList<>();
      this.images.add(null);
    }

    this.background = background;
    this.cells = new CopyOnWriteArrayList<>();
    this.rows = rows;
    this.columns = columns;

    if (this.getRows() == 1) {
      this.rowHeight = this.getHeight();
      this.yOffset = 0;
    } else {
      this.rowHeight = this.getHeight() / this.getRows() * 9 / 10;
      this.yOffset = this.getHeight() / (this.getRows() - 1) * 1 / 10;
    }
    if (this.getColumns() == 1) {
      this.columnWidth = this.getWidth();
      this.xOffset = 0;
    } else {
      this.columnWidth = this.getWidth() / this.getColumns() * 9 / 10;
      this.xOffset = this.getWidth() / (this.getColumns() - 1) * 1 / 10;
    }
  }

  public Spritesheet getBackground() {
    return this.background;
  }

  public List<ImageComponent> getCellComponents() {
    return this.cells;
  }

  public int getColumns() {
    return this.columns;
  }

  public List<Image> getImages() {
    return this.images;
  }

  public int getRows() {
    return this.rows;
  }

  @Override
  public void prepare() {

    if (!initialized) {
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
          final ImageComponent cell =
              this.createNewEntry(
                  this.getX() + i * (this.getColumnWidth() + this.xOffset),
                  this.getY() + j * (this.getRowHeight() + this.yOffset),
                  this.getColumnWidth(),
                  this.getRowHeight(),
                  this.getBackground(),
                  "",
                  img);
          this.cells.add(cell);
        }
      }

      this.getComponents().addAll(this.cells);
      this.initialized = true;
    }

    super.prepare();
  }

  public double getRowHeight() {
    return this.rowHeight;
  }

  public void setRowHeight(double rowHeight) {
    this.rowHeight = rowHeight;
  }

  public double getColumnWidth() {
    return this.columnWidth;
  }

  public void setColumnWidth(double columnWidth) {
    this.columnWidth = columnWidth;
  }

  public void setXOffset(final double xOffset) {
    this.xOffset = xOffset;
  }

  public void setYOffset(final double yOffset) {
    this.yOffset = yOffset;
  }

  protected ImageComponent createNewEntry(
      final double x,
      final double y,
      final double width,
      final double height,
      final Spritesheet spritesheet,
      final String text,
      final Image image) {
    return new ImageComponent(x, y, width, height, spritesheet, text, image);
  }
}
