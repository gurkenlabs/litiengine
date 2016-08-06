/***************************************************************
 * Copyright (c) 2014 - 2015 , gurkenlabs, All rights reserved *
 ***************************************************************/
package de.gurkenlabs.litiengine.tiled.tmx;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.Point;
import java.awt.Shape;
import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.attributes.AttributeModifier;
import de.gurkenlabs.litiengine.attributes.Modification;
import de.gurkenlabs.litiengine.entities.DecorMob;
import de.gurkenlabs.litiengine.entities.ICombatEntity;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IMovableCombatEntity;
import de.gurkenlabs.litiengine.entities.IMovableEntity;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.ImageCache;
import de.gurkenlabs.litiengine.graphics.LightSource;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.tiled.tmx.IMap;
import de.gurkenlabs.tiled.tmx.IMapLoader;
import de.gurkenlabs.tiled.tmx.IMapObject;
import de.gurkenlabs.tiled.tmx.IMapObjectLayer;
import de.gurkenlabs.tiled.tmx.TmxMapLoader;
import de.gurkenlabs.tiled.tmx.utilities.MapUtilities;
import de.gurkenlabs.util.geom.GeometricUtilities;
import de.gurkenlabs.util.image.ImageProcessing;

/**
 * The Class MapContainerBase.
 */
public class Environment implements IEnvironment {

  private static int localIdSequence = 0;
  private static int mapIdSequence;
  /** The map. */
  private final IMap map;

  private final Map<Integer, ICombatEntity> combatEntities;
  private final List<Prop> props;
  private final Map<Integer, IMovableEntity> movableEntities;

  private final List<LightSource> lightSources;
  private Image staticShadowImage;
  private Image ambientImage;
  private Color ambientColor;
  private int ambientAlpha;

  /**
   * Instantiates a new map container base.
   *
   * @param map
   *          the map
   */
  public Environment(final String mapPath) {
    final IMapLoader tmxLoader = new TmxMapLoader();
    this.map = tmxLoader.LoadMap(mapPath);
    mapIdSequence = MapUtilities.getMaxMapId(this.getMap());

    this.combatEntities = new ConcurrentHashMap<>();
    this.movableEntities = new ConcurrentHashMap<>();
    this.lightSources = new CopyOnWriteArrayList<>();
    this.props = new CopyOnWriteArrayList<>();
  }

  @Override
  public void add(final int mapId, final IMovableCombatEntity entity) {
    this.addCombatEntity(mapId, entity);
    this.addMovableEntity(mapId, entity);
  }

  private void addAmbientLight() {
    final String alphaProp = this.getMap().getCustomProperty("AMBIENTALPHA");
    final String colorProp = this.getMap().getCustomProperty("AMBIENTLIGHT");
    this.ambientAlpha = Integer.parseInt(alphaProp);
    this.ambientColor = Color.decode(colorProp);

    this.createAmbientImage();
  }

  public void addCollisionBoxes(final IMapObject mapObject) {
    if (mapObject.getType().equals(MapObjectTypes.COLLISIONBOX)) {
      Game.getPhysicsEngine().add(mapObject.getCollisionBox());
    }
  }

  @Override
  public void addCombatEntity(final int mapId, final ICombatEntity entity) {
    this.combatEntities.put(mapId, entity);
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
    this.addMovableEntity(mapObject.getId(), mob);
    if (mob.hasCollision()) {
      Game.getPhysicsEngine().add(mob);
    }
  }

  @Override
  public void addEffect(final IMapObject mapObject) {
    // TODO Auto-generated method stub

  }

  protected void addMapObject(final IMapObject mapObject) {
    if (mapObject.getType().equals(MapObjectTypes.LIGHTSOURCE)) {
      final String propBrightness = mapObject.getCustomProperty(MapObjectProperties.LIGHTBRIGHTNESS);
      final String propColor = mapObject.getCustomProperty(MapObjectProperties.LIGHTCOLOR);
      if (propBrightness == null || propBrightness.isEmpty() || propColor == null || propColor.isEmpty()) {
        return;
      }

      final int brightness = Integer.parseInt(propBrightness);
      final Color color = Color.decode(propColor);

      this.getLightSources().add(new LightSource(this, new Point(mapObject.getLocation()), (int) (mapObject.getDimension().getWidth() / 2.0), brightness, new Color(color.getRed(), color.getGreen(), color.getBlue(), brightness)));
    }

    this.addCollisionBoxes(mapObject);
  }

  @Override
  public void addMob(final IMapObject mapObject) {
    // TODO Auto-generated method stub

  }

  @Override
  public void addMovableEntity(final int mapId, final IMovableEntity entity) {
    this.movableEntities.put(mapId, entity);
  }

