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
  private int height;

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
  private int width;

  /** The x. */
  @XmlAttribute
  private int offsetx;

  /** The y. */
  @XmlAttribute
  private int offsety;

  @XmlAttribute
  private int order = -1;

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
    return new Point(this.offsetx, this.offsety);
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
  }
}
