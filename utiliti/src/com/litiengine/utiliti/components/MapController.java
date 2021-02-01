package com.litiengine.utiliti.components;

import java.util.List;

import com.litiengine.environment.tilemap.xml.TmxMap;

public interface MapController extends Controller {
  TmxMap getCurrentMap();

  void bind(List<TmxMap> maps);

  void bind(List<TmxMap> maps, boolean clear);

  void setSelection(TmxMap name);
}
