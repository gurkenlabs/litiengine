package de.gurkenlabs.utiliti.mcp;

import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;

public class McpPromptHandler {

  public static JsonObject getPromptsList() {
    JsonArrayBuilder promptsArr = Json.createArrayBuilder();

    // 1. analyze_litiengine_project
    JsonArrayBuilder analyzeArgs = Json.createArrayBuilder()
        .add(createArg("projectPath", "Optional project path when no project is currently loaded", false))
        .add(createArg("mapIds", "Optional subset of map names to analyze", false))
        .add(createArg("intent", "Analysis intent: understand_project, review_level_design, plan_new_map, or plan_map_update", false))
        .add(createArg("depth", "Analysis depth: quick, standard, or deep", false));
    promptsArr.add(createPromptDef(
        "analyze_litiengine_project",
        "LITIENGINE Project Map Analysis prompt for discovering map conventions, gameplay flow graph, tile vocabulary, and level-design profile.",
        analyzeArgs.build()));

    // 2. plan_litiengine_map
    JsonArrayBuilder planArgs = Json.createArrayBuilder()
        .add(createArg("mapId", "Unique map ID to plan updates or creation for", true))
        .add(createArg("goal", "Gameplay objective or level-design goal", true))
        .add(createArg("constraints", "Optional constraints to preserve (comma-separated)", false));
    promptsArr.add(createPromptDef(
        "plan_litiengine_map",
        "Staged level-design reasoning prompt for planning map updates using the Big -> Medium -> Small authoring workflow.",
        planArgs.build()));

    // 3. review_litiengine_map
    JsonArrayBuilder reviewArgs = Json.createArrayBuilder()
        .add(createArg("mapId", "Unique map ID to review", true))
        .add(createArg("depth", "Review depth: quick, standard, or deep", false));
    promptsArr.add(createPromptDef(
        "review_litiengine_map",
        "Level-design review prompt for evaluating spatial structure, readability, pacing, encounters, and collision topology.",
        reviewArgs.build()));

    // Existing templates
    promptsArr.add(createPromptDef("build-dungeon-room", "Template for constructing a classic dungeon room with props, light sources, and spawnpoints", null));
    promptsArr.add(createPromptDef("create-boss-arena", "Template for setting up a boss fight arena with combat entities, triggers, and collision boxes", null));
    promptsArr.add(createPromptDef("setup-patrolling-guards", "Template for generating creature entities with patrolling polyline routes", null));

    return Json.createObjectBuilder().add("prompts", promptsArr).build();
  }

  private static JsonObject createPromptDef(String name, String description, JsonArray arguments) {
    JsonObjectBuilder b = Json.createObjectBuilder()
        .add("name", name)
        .add("description", description);
    if (arguments != null) {
      b.add("arguments", arguments);
    }
    return b.build();
  }

  private static JsonObject createArg(String name, String description, boolean required) {
    return Json.createObjectBuilder()
        .add("name", name)
        .add("description", description)
        .add("required", required)
        .build();
  }

