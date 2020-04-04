package de.gurkenlabs.utiliti.components;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.environment.tilemap.xml.TmxMap;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.gui.ComponentMouseEvent;
import de.gurkenlabs.litiengine.gui.ComponentMouseWheelEvent;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.MathUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.ImageSerializer;
import de.gurkenlabs.utiliti.Cursors;
import de.gurkenlabs.utiliti.UndoManager;
import de.gurkenlabs.utiliti.handlers.Snap;
import de.gurkenlabs.utiliti.handlers.Transform;
import de.gurkenlabs.utiliti.handlers.Transform.TransformType;
import de.gurkenlabs.utiliti.handlers.Zoom;
import de.gurkenlabs.utiliti.renderers.Renderers;
import de.gurkenlabs.utiliti.swing.UI;
import de.gurkenlabs.utiliti.swing.dialogs.ConfirmDialog;
import de.gurkenlabs.utiliti.swing.dialogs.XmlExportDialog;
import de.gurkenlabs.utiliti.swing.dialogs.XmlImportDialog;

public class MapComponent extends GuiComponent {

  public static final int EDITMODE_CREATE = 0;
  public static final int EDITMODE_EDIT = 1;

  /**
   * @deprecated Will be replaced by {@link TransformType#MOVE}
   */
  @Deprecated()
  public static final int EDITMODE_MOVE = 2;
  private static final Logger log = Logger.getLogger(MapComponent.class.getName());

  private static final int BASE_SCROLL_SPEED = 50;

  private final List<IntConsumer> editModeChangedConsumer;
  private final List<Consumer<IMapObject>> focusChangedConsumer;
  private final List<Consumer<List<IMapObject>>> selectionChangedConsumer;
  private final List<Consumer<TmxMap>> loadingConsumer;
  private final List<Consumer<TmxMap>> loadedConsumer;
  private final List<Consumer<Blueprint>> copyTargetChangedConsumer;

  private final Map<String, Point2D> cameraFocus;
  private final Map<String, IMapObject> focusedObjects;
  private final Map<String, List<IMapObject>> selectedObjects;
  private final Map<String, Environment> environments;

  private int editMode = EDITMODE_EDIT;

  private final List<TmxMap> maps;

  private double scrollSpeed = BASE_SCROLL_SPEED;

  private Point2D startPoint;
  private Blueprint copiedBlueprint;

  /**
   * This flag is used to control the undo behavior of a <b>move
   * transformation</b>. It ensures that the UndoManager tracks the "changing"
   * event in the beginning of the operation (when the key event is recorded for
   * the first time) and also triggers the "changed" event upon key release.
   */
  private boolean isMoving;

  /**
   * This flag is used to control the undo behavior of a <b>resize
   * transformation</b>. It ensures that the UndoManager tracks the "changing"
   * event in the beginning of the operation (when the key event is recorded for
   * the first time) and also triggers the "changed" event upon key release.
   */
  private boolean isResizing;

  /**
   * This flag is used to bundle a move operation over several key events until
   * the arrow keys are released. This allows for the UndoManager to revert the
   * keyboard move operation once instead of having to revert for each
   * individual key stroke.
   */
  private boolean isMovingWithKeyboard;

  /**
   * This flag prevents circular focusing approaches while this instance is
   * already performing a focus process.
   */
  private boolean isFocussing;

  /**
   * This flag prevents certain UI operations from executing while the editor is
   * loading an environment.
   */
  private boolean loading;

  /**
   * Ensures that various initialization processes are only carried out once.
   */
  private boolean initialized;

  public MapComponent() {
    super(0, 0);
    this.editModeChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.selectionChangedConsumer = new CopyOnWriteArrayList<>();
    this.loadingConsumer = new CopyOnWriteArrayList<>();
    this.loadedConsumer = new CopyOnWriteArrayList<>();
    this.copyTargetChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusedObjects = new ConcurrentHashMap<>();
    this.selectedObjects = new ConcurrentHashMap<>();
    this.environments = new ConcurrentHashMap<>();
    this.maps = new CopyOnWriteArrayList<>();
    this.cameraFocus = new ConcurrentHashMap<>();
    this.onMouseEnter(e -> Game.window().cursor().setVisible(true));
    this.onMouseLeave(e -> Game.window().cursor().setVisible(false));

    UndoManager.onUndoStackChanged(e -> Transform.updateAnchors());
  }

