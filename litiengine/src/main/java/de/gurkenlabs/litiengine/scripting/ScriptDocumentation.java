package de.gurkenlabs.litiengine.scripting;

import java.util.HashMap;
import java.util.Map;

/** Central documentation catalog for LITIENGINE scripting API, classes, annotations, and lifecycle events. */
public final class ScriptDocumentation {
  private static final Map<String, String> CLASS_DOCS = new HashMap<>();
  private static final Map<String, String> METHOD_DOCS = new HashMap<>();
  private static final Map<String, String> ATTRIBUTE_DOCS = new HashMap<>();

  static {
    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptProperty", """
      ### @ScriptProperty
      Exports a script field to the **utiLITI Inspector** panel for live configuration and map persistence.

      Annotating a field with `@ScriptProperty` exposes it directly inside utiLITI's entity or map inspector. Designers can adjust values in the editor UI, and changes are automatically saved with map objects and loaded when the game runs.

      **Supported Field Types:**
      - **Primitives:** `int`, `float`, `double`, `boolean`, `long`, `short`, `byte`
      - **Text:** `String`
      - **Geometry:** `Point2D`, `Rectangle2D`
      - **Color:** `Color` *(renders color picker in utiLITI)*
      - **Enums:** Any custom Java `enum` *(renders as a dropdown list)*
      - **Entities & Assets:** Entity references, sprite names, audio assets

      **Attributes:**
      - `name` — Display label in the inspector (defaults to the field name).
      - `description` — Tooltip help text displayed on hover in utiLITI.
      - `category` — Inspector section/grouping header (default: `"Script"`).
      - `min` — Minimum numeric value boundary for sliders/spinners.
      - `max` — Maximum numeric value boundary for sliders/spinners.
      - `unit` — Unit label shown next to the input (e.g. `"px"`, `"sec"`, `"%"`, `"deg"`).
      - `required` — Flags the property as mandatory in the inspector.
      - `defaultValue` — Initial fallback string value.

      **Example:**
      ```java
      @ScriptProperty(name = "Movement Speed", description = "Movement velocity in pixels/second", min = 10, max = 500, unit = "px")
      private float speed = 120f;

      @ScriptProperty(name = "Alert Color", description = "Light color when alerted")
      private Color alertColor = Color.RED;

      @ScriptProperty(name = "Guard Mode", description = "Initial state machine behavior")
      private GuardMode mode = GuardMode.PATROL;
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptInfo", """
      ### @ScriptInfo
      Declares metadata and binding rules for an editor-visible script class.

      **Attributes:**
      - `id` *(required)* — Unique identifier string used for bindings and map entity references.
      - `name` — Human-readable script name shown in the asset tree and inspector dropdowns.
      - `host` — Target host category: `ScriptHostType.ENTITY`, `ENVIRONMENT`, or `GAME`.
      - `target` — Required entity class for entity scripts (e.g. `Creature.class`, `Prop.class`).

      **Example:**
      ```java
      @ScriptInfo(
        id = "npc-patrol",
        name = "Guard Patrol AI",
        host = ScriptHostType.ENTITY,
        target = Creature.class
      )
      public class PatrolAI extends CreatureScript {
        @ScriptProperty(name = "Patrol Radius", min = 20, max = 1000, unit = "px")
        private float radius = 150f;
      }
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.GameScript", """
      ### GameScript
      Base class for global game logic, manager controllers, cutscenes, and quest systems.

      **Key Subsystems & State:**
      - `globals` — Direct access to `ScriptGlobals` for sharing persistent game state across scenes.
      - `context()` — The active `ScriptContext` execution handle.

      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked once when the script becomes active in the game session.
      - `update()` — Invoked every frame tick during the main game loop.
      - `onUnloaded()` — Invoked when the script is detached or the game session ends.

      **Example:**
      ```java
      public class GameManager extends GameScript {
        @ScriptProperty(name = "Starting Lives", min = 1, max = 9)
        private int startingLives = 3;

        @Override
        public void onLoaded() {
          globals.put("lives", startingLives);
          globals.put("score", 0);
        }

        @Override
        public void update() {
          // Global per-frame game tick logic
        }
      }
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.CreatureScript", """
      ### CreatureScript
      Base class for scripting moving characters, player avatars, NPCs, and enemy AI.

      **Host Control via `host()`:**
      - `host().getVelocity()` / `setVelocity(v)` — Movement speed and direction.
      - `host().getHitPoints()` / `setHitPoints(hp)` — Health points.
      - `host().isDead()` / `die()` — Mortality status and events.
      - `host().getFacingDirection()` — Facing direction (`UP`, `DOWN`, `LEFT`, `RIGHT`).
      - `host().animations()` — Sprite animation controller.

      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked when the creature enters the active environment.
      - `update()` — Invoked every frame tick for AI and physics updates.
      - `onUnloaded()` — Invoked when the creature leaves or despawns.

      **Example:**
      ```java
      public class JanitorAI extends CreatureScript {
        @ScriptProperty(name = "Clean Speed", min = 10, max = 200, unit = "px")
        private float speed = 50f;

        @Override
        public void update() {
          if (host().isDead()) return;
          // AI logic
        }
      }
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.EntityScript", """
      ### EntityScript<T extends IEntity>
      Base class for scripting interactive game entities (props, doors, levers, lights, items).

      **Host Control via `host()`:**
      - `host().getLocation()` / `setLocation(x, y)` — Map position.
      - `host().getBoundingBox()` / `getCenter()` — Bounds and collision geometry.
      - `host().hasTag("tag")` / `addTag("tag")` — Entity tags and categories.
      - `host().onLoaded(listener)` / `onUnloaded(listener)` — Entity lifecycle.

      **Lifecycle Callbacks:**
      - `onLoaded()` — Invoked when the entity is loaded into the active environment.
      - `update()` — Invoked every frame tick.
      - `onUnloaded()` — Invoked when the entity is removed or unloaded.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.EnvironmentScript", """
      ### EnvironmentScript
      Base class for map-level scripts controlling scene triggers, ambient lighting, spawn sequences, and area logic.

      **Environment Control via `environment()`:**
      - `environment().get("name")` / `get(id)` — Find map entities.
      - `environment().getByType(Creature.class)` — Query entities by type.
      - `environment().add(entity)` / `remove(entity)` — Dynamic spawning and removal.
      - `environment().getAmbientLight()` — Ambient lighting and darkness.
      - `environment().getTriggers()` — Map trigger zones.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.AbstractScript", """
      ### AbstractScript
      Base abstract class for all LITIENGINE script instances.

      Provides convenient access to `host()`, `environment()`, `context()`, `globals`, `input()`, `ui()`, `camera()`, and `spawner()`.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptGlobals", """
      ### ScriptGlobals
      Thread-safe global state store shared across all running scripts and maps.

      **Key Methods:**
      - `put(key, value)` — Store a shared variable (numbers, strings, objects).
      - `get(key)` — Retrieve a value as `Object`.
      - `getInt(key)` / `getDouble(key)` / `getBoolean(key)` / `getString(key)` — Type-safe getters.
      - `onChanged(listener)` — Subscribe to variable modifications.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptContext", """
      ### ScriptContext
      Represents the execution and parameter binding context for script attachments.

      **Key Services:**
      - `ui()` — Access `ScriptUiOverlay` for floating combat text, screen HUD, and banners.
      - `camera()` — Access active `ICamera` controller.
      - `input()` — Access `ScriptInput` for key and mouse tracking.
      - `spawner()` — Fluent entity spawner for runtime spawning.
      - `sequence()` — Cancellable ordered sequences and cinematic transitions.
      - `entities(type)` — Fluent entity query engine.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.scripting.ScriptHostType", """
      ### ScriptHostType
      Enum defining the host binding target type for scripts.

      **Values:**
      - `ENTITY` — Script is attached to an individual entity instance.
      - `ENVIRONMENT` — Script is attached to the active map environment.
      - `GAME` — Script is attached globally to the game session.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.EntityQuery", """
      ### EntityQuery
      Fluent selection API for searching map entities in an `Environment`.

      **Query Pipeline:**
      - `EntityQuery.in(environment(), Creature.class)` — Set search scope.
      - `.within(centerPoint, radius)` — Radial distance filter.
      - `.alive()` / `.dead()` — Health status filter.
      - `.withTag("enemy")` — Tag filter.
      - `.nearest(point)` / `.furthest(point)` — Spatial sorting.
      - `.first()` / `.list()` / `.count()` — Query execution.

      **Example:**
      ```java
      List<Creature> nearbyEnemies = EntityQuery.in(environment(), Creature.class)
          .within(host().getCenter(), 200)
          .withTag("enemy")
          .alive()
          .list();
      ```
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.Game", """
      ### Game
      Static singleton hub for accessing global engine subsystems.

      **Subsystems:**
      - `Game.world()` — Active map and environment manager (`environment()`, `camera()`, `loadEnvironment(...)`).
      - `Game.loop()` — Main game loop tick provider and timer scheduling.
      - `Game.audio()` — Sound effects (`playSound(...)`) and music playback (`playMusic(...)`).
      - `Game.physics()` — Collision detection and raycasting engine.
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
      - `reset(mapName)` — Reload and reset map entities.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.environment.Environment", """
      ### Environment
      Active tile map environment container managing all loaded entities, light sources, emitters, triggers, and map properties.

      **Key Operations:**
      - `get(id)` / `get(name)` — Lookup entity by ID or map name.
      - `getByType(Class)` — Query all entities of a given type.
      - `add(entity)` / `remove(entity)` — Spawn or despawn entities dynamically.
      - `getEntities()` / `getCombatEntities()` — All loaded entities.
      - `getTriggers()` / `getLightSources()` — Map triggers and lighting entities.
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

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Creature", """
      ### Creature
      Base entity for moving characters, NPCs, enemies, and player avatars with physics velocity, animations, and attributes.

      **Key Operations:**
      - `getVelocity()` / `setVelocity(velocity)` — Movement speed and vector.
      - `getHitPoints()` / `setHitPoints(hp)` — Hit points and health management.
      - `isDead()` / `die()` — Mortality state check.
      - `getFacingDirection()` — Cardinal direction orientation (`UP`, `DOWN`, `LEFT`, `RIGHT`).
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

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Prop", """
      ### Prop
      Static or destructible decorative map object (chests, trees, rocks, barrels).
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Spawnpoint", """
      ### Spawnpoint
      Map marker location for spawning player characters, enemies, or items.
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.Emitter", """
      ### Emitter
      Particle emitter generating dynamic visual effects (smoke, fire, sparks, weather).
      """);

    CLASS_DOCS.put("de.gurkenlabs.litiengine.entities.CollisionBox", """
      ### CollisionBox
      Static geometric collision obstacle blocking entity movement in the physics engine.
      """);

    // Attribute documentation
    ATTRIBUTE_DOCS.put("name", "**name** *(String)*\n\nHuman-readable display label shown in the utiLITI Inspector panel. If omitted, defaults to the Java field name formatted with spaces.");
    ATTRIBUTE_DOCS.put("description", "**description** *(String)*\n\nHelpful tooltip description displayed when hovering over this property in utiLITI.");
    ATTRIBUTE_DOCS.put("category", "**category** *(String)*\n\nInspector section/group header under which this property appears (default: `\"Script\"`).");
    ATTRIBUTE_DOCS.put("min", "**min** *(double)*\n\nMinimum allowed value for numeric properties in the utiLITI spinner / slider control.");
    ATTRIBUTE_DOCS.put("max", "**max** *(double)*\n\nMaximum allowed value for numeric properties in the utiLITI spinner / slider control.");
    ATTRIBUTE_DOCS.put("unit", "**unit** *(String)*\n\nUnit suffix displayed next to the property control in the inspector (e.g. `\"px\"`, `\"sec\"`, `\"%\"`, `\"deg\"`).");
    ATTRIBUTE_DOCS.put("required", "**required** *(boolean)*\n\nFlags this property as mandatory in the inspector before the map can be exported.");
    ATTRIBUTE_DOCS.put("defaultValue", "**defaultValue** *(String)*\n\nDefault fallback value string assigned when initializing newly bound map entities.");
    ATTRIBUTE_DOCS.put("id", "**id** *(String)*\n\nUnique script identifier string used for script bindings and map entity references.");
    ATTRIBUTE_DOCS.put("host", "**host** *(ScriptHostType)*\n\nTarget binding host category: `ScriptHostType.ENTITY`, `ENVIRONMENT`, or `GAME`.");
    ATTRIBUTE_DOCS.put("target", "**target** *(Class<?>)*\n\nFor entity scripts, specifies the required entity class (e.g. `Creature.class`, `Prop.class`).");

    METHOD_DOCS.put("onLoaded", "Executed once when the script or entity is loaded into the active environment.");
    METHOD_DOCS.put("onStarted", "Executed when the script or game component starts.");
    METHOD_DOCS.put("update", "Executed every frame tick during the main game loop for updates and AI.");
    METHOD_DOCS.put("onUnloaded", "Executed when the script or entity is removed or detached.");
    METHOD_DOCS.put("onStopped", "Executed when the script or game component stops.");
    METHOD_DOCS.put("hit", "Event callback fired when damage or a hit is inflicted on the combat entity.");
    METHOD_DOCS.put("death", "Event callback fired when health reaches zero and the entity dies.");
    METHOD_DOCS.put("resurrect", "Event callback fired when the combat entity is revived.");
    METHOD_DOCS.put("loaded", "Event callback fired when the entity is loaded into the active environment.");
    METHOD_DOCS.put("unloaded", "Event callback fired when the entity is unloaded from the environment.");
    METHOD_DOCS.put("removed", "Event callback fired when an entity is removed from the environment.");
    METHOD_DOCS.put("collision", "Event callback fired when a physics collision occurs.");
    METHOD_DOCS.put("played", "Event callback fired when an animation finishes or plays.");
    METHOD_DOCS.put("host", "Returns the entity or creature host instance bound to this script.");
    METHOD_DOCS.put("environment", "Returns the active tile map environment.");
    METHOD_DOCS.put("context", "Returns the script execution and binding context.");
    METHOD_DOCS.put("loadMap", "Loads and transitions to the specified map environment.");
    METHOD_DOCS.put("playMusic", "Plays the specified background soundtrack.");
    METHOD_DOCS.put("playSound", "Plays a sound effect by audio asset name.");
    METHOD_DOCS.put("ui", "Returns the ScriptUiOverlay service for displaying floating combat text, screen HUD elements, and announcement banners.");
    METHOD_DOCS.put("camera", "Returns the active camera controller for panning, zooming, shaking, and viewport queries.");
    METHOD_DOCS.put("floatText", "Spawns animated floating combat text in world space that moves upward and fades out over time.");
    METHOD_DOCS.put("drawScreenText", "Draws persistent or timed screen-space text at fixed pixel coordinates.");
    METHOD_DOCS.put("showBanner", "Displays a prominent centered announcement banner with a title and optional subtitle.");
    METHOD_DOCS.put("cameraPanTo", "Smoothly pans the camera to focus on a target point or entity over a specified tick duration.");
    METHOD_DOCS.put("cameraZoom", "Smoothly zooms the camera to a target zoom level over a specified duration in milliseconds.");
    METHOD_DOCS.put("screenShake", "Applies a screen shake effect with specified intensity and duration.");
    METHOD_DOCS.put("moveTowards", "Moves the creature towards a target map position or entity using its current velocity.");
    METHOD_DOCS.put("moveInDirection", "Moves the creature in a specific compass direction (UP, DOWN, LEFT, RIGHT).");
    METHOD_DOCS.put("moveInAngle", "Moves the creature along a specific angle in degrees (0 = North, 90 = East, 180 = South, 270 = West).");
    METHOD_DOCS.put("enableTopDownMovement", "Configures 8-directional top-down keyboard movement (WASD or custom keys).");
    METHOD_DOCS.put("enablePlatformingMovement", "Configures 2D side-scrolling platformer physics movement and jumping.");
    METHOD_DOCS.put("disableMovementController", "Disables and detaches active keyboard/movement controllers.");
    METHOD_DOCS.put("createAbility", "Begins building a custom scripted combat ability with range, cooldown, and cast logic.");
    METHOD_DOCS.put("cast", "Executes a registered combat ability by name on the host creature.");
    METHOD_DOCS.put("canCast", "Checks if a combat ability is available and ready to be cast.");
    METHOD_DOCS.put("isOnCooldown", "Checks if a combat ability is currently cooling down.");
    METHOD_DOCS.put("spawnProjectile", "Begins building and launching a scripted projectile entity.");
    METHOD_DOCS.put("spawner", "Returns a fluent entity spawner for creating creatures, props, and custom entities.");
    METHOD_DOCS.put("spawnCreature", "Spawns a creature with the given sprite prefix at the specified coordinates.");
    METHOD_DOCS.put("spawnProp", "Spawns a prop entity with the given spritesheet name at the specified coordinates.");
    METHOD_DOCS.put("spawn", "Spawns an entity instance or entity class type in the active environment.");
    METHOD_DOCS.put("entities", "Begins a fluent spatial query for map entities of a given type in the active environment.");
    METHOD_DOCS.put("input", "Returns the managed ScriptInput helper for key/mouse event tracking and queries.");
    METHOD_DOCS.put("sequence", "Creates a cancellable ordered sequence of delayed actions and cinematic camera movements.");
    METHOD_DOCS.put("schedule", "Schedules a cancellable action to be performed after a frame delay.");
    METHOD_DOCS.put("manage", "Registers a resource, listener, or subscription to be automatically closed/cleaned up when the script is unloaded.");
    METHOD_DOCS.put("listen", "Registers an event listener that is automatically unregistered upon script unloading.");
    METHOD_DOCS.put("getVelocity", "Gets the movement velocity vector and speed of this mobile entity.");
    METHOD_DOCS.put("setVelocity", "Sets the movement speed and velocity of this entity in pixels/second.");
    METHOD_DOCS.put("getHitPoints", "Gets the current and maximum hit points of this combat entity.");
    METHOD_DOCS.put("setHitPoints", "Sets the current hit points of this combat entity.");
    METHOD_DOCS.put("isDead", "Checks if the combat entity is currently dead.");
    METHOD_DOCS.put("die", "Inflicts lethal damage and kills the combat entity.");
    METHOD_DOCS.put("resurrect", "Revives the combat entity with its initial hit points.");
    METHOD_DOCS.put("getCenter", "Returns the geometric center coordinate of this entity in map pixels.");
    METHOD_DOCS.put("getLocation", "Returns the top-left coordinate of this entity in map pixels.");
    METHOD_DOCS.put("setLocation", "Sets the position of this entity in map pixels.");
    METHOD_DOCS.put("sendMessage", "Dispatches a custom string message to entity message listeners.");
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

  public static String getAttributeDoc(String attributeName) {
    if (attributeName == null) return "";
    return ATTRIBUTE_DOCS.getOrDefault(attributeName, "");
  }
}
