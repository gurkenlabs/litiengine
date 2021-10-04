package de.gurkenlabs.litiengine.environment.tilemap.xml;

import java.awt.image.BufferedImage;
import java.net.URL;
import java.util.Arrays;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlTransient;

import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITileAnimation;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.ITilesetEntry;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ArrayUtilities;

@XmlAccessorType(XmlAccessType.FIELD)
public class TilesetEntry extends CustomPropertyProvider implements ITilesetEntry {
  @XmlTransient
  private Tileset tileset;

  private transient ITerrain[] terrains;

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

  @XmlElement(name="objectgroup")
  private MapObjectLayer collisionData;

  /**
   * Instantiates a new {@code TilesetEntry}.
   */
  public TilesetEntry() {
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
  public ITerrain[] getTerrain() {
    return this.terrains;
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

  @Override
  public IMapObjectLayer getCollisionInfo() {
    return this.collisionData;
  }

  protected void setTerrains(ITerrain[] terrains) {
    this.terrains = terrains;
  }

  protected int[] getTerrainIds() {
    int[] terrainIds = new int[] { Terrain.NONE, Terrain.NONE, Terrain.NONE, Terrain.NONE };
    if (this.terrain == null || this.terrain.isEmpty()) {
      return terrainIds;
    }

    int[] ids = ArrayUtilities.splitInt(this.terrain);
    if (ids.length != 4) {
      return terrainIds;
    } else {
      terrainIds = ids;
    }

    return terrainIds;
  }

  @Override
  public String toString() {
    return Arrays.toString(this.getTerrainIds());
  }

  @Override
  void finish(URL location) throws TmxException {
    super.finish(location);
    if (this.image != null) {
      this.image.finish(location);
    }
  }

  boolean shouldBeSaved() {
    return this.terrain != null || this.image != null || this.animation != null || this.type != null;
  }

  @SuppressWarnings("unused")
  private void afterUnmarshal(Unmarshaller u, Object parent) {
    this.tileset = (Tileset) parent;
  }
}
