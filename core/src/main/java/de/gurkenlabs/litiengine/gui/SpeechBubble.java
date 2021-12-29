package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.graphics.ShapeRenderer;
import de.gurkenlabs.litiengine.sound.Sound;

import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;

/**
 * A SpeechBubble is a GuiComponent with a given text that is pinned to an entity and moves with it.
 * After initializing the speech bubble, you can start it separately, which will add the component to the current screen, register it for updates and make it visible on screen.
 * Stopping the speech bubble will remove the component and deregister its updates.
 */
public class SpeechBubble extends GuiComponent implements IUpdateable {
  private int displayTime = GuiProperties.getDefaultSpeechBubbleDisplayTime();
  private long startedTick;
  private final IEntity entity;
  private Align boxAlign;
  private boolean renderTriangle;
  private Path2D triangle;
  private double triangleSize;
  private double entityCenterX;
  private int typeDelay;
  private int textIndex;
  private Sound typeSound;
  private long lastTypeTick;
  private final String totalText;

  /**
   * Instantiates a new speech bubble.
   *
   * @param entity the entity to which the Speech bubble will be pinned
   * @param text   the text which will appear in the speech bubble
   */
  public SpeechBubble(IEntity entity, String text) {
    this(entity, entity.getWidth() * 4 * Game.world().camera().getRenderScale(), entity.getHeight() * 2 * Game.world().camera().getRenderScale(), 3000, text);
  }

  /**
   * Instantiates a new Speech bubble.
   *
   * @param entity      the entity to which the Speech bubble will be pinned
   * @param width       the width of the text box
   * @param height      the height of the text box
   * @param displayTime the display time in milliseconds
   * @param text        the text which will appear in the speech bubble
   */
  public SpeechBubble(IEntity entity, double width, double height, int displayTime, String text) {
    super(Game.world().camera().getViewportDimensionCenter(entity).getX() * Game.world().camera().getRenderScale() - width / 2d,
        Game.world().camera().getViewportLocation(entity).getY() * Game.world().camera().getRenderScale() - height, width, height);
    this.entity = entity;
    this.totalText = text;
    setAutomaticLineBreaks(true);
    setForwardMouseEvents(false);
    setRenderTriangle(true);
    setBoxAlign(GuiProperties.getDefaultTextAlign());
    setTriangleSize(getWidth() * 1 / 10d);
    setDisplayTime(displayTime);
    setTypeDelay((int) (getDisplayTime() * 0.5 / totalText.length()));
  }

  /**
   * Gets the entity to which this speech bubble is pinned.
   *
   * @return the entity
   */
  public IEntity getEntity() {
    return entity;
  }

  /**
   * Gets the duration in milliseconds for which this speech bubble will be active.
   *
   * @return the display time in milliseconds
   */
  public int getDisplayTime() {
    return displayTime;
  }

  /**
   * Sets the duration in milliseconds for which this speech bubble will be active.
   *
   * @param displayTime the display time in milliseconds
   */
  public void setDisplayTime(int displayTime) {
    this.displayTime = displayTime;
  }

  /**
   * Start the speech bubble.
   * This will add it to the current Screen's components, make it visible and register it for updates in the Game loop.
   * After the display time has elapsed, the speech bubble will be stopped automatically.
   */
  public void start() {
    startedTick = Game.time().now();
    Game.screens().current().getComponents().add(this);
    prepare();
    Game.loop().attach(this);
  }

  /**
   * Stop the speech bubble.
   * This will remove it from the current Screen's components, make it invisible and deregister it from updates in the Game loop.
   */
  public void stop() {
    Game.screens().current().getComponents().remove(this);
    suspend();
    Game.loop().detach(this);
  }

  @Override
  public void render(Graphics2D g) {
    if (isRenderingTriangle()) {
      if (!getAppearance().isTransparentBackground()) {
        g.setColor(getAppearance().getBackgroundColor1());
        ShapeRenderer.render(g, getTriangle());
      }
      g.setColor(getCurrentAppearance().getBorderColor());
      ShapeRenderer.renderOutline(g, getTriangle(), getCurrentAppearance().getBorderStyle());
    }
    super.render(g);
  }

  /**
   * Gets the horizontal speech bubble alignment that dictates its position relative to the entity center point.
   *
   * @return the box Align
   */
  public Align getBoxAlign() {
    return boxAlign;
  }

  /**
   * Sets the horizontal speech bubble alignment that dictates its position relative to the entity center point.
   *
   * @param boxAlign the box Align
   */
  public void setBoxAlign(Align boxAlign) {
    this.boxAlign = boxAlign;
  }

