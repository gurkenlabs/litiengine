package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarFile;

/** Lazily indexes public types from the engine artifact and optional project classloaders. */
public final class EngineTypeCatalog {
  private static final String ENGINE_PACKAGE = "de/gurkenlabs/litiengine/";
  private static final Map<ClassLoader, List<Class<?>>> CACHE = new ConcurrentHashMap<>();

  private EngineTypeCatalog() {}

  public static List<Class<?>> publicTypes() {
    ClassLoader loader = Game.class.getClassLoader();
    return CACHE.computeIfAbsent(loader, EngineTypeCatalog::loadEngine);
  }

  public static List<Class<?>> projectTypes(ClassLoader loader) {
    if (loader == null) return List.of();
    return CACHE.computeIfAbsent(loader, EngineTypeCatalog::loadAll);
  }

  private static final Map<ClassLoader, Map<String, Class<?>>> NAME_LOOKUP_CACHE = new ConcurrentHashMap<>();

  public static Optional<Class<?>> findType(String name, ClassLoader loader) {
    if (name == null || name.isBlank()) return Optional.empty();
    ClassLoader effectiveLoader = loader == null ? Game.class.getClassLoader() : loader;
    Map<String, Class<?>> map = NAME_LOOKUP_CACHE.computeIfAbsent(effectiveLoader, l -> {
      Map<String, Class<?>> lookup = new ConcurrentHashMap<>();
      for (Class<?> type : projectTypes(l)) {
        lookup.putIfAbsent(type.getSimpleName(), type);
        lookup.putIfAbsent(type.getName(), type);
      }
      return lookup;
    });
    return Optional.ofNullable(map.get(name));
  }

  private static List<Class<?>> loadEngine(ClassLoader loader) {
    List<Class<?>> result = new ArrayList<>();
    Class<?>[] sentinelClasses = {
      Game.class,
      de.gurkenlabs.litiengine.entities.Entity.class,
      de.gurkenlabs.litiengine.abilities.Ability.class,
      de.gurkenlabs.litiengine.environment.Environment.class,
      de.gurkenlabs.litiengine.physics.PhysicsEngine.class,
      de.gurkenlabs.litiengine.graphics.RenderEngine.class,
      de.gurkenlabs.litiengine.sound.SoundEngine.class
    };
    for (Class<?> sentinel : sentinelClasses) {
      try {
        if (sentinel.getProtectionDomain() != null && sentinel.getProtectionDomain().getCodeSource() != null) {
          Path location = Path.of(sentinel.getProtectionDomain().getCodeSource().getLocation().toURI());
          if (Files.isDirectory(location)) result.addAll(fromDirectory(location, loader, ENGINE_PACKAGE));
          else if (location.toString().endsWith(".jar")) result.addAll(fromJar(location, loader, ENGINE_PACKAGE));
        }
      } catch (Exception ignored) {
      }
    }
    scanClasspath(loader, ENGINE_PACKAGE, result);
    return result.stream().distinct().sorted(typeOrder()).toList();
  }

  private static List<Class<?>> loadAll(ClassLoader loader) {
    List<Class<?>> result = new ArrayList<>(loadEngine(loader));
    try {
      java.net.URL[] urls = loader instanceof java.net.URLClassLoader urlLoader ? urlLoader.getURLs() : null;
      if (urls != null) {
        for (java.net.URL url : urls) {
          Path path = Path.of(url.toURI());
          if (Files.isDirectory(path)) result.addAll(fromDirectory(path, loader, ""));
          else if (path.toString().endsWith(".jar")) result.addAll(fromJar(path, loader, ""));
        }
      }
    } catch (Exception ignored) {
    }
    scanClasspath(loader, "", result);
    return result.stream().distinct().sorted(typeOrder()).toList();
  }

  private static void scanClasspath(ClassLoader loader, String packagePrefix, List<Class<?>> result) {
    String classpath = System.getProperty("java.class.path");
    if (classpath == null || classpath.isBlank()) return;
    String[] entries = classpath.split(java.io.File.pathSeparator);
    for (String entry : entries) {
      if (entry.isBlank()) continue;
      try {
        Path path = Path.of(entry).toAbsolutePath().normalize();
        if (!Files.exists(path)) continue;
        if (Files.isDirectory(path)) result.addAll(fromDirectory(path, loader, packagePrefix));
        else if (path.toString().endsWith(".jar")) result.addAll(fromJar(path, loader, packagePrefix));
      } catch (Exception ignored) {
      }
    }
  }

  private static List<Class<?>> fromDirectory(Path root, ClassLoader loader, String packagePrefix) {
    Path packageRoot = packagePrefix.isEmpty() ? root : root.resolve(packagePrefix);
    if (!Files.isDirectory(packageRoot)) return List.of();
    try (var files = Files.walk(packageRoot)) {
      return files.filter(path -> path.getFileName().toString().endsWith(".class"))
        .map(root::relativize).map(EngineTypeCatalog::className)
        .map(name -> load(name, loader)).filter(Optional::isPresent)
        .map(Optional::get).sorted(typeOrder()).toList();
    } catch (IOException error) {
      return List.of();
    }
  }

  private static List<Class<?>> fromJar(Path location, ClassLoader loader, String packagePrefix) {
    try (JarFile jar = new JarFile(location.toFile())) {
      return jar.stream().map(entry -> entry.getName())
        .filter(name -> packagePrefix.isEmpty() || name.startsWith(packagePrefix))
        .filter(name -> name.endsWith(".class")).map(EngineTypeCatalog::className)
        .map(name -> load(name, loader)).filter(Optional::isPresent)
        .map(Optional::get).sorted(typeOrder()).toList();
    } catch (IOException error) {
      return List.of();
    }
  }

  private static String className(Path path) {
    return className(path.toString().replace('\\', '/'));
  }

  private static String className(String path) {
    return path.substring(0, path.length() - ".class".length()).replace('/', '.');
  }

  private static Optional<Class<?>> load(String name, ClassLoader loader) {
    if (!isRelevantProjectClass(name)) return Optional.empty();
    try {
      Class<?> type = Class.forName(name, false, loader);
      return java.lang.reflect.Modifier.isPublic(type.getModifiers()) ? Optional.of(type) : Optional.empty();
    } catch (ClassNotFoundException | LinkageError ignored) {
      return Optional.empty();
    }
  }

  private static boolean isRelevantProjectClass(String name) {
    if (name.contains("$") || name.endsWith("module-info")) return false;
    if (name.startsWith("org.apache.") || name.startsWith("com.fasterxml.") || name.startsWith("org.codehaus.groovy.")
      || name.startsWith("me.friwi.") || name.startsWith("org.cef.") || name.startsWith("jakarta.")
      || name.startsWith("org.eclipse.") || name.startsWith("org.junit.") || name.startsWith("org.hamcrest.")
      || name.startsWith("com.sun.") || name.startsWith("sun.") || name.startsWith("java.awt.peer.")) return false;
    return true;
  }

  private static Comparator<Class<?>> typeOrder() {
    return Comparator.comparing((Class<?> type) -> type.getSimpleName(), String.CASE_INSENSITIVE_ORDER)
      .thenComparing(Class::getName);
  }
}
