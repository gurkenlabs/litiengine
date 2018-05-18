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
import de.gurkenlabs.litiengine.environment.tilemap.IMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.Spawnpoint;
import de.gurkenlabs.litiengine.environment.tilemap.TmxMapLoader;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
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

public class Environment implements IEnvironment {
  private static final Logger log = Logger.getLogger(Environment.class.getName());
  private static final Map<String, IMapObjectLoader> mapObjectLoaders;

  private final Map<Integer, ICombatEntity> combatEntities;
  private final Map<Integer, IMobileEntity> mobileEntities;
  private final Map<RenderType, Map<Integer, IEntity>> entities;
  private final Map<String, List<IEntity>> entitiesByTag;

  private final List<EnvironmentRenderListener> renderListeners;
  private final List<EnvironmentListener> listeners;
  private final List<EnvironmentEntityListener> entityListeners;

  private final Collection<IRenderable> groundRenderable;
  private final Collection<IRenderable> overlayRenderable;
  private final Collection<IRenderable> uiRenderable;
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
    this.entities.put(RenderType.UI, new ConcurrentHashMap<>());

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

    this.groundRenderable = Collections.newSetFromMap(new ConcurrentHashMap<IRenderable, Boolean>());
    this.overlayRenderable = Collections.newSetFromMap(new ConcurrentHashMap<IRenderable, Boolean>());
    this.uiRenderable = Collections.newSetFromMap(new ConcurrentHashMap<IRenderable, Boolean>());

