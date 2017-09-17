package de.gurkenlabs.utiLITI.Components;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.awt.Stroke;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.SpriteSheetInfo;
import de.gurkenlabs.litiengine.entities.CollisionEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionAlign;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionValign;
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehaviour;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.environment.tilemap.IImageLayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.ITileset;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.OrthogonalMapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.TmxMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Map;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.gui.GuiComponent;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.utiLITI.EditorScreen;
import de.gurkenlabs.utiLITI.Program;
import de.gurkenlabs.utiLITI.UndoManager;
import de.gurkenlabs.util.MathUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.io.FileUtilities;
import de.gurkenlabs.util.io.ImageSerializer;

public class MapComponent extends EditorComponent {
  public enum TransformType {
    UP,
    DOWN,
    LEFT,
    RIGHT,
    UPLEFT,
    UPRIGHT,
    DOWNLEFT,
    DOWNRIGHT,
    NONE
  }

  private static final int TRANSFORM_RECT_SIZE = 6;
  private static final int BASE_SCROLL_SPEED = 50;
  private double currentTransformRectSize = TRANSFORM_RECT_SIZE;
  private final java.util.Map<TransformType, Rectangle2D> transformRects;
  private TransformType currentTransform;

  public static final int EDITMODE_CREATE = 0;
  public static final int EDITMODE_EDIT = 1;
  public static final int EDITMODE_MOVE = 2;
  private final List<Consumer<Integer>> editModeChangedConsumer;
  private final List<Consumer<IMapObject>> focusChangedConsumer;
  private final List<Consumer<Map>> mapLoadedConsumer;
  private int currentEditMode = EDITMODE_EDIT;
  private static final float[] zooms = new float[] { 0.1f, 0.25f, 0.5f, 1, 1.5f, 2f, 3f, 4f, 5f, 6f, 7f, 8f, 9f, 10f, 16f, 32f, 50f, 80f, 100f };
  private int currentZoomIndex = 7;

  private final java.util.Map<String, Point2D> cameraFocus;
  private final java.util.Map<String, IMapObject> focusedObjects;

  private final List<Map> maps;

  private float scrollSpeed = BASE_SCROLL_SPEED;

  private Point2D startPoint;
  private Point2D dragPoint;
  private Point2D dragLocationMapObject;
  private boolean isMoving;
  private boolean isTransforming;
  private Dimension dragSizeMapObject;
  private Rectangle2D newObject;
  private IMapObject copiedMapObject;
  private int gridSize;

  public boolean snapToGrid = true;
  public boolean renderGrid = false;
  public boolean renderCollisionBoxes = true;

  private final EditorScreen screen;

  private boolean isLoading;

  public MapComponent(final EditorScreen screen) {
    super(ComponentType.MAP);
    this.editModeChangedConsumer = new CopyOnWriteArrayList<>();
    this.focusChangedConsumer = new CopyOnWriteArrayList<>();
    this.mapLoadedConsumer = new CopyOnWriteArrayList<>();
    this.focusedObjects = new ConcurrentHashMap<>();
    this.maps = new ArrayList<>();
    this.cameraFocus = new ConcurrentHashMap<>();
    this.transformRects = new ConcurrentHashMap<>();
    this.screen = screen;
    Game.getScreenManager().getCamera().onZoomChanged(zoom -> {
      this.currentTransformRectSize = TRANSFORM_RECT_SIZE / zoom;
      this.updateTransformControls();
    });
    this.gridSize = Program.USER_PREFERNCES.getGridSize();
  }

  public void onEditModeChanged(Consumer<Integer> cons) {
    this.editModeChangedConsumer.add(cons);
  }

  public void onFocusChanged(Consumer<IMapObject> cons) {
    this.focusChangedConsumer.add(cons);
  }

  public void onMapLoaded(Consumer<Map> cons) {
    this.mapLoadedConsumer.add(cons);
  }

