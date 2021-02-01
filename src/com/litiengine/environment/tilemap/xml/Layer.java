package com.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import com.litiengine.environment.tilemap.ILayer;
import com.litiengine.environment.tilemap.IMap;
import com.litiengine.environment.tilemap.LayerProperty;
import com.litiengine.graphics.RenderType;

public abstract class Layer extends CustomPropertyProvider implements ILayer {

  @XmlAttribute
  private int id;

  @XmlAttribute
  private String name;

  @XmlAttribute
  private Integer width;

  @XmlAttribute
  private Integer height;

  @XmlAttribute
  private Float opacity;

  @XmlAttribute
  @XmlJavaTypeAdapter(BooleanIntegerAdapter.class)
  private Boolean visible;

  @XmlAttribute
  private Integer offsetx;

  @XmlAttribute
  private Integer offsety;

  private transient TmxMap parentMap;
  private transient RenderType renderType;
  private transient boolean renderTypeLoaded;

  public Layer() {
    super();
  }

  /**
   * Copy Constructor for copying instances of Layers.
   *
   * @param layerToBeCopied
   *          the layer we want to copy
   */
  public Layer(Layer layerToBeCopied) {
    super(layerToBeCopied);
    this.setWidth(layerToBeCopied.getWidth());
    this.setHeight(layerToBeCopied.getHeight());
    this.setName(layerToBeCopied.getName());
    this.offsetx = layerToBeCopied.offsetx;
    this.offsety = layerToBeCopied.offsety;
    this.setOpacity(layerToBeCopied.getOpacity());
    this.setVisible(layerToBeCopied.isVisible());
  }

  /**
   * Gets the height.
   *
   * @return the height
   */
  @Override
  public int getHeight() {
    if (this.height == null) {
      if (this.parentMap == null) {
        return 0;
      } else {
        return this.parentMap.getSizeInTiles().height;
      }
    }

    return this.height;
  }

  @Override
  public int getId() {
    return this.id;
  }

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public float getOpacity() {
    if (this.opacity == null) {
      return 1.0f;
    }
    return this.opacity;
  }

  @Override
  public Point getOffset() {
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

  @Override
  public RenderType getRenderType() {
    if (this.renderTypeLoaded) {
      return this.renderType;
    }

    this.renderType = this.getEnumValue(LayerProperty.LAYER_RENDER_TYPE, RenderType.class, RenderType.GROUND);
    this.renderTypeLoaded = true;
    return this.renderType;
  }

  @Override
  public Dimension getSizeInTiles() {
    return new Dimension(this.getWidth(), this.getHeight());
  }

  /**
   * Gets the width.
   *
   * @return the width
   */
  @Override
  public int getWidth() {
    if (this.width == null) {
      if (this.parentMap == null) {
        return 0;
      } else {
        return this.parentMap.getSizeInTiles().width;
      }
    }

    return this.width;
  }

  @Override
  public IMap getMap() {
    return this.parentMap;
  }

  @Override
  public boolean isVisible() {
    if (this.visible == null) {
      return true;
    }

    return this.visible;
  }

  @Override
  @XmlTransient
  public void setName(String name) {
    this.name = name;
  }

  @Override
  @XmlTransient
  public void setWidth(int width) {
    this.width = width;
  }

  @Override
  @XmlTransient
  public void setHeight(int height) {
    this.height = height;
  }

  @Override
  @XmlTransient
  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  @Override
  @XmlTransient
  public void setRenderType(RenderType renderType) {
    this.renderType = renderType;
  }

  @Override
  @XmlTransient
  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  protected void setMap(TmxMap map) {
    this.parentMap = map;
  }

  protected void afterUnmarshal(Unmarshaller u, Object parent) {
    if (parent instanceof TmxMap) {
      this.parentMap = (TmxMap) parent;
    }

    if (this.offsetx != null && this.offsetx.intValue() == 0) {
      this.offsetx = null;
    }

    if (this.offsety != null && this.offsety.intValue() == 0) {
      this.offsety = null;
    }

    if (this.width != null && this.width.intValue() == 0) {
      this.width = null;
    }

    if (this.height != null && this.height.intValue() == 0) {
      this.height = null;
    }

    if (this.opacity != null && this.opacity.floatValue() == 1.0f) {
      this.opacity = null;
    }
  }
}
