package de.gurkenlabs.utiliti.controller;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.EmitterMapObjectLoader;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IGroupLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapOrientation;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.ILayerList;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapOrientations;
import de.gurkenlabs.litiengine.environment.tilemap.MapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerAxis;
import de.gurkenlabs.litiengine.environment.tilemap.StaggerIndex;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.gui.ComponentMouseWheelEvent;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.utiliti.controller.Transform.TransformMode;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.Tool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.model.Cursors;
import de.gurkenlabs.utiliti.view.components.CreaturePanel;
import de.gurkenlabs.utiliti.view.components.PropPanel;
import de.gurkenlabs.utiliti.view.components.SceneGraph;
import de.gurkenlabs.utiliti.view.components.UI;
import java.awt.Point;
import de.gurkenlabs.utiliti.view.dialogs.ConfirmDialog;
import de.gurkenlabs.utiliti.view.dialogs.NewMapDialog;
import de.gurkenlabs.utiliti.view.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.view.dialogs.XmlImportDialog;
import de.gurkenlabs.utiliti.view.renderers.GridRenderer;
import de.gurkenlabs.utiliti.view.renderers.Renderers;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;
import javax.imageio.ImageIO;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

public class MapComponent extends GuiComponent {

  private static final Logger log = Logger.getLogger(MapComponent.class.getName());
  private static final int FIT_PADDING = 32;
  private static final float MIN_FIT_ZOOM = 0.01f;

  private final List<Consumer<TransformMode>> transformModeChangedConsumer;
  private final List<Consumer<IMapObject>> focusChangedConsumer;
  private final List<Runnable> inspectorNavigationChangedConsumer;
  private final List<Consumer<List<IMapObject>>> selectionChangedConsumer;
  private final List<Consumer<TmxMap>> loadingConsumer;
  private final List<Consumer<TmxMap>> loadedConsumer;
  private final List<Consumer<Blueprint>> copyTargetChangedConsumer;

  private final Map<IMap, Point2D> cameraFocus;
  private final Map<IMap, Float> cameraZoom;
  private final Map<IMap, IMapObject> focusedObjects;
  private final Map<IMap, InspectorNavigationHistory<InspectorNavigationTarget>> inspectorNavigationHistory;
  private final Map<IMap, List<IMapObject>> selectedObjects;
  private final Map<IMap, Environment> environments;
  private final List<TmxMap> maps;
  private TransformMode transformMode = TransformMode.NONE;
  private Point2D startPoint;
  private Blueprint copiedBlueprint;
  private ProjectCodeIntegration.Definition createDefinition;

  /**
   * This flag is used to control the undo behavior of a <b>move transformation</b>. It ensures that the UndoManager tracks the "changing" event in
   * the beginning of the operation (when the key event is recorded for the first time) and also triggers the "changed" event upon key release.
   */
  private boolean isMoving;

  /**
   * This flag is used to control the undo behavior of a <b>resize transformation</b>. It ensures that the UndoManager tracks the "changing" event in
   * the beginning of the operation (when the key event is recorded for the first time) and also triggers the "changed" event upon key release.
   */
  private boolean isResizing;

  /**
   * This flag is used to bundle a move operation over several key events until the arrow keys are released. This allows for the UndoManager to revert
   * the keyboard move operation once instead of having to revert for each individual key stroke.
   */
  private boolean isMovingWithKeyboard;

  /**
   * This flag prevents circular focusing approaches while this instance is already performing a focus process.
   */
  private boolean isFocussing;
  private boolean navigatingInspector;
  private int inspectorNavigationMouseButton;
  private long inspectorNavigationMousePressedAt;
  private InspectorNavigationTarget currentInspectorTarget;

  /**
   * This flag prevents certain UI operations from executing while the editor is loading an environment.
   */
  private boolean loading;

  /**
   * Ensures that various initialization processes are only carried out once.
   */
  private boolean initialized;
  private boolean fitMode;

  public MapComponent() {
    super(0, 0);
    this.transformModeChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.inspectorNavigationChangedConsumer = new CopyOnWriteArrayList<>();
    this.selectionChangedConsumer = new CopyOnWriteArrayList<>();
    this.loadingConsumer = new CopyOnWriteArrayList<>();
    this.loadedConsumer = new CopyOnWriteArrayList<>();
    this.copyTargetChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusedObjects = Collections.synchronizedMap(new IdentityHashMap<>());
    this.inspectorNavigationHistory = Collections.synchronizedMap(new IdentityHashMap<>());
    this.selectedObjects = Collections.synchronizedMap(new IdentityHashMap<>());
    this.environments = Collections.synchronizedMap(new IdentityHashMap<>());
    this.maps = new CopyOnWriteArrayList<>();
    this.cameraFocus = Collections.synchronizedMap(new IdentityHashMap<>());
    this.cameraZoom = Collections.synchronizedMap(new IdentityHashMap<>());
    UndoManager.onUndoStackChanged(e -> Transform.updateAnchors());
  }

  public static boolean mapIsNull() {
    return Game.world().environment() == null || Game.world().environment().getMap() == null;
  }

  public void onTransformModeChanged(Consumer<TransformMode> cons) {
    this.transformModeChangedConsumer.add(cons);
  }

  public void onFocusChanged(Consumer<IMapObject> cons) {
    this.focusChangedConsumer.add(cons);
  }

  public void onInspectorNavigationChanged(Runnable runnable) {
    this.inspectorNavigationChangedConsumer.add(runnable);
  }

  public void onSelectionChanged(Consumer<List<IMapObject>> cons) {
    this.selectionChangedConsumer.add(cons);
  }

  public void onMapLoading(Consumer<TmxMap> cons) {
    this.loadingConsumer.add(cons);
  }

  public void onMapLoaded(Consumer<TmxMap> cons) {
    this.loadedConsumer.add(cons);
  }

  public void onCopyTargetChanged(Consumer<Blueprint> cons) {
    this.copyTargetChangedConsumer.add(cons);
  }

  @Override
  public void render(Graphics2D g) {
    if (Game.world().environment() == null) {
      return;
    }

    try {
      Renderers.render(g);
    } catch (Exception e) {
      log.log(Level.SEVERE, "Error during map component render: " + e.getMessage(), e);
    }
    super.render(g);
  }

  public void loadMaps(Path projectPath) {
    final List<TmxMap> loadedMaps = new ArrayList<>();

    try (Stream<Path> paths = Files.walk(projectPath)) {
      paths.filter(Files::isRegularFile)
        .filter(p -> p.toString().endsWith(TmxMap.FILE_EXTENSION))
        .forEach(p -> {
            TmxMap map = (TmxMap) Resources.maps().get(p.toString());
            if (map != null) {
              loadedMaps.add(map);
            }
            log.log(Level.INFO, "map found: {0}", new Object[] {p.toString()});
          }
        );
    } catch (Exception e) {
      log.log(Level.WARNING, "Error loading maps from project path: " + projectPath, e);
    }
    this.loadMaps(loadedMaps, true);
  }

  public void loadMaps(List<TmxMap> maps, boolean clearSelection) {
    if (maps == null) {
      return;
    }
    UI.getInspector().bind(null);
    this.setFocus(null, true);
    getMaps().clear();
    getMaps().addAll(sortedMaps(maps));
    UI.getMapController().bind(getMaps(), clearSelection);
  }

  static List<TmxMap> sortedMaps(List<TmxMap> maps) {
    List<TmxMap> sortedMaps = new ArrayList<>(maps);
    Collections.sort(sortedMaps);
    return sortedMaps;
  }

  public List<TmxMap> getMaps() {
    return this.maps;
  }

  public IMapObject getFocusedMapObject() {
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      return this.focusedObjects.get(Game.world().environment().getMap());
    }

