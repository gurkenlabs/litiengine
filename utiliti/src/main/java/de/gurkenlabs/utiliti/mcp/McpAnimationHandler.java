package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.resources.Resources;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonValue;
import java.util.Set;

final class McpAnimationHandler {
  private static final Set<String> TOOLS = Set.of(
      "list-sprite-animations",
      "get-sprite-animation",
      "create-sprite-animation",
      "edit-sprite-animation",
      "remove-sprite-animation");

  private McpAnimationHandler() {
    throw new UnsupportedOperationException();
  }

  static void addToolDefinitions(JsonArrayBuilder tools) {
    tools.add(McpToolHandler.createToolDef(
        "list-sprite-animations", "List sprite animations and their frame timing", JsonValue.EMPTY_JSON_OBJECT));

    tools.add(McpToolHandler.createToolDef(
        "get-sprite-animation", "Get one sprite animation", nameParams()));
    tools.add(McpToolHandler.createToolDef(
        "remove-sprite-animation", "Remove one sprite animation", nameParams()));

    JsonObjectBuilder createParams = Json.createObjectBuilder(nameParams())
        .add("spritesheet", McpToolHandler.createParam("string", "Spritesheet resource name", true))
        .add("loop", McpToolHandler.createParam("boolean", "Whether playback loops", false))
        .add("durations", McpToolHandler.createArrayParam(
            "Per-frame durations in milliseconds", true, Json.createObjectBuilder().add("type", "integer").build()));
    tools.add(McpToolHandler.createToolDef(
        "create-sprite-animation", "Create a sprite animation from an existing spritesheet", createParams.build()));

    JsonObjectBuilder editParams = Json.createObjectBuilder(nameParams())
        .add("loop", McpToolHandler.createParam("boolean", "New looping behavior", false))
        .add("durations", McpToolHandler.createArrayParam(
            "New per-frame durations in milliseconds", false, Json.createObjectBuilder().add("type", "integer").build()));
    tools.add(McpToolHandler.createToolDef(
        "edit-sprite-animation", "Edit sprite animation looping and frame timing", editParams.build()));
  }

  private static JsonObject nameParams() {
    return Json.createObjectBuilder()
        .add("name", McpToolHandler.createParam("string", "Animation name", true))
        .build();
  }

  static boolean handles(String toolName) {
    return TOOLS.contains(toolName);
  }

  static JsonObject handle(String toolName, JsonObject args) {
    return switch (toolName) {
      case "list-sprite-animations" -> listAnimations();
      case "get-sprite-animation" -> getAnimation(args);
      case "create-sprite-animation" -> createAnimation(args);
      case "edit-sprite-animation" -> editAnimation(args);
      case "remove-sprite-animation" -> removeAnimation(args);
      default -> error("Unknown animation tool: " + toolName);
    };
  }

  private static JsonObject listAnimations() {
    JsonArrayBuilder animations = Json.createArrayBuilder();
    Resources.animations().getAll().stream()
        .sorted((first, second) -> first.getName().compareToIgnoreCase(second.getName()))
        .forEach(animation -> animations.add(McpAssetHandler.animationInfo(animation)));
    return Json.createObjectBuilder()
        .add("success", true)
        .add("animations", animations)
        .build();
  }

  private static JsonObject getAnimation(JsonObject args) {
    String name = McpToolHandler.getString(args, "name", "");
    Animation animation = Resources.animations().get(name);
    return animation == null
        ? error("Animation not found: " + name)
        : Json.createObjectBuilder()
            .add("success", true)
            .add("animation", McpAssetHandler.animationInfo(animation))
            .build();
  }

  private static JsonObject createAnimation(JsonObject args) {
    String name = McpToolHandler.getString(args, "name", "");
    String spritesheetName = McpToolHandler.getString(args, "spritesheet", "");
    if (name.isBlank() || spritesheetName.isBlank()) {
      return error("'name' and 'spritesheet' are required");
    }
    if (Resources.animations().contains(name)) {
      return error("Animation already exists: " + name);
    }

    Spritesheet spritesheet = Resources.spritesheets().get(spritesheetName);
    if (spritesheet == null) {
      return error("Spritesheet not found: " + spritesheetName);
    }
    int[] durations = durations(args.getJsonArray("durations"), spritesheet.getTotalNumberOfSprites());
    if (durations == null) {
      return error("'durations' must contain one positive integer for each of the "
          + spritesheet.getTotalNumberOfSprites() + " sprites");
    }

    Animation animation = new Animation(
        name, spritesheet, McpToolHandler.getBoolean(args, "loop", true), durations);
    Resources.animations().add(name, animation);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Created sprite animation: " + name)
        .add("animation", McpAssetHandler.animationInfo(animation))
        .build();
  }

  private static JsonObject editAnimation(JsonObject args) {
    String name = McpToolHandler.getString(args, "name", "");
    Animation animation = Resources.animations().get(name);
    if (animation == null) {
      return error("Animation not found: " + name);
    }

    if (args.containsKey("loop")) {
      animation.setLooping(args.getBoolean("loop"));
    }
    if (args.containsKey("durations")) {
      int[] durations = durations(args.getJsonArray("durations"), animation.getKeyframes().size());
      if (durations == null) {
        return error("'durations' must contain one positive integer for each of the "
            + animation.getKeyframes().size() + " keyframes");
      }
      animation.setKeyFrameDurations(durations);
    }
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Updated sprite animation: " + name)
        .add("animation", McpAssetHandler.animationInfo(animation))
        .build();
  }

  private static JsonObject removeAnimation(JsonObject args) {
    String name = McpToolHandler.getString(args, "name", "");
    if (!Resources.animations().contains(name)) {
      return error("Animation not found: " + name);
    }
    Resources.animations().remove(name);
    McpAssetHandler.refreshAssets();
    return Json.createObjectBuilder()
        .add("success", true)
        .add("message", "Removed sprite animation: " + name)
        .build();
  }

  private static int[] durations(JsonArray values, int expected) {
    if (values == null || values.size() != expected) {
      return null;
    }
    int[] durations = new int[values.size()];
    for (int index = 0; index < values.size(); index++) {
      try {
        durations[index] = values.getInt(index);
      } catch (ClassCastException | NullPointerException ex) {
        return null;
      }
      if (durations[index] <= 0) {
        return null;
      }
    }
    return durations;
  }

  private static JsonObject error(String message) {
    return Json.createObjectBuilder()
        .add("success", false)
        .add("error", message)
        .build();
  }
}
