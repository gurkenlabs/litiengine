package de.gurkenlabs.litiengine;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;
import de.gurkenlabs.litiengine.graphics.IRenderable;

public final class GameMetrics implements IRenderable {
  private static final Font TITLE_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);
  private static final Font METRIC_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final int OFFSET_X = 5;
  private static final int OFFSET_Y = 14;

  private final List<Long> bytesReceived;
  private final List<Long> bytesSent;
  private final List<RenderMetrics> renderMetrics;

  private final Runtime runtime;

  private Color renderColor = Color.RED;

  private int currentOffsetY;

  private long downStreamInBytes;
  private long lastNetworkTickTime;
  private int packagesReceived;
  private int packagesSent;
  private long ping;
  private long upStreamInBytes;

  private int framesPerSecond;
  private int maxFramesPerSecond;

  private float usedMemory;

  GameMetrics() {
    this.bytesSent = new CopyOnWriteArrayList<>();
    this.bytesReceived = new CopyOnWriteArrayList<>();
    this.renderMetrics = new CopyOnWriteArrayList<>();
    this.runtime = Runtime.getRuntime();
  }

  public float getDownStreamInBytes() {
    return this.downStreamInBytes;
  }

  public int getFramesPerSecond() {
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

  public float getUpStreamInBytes() {
    return this.upStreamInBytes;
  }

  public float getUsedMemory() {
    return this.usedMemory;
  }

  public Color getRenderColor() {
    return this.renderColor;
  }

  public void packageReceived(final long size) {
    this.bytesReceived.add(size);
  }

  public void packageSent(final long size) {
    this.bytesSent.add(size);
  }

  public void trackRenderTime(String name, double renderTime, RenderInfo... infos) {
    this.renderMetrics.add(new RenderMetrics(name, renderTime, infos));
  }

  @Override
  public void render(final Graphics2D g) {
    this.updateMetrics();

    this.currentOffsetY = 0;

    g.setColor(this.renderColor);

    // render client metrics
    this.drawTitle(g, "[client]");
    this.drawMetric(g, "fps       : " + this.getFramesPerSecond());
    this.drawMetric(g, "max fps   : " + this.maxFramesPerSecond);

    // render jvm metrics if debug is enabled
    if (Game.config().debug().isDebugEnabled()) {
      this.drawTitle(g, "[jvm]");
      this.drawMetric(g, "java      : " + Runtime.class.getPackage().getImplementationVersion());
      this.drawMetric(g, "memory    : " + String.format("%-5.5s", this.usedMemory) + " MB");
      this.drawMetric(g, "threads   : " + Thread.activeCount());
    }

    // render network metrics
    final float downStream = Math.round(this.getDownStreamInBytes() / 1024f * 100) * 0.01f;
    final float upStream = Math.round(this.getUpStreamInBytes() / 1024f * 100) * 0.01f;

    this.drawTitle(g, "[network]");
    this.drawMetric(g, "ping      : " + this.getPing() + " ms");
    this.drawMetric(g, "in        : " + this.getPackagesReceived() + " - " + downStream + " kb/s");
    this.drawMetric(g, "out       : " + this.getPackagesSent() + " - " + upStream + " kb/s");

    // render rendering metrics
    if (!this.renderMetrics.isEmpty()) {
      this.drawTitle(g, "[rendering]");

      for (RenderMetrics metric : this.renderMetrics) {
        this.drawMetric(g, metric.toString());
      }

      this.renderMetrics.clear();
    }
  }

  public void setFramesPerSecond(final int currentFramesPerSecond) {
    this.framesPerSecond = currentFramesPerSecond;
  }

  public void setEstimatedMaxFramesPerSecond(final int maxFrames) {
    this.maxFramesPerSecond = maxFrames;
  }

  public void setPing(final long ping) {
    this.ping = ping;
  }

  /**
   * Sets the color that is used when rendering the metrics if <code>cl_showGameMetrics = true</code>.
   * 
   * @param color
   *          The color for rendering the metrics.
   * 
   * @see ClientConfiguration#showGameMetrics()
   * @see GameMetrics#render(Graphics2D)
   */
  public void setRenderColor(Color color) {
    this.renderColor = color;
  }

  private void updateMetrics() {
    this.usedMemory = Math.round((this.runtime.totalMemory() - this.runtime.freeMemory()) / (1024f * 1024f) * 10) * 0.1f;

    final long currentMillis = System.currentTimeMillis();
    if (currentMillis - this.lastNetworkTickTime >= 1000) {
      this.lastNetworkTickTime = currentMillis;

      if (!this.bytesSent.isEmpty()) {
        Optional<Long> sentOpt = this.bytesSent.parallelStream().reduce((n1, n2) -> n1 + n2);
        final long sumUp = !this.bytesSent.isEmpty() && sentOpt.isPresent() ? sentOpt.get() : 0;
        this.upStreamInBytes = sumUp;
        this.packagesSent = this.bytesSent.size();
        this.bytesSent.clear();
      }

      if (!this.bytesReceived.isEmpty()) {
        Optional<Long> receivedOpt = this.bytesReceived.parallelStream().reduce((n1, n2) -> n1 + n2);
        final long sumDown = !this.bytesReceived.isEmpty() && receivedOpt.isPresent() ? receivedOpt.get() : 0;
        this.downStreamInBytes = sumDown;
        this.packagesReceived = this.bytesReceived.size();
        this.bytesReceived.clear();
      }
    }
  }

  private void drawTitle(Graphics2D g, String title) {
    this.currentOffsetY += OFFSET_Y;
    g.setFont(TITLE_FONT);
    g.drawString(title, OFFSET_X, this.currentOffsetY);
    this.currentOffsetY += OFFSET_Y;
  }

  private void drawMetric(Graphics2D g, String metric) {
    g.setFont(METRIC_FONT);
    g.drawString(metric, OFFSET_X, this.currentOffsetY);
    this.currentOffsetY += OFFSET_Y;
  }

  public class RenderMetrics {
    private final List<RenderInfo> renderInfo;

    private final String renderName;
    private final double renderTime;

    RenderMetrics(String name, double renderTime, RenderInfo... infos) {
      this.renderInfo = new ArrayList<>();
      this.renderInfo.addAll(Arrays.asList(infos));

      this.renderName = name;
      this.renderTime = renderTime;
    }

    public String getRenderName() {
      return this.renderName;
    }

    public double getRenderTime() {
      return this.renderTime;
    }

    public List<RenderInfo> getRenderInfos() {
      return this.renderInfo;
    }

    @Override
    public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append(String.format("%-10.10s", this.getRenderName()));
      sb.append(": ");
      sb.append(String.format("%-4.4f", this.getRenderTime()));
      sb.append(" ms");
      if (!this.renderInfo.isEmpty()) {
        sb.append(" ");
        for (RenderInfo info : this.getRenderInfos()) {
          sb.append(info);
        }
      }
      return sb.toString();
    }
  }

  public static class RenderInfo {
    private final String name;
    private final Object value;

    public RenderInfo(String name, Object value) {
      this.name = name;
      this.value = value;
    }

    public String getName() {
      return this.name;
    }

    public Object getValue() {
      return this.value;
    }

    @Override
    public String toString() {
      return "[" + this.getName() + ": " + this.getValue() + "]";
    }
  }
}
