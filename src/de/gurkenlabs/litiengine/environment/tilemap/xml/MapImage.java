package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

/**
 * The Class MapImage.
 */
@XmlRootElement(name = "image")
@XmlAccessorType(XmlAccessType.FIELD)
public class MapImage extends CustomPropertyProvider implements IMapImage {
  private static final long serialVersionUID = -3571362172734426098L;

  /** The source. */
  @XmlAttribute
  private String source;

  /** The transparentcolor. */
  @XmlAttribute(name = "trans")
  private String transparentcolor;

  /** The width. */
  @XmlAttribute
  private int width;

  /** The height. */
  @XmlAttribute
  private int height;

  @XmlTransient
  private String absolutPath;

  @Override
  public String getAbsoluteSourcePath() {
    return this.absolutPath;
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
    if (this.transparentcolor != null && !this.transparentcolor.isEmpty()) {
      return Color.decode("#" + this.transparentcolor);
    }

    return null;
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth() {
    return this.width;
  }

  public void setAbsolutPath(final String mapPath) {
    this.absolutPath = FileUtilities.combine(mapPath, this.getSource());
  }
}
