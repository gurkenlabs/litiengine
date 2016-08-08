package de.gurkenlabs.litiengine.graphics;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.font.FontRenderContext;
import java.awt.font.LineBreakMeasurer;
import java.awt.font.TextAttribute;
import java.awt.font.TextLayout;
import java.awt.geom.Rectangle2D;
import java.text.AttributedCharacterIterator;
import java.text.AttributedString;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.tiled.tmx.RenderType;

public class SpeechBubble implements IUpdateable, IRenderable{
  private static final int DISPLAYTIME_PER_LETTER = 200;
  private static final int LETTER_WRITE_DELAY = 30;
  private static final Color SPEAK_FONT_COLOR = Color.WHITE;
  private static final Color SPEAK_FONT_BACKGROUNDCOLOR = new Color(0, 0, 0, 80);

  private final int TEXT_BOX_WIDTH;
  private final IEntity entity;
  private final Font font;

  private long lastTextDispay;
  private long currentTextDisplayTime;
  private String currentText;
  private String displayedText;
  private Queue<Character> currentTextQueue;
  private long lastCharPoll;
  
  public SpeechBubble(IEntity entity, Font font, String text){
    this.TEXT_BOX_WIDTH = (int) (entity.getWidth() * 4);
    this.entity = entity;
    this.font = font;

    this.currentText = text;
    this.currentTextDisplayTime = text.length() * DISPLAYTIME_PER_LETTER;
    this.currentTextQueue = new ConcurrentLinkedQueue<>();
    this.displayedText = "";
    for (int i = 0; i < this.currentText.length(); i++) {
      this.currentTextQueue.add(this.currentText.charAt(i));
    }

    this.lastTextDispay = Game.getLoop().getTicks();
    Game.getEnvironment().add(this, RenderType.OVERLAY);
    Game.getLoop().registerForUpdate(this);
  }
  
  @Override
  public void update(IGameLoop loop) {
    if (this.currentText == null) {
      Game.getEnvironment().remove(this);
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
      }
    }

    // continue displaying currently displayed text
  }

  @Override
  public void render(Graphics2D g) {
    if (this.displayedText == null || this.displayedText.isEmpty()) {
      return;
    }

    final int PADDING = 5;
    Rectangle2D bounds = new Rectangle2D.Double(this.entity.getLocation().getX() + this.entity.getWidth() / 2.0 - TEXT_BOX_WIDTH / 2.0 - PADDING, this.entity.getLocation().getY() - 30 - PADDING, TEXT_BOX_WIDTH + 2 * PADDING, 25 + PADDING);
    g.setColor(SPEAK_FONT_BACKGROUNDCOLOR);
    RenderEngine.fillShape(g, bounds);

    g.setColor(SPEAK_FONT_COLOR);
    RenderEngine.drawShape(g, bounds, new BasicStroke(2 / Game.getInfo().renderScale()));

    FontRenderContext frc = g.getFontRenderContext();

    String text = this.displayedText + "";
    AttributedString styledText = new AttributedString(text);
    styledText.addAttribute(TextAttribute.FONT, this.font);
    AttributedCharacterIterator iterator = styledText.getIterator();
    LineBreakMeasurer measurer = new LineBreakMeasurer(iterator, frc);
    measurer.setPosition(0);

    float x = (float) Game.getScreenManager().getCamera().getViewPortLocation(this.entity).getX() + this.entity.getWidth() / 2.0f - TEXT_BOX_WIDTH / 2.0f, y = (float) Game.getScreenManager().getCamera().getViewPortLocation(this.entity).getY() - 30;
    while (measurer.getPosition() < text.length()) {
      TextLayout layout = measurer.nextLayout(TEXT_BOX_WIDTH);

      y += layout.getAscent();
      float dx = layout.isLeftToRight() ? 0 : TEXT_BOX_WIDTH - layout.getAdvance();
      layout.draw(g, x + dx, y);
      y += layout.getDescent() + layout.getLeading();
    }
  }
}
