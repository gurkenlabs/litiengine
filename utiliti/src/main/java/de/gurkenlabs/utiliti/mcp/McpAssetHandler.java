package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Blueprint;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.graphics.Spritesheet;
import de.gurkenlabs.litiengine.graphics.animation.Animation;
import de.gurkenlabs.litiengine.graphics.emitters.xml.EmitterAttributes;
import de.gurkenlabs.litiengine.resources.ResourceBundle;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.litiengine.resources.TextureAtlas;
import de.gurkenlabs.litiengine.util.io.Codec;
import de.gurkenlabs.litiengine.util.io.FileUtilities;
import de.gurkenlabs.litiengine.util.io.XmlUtilities;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.view.components.UI;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.ImageIO;

final class McpAssetHandler {
  private static final Set<String> TOOLS = Set.of(
      "import-animation",
      "import-emitter",
      "import-blueprint",
      "import-tileset",
      "import-sprite-definition",
      "import-texture-atlas",
      "list-resources",
      "get-resource-info",
      "remove-resource",
      "export-resource");

  private McpAssetHandler() {
    throw new UnsupportedOperationException();
  }

  static void addToolDefinitions(JsonArrayBuilder tools) {
    tools.add(McpToolHandler.createToolDef(
        "import-animation", "Import an Aseprite JSON animation and its spritesheet", importParams()));
    tools.add(McpToolHandler.createToolDef(
        "import-emitter", "Import an emitter XML definition", importParams()));
    tools.add(McpToolHandler.createToolDef(
        "import-blueprint", "Import a blueprint or template XML definition", importParams()));

    JsonObjectBuilder importTilesetParams = Json.createObjectBuilder();
    importTilesetParams.add("path", McpToolHandler.createParam("string", "Absolute or project-relative source TSX file path", true));
    importTilesetParams.add("overwrite", McpToolHandler.createParam("boolean", "Replace a resource with the same name", false));
    tools.add(McpToolHandler.createToolDef(
        "import-tileset", "Import a TSX tileset into the project", importTilesetParams.build()));

    tools.add(McpToolHandler.createToolDef(
        "import-sprite-definition", "Import a sprites.info definition file", importParams()));
    tools.add(McpToolHandler.createToolDef(
        "import-texture-atlas", "Import every sprite from a TexturePacker-style XML atlas", importParams()));

    tools.add(McpToolHandler.createToolDef(
        "list-resources", "List project resources across all supported asset types", listParams()));

    tools.add(McpToolHandler.createToolDef(
        "get-resource-info", "Get detailed metadata for one project resource", resourceParams()));
    tools.add(McpToolHandler.createToolDef(
        "remove-resource", "Remove a resource from the project and runtime resource container", resourceParams()));

    tools.add(McpToolHandler.createToolDef(
        "export-resource", "Export an animation, spritesheet, sound, emitter, blueprint, or tileset", exportParams()));
  }

  private static JsonObject importParams() {
    return Json.createObjectBuilder()
        .add("path", McpToolHandler.createParam("string", "Absolute or project-relative source file path", true))
        .add("overwrite", McpToolHandler.createParam("boolean", "Replace a resource with the same name", false))
        .build();
  }

  private static JsonObject listParams() {
    return Json.createObjectBuilder()
        .add("type", McpToolHandler.createParam(
            "string", "Optional resource type: spritesheet, sound, animation, emitter, blueprint, or tileset", false))
        .build();
  }

  private static JsonObject resourceParams() {
    return Json.createObjectBuilder()
        .add("type", McpToolHandler.createParam(
            "string", "Resource type: spritesheet, sound, animation, emitter, blueprint, or tileset", true))
        .add("name", McpToolHandler.createParam("string", "Resource name", true))
        .build();
  }

  private static JsonObject exportParams() {
    return Json.createObjectBuilder()
        .add("type", McpToolHandler.createParam(
            "string", "Resource type: spritesheet, sound, animation, emitter, blueprint, or tileset", true))
        .add("name", McpToolHandler.createParam("string", "Resource name", true))
        .add("path", McpToolHandler.createParam("string", "Destination file path", true))
        .add("metadataOnly", McpToolHandler.createParam(
            "boolean", "For spritesheets, write only the sprites.info metadata file", false))
        .build();
  }

