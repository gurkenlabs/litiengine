package de.gurkenlabs.litiengine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.graphics.IRenderable;

public final class GameMetrics implements IUpdateable, IRenderable {
  private static final int OFFSET_X = 5;
  private static final int OFFSET_Y = 12;
  
  private final List<Long> bytesReceived;
  private final List<Long> bytesSent;

  private long downStreamInBytes;
  private long lastNetworkTickTime;
  private int packagesReceived;
  private int packagesSent;
  private long ping;
  private long upStreamInBytes;
  
  private long framesPerSecond;
  private long updatesPerSecond;
  
  private float usedMemory;

  GameMetrics() {
    this.bytesSent = new CopyOnWriteArrayList<>();
    this.bytesReceived = new CopyOnWriteArrayList<>();
  }

  public float getDownStreamInBytes() {
    return this.downStreamInBytes;
  }

  public long getFramesPerSecond() {
    return this.framesPerSecond;
  }

  public int getPackagesReceived() {
    return this.packagesReceived;
  }

  public int getPackagesSent() {
    return this.packagesSent;
  }

  public long getPing() {
    return this.ping;
  }

  public long getUpdatesPerSecond() {
    return this.updatesPerSecond;
  }

  public float getUpStreamInBytes() {
    return this.upStreamInBytes;
  }
  
  public float getUsedMemory() {
    return this.usedMemory;
  }

  public void packageReceived(final long size) {
    this.bytesReceived.add(size);
  }

  public void packageSent(final long size) {
    this.bytesSent.add(size);
  }

  public void recordNetworkTraffic() {
    Game.loop().attach(this);
  }

  @Override
  public void render(final Graphics2D g) {

    int currentOffsetY = OFFSET_Y;

    g.setColor(Color.RED);
    g.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 10));

    final String memory = "memory: " + this.usedMemory + "MB";
    g.drawString(memory, OFFSET_X, currentOffsetY);
    currentOffsetY += OFFSET_Y;

    final String pingText = "ping: " + this.getPing() + "ms";
    g.drawString(pingText, OFFSET_X, currentOffsetY);
    currentOffsetY += OFFSET_Y;

    final float upStream = Math.round(Game.metrics().getUpStreamInBytes() / 1024f * 100) * 0.01f;
    final float downStream = Math.round(Game.metrics().getDownStreamInBytes() / 1024f * 100) * 0.01f;
    final String in = "in: " + this.getPackagesReceived() + " - " + downStream + "kb/s";
    g.drawString(in, OFFSET_X, currentOffsetY);
    currentOffsetY += OFFSET_Y;

    final String out = "out: " + this.getPackagesSent() + " - " + upStream + "kb/s";
    g.drawString(out, OFFSET_X, currentOffsetY);
    currentOffsetY += OFFSET_Y;

    final String fpsString = "fps: " + this.getFramesPerSecond();
    g.drawString(fpsString, OFFSET_X, currentOffsetY);
    currentOffsetY += OFFSET_Y;

    final String upsString = "ups: " + this.getUpdatesPerSecond();
    g.drawString(upsString, OFFSET_X, currentOffsetY);
  }

  public void setFramesPerSecond(final long currentFramesPerSecond) {
    this.framesPerSecond = currentFramesPerSecond;
  }

  public void setPing(final long ping) {
    this.ping = ping;
  }

  public void setUpdatesPerSecond(final long updatesPerSecond) {
    this.updatesPerSecond = updatesPerSecond;
  }

  @Override
  public void update() {
    final Runtime runtime = Runtime.getRuntime();
    this.usedMemory = Math.round((runtime.totalMemory() - runtime.freeMemory()) / (1024f * 1024f) * 10) * 0.1f;
    
    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - this.lastNetworkTickTime >= 1000) {
      this.lastNetworkTickTime = currentMillis;

      Optional<Long> sentOpt = this.bytesSent.parallelStream().reduce((n1, n2) -> n1 + n2);
      final long sumUp = !this.bytesSent.isEmpty() && sentOpt.isPresent() ? sentOpt.get() : 0;
      this.upStreamInBytes = sumUp;
      this.packagesSent = this.bytesSent.size();

      Optional<Long> receivedOpt = this.bytesReceived.parallelStream().reduce((n1, n2) -> n1 + n2);
      final long sumDown = !this.bytesReceived.isEmpty() && receivedOpt.isPresent() ? receivedOpt.get() : 0;
      this.downStreamInBytes = sumDown;
      this.packagesReceived = this.bytesReceived.size();

      this.bytesSent.clear();
      this.bytesReceived.clear();
    }
  }
}
