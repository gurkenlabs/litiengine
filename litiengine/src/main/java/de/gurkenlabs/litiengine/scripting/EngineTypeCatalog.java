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

/// Lazily indexes public types from the engine artifact and optional project classloaders.
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
  private static final Map<ClassLoader, Map<String, List<Class<?>>>> PACKAGE_LOOKUP_CACHE = new ConcurrentHashMap<>();

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

  public static List<Class<?>> typesInPackage(String packageName, ClassLoader loader) {
    if (packageName == null || packageName.isBlank()) return List.of();
    ClassLoader effectiveLoader = loader == null ? Game.class.getClassLoader() : loader;
    Map<String, List<Class<?>>> map = PACKAGE_LOOKUP_CACHE.computeIfAbsent(effectiveLoader, l -> {
      Map<String, List<Class<?>>> lookup = new ConcurrentHashMap<>();
      for (Class<?> type : projectTypes(l)) {
        String pkg = type.getPackageName();
        if (pkg != null && !pkg.isBlank()) {
          lookup.computeIfAbsent(pkg, k -> new ArrayList<>()).add(type);
        }
      }
      return lookup;
    });
    return map.getOrDefault(packageName, List.of());
  }


  private static final List<Class<?>> STANDARD_JDK_TYPES = List.of(
    java.awt.Color.class,
    java.awt.Font.class,
    java.awt.Graphics2D.class,
    java.awt.geom.Point2D.class,
    java.awt.geom.Rectangle2D.class,
    java.awt.geom.Ellipse2D.class,
    java.awt.geom.Line2D.class,
    java.awt.geom.AffineTransform.class,
    java.awt.image.BufferedImage.class,
    java.util.List.class,
    java.util.Map.class,
    java.util.Set.class,
    java.util.Optional.class,
    java.util.Random.class,
    java.util.ArrayList.class,
    java.util.HashMap.class,
    java.util.HashSet.class,
    java.util.concurrent.ConcurrentHashMap.class,
    java.util.function.Consumer.class,
    java.util.function.Predicate.class,
    java.util.function.Function.class,
    java.util.function.Supplier.class
  );

  private static List<Class<?>> loadEngine(ClassLoader loader) {
    List<Class<?>> result = new ArrayList<>(STANDARD_JDK_TYPES);
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
      for (ClassLoader cl = loader; cl != null; cl = cl.getParent()) {
        if (cl instanceof java.net.URLClassLoader urlLoader) {
          for (java.net.URL url : urlLoader.getURLs()) {
            Path path = Path.of(url.toURI());
            if (Files.isDirectory(path)) result.addAll(fromDirectory(path, loader, ""));
            else if (path.toString().endsWith(".jar")) result.addAll(fromJar(path, loader, ""));
          }
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
    if (name == null || name.isBlank() || name.contains("$") || name.endsWith("module-info") || name.endsWith("package-info")) {
      return false;
    }
    if (name.startsWith("tools.")
        || name.startsWith("com.fasterxml.")
        || name.startsWith("com.google.")
        || name.startsWith("com.github.")
        || name.startsWith("com.formdev.")
        || name.startsWith("com.jgoodies.")
        || name.startsWith("com.sun.")
        || name.startsWith("org.apache.")
        || name.startsWith("org.codehaus.")
        || name.startsWith("org.cef.")
        || name.startsWith("org.eclipse.")
        || name.startsWith("org.junit.")
        || name.startsWith("org.hamcrest.")
        || name.startsWith("org.opentest4j.")
        || name.startsWith("org.apiguardian.")
        || name.startsWith("org.slf4j.")
        || name.startsWith("org.gradle.")
        || name.startsWith("org.joda.")
        || name.startsWith("org.jgrapht.")
        || name.startsWith("org.jetbrains.")
        || name.startsWith("org.intellij.")
        || name.startsWith("org.checkerframework.")
        || name.startsWith("net.java.")
        || name.startsWith("net.bytebuddy.")
        || name.startsWith("io.netty.")
        || name.startsWith("me.friwi.")
        || name.startsWith("org.glassfish.")
        || name.startsWith("org.w3c.")
        || name.startsWith("org.xml.")
        || name.startsWith("org.ietf.")
        || name.startsWith("org.omg.")
        || name.startsWith("com.oracle.")
        || name.startsWith("javax.xml.")
        || name.startsWith("javax.crypto.")
        || name.startsWith("javax.security.")
        || name.startsWith("javax.transaction.")
        || name.startsWith("javax.naming.")
        || name.startsWith("javax.management.")
        || name.startsWith("sun.")
        || name.startsWith("jdk.")
        || name.startsWith("java.awt.peer.")) {
      return false;
    }
    return true;

  }


  private static Comparator<Class<?>> typeOrder() {
    return Comparator.comparing((Class<?> type) -> type.getSimpleName(), String.CASE_INSENSITIVE_ORDER)
      .thenComparing(Class::getName);
  }
}