  public static boolean mapIsNull() {
    return Game.world().environment() == null || Game.world().environment().getMap() == null;
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

    Renderers.render(g);
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

    this.loadMaps(loadedMaps, true);
  }

  public void loadMaps(List<TmxMap> maps, boolean clearSelection) {
    if (maps == null) {
      return;
    }
    UI.getInspector().bind(null);
    this.setFocus(null, true);
    this.getMaps().clear();
    Collections.sort(maps);
    this.getMaps().addAll(maps);
    UI.getMapController().bind(this.getMaps(), clearSelection);
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

  public Rectangle2D getFocusBounds() {
    final IMapObject focusedObject = this.getFocusedMapObject();
    if (focusedObject == null) {
      return null;
    }

    return focusedObject.getBoundingBox();
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
    Game.world().camera().onZoom(event -> {
      Transform.updateAnchors();
      this.scrollSpeed = BASE_SCROLL_SPEED / event.getZoom();
    });

    Zoom.applyPreference();

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
        cons.accept(Game.world().environment() != null ? ((TmxMap) Game.world().environment().getMap()) : null);
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
      UI.getMapController().setSelection(map);
      UI.getInspector().bind(this.getFocusedMapObject());

      for (Consumer<TmxMap> cons : this.loadedConsumer) {
        cons.accept(map);
      }

    } finally {
      this.loading = false;
    }
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
    this.getSelectedMapObjects().clear();
    this.setFocus(null, true);
    for (IMapObject mapObject : layer.getMapObjects()) {
      Game.world().environment().loadFromMap(mapObject.getId());

      this.setSelection(mapObject, false);
      this.setFocus(mapObject, false);
    }
    Transform.updateAnchors();
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

    Editor.instance().getGameFile().getBluePrints().add(blueprint);
  }

