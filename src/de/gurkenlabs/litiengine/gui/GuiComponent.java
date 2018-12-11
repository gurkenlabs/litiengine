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

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;

/**
 * The Class GuiComponent.
 */
public abstract class GuiComponent implements MouseListener, MouseMotionListener, MouseWheelListener, IRenderable {
  protected static final Font ICON_FONT;
  private Font font;

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

  private final List<GuiComponent> components;

  private final List<Consumer<String>> textChangedConsumer;

  private boolean drawTextShadow = false;
  private boolean forwardMouseEvents = true;

  private Sound hoverSound;

  private final int id;

  private boolean isHovered;
  private boolean isPressed;
  private boolean isSelected;
  private boolean suspended;
  private boolean enabled;

  private Object tag;

  private String name;
  private String text;

  private Align textAlignment = Align.CENTER;

  private int textAngle = 0;

  private double textX;
  private double textY;

  private boolean visible;

  private double width;
  private double height;
  private double x;
  private double y;

  private double xMargin;
  static {
    Font icon = Resources.fonts().get("fontello.ttf");
    ICON_FONT = icon != null ? icon.deriveFont(16f) : null;
  }

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
    this.disabledAppearance.onChange(app -> {
      for (GuiComponent child : this.getComponents()) {
        child.getAppearanceDisabled().update(this.getAppearanceDisabled());
      }
    });
    this.id = ++componentId;
    this.x = x;
    this.y = y;
    this.setTextXMargin(this.getWidth() / 16);
    this.setFont(GuiProperties.getDefaultFont());
    this.setSelected(false);
    this.setEnabled(true);
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

  public Appearance getAppearance() {
    return this.appearance;
  }

  public Appearance getAppearanceHovered() {
    return this.hoveredAppearance;
  }