  @Override
  public void render(Graphics2D g) {
    Color COLOR_BOUNDING_BOX_FILL = new Color(0, 0, 0, 35);
    Color COLOR_NAME_FILL = new Color(0, 0, 0, 60);
    Color COLOR_BOUNDING_BOX_BORDER = new Color(0, 0, 0, 150);
    final Color COLOR_FOCUS_FILL = new Color(0, 0, 0, 50);
    final Color COLOR_FOCUS_BORDER = Color.BLACK;
    final Color COLOR_COLLISION_FILL = new Color(255, 0, 0, 15);
    final Color COLOR_NOCOLLISION_FILL = new Color(255, 100, 0, 15);
    final Color COLOR_COLLISION_BORDER = Color.RED;
    final Color COLOR_NOCOLLISION_BORDER = Color.ORANGE;
    final Color COLOR_SPAWNPOINT = Color.GREEN;
    final Color COLOR_LANE = Color.YELLOW;
    final Color COLOR_NEWOBJECT_FILL = new Color(0, 255, 0, 50);
    final Color COLOR_NEWOBJECT_BORDER = Color.GREEN.darker();
    final Color COLOR_TRANSFORM_RECT_FILL = new Color(255, 255, 255, 100);
    final Color COLOR_SHADOW_FILL = new Color(85, 130, 200, 15);
    final Color COLOR_SHADOW_BORDER = new Color(30, 85, 170);

    if (this.renderCollisionBoxes) {
      // render all entities
      for (final IMapObjectLayer layer : Game.getEnvironment().getMap().getMapObjectLayers()) {
        if (layer == null) {
          continue;
        }

        if (!EditorScreen.instance().getMapSelectionPanel().isSelectedMapObjectLayer(layer.getName())) {
          continue;
        }

        if (layer.getColor() != null) {
          COLOR_BOUNDING_BOX_BORDER = layer.getColor();
          COLOR_BOUNDING_BOX_FILL = new Color(layer.getColor().getRed(), layer.getColor().getGreen(), layer.getColor().getBlue(), 15);
        } else {
          COLOR_BOUNDING_BOX_BORDER = new Color(0, 0, 0, 150);
          COLOR_BOUNDING_BOX_FILL = new Color(0, 0, 0, 35);
        }

        for (final IMapObject mapObject : layer.getMapObjects()) {
          if (mapObject == null) {
            continue;
          }
          String objectName = mapObject.getName();
          if (objectName != null && !objectName.isEmpty()) {
            Rectangle2D nameBackground = new Rectangle2D.Double(mapObject.getX(), mapObject.getBoundingBox().getMaxY() - 3,
                mapObject.getDimension().getWidth(), 3);
            g.setColor(COLOR_NAME_FILL);
            RenderEngine.fillShape(g, nameBackground);
          }

          MapObjectType type = MapObjectType.get(mapObject.getType());

          // render spawn points
          if (type == MapObjectType.SPAWNPOINT) {
            g.setColor(COLOR_SPAWNPOINT);
            RenderEngine.fillShape(g,
                new Rectangle2D.Double(mapObject.getBoundingBox().getCenterX() - 1, mapObject.getBoundingBox().getCenterY() - 1, 2, 2));
            RenderEngine.drawShape(g, mapObject.getBoundingBox());
          } else if (type == MapObjectType.COLLISIONBOX) {
            g.setColor(COLOR_COLLISION_FILL);
            RenderEngine.fillShape(g, mapObject.getBoundingBox());
            g.setColor(COLOR_COLLISION_BORDER);
            RenderEngine.drawShape(g, mapObject.getBoundingBox());
          } else if (type == MapObjectType.STATICSHADOW) {
            g.setColor(COLOR_SHADOW_FILL);
            RenderEngine.fillShape(g, mapObject.getBoundingBox());
            g.setColor(COLOR_SHADOW_BORDER);
            RenderEngine.drawShape(g, mapObject.getBoundingBox());
          } else if (type == MapObjectType.LANE) {
            // render lane

            if (mapObject.getPolyline() == null || mapObject.getPolyline().getPoints().size() == 0) {
              continue;
            }
            // found the path for the rat
            final Path2D path = MapUtilities.convertPolylineToPath(mapObject);
            if (path == null) {
              continue;
            }

            g.setColor(COLOR_LANE);
            RenderEngine.drawShape(g, path);
            Point2D start = new Point2D.Double(mapObject.getLocation().getX(), mapObject.getLocation().getY());
            RenderEngine.fillShape(g, new Ellipse2D.Double(start.getX() - 1, start.getY() - 1, 3, 3));
            RenderEngine.drawMapText(g, "#" + mapObject.getId() + "(" + mapObject.getName() + ")", start.getX(), start.getY() - 5);
          }

          // render bounding boxes
          final IMapObject focusedMapObject = this.getFocusedMapObject();
          if (focusedMapObject == null || mapObject.getId() != focusedMapObject.getId()) {
            g.setColor(COLOR_BOUNDING_BOX_FILL);

            // don't fill rect for lightsource because it is important to judge
            // the color
            if (type != MapObjectType.LIGHTSOURCE) {
              if (type == MapObjectType.TRIGGER) {
                g.setColor(COLOR_NOCOLLISION_FILL);
              }
              RenderEngine.fillShape(g, mapObject.getBoundingBox());
            }

            if (type == MapObjectType.TRIGGER) {
              g.setColor(COLOR_NOCOLLISION_BORDER);
            }

            RenderEngine.drawShape(g, mapObject.getBoundingBox());
          }

          // render collision boxes
          String coll = mapObject.getCustomProperty(MapObjectProperties.COLLISION);
          final String collisionBoxWidthFactor = mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH);
          final String collisionBoxHeightFactor = mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT);
          final CollisionAlign align = CollisionAlign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN));
          final CollisionValign valign = CollisionValign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN));

          if (coll != null && collisionBoxWidthFactor != null && collisionBoxHeightFactor != null) {
            boolean collision = Boolean.valueOf(coll);
            final Color collisionColor = collision ? COLOR_COLLISION_FILL : COLOR_NOCOLLISION_FILL;
            final Color collisionShapeColor = collision ? COLOR_COLLISION_BORDER : COLOR_NOCOLLISION_BORDER;

            float collisionBoxWidth = 0, collisionBoxHeight = 0;
            try {
              collisionBoxWidth = Float.parseFloat(collisionBoxWidthFactor);
              collisionBoxHeight = Float.parseFloat(collisionBoxHeightFactor);
            } catch (NumberFormatException | NullPointerException e) {
              e.printStackTrace();
            }
            g.setColor(collisionColor);
            Rectangle2D collisionBox = CollisionEntity.getCollisionBox(mapObject.getLocation(), mapObject.getDimension().getWidth(),
                mapObject.getDimension().getHeight(), collisionBoxWidth, collisionBoxHeight, align, valign);

            RenderEngine.fillShape(g, collisionBox);
            g.setColor(collisionShapeColor);
            RenderEngine.drawShape(g, collisionBox);
          }
          g.setColor(Color.WHITE);
          float textSize = 3 * zooms[this.currentZoomIndex];
          g.setFont(Program.TEXT_FONT.deriveFont(textSize));
          RenderEngine.drawMapText(g, objectName, mapObject.getX() + 1, mapObject.getBoundingBox().getMaxY());
        }
      }
    }

    switch (this.currentEditMode) {
    case EDITMODE_CREATE:
      if (newObject != null) {
        g.setColor(COLOR_NEWOBJECT_FILL);
        RenderEngine.fillShape(g, newObject);
        g.setColor(COLOR_NEWOBJECT_BORDER);
        RenderEngine.drawShape(g, newObject);
        g.setFont(g.getFont().deriveFont(Font.BOLD));
        RenderEngine.drawMapText(g, newObject.getWidth() + "", newObject.getX() + newObject.getWidth() / 2 - 3, newObject.getY() - 5);
        RenderEngine.drawMapText(g, newObject.getHeight() + "", newObject.getX() - 10, newObject.getY() + newObject.getHeight() / 2);
      }
      break;
    case EDITMODE_EDIT:
      // draw selection
      final Point2D start = this.startPoint;
      if (start != null && !Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        Rectangle2D rect = this.getCurrentMouseSelectionArea();

        g.setColor(new Color(0, 130, 152, 30));
        RenderEngine.fillShape(g, rect);
        if (rect.getWidth() > 0 || rect.getHeight() > 0) {
          g.setColor(new Color(0, 130, 152, 255));
          g.setFont(g.getFont().deriveFont(Font.BOLD));
          RenderEngine.drawMapText(g, rect.getWidth() + "", rect.getX() + rect.getWidth() / 2 - 3, rect.getY() - 5);
          RenderEngine.drawMapText(g, rect.getHeight() + "", rect.getX() - 10, rect.getY() + rect.getHeight() / 2);
        }
        g.setColor(new Color(0, 130, 152, 150));
        RenderEngine.drawShape(g, rect,
            new BasicStroke(2 / Game.getInfo().getRenderScale(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1 }, 0));
      }
      break;
    }

    // render the focus and the transform rects
    final Rectangle2D focus = this.getFocus();
    final IMapObject focusedMapObject = this.getFocusedMapObject();
    if (focus != null && focusedMapObject != null) {
      Stroke stroke = new BasicStroke(2 / Game.getInfo().getRenderScale(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 1f }, 0);
      if (MapObjectType.get(focusedMapObject.getType()) != MapObjectType.LIGHTSOURCE) {
        g.setColor(COLOR_FOCUS_FILL);
        RenderEngine.fillShape(g, focus);
      }

      g.setColor(COLOR_FOCUS_BORDER);
      RenderEngine.drawShape(g, focus, stroke);

      // render transform rects
      if (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        Stroke transStroke = new BasicStroke(1 / Game.getInfo().getRenderScale());
        for (Rectangle2D trans : this.transformRects.values()) {
          g.setColor(COLOR_TRANSFORM_RECT_FILL);
          RenderEngine.fillShape(g, trans);
          g.setColor(COLOR_FOCUS_BORDER);
          RenderEngine.drawShape(g, trans, transStroke);
        }
      }
    }

    if (focusedMapObject != null) {
      Point2D loc = Game.getScreenManager().getCamera()
          .getViewPortLocation(new Point2D.Double(focusedMapObject.getX() + focusedMapObject.getDimension().getWidth() / 2, focusedMapObject.getY()));
      g.setFont(Program.TEXT_FONT.deriveFont(Font.BOLD, 15f));
      g.setColor(COLOR_FOCUS_BORDER);
      String id = "#" + focusedMapObject.getId();
      RenderEngine.drawText(g, id, loc.getX() * Game.getInfo().getRenderScale() - g.getFontMetrics().stringWidth(id) / 2,
          loc.getY() * Game.getInfo().getRenderScale() - (5 * this.currentTransformRectSize));
      if (MapObjectType.get(focusedMapObject.getType()) == MapObjectType.TRIGGER) {
        g.setColor(COLOR_NOCOLLISION_BORDER);
        g.setFont(Program.TEXT_FONT.deriveFont(11f));
        RenderEngine.drawMapText(g, focusedMapObject.getName(), focusedMapObject.getX() + 2, focusedMapObject.getY() + 5);
      }
    }

    // render the grid
    if (this.renderGrid && Game.getInfo().getRenderScale() >= 1) {

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

      g.setColor(new Color(255, 255, 255, 100));
      Stroke stroke = new BasicStroke(1 / Game.getInfo().getRenderScale(), BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[] { 0.5f }, 0);
      final double viewPortX = Math.max(0, Game.getScreenManager().getCamera().getViewPort().getX());
      final double viewPortMaxX = Math.min(Game.getEnvironment().getMap().getSizeInPixels().getWidth(),
          Game.getScreenManager().getCamera().getViewPort().getMaxX());

      final double viewPortY = Math.max(0, Game.getScreenManager().getCamera().getViewPort().getY());
      final double viewPortMaxY = Math.min(Game.getEnvironment().getMap().getSizeInPixels().getHeight(),
          Game.getScreenManager().getCamera().getViewPort().getMaxY());
      final int startX = Math.max(0, (int) (viewPortX / gridSize) * gridSize);
      final int startY = Math.max(0, (int) (viewPortY / gridSize) * gridSize);
      for (int x = startX; x <= viewPortMaxX; x += gridSize) {
        RenderEngine.drawShape(g, new Line2D.Double(x, viewPortY, x, viewPortMaxY), stroke);
      }

      for (int y = startY; y <= viewPortMaxY; y += gridSize) {
        RenderEngine.drawShape(g, new Line2D.Double(viewPortX, y, viewPortMaxX, y), stroke);
      }

      g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    }

    super.render(g);
  }

  public void loadMaps(String projectPath) {
    final List<String> files = FileUtilities.findFiles(new ArrayList<>(), Paths.get(projectPath), "tmx");
    System.out.println(files.size() + " maps found in folder " + projectPath);
    final List<Map> maps = new ArrayList<>();
    for (final String mapFile : files) {
      final IMapLoader tmxLoader = new TmxMapLoader();
      Map map = (Map) tmxLoader.loadMap(mapFile);
      maps.add(map);
      System.out.println("map found: " + map.getFileName());
    }

    this.loadMaps(maps);
  }

  public void loadMaps(List<Map> maps) {
    EditorScreen.instance().getMapObjectPanel().bind(null);
    this.setFocus(null);
    this.getMaps().clear();
    maps.sort(new Comparator<Map>() {

      @Override
      public int compare(Map arg0, Map arg1) {
        // TODO Auto-generated method stub
        return arg0.getName().compareTo(arg1.getName());
      }
    });

    this.getMaps().addAll(maps);
    EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps());
  }

  public List<Map> getMaps() {
    return this.maps;
  }

  public int getGridSize() {
    return this.gridSize;
  }

  @Override
  public void prepare() {
    Game.getScreenManager().getCamera().setZoom(zooms[this.currentZoomIndex], 0);
    Program.horizontalScroll.addAdjustmentListener(new AdjustmentListener() {

      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (isLoading) {
          return;
        }

        Point2D cameraFocus = new Point2D.Double(Program.horizontalScroll.getValue(), Game.getScreenManager().getCamera().getFocus().getY());
        Game.getScreenManager().getCamera().setFocus(cameraFocus);
      }
    });

    Program.verticalScroll.addAdjustmentListener(new AdjustmentListener() {

      @Override
      public void adjustmentValueChanged(AdjustmentEvent e) {
        if (isLoading) {
          return;
        }
        Point2D cameraFocus = new Point2D.Double(Game.getScreenManager().getCamera().getFocus().getX(), Program.verticalScroll.getValue());
        Game.getScreenManager().getCamera().setFocus(cameraFocus);
      }
    });

    Game.getScreenManager().getRenderComponent().addFocusListener(new FocusListener() {

      @Override
      public void focusLost(FocusEvent e) {
        startPoint = null;
      }

      @Override
      public void focusGained(FocusEvent e) {

      }
    });

    this.setupControls();

    super.prepare();
    this.onMouseMoved(e -> {

      if (this.getFocus() == null) {
        Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
        currentTransform = TransformType.NONE;
        return;
      }

      boolean hovered = false;
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
        return;
      }
      for (TransformType type : this.transformRects.keySet()) {
        Rectangle2D rect = this.transformRects.get(type);
        Rectangle2D hoverrect = new Rectangle2D.Double(rect.getX() - rect.getWidth() * 2, rect.getY() - rect.getHeight() * 2, rect.getWidth() * 4,
            rect.getHeight() * 4);
        if (hoverrect.contains(Input.mouse().getMapLocation())) {
          hovered = true;
          if (type == TransformType.DOWN || type == TransformType.UP) {
            Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_VERTICAL, 0, 0);
          } else if (type == TransformType.UPLEFT || type == TransformType.DOWNRIGHT) {
            Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_DIAGONAL_LEFT, 0, 0);
          } else if (type == TransformType.UPRIGHT || type == TransformType.DOWNLEFT) {
            Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_DIAGONAL_RIGHT, 0, 0);
          } else {
            Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_TRANS_HORIZONTAL, 0, 0);
          }

          currentTransform = type;
          break;
        }
      }

      if (!hovered) {
        Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
        currentTransform = TransformType.NONE;
      }
    });

    this.onMousePressed(e -> {
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
        if (this.isMoving || this.currentTransform != TransformType.NONE) {
          return;
        }

        final Point2D mouse = Input.mouse().getMapLocation();
        this.startPoint = mouse;
        boolean somethingIsFocused = false;
        boolean currentObjectFocused = false;

        for (IMapObjectLayer layer : Game.getEnvironment().getMap().getMapObjectLayers()) {
          if (layer == null) {
            continue;
          }

          if (somethingIsFocused) {
            break;
          }

          if (!EditorScreen.instance().getMapSelectionPanel().isSelectedMapObjectLayer(layer.getName())) {
            continue;
          }

          for (IMapObject mapObject : layer.getMapObjects()) {
            if (mapObject == null) {
              continue;
            }

            MapObjectType type = MapObjectType.get(mapObject.getType());
            if (type == MapObjectType.LANE) {
              continue;
            }

            if (mapObject.getBoundingBox().contains(mouse)) {
              if (this.getFocusedMapObject() != null && mapObject.getId() == this.getFocusedMapObject().getId()) {
                currentObjectFocused = true;
                continue;
              }

              this.setFocus(mapObject);
              EditorScreen.instance().getMapObjectPanel().bind(mapObject);
              somethingIsFocused = true;
              break;
            }
          }
        }

        if (!somethingIsFocused && !currentObjectFocused) {
          this.setFocus(null);
          EditorScreen.instance().getMapObjectPanel().bind(null);
        }
        break;
      }
    });

    this.onMouseDragged(e -> {
      if (!this.hasFocus()) {
        return;
      }

      switch (this.currentEditMode) {
      case EDITMODE_CREATE:
        if (startPoint == null) {
          return;
        }

        newObject = this.getCurrentMouseSelectionArea();
        break;
      case EDITMODE_EDIT:
        if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL)) {
          if (!this.isMoving) {
            this.isMoving = true;

            UndoManager.instance().mapObjectChanging(this.getFocusedMapObject());
          }

          this.handleEntityDrag();
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
        if (!this.isMoving) {
          this.isMoving = true;
          UndoManager.instance().mapObjectChanging(this.getFocusedMapObject());
        }

        this.handleEntityDrag();
        break;
      }
    });

    this.onMouseReleased(e -> {
      if (!this.hasFocus()) {
        return;
      }

      this.dragPoint = null;
      this.dragLocationMapObject = null;
      this.dragSizeMapObject = null;

      switch (this.currentEditMode) {
      case EDITMODE_CREATE:
        if (this.newObject == null) {
          break;
        }
        IMapObject mo = this.createNewMapObject(EditorScreen.instance().getMapObjectPanel().getObjectType());
        this.newObject = null;
        this.setFocus(mo);
        EditorScreen.instance().getMapObjectPanel().bind(mo);
        this.setEditMode(EDITMODE_EDIT);
        break;
      case EDITMODE_MOVE:

        if (this.isMoving) {
          this.isMoving = false;
          UndoManager.instance().mapObjectChanged(this.getFocusedMapObject());
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

        Rectangle2D rect = this.getCurrentMouseSelectionArea();
        if (rect.getHeight() > 0 && rect.getWidth() > 0) {

          boolean somethingIsFocused = false;
          for (IMapObjectLayer layer : Game.getEnvironment().getMap().getMapObjectLayers()) {
            if (layer == null) {
              continue;
            }

            if (!EditorScreen.instance().getMapSelectionPanel().isSelectedMapObjectLayer(layer.getName())) {
              continue;
            }

            for (IMapObject mapObject : layer.getMapObjects()) {
              if (mapObject == null) {
                continue;
              }
              MapObjectType type = MapObjectType.get(mapObject.getType());
              if (type == MapObjectType.LANE) {
                continue;
              }

              if (!GeometricUtilities.intersects(rect, mapObject.getBoundingBox())) {
                continue;
              }

              this.setFocus(mapObject);
              if (mapObject.equals(this.getFocusedMapObject())) {
                somethingIsFocused = true;
                continue;
              }

              EditorScreen.instance().getMapObjectPanel().bind(mapObject);
              somethingIsFocused = true;
              break;
            }
          }

          if (!somethingIsFocused) {
            this.setFocus(null);
            EditorScreen.instance().getMapObjectPanel().bind(null);
          }
        }
        break;
      }

      this.startPoint = null;
    });
  }

  public void loadEnvironment(Map map) {
    this.isLoading = true;
    try {
      if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null) {
        double x = Game.getScreenManager().getCamera().getFocus().getX();
        double y = Game.getScreenManager().getCamera().getFocus().getY();
        Point2D newPoint = new Point2D.Double(x, y);
        this.cameraFocus.put(Game.getEnvironment().getMap().getFileName(), newPoint);
      }

      Point2D newFocus = null;
      if (this.cameraFocus.containsKey(map.getFileName())) {
        newFocus = this.cameraFocus.get(map.getFileName());
      } else {
        newFocus = new Point2D.Double(map.getSizeInPixels().getWidth() / 2, map.getSizeInPixels().getHeight() / 2);
        this.cameraFocus.put(map.getFileName(), newFocus);
      }

      Game.getScreenManager().getCamera().setFocus(new Point2D.Double(newFocus.getX(), newFocus.getY()));

      this.ensureUniqueIds(map);
      Environment env = new Environment(map);
      env.init();
      Game.loadEnvironment(env);

      this.updateScrollBars();

      EditorScreen.instance().getMapSelectionPanel().setSelection(map.getFileName());
      EditorScreen.instance().getMapObjectPanel().bind(this.getFocusedMapObject());

      for (Consumer<Map> cons : this.mapLoadedConsumer) {
        cons.accept(map);
      }

    } finally {
      this.isLoading = false;
    }
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

  public void add(IMapObject mapObject, IMapObjectLayer layer) {
    layer.addMapObject(mapObject);
    Game.getEnvironment().loadFromMap(mapObject.getId());
    if (MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().createImage();
    }

    this.setFocus(mapObject);
    EditorScreen.instance().getMapObjectPanel().bind(mapObject);
    this.setEditMode(EDITMODE_EDIT);
  }

  public void copy() {
    if (this.currentEditMode != EDITMODE_EDIT) {
      return;
    }

    this.copiedMapObject = this.getFocusedMapObject();
  }

  public void paste() {
    if (this.copiedMapObject != null) {
      int x = (int) Input.mouse().getMapLocation().getX();
      int y = (int) Input.mouse().getMapLocation().getY();

      this.newObject = new Rectangle(x, y,
          (int) this.copiedMapObject.getDimension().getWidth(),
          (int) this.copiedMapObject.getDimension().getHeight());
      this.copyMapObject(this.copiedMapObject);
    }
  }

  public void cut() {
    if (this.currentEditMode != EDITMODE_EDIT) {
      return;
    }

    this.copiedMapObject = this.getFocusedMapObject();
    UndoManager.instance().mapObjectDeleting(this.getFocusedMapObject());
    this.delete(this.getFocusedMapObject());
  }

  public void delete() {
    if (this.getFocusedMapObject() == null) {
      return;
    }

    int n = JOptionPane.showConfirmDialog(
        null,
        "Do you really want to delete the entity [" + this.getFocusedMapObject().getId() + "]",
        "Delete Entity?",
        JOptionPane.YES_NO_OPTION);

    if (n == JOptionPane.OK_OPTION) {
      UndoManager.instance().mapObjectDeleting(this.getFocusedMapObject());
      this.delete(this.getFocusedMapObject());
    }
  }

  public void delete(final IMapObject mapObject) {
    if (mapObject == null) {
      return;
    }

    MapObjectType type = MapObjectType.valueOf(mapObject.getType());
    Game.getEnvironment().getMap().removeMapObject(mapObject.getId());
    Game.getEnvironment().remove(mapObject.getId());
    if (type == MapObjectType.STATICSHADOW ||
        type == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().createImage();
    }

    EditorScreen.instance().getMapObjectPanel().bind(null);
    this.setFocus(null);
  }

  public void setEditMode(int editMode) {
    if (editMode == this.currentEditMode) {
      return;
    }

    switch (editMode) {
    case EDITMODE_CREATE:
      this.setFocus(null);
      EditorScreen.instance().getMapObjectPanel().bind(null);
      break;
    case EDITMODE_EDIT:

      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR, 0, 0);
      break;
    case EDITMODE_MOVE:
      Game.getScreenManager().getRenderComponent().setCursor(Program.CURSOR_MOVE, 0, 0);
      break;
    }

    this.currentEditMode = editMode;
    for (Consumer<Integer> cons : this.editModeChangedConsumer) {
      cons.accept(this.currentEditMode);
    }
  }

  public void setFocus(IMapObject mapObject) {
    if (mapObject != null && mapObject.equals(this.getFocusedMapObject()) || mapObject == null && this.getFocusedMapObject() == null) {
      return;
    }

    if (Game.getEnvironment() == null || Game.getEnvironment().getMap() == null) {
      return;
    }

    if (this.isMoving || this.isTransforming) {
      return;
    }

    if (mapObject == null) {
      this.focusedObjects.remove(Game.getEnvironment().getMap().getFileName());
    } else {
      this.focusedObjects.put(Game.getEnvironment().getMap().getFileName(), mapObject);
    }
    for (Consumer<IMapObject> cons : this.focusChangedConsumer) {
      cons.accept(this.getFocusedMapObject());
    }

    this.updateTransformControls();
  }

  public void setGridSize(int gridSize) {
    Program.USER_PREFERNCES.setGridSize(gridSize);
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
      Rectangle2D transRect = new Rectangle2D.Double(this.getTransX(trans, focus), this.getTransY(trans, focus), this.currentTransformRectSize,
          this.currentTransformRectSize);
      this.transformRects.put(trans, transRect);
    }
  }

  public void deleteMap() {
    if (this.getMaps() == null || this.getMaps().size() == 0) {
      return;
    }

    if (Game.getEnvironment() == null && Game.getEnvironment().getMap() == null) {
      return;
    }

    this.getMaps().removeIf(x -> x.getFileName().equals(Game.getEnvironment().getMap().getFileName()));
    EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps());
    if (this.maps.size() > 0) {
      this.loadEnvironment(this.maps.get(0));
    } else {
      this.loadEnvironment(null);
    }
  }

  public void importMap() {
    if (this.getMaps() == null || this.getMaps().size() == 0) {
      return;
    }

    JFileChooser chooser;
    try {
      String defaultPath = EditorScreen.instance().getProjectPath() != null ? EditorScreen.instance().getProjectPath() : new File(".").getCanonicalPath();
      chooser = new JFileChooser(defaultPath);
      chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
      chooser.setDialogType(JFileChooser.OPEN_DIALOG);
      chooser.setDialogTitle("Import Map");
      FileFilter filter = new FileNameExtensionFilter("tmx - Tilemap XML", Map.FILE_EXTENSION);
      chooser.setFileFilter(filter);
      chooser.addChoosableFileFilter(filter);

      int result = chooser.showOpenDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {

        final IMapLoader tmxLoader = new TmxMapLoader();
        Map map = (Map) tmxLoader.loadMap(chooser.getSelectedFile().toString());
        if (map == null) {
          System.out.println("could not load map from file '" + chooser.getSelectedFile().toString() + "'");
          return;
        }

        Optional<Map> current = this.maps.stream().filter(x -> x.getFileName().equals(map.getFileName())).findFirst();
        if (current.isPresent()) {
          int n = JOptionPane.showConfirmDialog(
              null,
              "Do you really want to replace the existing map '" + map.getFileName() + "' ?",
              "Replace Map",
              JOptionPane.YES_NO_OPTION);

          if (n == JOptionPane.YES_OPTION) {
            this.getMaps().remove(current.get());
            ImageCache.MAPS.clear("^(" + OrthogonalMapRenderer.getCacheKey(map) + ").*(static)$");
          } else {
            return;
          }
        }

        this.getMaps().add(map);
        EditorScreen.instance().getMapSelectionPanel().bind(this.getMaps());

        // remove old spritesheets
        for (ITileset tileSet : map.getTilesets()) {
          Spritesheet sprite = Spritesheet.find(tileSet.getImage().getSource());
          if (sprite != null) {
            Spritesheet.remove(sprite.getName());
            this.screen.getGameFile().getTileSets().removeIf(x -> x.getName().equals(sprite.getName()));
          }
        }

        for (ITileset tileSet : map.getTilesets()) {
          Spritesheet sprite = Spritesheet.load(tileSet);
          this.screen.getGameFile().getTileSets().add(new SpriteSheetInfo(sprite));
        }

        for (IImageLayer imageLayer : map.getImageLayers()) {
          BufferedImage img = RenderEngine.getImage(imageLayer.getImage().getAbsoluteSourcePath(), true);
          Spritesheet sprite = Spritesheet.load(img, imageLayer.getImage().getSource(), img.getWidth(), img.getHeight());
          this.screen.getGameFile().getTileSets().add(new SpriteSheetInfo(sprite));
        }

        this.loadEnvironment(map);
        System.out.println("imported map '" + map.getFileName() + "'");
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  public void exportMap() {
    if (this.getMaps() == null || this.getMaps().size() == 0) {
      return;
    }

    Map map = (Map) Game.getEnvironment().getMap();
    if (map == null) {
      return;
    }

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

      int result = chooser.showSaveDialog(null);
      if (result == JFileChooser.APPROVE_OPTION) {
        String newFile = map.save(chooser.getSelectedFile().toString());

        // save all tilesets manually because a map has a relative reference to
        // the tilesets
        String dir = FileUtilities.getParentDirPath(newFile);
        for (ITileset tileSet : map.getTilesets()) {
          ImageSerializer.saveImage(Paths.get(dir, tileSet.getImage().getSource()).toString(),
              Spritesheet.find(tileSet.getImage().getSource()).getImage());
        }

        System.out.println("exported " + map.getFileName() + " to " + newFile);
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
  }

  public void createNewMap() {

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
    this.scrollSpeed = BASE_SCROLL_SPEED / this.zooms[this.currentZoomIndex];
  }

  private IMapObject copyMapObject(IMapObject obj) {

    IMapObject mo = new MapObject();
    mo.setType(obj.getType());
    mo.setX(this.snapX(this.newObject.getX()));
    mo.setY(this.snapY(this.newObject.getY()));
    mo.setWidth((int) obj.getDimension().getWidth());
    mo.setHeight((int) obj.getDimension().getHeight());
    mo.setId(Game.getEnvironment().getNextMapId());
    mo.setName(obj.getName());
    mo.setCustomProperties(obj.getAllCustomProperties());

    this.add(mo, getCurrentLayer());
    UndoManager.instance().mapObjectAdded(mo);
    return mo;
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
    mo.setX((int) this.newObject.getX());
    mo.setY((int) this.newObject.getY());
    mo.setWidth((int) this.newObject.getWidth());
    mo.setHeight((int) this.newObject.getHeight());
    mo.setId(Game.getEnvironment().getNextMapId());
    mo.setName("");

    switch (type) {
    case PROP:
      mo.setCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH, (this.newObject.getWidth() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT, (this.newObject.getHeight() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperties.COLLISION, "true");
      mo.setCustomProperty(MapObjectProperties.INDESTRUCTIBLE, "false");
      break;
    case DECORMOB:
      mo.setCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH, (this.newObject.getWidth() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT, (this.newObject.getHeight() * 0.4) + "");
      mo.setCustomProperty(MapObjectProperties.COLLISION, "false");
      mo.setCustomProperty(MapObjectProperties.DECORMOB_VELOCITY, "2");
      mo.setCustomProperty(MapObjectProperties.DECORMOB_BEHAVIOUR, MovementBehaviour.IDLE.toString());
      break;
    case LIGHTSOURCE:
      mo.setCustomProperty(MapObjectProperties.LIGHTBRIGHTNESS, "180");
      mo.setCustomProperty(MapObjectProperties.LIGHTCOLOR, "#ffffff");
      mo.setCustomProperty(MapObjectProperties.LIGHTSHAPE, LightSource.ELLIPSE);
      break;
    case SPAWNPOINT:
      mo.setWidth(3);
      mo.setHeight(3);
    default:
      break;
    }

    this.add(mo, getCurrentLayer());
    UndoManager.instance().mapObjectAdded(mo);
    return mo;
  }

  private Rectangle2D getCurrentMouseSelectionArea() {
    final Point2D startPoint = this.startPoint;
    if (startPoint == null) {
      return null;
    }

    final Point2D endPoint = Input.mouse().getMapLocation();
    double minX = this.snapX(Math.min(startPoint.getX(), endPoint.getX()));
    double maxX = this.snapX(Math.max(startPoint.getX(), endPoint.getX()));
    double minY = this.snapY(Math.min(startPoint.getY(), endPoint.getY()));
    double maxY = this.snapY(Math.max(startPoint.getY(), endPoint.getY()));

    double width = Math.abs(minX - maxX);
    double height = Math.abs(minY - maxY);

    return new Rectangle2D.Double(minX, minY, width, height);
  }

  private Rectangle2D getFocus() {
    if (this.getFocusedMapObject() != null) {
      return this.getFocusedMapObject().getBoundingBox();
    }

    return null;
  }

  private IMapObject getFocusedMapObject() {
    if (Game.getEnvironment() != null && Game.getEnvironment().getMap() != null) {
      return this.focusedObjects.get(Game.getEnvironment().getMap().getFileName());
    }

    return null;
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
    if (this.getFocusedMapObject() == null || this.currentEditMode != EDITMODE_EDIT || currentTransform == TransformType.NONE) {
      return;
    }

    if (this.dragPoint == null) {
      this.dragPoint = Input.mouse().getMapLocation();
      this.dragLocationMapObject = new Point2D.Double(this.getFocusedMapObject().getX(), this.getFocusedMapObject().getY());
      this.dragSizeMapObject = new Dimension(this.getFocusedMapObject().getDimension());
      return;
    }

    double deltaX = Input.mouse().getMapLocation().getX() - this.dragPoint.getX();
    double deltaY = Input.mouse().getMapLocation().getY() - this.dragPoint.getY();
    double newWidth = this.dragSizeMapObject.getWidth();
    double newHeight = this.dragSizeMapObject.getHeight();
    double newX = this.snapX(this.dragLocationMapObject.getX());
    double newY = this.snapY(this.dragLocationMapObject.getY());

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
      newX = MathUtilities.clamp(newX, 0, this.dragLocationMapObject.getX() + this.dragSizeMapObject.getWidth());
      break;
    case LEFT:
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, this.dragLocationMapObject.getX() + this.dragSizeMapObject.getWidth());
      break;
    case RIGHT:
      newWidth += deltaX;
      break;
    case UP:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, this.dragLocationMapObject.getY() + this.dragSizeMapObject.getHeight());
      break;
    case UPLEFT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, this.dragLocationMapObject.getY() + this.dragSizeMapObject.getHeight());
      newWidth -= deltaX;
      newX += deltaX;
      newX = MathUtilities.clamp(newX, 0, this.dragLocationMapObject.getX() + this.dragSizeMapObject.getWidth());
      break;
    case UPRIGHT:
      newHeight -= deltaY;
      newY += deltaY;
      newY = MathUtilities.clamp(newY, 0, this.dragLocationMapObject.getY() + this.dragSizeMapObject.getHeight());
      newWidth += deltaX;
      break;
    default:
      return;
    }

    this.getFocusedMapObject().setWidth(this.snapX(newWidth));
    this.getFocusedMapObject().setHeight(this.snapY(newHeight));
    this.getFocusedMapObject().setX(this.snapX(newX));
    this.getFocusedMapObject().setY(this.snapY(newY));

    Game.getEnvironment().reloadFromMap(this.getFocusedMapObject().getId());
    if (MapObjectType.get(this.getFocusedMapObject().getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().createImage();
    }

    EditorScreen.instance().getMapObjectPanel().bind(this.getFocusedMapObject());
    this.updateTransformControls();
  }

  private void handleEntityDrag() {
    // only handle drag i
    if (this.getFocusedMapObject() == null || (!Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && this.currentEditMode != EDITMODE_MOVE)) {
      return;
    }

    if (this.dragPoint == null) {
      this.dragPoint = Input.mouse().getMapLocation();
      this.dragLocationMapObject = new Point2D.Double(this.getFocusedMapObject().getX(), this.getFocusedMapObject().getY());
      return;
    }

    double deltaX = Input.mouse().getMapLocation().getX() - this.dragPoint.getX();
    double deltaY = Input.mouse().getMapLocation().getY() - this.dragPoint.getY();
    double newX = this.snapX(this.dragLocationMapObject.getX() + deltaX);
    double newY = this.snapY(this.dragLocationMapObject.getY() + deltaY);
    this.getFocusedMapObject().setX((int) newX);
    this.getFocusedMapObject().setY((int) newY);

    Game.getEnvironment().reloadFromMap(this.getFocusedMapObject().getId());
    if (MapObjectType.get(this.getFocusedMapObject().getType()) == MapObjectType.STATICSHADOW
        || MapObjectType.get(this.getFocusedMapObject().getType()) == MapObjectType.LIGHTSOURCE) {
      Game.getEnvironment().getAmbientLight().createImage();
    }

    EditorScreen.instance().getMapObjectPanel().bind(this.getFocusedMapObject());
    this.updateTransformControls();
  }

  private void setCurrentZoom() {
    Game.getScreenManager().getCamera().setZoom(zooms[this.currentZoomIndex], 0);
  }

  private void setupControls() {
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

    Input.keyboard().onKeyPressed(KeyEvent.VK_SPACE, e -> {
      if (this.getFocusedMapObject() != null) {
        Game.getScreenManager().getCamera().setFocus(new Point2D.Double(this.getFocus().getCenterX(), this.getFocus().getCenterY()));
      }
    });

    Input.keyboard().onKeyPressed(KeyEvent.VK_CONTROL, e -> {
      if (this.currentEditMode == EDITMODE_EDIT) {
        this.setEditMode(EDITMODE_MOVE);
      }
    });
    Input.keyboard().onKeyReleased(KeyEvent.VK_CONTROL, e -> {
      this.setEditMode(EDITMODE_EDIT);
    });

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

      if (Game.getScreenManager().getRenderComponent().hasFocus()) {
        if (this.currentEditMode == EDITMODE_EDIT) {
          this.delete();
        }
      }
    });

    Input.mouse().onWheelMoved(e -> {
      if (!this.hasFocus()) {
        return;
      }

      // horizontal scrolling
      if (Input.keyboard().isPressed(KeyEvent.VK_CONTROL) && this.dragPoint == null) {
        if (e.getWheelRotation() < 0) {
          Point2D cameraFocus = Game.getScreenManager().getCamera().getFocus();
          Point2D newFocus = new Point2D.Double(cameraFocus.getX() - this.scrollSpeed, cameraFocus.getY());
          Game.getScreenManager().getCamera().setFocus(newFocus);
        } else {
          Point2D cameraFocus = Game.getScreenManager().getCamera().getFocus();
          Point2D newFocus = new Point2D.Double(cameraFocus.getX() + this.scrollSpeed, cameraFocus.getY());
          Game.getScreenManager().getCamera().setFocus(newFocus);
        }

        Program.horizontalScroll.setValue((int) Game.getScreenManager().getCamera().getViewPort().getCenterX());
        return;
      }

      if (Input.keyboard().isPressed(KeyEvent.VK_ALT)) {
        if (e.getWheelRotation() < 0) {
          this.zoomIn();
        } else {
          this.zoomOut();
        }

        return;
      }

      if (e.getWheelRotation() < 0) {
        Point2D cameraFocus = Game.getScreenManager().getCamera().getFocus();
        Point2D newFocus = new Point2D.Double(cameraFocus.getX(), cameraFocus.getY() - this.scrollSpeed);
        Game.getScreenManager().getCamera().setFocus(newFocus);

      } else {
        Point2D cameraFocus = Game.getScreenManager().getCamera().getFocus();
        Point2D newFocus = new Point2D.Double(cameraFocus.getX(), cameraFocus.getY() + this.scrollSpeed);
        Game.getScreenManager().getCamera().setFocus(newFocus);
      }

      Program.verticalScroll.setValue((int) Game.getScreenManager().getCamera().getViewPort().getCenterY());
    });
  }

  private int snapX(double x) {
    if (!this.snapToGrid) {
      return MathUtilities.clamp((int) Math.round(x), 0, (int) Game.getEnvironment().getMap().getSizeInPixels().getWidth());
    }

    double snapped = ((int) (x / this.gridSize) * this.gridSize);
    return (int) Math.round(Math.min(Math.max(snapped, 0), Game.getEnvironment().getMap().getSizeInPixels().getWidth()));
  }

  private int snapY(double y) {
    if (!this.snapToGrid) {
      return MathUtilities.clamp((int) Math.round(y), 0, (int) Game.getEnvironment().getMap().getSizeInPixels().getHeight());
    }

    double snapped = (int) (y / this.gridSize) * this.gridSize;
    return (int) Math.round(Math.min(Math.max(snapped, 0), Game.getEnvironment().getMap().getSizeInPixels().getHeight()));
  }

  private void updateScrollBars() {
    Program.horizontalScroll.setMinimum(0);
    Program.horizontalScroll.setMaximum(Game.getEnvironment().getMap().getSizeInPixels().width);
    Program.verticalScroll.setMinimum(0);
    Program.verticalScroll.setMaximum(Game.getEnvironment().getMap().getSizeInPixels().height);
    Program.horizontalScroll.setValue((int) Game.getScreenManager().getCamera().getViewPort().getCenterX());
    Program.verticalScroll.setValue((int) Game.getScreenManager().getCamera().getViewPort().getCenterY());
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
