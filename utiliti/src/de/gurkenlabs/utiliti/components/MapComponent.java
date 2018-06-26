package de.gurkenlabs.utiliti.components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.Stroke;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
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
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Align;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.Resources;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.Valign;
import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.IEnvironment;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.gui.ComponentMouseWheelEvent;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.EditorScreen;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.swing.XmlImportDialog;

public class MapComponent extends EditorComponent implements IUpdateable {
  public enum TransformType {
    UP, DOWN, LEFT, RIGHT, UPLEFT, UPRIGHT, DOWNLEFT, DOWNRIGHT, NONE
  }

  public static final int EDITMODE_CREATE = 0;
  public static final int EDITMODE_EDIT = 1;
  public static final int EDITMODE_MOVE = 2;
  private static final Logger log = Logger.getLogger(MapComponent.class.getName());

  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private static final String DEFAULT_MAPOBJECTLAYER_NAME = "default";
  private static final int TRANSFORM_RECT_SIZE = 6;
  private static final int BASE_SCROLL_SPEED = 50;

  private static final Color DEFAULT_COLOR_BOUNDING_BOX_FILL = new Color(0, 0, 0, 35);

  private static final Color COLOR_COLLISION_FILL = new Color(255, 0, 0, 15);
  private static final Color COLOR_COLLISION_BORDER = Color.RED;
  private static final Color COLOR_NOCOLLISION_BORDER = new Color(255, 0, 0, 150);
  private static final Color COLOR_TRIGGER_BORDER = Color.YELLOW;
  private static final Color COLOR_TRIGGER_FILL = new Color(255, 255, 0, 15);
  private static final Color COLOR_SPAWNPOINT = Color.GREEN;
  private static final Color COLOR_LANE = Color.YELLOW;
  private static final Color COLOR_NEWOBJECT_FILL = new Color(0, 255, 0, 50);
  private static final Color COLOR_NEWOBJECT_BORDER = Color.GREEN.darker();
  private static final Color COLOR_TRANSFORM_RECT_FILL = new Color(255, 255, 255, 100);
  private static final Color COLOR_SHADOW_FILL = new Color(85, 130, 200, 15);
  private static final Color COLOR_SHADOW_BORDER = new Color(30, 85, 170);
  private static final Color COLOR_MOUSE_SELECTION_AREA_FILL = new Color(0, 130, 152, 80);
  private static final Color COLOR_MOUSE_SELECTION_AREA_BORDER = new Color(0, 130, 152, 150);

  private double currentTransformRectSize = TRANSFORM_RECT_SIZE;
  private final java.util.Map<TransformType, Rectangle2D> transformRects;

  private final List<Consumer<Integer>> editModeChangedConsumer;
  private final List<Consumer<IMapObject>> focusChangedConsumer;
  private final List<Consumer<List<MapObject>>> selectionChangedConsumer;
  private final List<Consumer<Map>> mapLoadedConsumer;

  private final java.util.Map<String, Integer> selectedLayers;
  private final java.util.Map<String, Point2D> cameraFocus;
  private final java.util.Map<String, IMapObject> focusedObjects;
  private final java.util.Map<String, List<MapObject>> selectedObjects;
  private final java.util.Map<String, IEnvironment> environments;
  private final java.util.Map<IMapObject, Point2D> dragLocationMapObjects;

  private int currentEditMode = EDITMODE_EDIT;
  private TransformType currentTransform;

  private int currentZoomIndex = 7;

  private final List<Map> maps;

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
  private int gridSize;

  private Color colorSelectionBorder;
  private float focusBorderBrightness = 0;
  private boolean focusBorderBrightnessIncreasing = true;

  private final EditorScreen screen;

  private boolean loading;
  private boolean initialized;

  public MapComponent(final EditorScreen screen) {
    super(ComponentType.MAP);
    this.editModeChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.selectionChangedConsumer = new CopyOnWriteArrayList<>();
    this.mapLoadedConsumer = new CopyOnWriteArrayList<>();
    this.focusedObjects = new ConcurrentHashMap<>();
    this.selectedObjects = new ConcurrentHashMap<>();
    this.environments = new ConcurrentHashMap<>();
    this.maps = new ArrayList<>();
    this.selectedLayers = new ConcurrentHashMap<>();
    this.cameraFocus = new ConcurrentHashMap<>();
    this.transformRects = new ConcurrentHashMap<>();
    this.dragLocationMapObjects = new ConcurrentHashMap<>();
    this.screen = screen;
    Game.getCamera().onZoomChanged(zoom -> {
      this.currentTransformRectSize = TRANSFORM_RECT_SIZE / zoom;
      this.updateTransformControls();
    });
    this.gridSize = Program.getUserPreferences().getGridSize();
  }

  public void onEditModeChanged(Consumer<Integer> cons) {
    this.editModeChangedConsumer.add(cons);
  }

  public void onFocusChanged(Consumer<IMapObject> cons) {
    this.focusChangedConsumer.add(cons);
  }

  public void onSelectionChanged(Consumer<List<MapObject>> cons) {
    this.selectionChangedConsumer.add(cons);
  }

  public void onMapLoaded(Consumer<Map> cons) {
    this.mapLoadedConsumer.add(cons);
  }

  @Override
  public void render(Graphics2D g) {
    if (Game.getEnvironment() == null) {
      return;
    }

    this.renderGrid(g);

    final BasicStroke shapeStroke = new BasicStroke(1 / Game.getCamera().getRenderScale());
    if (Program.getUserPreferences().isRenderBoundingBoxes()) {
      this.renderMapObjectBounds(g);
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
    final List<Map> loadedMaps = new ArrayList<>();
    for (final String mapFile : files) {
      Map map = (Map) MapLoader.load(mapFile);
      loadedMaps.add(map);
      log.log(Level.INFO, "map found: {0}", new Object[] { map.getFileName() });
    }

    this.loadMaps(loadedMaps);
  }

  public void loadMaps(List<Map> maps) {
    EditorScreen.instance().getMapObjectPanel().bind(null);
    this.setFocus(null, true);
    this.getMaps().clear();

    Collections.sort(maps);

    this.getMaps().addAll(maps);
    EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps(), true);
  }

