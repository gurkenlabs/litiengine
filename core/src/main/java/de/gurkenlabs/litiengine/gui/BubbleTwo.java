package de.gurkenlabs.litiengine.gui;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.IEntity;

import java.awt.*;
import java.awt.geom.Point2D;

public class BubbleTwo extends GuiComponent {
  private int textDisplayTime = GuiProperties.getDefaultSpeechBubbleDisplayTime();
  private long startedTick;
  private final IEntity entity;
  private Align indicatorAlign = GuiProperties.getDefaultTextAlign();
  private double offsetY;


  public BubbleTwo(IEntity entity, double width, double height, String text) {
    super(Game.world().camera().getViewportDimensionCenter(entity).getX() * Game.world().camera().getRenderScale() - width / 2d, Game.world().camera().getViewportLocation(entity).getY() * Game.world().camera().getRenderScale() - height, width, height);
    this.entity = entity;
    setText(text);
    setAutomaticLineBreaks(true);
    setForwardMouseEvents(false);
  }

  public IEntity getEntity() {
    return entity;
  }

  public int getTextDisplayTime() {
    return textDisplayTime;
  }

  public void setTextDisplayTime(int textDisplayTime) {
    this.textDisplayTime = textDisplayTime;
  }

  public void start() {
    startedTick = Game.time().now();
    Game.screens().current().getComponents().add(this);
    prepare();
  }

  public void stop() {
    Game.screens().current().getComponents().remove(this);
    suspend();
  }

  @Override
  public void render(Graphics2D g) {
    if (Game.time().since(startedTick) >= getTextDisplayTime()) {
      stop();
    }
    setLocation(new Point2D.Double(Game.world().camera().getViewportDimensionCenter(getEntity()).getX() * Game.world().camera().getRenderScale() - getWidth() / 2d, Game.world().camera().getViewportLocation(getEntity()).getY() * Game.world().camera().getRenderScale() - getHeight() + getOffsetY()));
    super.render(g);
  }


  public Align getIndicatorAlign() {
    return indicatorAlign;
  }

  public void setIndicatorAlign(Align indicatorAlign) {
    this.indicatorAlign = indicatorAlign;
  }

  public double getOffsetY() {
    return offsetY;
  }

  public void setOffsetY(double offsetY) {
    this.offsetY = offsetY;
  }
}
