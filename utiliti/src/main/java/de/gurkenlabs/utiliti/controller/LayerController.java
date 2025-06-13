package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import java.util.function.Consumer;

public interface LayerController extends Controller {
  IMapObjectLayer getCurrentLayer();

  void clear();

  void onLayersChanged(Consumer<IMap> consumer);
}
