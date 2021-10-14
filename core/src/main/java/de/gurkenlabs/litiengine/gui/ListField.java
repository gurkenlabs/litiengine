package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.input.Input;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.event.KeyEvent;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.IntConsumer;

/** The Class ListField. */
public class ListField extends GuiComponent {
  private final List<IntConsumer> changeConsumer;
  private final CopyOnWriteArrayList<CopyOnWriteArrayList<ImageComponent>> listEntries;
  private final Object[][] content;
  private final int shownRows;
  private final int shownColumns;
  private final int nbOfColumns;
  private boolean arrowKeyNavigation;
  private Spritesheet buttonSprite;
  private Spritesheet entrySprite;
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
   * Creates a vertical list field. <br>
   * <br>
   * The <b>content</b> of this list field can only be accessed through the first column (column 0).
   * <br>
   * Examples:
   *
   * <blockquote>
   *
   * content[0][0] - ok<br>
   * content[0][1] - ok<br>
   * content[0][8] - ok<br>
   * content[1][5] - NOK<br>
   * content[2][0] - NOK<br>
   *
   * </blockquote>
   *
   * @param x The x-coordinate of the ListField.
   * @param y The y-coordinate of the ListField.
   * @param width The width of the ListField.
   * @param height The height of the ListField.
   * @param content The 1 dimension array to show in the ListField.
   * @param shownRows The number of rows/elements to display before the user needs to scroll for
   *     more possible rows/elements.
   * @see #ListField(double, double, double, double, Object[], int)
   */
  public ListField(
      final double x,
      final double y,
      final double width,
      final double height,
      final Object[] content,
      final int shownRows) {
    this(x, y, width, height, new Object[][] {content}, shownRows, 1);
  }