  static boolean handles(String toolName) {
    return TOOLS.contains(toolName);
  }

  static JsonObject handle(String toolName, JsonObject args) {
    return switch (toolName) {
      case "import-animation" -> importAnimation(args);
      case "import-emitter" -> importEmitter(args);
      case "import-blueprint" -> importBlueprint(args);
      case "import-tileset" -> importTileset(args);
      case "import-sprite-definition" -> importSpriteDefinition(args);
      case "import-texture-atlas" -> importTextureAtlas(args);
      case "list-resources" -> listResources(args);
      case "get-resource-info" -> getResourceInfo(args);
      case "remove-resource" -> removeResource(args);
      case "export-resource" -> exportResource(args);
      default -> error("Unknown asset tool: " + toolName);
    };
  }

  private static JsonObject importAnimation(JsonObject args) {
    Path path = sourcePath(args);
    if (path == null) {
      return error("A valid animation JSON 'path' is required");
    }
    String name = FileUtilities.getFileName(path.getFileName().toString());
    boolean overwrite = McpToolHandler.getBoolean(args, "overwrite", false);
    if (Resources.animations().contains(name) && !overwrite) {
      return error("Animation already exists: " + name);
    }
    try {
      Animation animation = Resources.animations().importAseprite(path);
      refreshAssets();
      return success("Imported animation: " + animation.getName())
          .add("resource", animationInfo(animation))
          .build();
    } catch (Exception e) {
      return error("Could not import animation: " + e.getMessage());
    }
  }

  private static JsonObject importEmitter(JsonObject args) {
    ResourceBundle bundle = gameFile();
    Path path = sourcePath(args);
    if (bundle == null || path == null) {
      return error(bundle == null ? "No project is loaded" : "A valid emitter XML 'path' is required");
    }
    try {
      EmitterAttributes emitter = XmlUtilities.read(EmitterAttributes.class, path.toUri().toURL());
      if (emitter == null || emitter.getName() == null || emitter.getName().isBlank()) {
        return error("Emitter file contains no named emitter");
      }
      boolean exists = bundle.getEmitters().stream().anyMatch(item -> sameName(item.getName(), emitter.getName()));
      if (exists && !McpToolHandler.getBoolean(args, "overwrite", false)) {
        return error("Emitter already exists: " + emitter.getName());
      }
      bundle.getEmitters().removeIf(item -> sameName(item.getName(), emitter.getName()));
      bundle.getEmitters().add(emitter);
      refreshAssets();
      return success("Imported emitter: " + emitter.getName())
          .add("name", emitter.getName())
          .add("type", "emitter")
          .build();
    } catch (Exception e) {
      return error("Could not import emitter: " + e.getMessage());
    }
  }

  private static JsonObject importBlueprint(JsonObject args) {
    ResourceBundle bundle = gameFile();
    Path path = sourcePath(args);
    if (bundle == null || path == null) {
      return error(bundle == null ? "No project is loaded" : "A valid blueprint 'path' is required");
    }
    try {
      Blueprint blueprint = XmlUtilities.read(Blueprint.class, path.toUri().toURL());
      if (blueprint == null) {
        return error("Blueprint could not be read");
      }
      if (blueprint.getName() == null || blueprint.getName().isBlank()) {
        blueprint.setName(FileUtilities.getFileName(path.getFileName().toString()));
      }
      boolean exists = bundle.getBluePrints().stream().anyMatch(item -> sameName(item.getName(), blueprint.getName()));
      if (exists && !McpToolHandler.getBoolean(args, "overwrite", false)) {
        return error("Blueprint already exists: " + blueprint.getName());
      }
      bundle.getBluePrints().removeIf(item -> sameName(item.getName(), blueprint.getName()));
      bundle.getBluePrints().add(blueprint);
      Resources.blueprints().add(blueprint.getName(), blueprint);
      refreshAssets();
      return success("Imported blueprint: " + blueprint.getName())
          .add("name", blueprint.getName())
          .add("type", "blueprint")
          .build();
    } catch (Exception e) {
      return error("Could not import blueprint: " + e.getMessage());
    }
  }

