package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.lang.reflect.Modifier;
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
import de.gurkenlabs.litiengine.configuration.Quality;
import de.gurkenlabs.litiengine.entities.CollisionBox;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityInfo;
import de.gurkenlabs.litiengine.entities.EntityListener;
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
import de.gurkenlabs.litiengine.graphics.ColorLayer;
import de.gurkenlabs.litiengine.graphics.DebugRenderer;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.StaticShadowLayer;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.graphics.emitters.Emitter;
import de.gurkenlabs.litiengine.physics.GravityForce;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.util.TimeUtilities;
import de.gurkenlabs.litiengine.util.geom.GeometricUtilities;

public final class Environment implements IRenderable {
  private static final Map<String, IMapObjectLoader> mapObjectLoaders = new ConcurrentHashMap<>();
  private static final String GRAVITY_IDENTIFIER = "GRAVITY";
  private static final Logger log = Logger.getLogger(Environment.class.getName());

  private static int localIdSequence = 0;

  private final Map<Integer, ICombatEntity> combatEntities = new ConcurrentHashMap<>();
  private final Map<Integer, IMobileEntity> mobileEntities = new ConcurrentHashMap<>();
  private final Map<Integer, GravityForce> gravityForces = new ConcurrentHashMap<>();
  private final Map<RenderType, Map<Integer, IEntity>> miscEntities = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Map<IMapObjectLayer, List<IEntity>> layerEntities = new ConcurrentHashMap<>();
  private final Map<String, Collection<IEntity>> entitiesByTag = new ConcurrentHashMap<>();
  private final Map<Integer, IEntity> allEntities = new ConcurrentHashMap<>();

  private final Map<RenderType, Collection<EnvironmentRenderedListener>> renderListeners = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Collection<EnvironmentListener> listeners = ConcurrentHashMap.newKeySet();
  private final Collection<EnvironmentEntityListener> entityListeners = ConcurrentHashMap.newKeySet();

  private final Map<RenderType, Collection<IRenderable>> renderables = Collections.synchronizedMap(new EnumMap<>(RenderType.class));
  private final Collection<Emitter> emitters = ConcurrentHashMap.newKeySet();
  private final Collection<CollisionBox> colliders = ConcurrentHashMap.newKeySet();
  private final Collection<Prop> props = ConcurrentHashMap.newKeySet();
  private final Collection<Creature> creatures = ConcurrentHashMap.newKeySet();
  private final Collection<StaticShadow> staticShadows = ConcurrentHashMap.newKeySet();
  private final Collection<LightSource> lightSources = ConcurrentHashMap.newKeySet();
  private final Collection<Spawnpoint> spawnPoints = ConcurrentHashMap.newKeySet();
  private final Collection<MapArea> mapAreas = ConcurrentHashMap.newKeySet();
  private final Collection<Trigger> triggers = ConcurrentHashMap.newKeySet();

  private AmbientLight ambientLight;
  private StaticShadowLayer staticShadowLayer;
  private boolean loaded;
  private boolean initialized;
  private IMap map;

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

  /**
   * Instantiates a new <code>Environment</code> for the specified map.
   *
   * @param map
   *          The map that defines this environment.
   */
  public Environment(final IMap map) {
    this();
    this.map = map;
    if (this.getMap() != null) {
      Game.physics().setBounds(this.getMap().getBounds());
      this.setGravity(this.getMap().getIntValue(MapProperty.GRAVITY));
    }
  }

  /**
   * Instantiates a new <code>Environment</code> for the specified map.
   *
   * @param mapPath
   *          The path to the map resource that defines this environment.
   */
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
   * <p>
   * Custom entity types need to provide at least one constructor that matches the following criteria:
   * </p>
   * 
   * <ul>
   * <li>has 2 parameters: <code>Environment, IMapObject</code></li>
   * <li>has 2 parameters: <code>IMapObject, Environment</code></li>
   * <li>has 1 parameter: <code>IMapObject</code></li>
   * <li>has 1 parameter: <code>Environment</code></li>
   * <li>is empty constructor</li>
   * </ul>
   * 
   * 
   * @param mapObjectType
   *          The custom mapobjectType that is used by <code>IMapObjects</code> to determine the target entity implementation.
   * @param entityType
   *          The class type of the custom entity implementation.
   * 
   * @see IMapObject#getType()
   * @see EntityInfo#customMapObjectType()
   */
  public static void registerCustomEntityType(String mapObjectType, Class<? extends IEntity> entityType) {
    if (entityType.isInterface() || Modifier.isAbstract(entityType.getModifiers())) {
      log.log(Level.WARNING, "Cannot register the custom entity type [{0}]: Type must not be an interface or abstract class.", entityType.getName());
      return;
    }

    CustomMapObjectLoader.ConstructorInvocation invocation = CustomMapObjectLoader.findConstructor(entityType);
    if (invocation == null) {
      log.log(Level.WARNING, "Cannot register the custom entity type [{0}]: No matching constructor found.", entityType.getName());
      return;
    }

    CustomMapObjectLoader mapObjectLoader = new CustomMapObjectLoader(mapObjectType, invocation);
    registerMapObjectLoader(mapObjectLoader);
  }

