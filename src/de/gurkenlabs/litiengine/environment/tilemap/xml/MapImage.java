/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;
import java.awt.Dimension;
import java.io.File;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;

// TODO: Auto-generated Javadoc
/**
 * The Class MapImage.
 */
@XmlRootElement(name = "image")
@XmlAccessorType(XmlAccessType.FIELD)
public class MapImage extends CustomPropertyProvider implements IMapImage {
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

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ITileImage#getDimension()
   */
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMapImage#getSource()
   */
  @Override
  public String getSource() {
    return this.source;
  }

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ITileImage#getSourceFile()
   */
  @Override
  public File getSourceFile() {
    return new File(this.getSource());
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IMapImage#getTransparentColor()
   */
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
    int lastBackslash = mapPath.lastIndexOf("/");
    if (lastBackslash != -1) {
      String subPath = mapPath.substring(0, lastBackslash);
      this.absolutPath = subPath + "/" + this.getSource();
    } else {
      int lastForwardSlash = mapPath.lastIndexOf("\\");
      if (lastForwardSlash != -1) {
        String subPath = mapPath.substring(0, lastForwardSlash);
        this.absolutPath = subPath + "/" + this.getSource();
      }
    }
  }
}
