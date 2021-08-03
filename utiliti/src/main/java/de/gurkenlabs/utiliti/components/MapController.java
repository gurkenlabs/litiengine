package de.gurkenlabs.utiliti.components;

import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import java.util.List;

public interface MapController extends Controller {
  TmxMap getCurrentMap();

  void bind(List<TmxMap> maps);

  void bind(List<TmxMap> maps, boolean clear);

  void setSelection(TmxMap name);
}
