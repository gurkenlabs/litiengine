package de.gurkenlabs.litiengine;

import de.gurkenlabs.litiengine.configuration.ClientConfiguration;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * The class {@code GameMetrics} provides meta information about the game's metrics. This allows the
 * developer to get a feeling about the performance of different aspects (e.g. memory consumption,
 * potential fps, network traffic, ...) and to identify potential issues.
 *
 * <p>This information can be rendered as debug information if configured to get live data during a
 * gameplay session.
 *
 * @see ClientConfiguration#showGameMetrics()
 * @see #render(Graphics2D)
 */
public final class GameMetrics implements IRenderable {
  private static final Font TITLE_FONT = new Font(Font.MONOSPACED, Font.BOLD, 12);
  private static final Font METRIC_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 12);
  private static final int OFFSET_X = 5;
  private static final int OFFSET_Y = 14;

  private final List<RenderMetrics> renderMetrics;

  private final Runtime runtime;
  private final String javaVersion;

  private Color renderColor = Color.RED;

  private int currentOffsetY;

  private int framesPerSecond;
  private int maxFramesPerSecond;

  private float usedMemory;

  GameMetrics() {
    this.renderMetrics = new CopyOnWriteArrayList<>();
    this.runtime = Runtime.getRuntime();
    this.javaVersion =
        System.getProperty("java.version")
            + " (VM: "
            + System.getProperty("java.vm.name")
            + ", VENDOR: "
            + System.getProperty("java.vendor")
            + ")";
  }

  public int getFramesPerSecond() {
    return this.framesPerSecond;
  }

  public float getUsedMemory() {
    return this.usedMemory;
  }

  public Color getRenderColor() {
    return this.renderColor;
  }

  public void trackRenderTime(String name, double renderTime, RenderInfo... infos) {
    this.renderMetrics.add(new RenderMetrics(name, renderTime, infos));
  }

  @Override
  public void render(final Graphics2D g) {
    this.updateMetrics();

    if (!Game.config().client().showGameMetrics()) {
      return;
    }

    this.currentOffsetY = 0;

    g.setColor(this.renderColor);

    // render client metrics
    this.drawTitle(g, "[client]");
    this.drawMetric(g, "fps       : " + this.getFramesPerSecond());
    this.drawMetric(g, "max fps   : " + this.maxFramesPerSecond);
    this.drawMetric(g, "updatables: " + Game.loop().getUpdatableCount());

    // render jvm metrics if debug is enabled
    if (Game.config().debug().isDebugEnabled()) {
      this.drawTitle(g, "[jvm]");
      this.drawMetric(g, "java      : " + this.javaVersion);
      this.drawMetric(g, "memory    : " + String.format("%-5.5s", this.usedMemory) + " MB");
      this.drawMetric(g, "threads   : " + Thread.activeCount());
    }

    // render rendering metrics
    if (!this.renderMetrics.isEmpty()) {
      this.drawTitle(g, "[update]");

      for (RenderMetrics metric : this.renderMetrics) {
        this.drawMetric(g, metric.toString());
      }

      this.renderMetrics.clear();
    }
  }

  void setFramesPerSecond(final int currentFramesPerSecond) {
    this.framesPerSecond = currentFramesPerSecond;
  }

  void setEstimatedMaxFramesPerSecond(final int maxFrames) {
    this.maxFramesPerSecond = maxFrames;
  }

  /**
   * Sets the color that is used when rendering the metrics if {@code cl_showGameMetrics = true}.
   *
   * @param color The color for rendering the metrics.
   * @see ClientConfiguration#showGameMetrics()
   * @see GameMetrics#render(Graphics2D)
   */
  public void setRenderColor(Color color) {
    this.renderColor = color;
  }

  private void updateMetrics() {
    this.usedMemory =
        Math.round((this.runtime.totalMemory() - this.runtime.freeMemory()) / (1024f * 1024f) * 10)
            * 0.1f;
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
