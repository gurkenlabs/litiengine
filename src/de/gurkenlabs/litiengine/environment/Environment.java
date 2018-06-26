package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.annotation.EntityInfo;
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMobileEntity;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadow;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public class Environment {
  private static final Logger log = Logger.getLogger(Environment.class.getName());
  private static final Map<String, IMapObjectLoader> mapObjectLoaders;

  private final Map<Integer, ICombatEntity> combatEntities;
  private final Map<Integer, IMobileEntity> mobileEntities;
  private final Map<RenderType, Map<Integer, IEntity>> entities;
  private final Map<String, List<IEntity>> entitiesByTag;

  private final Map<RenderType, Collection<EnvironmentRenderListener>> renderListeners;
  private final List<EnvironmentListener> listeners;
  private final List<EnvironmentEntityListener> entityListeners;

  private final Map<RenderType, Collection<IRenderable>> renderables;
  private final Collection<CollisionBox> colliders;
  private final Collection<LightSource> lightSources;
  private final Collection<StaticShadow> staticShadows;
  private final Collection<Trigger> triggers;
  private final Collection<Prop> props;
  private final Collection<Emitter> emitters;
  private final Collection<Creature> creatures;
  private final Collection<Spawnpoint> spawnPoints;
  private final Collection<MapArea> mapAreas;

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
    Game.getPhysicsEngine().setBounds(this.getMap().getBounds());
  }

  public Environment(final String mapPath) {
    this();
    final IMap loadedMap = Game.getMap(FileUtilities.getFileName(mapPath));
    if (loadedMap == null) {
      this.map = MapLoader.load(mapPath);
    } else {
      this.map = loadedMap;
    }

    this.mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  private Environment() {
    this.entitiesByTag = new ConcurrentHashMap<>();
    this.entities = Collections.synchronizedMap(new EnumMap<>(RenderType.class));

    this.combatEntities = new ConcurrentHashMap<>();
    this.mobileEntities = new ConcurrentHashMap<>();

    this.lightSources = Collections.newSetFromMap(new ConcurrentHashMap<LightSource, Boolean>());
    this.colliders = Collections.newSetFromMap(new ConcurrentHashMap<CollisionBox, Boolean>());
    this.triggers = Collections.newSetFromMap(new ConcurrentHashMap<Trigger, Boolean>());
    this.mapAreas = Collections.newSetFromMap(new ConcurrentHashMap<MapArea, Boolean>());
    this.staticShadows = Collections.newSetFromMap(new ConcurrentHashMap<StaticShadow, Boolean>());
    this.props = Collections.newSetFromMap(new ConcurrentHashMap<Prop, Boolean>());
    this.emitters = Collections.newSetFromMap(new ConcurrentHashMap<Emitter, Boolean>());
    this.creatures = Collections.newSetFromMap(new ConcurrentHashMap<Creature, Boolean>());
    this.spawnPoints = Collections.newSetFromMap(new ConcurrentHashMap<Spawnpoint, Boolean>());

    this.renderables = Collections.synchronizedMap(new EnumMap<>(RenderType.class));

    this.renderListeners = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
    this.listeners = new CopyOnWriteArrayList<>();
    this.entityListeners = new CopyOnWriteArrayList<>();

    for (RenderType renderType : RenderType.values()) {
      this.entities.put(renderType, new ConcurrentHashMap<>());
      this.renderListeners.put(renderType, Collections.newSetFromMap(new ConcurrentHashMap<EnvironmentRenderListener, Boolean>()));
      this.renderables.put(renderType, Collections.newSetFromMap(new ConcurrentHashMap<IRenderable, Boolean>()));
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
   * already been loaded.
   *
   * @param entity
   *          The entity to add to the environment.
   */
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

      if (this.getEntitiesByTag().containsKey(tag)) {
        this.getEntitiesByTag().get(tag).add(entity);
        continue;
      }

      this.getEntitiesByTag().put(tag, new CopyOnWriteArrayList<>());
      this.getEntitiesByTag().get(tag).add(entity);
    }

    // if the environment has already been loaded,
    // we need to load the new entity manually
    if (this.loaded) {
      this.load(entity);
    }

    this.entities.get(entity.getRenderType()).put(entity.getMapId(), entity);

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

  private void updateColorLayers(IEntity entity) {
    if (this.staticShadowLayer != null) {
      this.staticShadowLayer.updateSection(entity.getBoundingBox());
    }

    if (this.ambientLight != null) {
      this.ambientLight.updateSection(entity.getBoundingBox());
    }
  }

  public void add(IRenderable renderable, RenderType renderType) {
    this.getRenderables(renderType).add(renderable);
  }

  public void clear() {
    Game.getPhysicsEngine().clear();
    this.dispose(this.getEntities());
    this.dispose(this.getTriggers());
    this.getCombatEntities().clear();
    this.getMobileEntities().clear();
    this.getLightSources().clear();
    this.getCollisionBoxes().clear();
    this.getSpawnPoints().clear();
    this.getAreas().clear();
    this.getTriggers().clear();
    this.getEntitiesByTag().clear();

    for (Map<Integer, IEntity> type : this.entities.values()) {
      type.clear();
    }

    this.initialized = false;

    this.fireEvent(l -> l.environmentCleared(this));
  }

  public List<ICombatEntity> findCombatEntities(final Shape shape) {
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

  public IEntity get(final int mapId) {
    for (RenderType type : RenderType.values()) {
      IEntity entity = this.entities.get(type).get(mapId);
      if (entity != null) {
        return entity;
      }
    }

    return null;
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

    for (RenderType type : RenderType.values()) {
      for (final IEntity entity : this.entities.get(type).values()) {
        if (entity.getName() != null && entity.getName().equals(name)) {
          return entity;
        }
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

  public <T extends IEntity> Collection<T> getByTag(String... tags) {
    return this.getByTag(null, tags);
  }

  public <T extends IEntity> Collection<T> getByTag(Class<T> clss, String... tags) {
    List<T> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();
      if (!this.getEntitiesByTag().containsKey(tag)) {
        continue;
      }
      for (IEntity ent : this.getEntitiesByTag().get(tag)) {
        if ((clss == null || clss.isInstance(ent)) && !foundEntities.contains(ent)) {
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
    final ArrayList<IEntity> ent = new ArrayList<>();
    for (Map<Integer, IEntity> type : this.entities.values()) {
      ent.addAll(type.values());
    }

    return ent;
  }

  public Collection<IEntity> getEntities(final RenderType renderType) {
    return this.entities.get(renderType).values();
  }

  public Map<String, List<IEntity>> getEntitiesByTag() {
    return this.entitiesByTag;
  }

  public <T extends IEntity> Collection<T> getByType(Class<T> cls) {
    List<T> foundEntities = new ArrayList<>();
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
    return ++mapIdSequence;
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

  public List<String> getUsedTags() {
    final List<String> tags = this.getEntitiesByTag().keySet().stream().collect(Collectors.toList());
    Collections.sort(tags);

    return tags;
  }

  public final void init() {
    if (this.initialized) {
      return;
    }

    this.loadMapObjects();
    this.addStaticShadows();
    this.addAmbientLight();

    this.fireEvent(l -> l.environmentInitialized(this));
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

    Game.getPhysicsEngine().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
    for (final IEntity entity : this.getEntities()) {
      this.load(entity);
    }

    this.loaded = true;
    this.fireEvent(l -> l.environmentLoaded(this));
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
    CustomMapObjectLoader<T> mapObjectLoader = new CustomMapObjectLoader<>(mapObjectType, entityType);
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
          "Cannot register a custom entity type without the related EntityInfo.customMapObjectType being specified.\n Add an EntityInfo annotation to the " + entityType + " class and provide the required information or use the registerCustomEntityType overload and provide the type explicitly.");
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

    if (this.entities.get(entity.getRenderType()) != null) {
      this.entities.get(entity.getRenderType()).entrySet().removeIf(e -> e.getValue().getMapId() == entity.getMapId());
    }

    for (String tag : entity.getTags()) {
      this.getEntitiesByTag().get(tag).remove(entity);

      if (this.getEntitiesByTag().get(tag).isEmpty()) {
        this.getEntitiesByTag().remove(tag);
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
      this.updateColorLayers(entity);
    }

    if (entity instanceof Trigger) {
      this.triggers.remove(entity);
    }

    if (entity instanceof Spawnpoint) {
      this.spawnPoints.remove(entity);
    }

    if (entity instanceof StaticShadow) {
      this.staticShadows.remove(entity);
      this.updateColorLayers(entity);
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

  public void render(final Graphics2D g) {
    g.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());

    StringBuilder renderDetails = new StringBuilder();
    long renderStart = System.nanoTime();

    renderDetails.append(this.render(g, RenderType.BACKGROUND));

    renderDetails.append(this.render(g, RenderType.GROUND));
    if (Game.getConfiguration().debug().isDebug()) {
      DebugRenderer.renderMapDebugInfo(g, this.getMap());
    }

    renderDetails.append(this.render(g, RenderType.SURFACE));

    renderDetails.append(this.render(g, RenderType.NORMAL));

    long shadowRenderStart = System.nanoTime();
    if (this.getStaticShadows().stream().anyMatch(x -> x.getShadowType() != StaticShadowType.NONE)) {
      this.getStaticShadowLayer().render(g);
    }

    final double shadowTime = TimeUtilities.nanoToMs(System.nanoTime() - shadowRenderStart);

    renderDetails.append(this.render(g, RenderType.OVERLAY));

    long ambientStart = System.nanoTime();
    if (Game.getConfiguration().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getAlpha() != 0) {
      this.getAmbientLight().render(g);
    }

    final double ambientTime = TimeUtilities.nanoToMs(System.nanoTime() - ambientStart);

    renderDetails.append(this.render(g, RenderType.UI));

    if (Game.getConfiguration().debug().isLogDetailedRenderTimes()) {
      final double totalRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
      log.log(Level.INFO, "total render time: {0}ms \n{1} \tSHADOWS: {2}ms \n\tAMBIENT: {3}ms ", new Object[] { totalRenderTime, renderDetails, shadowTime, ambientTime });
    }

    g.scale(1.0 / Game.getCamera().getRenderScale(), 1.0 / Game.getCamera().getRenderScale());
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

    this.loaded = false;
    this.fireEvent(l -> l.environmentUnloaded(this));
  }

  public Collection<IEntity> load(final IMapObject mapObject) {
    if (mapObjectLoaders.containsKey(mapObject.getType())) {
      Collection<IEntity> loadedEntities = mapObjectLoaders.get(mapObject.getType()).load(this, mapObject);
      for (IEntity entity : loadedEntities) {
        if (entity != null) {
          this.add(entity);
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

  private String render(Graphics2D g, RenderType renderType) {
    long renderStart = System.nanoTime();

    // 1. Render map layers
    Game.getRenderEngine().render(g, this.getMap(), renderType);

    // 2. Render renderables
    for (final IRenderable rend : this.getRenderables(renderType)) {
      rend.render(g);
    }

    // 3. Render entities
    Game.getRenderEngine().renderEntities(g, this.entities.get(renderType).values(), renderType == RenderType.NORMAL);

    // 4. fire event
    this.fireRenderEvent(g, renderType);

    if (Game.getConfiguration().debug().isLogDetailedRenderTimes()) {
      final double renderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
      return "\t" + renderType + ": " + renderTime + "ms ("
          + this.getMap().getRenderLayers().stream().filter(m -> m.getRenderType() == renderType).count() + " layers, "
          + this.getRenderables(renderType).size() + " renderables, "
          + this.entities.get(renderType).size() + " entities)\n";
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
    // 1. add to physics engine
    this.loadPhysicsEntity(entity);

    // 2. register for update or activate
    this.loadUpdatableOrEmitterEntity(entity);

    // 3. attach all controllers
    entity.attachControllers();

    if (entity instanceof LightSource || entity instanceof StaticShadow) {
      this.updateColorLayers(entity);
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

        this.load(mapObject);
      }
    }
  }

  private static void registerDefaultMapObjectLoaders() {
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

    // 3. detach all controllers
    entity.detachControllers();

    if (entity instanceof Emitter) {
      Emitter em = (Emitter) entity;
      em.deactivate();
    }
  }
}
