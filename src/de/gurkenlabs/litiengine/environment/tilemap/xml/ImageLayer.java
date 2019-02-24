package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.util.ColorHelper;

public class ImageLayer extends Layer implements IImageLayer {

  @XmlElement(name = "image")
  private MapImage image;

  @XmlAttribute(name = "trans")
  private String transparentcolor;

  @Override
  public IMapImage getImage() {
    return this.image;
  }

  @Override
  public Color getTransparentColor() {
    if (this.transparentcolor != null && !this.transparentcolor.isEmpty()) {
      return ColorHelper.decode(this.transparentcolor);
    }

    return null;
  }

  @Override
  public int getOffsetX() {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetX() * map.getTileWidth();
    }

    return super.getOffsetX();
  }

  @Override
  public int getOffsetY() {
    if (this.isInfiniteMap()) {
      Map map = (Map) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetY() * map.getTileHeight();
    }

    return super.getOffsetY();
  }

  public void setMapPath(final String path) {
    if (this.image == null) {
      return;
    }

    this.image.setAbsolutePath(path);
  }

  private boolean isInfiniteMap() {
    return this.getMap() != null && this.getMap().isInfinite() && this.getMap() instanceof Map;
  }
}
