package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.entities.Rotation;
import de.gurkenlabs.litiengine.environment.tilemap.IMapObject;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.util.Imaging;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.view.components.CreaturePanel;
import de.gurkenlabs.utiliti.view.components.PropPanel;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import javax.swing.Icon;
import javax.swing.ImageIcon;

/**
 * Utility that selects a single representative ("base") variant per logical sprite family (props &amp; creatures). The selection logic mirrors what the
 * AssetTree shows:
 * <ul>
 *   <li>Props: prefer explicit intact (prop-id-intact), else plain (prop-id), else first variant.</li>
 *   <li>Creatures: prefer idle-down, idle-right, idle-left, idle-up; else any idle-*; else first variant.</li>
 * </ul>
 * Returned maps preserve first-seen insertion order of base identifiers.
 */
public final class SpriteVariantSelector {
  private SpriteVariantSelector() {
  }

  /**
   * Select representative prop sprites from resource definitions.
   *
   * @param resources spritesheet resources
   * @return map baseIdentifier -> chosen SpritesheetResource
   */
  public static Map<String, SpritesheetResource> selectBasePropResources(Collection<SpritesheetResource> resources) {
    Map<String, List<SpritesheetResource>> grouped = new LinkedHashMap<>();
    for (SpritesheetResource res : resources) {
      if (res == null) {
        continue;
      }
      String name = res.getName();
      String identifier = PropPanel.getIdentifierBySpriteName(name);
      if (identifier == null) {
        continue;
      }
      grouped.computeIfAbsent(identifier, k -> new ArrayList<>()).add(res);
    }
    Map<String, SpritesheetResource> result = new LinkedHashMap<>();
    for (Map.Entry<String, List<SpritesheetResource>> entry : grouped.entrySet()) {
      result.put(entry.getKey(), choosePropVariant(entry.getKey(), entry.getValue()));
    }
    return result;
  }

  /**
   * Select representative creature sprites from resource definitions.
   *
   * @param resources spritesheet resources
   * @return map baseName -> chosen SpritesheetResource
   */
  public static Map<String, SpritesheetResource> selectBaseCreatureResources(Collection<SpritesheetResource> resources) {
    Map<String, List<SpritesheetResource>> grouped = new LinkedHashMap<>();
    for (SpritesheetResource res : resources) {
      if (res == null) {
        continue;
      }
      String name = res.getName();
      String base = CreaturePanel.getCreatureSpriteName(name);
      if (base == null) {
        continue;
      }
      grouped.computeIfAbsent(base, k -> new ArrayList<>()).add(res);
    }
    Map<String, SpritesheetResource> result = new LinkedHashMap<>();
    for (Map.Entry<String, List<SpritesheetResource>> entry : grouped.entrySet()) {
      result.put(entry.getKey(), chooseCreatureVariant(entry.getKey(), entry.getValue()));
    }
    return result;
  }

