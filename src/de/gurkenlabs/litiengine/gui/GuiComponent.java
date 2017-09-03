/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.IGuiComponent;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.sound.Sound;

// TODO: Auto-generated Javadoc
/**
 * The Class GuiComponent.
 */
public abstract class GuiComponent implements IGuiComponent, MouseListener, MouseMotionListener, MouseWheelListener {
  public static final int TEXT_ALIGN_CENTER = 3;
  public static final int TEXT_ALIGN_LEFT = 1;
  public static final int TEXT_ALIGN_RIGHT = 2;
  protected static final Font ICON_FONT = FontLoader.load("fontello.ttf").deriveFont(16f);

  /** The component id. */
  private static int componentId = 0;
  /** The Constant DEFAULT_COLOR. */
  private static final Color DEFAULT_COLOR = Color.WHITE;

  /** The back ground color. */
  private Color backGroundColor, textShadowColor;

  /** The click consumer. */
  private final List<Consumer<ComponentMouseEvent>> clickConsumer, hoverConsumer, mousePressedConsumer, mouseEnterConsumer, mouseLeaveConsumer, mouseDraggedConsumer, mouseReleasedConsumer, mouseMovedConsumer;

  private final List<Consumer<ComponentMouseWheelEvent>> mouseWheelConsumer;

  /** The components. */
  private final List<GuiComponent> components;

  private final List<Consumer<String>> textChangedConsumer;

  private Boolean drawTextShadow = false;
  private Font font;

  private Sound hoverSound;

  private Color hoverTextColor;

  /** The id. */
  private final int id;

  /** The is hovered. */
  private boolean isHovered, isPressed, isSelected;

  /** The suspended. */
  private boolean suspended;

  /** The tag. */
  private Object tag;

  private String text;

  private int textAlignment = TEXT_ALIGN_CENTER;

  private int textAngle = 0;

  /** The text color. */
  private Color textColor;
  private double textX;
  private double textY;

  /** The visible. */
  private boolean visible;

  /** The width. */
  private double width, height, x, y, defaultTextX, defaultTextY;
  private double xMargin;

  protected GuiComponent(final double x, final double y) {
    this.components = new CopyOnWriteArrayList<>();
    this.clickConsumer = new CopyOnWriteArrayList<>();
    this.hoverConsumer = new CopyOnWriteArrayList<>();
    this.mousePressedConsumer = new CopyOnWriteArrayList<>();
    this.mouseDraggedConsumer = new CopyOnWriteArrayList<>();
    this.mouseEnterConsumer = new CopyOnWriteArrayList<>();
    this.mouseLeaveConsumer = new CopyOnWriteArrayList<>();
    this.mouseReleasedConsumer = new CopyOnWriteArrayList<>();
    this.mouseWheelConsumer = new CopyOnWriteArrayList<>();
    this.mouseMovedConsumer = new CopyOnWriteArrayList<>();
    this.textChangedConsumer = new CopyOnWriteArrayList<>();

    this.setTextColor(DEFAULT_COLOR);
    this.setHoverTextColor(DEFAULT_COLOR);
    this.id = ++componentId;
    this.x = x;
    this.y = y;
    this.setTextXMargin(this.getWidth() / 16);
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
    this(x, y);
    this.setWidth(width);
    this.setHeight(height);
  }

