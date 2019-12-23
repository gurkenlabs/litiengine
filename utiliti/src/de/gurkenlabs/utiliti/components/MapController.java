package de.gurkenlabs.utiliti.components;

import java.util.List;

import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;

public interface MapController extends Controller {
  TmxMap getCurrentMap();

  void bind(List<TmxMap> maps);

  void bind(List<TmxMap> maps, boolean clear);

  void setSelection(TmxMap name);
}
