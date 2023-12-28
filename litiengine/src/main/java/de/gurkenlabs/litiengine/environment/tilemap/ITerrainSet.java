package de.gurkenlabs.litiengine.environment.tilemap;

import java.util.List;

/**
 * A set that contains {@link ITerrain} definitions and allocations to tiles of the related {@link ITileset}.
 */
public interface ITerrainSet extends ICustomPropertyProvider {
  /**
   * Gets the name of this terrain set.
   *
   * @return THe name of the terrain set.
   */
  String getName();

  /**
   * Gets the type of terrain represented by this instance.
   *
   * This method returns the specific type of terrain as an element of the {@link TerrainType} enumeration.
   * The {@link TerrainType} enum defines different types of terrain, including "corner," "edge," and "mixed."
   * The return value indicates the classification of the terrain associated with the current instance.
   *
   * @return The {@link TerrainType} representing the type of terrain.
   *
   * @see TerrainType
   * @see TerrainType#Corner
   * @see TerrainType#Edge
   * @see TerrainType#Mixed
   */
  TerrainType getType();

  /**
   * Gets the terrains defined by this terrain set.
   *
   * @return The terrains defined by this instance.
   */
  List<ITerrain> getTerrains();

  /**
   * Gets the terrains object associated with the specified tile ID.
   * <p>
   * This method searches through the collection of Wang tiles to find a match for the specified tile ID.
   * If a match is found, it extracts the Wang IDs associated with the tile and maps them to terrains defined by this {@link ITerrainSet}.
   * The resulting array contains references to the corresponding terrain objects based on the Wang IDs.
   * If a Wang ID is 0, the corresponding terrain in the array is set to null.
   *
   * @param tileId The tile ID for which terrains are to be retrieved.
   * @return An array of ITerrain objects representing the terrains associated with the given tile ID.
   */
  ITerrain[] getTerrains(int tileId);
}