  private static JsonObject importTileset(JsonObject args) {
    ResourceBundle bundle = gameFile();
    Path path = sourcePath(args);
    if (bundle == null || path == null) {
      return error(bundle == null ? "No project is loaded" : "A valid TSX 'path' is required");
    }
    try {
      Tileset tileset = XmlUtilities.read(Tileset.class, path.toUri().toURL());
      if (tileset == null) {
        return error("Tileset could not be read");
      }
      tileset.finish(path.toUri().toURL());
      if (tileset.getName() == null || tileset.getName().isBlank()) {
        return error("Tileset file contains no name");
      }
      boolean exists = bundle.getTilesets().stream().anyMatch(item -> sameName(item.getName(), tileset.getName()));
      if (exists && !McpToolHandler.getBoolean(args, "overwrite", false)) {
        return error("Tileset already exists: " + tileset.getName());
      }
      bundle.getTilesets().removeIf(item -> sameName(item.getName(), tileset.getName()));
      Resources.tilesets().remove(tileset.getName());
      Editor.instance().loadTileset(tileset, false);
      Resources.tilesets().add(tileset.getName(), tileset);
      refreshAssets();
      return success("Imported tileset: " + tileset.getName())
          .add("resource", tilesetInfo(tileset))
          .build();
    } catch (Exception e) {
      return error("Could not import tileset: " + e.getMessage());
    }
  }

  private static JsonObject importSpriteDefinition(JsonObject args) {
    ResourceBundle bundle = gameFile();
    Path path = sourcePath(args);
    if (bundle == null || path == null) {
      return error(bundle == null ? "No project is loaded" : "A valid sprites.info 'path' is required");
    }
    int before = bundle.getSpriteSheets().size();
    Editor.instance().importSpriteFile(path);
    int imported = Math.max(0, bundle.getSpriteSheets().size() - before);
    refreshAssets();
    return success("Imported sprite definition file")
        .add("path", path.toString())
        .add("importedCount", imported)
        .build();
  }

  private static JsonObject importTextureAtlas(JsonObject args) {
    ResourceBundle bundle = gameFile();
    Path path = sourcePath(args);
    if (bundle == null || path == null) {
      return error(bundle == null ? "No project is loaded" : "A valid texture-atlas XML 'path' is required");
    }
    TextureAtlas atlas = TextureAtlas.read(path.toString());
    if (atlas == null) {
      return error("Texture atlas could not be read");
    }
    Resources.images().load(atlas);
    boolean overwrite = McpToolHandler.getBoolean(args, "overwrite", false);
    JsonArrayBuilder imported = Json.createArrayBuilder();
    for (TextureAtlas.Sprite sprite : atlas.getSprites()) {
      String name = FileUtilities.getFileName(sprite.getName());
      boolean exists = bundle.getSpriteSheets().stream().anyMatch(item -> sameName(item.getName(), name));
      if (exists && !overwrite) {
        continue;
      }
      BufferedImage image = Resources.images().get(sprite.getName());
      if (image == null) {
        continue;
      }
      SpritesheetResource resource = new SpritesheetResource(image, name, image.getWidth(), image.getHeight());
      bundle.getSpriteSheets().removeIf(item -> sameName(item.getName(), name));
      bundle.getSpriteSheets().add(resource);
      Resources.spritesheets().load(resource);
      imported.add(name);
    }
    refreshAssets();
    return success("Imported texture atlas sprites")
        .add("path", path.toString())
        .add("resources", imported)
        .build();
  }

  private static JsonObject listResources(JsonObject args) {
    String requestedType = normalizeType(McpToolHandler.getString(args, "type", "all"));
    JsonArrayBuilder resources = Json.createArrayBuilder();
    for (String type : List.of("spritesheet", "sound", "animation", "emitter", "blueprint", "tileset")) {
      if ("all".equals(requestedType) || type.equals(requestedType)) {
        addResources(resources, type);
      }
    }
    return success("Listed project resources")
        .add("type", requestedType)
        .add("resources", resources)
        .build();
  }