  public Appearance getAppearanceDisabled() {
    return this.disabledAppearance;
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
  public double getHeight() {
    return this.height;
  }

  public Sound getHoverSound() {
    return this.hoverSound;
  }

  public String getName() {
    return this.name;
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

  public Align getTextAlign() {
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
  public double getWidth() {
    return this.width;
  }

  /**
   * Gets the x.
   *
   * @return the x
   */
  public double getX() {
    return this.x;
  }

  /**
   * Gets the y.
   *
   * @return the y
   */
  public double getY() {
    return this.y;
  }

  public boolean isEnabled() {
    return enabled;
  }

  public boolean isForwardMouseEvents() {
    return this.forwardMouseEvents;
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

  @Override
  public void mouseDragged(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseDraggedConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseEntered(final MouseEvent e) {
    if (!this.isForwardMouseEvents()) {
      return;
    }

    if (!this.mouseEventShouldBeForwarded(e)) {
      this.isHovered = false;
      return;
    }

    this.isHovered = true;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getHoverConsumer().forEach(consumer -> consumer.accept(event));
    if (this.getHoverSound() != null) {
      Game.audio().playSound(this.getHoverSound());
    }

    this.getMouseEnterConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if (!this.isForwardMouseEvents()) {
      return;
    }

    this.isHovered = false;
    this.isPressed = false;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMouseLeaveConsumer().forEach(consumer -> consumer.accept(event));
  }

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

  @Override
  public void mousePressed(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = true;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    this.getMousePressedConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (!this.mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = false;

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);

    // TODO: check if this should really call the clicked consumers...
    this.getClickConsumer().forEach(consumer -> consumer.accept(event));
    this.getMouseReleasedConsumer().forEach(consumer -> consumer.accept(event));
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

  public void prepare() {
    this.suspended = false;
    this.visible = true;
    Input.mouse().addMouseListener(this);
    Input.mouse().addMouseWheelListener(this);
    Input.mouse().addMouseMotionListener(this);
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

    if (!this.isEnabled()) {
      currentAppearance = this.getAppearanceDisabled();
    }

    if (!currentAppearance.isTransparentBackground()) {
      g.setPaint(currentAppearance.getBackgroundPaint(this.getWidth(), this.getHeight()));
      g.fill(this.getBoundingBox());
    }

    g.setColor(currentAppearance.getForeColor());
    g.setFont(this.getFont());

    this.renderText(g);

    for (final GuiComponent component : this.getComponents()) {
      if (!component.isVisible() || component.isSuspended()) {
        continue;
      }

      component.render(g);
    }

    if (Game.config().debug().renderGuiComponentBoundingBoxes()) {
      g.setColor(Color.RED);
      ShapeRenderer.renderOutline(g, this.getBoundingBox());
    }
  }

  public void setDimension(final double width, final double height) {
    this.width = width;
    this.height = height;
  }

  public void setEnabled(boolean enabled) {
    this.enabled = enabled;
    for (GuiComponent comp : this.getComponents()) {
      comp.setEnabled(this.isEnabled());
    }
  }

  public void setFont(Font font) {
    this.font = font;
  }

  public void setFontSize(float size) {
    this.font = this.font.deriveFont(size);
  }

  public void setForwardMouseEvents(boolean forwardMouseEvents) {
    this.forwardMouseEvents = forwardMouseEvents;
  }

  public void setHeight(final double height) {
    this.height = height;
  }

  public void setHovered(boolean hovered) {
    this.isHovered = hovered;
  }

  public void setHoverSound(final Sound hoverSound) {
    this.hoverSound = hoverSound;
  }

  public void setLocation(final double x, final double y) {
    this.setX(x);
    this.setY(y);
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setLocation(final Point2D location) {
    this.setX(location.getX());
    this.setY(location.getY());
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

  public void setTextAlignment(final Align textAlignment) {
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

  public void setWidth(final double width) {
    this.width = width;
  }

  public void setX(final double x) {
    double delta = x - this.x;
    this.x = x;

    for (GuiComponent component : this.getComponents()) {
      component.setX(component.getX() + delta);
    }
  }

  public void setY(final double y) {
    double delta = y - this.y;
    this.y = y;
    for (GuiComponent component : this.getComponents()) {
      component.setY(component.getY() + delta);
    }
  }

  /**
   * Suspend.
   */
  public void suspend() {
    Input.mouse().removeMouseListener(this);
    Input.mouse().removeMouseWheelListener(this);
    Input.mouse().removeMouseMotionListener(this);
    this.suspended = true;
    this.visible = false;
    for (final GuiComponent childComp : this.getComponents()) {
      childComp.suspend();
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

  protected List<Consumer<ComponentMouseEvent>> getHoverConsumer() {
    return this.hoverConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseDraggedConsumer() {
    return this.mouseDraggedConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseEnterConsumer() {
    return this.mouseEnterConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseLeaveConsumer() {
    return this.mouseLeaveConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseMovedConsumer() {
    return this.mouseMovedConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMousePressedConsumer() {
    return this.mousePressedConsumer;
  }

  protected List<Consumer<ComponentMouseEvent>> getMouseReleasedConsumer() {
    return this.mouseReleasedConsumer;
  }

  protected List<Consumer<ComponentMouseWheelEvent>> getMouseWheelConsumer() {
    return this.mouseWheelConsumer;
  }

  protected void initializeComponents() {
    // nothing to do in the base class
  }

  /**
   * Mouse event should be forwarded.
   *
   * @param e
   *          the e
   * @return true, if successful
   */
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isForwardMouseEvents() && this.isVisible() && this.isEnabled() && !this.isSuspended() && e != null && this.getBoundingBox().contains(e.getPoint());
  }

  /**
   * @param g
   */
  private void renderText(Graphics2D g) {
    if (this.getText() == null || this.getText().isEmpty()) {
      return;
    }

    final FontMetrics fm = g.getFontMetrics();

    double defaultTextX;
    double defaultTextY = fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
    switch (this.getTextAlign()) {
    case LEFT:
      defaultTextX = this.getTextXMargin();
      break;
    case RIGHT:
      defaultTextX = this.getWidth() - this.getTextXMargin() - fm.stringWidth(this.getTextToRender(g));
      break;
    case CENTER:
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

    Object antialiasing = this.getAppearance().getTextAntialiasing();
    if (this.isHovered()) {
      antialiasing = this.getAppearanceHovered().getTextAntialiasing();
    }
    if (!this.isEnabled()) {
      antialiasing = this.getAppearanceDisabled().getTextAntialiasing();
    }

    if (this.getTextAngle() == 0) {
      if (this.drawTextShadow()) {
        TextRenderer.renderWithOutline(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY(), this.getTextShadowColor(), antialiasing);
      } else {
        TextRenderer.render(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY(), antialiasing);
      }
    } else if (this.getTextAngle() == 90) {
      TextRenderer.renderRotated(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY() - fm.stringWidth(this.getTextToRender(g)), this.getTextAngle(), antialiasing);
    } else {
      TextRenderer.renderRotated(g, this.getTextToRender(g), this.getX() + this.getTextX(), this.getY() + this.getTextY(), this.getTextAngle(), antialiasing);
    }
  }
}