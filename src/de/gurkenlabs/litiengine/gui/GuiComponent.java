/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.graphics.IGuiComponent;
import de.gurkenlabs.litiengine.input.Input;

// TODO: Auto-generated Javadoc
/**
 * The Class GuiComponent.
 */
public abstract class GuiComponent implements IGuiComponent, MouseListener, MouseMotionListener {
  public static final int TEXT_ALIGN_LEFT = 1;
  public static final int TEXT_ALIGN_RIGHT = 2;
  public static final int TEXT_ALIGN_CENTER = 3;
  
  /** The component id. */
  private static int componentId = 0;

  /** The Constant DEFAULT_BG_COLOR. */
  private static final Color DEFAULT_BG_COLOR = Color.DARK_GRAY;

  /** The Constant DEFAULT_COLOR. */
  private static final Color DEFAULT_COLOR = Color.WHITE;

  /** The back ground color. */
  private Color backGroundColor;

  /** The click consumer. */
  private final List<Consumer<ComponentMouseEvent>> clickConsumer, hoverConsumer, mousePressedConsumer, mouseEnterConsumer, mouseLeaveConsumer, mouseDraggedConsumer;

  /** The components. */
  private final List<GuiComponent> components;

  /** The id. */
  private final int id;

  /** The is hovered. */
  private boolean isHovered, isPressed, isSelected, isDragged;

  /** The suspended. */
  private boolean suspended;

  /** The tag. */
  private Object tag;

  /** The text color. */
  private Color textColor;

  /** The visible. */
  private boolean visible;

  /** The width. */
  private double width, height, x, y;