  /**
   * Creates a 2D vertical list field. <br>
   * <br>
   * The given <b>content</b> should be arranged as columns of elements. <br>
   * Examples:
   *
   * <blockquote>
   *
   * content[0][0] - column 0, row 0<br>
   * content[0][1] - column 0, row 1<br>
   * content[2][8] - column 2, row 8<br>
   *
   * </blockquote>
   *
   * @param x The x-coordinate of the ListField.
   * @param y The y-coordinate of the ListField.
   * @param width The width of the ListField.
   * @param height The height of the ListField.
   * @param content The 2 dimension array to show in the ListField.
   * @param shownRows The number of rows to display before the user needs to scroll for more
   *     possible rows.
   * @param shownColumns The number of columns to display before the user needs to scroll for more
   *     possible columns.
   */
  public ListField(
      final double x,
      final double y,
      final double width,
      final double height,
      final Object[][] content,
      final int shownRows,
      final int shownColumns) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.content = content;
    this.nbOfColumns = this.content.length;
    this.listEntries = new CopyOnWriteArrayList<>();
    this.shownRows = shownRows;
    this.shownColumns = shownColumns;
    initSliders();
    initContentList();
    prepareInput();
  }

  /**
   * Resets the ListField's selection to {@code null}. <br>
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

  public void setButtonSprite(final Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
    initContentList();
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

  public void setEntrySprite(final Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
    initContentList();
  }

  public int getHorizontalLowerBound() {
    return this.horizontalLowerBound;
  }

  public void setHorizontalLowerBound(final int lowerBound) {
    this.horizontalLowerBound = lowerBound;
  }

  public HorizontalSlider getHorizontalSlider() {
    return this.horizontalSlider;
  }

  /**
   * Returns all list items of a specified column.
   *
   * @param column the column
   * @return a list of items
   */
  public List<ImageComponent> getListEntry(final int column) {
    if (column < 0 || column >= this.listEntries.size()) {
      return new ArrayList<>();
    }
    return this.listEntries.get(column);
  }

  /**
   * Returns item at a specified column and row.
   *
   * @param column the column
   * @param row the row
   * @return ImageComponent at [column,row]
   */
  public ImageComponent getListEntry(final int column, final int row) {
    if (column < 0
        || row < 0
        || column >= this.listEntries.size()
        || row >= this.listEntries.get(column).size()) {
      return null;
    }
    return this.listEntries.get(column).get(row);
  }

  /**
   * Returns the number of rows of the tallest column.
   *
   * @return int representing the length of the tallest column
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
    return shownRows;
  }

  public int getNumberOfShownColumns() {
    return shownColumns;
  }

  public ImageComponent getSelectedComponent() {
    return selectedComponent;
  }

  public Object getSelectedObject() {
    if (this.getSelectedComponent() == null) {
      return null;
    }
    return getContent()[selectionColumn][selectionRow];
  }

  /**
   * Returns the selected column.
   *
   * @return number of the column; -1 if isEntireRowSelected() is true
   */
  public int getSelectionColumn() {
    if (isEntireRowSelected()) {
      return -1;
    }
    return selectionColumn;
  }

  /**
   * Returns the selected row.
   *
   * @return number of the row
   */
  public int getSelectionRow() {
    return selectionRow;
  }

  public int getVerticalLowerBound() {
    return verticalLowerBound;
  }

  public void setVerticalLowerBound(final int lowerBound) {
    this.verticalLowerBound = lowerBound;
  }

  public VerticalSlider getVerticalSlider() {
    return verticalSlider;
  }

  public boolean isArrowKeyNavigation() {
    return arrowKeyNavigation;
  }

  public void setArrowKeyNavigation(final boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
  }

  public void onChange(final IntConsumer c) {
    getChangeConsumer().add(c);
  }

  public void refresh() {
    refreshListEntries();
    selectComponent();
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    if (this.selectedComponent != null) {
      Rectangle2D border;
      double borderWidth = selectedComponent.getWidth() + 2;
      double borderHeight = selectedComponent.getHeight() + 2;

      if (isEntireRowSelected()) {
        borderWidth = getWidth() + 2;
      }
      if (getVerticalSlider() != null
          && getVerticalSlider().isVisible()
          && isSliderInside()) {
        borderWidth = borderWidth - getVerticalSlider().getWidth();
      }

      if (isEntireColumnSelected()) {
        borderHeight = getHeight() + 2;
      }

      border =
          new Rectangle2D.Double(
              selectedComponent.getX() - 1,
              selectedComponent.getY() - 1,
              borderWidth,
              borderHeight);

      g.setColor(Color.WHITE);
      ShapeRenderer.renderOutline(g, border, 2);
    }
  }

  public void setForwardMouseEvents(final int column, final boolean forwardMouseEvents) {
    if (column < 0 && column >= nbOfColumns) {
      return;
    }
    for (ImageComponent comp : getListEntry(column)) {
      comp.setForwardMouseEvents(forwardMouseEvents);
    }
  }

  public void setSelection(final int column, final int row) {
    if (column < 0
        || column >= nbOfColumns
        || row < 0
        || row >= getContent()[column].length) {
      return;
    }
    this.selectionColumn = column;
    this.selectionRow = row;

    if (selectionRow >= getVerticalLowerBound() + getNumberOfShownRows()) {
      setVerticalLowerBound(getVerticalLowerBound() + 1);
    } else if (selectionRow < getVerticalLowerBound()
        && getVerticalLowerBound() > 0) {
      setVerticalLowerBound(getVerticalLowerBound() - 1);
    }
    if (selectionColumn >= getHorizontalLowerBound() + getNumberOfShownColumns()) {
      setHorizontalLowerBound(getHorizontalLowerBound() + 1);
    } else if (selectionColumn < getHorizontalLowerBound()
        && getHorizontalLowerBound() > 0) {
      setHorizontalLowerBound(getHorizontalLowerBound() - 1);
    }

    getChangeConsumer().forEach(consumer -> consumer.accept(selectionRow));
    refresh();
  }

  /**
   * If set to true, selecting an element will show a selection of the entire column on which that
   * element is on. Without taking account of its row. <br>
   * <br>
   * Set to <b>false</b> as default.
   *
   * @param selectEntireColumn a boolean
   */
  public void setSelectEntireColumn(boolean selectEntireColumn) {
    this.selectEntireColumn = selectEntireColumn;
  }

  /**
   * If set to true, selecting an element will show a selection of the entire row on which that
   * element is on. Without taking account of its column. <br>
   * <br>
   * Set to <b>false</b> as default.
   *
   * @param selectEntireRow a boolean
   */
  public void setSelectEntireRow(boolean selectEntireRow) {
    this.selectEntireRow = selectEntireRow;
  }

  /**
   * See {@link #setSelectEntireColumn(boolean)}
   *
   * @return true if selection is set to select the entire column; false otherwise
   */
  public boolean isEntireColumnSelected() {
    return selectEntireColumn;
  }

  /**
   * See {@link #setSelectEntireRow(boolean)}
   *
   * @return true if selection is set to select the entire row; false otherwise
   */
  public boolean isEntireRowSelected() {
    return selectEntireRow;
  }

  /**
   * Verify if sliders are set to be inside the ListField.
   *
   * @return true if slider is set to be inside the ListField; false otherwise
   */
  public boolean isSliderInside() {
    return sliderInside;
  }

  /**
   * If set to true, the sliders of this ListField will be displayed within its boundaries. This is
   * necessary, for example, when a ListField covers an entire screen. <br>
   * Set to <b>false</b> as default.
   *
   * @param sliderInside a boolean
   */
  public void setSliderInside(boolean sliderInside) {
    this.sliderInside = sliderInside;
    initSliders();
  }

  /** Slides the ListField up by one row. */
  public void slideUp() {
    if (getVerticalLowerBound() <= 0) {
      return;
    }

    setVerticalLowerBound(getVerticalLowerBound() - 1);
    refresh();
  }

  /** Slides the ListField down by one row. */
  public void slideDown() {
    if (getVerticalLowerBound() >= getMaxRows() - getNumberOfShownRows()) {
      return;
    }

    setVerticalLowerBound(getVerticalLowerBound() + 1);
    refresh();
  }
