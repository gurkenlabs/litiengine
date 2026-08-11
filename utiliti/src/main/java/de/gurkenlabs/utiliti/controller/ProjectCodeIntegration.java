package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityController;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectDefinition;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectPropertyDefinition;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptInfo;
import de.gurkenlabs.litiengine.scripting.ScriptInstance;
import de.gurkenlabs.litiengine.scripting.ScriptProperty;
import de.gurkenlabs.litiengine.util.ReflectionUtilities;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Discovers editor-visible entity types from project compiled main classes (Gradle, Kotlin, Maven, etc.). */
public final class ProjectCodeIntegration implements AutoCloseable {
  private static final Logger log = Logger.getLogger(ProjectCodeIntegration.class.getName());
  private static final List<Path> CLASS_DIRECTORIES = List.of(
    Path.of("build", "classes", "java", "main"),
    Path.of("build", "classes", "kotlin", "main"),
    Path.of("build", "classes", "groovy", "main"),
    Path.of("target", "classes"),
    Path.of("bin", "main"),
    Path.of("bin"),
    Path.of("out", "production", "main"),
    Path.of("out", "production", "classes")
  );

  private URLClassLoader classLoader;
  private List<Definition> definitions = List.of();
  private List<ScriptClassDefinition> scriptDefinitions = List.of();
  private List<ControllerDefinition> controllerDefinitions = List.of();

  public List<Definition> getDefinitions() {
    return definitions;
  }

  /** Returns compiled Java and Groovy script implementations visible to utiLITI. */
  public List<ScriptClassDefinition> getScriptDefinitions() {
    return this.scriptDefinitions;
  }

  /** Returns controller contracts and implementations discovered in compiled project output. */
  public List<ControllerDefinition> getControllerDefinitions() {
    return this.controllerDefinitions;
  }

  /** Returns the loader containing the latest compiled project classes. */
  public ClassLoader getClassLoader() {
    return this.classLoader;
  }

  public void reload(Path gameFile) {
    Path root = gameFile == null || gameFile.getParent() == null ? null : gameFile.getParent();
    List<Path> outputs = root == null ? List.of() : CLASS_DIRECTORIES.stream().map(root::resolve).toList();
    this.reload(outputs);
  }

  /** Reloads project types from the output directories supplied by the resolved project model. */
  public void reloadProject(ProjectModel project) {
    this.reload(project == null ? List.of() : project.outputDirectories());
  }

  private void reload(List<Path> outputDirectories) {
    close();
    List<Path> validDirectories = outputDirectories.stream()
      .filter(Files::isDirectory)
      .distinct()
      .toList();

    if (validDirectories.isEmpty()) {
      return;
    }

    try {
      URL[] urls = new URL[validDirectories.size()];
      for (int i = 0; i < validDirectories.size(); i++) {
        urls[i] = validDirectories.get(i).toUri().toURL();
      }
      classLoader = new URLClassLoader(urls, getClass().getClassLoader());
      List<Definition> discovered = new ArrayList<>();
      List<ScriptClassDefinition> discoveredScripts = new ArrayList<>();
      List<ControllerDefinition> discoveredControllers = new ArrayList<>();

      for (Path classesDirectory : validDirectories) {
        try (var paths = Files.walk(classesDirectory)) {
          paths.filter(path -> path.toString().endsWith(".class"))
            .map(path -> className(classesDirectory, path))
            .filter(name -> !name.endsWith("module-info") && !name.contains("$$"))
            .forEach(name -> {
              discover(name, discovered);
              discoverScript(name, discoveredScripts);
              discoverController(name, discoveredControllers);
            });
        }
      }
      discovered.sort(Comparator.comparing(Definition::displayName));
      definitions = List.copyOf(discovered);
      discoveredScripts.sort(Comparator.comparing(ScriptClassDefinition::displayName));
      scriptDefinitions = List.copyOf(discoveredScripts);
      discoveredControllers.sort(Comparator.comparing(ControllerDefinition::displayName));
      controllerDefinitions = List.copyOf(discoveredControllers);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not inspect compiled project classes in " + validDirectories, e);
      close();
    }
  }

