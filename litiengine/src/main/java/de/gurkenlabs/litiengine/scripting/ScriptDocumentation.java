package de.gurkenlabs.litiengine.scripting;

import java.util.HashMap;
import java.util.Map;

/** Central documentation catalog for LITIENGINE scripting API, classes, and lifecycle events. */
public final class ScriptDocumentation {
  private static final Map<String, String> CLASS_DOCS = new HashMap<>();
  private static final Map<String, String> METHOD_DOCS = new HashMap<>();

  static {
    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.GameScript", """
      ### GameScript
      Base script for global game logic and scene controllers.
      
      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked once when the script is attached to the active game scene.
      - `update()` — Invoked every frame tick during the main game loop.
      - `onUnloaded()` — Invoked when the script is detached or scene changes.
      
      **Shared Globals:**
      Access global game state via `globals.put("score", 100)` or `globals.get("score")`.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.CreatureScript", """
      ### CreatureScript
      Base script for controlling `Creature` entities in the game world.
      
      **Host Control:**
      Use `host()` to control movement, combat, attributes, and animations.
      
      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked when the creature enters the environment.
      - `update()` — Invoked every frame tick for AI and custom behavior.
      - `onUnloaded()` — Invoked when the creature despawns or dies.
      
      **Example:**
      ```java
      @Override
      protected void update() {
        if (host().isDead()) return;
        // creature AI logic
      }
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.EntityScript", """
      ### EntityScript
      Base script attached to game world entities (`IEntity`).
      
      **Host Control:**
      Use `host()` to inspect entity properties, position, tags, and components.
      
      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked when entity is loaded into environment.
      - `update()` — Invoked every frame tick.
      - `onUnloaded()` — Invoked when entity is removed from environment.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.EnvironmentScript", """
      ### EnvironmentScript
      Base script managing map environments, scene triggers, and ambient logic.
      
      **Environment Access:**
      Use `environment()` to query map entities, spawn objects, or trigger lighting effects.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.AbstractScript", """
      ### AbstractScript
      Base abstract class for all LITIENGINE script instances.
      
      Provides access to `host()`, `environment()`, `context()`, and `globals`.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptGlobals", """
      ### ScriptGlobals
      Thread-safe global state store shared across all running scripts.
      
      **Methods:**
      - `put(key, value)` — Store shared variable.
      - `get(key)` — Fetch shared variable.
      - `onChanged(listener)` — Subscribe to state mutations.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptContext", """
      ### ScriptContext
      Represents the execution and binding context for script attachments.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.EntityQuery", """
      ### EntityQuery
      Fluent selection API for searching map entities in an `Environment`.
      
      **Example Usage:**
      ```java
      EntityQuery.in(environment(), Creature.class)
                 .within(host().getCenter(), 150)
                 .alive()
                 .list();
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.CombatEntityListener", """
      ### CombatEntityListener
      Event listener for combat-related events on combat entities.
      
      **Events:**
      - `hit(EntityHitEvent event)` — Fired when damage is inflicted.
      - `death(ICombatEntity entity)` — Fired when health reaches zero.
      - `resurrect(ICombatEntity entity)` — Fired when revived.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.EntityListener", """
      ### EntityListener
      Event listener for entity lifecycle events.
      
      **Events:**
      - `loaded(EntityEvent event)` — Fired when entity enters environment.
      - `unloaded(EntityEvent event)` — Fired when entity leaves environment.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.physics.CollisionListener", """
      ### CollisionListener
      Event listener for physics collision events.
      
      **Events:**
      - `collision(CollisionEvent event)` — Fired when collision occurs.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.graphics.AnimationListener", """
      ### AnimationListener
      Event listener for sprite animations.
      
      **Events:**
      - `played(Animation animation)` — Fired when animation starts or finishes.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.IEntity", """
      ### IEntity
      Core interface for all game objects and entities in LITIENGINE.

      **Key Properties & Operations:**
      - `getId()` / `getName()` — Unique identifier and map name.
      - `getLocation()` / `setLocation(x, y)` — Map position coordinates.
      - `getBoundingBox()` / `getCenter()` — Collision and bounds geometry.
      - `hasTag(tag)` / `addTag(tag)` — Entity tags and classification.
      - `onLoaded(listener)` / `onUnloaded(listener)` — Lifecycle listeners.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.environment.Environment", """
      ### Environment
      Active tile map environment container managing all loaded entities, light sources, emitters, triggers, and map properties.

      **Key Operations:**
      - `get(id)` / `get(name)` — Lookup entity by ID or map name.
      - `getByType(Class)` — Query all entities of a given type.
      - `add(entity)` / `remove(entity)` — Spawn or despawn entities dynamically.
      - `addTrigger(trigger)` / `getTriggers()` — Query map triggers.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Creature", """
      ### Creature
      Base entity for moving characters, NPCs, enemies, and player avatars with physics velocity, animations, and attributes.

      **Key Operations:**
      - `getVelocity()` / `setVelocity(velocity)` — Movement speed and vector.
      - `getHealth()` / `setHealth(hp)` — Hit points and health management.
      - `isDead()` / `die()` — Mortality state check.
      - `getFacingDirection()` — Cardinal direction orientation.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.ICombatEntity", """
      ### ICombatEntity
      Interface for entities capable of dealing and receiving combat damage, health tracking, and mortality events.

      **Key Operations:**
      - `getHitPoints()` / `setHitPoints(hp)` — Current health points.
      - `getMaxHitPoints()` — Maximum health capacity.
      - `isDead()` / `die()` / `resurrect()` — Combat status.
      - `hit(damage, source)` — Inflict damage.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.Game", """
      ### Game
      Static singleton hub for accessing global engine subsystems.

      **Subsystems:**
      - `Game.world()` — Active map and environment manager.
      - `Game.loop()` — Main game loop tick provider.
      - `Game.audio()` — Sound effects and music playback engine.
      - `Game.physics()` — Collision detection and physics engine.
      - `Game.graphics()` — Render engine, camera, and window graphics.
      - `Game.config()` — Client and graphics configuration.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.GameWorld", """
      ### GameWorld
      Manages map loading, active environments, camera tracking, and scene transitions.

      **Key Operations:**
      - `environment()` — Active `Environment` instance.
      - `camera()` — Camera controller for pan, zoom, and target tracking.
      - `loadEnvironment(mapName)` — Load and display a new map.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptHostType", """
      ### ScriptHostType
      Enum defining the host binding target type for scripts.

      **Values:**
      - `ENTITY` — Script is attached to an individual entity instance.
      - `ENVIRONMENT` — Script is attached to the active map environment.
      - `GAME` — Script is attached globally to the game session.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptInfo", """
      ### @ScriptInfo
      Annotation declaring metadata for a script class (script ID, host type, target entity class).
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Prop", """
      ### Prop
      Static or destructible decorative map object (chests, trees, rocks, barrels).
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.LightSource", """
      ### LightSource
      Point light or shape lighting entity emitting ambient or colored light onto the map.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Trigger", """
      ### Trigger
      Collision volume executing actions when entities enter or exit specified map areas.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Spawnpoint", """
      ### Spawnpoint
      Map marker location for spawning player characters, enemies, or items.
      """);

    METHOD_DOCS.put("onLoaded", "Executed when the script or entity is loaded into the active environment.");
    METHOD_DOCS.put("update", "Executed every frame tick during game loop updates for AI and custom logic.");
    METHOD_DOCS.put("onUnloaded", "Executed when the script or entity is removed or detached.");
    METHOD_DOCS.put("hit", "Event callback fired when damage or a hit is inflicted on the combat entity.");
    METHOD_DOCS.put("death", "Event callback fired when health reaches zero and the entity dies.");
    METHOD_DOCS.put("resurrect", "Event callback fired when the combat entity is revived.");
    METHOD_DOCS.put("loaded", "Event callback fired when the entity is loaded into the active environment.");
    METHOD_DOCS.put("unloaded", "Event callback fired when the entity is unloaded from the environment.");
    METHOD_DOCS.put("removed", "Event callback fired when an entity is removed from the environment.");
    METHOD_DOCS.put("collision", "Event callback fired when a physics collision occurs.");
    METHOD_DOCS.put("played", "Event callback fired when an animation finishes or plays.");
    METHOD_DOCS.put("host", "Returns the entity or script host instance bound to this script.");
    METHOD_DOCS.put("environment", "Returns the active map environment.");
    METHOD_DOCS.put("context", "Returns the script execution and binding context.");
  }

  private ScriptDocumentation() {}

  public static String get(Class<?> type) {
    if (type == null) return "";
    return CLASS_DOCS.getOrDefault(type.getName(), "");
  }

  public static String get(String className) {
    if (className == null) return "";
    return CLASS_DOCS.getOrDefault(className, "");
  }

  public static String getMethodDoc(String methodName) {
    if (methodName == null) return "";
    return METHOD_DOCS.getOrDefault(methodName, "");
  }
}
