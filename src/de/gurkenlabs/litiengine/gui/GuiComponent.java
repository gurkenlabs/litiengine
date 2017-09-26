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

/**
 * The Class GuiComponent.
 */
public abstract class GuiComponent implements IGuiComponent, MouseListener, MouseMotionListener, MouseWheelListener {
  public static final int TEXT_ALIGN_CENTER = 3;
  public static final int TEXT_ALIGN_LEFT = 1;
  public static final int TEXT_ALIGN_RIGHT = 2;

  protected static final Font ICON_FONT = FontLoader.load("fontello.ttf").deriveFont(16f);

  private static int componentId = 0;

  private final Appearance appearance;
  private final Appearance hoveredAppearance;
  private final Appearance disabledAppearance;

  /** The back ground color. */
  private Color textShadowColor;

  /** The click consumer. */
  private final List<Consumer<ComponentMouseEvent>> clickConsumer;
  private final List<Consumer<ComponentMouseEvent>> hoverConsumer;
  private final List<Consumer<ComponentMouseEvent>> mousePressedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseEnterConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseLeaveConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseDraggedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseReleasedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseMovedConsumer;

  private final List<Consumer<ComponentMouseWheelEvent>> mouseWheelConsumer;

  /** The components. */
  private final List<GuiComponent> components;

  private final List<Consumer<String>> textChangedConsumer;

  private boolean drawTextShadow = false;

  private Sound hoverSound;

  private final int id;

  private boolean isHovered;
  private boolean isPressed;
  private boolean isSelected;
  private boolean suspended;

  private Object tag;

  private String text;

  private int textAlignment = TEXT_ALIGN_CENTER;

  private int textAngle = 0;

  private double textX;
  private double textY;

  private boolean visible;

  private double width;
  private double height;
  private double x;
  private double y;

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

    this.appearance = new Appearance();
    this.appearance.update(GuiProperties.getDefaultAppearance());
    this.appearance.onChange(app -> {
      for (GuiComponent child : this.getComponents()) {
        child.getAppearance().update(this.getAppearance());
      }
    });

    this.hoveredAppearance = new Appearance();
    this.hoveredAppearance.update(GuiProperties.getDefaultAppearanceHovered());
    this.hoveredAppearance.onChange(app -> {
      for (GuiComponent child : this.getComponents()) {
        child.getAppearanceHovered().update(this.getAppearanceHovered());
      }
    });

    this.disabledAppearance = new Appearance();
    this.disabledAppearance.update(GuiProperties.getDefaultAppearanceDisabled());
    this.appearance.onChange(app -> {
      for (GuiComponent child : this.getComponents()) {
        child.getAppearanceDisabled().update(this.getAppearanceDisabled());
      }
    });

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

  public boolean drawTextShadow() {
    return this.drawTextShadow;
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

  /**
   * Gets the height.
   *
   * @return the height
   */
  @Override
  public double getHeight() {
    return this.height;
  }

  public List<Consumer<ComponentMouseEvent>> getHoverConsumer() {
    return this.hoverConsumer;
  }

  public Sound getHoverSound() {
    return this.hoverSound;
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

  public Point2D getLocation() {
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

  public Appearance getAppearance() {
    return appearance;
  }

  public Appearance getAppearanceHovered() {
    return hoveredAppearance;
  }

  public Appearance getAppearanceDisabled() {
    return disabledAppearance;
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

  @Override
  public void prepare() {
    this.onHovered(e -> {
      if (this.getHoverSound() != null) {
        Game.getSoundEngine().playSound(this.getHoverSound());
      }
    });

    this.suspended = false;
    this.visible = true;
    Input.mouse().registerMouseListener(this);
    Input.mouse().registerMouseWheelListener(this);
    Input.mouse().registerMouseMotionListener(this);
    for (final GuiComponent component : this.getComponents()) {
      component.prepare();
    }
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.isSuspended() || !this.isVisible()) {
      return;
    }

    Appearance currentAppearance = this.getAppearance();
    if (this.isHovered()) {
      currentAppearance = this.getAppearanceHovered();
    }

    if (!currentAppearance.isTransparentBackground()) {
      g.setPaint(currentAppearance.getBackgroundPaint(this.getWidth(), this.getHeight()));
      g.fill(this.getBoundingBox());
    }

    g.setColor(currentAppearance.getForeColor());
    g.setFont(currentAppearance.getFont());

    this.renderText(g);

    for (final GuiComponent component : this.getComponents()) {
      if (!component.isVisible() || component.isSuspended()) {
        continue;
      }

      component.render(g);
    }

    if (Game.getConfiguration().debug().renderGuiComponentBoundingBoxes()) {
      g.setColor(Color.RED);
      g.draw(this.getBoundingBox());
    }
  }

  @Override
  public void setDimension(final double width, final double height) {
    this.width = width;
    this.height = height;
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
    Input.mouse().unregisterMouseListener(this);
    Input.mouse().unregisterMouseWheelListener(this);
    Input.mouse().unregisterMouseMotionListener(this);
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
  protected void initializeComponents() {
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

  private void renderText(Graphics2D g) {
    if (this.getText() != null) {
      final FontMetrics fm = g.getFontMetrics();

      double defaultTextX;
      double defaultTextY = fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
      switch (this.getTextAlignment()) {
      case TEXT_ALIGN_LEFT:
        defaultTextX = this.getTextXMargin();
        break;
      case TEXT_ALIGN_RIGHT:
        defaultTextX = this.getWidth() - this.getTextXMargin() - fm.stringWidth(this.getTextToRender(g));
        break;
      case TEXT_ALIGN_CENTER:
      default:
        defaultTextX = this.getWidth() / 2 - fm.stringWidth(this.getTextToRender(g)) / 2.0;
        break;
      }
      if (this.getTextY() == 0) {
        this.setTextY(defaultTextY);
      }

      if (this.getTextX() == 0) {
        this.setTextX(defaultTextX);
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
  }
}