  private void discoverController(String className, List<ControllerDefinition> discovered) {
    try {
      Class<?> type = Class.forName(className, false, this.classLoader);
      if (!IEntityController.class.isAssignableFrom(type) || type == IEntityController.class) return;
      boolean contract = type.isInterface() || Modifier.isAbstract(type.getModifiers());
      if (!contract && !Modifier.isPublic(type.getModifiers())) return;
      discovered.add(new ControllerDefinition(type.getSimpleName(), type.getName(), contract));
    } catch (LinkageError | ClassNotFoundException e) {
      // Project output can reference optional dependencies that are not available to the editor.
    }
  }

  private void discoverScript(String className, List<ScriptClassDefinition> discovered) {
    try {
      Class<?> type = Class.forName(className, false, this.classLoader);
      if (!ScriptInstance.class.isAssignableFrom(type) || type.isInterface() || Modifier.isAbstract(type.getModifiers())) return;
      type.asSubclass(ScriptInstance.class).getConstructor();
      ScriptInfo info = type.getAnnotation(ScriptInfo.class);
      if (info == null || info.id().isBlank()) return;
      List<ScriptPropertyDefinition> properties = new ArrayList<>();
      for (Field field : ReflectionUtilities.getAllFields(new ArrayList<>(), type)) {
        ScriptProperty property = field.getAnnotation(ScriptProperty.class);
        if (property == null) continue;
        properties.add(new ScriptPropertyDefinition(field.getName(), property.name().isBlank() ? field.getName() : property.name(),
          property.description(), property.category(), property.type().isBlank() ? field.getType().getName() : property.type(),
          property.defaultValue(), property.min(), property.max(), property.unit(), property.required()));
      }
      String displayName = info.name().isBlank() ? type.getSimpleName() : info.name();
      String targetType = info.target() == Object.class ? null : info.target().getName();
      discovered.add(new ScriptClassDefinition(info.id(), displayName, type.getName(), info.host(), targetType, List.copyOf(properties)));
    } catch (LinkageError | ClassNotFoundException | NoSuchMethodException e) {
      // Only complete, compatible script classes are shown.
    }
  }



  private static String className(Path root, Path classFile) {
    return root.relativize(classFile).toString().replace('\\', '.').replace('/', '.').replaceFirst("\\.class$", "");
  }

  @SuppressWarnings("unchecked")
  private void discover(String className, List<Definition> discovered) {
    try {
      Class<?> type = Class.forName(className, false, classLoader);
      if (!IEntity.class.isAssignableFrom(type) || type.isInterface() || Modifier.isAbstract(type.getModifiers())) {
        return;
      }
      MapObjectDefinition definition = type.getAnnotation(MapObjectDefinition.class);
      MapObjectType baseType = Creature.class.isAssignableFrom(type) ? MapObjectType.CREATURE : Prop.class.isAssignableFrom(type) ? MapObjectType.PROP : null;
      if (baseType == null) {
        return;
      }
      String id = definition == null || definition.id().isBlank() ? type.getName() : definition.id();
      String displayName = definition == null || definition.displayName().isBlank() ? type.getSimpleName() : definition.displayName();
      MapObjectType declaredBaseType = definition == null ? baseType : definition.baseType();
      if (declaredBaseType != baseType) {
        log.warning("Ignoring map object definition " + type.getName() + ": declared base type does not match its implementation class.");
        return;
      }
      discovered.add(new Definition(id, displayName, type.getName(), declaredBaseType,
        definition == null ? List.of() : List.of(definition.properties())));
    } catch (LinkageError | ClassNotFoundException e) {
      log.log(Level.FINE, "Could not inspect project class " + className, e);
    }
  }

  @Override
  public void close() {
    definitions = List.of();
    scriptDefinitions = List.of();
    controllerDefinitions = List.of();
    if (classLoader != null) {
      try {
        classLoader.close();
      } catch (IOException e) {
        log.log(Level.FINE, "Could not close project class loader", e);
      }
      classLoader = null;
    }
  }

  public record Definition(String id, String displayName, String className, MapObjectType baseType, List<MapObjectPropertyDefinition> properties) {
  }

  public record ScriptClassDefinition(String id, String displayName, String className, ScriptHostType host,
                                      String targetType, List<ScriptPropertyDefinition> properties) {
  }

  public record ScriptPropertyDefinition(String name, String displayName, String description, String category,
                                         String type, String defaultValue, double min, double max, String unit,
                                         boolean required) {
  }

  public record ControllerDefinition(String displayName, String className, boolean contract) {
  }
}