  private static JsonObject getResourceInfo(JsonObject args) {
    String type = normalizeType(McpToolHandler.getString(args, "type", ""));
    String name = McpToolHandler.getString(args, "name", null);
    JsonObject info = findResourceInfo(type, name);
    return info != null ? info : error("Resource not found: " + type + "/" + name);
  }

  private static JsonObject removeResource(JsonObject args) {
    ResourceBundle bundle = gameFile();
    if (bundle == null) {
      return error("No project is loaded");
    }
    String type = normalizeType(McpToolHandler.getString(args, "type", ""));
    String name = McpToolHandler.getString(args, "name", null);
    if (name == null || name.isBlank()) {
      return error("Resource 'name' is required");
    }

    boolean removed = switch (type) {
      case "spritesheet" -> {
        boolean changed = bundle.getSpriteSheets().removeIf(item -> sameName(item.getName(), name));
        yield Resources.spritesheets().remove(name) != null || changed;
      }
      case "sound" -> {
        boolean changed = bundle.getSounds().removeIf(item -> sameName(item.getName(), name));
        yield Resources.sounds().remove(name) != null || changed;
      }
      case "animation" -> Resources.animations().remove(name) != null;
      case "emitter" -> bundle.getEmitters().removeIf(item -> sameName(item.getName(), name));
      case "blueprint" -> {
        boolean changed = bundle.getBluePrints().removeIf(item -> sameName(item.getName(), name));
        yield Resources.blueprints().remove(name) != null || changed;
      }
      case "tileset" -> {
        boolean changed = bundle.getTilesets().removeIf(item -> sameName(item.getName(), name));
        yield Resources.tilesets().remove(name) != null || changed;
      }
      default -> false;
    };

    if (!removed) {
      return error("Resource not found: " + type + "/" + name);
    }
    refreshAssets();
    return success("Removed resource: " + type + "/" + name)
        .add("type", type)
        .add("name", name)
        .build();
  }

  private static JsonObject exportResource(JsonObject args) {
    String type = normalizeType(McpToolHandler.getString(args, "type", ""));
    String name = McpToolHandler.getString(args, "name", null);
    String pathValue = McpToolHandler.getString(args, "path", null);
    if (name == null || pathValue == null) {
      return error("Resource 'name' and destination 'path' are required");
    }
    try {
      Path path = resolvePath(pathValue);
      if (path.getParent() != null) {
        Files.createDirectories(path.getParent());
      }
      boolean exported = switch (type) {
        case "animation" -> Resources.animations().exportAseprite(Resources.animations().get(name), path);
        case "spritesheet" -> exportSpritesheet(name, path, McpToolHandler.getBoolean(args, "metadataOnly", false));
        case "sound" -> exportSound(name, path);
        case "emitter" -> exportXml(findEmitter(name), path);
        case "blueprint" -> exportXml(findBlueprint(name), path);
        case "tileset" -> exportXml(findTileset(name), path);
        default -> false;
      };
      if (!exported) {
        return error("Resource could not be exported: " + type + "/" + name);
      }
      return success("Exported resource: " + type + "/" + name)
          .add("type", type)
          .add("name", name)
          .add("filePath", path.toString())
          .add("fileUri", path.toUri().toString())
          .build();
    } catch (Exception e) {
      return error("Could not export resource: " + e.getMessage());
    }
  }

  private static boolean exportSpritesheet(String name, Path path, boolean metadataOnly) throws Exception {
    Spritesheet sprite = Resources.spritesheets().get(name);
    if (sprite == null) {
      return false;
    }
    if (metadataOnly) {
      return Resources.spritesheets().saveTo(path.toString(), true);
    }
    String format = sprite.getImageFormat() != null ? sprite.getImageFormat().toFileExtension().replace(".", "") : "png";
    if (format.isBlank()) {
      format = "png";
    }
    return ImageIO.write(sprite.getImage(), format, path.toFile());
  }

  private static boolean exportSound(String name, Path path) throws Exception {
    SoundResource sound = findSound(name);
    if (sound == null || sound.getData() == null) {
      return false;
    }
    Files.write(path, Codec.decode(sound.getData()));
    return true;
  }

  private static boolean exportXml(Object value, Path path) {
    if (value == null) {
      return false;
    }
    XmlUtilities.save(value, path);
    return Files.isRegularFile(path);
  }