  /**
   * Registers a custom <code>IEntity</code> implementation to support being loaded from an <code>IMap</code> instance.
   * Note that the specified class needs to be accessible in a static manner. Inner classes that aren't declared statically are not supported.
   * 
   * This implementation uses the provided <code>EntityInfo.customMapObjectType()</code> to determine for which type the specified class should be
   * used.
   * 
   * @param entityType
   *          The class type of the custom entity implementation.
   * 
   * @see Environment#registerCustomEntityType(String, Class)
   * @see IMapObject#getType()
   * @see EntityInfo#customMapObjectType()
   */
  public static void registerCustomEntityType(Class<? extends IEntity> entityType) {
    EntityInfo info = entityType.getAnnotation(EntityInfo.class);
    if (info == null || info.customMapObjectType().isEmpty()) {
      log.log(Level.WARNING, "Cannot register the custom entity type [{0}]: EntityInfo.customMapObjectType must be specified.\nAdd an EntityInfo annotation to the class and provide the required information or use the registerCustomEntityType overload and provide the type explicitly.",
          entityType.getName());
      return;
    }

    registerCustomEntityType(info.customMapObjectType(), entityType);
  }

  /**
   * Adds the specified environment rendered listener to receive events when this instance renders the specified renderType.
   * 
   * @param renderType
   *          The type that defines to which render process this listener should be attached.
   * @param listener
   *          The listener to add.
   */
  public void onRendered(RenderType renderType, EnvironmentRenderedListener listener) {
    this.renderListeners.get(renderType).add(listener);
  }

