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
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
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
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.MapLocation;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectTypes;
import de.gurkenlabs.litiengine.environment.tilemap.MapProperty;
import de.gurkenlabs.litiengine.graphics.AmbientLight;
import de.gurkenlabs.litiengine.graphics.IRenderable;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.RenderEngine;
import de.gurkenlabs.litiengine.graphics.RenderType;
import de.gurkenlabs.litiengine.graphics.particles.Emitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.FireEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.RainEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.ShimmerEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.SnowEmitter;
import de.gurkenlabs.litiengine.graphics.particles.emitters.Weather;
import de.gurkenlabs.tilemap.IMap;
import de.gurkenlabs.tilemap.IMapLoader;
import de.gurkenlabs.tilemap.IMapObject;
import de.gurkenlabs.tilemap.IMapObjectLayer;
import de.gurkenlabs.tilemap.TmxMapLoader;
import de.gurkenlabs.tilemap.utilities.MapUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.image.ImageProcessing;
import de.gurkenlabs.util.io.FileUtilities;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {
  private static int localIdSequence = 0;
  private static int mapIdSequence;

  private final List<Consumer<Graphics2D>> mapRenderedConsumer;
  private final List<Consumer<Graphics2D>> entitiesRenderedConsumer;
  private final List<Consumer<Graphics2D>> overlayRenderedConsumer;

  private final List<IRenderable> groundRenderable;
  private final List<IRenderable> overlayRenderable;

  private IMap map;

  private final Map<RenderType, Map<Integer, IEntity>> entities;

  private final Map<Integer, IMovableEntity> movableEntities;
  private final Map<Integer, ICombatEntity> combatEntities;
  
  private final List<MapLocation> spawnPoints;
  private final Collection<LightSource> lightSources;
  private final Collection<Trigger> triggers;
  private final Collection<Collider> colliders;
  private Image staticShadowImage;
  private AmbientLight ambientLight;

  private Weather weather;

  private Environment() {
    this.entities = new ConcurrentHashMap<>();
    this.entities.put(RenderType.GROUND, new ConcurrentHashMap<>());
    this.entities.put(RenderType.NORMAL, new ConcurrentHashMap<>());
    this.entities.put(RenderType.OVERLAY, new ConcurrentHashMap<>());

    this.combatEntities = new ConcurrentHashMap<>();
    this.movableEntities = new ConcurrentHashMap<>();

    this.lightSources = new CopyOnWriteArrayList<>();
    this.colliders = new CopyOnWriteArrayList<>();
    this.triggers = new CopyOnWriteArrayList<>();
    this.mapRenderedConsumer = new CopyOnWriteArrayList<>();
    this.entitiesRenderedConsumer = new CopyOnWriteArrayList<>();
    this.overlayRenderedConsumer = new CopyOnWriteArrayList<>();
    this.spawnPoints = new CopyOnWriteArrayList<>();

    this.groundRenderable = new CopyOnWriteArrayList<>();
    this.overlayRenderable = new CopyOnWriteArrayList<>();
  }

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    this();
    IMap loadedMap = Game.getMap(FileUtilities.getFileName(mapPath));
    if (loadedMap == null) {
      final IMapLoader tmxLoader = new TmxMapLoader();
      this.map = tmxLoader.LoadMap(mapPath);
    }else{
      this.map = loadedMap;
    }
    
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  public Environment(IMap map) {
    this();
    this.map = map;
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  @Override
  public void add(final IEntity entity) {
    // set local map id if none is set for the entity
    if(entity.getMapId() == 0){
      entity.setMapId(this.getLocalMapId());
    }
    
    if (entity instanceof ICombatEntity) {
      this.combatEntities.put(entity.getMapId(), (ICombatEntity) entity);
    }

    if (entity instanceof IMovableEntity) {
      this.movableEntities.put(entity.getMapId(), (IMovableEntity) entity);
    }

    if (entity instanceof ICollisionEntity) {
      ICollisionEntity coll = (ICollisionEntity) entity;
      if (coll.hasCollision()) {
        Game.getPhysicsEngine().add(coll);
      }
    }
    
    if(entity instanceof Collider){
      this.colliders.add((Collider)entity);
    }
    
    if(entity instanceof LightSource){
      this.lightSources.add((LightSource)entity);
    }
    
    if(entity instanceof Trigger){
      this.triggers.add((Trigger)entity);
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
  public void addCollisionBox(final IMapObject mapObject) {
    if (!mapObject.getType().equals(MapObjectTypes.COLLISIONBOX)) {
      return;
    }
    Collider col = new Collider();
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    col.setMapId(mapObject.getId());
    this.add(col);
    Game.getPhysicsEngine().add(col.getBoundingBox());
  }

  @Override
  public void addDecorMob(final IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(MapObjectTypes.DECORMOB)) {
      return;
    }
    final DecorMob mob = new DecorMob(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.MOBTYPE));
    mob.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR) != null) {
      mob.setCollisionBoxWidthFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR) != null) {
      mob.setCollisionBoxHeightFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR)));
    }
    mob.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    mob.setMapId(mapObject.getId());
    this.add(mob);
  }

  @Override
  public void addEmitter(final IMapObject mapObject) {
    if (mapObject.getType() != MapObjectTypes.EMITTER) {
      return;
    }
    Emitter emitter = null;
    switch (mapObject.getCustomProperty(MapObjectProperties.EMITTERTYPE)) {
    case "fire":
      emitter = new FireEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      this.add(emitter);
      final LightSource light = new LightSource(this, 50, Color.ORANGE, LightSource.ELLIPSE);
      light.setSize(emitter.getWidth(), emitter.getHeight());
      light.setLocation(emitter.getLocation());
      this.getLightSources().add(light);
      break;
    case "shimmer":
      emitter = new ShimmerEmitter(mapObject.getLocation().x, mapObject.getLocation().y);
      this.add(emitter);
      break;
    }

    if (emitter != null) {
      emitter.setMapId(mapObject.getId());
    }
  }

  @Override
  public void addLightSource(final IMapObject mapObject) {
    if (!mapObject.getType().equals(MapObjectTypes.LIGHTSOURCE)) {
      return;
    }
    final String mapObjectBrightness = mapObject.getCustomProperty(MapObjectProperties.LIGHTBRIGHTNESS);
    final String mapObjectColor = mapObject.getCustomProperty(MapObjectProperties.LIGHTCOLOR);
    if (mapObjectBrightness == null || mapObjectBrightness.isEmpty() || mapObjectColor == null || mapObjectColor.isEmpty()) {
      return;
    }

    final int brightness = Integer.parseInt(mapObjectBrightness);
    final Color color = Color.decode(mapObjectColor);

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
    final LightSource light = new LightSource(this, brightness, new Color(color.getRed(), color.getGreen(), color.getBlue(), brightness), lightType);
    light.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    light.setLocation(mapObject.getLocation());
    light.setMapId(mapObject.getId());
    this.add(light);
  }

  @Override
  public void addMapObject(final IMapObject mapObject) {
    this.addCollisionBox(mapObject);
    this.addLightSource(mapObject);
    this.addSpawnpoint(mapObject);
    this.addProp(mapObject);
    this.addDecorMob(mapObject);
    this.addMob(mapObject);
  }

  @Override
  public void addMob(final IMapObject mapObject) {

  }

  @Override
  public void addProp(final IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(MapObjectTypes.PROP)) {
      return;
    }

    // set map properties by map object
    Material material = mapObject.getCustomProperty(MapObjectProperties.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL));
    final Prop prop = new Prop(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), material);
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

    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR) != null) {
      prop.setCollisionBoxWidthFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR) != null) {
      prop.setCollisionBoxHeightFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR)));
    }
    prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);

    if (mapObject.getCustomProperty(MapObjectProperties.TEAM) != null) {
      prop.setTeam(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.TEAM)));
    }
    prop.setMapId(mapObject.getId());
    this.add(prop);
  }

  @Override
  public void addSpawnpoint(final IMapObject mapObject) {
    if (!mapObject.getType().equals(MapObjectTypes.SPAWNPOINT)) {
      return;
    }

    this.getSpawnPoints().add(new MapLocation(mapObject.getId(), new Point(mapObject.getLocation())));
  }

  @Override
  public void clear() {
    this.dispose(this.getEntities());
    this.getCombatEntities().clear();
    this.getMovableEntities().clear();
    this.getLightSources().clear();
    this.getColliders().clear();
    this.getSpawnPoints().clear();
    this.entities.get(RenderType.GROUND).clear();
    this.entities.get(RenderType.NORMAL).clear();
    this.entities.get(RenderType.OVERLAY).clear();
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
  public Collection<IEntity> getEntities() {
    ArrayList<IEntity> ent = new ArrayList<>();
    ent.addAll(this.entities.get(RenderType.GROUND).values());
    ent.addAll(this.entities.get(RenderType.NORMAL).values());
    ent.addAll(this.entities.get(RenderType.OVERLAY).values());
    return ent;
  }

  @Override
  public Collection<IEntity> getEntities(RenderType renderType) {
    return this.entities.get(renderType).values();
  }

  @Override
  public AmbientLight getAmbientLight() {
    return this.ambientLight;
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

  public Collection<IRenderable> getGroundRenderable() {
    return this.groundRenderable;
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
  public synchronized int getMapId() {
    return ++mapIdSequence;
  }

  @Override
  public Collection<IMovableEntity> getMovableEntities() {
    return this.movableEntities.values();
  }
  
  @Override
  public IEntity get(int mapId) {
    IEntity entity = this.entities.get(RenderType.GROUND).get(mapId);
    if(entity != null){
      return entity;
    }
    
    entity = this.entities.get(RenderType.NORMAL).get(mapId);
    if(entity != null){
      return entity;
    }
    
    entity = this.entities.get(RenderType.OVERLAY).get(mapId);
    if(entity != null){
      return entity;
    }
    
    return null;
  }

  @Override
  public IMovableEntity getMovableEntity(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      return this.movableEntities.get(mapId);
    }

    return null;
  }

  public List<IRenderable> getOverlayRenderable() {
    return this.overlayRenderable;
  }

  @Override
  public List<MapLocation> getSpawnPoints() {
    return this.spawnPoints;
  }
  
  @Override
  public Collection<Trigger> getTriggers() {
    return this.triggers;
  }

  public Image getStaticShadowImage() {
    return this.staticShadowImage;
  }

  @Override
  public WeatherType getWeather() {
    return this.weather == null ? WeatherType.Clear : this.weather.getType();
  }

  @Override
  public void init() {
    this.loadMapObjects();
    this.addStaticShadows();
    this.addAmbientLight();
  }

  @Override
  public void onEntitiesRendered(final Consumer<Graphics2D> consumer) {
    this.entitiesRenderedConsumer.add(consumer);
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
  public void remove(final int mapId) {
    this.movableEntities.remove(mapId);
    this.combatEntities.remove(mapId);

    this.removeEntity(mapId, RenderType.NORMAL);
    this.removeEntity(mapId, RenderType.GROUND);
    this.removeEntity(mapId, RenderType.OVERLAY);
  }

  @Override
  public void remove(IEntity entity) {
    this.movableEntities.values().remove(entity);
    this.combatEntities.values().remove(entity);
    this.entities.get(entity.getRenderType()).remove(entity);
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

    if (this.weather != null) {
      this.weather.render(g);
    }

    for (final IRenderable rend : this.getOverlayRenderable()) {
      rend.render(g);
    }

    this.informConsumers(g, this.overlayRenderedConsumer);

    g.scale(1.0 / Game.getInfo().getRenderScale(), 1.0 / Game.getInfo().getRenderScale());
  }

  @Override
  public void setWeather(final WeatherType weather) {
    switch (weather) {
    case Rain:
      this.weather = new RainEmitter();
      break;
    case Snow:
      this.weather = new SnowEmitter();
      break;
    case Clear:
    default:
      this.weather = null;
      break;
    }

    if (weather != null) {
      this.weather.activate(Game.getLoop());
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

  private void removeEntity(int mapId, RenderType renderType) {
    IEntity remove = null;
    for (IEntity e : this.entities.get(renderType).values()) {
      if (e.getMapId() == mapId) {
        // Remove the current element from the iterator and the list.
        remove = e;
        break;
      }
    }
    
    if(remove == null){
      return;
    }

    this.entities.get(renderType).remove(remove);
  }
  private void informConsumers(final Graphics2D g, final List<Consumer<Graphics2D>> consumers) {
    for (final Consumer<Graphics2D> consumer : consumers) {
      consumer.accept(g);
    }
  }
  
  private void addStaticShadows() {
    // build map specific cache key, respecting the lights and color
    // final StringBuilder sb = new StringBuilder();
    // for (final IMapObject col : this.getCollisionBoxes()) {
    // sb.append(col.getId() + "_" + col.getCollisionBox());
    // }
    //
    // final String cacheKey = "STATICSHADOWS_" +
    // this.getMap().getName().replaceAll("[\\/]", "-") + "_" +
    // sb.toString().hashCode();
    // final Image cachedImg = ImageCache.IMAGES.get(cacheKey);
    // if (cachedImg != null) {
    // this.staticShadowImage = cachedImg;
    // return;
    // }

    final int shadowOffset = 10;
    final List<Path2D> staticShadows = new ArrayList<>();
    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    for (final IMapObject col : this.getCollisionBoxMapObjects()) {
      final double shadowX = col.getCollisionBox().getX();
      final double shadowY = col.getCollisionBox().getY();
      final double shadowWidth = col.getCollisionBox().getWidth();
      final double shadowHeight = col.getCollisionBox().getHeight();

      final String shadowType = col.getCustomProperty(MapObjectProperties.SHADOWTYPE);
      if (shadowType == null) {
        continue;
      }
      final Path2D parallelogram = new Path2D.Double();
      if (shadowType.equals(Collider.StaticShadowType.DOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.DOWNLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.DOWNRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.LEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.LEFTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.LEFTRIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.RIGHTLEFT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.RIGHT)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.RIGHTDOWN)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(Collider.StaticShadowType.NOOFFSET)) {
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
    // ImageCache.IMAGES.put(cacheKey, img);
  }

  private void dispose(final Collection<? extends IEntity> entities) {
    for (final IEntity entity : entities) {
      if (entity instanceof IUpdateable) {
        Game.getLoop().unregisterFromUpdate((IUpdateable) entity);
      }

      if (entity.getAnimationController() != null) {
        entity.getAnimationController().dispose();
      }

      if (entity instanceof IMovableEntity) {
        if (((IMovableEntity) entity).getMovementController() != null) {
          Game.getLoop().unregisterFromUpdate(((IMovableEntity) entity).getMovementController());
        }
      }
    }
  }
  
  private List<IMapObject> getCollisionBoxMapObjects() {
    final List<IMapObject> collisionBoxes = new ArrayList<>();
    for (final IMapObjectLayer shapeLayer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject obj : shapeLayer.getMapObjects()) {
        if (obj.getType() == null || obj.getType().isEmpty()) {
          continue;
        }

        if (obj.getType().equals(MapObjectTypes.COLLISIONBOX)) {
          collisionBoxes.add(obj);
        }
      }
    }

    return collisionBoxes;
  }

  private void addAmbientLight() {
    final String alphaProp = this.getMap().getCustomProperty(MapProperty.AMBIENTALPHA);
    final String colorProp = this.getMap().getCustomProperty(MapProperty.AMBIENTCOLOR);
    int ambientAlpha = 0;
    Color ambientColor = Color.WHITE;
    try {
      ambientAlpha = Integer.parseInt(alphaProp);
      ambientColor = Color.decode(colorProp);
    } catch (final NumberFormatException e) {
    }

    this.ambientLight = new AmbientLight(this, ambientColor, ambientAlpha);
  }
}