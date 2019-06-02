package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
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
 * The abstract Class GuiComponent provides all properties and methods needed for screens, built-in, and custom GUI components such as buttons,
 * sliders, etc... It includes mouse event handling, different hovering states and appearances, and texts to be rendered.
 */
public abstract class GuiComponent implements MouseListener, MouseMotionListener, MouseWheelListener, IRenderable {

  protected static final Font ICON_FONT;
  private static int id = 0;
  static {
    final Font icon = Resources.fonts().get("fontello.ttf");
    ICON_FONT = icon != null ? icon.deriveFont(16f) : null;
  }
  private final Appearance appearance;
  private final List<Consumer<ComponentMouseEvent>> clickConsumer;
  private final List<GuiComponent> components;
  private final Appearance disabledAppearance;

  private boolean drawTextShadow = false;
  private boolean enabled;
  private Font font;
  private boolean forwardMouseEvents = true;
  private double height;
  private final List<Consumer<ComponentMouseEvent>> hoverConsumer;
  private final Appearance hoveredAppearance;
  private Sound hoverSound;
  private final int componentId;
  private boolean isHovered;

  private boolean isPressed;
  private boolean isSelected;
  private final List<Consumer<ComponentMouseEvent>> mouseDraggedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseEnterConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseLeaveConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseMovedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mousePressedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseReleasedConsumer;
  private final List<Consumer<ComponentMouseWheelEvent>> mouseWheelConsumer;
  private String name;
  private boolean suspended;
  private Object tag;
  private String text;
  private Align textAlignment = Align.CENTER;
  private int textAngle = 0;
  private final List<Consumer<String>> textChangedConsumer;
  private Color textShadowColor;
  private double textX;
  private double textY;
  private boolean visible;
  private double width;
  private double x;
  private double xMargin;
  private double y;

  /**
   * Instantiates a new gui component with the dimension (0,0) at the given location.
   *
   * @param x
   *          the x
   * @param y
   *          the y
   */
  protected GuiComponent(final double x, final double y) {
    this(x, y, 0, 0);
  }

  /**
   * Instantiates a new gui component at the point (x,y) with the dimension (width,height).
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
      for (final GuiComponent child : this.getComponents()) {
        child.getAppearance().update(this.getAppearance());
      }
    });

    this.hoveredAppearance = new Appearance();
    this.hoveredAppearance.update(GuiProperties.getDefaultAppearanceHovered());
    this.hoveredAppearance.onChange(app -> {
      for (final GuiComponent child : this.getComponents()) {
        child.getAppearanceHovered().update(this.getAppearanceHovered());
      }
    });

    this.disabledAppearance = new Appearance();
    this.disabledAppearance.update(GuiProperties.getDefaultAppearanceDisabled());
    this.disabledAppearance.onChange(app -> {
      for (final GuiComponent child : this.getComponents()) {
        child.getAppearanceDisabled().update(this.getAppearanceDisabled());
      }
    });
    this.componentId = ++id;

    this.setLocation(x, y);
    this.setDimension(width, height);

    this.setHorizontalTextMargin(this.getWidth() / 16);
    this.setFont(GuiProperties.getDefaultFont());
    this.setSelected(false);
    this.setEnabled(true);
    this.initializeComponents();
  }

  /**
   * Draw text shadow.
   *
   * @return true, if successful
   */
  public boolean drawTextShadow() {
    return this.drawTextShadow;
  }

  /**
   * Gets the default appearance object for this GuiComponent.
   *
   * @return the appearance
   */
  public Appearance getAppearance() {
    return this.appearance;
  }

  /**
   * Gets the appearance object for this GuiComponent while disabled.
   *
   * @return the appearance disabled
   */
  public Appearance getAppearanceDisabled() {
    return this.disabledAppearance;
  }

  /**
   * Gets the appearance object for this GuiComponent while hovered.
   *
   * @return the hovered appearance
   */
  public Appearance getAppearanceHovered() {
    return this.hoveredAppearance;
  }

  /**
   * Gets the bounding box of this GuiComponent.
   *
   * @return the bounding box
   */
  public Rectangle2D getBoundingBox() {
    return new Rectangle2D.Double(this.x, this.y, this.width, this.height);
  }