    this.renderListeners = new CopyOnWriteArrayList<>();
    this.listeners = new CopyOnWriteArrayList<>();
    this.entityListeners = new CopyOnWriteArrayList<>();
  }

  @Override
  public void addRenderListener(EnvironmentRenderListener listener) {
    this.renderListeners.add(listener);
  }

  @Override
  public void removeRenderListener(EnvironmentRenderListener listener) {
    this.renderListeners.remove(listener);
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
      this.getGroundRenderables().add(emitter.getGroundRenderable());
      this.getOverlayRenderables().add(emitter.getOverlayRenderable());
      this.emitters.add(emitter);
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

  private void updateColorLayers(IEntity entity) {
    if (this.staticShadowLayer != null) {
      this.staticShadowLayer.updateSection(entity.getBoundingBox());
    }

    if (this.ambientLight != null) {
      this.ambientLight.updateSection(entity.getBoundingBox());
    }
  }

  @Override
  public void addToGround(IRenderable renderable) {
    this.getGroundRenderables().add(renderable);
  }

  @Override
  public void addToOverlay(IRenderable renderable) {
    this.getOverlayRenderables().add(renderable);
  }

  @Override
  public void addToUI(IRenderable renderable) {
    this.getUIRenderables().add(renderable);
  }

  @Override
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

    this.entities.get(RenderType.NONE).clear();
    this.entities.get(RenderType.GROUND).clear();
    this.entities.get(RenderType.NORMAL).clear();
    this.entities.get(RenderType.OVERLAY).clear();
    this.initialized = false;

    this.fireEvent(l -> l.environmentCleared(this));
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
    for (RenderType type : RenderType.values()) {
      IEntity entity = this.entities.get(type).get(mapId);
      if (entity != null) {
        return entity;
      }
    }

    return null;
  }

  @Override
  public <T extends IEntity> T get(Class<T> clss, int mapId) {
    IEntity ent = this.get(mapId);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return (T) ent;
  }

  @Override
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

  @Override
  public <T extends IEntity> T get(Class<T> clss, String name) {
    IEntity ent = this.get(name);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return (T) ent;
  }

  @Override
  public <T extends IEntity> Collection<T> getByTag(String... tags) {
    return this.getByTag(null, tags);
  }

  @Override
  public <T extends IEntity> Collection<T> getByTag(Class<T> clss, String... tags) {
    List<T> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();
      if (!this.getEntitiesByTag().containsKey(tag)) {
        continue;
      }
      for (IEntity ent : this.getEntitiesByTag().get(tag)) {
        if ((clss == null || clss.isInstance(ent)) && !foundEntities.contains((T) ent)) {
          foundEntities.add((T) ent);
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

  public Map<String, List<IEntity>> getEntitiesByTag() {
    return this.entitiesByTag;
  }

  @Override
  public <T extends IEntity> Collection<T> getByType(Class<T> cls) {
    List<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.getEntities()) {
      if (cls.isInstance(ent)) {
        foundEntities.add((T) ent);
      }
    }

    return foundEntities;
  }

  @Override
  public Collection<IRenderable> getGroundRenderables() {
    return this.groundRenderable;
  }

  @Override
  public Collection<IRenderable> getUIRenderables() {
    return this.uiRenderable;
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
    return ++mapIdSequence;
  }

  @Override
  public Collection<IRenderable> getOverlayRenderables() {
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
  public List<String> getUsedTags() {
    final List<String> tags = this.getEntitiesByTag().keySet().stream().collect(Collectors.toList());
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

    Game.getPhysicsEngine().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
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
    this.getGroundRenderables().remove(renderable);
    this.getOverlayRenderables().remove(renderable);
    this.getUIRenderables().remove(renderable);
  }

  @Override
  public void render(final Graphics2D g) {
    g.scale(Game.getCamera().getRenderScale(), Game.getCamera().getRenderScale());

    long renderStart = System.nanoTime();

    Game.getRenderEngine().renderMap(g, this.getMap());

    this.fireRenderEvent(l -> l.mapRendered(g));

    final double mapRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    for (final IRenderable rend : this.getGroundRenderables()) {
      rend.render(g);
    }

    this.fireRenderEvent(l -> l.groundRendered(g));

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.GROUND).values(), false);

    final double groundRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    if (Game.getConfiguration().graphics().getGraphicQuality() == Quality.VERYHIGH) {
      Game.getRenderEngine().renderEntities(g, this.getLightSources(), false);
    }

    final double lightRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.NORMAL).values());
    this.fireRenderEvent(l -> l.entitiesRendered(g));

    final double normalRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    if (this.getStaticShadows().stream().anyMatch(x -> x.getShadowType() != StaticShadowType.NONE)) {
      this.getStaticShadowLayer().render(g);
    }

    final double staticShadowRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    Game.getRenderEngine().renderLayers(g, this.getMap(), RenderType.OVERLAY);
    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.OVERLAY).values(), false);
    for (final IRenderable rend : this.getOverlayRenderables()) {
      rend.render(g);
    }

    this.fireRenderEvent(l -> l.overlayRendered(g));

    final double overlayRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    if (Game.getConfiguration().graphics().getGraphicQuality().ordinal() >= Quality.MEDIUM.ordinal() && this.getAmbientLight() != null && this.getAmbientLight().getAlpha() != 0) {
      this.getAmbientLight().render(g);
    }

    final double ambientLightRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);
    renderStart = System.nanoTime();

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.UI).values(), false);
    for (final IRenderable rend : this.getUIRenderables()) {
      rend.render(g);
    }

    this.fireRenderEvent(l -> l.uiRendered(g));
    final double uiRenderTime = TimeUtilities.nanoToMs(System.nanoTime() - renderStart);

    if (Game.getConfiguration().debug().isLogDetailedRenderTimes()) {
      log.log(Level.INFO, "render details:\n 1. map:{0}ms\n 2. ground:{1}ms\n 3. light:{2}ms\n 4. entities({8}):{3}ms\n 5. shadows:{4}ms\n 6. overlay({9} + {10}):{5}ms\n 7. ambientLight:{6}ms\n 8. ui:{7}ms",
          new Object[] {
              mapRenderTime,
              groundRenderTime,
              lightRenderTime,
              normalRenderTime,
              staticShadowRenderTime,
              overlayRenderTime,
              ambientLightRenderTime,
              uiRenderTime,
              this.getEntities(RenderType.NORMAL).size(),
              this.getEntities(RenderType.OVERLAY).size(),
              this.getOverlayRenderables().size(),
          });
    }
    g.scale(1.0 / Game.getCamera().getRenderScale(), 1.0 / Game.getCamera().getRenderScale());
  }

  private void fireEvent(Consumer<EnvironmentListener> cons) {
    for (EnvironmentListener listener : this.listeners) {
      cons.accept(listener);
    }
  }

  private void fireRenderEvent(Consumer<EnvironmentRenderListener> cons) {
    for (EnvironmentRenderListener listener : this.renderListeners) {
      cons.accept(listener);
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

    this.loaded = false;
    this.fireEvent(l -> l.environmentUnloaded(this));
  }

  @Override
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