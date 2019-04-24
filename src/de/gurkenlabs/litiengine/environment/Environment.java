package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameMetrics;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.ILayer;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapRenderer;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderComponent;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.physics.GravityForce;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.ColorHelper;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public final class Environment implements IRenderable {
  private static final Map<String, IMapObjectLoader> mapObjectLoaders = new ConcurrentHashMap<>();
  private static final String GRAVITY_IDENTIFIER = "GRAVITY";
  private static final Logger log = Logger.getLogger(Environment.class.getName());

  private final Map<Integer, ICombatEntity> combatEntities = new ConcurrentHashMap<>();
  private final Map<Integer, IMobileEntity> mobileEntities = new ConcurrentHashMap<>();
  private final Map<Integer, GravityForce> gravityForces = new ConcurrentHashMap<>();
  private final Map<RenderType, Map<Integer, IEntity>> miscEntities = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Map<IMapObjectLayer, List<IEntity>> layerEntities = new ConcurrentHashMap<>();
  private final Map<String, Collection<IEntity>> entitiesByTag = new ConcurrentHashMap<>();
  private final Map<Integer, IEntity> allEntities = new ConcurrentHashMap<>();

  private final Map<RenderType, Collection<EnvironmentRenderListener>> renderListeners = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final List<EnvironmentListener> listeners = new CopyOnWriteArrayList<>();
  private final List<EnvironmentEntityListener> entityListeners = new CopyOnWriteArrayList<>();

  private final Map<RenderType, Collection<IRenderable>> renderables = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Collection<CollisionBox> colliders = ConcurrentHashMap.newKeySet();
  private final Collection<LightSource> lightSources = ConcurrentHashMap.newKeySet();
  private final Collection<StaticShadow> staticShadows = ConcurrentHashMap.newKeySet();
  private final Collection<Trigger> triggers = ConcurrentHashMap.newKeySet();
  private final Collection<Prop> props = ConcurrentHashMap.newKeySet();
  private final Collection<Emitter> emitters = ConcurrentHashMap.newKeySet();
  private final Collection<Creature> creatures = ConcurrentHashMap.newKeySet();
  private final Collection<Spawnpoint> spawnPoints = ConcurrentHashMap.newKeySet();
  private final Collection<MapArea> mapAreas = ConcurrentHashMap.newKeySet();

  private AmbientLight ambientLight;
  private StaticShadowLayer staticShadowLayer;
  private boolean loaded;
  private boolean initialized;
  private IMap map;
  private int localIdSequence = 0;

  private int gravity;

  static {
    registerMapObjectLoader(new PropMapObjectLoader());
    registerMapObjectLoader(new CollisionBoxMapObjectLoader());
    registerMapObjectLoader(new TriggerMapObjectLoader());
    registerMapObjectLoader(new EmitterMapObjectLoader());
    registerMapObjectLoader(new LightSourceMapObjectLoader());
    registerMapObjectLoader(new SpawnpointMapObjectLoader());
    registerMapObjectLoader(new MapAreaMapObjectLoader());
    registerMapObjectLoader(new StaticShadowMapObjectLoader());
    registerMapObjectLoader(new CreatureMapObjectLoader());
  }

  public Environment(final IMap map) {
    this();
    this.map = map;
    if (this.getMap() != null) {
      Game.physics().setBounds(this.getMap().getBounds());
      this.setGravity(this.getMap().getIntValue(MapProperty.GRAVITY));
    }
  }

  public Environment(final String mapPath) {
    this(Resources.maps().get(mapPath));
  }

  private Environment() {
    for (RenderType renderType : RenderType.values()) {
      this.miscEntities.put(renderType, new ConcurrentHashMap<>());
      this.renderListeners.put(renderType, ConcurrentHashMap.newKeySet());
      this.renderables.put(renderType, ConcurrentHashMap.newKeySet());
    }
  }

  public void addRenderListener(RenderType renderType, EnvironmentRenderListener listener) {
    this.renderListeners.get(renderType).add(listener);
  }

  public void removeRenderListener(EnvironmentRenderListener listener) {
    for (Collection<EnvironmentRenderListener> rends : this.renderListeners.values()) {
      rends.remove(listener);
    }
  }

  public void addListener(EnvironmentListener listener) {
    this.listeners.add(listener);
  }

  public void removeListener(EnvironmentListener listener) {
    this.listeners.remove(listener);
  }

  public void addEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.add(listener);
  }

  public void removeEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.remove(listener);
  }

  /**
   * Adds the specified entity to the environment container. This also loads the
   * entity (register entity and controllers for update) if the environment has
   * already been loaded. The entity will not be bound to a layer.
   *
   * @param entity
   *          The entity to add to the environment.
   */
  public void add(IEntity entity) {
    if (entity == null) {
      return;
    }
    this.addEntity(entity);
    this.miscEntities.get(entity.getRenderType()).put(entity.getMapId(), entity);
  }

  private void addEntity(final IEntity entity) {
    int desiredID = entity.getMapId();
    // assign local map id if the entity's mapID is invalid
    if (desiredID == 0 || this.getAllMapIDs().contains(desiredID)) {
      entity.setMapId(this.getLocalMapId());
      log.fine(() -> String.format("Entity [%s] was assigned a local mapID because #%d was already taken or invalid.", entity, desiredID));
    }

    if (entity instanceof Emitter)
    {
      Emitter emitter = (Emitter) entity;
      this.addEmitter(emitter);
    }

    if (entity instanceof ICombatEntity) {
      this.combatEntities.put(entity.getMapId(), (ICombatEntity) entity);
    }

    if (entity instanceof IMobileEntity) {
      this.mobileEntities.put(entity.getMapId(), (IMobileEntity) entity);
    }

    if (entity instanceof Prop) {
      this.props.add((Prop) entity);
    }

    if (entity instanceof Creature) {
      this.creatures.add((Creature) entity);
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

      this.getEntitiesByTag().computeIfAbsent(tag, t -> new CopyOnWriteArrayList<>()).add(entity);
    }

    // if the environment has already been loaded,
    // we need to load the new entity manually
    if (this.loaded) {
      this.load(entity);
    }

    this.allEntities.put(entity.getMapId(), entity);

    this.fireEntityEvent(l -> l.entityAdded(entity));
  }

  private void addEmitter(Emitter emitter) {
    this.manageEmitterRenderables(emitter, (rends, instance) -> rends.add(instance));
    this.emitters.add(emitter);
  }

  private void removeEmitter(Emitter emitter) {
    this.manageEmitterRenderables(emitter, (rends, instance) -> rends.remove(instance));
    this.emitters.remove(emitter);
  }

  private void manageEmitterRenderables(Emitter emitter, BiConsumer<Collection<IRenderable>, IRenderable> cons) {
    for (RenderType renderType : RenderType.values()) {
      if (renderType == RenderType.NONE) {
        continue;
      }

      IRenderable renderable = emitter.getRenderable(renderType);
      if (renderable != null) {
        cons.accept(this.getRenderables(renderType), renderable);
      }
    }

    this.emitters.remove(emitter);
  }

  public void updateLighting(IEntity entity) {
    if (entity instanceof StaticShadow) {
      StaticShadow shadow = (StaticShadow) entity;
      this.updateLighting(shadow.getArea() != null ? shadow.getArea().getBounds2D() : shadow.getBoundingBox());
      return;
    }

    this.updateLighting(entity.getBoundingBox());
  }

  public void updateLighting() {
    if (this.getMap() != null) {
      this.updateLighting(this.getMap().getBounds());
    }
  }

  public void updateLighting(Rectangle2D section) {
    if (this.staticShadowLayer != null) {
      this.staticShadowLayer.updateSection(section);
    }

    if (this.ambientLight != null) {
      this.ambientLight.updateSection(section);
    }
  }

  public void add(IRenderable renderable, RenderType renderType) {
    this.getRenderables(renderType).add(renderable);
  }

  public Collection<IEntity> build(Blueprint blueprint, double x, double y) {
    return this.build(blueprint, new Point2D.Double(x, y));
  }

  public Collection<IEntity> build(Blueprint blueprint, Point2D location) {
    Collection<IMapObject> mapObjects = blueprint.build(location);
    Collection<IEntity> loadedEntities = new ArrayList<>();
    for (IMapObject obj : mapObjects) {
      loadedEntities.addAll(this.load(obj));
    }

    return loadedEntities;
  }

  public void clear() {
    Game.physics().clear();

    this.combatEntities.clear();
    this.mobileEntities.clear();
    this.gravityForces.clear();
    this.layerEntities.clear();
    this.entitiesByTag.clear();
    this.allEntities.clear();

    for (RenderType renderType : RenderType.values()) {
      this.miscEntities.get(renderType).clear();
      this.renderListeners.get(renderType).clear();
      this.renderables.get(renderType).clear();
    }

    dispose(this.getEntities());
    dispose(this.getTriggers());
    this.getEmitters().clear();
    this.getCollisionBoxes().clear();
    this.getProps().clear();
    this.getCreatures().clear();
    this.getStaticShadows().clear();
    this.getCombatEntities().clear();
    this.getMobileEntities().clear();
    this.getLightSources().clear();
    this.getCollisionBoxes().clear();
    this.getSpawnPoints().clear();
    this.getAreas().clear();
    this.getTriggers().clear();

    this.ambientLight = null;
    this.staticShadowLayer = null;

    for (Map<Integer, IEntity> type : this.miscEntities.values()) {
      type.clear();
    }

    this.initialized = false;

    this.fireEvent(l -> l.cleared(this));
  }

  public Collection<ICombatEntity> findCombatEntities(final Shape shape) {
    return this.findCombatEntities(shape, entity -> true);
  }

  /**
   * Searches for all combat entities whose hitBox intersect the specified
   * shape.
   * 
   * @param shape
   *          The shape to check intersection for.
   * @param condition
   *          An additional condition that allows to specify a condition which
   *          determines if a {@link ICombatEntity} should be considered.
   * @return A list of all combat entities that intersect the specified
   *         {@link Shape}.
   */
  public Collection<ICombatEntity> findCombatEntities(final Shape shape, final Predicate<ICombatEntity> condition) {
    final Collection<ICombatEntity> foundCombatEntities = new ArrayList<>();
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

  public Collection<IEntity> findEntities(final Shape shape) {
    final Collection<IEntity> foundEntities = new ArrayList<>();
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

  public IEntity get(final int mapId) {
    return this.allEntities.get(mapId);
  }

  /**
   * Finds the entities in the environment with the specified map IDs.
   * 
   * @param mapIds
   *          The map IDs to search for.
   * @return A {@code List} of entities found, in the order given by the parameters.
   */
  public List<IEntity> get(final int... mapIds) {
    final List<IEntity> foundEntities = new ArrayList<>();
    if (mapIds == null) {
      return foundEntities;
    }

    for (int id : mapIds) {
      IEntity entity = this.allEntities.get(id);
      if (entity != null) {
        foundEntities.add(entity);
      }
    }

    return foundEntities;
  }

  public <T extends IEntity> T get(Class<T> clss, int mapId) {
    IEntity ent = this.get(mapId);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  public IEntity get(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final IEntity entity : this.allEntities.values()) {
      if (entity.getName() != null && entity.getName().equals(name)) {
        return entity;
      }
    }

    return null;
  }

  public <T extends IEntity> T get(Class<T> clss, String name) {
    IEntity ent = this.get(name);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  public Collection<IEntity> getByTag(String... tags) {
    Collection<IEntity> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();
      foundEntities.addAll(this.getEntitiesByTag().getOrDefault(tag, Arrays.asList()));
    }

    return foundEntities;
  }

  public <T extends IEntity> Collection<T> getByTag(Class<? extends T> clss, String... tags) {
    Collection<T> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();

      for (IEntity ent : this.getEntitiesByTag().getOrDefault(tag, Arrays.asList())) {
        if (!foundEntities.contains(ent) && clss.isInstance(ent)) {
          foundEntities.add(clss.cast(ent));
        }
      }

    }

    return foundEntities;
  }

  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  public Collection<MapArea> getAreas() {
    return this.mapAreas;
  }

  public MapArea getArea(final int mapId) {
    return getById(this.getAreas(), mapId);
  }

  public MapArea getArea(final String name) {
    return getByName(this.getAreas(), name);
  }

  public Point2D getCenter() {
    return new Point2D.Double(this.getMap().getSizeInPixels().getWidth() / 2.0, this.getMap().getSizeInPixels().getHeight() / 2.0);
  }

  public Collection<Emitter> getEmitters() {
    return this.emitters;
  }

  public Emitter getEmitter(int mapId) {
    return getById(this.getEmitters(), mapId);
  }

  public Emitter getEmitter(String name) {
    return getByName(this.getEmitters(), name);
  }

  public Collection<CollisionBox> getCollisionBoxes() {
    return this.colliders;
  }

  public CollisionBox getCollisionBox(int mapId) {
    return getById(this.getCollisionBoxes(), mapId);
  }

  public CollisionBox getCollisionBox(String name) {
    return getByName(this.getCollisionBoxes(), name);
  }

  public Collection<ICombatEntity> getCombatEntities() {
    return this.combatEntities.values();
  }

  public ICombatEntity getCombatEntity(final int mapId) {
    return getById(this.getCombatEntities(), mapId);
  }

  public ICombatEntity getCombatEntity(String name) {
    return getByName(this.getCombatEntities(), name);
  }

  public Collection<IEntity> getEntities() {
    return Collections.unmodifiableCollection(this.allEntities.values());
  }

  /**
   * Gets the entities with the specified render type that are not bound to layers.
   * <p>
   * Entities are unbound from there originating <code>MapObjectLayer</code> if their <code>RenderType</code> differs
   * from the layer's <code>RenderType</code>.
   * </p>
   *
   * @param renderType
   *          The render type
   * @return The miscellaneous entities with the specified render type
   * 
   * @see IEntity#getRenderType()
   * @see ILayer#getRenderType()
   */
  public Collection<IEntity> getEntities(final RenderType renderType) {
    return this.miscEntities.get(renderType).values();
  }

  /**
   * Gets the entities that are bound to the specified layer.
   * <p>
   * Entities are bound to a layer if their <code>RenderType</code> matches the layer's <code>RenderType</code>
   * </p>
   * 
   * @param layer
   *          The layer that the entities are bound to.
   * @return The entities that are bound to the specified layer.
   * 
   * @see IEntity#getRenderType()
   * @see ILayer#getRenderType()
   */
  public Collection<IEntity> getEntities(final IMapObjectLayer layer) {
    if (layer == null || !this.layerEntities.containsKey(layer)) {
      return Collections.emptySet();
    }

    return this.layerEntities.get(layer);
  }

  /**
   * Gets the entities that are bound to layer with the specified name.
   * <p>
   * Entities are bound to a layer if their <code>RenderType</code> matches the layer's <code>RenderType</code>
   * </p>
   * 
   * @param name
   *          The name of the layer
   * @return The entities that are bound to the specified layer.
   * 
   * @see IEntity#getRenderType()
   * @see ILayer#getRenderType()
   * @see ILayer#getName()
   */
  public Collection<IEntity> getEntitiesByLayer(final String name) {
    if (name == null || name.isEmpty()) {
      return Collections.emptySet();
    }

    for (Entry<IMapObjectLayer, List<IEntity>> entry : this.layerEntities.entrySet()) {
      if (name.equals(entry.getKey().getName())) {
        return entry.getValue();
      }
    }

    return Collections.emptySet();
  }

  /**
   * Gets the entities that are bound to layer with the specified layer ID.
   * <p>
   * Entities are bound to a layer if their <code>RenderType</code> matches the layer's <code>RenderType</code>
   * </p>
   * 
   * @param layerId
   *          The id of the layer
   * @return The entities that are bound to the specified layer.
   * 
   * @see IEntity#getRenderType()
   * @see ILayer#getRenderType()
   * @see ILayer#getId()
   */
  public Collection<IEntity> getEntitiesByLayer(final int layerId) {
    for (Entry<IMapObjectLayer, List<IEntity>> entry : this.layerEntities.entrySet()) {
      if (layerId == entry.getKey().getId()) {
        return entry.getValue();
      }
    }

    return Collections.emptySet();
  }

  public Map<String, Collection<IEntity>> getEntitiesByTag() {
    return this.entitiesByTag;
  }

  public <T> Collection<T> getByType(Class<? extends T> cls) {
    Collection<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.getEntities()) {
      if (cls.isInstance(ent)) {
        foundEntities.add(cls.cast(ent));
      }
    }

    return foundEntities;
  }

  public Collection<LightSource> getLightSources() {
    return this.lightSources;
  }

  public LightSource getLightSource(final int mapId) {
    return getById(this.getLightSources(), mapId);
  }

  public LightSource getLightSource(String name) {
    return getByName(this.getLightSources(), name);
  }

  /**
   * Gets the next unique local map id. (All local map ids are negative).
   * 
   * @return The next unique local map id.
   */
  public synchronized int getLocalMapId() {
    return --localIdSequence;
  }

  public IMap getMap() {
    return this.map;
  }

  public Collection<IMobileEntity> getMobileEntities() {
    return this.mobileEntities.values();
  }

  public IMobileEntity getMobileEntity(final int mapId) {
    return getById(this.getMobileEntities(), mapId);
  }

  public IMobileEntity getMobileEntity(String name) {
    return getByName(this.getMobileEntities(), name);
  }

  /**
   * Gets the next unique global map id.
   *
   * @return The next unique global map id.
   */
  public synchronized int getNextMapId() {
    int maxMapID = MapUtilities.getMaxMapId(this.getMap());
    return ++maxMapID;
  }

  public Collection<IRenderable> getRenderables(RenderType renderType) {
    return this.renderables.get(renderType);
  }

  public Collection<Prop> getProps() {
    return this.props;
  }

  public Prop getProp(int mapId) {
    return getById(this.getProps(), mapId);
  }

  public Prop getProp(String name) {
    return getByName(this.getProps(), name);
  }

  public Creature getCreature(int mapId) {
    return getById(this.getCreatures(), mapId);
  }

  public Creature getCreature(String name) {
    return getByName(this.getCreatures(), name);
  }

  public Collection<Creature> getCreatures() {
    return this.creatures;
  }

  public Spawnpoint getSpawnpoint(final int mapId) {
    return getById(this.getSpawnPoints(), mapId);
  }

  public Spawnpoint getSpawnpoint(final String name) {
    return getByName(this.getSpawnPoints(), name);
  }

  public Collection<Spawnpoint> getSpawnPoints() {
    return this.spawnPoints;
  }

  public Collection<StaticShadow> getStaticShadows() {
    return this.staticShadows;
  }

  public StaticShadow getStaticShadow(int mapId) {
    return getById(this.getStaticShadows(), mapId);
  }

  public StaticShadow getStaticShadow(String name) {
    return getByName(this.getStaticShadows(), name);
  }

  public StaticShadowLayer getStaticShadowLayer() {
    return this.staticShadowLayer;
  }

  public Trigger getTrigger(final int mapId) {
    return getById(this.getTriggers(), mapId);
  }

  public Trigger getTrigger(final String name) {
    return getByName(this.getTriggers(), name);
  }

  public Collection<Trigger> getTriggers() {
    return this.triggers;
  }

  public Collection<String> getUsedTags() {
    return new ArrayList<>(this.getEntitiesByTag().keySet());
  }

  public final void init() {
    if (this.initialized) {
      return;
    }

    if (this.getMap() != null) {
      this.loadMapObjects();
      this.addStaticShadows();
      this.addAmbientLight();
    }

    this.fireEvent(l -> l.initialized(this));
    this.initialized = true;
  }

  public boolean isLoaded() {
    return this.loaded;
  }

  public void load() {
    this.init();
    if (this.loaded) {
      return;
    }

    if (this.getMap() != null) {
      Game.physics().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
    }

    if (this.getMap() != null) {
      if (this.getMap().getBackgroundColor() != null) {
        Game.window().getRenderComponent().setBackground(ColorHelper.premultiply(this.getMap().getBackgroundColor()));
      }
    } else {
      Game.window().getRenderComponent().setBackground(Color.BLACK);
    }

    this.getEntities().stream().forEach(this::load);
    this.updateLighting();
    this.loaded = true;
    this.fireEvent(l -> l.loaded(this));
  }

  public void loadFromMap(final int mapId) {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      Optional<IMapObject> opt = layer.getMapObjects().stream().filter(mapObject -> mapObject.getType() != null && !mapObject.getType().isEmpty() && mapObject.getId() == mapId).findFirst();
      if (opt.isPresent()) {
        this.load(opt.get());
        break;
      }
    }
  }

  /**
   * Registers a custom loader instance that is responsible for loading and initializing entities of the defined
   * MapObjectType.
   * <br>
   * <br>
   * There can only be one loader for a particular type. Calling this method again for the same type will overwrite the previously registered loader.
   * 
   * @param mapObjectLoader
   *          The MapObjectLoader instance to be registered.
   * 
   * @see IMapObjectLoader#getMapObjectType()
   */
  public static void registerMapObjectLoader(IMapObjectLoader mapObjectLoader) {
    mapObjectLoaders.put(mapObjectLoader.getMapObjectType(), mapObjectLoader);
  }

  /**
   * Registers a custom <code>IEntity</code> implementation to support being loaded from an <code>IMap</code> instance.
   * Note that the specified class needs to be accessible in a static manner. Inner classes that aren't declared statically are not supported.
   * 
   * This is an overload of the {@link #registerCustomEntityType(Class)} method that allows to explicitly specify the <code>MapObjectType</code>
   * without
   * having to provide an <code>EntityInfo</code> annotation containing this information.
   * 
   * @param <T>
   *          The type of the custom entity.
   * @param mapObjectType
   *          The custom mapobjectType that is used by <code>IMapObjects</code> to determine the target entity implementation.
   * @param entityType
   *          The class type of the custom entity implementation.
   * 
   * @see IMapObject#getType()
   * @see EntityInfo#customMapObjectType()
   */
  public static <T extends IEntity> void registerCustomEntityType(String mapObjectType, Class<T> entityType) {
    CustomMapObjectLoader mapObjectLoader = new CustomMapObjectLoader(mapObjectType, entityType);
    registerMapObjectLoader(mapObjectLoader);
  }

  /**
   * Registers a custom <code>IEntity</code> implementation to support being loaded from an <code>IMap</code> instance.
   * Note that the specified class needs to be accessible in a static manner. Inner classes that aren't declared statically are not supported.
   * 
   * This implementation uses the provided <code>EntityInfo.customMapObjectType()</code> to determine for which type the specified class should be
   * used.
   * 
   * @param <T>
   *          The type of the custom entity.
   * @param entityType
   *          The class type of the custom entity implementation.
   * 
   * @see Environment#registerCustomEntityType(String, Class)
   * @see IMapObject#getType()
   * @see EntityInfo#customMapObjectType()
   */
  public static <T extends IEntity> void registerCustomEntityType(Class<T> entityType) {
    EntityInfo info = entityType.getAnnotation(EntityInfo.class);
    if (info == null || info.customMapObjectType().isEmpty()) {
      throw new IllegalArgumentException(
          "Cannot register a custom entity type without the related EntityInfo.customMapObjectType being specified.\nAdd an EntityInfo annotation to the " + entityType + " class and provide the required information or use the registerCustomEntityType overload and provide the type explicitly.");
    }

    registerCustomEntityType(info.customMapObjectType(), entityType);
  }

  public void reloadFromMap(final int mapId) {
    this.remove(mapId);
    this.loadFromMap(mapId);
  }

  public void remove(final IEntity entity) {
    if (entity == null) {
      return;
    }

    this.allEntities.remove(entity.getMapId());
    Iterator<List<IEntity>> iter = this.layerEntities.values().iterator();
    while (iter.hasNext()) {
      List<IEntity> layer = iter.next();
      if (layer.remove(entity) && layer.isEmpty()) {
        iter.remove();
      }
    }
    if (this.miscEntities.get(entity.getRenderType()) != null) {
      this.miscEntities.get(entity.getRenderType()).values().remove(entity);
    }

    for (String tag : entity.getTags()) {
      if (this.getEntitiesByTag().containsKey(tag)) {
        this.getEntitiesByTag().get(tag).remove(entity);

        if (this.getEntitiesByTag().get(tag).isEmpty()) {
          this.getEntitiesByTag().remove(tag);
        }
      }
    }

    if (entity instanceof Emitter) {
      Emitter emitter = (Emitter) entity;
      this.removeEmitter(emitter);
    }

    if (entity instanceof MapArea) {
      this.mapAreas.remove(entity);
    }

    if (entity instanceof Prop) {
      this.props.remove(entity);
    }

    if (entity instanceof Creature) {
      this.creatures.remove(entity);
    }

    if (entity instanceof CollisionBox) {
      this.colliders.remove(entity);
      this.staticShadows.removeIf(x -> x.getOrigin() != null && x.getOrigin().equals(entity));
    }

    if (entity instanceof LightSource) {
      this.lightSources.remove(entity);
      this.updateLighting(entity);
    }

    if (entity instanceof Trigger) {
      this.triggers.remove(entity);
    }

    if (entity instanceof Spawnpoint) {
      this.spawnPoints.remove(entity);
    }

    if (entity instanceof StaticShadow) {
      this.staticShadows.remove(entity);
      this.updateLighting(entity);
    }

    if (entity instanceof IMobileEntity) {
      this.mobileEntities.values().remove(entity);
    }

    if (entity instanceof ICombatEntity) {
      this.combatEntities.values().remove(entity);
    }

    this.unload(entity);

    this.fireEntityEvent(l -> l.entityRemoved(entity));
  }

  public void remove(final int mapId) {
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  public void remove(String name) {
    final IEntity ent = this.get(name);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  public <T extends IEntity> void remove(Collection<T> entities) {
    if (entities == null) {
      return;
    }

    for (T ent : entities) {
      this.remove(ent);
    }
  }

  public void removeRenderable(final IRenderable renderable) {
    for (Collection<IRenderable> rends : this.renderables.values()) {
      rends.remove(renderable);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.scale(Game.world().camera().getRenderScale(), Game.world().camera().getRenderScale());

    long renderStart = System.nanoTime();

    this.render(g, RenderType.BACKGROUND);

    this.render(g, RenderType.GROUND);
    if (Game.config().debug().isDebug()) {
      DebugRenderer.renderMapDebugInfo(g, this.getMap());
    }

    this.render(g, RenderType.SURFACE);
    this.render(g, RenderType.NORMAL);
    this.render(g, RenderType.OVERLAY);

    long ambientStart = System.nanoTime();
    if (Game.config().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getColor().getAlpha() != 0) {
      this.getAmbientLight().render(g);
    }

    final double ambientTime = TimeUtilities.nanoToMs(System.nanoTime() - ambientStart);

    long shadowRenderStart = System.nanoTime();
    if (this.getStaticShadows().stream().anyMatch(x -> x.getShadowType() != StaticShadowType.NONE)) {
      this.getStaticShadowLayer().render(g);
    }

    final double shadowTime = TimeUtilities.nanoToMs(System.nanoTime() - shadowRenderStart);
    
    this.render(g, RenderType.UI);

    if (Game.config().debug().trackRenderTimes()) {

      final double totalRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);

      Game.metrics().trackRenderTime("shadow", shadowTime);
      Game.metrics().trackRenderTime("ambient", ambientTime);
      Game.metrics().trackRenderTime("world", totalRenderTime);
    }

    g.scale(1.0 / Game.world().camera().getRenderScale(), 1.0 / Game.world().camera().getRenderScale());
  }

  public int getGravity() {
    return this.gravity;
  }

  public void setGravity(int gravity) {
    this.gravity = gravity;

    if (this.getGravity() != 0) {

      // if there are gravity forces for all mobile entities, just update the existing forces
      if (this.gravityForces.size() == this.getMobileEntities().size()) {
        for (GravityForce force : this.gravityForces.values()) {
          force.setStrength(this.gravity);
        }
      } else {
        // otherwise create a new force for every mobile entity in the environment
        for (IMobileEntity entity : this.getMobileEntities()) {
          this.addGravityForce(entity);
        }
      }
    } else {
      for (IMobileEntity entity : this.getMobileEntities()) {
        this.removeGravity(entity);
      }
    }
  }

  private void fireEvent(Consumer<EnvironmentListener> cons) {
    for (EnvironmentListener listener : this.listeners) {
      cons.accept(listener);
    }
  }

  private void fireRenderEvent(Graphics2D g, RenderType type) {
    for (EnvironmentRenderListener listener : this.renderListeners.get(type)) {
      listener.rendered(g, type);
    }
  }

  private void fireEntityEvent(Consumer<EnvironmentEntityListener> cons) {
    for (EnvironmentEntityListener listener : this.entityListeners) {
      cons.accept(listener);
    }
  }

  public void unload() {
    if (!this.loaded) {
      return;
    }

    // unregister all updatable entities from the current environment
    for (final IEntity entity : this.getEntities()) {
      this.unload(entity);
    }

    if (Game.screens() != null && Game.window().getRenderComponent() != null && Game.hasStarted()) {
      Game.window().getRenderComponent().setBackground(RenderComponent.DEFAULT_BACKGROUND_COLOR);
    }

    this.loaded = false;
    this.fireEvent(l -> l.unloaded(this));
  }

  public Collection<IEntity> load(final IMapObject mapObject) {
    if (mapObject == null) {
      return new ArrayList<>();
    }
    IMapObjectLoader loader = null;
    if (mapObject.getType() == null || mapObject.getType().isEmpty()) {
      // this makes it possible to register custom MapObjectLoaders that can handle a MapObject without a type specified
      // by default, the engine doesn't provide such a loader (because it's not clear what Entity the MapObject should be mapped to)
      // it might be useful for some games to do some custom handling e.g. for polygon, ellipse, polyline or point MapObjects.
      loader = mapObjectLoaders.getOrDefault(MapObjectType.UNDEFINED_MAPOBJECTTYPE, null);
    } else {
      loader = mapObjectLoaders.get(mapObject.getType());
    }

    if (loader != null) {
      Collection<IEntity> loadedEntities;
      try {
        loadedEntities = loader.load(this, mapObject);
      } catch (MapObjectException e) {
        log.log(Level.WARNING, "map object " + mapObject.getId() + " failed to load", e);
        return new ArrayList<>();
      }
      for (IEntity entity : loadedEntities) {
        if (entity != null) {

          // only add the entity to be rendered with it's layer if its RenderType equals the layer's RenderType
          if (mapObject.getLayer() != null && entity.renderWithLayer()) {
            this.addEntity(entity);
            this.layerEntities.computeIfAbsent(mapObject.getLayer(), m -> new CopyOnWriteArrayList<>()).add(entity);
          } else {
            this.add(entity);
          }
        }
      }

      return loadedEntities;
    }

    return new ArrayList<>();
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

  private void render(Graphics2D g, RenderType renderType) {
    long renderStart = System.nanoTime();

    // 1. Render map layers
    if (this.getMap() != null) {
      MapRenderer.render(g, this.getMap(), Game.world().camera().getViewport(), this, renderType);
    }

    // 2. Render renderables
    for (final IRenderable rend : this.getRenderables(renderType)) {
      rend.render(g);
    }

    // 3. Render entities
    Game.graphics().renderEntities(g, this.miscEntities.get(renderType).values(), renderType == RenderType.NORMAL);

    // 4. fire event
    this.fireRenderEvent(g, renderType);

    if (Game.config().debug().trackRenderTimes()) {
      final double renderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
      Game.metrics().trackRenderTime(renderType.toString().toLowerCase(), renderTime,
          new GameMetrics.RenderInfo("layers", this.getMap().getRenderLayers().stream().filter(m -> m.getRenderType() == renderType).count()),
          new GameMetrics.RenderInfo("renderables", this.getRenderables(renderType).size()),
          new GameMetrics.RenderInfo("entities", this.miscEntities.get(renderType).size()));
    }
  }

  public void renderLayer(Graphics2D g, IMapObjectLayer layer) {
    List<IEntity> entities = this.layerEntities.get(layer);
    if (entities != null) {
      Game.graphics().renderEntities(g, entities, layer.getRenderType() == RenderType.NORMAL);
    }
  }

  private void addAmbientLight() {
    final Color ambientColor = this.getMap().getColorValue(MapProperty.AMBIENTCOLOR, AmbientLight.DEFAULT_COLOR);
    this.ambientLight = new AmbientLight(this, ambientColor);
  }

  private void addStaticShadows() {
    final Color color = this.getMap().getColorValue(MapProperty.SHADOWCOLOR, StaticShadow.DEFAULT_COLOR);
    this.staticShadowLayer = new StaticShadowLayer(this, color);
  }

  public Collection<Integer> getAllMapIDs() {
    return this.allEntities.keySet();
  }

  private static void dispose(final Collection<? extends IEntity> entities) {
    for (final IEntity entity : entities) {
      if (entity instanceof IUpdateable) {
        Game.loop().detach((IUpdateable) entity);
      }

      entity.detachControllers();
    }
  }

  /**
   * Loads the specified entity by performing the following steps:
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
    // an entity can only exist on one environment at a time, so remove it from the current one
    if (entity.getEnvironment() != null) {
      entity.getEnvironment().remove(entity);
    }

    // 1. add to physics engine
    loadPhysicsEntity(entity);

    // 2. register for update or activate
    loadUpdatableOrEmitterEntity(entity);

    // 3. if a gravity is defined, add a gravity force to the entity
    if (entity instanceof IMobileEntity && this.getGravity() != 0) {
      this.addGravityForce((IMobileEntity) entity);
    }

    // 4. attach all controllers
    entity.attachControllers();

    if (this.loaded && (entity instanceof LightSource || entity instanceof StaticShadow)) {
      this.updateLighting(entity);
    }

    entity.loaded(this);
  }

  private void addGravityForce(IMobileEntity entity) {
    IMovementController mvmtControl = entity.getMovementController();
    if (mvmtControl != null) {
      GravityForce force = new GravityForce(entity, this.getGravity(), Direction.DOWN);
      force.setIdentifier(GRAVITY_IDENTIFIER);
      entity.getMovementController().apply(force);
      this.gravityForces.put(entity.getMapId(), force);
    }

  }

  private void removeGravity(IMobileEntity entity) {
    if (this.gravityForces.containsKey(entity.getMapId())) {
      this.gravityForces.get(entity.getMapId()).end();
    }
  }

  private static void loadPhysicsEntity(IEntity entity) {
    if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      if (coll.hasCollision()) {
        Game.physics().add(coll);
      }
    }
  }

  private static void loadUpdatableOrEmitterEntity(IEntity entity) {
    if (entity instanceof Emitter) {
      final Emitter emitter = (Emitter) entity;
      if (emitter.isActivateOnInit()) {
        emitter.activate();
      }
    } else if (entity instanceof IUpdateable) {
      Game.loop().attach((IUpdateable) entity);
    }
  }

  private void loadMapObjects() {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        this.load(mapObject);
      }
    }
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
    if (entity instanceof ICollisionEntity) {
      Game.physics().remove((ICollisionEntity) entity);
    }

    // 2. unregister from update
    if (entity instanceof IUpdateable) {
      Game.loop().detach((IUpdateable) entity);
    }

    if (entity instanceof IMobileEntity) {
      this.removeGravity((IMobileEntity) entity);
    }

    // 3. detach all controllers
    entity.detachControllers();

    if (entity instanceof Emitter) {
      Emitter em = (Emitter) entity;
      em.deactivate();
    }

    if (this.loaded && (entity instanceof LightSource || entity instanceof StaticShadow)) {
      this.updateLighting(entity);
    }

    entity.removed(this);
  }
}