  /**
   * Convenience for panels working with already loaded Spritesheet instances (engine objects).
   */
  public static Map<String, String> selectBasePropSpriteNames(Collection<Spritesheet> sheets) {
    Map<String, List<String>> grouped = new LinkedHashMap<>();
    for (Spritesheet s : sheets) {
      if (s == null) {
        continue;
      }
      String id = PropPanel.getIdentifierBySpriteName(s.getName());
      if (id == null) {
        continue;
      }
      grouped.computeIfAbsent(id, k -> new ArrayList<>()).add(s.getName());
    }
    Map<String, String> result = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
      result.put(entry.getKey(), choosePropVariantName(entry.getKey(), entry.getValue()));
    }
    return result;
  }

  /**
   * Convenience for panels working with already loaded Spritesheet instances (engine objects).
   */
  public static Map<String, String> selectBaseCreatureSpriteNames(Collection<Spritesheet> sheets) {
    Map<String, List<String>> grouped = new LinkedHashMap<>();
    for (Spritesheet s : sheets) {
      if (s == null) {
        continue;
      }
      String base = CreaturePanel.getCreatureSpriteName(s.getName());
      if (base == null) {
        continue;
      }
      grouped.computeIfAbsent(base, k -> new ArrayList<>()).add(s.getName());
    }
    Map<String, String> result = new LinkedHashMap<>();
    for (Map.Entry<String, List<String>> entry : grouped.entrySet()) {
      result.put(entry.getKey(), chooseCreatureVariantName(entry.getKey(), entry.getValue()));
    }
    return result;
  }

  private static SpritesheetResource choosePropVariant(String id, List<SpritesheetResource> variants) {
    String plain = PropAnimationController.PROP_IDENTIFIER + id;
    String intact = plain + '-' + PropState.INTACT.spriteString();
    SpritesheetResource plainRes = null;
    for (SpritesheetResource v : variants) {
      if (v.getName().equals(intact)) {
        return v; // explicit intact preferred
      }
      if (plainRes == null && v.getName().equals(plain)) {
        plainRes = v; // remember plain
      }
    }
    if (plainRes != null) {
      return plainRes; // treat plain as intact if no explicit
    }
    return variants.getFirst(); // fallback
  }

  private static String choosePropVariantName(String id, List<String> variants) {
    String plain = PropAnimationController.PROP_IDENTIFIER + id;
    String intact = plain + '-' + PropState.INTACT.spriteString();

    String plainName = null;
    for (String name : variants) {
      if (Objects.equals(name, intact)) {
        return name;
      }
      if (plainName == null && Objects.equals(name, plain)) {
        plainName = name;
      }
    }
    if (plainName != null) {
      return plainName;
    }
    return variants.getFirst();
  }

  private static SpritesheetResource chooseCreatureVariant(String base, List<SpritesheetResource> variants) {
    // Prefer idle animations by directional priority using Direction enum
    Direction[] pref = {Direction.DOWN, Direction.RIGHT, Direction.LEFT, Direction.UP};
    for (Direction dir : pref) {
      String expected = base + '-' + CreatureAnimationState.IDLE.spriteString() + '-'
        + dir.name().toLowerCase(Locale.ROOT);
      for (SpritesheetResource v : variants) {
        if (expected.equals(v.getName())) {
          return v;
        }
      }
    }
    // Fallback: any idle variant
    String idleToken = "-" + CreatureAnimationState.IDLE.spriteString() + "-";
    for (SpritesheetResource v : variants) {
      if (v.getName().contains(idleToken)) {
        return v;
      }
    }
    String moveToken = "-" + CreatureAnimationState.MOVE.spriteString() + "-";
    for (SpritesheetResource v : variants) {
      if (v.getName().contains(moveToken)) {
        return v;
      }
    }
    String walkToken = "-" + CreaturePanel.WALK_SPRITE_TOKEN + "-";
    for (SpritesheetResource v : variants) {
      if (v.getName().contains(walkToken)) {
        return v;
      }
    }
    // Final fallback: first available variant (order preserved by grouping)
    return variants.getFirst();
  }

  private static String chooseCreatureVariantName(String base, List<String> variants) {
    Direction[] dirPref = {Direction.DOWN, Direction.RIGHT, Direction.LEFT, Direction.UP};
    for (Direction dir : dirPref) {
      String expected = base + '-' + CreatureAnimationState.IDLE.spriteString() + '-'
        + dir.name().toLowerCase(Locale.ROOT);
      for (String v : variants) {
        if (v.equals(expected)) {
          return v;
        }
      }
    }
    for (String v : variants) {
      if (v.contains("-" + CreatureAnimationState.IDLE.spriteString() + "-")) {
        return v;
      }
    }
    for (String v : variants) {
      if (v.contains("-" + CreatureAnimationState.MOVE.spriteString() + "-")) {
        return v;
      }
    }
    for (String v : variants) {
      if (v.contains("-" + CreaturePanel.WALK_SPRITE_TOKEN + "-")) {
        return v;
      }
    }
    return variants.getFirst();
  }

  /**
   * Selects the exact matching creature sprite name according to direction and dead state.
   */
  public static String selectCreatureSpriteName(
      String base, Direction direction, boolean isDead, Collection<Spritesheet> available) {
    if (base == null || base.isBlank()) {
      return null;
    }
    String cleanBase = CreaturePanel.getCreatureSpriteName(base);
    if (cleanBase == null || cleanBase.isBlank()) {
      cleanBase = base;
    }
    Collection<Spritesheet> sheets = available != null ? available : Resources.spritesheets().getAll();
    return CreaturePanel.selectPreviewSpriteName(cleanBase, direction, isDead, sheets);
  }

  /**
   * Selects the exact matching prop sprite name according to state and rotation/direction.
   */
  public static String selectPropSpriteName(
      String identifier, PropState state, Collection<Spritesheet> available) {
    if (identifier == null || identifier.isBlank()) {
      return null;
    }
    String rawId = PropPanel.getIdentifierBySpriteName(identifier);
    String id = rawId != null
        ? rawId
        : (identifier.toLowerCase(Locale.ROOT).startsWith(PropAnimationController.PROP_IDENTIFIER)
            ? identifier.substring(PropAnimationController.PROP_IDENTIFIER.length())
            : identifier);

    String stateToken = state != null ? state.spriteString() : PropState.INTACT.spriteString();

    List<String> candidates = new ArrayList<>();
    candidates.add(PropAnimationController.PROP_IDENTIFIER + id + "-" + stateToken);
    candidates.add(PropAnimationController.PROP_IDENTIFIER + id);
    candidates.add(id + "-" + stateToken);
    candidates.add(id);

    Collection<Spritesheet> sheets = available != null ? available : Resources.spritesheets().getAll();
    for (String candidate : candidates) {
      for (Spritesheet sheet : sheets) {
        if (sheet != null && candidate.equalsIgnoreCase(sheet.getName())) {
          return sheet.getName();
        }
      }
      if (Editor.instance().getGameFile() != null) {
        for (SpritesheetResource res : Editor.instance().getGameFile().getSpriteSheets()) {
          if (res != null && candidate.equalsIgnoreCase(res.getName())) {
            return res.getName();
          }
        }
      }
    }

    return choosePropVariantName(id, sheets.stream().filter(Objects::nonNull).map(Spritesheet::getName).toList());
  }

  /**
   * Resolves the preview spritesheet for any entity or map object.
   */
  public static Spritesheet getPreviewSpritesheet(IEntity entity, IMapObject mapObject) {
    if (entity instanceof Creature creature) {
      Direction dir = creature.getFacingDirection();
      if (dir == null || dir == Direction.UNDEFINED) {
        if (mapObject != null) {
          dir = mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED);
        }
      }
      boolean dead = creature.isDead();
      if (!dead && mapObject != null) {
        dead = CreaturePanel.isStartDead(mapObject);
      }
      String spriteName = selectCreatureSpriteName(creature.getSpritesheetName(), dir, dead, Resources.spritesheets().getAll());
      return spriteName != null ? CreaturePanel.getOrLoadSpritesheet(spriteName) : null;
    }
    if (entity instanceof Prop prop) {
      PropState state = prop.getState() != null ? prop.getState() : PropState.INTACT;
      String spriteName = selectPropSpriteName(prop.getSpritesheetName(), state, Resources.spritesheets().getAll());
      if (spriteName != null) {
        Spritesheet sheet = Resources.spritesheets().get(spriteName);
        if (sheet == null && Editor.instance().getGameFile() != null) {
          for (SpritesheetResource res : Editor.instance().getGameFile().getSpriteSheets()) {
            if (res != null && spriteName.equalsIgnoreCase(res.getName())) {
              return Resources.spritesheets().load(res);
            }
          }
        }
        return sheet;
      }
      return null;
    }
    if (mapObject != null) {
      String type = mapObject.getType();
      if (MapObjectType.CREATURE.name().equalsIgnoreCase(type)) {
        String base = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
        Direction dir = mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED);
        boolean dead = CreaturePanel.isStartDead(mapObject);
        String spriteName = selectCreatureSpriteName(base, dir, dead, Resources.spritesheets().getAll());
        return spriteName != null ? CreaturePanel.getOrLoadSpritesheet(spriteName) : null;
      }
      if (MapObjectType.PROP.name().equalsIgnoreCase(type)) {
        String base = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
        PropState state = PropPanel.resolvePropState(mapObject);
        String spriteName = selectPropSpriteName(base, state, Resources.spritesheets().getAll());
        if (spriteName != null) {
          Spritesheet sheet = Resources.spritesheets().get(spriteName);
          if (sheet == null && Editor.instance().getGameFile() != null) {
            for (SpritesheetResource res : Editor.instance().getGameFile().getSpriteSheets()) {
              if (res != null && spriteName.equalsIgnoreCase(res.getName())) {
                return Resources.spritesheets().load(res);
              }
            }
          }
          return sheet;
        }
      }
    }
    return null;
  }

  /**
   * Resolves a scaled entity icon respecting facing direction, state, and animation.
   */
  public static Icon getEntityIcon(IEntity entity, IMapObject mapObject, int size) {
    if (entity instanceof Creature creature) {
      Direction dir = creature.getFacingDirection();
      if (dir == null || dir == Direction.UNDEFINED) {
        if (mapObject != null) {
          dir = mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED);
        }
      }
      boolean dead = creature.isDead();
      if (!dead && mapObject != null) {
        dead = CreaturePanel.isStartDead(mapObject);
      }
      return getCreatureIcon(creature.getSpritesheetName(), dir, dead, size);
    }
    if (entity instanceof Prop prop) {
      PropState state = prop.getState() != null ? prop.getState() : PropState.INTACT;
      Rotation rot = prop.getSpriteRotation() != null ? prop.getSpriteRotation() : Rotation.NONE;
      boolean flipH = prop.flipHorizontally();
      boolean flipV = prop.flipVertically();
      return getPropIcon(prop.getSpritesheetName(), state, rot, flipH, flipV, size);
    }
    if (entity instanceof LightSource lightSource) {
      return getLightSourceIcon(lightSource.getColor(), size);
    }

    if (mapObject != null) {
      String type = mapObject.getType();
      if (MapObjectType.CREATURE.name().equalsIgnoreCase(type)) {
        String base = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
        Direction dir = mapObject.getEnumValue(MapObjectProperty.SPAWN_DIRECTION, Direction.class, Direction.UNDEFINED);
        boolean dead = CreaturePanel.isStartDead(mapObject);
        return getCreatureIcon(base, dir, dead, size);
      }
      if (MapObjectType.PROP.name().equalsIgnoreCase(type)) {
        String base = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
        PropState state = PropPanel.resolvePropState(mapObject);
        Rotation rot = mapObject.getEnumValue(MapObjectProperty.PROP_ROTATION, Rotation.class, Rotation.NONE);
        boolean flipH = mapObject.getBoolValue(MapObjectProperty.PROP_FLIPHORIZONTALLY, false);
        boolean flipV = mapObject.getBoolValue(MapObjectProperty.PROP_FLIPVERTICALLY, false);
        return getPropIcon(base, state, rot, flipH, flipV, size);
      }
      if (MapObjectType.LIGHTSOURCE.name().equalsIgnoreCase(type)) {
        Color color = mapObject.getColorValue(MapObjectProperty.LIGHT_COLOR);
        return getLightSourceIcon(color, size);
      }
      MapObjectType objType = MapObjectType.get(type);
      return Icons.forMapObjectType(objType);
    }

    return null;
  }

  private static Icon getCreatureIcon(String baseSpriteName, Direction direction, boolean startDead, int size) {
    if (baseSpriteName == null || baseSpriteName.isBlank()
        || Game.world() == null || Game.world().environment() == null
        || Game.world().environment().getMap() == null) {
      return null;
    }
    String resolvedSprite = selectCreatureSpriteName(
        baseSpriteName, direction, startDead, Resources.spritesheets().getAll());
    if (resolvedSprite == null) {
      return null;
    }
    String mapName = Game.world().environment().getMap().getName();
    String cacheKey = mapName + "-creature-" + resolvedSprite + "-" + size + "px";
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      Spritesheet sprite = CreaturePanel.getOrLoadSpritesheet(resolvedSprite);
      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }
      return Imaging.scale(sprite.getSprite(0), size, size, true);
    });
    return img != null ? new ImageIcon(img) : null;
  }

  private static Icon getPropIcon(String baseSpriteName, PropState state, Rotation rotation, boolean flipH, boolean flipV, int size) {
    if (baseSpriteName == null || baseSpriteName.isBlank()
        || Game.world() == null || Game.world().environment() == null
        || Game.world().environment().getMap() == null) {
      return null;
    }
    String resolvedSprite = selectPropSpriteName(
        baseSpriteName, state, Resources.spritesheets().getAll());
    if (resolvedSprite == null) {
      return null;
    }
    String mapName = Game.world().environment().getMap().getName();
    String cacheKey = mapName + "-prop-" + resolvedSprite + "-" + state + "-" + rotation + "-" + flipH + "-" + flipV + "-" + size + "px";
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      Spritesheet sprite = Resources.spritesheets().get(resolvedSprite);
      if (sprite == null && Editor.instance().getGameFile() != null) {
        for (SpritesheetResource res : Editor.instance().getGameFile().getSpriteSheets()) {
          if (res != null && resolvedSprite.equalsIgnoreCase(res.getName())) {
            sprite = Resources.spritesheets().load(res);
            break;
          }
        }
      }
      if (sprite == null || sprite.getSprite(0) == null) {
        return null;
      }
      BufferedImage frame = sprite.getSprite(0);
      if (rotation != null && rotation != Rotation.NONE) {
        frame = Imaging.rotate(frame, rotation);
      }
      if (flipH) {
        frame = Imaging.horizontalFlip(frame);
      }
      if (flipV) {
        frame = Imaging.verticalFlip(frame);
      }
      return Imaging.scale(frame, size, size, true);
    });
    return img != null ? new ImageIcon(img) : null;
  }

  private static Icon getLightSourceIcon(Color lightColor, int size) {
    if (lightColor == null || Game.world() == null || Game.world().environment() == null
        || Game.world().environment().getMap() == null) {
      return null;
    }
    String mapName = Game.world().environment().getMap().getName();
    String cacheKey = mapName + "-light-" + Integer.toHexString(lightColor.getRGB()) + "-" + size + "px";
    BufferedImage img = Resources.images().get(cacheKey, () -> {
      int s = Math.max(10, Math.min(size, 16));
      BufferedImage newImg = Imaging.getCompatibleImage(s, s);
      Graphics2D g = (Graphics2D) Objects.requireNonNull(newImg).getGraphics();
      g.setColor(lightColor);
      g.fillRect(0, 0, s - 1, s - 1);
      g.setColor(Style.border());
      g.drawRect(0, 0, s - 1, s - 1);
      g.dispose();
      return newImg;
    });
    return img != null ? new ImageIcon(img) : null;
  }
}
