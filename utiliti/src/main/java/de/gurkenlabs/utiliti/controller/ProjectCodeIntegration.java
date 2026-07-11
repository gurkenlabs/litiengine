package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.Prop;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectDefinition;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectPropertyDefinition;
import de.gurkenlabs.litiengine.environment.tilemap.MapObjectType;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/** Discovers editor-visible entity types from a Gradle project's compiled main classes. */
public final class ProjectCodeIntegration implements AutoCloseable {
  private static final Logger log = Logger.getLogger(ProjectCodeIntegration.class.getName());
  private static final Path GRADLE_CLASSES = Path.of("build", "classes", "java", "main");

  private URLClassLoader classLoader;
  private List<Definition> definitions = List.of();

  public List<Definition> getDefinitions() {
    return definitions;
  }

  public void reload(Path gameFile) {
    close();
    if (gameFile == null || gameFile.getParent() == null) {
      return;
    }

    Path classesDirectory = gameFile.getParent().resolve(GRADLE_CLASSES);
    if (!Files.isDirectory(classesDirectory)) {
      return;
    }

    try {
      classLoader = new URLClassLoader(new URL[] {classesDirectory.toUri().toURL()}, getClass().getClassLoader());
      List<Definition> discovered = new ArrayList<>();
      try (var paths = Files.walk(classesDirectory)) {
        paths.filter(path -> path.toString().endsWith(".class"))
          .map(path -> className(classesDirectory, path))
          .filter(name -> !name.endsWith("module-info") && !name.contains("$$"))
          .forEach(name -> discover(name, discovered));
      }
      discovered.sort(Comparator.comparing(Definition::displayName));
      definitions = List.copyOf(discovered);
    } catch (IOException e) {
      log.log(Level.WARNING, "Could not inspect compiled project classes in " + classesDirectory, e);
      close();
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
}
