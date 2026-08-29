package de.gurkenlabs.utiliti.controller;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.abilities.Ability;
import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.entities.EntityQuery;
import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.scripting.ScriptContext;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.AbstractScript;
import de.gurkenlabs.litiengine.scripting.CreatureScript;
import de.gurkenlabs.litiengine.scripting.EntityScript;
import de.gurkenlabs.litiengine.scripting.EnvironmentScript;
import de.gurkenlabs.litiengine.scripting.GameScript;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds editor completion symbols directly from the loaded engine and project API. */
public final class ScriptApiIndex {
  private static final int MAX_INDEXED_TYPES = 240;
  private static final int MAX_CACHED_INDEXES = 8;
  private static final Map<CacheKey, ScriptApiIndex> CACHE = new LinkedHashMap<>(MAX_CACHED_INDEXES, 0.75f, true) {
    @Override
    protected boolean removeEldestEntry(Map.Entry<CacheKey, ScriptApiIndex> eldest) {
      return this.size() > MAX_CACHED_INDEXES;
    }
  };
  private final Map<String, TypeSymbol> types;
  private final List<TypeSymbol> sortedTypes;
  private final Map<String, String> nameLookupMap;

  private ScriptApiIndex(Map<String, TypeSymbol> types) {
    this.types = Map.copyOf(types);
    this.sortedTypes = this.types.values().stream()
      .sorted(java.util.Comparator.comparing(TypeSymbol::simpleName))
      .toList();
    Map<String, String> lookup = new HashMap<>();
    Map<String, Integer> counts = new HashMap<>();
    for (TypeSymbol symbol : this.types.values()) {
      lookup.put(symbol.qualifiedName(), symbol.qualifiedName());
      counts.put(symbol.simpleName(), counts.getOrDefault(symbol.simpleName(), 0) + 1);
    }
    for (TypeSymbol symbol : this.types.values()) {
      if (counts.get(symbol.simpleName()) == 1) {
        lookup.put(symbol.simpleName(), symbol.qualifiedName());
      }
    }
    this.nameLookupMap = Map.copyOf(lookup);
  }

  public static synchronized ScriptApiIndex create(ScriptDefinition definition) {
    ClassLoader loader = Editor.instance().getProjectCodeIntegration().getClassLoader();
    if (loader == null) loader = ScriptApiIndex.class.getClassLoader();
    String targetType = definition == null ? null : definition.getTargetType();
    CacheKey key = new CacheKey(targetType, loader);
    ScriptApiIndex cached = CACHE.get(key);
    if (cached != null) return cached;
    ScriptApiIndex created = build(definition, loader);
    CACHE.put(key, created);
    return created;
  }

