package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.image.BufferedImage;
import java.net.URL;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.resources.Resources;

@XmlAccessorType(XmlAccessType.FIELD)
public class TilesetEntry extends CustomPropertyProvider implements ITilesetEntry {
  @XmlTransient
  private Tileset tileset;

  @XmlAttribute
  private Integer id;

  @XmlAttribute
  private String terrain;

  @XmlElement
  private TileAnimation animation;

  @XmlElement
  private MapImage image;

  @XmlAttribute
  private String type;

  @XmlAttribute
  private Double probability;

  @XmlElement(name = "objectgroup")
  private MapObjectLayer collisionData;

  /**
   * Instantiates a new {@code TilesetEntry}.
   */
  public TilesetEntry() {
    // keep for serialization
  }

  /**
   * Instantiates a new {@code TilesetEntry} from the specified tileset.
   *
   * @param tileset
   *          The tileset that contains this entry.
   * @param id
   *          The identifier of this instance.
   */
  public TilesetEntry(Tileset tileset, int id) {
    this.tileset = tileset;
    this.id = id;
  }

  @Override
  public int getId() {
    if (this.id == null) {
      return 0;
    }

    return this.id;
  }

  @Override
  public ITileAnimation getAnimation() {
    return this.animation;
  }

  @Override
  public BufferedImage getImage() {
    if (this.animation == null) {
      return this.getBasicImage();
    }
    return this.tileset.getTile(this.animation.getCurrentFrame().getTileId()).getBasicImage();
  }

  @Override
  public BufferedImage getBasicImage() {
    if (this.image != null) {
      return Resources.images().get(this.image.getAbsoluteSourcePath());
    }
    return this.tileset.getSpritesheet().getSprite(this.getId(), this.tileset.getMargin(), this.tileset.getSpacing());
  }

  @Override
  public ITileset getTileset() {
    return this.tileset;
  }

  @Override
  public String getType() {
    return this.type;
  }

  public TilesetEntry(Tileset tileset, TilesetEntry original) {
    super(original);
    this.tileset = tileset;
    this.id = original.id;
    this.terrain = original.terrain;
    this.animation = original.animation != null ? new TileAnimation(original.animation.getFrames()) : null;
    this.image = original.image != null ? new MapImage(original.image) : null;
    this.type = original.type;
    this.probability = original.probability;
    this.collisionData = original.collisionData != null ? new MapObjectLayer(original.collisionData, true) : null;
  }

  public void setType(String type) {
    this.type = type == null || type.isBlank() ? null : type;
  }

  public double getProbability() {
    return this.probability != null ? this.probability : 1.0;
  }

  public void setProbability(double probability) {
    if (!Double.isFinite(probability) || probability < 0) {
      throw new IllegalArgumentException("Tile probability must be finite and non-negative.");
    }
    this.probability = probability == 1.0 ? null : probability;
  }

  public void setAnimation(TileAnimation animation) {
    this.animation = animation;
  }

  String getLegacyTerrain() {
    return this.terrain;
  }

  void clearLegacyTerrain() {
    this.terrain = null;
  }

  @Override
  public IMapObjectLayer getCollisionInfo() {
    return this.collisionData;
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    if (this.image != null) {
      this.image.finish(location);
    }
  }

  boolean shouldBeSaved() {
    return this.terrain != null || this.image != null || this.animation != null || this.type != null || this.probability != null
      || this.collisionData != null
      || !this.getProperties().isEmpty();
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    this.tileset = (Tileset) parent;
  }
}
