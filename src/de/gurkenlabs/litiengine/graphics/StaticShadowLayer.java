package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.MathUtilities;

public class StaticShadowLayer implements IRenderable {
  private int alpha;
  private Color color;
  private final IEnvironment environment;
  private Image image;

  public StaticShadowLayer(IEnvironment env, int alpha, Color color) {
    this.environment = env;
    this.alpha = alpha;
    this.color = color;
  }

  @Override
  public void render(Graphics2D g) {
    RenderEngine.renderImage(g, this.getImage(), Game.getCamera().getViewPortLocation(0, 0));
  }

  public int getAlpha() {
    return this.alpha;
  }

  public Color getColor() {
    return this.color;
  }

  public void setAlpha(int ambientAlpha) {
    this.alpha = MathUtilities.clamp(ambientAlpha, 0, 255);
    this.createImage();
  }

  public void setColor(final Color color) {
    this.color = color;
    this.createImage();
  }

  public void createImage() {
    final String cacheKey = this.getCacheKey();
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      this.image = ImageCache.IMAGES.get(cacheKey);
      return;
    }

    final Color shadowColor = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());

    final List<Path2D> newStaticShadows = new ArrayList<>();
    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    for (final StaticShadow shadow : this.environment.getStaticShadows()) {
      final double shadowX = shadow.getLocation().getX();
      final double shadowY = shadow.getLocation().getY();
      final double shadowWidth = shadow.getWidth();
      final double shadowHeight = shadow.getHeight();

      final StaticShadowType shadowType = shadow.getShadowType();

      final Path2D parallelogram = new Path2D.Double();
      if (shadowType.equals(StaticShadowType.DOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.DOWNLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.DOWNRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX + shadow.getOffset() / 2.0, shadowY + shadowHeight + shadow.getOffset());
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.NOOFFSET)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      }

      if (parallelogram.getWindingRule() != 0) {
        newStaticShadows.add(parallelogram);
      }
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.environment.getMap().getSizeInPixels().getWidth(), (int) this.environment.getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();
    g.setColor(shadowColor);

    final Area ar = new Area();
    for (final Path2D staticShadow : newStaticShadows) {
      final Area staticShadowArea = new Area(staticShadow);
      for (final LightSource light : this.environment.getLightSources()) {
        if (light.getDimensionCenter().getY() > staticShadow.getBounds2D().getMaxY() || staticShadow.getBounds2D().contains(light.getDimensionCenter())) {
          staticShadowArea.subtract(new Area(light.getLightShape()));
        }
      }

      ar.add(staticShadowArea);
    }

    g.fill(ar);
    g.dispose();

    this.image = img;

    ImageCache.IMAGES.put(cacheKey, img);
  }

  private Image getImage() {
    this.createImage();
    return this.image;
  }

  private String getCacheKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getColor());
    sb.append(this.getAlpha());

    for (final StaticShadow shadow : this.environment.getStaticShadows()) {
      sb.append(shadow.getShadowType());
      sb.append(shadow.getLocation());
      sb.append(shadow.getWidth());
      sb.append(shadow.getHeight());
    }

    sb.append(this.environment.getMap().getSizeInPixels());

    final int key = sb.toString().hashCode();
    return "staticshadow-" + this.environment.getMap().getFileName() + "-" + Integer.toString(key);
  }
}