  private static void addResources(JsonArrayBuilder result, String type) {
    ResourceBundle bundle = gameFile();
    if (bundle == null && !"animation".equals(type)) {
      return;
    }
    switch (type) {
      case "spritesheet" -> bundle.getSpriteSheets().forEach(item -> result.add(resourceSummary(type, item.getName())));
      case "sound" -> bundle.getSounds().forEach(item -> result.add(resourceSummary(type, item.getName())));
      case "animation" -> Resources.animations().getAll().forEach(item -> result.add(animationInfo(item)));
      case "emitter" -> bundle.getEmitters().forEach(item -> result.add(resourceSummary(type, item.getName())));
      case "blueprint" -> bundle.getBluePrints().forEach(item -> result.add(resourceSummary(type, item.getName())));
      case "tileset" -> bundle.getTilesets().forEach(item -> result.add(tilesetInfo(item)));
      default -> {
      }
    }
  }

  private static JsonObject findResourceInfo(String type, String name) {
    if (name == null) {
      return null;
    }
    return switch (type) {
      case "spritesheet" -> {
        SpritesheetResource resource = findSpritesheetResource(name);
        if (resource == null) {
          yield null;
        }
        yield Json.createObjectBuilder()
            .add("success", true)
            .add("type", type)
            .add("name", resource.getName())
            .add("frameWidth", resource.getWidth())
            .add("frameHeight", resource.getHeight())
            .add("keyFrameDurations", intArray(resource.getKeyframes()))
            .build();
      }
      case "sound" -> {
        SoundResource resource = findSound(name);
        yield resource == null ? null : Json.createObjectBuilder()
            .add("success", true)
            .add("type", type)
            .add("name", resource.getName())
            .add("format", resource.getFormat() != null ? resource.getFormat().toString() : "")
            .build();
      }
      case "animation" -> {
        Animation animation = Resources.animations().get(name);
        yield animation == null ? null : animationInfo(animation);
      }
      case "emitter" -> {
        EmitterAttributes emitter = findEmitter(name);
        yield emitter == null ? null : resourceSummary(type, emitter.getName());
      }
      case "blueprint" -> {
        Blueprint blueprint = findBlueprint(name);
        yield blueprint == null ? null : resourceSummary(type, blueprint.getName());
      }
      case "tileset" -> {
        Tileset tileset = findTileset(name);
        yield tileset == null ? null : tilesetInfo(tileset);
      }
      default -> null;
    };
  }

  static JsonObject animationInfo(Animation animation) {
    return Json.createObjectBuilder()
        .add("success", true)
        .add("type", "animation")
        .add("name", animation.getName())
        .add("spritesheet", animation.getSpritesheet() != null ? animation.getSpritesheet().getName() : "")
        .add("loop", animation.isLooping())
        .add("frameCount", animation.getKeyframes().size())
        .add("durations", intArray(animation.getKeyFrameDurations()))
        .add("totalDuration", animation.getTotalDuration())
        .build();
  }

  static JsonObject tilesetInfo(Tileset tileset) {
    return Json.createObjectBuilder()
        .add("success", true)
        .add("type", "tileset")
        .add("name", tileset.getName() != null ? tileset.getName() : "")
        .add("tileWidth", tileset.getTileWidth())
        .add("tileHeight", tileset.getTileHeight())
        .add("tileCount", tileset.getTileCount())
        .add("columns", tileset.getColumns())
        .add("terrainSetCount", tileset.getTerrainSets() != null ? tileset.getTerrainSets().size() : 0)
        .build();
  }

  private static JsonObject resourceSummary(String type, String name) {
    return Json.createObjectBuilder()
        .add("success", true)
        .add("type", type)
        .add("name", name != null ? name : "")
        .build();
  }

  private static JsonArrayBuilder intArray(int[] values) {
    JsonArrayBuilder result = Json.createArrayBuilder();
    if (values != null) {
      for (int value : values) {
        result.add(value);
      }
    }
    return result;
  }

  private static ResourceBundle gameFile() {
    return Editor.instance().getGameFile();
  }