  protected GuiComponent(final double x, final double y) {
    this.components = new CopyOnWriteArrayList<>();
    this.clickConsumer = new CopyOnWriteArrayList<>();
    this.hoverConsumer = new CopyOnWriteArrayList<>();
    this.mousePressedConsumer = new CopyOnWriteArrayList<>();
    this.mouseDraggedConsumer = new CopyOnWriteArrayList<>();
    this.mouseEnterConsumer = new CopyOnWriteArrayList<>();
    this.mouseLeaveConsumer = new CopyOnWriteArrayList<>();

    this.setTextColor(DEFAULT_COLOR);
    this.setBackGroundColor(DEFAULT_BG_COLOR);
    this.id = ++componentId;
    this.x = x;
    this.y = y;

    this.setVisible(true);
    this.suspended = true;
    this.setSelected(false);
    this.initializeComponents();
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
  protected GuiComponent(final double x, final double y, final double width, final double height) {
    this.width = width;
    this.height = height;
    this.components = new ArrayList<>();
    this.clickConsumer = new CopyOnWriteArrayList<>();
    this.hoverConsumer = new CopyOnWriteArrayList<>();
    this.mousePressedConsumer = new CopyOnWriteArrayList<>();
    this.mouseEnterConsumer = new CopyOnWriteArrayList<>();
    this.mouseLeaveConsumer = new CopyOnWriteArrayList<>();
    this.mouseDraggedConsumer = new CopyOnWriteArrayList<>();

    this.setTextColor(DEFAULT_COLOR);
    this.setBackGroundColor(DEFAULT_BG_COLOR);
    this.id = ++componentId;
    this.x = x;
    this.y = y;

    this.setVisible(true);
    this.suspended = true;
    this.setSelected(false);
    this.initializeComponents();
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
    return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
  }

  /**
   * Gets the click consumer.
   *
   * @return the click consumer
   */
  protected List<Consumer<ComponentMouseEvent>> getClickConsumer() {
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
   * Gets the components.
   *
   * @return the components
   */
  public List<GuiComponent> getComponents() {
    return this.components;
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  @Override
  public double getHeight() {
    return this.height;
  }

  /**
   * Gets the hover color.
   *
   * @return the hover color
   */
  public Color getHoverColor() {
    return this.getBackGroundColor().darker();
  }

  protected List<Consumer<ComponentMouseEvent>> getHoverConsumer() {
    return this.hoverConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseEnterConsumer() {
    return this.mouseEnterConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseLeaveConsumer() {
    return this.mouseLeaveConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMousePressedConsumer() {
    return this.mousePressedConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseDraggedConsumer() {
    return this.mouseDraggedConsumer;
  }

  public Point2D getPosition() {
    return new Point2D.Double(this.getX(), this.getY());
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
  public double getWidth() {
    return this.width;
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  @Override
  public double getX() {
    return this.x;
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  @Override
  public double getY() {
    return this.y;
  }

  /**
   * Initialize components.
   */
  protected abstract void initializeComponents();

  /**
   * Checks if is hovered.
   *
   * @return the boolean
   */
  public boolean isHovered() {
    return this.isHovered;
  }

  public boolean isDragged() {
    return this.isDragged();
  }

  /**
   * Checks if is hovered.
   *
   * @return the boolean
   */
  public boolean isPressed() {
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

    if (this.isPressed) {
      this.getClickConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
      this.isPressed = false;
    }
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
      this.isDragged = false;
      return;
    }
    this.isDragged = true;
    this.getMouseDraggedConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
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
    this.getHoverConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
    this.getMouseEnterConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
  }

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

  /*
   * (non-Javadoc)
   *
   * @see java.awt.event.MouseListener#mouseExited(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseExited(final MouseEvent e) {
    this.isHovered = false;
    this.isPressed = false;
    this.getMouseLeaveConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
  }

  /*
   * (non-Javadoc)
   *
   * @see
   * java.awt.event.MouseMotionListener#mouseMoved(java.awt.event.MouseEvent)
   */
  @Override
  public void mouseMoved(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e) && this.isHovered()) {
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
    this.getMousePressedConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
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

    this.getClickConsumer().forEach(consumer -> consumer.accept(new ComponentMouseEvent(e, this)));
  }

  /**
   * On clicked.
   *
   * @param callback
   *          the callback
   */
  public void onClicked(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getClickConsumer().contains(callback)) {
      this.getClickConsumer().add(callback);
    }
  }

  public void onHovered(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getHoverConsumer().contains(callback)) {
      this.getHoverConsumer().add(callback);
    }
  }

  public void onMouseEnter(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseEnterConsumer().contains(callback)) {
      this.getMouseEnterConsumer().add(callback);
    }
  }

  public void onMouseLeave(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseLeaveConsumer().contains(callback)) {
      this.getMouseLeaveConsumer().add(callback);
    }
  }

  public void onMousePressed(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMousePressedConsumer().contains(callback)) {
      this.getMousePressedConsumer().add(callback);
    }
  }

  public void onMouseDragged(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseDraggedConsumer().contains(callback)) {
      this.getMouseDraggedConsumer().add(callback);
    }
  }

  /**
   * Prepare.
   */
  @Override
  public void prepare() {
    for (final GuiComponent component : this.getComponents()) {
      component.prepare();
    }
    this.suspended = false;
    Input.MOUSE.registerMouseListener(this);
    Input.MOUSE.registerMouseMotionListener(this);
  }

  /**
   * Sets the location.
   *
   * @param newX
   *          the new x
   * @param newY
   *          the new y
   */

  /**
   * Render.
   *
   * @param g
   *          the g
   */
  @Override
  public void render(final Graphics2D g) {
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

  @Override
  public void setDimension(final double width, final double height) {
    this.width = width;
    this.height = height;
  }

  @Override
  public void setHeight(final int height) {
    this.height = height;
  }

  @Override
  public void setPosition(final double x, final double y) {
    this.x = x;
    this.y = y;
  }

  @Override
  public void setPosition(final Point2D newPosition) {
    this.x = newPosition.getX();
    this.y = newPosition.getY();
  }

  
  public void setX(double x) {
    this.x = x;
  }

  public void setY(double y) {
    this.y = y;
  }

  public void setSelected(final boolean bool) {
    this.isSelected = bool;
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
    for(GuiComponent comp: this.getComponents()){
      comp.setTextColor(color);
    }
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

  @Override
  public void setWidth(final int width) {
    this.width = width;
  }

  /**
   * Suspend.
   */
  @Override
  public void suspend() {
    for (final GuiComponent component : this.getComponents()) {
      component.suspend();
    }
    Input.MOUSE.unregisterMouseListener(this);
    Input.MOUSE.unregisterMouseMotionListener(this);
    this.suspended = true;
  }

  public void toggleSelection() {
    this.setSelected(!this.isSelected);

  }
}
