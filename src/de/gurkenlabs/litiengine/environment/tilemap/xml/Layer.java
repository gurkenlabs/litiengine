/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;

// TODO: Auto-generated Javadoc
/**
 * The Class Layer.
 */
public abstract class Layer extends CustomPropertyProvider implements ILayer {

  /** The height. */
  @XmlAttribute
  private Integer height;

  /** The name. */
  @XmlAttribute
  private String name;

  /** The opacity. */
  @XmlAttribute
  private final float opacity = 1;

  /** The visible. */
  @XmlAttribute
  private int visible = 1;

  /** The width. */
  @XmlAttribute
  private Integer width;

  /** The x. */
  @XmlAttribute
  private Integer offsetx;

  /** The y. */
  @XmlAttribute
  private Integer offsety;

  @XmlAttribute
  private int order = -1;

  /**
   * Gets the height.
   *
   * @return the height
   */
  public int getHeight() {
    if (this.height == null) {
      return 0;
    }

    return this.height;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.ILayer#getName()
   */
  @Override
  public String getName() {
    return this.name;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.ILayer#getOpacity()
   */
  @Override
  public float getOpacity() {
    return this.opacity;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.ILayer#getPosition()
   */
  @Override
  public Point getPosition() {
    return new Point(this.getOffsetX(), this.getOffsetY());
  }

  @Override
  public int getOffsetX() {
    if (this.offsetx == null) {
      return 0;
    }

    return this.offsetx;
  }

  @Override
  public int getOffsetY() {
    if (this.offsety == null) {
      return 0;
    }

    return offsety;
  }

  /*
   * (non-Javadoc)
   *
   * @see liti.map.ILayer#getDimension()
   */
  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  public int getWidth() {
    if (this.width == null) {
      return 0;
    }

    return this.width;
  }

  @Override
  public int getOrder() {
    return this.order;
  }

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.tiled.tmx.ILayer#isVisible()
   */
  @Override
  public boolean isVisible() {
    return this.visible > 0;
  }

  @Override
  @XmlTransient
  public void setName(String name) {
    this.name = name;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    if (order == -1) {
      if (parent instanceof Map) {
        Map map = (Map) parent;
        int layerCnt = map.getTileLayers().size();
        layerCnt += map.getImageLayers().size();
        layerCnt += map.getTileLayers().size();
        this.order = layerCnt;
      }
    }

    if (this.offsetx != null && this.offsetx == 0) {
      this.offsetx = null;
    }

    if (this.offsety != null && this.offsety == 0) {
      this.offsety = null;
    }

    if (this.width != null && this.width == 0) {
      this.width = null;
    }

    if (this.height != null && this.height == 0) {
      this.height = null;
    }
  }
}
