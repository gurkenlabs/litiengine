package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Direction;
import de.gurkenlabs.litiengine.entities.LightSource;
import de.gurkenlabs.litiengine.entities.Material;
import de.gurkenlabs.litiengine.entities.Trigger.TriggerActivation;
import de.gurkenlabs.litiengine.environment.tilemap.ICustomProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectProperty;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.environment.tilemap.TmxPropertyMetadataRegistry;
import de.gurkenlabs.litiengine.environment.tilemap.xml.MapObject;
import de.gurkenlabs.litiengine.graphics.StaticShadowType;
import de.gurkenlabs.litiengine.physics.Collision;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SoundResource;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.SpriteVariantSelector;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class McpEntityValidator {
  private McpEntityValidator() {}

  static List<String> validateForCreation(MapObject mapObject, JsonObject arguments) {
    List<String> errors = new ArrayList<>();
    if (mapObject == null) {
      errors.add("Entity is missing");
      return errors;
    }

    MapObjectType type = MapObjectType.get(mapObject.getType());
    if (type == null) {
      errors.add("Unknown map object type: " + mapObject.getType());
      return errors;
    }

    validateFiniteNumber(arguments, "x", errors);
    validateFiniteNumber(arguments, "y", errors);
    validateFiniteNumber(arguments, "width", errors);
    validateFiniteNumber(arguments, "height", errors);
    validateArgumentTypes(arguments, errors);
    if (!Float.isFinite(mapObject.getX()) || !Float.isFinite(mapObject.getY())) {
      errors.add("Entity coordinates must be finite numbers");
    }
    if (!Float.isFinite(mapObject.getWidth())
        || !Float.isFinite(mapObject.getHeight())
        || mapObject.getWidth() <= 0
        || mapObject.getHeight() <= 0) {
      errors.add("Entity width and height must be finite numbers greater than zero");
    }

    validateMetadataTypes(mapObject, errors);
    validateOptionalEnum(
        mapObject, MapObjectProperty.COLLISION_TYPE, Collision.class, errors);
    validateRequestedCollisionType(arguments, errors);

    switch (type) {
      case PROP:
        validateRequiredSprite(mapObject, type, errors);
        validateMaterial(mapObject, errors);
        validateNonNegative(mapObject, MapObjectProperty.COLLISIONBOX_WIDTH, errors);
        validateNonNegative(mapObject, MapObjectProperty.COLLISIONBOX_HEIGHT, errors);
        break;
      case CREATURE:
        validateRequiredSprite(mapObject, type, errors);
        validateOptionalEnum(
            mapObject, MapObjectProperty.SPAWN_DIRECTION, Direction.class, errors);
        validateNonNegative(mapObject, MapObjectProperty.MOVEMENT_VELOCITY, errors);
        validateNonNegative(mapObject, MapObjectProperty.COMBAT_HITPOINTS, errors);
        validateNonNegative(mapObject, MapObjectProperty.COMBAT_TEAM, errors);
        break;
      case SOUNDSOURCE:
        validateRequiredSound(mapObject, errors);
        validateRange(mapObject, MapObjectProperty.SOUND_VOLUME, 0, 1, errors);
        validateNonNegative(mapObject, MapObjectProperty.SOUND_RANGE, errors);
        break;
      case LIGHTSOURCE:
        validateRequiredColor(mapObject, MapObjectProperty.LIGHT_COLOR, errors);
        validateRange(mapObject, MapObjectProperty.LIGHT_INTENSITY, 0, 255, errors);
        validateOptionalEnum(
            mapObject, MapObjectProperty.LIGHT_SHAPE, LightSource.Type.class, errors);
        break;
      case TRIGGER:
        validateOptionalEnum(
            mapObject,
            MapObjectProperty.TRIGGER_ACTIVATION,
            TriggerActivation.class,
            errors);
        validateNonNegative(mapObject, MapObjectProperty.TRIGGER_COOLDOWN, errors);
        validateIntegerList(mapObject, MapObjectProperty.TRIGGER_TARGETS, "targets", errors);
        validateIntegerList(mapObject, MapObjectProperty.TRIGGER_ACTIVATORS, "activators", errors);
        break;
      case SPAWNPOINT:
        validateOptionalEnum(
            mapObject, MapObjectProperty.SPAWN_DIRECTION, Direction.class, errors);
        break;
      case STATICSHADOW:
        validateOptionalEnum(
            mapObject, MapObjectProperty.SHADOW_TYPE, StaticShadowType.class, errors);
        break;
      case COLLISIONBOX:
      case EMITTER:
      case AREA:
        break;
    }

    if (arguments != null && arguments.containsKey("cooldown")) {
      Double cooldown = readNumber(arguments.get("cooldown"));
      if (cooldown == null || !Double.isFinite(cooldown) || cooldown < 0) {
        errors.add("cooldown must be a non-negative number");
      }
    }
    if (arguments != null && arguments.containsKey("properties")) {
      JsonValue properties = arguments.get("properties");
      if (!(properties instanceof JsonObject propertyObject)) {
        errors.add("properties must be an object of scalar values");
      } else {
        for (Map.Entry<String, JsonValue> entry : propertyObject.entrySet()) {
          JsonValue.ValueType valueType = entry.getValue().getValueType();
          if (valueType == JsonValue.ValueType.ARRAY
              || valueType == JsonValue.ValueType.OBJECT
              || valueType == JsonValue.ValueType.NULL) {
            errors.add(
                "Property '" + entry.getKey() + "' must be a string, number, or boolean");
          }
        }
      }
    }

    return new ArrayList<>(new LinkedHashSet<>(errors));
  }

  static String normalizeSpriteReference(MapObjectType type, String reference) {
    if (reference == null || reference.isBlank()) {
      return reference;
    }

    String normalized = findSpriteFamily(type, reference, Resources.spritesheets().getAll());
    if (normalized != null) {
      return normalized;
    }

    try {
      Collection<SpritesheetResource> resources =
          Editor.instance().getGameFile().getSpriteSheets();
      Map<String, SpritesheetResource> families =
          type == MapObjectType.PROP
              ? SpriteVariantSelector.selectBasePropResources(resources)
              : SpriteVariantSelector.selectBaseCreatureResources(resources);
      for (Map.Entry<String, SpritesheetResource> entry : families.entrySet()) {
        if (entry.getKey().equalsIgnoreCase(reference)
            || entry.getValue().getName().equalsIgnoreCase(reference)) {
          return entry.getKey();
        }
      }

      // Fuzzy matching fallback (e.g. bed3 -> prop-bed3-intact)
      String cleanRef = reference.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
      if (!cleanRef.isBlank()) {
        for (String key : families.keySet()) {
          String cleanKey = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
          if (cleanKey.endsWith(cleanRef) || cleanRef.endsWith(cleanKey)) {
            return key;
          }
        }
        for (String key : families.keySet()) {
          String cleanKey = key.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
          if (cleanKey.contains(cleanRef) || cleanRef.contains(cleanKey)) {
            return key;
          }
        }
      }
    } catch (RuntimeException _) {
      // Project metadata is unavailable while the editor is starting or shutting down.
    }

    return reference;
  }

  private static String findSpriteFamily(
      MapObjectType type,
      String reference,
      Collection<de.gurkenlabs.litiengine.graphics.Spritesheet> spritesheets) {
    Map<String, String> families =
        type == MapObjectType.PROP
            ? SpriteVariantSelector.selectBasePropSpriteNames(spritesheets)
            : SpriteVariantSelector.selectBaseCreatureSpriteNames(spritesheets);
    for (Map.Entry<String, String> entry : families.entrySet()) {
      if (entry.getKey().equalsIgnoreCase(reference)
          || entry.getValue().equalsIgnoreCase(reference)) {
        return entry.getKey();
      }
    }
    return null;
  }

  private static void validateRequiredSprite(
      MapObject mapObject, MapObjectType type, List<String> errors) {
    String sprite = mapObject.getStringValue(MapObjectProperty.SPRITESHEETNAME, null);
    if (sprite == null || sprite.isBlank()) {
      errors.add(type + " requires a spritesheetName");
      return;
    }
    String normalized = normalizeSpriteReference(type, sprite);
    if (normalized == null
        || normalized.isBlank()
        || !hasSpriteFamily(type, normalized)) {
      String validHint = "";
      try {
        Collection<SpritesheetResource> resources = Editor.instance().getGameFile().getSpriteSheets();
        Map<String, SpritesheetResource> families = type == MapObjectType.PROP
            ? SpriteVariantSelector.selectBasePropResources(resources)
            : SpriteVariantSelector.selectBaseCreatureResources(resources);
        if (!families.isEmpty()) {
          List<String> samples = families.keySet().stream().limit(10).sorted().toList();
          validHint = ". Available " + type.name().toLowerCase(Locale.ROOT) + " sprites include: " + samples
              + (families.size() > 10 ? " (and " + (families.size() - 10) + " more)" : "");
        }
      } catch (RuntimeException _) {}
      errors.add(
          "spritesheetName '"
              + sprite
              + "' does not resolve to an existing "
              + type.name().toLowerCase(Locale.ROOT)
              + " sprite family"
              + validHint);
    }
  }

  private static boolean hasSpriteFamily(MapObjectType type, String reference) {
    if (findSpriteFamily(type, reference, Resources.spritesheets().getAll()) != null) {
      return true;
    }
    try {
      Collection<SpritesheetResource> resources =
          Editor.instance().getGameFile().getSpriteSheets();
      Map<String, SpritesheetResource> families =
          type == MapObjectType.PROP
              ? SpriteVariantSelector.selectBasePropResources(resources)
              : SpriteVariantSelector.selectBaseCreatureResources(resources);
      return families.keySet().stream().anyMatch(name -> name.equalsIgnoreCase(reference));
    } catch (RuntimeException _) {
      return false;
    }
  }

  private static void validateRequiredSound(MapObject mapObject, List<String> errors) {
    String soundName = mapObject.getStringValue(MapObjectProperty.SOUND_NAME, null);
    if (soundName == null || soundName.isBlank()) {
      errors.add("SOUNDSOURCE requires a soundName");
      return;
    }
    if (Resources.sounds().contains(soundName)) {
      return;
    }
    try {
      for (SoundResource sound : Editor.instance().getGameFile().getSounds()) {
        if (sound != null
            && sound.getName() != null
            && sound.getName().equalsIgnoreCase(soundName)) {
          return;
        }
      }
    } catch (RuntimeException _) {
      // Fall through to the validation error.
    }
    errors.add("soundName '" + soundName + "' does not exist in the project");
  }

  private static void validateRequiredColor(
      MapObject mapObject, String property, List<String> errors) {
    String color = mapObject.getStringValue(property, null);
    if (color == null
        || !color.matches("(?i)^#?(?:[0-9a-f]{6}|[0-9a-f]{8})$")) {
      errors.add(property + " must be a valid 6- or 8-digit hexadecimal color");
    }
  }

  private static void validateMaterial(MapObject mapObject, List<String> errors) {
    String value = mapObject.getStringValue(MapObjectProperty.PROP_MATERIAL, null);
    if (value == null || value.isBlank() || "UNDEFINED".equalsIgnoreCase(value)) {
      return;
    }
    if (Material.get(value) == Material.UNDEFINED) {
      String valid = String.join(", ", Material.getMaterials().stream().map(Material::getName).sorted().toList());
      errors.add("material '" + value + "' is not a registered material. Valid materials are: [" + valid + "]");
    }
  }

  private static void validateIntegerList(
      MapObject mapObject, String property, String paramName, List<String> errors) {
    String val = mapObject.getStringValue(property, null);
    if (val == null || val.isBlank()) {
      return;
    }
    for (String token : val.split(",")) {
      if (token == null || token.isBlank()) {
        continue;
      }
      try {
        Integer.parseInt(token.trim());
      } catch (NumberFormatException _) {
        errors.add(paramName + " must be a comma-separated list of integer entity IDs (e.g. '101,102'), not string names ('" + val + "')");
        break;
      }
    }
  }

  private static void validateArgumentTypes(
      JsonObject arguments, List<String> errors) {
    if (arguments == null) {
      return;
    }
    for (String property :
        List.of("velocity", "volume")) {
      if (arguments.containsKey(property) && readNumber(arguments.get(property)) == null) {
        errors.add(property + " must be a number");
      }
    }
    for (String property :
        List.of("hitpoints", "team", "cooldown", "lightIntensity", "range")) {
      if (!arguments.containsKey(property)) {
        continue;
      }
      Double value = readNumber(arguments.get(property));
      if (value == null || !Double.isFinite(value) || value != Math.rint(value)) {
        errors.add(property + " must be an integer");
      }
    }
    for (String property :
        List.of(
            "addShadow",
            "indestructible",
            "collision",
            "scaleSprite",
            "oneTime",
            "lightActive",
            "loop")) {
      if (!arguments.containsKey(property)) {
        continue;
      }
      JsonValue value = arguments.get(property);
      boolean valid =
          value.getValueType() == JsonValue.ValueType.TRUE
              || value.getValueType() == JsonValue.ValueType.FALSE
              || value instanceof JsonString string
                  && ("true".equalsIgnoreCase(string.getString())
                      || "false".equalsIgnoreCase(string.getString()));
      if (!valid) {
        errors.add(property + " must be a boolean");
      }
    }
    for (String property :
        List.of(
            "name",
            "layer",
            "spritesheetName",
            "material",
            "collisionType",
            "message",
            "activation",
            "targets",
            "spawnType",
            "direction",
            "lightColor",
            "lightShape",
            "shadowType",
            "emitterData",
            "soundName",
            "itemType",
            "shapeType")) {
      if (arguments.containsKey(property)
          && arguments.get(property).getValueType() != JsonValue.ValueType.STRING) {
        errors.add(property + " must be a string");
      }
    }
  }

  private static void validateRequestedCollisionType(
      JsonObject arguments, List<String> errors) {
    if (arguments == null || !arguments.containsKey("collisionType")) {
      return;
    }
    String value =
        arguments.get("collisionType") instanceof JsonString string
            ? string.getString()
            : null;
    if (value == null || value.isBlank()) {
      return;
    }
    if (!Collision.STATIC.name().equalsIgnoreCase(value)
        && !Collision.DYNAMIC.name().equalsIgnoreCase(value)) {
      errors.add(
          MapObjectProperty.COLLISION_TYPE + " must be one of STATIC, DYNAMIC");
    }
  }

  private static void validateMetadataTypes(MapObject mapObject, List<String> errors) {
    try {
      for (Map.Entry<String, ICustomProperty> entry : mapObject.getProperties().entrySet()) {
        if (entry.getValue() == null) {
          continue;
        }
        String value = entry.getValue().getAsString();
        TmxPropertyMetadataRegistry.PropertyMetadata metadata =
            TmxPropertyMetadataRegistry.getProperty(entry.getKey());
        if (metadata == null || value == null) {
          continue;
        }
        switch (metadata.type().toLowerCase(Locale.ROOT)) {
          case "boolean":
            if (!"true".equalsIgnoreCase(value) && !"false".equalsIgnoreCase(value)) {
              errors.add(entry.getKey() + " must be a boolean");
            }
            break;
          case "int":
            try {
              Integer.parseInt(value);
            } catch (NumberFormatException _) {
              errors.add(entry.getKey() + " must be an integer");
            }
            break;
          case "float":
          case "number":
            try {
              if (!Double.isFinite(Double.parseDouble(value))) {
                errors.add(entry.getKey() + " must be a finite number");
              }
            } catch (NumberFormatException _) {
              errors.add(entry.getKey() + " must be a number");
            }
            break;
          case "color":
            if (!value.matches("(?i)^#?(?:[0-9a-f]{6}|[0-9a-f]{8})$")) {
              errors.add(entry.getKey() + " must be a valid hexadecimal color");
            }
            break;
          default:
            break;
        }
      }
    } catch (Throwable _) {
      // Ignore class loading fallback in metadata validation
    }
  }

  private static <T extends Enum<T>> void validateOptionalEnum(
      MapObject mapObject, String property, Class<T> enumType, List<String> errors) {
    String value = mapObject.getStringValue(property, null);
    if (value == null || value.isBlank()) {
      return;
    }
    for (T constant : enumType.getEnumConstants()) {
      if (constant.name().equalsIgnoreCase(value)) {
        return;
      }
    }
    errors.add(
        property
            + " must be one of "
            + String.join(
                ", ",
                java.util.Arrays.stream(enumType.getEnumConstants())
                    .map(Enum::name)
                    .toList()));
  }

  private static void validateNonNegative(
      MapObject mapObject, String property, List<String> errors) {
    validateRange(mapObject, property, 0, Double.POSITIVE_INFINITY, errors);
  }

  private static void validateRange(
      MapObject mapObject,
      String property,
      double minimum,
      double maximum,
      List<String> errors) {
    String raw = mapObject.getStringValue(property, null);
    if (raw == null || raw.isBlank()) {
      return;
    }
    try {
      double value = Double.parseDouble(raw);
      if (!Double.isFinite(value) || value < minimum || value > maximum) {
        errors.add(
            property
                + " must be between "
                + formatBound(minimum)
                + " and "
                + formatBound(maximum));
      }
    } catch (NumberFormatException _) {
      errors.add(property + " must be a number");
    }
  }

  private static String formatBound(double bound) {
    return Double.isInfinite(bound) ? "infinity" : Double.toString(bound);
  }

  private static void validateFiniteNumber(
      JsonObject arguments, String property, List<String> errors) {
    if (arguments == null || !arguments.containsKey(property)) {
      return;
    }
    Double value = readNumber(arguments.get(property));
    if (value == null || !Double.isFinite(value)) {
      errors.add(property + " must be a finite number");
    }
  }

  private static Double readNumber(JsonValue value) {
    if (value instanceof JsonNumber number) {
      return number.doubleValue();
    }
    if (value != null && value.getValueType() == JsonValue.ValueType.STRING) {
      try {
        return Double.parseDouble(((JsonString) value).getString());
      } catch (NumberFormatException _) {
        return null;
      }
    }
    return null;
  }
}
