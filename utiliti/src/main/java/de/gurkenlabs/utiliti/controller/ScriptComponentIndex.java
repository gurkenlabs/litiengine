package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.entities.IEntityController;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.JarURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarFile;

/** Discovers entity-controller contracts and host-compatible implementations for script completion. */
public final class ScriptComponentIndex {
  private static final String ENGINE_PACKAGE = "de.gurkenlabs.litiengine";
  private static final List<Class<?>> ENGINE_CONTROLLERS = discoverEngineControllers();

  private final List<ComponentSymbol> components;

  private ScriptComponentIndex(List<ComponentSymbol> components) {
    this.components = List.copyOf(components);
  }

  public static ScriptComponentIndex create(ScriptDefinition definition) {
    ProjectCodeIntegration projectCode = Editor.instance().getProjectCodeIntegration();
    ClassLoader projectLoader = projectCode.getClassLoader();
    ClassLoader loader = projectLoader == null ? ScriptComponentIndex.class.getClassLoader() : projectLoader;
    Class<?> hostType = resolveHostType(definition, loader);
    Map<String, Class<?>> types = new LinkedHashMap<>();
    ENGINE_CONTROLLERS.forEach(type -> types.put(type.getName(), type));
    for (ProjectCodeIntegration.ControllerDefinition controller : projectCode.getControllerDefinitions()) {
      try {
        Class<?> type = Class.forName(controller.className(), false, loader);
        types.put(type.getName(), type);
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Stale or incomplete project output should not break engine completion.
      }
    }

    List<ComponentSymbol> symbols = types.values().stream()
      .map(type -> symbol(type, hostType))
      .filter(java.util.Objects::nonNull)
      .sorted(Comparator.comparing(ComponentSymbol::simpleName).thenComparing(ComponentSymbol::qualifiedName))
      .toList();
    return new ScriptComponentIndex(symbols);
  }

  public List<ComponentSymbol> components() {
    return this.components;
  }

  private static ComponentSymbol symbol(Class<?> type, Class<?> hostType) {
    boolean contract = type.isInterface() || Modifier.isAbstract(type.getModifiers());
    if (contract) {
      return new ComponentSymbol(type.getSimpleName(), type.getName(), true, contractName(type),
        assignableTypes(type), List.of());
    }
    if (!Modifier.isPublic(type.getModifiers())) return null;

    List<ConstructorSymbol> constructors = new ArrayList<>();
    for (Constructor<?> constructor : type.getConstructors()) {
      Class<?>[] parameters = constructor.getParameterTypes();
      if (parameters.length == 0 || parameters[0].isAssignableFrom(hostType)) {
        constructors.add(new ConstructorSymbol(signature(constructor), parameters.length == 0,
          parameters.length > 0 && parameters[0].isAssignableFrom(hostType), parameters.length));
      }
    }
    if (constructors.isEmpty()) return null;
    constructors.sort(Comparator.comparingInt(ConstructorSymbol::parameterCount));
    return new ComponentSymbol(type.getSimpleName(), type.getName(), false, contractName(type),
      assignableTypes(type), List.copyOf(constructors));
  }

  private static Class<?> resolveHostType(ScriptDefinition definition, ClassLoader loader) {
    String target = definition == null ? null : definition.getTargetType();
    if (target != null && !target.isBlank()) {
      try {
        Class<?> type = Class.forName(target, false, loader);
        if (IEntity.class.isAssignableFrom(type)) return type;
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Fall through to the broad entity contract while project output is unavailable.
      }
    }
    return IEntity.class;
  }

  private static String contractName(Class<?> type) {
    Class<?> current = type;
    while (current != null) {
      for (Class<?> contract : current.getInterfaces()) {
        Class<?> result = controllerContract(contract);
        if (result != null) return result.getSimpleName();
      }
      current = current.getSuperclass();
    }
    return IEntityController.class.getSimpleName();
  }

