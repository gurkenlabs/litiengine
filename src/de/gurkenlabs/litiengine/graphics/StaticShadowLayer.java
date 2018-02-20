package de.gurkenlabs.litiengine.graphics;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Area;
import java.awt.image.BufferedImage;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.util.ImageProcessing;

public class StaticShadowLayer extends ColorLayer implements IRenderable {
  public StaticShadowLayer(IEnvironment env, int alpha, Color color) {
    super(env, color, alpha);
  }

  @Override
  public void render(Graphics2D g) {
    RenderEngine.renderImage(g, this.getImage(), Game.getCamera().getViewPortLocation(0, 0));
  }

  @Override
  public void createImage() {
    if (this.getColor() == null) {
      return;
    }

    final String cacheKey = this.getCacheKey();
    if (ImageCache.IMAGES.containsKey(cacheKey)) {
      this.setImage(ImageCache.IMAGES.get(cacheKey));
      return;
    }

    final Color shadowColor = new Color(this.getColor().getRed(), this.getColor().getGreen(), this.getColor().getBlue(), this.getAlpha());

    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getEnvironment().getMap().getSizeInPixels().getWidth(), (int) this.getEnvironment().getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();
    g.setColor(shadowColor);

    final Area ar = new Area();
    for (final StaticShadow staticShadow : this.getEnvironment().getStaticShadows()) {
      if(staticShadow.getShadowType() == StaticShadowType.NONE) {
        continue;
      }
      
      final Area staticShadowArea = staticShadow.getArea();
      for (final LightSource light : this.getEnvironment().getLightSources()) {
        if (light.getDimensionCenter().getY() > staticShadow.getBoundingBox().getMaxY() || staticShadow.getBoundingBox().contains(light.getDimensionCenter())) {
          staticShadowArea.subtract(new Area(light.getLightShape()));
        }
      }

      ar.add(staticShadowArea);
    }

    g.fill(ar);
    g.dispose();

    this.setImage(img);

    ImageCache.IMAGES.put(cacheKey, img);
  }

  @Override
  protected String getCacheKey() {
    final StringBuilder sb = new StringBuilder();
    sb.append(this.getColor());
    sb.append(this.getAlpha());

    for (final StaticShadow shadow : this.getEnvironment().getStaticShadows()) {
      sb.append(shadow.getShadowType());
      sb.append(shadow.getLocation());
      sb.append(shadow.getWidth());
      sb.append(shadow.getHeight());
    }

    sb.append(this.getEnvironment().getMap().getSizeInPixels());

    final int key = sb.toString().hashCode();
    return "staticshadow-" + this.getEnvironment().getMap().getFileName() + "-" + Integer.toString(key);
  }
}
