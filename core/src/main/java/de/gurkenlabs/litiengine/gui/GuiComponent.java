package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.tweening.TweenType;
import de.gurkenlabs.litiengine.tweening.Tweenable;
import de.gurkenlabs.litiengine.util.ColorHelper;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.Shape;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;
import java.awt.geom.RoundRectangle2D;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * The abstract Class GuiComponent provides all properties and methods needed for screens, built-in, and custom GUI
 * components such as buttons, sliders, etc... It includes mouse event handling, different hovering states and
 * appearances, and texts to be rendered.
 */
public abstract class GuiComponent
    implements MouseListener, MouseMotionListener, MouseWheelListener, IRenderable, Tweenable {

  protected static final Font ICON_FONT;
  private static int id = 0;

  static {
    final Font icon = Resources.fonts().get("fontello.ttf");
    ICON_FONT = icon != null ? icon.deriveFont(16f) : null;
  }

  private final List<Consumer<ComponentMouseEvent>> clickConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseDraggedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseEnterConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseLeaveConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseMovedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mousePressedConsumer;
  private final List<Consumer<ComponentMouseEvent>> mouseReleasedConsumer;
  private final List<Consumer<ComponentMouseWheelEvent>> mouseWheelConsumer;
  private final List<Consumer<ComponentMouseEvent>> hoverConsumer;
  private final List<Consumer<String>> textChangedConsumer;

  private final Collection<ComponentRenderListener> renderListeners = ConcurrentHashMap.newKeySet();
  private final Collection<ComponentRenderedListener> renderedListeners = ConcurrentHashMap.newKeySet();

  private final int componentId;
  private final Appearance appearance;
  private final Appearance hoveredAppearance;

  private final List<GuiComponent> components;
  private final Appearance disabledAppearance;

  private boolean enabled;
  private Font font;
  private boolean forwardMouseEvents = true;
  private double width;
  private double height;

  private Sound hoverSound;
  private boolean textAntialiasing;
  private boolean textShadow;

  private Color textShadowColor;
  private float textShadowRadius;

  private boolean isHovered;
  private boolean isPressed;
  private boolean isSelected;
  private String name;
  private boolean suspended;
  private Object tag;
  private String text;
  private Align textAlign;
  private Valign textValign;
  private boolean automaticLineBreaks;
  private int textAngle = 0;

  private double textX;
  private double textY;
  private boolean visible;
  private Point2D location;
  private Rectangle2D boundingBox;

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
    this.appearance.onChange(
        app -> {
          for (final GuiComponent child : this.getComponents()) {
            child.getAppearance().update(this.getAppearance());
          }
        });

    this.hoveredAppearance = new Appearance();
    this.hoveredAppearance.update(GuiProperties.getDefaultAppearanceHovered());
    this.hoveredAppearance.onChange(
        app -> {
          for (final GuiComponent child : this.getComponents()) {
            child.getAppearanceHovered().update(this.getAppearanceHovered());
          }
        });

    this.disabledAppearance = new Appearance();
    this.disabledAppearance.update(GuiProperties.getDefaultAppearanceDisabled());
    this.disabledAppearance.onChange(
        app -> {
          for (final GuiComponent child : this.getComponents()) {
            child.getAppearanceDisabled().update(this.getAppearanceDisabled());
          }
        });

    setTextAlign(GuiProperties.getDefaultTextAlign());
    setTextValign(GuiProperties.getDefaultTextValign());

    setTextAntialiasing(GuiProperties.getDefaultTextAntialiasing());
    setTextShadow(GuiProperties.getDefaultTextShadow());
    setTextShadowColor(GuiProperties.getDefaultTextShadowColor());
    setTextShadowRadius(GuiProperties.getDefaultTextShadowRadius());

    this.componentId = ++id;
    this.location = new Point2D.Double(x, y);
    setDimension(width, height);
    setFont(GuiProperties.getDefaultFont());
    setSelected(false);
    setEnabled(true);
    initializeComponents();
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
    if (boundingBox != null) {
      return boundingBox;
    }

    this.boundingBox = new Rectangle2D.Double(getX(), getY(), getWidth(), getHeight());
    return boundingBox;
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
    return location;
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
   * Gets the entire Text associated with this GuiComponent. Parts of the Text may get cropped and can therefore be
   * invisible. To retrieve only the visible part of the text, use {@code GuiComponent.getTextToRender(Graphics2D g)}.
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
    return this.textAlign;
  }

  /**
   * Gets the vertical text alignment.
   *
   * @return the vertical text alignment
   */
  public Valign getTextValign() {
    return this.textValign;
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
   * Check whether text antialiasing is activated.
   *
   * @return true, if this GuiComponent is currently configured to draw its text with antialiasing.
   */
  public boolean hasTextAntialiasing() {
    return this.textAntialiasing;
  }

  /**
   * Check whether text shadow is activated.
   *
   * @return true, if this GuiComponent is currently configured to draw a shadow below its text.
   */
  public boolean hasTextShadow() {
    return this.textShadow;
  }

  public Color getTextShadowColor() {
    return textShadowColor;
  }

  public void setTextShadowColor(Color textShadowColor) {
    this.textShadowColor = textShadowColor;
  }

  public float getTextShadowRadius() {
    return textShadowRadius;
  }

  public void setTextShadowRadius(float textShadowRadius) {
    this.textShadowRadius = textShadowRadius;
  }

  public boolean hasAutomaticLineBreaks() {
    return this.automaticLineBreaks;
  }

  /**
   * Gets only the non-cropped bits of Text visible on this GuiComponent.m To retrieve only the entire text associated
   * with this GuiComponent, use {@code GuiComponent.getText()}.
   *
   * @param g
   *          The graphics object to render on.
   * @return the text to render
   */
  public String getTextToRender(final Graphics2D g) {
    if (this.getText() == null) {
      return "";
    } else if (hasAutomaticLineBreaks()) {
      return getText();
    }
    final FontMetrics fm = g.getFontMetrics();
    String newText = getText();

    while (newText.length() > 1 && fm.stringWidth(newText) >= getWidth()) {
      newText = newText.substring(1);
    }
    return newText;
  }

  /**
   * Gets the text X coordinate.
   *
   * @return the text X
   */
  public double getTextX() {
    return textX;
  }

  /**
   * Gets the text Y coordinate.
   *
   * @return the text Y
   */
  public double getTextY() {
    return textY;
  }

  /**
   * Gets the width of this GuiComponent.
   *
   * @return the width
   */
  public double getWidth() {
    return width;
  }

  /**
   * Gets the x coordinate of this GuiComponent.
   *
   * @return the x coordinate
   */
  public double getX() {
    return getLocation().getX();
  }

  /**
   * Gets the y coordinate of this GuiComponent.
   *
   * @return the y coordinate
   */
  public double getY() {
    return getLocation().getY();
  }

  /**
   * Checks if the GuiComponent is enabled.
   *
   * @return true, if is enabled
   */
  public boolean isEnabled() {
    return enabled;
  }

  /**
   * Checks if mouse events are being forwarded by this GuiComponent.
   *
   * @return true, the GuiComponent forwards mouse events
   */
  public boolean isForwardMouseEvents() {
    return forwardMouseEvents;
  }

  /**
   * Checks if the cursor bounding box intersects with this GuiComponent's bounding box.
   *
   * @return true, if the GuiComponent is hovered
   */
  public boolean isHovered() {
    return isHovered;
  }

  /**
   * Checks if the mouse button is currently being pressed on this GuiComponent.
   *
   * @return true, if the mouse is currently pressed on the GuiComponent
   */
  public boolean isPressed() {
    return isPressed;
  }

  /**
   * Checks if the GuiComponent is currently selected.
   *
   * @return true, if the GuiComponent is selected
   */
  public boolean isSelected() {
    return isSelected;
  }

  /**
   * Checks if the GuiComponent is currently suspended.
   *
   * @return true, if the GuiComponent is suspended
   */
  public boolean isSuspended() {
    return suspended;
  }

  /**
   * Checks if the GuiComponent is currently visible.
   *
   * @return true, if the GuiComponent is visible
   */
  public boolean isVisible() {
    return visible;
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
    if (!isForwardMouseEvents()) {
      return;
    }

    if (!mouseEventShouldBeForwarded(e)) {
      this.isHovered = false;
      return;
    }

    this.isHovered = true;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    getHoverConsumer().forEach(consumer -> consumer.accept(event));
    if (getHoverSound() != null) {
      Game.audio().playSound(getHoverSound());
    }

    this.getMouseEnterConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseExited(final MouseEvent e) {
    if (!isForwardMouseEvents()) {
      return;
    }

    this.isHovered = false;
    this.isPressed = false;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    getMouseLeaveConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseMoved(final MouseEvent e) {
    if (!mouseEventShouldBeForwarded(e) && isHovered()) {
      mouseExited(e);
      return;
    }

    // also throw enter event if the mouse did not hover the component
    // before
    if (!isHovered()) {
      mouseEntered(e);
    }

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    getMouseMovedConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mousePressed(final MouseEvent e) {
    if (!mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = true;
    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);
    getMousePressedConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseReleased(final MouseEvent e) {
    if (!mouseEventShouldBeForwarded(e)) {
      return;
    }

    this.isPressed = false;

    final ComponentMouseEvent event = new ComponentMouseEvent(e, this);

    // TODO: check if this should really call the clicked consumers...
    getClickConsumer().forEach(consumer -> consumer.accept(event));
    getMouseReleasedConsumer().forEach(consumer -> consumer.accept(event));
  }

  @Override
  public void mouseWheelMoved(final MouseWheelEvent e) {
    getMouseWheelConsumer().forEach(consumer -> consumer.accept(new ComponentMouseWheelEvent(e, this)));
  }

  /**
   * Add a callback that is being executed if this GuiComponent is clicked once.
   *
   * @param callback
   *          the callback
   */
  public void onClicked(final Consumer<ComponentMouseEvent> callback) {
    if (!getClickConsumer().contains(callback)) {
      getClickConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if this GuiComponent is hovered with the mouse.
   *
   * @param callback
   *          the callback
   */
  public void onHovered(final Consumer<ComponentMouseEvent> callback) {
    if (!getHoverConsumer().contains(callback)) {
      getHoverConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is pressed and moving around while within the bounds of this
   * GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseDragged(final Consumer<ComponentMouseEvent> callback) {
    if (!getMouseDraggedConsumer().contains(callback)) {
      getMouseDraggedConsumer().add(callback);
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
    if (!getMouseLeaveConsumer().contains(callback)) {
      getMouseLeaveConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is moving around while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseMoved(final Consumer<ComponentMouseEvent> callback) {
    if (!getMouseMovedConsumer().contains(callback)) {
      getMouseMovedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse is continually pressed while within the bounds of this
   * GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMousePressed(final Consumer<ComponentMouseEvent> callback) {
    if (!getMousePressedConsumer().contains(callback)) {
      getMousePressedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse button is released while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseReleased(final Consumer<ComponentMouseEvent> callback) {
    if (!getMouseReleasedConsumer().contains(callback)) {
      getMouseReleasedConsumer().add(callback);
    }
  }

  /**
   * Add a callback that is being executed if the mouse wheel is scrolled while within the bounds of this GuiComponent.
   *
   * @param callback
   *          the callback
   */
  public void onMouseWheelScrolled(final Consumer<ComponentMouseWheelEvent> callback) {
    if (!getMouseWheelConsumer().contains(callback)) {
      getMouseWheelConsumer().add(callback);
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

  public void addRenderListener(final ComponentRenderListener listener) {
    this.renderListeners.add(listener);
  }

  public void removeListener(final ComponentRenderListener listener) {
    this.renderListeners.remove(listener);
  }

  public void addRenderedListener(final ComponentRenderedListener listener) {
    this.renderedListeners.add(listener);
  }

  public void removeListener(final ComponentRenderedListener listener) {
    this.renderedListeners.remove(listener);
  }

  /**
   * Prepare the GuiComponent and all its child Components (Makes the GuiComponent visible and adds mouse listeners.).
   * This is, for example, done right before switching to a new screen.
   */
  public void prepare() {
    this.suspended = false;
    this.visible = true;
    Input.mouse().addMouseListener(this);
    Input.mouse().onWheelMoved(this);
    Input.mouse().addMouseMotionListener(this);
    for (final GuiComponent component : this.getComponents()) {
      component.prepare();
    }
  }

  /**
   * Note: If you override this and are modifying swing components, be sure you are in the AWT thread when you do so!
   */
  @Override
  public void render(final Graphics2D g) {
    if (this.isSuspended() || !this.isVisible()) {
      return;
    }

    for (ComponentRenderListener listener : this.renderListeners) {
      if (!listener.canRender(this)) {
        return;
      }
    }

    final ComponentRenderEvent event = new ComponentRenderEvent(g, this);
    for (ComponentRenderListener listener : this.renderListeners) {
      listener.rendering(event);
    }

    Shape clip = g.getClip();
    g.clip(this.getShape());

    if (!getCurrentAppearance().isTransparentBackground()) {
      g.setPaint(getCurrentAppearance().getBackgroundPaint(this.getWidth(), this.getHeight()));
      ShapeRenderer.render(g, getBoundingBox());
    }

    g.setColor(getCurrentAppearance().getForeColor());
    g.setFont(this.getFont());

    this.renderText(g);

    g.setClip(clip);
    if (getCurrentAppearance().getBorderColor() != null
        && getCurrentAppearance().getBorderStyle() != null) {
      g.setColor(getCurrentAppearance().getBorderColor());
      ShapeRenderer.renderOutline(g, getBoundingBox(), getCurrentAppearance().getBorderStyle());
    }
    for (final GuiComponent component : this.getComponents()) {
      if (!component.isVisible() || component.isSuspended()) {
        continue;
      }

      component.render(g);
    }

    for (ComponentRenderListener listener : this.renderListeners) {
      listener.rendered(event);
    }

    for (ComponentRenderedListener listener : this.renderedListeners) {
      listener.rendered(event);
    }

    if (Game.config().debug().renderGuiComponentBoundingBoxes()) {
      g.setColor(Color.RED);
      ShapeRenderer.renderOutline(g, getBoundingBox());
    }
  }

  @Override
  public float[] getTweenValues(TweenType tweenType) {
    switch (tweenType) {
      case LOCATION_X:
        return new float[] {(float) this.getX()};
      case LOCATION_Y:
        return new float[] {(float) this.getY()};
      case LOCATION_XY:
        return new float[] {(float) this.getX(), (float) this.getY()};
      case SIZE_WIDTH:
        return new float[] {(float) this.getWidth()};
      case SIZE_HEIGHT:
        return new float[] {(float) this.getHeight()};
      case SIZE_BOTH:
        return new float[] {(float) this.getWidth(), (float) this.getHeight()};
      case ANGLE:
        return new float[] {this.getTextAngle()};
      case FONTSIZE:
        return new float[] {this.getFont().getSize2D()};
      case OPACITY:
        Color bg1 = this.getCurrentAppearance().getBackgroundColor1();
        Color bg2 = this.getCurrentAppearance().getBackgroundColor2();
        Color fore = this.getCurrentAppearance().getForeColor();
        Color shadow = this.getTextShadowColor();
        Color border = this.getCurrentAppearance().getBorderColor();
        return new float[] {
            bg1 == null ? 0 : bg1.getAlpha(),
            bg2 == null ? 0 : bg2.getAlpha(),
            fore == null ? 0 : fore.getAlpha(),
            shadow == null ? 0 : shadow.getAlpha(),
            border == null ? 0 : border.getAlpha()
        };
      default:
        return Tweenable.super.getTweenValues(tweenType);
    }
  }

  @Override
  public void setTweenValues(TweenType tweenType, float[] newValues) {
    switch (tweenType) {
      case LOCATION_X -> this.setX(newValues[0]);
      case LOCATION_Y -> this.setY(newValues[0]);
      case LOCATION_XY -> {
        this.setX(newValues[0]);
        this.setY(newValues[1]);
      }
      case SIZE_WIDTH -> this.setWidth(newValues[0]);
      case SIZE_HEIGHT -> this.setHeight(newValues[0]);
      case SIZE_BOTH -> {
        this.setWidth(newValues[0]);
        this.setHeight(newValues[1]);
      }
      case ANGLE -> this.setTextAngle(Math.round(newValues[0]));
      case FONTSIZE -> this.setFontSize(newValues[0]);
      case OPACITY -> {
        Color bg1 = this.getCurrentAppearance().getBackgroundColor1();
        Color bg2 = this.getCurrentAppearance().getBackgroundColor2();
        Color fore = this.getCurrentAppearance().getForeColor();
        Color border = this.getCurrentAppearance().getBorderColor();
        getCurrentAppearance()
            .setBackgroundColor1(
                bg1 == null ? null : ColorHelper.getTransparentVariant(bg1, (int) newValues[0]));
        getCurrentAppearance()
            .setBackgroundColor2(
                bg2 == null ? null : ColorHelper.getTransparentVariant(bg2, (int) newValues[1]));
        getCurrentAppearance()
            .setForeColor(
                fore == null ? null : ColorHelper.getTransparentVariant(fore, (int) newValues[2]));
        setTextShadowColor(
            getTextShadowColor() == null
                ? null
                : ColorHelper.getTransparentVariant(getTextShadowColor(), (int) newValues[3]));
        getCurrentAppearance()
            .setBorderColor(
                border == null
                    ? null
                    : ColorHelper.getTransparentVariant(border, (int) newValues[4]));
      }
      default -> Tweenable.super.setTweenValues(tweenType, newValues);
    }
  }

  public RectangularShape getShape() {
    float radius = this.getCurrentAppearance().getBorderRadius();
    if (radius == 0f) {
      return this.getBoundingBox();
    }
    return new RoundRectangle2D.Double(
        this.getX(),
        this.getY(),
        this.getWidth(),
        this.getHeight(),
        this.getCurrentAppearance().getBorderRadius(),
        this.getCurrentAppearance().getBorderRadius());
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
    setWidth(width);
    setHeight(height);
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
    if (this.font == null) {
      return;
    }
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
    this.boundingBox = null; // trigger recreation in next boundingBox getter call
  }

  /**
   * Sets the "hovered" property on this GuiComponent.
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
    setLocation(new Point2D.Double(x, y));
  }

  /**
   * Sets this GuiComponent's location.
   *
   * @param location
   *          the new location
   */
  public void setLocation(final Point2D location) {
    final double deltaX = location.getX() - getX();
    final double deltaY = location.getY() - getY();

    this.location = location;
    this.boundingBox = null; // trigger recreation in next boundingBox getter call
    for (final GuiComponent component : getComponents()) {
      component.setLocation(new Point2D.Double(component.getX() + deltaX, component.getY() + deltaY));
    }
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
  }

  /**
   * Sets the {@link RenderingHints#KEY_TEXT_ANTIALIASING} settings for the rendered text.
   *
   * @param antialiasing
   *          Either {@link RenderingHints#VALUE_TEXT_ANTIALIAS_ON} or {@link RenderingHints#VALUE_TEXT_ANTIALIAS_OFF}
   */
  public void setTextAntialiasing(boolean antialiasing) {
    this.textAntialiasing = antialiasing;
  }

  public void setAutomaticLineBreaks(boolean automaticLineBreaks) {
    this.automaticLineBreaks = automaticLineBreaks;
  }

  /**
   * Sets the horizontal text alignment.
   *
   * @param textAlign
   *          the new text align
   */
  public void setTextAlign(final Align textAlign) {
    this.textAlign = textAlign;
  }

  /**
   * Sets the vertical text alignment.
   *
   * @param textValign
   *          the new text align
   */
  public void setTextValign(final Valign textValign) {
    this.textValign = textValign;
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
    this.textShadow = drawTextShadow;
    for (final GuiComponent comp : this.getComponents()) {
      comp.setTextShadow(drawTextShadow);
    }
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
    this.boundingBox = null; // trigger recreation in next boundingBox getter call
  }

  /**
   * Sets the GuiComponent's x coordinate.
   *
   * @param x
   *          the new x coordinate
   */
  public void setX(final double x) {
    setLocation(x, getY());
  }

  /**
   * Sets the GuiComponent's y coordinate.
   *
   * @param y
   *          the new y coordinate
   */
  public void setY(final double y) {
    setLocation(getX(), y);
  }

  /**
   * Suspend the GuiComponent and all its child Components (Makes the GuiComponent invisible and removes mouse
   * listeners.).
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
   * Toggle this GuiComponent's suspension state. If it's suspended, prepare it. If it's prepared, suspend it.
   */
  public void toggleSuspension() {
    if (!this.isSuspended()) {
      this.suspend();
    } else {
      this.prepare();
    }
  }

  public Appearance getCurrentAppearance() {
    if (!this.isEnabled()) {
      return this.getAppearanceDisabled();
    }
    return this.isHovered() ? this.getAppearanceHovered() : this.getAppearance();
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
    return this.isForwardMouseEvents()
        && this.isVisible()
        && this.isEnabled()
        && !this.isSuspended()
        && e != null
        && this.getBoundingBox().contains(e.getPoint());
  }

  /**
   * Render this GuiComponent's text.
   *
   * @param g
   *          the {@code Graphics2D} object used for drawing
   */
  private void renderText(final Graphics2D g) {
    if (this.getText() == null || this.getText().isEmpty()) {
      return;
    }

    final FontMetrics fm = g.getFontMetrics();

    double textWidth = fm.stringWidth(this.getTextToRender(g));
    double textHeight = (double) fm.getAscent() + fm.getDescent();

    double xCoord =
        this.getTextAlign() != null
            ? this.getX() + this.getTextAlign().getLocation(this.getWidth(), textWidth)
            : this.getTextX();
    double yCoord =
        this.getTextValign() != null
            ? this.getY() + this.getTextValign().getLocation(this.getHeight(), textHeight)
            : this.getTextY();
    if (this.getTextAngle() == 0) {
      if (this.hasTextShadow()) {
        TextRenderer.renderWithOutline(
            g,
            this.getTextToRender(g),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            this.getTextShadowColor(),
            this.getTextShadowRadius(),
            this.getTextAlign(),
            this.getTextValign(),
            this.hasTextAntialiasing());
      } else {
        TextRenderer.renderWithLinebreaks(
            g,
            this.getTextToRender(g),
            this.getTextAlign(),
            this.getTextValign(),
            this.getX(),
            this.getY(),
            this.getWidth(),
            this.getHeight(),
            this.hasTextAntialiasing());
      }
    } else if (this.getTextAngle() == 90) {
      TextRenderer.renderRotated(
          g,
          this.getTextToRender(g),
          xCoord,
          yCoord - fm.stringWidth(this.getTextToRender(g)),
          this.getTextAngle(),
          this.hasTextAntialiasing());
    } else {
      TextRenderer.renderRotated(
          g,
          this.getTextToRender(g),
          xCoord,
          yCoord,
          this.getTextAngle(),
          this.hasTextAntialiasing());
    }
  }
}
