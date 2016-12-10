package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Area;
import java.awt.geom.GeneralPath;
import java.awt.geom.Point2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class SpeechBubble implements IUpdateable, IRenderable {
  private static final int DISPLAYTIME_PER_LETTER = 120;
  private static final int DISPLAYTIME_MIN = 2000;
  private static final int LETTER_WRITE_DELAY = 30;
  private static final int PADDING = 4;
  private static final Color SPEAK_FONT_COLOR = Color.WHITE;
  private static final Color SPEAK_BACKGROUNDCOLOR = new Color(0, 0, 0, 80);
  private static final Color SPEAK_BORDERCOLOR = new Color(0, 0, 0, 160);

  private int textBoxWidth;
  private final IEntity entity;
  private final Font font;
  private Image bubble;
  private Sound typeSound;
  private int width;
  private int height;

  private long lastTextDispay;
  private final long currentTextDisplayTime;
  private String currentText;
  private String displayedText;
  private final Queue<Character> currentTextQueue;
  private long lastCharPoll;

  public SpeechBubble(final IEntity entity, final Font font, final String text, final Sound typeSound) {
    this(entity, font, text);
    this.typeSound = typeSound;
  }

  public SpeechBubble(final IEntity entity, final Font font, final String text) {
    this.textBoxWidth = (int) (entity.getWidth() * 4);
    this.entity = entity;
    this.font = font;

    this.currentText = text;
    this.currentTextDisplayTime = DISPLAYTIME_MIN + text.length() * DISPLAYTIME_PER_LETTER;
    this.currentTextQueue = new ConcurrentLinkedQueue<>();
    this.displayedText = "";
    for (int i = 0; i < this.currentText.length(); i++) {
      this.currentTextQueue.add(this.currentText.charAt(i));
    }

    this.lastTextDispay = Game.getLoop().getTicks();
    this.createBubbleImage();
    Game.getEnvironment().add(this, RenderType.OVERLAY);
    Game.getLoop().registerForUpdate(this);
  }

  private void createBubbleImage() {
    final int TRIANGLE_SIZE = 6;

    BufferedImage img = ImageProcessing.getCompatibleImage(500, 500);
    Graphics2D g = img.createGraphics();
    g.setFont(this.font);
    int stringWidth = g.getFontMetrics().stringWidth(this.currentText);
    if (stringWidth < this.textBoxWidth) {
      this.textBoxWidth = stringWidth;
    }

    final FontRenderContext frc = g.getFontRenderContext();
    final AttributedString styledText = new AttributedString(this.currentText);
    styledText.addAttribute(TextAttribute.FONT, this.font);
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    float y = 0;
    while (measurer.getPosition() < this.currentText.length()) {
      final TextLayout layout = measurer.nextLayout(this.textBoxWidth);
      y += layout.getAscent() + layout.getLeading() + 0.2;
    }

    final RoundRectangle2D bounds = new RoundRectangle2D.Double(0, 0, this.textBoxWidth + 2 * PADDING, y + 2 * PADDING, PADDING, PADDING);

    // Build a path
    GeneralPath path = new GeneralPath();
    path.moveTo(bounds.getWidth() / 2.0 - TRIANGLE_SIZE / 2.0, bounds.getHeight());
    path.lineTo(bounds.getWidth() / 2.0, bounds.getHeight() + TRIANGLE_SIZE);
    path.lineTo(bounds.getWidth() / 2.0 + TRIANGLE_SIZE / 2.0, bounds.getHeight());
    path.closePath();

    Area ar = new Area(bounds);
    ar.add(new Area(path));

    this.width = ar.getBounds().width;
    this.height = ar.getBounds().height;
    g.setColor(SPEAK_BACKGROUNDCOLOR);
    g.fill(ar);

    g.setColor(SPEAK_BORDERCOLOR);
    g.draw(ar);
    g.dispose();

    this.bubble = ImageProcessing.crop(img, ImageProcessing.CROP_ALIGN_LEFT, ImageProcessing.CROP_VALIGN_TOP, width + 1, height + 1);
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.displayedText == null || this.displayedText.isEmpty() || !Game.getRenderEngine().canRender(this.entity)) {
      return;
    }

    Point2D location = Game.getScreenManager().getCamera().getViewPortLocation(this.entity);
    RenderEngine.renderImage(g, this.bubble, new Point2D.Double(location.getX() + this.entity.getWidth() / 2.0 - this.textBoxWidth / 2.0 - PADDING, location.getY() - this.height - PADDING));

    g.setColor(SPEAK_FONT_COLOR);
    final FontRenderContext frc = g.getFontRenderContext();

    final String text = this.displayedText;
    final AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, this.font);
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    final float x = (float) Game.getScreenManager().getCamera().getViewPortLocation(this.entity).getX() + this.entity.getWidth() / 2.0f - this.textBoxWidth / 2.0f;
    float y = (float) Game.getScreenManager().getCamera().getViewPortLocation(this.entity).getY() - this.height;
    while (measurer.getPosition() < text.length()) {
      final TextLayout layout = measurer.nextLayout(this.textBoxWidth);

      y += layout.getAscent();
      final float dx = layout.isLeftToRight() ? 0 : this.textBoxWidth - layout.getAdvance();
      layout.draw(g, x + dx, y);
      y += layout.getDescent() + layout.getLeading();
    }
  }

  @Override
  public void update(final IGameLoop loop) {
    if (this.currentText == null) {
      Game.getEnvironment().removeRenderable(this);
      loop.unregisterFromUpdate(this);
      return;
    }

    // old text was displayed long enough
    if (this.lastTextDispay != 0 && loop.getDeltaTime(this.lastTextDispay) > this.currentTextDisplayTime) {
      this.currentText = null;
      this.displayedText = null;
      this.lastTextDispay = 0;
      return;
    }

    // display new text
    if (!this.currentTextQueue.isEmpty() && this.currentText != null) {
      if (loop.getDeltaTime(this.lastCharPoll) > LETTER_WRITE_DELAY) {
        this.displayedText += this.currentTextQueue.poll();
        this.lastCharPoll = loop.getTicks();
        if (this.typeSound != null) {
          Game.getSoundEngine().playSound(this.entity, this.typeSound);
        }
      }
    }

    // continue displaying currently displayed text
  }
}
