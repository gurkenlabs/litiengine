package de.gurkenlabs.utiliti.view.components;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Builds a stable editor outline from incomplete Java or Groovy script source. */
final class ScriptOutline {
  private static final Pattern CLASS = Pattern.compile(
    "^(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*(?:(?:public|protected|private|static|final|abstract|sealed|non-sealed)\\s+)*"
      + "(?:class|interface|enum|record|trait)\\s+([A-Za-z_$][\\w$]*)(?:\\s+(?:extends|implements)\\s+([\\w.$,<>?\\s]+))?");
  private static final Pattern METHOD = Pattern.compile(
    "^(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*(?:(?:public|protected|private|static|final|synchronized|abstract)\\s+)*"
      + "(def|void|[A-Za-z_$][\\w$<>.?\\[\\]]*)\\s+([A-Za-z_$][\\w$]*)\\s*\\(([^)]*)\\)");
  private static final Pattern CONSTRUCTOR = Pattern.compile(
    "^(?:public|protected|private)?\\s*([A-Za-z_$][\\w$]*)\\s*\\(([^)]*)\\)");
  private static final Pattern FIELD = Pattern.compile(
    "^(?:@[\\w.]+(?:\\([^)]*\\))?\\s*)*(?:(?:public|protected|private|static|final|transient|volatile)\\s+)*"
      + "(def|[A-Za-z_$][\\w$<>.?\\[\\]]*)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=|;|$)");
  private static final Pattern NEW_TYPE = Pattern.compile("\\bnew\\s+([A-Z][\\w$]*)\\b");
  private static final Set<String> BUILT_INS = Set.of(
    "String", "Object", "Boolean", "Byte", "Short", "Integer", "Long", "Float", "Double", "Character",
    "List", "Set", "Map", "Collection", "Optional", "Class", "Creature", "Entity", "IEntity", "Environment",
    "ScriptContext", "ScriptHostType", "ScriptInfo", "Override");

  private ScriptOutline() {}

  static Symbol parse(String source) {
    if (source == null || source.isBlank()) return null;
    String[] lines = source.split("\\R", -1);
    String className = null;
    String baseType = null;
    int classLine = -1;
    int depth = 0;
    int classDepth = -1;
    boolean blockComment = false;
    List<Symbol> innerClasses = new ArrayList<>();
    List<Symbol> fields = new ArrayList<>();
    List<Symbol> methods = new ArrayList<>();
    Map<String, Symbol> dependencies = new LinkedHashMap<>();

    for (int index = 0; index < lines.length; index++) {
      SanitizedLine sanitized = sanitize(lines[index], blockComment);
      blockComment = sanitized.blockComment();
      String code = sanitized.code().strip();

      if (className == null) {
        Matcher declaration = CLASS.matcher(code);
        if (declaration.find()) {
          className = declaration.group(1);
          baseType = simpleName(declaration.group(2));
          classLine = index;
          classDepth = depth + 1;
        }
      } else if (depth == classDepth && !code.isBlank()) {
        Matcher innerClass = CLASS.matcher(code);
        Matcher method = METHOD.matcher(code);
        Matcher constructor = CONSTRUCTOR.matcher(code);
        Matcher field = FIELD.matcher(code);

        if (innerClass.find() && !innerClass.group(1).equals(className)) {
          String type = simpleName(innerClass.group(2));
          innerClasses.add(new Symbol(Kind.CLASS, innerClass.group(1), type == null ? "" : type, index, List.of()));
          if (type != null) addDependency(dependencies, type, index, className, baseType);
        } else if (method.find()) {
          String returnType = "def".equals(method.group(1)) ? "Object" : simpleName(method.group(1));
          methods.add(new Symbol(Kind.METHOD, method.group(2), formatParameters(method.group(3)) + " : " + returnType,
            index, List.of()));
          addDependency(dependencies, returnType, index, className, baseType);
          addParameterDependencies(dependencies, method.group(3), index, className, baseType);
        } else if (constructor.find() && constructor.group(1).equals(className)) {
          methods.add(new Symbol(Kind.METHOD, className, formatParameters(constructor.group(2)), index, List.of()));
          addParameterDependencies(dependencies, constructor.group(2), index, className, baseType);
        } else if (field.find()) {
          String type = "def".equals(field.group(1)) ? "Object" : simpleName(field.group(1));
          fields.add(new Symbol(Kind.FIELD, field.group(2), type, index, List.of()));
          addDependency(dependencies, type, index, className, baseType);
        }
      }

      if (className != null) {
        Matcher createdType = NEW_TYPE.matcher(code);
        while (createdType.find()) addDependency(dependencies, createdType.group(1), index, className, baseType);
      }
      depth += braceDelta(sanitized.code());
    }

    if (className == null) return null;
    List<Symbol> groups = new ArrayList<>();
    if (!innerClasses.isEmpty()) groups.add(new Symbol(Kind.GROUP, "Classes", Integer.toString(innerClasses.size()), -1, List.copyOf(innerClasses)));
    if (!fields.isEmpty()) groups.add(new Symbol(Kind.GROUP, "Fields", Integer.toString(fields.size()), -1, List.copyOf(fields)));
    if (!methods.isEmpty()) groups.add(new Symbol(Kind.GROUP, "Methods", Integer.toString(methods.size()), -1, List.copyOf(methods)));
    if (!dependencies.isEmpty()) groups.add(new Symbol(Kind.GROUP, "Dependencies", Integer.toString(dependencies.size()), -1,
      List.copyOf(dependencies.values())));
    return new Symbol(Kind.CLASS, className, baseType == null ? "" : baseType, classLine, List.copyOf(groups));
  }

