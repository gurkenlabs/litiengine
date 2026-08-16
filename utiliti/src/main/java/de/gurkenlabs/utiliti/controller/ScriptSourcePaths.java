package de.gurkenlabs.utiliti.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

/** Conventional project-relative locations for runtime-compiled script sources. */
public final class ScriptSourcePaths {
  private static final String SCRIPT_ROOT = "scripts/";
  private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([a-zA-Z0-9_.]+)\\s*;");

  private ScriptSourcePaths() {}

  public static String create(String language, String className) {
    return create(null, language, className);
  }

  public static String create(ProjectModel model, String language, String className) {
    String normalizedLanguage = normalizeLanguage(language);
    String ext = extension(normalizedLanguage);

    if (model == null || model.projectRoot() == null) {
      return SCRIPT_ROOT + normalizedLanguage + "/" + className + ext;
    }

    Path projectRoot = model.projectRoot();
    Path chosenSourceRoot = selectSourceRoot(model);

    String scriptPackage = detectScriptPackage(model, chosenSourceRoot);

    if (chosenSourceRoot != null && scriptPackage != null && !scriptPackage.isBlank()) {
      String relSourceRoot = projectRoot.relativize(chosenSourceRoot).toString().replace('\\', '/');
      if (relSourceRoot.endsWith("/")) {
        relSourceRoot = relSourceRoot.substring(0, relSourceRoot.length() - 1);
      }
      String packageDir = scriptPackage.replace('.', '/');
      String prefix = relSourceRoot.isEmpty() ? "" : relSourceRoot + "/";
      return prefix + packageDir + "/" + className + ext;
    }

    if (Files.isDirectory(projectRoot.resolve("scripts"))) {
      return SCRIPT_ROOT + normalizedLanguage + "/" + className + ext;
    }

    if (chosenSourceRoot != null) {
      String relSourceRoot = projectRoot.relativize(chosenSourceRoot).toString().replace('\\', '/');
      String prefix = relSourceRoot.isEmpty() ? "" : relSourceRoot + "/";
      return prefix + className + ext;
    }

    return SCRIPT_ROOT + normalizedLanguage + "/" + className + ext;
  }

  private static Path selectSourceRoot(ProjectModel model) {
    if (model == null || model.sourceRoots() == null || model.sourceRoots().isEmpty()) {
      return null;
    }
    if (model.projectRoot() != null) {
      Path src = model.projectRoot().resolve("src");
      if (model.sourceRoots().contains(src) && isFlatSourceRoot(src)) {
        return src;
      }
    }
    for (Path root : model.sourceRoots()) {
      if (Files.isDirectory(root)) {
        return root;
      }
    }
    return model.sourceRoots().get(0);
  }

  static boolean isFlatSourceRoot(Path src) {
    if (!Files.isDirectory(src)) return false;
    try (var stream = Files.list(src)) {
      return stream.filter(Files::isDirectory)
          .map(p -> p.getFileName().toString())
          .anyMatch(name -> !"main".equals(name) && !"test".equals(name) && !"resources".equals(name) && !"target".equals(name) && !"build".equals(name));
    } catch (IOException ignored) {
      return false;
    }
  }

  public static String detectScriptPackage(ProjectModel model) {
    return detectScriptPackage(model, model != null ? selectSourceRoot(model) : null);
  }

  public static String detectScriptPackage(ProjectModel model, Path sourceRoot) {
    if (model == null) return null;

    if (model.mainClass() != null && !model.mainClass().isBlank()) {
      int lastDot = model.mainClass().lastIndexOf('.');
      if (lastDot > 0) {
        String base = model.mainClass().substring(0, lastDot);
        return base.endsWith(".scripts") ? base : base + ".scripts";
      }
    }

    if (sourceRoot != null && Files.isDirectory(sourceRoot)) {
      String detected = scanForPackage(sourceRoot);
      if (detected != null && !detected.isBlank()) {
        return detected.endsWith(".scripts") ? detected : detected + ".scripts";
      }
    }

    return null;
  }

  private static String scanForPackage(Path sourceRoot) {
    java.util.List<String> packages = new java.util.ArrayList<>();
    try (Stream<Path> stream = Files.walk(sourceRoot, 8)) {
      var sourceFiles = stream.filter(p -> {
        if (!Files.isRegularFile(p)) return false;
        String name = p.getFileName().toString().toLowerCase(Locale.ROOT);
        return name.endsWith(".java") || name.endsWith(".groovy") || name.endsWith(".kt");
      }).limit(25).toList();

      for (Path file : sourceFiles) {
        try {
          String content = Files.readString(file);
          Matcher matcher = PACKAGE_PATTERN.matcher(content);
          if (matcher.find()) {
            String pkg = matcher.group(1).trim();
            if (isValidPackage(pkg)) {
              packages.add(pkg);
            }
          }
        } catch (IOException ignored) {
        }
      }
    } catch (IOException ignored) {
    }

    if (packages.isEmpty()) return null;
    return commonPackagePrefix(packages);
  }