  private static Class<?> controllerContract(Class<?> type) {
    if (type != IEntityController.class && IEntityController.class.isAssignableFrom(type)) return type;
    for (Class<?> parent : type.getInterfaces()) {
      Class<?> result = controllerContract(parent);
      if (result != null) return result;
    }
    return null;
  }

  private static String signature(Constructor<?> constructor) {
    return constructor.getDeclaringClass().getSimpleName() + "(" + java.util.Arrays.stream(constructor.getParameterTypes())
      .map(Class::getSimpleName).collect(java.util.stream.Collectors.joining(", ")) + ")";
  }

  private static List<String> assignableTypes(Class<?> type) {
    java.util.LinkedHashSet<String> result = new java.util.LinkedHashSet<>();
    collectAssignableTypes(type, result);
    return List.copyOf(result);
  }

  private static void collectAssignableTypes(Class<?> type, Set<String> result) {
    if (type == null || !IEntityController.class.isAssignableFrom(type) || !result.add(type.getName())) return;
    for (Class<?> contract : type.getInterfaces()) collectAssignableTypes(contract, result);
    collectAssignableTypes(type.getSuperclass(), result);
  }

  private static List<Class<?>> discoverEngineControllers() {
    Map<String, Class<?>> result = new LinkedHashMap<>();
    result.put(IEntityController.class.getName(), IEntityController.class);
    for (String className : engineClassNames()) {
      try {
        Class<?> type = Class.forName(className, false, IEntityController.class.getClassLoader());
        if (IEntityController.class.isAssignableFrom(type) && !type.isSynthetic()) result.put(type.getName(), type);
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Optional engine integrations may not be available in every editor distribution.
      }
    }
    return List.copyOf(result.values());
  }

  private static List<String> engineClassNames() {
    String packagePath = ENGINE_PACKAGE.replace('.', '/');
    URL location = IEntityController.class.getResource("/" + packagePath + "/entities/IEntityController.class");
    if (location == null) return List.of();
    try {
      if ("jar".equals(location.getProtocol())) {
        JarURLConnection connection = (JarURLConnection) location.openConnection();
        try (JarFile jar = connection.getJarFile()) {
          return jar.stream().map(entry -> entry.getName())
            .filter(name -> name.startsWith(packagePath + "/") && name.endsWith(".class") && !name.contains("$"))
            .map(ScriptComponentIndex::className).toList();
        }
      }
      if ("file".equals(location.getProtocol())) {
        Path classFile = Path.of(new URI(location.toString()));
        Path root = classFile;
        for (int index = 0; index < packagePath.split("/").length + 2; index++) root = root.getParent();
        Path packageRoot = root.resolve(packagePath);
        try (var paths = Files.walk(packageRoot)) {
          Path finalRoot = root;
          return paths.filter(path -> path.toString().endsWith(".class"))
            .map(path -> finalRoot.relativize(path).toString().replace('\\', '/'))
            .filter(name -> !name.contains("$"))
            .map(ScriptComponentIndex::className).toList();
        }
      }
    } catch (IOException | URISyntaxException | RuntimeException ignored) {
      // A minimal IEntityController suggestion is still available when the class path cannot be scanned.
    }
    return List.of();
  }

  private static String className(String path) {
    return path.substring(0, path.length() - ".class".length()).replace('/', '.');
  }

  public record ComponentSymbol(String simpleName, String qualifiedName, boolean contract, String contractName,
                                List<String> assignableTypes, List<ConstructorSymbol> constructors) {
    public ConstructorSymbol preferredConstructor() {
      return this.constructors.isEmpty() ? null : this.constructors.getFirst();
    }

    public boolean isAssignableTo(String typeName) {
      if (typeName == null || typeName.isBlank()) return true;
      return this.assignableTypes.stream().anyMatch(type -> type.equals(typeName) || type.endsWith("." + typeName));
    }
  }

  public record ConstructorSymbol(String signature, boolean noArguments, boolean acceptsHost, int parameterCount) {
  }
}
