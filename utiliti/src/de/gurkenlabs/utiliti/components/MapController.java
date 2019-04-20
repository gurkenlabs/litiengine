package de.gurkenlabs.utiliti.components;

import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;

public interface MapController extends Controller {
  IMap getCurrentMap();

  void bind(List<TmxMap> maps);

  void bind(List<TmxMap> maps, boolean clear);

  void setSelection(String name);
}
