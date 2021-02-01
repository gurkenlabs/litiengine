package com.litiengine.utiliti.components;

import java.util.function.Consumer;

import com.litiengine.environment.tilemap.IMap;
import com.litiengine.environment.tilemap.IMapObjectLayer;

public interface LayerController extends Controller {
  IMapObjectLayer getCurrentLayer();

  void clear();

  public void onLayersChanged(Consumer<IMap> consumer);
}
