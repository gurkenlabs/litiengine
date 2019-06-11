package de.gurkenlabs.utiliti.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.Stroke;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RoundRectangle2D;
import java.awt.image.BufferedImage;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.TextRenderer;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.gui.ComponentMouseWheelEvent;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.Style;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.handlers.Snap;
import de.gurkenlabs.utiliti.handlers.Transform.ResizeAnchor;
import de.gurkenlabs.utiliti.handlers.Transform.TransformType;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.swing.dialogs.XmlImportDialog;

public class MapComponent extends GuiComponent implements IUpdateable {

  public static final int EDITMODE_CREATE = 0;
  public static final int EDITMODE_EDIT = 1;

  /**
   * @deprecated Will be replaced by {@link TransformType#MOVE}
   */
  @Deprecated()
  public static final int EDITMODE_MOVE = 2;
  private static final Logger log = Logger.getLogger(MapComponent.class.getName());

  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private static final int DEFAULT_ZOOM_INDEX = 3;
  private static final String DEFAULT_MAPOBJECTLAYER_NAME = "default";
  private static final int TRANSFORM_RECT_SIZE = 6;
  private static final int BASE_SCROLL_SPEED = 50;

  private double currentTransformRectSize = TRANSFORM_RECT_SIZE;
  private final java.util.Map<ResizeAnchor, Rectangle2D> transformRects;

  private final List<IntConsumer> editModeChangedConsumer;
  private final List<Consumer<IMapObject>> focusChangedConsumer;
  private final List<Consumer<List<IMapObject>>> selectionChangedConsumer;
  private final List<Consumer<TmxMap>> loadingConsumer;
  private final List<Consumer<TmxMap>> loadedConsumer;
  private final List<Consumer<Blueprint>> copyTargetChangedConsumer;

  private final java.util.Map<String, Point2D> cameraFocus;
  private final java.util.Map<String, IMapObject> focusedObjects;
  private final java.util.Map<String, List<IMapObject>> selectedObjects;
  private final java.util.Map<String, Environment> environments;
  private final java.util.Map<IMapObject, Point2D> dragLocationMapObjects;

  private int currentEditMode = EDITMODE_EDIT;
  private TransformType currentTransform;
  private ResizeAnchor currentAnchor;

  private int currentZoomIndex = 7;

  private final List<TmxMap> maps;

  private float scrollSpeed = BASE_SCROLL_SPEED;

  private Point2D startPoint;
  private Point2D dragPoint;

  private boolean isMoving;
  private boolean isMovingWithKeyboard;
  private boolean isTransforming;
  private boolean isFocussing;
  private float dragSizeHeight;
  private float dragSizeWidth;
  private Rectangle2D newObjectArea;
  private Blueprint copiedBlueprint;

  private Color colorSelectionBorder;
  private float focusBorderBrightness = 0;
  private boolean focusBorderBrightnessIncreasing = true;

  private final EditorScreen screen;

  private boolean loading;
  private boolean initialized;

  public MapComponent(final EditorScreen screen) {
    super(0, EditorScreen.instance().getPadding(), Game.window().getResolution().getWidth(), Game.window().getResolution().getHeight() - Game.window().getResolution().getHeight() * 1 / 15);
    this.editModeChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.selectionChangedConsumer = new CopyOnWriteArrayList<>();
    this.loadingConsumer = new CopyOnWriteArrayList<>();
    this.loadedConsumer = new CopyOnWriteArrayList<>();
    this.copyTargetChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusedObjects = new ConcurrentHashMap<>();
    this.selectedObjects = new ConcurrentHashMap<>();
    this.environments = new ConcurrentHashMap<>();
    this.maps = new ArrayList<>();
    this.cameraFocus = new ConcurrentHashMap<>();
    this.transformRects = new ConcurrentHashMap<>();
    this.dragLocationMapObjects = new ConcurrentHashMap<>();
    this.screen = screen;
    Game.world().camera().onZoomChanged(zoom -> {
      this.currentTransformRectSize = TRANSFORM_RECT_SIZE / zoom;
      this.updateTransformControls();
    });

    UndoManager.onUndoStackChanged(e -> this.updateTransformControls());
  }

  public void onEditModeChanged(IntConsumer cons) {
    this.editModeChangedConsumer.add(cons);
  }

  public void onFocusChanged(Consumer<IMapObject> cons) {
    this.focusChangedConsumer.add(cons);
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

    renderGrid(g);

    final BasicStroke shapeStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
    if (Program.preferences().isRenderBoundingBoxes()) {
      renderMapObjectBounds(g);
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      this.renderNewObjectArea(g, shapeStroke);
      break;
    case EDITMODE_EDIT:
      this.renderMouseSelectionArea(g, shapeStroke);
      break;
    default:
      break;
    }

    this.renderSelection(g);
    this.renderFocus(g);

    super.render(g);
  }

  public void loadMaps(String projectPath) {
    final List<String> files = FileUtilities.findFilesByExtension(new ArrayList<>(), Paths.get(projectPath), "tmx");
    log.log(Level.INFO, "{0} maps found in folder {1}", new Object[] { files.size(), projectPath });
    final List<TmxMap> loadedMaps = new ArrayList<>();
    for (final String mapFile : files) {
      TmxMap map = (TmxMap) Resources.maps().get(mapFile);
      if (map != null) { // if an error occurred or it's not in the expected
                         // format
        loadedMaps.add(map);
        log.log(Level.INFO, "map found: {0}", new Object[] { map.getName() });
      }
    }

    this.loadMaps(loadedMaps);
  }

  public void loadMaps(List<TmxMap> maps) {
    if (maps == null) {
      return;
    }
    UI.getInspector().bind(null);
    this.setFocus(null, true);
    this.getMaps().clear();

    Collections.sort(maps);

    this.getMaps().addAll(maps);
    UI.getMapController().bind(this.getMaps(), true);
  }

  public List<TmxMap> getMaps() {
    return this.maps;
  }

  public IMapObject getFocusedMapObject() {
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      return this.focusedObjects.get(Game.world().environment().getMap().getName());
    }