  private static SpritesheetResource findSpritesheetResource(String name) {
    ResourceBundle bundle = gameFile();
    return bundle == null ? null : bundle.getSpriteSheets().stream()
        .filter(item -> sameName(item.getName(), name))
        .findFirst()
        .orElse(null);
  }

  private static SoundResource findSound(String name) {
    ResourceBundle bundle = gameFile();
    return bundle == null ? null : bundle.getSounds().stream()
        .filter(item -> sameName(item.getName(), name))
        .findFirst()
        .orElse(null);
  }

  private static EmitterAttributes findEmitter(String name) {
    ResourceBundle bundle = gameFile();
    return bundle == null ? null : bundle.getEmitters().stream()
        .filter(item -> sameName(item.getName(), name))
        .findFirst()
        .orElse(null);
  }

  private static Blueprint findBlueprint(String name) {
    ResourceBundle bundle = gameFile();
    return bundle == null ? null : bundle.getBluePrints().stream()
        .filter(item -> sameName(item.getName(), name))
        .findFirst()
        .orElse(null);
  }

  static Tileset findTileset(String name) {
    ResourceBundle bundle = gameFile();
    if (name == null || name.isBlank()) {
      if (Game.world().environment() != null && Game.world().environment().getMap() != null) {
        for (de.gurkenlabs.litiengine.environment.tilemap.ITileset ts : Game.world().environment().getMap().getTilesets()) {
          if (ts instanceof Tileset tileset) {
            return tileset;
          }
        }
      }
      if (bundle != null && !bundle.getTilesets().isEmpty()) {
        return bundle.getTilesets().get(0);
      }
      return null;
    }
    if (bundle != null) {
      Tileset projectTileset = bundle.getTilesets().stream()
          .filter(item -> sameName(item.getName(), name))
          .findFirst()
          .orElse(null);
      if (projectTileset != null) {
        return projectTileset;
      }
    }
    if (Game.world().environment() != null
        && Game.world().environment().getMap() != null) {
      Tileset mapTileset =
          Game.world().environment().getMap().getTilesets().stream()
              .filter(Tileset.class::isInstance)
              .map(Tileset.class::cast)
              .filter(item -> sameName(item.getName(), name))
              .findFirst()
              .orElse(null);
      if (mapTileset != null) {
        return mapTileset;
      }
    }
    return Resources.tilesets().get(name);
  }

  private static Path sourcePath(JsonObject args) {
    String raw = McpToolHandler.getString(args, "path", null);
    if (raw == null || raw.isBlank()) {
      return null;
    }
    try {
      Path path = resolvePath(raw);
      return Files.isRegularFile(path) ? path : null;
    } catch (Exception e) {
      return null;
    }
  }

  private static Path resolvePath(String raw) {
    Path path = Path.of(raw);
    if (!path.isAbsolute()) {
      Path projectPath = Editor.instance().getProjectPath();
      path = (projectPath != null ? projectPath : Path.of("").toAbsolutePath()).resolve(path);
    }
    return path.toAbsolutePath().normalize();
  }

  private static String normalizeType(String type) {
    if (type == null) {
      return "";
    }
    String normalized = type.toLowerCase(Locale.ROOT).replace("_", "").replace("-", "");
    return switch (normalized) {
      case "sprite", "sprites", "spritesheet", "spritesheets" -> "spritesheet";
      case "sound", "sounds", "audio" -> "sound";
      case "animation", "animations" -> "animation";
      case "emitter", "emitters", "particle", "particles" -> "emitter";
      case "blueprint", "blueprints", "template", "templates" -> "blueprint";
      case "tileset", "tilesets" -> "tileset";
      case "all", "" -> "all";
      default -> normalized;
    };
  }

  private static boolean sameName(String first, String second) {
    return first != null && second != null && first.equalsIgnoreCase(second);
  }

  static void refreshAssets() {
    try {
      if (UI.getAssetController() != null) {
        UI.getAssetController().refresh();
      }
    } catch (RuntimeException ignored) {
      // Headless MCP clients do not have an asset panel to refresh.
    }
  }

  private static JsonObjectBuilder success(String message) {
    return Json.createObjectBuilder().add("success", true).add("message", message);
  }

  private static JsonObject error(String message) {
    return Json.createObjectBuilder().add("success", false).add("error", message).build();
  }
}