  /**
   * Gets the component id of this GuiComponent.
   *
   * @return the component id
   */
  public int getComponentId() {
    return this.componentId;
  }

  /**
   * Gets the child components of this GuiComponent.
   *
   * @return the child components
   */
  public List<GuiComponent> getComponents() {
    return this.components;
  }

  /**
   * Gets the font of this GuiComponent's text.
   *
   * @return the GuiComponent's font
   */
  public Font getFont() {
    return this.font;
  }

  /**
   * Gets the height of this GuiComponent.
   *
   * @return the height
   */
  public double getHeight() {
    return this.height;
  }

  /**
   * Gets the margin size between the GuiComponent's left and right border and the Text bounds.
   *
   * @return the horizontal text margin
   */
  public double getHorizontalTextMargin() {
    return this.xMargin;
  }

  /**
   * Gets the sound that is played when hovering the GuiComponent.
   *
   * @return the hover sound
   */
  public Sound getHoverSound() {
    return this.hoverSound;
  }

  /**
   * Gets the screen location of this GuiComponent.
   *
   * @return the screen location
   */
  public Point2D getLocation() {
    return new Point2D.Double(this.getX(), this.getY());
  }

  /**
   * Gets the name of this GuiComponent.
   *
   * @return the name
   */
  public String getName() {
    return this.name;
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
   * Gets the entire Text associated with this GuiComponent. Parts of the Text may get cropped and can therefore be invisible.
   * To retrieve only the visible part of the text, use <code>GuiComponent.getTextToRender(Graphics2D g)</code>.
   *
   * @return the entire text on this GuiComponent
   */
  public String getText() {
    return this.text;
  }

  /**
   * Gets the horizontal text alignment.
   *
   * @return the horizontal text alignment
   */
  public Align getTextAlign() {
    return this.textAlignment;
  }

  /**
   * Gets the text angle.
   *
   * @return the text angle
   */
  public int getTextAngle() {
    return this.textAngle;
  }

  /**
   * Gets the text shadow color.
   *
   * @return the text shadow color
   */
  public Color getTextShadowColor() {
    return this.textShadowColor;
  }

  /**
   * Gets only the non-cropped bits of Text visible on this GuiComponent.m
   * To retrieve only the entire text associated with this GuiComponent, use <code>GuiComponent.getText()</code>.
   *
   * @param g
   *          The graphics object to render on.
   * @return the text to render
   */
  public String getTextToRender(final Graphics2D g) {
    if (this.getText() == null) {
      return "";
    }
    final FontMetrics fm = g.getFontMetrics();
    String newText = this.getText();

    while (newText.length() > 1 && fm.stringWidth(newText) >= this.getWidth() - this.getHorizontalTextMargin()) {
      newText = newText.substring(1, newText.length());
    }
    return newText;

  }

  /**
   * Gets the text X coordinate.
   *
   * @return the text X
   */
  public double getTextX() {
    return this.textX;
  }

  /**
   * Gets the text Y coordinate.
   *
   * @return the text Y
   */
  public double getTextY() {
    return this.textY;
  }

  /**
   * Gets the width of this GuiComponent.
   *
   * @return the width
   */
  public double getWidth() {
    return this.width;
  }

  /**
   * Gets the x coordinate of this GuiComponent.
   *
   * @return the x coordinate
   */
  public double getX() {
    return this.x;
  }

  /**
   * Gets the y coordinate of this GuiComponent.
   *
   * @return the y coordinate
   */
  public double getY() {
    return this.y;
  }

  /**
   * Checks if the GuiComponent is enabled.
   *
   * @return true, if is enabled
   */
  public boolean isEnabled() {
    return this.enabled;
  }

  /**
   * Checks if mouse events are being forwarded by this GuiComponent.
   *
   * @return true, the GuiComponent forwards mouse events
   */
  public boolean isForwardMouseEvents() {
    return this.forwardMouseEvents;
  }

  /**
   * Checks if the cursor bounding box intersects with this GuiCOmponent's bounding box.
   *
   * @return true, if the GuiComponent is hovered
   */
  public boolean isHovered() {
    return this.isHovered;
  }

  /**
   * Checks if the mouse button is currently being pressed on this GuiComponent.
   *
   * @return true, if the mouse is currently pressed on the GuiComponent
   */
  public boolean isPressed() {
    return this.isPressed;
  }

  /**
   * Checks if the GuiComponent is currently selected.
   *
   * @return true, if the GuiComponent is selected
   */
  public boolean isSelected() {
    return this.isSelected;
  }

  /**
   * Checks if the GuiComponent is currently suspended.
   *
   * @return true, if the GuiComponent is suspended
   */
  public boolean isSuspended() {
    return this.suspended;
  }

  /**
   * Checks if the GuiComponent is currently visible.
   *
   * @return true, if the GuiComponent is visible
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
   * Add a callback that is being executed if this GuiComponent is clicked once.
   *
   * @param callback
   *          the callback
   */
  public void onClicked(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getClickConsumer().contains(callback)) {
      this.getClickConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if this GuiComponent is hovered with the mouse.
   *
   * @param callback
   *          the callback
   */
  public void onHovered(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getHoverConsumer().contains(callback)) {
      this.getHoverConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is pressed and moving around while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseDragged(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseDraggedConsumer().contains(callback)) {
      this.getMouseDraggedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse enters the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseEnter(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseEnterConsumer().contains(callback)) {
      this.getMouseEnterConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse leaves the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseLeave(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseLeaveConsumer().contains(callback)) {
      this.getMouseLeaveConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is moving around while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseMoved(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseMovedConsumer().contains(callback)) {
      this.getMouseMovedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is continually pressed while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMousePressed(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMousePressedConsumer().contains(callback)) {
      this.getMousePressedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse button is released while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseReleased(final Consumer<ComponentMouseEvent> callback) {
    if (!this.getMouseReleasedConsumer().contains(callback)) {
      this.getMouseReleasedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse wheel is scrolled while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseWheelScrolled(final Consumer<ComponentMouseWheelEvent> callback) {
    if (!this.getMouseWheelConsumer().contains(callback)) {
      this.getMouseWheelConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the text on this GuiComponent changes.
   *
   * @param cons
   *          the cons
   */
  public void onTextChanged(final Consumer<String> cons) {
    this.textChangedConsumer.add(cons);
  }

  /**
   * Prepare the GuiComponent and all its child Components (Makes the GuiComponent visible and adds mouse listeners.).
   * This is, for example, done right before switching to a new screen.
   *
   */
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

    Shape clip = g.getClip();
    g.clip(this.getShape());

    if (!currentAppearance.isTransparentBackground()) {
      g.setPaint(currentAppearance.getBackgroundPaint(this.getWidth(), this.getHeight()));
      g.fill(this.getBoundingBox());
    }

    g.setColor(currentAppearance.getForeColor());
    g.setFont(this.getFont());

    this.renderText(g);

    g.setClip(clip);
    if (currentAppearance.getBorderColor() != null && currentAppearance.getBorderStyle() != null) {
      Stroke s = g.getStroke();
      g.setStroke(currentAppearance.getBorderStyle());
      g.setColor(currentAppearance.getBorderColor());
      g.draw(this.getShape());
      g.setStroke(s);
    }
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

  public RectangularShape getShape() {
    float radius = this.getCurrentAppearance().getBorderRadius();
    if (radius == 0f) {
      return this.getBoundingBox();
    }
    return new RoundRectangle2D.Double(this.getX(), this.getY(), this.getWidth(), this.getHeight(), this.getCurrentAppearance().getBorderRadius(), this.getCurrentAppearance().getBorderRadius());
  }

  protected Appearance getCurrentAppearance() {
    if (!this.isEnabled()) {
      return this.getAppearanceDisabled();
    }
    return this.isHovered() ? this.getAppearanceHovered() : this.getAppearance();
  }

  /**
   * Sets the width and height of this GuiComponent.
   *
   * @param width
   *          the width
   * @param height
   *          the height
   */
  public void setDimension(final double width, final double height) {
    this.width = width;
    this.height = height;
  }

  /**
   * Sets the "enabled" property on this GuiComponent and its child components.
   *
   * @param enabled
   *          the new enabled property
   */
  public void setEnabled(final boolean enabled) {
    this.enabled = enabled;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setEnabled(this.isEnabled());
    }
  }

  /**
   * Sets the font for this GuiComponent's text.
   *
   * @param font
   *          the new font
   */
  public void setFont(final Font font) {
    this.font = font;
  }

  /**
   * Sets the font size for this GuiComponent's text.
   *
   * @param size
   *          the new font size
   */
  public void setFontSize(final float size) {
    this.font = this.font.deriveFont(size);
  }

  /**
   * Enable or disable forwarding mouse events by this GuiComponent.
   *
   * @param forwardMouseEvents
   *          the new forward mouse events
   */
  public void setForwardMouseEvents(final boolean forwardMouseEvents) {
    this.forwardMouseEvents = forwardMouseEvents;
  }

  /**
   * Sets the GuiComponent's height.
   *
   * @param height
   *          the new height
   */
  public void setHeight(final double height) {
    this.height = height;
  }

  /**
   * Sets the margin size between the GuiComponent's left and right border and the Text bounds.
   *
   * @param xMargin
   *          the new text X margin
   */
  public void setHorizontalTextMargin(final double xMargin) {
    this.xMargin = xMargin;
  }

  /**
   * Sets the "enabled" property on this GuiComponent.
   *
   * @param hovered
   *          the new hovered
   */
  public void setHovered(final boolean hovered) {
    this.isHovered = hovered;
  }

  /**
   * Sets the hover sound.
   *
   * @param hoverSound
   *          the new hover sound
   */
  public void setHoverSound(final Sound hoverSound) {
    this.hoverSound = hoverSound;
  }

  /**
   * Sets this GuiComponent's location.
   *
   * @param x
   *          the new x coordinate
   * @param y
   *          the new y coordinate
   */
  public void setLocation(final double x, final double y) {
    this.setX(x);
    this.setY(y);
  }

  /**
   * Sets this GuiComponent's location.
   *
   * @param location
   *          the new location
   */
  public void setLocation(final Point2D location) {
    this.setLocation(location.getX(), location.getY());
  }

  /**
   * Sets this GuiComponent's name.
   *
   * @param name
   *          the new name
   */
  public void setName(final String name) {
    this.name = name;
  }

  /**
   * Sets the "selected" property on this GuiComponent.
   *
   * @param bool
   *          the new selected
   */
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
   * Sets the text.
   *
   * @param text
   *          the new text
   */
  public void setText(final String text) {
    this.text = text;
    for (final Consumer<String> cons : this.textChangedConsumer) {
      cons.accept(this.getText());
    }
    this.setTextX(0);
  }

  /**
   * Sets the horizontal text alignment.
   *
   * @param textAlignment
   *          the new text align
   */
  public void setTextAlign(final Align textAlignment) {
    this.textAlignment = textAlignment;
  }

  /**
   * Sets the text angle in degrees.
   *
   * @param textAngle
   *          the new text angle in degrees
   */
  public void setTextAngle(final int textAngle) {
    this.textAngle = textAngle;
  }

  /**
   * Enable or disable the shadow being drawn below the text
   *
   * @param drawTextShadow
   *          the boolean determining if a text shadow should be drawn
   */
  public void setTextShadow(final boolean drawTextShadow) {
    this.drawTextShadow = drawTextShadow;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setTextShadow(drawTextShadow);
    }
  }

  /**
   * Sets the text shadow color.
   *
   * @param textShadowColor
   *          the new text shadow color
   */
  public void setTextShadowColor(final Color textShadowColor) {
    this.textShadowColor = textShadowColor;
  }

  /**
   * Sets the text X coordinate.
   *
   * @param x
   *          the new text X
   */
  public void setTextX(final double x) {
    this.textX = x;
  }

  /**
   * Sets the text Y coordinate.
   *
   * @param y
   *          the new text Y
   */
  public void setTextY(final double y) {
    this.textY = y;
  }

  /**
   * Sets the "visible" property on this GuiComponent.
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
   * Sets the GuiComponent's width.
   *
   * @param width
   *          the new width
   */
  public void setWidth(final double width) {
    this.width = width;
  }

  /**
   * Sets the GuiComponent's x coordinate.
   *
   * @param x
   *          the new x coordinate
   */
  public void setX(final double x) {
    final double delta = x - this.x;
    this.x = x;

    for (final GuiComponent component : this.getComponents()) {
      component.setX(component.getX() + delta);
    }
  }

  /**
   * Sets the GuiComponent's y coordinate.
   *
   * @param y
   *          the new y coordinate
   */
  public void setY(final double y) {
    final double delta = y - this.y;
    this.y = y;
    for (final GuiComponent component : this.getComponents()) {
      component.setY(component.getY() + delta);
    }
  }

  /**
   * Suspend the GuiComponent and all its child Components (Makes the GuiComponent invisible and removes mouse listeners.).
   *
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

  /**
   * Toggle this GuiComponent's selection.
   */
  public void toggleSelection() {
    this.setSelected(!this.isSelected);
  }

  /**
   * Gets the click consumer list.
   *
   * @return the click consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getClickConsumer() {
    return this.clickConsumer;
  }

  /**
   * Gets the hover consumer list.
   *
   * @return the hover consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getHoverConsumer() {
    return this.hoverConsumer;
  }

  /**
   * Gets the mouse dragged consumer list.
   *
   * @return the mouse dragged consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMouseDraggedConsumer() {
    return this.mouseDraggedConsumer;
  }

  /**
   * Gets the mouse enter consumer list.
   *
   * @return the mouse enter consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMouseEnterConsumer() {
    return this.mouseEnterConsumer;
  }

  /**
   * Gets the mouse leave consumer list.
   *
   * @return the mouse leave consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMouseLeaveConsumer() {
    return this.mouseLeaveConsumer;
  }

  /**
   * Gets the mouse moved consumer list.
   *
   * @return the mouse moved consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMouseMovedConsumer() {
    return this.mouseMovedConsumer;
  }

  /**
   * Gets the mouse pressed consumer list.
   *
   * @return the mouse pressed consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMousePressedConsumer() {
    return this.mousePressedConsumer;
  }

  /**
   * Gets the mouse released consumer list.
   *
   * @return the mouse released consumer list
   */
  protected List<Consumer<ComponentMouseEvent>> getMouseReleasedConsumer() {
    return this.mouseReleasedConsumer;
  }

  /**
   * Gets the mouse wheel consumer list.
   *
   * @return the mouse wheel consumer list
   */
  protected List<Consumer<ComponentMouseWheelEvent>> getMouseWheelConsumer() {
    return this.mouseWheelConsumer;
  }

  /**
   * Initialize child components.
   */
  protected void initializeComponents() {
    // nothing to do in the base class
  }

  /**
   * Check if a Mouse event should be forwarded.
   *
   * @param e
   *          the mouse event
   * @return true, if the Mouse event should be forwarded
   */
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isForwardMouseEvents() && this.isVisible() && this.isEnabled() && !this.isSuspended() && e != null && this.getBoundingBox().contains(e.getPoint());
  }

  /**
   * Render this GuiComponent's text.
   *
   * @param g
   *          the <code>Graphics2D</code> object used for drawing
   */
  private void renderText(final Graphics2D g) {
    if (this.getText() == null || this.getText().isEmpty()) {
      return;
    }

    final FontMetrics fm = g.getFontMetrics();

    double defaultTextX;
    final double defaultTextY = fm.getAscent() + (this.getHeight() - (fm.getAscent() + fm.getDescent())) / 2;
    switch (this.getTextAlign()) {
    case LEFT:
      defaultTextX = this.getHorizontalTextMargin();
      break;
    case RIGHT:
      defaultTextX = this.getWidth() - this.getHorizontalTextMargin() - fm.stringWidth(this.getTextToRender(g));
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

    boolean antialiasing = this.getAppearance().getTextAntialiasing();
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