package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import java.awt.Image;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Represents a list of image components in a GUI. This class provides functionality to manage and display a list of image components.
 */
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

  public ImageComponentList(final double x, final double y, final double width, final double height, final int rows, final int columns,
    final List<Image> images, final Spritesheet background) {
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

  /**
   * Gets the background spritesheet of the image component list.
   *
   * @return The background spritesheet.
   */
  public Spritesheet getBackground() {
    return this.background;
  }

  /**
   * Gets the list of image components (cells) in the image component list.
   *
   * @return The list of image components.
   */
  public List<ImageComponent> getCellComponents() {
    return this.cells;
  }

  /**
   * Gets the number of columns in the image component list.
   *
   * @return The number of columns.
   */
  public int getColumns() {
    return this.columns;
  }

  /**
   * Gets the list of images in the image component list.
   *
   * @return The list of images.
   */
  public List<Image> getImages() {
    return this.images;
  }

  /**
   * Gets the number of rows in the image component list.
   *
   * @return The number of rows.
   */
  public int getRows() {
    return this.rows;
  }

  @Override public void prepare() {

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
            this.createNewEntry(this.getX() + i * (this.getColumnWidth() + this.xOffset), this.getY() + j * (this.getRowHeight() + this.yOffset),
              this.getColumnWidth(), this.getRowHeight(), this.getBackground(), "", img);
          this.cells.add(cell);
        }
      }

      this.getComponents().addAll(this.cells);
      this.initialized = true;
    }

    super.prepare();
  }

  /**
   * Gets the height of each row in the image component list.
   *
   * @return The height of each row.
   */
  public double getRowHeight() {
    return this.rowHeight;
  }

  /**
   * Sets the height of each row in the image component list.
   *
   * @param rowHeight The height to set for each row.
   */
  public void setRowHeight(double rowHeight) {
    this.rowHeight = rowHeight;
  }

  /**
   * Gets the width of each column in the image component list.
   *
   * @return The width of each column.
   */
  public double getColumnWidth() {
    return this.columnWidth;
  }

  /**
   * Sets the width of each column in the image component list.
   *
   * @param columnWidth The width to set for each column.
   */
  public void setColumnWidth(double columnWidth) {
    this.columnWidth = columnWidth;
  }

  /**
   * Sets the horizontal offset for the image component list.
   *
   * @param xOffset The horizontal offset to set.
   */
  public void setXOffset(final double xOffset) {
    this.xOffset = xOffset;
  }

  /**
   * Sets the vertical offset for the image component list.
   *
   * @param yOffset The vertical offset to set.
   */
  public void setYOffset(final double yOffset) {
    this.yOffset = yOffset;
  }

  /**
   * Creates a new image component entry.
   *
   * @param x           The x-coordinate of the new entry.
   * @param y           The y-coordinate of the new entry.
   * @param width       The width of the new entry.
   * @param height      The height of the new entry.
   * @param spritesheet The spritesheet to be used for the new entry.
   * @param text        The text to be displayed on the new entry.
   * @param image       The image to be displayed on the new entry.
   * @return The newly created image component.
   */
  protected ImageComponent createNewEntry(final double x, final double y, final double width, final double height, final Spritesheet spritesheet,
    final String text, final Image image) {
    return new ImageComponent(x, y, width, height, spritesheet, text, image);
  }
}