  public List<Map> getMaps() {
    return this.maps;
  }

  public int getGridSize() {
    return this.gridSize;
  }

  public IMapObject getFocusedMapObject() {
    if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null) {
      return this.focusedObjects.get(Game.getEnvironment().getMap().getFileName());
    }

    return null;
  }

  public List<MapObject> getSelectedMapObjects() {
    final String map = Game.getEnvironment().getMap().getFileName();
    if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null && this.selectedObjects.containsKey(map)) {
      return this.selectedObjects.get(map);
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
    Game.getCamera().setZoom(zooms[this.currentZoomIndex], 0);
    if (!this.initialized) {
      Game.getScreenManager().getRenderComponent().addFocusListener(new FocusAdapter() {
        @Override
        public void focusLost(FocusEvent e) {
          startPoint = null;
        }
      });

      this.setupKeyboardControls();
      this.setupMouseControls();
      this.initialized = true;

      Game.getLoop().attach(this);
    }

    super.prepare();
  }

  public void loadEnvironment(Map map) {
    this.loading = true;
    try {
      if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null) {
        final String mapName = Game.getEnvironment().getMap().getFileName();
        double x = Game.getCamera().getFocus().getX();
        double y = Game.getCamera().getFocus().getY();
        Point2D newPoint = new Point2D.Double(x, y);
        this.cameraFocus.put(mapName, newPoint);
        this.selectedLayers.put(mapName, EditorScreen.instance().getMapSelectionPanel().getSelectedLayerIndex());
      }

      Point2D newFocus = null;

      if (this.cameraFocus.containsKey(map.getFileName())) {
        newFocus = this.cameraFocus.get(map.getFileName());
      } else {
        newFocus = new Point2D.Double(map.getSizeInPixels().getWidth() / 2, map.getSizeInPixels().getHeight() / 2);
        this.cameraFocus.put(map.getFileName(), newFocus);
      }

      Game.getCamera().setFocus(new Point2D.Double(newFocus.getX(), newFocus.getY()));

      this.ensureUniqueIds(map);

      if (!this.environments.containsKey(map.getFileName())) {
        Environment env = new Environment(map);
        env.init();
        this.environments.put(map.getFileName(), env);
      }

      Game.loadEnvironment(this.environments.get(map.getFileName()));

      Program.updateScrollBars();

      EditorScreen.instance().getMapSelectionPanel().setSelection(map.getFileName());
      if (this.selectedLayers.containsKey(map.getFileName())) {
        EditorScreen.instance().getMapSelectionPanel().selectLayer(this.selectedLayers.get(map.getFileName()));
      }

      EditorScreen.instance().getMapObjectPanel().bind(this.getFocusedMapObject());

      for (Consumer<Map> cons : this.mapLoadedConsumer) {
        cons.accept(map);
      }

    } finally {
      this.loading = false;
    }
  }

  public void reloadEnvironment() {
    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return;
    }

    this.loadEnvironment((Map) Game.getEnvironment().getMap());
  }

  public void add(IMapObject mapObject) {
    this.add(mapObject, getCurrentLayer());
    UndoManager.instance().mapObjectAdded(mapObject);
  }

  public void add(IMapObject mapObject, IMapObjectLayer layer) {
    if (layer == null || mapObject == null) {
      return;
    }

    layer.addMapObject(mapObject);
    Game.getEnvironment().loadFromMap(mapObject.getId());
    if (MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().updateSection(mapObject.getBoundingBox());
    }

    Game.getScreenManager().getRenderComponent().requestFocus();
    this.setFocus(mapObject, false);
    this.setEditMode(EDITMODE_EDIT);
  }

  public void copy() {
    this.copiedBlueprint = new Blueprint("", this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()]));
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
      for (MapObject mapObject : this.copiedBlueprint.build(x, y)) {
        this.add(mapObject);
        this.setSelection(mapObject, false);
      }

      // clean up copied blueprints in case, we cut the objects and kept the IDs
      if (this.copiedBlueprint.keepIds()) {
        this.copiedBlueprint = null;
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void cut() {
    this.copiedBlueprint = new Blueprint("", true, this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()]));

    UndoManager.instance().beginOperation();
    try {
      for (MapObject mapObject : this.getSelectedMapObjects()) {
        this.delete(mapObject);
        UndoManager.instance().mapObjectDeleted(mapObject);
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void clearAll() {
    this.focusedObjects.clear();
    this.selectedLayers.clear();
    this.selectedObjects.clear();
    this.cameraFocus.clear();
    this.environments.clear();
  }

  public void delete() {
    UndoManager.instance().beginOperation();
    try {
      for (IMapObject deleteObject : this.getSelectedMapObjects()) {
        if (deleteObject == null) {
          continue;
        }

        this.delete(deleteObject);
        UndoManager.instance().mapObjectDeleted(deleteObject);
      }
    } finally {
      UndoManager.instance().endOperation();
    }
  }

  public void delete(final IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    MapObjectType type = MapObjectType.get(mapObject.getType());
    Game.getEnvironment().getMap().removeMapObject(mapObject.getId());
    Game.getEnvironment().remove(mapObject.getId());
    if (type == MapObjectType.STATICSHADOW || type == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().updateSection(mapObject.getBoundingBox());
    }

    if (mapObject.equals(this.getFocusedMapObject())) {
      this.setFocus(null, true);
    }
  }

  public void defineBlueprint() {
    if (this.getFocusedMapObject() == null) {
      return;
    }

    Object name = JOptionPane.showInputDialog(Game.getScreenManager().getRenderComponent(), Resources.get("input_prompt_name"), Resources.get("input_prompt_name_title"), JOptionPane.PLAIN_MESSAGE, null, null, this.getFocusedMapObject().getName());
    if (name == null) {
      return;
    }

    Blueprint blueprint = new Blueprint(name.toString(), this.getSelectedMapObjects().toArray(new MapObject[this.getSelectedMapObjects().size()]));

    EditorScreen.instance().getGameFile().getBluePrints().add(blueprint);
    Program.getAssetTree().forceUpdate();
  }

  public void centerCameraOnFocus() {
    if (this.getFocusedMapObject() != null) {
      final Rectangle2D focus = this.getFocus();
      if (focus == null) {
        return;
      }

      Game.getCamera().setFocus(new Point2D.Double(focus.getCenterX(), focus.getCenterY()));
    }
  }

  public void setEditMode(int editMode) {
    if (editMode == this.currentEditMode) {
      return;
    }

    switch (editMode) {
    case EDITMODE_CREATE:
      this.setFocus(null, true);
      EditorScreen.instance().getMapObjectPanel().bind(null);
      break;
    case EDITMODE_EDIT:
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      break;
    case EDITMODE_MOVE:
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_MOVE, 0, 0);
      break;
    default:
      break;
    }

    this.currentEditMode = editMode;
    for (Consumer<Integer> cons : this.editModeChangedConsumer) {
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

      if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
        return;
      }

      if (this.isMoving || this.isTransforming) {
        return;
      }
      EditorScreen.instance().getMapObjectPanel().bind(mapObject);
      EditorScreen.instance().getMapSelectionPanel().focus(mapObject);
      if (mapObject == null) {
        this.focusedObjects.remove(Game.getEnvironment().getMap().getFileName());
      } else {
        this.focusedObjects.put(Game.getEnvironment().getMap().getFileName(), mapObject);
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
    if (mapObject == null) {
      this.getSelectedMapObjects().clear();
      for (Consumer<List<MapObject>> cons : this.selectionChangedConsumer) {
        cons.accept(this.getSelectedMapObjects());
      }
      return;
    }

    final String map = Game.getEnvironment().getMap().getFileName();
    if (!this.selectedObjects.containsKey(map)) {
      this.selectedObjects.put(map, new CopyOnWriteArrayList<>());
    }

    if (clearSelection) {
      this.getSelectedMapObjects().clear();
    }

    if (!this.getSelectedMapObjects().contains(mapObject)) {
      this.getSelectedMapObjects().add((MapObject) mapObject);
    }

    for (Consumer<List<MapObject>> cons : this.selectionChangedConsumer) {
      cons.accept(this.getSelectedMapObjects());
    }
  }

  public void setGridSize(int gridSize) {
    Program.getUserPreferences().setGridSize(gridSize);
    this.gridSize = gridSize;
  }

  public void updateTransformControls() {
    final Rectangle2D focus = this.getFocus();
    if (focus == null) {
      this.transformRects.clear();
      return;
    }

    for (TransformType trans : TransformType.values()) {
      if (trans == TransformType.NONE) {
        continue;
      }
      Rectangle2D transRect = new Rectangle2D.Double(this.getTransX(trans, focus), this.getTransY(trans, focus), this.currentTransformRectSize, this.currentTransformRectSize);
      this.transformRects.put(trans, transRect);
    }
  }

  public void deleteMap() {
    if (this.getMaps() == null || this.getMaps().isEmpty()) {
      return;
    }

    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return;
    }

    int n = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), Resources.get("hud_deleteMapMessage") + "\n" + Game.getEnvironment().getMap().getName(), Resources.get("hud_deleteMap"), JOptionPane.YES_NO_OPTION);
    if (n != JOptionPane.YES_OPTION) {
      return;
    }

    this.getMaps().removeIf(x -> x.getFileName().equals(Game.getEnvironment().getMap().getFileName()));

    // TODO: remove all tile sets from the game file that are no longer needed
    // by any other map.
    EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps());
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

    XmlImportDialog.importXml("Tilemap", Map.FILE_EXTENSION, file -> {
      String mapPath = file.toString();
      Map map = (Map) MapLoader.load(mapPath);
      if (map == null) {
        log.log(Level.WARNING, "could not load map from file {0}", new Object[] { mapPath });
        return;
      }

      if (map.getMapObjectLayers().isEmpty()) {

        // make sure there's a map object layer on the map because we need one
        // to add any kind of entities
        MapObjectLayer layer = new MapObjectLayer();
        layer.setName(DEFAULT_MAPOBJECTLAYER_NAME);
        map.addMapObjectLayer(layer);
      }

      Optional<Map> current = this.maps.stream().filter(x -> x.getFileName().equals(map.getFileName())).findFirst();
      if (current.isPresent()) {
        int n = JOptionPane.showConfirmDialog(Game.getScreenManager().getRenderComponent(), Resources.get("input_replace_map", map.getFileName()), Resources.get("input_replace_map_title"), JOptionPane.YES_NO_OPTION);

        if (n == JOptionPane.YES_OPTION) {
          this.getMaps().remove(current.get());

        } else {
          return;
        }
      }

      this.getMaps().add(map);
      Collections.sort(this.getMaps());

      for (IImageLayer imageLayer : map.getImageLayers()) {
        BufferedImage img = Resources.getImage(imageLayer.getImage().getAbsoluteSourcePath(), true);
        if (img == null) {
          continue;
        }

        Spritesheet sprite = Spritesheet.load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
        this.screen.getGameFile().getSpriteSheets().add(new SpriteSheetInfo(sprite));
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
      ImageCache.clearAll();
      if (this.environments.containsKey(map.getFileName())) {
        this.environments.remove(map.getFileName());
      }

      EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps(), true);
      this.loadEnvironment(map);
      log.log(Level.INFO, "imported map {0}", new Object[] { map.getFileName() });
    });
  }

  public void loadTileset(ITileset tileset, boolean embedded) {
    Spritesheet sprite = Spritesheet.find(tileset.getImage().getSource());
    if (sprite != null) {
      Spritesheet.remove(sprite.getName());
      this.screen.getGameFile().getSpriteSheets().removeIf(x -> x.getName().equals(sprite.getName()));
    }

    Spritesheet newSprite = Spritesheet.load(tileset);
    SpriteSheetInfo info = new SpriteSheetInfo(newSprite);
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

    Map map = (Map) Game.getEnvironment().getMap();
    if (map == null) {
      return;
    }

    this.exportMap(map);
  }

  public void exportMap(Map map) {
    // TODO: replace by XmlExportDialog call
    JFileChooser chooser;
    try {
      String source = EditorScreen.instance().getProjectPath();
      chooser = new JFileChooser(source != null ? source : new File(".").getCanonicalPath());
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.SAVE_DIALOG);
      chooser.setDialogTitle("Export Map");
      FileFilter filter = new FileNameExtensionFilter("tmx - Tilemap XML", Map.FILE_EXTENSION);
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);
      chooser.setSelectedFile(new File(map.getFileName() + "." + Map.FILE_EXTENSION));

      int result = chooser.showSaveDialog(Game.getScreenManager().getRenderComponent());
      if (result == JFileChooser.APPROVE_OPTION) {
        String newFile = XmlUtilities.save(map, chooser.getSelectedFile().toString(), Map.FILE_EXTENSION);

        // save all tilesets manually because a map has a relative reference to
        // the tilesets
        String dir = FileUtilities.getParentDirPath(newFile);
        for (ITileset tileSet : map.getTilesets()) {
          ImageFormat format = ImageFormat.get(FileUtilities.getExtension(tileSet.getImage().getSource()));
          ImageSerializer.saveImage(Paths.get(dir, tileSet.getImage().getSource()).toString(), Spritesheet.find(tileSet.getImage().getSource()).getImage(), format);

          Tileset tile = (Tileset) tileSet;
          if (tile.isExternal()) {
            tile.saveSource(dir);
          }
        }

        log.log(Level.INFO, "exported {0} to {1}", new Object[] { map.getFileName(), newFile });
      }
    } catch (IOException e) {
      log.log(Level.SEVERE, e.getMessage(), e);
    }
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

  private void updateScrollSpeed() {
    this.scrollSpeed = BASE_SCROLL_SPEED / zooms[this.currentZoomIndex];
  }

  private void ensureUniqueIds(IMap map) {
    int maxMapId = MapUtilities.getMaxMapId(map);
    List<Integer> usedIds = new ArrayList<>();
    for (IMapObject obj : map.getMapObjects()) {
      if (usedIds.contains(obj.getId())) {
        obj.setId(++maxMapId);
      }

      usedIds.add(obj.getId());
    }
  }

  private static IMapObjectLayer getCurrentLayer() {
    int layerIndex = EditorScreen.instance().getMapSelectionPanel().getSelectedLayerIndex();
    if (layerIndex < 0 || layerIndex >= Game.getEnvironment().getMap().getMapObjectLayers().size()) {
      layerIndex = 0;
    }

    return Game.getEnvironment().getMap().getMapObjectLayers().get(layerIndex);
  }

  private IMapObject createNewMapObject(MapObjectType type) {
    IMapObject mo = new MapObject();
    mo.setType(type.toString());
    mo.setX((int) this.newObjectArea.getX());
    mo.setY((int) this.newObjectArea.getY());
    mo.setWidth((int) this.newObjectArea.getWidth());
    mo.setHeight((int) this.newObjectArea.getHeight());
    mo.setId(Game.getEnvironment().getNextMapId());
    mo.setName("");

    switch (type) {
    case PROP:
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOX_WIDTH, (this.newObjectArea.getWidth() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISIONBOX_HEIGHT, (this.newObjectArea.getHeight() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperty.COLLISION, "true");
      mo.setCustomProperty(MapObjectProperty.PROP_INDESTRUCTIBLE, "false");
      mo.setCustomProperty(MapObjectProperty.PROP_ADDSHADOW, "true");
      break;
    case LIGHTSOURCE:
      mo.setCustomProperty(MapObjectProperty.LIGHT_ALPHA, "180");
      mo.setCustomProperty(MapObjectProperty.LIGHT_COLOR, "#ffffff");
      mo.setCustomProperty(MapObjectProperty.LIGHT_SHAPE, LightSource.ELLIPSE);
      mo.setCustomProperty(MapObjectProperty.LIGHT_ACTIVE, "true");
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
      minX = this.snapX(minX);
      maxX = this.snapX(maxX);
      minY = this.snapY(minY);
      maxY = this.snapY(maxY);
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

  private double getTransX(TransformType type, Rectangle2D focus) {
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

  private double getTransY(TransformType type, Rectangle2D focus) {
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
    if (transformObject == null || this.currentEditMode != EDITMODE_EDIT || currentTransform == TransformType.NONE) {
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
    double newX = this.snapX(dragLocationMapObject.getX());
    double newY = this.snapY(dragLocationMapObject.getY());

    switch (this.currentTransform) {
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

    transformObject.setWidth(this.snapX(newWidth));
    transformObject.setHeight(this.snapY(newHeight));
    transformObject.setX(this.snapX(newX));
    transformObject.setY(this.snapY(newY));

    Game.getEnvironment().reloadFromMap(transformObject.getId());
    if (MapObjectType.get(transformObject.getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().updateSection(transformObject.getBoundingBox());
    }

    EditorScreen.instance().getMapObjectPanel().bind(transformObject);
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
    float newX = this.snapX(dragLocationMapObjectMinX.getX() + deltaX);
    float snappedDeltaX = newX - minX.getX();

    double deltaY = Input.mouse().getMapLocation().getY() - this.dragPoint.getY();
    float newY = this.snapY(dragLocationMapObjectMinY.getY() + deltaY);
    float snappedDeltaY = newY - minY.getY();

    if (snappedDeltaX == 0 && snappedDeltaY == 0) {
      return;
    }

    this.handleEntityDrag(snappedDeltaX, snappedDeltaY);

    if (this.getSelectedMapObjects().stream().anyMatch(x -> MapObjectType.get(x.getType()) == MapObjectType.STATICSHADOW || MapObjectType.get(x.getType()) == MapObjectType.LIGHTSOURCE)) {
      Game.getEnvironment().getAmbientLight().updateSection(MapObject.getBounds2D(this.getSelectedMapObjects()));
    }
  }

  private void handleEntityDrag(float snappedDeltaX, float snappedDeltaY) {
    for (IMapObject selected : this.getSelectedMapObjects()) {

      selected.setX(selected.getX() + snappedDeltaX);
      selected.setY(selected.getY() + snappedDeltaY);

      IEntity entity = Game.getEnvironment().get(selected.getId());
      if (entity != null) {
        entity.setX(selected.getLocation().getX());
        entity.setY(selected.getLocation().getY());
      } else {
        Game.getEnvironment().reloadFromMap(selected.getId());
      }

      if (selected.equals(this.getFocusedMapObject())) {
        EditorScreen.instance().getMapObjectPanel().bind(selected);
        this.updateTransformControls();
      }
    }
  }

  private void setCurrentZoom() {
    Game.getCamera().setZoom(zooms[this.currentZoomIndex], 0);
  }

  private void setupKeyboardControls() {
    Input.keyboard().onKeyReleased(KeyEvent.VK_ADD, e -> {
      if (this.isSuspended() || !this.isVisible()) {
        return;
      }
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        this.zoomIn();
      }
    });

    Input.keyboard().onKeyReleased(KeyEvent.VK_SUBTRACT, e -> {
      if (this.isSuspended() || !this.isVisible()) {
        return;
      }
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        this.zoomOut();
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_SPACE, e -> this.centerCameraOnFocus());

    Input.keyboard().onKeyPressed(KeyEvent.VK_CONTROL, e -> {
      if (this.currentEditMode == EDITMODE_EDIT) {
        this.setEditMode(EDITMODE_MOVE);
      }
    });
    Input.keyboard().onKeyReleased(KeyEvent.VK_CONTROL, e -> this.setEditMode(EDITMODE_EDIT));

    Input.keyboard().onKeyReleased(KeyEvent.VK_Z, e -> {
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        UndoManager.instance().undo();
      }
    });

    Input.keyboard().onKeyReleased(KeyEvent.VK_Y, e -> {
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        UndoManager.instance().redo();
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_DELETE, e -> {
      if (this.isSuspended() || !this.isVisible() || this.getFocusedMapObject() == null) {
        return;
      }

      if (Game.getScreenManager().getRenderComponent().hasFocus() && this.currentEditMode == EDITMODE_EDIT) {
        this.delete();
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
      if (!Game.getScreenManager().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(1, 0);
      this.afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_LEFT, e -> {
      if (!Game.getScreenManager().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(-1, 0);
      this.afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_UP, e -> {
      if (!Game.getScreenManager().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(0, -1);
      this.afterKeyPressed();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_DOWN, e -> {
      if (!Game.getScreenManager().getRenderComponent().hasFocus()) {
        return;
      }

      this.beforeKeyPressed();
      this.handleEntityDrag(0, 1);
      this.afterKeyPressed();
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

  private void afterKeyPressed() {
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
    if (!this.hasFocus()) {
      return;
    }

    final Point2D currentFocus = Game.getCamera().getFocus();
    // horizontal scrolling
    if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && this.dragPoint == null) {
      if (e.getEvent().getWheelRotation() < 0) {

        Point2D newFocus = new Point2D.Double(currentFocus.getX() - this.scrollSpeed, currentFocus.getY());
        Game.getCamera().setFocus(newFocus);
      } else {
        Point2D newFocus = new Point2D.Double(currentFocus.getX() + this.scrollSpeed, currentFocus.getY());
        Game.getCamera().setFocus(newFocus);
      }

      Program.getHorizontalScrollBar().setValue((int) Game.getCamera().getViewPort().getCenterX());
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
      Game.getCamera().setFocus(newFocus);

    } else {
      Point2D newFocus = new Point2D.Double(currentFocus.getX(), currentFocus.getY() + this.scrollSpeed);
      Game.getCamera().setFocus(newFocus);
    }

    Program.getVerticalcrollBar().setValue((int) Game.getCamera().getViewPort().getCenterY());
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
    if (this.getFocus() == null) {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      this.currentTransform = TransformType.NONE;
      return;
    }

    boolean hovered = false;
    if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
      return;
    }
    for (Entry<TransformType, Rectangle2D> entry : this.transformRects.entrySet()) {
      Rectangle2D rect = entry.getValue();
      Rectangle2D hoverrect = new Rectangle2D.Double(rect.getX() - rect.getWidth() * 3, rect.getY() - rect.getHeight() * 3, rect.getWidth() * 5, rect.getHeight() * 5);
      if (hoverrect.contains(Input.mouse().getMapLocation())) {
        hovered = true;
        if (entry.getKey() == TransformType.DOWN || entry.getKey() == TransformType.UP) {
          Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_VERTICAL, 0, 0);
        } else if (entry.getKey() == TransformType.UPLEFT || entry.getKey() == TransformType.DOWNRIGHT) {
          Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_DIAGONAL_LEFT, 0, 0);
        } else if (entry.getKey() == TransformType.UPRIGHT || entry.getKey() == TransformType.DOWNLEFT) {
          Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_DIAGONAL_RIGHT, 0, 0);
        } else {
          Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_HORIZONTAL, 0, 0);
        }

        this.currentTransform = entry.getKey();
        break;
      }
    }

    if (!hovered) {
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      this.currentTransform = TransformType.NONE;
    }
  }

  private void handleMousePressed(ComponentMouseEvent e) {
    if (!this.hasFocus()) {
      return;
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      this.startPoint = Input.mouse().getMapLocation();
      break;
    case EDITMODE_MOVE:
      break;
    case EDITMODE_EDIT:
      if (this.isMoving || this.currentTransform != TransformType.NONE || SwingUtilities.isRightMouseButton(e.getEvent())) {
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
    if (!this.hasFocus()) {
      return;
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      if (startPoint == null) {
        return;
      }

      newObjectArea = this.getCurrentMouseSelectionArea(true);
      break;
    case EDITMODE_EDIT:
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        this.handleSelectedEntitiesDrag();
        return;
      } else if (this.currentTransform != TransformType.NONE) {
        if (!this.isTransforming) {
          this.isTransforming = true;
          UndoManager.instance().mapObjectChanging(this.getFocusedMapObject());
        }

        this.handleTransform();
        return;
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
    if (!this.hasFocus()) {
      return;
    }

    this.dragPoint = null;
    this.dragLocationMapObjects.clear();
    this.dragSizeHeight = 0;
    this.dragSizeWidth = 0;

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      if (this.newObjectArea == null) {
        break;
      }
      IMapObject mo = this.createNewMapObject(EditorScreen.instance().getMapObjectPanel().getObjectType());
      this.newObjectArea = null;
      this.setFocus(mo, !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
      EditorScreen.instance().getMapObjectPanel().bind(mo);
      this.setEditMode(EDITMODE_EDIT);
      break;
    case EDITMODE_MOVE:

      if (this.isMoving) {
        this.isMoving = false;

        for (IMapObject selected : this.getSelectedMapObjects()) {
          UndoManager.instance().mapObjectChanged(selected);
        }

        UndoManager.instance().endOperation();
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

      Rectangle2D rect = this.getCurrentMouseSelectionArea(false);
      boolean somethingIsFocused = false;
      boolean currentObjectFocused = false;
      for (IMapObjectLayer layer : Game.getEnvironment().getMap().getMapObjectLayers()) {
        if (layer == null || !EditorScreen.instance().getMapSelectionPanel().isVisibleMapObjectLayer(layer.getName())) {
          continue;
        }

        for (IMapObject mapObject : layer.getMapObjects()) {
          if (mapObject == null) {
            continue;
          }

          MapObjectType type = MapObjectType.get(mapObject.getType());
          if (type == MapObjectType.PATH) {
            continue;
          }

          if (!GeometricUtilities.intersects(rect, mapObject.getBoundingBox())) {
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

          this.setFocus(mapObject, !Input.keyboard().isPressed(KeyEvent.VK_SHIFT));
          EditorScreen.instance().getMapObjectPanel().bind(mapObject);
          somethingIsFocused = true;
        }
      }

      if (!somethingIsFocused && !currentObjectFocused) {
        this.setFocus(null, true);
        EditorScreen.instance().getMapObjectPanel().bind(null);
      }

      break;
    default:
      break;
    }

    this.startPoint = null;
  }

  private float snapX(double x) {
    if (Program.getUserPreferences().isSnapGrid()) {
      double snapped = ((int) (x / this.gridSize) * this.gridSize);
      return (int) Math.round(Math.min(Math.max(snapped, 0), Game.getEnvironment().getMap().getSizeInPixels().getWidth()));
    }

    if (Program.getUserPreferences().isSnapPixels()) {
      return MathUtilities.clamp((int) Math.round(x), 0, (int) Game.getEnvironment().getMap().getSizeInPixels().getWidth());
    }

    return MathUtilities.round((float) x, 2);
  }

  private float snapY(double y) {
    if (Program.getUserPreferences().isSnapGrid()) {
      int snapped = (int) (y / this.gridSize) * this.gridSize;
      return (int) Math.round(Math.min(Math.max(snapped, 0), Game.getEnvironment().getMap().getSizeInPixels().getHeight()));
    }

    if (Program.getUserPreferences().isSnapPixels()) {
      return MathUtilities.clamp((int) Math.round(y), 0, (int) Game.getEnvironment().getMap().getSizeInPixels().getHeight());
    }

    return MathUtilities.round((float) y, 2);
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

  private void renderMapObjectBounds(Graphics2D g) {
    // render all entities
    for (final IMapObjectLayer layer : Game.getEnvironment().getMap().getMapObjectLayers()) {
      if (layer == null) {
        continue;
      }

      if (!EditorScreen.instance().getMapSelectionPanel().isVisibleMapObjectLayer(layer.getName())) {
        continue;
      }

      Color colorBoundingBoxFill;
      if (layer.getColor() != null) {
        colorBoundingBoxFill = new Color(layer.getColor().getRed(), layer.getColor().getGreen(), layer.getColor().getBlue(), 15);
      } else {
        colorBoundingBoxFill = DEFAULT_COLOR_BOUNDING_BOX_FILL;
      }

      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject == null) {
          continue;
        }

        MapObjectType type = MapObjectType.get(mapObject.getType());
        final BasicStroke shapeStroke = new BasicStroke(1f / Game.getCamera().getRenderScale());
        // render spawn points
        if (type == MapObjectType.SPAWNPOINT) {
          g.setColor(COLOR_SPAWNPOINT);
          Game.getRenderEngine().renderShape(g, new Rectangle2D.Double(mapObject.getBoundingBox().getCenterX() - 1, mapObject.getBoundingBox().getCenterY() - 1, 2, 2));
        } else if (type == MapObjectType.PATH) {
          // render lane

          if (mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().isEmpty()) {
            continue;
          }

          // found the path for the rat
          final Path2D path = MapUtilities.convertPolylineToPath(mapObject);
          if (path == null) {
            continue;
          }

          g.setColor(COLOR_LANE);
          Game.getRenderEngine().renderOutline(g, path, shapeStroke);
          Point2D start = new Point2D.Double(mapObject.getLocation().getX(), mapObject.getLocation().getY());
          Game.getRenderEngine().renderShape(g, new Ellipse2D.Double(start.getX() - 1, start.getY() - 1, 3, 3));
          Game.getRenderEngine().renderText(g, "#" + mapObject.getId() + "(" + mapObject.getName() + ")", start.getX(), start.getY() - 5);
        }

        if (type != MapObjectType.COLLISIONBOX) {
          this.renderBoundingBox(g, mapObject, colorBoundingBoxFill, shapeStroke);
        }

        this.renderCollisionBox(g, mapObject, shapeStroke);
      }
    }
  }

  private void renderName(Graphics2D g, Color nameColor, IMapObject mapObject) {
    final int MAX_TAGS_RENDER_CHARS = 30;
    g.setColor(nameColor.getAlpha() > 100 ? nameColor : Color.WHITE);
    float textSize = 2.5f * zooms[this.currentZoomIndex];
    g.setFont(Program.TEXT_FONT.deriveFont(textSize).deriveFont(Font.PLAIN));

    String objectName = mapObject.getName();
    if (objectName != null && !objectName.isEmpty()) {
      Game.getRenderEngine().renderText(g, mapObject.getName(), mapObject.getX() + 1, mapObject.getBoundingBox().getMaxY() - 1);
    }

    // render tags
    String tags = mapObject.getCustomProperty(MapObjectProperty.TAGS);
    if (tags == null || tags.isEmpty()) {
      return;
    }

    FontMetrics fm = g.getFontMetrics();

    double y = objectName != null && !objectName.isEmpty() ? mapObject.getBoundingBox().getMaxY() - 2 - fm.getMaxDescent() : mapObject.getBoundingBox().getMaxY() - 1;
    if (tags.length() > MAX_TAGS_RENDER_CHARS) {
      tags = tags.substring(0, MAX_TAGS_RENDER_CHARS - 1);
      tags += "...";
    }

    Game.getRenderEngine().renderText(g, "[" + tags + "]", mapObject.getX() + 1, y);
  }

  private void renderGrid(Graphics2D g) {
    // render the grid
    if (Program.getUserPreferences().isShowGrid() && Game.getCamera().getRenderScale() >= 1) {

      g.setColor(new Color(255, 255, 255, 70));
      final Stroke stroke = new BasicStroke(1 / Game.getCamera().getRenderScale());
      final double viewPortX = Math.max(0, Game.getCamera().getViewPort().getX());
      final double viewPortMaxX = Math.min(Game.getEnvironment().getMap().getSizeInPixels().getWidth(), Game.getCamera().getViewPort().getMaxX());

      final double viewPortY = Math.max(0, Game.getCamera().getViewPort().getY());
      final double viewPortMaxY = Math.min(Game.getEnvironment().getMap().getSizeInPixels().getHeight(), Game.getCamera().getViewPort().getMaxY());
      final int startX = Math.max(0, (int) (viewPortX / gridSize) * gridSize);
      final int startY = Math.max(0, (int) (viewPortY / gridSize) * gridSize);
      for (int x = startX; x <= viewPortMaxX; x += gridSize) {
        Game.getRenderEngine().renderOutline(g, new Line2D.Double(x, viewPortY, x, viewPortMaxY), stroke);
      }

      for (int y = startY; y <= viewPortMaxY; y += gridSize) {
        Game.getRenderEngine().renderOutline(g, new Line2D.Double(viewPortX, y, viewPortMaxX, y), stroke);
      }
    }
  }

  private void renderNewObjectArea(Graphics2D g, Stroke shapeStroke) {
    if (this.newObjectArea == null) {
      return;
    }

    g.setColor(COLOR_NEWOBJECT_FILL);
    Game.getRenderEngine().renderShape(g, newObjectArea);
    g.setColor(COLOR_NEWOBJECT_BORDER);
    Game.getRenderEngine().renderOutline(g, newObjectArea, shapeStroke);
    g.setFont(g.getFont().deriveFont(Font.BOLD));
    Game.getRenderEngine().renderText(g, newObjectArea.getWidth() + "", newObjectArea.getX() + newObjectArea.getWidth() / 2 - 3, newObjectArea.getY() - 5);
    Game.getRenderEngine().renderText(g, newObjectArea.getHeight() + "", newObjectArea.getX() - 10, newObjectArea.getY() + newObjectArea.getHeight() / 2);
  }

  private void renderMouseSelectionArea(Graphics2D g, Stroke shapeStroke) {
    // draw mouse selection area
    final Point2D start = this.startPoint;
    if (start != null && !Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
      final Rectangle2D rect = this.getCurrentMouseSelectionArea(false);
      if (rect == null) {
        return;
      }

      g.setColor(COLOR_MOUSE_SELECTION_AREA_FILL);
      Game.getRenderEngine().renderShape(g, rect);
      g.setColor(COLOR_MOUSE_SELECTION_AREA_BORDER);
      Game.getRenderEngine().renderOutline(g, rect, shapeStroke);
    }
  }

  private void renderFocus(Graphics2D g) {
    // render the focus and the transform rects
    final Rectangle2D focus = this.getFocus();
    final IMapObject focusedMapObject = this.getFocusedMapObject();
    if (focus != null && focusedMapObject != null) {
      Stroke stroke = new BasicStroke(1 / Game.getCamera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.getLoop().getTicks() / 15);

      g.setColor(Color.BLACK);

      Game.getRenderEngine().renderOutline(g, focus, stroke);

      Stroke whiteStroke = new BasicStroke(1 / Game.getCamera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_MITER, 4, new float[] { 1f, 1f }, Game.getLoop().getTicks() / 15 - 1f);
      g.setColor(Color.WHITE);
      Game.getRenderEngine().renderOutline(g, focus, whiteStroke);

      // render transform rects
      if (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        Stroke transStroke = new BasicStroke(1 / Game.getCamera().getRenderScale());
        for (Rectangle2D trans : this.transformRects.values()) {
          g.setColor(COLOR_TRANSFORM_RECT_FILL);
          Game.getRenderEngine().renderShape(g, trans);
          g.setColor(Color.BLACK);
          Game.getRenderEngine().renderOutline(g, trans, transStroke);
        }
      }

      // render transform rects
      if (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        Stroke transStroke = new BasicStroke(1 / Game.getCamera().getRenderScale());
        for (Rectangle2D trans : this.transformRects.values()) {
          g.setColor(COLOR_TRANSFORM_RECT_FILL);
          Game.getRenderEngine().renderShape(g, trans);
          g.setColor(Color.BLACK);
          Game.getRenderEngine().renderOutline(g, trans, transStroke);
        }
      }
    }

    if (focusedMapObject != null) {
      Point2D loc = Game.getCamera().getViewPortLocation(new Point2D.Double(focusedMapObject.getX() + focusedMapObject.getWidth() / 2, focusedMapObject.getY()));
      g.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD, 15f));
      g.setColor(Color.WHITE);
      String id = "#" + focusedMapObject.getId();
      RenderEngine.drawText(g, id, loc.getX() * Game.getCamera().getRenderScale() - g.getFontMetrics().stringWidth(id) / 2.0, loc.getY() * Game.getCamera().getRenderScale() - (5 * this.currentTransformRectSize));
    }
  }

  private void renderSelection(Graphics2D g) {
    for (IMapObject mapObject : this.getSelectedMapObjects()) {
      if (mapObject.equals(this.getFocusedMapObject())) {
        continue;
      }

      Stroke stroke = new BasicStroke(1 / Game.getCamera().getRenderScale());

      g.setColor(colorSelectionBorder);
      Game.getRenderEngine().renderOutline(g, mapObject.getBoundingBox(), stroke);
    }
  }

  private void renderBoundingBox(Graphics2D g, IMapObject mapObject, Color colorBoundingBoxFill, BasicStroke shapeStroke) {
    MapObjectType type = MapObjectType.get(mapObject.getType());
    Color fillColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      fillColor = COLOR_TRIGGER_FILL;
    } else if (type == MapObjectType.STATICSHADOW) {
      fillColor = COLOR_SHADOW_FILL;
    }

    // render bounding boxes
    g.setColor(fillColor);

    // don't fill rect for lightsource because it is important to judge
    // the color
    if (type != MapObjectType.LIGHTSOURCE) {
      Game.getRenderEngine().renderShape(g, mapObject.getBoundingBox());
    }

    Color borderColor = colorBoundingBoxFill;
    if (type == MapObjectType.TRIGGER) {
      borderColor = COLOR_TRIGGER_BORDER;
    } else if (type == MapObjectType.LIGHTSOURCE) {
      final String mapObjectColor = mapObject.getCustomProperty(MapObjectProperty.LIGHT_COLOR);
      if (mapObjectColor != null && !mapObjectColor.isEmpty()) {
        Color lightColor = Color.decode(mapObjectColor);
        borderColor = new Color(lightColor.getRed(), lightColor.getGreen(), lightColor.getBlue(), 180);
      }
    } else if (type == MapObjectType.STATICSHADOW) {
      borderColor = COLOR_SHADOW_BORDER;
    } else if (type == MapObjectType.SPAWNPOINT) {
      borderColor = COLOR_SPAWNPOINT;
    }

    g.setColor(borderColor);

    Game.getRenderEngine().renderOutline(g, mapObject.getBoundingBox(), shapeStroke);

    this.renderName(g, borderColor, mapObject);
  }

  private void renderCollisionBox(Graphics2D g, IMapObject mapObject, BasicStroke shapeStroke) {
    // render collision boxes
    boolean collision = mapObject.getCustomPropertyBool(MapObjectProperty.COLLISION, false);
    float collisionBoxWidth = mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_WIDTH, -1);
    float collisionBoxHeight = mapObject.getCustomPropertyFloat(MapObjectProperty.COLLISIONBOX_HEIGHT, -1);
    final Align align = Align.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_ALIGN));
    final Valign valign = Valign.get(mapObject.getCustomProperty(MapObjectProperty.COLLISION_VALIGN));

    if (MapObjectType.get(mapObject.getType()) == MapObjectType.COLLISIONBOX) {
      collisionBoxWidth = mapObject.getWidth();
      collisionBoxHeight = mapObject.getHeight();
      collision = true;
    }

    if (collisionBoxWidth != -1 && collisionBoxHeight != -1) {

      g.setColor(COLOR_COLLISION_FILL);
      Rectangle2D collisionBox = CollisionEntity.getCollisionBox(mapObject.getLocation(), mapObject.getWidth(), mapObject.getHeight(), collisionBoxWidth, collisionBoxHeight, align, valign);

      Game.getRenderEngine().renderShape(g, collisionBox);
      g.setColor(collision ? COLOR_COLLISION_BORDER : COLOR_NOCOLLISION_BORDER);

      Stroke collisionStroke = collision ? shapeStroke : new BasicStroke(1 / Game.getCamera().getRenderScale(), BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL, 0, new float[] { 1f }, 0);
      Game.getRenderEngine().renderOutline(g, collisionBox, collisionStroke);
    }
  }

  @Override
  public void update() {
    if (this.focusBorderBrightness <= 0.4) {
      this.focusBorderBrightnessIncreasing = true;
    } else if (this.focusBorderBrightness >= 0.9) {
      this.focusBorderBrightnessIncreasing = false;
    }

    if (this.focusBorderBrightnessIncreasing && this.focusBorderBrightness < 0.9) {
      this.focusBorderBrightness += 0.005;
    } else if (!focusBorderBrightnessIncreasing && this.focusBorderBrightness >= 0.4) {
      this.focusBorderBrightness -= 0.005;
    }

    this.colorSelectionBorder = Color.getHSBColor(0, 0, this.focusBorderBrightness);
  }
}
