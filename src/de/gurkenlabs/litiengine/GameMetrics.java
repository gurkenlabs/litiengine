/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.graphics.IRenderable;

/**
 * The Class GameMetrics.
 */
public class GameMetrics implements IUpdateable, IRenderable {

  private final List<Long> bytesReceived;

  private final List<Long> bytesSent;

  /** The fps. */
  private final List<Long> fps;

  private final List<Long> ups;

  /** The down stream in bytes. */
  private long downStreamInBytes;

  /** The frames per second. */
  private long framesPerSecond;

  private long lastNetworkTickTime;

  private int packagesReceived;

  private int packagesSent;

  /** The ping. */
  private long ping;

  private long updatesPerSecond;

  /** The up stream in bytes. */
  private long upStreamInBytes;

  /**
   * Instantiates a new game metrics.
   */
  public GameMetrics() {
    this.fps = new CopyOnWriteArrayList<>();
    this.ups = new CopyOnWriteArrayList<>();
    this.bytesSent = new CopyOnWriteArrayList<>();
    this.bytesReceived = new CopyOnWriteArrayList<>();
    if (Game.getConfiguration().CLIENT.showGameMetrics()) {
      Game.getScreenManager().onRendered((g) -> this.render(g));
    }
  }

  public void recordNetworkTraffic() {
    Game.getLoop().registerForUpdate(this);
  }

  /**
   * Gets the average frames per second.
   *
   * @return the average frames per second
   */
  public float getAverageFramesPerSecond() {
    if (this.fps.size() == 0) {
      return 0;
    }

    return this.fps.stream().reduce((x, y) -> x + y).get() / (float) this.fps.size();
  }

  public float getAverageUpdatesPerSecond() {
    if (this.ups.size() == 0) {
      return 0;
    }

    return this.ups.stream().reduce((x, y) -> x + y).get() / (float) this.ups.size();
  }

  /**
   * Gets the down stream in bytes.
   *
   * @return the down stream in bytes
   */
  public float getDownStreamInBytes() {
    return this.downStreamInBytes;
  }

  /**
   * Gets the frames per second.
   *
   * @return the frames per second
   */
  public long getFramesPerSecond() {
    return this.framesPerSecond;
  }

  public int getPackagesReceived() {
    return this.packagesReceived;
  }

  public int getPackagesSent() {
    return this.packagesSent;
  }

  /**
   * Gets the ping.
   *
   * @return the ping
   */
  public long getPing() {
    return this.ping;
  }

  public long getUpdatesPerSecond() {
    return this.updatesPerSecond;
  }

  /**
   * Gets the up stream in bytes.
   *
   * @return the up stream in bytes
   */
  public float getUpStreamInBytes() {
    return this.upStreamInBytes;
  }

  public void packageReceived(final long size) {
    this.bytesReceived.add(size);
  }

  public void packageSent(final long size) {
    this.bytesSent.add(size);
  }

  /**
   * Sets the frames per second.
   *
   * @param currentFramesPerSecond
   *          the new frames per second
   */
  public void setFramesPerSecond(final long currentFramesPerSecond) {
    this.framesPerSecond = currentFramesPerSecond;
    this.fps.add(this.framesPerSecond);
  }

  /**
   * Sets the ping.
   *
   * @param ping
   *          the new ping
   */
  public void setPing(final long ping) {
    this.ping = ping;
  }

  public void setUpdatesPerSecond(final long updatesPerSecond) {
    this.updatesPerSecond = updatesPerSecond;
    this.ups.add(this.updatesPerSecond);
  }

  @Override
  public void update(final IGameLoop loop) {
    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - this.lastNetworkTickTime >= 1000) {
      this.lastNetworkTickTime = currentMillis;
      final long sumUp = this.bytesSent.size() > 0 ? this.bytesSent.parallelStream().reduce((n1, n2) -> n1 + n2).get() : 0;
      this.upStreamInBytes = sumUp;
      this.packagesSent = this.bytesSent.size();

      final long sumDown = this.bytesReceived.size() > 0 ? this.bytesReceived.parallelStream().reduce((n1, n2) -> n1 + n2).get() : 0;
      this.downStreamInBytes = sumDown;
      this.packagesReceived = this.bytesReceived.size();

      this.bytesSent.clear();
      this.bytesReceived.clear();
    }
  }

  @Override
  public void render(Graphics2D g) {
    final int OffsetX = 5;
    final int OffsetY = 12;
    int currentOffsetY = OffsetY;

    g.setColor(Color.RED);
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));
    final String ping = "ping: " + this.getPing() + "ms";
    g.drawString(ping, OffsetX, currentOffsetY);
    currentOffsetY += OffsetY;

    final float upStream = Float.valueOf(Math.round(Game.getMetrics().getUpStreamInBytes() / 1024f * 100) / 100.0f);
    final float downStream = Float.valueOf(Math.round(Game.getMetrics().getDownStreamInBytes() / 1024f * 100) / 100.0f);
    final String in = "in: " + this.getPackagesReceived() + " - " + downStream + "kb/s";
    g.drawString(in, OffsetX, currentOffsetY);
    currentOffsetY += OffsetY;

    final String out = "out: " + this.getPackagesSent() + " - " + upStream + "kb/s";
    g.drawString(out, OffsetX, currentOffsetY);
    currentOffsetY += OffsetY;

    final String fps = "fps: " + this.getFramesPerSecond();
    g.drawString(fps, OffsetX, currentOffsetY);
    currentOffsetY += OffsetY;

    final String ups = "ups: " + this.getUpdatesPerSecond();
    g.drawString(ups, OffsetX, currentOffsetY);
    currentOffsetY += OffsetY;
  }
}
