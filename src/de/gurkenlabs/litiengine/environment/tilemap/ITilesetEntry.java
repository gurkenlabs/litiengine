package de.gurkenlabs.litiengine.environment.tilemap;

public interface ITilesetEntry extends ICustomPropertyProvider {

  public int getId();

  public ITerrain[] getTerrain();

  public ITileAnimation getAnimation();
}
