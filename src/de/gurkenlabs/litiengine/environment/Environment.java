package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.gurkenlabs.configuration.Quality;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxMapLoader;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.util.ImageProcessing;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.io.FileUtilities;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {
  private static final Logger log = Logger.getLogger(Environment.class.getName());
  private static final Map<String, IMapObjectLoader> mapObjectLoaders;

  private final Collection<CollisionBox> colliders;
  private final Map<Integer, ICombatEntity> combatEntities;

  private final Map<RenderType, Map<Integer, IEntity>> entities;
  private final Map<String, List<IEntity>> entitiesByTag;

  private final List<Consumer<Graphics2D>> entitiesRenderedConsumers;
  private final List<Consumer<IEnvironment>> initializedConsumers;
  private final List<Consumer<IEnvironment>> loadedConsumers;
  private final List<Consumer<IEntity>> entityAddedConsumers;
  private final List<Consumer<IEntity>> entityRemovedConsumers;
  private final List<Consumer<Graphics2D>> overlayRenderedConsumer;
  private final List<Consumer<Graphics2D>> mapRenderedConsumer;

  private final List<IRenderable> groundRenderable;
  private final Collection<LightSource> lightSources;
  private final Collection<StaticShadow> staticShadows;
  private final Collection<Trigger> triggers;

  private final List<MapArea> mapAreas;

  private final Map<Integer, IMovableEntity> movableEntities;
  private final List<IRenderable> overlayRenderable;

  private final List<Spawnpoint> spawnPoints;

  private AmbientLight ambientLight;
  private boolean loaded;
  private boolean initialized;
  private IMap map;
  private Image staticShadowImage;

  private int localIdSequence = 0;
  private int mapIdSequence;

  static {
    mapObjectLoaders = new ConcurrentHashMap<>();
    registerDefaultMapObjectLoaders();
  }

  public Environment(final IMap map) {
    this();
    this.map = map;
    this.mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  /**
   * Instantiates a new map container base.
   *
   * @param mapPath
   *          the mapPath
   */
  public Environment(final String mapPath) {
    this();
    final IMap loadedMap = Game.getMap(FileUtilities.getFileName(mapPath));
    if (loadedMap == null) {
      final IMapLoader tmxLoader = new TmxMapLoader();
      this.map = tmxLoader.loadMap(mapPath);
    } else {
      this.map = loadedMap;
    }

    this.mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  private Environment() {
    this.entitiesByTag = new ConcurrentHashMap<>();
    this.entities = new ConcurrentHashMap<>();
    this.entities.put(RenderType.GROUND, new ConcurrentHashMap<>());
    this.entities.put(RenderType.NORMAL, new ConcurrentHashMap<>());
    this.entities.put(RenderType.OVERLAY, new ConcurrentHashMap<>());

    this.combatEntities = new ConcurrentHashMap<>();
    this.movableEntities = new ConcurrentHashMap<>();

    this.lightSources = new CopyOnWriteArrayList<>();
    this.colliders = new CopyOnWriteArrayList<>();
    this.triggers = new CopyOnWriteArrayList<>();
    this.mapAreas = new CopyOnWriteArrayList<>();
    this.staticShadows = new CopyOnWriteArrayList<>();

    this.mapRenderedConsumer = new CopyOnWriteArrayList<>();
    this.entitiesRenderedConsumers = new CopyOnWriteArrayList<>();
    this.overlayRenderedConsumer = new CopyOnWriteArrayList<>();
    this.initializedConsumers = new CopyOnWriteArrayList<>();
    this.loadedConsumers = new CopyOnWriteArrayList<>();
    this.entityAddedConsumers = new CopyOnWriteArrayList<>();
    this.entityRemovedConsumers = new CopyOnWriteArrayList<>();

    this.spawnPoints = new CopyOnWriteArrayList<>();

    this.groundRenderable = new CopyOnWriteArrayList<>();
    this.overlayRenderable = new CopyOnWriteArrayList<>();
  }

  @Override
  public void add(final IEntity entity) {
    if (entity == null) {
      return;
    }

    // set local map id if none is set for the entity
    if (entity.getMapId() == 0) {
      entity.setMapId(this.getLocalMapId());
    }

    if (entity instanceof Emitter) {
      Emitter emitter = (Emitter) entity;
      this.getGroundRenderables().add(emitter.getGroundRenderable());
      this.getOverlayRenderables().add(emitter.getOverlayRenderable());
    }

    if (entity instanceof ICombatEntity) {
      this.combatEntities.put(entity.getMapId(), (ICombatEntity) entity);
    }

    if (entity instanceof IMovableEntity) {
      this.movableEntities.put(entity.getMapId(), (IMovableEntity) entity);
    }

    if (entity instanceof CollisionBox) {
      this.colliders.add((CollisionBox) entity);
    }

    if (entity instanceof LightSource) {
      this.lightSources.add((LightSource) entity);
    }

    if (entity instanceof Trigger) {
      this.triggers.add((Trigger) entity);
    }

    if (entity instanceof Spawnpoint) {
      this.spawnPoints.add((Spawnpoint) entity);
    }

    for (String tag : entity.getTags()) {
      if (this.entitiesByTag.containsKey(tag)) {
        this.entitiesByTag.get(tag).add(entity);
        continue;
      }

      this.entitiesByTag.put(tag, new CopyOnWriteArrayList<>());
      this.entitiesByTag.get(tag).add(entity);
    }

    // if the environment has already been loaded,
    // we need to load the new entity manually
    if (this.loaded) {
      this.load(entity);
    }

    this.entities.get(entity.getRenderType()).put(entity.getMapId(), entity);

    for (Consumer<IEntity> cons : this.entityAddedConsumers) {
      cons.accept(entity);
    }
  }

  @Override
  public void add(final IRenderable renderable, final RenderType type) {
    switch (type) {
    case GROUND:
      this.getGroundRenderables().add(renderable);
      break;
    case OVERLAY:
      this.getOverlayRenderables().add(renderable);
      break;

    default:
      break;
    }
  }

  @Override
  public void clear() {
    Game.getPhysicsEngine().clear();
    this.dispose(this.getEntities());
    this.dispose(this.getTriggers());
    this.getCombatEntities().clear();
    this.getMovableEntities().clear();
    this.getLightSources().clear();
    this.getCollisionBoxes().clear();
    this.getSpawnPoints().clear();
    this.getAreas().clear();
    this.getTriggers().clear();

    this.entities.get(RenderType.GROUND).clear();
    this.entities.get(RenderType.NORMAL).clear();
    this.entities.get(RenderType.OVERLAY).clear();
    this.initialized = false;
  }

  @Override
  public List<ICombatEntity> findCombatEntities(final Shape shape) {
    return this.findCombatEntities(shape, entity -> true);
  }

  @Override
  public List<ICombatEntity> findCombatEntities(final Shape shape, final Predicate<ICombatEntity> condition) {
    final ArrayList<ICombatEntity> foundCombatEntities = new ArrayList<>();
    if (shape == null) {
      return foundCombatEntities;
    }

    // for rectangle we can just use the intersects method
    if (shape instanceof Rectangle2D) {
      final Rectangle2D rect = (Rectangle2D) shape;
      for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
        if (combatEntity.getHitBox().intersects(rect)) {
          foundCombatEntities.add(combatEntity);
        }
      }

      return foundCombatEntities;
    }

    // for other shapes, we check if the shape's bounds intersect the hitbox and
    // if so, we then check if the actual shape intersects the hitbox
    for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
      if (combatEntity.getHitBox().intersects(shape.getBounds()) && GeometricUtilities.shapeIntersects(combatEntity.getHitBox(), shape)) {
        foundCombatEntities.add(combatEntity);
      }
    }

    return foundCombatEntities;
  }

  @Override
  public List<IEntity> findEntities(final Shape shape) {
    final ArrayList<IEntity> foundEntities = new ArrayList<>();
    if (shape == null) {
      return foundEntities;
    }
    if (shape instanceof Rectangle2D) {
      final Rectangle2D rect = (Rectangle2D) shape;
      for (final IEntity entity : this.getEntities()) {
        if (entity.getBoundingBox().intersects(rect)) {
          foundEntities.add(entity);
        }
      }
      return foundEntities;
    }
    // for other shapes, we check if the shape's bounds intersect the hitbox
    // and
    // if so, we then check if the actual shape intersects the hitbox
    for (final IEntity entity : this.getEntities()) {
      if (entity.getBoundingBox().intersects(shape.getBounds()) && GeometricUtilities.shapeIntersects(entity.getBoundingBox(), shape)) {
        foundEntities.add(entity);
      }
    }

    return foundEntities;
  }

  @Override
  public IEntity get(final int mapId) {
    IEntity entity = this.entities.get(RenderType.GROUND).get(mapId);
    if (entity != null) {
      return entity;
    }

    entity = this.entities.get(RenderType.NORMAL).get(mapId);
    if (entity != null) {
      return entity;
    }

    entity = this.entities.get(RenderType.OVERLAY).get(mapId);
    if (entity != null) {
      return entity;
    }

    return null;
  }

  @Override
  public IEntity get(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final IEntity entity : this.entities.get(RenderType.GROUND).values()) {
      if (entity.getName() != null && entity.getName().equals(name)) {
        return entity;
      }
    }

    for (final IEntity entity : this.entities.get(RenderType.NORMAL).values()) {
      if (entity.getName() != null && entity.getName().equals(name)) {
        return entity;
      }
    }

    for (final IEntity entity : this.entities.get(RenderType.OVERLAY).values()) {
      if (entity.getName() != null && entity.getName().equals(name)) {
        return entity;
      }
    }

    return null;
  }

  @Override
  public List<IEntity> getByTag(String tag) {
    if (this.entitiesByTag.containsKey(tag)) {
      return this.entitiesByTag.get(tag);
    }

    return new ArrayList<>();
  }

  @Override
  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  @Override
  public MapArea getArea(final int mapId) {
    for (final MapArea m : this.getAreas()) {
      if (m.getMapId() == mapId) {
        return m;
      }
    }

    return null;
  }

  @Override
  public MapArea getArea(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final MapArea m : this.getAreas()) {
      if (m.getName() != null && m.getName().equals(name)) {
        return m;
      }
    }
    return null;
  }

  @Override
  public List<MapArea> getAreas() {
    return this.mapAreas;
  }

  @Override
  public Collection<CollisionBox> getCollisionBoxes() {
    return this.colliders;
  }

  @Override
  public CollisionBox getCollisionBox(int mapId) {
    for (final CollisionBox collider : this.getCollisionBoxes()) {
      if (collider.getMapId() == mapId) {
        return collider;
      }
    }

    return null;
  }

  @Override
  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  @Override
  public ICombatEntity getCombatEntity(final int mapId) {
    if (this.combatEntities.containsKey(mapId)) {
      return this.combatEntities.get(mapId);
    }

    return null;
  }

  @Override
  public Collection<IEntity> getEntities() {
    final ArrayList<IEntity> ent = new ArrayList<>();
    ent.addAll(this.entities.get(RenderType.GROUND).values());
    ent.addAll(this.entities.get(RenderType.NORMAL).values());
    ent.addAll(this.entities.get(RenderType.OVERLAY).values());
    return ent;
  }

  @Override
  public Collection<IEntity> getEntities(final RenderType renderType) {
    return this.entities.get(renderType).values();
  }

  @Override
  public <T extends IEntity> Collection<T> getEntitiesByType(Class<T> cls) {
    List<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.getEntities()) {
      if (cls.isInstance(ent)) {
        foundEntities.add((T) ent);
      }
    }

    return foundEntities;
  }

  public Collection<IRenderable> getGroundRenderables() {
    return this.groundRenderable;
  }

  @Override
  public LightSource getLightSource(final int mapId) {
    for (final LightSource light : this.getLightSources()) {
      if (light.getMapId() == mapId) {
        return light;
      }
    }

    return null;
  }

  @Override
  public Collection<LightSource> getLightSources() {
    return this.lightSources;
  }

  /**
   * Negative map ids are only used locally.
   */
  @Override
  public synchronized int getLocalMapId() {
    return --localIdSequence;
  }

  @Override
  public IMap getMap() {
    return this.map;
  }

  @Override
  public Collection<IMovableEntity> getMovableEntities() {
    return this.movableEntities.values();
  }

  @Override
  public IMovableEntity getMovableEntity(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      return this.movableEntities.get(mapId);
    }

    return null;
  }

  @Override
  public synchronized int getNextMapId() {
    return ++mapIdSequence;
  }

  public List<IRenderable> getOverlayRenderables() {
    return this.overlayRenderable;
  }

  @Override
  public Spawnpoint getSpawnpoint(final int mapId) {
    for (final Spawnpoint m : this.getSpawnPoints()) {
      if (m.getMapId() == mapId) {
        return m;
      }
    }

    return null;
  }

  @Override
  public Spawnpoint getSpawnpoint(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final Spawnpoint m : this.getSpawnPoints()) {
      if (m.getName() != null && m.getName().equals(name)) {
        return m;
      }
    }
    return null;
  }

  @Override
  public List<Spawnpoint> getSpawnPoints() {
    return this.spawnPoints;
  }

  public Image getStaticShadowImage() {
    return this.staticShadowImage;
  }

  @Override
  public Collection<StaticShadow> getStaticShadows() {
    return this.staticShadows;
  }

  public StaticShadow getStaticShadow(int mapID) {
    StaticShadow shadow = null;
    for (StaticShadow sh : this.getStaticShadows()) {
      shadow = (sh.getMapId() == mapID) ? sh : null;
    }
    return shadow;
  }

  @Override
  public Trigger getTrigger(final int mapId) {
    for (final Trigger t : this.getTriggers()) {
      if (t.getMapId() == mapId) {
        return t;
      }
    }

    return null;
  }

  @Override
  public Trigger getTrigger(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final Trigger t : this.getTriggers()) {
      if (t.getName() != null && t.getName().equals(name)) {
        return t;
      }
    }
    return null;
  }

  @Override
  public Collection<Trigger> getTriggers() {
    return this.triggers;
  }

  @Override
  public Collection<Trigger> getTriggers(final String name) {
    final List<Trigger> foundTriggers = new ArrayList<>();
    if (name == null || name.isEmpty()) {
      return foundTriggers;
    }

    for (final Trigger t : this.getTriggers()) {
      if (t.getName() != null && t.getName().equals(name)) {
        foundTriggers.add(t);
      }
    }

    return foundTriggers;
  }

  @Override
  public final void init() {
    if (this.initialized) {
      return;
    }

    this.loadMapObjects();
    this.addStaticShadows();
    this.addAmbientLight();

    for (final Consumer<IEnvironment> cons : this.initializedConsumers) {
      cons.accept(this);
    }

    this.initialized = true;
  }

  @Override
  public boolean isLoaded() {
    return this.loaded;
  }

  @Override
  public void load() {
    this.init();
    if (this.loaded) {
      return;
    }

    Game.getPhysicsEngine().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
    for (final IEntity entity : this.getEntities()) {
      this.load(entity);
    }

    this.loaded = true;
    for (final Consumer<IEnvironment> cons : this.loadedConsumers) {
      cons.accept(this);
    }
  }

  @Override
  public void loadFromMap(final int mapId) {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      Optional<IMapObject> opt = layer.getMapObjects().stream().filter(mapObject -> mapObject.getType() != null && !mapObject.getType().isEmpty() && mapObject.getId() == mapId).findFirst();
      if (opt.isPresent()) {
        IMapObject mapObject = opt.get();
        this.addMapObject(mapObject);
        if (MapObjectType.get(mapObject.getType()) == MapObjectType.STATICSHADOW || MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
          this.addStaticShadows();
        }

        break;
      }
    }

  }

  @Override
  public void onEntityRemoved(Consumer<IEntity> consumer) {
    this.entityRemovedConsumers.add(consumer);
  }

  @Override
  public void onEntityAdded(Consumer<IEntity> consumer) {
    this.entityAddedConsumers.add(consumer);
  }

  @Override
  public void onEntitiesRendered(final Consumer<Graphics2D> consumer) {
    this.entitiesRenderedConsumers.add(consumer);
  }

  @Override
  public void onInitialized(final Consumer<IEnvironment> consumer) {
    this.initializedConsumers.add(consumer);
  }

  @Override
  public void onLoaded(final Consumer<IEnvironment> consumer) {
    this.loadedConsumers.add(consumer);
  }

  @Override
  public void onMapRendered(final Consumer<Graphics2D> consumer) {
    this.mapRenderedConsumer.add(consumer);
  }

  @Override
  public void onOverlayRendered(final Consumer<Graphics2D> consumer) {
    this.overlayRenderedConsumer.add(consumer);
  }

  public static void registerMapObjectLoader(String mapObjectType, IMapObjectLoader mapObjectLoader) {
    mapObjectLoaders.put(mapObjectType, mapObjectLoader);
  }

  public static void registerMapObjectLoader(MapObjectType mapObjectType, IMapObjectLoader mapObjectLoader) {
    registerMapObjectLoader(mapObjectType.name(), mapObjectLoader);
  }

  @Override
  public void reloadFromMap(final int mapId) {
    this.remove(mapId);
    this.loadFromMap(mapId);
  }

  @Override
  public void remove(final IEntity entity) {
    if (entity == null) {
      return;
    }

    if (this.entities.get(entity.getRenderType()) != null) {
      this.entities.get(entity.getRenderType()).entrySet().removeIf(e -> e.getValue().getMapId() == entity.getMapId());
    }

    for (String tag : entity.getTags()) {
      if (this.entitiesByTag.containsKey(tag)) {
        this.entitiesByTag.get(tag).remove(entity);
      }
    }

    if (entity instanceof Emitter) {
      Emitter emitter = (Emitter) entity;
      this.groundRenderable.remove(emitter.getGroundRenderable());
      this.overlayRenderable.remove(emitter.getOverlayRenderable());
    }

    if (entity instanceof CollisionBox) {
      this.colliders.remove(entity);
    }

    if (entity instanceof LightSource) {
      this.lightSources.remove(entity);
      this.addStaticShadows();
    }

    if (entity instanceof Trigger) {
      this.triggers.remove(entity);
    }

    if (entity instanceof IMovableEntity) {
      this.movableEntities.values().remove(entity);
    }

    if (entity instanceof ICombatEntity) {
      this.combatEntities.values().remove(entity);
    }

    this.unload(entity);

    for (Consumer<IEntity> cons : this.entityRemovedConsumers) {
      cons.accept(entity);
    }
  }

  @Override
  public void remove(final int mapId) {
    this.getSpawnPoints().removeIf(x -> x.getMapId() == mapId);
    if (this.getStaticShadow(mapId) != null) {
      this.getStaticShadows().remove(this.getStaticShadow(mapId));
      this.addStaticShadows();
      return;
    }
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  @Override
  public <T extends IEntity> void remove(Collection<T> entities) {
    for (T ent : entities) {
      this.remove(ent);
    }
  }

  @Override
  public void removeRenderable(final IRenderable renderable) {
    if (this.getGroundRenderables().contains(renderable)) {
      this.getGroundRenderables().remove(renderable);
    }

    if (this.getOverlayRenderables().contains(renderable)) {
      this.getOverlayRenderables().remove(renderable);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());

    Game.getRenderEngine().renderMap(g, this.getMap());
    this.informConsumers(g, this.mapRenderedConsumer);

    for (final IRenderable rend : this.getGroundRenderables()) {
      rend.render(g);
    }

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.GROUND).values(), false);
    if (Game.getConfiguration().graphics().getGraphicQuality() == Quality.VERYHIGH) {
      Game.getRenderEngine().renderEntities(g, this.getLightSources(), false);
    }

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.NORMAL).values());
    this.informConsumers(g, this.entitiesRenderedConsumers);

    Game.getRenderEngine().renderLayers(g, this.getMap(), RenderType.OVERLAY);

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.OVERLAY).values(), false);

    if (this.getStaticShadows().stream().anyMatch(x -> x.getShadowType() != StaticShadowType.NONE)) {
      // render static shadows
      RenderEngine.renderImage(g, this.getStaticShadowImage(), Game.getCamera().getViewPortLocation(0, 0));
    }

    if (Game.getConfiguration().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getAlpha() != 0) {
      RenderEngine.renderImage(g, this.getAmbientLight().getImage(), Game.getCamera().getViewPortLocation(0, 0));
    }

    for (final IRenderable rend : this.getOverlayRenderables()) {
      rend.render(g);
    }

    this.informConsumers(g, this.overlayRenderedConsumer);
    g.scale(1.0 / Game.getCamera().getRenderScale(), 1.0 / Game.getCamera().getRenderScale());
  }

  @Override
  public void unload() {
    if (!this.loaded) {
      return;
    }

    // unregister all updatable entities from the current environment
    for (final IEntity entity : this.getEntities()) {
      this.unload(entity);
    }

    this.loaded = false;
  }

  protected void addMapObject(final IMapObject mapObject) {
    if (mapObjectLoaders.containsKey(mapObject.getType())) {
      IEntity entity = mapObjectLoaders.get(mapObject.getType()).load(mapObject);
      if (entity != null) {
        this.add(entity);
        return;
      }
    }

    this.addStaticShadow(mapObject);
    this.addMapArea(mapObject);
  }

  protected void addStaticShadow(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.STATICSHADOW) {
      return;
    }
    double x = mapObject.getX();
    double y = mapObject.getY();
    double width = mapObject.getDimension().getWidth();
    double height = mapObject.getDimension().getHeight();
    final StaticShadow shadow = new StaticShadow(mapObject.getId(), mapObject.getName(), x, y, width, height, StaticShadowType.get(mapObject.getCustomProperty(MapObjectProperty.SHADOWTYPE)));
    this.getStaticShadows().add(shadow);
  }

  protected void addMapArea(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.AREA) {
      return;
    }

    final MapArea area = new MapArea(mapObject.getId(), mapObject.getName(), mapObject.getX(), mapObject.getY(), mapObject.getDimension().getWidth(), mapObject.getDimension().getHeight());
    this.getAreas().add(area);
  }

  private void addAmbientLight() {
    final String alphaProp = this.getMap().getCustomProperty(MapProperty.AMBIENTALPHA);
    final String colorProp = this.getMap().getCustomProperty(MapProperty.AMBIENTCOLOR);
    int ambientAlpha = 0;
    Color ambientColor = Color.WHITE;
    try {
      if (alphaProp != null && !alphaProp.isEmpty()) {
        ambientAlpha = (int) Double.parseDouble(alphaProp);
      }

      if (colorProp != null && !colorProp.isEmpty()) {
        ambientColor = Color.decode(colorProp);
      }
    } catch (final NumberFormatException e) {
      log.log(Level.WARNING, e.getMessage(), e);
    }

    this.ambientLight = new AmbientLight(this, ambientColor, ambientAlpha);
  }

  private void addStaticShadows() {
    final int shadowOffset = 10;
    final List<Path2D> newStaticShadows = new ArrayList<>();
    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    for (final StaticShadow col : this.getStaticShadows()) {
      final double shadowX = col.getX();
      final double shadowY = col.getY();
      final double shadowWidth = col.getWidth();
      final double shadowHeight = col.getHeight();

      final StaticShadowType shadowType = col.getShadowType();

      final Path2D parallelogram = new Path2D.Double();
      if (shadowType.equals(StaticShadowType.DOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.DOWNLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.DOWNRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2.0, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.NOOFFSET)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      }

      if (parallelogram.getWindingRule() != 0) {
        newStaticShadows.add(parallelogram);
      }
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getMap().getSizeInPixels().getWidth(), (int) this.getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();
    g.setColor(new Color(0, 0, 0, 75));

    final Area ar = new Area();
    for (final Path2D staticShadow : newStaticShadows) {
      final Area staticShadowArea = new Area(staticShadow);
      for (final LightSource light : this.getLightSources()) {
        if (light.getDimensionCenter().getY() > staticShadow.getBounds2D().getMaxY() || staticShadow.getBounds2D().contains(light.getDimensionCenter())) {
          staticShadowArea.subtract(new Area(light.getLightShape()));
        }
      }

      ar.add(staticShadowArea);
    }

    g.fill(ar);
    g.dispose();

    this.staticShadowImage = img;
  }

  private void dispose(final Collection<? extends IEntity> entities) {
    for (final IEntity entity : entities) {
      if (entity instanceof IUpdateable) {
        Game.getLoop().detach((IUpdateable) entity);
      }

      Game.getEntityControllerManager().disposeControllers(entity);
    }
  }

  private void informConsumers(final Graphics2D g, final List<Consumer<Graphics2D>> consumers) {
    for (final Consumer<Graphics2D> consumer : consumers) {
      consumer.accept(g);
    }
  }

  /**
   * Loads the specified entiy by performing the following steps:
   * <ol>
   * <li>add to physics engine</li>
   * <li>register entity for update</li>
   * <li>register animation controller for update</li>
   * <li>register movement controller for update</li>
   * <li>register AI controller for update</li>
   * </ol>
   *
   * @param entity
   */
  private void load(final IEntity entity) {
    // 1. add to physics engine
    this.loadPhysicsEntity(entity);

    // 2. register for update or activate
    this.loadUpdatableOrEmitterEntity(entity);

    // 3. register animation controller for update
    final IAnimationController animation = Game.getEntityControllerManager().getAnimationController(entity);
    if (animation != null) {
      Game.getLoop().attach(animation);
    }

    // 4. register movement controller for update
    if (entity instanceof IMovableEntity) {
      final IMovementController<? extends IMovableEntity> movementController = Game.getEntityControllerManager().getMovementController((IMovableEntity) entity);
      if (movementController != null) {
        Game.getLoop().attach(movementController);
      }
    }

    // 5. register ai controller for update
    final IEntityController<? extends IEntity> controller = Game.getEntityControllerManager().getAIController(entity);
    if (controller != null) {
      Game.getLoop().attach(controller);
    }
  }

  private void loadPhysicsEntity(IEntity entity) {
    if (entity instanceof CollisionBox) {
      final CollisionBox coll = (CollisionBox) entity;
      if (coll.isObstacle()) {
        Game.getPhysicsEngine().add(coll.getBoundingBox());
      } else {
        Game.getPhysicsEngine().add(coll);
      }
    } else if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      if (coll.hasCollision()) {
        Game.getPhysicsEngine().add(coll);
      }
    }
  }

  private void loadUpdatableOrEmitterEntity(IEntity entity) {
    if (entity instanceof Emitter) {
      final Emitter emitter = (Emitter) entity;
      if (emitter.isActivateOnInit()) {
        emitter.activate();
      }
    } else if (entity instanceof IUpdateable) {
      Game.getLoop().attach((IUpdateable) entity);
    }
  }

  private void loadMapObjects() {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject.getType() == null || mapObject.getType().isEmpty()) {
          continue;
        }

        this.addMapObject(mapObject);
      }
    }
  }

  private static void registerDefaultMapObjectLoaders() {
    registerMapObjectLoader(MapObjectType.PROP, new PropMapObjectLoader());
    registerMapObjectLoader(MapObjectType.COLLISIONBOX, new CollisionBoxMapObjectLoader());
    registerMapObjectLoader(MapObjectType.TRIGGER, new TriggerMapObjectLoader());
    registerMapObjectLoader(MapObjectType.DECORMOB, new DecorMobMapObjectLoader());
    registerMapObjectLoader(MapObjectType.EMITTER, new EmitterMapObjectLoader());
    registerMapObjectLoader(MapObjectType.LIGHTSOURCE, new LightSourceMapObjectLoader());
    registerMapObjectLoader(MapObjectType.SPAWNPOINT, new SpawnpointMapObjectLoader());
  }

  /**
   * Unload the specified entity by performing the following steps:
   * <ol>
   * <li>remove entities from physics engine</li>
   * <li>unregister units from update</li>
   * <li>unregister ai controller from update</li>
   * <li>unregister animation controller from update</li>
   * <li>unregister movement controller from update</li>
   * </ol>
   *
   * @param entity
   */
  private void unload(final IEntity entity) {
    // 1. remove from physics engine
    if (entity instanceof CollisionBox) {
      final CollisionBox coll = (CollisionBox) entity;
      if (coll.isObstacle()) {
        Game.getPhysicsEngine().remove(coll.getBoundingBox());
      } else {
        Game.getPhysicsEngine().remove(coll);
      }
    } else if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      Game.getPhysicsEngine().remove(coll);
    }

    // 2. unregister from update
    if (entity instanceof IUpdateable) {
      Game.getLoop().detach((IUpdateable) entity);
    }

    // 3. unregister ai controller from update
    final IEntityController<? extends IEntity> controller = Game.getEntityControllerManager().getAIController(entity);
    if (controller != null) {
      Game.getLoop().detach(controller);
    }

    // 4. unregister animation controller from update
    final IAnimationController animation = Game.getEntityControllerManager().getAnimationController(entity);
    if (animation != null) {
      Game.getLoop().detach(animation);
    }

    // 5. unregister movement controller from update
    if (entity instanceof IMovableEntity) {
      final IMovementController<? extends IMovableEntity> movementController = Game.getEntityControllerManager().getMovementController((IMovableEntity) entity);
      if (movementController != null) {
        Game.getLoop().detach(movementController);
      }
    }

    if (entity instanceof Emitter) {
      Emitter em = (Emitter) entity;
      em.deactivate();
    }
  }
}