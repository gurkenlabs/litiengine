package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.LayerProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;

/**
 * The Class Layer.
 */
public abstract class Layer extends CustomPropertyProvider implements ILayer, Serializable {
  private static final long serialVersionUID = -5136089511774411328L;

  /** The name. */
  @XmlAttribute
  private String name;

  /** The width. */
  @XmlAttribute
  private Integer width;

  /** The height. */
  @XmlAttribute
  private Integer height;

  /** The opacity. */
  @XmlAttribute
  private Float opacity;

  /** The visible. */
  @XmlAttribute
  private Integer visible;

  /** The x. */
  @XmlAttribute
  private Integer offsetx;

  /** The y. */
  @XmlAttribute
  private Integer offsety;

  private transient RenderType renderType;
  private boolean renderTypeLoaded;

  public Layer() {
    super();
  }

  /**
   * Copy Constructor for copying instances of Layers.
   *
   * @param layerToBeCopied
   *          the layer we want to copy
   */
  public Layer(ILayer layerToBeCopied) {
    super(layerToBeCopied);
    this.setWidth(layerToBeCopied.getWidth());
    this.setHeight(layerToBeCopied.getHeight());
    this.setName(layerToBeCopied.getName());
    this.setOffsetX(layerToBeCopied.getOffsetX());
    this.setOffsetY(layerToBeCopied.getOffsetY());
    this.setOpacity(layerToBeCopied.getOpacity());
    this.setOrder(layerToBeCopied.getOrder());
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
      return 0;
    }

    return this.height;
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

  @Override
  public RenderType getRenderType() {
    if (this.renderTypeLoaded) {
      return this.renderType;
    }

    final String renderTypeProp = this.getCustomProperty(LayerProperty.LAYER_RENDER_TYPE);
    if (renderTypeProp != null && !renderTypeProp.isEmpty()) {
      this.renderType = RenderType.valueOf(renderTypeProp);
    } else {
      this.renderType = RenderType.GROUND;
    }

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
      return 0;
    }

    return this.width;
  }

  @Override
  public int getOrder() {
    return this.getCustomPropertyInt(LayerProperty.LAYER_ORDER, -1);
  }

  @Override
  public boolean isVisible() {
    if (this.visible == null) {
      return true;
    }

    return this.visible > 0;
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

  @XmlTransient
  public void setOpacity(float opacity) {
    this.opacity = opacity;
  }

  @Override
  @XmlTransient
  public void setVisible(boolean visible) {
    this.visible = visible ? 1 : 0;
  }

  @XmlTransient
  public void setOffsetX(int offsetX) {
    this.offsetx = offsetX;
  }

  @XmlTransient
  public void setOffsetY(int offsetY) {
    this.offsety = offsetY;
  }

  private void setOrder(int order) {
    this.setCustomProperty(LayerProperty.LAYER_ORDER, Integer.toString(order));
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    int order = this.getCustomPropertyInt(LayerProperty.LAYER_ORDER, -1);
    if (order == -1 && parent instanceof Map) {
      Map map = (Map) parent;
      int layerCnt = map.getRawImageLayers().size();
      layerCnt += map.getRawMapObjectLayers().size();
      layerCnt += map.getRawTileLayers().size();
      this.setOrder(layerCnt);
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

    if (this.visible != null && this.visible.intValue() == 1) {
      this.visible = null;
    }
  }
}