    return null;
  }

  public Rectangle2D getFocusBounds() {
    final IMapObject focusedObject = getFocusedMapObject();
    if (focusedObject == null) {
      return null;
    }

    return focusedObject.getBoundingBox();
  }

  public List<IMapObject> getSelectedMapObjects() {
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      final IMap map = Game.world().environment().getMap();
      if (this.selectedObjects.containsKey(map)) {
        return this.selectedObjects.get(map);
      }
    }

    return new ArrayList<>();
  }

  public Blueprint getCopiedBlueprint() {
    return this.copiedBlueprint;
  }

  public boolean isLoading() {
    return this.loading;
  }

  @Override
  public void prepare() {
    Game.world().camera().onZoom(event -> Transform.updateAnchors());

    Zoom.applyPreference();

    if (!this.initialized) {
      Game.window()
        .getRenderComponent()
        .addFocusListener(
          new FocusAdapter() {
            @Override
            public void focusLost(FocusEvent e) {
              startPoint = null;
              inspectorNavigationMouseButton = MouseEvent.NOBUTTON;
            }
          });

      this.setupKeyboardControls();
      this.setupMouseControls();
      Game.window().getRenderComponent().addComponentListener(new ComponentAdapter() {
        @Override
        public void componentResized(ComponentEvent e) {
          refitMapIfNeeded();
        }
      });
      this.initialized = true;
    }

    super.prepare();
  }

  public void loadEnvironment(TmxMap map) {
    if (map == null) {
      if (UI.getMapController() != null) {
        UI.getMapController().setSelection(null);
      }
      return;
    }
    boolean refitAfterLoad = this.fitMode
      && Game.world().environment() != null
      && Game.world().environment().getMap() == map;
    this.loading = true;
    try {
      if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
        final IMap currentMap = Game.world().environment().getMap();
        double x = Game.world().camera().getFocus().getX();
        double y = Game.world().camera().getFocus().getY();
        Point2D newPoint = new Point2D.Double(x, y);
        this.cameraFocus.put(currentMap, newPoint);
        this.cameraZoom.put(currentMap, Game.world().camera().getZoom());
      }

      for (Consumer<TmxMap> cons : this.loadingConsumer) {
        cons.accept(
          Game.world().environment() != null
            ? ((TmxMap) Game.world().environment().getMap())
            : null);
      }

      Point2D newFocus;

      boolean fitOnLoad = !this.cameraFocus.containsKey(map);
      if (!fitOnLoad) {
        newFocus = this.cameraFocus.get(map);
      } else {
        newFocus =
          new Point2D.Double(
            map.getSizeInPixels().getWidth() / 2, map.getSizeInPixels().getHeight() / 2);
        this.cameraFocus.put(map, newFocus);
      }

      Game.world().camera().setFocus(new Point2D.Double(newFocus.getX(), newFocus.getY()));

      if (!this.environments.containsKey(map)) {
        Environment env = new Environment(map);
        env.init();
        this.environments.put(map, env);
      }

      Game.world().loadEnvironment(this.environments.get(map));

      if (!fitOnLoad && !refitAfterLoad && this.cameraZoom.containsKey(map)) {
        this.fitMode = false;
        Game.world().camera().setZoom(this.cameraZoom.get(map), 0);
        if (UI.getViewportToolbar() != null) {
          UI.getViewportToolbar().refreshZoomLabel();
        }
      }

      if (fitOnLoad || refitAfterLoad) {
        SwingUtilities.invokeLater(() -> {
          if (Game.world().environment() != null && Game.world().environment().getMap() == map) {
            this.fitMap();
          }
        });
      }

      if (UI.getMapController() != null) {
        UI.getMapController().setSelection(map);
      }
      IMapObject focused = getFocusedMapObject();
      this.refreshInspector();
      if (UI.getMapController() != null) {
        if (focused == null) {
          UI.showMapProperties();
        } else {
          UI.showObjectInspector();
        }
      }

      for (Consumer<TmxMap> cons : this.loadedConsumer) {
        cons.accept(map);
      }

    } finally {
      this.loading = false;
      Scroll.updateScrollHandlers();
    }
  }

  public boolean canNavigateInspectorBack() {
    InspectorNavigationHistory<InspectorNavigationTarget> history = this.getCurrentInspectorNavigationHistory();
    return history != null && history.canGoBack(this::isNavigableInspectorHistoryEntry);
  }

  public boolean canNavigateInspectorForward() {
    InspectorNavigationHistory<InspectorNavigationTarget> history = this.getCurrentInspectorNavigationHistory();
    return history != null && history.canGoForward(this::isNavigableInspectorHistoryEntry);
  }

  public void navigateInspectorBack() {
    InspectorNavigationHistory<InspectorNavigationTarget> history = this.getCurrentInspectorNavigationHistory();
    if (isMoving || isResizing
      || history == null
      || !history.canGoBack(this::isNavigableInspectorHistoryEntry)) {
      return;
    }
    this.navigateInspectorTo(history.goBack(this::isNavigableInspectorHistoryEntry));
  }

  public void navigateInspectorForward() {
    InspectorNavigationHistory<InspectorNavigationTarget> history = this.getCurrentInspectorNavigationHistory();
    if (isMoving || isResizing
      || history == null
      || !history.canGoForward(this::isNavigableInspectorHistoryEntry)) {
      return;
    }
    this.navigateInspectorTo(history.goForward(this::isNavigableInspectorHistoryEntry));
  }

  public void reloadEnvironment() {
    if (mapIsNull()) {
      return;
    }

    this.loadEnvironment((TmxMap) Game.world().environment().getMap());
  }

  public void add(IMapObject mapObject) {
    this.add(mapObject, UI.getLayerController().getCurrentLayer());
    UndoManager.instance().mapObjectAdded(mapObject);
  }

  public void add(IMapObjectLayer layer) {
    if (layer == null) {
      return;
    }
    getSelectedMapObjects().clear();
    this.setFocus(null, true);
    for (IMapObject mapObject : layer.getMapObjects()) {
      Game.world().environment().loadFromMap(mapObject.getId());

      this.setSelection(mapObject, false);
      this.setFocus(mapObject, false);
    }
    Transform.updateAnchors();
    this.setTransformMode(TransformMode.MOVE);
  }

  public void synchronizeEnvironmentEntities(IMap map) {
    if (map == null) {
      return;
    }

    Environment environment = Game.world().environment();
    if (environment == null || environment.getMap() != map) {
      this.environments.remove(map);
      return;
    }

    Environment rebuilt = new Environment(map);
    Game.world().loadEnvironment(rebuilt);
    this.environments.put(map, rebuilt);
    Transform.updateAnchors();
  }

  public void add(IMapObject mapObject, IMapObjectLayer layer) {
    if (layer == null || mapObject == null) {
      return;
    }

    layer.addMapObject(mapObject);
    Game.world().environment().loadFromMap(mapObject.getId());
    UI.getLayerController().refresh();

    Game.window().getRenderComponent().requestFocus();
    this.setFocus(mapObject, true);
    this.setTransformMode(TransformMode.NONE);
  }

  public boolean addMapObjectAt(Object asset, Point dropPoint) {
    ICamera camera = Game.world().camera();
    if (asset == null || dropPoint == null || Game.world().environment() == null || camera == null) {
      return false;
    }
    return addMapObjectFromAsset(asset, toMapLocation(dropPoint, camera));
  }

  static Point2D toMapLocation(Point2D canvasLocation, ICamera camera) {
    double renderScale = camera.getRenderScale();
    if (!Double.isFinite(renderScale) || renderScale <= 0) {
      renderScale = 1;
    }
    return camera.getMapLocation(new Point2D.Double(
      canvasLocation.getX() / renderScale,
      canvasLocation.getY() / renderScale));
  }

  public List<IMapObject> getMapObjectsAt(Point2D canvasLocation) {
    if (canvasLocation == null || mapIsNull() || Game.world().camera() == null) {
      return List.of();
    }

    return mapObjectsAt(
      Game.world().environment().getMap(),
      toMapLocation(canvasLocation, Game.world().camera()));
  }

  static List<IMapObject> mapObjectsAt(IMap map, Point2D location) {
    if (map == null || location == null) {
      return List.of();
    }

    Rectangle2D point = new Rectangle2D.Double(location.getX(), location.getY(), 0, 0);
    List<IMapObject> matches = new ArrayList<>();
    for (IMapObjectLayer layer : map.getMapObjectLayers()) {
      if (layer == null || !isLayerEffectivelyVisible(map, layer)) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject != null
          && MapObjectType.get(mapObject.getType()) != null
          && GeometricUtilities.intersects(point, mapObject.getBoundingBox())) {
          matches.add(mapObject);
        }
      }
    }
    return matches;
  }

  public boolean addMapObjectFromAsset(Object asset, Point2D location) {
    if (asset == null || location == null || UI.getLayerController() == null
      || UI.getLayerController().getCurrentLayer() == null) {
      return false;
    }
    boolean added = false;
    if (asset instanceof SpritesheetResource spritesheetResource) {
      added = addSpriteFromDrop(spritesheetResource, location);
    } else if (asset instanceof EmitterAttributes emitterData) {
      added = addEmitterFromDrop(emitterData, location);
    } else if (asset instanceof Blueprint blueprint) {
      added = addBlueprintFromDrop(blueprint, location);
    }
    if (added) {
      this.activatePointerTool();
    }
    return added;
  }

  private boolean addSpriteFromDrop(SpritesheetResource spritesheetResource, Point2D location) {
    String propName = PropPanel.getIdentifierBySpriteName(spritesheetResource.getName());
    String creatureName = CreaturePanel.getCreatureSpriteName(spritesheetResource.getName());
    if (propName == null && creatureName == null) {
      return false;
    }

    MapObject mo = new MapObject();
    mo.setType(propName != null ? MapObjectType.PROP.name() : MapObjectType.CREATURE.name());
    mo.setValue(MapObjectProperty.SPRITESHEETNAME, propName != null ? propName : creatureName);

    mo.setX((float) (location.getX() - spritesheetResource.getWidth() / 2.0));
    mo.setY((float) (location.getY() - spritesheetResource.getHeight() / 2.0));
    mo.setWidth(spritesheetResource.getWidth());
    mo.setHeight(spritesheetResource.getHeight());
    mo.setId(Game.world().environment().getNextMapId());
    mo.setName("");
    mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, spritesheetResource.getWidth() * 0.4);
    mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, spritesheetResource.getHeight() * 0.4);
    mo.setValue(MapObjectProperty.COLLISION, true);
    mo.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
    mo.setValue(MapObjectProperty.PROP_ADDSHADOW, true);

    this.add(mo);
    return true;
  }

  private boolean addEmitterFromDrop(EmitterAttributes emitterData, Point2D location) {
    MapObject newEmitter = (MapObject) EmitterMapObjectLoader.createMapObject(emitterData);
    newEmitter.setX((float) (location.getX() - newEmitter.getWidth()));
    newEmitter.setY((float) (location.getY() - newEmitter.getHeight()));
    newEmitter.setId(Game.world().environment().getNextMapId());
    this.add(newEmitter);
    return true;
  }

  private boolean addBlueprintFromDrop(Blueprint blueprint, Point2D location) {
    List<IMapObject> newObjects = blueprint.build(
      (int) location.getX() - blueprint.getWidth() / 2,
      (int) location.getY() - blueprint.getHeight() / 2);
    if (newObjects.isEmpty()) {
      return false;
    }
    UndoManager.instance().beginOperation();
    try {
      newObjects.forEach(obj -> this.add(obj));
      newObjects.forEach(obj -> this.setSelection(obj, false));
    } finally {
      UndoManager.instance().endOperation();
    }
    return true;
  }

  public void delete(IMapObjectLayer layer) {
    if (layer == null) {
      return;
    }

    boolean shadow =
      layer.getMapObjects().stream()
        .anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW);
    for (IMapObject mapObject : layer.getMapObjects()) {
      if (!shadow && MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
        Game.world().environment().updateLighting(mapObject.getBoundingBox());
      }

      Game.world().environment().remove(mapObject.getId());
      if (mapObject.equals(getFocusedMapObject())) {
        this.setFocus(null, true);
      }
      getSelectedMapObjects().remove(mapObject);
    }

    if (shadow) {
      Game.world().environment().updateLighting();
    }
  }

  public static boolean isLayerEffectivelyVisible(IMap map, ILayer target) {
    return map != null && target != null && isLayerEffectivelyVisible(map, target, true);
  }

  static boolean containsLayer(ILayerList parent, ILayer target) {
    if (parent == null || target == null) {
      return false;
    }
    for (ILayer layer : parent.getRenderLayers()) {
      if (layer == target || layer instanceof IGroupLayer group && containsLayer(group, target)) {
        return true;
      }
    }
    return false;
  }

  private static boolean isLayerEffectivelyVisible(ILayerList parent, ILayer target, boolean ancestorsVisible) {
    for (ILayer layer : parent.getRenderLayers()) {
      boolean visible = ancestorsVisible && layer.isVisible() && layer.getOpacity() > 0f;
      if (layer == target) {
        return visible;
      }
      if (layer instanceof IGroupLayer group && isLayerEffectivelyVisible(group, target, visible)) {
        return true;
      }
    }
    return false;
  }

  public void copy() {
    this.setCopyBlueprint(
      new Blueprint(
        "",
        getSelectedMapObjects()
          .toArray(new MapObject[0])));
  }

  public void paste() {
    if (this.copiedBlueprint == null) {
      return;
    }

    int x = (int) Input.mouse().getMapLocation().getX();
    int y = (int) Input.mouse().getMapLocation().getY();

    this.paste(x, y);
  }

  public void paste(int x, int y) {
    if (this.copiedBlueprint == null) {
      return;
    }

    UndoManager.instance().beginOperation();
    try {
      this.setFocus(null, true);
      for (IMapObject mapObject : this.copiedBlueprint.build(x, y)) {
        this.add(mapObject);
        this.setSelection(mapObject, false);
      }

      // clean up copied blueprints in case, we cut the objects and kept the IDs
      if (this.copiedBlueprint.keepIds()) {
        this.setCopyBlueprint(null);
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void cut() {
    this.setCopyBlueprint(
      new Blueprint(
        "",
        true,
        getSelectedMapObjects()
          .toArray(new MapObject[0])));

    UndoManager.instance().beginOperation();
    try {
      for (IMapObject mapObject : getSelectedMapObjects()) {
        // call the undomanager first because otherwise the information about
        // the object's layer will be lost
        UndoManager.instance().mapObjectDeleted(mapObject);
        this.delete(mapObject);
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void clearAll() {
    this.focusedObjects.clear();
    this.inspectorNavigationHistory.clear();
    this.currentInspectorTarget = null;
    this.notifyInspectorNavigationChanged();
    UI.getLayerController().clear();
    this.selectedObjects.clear();
    this.cameraFocus.clear();
    this.cameraZoom.clear();
    this.environments.clear();
    UI.getEntityController().refresh();
    UI.getLayerController().refresh();
  }

  public void delete() {
    if (isSuspended() || !isVisible() || getFocusedMapObject() == null) {
      return;
    }

    UndoManager.instance().beginOperation();
    try {
      for (IMapObject deleteObject : getSelectedMapObjects()) {
        if (deleteObject == null) {
          continue;
        }

        // call the undomanager first because otherwise the information about
        // the object's layer will be lost
        UndoManager.instance().mapObjectDeleted(deleteObject);
        this.delete(deleteObject);
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void selectAll() {
    final List<IMapObjectLayer> layers = Game.world().environment().getMap().getMapObjectLayers();

    ArrayList<IMapObject> selection = new ArrayList<>();
    for (final IMapObjectLayer layer : layers) {
      if (layer == null || !isLayerEffectivelyVisible(Game.world().environment().getMap(), layer)) {
        continue;
      }

      selection.addAll(layer.getMapObjects());

      this.setSelection(selection, true);
    }
  }

  public void deselect() {
    this.setSelection(Collections.emptyList(), true);
  }

  public void delete(final IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    Game.world().environment().getMap().removeMapObject(mapObject.getId());
    Game.world().environment().remove(mapObject.getId());
    UI.getLayerController().refresh();
    UI.getEntityController().remove(mapObject);

    if (mapObject.equals(getFocusedMapObject())) {
      this.setFocus(null, true);
    }
  }

  public void saveEmitter() {
    if (getFocusedMapObject() == null) {
      return;
    }

    Object name =
      JOptionPane.showInputDialog(
        Game.window().getRenderComponent(),
        Resources.strings().get("input_prompt_name"),
        Resources.strings().get("input_prompt_emitter_name_title"),
        JOptionPane.PLAIN_MESSAGE,
        null,
        null,
        getFocusedMapObject().getName());
    if (name == null) {
      return;
    }
    if (getFocusedMapObject().getType().equals(MapObjectType.EMITTER.toString())) {
      Emitter emitter = Game.world().environment().getEmitter(getFocusedMapObject().getId());
      final EmitterAttributes data = emitter.data();
      data.setName(name.toString());

      Editor.instance()
        .getGameFile()
        .getEmitters()
        .removeIf(x -> x.getName().equals(data.getName()));
      Editor.instance().getGameFile().getEmitters().add(data);
    }
  }

  public void defineBlueprint() {
    if (getFocusedMapObject() == null) {
      return;
    }

    Object name =
      JOptionPane.showInputDialog(
        Game.window().getRenderComponent(),
        Resources.strings().get("input_prompt_name"),
        Resources.strings().get("input_prompt_blueprint_name_title"),
        JOptionPane.PLAIN_MESSAGE,
        null,
        null,
        getFocusedMapObject().getName());
    if (name == null) {
      return;
    }
    Blueprint blueprint =
      new Blueprint(
        name.toString(),
        getSelectedMapObjects().toArray(new MapObject[0]));
    Editor.instance().getGameFile().getBluePrints().add(blueprint);
    UI.getAssetController().refresh();
  }

  public void centerCameraOnFocus() {
    if (this.hasFocus() && getFocusedMapObject() != null) {
      final Rectangle2D focus = getFocusBounds();
      if (focus == null) {
        return;
      }

      this.exitFitMode();
      Game.world().camera().setFocus(new Point2D.Double(focus.getCenterX(), focus.getCenterY()));
    }
  }

  public void centerCameraOnMap() {
    final Environment env = Game.world().environment();
    if (env == null) {
      return;
    }

    this.exitFitMode();
    Game.world().camera().setFocus(env.getCenter());
  }

  public void fitMap() {
    if (Game.world() == null
      || Game.world().environment() == null
      || Game.world().environment().getMap() == null
      || Game.world().camera() == null
      || Game.window() == null
      || Game.window().getRenderComponent() == null) {
      return;
    }

    Dimension mapSize = Game.world().environment().getMap().getSizeInPixels();
    Dimension viewportSize = Game.window().getRenderComponent().getSize();
    if (mapSize.width <= 0 || mapSize.height <= 0
      || viewportSize.width <= 0 || viewportSize.height <= 0) {
      return;
    }

    float zoom = calculateFitZoom(mapSize, viewportSize, Editor.preferences().getUiScale());
    this.fitMode = true;
    Game.world().camera().setZoom(zoom, 0);
    Editor.preferences().setZoom(zoom);
    Game.world().camera().setFocus(Game.world().environment().getCenter());
    if (UI.getViewportToolbar() != null) {
      UI.getViewportToolbar().refreshZoomLabel();
    }
  }

  public void refitMapIfNeeded() {
    if (this.fitMode) {
      this.fitMap();
    }
  }

  public void exitFitMode() {
    this.fitMode = false;
  }

  static float calculateFitZoom(Dimension mapSize, Dimension viewportSize, float uiScale) {
    double padding = FIT_PADDING * uiScale;
    double availableWidth = Math.max(1, viewportSize.getWidth() - 2 * padding);
    double availableHeight = Math.max(1, viewportSize.getHeight() - 2 * padding);
    float zoom = (float) Math.min(
      availableWidth / mapSize.getWidth(), availableHeight / mapSize.getHeight());
    return Math.max(MIN_FIT_ZOOM, Math.min(Zoom.getMax(), zoom));
  }

  public TransformMode getTransformMode() {
    return this.transformMode;
  }

  public void setTransformMode(TransformMode transformMode) {
    if (transformMode == this.transformMode) {
      return;
    }

    switch (transformMode) {
      case CREATE -> {
        this.setFocus(null, true);
        UI.getInspector().bind(null);
        Cursors.apply(Cursors.ADD);
      }
      case NONE -> Cursors.apply(Cursors.DEFAULT);
      case MOVE -> Cursors.apply(Cursors.MOVE);
      case RESIZE -> { /* transitional state handled by transform logic */ }
    }

    this.transformMode = transformMode;
    for (Consumer<TransformMode> cons : this.transformModeChangedConsumer) {
      cons.accept(this.transformMode);
    }
  }

  public static IMapObject resolveParentEntity(IMapObject mapObject) {
    IMap map =
      Game.world() != null && Game.world().environment() != null
        ? Game.world().environment().getMap()
        : null;
    return resolveParentEntity(mapObject, map);
  }

  static IMapObject resolveParentEntity(IMapObject mapObject, IMap map) {
    if (mapObject == null) {
      return null;
    }
    MapObjectType type = MapObjectType.get(mapObject.getType());
    if (type == MapObjectType.PROP
      || type == MapObjectType.CREATURE
      || type == MapObjectType.SOUNDSOURCE
      || type == MapObjectType.LIGHTSOURCE) {
      return mapObject;
    }

    if (map != null && mapObject.getBoundingBox() != null) {
      for (IMapObjectLayer layer : map.getMapObjectLayers()) {
        if (layer == null || !isLayerEffectivelyVisible(map, layer)) {
          continue;
        }
        for (IMapObject other : layer.getMapObjects()) {
          if (other == null || other.equals(mapObject)) {
            continue;
          }
          MapObjectType otherType = MapObjectType.get(other.getType());
          if ((otherType == MapObjectType.PROP || otherType == MapObjectType.CREATURE)
              && other.getBoundingBox() != null
              && other.getBoundingBox().intersects(mapObject.getBoundingBox())) {
            return other;
          }
        }
      }
    }
    return mapObject;
  }

  public void setFocus(IMapObject mapObject, boolean clearSelection) {
    if (isFocussing) {
      return;
    }

    isFocussing = true;
    try {
      final IMapObject currentFocus = getFocusedMapObject();
      if (mapObject != null && mapObject.equals(currentFocus)
        || mapObject == null && currentFocus == null) {
        if (mapObject != null) {
          this.setSelection(mapObject, clearSelection);
        }
        return;
      }

      if (mapIsNull()) {
        return;
      }

      if (isMoving || isResizing) {
        return;
      }

      if (mapObject == null) {
        this.focusedObjects.remove(Game.world().environment().getMap());
      } else {
        this.focusedObjects.put(Game.world().environment().getMap(), mapObject);
      }

      this.setSelection(mapObject, clearSelection);
      Transform.updateAnchors();
      UI.getEntityController().select(mapObject);
      this.refreshInspector();
      if (mapObject == null) {
        UI.showMapProperties();
      } else {
        UI.showObjectInspector();
      }

      for (Consumer<IMapObject> cons : this.focusChangedConsumer) {
        cons.accept(mapObject);
      }
    } finally {
      isFocussing = false;
    }
  }

  public boolean isFocussing() {
    return isFocussing;
  }

  public void setSelection(IMapObject mapObject, boolean clearSelection) {
    this.setSelection(
      mapObject == null ? null : Collections.singletonList(mapObject), clearSelection);
  }

  public void setSelection(List<IMapObject> mapObjects, boolean clearSelection) {
    if (mapObjects == null || mapObjects.isEmpty()) {
      getSelectedMapObjects().clear();
      for (Consumer<List<IMapObject>> cons : this.selectionChangedConsumer) {
        cons.accept(getSelectedMapObjects());
      }
      this.refreshInspector();
      return;
    }

    final IMap map = Game.world().environment().getMap();
    this.selectedObjects.putIfAbsent(map, new CopyOnWriteArrayList<>());

    if (clearSelection) {
      getSelectedMapObjects().clear();
    }

    for (IMapObject mapObject : mapObjects) {
      if (!getSelectedMapObjects().contains(mapObject)) {
        getSelectedMapObjects().add(mapObject);
      }
    }

    for (Consumer<List<IMapObject>> cons : this.selectionChangedConsumer) {
      cons.accept(getSelectedMapObjects());
    }
    this.refreshInspector();
  }

  public void refreshInspector() {
    UI.getInspector().bindAll(inspectorSelection(getFocusedMapObject()));
  }

  private List<IMapObject> inspectorSelection(IMapObject focused) {
    List<IMapObject> selection = new ArrayList<>(getSelectedMapObjects());
    if (selection.isEmpty() && focused != null) {
      return List.of(focused);
    }
    if (focused != null && selection.remove(focused)) {
      selection.add(0, focused);
    }
    return selection;
  }

  public void deleteMap() {
    if (getMaps() == null || getMaps().isEmpty()) {
      return;
    }

    if (mapIsNull()) {
      return;
    }

    if (!ConfirmDialog.show(
      Resources.strings().get("hud_deleteMap"),
      Resources.strings().get("hud_deleteMapMessage")
        + "\n"
        + Game.world().environment().getMap().getName())) {
      return;
    }

    IMap deletedMap = Game.world().environment().getMap();
    getMaps().remove(deletedMap);
    clearMapObjectState(deletedMap);

    // TODO: remove all tile sets from the game file that are no longer needed
    // by any other map.
    UI.getMapController().bind(getMaps());
    if (!this.maps.isEmpty()) {
      this.loadEnvironment(this.maps.getFirst());
    } else {
      Game.world().unloadEnvironment();
      UndoManager.clearAll();
      this.clearAll();
    }

    Editor.instance().updateGameFileMaps();
    Objects.requireNonNull(Renderers.get(GridRenderer.class)).clearCache();
  }

  public void importMap() {
    if (getMaps() == null) {
      return;
    }

    XmlImportDialog.importXml(
      Resources.strings().get("resource_tilemap"),
      file -> {
        String mapPath = file.toUri().toString();
        Resources.maps().clear();
        TmxMap map = (TmxMap) Resources.maps().get(mapPath);
        if (map == null) {
          log.log(Level.WARNING, "could not load map from file {0}", new Object[] {mapPath});
          return;
        }

        if (map.getMapObjectLayers().isEmpty()) {

          // make sure there's a map object layer on the map because we need one
          // to add any kind of entities
          MapObjectLayer layer = new MapObjectLayer();
          layer.setName(MapObjectLayer.DEFAULT_MAPOBJECTLAYER_NAME);
          map.addLayer(layer);
        }

        Optional<TmxMap> current =
          this.maps.stream().filter(x -> x.getName().equals(map.getName())).findFirst();
        if (current.isPresent()) {
          if (ConfirmDialog.show(
            Resources.strings().get("input_replace_map_title"),
            Resources.strings().get("input_replace_map", map.getName()))) {
            clearMapObjectState(current.get());
            getMaps().remove(current.get());
          } else {
            return;
          }
        }

        getMaps().add(map);
        Collections.sort(getMaps());

        for (IImageLayer imageLayer : map.getImageLayers()) {
          BufferedImage img =
            Resources.images().get(imageLayer.getImage().getAbsoluteSourcePath(), true);
          if (img == null) {
            continue;
          }

          Spritesheet sprite =
            Resources.spritesheets()
              .load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
          Editor.instance().getGameFile().getSpriteSheets().add(new SpritesheetResource(sprite));
        }

        // remove old spritesheets
        for (ITileset tileSet : map.getTilesets()) {
          Editor.instance().loadTileset(tileSet, true);
        }

        // remove old tilesets
        for (ITileset tileset : map.getExternalTilesets()) {
          Editor.instance().loadTileset(tileset, false);
        }

        Editor.instance().updateGameFileMaps();
        Resources.images().clear();
        Objects.requireNonNull(Renderers.get(GridRenderer.class)).clearCache();
        this.environments.remove(map);

        UI.getMapController().bind(getMaps(), true);
        this.loadEnvironment(map);
        log.log(Level.INFO, "imported map {0}", new Object[] {map.getName()});
      },
      TmxMap.FILE_EXTENSION);
  }

  public void newMap() {
    if (getMaps() == null) {
      return;
    }

    NewMapDialog dialog = new NewMapDialog(Game.window().getHostControl());
    dialog.setVisible(true);
    if (!dialog.isConfirmed()) {
      return;
    }

    IMapOrientation orientation = dialog.getOrientation();
    int mapWidth = dialog.getMapWidth();
    int mapHeight = dialog.getMapHeight();
    int tileWidth = dialog.getTileWidth();
    int tileHeight = dialog.getTileHeight();
    String mapName = dialog.getName().trim();
    if (mapName.isEmpty()) {
      return;
    }

    if (!isMapNameAvailable(null, mapName)) {
      log.log(Level.WARNING, "A map named {0} already exists.", mapName);
      return;
    }

    createMap(
        mapName,
        orientation,
        mapWidth,
        mapHeight,
        tileWidth,
        tileHeight,
        dialog.getStaggerAxis(),
        dialog.getStaggerIndex(),
        dialog.getHexSideLength());
  }

  public TmxMap createMap(
      String mapName,
      IMapOrientation orientation,
      int mapWidth,
      int mapHeight,
      int tileWidth,
      int tileHeight,
      StaggerAxis staggerAxis,
      StaggerIndex staggerIndex,
      int hexSideLength) {
    return createMap(
        mapName,
        orientation,
        mapWidth,
        mapHeight,
        tileWidth,
        tileHeight,
        staggerAxis,
        staggerIndex,
        hexSideLength,
        List.of());
  }

  public TmxMap createMap(
      String mapName,
      IMapOrientation orientation,
      int mapWidth,
      int mapHeight,
      int tileWidth,
      int tileHeight,
      StaggerAxis staggerAxis,
      StaggerIndex staggerIndex,
      int hexSideLength,
      List<Tileset> projectTilesets) {
    if (getMaps() == null
        || mapName == null
        || mapName.isBlank()
        || orientation == null
        || mapWidth <= 0
        || mapHeight <= 0
        || tileWidth <= 0
        || tileHeight <= 0
        || !isMapNameAvailable(null, mapName.trim())) {
      return null;
    }

    TmxMap map = new TmxMap(orientation);
    map.setName(mapName.trim());
    map.setTileWidth(tileWidth);
    map.setTileHeight(tileHeight);
    map.setWidth(mapWidth);
    map.setHeight(mapHeight);

    // Set stagger properties for Staggered and Hexagonal orientations
    if (orientation == MapOrientations.ISOMETRIC_STAGGERED || orientation == MapOrientations.HEXAGONAL) {
      map.setStaggerAxis(staggerAxis != null ? staggerAxis : StaggerAxis.Y);
      map.setStaggerIndex(staggerIndex != null ? staggerIndex : StaggerIndex.ODD);
    }

    // Set hex side length for Hexagonal orientation
    if (orientation == MapOrientations.HEXAGONAL) {
      map.setHexSideLength(hexSideLength);
    }

    long nextFirstGridId = 1;
    if (projectTilesets != null) {
      for (Tileset projectTileset : projectTilesets) {
        if (projectTileset == null || nextFirstGridId > Integer.MAX_VALUE) {
          return null;
        }
        map.getTilesets().add(new Tileset(projectTileset, (int) nextFirstGridId));
        nextFirstGridId += Math.max(1, projectTileset.getTileCount());
      }
    }

    // Add a default map object layer
    MapObjectLayer layer = new MapObjectLayer();
    layer.setName(MapObjectLayer.DEFAULT_MAPOBJECTLAYER_NAME);
    map.addLayer(layer);

    getMaps().add(map);
    Collections.sort(getMaps());

    Editor.instance().updateGameFileMaps();
    GridRenderer gridRenderer = Renderers.get(GridRenderer.class);
    if (gridRenderer != null) {
      gridRenderer.clearCache();
    }
    this.environments.remove(map);

    if (UI.getMapController() != null) {
      UI.getMapController().bind(getMaps(), true);
      this.loadEnvironment(map);
    } else {
      Environment environment = new Environment(map);
      environment.init();
      this.environments.put(map, environment);
      Game.world().loadEnvironment(environment);
    }
    UndoManager.forMap(map).recordChanges();
    log.log(Level.INFO, "created new map {0}", new Object[] {map.getName()});
    return map;
  }

  public boolean renameMap(IMap map, String requestedName) {
    if (map == null || requestedName == null) {
      return false;
    }
    String name = requestedName.trim();
    if (!isMapNameAvailable(map, name)) {
      return false;
    }
    if (name.equals(map.getName())) {
      return true;
    }
    map.setName(name);
    Collections.sort(this.maps);
    if (UI.getMapController() != null) {
      UI.getMapController().refresh();
    }
    return true;
  }

  public boolean canRenameMap(IMap map, String requestedName) {
    return map != null && requestedName != null && isMapNameAvailable(map, requestedName.trim());
  }

  boolean isMapNameAvailable(IMap map, String name) {
    return name != null && !name.isBlank()
      && this.maps.stream().noneMatch(existing -> existing != map && name.equalsIgnoreCase(existing.getName()));
  }

  public void exportMap() {
    if (getMaps() == null || getMaps().isEmpty()) {
      return;
    }

    TmxMap map = (TmxMap) Game.world().environment().getMap();
    if (map == null) {
      return;
    }

    XmlExportDialog.export(
      map,
      Resources.strings().get("resource_map"),
      map.getName(),
      TmxMap.FILE_EXTENSION,
      dirStr -> {
        Path dir = Path.of(dirStr);
        for (ITileset tileSet : map.getTilesets()) {
          String source = tileSet.getImage().getSource();
          ImageFormat format =
            ImageFormat.get(FileUtilities.getExtension(tileSet.getImage().getSource()));
          Path imagePath = dir.resolve(source);
          try {
            ImageIO.write(Resources.spritesheets().get(tileSet.getImage().getSource()).getImage(), format.toFileExtension(), imagePath.toFile());
          } catch (IOException e) {
            log.log(Level.SEVERE, e.getLocalizedMessage(), e);
          }

          Tileset tile = (Tileset) tileSet;
          if (tile.isExternal()) {
            tile.saveSource(dir);
          }
        }
      });
  }

  public IdReassignmentResult reassignIds(TmxMap map, int startID) {
    if (map == null) {
      return new IdReassignmentResult(Collections.emptyList(), Collections.emptyMap(), Collections.emptySet(), Collections.emptyList(), 0);
    }
    List<IdChange> changes = new ArrayList<>();
    Map<Integer, Integer> unambiguousMap = new LinkedHashMap<>();
    Set<Integer> ambiguousOldIds = new LinkedHashSet<>();

    Map<Integer, List<IMapObject>> oldIdGroups = new LinkedHashMap<>();
    for (IMapObject obj : map.getMapObjects()) {
      oldIdGroups.computeIfAbsent(obj.getId(), k -> new ArrayList<>()).add(obj);
    }

    for (Map.Entry<Integer, List<IMapObject>> entry : oldIdGroups.entrySet()) {
      if (entry.getValue().size() > 1) {
        ambiguousOldIds.add(entry.getKey());
      }
    }

    int maxMapId = startID;
    UndoManager.instance().beginOperation();
    for (IMapObject obj : map.getMapObjects()) {
      final int previousId = obj.getId();
      final int newId = maxMapId++;
      UndoManager.instance().mapObjectChanging(obj);
      obj.setId(newId);
      UndoManager.instance().mapObjectChanged(obj, previousId);
      changes.add(new IdChange(previousId, newId, obj.getName()));
      if (!ambiguousOldIds.contains(previousId)) {
        unambiguousMap.put(previousId, newId);
      }
    }

    int updatedReferences = 0;
    List<String> warnings = new ArrayList<>();

    for (IMapObject obj : map.getMapObjects()) {
      for (String propName : new String[] { MapObjectProperty.TRIGGER_TARGETS, MapObjectProperty.TRIGGER_ACTIVATORS }) {
        de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty prop = obj.getProperty(propName);
        if (prop != null && prop.getAsString() != null && !prop.getAsString().isBlank()) {
          IdReferenceRemap remap = remapIdReferences(prop.getAsString(), unambiguousMap, ambiguousOldIds);
          if (remap.replacements() > 0 || !remap.ambiguousReferences().isEmpty()) {
            if (remap.replacements() > 0) {
              UndoManager.instance().mapObjectChanging(obj);
              prop.setValue(remap.value());
              UndoManager.instance().mapObjectChanged(obj, obj.getId());
              updatedReferences += remap.replacements();
            }
            for (int ambiguousId : remap.ambiguousReferences()) {
              warnings.add("Property " + propName + " on object " + obj.getId() + " references ambiguous old ID: " + ambiguousId);
            }
          }
        }
      }
    }

    UndoManager.instance().endOperation();

    if (Game.world().environment() != null && Game.world().environment().getMap() == map) {
      Game.world().environment().clear();
      Game.world().environment().load();
    }
    if (UI.getMapController() != null) {
      UI.getMapController().refresh();
    }
    log.log(Level.INFO, "Reassigned IDs for Map {0}.", new Object[] {map.getName()});

    return new IdReassignmentResult(changes, unambiguousMap, ambiguousOldIds, warnings, updatedReferences);
  }

  @Override
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return isForwardMouseEvents()
      && isVisible()
      && isEnabled()
      && !isSuspended()
      && e != null;
  }

  public void saveMapSnapshot() {
    if (mapIsNull()) {
      return;
    }

    final TmxMap currentMap = (TmxMap) Game.world().environment().getMap();
    Dimension size = currentMap.getOrientation().getSize(currentMap);
    BufferedImage img = new BufferedImage(size.width, size.height, BufferedImage.TYPE_INT_ARGB);
    MapRenderer.render(img.createGraphics(), currentMap, currentMap.getBounds());

    try {
      Path screenshotsDir = Path.of("screenshots");
      if (Files.notExists(screenshotsDir)) {
        Files.createDirectories(screenshotsDir);
      }
      final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      Path filePath = screenshotsDir.resolve(timeStamp + ImageFormat.PNG.toFileExtension());
      ImageIO.write(img, ImageFormat.PNG.toFileExtension(), filePath.toFile());
      log.log(Level.INFO, "Saved map snapshot to {0}", new Object[] {filePath});
    } catch (Exception e) {
      log.log(Level.SEVERE, e.getLocalizedMessage(), e);
    }
  }

  public Rectangle2D getMouseSelectArea(boolean snap) {
    final Point2D start = this.startPoint;
    if (start == null) {
      return null;
    }

    final Point2D endPoint = Input.mouse().getMapLocation();
    double minX = Math.min(start.getX(), endPoint.getX());
    double maxX = Math.max(start.getX(), endPoint.getX());
    double minY = Math.min(start.getY(), endPoint.getY());
    double maxY = Math.max(start.getY(), endPoint.getY());

    if (snap) {
      minX = Snap.x(minX);
      maxX = Snap.x(maxX);
      minY = Snap.y(minY);
      maxY = Snap.y(maxY);
    }

    final TmxMap map = (TmxMap) Game.world().environment().getMap();
    if (map != null && Editor.preferences().clampToMap()) {
      minX = Math.clamp(minX, 0, map.getSizeInPixels().width);
      maxX = Math.clamp(maxX, 0, map.getSizeInPixels().width);
      minY = Math.clamp(minY, 0, map.getSizeInPixels().height);
      maxY = Math.clamp(maxY, 0, map.getSizeInPixels().height);
    }

    double width = Math.abs(minX - maxX);
    double height = Math.abs(minY - maxY);

    return new Rectangle2D.Double(minX, minY, width, height);
  }

  public void setCreateDefinition(ProjectCodeIntegration.Definition definition) {
    this.activatePointerTool();
    this.createDefinition = definition;
    this.setTransformMode(TransformMode.CREATE);
  }

  private void clearMapObjectState(IMap map) {
    if (map == null) {
      return;
    }
    boolean activeMap = Game.world().environment() != null
      && map == Game.world().environment().getMap();
    if (activeMap) {
      setFocus(null, true);
      setSelection((IMapObject) null, true);
    }
    this.focusedObjects.remove(map);
    this.inspectorNavigationHistory.remove(map);
    this.notifyInspectorNavigationChanged();
    this.selectedObjects.remove(map);
    this.cameraFocus.remove(map);
    this.cameraZoom.remove(map);
    this.environments.remove(map);
    UndoManager.remove(map);
    if (UI.getLayerController() instanceof de.gurkenlabs.utiliti.view.components.SceneGraph sceneGraph) {
      sceneGraph.clearMapState(map);
    }
  }

  Environment getCachedEnvironmentForTest(IMap map) {
    return this.environments.get(map);
  }

  public void setCreateMapObjectType(MapObjectType type) {
    this.activatePointerTool();
    this.createDefinition = null;
    this.setTransformMode(TransformMode.CREATE);
    UI.getInspector().setMapObjectType(type);
  }

  private void activatePointerTool() {
    if (ToolManager.instance().getActiveTool() instanceof PointerTool) {
      return;
    }
    PointerTool pointer = ToolManager.instance().getTool(PointerTool.class);
    if (pointer != null) {
      ToolManager.instance().setActiveTool(pointer);
    }
  }

  private IMapObject createNewMapObject(MapObjectType type) {
    final Rectangle2D newObjectArea = getMouseSelectArea(true);
    IMapObject mo =
        createMapObjectWithEditorDefaults(
            type,
            (float) newObjectArea.getX(),
            (float) newObjectArea.getY(),
            (float) newObjectArea.getWidth(),
            (float) newObjectArea.getHeight());
    mo.setId(Game.world().environment().getNextMapId());

    if (this.createDefinition != null) {
      mo.setValue(MapObjectProperty.IMPLEMENTATION, this.createDefinition.id());
      for (var property : this.createDefinition.properties()) {
        setDefaultValue(mo, property);
      }
    }

    this.add(mo);
    return mo;
  }

  /**
   * Creates a map object with the same built-in defaults used by the visual map editor.
   *
   * <p>The returned object is not assigned an ID and is not added to a map. Callers can apply
   * overrides and validate the completed object before mutating the current map.
   */
  public static MapObject createMapObjectWithEditorDefaults(
      MapObjectType type, float x, float y, float width, float height) {
    Objects.requireNonNull(type, "type");

    MapObject mo = new MapObject();
    mo.setType(type.toString());
    mo.setX(x);
    mo.setY(y);
    mo.setWidth(width == 0 ? 16 : width);
    mo.setHeight(height == 0 ? 16 : height);
    mo.setName("");

    switch (type) {
      case EMITTER:
        EmitterAttributes defaultData = new EmitterAttributes();
        defaultData.initDefaults();
        defaultData.setWidth(mo.getWidth());
        defaultData.setHeight(mo.getHeight());
        defaultData.setName(mo.getName());
        EmitterMapObjectLoader.updateMapObject(defaultData, mo);
        break;
      case PROP:
        String propSprite =
            getDefaultSpriteName(MapObjectType.PROP, Resources.spritesheets().getAll());
        if (propSprite != null) {
          mo.setValue(MapObjectProperty.SPRITESHEETNAME, propSprite);
        }
        mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, mo.getWidth() * 0.4);
        mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, mo.getHeight() * 0.4);
        mo.setValue(MapObjectProperty.COLLISION, true);
        mo.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
        mo.setValue(MapObjectProperty.PROP_ADDSHADOW, true);
        break;
      case CREATURE:
        String creatureSprite =
            getDefaultSpriteName(MapObjectType.CREATURE, Resources.spritesheets().getAll());
        if (creatureSprite != null) {
          mo.setValue(MapObjectProperty.SPRITESHEETNAME, creatureSprite);
        }
        break;
      case LIGHTSOURCE:
        mo.setValue(MapObjectProperty.LIGHT_COLOR, Color.WHITE);
        mo.setValue(MapObjectProperty.LIGHT_INTENSITY, 100);
        mo.setValue(MapObjectProperty.LIGHT_SHAPE, "ellipse");
        mo.setValue(MapObjectProperty.LIGHT_ACTIVE, true);
        break;
      case COLLISIONBOX:
        mo.setValue(MapObjectProperty.COLLISION_TYPE, Collision.STATIC);
        break;
      case SOUNDSOURCE:
        mo.setValue(MapObjectProperty.SOUND_VOLUME, 1);
        mo.setValue(MapObjectProperty.SOUND_LOOP, true);
        mo.setValue(MapObjectProperty.SOUND_RANGE, Game.audio().getMaxDistance());
        break;
      case SPAWNPOINT:
      case AREA:
      case STATICSHADOW:
      case TRIGGER:
        break;
    }

    return mo;
  }

  static String getDefaultSpriteName(MapObjectType type, java.util.Collection<Spritesheet> spritesheets) {
    java.util.Set<String> names = switch (type) {
      case PROP -> SpriteVariantSelector.selectBasePropSpriteNames(spritesheets).keySet();
      case CREATURE -> SpriteVariantSelector.selectBaseCreatureSpriteNames(spritesheets).keySet();
      default -> java.util.Set.of();
    };
    return names.stream().sorted().findFirst().orElse(null);
  }

  private static void setDefaultValue(IMapObject mapObject, de.gurkenlabs.litiengine.environment.tilemap.MapObjectPropertyDefinition property) {
    switch (property.type()) {
      case BOOLEAN -> mapObject.setValue(property.name(), Boolean.parseBoolean(property.defaultValue()));
      case INTEGER -> mapObject.setValue(property.name(), Integer.parseInt(property.defaultValue()));
      case FLOAT -> mapObject.setValue(property.name(), Float.parseFloat(property.defaultValue()));
      case STRING -> mapObject.setValue(property.name(), property.defaultValue());
    }
  }

  private void setCopyBlueprint(Blueprint copyTarget) {
    this.copiedBlueprint = copyTarget;
    for (Consumer<Blueprint> consumer : this.copyTargetChangedConsumer) {
      consumer.accept(this.copiedBlueprint);
    }
  }

  private void setupKeyboardControls() {
    Input.keyboard()
      .onKeyPressed(
        KeyEvent.VK_CONTROL,
        e -> {
          if (this.transformMode == TransformMode.NONE && getFocusBounds() != null) {
            this.setTransformMode(TransformMode.MOVE);
          }
        });

    Input.keyboard()
      .onKeyReleased(
        KeyEvent.VK_CONTROL,
        e -> {
          if (this.transformMode == TransformMode.MOVE) {
            this.setTransformMode(TransformMode.NONE);
          }
        });

    Input.keyboard()
      .onKeyPressed(
        KeyEvent.VK_ESCAPE,
        e -> {
          if (this.transformMode == TransformMode.CREATE) {
            this.setTransformMode(TransformMode.NONE);
          }
        });

    Input.keyboard()
      .onKeyReleased(
        e -> {
          if (e.getKeyCode() != KeyEvent.VK_RIGHT
            && e.getKeyCode() != KeyEvent.VK_LEFT
            && e.getKeyCode() != KeyEvent.VK_UP
            && e.getKeyCode() != KeyEvent.VK_DOWN) {
            return;
          }

          // if one of the move buttons is still pressed, don't end the operation
          if (Input.keyboard().isPressed(KeyEvent.VK_RIGHT)
            || Input.keyboard().isPressed(KeyEvent.VK_LEFT)
            || Input.keyboard().isPressed(KeyEvent.VK_UP)
            || Input.keyboard().isPressed(KeyEvent.VK_DOWN)) {
            return;
          }

          this.afterArrowKeysReleased();
        });

    Input.keyboard().onKeyPressed(KeyEvent.VK_RIGHT, e -> {
      if (shouldHandleArrowTransform(e.getModifiersEx())) {
        this.handleKeyboardTransform(1, 0);
      }
    });
    Input.keyboard().onKeyPressed(KeyEvent.VK_LEFT, e -> {
      if (shouldHandleArrowTransform(e.getModifiersEx())) {
        this.handleKeyboardTransform(-1, 0);
      }
    });
    Input.keyboard().onKeyPressed(KeyEvent.VK_UP, e -> this.handleKeyboardTransform(0, -1));
    Input.keyboard().onKeyPressed(KeyEvent.VK_DOWN, e -> this.handleKeyboardTransform(0, 1));
  }

  static boolean shouldHandleArrowTransform(int modifiers) {
    return (modifiers & InputEvent.ALT_DOWN_MASK) == 0;
  }

  private void handleKeyboardTransform(int x, int y) {
    if (!Game.window().getRenderComponent().hasFocus()) {
      return;
    }

    SwingUtilities.invokeLater(
      () -> {
        this.beforeArrowKeyPressed();
        Transform.moveEntities(getSelectedMapObjects(), x, y);
      });
  }

  private void beforeArrowKeyPressed() {
    if (!isMovingWithKeyboard) {
      UndoManager.instance().beginOperation();
      for (IMapObject selected : getSelectedMapObjects()) {
        UndoManager.instance().mapObjectChanging(selected);
      }

      isMovingWithKeyboard = true;
    }

    Transform.startDragging(getSelectedMapObjects());
  }

  private void afterArrowKeysReleased() {
    if (isMovingWithKeyboard) {
      SwingUtilities.invokeLater(
        () -> {
          for (IMapObject selected : getSelectedMapObjects()) {
            UndoManager.instance().mapObjectMoved(selected);
          }

          UndoManager.instance().endOperation();
          isMovingWithKeyboard = false;
          Transform.resetDragging();
        });
    }
  }

  private void setupMouseControls() {
    this.onMouseWheelScrolled(this::handleMouseWheelScrolled);
    this.onMouseMoved(this::handleMouseMoved);
    this.onMousePressed(this::handleMousePressed);
    this.onMouseDragged(this::handleMouseDragged);
    this.onMouseReleased(this::handleMouseReleased);
  }

  private void handleMouseWheelScrolled(ComponentMouseWheelEvent e) {
    if (!this.hasFocus() || mapIsNull() || e.getEvent().getWheelRotation() == 0) {
      return;
    }

    // horizontal scrolling
    if (Input.keyboard().isPressed(KeyEvent.VK_SHIFT) || e.getEvent().isShiftDown()) {
      if (e.getEvent().getWheelRotation() < 0) {
        Scroll.left();
      } else {
        Scroll.right();
      }

      return;
    }

    if (Input.keyboard().isPressed(KeyEvent.VK_ALT)) {
      if (e.getEvent().getWheelRotation() < 0) {
        Zoom.in();
      } else {
        Zoom.out();
      }

      return;
    }

    if (e.getEvent().getWheelRotation() < 0) {
      Scroll.up();
    } else {
      Scroll.down();
    }
  }

  /***
   * Handles the mouse moved event and executes the following:
   * <ol>
   * <li>Set cursor image depending on the hovered transform control</li>
   * <li>Update the currently active transform field.</li>
   * </ol>
   *
   * @param e
   *          The mouse event of the calling {@link GuiComponent}
   */
  void handleMouseMoved(ComponentMouseEvent e) {
    Tool active = ToolManager.instance().getActiveTool();
    if (active == null) {
      return;
    }
    if (!(active instanceof PointerTool)) {
      active.mouseMoved(e);
      return;
    }
    Transform.updateTransform();
  }

  void handleMousePressed(ComponentMouseEvent e) {
    if (e != null && this.handleInspectorNavigationMousePressed(e.getEvent())) {
      return;
    }

    Tool active = ToolManager.instance().getActiveTool();
    if (active == null) {
      return;
    }
    if (!(active instanceof PointerTool)) {
      active.mousePressed(e);
      return;
    }
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    if (Transform.mode() == TransformMode.MOVE) {
      this.setTransformMode(TransformMode.MOVE);
    }

    switch (this.transformMode) {
      case CREATE, MOVE -> {
        if (SwingUtilities.isLeftMouseButton(e.getEvent())) {
          this.startPoint = Input.mouse().getMapLocation();
        }
      }
      case NONE -> {
        if (isMoving
          || Transform.mode() == TransformMode.RESIZE
          || SwingUtilities.isRightMouseButton(e.getEvent())) {
          return;
        }

        this.startPoint = Input.mouse().getMapLocation();
      }
      default -> {
      }
    }
  }

  static int inspectorNavigationDirection(int mouseButton) {
    return switch (mouseButton) {
      case 4 -> -1;
      case 5 -> 1;
      default -> 0;
    };
  }

  public boolean handleInspectorNavigationMousePressed(MouseEvent event) {
    if (event == null) {
      return false;
    }
    int direction = inspectorNavigationDirection(event.getButton());
    if (direction == 0) {
      return false;
    }
    if (event.getButton() != this.inspectorNavigationMouseButton
      || event.getWhen() != this.inspectorNavigationMousePressedAt) {
      this.inspectorNavigationMouseButton = event.getButton();
      this.inspectorNavigationMousePressedAt = event.getWhen();
      if (direction < 0) {
        this.navigateInspectorBack();
      } else {
        this.navigateInspectorForward();
      }
    }
    event.consume();
    return true;
  }

  public boolean handleInspectorNavigationMouseReleased(MouseEvent event) {
    if (event == null || inspectorNavigationDirection(event.getButton()) == 0) {
      return false;
    }
    if (event.getButton() == this.inspectorNavigationMouseButton) {
      this.inspectorNavigationMouseButton = MouseEvent.NOBUTTON;
    }
    event.consume();
    return true;
  }

  public void inspectorMapShown(IMap map) {
    if (map != null) {
      this.inspectorTargetShown(InspectorNavigationTarget.map(map));
    }
  }

  public void inspectorObjectShown(IMapObject mapObject) {
    if (!mapIsNull() && mapObject != null) {
      this.inspectorTargetShown(
        InspectorNavigationTarget.object(Game.world().environment().getMap(), mapObject));
    }
  }

  public void inspectorLayerShown(ILayer layer) {
    if (!mapIsNull() && layer != null) {
      this.inspectorTargetShown(
        InspectorNavigationTarget.layer(Game.world().environment().getMap(), layer));
    }
  }

  public void showLayerInspector(ILayer layer) {
    if (layer == null) {
      return;
    }
    this.navigatingInspector = true;
    try {
      this.setFocus(null, true);
    } finally {
      this.navigatingInspector = false;
    }
    UI.showLayerProperties(layer);
  }

  public void inspectorSpriteShown(SpritesheetResource sprite) {
    if (!mapIsNull() && sprite != null) {
      this.inspectorTargetShown(
        InspectorNavigationTarget.sprite(Game.world().environment().getMap(), sprite));
    }
  }

  private void inspectorTargetShown(InspectorNavigationTarget target) {
    this.currentInspectorTarget = target;
    if (!this.navigatingInspector) {
      this.inspectorNavigationHistory
        .computeIfAbsent(target.map(), ignored -> new InspectorNavigationHistory<>(target))
        .record(target);
    }
    this.notifyInspectorNavigationChanged();
  }

  private InspectorNavigationHistory<InspectorNavigationTarget> getCurrentInspectorNavigationHistory() {
    if (mapIsNull()) {
      return null;
    }
    return this.inspectorNavigationHistory.get(Game.world().environment().getMap());
  }

  private boolean isNavigableInspectorHistoryEntry(InspectorNavigationTarget target) {
    if (target == null || mapIsNull() || target.map() != Game.world().environment().getMap()) {
      return false;
    }
    boolean exists = switch (target.type()) {
      case MAP -> true;
      case OBJECT -> target.value() instanceof IMapObject mapObject
        && target.map().getMapObjects().contains(mapObject);
      case LAYER -> target.value() instanceof ILayer layer && containsLayer(target.map(), layer);
      case SPRITE -> target.value() instanceof SpritesheetResource sprite
        && Editor.instance().getGameFile().getSpriteSheets().contains(sprite);
    };
    return exists && !Objects.equals(target, this.currentInspectorTarget);
  }

  private void navigateInspectorTo(InspectorNavigationTarget target) {
    this.navigatingInspector = true;
    try {
      switch (target.type()) {
        case MAP -> {
          this.setFocus(null, true);
          UI.showMapProperties();
        }
        case OBJECT -> {
          this.setFocus((IMapObject) target.value(), true);
          UI.showObjectInspector();
        }
        case LAYER -> {
          this.setFocus(null, true);
          if (UI.getLayerController() instanceof SceneGraph sceneGraph) {
            sceneGraph.selectLayerForInspector((ILayer) target.value());
          }
          UI.showLayerProperties((ILayer) target.value());
        }
        case SPRITE -> UI.showSpriteInspector((SpritesheetResource) target.value());
      }
    } finally {
      this.navigatingInspector = false;
      this.notifyInspectorNavigationChanged();
    }
  }

  private void notifyInspectorNavigationChanged() {
    for (Runnable runnable : this.inspectorNavigationChangedConsumer) {
      runnable.run();
    }
  }

  void handleMouseDragged(ComponentMouseEvent e) {
    if (this.inspectorNavigationMouseButton != MouseEvent.NOBUTTON) {
      if (e != null && e.getEvent() != null) {
        e.getEvent().consume();
      }
      return;
    }

    Tool active = ToolManager.instance().getActiveTool();
    if (active == null) {
      return;
    }
    if (!(active instanceof PointerTool)) {
      active.mouseDragged(e);
      return;
    }
    if (e == null || e.getEvent() == null || !isPrimaryButtonDown(e.getEvent())) {
      return;
    }
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    switch (this.transformMode) {
      case NONE -> {
        if (Transform.mode() == TransformMode.RESIZE) {
          if (!isResizing) {
            isResizing = true;

            UndoManager.instance().beginOperation();
            UndoManager.instance().mapObjectChanging(getFocusedMapObject());
          }

          Transform.resize();
        }
      }
      case MOVE -> {
        if (!isMoving) {
          isMoving = true;

          UndoManager.instance().beginOperation();
          for (IMapObject selected : getSelectedMapObjects()) {
            UndoManager.instance().mapObjectChanging(selected);
          }
        }

        Transform.move();
      }
      default -> {
      }
    }
  }

  static boolean isPrimaryButtonDown(MouseEvent event) {
    return event != null && (event.getModifiersEx() & InputEvent.BUTTON1_DOWN_MASK) != 0;
  }

  void handleMouseReleased(ComponentMouseEvent e) {
    if (e != null && this.handleInspectorNavigationMouseReleased(e.getEvent())) {
      return;
    }

    Tool active = ToolManager.instance().getActiveTool();
    if (active == null) {
      return;
    }
    if (!(active instanceof PointerTool)) {
      active.mouseReleased(e);
      return;
    }
    if (!this.hasFocus() || mapIsNull() || !SwingUtilities.isLeftMouseButton(e.getEvent())) {
      return;
    }

    Transform.resetDragging();

    switch (this.transformMode) {
      case CREATE -> {
        UndoManager undoManager = UndoManager.instance();
        undoManager.beginOperation();
        try {
          MapObjectType type = this.createDefinition == null ? UI.getInspector().getObjectType() : this.createDefinition.baseType();
          IMapObject mo = this.createNewMapObject(type);

          this.setFocus(mo, !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
          this.setTransformMode(TransformMode.NONE);
        } finally {
          undoManager.endOperation();
        }
      }
      case MOVE -> {
        if (isMoving) {
          isMoving = false;

          for (IMapObject selected : getSelectedMapObjects()) {
            UndoManager.instance().mapObjectMoved(selected);
          }

          UndoManager.instance().endOperation();
          this.setTransformMode(TransformMode.NONE);
        } else {
          this.setTransformMode(TransformMode.NONE);
          this.evaluateFocus();
        }
      }
      case NONE -> {
        if (isMoving || isResizing) {
          boolean moved = isMoving;
          isMoving = false;
          isResizing = false;
          if (moved) {
            UndoManager.instance().mapObjectMoved(getFocusedMapObject());
          } else {
            UndoManager.instance().mapObjectResized(getFocusedMapObject());
          }
          UndoManager.instance().endOperation();
        }

        if (this.startPoint == null) {
          return;
        }

        this.evaluateFocus();
      }
      default -> {
      }
    }

    this.startPoint = null;
  }

  private void evaluateFocus() {
    Rectangle2D rect = getMouseSelectArea(false);
    if (rect == null) {
      return;
    }

    boolean somethingIsFocused = false;
    boolean currentObjectFocused = false;
    for (IMapObjectLayer layer : Game.world().environment().getMap().getMapObjectLayers()) {
      if (layer == null || !isLayerEffectivelyVisible(Game.world().environment().getMap(), layer)) {
        continue;
      }

      for (IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        MapObjectType type = MapObjectType.get(mapObject.getType());
        if (type == null || !GeometricUtilities.intersects(rect, mapObject.getBoundingBox())) {
          continue;
        }

        if (getFocusedMapObject() != null
          && mapObject.getId() == getFocusedMapObject().getId()) {
          currentObjectFocused = true;
          continue;
        }

        if (somethingIsFocused) {
          if (rect.getWidth() == 0 && rect.getHeight() == 0) {
            break;
          }

          this.setSelection(mapObject, false);
          continue;
        }
        if (getSelectedMapObjects().contains(mapObject)) {
          getSelectedMapObjects().remove(mapObject);
        } else {
          this.setFocus(
            resolveParentEntity(mapObject),
            !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
        }
        somethingIsFocused = true;
      }
    }

    if (!somethingIsFocused && !currentObjectFocused) {
      this.setFocus(null, true);
      this.setSelection(Collections.emptyList(), true);
    }
  }

  private boolean hasFocus() {
    if (isSuspended() || !isVisible()) {
      return false;
    }

    for (GuiComponent comp : getComponents()) {
      if (comp.isHovered() && !comp.isSuspended()) {
        return false;
      }
    }

    return true;
  }

  public boolean deleteMap(TmxMap map) {
    if (map == null) {
      return false;
    }
    boolean isCurrentMap = Game.world().environment() != null && Game.world().environment().getMap() == map;
    if (getMaps().remove(map)) {
      if (Editor.instance().getGameFile() != null) {
        Editor.instance().getGameFile().getMaps().remove(map);
      }
      if (isCurrentMap) {
        TmxMap nextMap = getMaps().isEmpty() ? null : getMaps().get(0);
        loadEnvironment(nextMap);
      } else if (UI.getMapController() != null) {
        UI.getMapController().bind(getMaps(), true);
      }
      return true;
    }
    return false;
  }

  public record IdChange(int oldId, int newId, String name) {}
  public record IdReassignmentResult(List<IdChange> changes, Map<Integer, Integer> unambiguousMapping, Set<Integer> ambiguousOldIds, List<String> warnings, int updatedReferences) {}
  public record IdReferenceRemap(String value, Set<Integer> ambiguousReferences, int replacements) {}

  public static IdReferenceRemap remapIdReferences(String rawValue, Map<Integer, Integer> unambiguousMap, Set<Integer> ambiguousOldIds) {
    if (rawValue == null || rawValue.isBlank()) {
      return new IdReferenceRemap(rawValue, Collections.emptySet(), 0);
    }
    Set<Integer> ambiguousFound = new LinkedHashSet<>();
    int count = 0;
    String[] parts = rawValue.split(",");
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < parts.length; i++) {
      String rawPart = parts[i];
      int startIdx = 0;
      while (startIdx < rawPart.length() && Character.isWhitespace(rawPart.charAt(startIdx))) {
        startIdx++;
      }
      int endIdx = rawPart.length();
      while (endIdx > startIdx && Character.isWhitespace(rawPart.charAt(endIdx - 1))) {
        endIdx--;
      }
      String leadingSpace = rawPart.substring(0, startIdx);
      String trailingSpace = rawPart.substring(endIdx);
      String part = rawPart.substring(startIdx, endIdx);
      try {
        int parsedId = Integer.parseInt(part);
        if (ambiguousOldIds != null && ambiguousOldIds.contains(parsedId)) {
          ambiguousFound.add(parsedId);
        }
        if (unambiguousMap != null && unambiguousMap.containsKey(parsedId)) {
          sb.append(leadingSpace).append(unambiguousMap.get(parsedId)).append(trailingSpace);
          count++;
        } else {
          sb.append(rawPart);
        }
      } catch (NumberFormatException e) {
        sb.append(rawPart);
      }
      if (i < parts.length - 1) {
        sb.append(",");
      }
    }
    return new IdReferenceRemap(sb.toString(), ambiguousFound, count);
  }
}
