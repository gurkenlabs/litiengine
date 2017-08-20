/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.environment;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Path2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import de.gurkenlabs.configuration.Quality;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.IUpdateable;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.Collider;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionAlign;
import de.gurkenlabs.litiengine.entities.CollisionEntity.CollisionValign;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehaviour;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.entities.ai.IEntityController;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.environment.tilemap.IMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObjectLayer;
import de.gurkenlabs.litiengine.environment.tilemap.MapArea;
import de.gurkenlabs.litiengine.environment.tilemap.MapLocation;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapUtilities;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow;
import de.gurkenlabs.litiengine.environment.tilemap.StaticShadow.StaticShadowType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxMapLoader;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Property;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.animation.IAnimationController;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.FireEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.ShimmerEmitter;
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
import de.gurkenlabs.litiengine.physics.IMovementController;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.image.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {
  private static int localIdSequence = 0;
  private static int mapIdSequence;

  private AmbientLight ambientLight;
  private final Collection<Collider> colliders;
  private final Map<Integer, ICombatEntity> combatEntities;

  private final Map<RenderType, Map<Integer, IEntity>> entities;
  private final Map<String, List<IEntity>> entitiesByTag;

  private final List<Consumer<Graphics2D>> entitiesRenderedConsumer;
  private final List<IRenderable> groundRenderable;
  private boolean initialized;
  private final List<Consumer<IEnvironment>> initializedConsumer, loadedConsumer;

  private final Collection<LightSource> lightSources;
  private final Collection<StaticShadow> staticShadows;
  private boolean loaded;

  private IMap map;

  private final List<MapArea> mapAreas;

  private final List<Consumer<Graphics2D>> mapRenderedConsumer;

  private final Map<Integer, IMovableEntity> movableEntities;
  private final List<IRenderable> overlayRenderable;
  private final List<Consumer<Graphics2D>> overlayRenderedConsumer;
  private final List<MapLocation> spawnPoints;

  private Image staticShadowImage;
  private final Collection<Trigger> triggers;

  public Environment(final IMap map) {
    this();
    this.map = map;
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    this();
    final IMap loadedMap = Game.getMap(FileUtilities.getFileName(mapPath));
    if (loadedMap == null) {
      final IMapLoader tmxLoader = new TmxMapLoader();
      this.map = tmxLoader.LoadMap(mapPath);
    } else {
      this.map = loadedMap;
    }

    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
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
    this.entitiesRenderedConsumer = new CopyOnWriteArrayList<>();
    this.overlayRenderedConsumer = new CopyOnWriteArrayList<>();
    this.initializedConsumer = new CopyOnWriteArrayList<>();
    this.loadedConsumer = new CopyOnWriteArrayList<>();

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

    if (entity instanceof ICombatEntity) {
      this.combatEntities.put(entity.getMapId(), (ICombatEntity) entity);
    }

    if (entity instanceof IMovableEntity) {
      this.movableEntities.put(entity.getMapId(), (IMovableEntity) entity);
    }

    if (entity instanceof Collider) {
      this.colliders.add((Collider) entity);
    }

    if (entity instanceof LightSource) {
      this.lightSources.add((LightSource) entity);
    }

    if (entity instanceof Trigger) {
      this.triggers.add((Trigger) entity);
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
  }

  @Override
  public void add(final IRenderable renderable, final RenderType type) {
    switch (type) {
    case GROUND:
      this.getGroundRenderable().add(renderable);
      break;
    case OVERLAY:
      this.getOverlayRenderable().add(renderable);
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
    this.getColliders().clear();
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
    return this.findCombatEntities(shape, (entity) -> true);
  }

  @Override
  public List<ICombatEntity> findCombatEntities(final Shape shape, final Predicate<ICombatEntity> condition) {
    final ArrayList<ICombatEntity> entities = new ArrayList<>();
    if (shape == null) {
      return entities;
    }

    // for rectangle we can just use the intersects method
    if (shape instanceof Rectangle2D) {
      final Rectangle2D rect = (Rectangle2D) shape;
      for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
        if (combatEntity.getHitBox().intersects(rect)) {
          entities.add(combatEntity);
        }
      }

      return entities;
    }

    // for other shapes, we check if the shape's bounds intersect the hitbox and
    // if so, we then check if the actual shape intersects the hitbox
    for (final ICombatEntity combatEntity : this.getCombatEntities().stream().filter(condition).collect(Collectors.toList())) {
      if (combatEntity.getHitBox().intersects(shape.getBounds())) {
        if (GeometricUtilities.shapeIntersects(combatEntity.getHitBox(), shape)) {
          entities.add(combatEntity);
        }
      }
    }

    return entities;
  }

  @Override
  public List<IEntity> findEntities(final Shape shape) {
    final ArrayList<IEntity> entities = new ArrayList<>();
    if (shape == null) {
      return entities;
    }
    if (shape instanceof Rectangle2D) {
      final Rectangle2D rect = (Rectangle2D) shape;
      for (final IEntity entity : this.getEntities()) {
        if (entity.getBoundingBox().intersects(rect)) {
          entities.add(entity);
        }
      }
      return entities;
    }
    // for other shapes, we check if the shape's bounds intersect the hitbox
    // and
    // if so, we then check if the actual shape intersects the hitbox
    for (final IEntity entity : this.getEntities()) {
      if (entity.getBoundingBox().intersects(shape.getBounds())) {
        if (GeometricUtilities.shapeIntersects(entity.getBoundingBox(), shape)) {
          entities.add(entity);
        }
      }
    }

    return entities;
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
  public Collection<Collider> getColliders() {
    return this.colliders;

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
    List<T> entities = new ArrayList<>();
    for (IEntity ent : this.getEntities()) {
      if (cls.isInstance(ent)) {
        entities.add((T) ent);
      }
    }

    return entities;
  }

  public Collection<IRenderable> getGroundRenderable() {
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

  /*
   * (non-Javadoc)
   *
   * @see de.gurkenlabs.liti.map.IMapContainer#getMap()
   */
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

  public List<IRenderable> getOverlayRenderable() {
    return this.overlayRenderable;
  }

  @Override
  public MapLocation getSpawnpoint(final int mapId) {
    for (final MapLocation m : this.getSpawnPoints()) {
      if (m.getMapId() == mapId) {
        return m;
      }
    }

    return null;
  }

  @Override
  public MapLocation getSpawnpoint(final String name) {
    if (name == null || name.isEmpty()) {
      return null;
    }

    for (final MapLocation m : this.getSpawnPoints()) {
      if (m.getName() != null && m.getName().equals(name)) {
        return m;
      }
    }
    return null;
  }

  @Override
  public List<MapLocation> getSpawnPoints() {
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
    final List<Trigger> triggers = new ArrayList<>();
    if (name == null || name.isEmpty()) {
      return triggers;
    }

    for (final Trigger t : this.getTriggers()) {
      if (t.getName() != null && t.getName().equals(name)) {
        triggers.add(t);
      }
    }

    return triggers;
  }

  @Override
  public final void init() {
    if (this.initialized) {
      return;
    }

    this.loadMapObjects();
    this.addStaticShadows();
    this.addAmbientLight();

    for (final Consumer<IEnvironment> cons : this.initializedConsumer) {
      cons.accept(this);
    }

    this.initialized = true;
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
    for (final Consumer<IEnvironment> cons : this.loadedConsumer) {
      cons.accept(this);
    }
  }

  @Override
  public void loadFromMap(final int mapId) {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject.getType() == null || mapObject.getType().isEmpty() || mapObject.getId() != mapId) {
          continue;
        }

        this.addMapObject(mapObject);
        if (MapObjectType.get(mapObject.getType()) == MapObjectType.STATICSHADOW || MapObjectType.get(mapObject.getType()) == MapObjectType.LIGHTSOURCE) {
          this.addStaticShadows();
        }
        break;
      }
    }
  }

  @Override
  public void onEntitiesRendered(final Consumer<Graphics2D> consumer) {
    this.entitiesRenderedConsumer.add(consumer);
  }

  @Override
  public void onInitialized(final Consumer<IEnvironment> consumer) {
    this.initializedConsumer.add(consumer);
  }

  @Override
  public void onLoaded(final Consumer<IEnvironment> consumer) {
    this.loadedConsumer.add(consumer);
  }

  @Override
  public void onMapRendered(final Consumer<Graphics2D> consumer) {
    this.mapRenderedConsumer.add(consumer);
  }

  @Override
  public void onOverlayRendered(final Consumer<Graphics2D> consumer) {
    this.overlayRenderedConsumer.add(consumer);
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

    this.entities.get(entity.getRenderType()).entrySet().removeIf(e -> e.getValue().getMapId() == entity.getMapId());
    for (String tag : entity.getTags()) {
      if (this.entitiesByTag.containsKey(tag)) {
        this.entitiesByTag.get(tag).remove(entity);
      }
    }

    if (entity instanceof Collider) {
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
      System.out.println("could not remove entity with id '" + mapId + "' from the environment, because there is no entity with such a map ID.");
      return;
    }
    this.remove(ent);
  }

  @Override
  public void removeRenderable(final IRenderable renderable) {
    if (this.getGroundRenderable().contains(renderable)) {
      this.getGroundRenderable().remove(renderable);
    }

    if (this.getOverlayRenderable().contains(renderable)) {
      this.getOverlayRenderable().remove(renderable);
    }
  }

  @Override
  public void render(final Graphics2D g) {
    g.scale(Game.getInfo().getRenderScale(), Game.getInfo().getRenderScale());

    Game.getRenderEngine().renderMap(g, this.getMap());
    this.informConsumers(g, this.mapRenderedConsumer);

    for (final IRenderable rend : this.getGroundRenderable()) {
      rend.render(g);
    }

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.GROUND).values(), false);
    if (Game.getConfiguration().GRAPHICS.getGraphicQuality() == Quality.VERYHIGH) {
      Game.getRenderEngine().renderEntities(g, this.getLightSources(), false);
    }

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.NORMAL).values());
    this.informConsumers(g, this.entitiesRenderedConsumer);

    Game.getRenderEngine().renderEntities(g, this.entities.get(RenderType.OVERLAY).values(), false);

    Game.getRenderEngine().renderLayers(g, this.getMap(), RenderType.OVERLAY);

    // render static shadows
    RenderEngine.renderImage(g, this.getStaticShadowImage(), Game.getScreenManager().getCamera().getViewPortLocation(0, 0));

    if (this.getAmbientLight() != null && this.getAmbientLight().getAlpha() != 0) {
      // this.getAmbientLight().createImage();
      RenderEngine.renderImage(g, this.getAmbientLight().getImage(), Game.getScreenManager().getCamera().getViewPortLocation(0, 0));
    }

    for (final IRenderable rend : this.getOverlayRenderable()) {
      rend.render(g);
    }

    this.informConsumers(g, this.overlayRenderedConsumer);
    g.scale(1.0 / Game.getInfo().getRenderScale(), 1.0 / Game.getInfo().getRenderScale());
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

  protected void addStaticShadow(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.STATICSHADOW) {
      return;
    }
    double x = mapObject.getX();
    double y = mapObject.getY();
    double width = mapObject.getDimension().getWidth();
    double height = mapObject.getDimension().getHeight();
    final StaticShadow shadow = new StaticShadow(mapObject.getId(), mapObject.getName(), x, y, width, height, StaticShadowType.get(mapObject.getCustomProperty(MapObjectProperties.SHADOWTYPE)));
    this.getStaticShadows().add(shadow);
  }

  protected void addCollisionBox(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      return;
    }

    final String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    boolean isObstacle = true;
    if (obstacle != null && !obstacle.isEmpty()) {
      isObstacle = Boolean.valueOf(obstacle);
    }

    final Collider col = new Collider(isObstacle);
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    col.setCollisionBoxWidth(col.getWidth());
    col.setCollisionBoxHeight(col.getHeight());
    col.setMapId(mapObject.getId());
    col.setName(mapObject.getName());
    this.add(col);
  }

  protected void addDecorMob(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.DECORMOB) {
      return;
    }
    if (mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME) == null) {
      return;
    }

    short velocity = (short) (100 / Game.getInfo().getRenderScale());
    if (mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY) != null) {
      velocity = Short.parseShort(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY));
    }

    final DecorMob mob = new DecorMob(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), MovementBehaviour.get(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_BEHAVIOUR)), velocity);

    if (mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      mob.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }

    mob.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH) != null) {
      mob.setCollisionBoxWidth(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT) != null) {
      mob.setCollisionBoxHeight(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)));
    }

    mob.setCollisionBoxAlign(CollisionAlign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)));
    mob.setCollisionBoxValign(CollisionValign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)));
    mob.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    mob.setMapId(mapObject.getId());
    mob.setName(mapObject.getName());

    this.add(mob);
  }

  protected void addEmitter(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      return;
    }

    Emitter emitter = null;
    final String emitterType = mapObject.getCustomProperty(MapObjectProperties.EMITTERTYPE);
    if (emitterType == null || emitterType.isEmpty()) {
      return;
    }

    switch (emitterType) {
    case "fire":
      emitter = new FireEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      break;
    case "shimmer":
      emitter = new ShimmerEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      break;
    }

    // try to load custom emitter
    if (emitter == null && emitterType.endsWith(".xml")) {
      emitter = new CustomEmitter(mapObject.getLocation().x, mapObject.getLocation().y, emitterType);
    }

    if (emitter != null) {
      emitter.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
      emitter.setMapId(mapObject.getId());
      emitter.setName(mapObject.getName());

      this.add(emitter);
    }
  }

  protected void addLightSource(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.LIGHTSOURCE) {
      return;
    }
    final String mapObjectBrightness = mapObject.getCustomProperty(MapObjectProperties.LIGHTBRIGHTNESS);
    final String mapObjectIntensity = mapObject.getCustomProperty(MapObjectProperties.LIGHTINTENSITY);
    final String mapObjectColor = mapObject.getCustomProperty(MapObjectProperties.LIGHTCOLOR);
    if (mapObjectBrightness == null || mapObjectBrightness.isEmpty() || mapObjectColor == null || mapObjectColor.isEmpty()) {
      return;
    }

    final int brightness = Integer.parseInt(mapObjectBrightness);
    final Color color = Color.decode(mapObjectColor);

    int intensity = brightness;
    if (mapObjectIntensity != null && !mapObjectIntensity.isEmpty()) {
      intensity = Integer.parseInt(mapObjectIntensity);
    }

    String lightType;
    switch (mapObject.getCustomProperty(MapObjectProperties.LIGHTSHAPE)) {
    case LightSource.ELLIPSE:
      lightType = LightSource.ELLIPSE;
      break;
    case LightSource.RECTANGLE:
      lightType = LightSource.RECTANGLE;
      break;
    default:
      lightType = LightSource.ELLIPSE;
    }
    final LightSource light = new LightSource(this, brightness, intensity, new Color(color.getRed(), color.getGreen(), color.getBlue(), brightness), lightType);
    light.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    light.setLocation(mapObject.getLocation());
    light.setMapId(mapObject.getId());
    light.setName(mapObject.getName());
    this.add(light);
  }

  protected void addMapObject(final IMapObject mapObject) {
    this.addCollisionBox(mapObject);
    this.addStaticShadow(mapObject);
    this.addLightSource(mapObject);
    this.addSpawnpoint(mapObject);
    this.addMapArea(mapObject);
    this.addProp(mapObject);
    this.addEmitter(mapObject);
    this.addDecorMob(mapObject);
    this.addMob(mapObject);
    this.addTrigger(mapObject);
  }

  protected void addMob(final IMapObject mapObject) {

  }

  protected void addProp(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      return;
    }

    // set map properties by map object
    final Material material = mapObject.getCustomProperty(MapObjectProperties.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL));
    final Prop prop = this.createNewProp(mapObject, mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), material);
    prop.setMapId(mapObject.getId());

    if (mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE) != null && !mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      prop.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.HEALTH) != null) {
      prop.getAttributes().getHealth().modifyMaxBaseValue(new AttributeModifier<>(Modification.Set, Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.HEALTH))));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISION) != null) {
      prop.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    }

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH) != null) {
      prop.setCollisionBoxWidth(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTH)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT) != null) {
      prop.setCollisionBoxHeight(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHT)));
    }

    prop.setCollisionBoxAlign(CollisionAlign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONALGIN)));
    prop.setCollisionBoxValign(CollisionValign.get(mapObject.getCustomProperty(MapObjectProperties.COLLISIONVALGIN)));
    prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);

    if (mapObject.getCustomProperty(MapObjectProperties.TEAM) != null) {
      prop.setTeam(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.TEAM)));
    }
    prop.setMapId(mapObject.getId());
    prop.setName(mapObject.getName());
    this.add(prop);
  }

  protected void addSpawnpoint(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.SPAWNPOINT) {
      return;
    }

    final MapLocation spawn = new MapLocation(mapObject.getId(), new Point(mapObject.getLocation()));
    spawn.setName(mapObject.getName());
    this.getSpawnPoints().add(spawn);
  }

  protected void addTrigger(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.TRIGGER) {
      return;
    }

    final String message = mapObject.getCustomProperty(MapObjectProperties.TRIGGERMESSAGE);

    final TriggerActivation act = mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATION) != null ? TriggerActivation.valueOf(mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATION)) : TriggerActivation.COLLISION;
    final String targets = mapObject.getCustomProperty(MapObjectProperties.TRIGGERTARGETS);
    final String activators = mapObject.getCustomProperty(MapObjectProperties.TRIGGERACTIVATORS);
    final String oneTime = mapObject.getCustomProperty(MapObjectProperties.TRIGGERONETIME);
    final boolean oneTimeBool = oneTime != null && !oneTime.isEmpty() ? Boolean.valueOf(oneTime) : false;

    final Map<String, String> triggerArguments = new HashMap<>();
    for (final Property prop : mapObject.getAllCustomProperties()) {
      if (MapObjectProperties.isCustom(prop.getName())) {
        triggerArguments.put(prop.getName(), prop.getValue());
      }
    }

    final Trigger trigger = new Trigger(act, mapObject.getName(), message, oneTimeBool, triggerArguments);
    if (targets != null && !targets.isEmpty()) {
      final String[] split = targets.split(",");
      for (final String s : split) {
        if (s == null || s.isEmpty()) {
          continue;
        }
        try {
          trigger.addTarget(Integer.parseInt(s));
        } catch (final NumberFormatException ne) {
          ne.printStackTrace();
        }
      }
    }

    if (activators != null && !activators.isEmpty()) {
      final String[] split = activators.split(",");
      for (final String s : split) {
        if (s == null || s.isEmpty()) {
          continue;
        }
        try {
          trigger.addActivator(Integer.parseInt(s));
        } catch (final NumberFormatException ne) {
          ne.printStackTrace();
        }
      }
    }

    trigger.setMapId(mapObject.getId());
    trigger.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    trigger.setCollisionBoxHeight(trigger.getHeight());
    trigger.setCollisionBoxWidth(trigger.getWidth());
    trigger.setLocation(new Point2D.Double(mapObject.getLocation().x, mapObject.getLocation().y));
    this.add(trigger);
  }

  protected Prop createNewProp(IMapObject mapObject, String spriteSheetName, Material material) {
    Prop prop = new Prop(mapObject.getLocation(), spriteSheetName, material);
    final String obstacle = mapObject.getCustomProperty(MapObjectProperties.OBSTACLE);
    if (obstacle != null && !obstacle.isEmpty()) {
      prop.setObstacle(Boolean.valueOf(obstacle));
    }

    return prop;
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
    }

    this.ambientLight = new AmbientLight(this, ambientColor, ambientAlpha);
  }

  private void addMapArea(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.AREA) {
      return;
    }

    final MapArea area = new MapArea(mapObject.getId(), mapObject.getName(), mapObject.getX(), mapObject.getY(), mapObject.getDimension().getWidth(), mapObject.getDimension().getHeight());
    this.getAreas().add(area);
  }

  private void addStaticShadows() {
    final int shadowOffset = 10;
    final List<Path2D> staticShadows = new ArrayList<>();
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
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.DOWNRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.LEFTRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(StaticShadowType.RIGHTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
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
        staticShadows.add(parallelogram);
      }
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getMap().getSizeInPixels().getWidth(), (int) this.getMap().getSizeInPixels().getHeight());
    final Graphics2D g = img.createGraphics();
    g.setColor(new Color(0, 0, 0, 75));

    final Area ar = new Area();
    for (final Path2D staticShadow : staticShadows) {
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
    // 1. add to physics engins
    if (entity instanceof Collider) {
      final Collider coll = (Collider) entity;
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

    // 2. register for update or activate
    if (entity instanceof Emitter) {
      final Emitter emitter = (Emitter) entity;
      if (emitter.isActivateOnInit()) {
        emitter.activate(Game.getLoop());
      }
    } else if (entity instanceof IUpdateable) {
      Game.getLoop().attach((IUpdateable) entity);
    }

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
    if (entity instanceof Collider) {
      final Collider coll = (Collider) entity;
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

  @Override
  public <T extends IEntity> void remove(Collection<T> entities) {
    for (T ent : entities) {
      this.remove(ent);
    }
  }
}