package de.gurkenlabs.litiengine.environment;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Stroke;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.animation.NarratorPortraitAnimationController;
import de.gurkenlabs.litiengine.gui.FontLoader;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class Narrator implements IUpdateable, IRenderable {
  public static final int LAYOUT_LEFT = 0;
  public static final int LAYOUT_RIGHT = 1;

  public enum Emotion {
    NORMAL, ANGRY, SAD, SURPRISED, BORED, HAPPY, SILENT
  }

  private static final int DISPLAYTIME_MIN = 2000;
  private static final int DISPLAYTIME_PER_LETTER = 120;
  private int layout;

  private Image background;
  private Color backgroundColor = new Color(0, 0, 0, 100);
  private Color borderColor = new Color(0, 0, 0, 180);

  private double boxWidth, boxHeight;
  private String currentText;
  private long currentTextDisplayTime;
  private Queue<Character> currentTextQueue;
  private String displayedText;

  private Font font;
  private Color fontColor = Color.WHITE;
  private long lastCharPoll;
  private long lastTextDispay;
  private int letterTypeDelay = 30;

  private Emotion emotion;
  private String name;
  private boolean narrating;
  private double padding;
  private Point2D renderLocation;

  private Sound typingSound;
  private float portraitWidth, portraitHeight, textboxWidth, textBoxX, textBoxY, portraitX, portraitY;
  private NarratorPortraitAnimationController animationController;

  public Narrator(final IEnvironment environment, final String narratorName, int layout) {
    this.setLayout(layout);
    this.setName(narratorName);
    this.setEmotion(Emotion.NORMAL);
    this.setRenderLocation(new Point2D.Double(0, Game.getScreenManager().getResolution().getHeight() * (6 / 8.0)));
    this.setSize(Game.getScreenManager().getResolution().getWidth(), Game.getScreenManager().getResolution().getHeight() * (2 / 8.0));
    this.setFont(FontLoader.load("04B_11_.ttf").deriveFont((float) (this.getBoxHeight() / 6)));
    Game.getLoop().registerForUpdate(this);
    this.animationController = new NarratorPortraitAnimationController(this);
  }

  public Narrator(final IEnvironment environment, final String narratorName) {
    this(environment, narratorName, 0);
  }

  public void cancel() {
    this.narrating = false;
  }

  private void createBackgroundImage() {
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getBoxWidth(), (int) this.getBoxHeight());
    final Graphics2D g = img.createGraphics();
    g.setFont(this.getFont());

    final Rectangle2D bounds = new Rectangle2D.Double(0, 0, img.getWidth(), img.getHeight());

    g.setColor(this.getBackgroundColor());
    g.fill(bounds);

    g.setColor(this.getBorderColor());
    final Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getPadding() / 8)));
    g.draw(bounds);
    g.setStroke(oldStroke);
    g.dispose();

    this.setBackgroundImage(img);
  }

  public NarratorPortraitAnimationController getAnimationController() {
    return this.animationController;
  }

  public int getLayout() {
    return this.layout;
  }

  public Image getBackground() {
    return this.background;
  }

  public Color getBackgroundColor() {
    return this.backgroundColor;
  }

  public Image getBackgroundImage() {
    return this.background;
  }

  public Color getBorderColor() {
    return this.borderColor;
  }

  public double getBoxHeight() {
    return this.boxHeight;
  }

  public double getBoxWidth() {
    return this.boxWidth;
  }

  public String getCurrentText() {
    return this.currentText;
  }

  public Font getFont() {
    return this.font;
  }

  public Color getFontColor() {
    return this.fontColor;
  }

  public int getLetterTypeDelay() {
    return this.letterTypeDelay;
  }

  public String getName() {
    return this.name;
  }

  public double getPadding() {
    return this.padding;
  }

  public Point2D getRenderLocation() {
    return this.renderLocation;
  }

  public Sound getTypingSound() {
    return this.typingSound;
  }

  public Emotion getEmotion() {
    return this.emotion;
  }

  public boolean isNarrating() {
    return this.narrating;
  }

  public void narrate(final String text) {
    for (final Narrator otherNarrator : Game.getEnvironment().getNarrators()) {
      if (otherNarrator != this && otherNarrator.isNarrating()) {
        otherNarrator.cancel();
      }
    }
    this.narrating = true;
    this.currentText = text;
    this.currentTextDisplayTime = DISPLAYTIME_MIN + text.length() * DISPLAYTIME_PER_LETTER;
    this.currentTextQueue = new ConcurrentLinkedQueue<>();
    this.displayedText = "";
    for (int i = 0; i < this.currentText.length(); i++) {
      this.currentTextQueue.add(this.currentText.charAt(i));
    }
    this.lastTextDispay = Game.getLoop().getTicks();
  }

  @Override
  public void render(final Graphics2D g) {
    if (this.displayedText == null || this.displayedText.isEmpty() || !this.isNarrating()) {
      return;
    }
    g.drawImage(this.getBackgroundImage(), (int) this.getRenderLocation().getX(), (int) this.getRenderLocation().getY(), null);
    RenderEngine.renderImage(g, this.getBackgroundImage(), this.getRenderLocation());
    BufferedImage img = this.getAnimationController().getCurrentSprite();
    if (img != null) {
      img = ImageProcessing.scaleImage(img, (int) this.portraitWidth);
      RenderEngine.renderImage(g, img, new Point2D.Double(this.portraitX, this.portraitY));
    }
    g.setColor(this.getFontColor());
    final FontRenderContext frc = g.getFontRenderContext();

    final String text = this.displayedText;
    if (text == null || text.isEmpty()) {
      return;
    }

    final AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, this.font);
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    float textY = this.textBoxY;
    while (measurer.getPosition() < text.length()) {
      final TextLayout layout = measurer.nextLayout((float) (this.textboxWidth));

      textY += layout.getAscent();
      final float dx = layout.isLeftToRight() ? 0 : (float) (this.textboxWidth) - layout.getAdvance();
      layout.draw(g, textBoxX + dx, textY);
      textY += layout.getDescent() + layout.getLeading();
    }
  }

  public void setEmotion(Emotion emotion) {
    this.emotion = emotion;
  }

  public void setBackground(final Image background) {
    this.background = background;
  }

  public void setBackgroundColor(final Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public void setBackgroundImage(final BufferedImage img) {
    this.background = img;
  }

  public void setBorderColor(final Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setFont(final Font textFont) {
    this.font = textFont;
  }

  public void setFontColor(final Color fontColor) {
    this.fontColor = fontColor;
  }

  public void setLayout(final int newLayout) {
    this.layout = newLayout;
  }

  public void setLetterTypeDelay(final int letterTypeDelay) {
    this.letterTypeDelay = letterTypeDelay;
  }

  public void setName(final String name) {
    this.name = name;
  }

  public void setPadding(final double padding) {
    this.padding = padding;
    this.createBackgroundImage();
  }

  public void setRenderLocation(final Point2D renderLocation) {
    this.renderLocation = renderLocation;
  }

  public void setSize(final double width, final double height) {
    this.boxWidth = width;
    this.boxHeight = height;
    this.setPadding(this.getBoxWidth() / 20d);
    this.setupLayout();
  }

  public void setTypingSound(final Sound typingSound) {
    this.typingSound = typingSound;
  }

  @Override
  public void update(final IGameLoop loop) {
    if (this.currentText == null || !this.isNarrating()) {
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
      if (loop.getDeltaTime(this.lastCharPoll) > this.letterTypeDelay) {
        this.displayedText += this.currentTextQueue.poll();
        this.lastCharPoll = loop.getTicks();
        if (this.getTypingSound() != null) {
          Game.getSoundEngine().playSound(this.getTypingSound());
        }
      }
    }

    // continue displaying currently displayed text
  }

  private void setupLayout() {
    this.portraitHeight = (float) (this.getBoxHeight() - this.getPadding());
    this.portraitWidth = this.portraitHeight;
    this.textboxWidth = (float) (this.getPadding() * 16);
    this.portraitY = (float) (this.getRenderLocation().getY() + this.getPadding() / 2);
    this.textBoxY = this.portraitY;
    switch (this.getLayout()) {
    case LAYOUT_LEFT:
      this.portraitX = (float) (this.getRenderLocation().getX() + this.getPadding() / 2);
      this.textBoxX = (float) (this.portraitX + this.portraitWidth + this.getPadding() / 2);
      break;
    case LAYOUT_RIGHT:
      this.textBoxX = (float) (this.getRenderLocation().getX() + this.getPadding() / 2);
      this.portraitX = (float) (this.getRenderLocation().getX() + this.getBoxWidth() - this.getPadding() / 2 - this.portraitWidth);
      break;
    }
  }
}
