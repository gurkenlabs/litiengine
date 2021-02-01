package com.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.net.MalformedURLException;
import java.net.URL;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.litiengine.environment.tilemap.IMapImage;

@XmlAccessorType(XmlAccessType.FIELD)
public class MapImage extends CustomPropertyProvider implements IMapImage {

  @XmlAttribute
  private String source;

  @XmlAttribute(name = "trans")
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color transparentcolor;

  @XmlAttribute
  private int width;

  @XmlAttribute
  private int height;

  @XmlTransient
  private URL absolutePath;

  /**
   * Instantiates a new {@code MapImage} instance.
   */
  public MapImage() {
    super();
  }

  /**
   * Instantiates a new {@code MapImage} instance by copying the specified original.
   *
   * @param original
   *          the original we want to copy
   */
  public MapImage(MapImage original) {
    super(original);

    if (original == null) {
      return;
    }

    this.source = original.getSource();
    if (original.getTransparentColor() != null) {
      this.transparentcolor = new Color(original.getTransparentColor().getRed(),
          original.getTransparentColor().getGreen(),
          original.getTransparentColor().getBlue(),
          original.getTransparentColor().getAlpha());
    }
    this.width = original.getWidth();
    this.height = original.getHeight();
    this.absolutePath = original.getAbsoluteSourcePath();
  }

  @Override
  public URL getAbsoluteSourcePath() {
    return this.absolutePath;
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  @Override
  public int getWidth() {
    return this.width;
  }

  @Override
  public int getHeight() {
    return this.height;
  }

  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public Color getTransparentColor() {
    return this.transparentcolor;
  }

  @Override
  public void setTransparentColor(Color color) {
    this.transparentcolor = color;
  }

  @Override
  public void setSource(String source) {
    this.source = source;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    try {
      this.absolutePath = new URL(location, this.source);
    } catch (MalformedURLException e) {
      throw new MissingImageException(e);
    }
  }

  @Override
  public void setAbsoluteSourcePath(URL absolutePath) {
    this.absolutePath = absolutePath;
  }

  @Override
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  public boolean equals(Object anObject) {
    if (!(anObject instanceof IMapImage)) {
      return false;
    }

    if (this == anObject) {
      return true;
    }

    IMapImage other = (IMapImage) anObject;
    return this.getTransparentColor().equals(other.getTransparentColor()) && this.getAbsoluteSourcePath().equals(other.getAbsoluteSourcePath());
  }

  /**
   * Computes a hash code for this map image. The hash code for
   * a map image is equal to the hash code of its absolute source
   * path xor the hash code of its transparent color.
   * 
   * @return The hash code for this map image
   */
  @Override
  public int hashCode() {
    return this.getAbsoluteSourcePath().hashCode() ^ this.getTransparentColor().hashCode();
  }

  @Override
  public String toString() {
    return this.getAbsoluteSourcePath().toExternalForm();
  }
}
