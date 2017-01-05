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
import de.gurkenlabs.litiengine.gui.FontLoader;
import de.gurkenlabs.litiengine.sound.Sound;
import de.gurkenlabs.util.image.ImageProcessing;

public class Narrator implements IUpdateable, IRenderable {

  private static final int DISPLAYTIME_PER_LETTER = 120;
  private static final int DISPLAYTIME_MIN = 2000;
  private int letterTypeDelay = 30;

  private Color fontColor = Color.WHITE;
  private Color backgroundColor = new Color(0, 0, 0, 100);
  private Color borderColor = new Color(0, 0, 0, 180);

  private Sound typingSound;
  private Font font;
  private Point2D renderLocation;
  private Image background;

  private double padding;
  private double boxWidth, boxHeight;
  private long lastTextDispay;
  private long currentTextDisplayTime;
  private String currentText;

  private String displayedText;
  private Queue<Character> currentTextQueue;
  private long lastCharPoll;

  private boolean narrating;

  public Narrator(IEnvironment environment) {
    this.setSize(Game.getScreenManager().getResolution().getWidth(), (double) (Game.getScreenManager().getResolution().getHeight() * (2 / 8.0)));
    this.setRenderLocation(new Point2D.Double(0, (double) (Game.getScreenManager().getResolution().getHeight() * (6 / 8.0))));
    this.setPadding((this.getBoxHeight() / 4));
    this.setFont(FontLoader.getGuiFont().deriveFont((float) (this.getPadding() * 0.7)));
    Game.getLoop().registerForUpdate(this);
  }

  private void createBackgroundImage() {
    BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getBoxWidth(), (int) this.getBoxHeight());
    Graphics2D g = img.createGraphics();
    g.setFont(this.getFont());

    final RoundRectangle2D bounds = new RoundRectangle2D.Double(0, 0, img.getWidth(), img.getHeight(), this.getPadding(), this.getPadding());

    g.setColor(this.getBackgroundColor());
    g.fill(bounds);

    g.setColor(this.getBorderColor());
    Stroke oldStroke = g.getStroke();
    g.setStroke(new BasicStroke((float) (this.getPadding() / 8)));
    g.draw(bounds);
    g.setStroke(oldStroke);
    g.dispose();

    this.setBackgroundImage(img);
  }

  @Override
  public void render(Graphics2D g) {
    if (this.displayedText == null || this.displayedText.isEmpty() || !this.isNarrating()) {
      return;
    }
    g.drawImage(getBackgroundImage(), (int) this.getRenderLocation().getX(), (int) this.getRenderLocation().getY(), null);
    RenderEngine.renderImage(g, this.getBackgroundImage(), this.getRenderLocation());
    g.setColor(this.getFontColor());
    final FontRenderContext frc = g.getFontRenderContext();

    final String text = this.displayedText;
    final AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, this.font);
    final AttributedCharacterIterator iterator = styledText.getIterator();
    final LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);
    final float x = (float) (this.getRenderLocation().getX() + this.getPadding() / 2);
    float y = (float) (this.getRenderLocation().getY() + this.getPadding() / 2);
    while (measurer.getPosition() < text.length()) {
      final TextLayout layout = measurer.nextLayout((float) (this.getBoxWidth() - this.getPadding() / 2));

      y += layout.getAscent();
      final float dx = layout.isLeftToRight() ? 0 : (float) (this.getBoxWidth() - this.getPadding() / 2) - layout.getAdvance();
      layout.draw(g, x + dx, y);
      y += layout.getDescent() + layout.getLeading();
    }
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
      if (loop.getDeltaTime(this.lastCharPoll) > letterTypeDelay) {
        this.displayedText += this.currentTextQueue.poll();
        this.lastCharPoll = loop.getTicks();
        if (this.getTypingSound() != null) {
          Game.getSoundEngine().playSound(this.getTypingSound());
        }
      }
    }

    // continue displaying currently displayed text
  }

  public void cancel() {
    this.narrating = false;
  }

  public void narrate(String text) {
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

  public Sound getTypingSound() {
    return this.typingSound;
  }

  public Image getBackgroundImage() {
    return this.background;
  }

  public String getCurrentText() {
    return this.currentText;
  }

  public double getPadding() {
    return this.padding;
  }

  public Point2D getRenderLocation() {
    return this.renderLocation;
  }

  public Font getFont() {
    return this.font;
  }

  public Color getBackgroundColor() {
    return this.backgroundColor;
  }

  public Color getBorderColor() {
    return this.borderColor;
  }

  public Image getBackground() {
    return this.background;
  }

  public Color getFontColor() {
    return this.fontColor;
  }

  public int getLetterTypeDelay() {
    return this.letterTypeDelay;
  }

  public double getBoxWidth() {
    return this.boxWidth;
  }

  public double getBoxHeight() {
    return this.boxHeight;
  }

  public boolean isNarrating() {
    return this.narrating;
  }

  public void setLetterTypeDelay(int letterTypeDelay) {
    this.letterTypeDelay = letterTypeDelay;
  }

  public void setBackgroundColor(Color backgroundColor) {
    this.backgroundColor = backgroundColor;
  }

  public void setBorderColor(Color borderColor) {
    this.borderColor = borderColor;
  }

  public void setFontColor(Color fontColor) {
    this.fontColor = fontColor;
  }

  public void setBackground(Image background) {
    this.background = background;
  }

  public void setSize(double x, double y) {
    this.boxWidth = x;
    this.boxHeight = y;
    this.createBackgroundImage();
  }

  public void setBackgroundImage(BufferedImage img) {
    this.background = img;
  }

  public void setPadding(double padding) {
    this.padding = padding;
    this.createBackgroundImage();
  }

  public void setTypingSound(Sound typingSound) {
    this.typingSound = typingSound;
  }

  public void setFont(Font textFont) {
    this.font = textFont;
  }

  public void setRenderLocation(Point2D renderLocation) {
    this.renderLocation = renderLocation;
  }
}