  public void centerCameraOnFocus() {
    if (this.hasFocus() && this.getFocusedMapObject() != null) {
      final Rectangle2D focus = this.getFocusBounds();
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

  public int getEditMode() {
    return this.editMode;
  }

  public void setEditMode(int editMode) {
    if (editMode == this.editMode) {
      return;
    }

    switch (editMode) {
    case EDITMODE_CREATE:
      this.setFocus(null, true);
      UI.getInspector().bind(null);
      Game.window().cursor().set(Cursors.ADD, 0, 0);
      break;
    case EDITMODE_EDIT:
      Game.window().cursor().set(Cursors.DEFAULT, 0, 0);
      break;
    case EDITMODE_MOVE:
      Game.window().cursor().set(Cursors.MOVE, 0, 0);
      break;
    default:
      break;
    }

    this.editMode = editMode;
    for (IntConsumer cons : this.editModeChangedConsumer) {
      cons.accept(this.editMode);
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

      if (mapIsNull()) {
        return;
      }

      if (this.isMoving || this.isResizing) {
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

      Transform.updateAnchors();
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
        this.getSelectedMapObjects().add(mapObject);
      }
    }

    for (Consumer<List<IMapObject>> cons : this.selectionChangedConsumer) {
      cons.accept(this.getSelectedMapObjects());
    }
  }

  public void deleteMap() {
    if (this.getMaps() == null || this.getMaps().isEmpty()) {
      return;
    }

    if (mapIsNull()) {
      return;
    }

    if (!ConfirmDialog.show(Resources.strings().get("hud_deleteMap"), Resources.strings().get("hud_deleteMapMessage") + "\n" + Game.world().environment().getMap().getName())) {
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

    Editor.instance().updateGameFileMaps();
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
        layer.setName(MapObjectLayer.DEFAULT_MAPOBJECTLAYER_NAME);
        map.addLayer(layer);
      }

      Optional<TmxMap> current = this.maps.stream().filter(x -> x.getName().equals(map.getName())).findFirst();
      if (current.isPresent()) {
        if (ConfirmDialog.show(Resources.strings().get("input_replace_map_title"), Resources.strings().get("input_replace_map", map.getName()))) {
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
      if (this.environments.containsKey(map.getName())) {
        this.environments.remove(map.getName());
      }

      UI.getMapController().bind(this.getMaps(), true);
      this.loadEnvironment(map);
      log.log(Level.INFO, "imported map {0}", new Object[] { map.getName() });
    }, TmxMap.FILE_EXTENSION);

  }

  public void exportMap() {
    if (this.getMaps() == null || this.getMaps().isEmpty()) {
      return;
    }

    TmxMap map = (TmxMap) Game.world().environment().getMap();
    if (map == null) {
      return;
    }

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

  public void reassignIds(TmxMap map, int startID) {
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
    log.log(Level.INFO, "Reassigned IDs for Map {0}.", new Object[] { map.getName() });
  }

  @Override
  protected boolean mouseEventShouldBeForwarded(final MouseEvent e) {
    return this.isForwardMouseEvents() && this.isVisible() && this.isEnabled() && !this.isSuspended() && e != null;
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
      final String timeStamp = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(new Date());
      final File folder = new File("./screenshots/");
      if (!folder.exists()) {
        folder.mkdirs();
      }
      File snapshot = new File("./screenshots/" + timeStamp + ImageFormat.PNG.toFileExtension());
      ImageSerializer.saveImage(snapshot.toString(), img);
      log.log(Level.INFO, "Saved map snapshot to {0}", new Object[] { snapshot.getCanonicalPath() });
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
      minX = MathUtilities.clamp(minX, 0, map.getSizeInPixels().width);
      maxX = MathUtilities.clamp(maxX, 0, map.getSizeInPixels().width);
      minY = MathUtilities.clamp(minY, 0, map.getSizeInPixels().height);
      maxY = MathUtilities.clamp(maxY, 0, map.getSizeInPixels().height);
    }

    double width = Math.abs(minX - maxX);
    double height = Math.abs(minY - maxY);

    return new Rectangle2D.Double(minX, minY, width, height);
  }

  private IMapObject createNewMapObject(MapObjectType type) {
    final Rectangle2D newObjectArea = this.getMouseSelectArea(true);
    IMapObject mo = new MapObject();
    mo.setType(type.toString());
    mo.setX((float) newObjectArea.getX());
    mo.setY((float) newObjectArea.getY());

    // ensure a minimum size for the new object
    float width = (float) newObjectArea.getWidth();
    float height = (float) newObjectArea.getHeight();
    mo.setWidth(width == 0 ? 16 : width);
    mo.setHeight(height == 0 ? 16 : height);
    mo.setId(Game.world().environment().getNextMapId());
    mo.setName("");

    switch (type) {
    case PROP:
      mo.setValue(MapObjectProperty.COLLISIONBOX_WIDTH, (newObjectArea.getWidth() * 0.4));
      mo.setValue(MapObjectProperty.COLLISIONBOX_HEIGHT, (newObjectArea.getHeight() * 0.4));
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

  private void setCopyBlueprint(Blueprint copyTarget) {
    this.copiedBlueprint = copyTarget;
    for (Consumer<Blueprint> consumer : this.copyTargetChangedConsumer) {
      consumer.accept(this.copiedBlueprint);
    }
  }

  private void setupKeyboardControls() {
    Input.keyboard().onKeyPressed(KeyEvent.VK_CONTROL, e -> {
      if (this.editMode == EDITMODE_EDIT && this.getFocusBounds() != null) {
        this.setEditMode(EDITMODE_MOVE);
      }
    });

    Input.keyboard().onKeyReleased(KeyEvent.VK_CONTROL, e -> {
      if (this.editMode == EDITMODE_MOVE) {
        this.setEditMode(EDITMODE_EDIT);
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_ESCAPE, e -> {
      if (this.editMode == EDITMODE_CREATE) {
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

      this.afterArrowKeysReleased();
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_RIGHT, e -> this.handleKeyboardTransform(1, 0));
    Input.keyboard().onKeyPressed(KeyEvent.VK_LEFT, e -> this.handleKeyboardTransform(-1, 0));
    Input.keyboard().onKeyPressed(KeyEvent.VK_UP, e -> this.handleKeyboardTransform(0, -1));
    Input.keyboard().onKeyPressed(KeyEvent.VK_DOWN, e -> this.handleKeyboardTransform(0, 1));
  }

  private void handleKeyboardTransform(int x, int y) {
    if (!Game.window().getRenderComponent().hasFocus()) {
      return;
    }

    this.beforeArrowKeyPressed();
    Transform.moveEntities(this.getSelectedMapObjects(), x, y);
  }

  private void beforeArrowKeyPressed() {
    if (!this.isMovingWithKeyboard) {
      UndoManager.instance().beginOperation();
      for (IMapObject selected : this.getSelectedMapObjects()) {
        UndoManager.instance().mapObjectChanging(selected);
      }

      this.isMovingWithKeyboard = true;
    }

    Transform.startDragging(this.getSelectedMapObjects());
  }

  private void afterArrowKeysReleased() {
    if (this.isMovingWithKeyboard) {
      for (IMapObject selected : this.getSelectedMapObjects()) {
        UndoManager.instance().mapObjectChanged(selected);
      }

      UndoManager.instance().endOperation();
      this.isMovingWithKeyboard = false;
      Transform.resetDragging();
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
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    final Point2D currentFocus = Game.world().camera().getFocus();
    // horizontal scrolling
    if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
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
        Zoom.in();
      } else {
        Zoom.out();
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
    Transform.updateTransform();
  }

  private void handleMousePressed(ComponentMouseEvent e) {
    if (!this.hasFocus() || mapIsNull()) {
      return;
    }

    if (Transform.type() == TransformType.MOVE) {
      this.setEditMode(EDITMODE_MOVE);
    }

    switch (this.editMode) {
    case EDITMODE_CREATE:
    case EDITMODE_MOVE:
      if (SwingUtilities.isLeftMouseButton(e.getEvent())) {
        this.startPoint = Input.mouse().getMapLocation();
      }
      break;
    case EDITMODE_EDIT:
      if (this.isMoving || Transform.type() == TransformType.RESIZE || SwingUtilities.isRightMouseButton(e.getEvent())) {
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

    switch (this.editMode) {
    case EDITMODE_EDIT:
      if (Transform.type() == TransformType.RESIZE) {
        if (!this.isResizing) {
          this.isResizing = true;
          UndoManager.instance().mapObjectChanging(this.getFocusedMapObject());
        }

        Transform.resize();
      }

      break;
    case EDITMODE_MOVE:
      if (!this.isMoving) {
        this.isMoving = true;

        UndoManager.instance().beginOperation();
        for (IMapObject selected : this.getSelectedMapObjects()) {
          UndoManager.instance().mapObjectChanging(selected);
        }
      }

      Transform.move();

      break;
    default:
      break;
    }
  }

  private void handleMouseReleased(ComponentMouseEvent e) {
    if (!this.hasFocus() || mapIsNull() || !SwingUtilities.isLeftMouseButton(e.getEvent())) {
      return;
    }

    Transform.resetDragging();

    switch (this.editMode) {
    case EDITMODE_CREATE:
      if (SwingUtilities.isRightMouseButton(e.getEvent())) {
        this.setEditMode(EDITMODE_EDIT);
        break;
      }

      IMapObject mo = this.createNewMapObject(UI.getInspector().getObjectType());

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
      if (this.isMoving || this.isResizing) {
        this.isMoving = false;
        this.isResizing = false;
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
    Rectangle2D rect = this.getMouseSelectArea(false);
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
        if (this.getSelectedMapObjects().contains(mapObject)) {
          this.getSelectedMapObjects().remove(mapObject);
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
}