  @Override
  public void addProp(final IMapObject mapObject) {
    if (!mapObject.getType().equalsIgnoreCase(MapObjectTypes.PROP)) {
      return;
    }
    final Prop prop = new Prop(mapObject.getLocation(), mapObject.getCustomProperty(MapObjectProperties.SPRITESHEETNAME), Material.valueOf(mapObject.getCustomProperty(MapObjectProperties.MATERIAL)));
    if (!mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE).isEmpty()) {
      prop.setIndestructible(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.INDESTRUCTIBLE)));
    }
    if (!prop.isIndestructible()) {
      prop.getAnimationController().add(PropAnimationController.createAnimation(prop, PropState.Damaged));
      prop.getAnimationController().add(PropAnimationController.createAnimation(prop, PropState.Destroyed));
    }
    prop.getAttributes().getHealth().addMaxModifier(new AttributeModifier<>(Modification.Set, Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.HEALTH))));
    prop.setCollision(Boolean.valueOf(mapObject.getCustomProperty(MapObjectProperties.COLLISION)));
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR) != null) {
      prop.setCollisionBoxWidthFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXWIDTHFACTOR)));
    }
    if (mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR) != null) {
      prop.setCollisionBoxHeightFactor(Float.parseFloat(mapObject.getCustomProperty(MapObjectProperties.COLLISIONBOXHEIGHTFACTOR)));
    }
    prop.setSize(mapObject.getDimension().width, mapObject.getDimension().height);
    this.getProps().add(prop);
    this.addCombatEntity(mapObject.getId(), prop);
    if (mapObject.getCustomProperty(MapObjectProperties.TEAM) != null) {
      prop.setTeam(Integer.parseInt(mapObject.getCustomProperty(MapObjectProperties.TEAM)));
    }
    if (prop.hasCollision()) {
      Game.getPhysicsEngine().add(prop);
    }
  }

  private void addStaticShadows() {
    // build map specific cache key, respecting the lights and color
    final StringBuilder sb = new StringBuilder();
    for (final IMapObject col : this.getShadowBoxes()) {
      sb.append(col.getId() + "_" + col.getCollisionBox());
    }

    final String cacheKey = "STATICSHADOWS_" + this.getMap().getName().replaceAll("[\\/]", "-") + "_" + sb.toString().hashCode();
    final Image cachedImg = ImageCache.IMAGES.get(cacheKey);
    if (cachedImg != null) {
      this.staticShadowImage = cachedImg;
      return;
    }

    final int shadowOffset = 10;
    final List<Path2D> staticShadows = new ArrayList<>();
    // check if the collision boxes have shadows. if so, determine which
    // shadow is needed, create the shape and add it to the
    // list of static shadows.
    for (final IMapObject col : this.getShadowBoxes()) {
      final double shadowX = col.getCollisionBox().getX();
      final double shadowY = col.getCollisionBox().getY();
      final double shadowWidth = col.getCollisionBox().getWidth();
      final double shadowHeight = col.getCollisionBox().getHeight();

      final String shadowType = col.getCustomProperty(MapObjectProperties.SHADOWTYPE);
      final String down = "DOWN";
      final String downLeft = "DOWNLEFT";
      final String downRight = "DOWNRIGHT";
      final String left = "LEFT";
      final String leftDown = "LEFTDOWN";
      final String leftRight = "LEFTRIGHT";
      final String rightLeft = "RIGHTLEFT";
      final String right = "RIGHT";
      final String rightDown = "RIGHTDOWN";
      final String noOffset = "NOOFFSET";

      final Path2D parallelogram = new Path2D.Double();
      if (shadowType.equals(down)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(downLeft)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(downRight)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight + shadowOffset);
        parallelogram.closePath();
      } else if (shadowType.equals(left)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(leftDown)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(leftRight)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(rightLeft)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth - shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(right)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(rightDown)) {
        parallelogram.moveTo(shadowX, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight);
        parallelogram.lineTo(shadowX + shadowWidth, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX + shadowOffset / 2, shadowY + shadowHeight + shadowOffset);
        parallelogram.lineTo(shadowX, shadowY + shadowHeight);
        parallelogram.closePath();
      } else if (shadowType.equals(noOffset)) {
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

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getMap().getSizeInPixles().getWidth(), (int) this.getMap().getSizeInPixles().getHeight());
    final Graphics2D g = (Graphics2D) img.getGraphics();
    g.setColor(new Color(0, 0, 0, 75));

    final Area ar = new Area();
    for (final Path2D staticShadow : staticShadows) {
      ar.add(new Area(staticShadow));
    }

    for (final LightSource light : this.getLightSources()) {
      final Ellipse2D lightCircle = new Ellipse2D.Double(light.getLocation().getX(), light.getLocation().getY(), light.getRadius() * 2, light.getRadius() * 2);
      ar.subtract(new Area(lightCircle));
    }

    g.fill(ar);
    g.dispose();

    this.staticShadowImage = img;
    ImageCache.IMAGES.putPersistent(cacheKey, img);
  }

  public void clear() {
    final List<IEntity> allEntities = Stream.concat(this.combatEntities.values().stream(), this.movableEntities.values().stream()).collect(Collectors.toList());
    for (final IEntity e : allEntities) {
      if (e.getAnimationController() != null) {
        e.getAnimationController().dispose();
      }
    }

    this.combatEntities.clear();
    this.movableEntities.clear();
    this.lightSources.clear();
  }

  public void createAmbientImage() {
    final Color col = new Color(this.getAmbientColor().getRed(), this.getAmbientColor().getGreen(), this.getAmbientColor().getBlue(), this.getAmbientAlpha());
    final StringBuilder sb = new StringBuilder();
    for (final LightSource light : this.getLightSources()) {
      light.deactivate();
      sb.append(light.getRadius() + "_" + light.getLocation().getX() + "_" + light.getLocation().getY());
    }

    // build map specific cache key, respecting the lights and color
    final String cacheKey = "AMBIENT_" + this.getMap().getName().replaceAll("[\\/]", "-") + "_" + sb.toString().hashCode() + "_" + this.getAmbientColor().getRed() + "_" + this.getAmbientColor().getGreen() + "_" + this.getAmbientColor().getBlue() + "_" + this.getAmbientAlpha();
    final Image cachedImg = ImageCache.IMAGES.get(cacheKey);
    if (cachedImg != null) {
      this.ambientImage = cachedImg;
      return;
    }

    // create large rectangle and crop lights from it
    final Area ar = new Area(new Rectangle2D.Double(0, 0, this.getMap().getSizeInPixles().getWidth(), this.getMap().getSizeInPixles().getHeight()));
    for (final LightSource light : this.getLightSources()) {
      final Ellipse2D lightCircle = new Ellipse2D.Double(light.getLocation().getX(), light.getLocation().getY(), light.getRadius() * 2, light.getRadius() * 2);
      ar.subtract(new Area(lightCircle));
    }

    final BufferedImage img = ImageProcessing.getCompatibleImage((int) this.getMap().getSizeInPixles().getWidth(), (int) this.getMap().getSizeInPixles().getHeight());
    final Graphics2D g = (Graphics2D) img.getGraphics();
    g.setColor(col);
    g.fill(ar);

    // apply 2 step gradient for all lights
    for (final LightSource light : this.getLightSources()) {
      // set gradient step size, relative to the light radius
      final double LIGHT_GRADIENT_STEP = light.getRadius() * 0.15;
      final Ellipse2D lightCircle = new Ellipse2D.Double(light.getLocation().getX(), light.getLocation().getY(), light.getRadius() * 2, light.getRadius() * 2);
      final Ellipse2D midLightCircle = new Ellipse2D.Double(light.getLocation().getX() + LIGHT_GRADIENT_STEP, light.getLocation().getY() + LIGHT_GRADIENT_STEP, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP) * 2);
      final Ellipse2D smallLightCircle = new Ellipse2D.Double(light.getLocation().getX() + LIGHT_GRADIENT_STEP * 2, light.getLocation().getY() + LIGHT_GRADIENT_STEP * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2, (light.getRadius() - LIGHT_GRADIENT_STEP * 2) * 2);

      final Area mid = new Area(lightCircle);
      mid.subtract(new Area(midLightCircle));
      g.setColor(new Color(this.getAmbientColor().getRed(), this.getAmbientColor().getGreen(), this.getAmbientColor().getBlue(), (int) (this.getAmbientAlpha() * 0.5)));
      g.fill(mid);

      final Area small = new Area(midLightCircle);
      small.subtract(new Area(smallLightCircle));
      g.setColor(new Color(this.getAmbientColor().getRed(), this.getAmbientColor().getGreen(), this.getAmbientColor().getBlue(), (int) (this.getAmbientAlpha() * 0.25)));
      g.fill(small);
    }

    g.dispose();
    this.ambientImage = img;
    ImageCache.IMAGES.putPersistent(cacheKey, img);
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

    // for rectangle we can jsut use the intersects method
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

  public int getAmbientAlpha() {
    return this.ambientAlpha;
  }

  public Color getAmbientColor() {
    return this.ambientColor;
  }

  public Image getAmbientShape() {
    return this.ambientImage;
  }

  public List<IMapObject> getCollisionBoxes() {
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
  public List<LightSource> getLightSources() {
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
  public List<Prop> getProps() {
    return this.props;
  }

  public List<IMapObject> getShadowBoxes() {
    final List<IMapObject> shadowBoxes = new ArrayList<>();
    for (final IMapObjectLayer shapeLayer : this.getMap().getMapObjectLayers()) {
      for (final IMapObject obj : shapeLayer.getMapObjects()) {
        if (obj.getType() == null || obj.getType().isEmpty()) {
          continue;
        }

        if (obj.getType().equals(MapObjectTypes.SHADOWBOX)) {
          shadowBoxes.add(obj);
        }
      }
    }

    return shadowBoxes;
  }

  public Image getStaticShadowImage() {
    return this.staticShadowImage;
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
  public void remove(final int mapId) {
    if (this.movableEntities.containsKey(mapId)) {
      this.movableEntities.remove(mapId);
    }

    if (this.combatEntities.containsKey(mapId)) {
      this.combatEntities.remove(mapId);
    }
  }

  public void setAmbientAlpha(int ambientAlpha) {
    if (ambientAlpha < 0) {
      ambientAlpha = 0;
    }

    this.ambientAlpha = Math.min(ambientAlpha, 255);
    this.createAmbientImage();
  }

  public void setAmbientColor(final Color color) {
    this.ambientColor = color;
    this.createAmbientImage();
  }
}