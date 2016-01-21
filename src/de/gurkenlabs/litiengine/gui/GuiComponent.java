/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.IGuiComponent;
import de.gurkenlabs.litiengine.input.Input;

// TODO: Auto-generated Javadoc
/**
 * The Class GuiComponent.
 */
public abstract class GuiComponent implements IGuiComponent, MouseListener, MouseMotionListener {

  /** The component id. */
  private static int componentId = 0;

  /** The Constant DEFAULT_BG_COLOR. */
  private static final Color DEFAULT_BG_COLOR = Color.DARK_GRAY;

  /** The Constant DEFAULT_COLOR. */
  private static final Color DEFAULT_COLOR = Color.WHITE;

  /** The back ground color. */
  private Color backGroundColor;

  /** The click consumer. */
  private final List<Consumer<ComponentMouseEvent>> clickConsumer;

  /** The components. */
  private final List<GuiComponent> components;

  /** The id. */
  private final int id;

  /** The is hovered. */
  private boolean isHovered, isPressed, isSelected;

  /** The suspended. */
  private boolean suspended;

  /** The tag. */
  private Object tag;

  /** The text color. */
  private Color textColor;

  /** The visible. */
  private boolean visible;

  /** The width. */
  private int width;

  /** The height. */
  private int height;

  /** The x. */
  private int x;

  /** The y. */
  private int y;

  protected GuiComponent(final int x, final int y) {
    this.components = new ArrayList<>();
    this.clickConsumer = new ArrayList<Consumer<ComponentMouseEvent>>();

    this.setTextColor(DEFAULT_COLOR);
    this.setBackGroundColor(DEFAULT_BG_COLOR);
    this.id = ++componentId;
    this.x = x;
    this.y = y;

    this.setVisible(true);
    this.setSuspended(true);
    this.setSelected(false);

    this.initializeComponents();
    Input.MOUSE.registerMouseListener(this);
    Input.MOUSE.registerMouseMotionListener(this);
  }

  /**
   * Instantiates a new gui component.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   * @param width
   *          the width
   * @param height
   *          the height
   */
  protected GuiComponent(final int x, final int y, final int width, final int height) {
    this(x, y);
    this.width = width;
    this.height = height;
  }

  /**
   * Gets the back ground color.
   *
   * @return the back ground color
   */
  public Color getBackGroundColor() {
    return this.backGroundColor;
  }

  /**
   * Gets the bounding box.
   *
   * @return the bounding box
   */
  public Rectangle2D getBoundingBox() {
    return new Rectangle(x, y, this.width, this.height);
  }

  /**
   * Gets the click consumer.
   *
   * @return the click consumer
   */
  public List<Consumer<ComponentMouseEvent>> getClickConsumer() {
    return this.clickConsumer;
  }

  /**
   * Gets the component id.
   *
   * @return the component id
   */
  public int getComponentId() {
    return this.id;
  }

  /**
   * Gets the hover color.
   *
   * @return the hover color
   */
  public Color getHoverColor() {
    return this.getBackGroundColor().darker();
  }

  /**
   * Gets the tag.
   *
   * @return the tag
   */
  public Object getTag() {
    return this.tag;
  }

  /**
   * Gets the text color.
   *
   * @return the text color
   */
  public Color getTextColor() {
    return this.textColor;
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  @Override
  public int getWidth() {
    return this.width;
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  @Override
  public int getHeight() {
    return this.height;
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  @Override
  public int getX() {
    return this.x;
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  @Override
  public int getY() {
    return this.y;
  }

  /**
   * Checks if is hovered.
   *
   * @return the boolean
   */
  public Boolean isHovered() {
    return this.isHovered;
  }

  /**
   * Checks if is hovered.
   *
   * @return the boolean
   */
  public Boolean isPressed() {
    return this.isPressed;
  }

  public boolean isSelected() {
    return this.isSelected;
  }

  /**
   * Checks if is suspended.
   *
   * @return true, if is suspended
   */
  public boolean isSuspended() {
    return this.suspended;
  }

  /**
   * Checks if is visible.
   *
   * @return true, if is visible
   */
  public boolean isVisible() {
    return this.visible;
  }

  @Override
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseClicked(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseClicked(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.getClickConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseDragged(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseDragged(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseEntered(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseEntered(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      this.isHovered = false;
      return;
    }

    this.isHovered = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(final MouseEvent e) {
    this.isHovered = false;
    this.isPressed = false;
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      this.mouseExited(e);
      return;
    }

    // also throw enter event if the mouse did not hover the component
    // before
    if (!this.isHovered()) {
      this.mouseEntered(e);
    }
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mousePressed(java.awt.event.MouseEvent)
   */
  @Override
  public void mousePressed(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseReleased(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseReleased(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = false;
  }

  /**
   * On clicked.
   *
   * @param callback
   *          the callback
   */
  public void onClicked(final Consumer<ComponentMouseEvent> callback) {
    this.getClickConsumer().add(callback);
  }

  /**
   * Prepare.
   */
  @Override
  public void prepare() {
    this.setSuspended(false);

  }

  /**
   * Sets the location.
   *
   * @param newX
   *          the new x
   * @param newY
   *          the new y
   */
  public void relocate(final int newX, final int newY) {
    this.x = newX;
    this.y = newY;
  }

  /**
   * Render.
   *
   * @param g
   *          the g
   */
  @Override
  public void render(final Graphics g) {
    if (this.isSuspended() || !this.isVisible()) {
      return;
    }

    for (final GuiComponent component : this.getComponents()) {
      if (!component.isVisible() || component.isSuspended()) {
        continue;
      }

      component.render(g);
    }
  }

  /**
   * Sets the back ground color.
   *
   * @param backGroundColor
   *          the new back ground color
   */
  public void setBackGroundColor(final Color backGroundColor) {
    this.backGroundColor = backGroundColor;
  }

  public void setSelected(final boolean bool) {
    this.isSelected = bool;
  }

  /**
   * Sets the suspended.
   *
   * @param suspended
   *          the new suspended
   */
  public void setSuspended(final boolean suspended) {
    this.suspended = suspended;
    for (final GuiComponent component : this.getComponents()) {
      component.setSuspended(suspended);
    }
  }

  /**
   * Sets the tag.
   *
   * @param tag
   *          the new tag
   */
  public void setTag(final Object tag) {
    this.tag = tag;
  }

  /**
   * Sets the text color.
   *
   * @param color
   *          the new text color
   */
  public void setTextColor(final Color color) {
    this.textColor = color;
  }

  /**
   * Sets the visible.
   *
   * @param visible
   *          the new visible
   */
  public void setVisible(final boolean visible) {
    this.visible = visible;
    for (final GuiComponent component : this.getComponents()) {
      component.setVisible(visible);
    }
  }

  /**
   * Suspend.
   */
  @Override
  public void suspend() {
    this.setSuspended(true);
    for (final GuiComponent component : this.getComponents()) {
      component.setSuspended(true);
    }
  }

  public void toggleSelection() {
    this.setSelected(!this.isSelected);

  }

  /**
   * Gets the components.
   *
   * @return the components
   */
  protected List<GuiComponent> getComponents() {
    return this.components;
  }

  /**
   * Initialize components.
   */
  protected abstract void initializeComponents();

  /**
   * Mouse event should be forwarded.
   *
   * @param e
   *          the e
   * @return true, if successful
   */
  private boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isVisible() && !this.isSuspended() && this.getBoundingBox().contains(e.getPoint());
  }
}
