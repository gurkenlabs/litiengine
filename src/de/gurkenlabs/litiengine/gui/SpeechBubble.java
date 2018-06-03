package de.gurkenlabs.litiengine.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityProvider;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.litiengine.util.ImageProcessing;

public class SpeechBubble implements IUpdateable, IRenderable, IEntityProvider {
  public static final SpeechBubbleAppearance DEFAULT_APPEARANCE = new SpeechBubbleAppearance(Color.WHITE, new Color(16, 20, 19, 150), new Color(16, 20, 19), 4.0f);

  private static final Map<IEntity, SpeechBubble> activeSpeechBubbles = new ConcurrentHashMap<>();
  private static final int DISPLAYTIME_MIN = 2000;
  private static final int DISPLAYTIME_PER_LETTER = 120;
  private static final int LETTER_WRITE_DELAY = 30;
  private Font font;

  private static final double TRIANGLE_SIZE = 6;

  private final int currentTextDisplayTime;
  private final Queue<Character> currentTextQueue;
  private final SpeechBubbleAppearance appearance;

  private BufferedImage bubble;
  private String currentText;

  private String displayedText;
  private final IEntity entity;

  private long lastCharPoll;
  private long lastTextDispay;
  private float textBoxWidth;
  private Sound typeSound;
  private Point2D entityCenter;

  private SpeechBubble(final IEntity entity, final String text, SpeechBubbleAppearance appearance, Font font) {
    if (appearance == null) {
      this.appearance = DEFAULT_APPEARANCE;
    } else {
      this.appearance = appearance;
    }

    final SpeechBubble active = activeSpeechBubbles.get(entity);
    if (active != null) {
      active.cancel();
    }
    this.setFont(font);

    this.textBoxWidth = entity.getWidth() * 4;
    this.entity = entity;

    this.currentText = text;
    this.currentTextDisplayTime = DISPLAYTIME_MIN + text.length() * DISPLAYTIME_PER_LETTER;
    this.currentTextQueue = new ConcurrentLinkedQueue<>();
    this.displayedText = "";
    for (int i = 0; i < this.currentText.length(); i++) {
      this.currentTextQueue.add(this.currentText.charAt(i));
    }

    this.lastTextDispay = Game.getLoop().getTicks();
    this.createBubbleImage();
    Game.getEnvironment().add(this, RenderType.UI);
    Game.getRenderLoop().attach(this);
    activeSpeechBubbles.put(entity, this);
  }

  private SpeechBubble(final IEntity entity, final String text, final Sound typeSound, SpeechBubbleAppearance appearance, Font font) {
    this(entity, text, appearance, font);
    this.typeSound = typeSound;
  }

  public static SpeechBubble create(final IEntity entity, final String text, SpeechBubbleAppearance appearance, Font font) {
    return new SpeechBubble(entity, text, appearance, font);
  }

  public static SpeechBubble create(final IEntity entity, final String text) {
    return new SpeechBubble(entity, text, null, GuiProperties.getDefaultFont());
  }

  public static SpeechBubble create(final IEntity entity, final Font font, final String text) {
    SpeechBubbleAppearance app = new SpeechBubbleAppearance(DEFAULT_APPEARANCE.getForeColor(), DEFAULT_APPEARANCE.getBackgroundColor1(), DEFAULT_APPEARANCE.getBorderColor(), DEFAULT_APPEARANCE.getPadding());
    return new SpeechBubble(entity, text, app, font);
  }

  public static SpeechBubble create(final IEntity entity, final Font font, final String text, final Sound typeSound) {
    SpeechBubbleAppearance app = new SpeechBubbleAppearance(DEFAULT_APPEARANCE.getForeColor(), DEFAULT_APPEARANCE.getBackgroundColor1(), DEFAULT_APPEARANCE.getBorderColor(), DEFAULT_APPEARANCE.getPadding());
    return new SpeechBubble(entity, text, typeSound, app, font);
  }

  public static boolean isActive(final IEntity entity) {
    return activeSpeechBubbles.containsKey(entity);
  }

  @Override
  public IEntity getEntity() {
    return this.entity;
  }

  public SpeechBubbleAppearance getAppearance() {
    return this.appearance;
  }

  public Font getFont() {
    return this.font;
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.displayedText == null || this.displayedText.isEmpty() || !Game.getRenderEngine().canRender(this.entity)) {
      return;
    }

    final float deltaX = (float) (this.textBoxWidth / 2.0 + this.getAppearance().getPadding());
    final float deltaY = (float) ((this.getEntity().getHeight() / 2.0) + this.bubble.getHeight() + this.getAppearance().getPadding() + 1);

    final float startX = (float) (entityCenter.getX() - deltaX);
    final float startY = (float) (entityCenter.getY() - deltaY);
    RenderEngine.renderImage(g, this.bubble, new Point2D.Double(startX, startY));