  @Override public void update() {
    if (Game.time().since(startedTick) >= getDisplayTime()) {
      stop();
      return;
    }
    this.entityCenterX = Game.world().camera().getViewportDimensionCenter(getEntity()).getX() * Game.world().camera().getRenderScale();
    switch (getBoxAlign()) {
    case CENTER, CENTER_LEFT, CENTER_RIGHT -> setX(entityCenterX - getBoxAlign().getValue(getWidth()));
    case LEFT -> setX(entityCenterX - getBoxAlign().getValue(getWidth()) - getTriangleSize() / 2d);
    case RIGHT -> setX(entityCenterX - getBoxAlign().getValue(getWidth()) + getTriangleSize() / 2d);
    default -> throw new IllegalStateException("Unexpected value: " + getBoxAlign());
    }
    setY(Game.world().camera().getViewportLocation(getEntity()).getY() * Game.world().camera().getRenderScale() - (getHeight() + getTriangleSize()));
    type();
  }

  /**
   * Checks if the triangle indicator is active.
   *
   * @return true, if the speech bubble is rendering a triangle indicator on the bottom.
   */
  public boolean isRenderingTriangle() {
    return renderTriangle;
  }

  /**
   * Sets the visibility status of the triangle indicator.
   *
   * @param renderTriangle if true, the triangle will be visible.
   */
  public void setRenderTriangle(boolean renderTriangle) {
    this.renderTriangle = renderTriangle;
  }

  @Override public void setLocation(Point2D location) {
    this.triangle = null; // trigger recreation in next boundingBox getter call
    super.setLocation(location);
  }

  @Override public void setWidth(double width) {
    this.triangle = null; // trigger recreation in next boundingBox getter call
    super.setWidth(width);
  }

  @Override public void setHeight(double height) {
    this.triangle = null; // trigger recreation in next boundingBox getter call
    super.setHeight(height);
  }

  /**
   * Gets the triangle indicator's shape, translated to the correct location on screen.
   *
   * @return A Path2D object representing the triangle indicator.
   */
  public Path2D getTriangle() {
    if (triangle != null) {
      return triangle;
    }
    this.triangle = new Path2D.Double();
    triangle.moveTo(0, 0);
    triangle.lineTo(getTriangleSize(), 0);
    triangle.lineTo(getTriangleSize() / 2d, getTriangleSize());
    triangle.closePath();

    AffineTransform at = new AffineTransform();
    at.translate(entityCenterX - getTriangleSize() / 2d, getY() + getHeight());
    triangle.transform(at);

    return triangle;
  }

  /**
   * Gets the triangle indicator size. Its size is identical for width and height.
   *
   * @return the triangle indicator size
   */
  public double getTriangleSize() {
    return triangleSize;
  }

  /**
   * Gets the triangle indicator size. Its size is identical for width and height.
   *
   * @param triangleSize the new triangle indicator size
   */
  public void setTriangleSize(double triangleSize) {
    this.triangle = null; // trigger recreation in next boundingBox getter call
    this.triangleSize = triangleSize;
  }

  /**
   * Updates the displayed text for a typewriter effect
   */
  private void type() {
    // display new text
    if (textIndex < totalText.length() && Game.time().since(lastTypeTick) > getTypeDelay()) {
      this.textIndex++;
      setText(totalText.substring(0, textIndex));
      this.lastTypeTick = Game.time().now();
      if (getTypeSound() != null) {
        Game.audio().playSound(getTypeSound(), getEntity());
      }
    }
  }

  /**
   * Gets type delay, which determines how fast the typewriter effect will be.
   *
   * @return the type delay in milliseconds
   */
  public int getTypeDelay() {
    return typeDelay;
  }

  /**
   * Sets type delay, which determines how fast the typewriter effect will be.
   *
   * @param typeDelay the type delay in milliseconds
   */
  public void setTypeDelay(int typeDelay) {
    this.typeDelay = typeDelay;
  }

  /**
   * Gets the sound that is played every time a new letter appears.
   *
   * @return the type sound
   */
  public Sound getTypeSound() {
    return typeSound;
  }

  /**
   * Sets the sound that is played every time a new letter appears.
   *
   * @param typeSound the type sound
   */
  public void setTypeSound(Sound typeSound) {
    this.typeSound = typeSound;
  }

  /**
   * Gets the total text that this Speech bubble will display.
   *
   * @return the total text
   */
  public String getTotalText() {
    return totalText;
  }
}
