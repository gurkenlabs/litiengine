package de.gurkenlabs.utiliti.components;

import java.util.function.Consumer;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;

public interface LayerController extends Controller {
  IMapObjectLayer getCurrentLayer();

  void clear();

  public void onLayersChanged(Consumer<IMap> consumer);
}