  static String commonPackagePrefix(java.util.List<String> packages) {
    if (packages == null || packages.isEmpty()) return null;
    String[] prefixTokens = packages.get(0).split("\\.");
    int matchingCount = prefixTokens.length;

    for (int i = 1; i < packages.size(); i++) {
      String[] tokens = packages.get(i).split("\\.");
      matchingCount = Math.min(matchingCount, tokens.length);
      for (int j = 0; j < matchingCount; j++) {
        if (!prefixTokens[j].equals(tokens[j])) {
          matchingCount = j;
          break;
        }
      }
    }

    if (matchingCount == 0) return null;
    StringBuilder sb = new StringBuilder();
    for (int i = 0; i < matchingCount; i++) {
      if (i > 0) sb.append('.');
      sb.append(prefixTokens[i]);
    }
    return sb.toString();
  }

  static boolean isValidPackage(String packageName) {
    if (packageName == null || packageName.isBlank()) return false;
    String[] parts = packageName.split("\\.");
    for (String part : parts) {
      if (!javax.lang.model.SourceVersion.isIdentifier(part) || javax.lang.model.SourceVersion.isKeyword(part)) {
        return false;
      }
    }
    return true;
  }

  public static String derivePackageName(ProjectModel model, String relativeSource) {
    if (model == null || model.projectRoot() == null || relativeSource == null || relativeSource.isBlank()) {
      return null;
    }
    Path file = model.projectRoot().resolve(relativeSource).normalize();
    for (Path sourceRoot : model.sourceRoots()) {
      Path normRoot = sourceRoot.normalize();
      if (file.startsWith(normRoot) && !file.getParent().equals(normRoot)) {
        Path rel = normRoot.relativize(file.getParent());
        String pkg = rel.toString().replace('\\', '/').replace('/', '.');
        if (isValidPackage(pkg)) {
          return pkg;
        }
      }
    }
    return null;
  }

  /** Keeps renamed legacy scripts in their current directory while changing the file name. */
  public static String rename(String currentSource, String language, String className) {
    if (currentSource == null || currentSource.isBlank()) {
      return create(language, className);
    }
    String normalized = currentSource.replace('\\', '/');
    int separator = normalized.lastIndexOf('/');
    String directory = separator < 0 ? "" : normalized.substring(0, separator + 1);
    String fileName = separator < 0 ? normalized : normalized.substring(separator + 1);
    int extension = fileName.lastIndexOf('.');
    String suffix = extension < 0 ? extension(normalizeLanguage(language)) : fileName.substring(extension);
    return directory + className + suffix;
  }

  /** Extracts simple class name from Java/Groovy source text. */
  public static String extractClassName(String source) {
    if (source == null || source.isBlank()) return null;
    var matcher = Pattern.compile("(?m)^\\s*(?:public\\s+)?(?:class|interface|enum|record)\\s+([A-Za-z_$][\\w$]*)").matcher(source);
    return matcher.find() ? matcher.group(1) : null;
  }

  /** Extracts fully-qualified class name (package + simple name) from source text. */
  public static String extractFullyQualifiedClassName(String source) {
    if (source == null || source.isBlank()) return null;
    String simpleName = extractClassName(source);
    if (simpleName == null) return null;
    Matcher pkgMatcher = PACKAGE_PATTERN.matcher(source);
    if (pkgMatcher.find()) {
      String pkg = pkgMatcher.group(1).trim();
      if (!pkg.isEmpty()) {
        return pkg + "." + simpleName;
      }
    }
    return simpleName;
  }

  /** Resolves a relative script source path against the project file path or root directory. */
  public static Path resolvePath(Path projectPath, String relative) {
    if (relative == null || relative.isBlank()) return null;
    if (projectPath == null) return Path.of(relative);
    Path root = Files.isDirectory(projectPath) ? projectPath : projectPath.toAbsolutePath().normalize().getParent();
    return root != null ? root.resolve(relative).normalize() : Path.of(relative);
  }

  /** Resolves standard scripts directory (src/main/java or scripts/) for a project. */
  public static Path resolveScriptsDirectory(Path projectPath) {
    if (projectPath == null) return null;
    Path root = Files.isDirectory(projectPath) ? projectPath : projectPath.toAbsolutePath().normalize().getParent();
    if (root == null) return null;
    Path standardSrc = root.resolve("src/main/java");
    if (Files.isDirectory(standardSrc)) return standardSrc;
    return root.resolve("scripts");
  }

  private static String normalizeLanguage(String language) {
    return language == null || language.isBlank() ? "java" : language.toLowerCase(Locale.ROOT);
  }

  private static String extension(String language) {
    return "." + ("groovy".equals(language) ? "groovy" : "java");
  }
}