  private static ScriptApiIndex build(ScriptDefinition definition, ClassLoader loader) {
    LinkedHashMap<String, TypeSymbol> symbols = new LinkedHashMap<>();
    ArrayDeque<Class<?>> queue = new ArrayDeque<>(List.of(
      Game.class, IEntity.class, Creature.class, Environment.class, ScriptContext.class, EntityQuery.class, Ability.class,
      AbstractScript.class, EntityScript.class, CreatureScript.class, EnvironmentScript.class, GameScript.class,
      String.class, Object.class, Class.class, List.class, java.util.Collection.class, java.util.Map.class,
      java.util.Optional.class, Stream.class));
    if (definition != null && definition.getTargetType() != null) {
      try {
        queue.add(Class.forName(definition.getTargetType(), false, loader));
      } catch (ClassNotFoundException | LinkageError ignored) {
        // The general entity API remains available when project output is stale.
      }
    }
    Editor.instance().getProjectCodeIntegration().getDefinitions().stream().limit(80).forEach(projectType -> {
      try {
        queue.add(Class.forName(projectType.className(), false, Editor.instance().getProjectCodeIntegration().getClassLoader()));
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Skip incomplete project types.
      }
    });
    ScriptComponentIndex.create(definition).components().forEach(component -> {
      try {
        queue.add(Class.forName(component.qualifiedName(), false, loader));
      } catch (ClassNotFoundException | LinkageError ignored) {
        // Component lookahead remains useful even if an optional implementation cannot be indexed deeply.
      }
    });

    Set<Class<?>> visited = new HashSet<>();
    SourceJavadocs javadocs = new SourceJavadocs();
    while (!queue.isEmpty() && visited.size() < MAX_INDEXED_TYPES) {
      Class<?> type = normalized(queue.removeFirst());
      if (type == null || !visited.add(type) || !isRelevant(type)) continue;
      List<MemberSymbol> members = new ArrayList<>();
      for (Method method : type.getMethods()) {
        if (!Modifier.isPublic(method.getModifiers()) || method.isSynthetic()) continue;
        List<ParameterSymbol> parameters = new ArrayList<>();
        Parameter[] reflectedParameters = method.getParameters();
        Type[] genericParameters = method.getGenericParameterTypes();
        for (int index = 0; index < reflectedParameters.length; index++) {
          parameters.add(new ParameterSymbol(displayType(genericParameters[index]), parameterName(reflectedParameters[index])));
        }
        String returnType = method.getName().equals("getClass") && method.getDeclaringClass() == Object.class
          ? "Class<? extends " + type.getSimpleName() + ">" : displayType(method.getGenericReturnType());
        members.add(new MemberSymbol(method.getName(), returnType, qualifiedName(method.getReturnType()),
          method.getGenericReturnType().getTypeName().replace('$', '.'),
          signature(method, parameters, returnType), javadocs.documentation(method.getDeclaringClass(), method.getName()),
          List.copyOf(parameters), true,
          Modifier.isStatic(method.getModifiers())));
        enqueue(queue, method.getReturnType());
        for (Class<?> parameterType : method.getParameterTypes()) enqueue(queue, parameterType);
      }
      for (Field field : type.getFields()) {
        if (!Modifier.isPublic(field.getModifiers()) || field.isSynthetic()) continue;
        members.add(new MemberSymbol(field.getName(), simpleName(field.getType()), qualifiedName(field.getType()),
          field.getGenericType().getTypeName().replace('$', '.'),
          field.getName() + " : " + simpleName(field.getType()),
          javadocs.documentation(field.getDeclaringClass(), field.getName()), List.of(), false,
          Modifier.isStatic(field.getModifiers())));
        enqueue(queue, field.getType());
      }
      symbols.put(type.getName(), new TypeSymbol(type.getSimpleName(), type.getName(),
        java.util.Arrays.stream(type.getTypeParameters()).map(variable -> variable.getName()).toList(),
        javadocs.documentation(type, type.getSimpleName()), List.copyOf(members)));
    }
    return new ScriptApiIndex(symbols);
  }

  public List<TypeSymbol> types() {
    return this.sortedTypes;
  }

  /** Resolves either a qualified or unambiguous simple type name from the indexed API. */
  public String resolveType(String name) {
    if (name == null || name.isBlank()) return null;
    String raw = name.strip();
    int generic = raw.indexOf('<');
    if (generic >= 0) raw = raw.substring(0, generic).strip();
    if (raw.endsWith("[]")) raw = raw.substring(0, raw.length() - 2).strip();
    return this.nameLookupMap.get(raw);
  }

  /** Returns the indexed public API for a type name. */
  public TypeSymbol type(String name) {
    String resolved = this.resolveType(name);
    return resolved == null ? null : this.types.get(resolved);
  }

  private static void enqueue(ArrayDeque<Class<?>> queue, Class<?> type) {
    Class<?> normalized = normalized(type);
    if (normalized != null && isRelevant(normalized)) queue.addLast(normalized);
  }

  private static Class<?> normalized(Class<?> type) {
    if (type == null || type == Void.TYPE || type.isPrimitive()) return null;
    while (type.isArray()) type = type.getComponentType();
    return type.isPrimitive() ? null : type;
  }

  private static boolean isRelevant(Class<?> type) {
    Package typePackage = type.getPackage();
    String name = typePackage == null ? "" : typePackage.getName();
    return name.startsWith("de.gurkenlabs.litiengine") || name.startsWith("de.gurkenlabs.utiliti")
      || name.startsWith("java.awt.geom") || name.startsWith("java.util") || name.startsWith("java.lang")
      || (!name.startsWith("java.") && !name.startsWith("javax.") && !name.startsWith("jakarta.")
        && !name.startsWith("org.") && !name.startsWith("com.sun."));
  }

