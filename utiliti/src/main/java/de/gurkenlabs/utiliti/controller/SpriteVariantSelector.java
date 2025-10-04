package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.PropState;
import de.gurkenlabs.litiengine.graphics.CreatureAnimationState;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.PropAnimationController;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.view.components.CreaturePanel;
import de.gurkenlabs.utiliti.view.components.PropPanel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * Utility that selects a single representative ("base") variant per logical sprite family (props & creatures). The selection logic mirrors what the
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
      String expected = base + '-' + CreatureAnimationState.IDLE.spriteString() + '-' + dir.name().toLowerCase();
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
    // Final fallback: first available variant (order preserved by grouping)
    return variants.getFirst();
  }

  private static String chooseCreatureVariantName(String base, List<String> variants) {
    Direction[] dirPref = {Direction.DOWN, Direction.RIGHT, Direction.LEFT, Direction.UP};
    for (Direction dir : dirPref) {
      String expected = base + '-' + CreatureAnimationState.IDLE.spriteString() + '-' + dir.name().toLowerCase();
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
    return variants.getFirst();
  }
}