    g.setColor(this.getAppearance().getForeColor());
    final FontRenderContext frc = g.getFontRenderContext();

    final String text = this.displayedText;
    final AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, this.getFont());
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);

    float y = startY + this.getAppearance().getPadding();
    float x = startX + this.getAppearance().getPadding();
    while (measurer.getPosition() < text.length()) {
      final TextLayout layout = measurer.nextLayout(this.textBoxWidth);

      y += layout.getAscent();
      final float dx = layout.isLeftToRight() ? 0 : this.textBoxWidth - layout.getAdvance();
      layout.draw(g, x + dx, y);
      y += layout.getDescent() + layout.getLeading();
    }

    if (Game.getConfiguration().debug().renderGuiComponentBoundingBoxes()) {
      g.setColor(Color.RED);
      Game.getRenderEngine().renderOutline(g, new Rectangle2D.Double(this.getEntity().getCenter().getX() - deltaX, this.getEntity().getCenter().getY() - deltaY, this.bubble.getWidth(), this.bubble.getHeight()));
    }
  }

  public void setFont(Font font) {
    this.font = font;
  }

  @Override
  public void update() {
    if (this.currentText == null) {
      Game.getEnvironment().removeRenderable(this);
      Game.getRenderLoop().detach(this);

      if (activeSpeechBubbles.containsKey(this.getEntity()) && activeSpeechBubbles.get(this.getEntity()).equals(this)) {
        activeSpeechBubbles.remove(this.getEntity());
      }

      return;
    }

    this.entityCenter = Game.getCamera().getViewPortLocation(this.getEntity().getCenter());

    // old text was displayed long enough
    if (this.lastTextDispay != 0 && Game.getLoop().getDeltaTime(this.lastTextDispay) > this.currentTextDisplayTime) {
      this.currentText = null;
      this.displayedText = null;
      this.lastTextDispay = 0;
      return;
    }

    // display new text
    if (!this.currentTextQueue.isEmpty() && Game.getLoop().getDeltaTime(this.lastCharPoll) > LETTER_WRITE_DELAY) {
      this.displayedText += this.currentTextQueue.poll();
      this.lastCharPoll = Game.getLoop().getTicks();
      if (this.typeSound != null) {
        Game.getSoundEngine().playSound(this.getEntity(), this.typeSound);
      }
    }

    // continue displaying currently displayed text
  }

  private void cancel() {
    Game.getEnvironment().removeRenderable(this);
    Game.getRenderLoop().detach(this);
    if (activeSpeechBubbles.get(this.getEntity()) != null && activeSpeechBubbles.remove(this.getEntity()).equals(this)) {
      activeSpeechBubbles.remove(this.getEntity());
    }
  }

  private void createBubbleImage() {
    final BufferedImage img = ImageProcessing.getCompatibleImage(500, 500);
    final Graphics2D g = img.createGraphics();
    g.setFont(this.getFont());
    final float stringWidth = g.getFontMetrics().stringWidth(this.currentText);
    if (stringWidth < this.textBoxWidth) {
      this.textBoxWidth = stringWidth;
    }

    final FontRenderContext frc = g.getFontRenderContext();
    final AttributedString styledText = new AttributedString(this.currentText);
    styledText.addAttribute(TextAttribute.FONT, this.getFont());
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    float y = 0;
    while (measurer.getPosition() < this.currentText.length()) {
      final TextLayout layout = measurer.nextLayout(this.textBoxWidth);
      y += layout.getAscent() + layout.getLeading() + layout.getDescent();
    }

    final Rectangle2D bounds = new Rectangle2D.Double(0, 0, this.textBoxWidth + 2 * this.getAppearance().getPadding(), y + 2 * this.getAppearance().getPadding());

    final Area ar = new Area(bounds);
    if (this.getAppearance().isRenderIndicator()) {
      // Build a path
      final GeneralPath path = new GeneralPath();
      path.moveTo(bounds.getWidth() / 2.0, bounds.getHeight());
      path.lineTo(bounds.getWidth() / 2.0, bounds.getHeight() + TRIANGLE_SIZE);
      path.lineTo(bounds.getWidth() / 2.0 + TRIANGLE_SIZE, bounds.getHeight());
      path.closePath();
      ar.add(new Area(path));
    }

    int width = ar.getBounds().width;
    int height = ar.getBounds().height;
    g.setPaint(this.getAppearance().getBackgroundPaint(width, height));
    g.fill(ar);

    g.setColor(this.getAppearance().getBorderColor());
    g.draw(ar);
    g.dispose();

    this.bubble = ImageProcessing.crop(img, ImageProcessing.CROP_ALIGN_LEFT, ImageProcessing.CROP_VALIGN_TOP, width + 1, height + 1);
  }
}
