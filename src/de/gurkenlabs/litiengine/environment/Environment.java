package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
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
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.io.FileUtilities;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {
  private static final Logger log = Logger.getLogger(Environment.class.getName());
  private static final Map<String, IMapObjectLoader> mapObjectLoaders;

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
  private final Collection<CollisionBox> colliders;
  private final Collection<LightSource> lightSources;
  private final Collection<StaticShadow> staticShadows;
  private final Collection<Trigger> triggers;
  private final Collection<Prop> props;
  private final Collection<Emitter> emitters;

  private final Collection<MapArea> mapAreas;

  private final Map<Integer, IMovableEntity> movableEntities;
  private final List<IRenderable> overlayRenderable;

  private final List<Spawnpoint> spawnPoints;

  private AmbientLight ambientLight;
  private StaticShadowLayer staticShadowLayer;
  private boolean loaded;
  private boolean initialized;
  private IMap map;

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
    this.entities.put(RenderType.NONE, new ConcurrentHashMap<>());
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
    this.props = new CopyOnWriteArrayList<>();
    this.emitters = new CopyOnWriteArrayList<>();

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
      this.emitters.add(emitter);
    }

    if (entity instanceof ICombatEntity) {
      this.combatEntities.put(entity.getMapId(), (ICombatEntity) entity);
    }

    if (entity instanceof IMovableEntity) {
      this.movableEntities.put(entity.getMapId(), (IMovableEntity) entity);
    }

    if (entity instanceof Prop) {
      this.props.add((Prop) entity);
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

    if (entity instanceof StaticShadow) {
      this.staticShadows.add((StaticShadow) entity);
    } else if (entity instanceof MapArea) {
      this.mapAreas.add((MapArea) entity);
    }

    for (String rawTag : entity.getTags()) {
      if (rawTag == null) {
        continue;
      }

      final String tag = rawTag.trim().toLowerCase();
      if (tag.isEmpty()) {
        continue;
      }

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

    this.entities.get(RenderType.NONE).clear();
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

    entity = this.entities.get(RenderType.NONE).get(mapId);
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

    for (final IEntity entity : this.entities.get(RenderType.NONE).values()) {
      if (entity.getName() != null && entity.getName().equals(name)) {
        return entity;
      }
    }

    return null;
  }

  @Override
  public Collection<IEntity> getByTag(String tag) {
    if (this.entitiesByTag.containsKey(tag.toLowerCase())) {
      return this.entitiesByTag.get(tag);
    }

    return new ArrayList<>();
  }

  @Override
  public <T extends IEntity> Collection<T> getByTag(Class<T> clss, String rawTag) {
    List<T> entities = new ArrayList<>();
    final String tag = rawTag.toLowerCase();
    if (!this.entitiesByTag.containsKey(tag.toLowerCase())) {
      return entities;
    }

    for (IEntity ent : this.entitiesByTag.get(tag)) {
      if (clss.isInstance(ent)) {
        entities.add((T) ent);
      }
    }

    return entities;
  }

  @Override
  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  @Override
  public Collection<MapArea> getAreas() {
    return this.mapAreas;
  }

  @Override
  public MapArea getArea(final int mapId) {
    return getById(this.getAreas(), mapId);
  }

  @Override
  public MapArea getArea(final String name) {
    return getByName(this.getAreas(), name);
  }

  @Override
  public Collection<Emitter> getEmitters() {
    return this.emitters;
  }

  @Override
  public Emitter getEmitter(int mapId) {
    return getById(this.getEmitters(), mapId);
  }

  @Override
  public Emitter getEmitter(String name) {
    return getByName(this.getEmitters(), name);
  }

  @Override
  public Collection<CollisionBox> getCollisionBoxes() {
    return this.colliders;
  }

  @Override
  public CollisionBox getCollisionBox(int mapId) {
    return getById(this.getCollisionBoxes(), mapId);
  }

  @Override
  public CollisionBox getCollisionBox(String name) {
    return getByName(this.getCollisionBoxes(), name);
  }

  @Override
  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  @Override
  public ICombatEntity getCombatEntity(final int mapId) {
    return getById(this.getCombatEntities(), mapId);
  }

  @Override
  public ICombatEntity getCombatEntity(String name) {
    return getByName(this.getCombatEntities(), name);
  }

  @Override
  public Collection<IEntity> getEntities() {
    final ArrayList<IEntity> ent = new ArrayList<>();
    ent.addAll(this.entities.get(RenderType.NONE).values());
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
  public Collection<LightSource> getLightSources() {
    return this.lightSources;
  }

  @Override
  public LightSource getLightSource(final int mapId) {
    return getById(this.getLightSources(), mapId);
  }

  @Override
  public LightSource getLightSource(String name) {
    return getByName(this.getLightSources(), name);
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
    return getById(this.getMovableEntities(), mapId);
  }

  @Override
  public IMovableEntity getMovableEntity(String name) {
    return getByName(this.getMovableEntities(), name);
  }

  @Override
  public synchronized int getNextMapId() {
    return ++mapIdSequence;
  }

  public List<IRenderable> getOverlayRenderables() {
    return this.overlayRenderable;
  }

  @Override
  public Collection<Prop> getProps() {
    return this.props;
  }

  @Override
  public Prop getProp(int mapId) {
    return getById(this.getProps(), mapId);
  }

  @Override
  public Prop getProp(String name) {
    return getByName(this.getProps(), name);
  }

  @Override
  public Spawnpoint getSpawnpoint(final int mapId) {
    return getById(this.getSpawnPoints(), mapId);
  }

  @Override
  public Spawnpoint getSpawnpoint(final String name) {
    return getByName(this.getSpawnPoints(), name);
  }

  @Override
  public List<Spawnpoint> getSpawnPoints() {
    return this.spawnPoints;
  }

  @Override
  public Collection<StaticShadow> getStaticShadows() {
    return this.staticShadows;
  }

  @Override
  public StaticShadow getStaticShadow(int mapId) {
    return getById(this.getStaticShadows(), mapId);
  }

  @Override
  public StaticShadow getStaticShadow(String name) {
    return getByName(this.getStaticShadows(), name);
  }

  @Override
  public StaticShadowLayer getStaticShadowLayer() {
    return this.staticShadowLayer;
  }

  @Override
  public Trigger getTrigger(final int mapId) {
    return getById(this.getTriggers(), mapId);
  }

  @Override
  public Trigger getTrigger(final String name) {
    return getByName(this.getTriggers(), name);
  }

  @Override
  public Collection<Trigger> getTriggers() {
    return this.triggers;
  }

  @Override
  public List<String> getUsedTags() {
    final List<String> tags = this.entitiesByTag.keySet().stream().collect(Collectors.toList());
    Collections.sort(tags);

    return tags;
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

        if (this.entitiesByTag.get(tag).isEmpty()) {
          this.entitiesByTag.remove(tag);
        }
      }
    }

    if (entity instanceof Emitter) {
      Emitter emitter = (Emitter) entity;
      this.groundRenderable.remove(emitter.getGroundRenderable());
      this.overlayRenderable.remove(emitter.getOverlayRenderable());
      this.emitters.remove(emitter);
    }

    if (entity instanceof MapArea) {
      this.mapAreas.remove(entity);
    }

    if (entity instanceof Prop) {
      this.props.remove(entity);
    }

    if (entity instanceof CollisionBox) {
      this.colliders.remove(entity);
      this.staticShadows.removeIf(x -> x.getOrigin() != null && x.getOrigin().equals(entity));
    }

    if (entity instanceof LightSource) {
      this.lightSources.remove(entity);
      this.addStaticShadows();
    }

    if (entity instanceof Trigger) {
      this.triggers.remove(entity);
    }

    if (entity instanceof Spawnpoint) {
      this.spawnPoints.remove(entity);
    }

    if (entity instanceof StaticShadow) {
      this.staticShadows.remove(entity);
      this.addStaticShadows();
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
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  @Override
  public <T extends IEntity> void remove(Collection<T> entities) {
    if (entities == null) {
      return;
    }

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
      this.getStaticShadowLayer().render(g);
    }

    if (Game.getConfiguration().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getAlpha() != 0) {
      this.getAmbientLight().render(g);
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
      Collection<IEntity> loadedEntities = mapObjectLoaders.get(mapObject.getType()).load(mapObject);
      for (IEntity entity : loadedEntities) {
        if (entity != null) {
          this.add(entity);
        }
      }
    }
  }

  private static <T extends IEntity> T getById(Collection<T> entities, int mapId) {
    for (final T m : entities) {
      if (m.getMapId() == mapId) {
        return m;
      }
    }

    return null;
  }

  private static <T extends IEntity> T getByName(Collection<T> entities, String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final T m : entities) {
      if (m.getName() != null && m.getName().equals(name)) {
        return m;
      }
    }
    return null;
  }

  private void addAmbientLight() {
    final int ambientAlpha = this.getMap().getCustomPropertyInt(MapProperty.AMBIENTALPHA);
    final Color ambientColor = this.getMap().getCustomPropertyColor(MapProperty.AMBIENTCOLOR, Color.WHITE);
    this.ambientLight = new AmbientLight(this, ambientColor, ambientAlpha);
  }

  private void addStaticShadows() {
    final int alpha = this.getMap().getCustomPropertyInt(MapProperty.SHADOWALPHA, StaticShadow.DEFAULT_ALPHA);
    final Color color = this.getMap().getCustomPropertyColor(MapProperty.SHADOWCOLOR, StaticShadow.DEFAULT_COLOR);
    this.staticShadowLayer = new StaticShadowLayer(this, alpha, color);
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
    registerMapObjectLoader(MapObjectType.AREA, new MapAreaMapObjectLoader());
    registerMapObjectLoader(MapObjectType.STATICSHADOW, new StaticShadowMapObjectLoader());
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