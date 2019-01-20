package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

/**
 * The Class MapImage.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class MapImage extends CustomPropertyProvider implements IMapImage {

  /** The source. */
  @XmlAttribute
  private String source;

  /** The transparentcolor. */
  @XmlAttribute(name = "trans")
  @XmlJavaTypeAdapter(ColorAdapter.class)
  private Color transparentcolor;

  /** The width. */
  @XmlAttribute
  private int width;

  /** The height. */
  @XmlAttribute
  private int height;

  @XmlTransient
  private String absolutePath;

  @Override
  public String getAbsoluteSourcePath() {
    return this.absolutePath;
  }

  @Override
  public Dimension getDimension() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight() {
    return this.height;
  }

  @Override
  public String getSource() {
    return this.source;
  }

  @Override
  public File getSourceFile() {
    return new File(this.getSource());
  }

  @Override
  public Color getTransparentColor() {
    return this.transparentcolor;
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth() {
    return this.width;
  }

  public void setAbsolutePath(final String mapPath) {
    this.absolutePath = FileUtilities.combine(mapPath, this.getSource());
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

  @Override
  public int hashCode() {
    return this.getAbsoluteSourcePath().hashCode() ^ this.getTransparentColor().hashCode();
  }

  @Override
  public String toString() {
    return this.getAbsoluteSourcePath();
  }
}
