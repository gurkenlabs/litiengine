package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.awt.Color;
import java.net.URL;

/**
 * Represents an image layer in a tile map. This class extends the {@link Layer} class and implements the {@link IImageLayer} interface. It includes
 * attributes for repeating the image horizontally and vertically, the image itself, and a transparent color.
 */
public class ImageLayer extends Layer implements IImageLayer {

  @XmlAttribute
  private boolean repeatx;

  @XmlAttribute
  private boolean repeaty;

  @XmlElement
  private MapImage image;

  @XmlAttribute
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color trans;

  /**
   * Default no-args constructor for the {@link ImageLayer} class. Initializes a new instance of the {@link ImageLayer} class with default values.
   */
  public ImageLayer() {
    //  default no-args constructor
  }

  /**
   * Copy constructor for the {@link ImageLayer} class. Creates a new instance of the {@link ImageLayer} class by copying the properties from the
   * provided {@link ImageLayer} object.
   *
   * @param original The original {@link ImageLayer} object to copy from.
   */
  public ImageLayer(ImageLayer original) {
    super(original);
    this.repeatx = original.repeatHorizontally();
    this.repeaty = original.repeatVertically();
    this.image = new MapImage(original.image);
    this.trans = original.getTransparentColor();
  }

  @Override
  public IMapImage getImage() {
    return this.image;
  }

  @Override
  public Color getTransparentColor() {
    return this.trans;
  }

  @Override
  public boolean repeatHorizontally() {
    return this.repeatx;
  }

  @Override
  public boolean repeatVertically() {
    return this.repeaty;
  }

  @Override
  public double getOffsetX() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetX() * map.getTileWidth();
    }

    return super.getOffsetX();
  }

  @Override
  public double getOffsetY() {
    if (this.isInfiniteMap()) {
      TmxMap map = (TmxMap) this.getMap();
      return super.getOffsetX() - map.getChunkOffsetY() * map.getTileHeight();
    }

    return super.getOffsetY();
  }

  /**
   * Checks if the map is infinite. This method determines whether the map associated with this layer is infinite and is an instance of
   * {@code TmxMap}.
   *
   * @return {@code true} if the map is infinite and an instance of {@code TmxMap}; {@code false} otherwise.
   */
  private boolean isInfiniteMap() {
    return this.getMap() != null && this.getMap().isInfinite() && this.getMap() instanceof TmxMap;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    this.image.finish(location);
  }
}
