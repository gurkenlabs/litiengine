package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;

/**
 * The Class ListField.
 */
public class ListField extends GuiComponent {
  private boolean arrowKeyNavigation;
  private Spritesheet buttonSprite;
  private Spritesheet entrySprite;
  private final List<IntConsumer> changeConsumer;
  private final CopyOnWriteArrayList<CopyOnWriteArrayList<ImageComponent>> listEntries;
  private final Object[][] content;
  private final int shownRows;
  private final int shownColumns;

  private int nbOfColumns;

  private int verticalLowerBound = 0;
  private int horizontalLowerBound = 0;

  private ImageComponent selectedComponent;

  private int selectionColumn = -1;
  private int selectionRow = -1;

  private boolean selectEntireColumn = false;
  private boolean selectEntireRow = false;

  private VerticalSlider verticalSlider;
  private HorizontalSlider horizontalSlider;
  private boolean sliderInside = false;

  /**
   * Creates a vertical list field.
   * <br>
   * <br>
   * The <b>content</b> of this list field can only be accessed through the first column (column 0).
   * <br>
   * Examples:
   * <blockquote>
   * content[0][0] - ok<br>
   * content[0][1] - ok<br>
   * content[0][8] - ok<br>
   * content[1][5] - NOK<br>
   * content[2][0] - NOK<br>
   * </blockquote>
   * 
   * @param x
   *          The x-coordinate of the ListField.
   * @param y
   *          The y-coordinate of the ListField.
   * @param width
   *          The width of the ListField.
   * @param height
   *          The height of the ListField.
   * @param content
   *          The 1 dimension array to show in the ListField.
   * @param shownRows
   *          The number of rows/elements to
   *          display before the user needs to scroll for more possible rows/elements.
   * @see #ListField(double, double, double, double, Object[], int, boolean, Spritesheet, Spritesheet)
   */
  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final int shownRows) {
    this(x, y, width, height, new Object[][] { content }, shownRows, 1);
  }

  /**
   * Creates a 2D vertical list field.
   * <br>
   * <br>
   * The given <b>content</b> should be arranged as columns of elements.
   * <br>
   * Examples:
   * <blockquote>
   * content[0][0] - column 0, row 0<br>
   * content[0][1] - column 0, row 1<br>
   * content[2][8] - column 2, row 8<br>
   * </blockquote>
   * 
   * @param x
   *          The x-coordinate of the ListField.
   * @param y
   *          The y-coordinate of the ListField.
   * @param width
   *          The width of the ListField.
   * @param height
   *          The height of the ListField.
   * @param content
   *          The 2 dimension array to show in the ListField.
   * @param shownRows
   *          The number of rows to
   *          display before the user needs to scroll for more possible rows.
   * @param shownColumns
   *          The number of columns to
   *          display before the user needs to scroll for more possible columns.
   */
  public ListField(final double x, final double y, final double width, final double height, final Object[][] content, final int shownRows, final int shownColumns) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.content = content;
    this.nbOfColumns = this.content.length;
    this.listEntries = new CopyOnWriteArrayList<>();
    this.shownRows = shownRows;
    this.shownColumns = shownColumns;
    this.initSliders();
    this.initContentList();
    this.prepareInput();
  }

  /**
   * Resets the ListField's selection to {@code null}.
   * <br>
   * The ListField will then show no selection.
   */
  public void deselect() {
    this.selectionColumn = -1;
    this.selectionRow = -1;
    this.selectedComponent = null;
  }

  public Spritesheet getButtonSprite() {
    return this.buttonSprite;
  }

  public List<IntConsumer> getChangeConsumer() {
    return this.changeConsumer;
  }

  public Object[][] getContent() {
    return this.content;
  }

  public Spritesheet getEntrySprite() {
    return this.entrySprite;
  }

  public int getHorizontalLowerBound() {
    return this.horizontalLowerBound;
  }

  public HorizontalSlider getHorizontalSlider() {
    return this.horizontalSlider;
  }

  /**
   * Returns all list items of a specified column.
   *
   * @param column
   *          the column
   * @return a list of items
   */
  public List<ImageComponent> getListEntry(final int column) {
    if (column < 0 || column >= this.listEntries.size()) {
      return null;
    }
    return this.listEntries.get(column);
  }

  /**
   * Returns item at a specified column and row.
   *
   * @param column
   *          the column
   * @param row
   *          the row
   * @return ImageComponent at [column,row]
   */
  public ImageComponent getListEntry(final int column, final int row) {
    if (column < 0 || row < 0 || column >= this.listEntries.size() || row >= this.listEntries.get(column).size()) {
      return null;
    }
    return this.listEntries.get(column).get(row);
  }

  /**
   * Returns the number of rows of the tallest column.
   * 
   * @return
   *         int representing the length of the tallest column
   */
  public int getMaxRows() {
    int result = 0;
    for (Object[] o : this.getContent()) {
      if (o.length > result) {
        result = o.length;
      }
    }
    return result;
  }

  public int getNumberOfShownRows() {
    return this.shownRows;
  }

  public int getNumberOfShownColumns() {
    return this.shownColumns;
  }

  public ImageComponent getSelectedComponent() {
    return this.selectedComponent;
  }

  public Object getSelectedObject() {
    if (this.getSelectedComponent() == null) {
      return null;
    }
    return this.getContent()[this.selectionColumn][this.selectionRow];
  }

  /**
   * Returns the selected column.
   *
   * @return number of the column; -1 if isEntireRowSelected() is true
   */
  public int getSelectionColumn() {
    if (this.isEntireRowSelected()) {
      return -1;
    }
    return this.selectionColumn;
  }

  /**
   * Returns the selected row.
   *
   * @return number of the row
   */
  public int getSelectionRow() {
    return this.selectionRow;
  }

  public int getVerticalLowerBound() {
    return this.verticalLowerBound;
  }

  public VerticalSlider getVerticalSlider() {
    return this.verticalSlider;
  }

  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  public void onChange(final IntConsumer c) {
    this.getChangeConsumer().add(c);
  }

  public void refresh() {
    for (int column = 0; column < this.getNumberOfShownColumns(); column++) {
      for (int row = 0; row < this.getNumberOfShownRows(); row++) {
        if (this.getContent()[column].length <= row) {
          continue;
        }

        if (row + this.getVerticalLowerBound() < this.getContent()[column + this.getHorizontalLowerBound()].length && this.getContent()[column + this.getHorizontalLowerBound()][row + this.getVerticalLowerBound()] != null) {
          if (this.getContent()[column + this.getHorizontalLowerBound()][row + this.getVerticalLowerBound()] instanceof Image) {
            this.getListEntry(column, row).setImage((Image) this.getContent()[column + this.getHorizontalLowerBound()][row + this.getVerticalLowerBound()]);
          } else {
            this.getListEntry(column, row).setText(this.getContent()[column + this.getHorizontalLowerBound()][row + this.getVerticalLowerBound()].toString());
          }
        } else {
          this.getListEntry(column, row).setText("");
        }
      }
    }

    if (!this.isEntireRowSelected() && this.selectionColumn >= this.getHorizontalLowerBound() && this.selectionColumn < this.getHorizontalLowerBound() + this.getNumberOfShownColumns() && this.selectionRow >= this.getVerticalLowerBound()
        && this.selectionRow < this.getVerticalLowerBound() + this.getNumberOfShownRows()) {
      this.selectedComponent = this.getListEntry(this.selectionColumn - this.getHorizontalLowerBound()).get(this.selectionRow - this.getVerticalLowerBound());
    } else if (this.isEntireRowSelected() && this.selectionColumn >= 0 && this.selectionColumn < this.nbOfColumns && this.selectionRow >= this.getVerticalLowerBound() && this.selectionRow < this.getVerticalLowerBound() + this.getNumberOfShownRows()) {
      this.selectedComponent = this.getListEntry(0).get(this.selectionRow - this.getVerticalLowerBound());
    } else {
      this.selectedComponent = null;
    }

    if (this.selectedComponent != null) {
      this.selectedComponent.setSelected(true);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    if (this.selectedComponent != null) {
      Rectangle2D border;
      double borderWidth = this.selectedComponent.getWidth() + 2;
      double borderHeight = this.selectedComponent.getHeight() + 2;

      if (this.isEntireRowSelected()) {
        borderWidth = this.getWidth() + 2;
      }
      if (this.getVerticalSlider() != null && this.getVerticalSlider().isVisible() && this.isSliderInside()) {
        borderWidth = borderWidth - this.getVerticalSlider().getWidth();
      }

      if (this.isEntireColumnSelected()) {
        borderHeight = this.getHeight() + 2;
      }

      border = new Rectangle2D.Double(this.selectedComponent.getX() - 1, this.selectedComponent.getY() - 1, borderWidth, borderHeight);

      g.setColor(Color.WHITE);
      ShapeRenderer.renderOutline(g, border, 2);
    }
  }

  public void setArrowKeyNavigation(final boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
  }

  public void setButtonSprite(final Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
    this.initContentList();
  }

  public void setEntrySprite(final Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
    this.initContentList();
  }

  public void setForwardMouseEvents(final int column, final boolean forwardMouseEvents) {
    if (column < 0 && column >= this.nbOfColumns) {
      return;
    }
    for (ImageComponent comp : this.getListEntry(column)) {
      comp.setForwardMouseEvents(forwardMouseEvents);
    }
  }

  public void setHorizontalLowerBound(final int lowerBound) {
    this.horizontalLowerBound = lowerBound;
  }

  public void setSelection(final int column, final int row) {
    if (column < 0 || column >= this.nbOfColumns || row < 0 || row >= this.getContent()[column].length) {
      return;
    }
    this.selectionColumn = column;
    this.selectionRow = row;

    if (this.selectionRow >= this.getVerticalLowerBound() + this.getNumberOfShownRows()) {
      this.setVerticalLowerBound(this.getVerticalLowerBound() + 1);
    } else if (this.selectionRow < this.getVerticalLowerBound() && this.getVerticalLowerBound() > 0) {
      this.setVerticalLowerBound(this.getVerticalLowerBound() - 1);
    }
    if (this.selectionColumn >= this.getHorizontalLowerBound() + this.getNumberOfShownColumns()) {
      this.setHorizontalLowerBound(this.getHorizontalLowerBound() + 1);
    } else if (this.selectionColumn < this.getHorizontalLowerBound() && this.getHorizontalLowerBound() > 0) {
      this.setHorizontalLowerBound(this.getHorizontalLowerBound() - 1);
    }

    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.selectionRow));
    this.refresh();
  }

  /**
   * If set to true, selecting a element will show a selection of
   * the entire column on which that element is on. Without taking
   * account of its row.
   * <br>
   * <br>
   * Set to <b>false</b> as default.
   * 
   * @param selectEntireColumn
   *          a boolean
   */
  public void setSelectEntireColumn(boolean selectEntireColumn) {
    this.selectEntireColumn = selectEntireColumn;
  }

  /**
   * If set to true, the sliders of this ListField will be displayed within its boundaries.
   * This is necessary, for example, when a ListField covers an entire screen.
   * <br>
   * Set to <b>false</b> as default.
   * 
   * @param sliderInside
   *          a boolean
   */
  public void setSliderInside(boolean sliderInside) {
    this.sliderInside = sliderInside;
    this.initSliders();
  }

  /**
   * If set to true, selecting a element will show a selection of
   * the entire row on which that element is on. Without taking
   * account of its column.
   * <br>
   * <br>
   * Set to <b>false</b> as default.
   * 
   * @param selectEntireRow
   *          a boolean
   */
  public void setSelectEntireRow(boolean selectEntireRow) {
    this.selectEntireRow = selectEntireRow;
  }

  public void setVerticalLowerBound(final int lowerBound) {
    this.verticalLowerBound = lowerBound;
  }

  /**
   * See {@link #setSelectEntireColumn(boolean)}
   * 
   * @return
   *         true if selection is set to select the entire column; false otherwise
   */
  public boolean isEntireColumnSelected() {
    return this.selectEntireColumn;
  }

  /**
   * See {@link #setSelectEntireRow(boolean)}
   * 
   * @return
   *         true if selection is set to select the entire row; false otherwise
   */
  public boolean isEntireRowSelected() {
    return this.selectEntireRow;
  }

  /**
   * Verify if sliders are set to be inside the ListField.
   * 
   * @return
   *         true if slider is set to be inside the ListField; false otherwise
   */
  public boolean isSliderInside() {
    return this.sliderInside;
  }

  /**
   * Slides the ListField up by one row.
   */
  public void slideUp() {
    if (this.getVerticalLowerBound() <= 0) {
      return;
    }

    this.setVerticalLowerBound(this.getVerticalLowerBound() - 1);
    this.refresh();
  }

  /**
   * Slides the ListField down by one row.
   */
  public void slideDown() {
    if (this.getVerticalLowerBound() >= this.getMaxRows() - this.getNumberOfShownRows()) {
      return;
    }

    this.setVerticalLowerBound(this.getVerticalLowerBound() + 1);
    this.refresh();
  }

  private void prepareInput() {
    Input.keyboard().onKeyTyped(KeyEvent.VK_UP, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getHorizontalLowerBound(), this.selectionRow - 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getHorizontalLowerBound(), this.selectionRow + 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_LEFT, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getHorizontalLowerBound() - 1, this.selectionRow);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_RIGHT, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.getHorizontalLowerBound() + 1, this.selectionRow);
    });

    this.onMouseWheelScrolled(e -> {
      if (this.isSuspended() || !this.isVisible()) {
        return;
      }
      if (this.isHovered()) {
        if (e.getEvent().getWheelRotation() < 0) {
          this.slideUp();
        } else {
          this.slideDown();
        }
        return;
      }
    });
  }

  private void initContentList() {
    final double columnWidth = this.getWidth() / this.getNumberOfShownColumns();
    final double rowHeight = this.getHeight() / this.getNumberOfShownRows();
    for (int column = 0; column < this.getNumberOfShownColumns(); column++) {
      this.listEntries.add(new CopyOnWriteArrayList<ImageComponent>());
      for (int row = 0; row < this.getNumberOfShownRows(); row++) {
        if (this.getContent()[column].length <= row) {
          continue;
        }

        ImageComponent entryComponent;
        if (this.getContent()[column][row] == null) {
          entryComponent = new ImageComponent(this.getX() + (columnWidth * column), this.getY() + (rowHeight * row), columnWidth, rowHeight, this.entrySprite, "", null);
        } else if (this.getContent()[column][row] instanceof Image) {
          entryComponent = new ImageComponent(this.getX() + (columnWidth * column), this.getY() + (rowHeight * row), columnWidth, rowHeight, this.entrySprite, "", (Image) this.getContent()[column][row]);
        } else {
          entryComponent = new ImageComponent(this.getX() + (columnWidth * column), this.getY() + (rowHeight * row), columnWidth, rowHeight, this.entrySprite, this.getContent()[column][row].toString(), null);
        }
        if (this.isSliderInside() && this.getVerticalSlider() != null) {
          entryComponent.setX(this.getX() + ((columnWidth - (this.getVerticalSlider().getWidth() / this.getNumberOfShownColumns())) * column));
          entryComponent.setWidth(entryComponent.getWidth() - (this.getVerticalSlider().getWidth() / this.getNumberOfShownColumns()));
        }
        if (this.isSliderInside() && this.getHorizontalSlider() != null) {
          entryComponent.setY(this.getY() + ((rowHeight - (this.getHorizontalSlider().getHeight() / this.getNumberOfShownRows())) * row));
          entryComponent.setHeight(entryComponent.getHeight() - (this.getHorizontalSlider().getHeight() / this.getNumberOfShownRows()));
        }
        entryComponent.setTextAlign(Align.LEFT);
        this.getListEntry(column).add(entryComponent);
      }
      this.getComponents().addAll(this.getListEntry(column));
      final int col = column;
      for (final ImageComponent comp : this.getListEntry(col)) {
        comp.onClicked(e -> {
          this.setSelection(this.getHorizontalLowerBound() + col % this.getNumberOfShownColumns(), this.getVerticalLowerBound() + this.getListEntry(col).indexOf(comp) % this.getNumberOfShownRows());
          this.refresh();
        });
      }
    }

    this.onChange(s -> {
      if (this.getVerticalSlider() != null) {
        this.getVerticalSlider().setCurrentValue(this.getVerticalLowerBound());
        this.getVerticalSlider().getSliderComponent().setLocation(this.getVerticalSlider().getRelativeSliderPosition());
      }
      if (this.getHorizontalSlider() != null) {
        this.getHorizontalSlider().setCurrentValue(this.getHorizontalLowerBound());
        this.getHorizontalSlider().getSliderComponent().setLocation(this.getHorizontalSlider().getRelativeSliderPosition());
      }
    });
    if (this.getVerticalSlider() != null) {
      this.getVerticalSlider().onChange(sliderValue -> {
        this.setVerticalLowerBound(sliderValue.intValue());
        this.getVerticalSlider().getSliderComponent().setLocation(this.getVerticalSlider().getRelativeSliderPosition());
        this.refresh();
      });
    }
    if (this.getHorizontalSlider() != null) {
      this.getHorizontalSlider().onChange(sliderValue -> {
        this.setHorizontalLowerBound(sliderValue.intValue());
        this.getHorizontalSlider().getSliderComponent().setLocation(this.getHorizontalSlider().getRelativeSliderPosition());
        this.refresh();
      });
    }
  }

  private void initSliders() {
    final double sliderSize = this.getHeight() / 5;
    final int maxNbOfRows = this.getMaxRows() - this.getNumberOfShownRows();
    if (this.getNumberOfShownColumns() < this.getContent().length) {
      if (this.isSliderInside()) {
        this.horizontalSlider = new HorizontalSlider(this.getX(), this.getY() + this.getHeight() - sliderSize, this.getWidth() - sliderSize, sliderSize, 0, this.nbOfColumns - this.getNumberOfShownColumns(), 1);
      } else {
        this.horizontalSlider = new HorizontalSlider(this.getX(), this.getY() + this.getHeight(), this.getWidth(), sliderSize, 0, this.nbOfColumns - this.getNumberOfShownColumns(), 1);
      }
      this.getHorizontalSlider().setCurrentValue(this.getHorizontalLowerBound());
      this.getComponents().add(this.getHorizontalSlider());
    }

    if (maxNbOfRows > 0) {
      if (this.isSliderInside()) {
        if (this.getHorizontalSlider() != null) {
          this.verticalSlider = new VerticalSlider(this.getX() + this.getWidth() - sliderSize, this.getY(), sliderSize, this.getHeight() - sliderSize, 0, this.getMaxRows() - this.getNumberOfShownRows(), 1);
        } else {
          this.verticalSlider = new VerticalSlider(this.getX() + this.getWidth() - sliderSize, this.getY(), sliderSize, this.getHeight(), 0, this.getMaxRows() - this.getNumberOfShownRows(), 1);
        }
      } else {
        this.verticalSlider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), sliderSize, this.getHeight(), 0, this.getMaxRows() - this.getNumberOfShownRows(), 1);
      }
      this.getVerticalSlider().setCurrentValue(this.getVerticalLowerBound());
      this.getComponents().add(this.getVerticalSlider());
    }
  }
}