package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
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

  private final Object[][] content;
  private int nbOfColumns;

  private final CopyOnWriteArrayList<CopyOnWriteArrayList<ImageComponent>> listEntries;

  private int lowerBound = 0;

  private ImageComponent selectedComponent;

  private int selectionColumn;
  private int selectionRow;

  private boolean selectEntireRow = false;

  private final int shownElements;

  private VerticalSlider slider;
  private boolean sliderInside = false;

  /**
   * Creates a vertical list field.
   * <br><br>
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
   * the x
   * @param y
   * the y
   * @param width
   * the width
   * @param height
   * the height
   * @param content
   * the 1D content
   * @param shownElements
   * the number of rows/elements to 
   * display before the user needs to scroll for more possible rows/elements
   * @param entrySprite
   * the entrySprite
   * @param buttonSprite
   * the buttonSprite
   */
  public ListField(final double x, final double y, final double width, final double height, final Object[] content, final int shownElements, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
    this(x, y, width, height, new Object[][] {content}, shownElements, entrySprite, buttonSprite);
  }

  /**
   * Creates a 2D vertical list field.
   * <br><br>
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
   * the x
   * @param y
   * the y
   * @param width
   * the width
   * @param height
   * the height
   * @param content
   * the 2D content
   * @param shownElements
   * the number of rows/elements to 
   * display before the user needs to scroll for more possible rows/elements
   * @param entrySprite
   * the entrySprite
   * @param buttonSprite
   * the buttonSprite
   */
  public ListField(final double x, final double y, final double width, final double height, final Object[][] content, final int shownElements, final Spritesheet entrySprite, final Spritesheet buttonSprite) {
    super(x, y, width, height);
    this.changeConsumer = new CopyOnWriteArrayList<>();
    this.content = content;
    this.nbOfColumns = this.content.length;
    this.listEntries = new CopyOnWriteArrayList<>();
    this.buttonSprite = buttonSprite;
    this.entrySprite = entrySprite;
    this.shownElements = shownElements;
    this.initContentList();
    this.prepareInput();
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

  /**
   * Returns all list items of a specified column.
   *
   * @param column
   * the column
   * @return a list of items
   */
  public List<ImageComponent> getListEntry(final int column) {
    if (column < 0 || column >= this.listEntries.size()) {
      return null;
    }
    return this.listEntries.get(column);
  }

  public int getLowerBound() {
    return this.lowerBound;
  }

  /**
   * Returns the number of rows of the tallest column.
   * 
   * @return
   * int representing the length of the tallest column
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

  public int getNumberOfShownElements() {
    return this.shownElements;
  }

  public ImageComponent getSelectedComponent() {
    return this.selectedComponent;
  }

  public Object getSelectedObject() {
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

  public VerticalSlider getSlider() {
    return this.slider;
  }

  public boolean isArrowKeyNavigation() {
    return this.arrowKeyNavigation;
  }

  public void onChange(final IntConsumer c) {
    this.getChangeConsumer().add(c);
  }

  public void refresh() {
    for (int column = 0; column < this.nbOfColumns; column++) {
      for (int row = 0; row < this.getNumberOfShownElements(); row++) {
        if (this.getContent()[column].length <= row) {
          continue;
        }

        if (this.getListEntry(column).get(row) != null) {
          this.getListEntry(column).get(row).setText(this.getContent()[column][row+ this.getLowerBound()].toString());
        }
      }
    }
    if (this.selectionColumn >= 0 && this.selectionColumn < this.nbOfColumns && this.selectionRow >= this.getLowerBound() && this.selectionRow < this.getLowerBound() + this.getNumberOfShownElements()) {
      this.selectedComponent = this.getListEntry(this.selectionColumn).get(this.selectionRow - this.getLowerBound());
      if (this.selectedComponent != null) {
        this.selectedComponent.setSelected(true);
      }
    } else {
      this.selectedComponent = null;
    }
  }

  @Override
  public void render(final Graphics2D g) {
    super.render(g);
    if (this.selectedComponent != null) {
      Rectangle2D border;
      if (this.isEntireRowSelected()) {
        if (this.isSliderInside()) {
          border = new Rectangle2D.Double(this.getX() - 1, this.selectedComponent.getY() - 1, this.getWidth() + 2 - this.slider.getWidth(), this.selectedComponent.getHeight() + 2);
        }
        else {
          border = new Rectangle2D.Double(this.getX() - 1, this.selectedComponent.getY() - 1, this.getWidth() + 2, this.selectedComponent.getHeight() + 2);
        }
      }
      else {
        if (this.isSliderInside()) {
          border = new Rectangle2D.Double(this.selectedComponent.getX() - 1, this.selectedComponent.getY() - 1, this.selectedComponent.getWidth() + 2 - this.slider.getWidth(), this.selectedComponent.getHeight() + 2);
        }
        else {
          border = new Rectangle2D.Double(this.selectedComponent.getX() - 1, this.selectedComponent.getY() - 1, this.selectedComponent.getWidth() + 2, this.selectedComponent.getHeight() + 2);
        }
      }
      g.setColor(Color.WHITE);
      ShapeRenderer.renderOutline(g, border, 2);
    }
  }

  public void setArrowKeyNavigation(final boolean arrowKeyNavigation) {
    this.arrowKeyNavigation = arrowKeyNavigation;
  }

  public void setButtonSprite(final Spritesheet buttonSprite) {
    this.buttonSprite = buttonSprite;
  }

  public void setEntrySprite(final Spritesheet entrySprite) {
    this.entrySprite = entrySprite;
  }

  public void setForwardMouseEvents(final int column, final boolean forwardMouseEvents) {
    if (column < 0 && column >= this.nbOfColumns) {
      return;
    }
    for (ImageComponent comp : this.getListEntry(column)) {
      comp.setForwardMouseEvents(forwardMouseEvents);
    }
  }

  public void setLowerBound(final int lowerBound) {
    this.lowerBound = lowerBound;
  }

  public void setSelection(final int column, final int row) {
    if (column < 0 || column >= this.nbOfColumns || row < 0 || row >= this.getContent()[column].length) {
      return;
    }
    this.selectionColumn = column;
    this.selectionRow = row;

    if (this.selectionRow >= this.getLowerBound() + this.getNumberOfShownElements()) {
      this.setLowerBound(this.getLowerBound() + 1);
    } else if (this.selectionRow < this.getLowerBound() && this.getLowerBound() > 0) {
      this.setLowerBound(this.getLowerBound() - 1);
    }
    this.getChangeConsumer().forEach(consumer -> consumer.accept(this.selectionRow));
    this.refresh();
  }

  /**
   * If set to true, selecting a element will show a selection of 
   * the entire row on which that element is on. Without taking 
   * account of its column.
   * <br><br>
   * Set to <b>false</b> as default.
   * 
   * @param selectEntireRow
   * a boolean
   */
  public void setSelectEntireRow(boolean selectEntireRow) {
    this.selectEntireRow = selectEntireRow;
  }

  /**
   * If set to true, the slider will show inside the ListField.
   * <br>
   * This can be used, for example, if the ListField's width matches the screen's width.
   * <br><br>
   * Set to <b>false</b> as default.
   * 
   * @param sliderInside
   * a boolean
   */
  public void setSliderInside(boolean sliderInside) {
    this.sliderInside = sliderInside;
    this.listEntries.clear();
    this.getComponents().clear();
    this.initContentList();
  }

  private void initContentList() {
    final int maxNbOfRows = this.getMaxRows() - this.getNumberOfShownElements();
    if (maxNbOfRows > 0) {
      if (this.isSliderInside()) {
        this.slider = new VerticalSlider(this.getX() + this.getWidth() - (this.getHeight() / this.getNumberOfShownElements()), this.getY(), this.getHeight() / this.getNumberOfShownElements(), this.getHeight(), 0, this.getMaxRows() - this.getNumberOfShownElements(), 1);
      }
      else {
        this.slider = new VerticalSlider(this.getX() + this.getWidth(), this.getY(), this.getHeight() / this.getNumberOfShownElements(), this.getHeight(), 0, this.getMaxRows() - this.getNumberOfShownElements(), 1);
      }
      this.getSlider().setCurrentValue(this.getLowerBound());
      this.getComponents().add(this.getSlider());
    }

    final double columnWidth = this.getWidth() / this.nbOfColumns;
    for (int column = 0; column < this.nbOfColumns; column++) {
      this.listEntries.add(new CopyOnWriteArrayList<ImageComponent>());
      for (int row = 0; row < this.getNumberOfShownElements(); row++) {
        if (this.getContent()[column].length <= row) {
          continue;
        }

        ImageComponent entryComponent;
        if (this.getContent()[column][row] == null) {
          entryComponent = new ImageComponent(this.getX() + (columnWidth * column), this.getY() + ((this.getHeight() / this.getNumberOfShownElements()) * row), (this.getWidth() / this.nbOfColumns), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, "", null);
        }
        else {
          entryComponent = new ImageComponent(this.getX() + (columnWidth * column), this.getY() + ((this.getHeight() / this.getNumberOfShownElements()) * row), (this.getWidth() / this.nbOfColumns), this.getHeight() / this.getNumberOfShownElements(), this.entrySprite, this.getContent()[column][row].toString(), null);
        }
        if (this.isSliderInside()) {
          entryComponent.setX(this.getX() + ((columnWidth - (this.getSlider().getWidth() / this.nbOfColumns))  * column));
          entryComponent.setWidth(entryComponent.getWidth() - (this.getSlider().getWidth() / this.nbOfColumns));
        }
        entryComponent.setTextAlign(Align.LEFT);
        this.getListEntry(column).add(entryComponent);
      }
      this.getComponents().addAll(this.getListEntry(column));
      final int col = column;
      for (final ImageComponent comp : this.getListEntry(col)) {
        comp.onClicked(e -> {
          this.setSelection(col, this.getLowerBound() + this.getListEntry(col).indexOf(comp) % this.getNumberOfShownElements());
          this.refresh();
        });
      }
    }

    this.onChange(s -> {
      if (this.getSlider() != null) {
        this.getSlider().setCurrentValue(this.getLowerBound());
        this.getSlider().getSliderComponent().setLocation(this.getSlider().getRelativeSliderPosition());
      }
    });
    if (this.getSlider() != null) {
      this.getSlider().onChange(sliderValue -> {
        this.setLowerBound(sliderValue.intValue());
        this.getSlider().getSliderComponent().setLocation(this.getSlider().getRelativeSliderPosition());
        this.refresh();
      });
    }
  }

  /**
   * See {@link #setSelectEntireRow(boolean)}
   * 
   * @return
   * true if selection is set to select the entire row; false otherwise
   */
  public boolean isEntireRowSelected() {
    return this.selectEntireRow;
  }

  /**
   * See {@link #setSliderInside(boolean)}
   * 
   * @return
   * true if slider is set to be inside the ListField; false otherwise
   */
  public boolean isSliderInside() {
    return this.sliderInside;
  }

  private void prepareInput() {
    Input.keyboard().onKeyTyped(KeyEvent.VK_UP, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.selectionColumn, this.selectionRow - 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_DOWN, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.selectionColumn, this.selectionRow + 1);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_LEFT, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.selectionColumn - 1, this.selectionRow);
    });

    Input.keyboard().onKeyTyped(KeyEvent.VK_RIGHT, e -> {
      if (this.isSuspended() || !this.isVisible() || !this.isArrowKeyNavigation()) {
        return;
      }
      this.setSelection(this.selectionColumn + 1, this.selectionRow);
    });

    this.onMouseWheelScrolled(e -> {
      if (this.isSuspended() || !this.isVisible()) {
        return;
      }
      if (this.isHovered()) {
        if (e.getEvent().getWheelRotation() < 0) {
          this.setSelection(this.selectionColumn, this.selectionRow - 1);
        } else {
          this.setSelection(this.selectionColumn, this.selectionRow + 1);
        }
        return;
      }
    });
  }
}