  public static JsonObject handleGetPrompt(String name) {
    return switch (name) {
      case "analyze_litiengine_project" -> createPromptResult(
          "LITIENGINE Project Map Analysis and Level-Authoring Prompt",
          "You are a level-design and map-authoring agent operating through the utiLITI MCP server for LITIENGINE.\n"
              + "Your purpose is to understand an existing LITIENGINE project well enough to safely create, extend, and update its maps while preserving technical conventions, visual language, gameplay rules, and level-design intent.\n\n"
              + "Treat every map as four things simultaneously:\n"
              + "1. A place — a coherent environment with understandable spatial structure.\n"
              + "2. An experience — a sequence of exploration, interaction, combat, tension, recovery, and discovery.\n"
              + "3. A goal — the player should understand where they are going and why.\n"
              + "4. A language — tiles, props, lighting, collisions, triggers, doors, paths, and landmarks must communicate consistently.\n\n"
              + "Primary operating rule: Inspect first. Infer second. Plan third. Modify last. Never create or edit a map before you understand relevant conventions.\n"
              + "Tiling rule: Always use Wang terrains (paint_terrain / paint-terrain) over plain tile-by-tile GID editing (edit_tiles / fill_tiles / set_tile) whenever possible. Discover terrain sets with list_terrains before painting ground or walls.\n\n"
              + "Phases:\n"
              + "Phase 1: Discover the project (use get_project_context, analyze_project, get_map, query_region, list_terrains).\n"
              + "Phase 2: Infer the project's map grammar (layer stack, tile GID vocabulary, Wang terrain sets, object conventions).\n"
              + "Phase 3: Reconstruct gameplay flow (spawns -> transitions -> target maps -> target spawns).\n"
              + "Phase 4: Analyze maps as levels (functional overview, spatial structure, critical path, encounters, readability, pacing).\n"
              + "Phase 5: Apply staged level-design reasoning (Big: structure -> Medium: spatial identity -> Small: details).\n"
              + "Phase 6: Produce or consult Project Map Profile.\n"
              + "Phase 7: Plan map changes (use plan_map_changes).\n"
              + "Phase 8: Create or update maps safely (use paint_terrain and Level A semantic tools first).\n"
              + "Phase 9: Validate every mutation (use validate_map_plan, analyze_map, analyze_collision, render_region).");

      case "plan_litiengine_map" -> createPromptResult(
          "LITIENGINE Map Authoring Plan Prompt",
          "Staged Level-Design Authoring Workflow (Big -> Medium -> Small):\n\n"
              + "Tiling Rule: Always use Wang terrains (paint_terrain / paint-terrain) over plain tile-by-tile GID editing (edit_tiles / fill_tiles / set_tile) whenever possible. Discover terrain sets with list_terrains before painting ground or walls.\n\n"
              + "Step 1: Big (Gameplay Structure)\n"
              + "- Resolve entrance, goal, critical path, major rooms, gates, encounters, transitions, and collision topology.\n"
              + "- Paint ground and wall regions using Wang terrains (paint_terrain).\n"
              + "- Do not place decorative clutter while core questions remain unresolved.\n\n"
              + "Step 2: Medium (Spatial Identity)\n"
              + "- Establish walls, doors, major furniture, machinery, structural lighting, cover, and landmarks.\n"
              + "- Ensure the space reads as its intended environment while supporting mechanics.\n\n"
              + "Step 3: Small (Detail & Atmosphere)\n"
              + "- Add floor variation, wall details, debris, small props, particles, atmospheric lighting, and overlay details.\n"
              + "- Details must reinforce composition, navigation, or atmosphere. Avoid random noise.\n\n"
              + "Use the `plan_map_changes` tool to generate a declarative plan before making editor edits.");

      case "review_litiengine_map" -> createPromptResult(
          "LITIENGINE Level-Design Review Prompt",
          "Evaluate the map across the following 9 level-design criteria:\n"
              + "1. Functional overview & environment identity\n"
              + "2. Spatial structure (rooms, corridors, hubs, chokepoints)\n"
              + "3. Critical path & navigation clarity\n"
              + "4. Encounter structure & enemy spacing\n"
              + "5. Interaction & puzzle flow (problem before solution)\n"
              + "6. Readability & 3-second render test\n"
              + "7. Pacing (tension, release, exploration rhythms)\n"
              + "8. Collision & traversal quality (no trapped spawns or clipping)\n"
              + "9. Technical integrity (no broken entity/trigger links)\n\n"
              + "Use `analyze_project` and `validate_map_plan` to inspect structural evidence.");

      case "build-dungeon-room" -> createPromptResult(
          "Instructions for dungeon room layout",
          "1. Use `create_entities` to place PROP objects for walls and decorations.\n"
              + "2. Use `create_entities` with type LIGHTSOURCE to place torches with warm light colors.\n"
              + "3. Use `create_entities` with type SPAWNPOINT for player and enemy entry points.");

      case "create-boss-arena" -> createPromptResult(
          "Instructions for boss arena layout",
          "1. Use `create_entities` with type CREATURE for the boss entity.\n"
              + "2. Use `create_entities` with type TRIGGER at entrance points to trigger an arena lock-in message.\n"
              + "3. Use `create_entities` with type COLLISIONBOX to define boundary walls.");

      case "setup-patrolling-guards" -> createPromptResult(
          "Instructions for guard patrols",
          "1. Use `create_entities` with type PATH to create patrol path geometry.\n"
              + "2. Use `create_entities` with type CREATURE to spawn guard entities.\n"
              + "3. Use `update_entities` to assign the patrol path name to guards.");

      default -> Json.createObjectBuilder().add("error", "Unknown prompt template: " + name).build();
    };
  }

  private static JsonObject createPromptResult(String description, String text) {
    return Json.createObjectBuilder()
        .add("description", description)
        .add("messages", Json.createArrayBuilder()
            .add(Json.createObjectBuilder()
                .add("role", "user")
                .add("content", Json.createObjectBuilder()
                    .add("type", "text")
                    .add("text", text))))
        .build();
  }
}