private boolean canHandleKeyboardEvent(){
   return !isSuspended() && isVisible() && isArrowKeyNavigation();
}
  private void prepareInput() {
    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_UP,
            e -> {
              if (!canHandleKeyboardEvent()) {
                return;
              }
              setSelection(getHorizontalLowerBound(), selectionRow - 1);
            });

    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_DOWN,
            e -> {if (!canHandleKeyboardEvent()) {
                return;
              }
              setSelection(getHorizontalLowerBound(), selectionRow + 1);
            });

    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_LEFT,
            e -> {
              if (!canHandleKeyboardEvent()) {
                return;
              }
              setSelection(getHorizontalLowerBound() - 1, selectionRow);
            });

    Input.keyboard()
        .onKeyTyped(
            KeyEvent.VK_RIGHT,
            e -> {
              if (!canHandleKeyboardEvent()) {
                return;
              }
              setSelection(getHorizontalLowerBound() + 1, selectionRow);
            });

    this.onMouseWheelScrolled(
        e -> {
          if (isSuspended() || !isVisible()) {
            return;
          }
          if (isHovered()) {
            if (e.getEvent().getWheelRotation() < 0) {
              slideUp();
            } else {
              slideDown();
            }
          }
        });
  }

  private void initContentList() {
    final double columnWidth = getWidth() / getNumberOfShownColumns();
    final double rowHeight = getHeight() / getNumberOfShownRows();
    for (int column = 0; column < getNumberOfShownColumns(); column++) {
      this.listEntries.add(new CopyOnWriteArrayList<>());
      for (int row = 0; row < getNumberOfShownRows(); row++) {
        if (getContent()[column].length <= row) {
          continue;
        }

        ImageComponent entryComponent;
        if (getContent()[column][row] == null) {
          entryComponent =
              new ImageComponent(
                  getX() + (columnWidth * column),
                  getY() + (rowHeight * row),
                  columnWidth,
                  rowHeight,
                  entrySprite,
                  "",
                  null);
        } else if (getContent()[column][row] instanceof Image image) {
          entryComponent =
              new ImageComponent(
                  getX() + (columnWidth * column),
                  getY() + (rowHeight * row),
                  columnWidth,
                  rowHeight,
                  entrySprite,
                  "",
                  image);
        } else {
          entryComponent =
              new ImageComponent(
                  getX() + (columnWidth * column),
                  getY() + (rowHeight * row),
                  columnWidth,
                  rowHeight,
                  entrySprite,
                  getContent()[column][row].toString(),
                  null);
        }
        if (isSliderInside() && getVerticalSlider() != null) {
          entryComponent.setX(              getX()                  + ((columnWidth                          - (getVerticalSlider().getWidth() / getNumberOfShownColumns())) * column));
          entryComponent.setWidth(
              entryComponent.getWidth()
                  - (getVerticalSlider().getWidth() / getNumberOfShownColumns()));
        }
        if (isSliderInside() && getHorizontalSlider() != null) {
          entryComponent.setY(
              getY()
                  + ((rowHeight
                          - (getHorizontalSlider().getHeight() / getNumberOfShownRows())) * row));
          entryComponent.setHeight(entryComponent.getHeight() - (getHorizontalSlider().getHeight() / getNumberOfShownRows()));
        }
        entryComponent.setTextAlign(Align.LEFT);
        getListEntry(column).add(entryComponent);
      }
      this.getComponents().addAll(getListEntry(column));
      final int col = column;
      for (final ImageComponent comp : getListEntry(col)) {
        comp.onClicked(
            e -> {
              setSelection(
                  getHorizontalLowerBound() + col % getNumberOfShownColumns(),
                  getVerticalLowerBound()
                      + getListEntry(col).indexOf(comp) % getNumberOfShownRows());
              refresh();
            });
      }
    }

    this.onChange(
        s -> {
          if (getVerticalSlider() != null) {
            getVerticalSlider().setCurrentValue(getVerticalLowerBound());
            getVerticalSlider()
                .getSliderComponent()
                .setLocation(getVerticalSlider().getRelativeSliderPosition());
          }
          if (getHorizontalSlider() != null) {
            getHorizontalSlider().setCurrentValue(getHorizontalLowerBound());
            getHorizontalSlider()
                .getSliderComponent()
                .setLocation(getHorizontalSlider().getRelativeSliderPosition());
          }
        });
    if (getVerticalSlider() != null) {
      getVerticalSlider()
          .onChange(
              sliderValue -> {
                setVerticalLowerBound(sliderValue.intValue());
                getVerticalSlider()
                    .getSliderComponent()
                    .setLocation(getVerticalSlider().getRelativeSliderPosition());
                refresh();
              });
    }
    if (getHorizontalSlider() != null) {
      getHorizontalSlider()
          .onChange(
              sliderValue -> {
                setHorizontalLowerBound(sliderValue.intValue());
                getHorizontalSlider()
                    .getSliderComponent()
                    .setLocation(getHorizontalSlider().getRelativeSliderPosition());
                refresh();
              });
    }
  }

  private void initSliders() {
    final double sliderSize = getHeight() / 5;
    final int maxNbOfRows = getMaxRows() - getNumberOfShownRows();
    if (this.getNumberOfShownColumns() < getContent().length) {
      if (this.isSliderInside()) {
        this.horizontalSlider =
            new HorizontalSlider(
                getX(),
                getY() + getHeight() - sliderSize,
                getWidth() - sliderSize,
                sliderSize,
                0,
                (float) nbOfColumns - getNumberOfShownColumns(),
                1);
      } else {
        this.horizontalSlider =
            new HorizontalSlider(
                getX(),
                getY() + getHeight(),
                getWidth(),
                sliderSize,
                0,
                (float) nbOfColumns - getNumberOfShownColumns(),
                1);
      }
      this.getHorizontalSlider().setCurrentValue(this.getHorizontalLowerBound());
      this.getComponents().add(this.getHorizontalSlider());
    }

    if (maxNbOfRows > 0) {
      if (isSliderInside()) {
        if (getHorizontalSlider() != null) {
          this.verticalSlider =
              new VerticalSlider(
                  getX() + getWidth() - sliderSize,
                  getY(),
                  sliderSize,
                  getHeight() - sliderSize,
                  0,
                  (float) getMaxRows() - getNumberOfShownRows(),
                  1);
        } else {
          this.verticalSlider =
              new VerticalSlider(
                  this.getX() + this.getWidth() - sliderSize,
                  this.getY(),
                  sliderSize,
                  this.getHeight(),
                  0,
                  (float) this.getMaxRows() - this.getNumberOfShownRows(),
                  1);
        }
      } else {
        this.verticalSlider =
            new VerticalSlider(
                this.getX() + this.getWidth(),
                this.getY(),
                sliderSize,
                this.getHeight(),
                0,
                (float) this.getMaxRows() - this.getNumberOfShownRows(),
                1);
      }
      this.getVerticalSlider().setCurrentValue(this.getVerticalLowerBound());
      this.getComponents().add(this.getVerticalSlider());
    }
  }

  private void refreshListEntries() {
    for (int column = 0; column < getNumberOfShownColumns(); column++) {
      for (int row = 0; row < getNumberOfShownRows(); row++) {
        if (getContent()[column].length <= row) {
          continue;
        }

        if (row + getVerticalLowerBound()
                < getContent()[column + getHorizontalLowerBound()].length
            && getContent()[column + getHorizontalLowerBound()][
                    row + getVerticalLowerBound()]
                != null) {
          if (getContent()[column + getHorizontalLowerBound()][
                  row + getVerticalLowerBound()]
              instanceof Image image) {
            getListEntry(column, row).setImage(image);
          } else {
            getListEntry(column, row)
                .setText(
                    getContent()[column + getHorizontalLowerBound()][row + getVerticalLowerBound()]
                        .toString());
          }
        } else {
          getListEntry(column, row).setText("");
        }
      }
    }
  }

  private void selectComponent() {
    if (!isEntireRowSelected()
        && getSelectionColumn() >= getHorizontalLowerBound()
        && getSelectionColumn() < getHorizontalLowerBound() + getNumberOfShownColumns()
        && getSelectionRow() >= getVerticalLowerBound()
        && getSelectionRow() < getVerticalLowerBound() + getNumberOfShownRows()) {
      this.selectedComponent =
          getListEntry(
              getSelectionColumn() - getHorizontalLowerBound(),
              getSelectionRow() - getVerticalLowerBound());
    } else if (isEntireRowSelected()
        && getSelectionColumn() >= 0
        && getSelectionColumn() < nbOfColumns
        && getSelectionRow() >= getVerticalLowerBound()
        && getSelectionRow() < getVerticalLowerBound() + getNumberOfShownRows()) {
      this.selectedComponent = getListEntry(0, getSelectionRow() - getVerticalLowerBound());
    } else {
      this.selectedComponent = null;
    }

    if (this.selectedComponent != null) {
      this.selectedComponent.setSelected(true);
    }
  }
}
