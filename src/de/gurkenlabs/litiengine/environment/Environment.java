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
import de.gurkenlabs.litiengine.entities.Collider.StaticShadowType;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.DecorMob.MovementBehaviour;
import de.gurkenlabs.litiengine.entities.ICollisionEntity;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.Trigger;
import de.gurkenlabs.litiengine.environment.tilemap.MapLocation;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperties;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
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
import de.gurkenlabs.litiengine.graphics.particles.xml.CustomEmitter;
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

  private AmbientLight ambientLight;
  private final Collection<Collider> colliders;
  private final Map<Integer, ICombatEntity> combatEntities;

  private final Map<RenderType, Map<Integer, IEntity>> entities;
  private final List<Consumer<Graphics2D>> entitiesRenderedConsumer;
  private final List<Consumer<Graphics2D>> overlayRenderedConsumer;
  private final List<Consumer<Graphics2D>> mapRenderedConsumer;

  private final List<IRenderable> groundRenderable;
  private final Collection<LightSource> lightSources;

  private IMap map;

  private final Map<Integer, IMovableEntity> movableEntities;

  private final List<IRenderable> overlayRenderable;

  private final List<MapLocation> spawnPoints;
  private Image staticShadowImage;
  private final Collection<Trigger> triggers;
  private CopyOnWriteArrayList<Narrator> narrators;

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

  public Environment(final IMap map) {
    this();
    this.map = map;
    this.setMapTitleAndDescription();
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
    this.setMapTitleAndDescription();
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());
    Game.getPhysicsEngine().setBounds(new Rectangle(this.getMap().getSizeInPixels()));
  }

  @Override
  public void add(final IEntity entity) {
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

    if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      if (coll.hasCollision()) {
        Game.getPhysicsEngine().add(coll);
      }
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

    if (ambientAlpha > 0) {
      this.ambientLight = new AmbientLight(ambientColor, ambientAlpha);
    }
  }

  protected void addCollisionBox(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.COLLISIONBOX) {
      return;
    }
    final Collider col = new Collider();
    col.setLocation(mapObject.getLocation());
    col.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    col.setMapId(mapObject.getId());

    String shadowType = mapObject.getCustomProperty(MapObjectProperties.SHADOWTYPE);
    if (shadowType != null && !shadowType.isEmpty()) {
      col.setShadowType(Collider.StaticShadowType.valueOf(shadowType));
    }

    this.add(col);
    Game.getPhysicsEngine().add(col.getBoundingBox());
  }

  protected void addDecorMob(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.DECORMOB) {
      return;
    }

    short velocity = (short) (100 / Game.getInfo().getRenderScale());
    if (mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY) != null) {
      velocity = Short.parseShort(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_VELOCITY));
    }

    final DecorMob mob = new DecorMob(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), MovementBehaviour.get(mapObject.getCustomProperty(MapObjectProperties.DECORMOB_BEHAVIOUR)), velocity);
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

  protected void addEmitter(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.EMITTER) {
      return;
    }

    Emitter emitter = null;
    String emitterType = mapObject.getCustomProperty(MapObjectProperties.EMITTERTYPE);
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
    this.add(light);
  }

  @Override
  public void addMapObject(final IMapObject mapObject) {
    this.addCollisionBox(mapObject);
    this.addLightSource(mapObject);
    this.addSpawnpoint(mapObject);
    this.addProp(mapObject);
    this.addEmitter(mapObject);
    this.addDecorMob(mapObject);
    this.addMob(mapObject);
    this.addTrigger(mapObject);
  }

  protected void addMob(final IMapObject mapObject) {

  }

  @Override
  public void addNarrator(final String name) {
    this.addNarrator(name, 0);
  }

  @Override
  public void addNarrator(final String name, final int layout) {
    if (this.getNarrators() == null) {
      this.narrators = new CopyOnWriteArrayList<>();
    }
    Narrator newNarrator = new Narrator(this, name, layout);
    this.getNarrators().add(newNarrator);
  }

  protected void addProp(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.PROP) {
      return;
    }

    // set map properties by map object
    final Material material = mapObject.getCustomProperty(MapObjectProperties.MATERIAL) == null ? Material.UNDEFINED : Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL));
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

  protected void addSpawnpoint(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.SPAWNPOINT) {
      return;
    }

    this.getSpawnPoints().add(new MapLocation(mapObject.getId(), new Point(mapObject.getLocation())));
  }

  private void addStaticShadows() {
    final int shadowOffset = 10;
    final List<Path2D> staticShadows = new ArrayList<>();
    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    for (final IMapObject col : this.getCollisionBoxMapObjects()) {
      final double shadowX = col.getBoundingBox().getX();
      final double shadowY = col.getBoundingBox().getY();
      final double shadowWidth = col.getBoundingBox().getWidth();
      final double shadowHeight = col.getBoundingBox().getHeight();

      final Collider.StaticShadowType shadowType = StaticShadowType.get(col.getCustomProperty(MapObjectProperties.SHADOWTYPE));
      if (shadowType == StaticShadowType.NONE) {
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
  }

  protected void addTrigger(final IMapObject mapObject) {
    if (MapObjectType.get(mapObject.getType()) != MapObjectType.TRIGGER) {
      return;
    }

    final String message = mapObject.getCustomProperty(MapObjectProperties.TRIGGERMESSAGE);
    final Trigger trigger = new Trigger(mapObject.getName(), message);
    trigger.setMapId(mapObject.getId());
    trigger.setCollisionBoxHeightFactor(1);
    trigger.setCollisionBoxWidthFactor(1);
    trigger.setSize((float) mapObject.getDimension().getWidth(), (float) mapObject.getDimension().getHeight());
    trigger.setLocation(new Point2D.Double(mapObject.getLocation().x, mapObject.getLocation().y));
    this.add(trigger);
  }

  @Override
  public void clear() {
    Game.getPhysicsEngine().clear();
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
  public AmbientLight getAmbientLight() {
    return this.ambientLight;
  }

  @Override
  public Collection<Collider> getColliders() {
    return this.colliders;

  }

  private List<IMapObject> getCollisionBoxMapObjects() {
    final List<IMapObject> collisionBoxes = new ArrayList<>();
    for (final IMapObjectLayer shapeLayer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject obj : shapeLayer.getMapObjects()) {
        if (obj.getType() == null || obj.getType().isEmpty()) {
          continue;
        }

        if (MapObjectType.get(obj.getType()) == MapObjectType.COLLISIONBOX) {
          collisionBoxes.add(obj);
        }
      }
    }

    return collisionBoxes;
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
  public IMovableEntity getMovableEntity(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      return this.movableEntities.get(mapId);
    }

    return null;
  }

  @Override
  public Narrator getNarrator(final int index) {
    return this.getNarrators().get(index);
  }

  @Override
  public Narrator getNarrator(final String name) {
    for (Narrator narrator : this.getNarrators()) {
      if (narrator.getName() == name) {
        return narrator;
      }
    }
    return null;
  }

  @Override
  public CopyOnWriteArrayList<Narrator> getNarrators() {
    return this.narrators;
  }

  public List<IRenderable> getOverlayRenderable() {
    return this.overlayRenderable;
  }

  @Override
  public List<MapLocation> getSpawnPoints() {
    return this.spawnPoints;
  }

  public Image getStaticShadowImage() {
    return this.staticShadowImage;
  }

  @Override
  public Collection<Trigger> getTriggers() {
    return this.triggers;
  }

  @Override
  public WeatherType getWeather() {
    return this.weather == null ? WeatherType.Clear : this.weather.getType();
  }

  private void informConsumers(final Graphics2D g, final List<Consumer<Graphics2D>> consumers) {
    for (final Consumer<Graphics2D> consumer : consumers) {
      consumer.accept(g);
    }
  }

  @Override
  public void init() {
    this.loadMapObjects();
    this.addStaticShadows();
    this.addAmbientLight();
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
  public void remove(final IEntity entity) {
    if(entity == null){
      return;
    }
    
    this.entities.get(entity.getRenderType()).entrySet().removeIf(e -> e.getValue().getMapId() == entity.getMapId());

    if (entity instanceof ICollisionEntity) {
      final ICollisionEntity coll = (ICollisionEntity) entity;
      Game.getPhysicsEngine().remove(coll);
    }

    if (entity instanceof Collider) {
      this.colliders.remove(entity);
    }

    if (entity instanceof LightSource) {
      this.lightSources.remove(entity);
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
  }

  @Override
  public void remove(final int mapId) {
    this.getSpawnPoints().removeIf(x -> x.getMapId() == mapId);
    final IEntity ent = this.get(mapId);
    if (ent == null) {
      System.out.println("could not remove entity with id '" + mapId + "' from the environment, because there is no entity with such a map ID.");
      return;
    }

    this.remove(ent);
  }

  @Override
  public void reloadFromMap(int mapId) {
    this.remove(mapId);
    this.loadFromMap(mapId);
  }

  @Override
  public void loadFromMap(int mapId) {
    for (final IMapObjectLayer layer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject mapObject : layer.getMapObjects()) {
        if (mapObject.getType() == null || mapObject.getType().isEmpty() || mapObject.getId() != mapId) {
          continue;
        }

        this.addMapObject(mapObject);
        if (MapObjectType.get(mapObject.getType()) == MapObjectType.COLLISIONBOX) {
          this.addStaticShadows();
        }
        break;
      }
    }
  }

  @Override
  public void removeNarrator(final String name) {
    if (this.getNarrators() == null) {
      return;
    }
    for (Narrator narrator : this.getNarrators()) {
      if (narrator.getName() == name) {
        this.getNarrators().remove(narrator);
      }
    }

  }

  @Override
  public void removeNarrator(final int index) {
    if (this.getNarrators() == null || this.getNarrators().size() <= index) {
      return;
    }
    this.getNarrators().remove(index);
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
    if (this.getNarrators() == null || this.getNarrators().isEmpty()) {
      return;
    }
    for (final Narrator narr : this.getNarrators()) {
      narr.render(g);
    }

  }

  private void setMapTitleAndDescription() {

    final String mapTitle = this.getMap().getCustomProperty(MapProperty.MAP_TITLE);
    this.getMap().setTitle(mapTitle);

    final String mapDescription = this.getMap().getCustomProperty(MapProperty.MAP_DESCRIPTION);
    this.getMap().setDescription(mapDescription);

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
}