  /**
   * Removes the specified environment rendered listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeRenderListener(EnvironmentRenderedListener listener) {
    for (Collection<EnvironmentRenderedListener> rends : this.renderListeners.values()) {
      rends.remove(listener);
    }
  }

  /**
   * Adds the specified environment listener to receive events about the basic environment life-cycle.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addListener(EnvironmentListener listener) {
    this.listeners.add(listener);
  }

  /**
   * Removes the environment listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeListener(EnvironmentListener listener) {
    this.listeners.remove(listener);
  }

  /**
   * Adds the specified environment entity listener to receive events about entities on this environment.
   * 
   * @param listener
   *          The listener to add.
   */
  public void addEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.add(listener);
  }

  /**
   * Removes the environment entity listener listener.
   * 
   * @param listener
   *          The listener to remove.
   */
  public void removeEntityListener(EnvironmentEntityListener listener) {
    this.entityListeners.remove(listener);
  }

  /**
   * Adds the specified entity to the environment container. This also loads the
   * entity (registers entity and controllers for update) if the environment has
   * already been loaded. The entity will not be bound to a layer.
   *
   * @param entity
   *          The entity to add to the environment.
   * 
   * @see #isLoaded()
   * @see IEntity#loaded(Environment)
   * @see EnvironmentEntityListener#entityAdded(IEntity)
   */
  public void add(IEntity entity) {
    if (entity == null) {
      return;
    }
    this.addEntity(entity);
    this.miscEntities.get(entity.getRenderType()).put(entity.getMapId(), entity);
    this.fireEntityEvent(l -> l.entityAdded(entity));
  }

  /**
   * Adds all the specified entities to the environment container.
   * 
   * @param <T>
   *          The type of the entity.
   * @param entities
   *          The entities to be added to the environment.
   * 
   * @see #add(IEntity)
   * @see #addAll(IEntity...)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public <T extends IEntity> void addAll(Iterable<T> entities) {
    if (entities == null) {
      return;
    }

    for (T ent : entities) {
      this.add(ent);
    }
  }

  /**
   * Adds all the specified entities to the environment container.
   * 
   * @param entities
   *          The entities to be added to the environment.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public void addAll(IEntity... entities) {
    this.addAll(Arrays.asList(entities));
  }

  /**
   * Forces an update on the lighting layers for the entire map.
   * 
   * @see #getStaticShadowLayer()
   * @see #getAmbientLight()
   * @see ColorLayer#updateSection(Rectangle2D)
   */
  public void updateLighting() {
    if (this.getMap() != null) {
      this.updateLighting(this.getMap().getBounds());
    }
  }

  /**
   * Forces an update on the lighting layers for the specified section on the map.
   * 
   * @param section
   *          The section for which to update the lighting layers.
   * 
   * @see #getStaticShadowLayer()
   * @see #getAmbientLight()
   * @see ColorLayer#updateSection(Rectangle2D)
   */
  public void updateLighting(Rectangle2D section) {
    if (this.staticShadowLayer != null) {
      this.staticShadowLayer.updateSection(section);
    }

    if (this.ambientLight != null) {
      this.ambientLight.updateSection(section);
    }
  }

  /**
   * Adds the specified instance to be rendered with the defined <code>RenderType</code> whenever the environment's render pipeline is executed.
   * 
   * <p>
   * This method can be used for any custom rendering that is not related to an entity, a GUI component or the map.
   * </p>
   * 
   * <p>
   * Note that you don't need to explicitly add an <code>Entity</code> if it implements <code>IRenderable</code>. The render engine will inherently
   * call an entity's render method.
   * </p>
   * 
   * @param renderable
   *          The instance that should be rendered.
   * @param renderType
   *          The render type that determines how the instance is processed by the environment's render pipeline.
   * 
   * @see #render(Graphics2D)
   * @see RenderEngine#renderEntity(Graphics2D, IEntity)
   */
  public void add(IRenderable renderable, RenderType renderType) {
    this.renderables.get(renderType).add(renderable);
  }

  /**
   * Adds entities by the specified blueprint to this environment at the defined location.
   * 
   * @param blueprint
   *          The blueprint, defining the map object to load the entities from.
   * @param x
   *          The x-coordinate of the location at which to spawn the entities.
   * @param y
   *          The y-coordinate of the location at which to spawn the entities.
   * 
   * @return A collection with all added entities.
   */
  public Collection<IEntity> build(Blueprint blueprint, double x, double y) {
    return this.build(blueprint, new Point2D.Double(x, y));
  }

  /**
   * Adds entities by the specified blueprint to this environment at the defined location.
   * 
   * @param blueprint
   *          The blueprint, defining the map object to load the entities from.
   * @param location
   *          The location at which to spawn the entities.
   * 
   * @return A collection with all added entities.
   */
  public Collection<IEntity> build(Blueprint blueprint, Point2D location) {
    Collection<IMapObject> mapObjects = blueprint.build(location);
    Collection<IEntity> loadedEntities = new ArrayList<>();
    for (IMapObject obj : mapObjects) {
      loadedEntities.addAll(this.load(obj));
    }

    return loadedEntities;
  }

  /**
   * Clears all loaded entities and renderable instances from this environment.
   */
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

    dispose(this.allEntities.values());
    dispose(this.triggers);
    this.emitters.clear();
    this.colliders.clear();
    this.props.clear();
    this.creatures.clear();
    this.staticShadows.clear();
    this.combatEntities.clear();
    this.mobileEntities.clear();
    this.lightSources.clear();
    this.spawnPoints.clear();
    this.mapAreas.clear();
    this.triggers.clear();

    this.ambientLight = null;
    this.staticShadowLayer = null;

    for (Map<Integer, IEntity> type : this.miscEntities.values()) {
      type.clear();
    }

    this.initialized = false;

    this.fireEvent(l -> l.cleared(this));
  }

  /**
   * Determines whether the environment contains the specified entity.
   * 
   * @param entity
   *          The entity to check for.
   * 
   * @return True if the environment contains the specified entity; otherwise false.
   */
  public boolean contains(IEntity entity) {
    return this.contains(entity.getMapId());
  }

  /**
   * Determines whether the environment contains any entity with the specified map ID.
   * 
   * @param mapId
   *          The map ID of the entity to check for.
   * 
   * @return True if the environment contains an entity with the specified map ID; otherwise false.
   */
  public boolean contains(int mapId) {
    return this.allEntities.containsKey(mapId);
  }

  /**
   * Attempts to find all combat entities whose hitBox intersects with the specified shape.
   * 
   * @param shape
   *          The shape to check intersection for.
   * 
   * @return A collection of all combat entities that intersect the specified {@link Shape}.
   */
  public Collection<ICombatEntity> findCombatEntities(final Shape shape) {
    return this.findCombatEntities(shape, entity -> true);
  }

  /**
   * Attempts to find all combat entities whose hitBox intersects with the specified shape.
   * 
   * @param shape
   *          The shape to check intersection for.
   * @param condition
   *          An additional condition that allows to specify a condition which
   *          determines if a {@link ICombatEntity} should be considered.
   * @return A collection of all combat entities that intersect the specified {@link Shape}.
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

  /**
   * Attempts to find all entities whose bounding box intersects with the specified shape.
   * 
   * @param shape
   *          The shape to check intersection for.
   * @return A collection of all entities that intersect the specified {@link Shape}.
   */
  public Collection<IEntity> findEntities(final Shape shape) {
    final Collection<IEntity> foundEntities = new ArrayList<>();
    if (shape == null) {
      return foundEntities;
    }
    if (shape instanceof Rectangle2D) {
      final Rectangle2D rect = (Rectangle2D) shape;
      for (final IEntity entity : this.allEntities.values()) {
        if (entity.getBoundingBox().intersects(rect)) {
          foundEntities.add(entity);
        }
      }
      return foundEntities;
    }
    // for other shapes, we check if the shape's bounds intersect the hitbox
    // and then we then check if the actual shape intersects the hitbox
    for (final IEntity entity : this.allEntities.values()) {
      if (entity.getBoundingBox().intersects(shape.getBounds()) && GeometricUtilities.shapeIntersects(entity.getBoundingBox(), shape)) {
        foundEntities.add(entity);
      }
    }

    return foundEntities;
  }

  /**
   * Gets the entity with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * @return The entity with the specified map ID or null if no entity could be found.
   */
  public IEntity get(final int mapId) {
    return this.allEntities.get(mapId);
  }

  /**
   * Gets all entities with the specified map IDs from this environment.
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

  /**
   * Gets the strongly typed entity with the specified map ID from this environment.
   * 
   * @param <T>
   *          The type of the entity.
   * @param clss
   *          The class instance defining the type of the entity.
   * @param mapId
   *          The map ID of the entity.
   * @return The strongly typed entity with the specified map ID or null if no entity could be found or if the defined type doesn't match.
   */
  public <T extends IEntity> T get(Class<T> clss, int mapId) {
    IEntity ent = this.get(mapId);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  /**
   * Gets the entity with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * @return The entity with the specified name or null if no entity could be found or if the defined type doesn't match.
   */
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

  /**
   * Gets the strongly typed entity with the specified name from this environment.
   * 
   * @param <T>
   *          The type of the entity.
   * @param clss
   *          The class instance defining the type of the entity.
   * @param name
   *          The name of the entity.
   * @return The strongly typed entity with the specified name or null if no entity could be found or if the defined type doesn't match.
   */
  public <T extends IEntity> T get(Class<T> clss, String name) {
    IEntity ent = this.get(name);
    if (ent == null || !clss.isInstance(ent)) {
      return null;
    }

    return clss.cast(ent);
  }

  /**
   * Gets a distinct collection of all entities with any of the specified tags.
   * 
   * @param tags
   *          The tags to search for.
   * 
   * @return All entities with any of the specified tags.
   */
  public Collection<IEntity> getByTag(String... tags) {
    Collection<IEntity> foundEntities = new ArrayList<>();
    for (String rawTag : tags) {
      String tag = rawTag.toLowerCase();
      for (IEntity ent : this.getEntitiesByTag().getOrDefault(tag, Arrays.asList())) {
        if (!foundEntities.contains(ent)) {
          foundEntities.add(ent);
        }
      }
    }

    return foundEntities;
  }

  /**
   * Gets a distinct and strongly named collection of all entities with any of the specified tags.
   * 
   * @param <T>
   *          The type of the entity.
   * @param clss
   *          The class instance defining the type of the entity.
   * @param tags
   *          The tags to search for.
   * 
   * @return All entities with any of the specified tags.
   */
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

  /**
   * Gets the ambient light instance of this environment.
   * 
   * @return The ambient light instance of this environment.
   * 
   * @see #getStaticShadowLayer()
   */
  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  /**
   * Gets the static shadow lighting layer of this environment.
   * 
   * @return The static shadow lighting layer of this environment.
   * 
   * @see #getAmbientLight()
   */
  public StaticShadowLayer getStaticShadowLayer() {
    return this.staticShadowLayer;
  }

  /**
   * Gets an immutable collection with all assigned map IDs on this environment.
   * 
   * @return An immutable collection with all map IDs.
   */
  public Collection<Integer> getAllMapIDs() {
    return Collections.unmodifiableCollection(this.allEntities.keySet());
  }

  /**
   * Gets an immutable collection containing all {@link MapArea} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link MapArea} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<MapArea> getAreas() {
    return Collections.unmodifiableCollection(this.mapAreas);
  }

  /**
   * Gets the {@link MapArea} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link MapArea} with the specified map ID or null if no entity is found.
   * 
   * @see #getArea(String)
   * @see #getAreas()
   */
  public MapArea getArea(final int mapId) {
    return getById(this.mapAreas, mapId);
  }

  /**
   * Gets the {@link MapArea} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link MapArea} with the specified name or null if no entity is found.
   * 
   * @see #getArea(int)
   * @see #getAreas()
   */
  public MapArea getArea(final String name) {
    return getByName(this.mapAreas, name);
  }

  /**
   * Gets an immutable collection containing all {@link Emitter} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link Emitter} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<Emitter> getEmitters() {
    return Collections.unmodifiableCollection(this.emitters);
  }

  /**
   * Gets the {@link Emitter} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link Emitter} with the specified map ID or null if no entity is found.
   * 
   * @see #getEmitter(String)
   * @see #getEmitters()
   */
  public Emitter getEmitter(int mapId) {
    return getById(this.emitters, mapId);
  }

  /**
   * Gets the {@link Emitter} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link Emitter} with the specified name or null if no entity is found.
   * 
   * @see #getEmitter(int)
   * @see #getEmitters()
   */
  public Emitter getEmitter(String name) {
    return getByName(this.emitters, name);
  }

  /**
   * Gets an immutable collection containing all {@link CollisionBox} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link CollisionBox} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<CollisionBox> getCollisionBoxes() {
    return Collections.unmodifiableCollection(this.colliders);
  }

  /**
   * Gets the {@link CollisionBox} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link CollisionBox} with the specified map ID or null if no entity is found.
   * 
   * @see #getCollisionBox(String)
   * @see #getCollisionBoxes()
   */
  public CollisionBox getCollisionBox(int mapId) {
    return getById(this.colliders, mapId);
  }

  /**
   * Gets the {@link CollisionBox} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link CollisionBox} with the specified name or null if no entity is found.
   * 
   * @see #getCollisionBox(int)
   * @see #getCollisionBoxes()
   */
  public CollisionBox getCollisionBox(String name) {
    return getByName(this.colliders, name);
  }

  /**
   * Gets an immutable collection containing all {@link ICombatEntity} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link ICombatEntity} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<ICombatEntity> getCombatEntities() {
    return Collections.unmodifiableCollection(this.combatEntities.values());
  }

  /**
   * Gets the {@link ICombatEntity} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link ICombatEntity} with the specified map ID or null if no entity is found.
   * 
   * @see #getCombatEntity(String)
   * @see #getCombatEntities()
   */
  public ICombatEntity getCombatEntity(final int mapId) {
    return getById(this.combatEntities.values(), mapId);
  }

  /**
   * Gets the {@link ICombatEntity} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link ICombatEntity} with the specified name or null if no entity is found.
   * 
   * @see #getCombatEntity(int)
   * @see #getCombatEntities()
   */
  public ICombatEntity getCombatEntity(String name) {
    return getByName(this.combatEntities.values(), name);
  }

  /**
   * Gets an immutable collection containing all entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<IEntity> getEntities() {
    return Collections.unmodifiableCollection(this.allEntities.values());
  }

  /**
   * Gets all entities of the specified type on this environment.
   * 
   * @param <T>
   *          The type of the entity.
   * @param cls
   *          The class instance defining the type of the entity.
   * 
   * @return All entities of the specified type.
   */
  public <T> Collection<T> getEntities(Class<? extends T> cls) {
    Collection<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.allEntities.values()) {
      if (cls.isInstance(ent)) {
        foundEntities.add(cls.cast(ent));
      }
    }

    return foundEntities;
  }

  /**
   * Gets all entities of the specified type on this environment.
   * 
   * @param <T>
   *          The type of the entity.
   * @param cls
   *          The class instance defining the type of the entity.
   * @param pred
   *          A predicate that decides whether the defined entity should be included in the result.
   * 
   * @return All entities of the specified type.
   */
  public <T> Collection<T> getEntities(Class<? extends T> cls, Predicate<T> pred) {
    Collection<T> foundEntities = new ArrayList<>();
    for (IEntity ent : this.allEntities.values()) {
      if (cls.isInstance(ent)) {
        T entity = cls.cast(ent);

        if (pred.test(entity)) {
          foundEntities.add(entity);
        }
      }
    }

    return foundEntities;
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
    return Collections.unmodifiableCollection(this.miscEntities.get(renderType).values());
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

    return Collections.unmodifiableCollection(this.layerEntities.get(layer));
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
        return Collections.unmodifiableCollection(entry.getValue());
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
        return Collections.unmodifiableCollection(entry.getValue());
      }
    }

    return Collections.emptySet();
  }

  /**
   * <b>DON'T USE THIS! THIS IS FOR ENGINE INTERNAL PURPOSES ONLY!</b>.
   * 
   * @return A map with all entities by tags.
   */
  public Map<String, Collection<IEntity>> getEntitiesByTag() {
    return this.entitiesByTag;
  }

  /**
   * Gets an immutable collection containing all {@link LightSource} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link LightSource} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<LightSource> getLightSources() {
    return Collections.unmodifiableCollection(this.lightSources);
  }

  /**
   * Gets the {@link LightSource} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link LightSource} with the specified map ID or null if no entity is found.
   * 
   * @see #getLightSource(String)
   * @see #getLightSources()
   */
  public LightSource getLightSource(final int mapId) {
    return getById(this.lightSources, mapId);
  }

  /**
   * Gets the {@link LightSource} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link LightSource} with the specified name or null if no entity is found.
   * 
   * @see #getLightSource(int)
   * @see #getLightSources()
   */
  public LightSource getLightSource(String name) {
    return getByName(this.lightSources, name);
  }

  /**
   * Gets the next unique local map id. (All local map ids are negative).
   * 
   * @return The next unique local map id.
   */
  public static synchronized int getLocalMapId() {
    return --localIdSequence;
  }

  /**
   * Gets the map on which this environment is based upon.
   * 
   * @return The map of this environment.
   */
  public IMap getMap() {
    return this.map;
  }

  /**
   * Gets an immutable collection containing all {@link IMobileEntity} instances on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link IMobileEntity} instances.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<IMobileEntity> getMobileEntities() {
    return Collections.unmodifiableCollection(this.mobileEntities.values());
  }

  /**
   * Gets the {@link IMobileEntity} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link IMobileEntity} with the specified map ID or null if no entity is found.
   * 
   * @see #getMobileEntity(String)
   * @see #getMobileEntities()
   */
  public IMobileEntity getMobileEntity(final int mapId) {
    return getById(this.mobileEntities.values(), mapId);
  }

  /**
   * Gets the {@link IMobileEntity} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link IMobileEntity} with the specified name or null if no entity is found.
   * 
   * @see #getMobileEntity(int)
   * @see #getMobileEntities()
   */
  public IMobileEntity getMobileEntity(String name) {
    return getByName(this.mobileEntities.values(), name);
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

  /**
   * Gets an immutable collection containing all {@link IRenderable} instances for the specified render type on this environment.
   * 
   * <p>
   * To add or remove instances, use the corresponding methods on this environment.
   * </p>
   * 
   * @param renderType
   *          The render type of the renderable instances.
   * 
   * @return An immutable collection with all {@link IRenderable} instances.
   * 
   * @see #add(IRenderable, RenderType)
   * @see #removeRenderable(IRenderable)
   */
  public Collection<IRenderable> getRenderables(RenderType renderType) {
    return Collections.unmodifiableCollection(this.renderables.get(renderType));
  }

  /**
   * Gets an immutable collection containing all {@link Prop} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link Prop} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<Prop> getProps() {
    return Collections.unmodifiableCollection(this.props);
  }

  /**
   * Gets the {@link Prop} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link Prop} with the specified map ID or null if no entity is found.
   * 
   * @see #getProp(String)
   * @see #getProps()
   */
  public Prop getProp(int mapId) {
    return getById(this.props, mapId);
  }

  /**
   * Gets the {@link Prop} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link Prop} with the specified name or null if no entity is found.
   * 
   * @see #getProp(int)
   * @see #getProps()
   */
  public Prop getProp(String name) {
    return getByName(this.props, name);
  }

  /**
   * Gets an immutable collection containing all {@link Creature} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link Creature} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<Creature> getCreatures() {
    return Collections.unmodifiableCollection(this.creatures);
  }

  /**
   * Gets the {@link Creature} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link Creature} with the specified map ID or null if no entity is found.
   * 
   * @see #getCreature(String)
   * @see #getCreatures()
   */
  public Creature getCreature(int mapId) {
    return getById(this.creatures, mapId);
  }

  /**
   * Gets the {@link Creature} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link Creature} with the specified name or null if no entity is found.
   * 
   * @see #getCreature(int)
   * @see #getCreatures()
   */
  public Creature getCreature(String name) {
    return getByName(this.creatures, name);
  }

  /**
   * Gets an immutable collection containing all {@link Spawnpoint} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link Spawnpoint} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<Spawnpoint> getSpawnPoints() {
    return Collections.unmodifiableCollection(this.spawnPoints);
  }

  /**
   * Gets the {@link Spawnpoint} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link Spawnpoint} with the specified map ID or null if no entity is found.
   * 
   * @see #getSpawnpoint(String)
   * @see #getSpawnPoints()
   */
  public Spawnpoint getSpawnpoint(final int mapId) {
    return getById(this.spawnPoints, mapId);
  }

  /**
   * Gets the {@link Spawnpoint} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link Spawnpoint} with the specified name or null if no entity is found.
   * 
   * @see #getSpawnpoint(int)
   * @see #getSpawnPoints()
   */
  public Spawnpoint getSpawnpoint(final String name) {
    return getByName(this.spawnPoints, name);
  }

  /**
   * Gets an immutable collection containing all {@link StaticShadow} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link StaticShadow} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<StaticShadow> getStaticShadows() {
    return Collections.unmodifiableCollection(this.staticShadows);
  }

  /**
   * Gets the {@link StaticShadow} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link StaticShadow} with the specified map ID or null if no entity is found.
   * 
   * @see #getStaticShadow(String)
   * @see #getStaticShadows()
   */
  public StaticShadow getStaticShadow(int mapId) {
    return getById(this.staticShadows, mapId);
  }

  /**
   * Gets the {@link StaticShadow} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link StaticShadow} with the specified name or null if no entity is found.
   * 
   * @see #getStaticShadow(int)
   * @see #getStaticShadows()
   */
  public StaticShadow getStaticShadow(String name) {
    return getByName(this.staticShadows, name);
  }

  /**
   * Gets an immutable collection containing all {@link Trigger} entities on this environment.
   * 
   * <p>
   * To add or remove entities, use the corresponding methods on this environment.
   * </p>
   * 
   * @return An immutable collection with all {@link Trigger} entities.
   * 
   * @see #add(IEntity)
   * @see #addAll(Iterable)
   * @see #remove(IEntity)
   * @see #removeAll(Iterable)
   */
  public Collection<Trigger> getTriggers() {
    return Collections.unmodifiableCollection(this.triggers);
  }

  /**
   * Gets the {@link Trigger} with the specified map ID from this environment.
   * 
   * @param mapId
   *          The map ID of the entity.
   * 
   * @return The {@link Trigger} with the specified map ID or null if no entity is found.
   * 
   * @see #getTrigger(String)
   * @see #getTriggers()
   */
  public Trigger getTrigger(final int mapId) {
    return getById(this.triggers, mapId);
  }

  /**
   * Gets the {@link Trigger} with the specified name from this environment.
   * 
   * @param name
   *          The name of the entity.
   * 
   * @return The {@link Trigger} with the specified name or null if no entity is found.
   * 
   * @see #getTrigger(int)
   * @see #getTriggers()
   */
  public Trigger getTrigger(final String name) {
    return getByName(this.triggers, name);
  }

  /**
   * Gets all tags that are assigned to entities on this environment.
   * 
   * @return All assigned tags of this environment.
   */
  public Collection<String> getUsedTags() {
    return Collections.unmodifiableCollection(this.getEntitiesByTag().keySet());
  }

  /**
   * Gets the center location of the boundaries defined by the map of this environment.
   * 
   * @return The center of this environment.
   * 
   * @see #getMap()
   */
  public Point2D getCenter() {
    return new Point2D.Double(this.getMap().getSizeInPixels().getWidth() / 2.0, this.getMap().getSizeInPixels().getHeight() / 2.0);
  }

  /**
   * Initializes all entities and lighting layers of this environment.
   * 
   * @see EnvironmentListener#initialized(Environment)
   */
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

  /**
   * Determines whether this environment has been loaded.
   * 
   * @return True if this environment has been loaded; otherwise false.
   * 
   * @see #load()
   * @see #unload()
   */
  public boolean isLoaded() {
    return this.loaded;
  }

  /**
   * Initializes and loads this environment and all its entities.
   * 
   * @see #init()
   * @see EnvironmentListener#loaded(Environment)
   * @see #isLoaded()
   */
  public void load() {
    this.init();
    if (this.loaded) {
      return;
    }

    if (this.getMap() != null) {
      Game.physics().setBounds(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixels().getWidth(), this.getMap().getSizeInPixels().getHeight()));
    }

    this.allEntities.values().stream().forEach(this::load);
    this.updateLighting();
    this.loaded = true;
    this.fireEvent(l -> l.loaded(this));
  }

  /**
   * Loads the entities from the map object with the specified map ID from the map of this environment.
   * 
   * @param mapId
   *          The map ID of the map object.
   * 
   * @return True if any entity could be loaded; otherwise false.
   */
  public boolean loadFromMap(final int mapId) {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      Optional<IMapObject> opt = layer.getMapObjects().stream().filter(mapObject -> mapObject.getType() != null && !mapObject.getType().isEmpty() && mapObject.getId() == mapId).findFirst();
      if (opt.isPresent()) {
        return !this.load(opt.get()).isEmpty();
      }
    }

    return false;
  }

  /**
   * Reloads the map object with the specified map ID from the map by first removing any previously loaded entity
   * and then loading it freshly from its map definition.
   * 
   * @param mapId
   *          The map ID of the map object.
   *
   * @see #remove(int)
   * @see Environment#loadFromMap(int)
   */
  public void reloadFromMap(final int mapId) {
    this.remove(mapId);
    this.loadFromMap(mapId);
  }

  /**
   * Loads all entities for the specified map object.
   * 
   * @param mapObject
   *          The mapObject to load the entities from.
   * @return A collection of all loaded entities.
   * 
   * @see MapObjectLoader#load(Environment, IMapObject)
   */
  public Collection<IEntity> load(final IMapObject mapObject) {
    if (mapObject == null) {
      return Collections.emptySet();
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
      loadedEntities = loader.load(this, mapObject);
      for (IEntity entity : loadedEntities) {
        if (entity != null) {

          // only add the entity to be rendered with it's layer if its RenderType equals the layer's RenderType
          if (mapObject.getLayer() != null && entity.renderWithLayer()) {
            this.addEntity(entity);
            this.layerEntities.computeIfAbsent(mapObject.getLayer(), m -> new CopyOnWriteArrayList<>()).add(entity);
            this.fireEntityEvent(l -> l.entityAdded(entity));
          } else {
            this.add(entity);
          }
        }
      }

      return loadedEntities;
    }

    return Collections.emptySet();
  }

  /**
   * Attempts to interact with triggers on this environment.
   * 
   * @param source
   *          The entity that attempts to interacts with triggers.
   * @return The trigger that the source entity was able to interact with or null.
   */
  public Trigger interact(ICollisionEntity source) {
    return this.interact(source, null);
  }

  /**
   * Attempts to interact with triggers on this environment.
   * 
   * @param source
   *          The entity that attempts to interacts with triggers.
   * @param condition
   *          The condition that determines whether a trigger can be interacted with.
   * 
   * @return The trigger that the entity was able to interact with or null.
   * 
   * @see Trigger#canTrigger(ICollisionEntity)
   */
  public Trigger interact(ICollisionEntity source, Predicate<Trigger> condition) {
    for (final Trigger trigger : this.triggers) {
      if (trigger.canTrigger(source) && (condition == null || condition.test(trigger))) {
        boolean result = trigger.interact(source);
        if (result) {
          return trigger;
        }
      }
    }

    return null;
  }

  /**
   * Removes the specified entity from this environment and unloads is.
   * 
   * @param entity
   *          The entity to be removed.
   * 
   * @see #remove(int)
   * @see #remove(String)
   * @see #removeAll(Iterable)
   * @see #unload(IEntity)
   * @see EnvironmentEntityListener#entityRemoved(IEntity)
   * @see IEntity#removed(Environment)
   * @see EntityListener#removed(IEntity, Environment)
   */
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

  /**
   * Removes the entity with the specified map ID from this environment and unloads is.
   * 
   * @param mapId
   *          The map ID of the entity to be removed.
   * 
   * @see #remove(int)
   * @see #remove(String)
   * @see #removeAll(Iterable)
   * @see #unload(IEntity)
   * @see EnvironmentEntityListener#entityRemoved(IEntity)
   * @see IEntity#removed(Environment)
   * @see EntityListener#removed(IEntity, Environment)
   */
  public void remove(final int mapId) {
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  /**
   * Removes the entity with the specified name from this environment and unloads is.
   * 
   * @param name
   *          The name of the entity to be removed.
   * 
   * @see #remove(int)
   * @see #remove(String)
   * @see #removeAll(Iterable)
   * @see #unload(IEntity)
   * @see EnvironmentEntityListener#entityRemoved(IEntity)
   * @see IEntity#removed(Environment)
   * @see EntityListener#removed(IEntity, Environment)
   */
  public void remove(String name) {
    final IEntity ent = this.get(name);
    if (ent == null) {
      return;
    }

    this.remove(ent);
  }

  /**
   * Removes all specified entities from this environment.
   * 
   * @param <T>
   *          The type of the specified entities
   * 
   * @param entities
   *          The entities to be removed.
   *
   * @see #remove(int)
   * @see #remove(String)
   */
  public <T extends IEntity> void removeAll(Iterable<T> entities) {
    if (entities == null) {
      return;
    }

    for (T ent : entities) {
      this.remove(ent);
    }
  }

  /**
   * Removes all specified entities from this environment.
   * 
   * @param entities
   *          The entities to be removed.
   * 
   * @see #remove(int)
   * @see #remove(String)
   */
  public void removeAll(IEntity... entities) {
    this.removeAll(Arrays.asList(entities));
  }

  public void removeRenderable(final IRenderable renderable) {
    for (Collection<IRenderable> rends : this.renderables.values()) {
      rends.remove(renderable);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    long renderStart = System.nanoTime();

    AffineTransform otx = g.getTransform();
    g.scale(Game.world().camera().getRenderScale(), Game.world().camera().getRenderScale());
    if (this.getMap() != null && this.getMap().getBackgroundColor() != null) {
      g.setColor(this.getMap().getBackgroundColor());
      g.fill(new Rectangle2D.Double(0.0, 0.0, Game.world().camera().getViewport().getWidth(), Game.world().camera().getViewport().getHeight()));
    }

    this.render(g, RenderType.BACKGROUND);

    this.render(g, RenderType.GROUND);
    DebugRenderer.renderMapDebugInfo(g, this.getMap());

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

    g.setTransform(otx);
  }

  /**
   * Gets the gravity defined for this environment.
   * 
   * @return The gravity of this environment.
   * 
   * @see GameWorld#gravity()
   * @see GameWorld#setGravity(int)
   * @see #setGravity(int)
   */
  public int getGravity() {
    return this.gravity;
  }

  /**
   * Sets the gravity for this particular environment.
   * 
   * <p>
   * This typically only needs to be called explicitly, when the gravity is different than for other environments.
   * </p>
   * 
   * @param gravity
   *          The new gravity for this environment. If 0, no gravity will be applied.
   *
   * @see GameWorld#gravity()
   * @see GameWorld#setGravity(int)
   * @see #getGravity()
   */
  public void setGravity(int gravity) {
    this.gravity = gravity;

    if (this.getGravity() != 0) {
      for (IMobileEntity entity : this.mobileEntities.values()) {
        if (this.gravityForces.containsKey(entity.getMapId())) {
          this.gravityForces.get(entity.getMapId()).setStrength(this.gravity);
        } else {
          this.addGravityForce(entity);
        }
      }
    } else {
      for (IMobileEntity entity : this.mobileEntities.values()) {
        this.removeGravity(entity);
      }
    }
  }

  /**
   * Unloads all entities of this environment.
   * 
   * @see #unload(IEntity)
   * @see EnvironmentListener#unloaded(Environment)
   */
  public void unload() {
    if (!this.loaded) {
      return;
    }

    // unregister all updatable entities from the current environment
    for (final IEntity entity : this.allEntities.values()) {
      this.unload(entity);
    }

    this.loaded = false;
    this.fireEvent(l -> l.unloaded(this));
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

  private static void dispose(final Collection<? extends IEntity> entities) {
    for (final IEntity entity : entities) {
      if (entity instanceof IUpdateable) {
        Game.loop().detach((IUpdateable) entity);
      }

      entity.detachControllers();
    }
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

  private void addAmbientLight() {
    final Color ambientColor = this.getMap().getColorValue(MapProperty.AMBIENTCOLOR, AmbientLight.DEFAULT_COLOR);
    this.ambientLight = new AmbientLight(this, ambientColor);
  }

  private void addStaticShadows() {
    final Color color = this.getMap().getColorValue(MapProperty.SHADOWCOLOR, StaticShadow.DEFAULT_COLOR);
    this.staticShadowLayer = new StaticShadowLayer(this, color);
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
    IMovementController mvmtControl = entity.movement();
    if (mvmtControl != null) {
      GravityForce force = new GravityForce(entity, this.getGravity(), Direction.DOWN);
      force.setIdentifier(GRAVITY_IDENTIFIER);
      entity.movement().apply(force);
      this.gravityForces.put(entity.getMapId(), force);
    }
  }

  private void removeGravity(IMobileEntity entity) {
    if (this.gravityForces.containsKey(entity.getMapId())) {
      this.gravityForces.get(entity.getMapId()).end();
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

  private void addEntity(final IEntity entity) {
    int desiredID = entity.getMapId();
    // assign local map id if the entity's mapID is invalid
    if (desiredID == 0 || this.allEntities.keySet().contains(desiredID)) {
      entity.setMapId(this.getLocalMapId());
      log.fine(() -> String.format("Entity [%s] was assigned a local mapID because #%d was already taken or invalid.", entity, desiredID));
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

    this.allEntities.put(entity.getMapId(), entity);
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
        cons.accept(this.renderables.get(renderType), renderable);
      }
    }

    this.emitters.remove(emitter);
  }

  private void updateLighting(IEntity entity) {
    if (entity instanceof StaticShadow) {
      StaticShadow shadow = (StaticShadow) entity;
      this.updateLighting(shadow.getArea() != null ? shadow.getArea().getBounds2D() : shadow.getBoundingBox());
      return;
    }

    this.updateLighting(entity.getBoundingBox());
  }

  private void fireEvent(Consumer<EnvironmentListener> cons) {
    for (EnvironmentListener listener : this.listeners) {
      cons.accept(listener);
    }
  }

  private void fireRenderEvent(Graphics2D g, RenderType type) {
    for (EnvironmentRenderedListener listener : this.renderListeners.get(type)) {
      listener.rendered(g, type);
    }
  }

  private void fireEntityEvent(Consumer<EnvironmentEntityListener> cons) {
    for (EnvironmentEntityListener listener : this.entityListeners) {
      cons.accept(listener);
    }
  }
}