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
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.MapArea;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Spawnpoint;
import de.gurkenlabs.litiengine.entities.StaticShadow;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
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
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;
import de.gurkenlabs.litiengine.util.io.FileUtilities;

public class Environment implements IEnvironment {
  private static final Logger log = Logger.getLogger(Environment.class.getName());
  private static final Map<String, IMapObjectLoader> mapObjectLoaders = new ConcurrentHashMap<>();

  private final Map<Integer, ICombatEntity> combatEntities = new ConcurrentHashMap<>();
  private final Map<Integer, IMobileEntity> mobileEntities = new ConcurrentHashMap<>();
  private final Map<RenderType, Map<Integer, IEntity>> entities = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Map<String, Collection<IEntity>> entitiesByTag = new ConcurrentHashMap<>();

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
    }
  }

  public Environment(final String mapPath) {
    this();
    this.map = Resources.maps().get(FileUtilities.getFileName(mapPath));
    if (this.getMap() != null) {
      Game.physics().setBounds(this.getMap().getBounds());
    }
  }

  private Environment() {
    for (RenderType renderType : RenderType.values()) {
      this.entities.put(renderType, new ConcurrentHashMap<>());
      this.renderListeners.put(renderType, ConcurrentHashMap.newKeySet());
      this.renderables.put(renderType, ConcurrentHashMap.newKeySet());
    }
  }

  @Override
  public void addRenderListener(RenderType renderType, EnvironmentRenderListener listener) {
    this.renderListeners.get(renderType).add(listener);
  }

  @Override
  public void removeRenderListener(EnvironmentRenderListener listener) {
    for (Collection<EnvironmentRenderListener> rends : this.renderListeners.values()) {
      rends.remove(listener);
    }
  }

  @Override
  public void addListener(EnvironmentListener listener) {
    this.listeners.add(listener);
  }

  @Override
  public void removeListener(EnvironmentListener listener) {
    this.listeners.remove(listener);
  }

  @Override
  public void addEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.add(listener);
  }

  @Override
  public void removeEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.remove(listener);
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

  @Override
  public void add(IRenderable renderable, RenderType renderType) {
    this.getRenderables(renderType).add(renderable);
  }

  @Override
  public Collection<IEntity> build(Blueprint blueprint, double x, double y) {
    return this.build(blueprint, new Point2D.Double(x, y));
  }

  @Override
  public Collection<IEntity> build(Blueprint blueprint, Point2D location) {
    Collection<IMapObject> mapObjects = blueprint.build(location);
    Collection<IEntity> loadedEntities = new ArrayList<>();
    for (IMapObject obj : mapObjects) {
      loadedEntities.addAll(this.load(obj));
    }

    return loadedEntities;
  }

  @Override
  public void clear() {
    Game.physics().clear();
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

  @Override
  public Collection<ICombatEntity> findCombatEntities(final Shape shape) {
    return this.findCombatEntities(shape, entity -> true);
  }

  @Override
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

  @Override
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

  @Override
  public IEntity get(final int mapId) {
    for (Map<Integer, IEntity> type : this.entities.values()) {
      IEntity entity = type.get(mapId);
      if (entity != null) {
        return entity;
      }
    }

    return null;
  }

  @Override
  public List<IEntity> get(final int... mapIds) {
    final List<IEntity> foundEntities = new ArrayList<>();
    if (mapIds == null) {
      return foundEntities;
    }

    for (Map<Integer, IEntity> type : this.entities.values()) {
      for (int id : mapIds) {
        IEntity entity = type.get(id);
        if (entity != null) {
          foundEntities.add(entity);
        }
      }

    }
    return foundEntities;
  }

  @Override
  public <T extends IEntity> T get(Class<T> clss, int mapId) {
    IEntity ent = this.get(mapId);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  @Override
  public IEntity get(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (Map<Integer, IEntity> type : this.entities.values()) {
      for (final IEntity entity : type.values()) {
        if (entity.getName() != null && entity.getName().equals(name)) {
          return entity;
        }
      }
    }

    return null;
  }

  @Override
  public <T extends IEntity> T get(Class<T> clss, String name) {
    IEntity ent = this.get(name);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  @Override
  public Collection<IEntity> getByTag(String... tags) {
    Collection<IEntity> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();
      foundEntities.addAll(this.getEntitiesByTag().getOrDefault(tag, Arrays.asList()));
    }

    return foundEntities;
  }

  @Override
  public <T> Collection<T> getByTag(Class<? extends T> clss, String... tags) {
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
  public Point2D getCenter() {
    return new Point2D.Double(this.getMap().getSizeInPixels().getWidth() / 2.0, this.getMap().getSizeInPixels().getHeight() / 2.0);
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
    for (Map<Integer, IEntity> type : this.entities.values()) {
      ent.addAll(type.values());
    }

    return ent;
  }

  @Override
  public Collection<IEntity> getEntities(final RenderType renderType) {
    return this.entities.get(renderType).values();
  }

  public Map<String, Collection<IEntity>> getEntitiesByTag() {
    return this.entitiesByTag;
  }

  @Override
  public <T> Collection<T> getByType(Class<? extends T> cls) {
    Collection<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.getEntities()) {
      if (cls.isInstance(ent)) {
        foundEntities.add(cls.cast(ent));
      }
    }

    return foundEntities;
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
  public Collection<IMobileEntity> getMobileEntities() {
    return this.mobileEntities.values();
  }

  @Override
  public IMobileEntity getMobileEntity(final int mapId) {
    return getById(this.getMobileEntities(), mapId);
  }

  @Override
  public IMobileEntity getMobileEntity(String name) {
    return getByName(this.getMobileEntities(), name);
  }

  @Override
  public synchronized int getNextMapId() {
    int maxMapID = MapUtilities.getMaxMapId(this.getMap());
    return ++maxMapID;
  }

  @Override
  public Collection<IRenderable> getRenderables(RenderType renderType) {
    return this.renderables.get(renderType);
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
  public Creature getCreature(int mapId) {
    return getById(this.getCreatures(), mapId);
  }

  @Override
  public Creature getCreature(String name) {
    return getByName(this.getCreatures(), name);
  }

  @Override
  public Collection<Creature> getCreatures() {
    return this.creatures;
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
  public Collection<Spawnpoint> getSpawnPoints() {
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
  public Collection<String> getUsedTags() {
    return new ArrayList<>(this.getEntitiesByTag().keySet());
  }

  @Override
  public final void init() {
    if (this.initialized) {
      return;
    }

    if (this.getMap() != null) {
      this.loadMapObjects();
      this.addStaticShadows();
      this.addAmbientLight();
    }

    this.fireEvent(l -> l.environmentInitialized(this));
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

    if (this.getMap() != null) {
      Game.physics().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
    }

    if (this.getMap() != null) {
      if (this.getMap().getBackgroundColor() != null) {
        Game.window().getRenderComponent().setBackground(this.getMap().getBackgroundColor());
      }
    } else {
      Game.window().getRenderComponent().setBackground(Color.BLACK);
    }

    for (final IEntity entity : this.getEntities()) {
      this.load(entity);
    }

    this.loaded = true;
    this.fireEvent(l -> l.environmentLoaded(this));
  }

  @Override
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

  @Override
  public void remove(final int mapId) {
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  @Override
  public void remove(String name) {
    final IEntity ent = this.get(name);
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
    for (Collection<IRenderable> rends : this.renderables.values()) {
      rends.remove(renderable);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());

    StringBuilder renderDetails = new StringBuilder();
    long renderStart = System.nanoTime();

    renderDetails.append(this.render(g, RenderType.BACKGROUND));

    renderDetails.append(this.render(g, RenderType.GROUND));
    if (Game.config().debug().isDebug()) {
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
    if (Game.config().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getColor().getAlpha() != 0) {
      this.getAmbientLight().render(g);
    }

    final double ambientTime = TimeUtilities.nanoToMs(System.nanoTime() - ambientStart);

    renderDetails.append(this.render(g, RenderType.UI));

    if (Game.config().debug().isLogDetailedRenderTimes()) {
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

  @Override
  public void unload() {
    if (!this.loaded) {
      return;
    }

    // unregister all updatable entities from the current environment
    for (final IEntity entity : this.getEntities()) {
      this.unload(entity);
    }

    if (Game.screens() != null && Game.window().getRenderComponent() != null) {
      Game.window().getRenderComponent().setBackground(RenderComponent.DEFAULT_BACKGROUND_COLOR);
    }

    this.loaded = false;
    this.fireEvent(l -> l.environmentUnloaded(this));
  }

  @Override
  public Collection<IEntity> load(final IMapObject mapObject) {
    IMapObjectLoader loader = mapObjectLoaders.get(mapObject.getType());
    if (loader != null) {
      Collection<IEntity> loadedEntities;
      try {
        loadedEntities = loader.load(this, mapObject);
      } catch (MapObjectException e) {
        return new ArrayList<>();
      }
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
    Game.graphics().render(g, this.getMap(), renderType);

    // 2. Render renderables
    for (final IRenderable rend : this.getRenderables(renderType)) {
      rend.render(g);
    }

    // 3. Render entities
    Game.graphics().renderEntities(g, this.entities.get(renderType).values(), renderType == RenderType.NORMAL);

    // 4. fire event
    this.fireRenderEvent(g, renderType);

    if (Game.config().debug().isLogDetailedRenderTimes()) {
      final double renderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
      return "\t" + renderType + ": " + renderTime + "ms ("
          + this.getMap().getRenderLayers().stream().filter(m -> m.getRenderType() == renderType).count() + " layers, "
          + this.getRenderables(renderType).size() + " renderables, "
          + this.entities.get(renderType).size() + " entities)\n";
    }

    return null;
  }

  private void addAmbientLight() {
    final Color ambientColor = this.getMap().getColorValue(MapProperty.AMBIENTCOLOR, AmbientLight.DEFAULT_COLOR);
    this.ambientLight = new AmbientLight(this, ambientColor);
  }

  private void addStaticShadows() {
    final Color color = this.getMap().getColorValue(MapProperty.SHADOWCOLOR, StaticShadow.DEFAULT_COLOR);
    this.staticShadowLayer = new StaticShadowLayer(this, color);
  }

  private void dispose(final Collection<? extends IEntity> entities) {
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
    // 1. add to physics engine
    this.loadPhysicsEntity(entity);

    // 2. register for update or activate
    this.loadUpdatableOrEmitterEntity(entity);

    // 3. attach all controllers
    entity.attachControllers();

    if (entity instanceof LightSource || entity instanceof StaticShadow) {
      this.updateColorLayers(entity);
    }

    entity.loaded();
  }

  private void loadPhysicsEntity(IEntity entity) {
    if (entity instanceof CollisionBox) {
      final CollisionBox coll = (CollisionBox) entity;
      if (coll.isObstacle()) {
        Game.physics().add(coll.getBoundingBox());
      } else {
        Game.physics().add(coll);
      }
    } else if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      if (coll.hasCollision()) {
        Game.physics().add(coll);
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
      Game.loop().attach((IUpdateable) entity);
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
        Game.physics().remove(coll.getBoundingBox());
      } else {
        Game.physics().remove(coll);
      }
    } else if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      Game.physics().remove(coll);
    }

    // 2. unregister from update
    if (entity instanceof IUpdateable) {
      Game.loop().detach((IUpdateable) entity);
    }

    // 3. detach all controllers
    entity.detachControllers();

    if (entity instanceof Emitter) {
      Emitter em = (Emitter) entity;
      em.deactivate();
    }

    entity.removed();
  }
}