  private static String parameterName(Parameter parameter) {
    return parameter.isNamePresent() ? parameter.getName() : "value";
  }

  private static String signature(Method method, List<ParameterSymbol> parameters, String returnType) {
    return method.getName() + "(" + parameters.stream()
      .map(parameter -> parameter.type() + " " + parameter.name())
      .collect(java.util.stream.Collectors.joining(", ")) + ") : " + returnType;
  }

  private static String simpleName(Class<?> type) {
    if (type == null) return "void";
    if (type.isArray()) return simpleName(type.getComponentType()) + "[]";
    return type.getSimpleName();
  }

  private static String qualifiedName(Class<?> type) {
    if (type == null || type == Void.TYPE) return "void";
    if (type.isArray()) return qualifiedName(type.getComponentType()) + "[]";
    return type.getName();
  }

  private static String displayType(Type type) {
    if (type == null) return "void";
    return type.getTypeName().replace('$', '.')
      .replaceAll("(?:[a-z_$][\\w$]*\\.)+([A-Z][\\w$]*)", "$1");
  }

  public record TypeSymbol(String simpleName, String qualifiedName, List<String> typeParameters,
                           String documentation, List<MemberSymbol> members) {}

  public record MemberSymbol(String name, String returnType, String qualifiedReturnType, String genericReturnType,
                             String signature, String documentation, List<ParameterSymbol> parameters,
                             boolean function, boolean staticMember) {}

  public record ParameterSymbol(String type, String name) {}

  private record CacheKey(String targetType, ClassLoader loader) {}

  /** Best-effort source Javadoc reader. Packaged builds can replace this with the generated API index. */
  private static final class SourceJavadocs {
    private static final Pattern JAVADOC = Pattern.compile("/\\*\\*(.*?)\\*/\\s*([^;{]+[;{])", Pattern.DOTALL);
    private final Map<Class<?>, Map<String, String>> cache = new HashMap<>();

    String documentation(Class<?> type, String member) {
      return this.cache.computeIfAbsent(type, this::read).getOrDefault(member, "");
    }

    private Map<String, String> read(Class<?> type) {
      Path source = locateSource(type);
      if (source == null) return Map.of();
      try {
        String content = Files.readString(source);
        Map<String, String> result = new HashMap<>();
        Matcher matcher = JAVADOC.matcher(content);
        while (matcher.find()) {
          String declaration = matcher.group(2);
          String documentation = clean(matcher.group(1));
          if (declaration.contains("class " + type.getSimpleName()) || declaration.contains("interface " + type.getSimpleName())) {
            result.put(type.getSimpleName(), documentation);
          }
          for (Method method : type.getDeclaredMethods()) {
            if (declaration.matches("(?s).*\\b" + Pattern.quote(method.getName()) + "\\s*\\(.*")) {
              result.putIfAbsent(method.getName(), documentation);
            }
          }
          for (Field field : type.getDeclaredFields()) {
            if (declaration.matches("(?s).*\\b" + Pattern.quote(field.getName()) + "\\b.*")) {
              result.putIfAbsent(field.getName(), documentation);
            }
          }
        }
        return result;
      } catch (IOException ignored) {
        return Map.of();
      }
    }

    private static Path locateSource(Class<?> type) {
      String relative = type.getName().replace('.', '/').replaceFirst("\\$.*$", "") + ".java";
      List<Path> roots = new ArrayList<>();
      if (Editor.instance().getProjectPath() != null) {
        Path project = Editor.instance().getProjectPath().getParent();
        roots.add(project.resolve("src/main/java"));
        roots.add(project.resolve("src/main/groovy"));
      }
      Path working = Path.of(System.getProperty("user.dir", ".")).toAbsolutePath();
      roots.add(working.resolve("litiengine/src/main/java"));
      roots.add(working.resolve("utiliti/src/main/java"));
      roots.add(working.resolve("src/main/java"));
      return roots.stream().map(root -> root.resolve(relative)).filter(Files::isRegularFile).findFirst().orElse(null);
    }

    private static String clean(String raw) {
      return raw.lines().map(line -> line.strip().replaceFirst("^\\*\\s?", ""))
        .filter(line -> !line.startsWith("@"))
        .collect(java.util.stream.Collectors.joining(" ")).replaceAll("\\s+", " ").trim();
    }
  }
}
