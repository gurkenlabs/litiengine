package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.util.List;

public interface MapController extends Controller {
  TmxMap getCurrentMap();

  void bind(List<TmxMap> maps);

  void bind(List<TmxMap> maps, boolean clear);

  void setSelection(TmxMap name);
}
