package de.gurkenlabs.litiengine.environment.tilemap.xml;

import de.gurkenlabs.litiengine.environment.tilemap.ITerrain;
import de.gurkenlabs.litiengine.environment.tilemap.ITerrainSet;
import de.gurkenlabs.litiengine.environment.tilemap.TerrainType;
import jakarta.xml.bind.annotation.*;

import java.util.List;

@XmlAccessorType(XmlAccessType.FIELD)
public class WangSet extends CustomPropertyProvider implements ITerrainSet {

  @XmlAttribute
  private String name;

  @XmlAttribute(name = "class")
  private String wangSetClass;

  @XmlAttribute
  private TerrainType type;

  @XmlAttribute
  private int tile; // think this is unused in Tiled but still serialized

  @XmlElement(type = WangColor.class)
  private List<ITerrain> wangcolor;

  @XmlElement(name = "wangtile")
  private List<WangTile> wangtiles;

  @Override
  public String getName() {
    return this.name;
  }

  @Override
  public TerrainType getType() {
    return this.type;
  }

  @Override
  public List<ITerrain> getTerrains() {
    return this.wangcolor;
  }

  @Override
  public ITerrain[] getTerrains(int tileId) {
    final int TERRAIN_COUNT = 8;
    var terrains = new ITerrain[TERRAIN_COUNT];

    for (var wangtile : this.wangtiles) {
      if (wangtile.getTileId() == tileId) {
        for(int i = 0; i < TERRAIN_COUNT; i++){
          var terrain = wangtile.getWangId()[i];
          terrains[i] = terrain == 0 ? null : this.getTerrains().get(wangtile.getWangId()[i] - 1);
        }

        break;
      }
    }

    return terrains;
  }
}