  private static void addParameterDependencies(Map<String, Symbol> dependencies, String parameters, int line,
                                               String className, String baseType) {
    if (parameters == null || parameters.isBlank()) return;
    for (String parameter : parameters.split(",")) {
      String[] tokens = parameter.strip().split("\\s+");
      if (tokens.length > 1) addDependency(dependencies, simpleName(tokens[0]), line, className, baseType);
    }
  }

  private static void addDependency(Map<String, Symbol> dependencies, String type, int line,
                                    String className, String baseType) {
    String simple = simpleName(type);
    if (simple == null || simple.isBlank() || simple.equals(className) || simple.equals(baseType)
      || BUILT_INS.contains(simple) || Character.isLowerCase(simple.charAt(0))) return;
    dependencies.putIfAbsent(simple, new Symbol(Kind.DEPENDENCY, simple, "", line, List.of()));
  }

  private static String formatParameters(String parameters) {
    if (parameters == null || parameters.isBlank()) return "()";
    List<String> formatted = new ArrayList<>();
    for (String parameter : parameters.split(",")) {
      String value = parameter.strip().replaceAll("\\s*=.*$", "");
      String[] tokens = value.split("\\s+");
      formatted.add(tokens.length > 1 ? simpleName(tokens[0]) + " " + tokens[tokens.length - 1] : value);
    }
    return "(" + String.join(", ", formatted) + ")";
  }

  private static String simpleName(String type) {
    if (type == null) return null;
    String normalized = type.strip().replaceAll("<.*>", "").replace("[]", "");
    int separator = normalized.lastIndexOf('.');
    return separator < 0 ? normalized : normalized.substring(separator + 1);
  }

  private static int braceDelta(String line) {
    int delta = 0;
    boolean string = false;
    char quote = 0;
    for (int index = 0; index < line.length(); index++) {
      char value = line.charAt(index);
      if (string) {
        if (value == quote && (index == 0 || line.charAt(index - 1) != '\\')) string = false;
      } else if (value == '\'' || value == '"') {
        string = true;
        quote = value;
      } else if (value == '{') delta++;
      else if (value == '}') delta--;
    }
    return delta;
  }

  private static SanitizedLine sanitize(String line, boolean inBlockComment) {
    StringBuilder result = new StringBuilder();
    boolean block = inBlockComment;
    boolean string = false;
    char quote = 0;
    for (int index = 0; index < line.length(); index++) {
      char value = line.charAt(index);
      char next = index + 1 < line.length() ? line.charAt(index + 1) : 0;
      if (block) {
        if (value == '*' && next == '/') {
          block = false;
          index++;
        }
      } else if (string) {
        result.append(' ');
        if (value == quote && (index == 0 || line.charAt(index - 1) != '\\')) string = false;
      } else if (value == '/' && next == '*') {
        block = true;
        index++;
      } else if (value == '/' && next == '/') {
        break;
      } else {
        result.append(value);
        if (value == '\'' || value == '"') {
          string = true;
          quote = value;
        }
      }
    }
    return new SanitizedLine(result.toString(), block);
  }

  enum Kind { CLASS, GROUP, FIELD, METHOD, DEPENDENCY }

  record Symbol(Kind kind, String name, String detail, int line, List<Symbol> children) {}

  private record SanitizedLine(String code, boolean blockComment) {}
}