  public Boolean drawTextShadow() {
    return this.drawTextShadow;
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

  public Font getFont() {
    return this.font;
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

  public List<Consumer<ComponentMouseEvent>> getHoverConsumer() {
    return this.hoverConsumer;
  }

  public Sound getHoverSound() {
    return this.hoverSound;
  }

  public Color getHoverTextColor() {
    return this.hoverTextColor;
  }

  public List<Consumer<ComponentMouseEvent>> getMouseDraggedConsumer() {
    return this.mouseDraggedConsumer;
  }

  public List<Consumer<ComponentMouseEvent>> getMouseEnterConsumer() {
    return this.mouseEnterConsumer;
  }

  public List<Consumer<ComponentMouseEvent>> getMouseLeaveConsumer() {
    return this.mouseLeaveConsumer;
  }

  public List<Consumer<ComponentMouseEvent>> getMouseMovedConsumer() {
    return this.mouseMovedConsumer;
  }

  public List<Consumer<ComponentMouseEvent>> getMousePressedConsumer() {
    return this.mousePressedConsumer;
  }

  public List<Consumer<ComponentMouseEvent>> getMouseReleasedConsumer() {
    return this.mouseReleasedConsumer;
  }

  public List<Consumer<ComponentMouseWheelEvent>> getMouseWheelConsumer() {
    return this.mouseWheelConsumer;
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

  public String getText() {
    return this.text;
  }

  public int getTextAlignment() {
    return this.textAlignment;
  }

  public int getTextAngle() {
    return this.textAngle;
  }

  /**
   * Gets the text color.
   *
   * @return the text color
   */
  public Color getTextColor() {
    return this.textColor;
  }

  public Color getTextShadowColor() {
    return this.textShadowColor;
  }

  public String getTextToRender(final Graphics2D g) {
    if (this.getText() == null) {
      return "";
    }
    final FontMetrics fm = g.getFontMetrics();
    String newText = this.getText();

    while (newText.length() > 1 && fm.stringWidth(newText) >= this.getWidth() - this.getTextXMargin()) {
      newText = newText.substring(1, newText.length());
    }
    return newText;

  }

  public double getTextX() {
    return this.textX;
  }

  public double getTextXMargin() {
    return this.xMargin;
  }

  public double getTextY() {
    return this.textY;
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

  public boolean isDragged() {
    return this.isDragged();
  }

  /**
   * Checks if is hovered.
   *
   * @return the boolean
   */
  public boolean isHovered() {
    return this.isHovered;
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
      final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
      this.getClickConsumer().forEach(consumer -> consumer.accept(event));
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
      return;
    }
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseDraggedConsumer().forEach(consumer -> consumer.accept(event));
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
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getHoverConsumer().forEach(consumer -> consumer.accept(event));
    this.getMouseEnterConsumer().forEach(consumer -> consumer.accept(event));
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
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseLeaveConsumer().forEach(consumer -> consumer.accept(event));
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

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseMovedConsumer().forEach(consumer -> consumer.accept(event));
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
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMousePressedConsumer().forEach(consumer -> consumer.accept(event));
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

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseReleasedConsumer().forEach(consumer -> consumer.accept(event));
    this.getClickConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    this.getMouseWheelConsumer().forEach(consumer -> consumer.accept(new ComponentMouseWheelEvent(e, this)));

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

  public void onMouseDragged(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseDraggedConsumer().contains(callback)) {
      this.getMouseDraggedConsumer().add(callback);
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

  public void onMouseMoved(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseMovedConsumer().contains(callback)) {
      this.getMouseMovedConsumer().add(callback);
    }
  }

  public void onMousePressed(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMousePressedConsumer().contains(callback)) {
      this.getMousePressedConsumer().add(callback);
    }
  }

  public void onMouseReleased(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseReleasedConsumer().contains(callback)) {
      this.getMouseReleasedConsumer().add(callback);
    }
  }

  public void onMouseWheelScrolled(final Consumer<ComponentMouseWheelEvent> callback) {
    if (!this.getMouseWheelConsumer().contains(callback)) {
      this.getMouseWheelConsumer().add(callback);
    }
  }

  public void onTextChanged(final Consumer<String> cons) {
    this.textChangedConsumer.add(cons);
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
   * Prepare.
   */
  @Override
  public void prepare() {
    this.onHovered(e -> {
      if (this.getHoverSound() != null) {
        Game.getSoundEngine().playSound(this.getHoverSound());
      }
    });

    this.suspended = false;
    this.visible = true;
    Input.MOUSE.registerMouseListener(this);
    Input.MOUSE.registerMouseWheelListener(this);
    Input.MOUSE.registerMouseMotionListener(this);
    for (final GuiComponent component : this.getComponents()) {
      component.prepare();
    }
  }

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
    if (this.getBackGroundColor() != null) {
      g.setColor(this.getBackGroundColor());
      g.fill(this.getBoundingBox());
    }
    g.setColor(this.isHovered() ? this.getHoverTextColor() : this.getTextColor());
    g.setFont(this.getFont());
    if (this.getText() != null) {
      final FontMetrics fm = g.getFontMetrics();

      this.defaultTextY = fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
      switch (this.getTextAlignment()) {
      case TEXT_ALIGN_LEFT:
        this.defaultTextX = this.getTextXMargin();
        break;
      case TEXT_ALIGN_RIGHT:
        this.defaultTextX = this.getWidth() - this.getTextXMargin() - fm.stringWidth(this.getTextToRender(g));
        break;
      default:
      case TEXT_ALIGN_CENTER:
        this.defaultTextX = this.getWidth() / 2 - fm.stringWidth(this.getTextToRender(g)) / 2;
        break;
      }
      if (this.getTextY() == 0) {
        this.setTextY(this.defaultTextY);
      }

      if (this.getTextX() == 0) {
        this.setTextX(this.defaultTextX);
      }
      if (this.getTextAngle() == 0) {
        if (this.drawTextShadow()) {
          RenderEngine.drawTextWithShadow(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY(), this.getTextShadowColor());
        } else {
          RenderEngine.drawText(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY());
        }
      } else if (this.getTextAngle() == 90) {
        RenderEngine.drawRotatedText(g, this.getX() + this.getTextX(), this.getY() + this.getTextY() - fm.stringWidth(this.getTextToRender(g)), this.getTextAngle(), this.getTextToRender(g));
      } else {
        RenderEngine.drawRotatedText(g, this.getX() + this.getTextX(), this.getY() + this.getTextY(), this.getTextAngle(), this.getTextToRender(g));
      }

    }

    for (final GuiComponent component : this.getComponents()) {
      if (!component.isVisible() || component.isSuspended()) {
        continue;
      }

      component.render(g);
    }
  }

  public void setBackGroundColor(final Color backGroundColor) {
    this.backGroundColor = backGroundColor;
  }

  @Override
  public void setDimension(final double width, final double height) {
    this.width = width;
    this.height = height;
  }

  public void setFont(final Font font) {
    this.font = font;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setFont(font);
    }
  }

  public void setFontSize(final float size) {
    this.setFont(this.getFont().deriveFont(size));
  }

  @Override
  public void setHeight(final double height) {
    this.height = height;
  }

  public void setHoverSound(final Sound hoverSound) {
    this.hoverSound = hoverSound;
    for (final GuiComponent component : this.getComponents()) {
      component.setHoverSound(hoverSound);
    }
  }

  public void setHoverTextColor(final Color hoverTextColor) {
    this.hoverTextColor = hoverTextColor;
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

  public void setText(final String text) {
    this.text = text;
    for (final Consumer<String> cons : this.textChangedConsumer) {
      cons.accept(this.getText());
    }
    this.setTextX(0);
  }

  public void setTextAlignment(final int textAlignment) {
    this.textAlignment = textAlignment;
  }

  public void setTextAngle(final int textAngle) {
    this.textAngle = textAngle;
  }

  /**
   * Sets the text color.
   *
   * @param color
   *          the new text color
   */
  public void setTextColor(final Color color) {
    this.textColor = color;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setTextColor(color);
    }
  }

  public void setTextShadow(final boolean drawTextShadow) {
    this.drawTextShadow = drawTextShadow;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setTextShadow(drawTextShadow);
    }
  }

  public void setTextShadowColor(final Color textShadowColor) {
    this.textShadowColor = textShadowColor;
  }

  public void setTextX(final double x) {
    this.textX = x;
  }

  public void setTextXMargin(final double xMargin) {
    this.xMargin = xMargin;
  }

  public void setTextY(final double y) {
    this.textY = y;
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
  public void setWidth(final double width) {
    this.width = width;
  }

  public void setX(final double x) {
    this.x = x;
  }

  public void setY(final double y) {
    this.y = y;
  }

  /**
   * Suspend.
   */
  @Override
  public void suspend() {
    Input.MOUSE.unregisterMouseListener(this);
    Input.MOUSE.unregisterMouseWheelListener(this);
    Input.MOUSE.unregisterMouseMotionListener(this);
    this.suspended = true;
    this.visible = false;
    for (final IGuiComponent childComp : this.getComponents()) {
      childComp.suspend();
      this.getComponents().remove(childComp);
    }
  }

  public void toggleSelection() {
    this.setSelected(!this.isSelected);
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
