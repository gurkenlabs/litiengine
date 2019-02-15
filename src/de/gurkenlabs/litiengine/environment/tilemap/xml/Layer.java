package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.Dimension;
import java.awt.Point;
import java.io.Serializable;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.LayerProperty;
import de.gurkenlabs.litiengine.graphics.RenderType;

import de.gurkenlabs.litiengine.util.io.CSV;

/**
 * The Class Layer.
 */
public abstract class Layer extends CustomPropertyProvider implements ILayer, Serializable {
  private static final long serialVersionUID = -5136089511774411328L;

  @XmlAttribute
  private int id;

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

  private transient Map parentMap;
  private transient RenderType renderType;
  private transient boolean renderTypeLoaded;

  public Layer() {
    super();
  }

  /**
   * Copy Constructor for copying instances of Layers.
   *
   * @param layerToBeCopied
   *                          the layer we want to copy
   */
  public Layer(Layer layerToBeCopied) {
    super(layerToBeCopied);
    this.setWidth(layerToBeCopied.getWidth());
    this.setHeight(layerToBeCopied.getHeight());
    this.setName(layerToBeCopied.getName());
    this.offsetx = layerToBeCopied.offsetx;
    this.offsety = layerToBeCopied.offsety;
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

    final String renderTypeProp = this.getStringValue(LayerProperty.LAYER_RENDER_TYPE);
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
      if (this.parentMap == null) {
        return 0;
      } else {
        return this.parentMap.getSizeInTiles().width;
      }
    }

    return this.width;
  }

  @Override
  public int getOrder() {
    return this.getIntValue(LayerProperty.LAYER_ORDER, -1);
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

  protected void setMap(Map map) {
    this.parentMap = map;
  }

  private void setOrder(int order) {
    this.setValue(LayerProperty.LAYER_ORDER, order);
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    int numberOfBranches = 17;
    int branches[] = new int[numberOfBranches];

    branches[0] = 1;

    if (parent instanceof Map) {
      this.parentMap = (Map) parent;
      branches[1] = 1;
    } else {
      branches[2] = 1;
    }

    int order = this.getIntValue(LayerProperty.LAYER_ORDER, -1);
    if (order == -1 && parentMap != null) {
      int layerCnt = this.parentMap.getRawImageLayers().size();
      layerCnt += this.parentMap.getRawMapObjectLayers().size();
      layerCnt += this.parentMap.getRawTileLayers().size();
      this.setOrder(layerCnt);

      branches[3] = 1;
    } else {
      branches[4] = 1;
    }

    if (this.offsetx != null && this.offsetx.intValue() == 0) {
      this.offsetx = null;
      branches[5] = 1;
    } else {
      branches[6] = 1;
    }

    if (this.offsety != null && this.offsety.intValue() == 0) {
      this.offsety = null;
      branches[7] = 1;
    } else {
      branches[8] = 1;
    }

    if (this.width != null && this.width.intValue() == 0) {
      this.width = null;
      branches[9] = 1;
    } else {
      branches[10] = 1;
    }

    if (this.height != null && this.height.intValue() == 0) {
      this.height = null;
      branches[11] = 1;
    } else {
      branches[12] = 1;
    }

    if (this.opacity != null && this.opacity.floatValue() == 1.0f) {
      this.opacity = null;
      branches[13] = 1;
    } else {
      branches[14] = 1;
    }

    if (this.visible != null && this.visible.intValue() == 1) {
      this.visible = null;
      branches[15] = 1;
    } else {
      branches[16] = 1;
    }

    try {
      CSV.write(branches, 8);
    } catch (Exception e) {
      System.err.println("Error: " + e);
    }
  }
}
