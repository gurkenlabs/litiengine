package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.net.URL;

public class ImageLayer extends Layer implements IImageLayer {

  @XmlElement private MapImage image;

  @XmlAttribute
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color trans;

  @Override
  public IMapImage getImage() {
    return this.image;
  }

  @Override
  public Color getTransparentColor() {
    return this.trans;
  }

  @Override
  public int getOffsetX() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetX() * map.getTileWidth();
    }

    return super.getOffsetX();
  }

  @Override
  public int getOffsetY() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetY() * map.getTileHeight();
    }

    return super.getOffsetY();
  }

  private boolean isInfiniteMap() {
    return this.getMap() != null && this.getMap().isInfinite() && this.getMap() instanceof TmxMap;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    this.image.finish(location);
  }
}