    return null;
  }

  public List<IMapObject> getSelectedMapObjects() {
    if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
      final String map = Game.world().environment().getMap().getName();
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
    Game.world().camera().setZoom(zooms[this.currentZoomIndex], 0);
    if (!this.initialized) {
      Game.window().getRenderComponent().addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          startPoint = null;
        }
      });

      this.setupKeyboardControls();
      this.setupMouseControls();
      this.initialized = true;

      Game.loop().attach(this);
    }

    super.prepare();
  }

  public void loadEnvironment(TmxMap map) {
    if (map == null) {
      return;
    }
    this.loading = true;
    try {
      if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
        final String mapName = Game.world().environment().getMap().getName();
        double x = Game.world().camera().getFocus().getX();
        double y = Game.world().camera().getFocus().getY();
        Point2D newPoint = new Point2D.Double(x, y);
        this.cameraFocus.put(mapName, newPoint);
      }

      for (Consumer<TmxMap> cons : this.loadingConsumer) {
        cons.accept(Game.world().environment() != null ? (TmxMap) Game.world().environment().getMap() : null);
      }

      Point2D newFocus = null;

      if (this.cameraFocus.containsKey(map.getName())) {
        newFocus = this.cameraFocus.get(map.getName());
      } else {
        newFocus = new Point2D.Double(map.getSizeInPixels().getWidth() / 2, map.getSizeInPixels().getHeight() / 2);
        this.cameraFocus.put(map.getName(), newFocus);
      }

      Game.world().camera().setFocus(new Point2D.Double(newFocus.getX(), newFocus.getY()));

      if (!this.environments.containsKey(map.getName())) {
        Environment env = new Environment(map);
        env.init();
        this.environments.put(map.getName(), env);
      }

      Game.world().loadEnvironment(this.environments.get(map.getName()));

      UI.updateScrollBars();
      UI.getMapController().setSelection(map.getName());
      UI.getInspector().bind(this.getFocusedMapObject());

      for (Consumer<TmxMap> cons : this.loadedConsumer) {
        cons.accept(map);
      }

    } finally {
      this.loading = false;
    }
  }

  public void reloadEnvironment() {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
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
    this.getSelectedMapObjects().clear();
    this.setFocus(null, true);
    for (IMapObject mapObject : layer.getMapObjects()) {
      Game.world().environment().loadFromMap(mapObject.getId());

      this.setSelection(mapObject, false);
      this.setFocus(mapObject, false);
    }
    this.updateTransformControls();
    this.setEditMode(EDITMODE_MOVE);
  }

  public void add(IMapObject mapObject, IMapObjectLayer layer) {
    if (layer == null || mapObject == null) {
      return;
    }

    layer.addMapObject(mapObject);
    Game.world().environment().loadFromMap(mapObject.getId());

    Game.window().getRenderComponent().requestFocus();
    this.setFocus(mapObject, false);
    this.setEditMode(EDITMODE_EDIT);
  }

  public void delete(IMapObjectLayer layer) {
    if (layer == null) {
      return;
    }

    boolean shadow = layer.getMapObjects().stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW);
    for (IMapObject mapObject : layer.getMapObjects()) {
      if (!shadow && MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
        Game.world().environment().updateLighting(mapObject.getBoundingBox());
      }

      Game.world().environment().remove(mapObject.getId());
      if (mapObject.equals(this.getFocusedMapObject())) {
        this.setFocus(null, true);
      }
      this.getSelectedMapObjects().remove(mapObject);
    }

    if (shadow) {
      Game.world().environment().updateLighting();
    }
  }

  public void copy() {
    this.setCopyBlueprint(new Blueprint("", this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()])));
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
    this.setCopyBlueprint(new Blueprint("", true, this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()])));

    UndoManager.instance().beginOperation();
    try {
      for (IMapObject mapObject : this.getSelectedMapObjects()) {
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
    UI.getLayerController().clear();
    this.selectedObjects.clear();
    this.cameraFocus.clear();
    this.environments.clear();
  }

  public void delete() {
    if (this.isSuspended() || !this.isVisible() || this.getFocusedMapObject() == null) {
      return;
    }

    UndoManager.instance().beginOperation();
    try {
      for (IMapObject deleteObject : this.getSelectedMapObjects()) {
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
      UI.getEntityController().refresh();
    }
  }

  public void selectAll() {
    final List<IMapObjectLayer> layers = Game.world().environment().getMap().getMapObjectLayers();

    ArrayList<IMapObject> selection = new ArrayList<>();
    for (final IMapObjectLayer layer : layers) {
      if (layer == null || !layer.isVisible()) {
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

    if (mapObject.equals(this.getFocusedMapObject())) {
      this.setFocus(null, true);
    }
  }

  public void defineBlueprint() {
    if (this.getFocusedMapObject() == null) {
      return;
    }

    Object name = JOptionPane.showInputDialog(Game.window().getRenderComponent(), Resources.strings().get("input_prompt_name"), Resources.strings().get("input_prompt_name_title"), JOptionPane.PLAIN_MESSAGE, null, null, this.getFocusedMapObject().getName());
    if (name == null) {
      return;
    }

    Blueprint blueprint = new Blueprint(name.toString(), this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()]));

    EditorScreen.instance().getGameFile().getBluePrints().add(blueprint);
  }

  public void centerCameraOnFocus() {
    if (this.hasFocus() && this.getFocusedMapObject() != null) {
      final Rectangle2D focus = this.getFocus();
      if (focus == null) {
        return;
      }

      Game.world().camera().setFocus(new Point2D.Double(focus.getCenterX(), focus.getCenterY()));
    }
  }

  public void centerCameraOnMap() {
    final Environment env = Game.world().environment();
    if (env == null) {
      return;
    }

    Game.world().camera().setFocus(env.getCenter());
  }

  public void setEditMode(int editMode) {
    if (editMode == this.currentEditMode) {
      return;
    }

    switch (editMode) {
    case EDITMODE_CREATE:
      this.setFocus(null, true);
      UI.getInspector().bind(null);
      Game.window().getRenderComponent().setCursor(Cursors.ADD, 0, 0);
      break;
    case EDITMODE_EDIT:
      Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
      break;
    case EDITMODE_MOVE:
      Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
      break;
    default:
      break;
    }

    this.currentEditMode = editMode;
    for (IntConsumer cons : this.editModeChangedConsumer) {
      cons.accept(this.currentEditMode);
    }
  }

  public void setFocus(IMapObject mapObject, boolean clearSelection) {
    if (this.isFocussing) {
      return;
    }

    this.isFocussing = true;
    try {
      final IMapObject currentFocus = this.getFocusedMapObject();
      if (mapObject != null && currentFocus != null && mapObject.equals(currentFocus) || mapObject == null && currentFocus == null) {
        return;
      }

      if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
        return;
      }

      if (this.isMoving || this.isTransforming) {
        return;
      }

      UI.getInspector().bind(mapObject);
      UI.getEntityController().select(mapObject);
      if (mapObject == null) {
        this.focusedObjects.remove(Game.world().environment().getMap().getName());
      } else {
        this.focusedObjects.put(Game.world().environment().getMap().getName(), mapObject);
      }

      for (Consumer<IMapObject> cons : this.focusChangedConsumer) {
        cons.accept(mapObject);
      }

      this.updateTransformControls();
      this.setSelection(mapObject, clearSelection);
    } finally {
      this.isFocussing = false;
    }
  }

  public void setSelection(IMapObject mapObject, boolean clearSelection) {
    this.setSelection(mapObject == null ? null : Collections.singletonList(mapObject), clearSelection);
  }

  public void setSelection(List<IMapObject> mapObjects, boolean clearSelection) {
    if (mapObjects == null || mapObjects.isEmpty()) {
      this.getSelectedMapObjects().clear();
      for (Consumer<List<IMapObject>> cons : this.selectionChangedConsumer) {
        cons.accept(this.getSelectedMapObjects());
      }
      return;
    }

    final String map = Game.world().environment().getMap().getName();
    if (!this.selectedObjects.containsKey(map)) {
      this.selectedObjects.put(map, new CopyOnWriteArrayList<>());
    }

    if (clearSelection) {
      this.getSelectedMapObjects().clear();
    }

    for (IMapObject mapObject : mapObjects) {
      if (!this.getSelectedMapObjects().contains(mapObject)) {
        this.getSelectedMapObjects().add((MapObject) mapObject);
      }
    }

    for (Consumer<List<IMapObject>> cons : this.selectionChangedConsumer) {
      cons.accept(this.getSelectedMapObjects());
    }
  }

  public void updateTransformControls() {
    final Rectangle2D focus = this.getFocus();
    if (focus == null) {
      this.transformRects.clear();
      return;
    }

    for (ResizeAnchor trans : ResizeAnchor.values()) {
      Rectangle2D transRect = new Rectangle2D.Double(this.getTransX(trans, focus), this.getTransY(trans, focus), this.currentTransformRectSize, this.currentTransformRectSize);
      this.transformRects.put(trans, transRect);
    }
  }

  public void deleteMap() {
    if (this.getMaps() == null || this.getMaps().isEmpty()) {
      return;
    }

    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("hud_deleteMapMessage") + "\n" + Game.world().environment().getMap().getName(), Resources.strings().get("hud_deleteMap"), JOptionPane.YES_NO_OPTION);
    if (n != JOptionPane.YES_OPTION) {
      return;
    }

    this.getMaps().removeIf(x -> x.getName().equals(Game.world().environment().getMap().getName()));

    // TODO: remove all tile sets from the game file that are no longer needed
    // by any other map.
    UI.getMapController().bind(this.getMaps());
    if (!this.maps.isEmpty()) {
      this.loadEnvironment(this.maps.get(0));
    } else {
      this.loadEnvironment(null);
    }

    EditorScreen.instance().updateGameFileMaps();
  }

  public void importMap() {
    if (this.getMaps() == null) {
      return;
    }

    XmlImportDialog.importXml("Tilemap", file -> {
      String mapPath = file.toURI().toString();
      Resources.maps().clear();
      TmxMap map = (TmxMap) Resources.maps().get(mapPath);
      if (map == null) {
        log.log(Level.WARNING, "could not load map from file {0}", new Object[] { mapPath });
        return;
      }

      if (map.getMapObjectLayers().isEmpty()) {

        // make sure there's a map object layer on the map because we need one
        // to add any kind of entities
        MapObjectLayer layer = new MapObjectLayer();
        layer.setName(DEFAULT_MAPOBJECTLAYER_NAME);
        map.addLayer(layer);
      }

      Optional<TmxMap> current = this.maps.stream().filter(x -> x.getName().equals(map.getName())).findFirst();
      if (current.isPresent()) {
        int n = JOptionPane.showConfirmDialog(Game.window().getRenderComponent(), Resources.strings().get("input_replace_map", map.getName()), Resources.strings().get("input_replace_map_title"), JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
          this.getMaps().remove(current.get());

        } else {
          return;
        }
      }

      this.getMaps().add(map);
      Collections.sort(this.getMaps());

      for (IImageLayer imageLayer : map.getImageLayers()) {
        BufferedImage img = Resources.images().get(imageLayer.getImage().getAbsoluteSourcePath(), true);
        if (img == null) {
          continue;
        }

        Spritesheet sprite = Resources.spritesheets().load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        this.screen.getGameFile().getSpriteSheets().add(new SpritesheetResource(sprite));
      }

      // remove old spritesheets
      for (ITileset tileSet : map.getTilesets()) {
        this.loadTileset(tileSet, true);
      }

      // remove old tilesets
      for (ITileset tileset : map.getExternalTilesets()) {
        this.loadTileset(tileset, false);
      }

      EditorScreen.instance().updateGameFileMaps();
      Resources.images().clear();
      if (this.environments.containsKey(map.getName())) {
        this.environments.remove(map.getName());
      }

      UI.getMapController().bind(this.getMaps(), true);
      this.loadEnvironment(map);
      log.log(Level.INFO, "imported map {0}", new Object[] { map.getName() });
    }, TmxMap.FILE_EXTENSION);
  }

  public void loadTileset(ITileset tileset, boolean embedded) {
    Spritesheet sprite = Resources.spritesheets().get(tileset.getImage().getSource());
    if (sprite != null) {
      Resources.spritesheets().remove(sprite.getName());
      this.screen.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(sprite.getName()));
    }

    Spritesheet newSprite = Resources.spritesheets().load(tileset);
    SpritesheetResource info = new SpritesheetResource(newSprite);
    EditorScreen.instance().getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(info.getName()));
    EditorScreen.instance().getGameFile().getSpriteSheets().add(info);
    EditorScreen.instance().loadSpriteSheets(Arrays.asList(info), true);
    if (!embedded) {
      this.screen.getGameFile().getTilesets().removeIf(x -> x.getName().equals(tileset.getName()));
      this.screen.getGameFile().getTilesets().add((Tileset) tileset);
    }
  }

  public void exportMap() {
    if (this.getMaps() == null || this.getMaps().isEmpty()) {
      return;
    }

    TmxMap map = (TmxMap) Game.world().environment().getMap();
    if (map == null) {
      return;
    }

    this.exportMap(map);
  }

  public void exportMap(TmxMap map) {
    XmlExportDialog.export(map, "Map", map.getName(), TmxMap.FILE_EXTENSION, dir -> {
      for (ITileset tileSet : map.getTilesets()) {
        ImageFormat format = ImageFormat.get(FileUtilities.getExtension(tileSet.getImage().getSource()));
        ImageSerializer.saveImage(Paths.get(dir, tileSet.getImage().getSource()).toString(), Resources.spritesheets().get(tileSet.getImage().getSource()).getImage(), format);

        Tileset tile = (Tileset) tileSet;
        if (tile.isExternal()) {
          tile.saveSource(dir);
        }
      }
    });
  }

  public void zoomIn() {
    if (this.currentZoomIndex < zooms.length - 1) {
      this.currentZoomIndex++;
    }

    this.setCurrentZoom();
    this.updateScrollSpeed();
  }

  public void zoomOut() {
    if (this.currentZoomIndex > 0) {
      this.currentZoomIndex--;
    }

    this.setCurrentZoom();
    this.updateScrollSpeed();
  }

  public void reassignIds(IMap map, int startID) {
    int maxMapId = startID;
    UndoManager.instance().beginOperation();
    for (IMapObject obj : map.getMapObjects()) {
      final int previousId = obj.getId();
      UndoManager.instance().mapObjectChanging(obj);
      obj.setId(maxMapId);
      UndoManager.instance().mapObjectChanged(obj, previousId);
      maxMapId++;
    }
    UndoManager.instance().endOperation();

    Game.world().environment().clear();
    Game.world().environment().load();
    UI.getMapController().refresh();
  }

  @Override
  public void update() {
    if (this.focusBorderBrightness <= 0.4) {
      this.focusBorderBrightnessIncreasing = true;
    } else if (this.focusBorderBrightness >= 0.9) {
      this.focusBorderBrightnessIncreasing = false;
    }

    if (this.focusBorderBrightnessIncreasing && this.focusBorderBrightness < 0.9) {
      this.focusBorderBrightness += 0.01;
    } else if (!focusBorderBrightnessIncreasing && this.focusBorderBrightness >= 0.4) {
      this.focusBorderBrightness -= 0.01;
    }

    this.colorSelectionBorder = Color.getHSBColor(0, 0, this.focusBorderBrightness);
  }

  @Override
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isForwardMouseEvents() && this.isVisible() && this.isEnabled() && !this.isSuspended() && e != null;
  }

  private void updateScrollSpeed() {
    this.scrollSpeed = BASE_SCROLL_SPEED / zooms[this.currentZoomIndex];
  }

  private static boolean mapIsNull() {
    return Game.world().environment() == null || Game.world().environment().getMap() == null;
  }

  private IMapObject createNewMapObject(MapObjectType type) {
    IMapObject mo = new MapObject();
    mo.setType(type.toString());
    mo.setX((float) this.newObjectArea.getX());
    mo.setY((float) this.newObjectArea.getY());

    // ensure a minimum size for the new object
    float width = (float) this.newObjectArea.getWidth();
    float height = (float) this.newObjectArea.getHeight();
    mo.setWidth(width == 0 ? 16 : width);
    mo.setHeight(height == 0 ? 16 : height);
    mo.setId(Game.world().environment().getNextMapId());
    mo.setName("");

    switch (type) {
    case PROP:
      mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, (this.newObjectArea.getWidth() * 0.4));
      mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, (this.newObjectArea.getHeight() * 0.4));
      mo.setValue(MapObjectProperty.COLLISION, true);
      mo.setValue(MapObjectProperty.COMBAT_INDESTRUCTIBLE, false);
      mo.setValue(MapObjectProperty.PROP_ADDSHADOW, true);
      break;
    case LIGHTSOURCE:
      mo.setValue(MapObjectProperty.LIGHT_COLOR, Color.WHITE);
      mo.setValue(MapObjectProperty.LIGHT_SHAPE, "ellipse");
      mo.setValue(MapObjectProperty.LIGHT_ACTIVE, true);
      break;
    case COLLISIONBOX:
      mo.setValue(MapObjectProperty.COLLISION_TYPE, Collision.STATIC);
      break;
    case SPAWNPOINT:
    default:
      break;
    }

    this.add(mo);
    return mo;
  }

  private Rectangle2D getCurrentMouseSelectionArea(boolean snap) {
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

    final IMap map = Game.world().environment().getMap();
    if (map != null && Program.preferences().clampToMap()) {
      minX = MathUtilities.clamp(minX, 0, map.getSizeInPixels().width);
      maxX = MathUtilities.clamp(maxX, 0, map.getSizeInPixels().width);
      minY = MathUtilities.clamp(minY, 0, map.getSizeInPixels().height);
      maxY = MathUtilities.clamp(maxY, 0, map.getSizeInPixels().height);
    }

    double width = Math.abs(minX - maxX);
    double height = Math.abs(minY - maxY);

    return new Rectangle2D.Double(minX, minY, width, height);
  }

  private Rectangle2D getFocus() {
    final IMapObject focusedObject = this.getFocusedMapObject();
    if (focusedObject == null) {
      return null;
    }

    return focusedObject.getBoundingBox();
  }

  private double getTransX(ResizeAnchor type, Rectangle2D focus) {
    switch (type) {
    case DOWN:
    case UP:
      return focus.getCenterX() - this.currentTransformRectSize / 2;
    case LEFT:
    case DOWNLEFT:
    case UPLEFT:
      return focus.getX() - this.currentTransformRectSize;
    case RIGHT:
    case DOWNRIGHT:
    case UPRIGHT:
      return focus.getMaxX();
    default:
      return 0;
    }
  }

  private double getTransY(ResizeAnchor type, Rectangle2D focus) {
    switch (type) {
    case DOWN:
    case DOWNLEFT:
    case DOWNRIGHT:
      return focus.getMaxY();
    case UP:
    case UPLEFT:
    case UPRIGHT:
      return focus.getY() - this.currentTransformRectSize;
    case LEFT:
    case RIGHT:
      return focus.getCenterY() - this.currentTransformRectSize / 2;
    default:
      return 0;
    }
  }

  private void handleTransform() {
    final IMapObject transformObject = this.getFocusedMapObject();
    if (transformObject == null || this.currentEditMode != EDITMODE_EDIT || currentTransform != TransformType.RESIZE || this.currentAnchor == null) {
      return;
    }

    if (this.dragPoint == null) {
      this.dragPoint = Input.mouse().getMapLocation();
      this.dragLocationMapObjects.put(this.getFocusedMapObject(), new Point2D.Double(transformObject.getX(), transformObject.getY()));
      this.dragSizeHeight = transformObject.getHeight();
      this.dragSizeWidth = transformObject.getWidth();
      return;
    }

    Point2D dragLocationMapObject = this.dragLocationMapObjects.get(this.getFocusedMapObject());
    double deltaX = Input.mouse().getMapLocation().getX() - this.dragPoint.getX();
    double deltaY = Input.mouse().getMapLocation().getY() - this.dragPoint.getY();
    double newWidth = this.dragSizeWidth;
    double newHeight = this.dragSizeHeight;
    double newX = dragLocationMapObject.getX();
    double newY = dragLocationMapObject.getY();

    switch (this.currentAnchor) {
    case DOWN:
      newHeight += deltaY;
      break;
    case DOWNRIGHT:
      newHeight += deltaY;
      newWidth += deltaX;
      break;
    case DOWNLEFT:
      newHeight += deltaY;
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + this.dragSizeWidth);
      break;
    case LEFT:
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + this.dragSizeWidth);
      break;
    case RIGHT:
      newWidth += deltaX;
      break;
    case UP:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + this.dragSizeHeight);
      break;
    case UPLEFT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + this.dragSizeHeight);
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, dragLocationMapObject.getX() + this.dragSizeWidth);
      break;
    case UPRIGHT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, dragLocationMapObject.getY() + this.dragSizeHeight);
      newWidth += deltaX;
      break;
    default:
      return;
    }

    newX = Snap.x(newX);
    newY = Snap.y(newY);
    newWidth = Snap.x(newWidth);
    newHeight = Snap.y(newHeight);

    final IMap map = Game.world().environment().getMap();
    if (map != null && Program.preferences().clampToMap()) {
      newX = MathUtilities.clamp(newX, 0, map.getSizeInPixels().width);
      newY = MathUtilities.clamp(newX, 0, map.getSizeInPixels().height);

      newWidth = MathUtilities.clamp(newWidth, 0, map.getSizeInPixels().width - newX);
      newHeight = MathUtilities.clamp(newHeight, 0, map.getSizeInPixels().height - newY);
    }

    transformObject.setWidth((float) newWidth);
    transformObject.setHeight((float) newHeight);

    transformObject.setX((float) newX);
    transformObject.setY((float) newY);

    Game.world().environment().reloadFromMap(transformObject.getId());
    MapObjectType type = MapObjectType.get(transformObject.getType());
    if (type == MapObjectType.LIGHTSOURCE) {
      Game.world().environment().updateLighting(transformObject.getBoundingBox());
    }

    if (type == MapObjectType.STATICSHADOW) {
      Game.world().environment().updateLighting();
    }

    UI.getInspector().bind(transformObject);
    this.updateTransformControls();
  }

  private void handleSelectedEntitiesDrag() {
    if (!this.isMoving) {
      this.isMoving = true;

      UndoManager.instance().beginOperation();
      for (IMapObject selected : this.getSelectedMapObjects()) {
        UndoManager.instance().mapObjectChanging(selected);
      }
    }

    IMapObject minX = null;
    IMapObject minY = null;
    for (IMapObject selected : this.getSelectedMapObjects()) {
      if (minX == null || selected.getX() < minX.getX()) {
        minX = selected;
      }

      if (minY == null || selected.getY() < minY.getY()) {
        minY = selected;
      }
    }

    if (minX == null || minY == null || (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && this.currentEditMode != EDITMODE_MOVE)) {
      return;
    }

    if (this.dragPoint == null) {
      this.dragPoint = Input.mouse().getMapLocation();
      return;
    }

    if (!this.dragLocationMapObjects.containsKey(minX)) {
      this.dragLocationMapObjects.put(minX, new Point2D.Double(minX.getX(), minX.getY()));
    }

    if (!this.dragLocationMapObjects.containsKey(minY)) {
      this.dragLocationMapObjects.put(minY, new Point2D.Double(minY.getX(), minY.getY()));
    }

    Point2D dragLocationMapObjectMinX = this.dragLocationMapObjects.get(minX);
    Point2D dragLocationMapObjectMinY = this.dragLocationMapObjects.get(minY);

    double deltaX = Input.mouse().getMapLocation().getX() - this.dragPoint.getX();
    float newX = Snap.x(dragLocationMapObjectMinX.getX() + deltaX);
    float snappedDeltaX = newX - minX.getX();

    double deltaY = Input.mouse().getMapLocation().getY() - this.dragPoint.getY();
    float newY = Snap.y(dragLocationMapObjectMinY.getY() + deltaY);
    float snappedDeltaY = newY - minY.getY();

    if (snappedDeltaX == 0 && snappedDeltaY == 0) {
      return;
    }

    final Rectangle2D beforeBounds = MapObject.getBounds2D(this.getSelectedMapObjects());
    this.handleEntityDrag(snappedDeltaX, snappedDeltaY);

    if (this.getSelectedMapObjects().stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW)) {
      Game.world().environment().updateLighting();
    } else if (this.getSelectedMapObjects().stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.LIGHTSOURCE)) {
      final Rectangle2D afterBounds = MapObject.getBounds2D(this.getSelectedMapObjects());
      double x = Math.min(beforeBounds.getX(), afterBounds.getX());
      double y = Math.min(beforeBounds.getY(), afterBounds.getY());
      double width = Math.max(beforeBounds.getMaxX(), afterBounds.getMaxX()) - x;
      double height = Math.max(beforeBounds.getMaxY(), afterBounds.getMaxY()) - y;
      Game.world().environment().updateLighting(new Rectangle2D.Double(x, y, width, height));
    }
  }

  private void handleEntityDrag(float snappedDeltaX, float snappedDeltaY) {
    final IMap map = Game.world().environment().getMap();

    for (IMapObject selected : this.getSelectedMapObjects()) {

      float newX = selected.getX() + snappedDeltaX;
      float newY = selected.getY() + snappedDeltaY;
      if (Program.preferences().clampToMap()) {
        newX = MathUtilities.clamp(newX, 0, map.getSizeInPixels().width - selected.getWidth());
        newY = MathUtilities.clamp(newY, 0, map.getSizeInPixels().height - selected.getHeight());
      }

      selected.setX(newX);
      selected.setY(newY);

      IEntity entity = Game.world().environment().get(selected.getId());
      if (entity != null) {
        entity.setX(selected.getLocation().getX());
        entity.setY(selected.getLocation().getY());
      } else {
        Game.world().environment().reloadFromMap(selected.getId());
      }

      if (selected.equals(this.getFocusedMapObject())) {
        UI.getInspector().bind(selected);
        this.updateTransformControls();
      }
    }
  }

  private void setCurrentZoom() {
    Game.world().camera().setZoom(zooms[this.currentZoomIndex], 0);
  }

  private void setCopyBlueprint(Blueprint copyTarget) {
    this.copiedBlueprint = copyTarget;
    for (Consumer<Blueprint> consumer : this.copyTargetChangedConsumer) {
      consumer.accept(this.copiedBlueprint);
    }
  }

  private void setupKeyboardControls() {
    Input.keyboard().onKeyPressed(KeyEvent.VK_CONTROL, e -> {
      if (this.currentEditMode == EDITMODE_EDIT && this.getFocus() != null) {
        this.setEditMode(EDITMODE_MOVE);
      }
    });

    Input.keyboard().onKeyReleased(KeyEvent.VK_CONTROL, e -> {
      if (this.currentEditMode == EDITMODE_MOVE) {
        this.setEditMode(EDITMODE_EDIT);
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_ESCAPE, e -> {
      if (this.currentEditMode == EDITMODE_CREATE) {
        this.setEditMode(EDITMODE_EDIT);
      }
    });

    Input.keyboard().onKeyReleased(e -> {
      if (e.getKeyCode() != KeyEvent.VK_RIGHT && e.getKeyCode() != KeyEvent.VK_LEFT && e.getKeyCode() != KeyEvent.VK_UP && e.getKeyCode() != KeyEvent.VK_DOWN) {
        return;
      }

      // if one of the move buttons is still pressed, don't end the operation
      if (Input.keyboard().isPressed(KeyEvent.VK_RIGHT) || Input.keyboard().isPressed(KeyEvent.VK_LEFT) || Input.keyboard().isPressed(KeyEvent.VK_UP) || Input.keyboard().isPressed(KeyEvent.VK_DOWN)) {
        return;
      }

      if (this.isMovingWithKeyboard) {
        for (IMapObject selected : this.getSelectedMapObjects()) {
          UndoManager.instance().mapObjectChanged(selected);
        }

        UndoManager.instance().endOperation();
        this.isMovingWithKeyboard = false;
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_RIGHT, e -> {
      if (!Game.window().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(1, 0);
      afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_LEFT, e -> {
      if (!Game.window().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(-1, 0);
      afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_UP, e -> {
      if (!Game.window().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(0, -1);
      afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_DOWN, e -> {
      if (!Game.window().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(0, 1);
      afterKeyPressed();
    });
  }

  private void beforeKeyPressed() {
    if (!this.isMovingWithKeyboard) {
      UndoManager.instance().beginOperation();
      for (IMapObject selected : this.getSelectedMapObjects()) {
        UndoManager.instance().mapObjectChanging(selected);
      }

      this.isMovingWithKeyboard = true;
    }
  }

  private static void afterKeyPressed() {
    EditorScreen.instance().getMapComponent().updateTransformControls();
  }

  private void setupMouseControls() {
    this.onMouseWheelScrolled(this::handleMouseWheelScrolled);
    this.onMouseMoved(this::handleMouseMoved);
    this.onMousePressed(this::handleMousePressed);
    this.onMouseDragged(this::handleMouseDragged);
    this.onMouseReleased(this::handleMouseReleased);
  }

  private void handleMouseWheelScrolled(ComponentMouseWheelEvent e) {
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    final Point2D currentFocus = Game.world().camera().getFocus();
    // horizontal scrolling
    if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && this.dragPoint == null) {
      if (e.getEvent().getWheelRotation() < 0) {

        Point2D newFocus = new Point2D.Double(currentFocus.getX() - this.scrollSpeed, currentFocus.getY());
        Game.world().camera().setFocus(newFocus);
      } else {
        Point2D newFocus = new Point2D.Double(currentFocus.getX() + this.scrollSpeed, currentFocus.getY());
        Game.world().camera().setFocus(newFocus);
      }

      UI.getHorizontalScrollBar().setValue((int) Game.world().camera().getViewport().getCenterX());
      return;
    }

    if (Input.keyboard().isPressed(KeyEvent.VK_ALT)) {
      if (e.getEvent().getWheelRotation() < 0) {
        this.zoomIn();
      } else {
        this.zoomOut();
      }

      return;
    }

    if (e.getEvent().getWheelRotation() < 0) {
      Point2D newFocus = new Point2D.Double(currentFocus.getX(), currentFocus.getY() - this.scrollSpeed);
      Game.world().camera().setFocus(newFocus);

    } else {
      Point2D newFocus = new Point2D.Double(currentFocus.getX(), currentFocus.getY() + this.scrollSpeed);
      Game.world().camera().setFocus(newFocus);
    }

    UI.getVerticalcrollBar().setValue((int) Game.world().camera().getViewport().getCenterY());
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
  private void handleMouseMoved(ComponentMouseEvent e) {
    boolean hovered = false;
    if (this.getFocus() != null) {
      this.currentAnchor = null;

      for (Entry<ResizeAnchor, Rectangle2D> entry : this.transformRects.entrySet()) {
        Rectangle2D rect = entry.getValue();
        Rectangle2D hoverrect = new Rectangle2D.Double(rect.getX() - rect.getWidth() * 2.5, rect.getY() - rect.getHeight() * 2.5, rect.getWidth() * 5, rect.getHeight() * 5);
        if (hoverrect.contains(Input.mouse().getMapLocation())) {
          hovered = true;
          if (entry.getKey() == ResizeAnchor.DOWN || entry.getKey() == ResizeAnchor.UP) {
            Game.window().getRenderComponent().setCursor(Cursors.TRANS_VERTICAL, 0, 0);
          } else if (entry.getKey() == ResizeAnchor.UPLEFT || entry.getKey() == ResizeAnchor.DOWNRIGHT) {
            Game.window().getRenderComponent().setCursor(Cursors.TRANS_DIAGONAL_LEFT, 0, 0);
          } else if (entry.getKey() == ResizeAnchor.UPRIGHT || entry.getKey() == ResizeAnchor.DOWNLEFT) {
            Game.window().getRenderComponent().setCursor(Cursors.TRANS_DIAGONAL_RIGHT, 0, 0);
          } else {
            Game.window().getRenderComponent().setCursor(Cursors.TRANS_HORIZONTAL, 0, 0);
          }

          this.currentAnchor = entry.getKey();
          this.currentTransform = TransformType.RESIZE;
          break;
        }
      }
    }

    if (!hovered) {
      boolean moveMode = false;

      for (IMapObject selected : this.getSelectedMapObjects()) {
        if (selected.getBoundingBox().contains(Input.mouse().getMapLocation())) {
          moveMode = true;
          break;
        }
      }

      if (moveMode) {
        Game.window().getRenderComponent().setCursor(Cursors.MOVE, 0, 0);
        this.currentTransform = TransformType.MOVE;
      } else {
        Game.window().getRenderComponent().setCursor(Cursors.DEFAULT, 0, 0);
        this.currentTransform = TransformType.NONE;
      }
    }
  }

  private void handleMousePressed(ComponentMouseEvent e) {
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    if (this.currentTransform == TransformType.MOVE) {
      this.setEditMode(EDITMODE_MOVE);
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
    case EDITMODE_MOVE:
      if (SwingUtilities.isLeftMouseButton(e.getEvent())) {
        this.startPoint = Input.mouse().getMapLocation();
      }
      break;
    case EDITMODE_EDIT:
      if (this.isMoving || this.currentTransform == TransformType.RESIZE || SwingUtilities.isRightMouseButton(e.getEvent())) {
        return;
      }

      final Point2D mouse = Input.mouse().getMapLocation();
      this.startPoint = mouse;
      break;
    default:
      break;
    }
  }

  private void handleMouseDragged(ComponentMouseEvent e) {
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      if (startPoint == null) {
        return;
      }

      if (SwingUtilities.isLeftMouseButton(e.getEvent())) {
        newObjectArea = this.getCurrentMouseSelectionArea(true);
      }

      break;
    case EDITMODE_EDIT:
      if (this.currentTransform == TransformType.RESIZE) {
        if (!this.isTransforming) {
          this.isTransforming = true;
          UndoManager.instance().mapObjectChanging(this.getFocusedMapObject());
        }

        this.handleTransform();
      }
      break;
    case EDITMODE_MOVE:
      this.handleSelectedEntitiesDrag();

      break;
    default:
      break;
    }
  }

  private void handleMouseReleased(ComponentMouseEvent e) {
    if (!this.hasFocus() || mapIsNull() || !SwingUtilities.isLeftMouseButton(e.getEvent())) {
      return;
    }

    this.dragPoint = null;
    this.dragLocationMapObjects.clear();
    this.dragSizeHeight = 0;
    this.dragSizeWidth = 0;

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      if (SwingUtilities.isRightMouseButton(e.getEvent())) {
        this.newObjectArea = null;
        this.setEditMode(EDITMODE_EDIT);
        break;
      }

      if (this.newObjectArea == null) {
        break;
      }

      IMapObject mo = this.createNewMapObject(UI.getInspector().getObjectType());
      this.newObjectArea = null;
      this.setFocus(mo, !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
      UI.getInspector().bind(mo);
      this.setEditMode(EDITMODE_EDIT);
      break;
    case EDITMODE_MOVE:

      if (this.isMoving) {
        this.isMoving = false;

        for (IMapObject selected : this.getSelectedMapObjects()) {
          UndoManager.instance().mapObjectChanged(selected);
        }

        UndoManager.instance().endOperation();
        this.setEditMode(EDITMODE_EDIT);
      } else {
        this.setEditMode(EDITMODE_EDIT);
        this.evaluateFocus();
      }

      break;
    case EDITMODE_EDIT:
      if (this.isMoving || this.isTransforming) {
        this.isMoving = false;
        this.isTransforming = false;
        UndoManager.instance().mapObjectChanged(this.getFocusedMapObject());
      }

      if (this.startPoint == null) {
        return;
      }

      this.evaluateFocus();
      break;
    default:
      break;
    }

    this.startPoint = null;
  }

  private void evaluateFocus() {
    Rectangle2D rect = this.getCurrentMouseSelectionArea(false);
    if (rect == null) {
      return;
    }

    boolean somethingIsFocused = false;
    boolean currentObjectFocused = false;
    for (IMapObjectLayer layer : Game.world().environment().getMap().getMapObjectLayers()) {
      if (layer == null || !layer.isVisible()) {
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

        if (this.getFocusedMapObject() != null && mapObject.getId() == this.getFocusedMapObject().getId()) {
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
        if (this.getSelectedMapObjects().contains((MapObject) mapObject)) {
          this.getSelectedMapObjects().remove((MapObject) mapObject);
        } else {
          this.setFocus(mapObject, !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
          UI.getInspector().bind(mapObject);
        }
        somethingIsFocused = true;
      }
    }

    if (!somethingIsFocused && !currentObjectFocused) {
      this.setFocus(null, true);
      this.setSelection(Collections.emptyList(), true);
      UI.getInspector().bind(null);
    }
  }

  private boolean hasFocus() {
    if (this.isSuspended() || !this.isVisible()) {
      return false;
    }

    for (GuiComponent comp : this.getComponents()) {
      if (comp.isHovered() && !comp.isSuspended()) {
        return false;
      }
    }

    return true;
  }

  private static void renderMapObjectBounds(Graphics2D g) {
    if (Game.world().environment() == null || Game.world().environment().getMap() == null) {
      return;
    }

    final List<IMapObjectLayer> layers = Game.world().environment().getMap().getMapObjectLayers();
    // render all entities
    for (final IMapObjectLayer layer : layers) {
      if (layer == null || !layer.isVisible()) {
        continue;
      }

      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        MapObjectType type = MapObjectType.get(mapObject.getType());
        final BasicStroke shapeStroke = new BasicStroke(1f / Game.world().camera().getRenderScale());
        if (type == null) {
          if (Program.preferences().isRenderCustomMapObjects()) {
            renderUnsupportedMapObject(g, mapObject, shapeStroke);
          }

          continue;
        }

        // render spawn points
        if (type == MapObjectType.SPAWNPOINT) {
          g.setColor(Style.COLOR_SPAWNPOINT);
          RenderEngine.renderShape(g, new Rectangle2D.Double(mapObject.getBoundingBox().getCenterX() - 1, mapObject.getBoundingBox().getCenterY() - 1, 2, 2));
        }

        if (type != MapObjectType.COLLISIONBOX) {
          Color colorBoundingBoxFill;
          if (layer.getColor() != null) {
            colorBoundingBoxFill = new Color(layer.getColor().getRed(), layer.getColor().getGreen(), layer.getColor().getBlue(), 15);
          } else {
            colorBoundingBoxFill = Style.COLOR_DEFAULT_BOUNDING_BOX_FILL;
          }

          renderBoundingBox(g, mapObject, colorBoundingBoxFill, shapeStroke);
        }

        renderCollisionBox(g, mapObject, shapeStroke);
      }
    }
  }

  private static void renderUnsupportedMapObject(Graphics2D g, IMapObject mapObject, BasicStroke shapeStroke) {
    g.setColor(Style.COLOR_UNSUPPORTED);
    Point2D start = new Point2D.Double(mapObject.getLocation().getX(), mapObject.getLocation().getY());
    StringBuilder info = new StringBuilder("#");
    info.append(mapObject.getId());
    if (mapObject.getName() != null && !mapObject.getName().isEmpty()) {
      info.append("(");
      info.append(mapObject.getName());
      info.append(")");
    }

    RenderEngine.renderText(g, info.toString(), start.getX(), start.getY() - 5);
    RenderEngine.renderShape(g, new Ellipse2D.Double(start.getX() - 1, start.getY() - 1, 3, 3));

    if (mapObject.isPolyline()) {

      if (mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().isEmpty()) {
        return;
      }

      // found the path for the rat
      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      RenderEngine.renderOutline(g, path, shapeStroke);
    } else if (mapObject.isPolygon()) {
      if (mapObject.getPolygon() == null || mapObject.getPolygon().getPoints().isEmpty()) {
        return;
      }

      // found the path for the rat
      final Path2D path = MapUtilities.convertPolyshapeToPath(mapObject);
      if (path == null) {
        return;
      }

      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      RenderEngine.renderShape(g, path);
      g.setColor(Style.COLOR_UNSUPPORTED);
      RenderEngine.renderOutline(g, path, shapeStroke);
    } else if (mapObject.isEllipse()) {
      if (mapObject.getEllipse() == null) {
        return;
      }
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      RenderEngine.renderShape(g, mapObject.getEllipse());

      g.setColor(Style.COLOR_UNSUPPORTED);
      RenderEngine.renderOutline(g, mapObject.getEllipse(), shapeStroke);
    } else {
      g.setColor(Style.COLOR_UNSUPPORTED_FILL);
      RenderEngine.renderShape(g, mapObject.getBoundingBox());
      g.setColor(Style.COLOR_UNSUPPORTED);
      RenderEngine.renderOutline(g, mapObject.getBoundingBox(), shapeStroke);
    }
  }

  private static void renderName(Graphics2D g, IMapObject mapObject) {
    g.setFont(Style.FONT_DEFAULT.deriveFont(10f));
    FontMetrics fm = g.getFontMetrics();

    String objectName = mapObject.getName();
    if (objectName != null && !objectName.isEmpty()) {
      final int PADDING = 2;
      double stringWidth = fm.stringWidth(objectName) / Game.world().camera().getRenderScale();
      double stringHeight = fm.getHeight() * .5 / Game.world().camera().getRenderScale();
      double x = mapObject.getX() + ((mapObject.getWidth() - stringWidth) / 2.0) - PADDING;
      double y = mapObject.getY() + mapObject.getHeight() + stringHeight;
      double width = stringWidth + PADDING * 2;
      double height = stringHeight + PADDING * 2;
      RoundRectangle2D rect = new RoundRectangle2D.Double(x, y, width, height, 2, 2);
      g.setColor(new Color(0, 0, 0, 200));
      RenderEngine.renderShape(g, rect, true);

      g.setColor(Color.WHITE);
      RenderEngine.renderText(g, mapObject.getName(), x + PADDING, rect.getMaxY() - PADDING, true);
    }
  }

  private static void renderGrid(Graphics2D g) {
    // render the grid
    if (Program.preferences().isShowGrid() && Game.world().camera().getRenderScale() >= 1 && Game.world().environment() != null) {

      final IMap map = Game.world().environment().getMap();
      if (map == null) {
        return;
      }

      g.setColor(Program.preferences().getGridColor());
      final Stroke stroke = new BasicStroke(Program.preferences().getGridLineWidth() / Game.world().camera().getRenderScale());
      for (int x = 0; x < map.getWidth(); x++) {
        for (int y = 0; y < map.getHeight(); y++) {
          Shape tile = map.getOrientation().getShape(x, y, map);
          if (Game.world().camera().getViewport().intersects(tile.getBounds2D())) {
            RenderEngine.renderOutline(g, tile, stroke);
          }
        }
      }
    }
  }

  private void renderNewObjectArea(Graphics2D g, Stroke shapeStroke) {
    if (this.newObjectArea == null) {
      return;
    }

    g.setColor(Style.COLOR_NEWOBJECT_FILL);
    RenderEngine.renderShape(g, newObjectArea);
    g.setColor(Style.COLOR_NEWOBJECT_BORDER);
    RenderEngine.renderOutline(g, newObjectArea, shapeStroke);
    g.setFont(g.getFont().deriveFont(Font.BOLD));
    RenderEngine.renderText(g, newObjectArea.getWidth() + "", newObjectArea.getX() + newObjectArea.getWidth() / 2 - 3, newObjectArea.getY() - 5);
    RenderEngine.renderText(g, newObjectArea.getHeight() + "", newObjectArea.getX() - 10, newObjectArea.getY() + newObjectArea.getHeight() / 2);
  }

  private void renderMouseSelectionArea(Graphics2D g, Stroke shapeStroke) {
    // draw mouse selection area
    final Point2D start = this.startPoint;
    if (start != null && !Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
      final Rectangle2D rect = this.getCurrentMouseSelectionArea(false);
      if (rect == null) {
        return;
      }

      g.setColor(Style.COLOR_MOUSE_SELECTION_AREA_FILL);
      RenderEngine.renderShape(g, rect);
      g.setColor(Style.COLOR_MOUSE_SELECTION_AREA_BORDER);
      RenderEngine.renderOutline(g, rect, shapeStroke);
    }
  }

  private void renderFocus(Graphics2D g) {
    // render the focus and the transform rects
    final Rectangle2D focus = this.getFocus();
    final IMapObject focusedMapObject = this.getFocusedMapObject();
    if (focus != null && focusedMapObject != null) {
      Stroke stroke = new BasicStroke(1 / Game.world().camera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.time().now() / 15);

      g.setColor(Color.BLACK);

      RenderEngine.renderOutline(g, focus, stroke);

      Stroke whiteStroke = new BasicStroke(1 / Game.world().camera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.time().now() / 15 - 1f);
      g.setColor(Color.WHITE);
      RenderEngine.renderOutline(g, focus, whiteStroke);

      // render transform rects
      if (this.currentEditMode != EDITMODE_MOVE) {
        Stroke transStroke = new BasicStroke(1 / Game.world().camera().getRenderScale());
        for (Rectangle2D trans : this.transformRects.values()) {
          g.setColor(Style.COLOR_TRANSFORM_RECT_FILL);
          RenderEngine.renderShape(g, trans);
          g.setColor(Color.BLACK);
          RenderEngine.renderOutline(g, trans, transStroke);
        }
      }
    }

    if (focusedMapObject != null) {
      renderObjectId(g, focusedMapObject);
    }
  }

  private void renderObjectId(Graphics2D g, IMapObject mapObject) {
    if (!Program.preferences().isRenderMapIds()) {
      return;
    }

    Font previousFont = g.getFont();
    Font idFont = previousFont.deriveFont(Math.max(8f, (float) (10 * Math.sqrt(Game.world().camera().getRenderScale()))));
    if (this.currentZoomIndex > DEFAULT_ZOOM_INDEX) {
      idFont = idFont.deriveFont(Font.BOLD);
    }

    Point2D loc = Game.world().camera().getViewportLocation(new Point2D.Double(mapObject.getX() + mapObject.getWidth() / 2, mapObject.getY()));
    g.setColor(Style.COLOR_STATUS);

    g.setFont(idFont);
    String id = Integer.toString(mapObject.getId());

    double x = loc.getX() * Game.world().camera().getRenderScale() - g.getFontMetrics().stringWidth(id) / 2.0;
    double y = loc.getY() * Game.world().camera().getRenderScale() - (g.getFontMetrics().getHeight() * .30);

    if (this.currentZoomIndex < DEFAULT_ZOOM_INDEX) {
      TextRenderer.render(g, id, x, y);
    } else {
      TextRenderer.renderWithOutline(g, id, x, y, Style.COLOR_DARKBORDER, 5, true);
    }

    g.setFont(previousFont);
  }

  private void renderSelection(Graphics2D g) {
    for (IMapObject mapObject : this.getSelectedMapObjects()) {
      if (mapObject.equals(this.getFocusedMapObject())) {
        continue;
      }

      Stroke stroke = new BasicStroke(1 / Game.world().camera().getRenderScale());

      g.setColor(colorSelectionBorder);
      RenderEngine.renderOutline(g, mapObject.getBoundingBox(), stroke);
      renderObjectId(g, mapObject);
    }
  }

  private static void renderBoundingBox(Graphics2D g, IMapObject mapObject, Color colorBoundingBoxFill, BasicStroke shapeStroke) {
    MapObjectType type = MapObjectType.get(mapObject.getType());
    Color fillColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      fillColor = Style.COLOR_TRIGGER_FILL;
    } else if (type == MapObjectType.STATICSHADOW) {
      fillColor = Style.COLOR_SHADOW_FILL;
    }

    // render bounding boxes
    g.setColor(fillColor);

    // don't fill rect for lightsource because it is important to judge
    // the color
    if (type != MapObjectType.LIGHTSOURCE) {
      RenderEngine.renderShape(g, mapObject.getBoundingBox());
    }

    Color borderColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      borderColor = Style.COLOR_TRIGGER_BORDER;
    } else if (type == MapObjectType.LIGHTSOURCE) {
      final String mapObjectColor = mapObject.getStringValue(MapObjectProperty.LIGHT_COLOR);
      if (mapObjectColor != null && !mapObjectColor.isEmpty()) {
        Color lightColor = ColorHelper.decode(mapObjectColor);
        borderColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 255);
      }
    } else if (type == MapObjectType.STATICSHADOW) {
      borderColor = Style.COLOR_SHADOW_BORDER;
    } else if (type == MapObjectType.SPAWNPOINT) {
      borderColor = Style.COLOR_SPAWNPOINT;
    } else {
      borderColor = new Color(colorBoundingBoxFill.getRed(), colorBoundingBoxFill.getGreen(), colorBoundingBoxFill.getBlue(), 150);
    }

    g.setColor(borderColor);

    RenderEngine.renderOutline(g, mapObject.getBoundingBox(), shapeStroke);

    if (Program.preferences().isRenderNames()) {
      renderName(g, mapObject);
    }
  }

  private static void renderCollisionBox(Graphics2D g, IMapObject mapObject, BasicStroke shapeStroke) {
    // render collision boxes
    boolean collision = mapObject.getBoolValue(MapObjectProperty.COLLISION, false);
    float collisionBoxWidth = mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_WIDTH, -1);
    float collisionBoxHeight = mapObject.getFloatValue(MapObjectProperty.COLLISIONBOX_HEIGHT, -1);
    final Align align = Align.get(mapObject.getStringValue(MapObjectProperty.COLLISION_ALIGN));
    final Valign valign = Valign.get(mapObject.getStringValue(MapObjectProperty.COLLISION_VALIGN));

    if (MapObjectType.get(mapObject.getType()) == MapObjectType.COLLISIONBOX) {
      collisionBoxWidth = mapObject.getWidth();
      collisionBoxHeight = mapObject.getHeight();
      collision = true;
    }

    if (collisionBoxWidth != -1 && collisionBoxHeight != -1) {

      g.setColor(Style.COLOR_COLLISION_FILL);
      Rectangle2D collisionBox = CollisionEntity.getCollisionBox(mapObject.getLocation(), mapObject.getWidth(), mapObject.getHeight(), collisionBoxWidth, collisionBoxHeight, align, valign);

      RenderEngine.renderShape(g, collisionBox);
      g.setColor(collision ? Style.COLOR_COLLISION_BORDER : Style.COLOR_NOCOLLISION_BORDER);

      Stroke collisionStroke = collision ? shapeStroke : new BasicStroke(1 / Game.world().camera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] { 1f }, 0);
      RenderEngine.renderOutline(g, collisionBox, collisionStroke);
    }
  }
}