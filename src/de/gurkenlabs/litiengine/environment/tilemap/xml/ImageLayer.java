/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Color;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapImage;

// TODO: Auto-generated Javadoc
/**
 * The Class ImageLayer.
 */
@XmlRootElement(name = "imagelayer")
public class ImageLayer extends Layer implements IImageLayer {

  /** The image. */
  @XmlElement(name = "image")
  private MapImage image;

  /** The transparentcolor. */
  @XmlAttribute(name = "trans")
  private String transparentcolor;

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IImageLayer#getImage()
   */
  @Override
  public IMapImage getImage() {
    return this.image;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.IImageLayer#getTransparentColor()
   */
  @Override
  public Color getTransparentColor() {
    if (this.transparentcolor != null && !this.transparentcolor.isEmpty()) {
      return Color.decode(this.transparentcolor);
    }

    return null;
  }

  public void setMapPath(final String path) {
    this.image.setAbsolutPath(path);
  }
}
