package de.gurkenlabs.litiengine.scripting.groovy;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.scripting.ScriptContext;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import groovy.lang.GroovyClassLoader;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.codehaus.groovy.ast.ClassNode;
import org.codehaus.groovy.ast.FieldNode;
import org.codehaus.groovy.ast.MethodNode;
import org.codehaus.groovy.ast.ModuleNode;
import org.codehaus.groovy.control.CompilationUnit;
import org.codehaus.groovy.control.CompilerConfiguration;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.Phases;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;

/** Groovy compiler-backed semantic tooling. Source is parsed and type-checked but never executed. */
final class GroovyLanguageService implements ScriptLanguageService {
  private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+([\\w.$]+)(?:\\s+as\\s+(\\w+))?");
  private static final Pattern EXPLICIT_VARIABLE = Pattern.compile(
    "(?m)(?:^|[;{}])\\s*(?:final\\s+)?([\\w.$]+(?:<[^;=]+>)?)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=|;)");
  private static final Pattern INFERRED_VARIABLE = Pattern.compile(
    "(?m)(?:^|[;{}])\\s*(?:def|var)\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*([^;\\n]+)");
  private static final Set<String> KEYWORDS = Set.of(
    "class", "extends", "implements", "import", "package", "public", "protected", "private", "static",
    "final", "void", "return", "if", "else", "for", "while", "switch", "case", "new", "true", "false",
    "null", "def", "var", "this", "super", "try", "catch", "finally", "throw");

  private final Workspace workspace;
  private final GroovyClassLoader classLoader;
  private volatile ParsedDocument lastValid;

  GroovyLanguageService(Workspace workspace) {
    this.workspace = Objects.requireNonNull(workspace);
    this.classLoader = new GroovyClassLoader(workspace.classLoader());
  }

  @Override
  public void close() {
    try {
      this.classLoader.close();
    } catch (IOException ignored) {
      // Semantic analysis never creates live instances; failed cleanup is non-fatal.
    }
  }

  @Override
  public Analysis analyze(Document document) {
    ParsedDocument parsed = this.parse(document);
    if (parsed.valid()) this.lastValid = parsed;
    return new Analysis(parsed.diagnostics(), parsed.symbols(), List.of());
  }

  @Override
  public List<Completion> complete(Document document, Position position) {
    int offset = offset(document.text(), position);
    String prefix = document.text().substring(0, offset);
    String source = document.text();
    String receiver = receiverExpression(prefix);
    if (receiver == null) {
      int lastDot = prefix.lastIndexOf('.');
      if (lastDot >= 0) {
        String beforeDot = prefix.substring(0, lastDot + 1);
        receiver = receiverExpression(beforeDot);
      }
    }
    Map<String, Class<?>> variables = this.variables(document, prefix);
    ResolvedType type = receiver == null ? null : this.resolveExpression(receiver, document.definition(), variables, source);
    Set<String> importedFqns = this.importedTypes(source).values().stream().map(Class::getName).collect(java.util.stream.Collectors.toSet());
    int importInsertLine = importInsertLine(source);
    List<Completion> result = new ArrayList<>();
    if (type != null) {
      addMembers(result, type);
    } else {
      addScriptDeclaredMembers(result, source);
      variables.forEach((name, typeClass) -> {
        result.add(new Completion(name, CompletionKind.VARIABLE, simpleName(typeClass.getName()),
          "Local variable or binding `" + name + "` (" + typeClass.getName() + ")", name, typeClass.getName(), List.of(), List.of()));
      });
      this.hostType(document.definition()).ifPresent(host -> addMembers(result, new ResolvedType(host, null, false)));

      KEYWORDS.stream().sorted().forEach(keyword -> result.add(new Completion(
        keyword, CompletionKind.KEYWORD, "Groovy keyword", "", keyword, null, List.of(), List.of())));
      addScriptScope(result, document.definition());
      this.importedTypes(source).values().stream().distinct().sorted(Comparator.comparing(Class::getSimpleName))
        .forEach(imported -> result.add(typeCompletion(imported)));

      boolean constructorContext = prefix.matches("(?s).*\\bnew(?:\\s+[\\w.$]*)?$");
      String currentWord = wordAt(source, offset);
      boolean isUpper = !currentWord.isEmpty() && Character.isUpperCase(currentWord.charAt(0));

      if (constructorContext || isUpper || result.size() < 15) {
        for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
          List<TextEdit> edits = importedFqns.contains(engineType.getName()) ? List.of()
            : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
              "import " + engineType.getName() + "\n"));
          result.add(typeCompletion(engineType, constructorContext, edits));
        }
        for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
          if (importedFqns.contains(projectType.getName())) continue;
          List<TextEdit> edits = List.of(new TextEdit(
            new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + projectType.getName() + "\n"));
          result.add(typeCompletion(projectType, constructorContext, edits));
        }
      }
    }
    return result.stream().collect(java.util.stream.Collectors.toMap(
      completion -> completion.kind() + ":" + completion.label() + ":" + completion.detail(), completion -> completion,
      (left, right) -> left, LinkedHashMap::new)).values().stream()
      .sorted(Comparator.comparing(GroovyLanguageService::memberRank)
        .thenComparing(Comparator.comparing(Completion::label, String.CASE_INSENSITIVE_ORDER)))
      .toList();
  }

  @Override
  public Optional<Hover> hover(Document document, Position position) {
    String text = document.text();
    int off = offset(text, position);
    String word = wordAt(text, off);
    if (word.isBlank()) return Optional.empty();

    Map<String, Class<?>> variables = this.variables(document, text);

    Class<?> type = variables.get(word);
    if (type != null) return Optional.of(new Hover("**" + simpleName(type.getName()) + "**  `" + type.getName() + "`", null));

    Optional<Class<?>> resolved = this.resolveType(word, text);
    if (resolved.isPresent()) {
      Class<?> cls = resolved.get();
      String pkg = cls.getPackage() == null ? "" : cls.getPackage().getName();
      String docs = "**" + cls.getSimpleName() + "**";
      if (!pkg.isEmpty()) docs += "\n\n`" + pkg + "`";
      return Optional.of(new Hover(docs, null));
    }

    String prefix = text.substring(0, off);
    String receiver = receiverExpression(prefix);
    if (receiver != null) {
      ResolvedType receiverType = this.resolveExpression(receiver, document.definition(), variables, text);
      if (receiverType != null) {
        Method method = Arrays.stream(receiverType.type().getMethods())
          .filter(m -> m.getName().equals(word)).findFirst().orElse(null);
        if (method != null) {
          String javadoc = extractJavadoc(method.getDeclaringClass(), method.getName(), method.getParameterCount());
          String sig = simpleName(method.getGenericReturnType().getTypeName()) + " " + word + "("
            + Arrays.stream(method.getParameters())
              .map(p -> simpleName(p.getParameterizedType().getTypeName()) + " " + p.getName())
              .reduce((a, b) -> a + ", " + b).orElse("") + ")";
          String doc;
          if (javadoc != null && !javadoc.isBlank()) {
            doc = javadoc + "\n\n---\n\n`" + sig + "`\n\nDeclared in `" + method.getDeclaringClass().getName() + "`";
          } else {
            doc = "```groovy\n" + sig + "\n```\n\nDeclared in `" + method.getDeclaringClass().getName() + "`";
          }
          return Optional.of(new Hover(doc, null));
        }
        try {
          Field field = receiverType.type().getField(word);
          String fieldJavadoc = extractJavadoc(field.getDeclaringClass(), field.getName(), -1);
          String sig = simpleName(field.getGenericType().getTypeName()) + " " + word;
          String doc;
          if (fieldJavadoc != null && !fieldJavadoc.isBlank()) {
            doc = fieldJavadoc + "\n\n---\n\n`" + sig + "`\n\nDeclared in `" + field.getDeclaringClass().getName() + "`";
          } else {
            doc = "```groovy\n" + sig + "\n```\n\nDeclared in `" + field.getDeclaringClass().getName() + "`";
          }
          return Optional.of(new Hover(doc, null));
        } catch (NoSuchFieldException ignored) {
        }
      }
    }

    Pattern fieldPattern = Pattern.compile(
      "(?m)^\\s*(?:public|protected|private|static|final|transient|volatile|def|var|\\s)*\\s+(\\S+)\\s+" + Pattern.quote(word) + "\\s*[=;]");
    Matcher fieldMatch = fieldPattern.matcher(text);
    if (fieldMatch.find()) {
      return Optional.of(new Hover("```groovy\n" + fieldMatch.group(1) + " " + word + "\n```\n\nField declared in this script", null));
    }

    Pattern methodPattern = Pattern.compile(
      "(?m)^\\s*(?:public|protected|private|static|final|abstract|synchronized|native|def|var|void|\\s)*\\s+(\\S+)\\s+" + Pattern.quote(word) + "\\s*\\(");
    Matcher methodMatch = methodPattern.matcher(text);
    if (methodMatch.find()) {
      return Optional.of(new Hover("```groovy\n" + methodMatch.group(1) + " " + word + "()\n```\n\nMethod declared in this script", null));
    }

    return Optional.empty();
  }

  @Override
  public Optional<SignatureHelp> signatureHelp(Document document, Position position) {
    int offset = offset(document.text(), position);
    String prefix = document.text().substring(0, offset);
    int open = prefix.lastIndexOf('(');
    if (open < 0) return Optional.empty();
    Matcher method = Pattern.compile("([A-Za-z_$][\\w$]*)\\s*$").matcher(prefix.substring(0, open));
    if (!method.find()) return Optional.empty();
    String name = method.group(1);
    String receiver = receiverExpression(prefix.substring(0, method.start()));
    ResolvedType type = receiver == null ? this.scriptType(document.definition())
      : this.resolveExpression(receiver, document.definition(), this.variables(document, prefix), document.text());
    if (type == null) return Optional.empty();
    List<Signature> signatures = Arrays.stream(type.type().getMethods()).filter(candidate -> candidate.getName().equals(name))
      .map(GroovyLanguageService::signature).toList();
    if (signatures.isEmpty()) return Optional.empty();
    int activeParameter = (int) prefix.substring(open + 1).chars().filter(character -> character == ',').count();
    return Optional.of(new SignatureHelp(signatures, 0, activeParameter));
  }

  @Override
  public Optional<Location> definition(Document document, Position position) {
    String text = document.text();
    int off = offset(text, position);
    String word = wordAt(text, off);
    if (word.isBlank()) return Optional.empty();

    // Check if the word is on an import line — jump to the fully-qualified class source.
    Matcher importLine = IMPORT.matcher(text);
    while (importLine.find()) {
      String alias = importLine.group(2) == null ? simpleName(importLine.group(1)) : importLine.group(2);
      if (alias.equals(word)) {
        String fqn = importLine.group(1);
        return Optional.of(new Location(classUri(fqn), new Range(new Position(0, 0), new Position(0, 0))));
      }
    }

    // Resolve as a type in the usual lookup order.
    Map<String, Class<?>> variables = this.variables(document, text);
    Optional<Class<?>> type = this.resolveType(word, text);
    if (type.isPresent()) return Optional.of(new Location(classUri(type.get().getName()), new Range(new Position(0, 0), new Position(0, 0))));

    // Check script-scope variables — jump to the declaration line.
    if (variables.containsKey(word)) {
      int declLine = findDeclarationLine(text, word);
      return Optional.of(new Location(document.uri(), new Range(new Position(declLine, 0), new Position(declLine, 0))));
    }

    // Search for script-owned field or method declarations.
    Optional<Location> scriptMember = findScriptMember(text, word, document.uri());
    if (scriptMember.isPresent()) return scriptMember;

    // Resolve as a member on a receiver expression (e.g. entity.getX → entity's type).
    String prefix = text.substring(0, off);
    String receiver = receiverExpression(prefix);
    if (receiver != null) {
      ResolvedType receiverType = this.resolveExpression(receiver, document.definition(), variables, text);
      if (receiverType != null) {
        Method method = Arrays.stream(receiverType.type().getMethods())
          .filter(m -> m.getName().equals(word)).findFirst().orElse(null);
        if (method != null) return Optional.of(new Location(classUri(method.getDeclaringClass().getName()),
          new Range(new Position(0, 0), new Position(0, 0))));
        try {
          Field field = receiverType.type().getField(word);
          return Optional.of(new Location(classUri(field.getDeclaringClass().getName()),
            new Range(new Position(0, 0), new Position(0, 0))));
        } catch (NoSuchFieldException ignored) {
        }
      }
    }

    return Optional.empty();
  }

  @Override
  public List<CodeAction> codeActions(Document document, Range range, List<ScriptDiagnostic> diagnostics) {
    if (document.text().contains("@CompileStatic")) return List.of();
    Position start = new Position(0, 0);
    return List.of(new CodeAction("Enable static type checking", "source", List.of(
      new TextEdit(new Range(start, start), "import groovy.transform.CompileStatic\n"),
      new TextEdit(new Range(classLine(document.text()), new Position(classLine(document.text()).line(), 0)), "@CompileStatic\n"))));
  }

  private ParsedDocument parse(Document document) {
    CompilerConfiguration configuration = new CompilerConfiguration();
    configuration.setScriptBaseClass(Object.class.getName());
    CompilationUnit unit = new CompilationUnit(configuration, null, this.classLoader);
    unit.addSource(document.uri() == null ? "Script.groovy" : document.uri().toString(), document.text());
    List<ScriptDiagnostic> diagnostics = new ArrayList<>();
    try {
      unit.compile(Phases.SEMANTIC_ANALYSIS);
      List<Symbol> symbols = new ArrayList<>();
      for (ModuleNode module : unit.getAST().getModules()) {
        for (ClassNode type : module.getClasses()) symbols.add(symbol(type));
      }
      return new ParsedDocument(document.uri(), true, List.copyOf(diagnostics), List.copyOf(symbols));
    } catch (MultipleCompilationErrorsException exception) {
      for (Message message : exception.getErrorCollector().getErrors()) {
        if (message instanceof SyntaxErrorMessage syntaxMessage) {
          var syntax = syntaxMessage.getCause();
          diagnostics.add(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR,
            document.definition() == null ? null : document.definition().getId(),
            document.uri() == null ? null : document.uri().toString(), syntax.getStartLine(), syntax.getStartColumn(),
            syntax.getOriginalMessage()));
        }
      }
      return new ParsedDocument(document.uri(), false, List.copyOf(diagnostics),
        this.lastValid == null ? List.of() : this.lastValid.symbols());
    }
  }

  private Map<String, Class<?>> variables(Document document, String source) {
    Map<String, Class<?>> variables = new LinkedHashMap<>(this.importedTypes(source));
    ScriptDefinition definition = document.definition();
    this.hostType(definition).ifPresent(type -> variables.put("host", type));
    variables.put("context", ScriptContext.class);
    variables.put("environment", Environment.class);
    Matcher explicit = EXPLICIT_VARIABLE.matcher(source);
    while (explicit.find()) this.resolveType(rawType(explicit.group(1)), source).ifPresent(type -> variables.put(explicit.group(2), type));
    Matcher inferred = INFERRED_VARIABLE.matcher(source);
    while (inferred.find()) {
      ResolvedType resolved = this.resolveExpression(inferred.group(2).strip(), definition, variables, source);
      if (resolved != null) variables.put(inferred.group(1), resolved.type());
    }
    Pattern varDecl = Pattern.compile("(?m)(?:^|[;{}])\\s*var\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*([^;\\n]+)");
    Matcher varMatcher = varDecl.matcher(source);
    while (varMatcher.find()) {
      if (variables.containsKey(varMatcher.group(1))) continue;
      ResolvedType resolved = this.resolveExpression(varMatcher.group(2).strip(), definition, variables, source);
      if (resolved != null) variables.put(varMatcher.group(1), resolved.type());
    }
    return variables;
  }

  private ResolvedType resolveExpression(String expression, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    List<String> chain = splitChain(expression);
    if (chain.isEmpty()) return null;
    String root = chain.getFirst();
    ResolvedType current = resolveRoot(root, definition, variables, source);
    if (current == null) return null;
    for (int index = 1; index < chain.size(); index++) {
      current = resolveMember(current, chain.get(index), source, this.workspace.classLoader());
      if (current == null) return null;
    }
    return current;
  }

  private ResolvedType resolveRoot(String root, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    if (root.matches("host\\s*\\(\\s*\\)")) {
      Optional<Class<?>> host = this.hostType(definition);
      if (host.isPresent()) return new ResolvedType(host.get(), null, false);
      if (variables.containsKey("host")) return new ResolvedType(variables.get("host"), null, false);
      return null;
    }
    if (root.matches("context\\s*\\(\\s*\\)")) return new ResolvedType(ScriptContext.class, null, false);
    if (root.matches("environment\\s*\\(\\s*\\)")) return new ResolvedType(Environment.class, null, false);
    if (root.equals("Game")) return new ResolvedType(Game.class, null, true);
    if (root.startsWith("new ")) return this.resolveType(root.substring(4).replaceFirst("\\(.*", ""), source)
      .map(type -> new ResolvedType(type, null, false)).orElse(null);
    if (variables.containsKey(root)) return new ResolvedType(variables.get(root), null, false);
    if (root.endsWith("()")) {
      String methodName = root.replaceFirst("\\(.*", "").strip();
      if (variables.containsKey(methodName)) return new ResolvedType(variables.get(methodName), null, false);
      return this.resolveImplicitMethodCall(root, definition, variables);
    }
    return this.resolveType(root, source).map(type -> new ResolvedType(type, null, true)).orElse(null);
  }

  private ResolvedType resolveImplicitMethodCall(String call, ScriptDefinition definition, Map<String, Class<?>> variables) {
    String methodName = call.replaceFirst("\\(.*", "").strip();
    ResolvedType scriptType = this.scriptType(definition);
    if (scriptType == null) return null;
    Method method = Arrays.stream(scriptType.type().getMethods())
      .filter(m -> m.getName().equals(methodName)).findFirst().orElse(null);
    if (method == null) return null;
    return new ResolvedType(method.getReturnType(), method.getGenericReturnType(), false);
  }

  private ResolvedType resolveMember(ResolvedType receiver, String segment, String source, ClassLoader loader) {
    String name = segment.replaceFirst("\\(.*", "").strip();
    boolean call = segment.contains("(");
    if (!call) {
      try {
        Field field = receiver.type().getField(name);
        return new ResolvedType(field.getType(), field.getGenericType(), false);
      } catch (NoSuchFieldException ignored) {
        String capitalized = name.isEmpty() ? "" : Character.toUpperCase(name.charAt(0)) + name.substring(1);
        for (String prefix : List.of("get", "is", "has")) {
          String getterName = prefix + capitalized;
          Method getter = Arrays.stream(receiver.type().getMethods())
            .filter(m -> m.getName().equals(getterName) && m.getParameterCount() == 0 && m.getReturnType() != void.class)
            .filter(m -> !receiver.staticOnly() || Modifier.isStatic(m.getModifiers()))
            .findFirst().orElse(null);
          if (getter != null) {
            return new ResolvedType(getter.getReturnType(), getter.getGenericReturnType(), false);
          }
        }
        return null;
      }
    }
    Method method = Arrays.stream(receiver.type().getMethods())
      .filter(candidate -> candidate.getName().equals(name))
      .filter(candidate -> !receiver.staticOnly() || Modifier.isStatic(candidate.getModifiers()))
      .sorted(Comparator.comparingInt(Method::getParameterCount)).findFirst().orElse(null);
    if (method == null) return null;
    Type generic = method.getGenericReturnType();
    Class<?> result = method.getReturnType();
    if (segment.matches(".*[\\w.$]+\\.class.*")) {
      Matcher literal = Pattern.compile("([\\w.$]+)\\.class").matcher(segment);
      if (literal.find()) {
        Optional<Class<?>> resolvedArg = resolveType(literal.group(1), source);
        if (resolvedArg.isPresent()) {
          result = resolvedArg.get();
          generic = result;
        } else {
          try {
            result = Class.forName(literal.group(1), false, loader);
            generic = result;
          } catch (ClassNotFoundException ignored) {
          }
        }
      }
    }
    return new ResolvedType(result, generic, false);
  }

  private Optional<Class<?>> hostType(ScriptDefinition definition) {
    if (definition == null || definition.getTargetType() == null || definition.getTargetType().isBlank()) return Optional.empty();
    try {
      return Optional.of(Class.forName(definition.getTargetType(), false, this.workspace.classLoader()));
    } catch (ClassNotFoundException | LinkageError ignored) {
      return this.resolveType(definition.getTargetType(), "");
    }
  }

  private ResolvedType scriptType(ScriptDefinition definition) {
    return this.hostType(definition).map(type -> new ResolvedType(type, null, false)).orElse(null);
  }

  private Map<String, Class<?>> importedTypes(String source) {
    Map<String, Class<?>> imports = new LinkedHashMap<>();
    Matcher matcher = IMPORT.matcher(source);
    ClassLoader[] loaders = {
      this.workspace.classLoader(),
      GroovyLanguageService.class.getClassLoader(),
      Thread.currentThread().getContextClassLoader()
    };
    while (matcher.find()) {
      String fqn = matcher.group(1);
      String alias = matcher.group(2);
      for (ClassLoader loader : loaders) {
        if (loader == null) continue;
        try {
          Class<?> type = Class.forName(fqn, false, loader);
          imports.put(alias == null ? type.getSimpleName() : alias, type);
          break;
        } catch (ClassNotFoundException | LinkageError ignored) {
        }
      }
    }
    return imports;
  }

  private Optional<Class<?>> resolveType(String name, String source) {
    String raw = rawType(name);
    Class<?> imported = this.importedTypes(source).get(raw);
    if (imported != null) return Optional.of(imported);
    List<String> candidates = List.of(
      raw,
      "java.lang." + raw,
      "java.util." + raw,
      "java.awt." + raw,
      "java.awt.geom." + raw,
      "de.gurkenlabs.litiengine." + raw,
      "de.gurkenlabs.litiengine.entities." + raw,
      "de.gurkenlabs.litiengine.entities.behavior." + raw,
      "de.gurkenlabs.litiengine.abilities." + raw,
      "de.gurkenlabs.litiengine.attributes." + raw,
      "de.gurkenlabs.litiengine.environment." + raw,
      "de.gurkenlabs.litiengine.graphics." + raw,
      "de.gurkenlabs.litiengine.physics." + raw,
      "de.gurkenlabs.litiengine.scripting." + raw,
      "de.gurkenlabs.litiengine.sound." + raw,
      "de.gurkenlabs.litiengine.tween." + raw,
      "de.gurkenlabs.litiengine.input." + raw,
      "de.gurkenlabs.litiengine.resources." + raw
    );
    ClassLoader[] loaders = {
      this.workspace.classLoader(),
      GroovyLanguageService.class.getClassLoader(),
      Thread.currentThread().getContextClassLoader()
    };
    for (String candidate : candidates) {
      for (ClassLoader loader : loaders) {
        if (loader == null) continue;
        try {
          return Optional.of(Class.forName(candidate, false, loader));
        } catch (ClassNotFoundException | LinkageError ignored) {
        }
      }
    }
    for (Class<?> pType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
      if (pType.getSimpleName().equals(raw) || pType.getName().equals(raw)) {
        return Optional.of(pType);
      }
    }
    return Optional.empty();
  }

  private static void addScriptScope(List<Completion> result, ScriptDefinition definition) {
    String host = definition == null || definition.getTargetType() == null ? "Object" : simpleName(definition.getTargetType());
    result.add(function("host", host, "The typed object controlled by this script."));
    result.add(function("context", ScriptContext.class.getSimpleName(), "The current script attachment context."));
    result.add(function("environment", Environment.class.getSimpleName(), "The host's current environment."));
    result.add(new Completion("Game", CompletionKind.CLASS, "de.gurkenlabs.litiengine.Game",
      "The central LITIENGINE static entry point for game systems.", "Game", "Game", List.of(), List.of()));
  }

  private static void addMembers(List<Completion> result, ResolvedType receiver) {
    String owner = simpleName(receiver.type().getName());
    for (Method method : receiver.type().getMethods()) {
      if (receiver.staticOnly() && !Modifier.isStatic(method.getModifiers())) continue;
      List<Parameter> parameters = Arrays.stream(method.getParameters())
        .map(parameter -> new Parameter(parameter.getName(), parameter.getParameterizedType().getTypeName())).toList();
      String detail = method.getName() + "(" + String.join(", ", parameters.stream()
        .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
      String javadoc = extractJavadoc(method.getDeclaringClass(), method.getName(), method.getParameterCount());
      String docs;
      if (javadoc != null) {
        docs = javadoc + "\n\n---\n\n`" + simpleName(method.getGenericReturnType().getTypeName()) + " "
          + owner + "." + detail + "`\n\nDeclared in `" + method.getDeclaringClass().getName() + "`";
      } else {
        docs = "```groovy\n" + simpleName(method.getGenericReturnType().getTypeName()) + " " + owner + "." + detail + "\n```\n\n"
          + "Declared in `" + method.getDeclaringClass().getName() + "`";
      }
      result.add(new Completion(method.getName(), CompletionKind.METHOD, detail, docs, method.getName() + "()",
        method.getGenericReturnType().getTypeName(), parameters, List.of()));

      if (method.getParameterCount() == 0 && method.getReturnType() != void.class) {
        String mName = method.getName();
        String propName = null;
        if (mName.startsWith("get") && mName.length() > 3 && Character.isUpperCase(mName.charAt(3))) {
          propName = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
        } else if (mName.startsWith("is") && mName.length() > 2 && Character.isUpperCase(mName.charAt(2))) {
          propName = Character.toLowerCase(mName.charAt(2)) + mName.substring(3);
        } else if (mName.startsWith("has") && mName.length() > 3 && Character.isUpperCase(mName.charAt(3))) {
          propName = Character.toLowerCase(mName.charAt(3)) + mName.substring(4);
        }
        if (propName != null) {
          String doc = "```groovy\nProperty: " + simpleName(method.getGenericReturnType().getTypeName()) + " " + owner + "." + propName + "\n```\n\nAccesses `" + mName + "()`";
          result.add(new Completion(propName, CompletionKind.PROPERTY, method.getGenericReturnType().getTypeName(), doc,
            propName, method.getGenericReturnType().getTypeName(), List.of(), List.of()));
        }
      }
    }
    for (Field field : receiver.type().getFields()) {
      if (receiver.staticOnly() && !Modifier.isStatic(field.getModifiers())) continue;
      String fieldDoc = extractJavadoc(field.getDeclaringClass(), field.getName(), -1);
      String docs;
      if (fieldDoc != null) {
        docs = fieldDoc + "\n\n---\n\n`" + simpleName(field.getGenericType().getTypeName()) + " " + owner + "." + field.getName() + "`\n\n"
          + "Declared in `" + field.getDeclaringClass().getName() + "`";
      } else {
        docs = "```groovy\n" + simpleName(field.getGenericType().getTypeName()) + " " + owner + "." + field.getName() + "\n```\n\n"
          + "Declared in `" + field.getDeclaringClass().getName() + "`";
      }
      result.add(new Completion(field.getName(), CompletionKind.FIELD, field.getGenericType().getTypeName(), docs,
        field.getName(), field.getGenericType().getTypeName(), List.of(), List.of()));
    }
  }

  private static void addScriptDeclaredMembers(List<Completion> result, String source) {
    Pattern methodPattern = Pattern.compile(
      "\\b(?:public|protected|private|static|final|abstract|synchronized|native|strictfp|def|var|void|[A-Za-z0-9_$<>]+)\\s+([A-Za-z_$][\\w$]*)\\s*\\(([^)]*)\\)");
    Matcher methodMatcher = methodPattern.matcher(source);
    Set<String> reservedKeywords = Set.of("if", "while", "for", "switch", "catch", "new", "super", "this", "return", "class", "import", "package");
    while (methodMatcher.find()) {
      String mName = methodMatcher.group(1);
      if (reservedKeywords.contains(mName)) continue;
      String params = methodMatcher.group(2).strip();
      String detail = mName + "(" + params + ")";
      String doc = "```groovy\n" + detail + "\n```\n\nMethod declared in this script";
      result.add(new Completion(mName, CompletionKind.METHOD, detail, doc, mName + "()", "void", List.of(), List.of()));
    }

    Pattern fieldPattern = Pattern.compile(
      "\\b(?:public|protected|private|static|final|transient|volatile|def|var|[A-Za-z0-9_$<>]+)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=[^;\\n]+|;)");
    Matcher fieldMatcher = fieldPattern.matcher(source);
    while (fieldMatcher.find()) {
      String fName = fieldMatcher.group(1);
      if (reservedKeywords.contains(fName)) continue;
      String doc = "```groovy\n" + fName + "\n```\n\nField or variable declared in this script";
      result.add(new Completion(fName, CompletionKind.VARIABLE, fName, doc, fName, "Object", List.of(), List.of()));
    }
  }

  private static final Map<String, String> JAVADOC_CACHE = new ConcurrentHashMap<>();

  private static String extractJavadoc(Class<?> declaringClass, String memberName, int paramCount) {
    String cacheKey = declaringClass.getName() + "#" + memberName + "/" + paramCount;
    String cached = JAVADOC_CACHE.get(cacheKey);
    if (cached != null) return cached.isEmpty() ? null : cached;

    String result = extractJavadocFromHierarchy(declaringClass, memberName, paramCount);
    JAVADOC_CACHE.put(cacheKey, result == null ? "" : result);
    return result;
  }

  private static String extractJavadocFromHierarchy(Class<?> type, String memberName, int paramCount) {
    Set<Class<?>> visited = new java.util.HashSet<>();
    java.util.Queue<Class<?>> queue = new java.util.LinkedList<>();
    queue.add(type);
    visited.add(type);

    while (!queue.isEmpty()) {
      Class<?> current = queue.poll();
      String source = findSourceFile(current);
      if (source != null) {
        try {
          String content = new String(Files.readAllBytes(Path.of(source)), StandardCharsets.UTF_8);
          String result = parseJavadoc(content, memberName, paramCount);
          if (result != null) return result;
        } catch (IOException ignored) {
        }
      }
      if (current.getSuperclass() != null && !current.getSuperclass().equals(Object.class) && visited.add(current.getSuperclass())) {
        queue.add(current.getSuperclass());
      }
      for (Class<?> iface : current.getInterfaces()) {
        if (visited.add(iface)) queue.add(iface);
      }
    }
    return null;
  }

  private static String findSourceFile(Class<?> type) {
    String relativePath = type.getName().replace('.', '/') + ".java";
    Path srcLayout = Path.of("src/main/java", relativePath);
    if (Files.exists(srcLayout)) return srcLayout.toAbsolutePath().toString();

    try {
      java.net.URL location = type.getProtectionDomain().getCodeSource().getLocation();
      if (location != null) {
        Path codePath = Path.of(location.toURI());
        if (codePath.toString().endsWith(".jar")) {
          Path sourceJar = resolveSourceJar(codePath);
          if (sourceJar != null) {
            String fromJar = extractFromJar(sourceJar, relativePath);
            if (fromJar != null) return fromJar;
          }
        }
        Path found = walkUpForSource(codePath, relativePath);
        if (found != null) return found.toString();
      }
    } catch (Exception ignored) {
    }

    try {
      java.net.URL resource = type.getResource(type.getSimpleName() + ".class");
      if (resource != null) {
        Path classDir = Path.of(resource.toURI()).getParent();
        Path found = walkUpForSource(classDir, relativePath);
        if (found != null) return found.toString();
      }
    } catch (Exception ignored) {
    }

    return null;
  }

  private static Path walkUpForSource(Path start, String relativePath) {
    Path current = start.toAbsolutePath().getParent();
    while (current != null) {
      for (String dir : List.of("src/main/java", "src/main/groovy", "src")) {
        Path candidate = current.resolve(dir).resolve(relativePath);
        if (Files.exists(candidate)) return candidate;
      }
      current = current.getParent();
    }
    return null;
  }

  private static Path resolveSourceJar(Path jarPath) {
    String name = jarPath.getFileName().toString();
    Path parent = jarPath.getParent();
    if (parent == null) return null;

    String sourcesName = name.replace(".jar", "-sources.jar");
    Path sources = parent.resolve(sourcesName);
    if (Files.exists(sources)) return sources;

    for (String prefix : List.of("sources/", "lib/", "")) {
      Path candidate = parent.resolve(prefix).resolve(sourcesName);
      if (Files.exists(candidate)) return candidate;
    }
    return null;
  }

  private static String extractFromJar(Path jarPath, String entryPath) {
    try (java.util.jar.JarFile jar = new java.util.jar.JarFile(jarPath.toFile())) {
      java.util.jar.JarEntry entry = jar.getJarEntry(entryPath);
      if (entry == null) return null;
      try (java.io.InputStream is = jar.getInputStream(entry)) {
        return new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
      }
    } catch (IOException ignored) {
      return null;
    }
  }

  private static String parseJavadoc(String source, String memberName, int paramCount) {
    Pattern javadocEnd = Pattern.compile("\\*/");
    Pattern memberPattern;
    if (paramCount < 0) {
      memberPattern = Pattern.compile("(?:public|protected|private|static|final|transient|volatile|\\s)*\\s+\\S+\\s+" + Pattern.quote(memberName) + "\\s*[=;]");
    } else {
      memberPattern = Pattern.compile("(?:public|protected|private|static|final|abstract|synchronized|native|\\s)*\\s+\\S+\\s+" + Pattern.quote(memberName) + "\\s*\\(");
    }

    int searchFrom = 0;
    while (searchFrom < source.length()) {
      java.util.regex.Matcher m = memberPattern.matcher(source);
      if (!m.find(searchFrom)) return null;
      int memberIdx = m.start();

      String before = source.substring(0, memberIdx);
      int docStart = before.lastIndexOf("/**");
      if (docStart < 0) { searchFrom = m.end(); continue; }

      int docEnd = -1;
      java.util.regex.Matcher endMatch = javadocEnd.matcher(source);
      if (endMatch.find(docStart + 3)) {
        docEnd = endMatch.start();
      }
      if (docEnd < 0 || docEnd > memberIdx) { searchFrom = m.end(); continue; }

      String raw = source.substring(docStart + 3, docEnd);
      return formatJavadoc(raw);
    }
    return null;
  }

  private static String formatJavadoc(String raw) {
    StringBuilder sb = new StringBuilder();
    for (String line : raw.split("\n")) {
      line = line.stripLeading();
      if (line.startsWith("* ")) line = line.substring(2);
      else if (line.startsWith("*")) line = line.substring(1);
      else line = line.strip();
      sb.append(line).append("\n");
    }
    String text = sb.toString().strip();

    text = text.replaceAll("@param\\s+(\\S+)", "**$1:**");
    text = text.replaceAll("@return\\s*", "**Returns:** ");
    text = text.replaceAll("@throws\\s+(\\S+)", "**Throws:** `$1` ");
    text = text.replaceAll("@exception\\s+(\\S+)", "**Throws:** `$1` ");
    text = text.replaceAll("@see\\s+#?(\\w+(?:\\.[\\w]+)*\\(.*?\\))", "**See:** `$1`");
    text = text.replaceAll("@see\\s+(\\S+)", "**See:** `$1`");
    text = text.replaceAll("@since\\s+", "**Since:** ");
    text = text.replaceAll("@deprecated\\s*", "**Deprecated:** ");
    text = text.replaceAll("\\{@link\\s+([^}]+)}", "`$1`");
    text = text.replaceAll("@(author|version)\\s+.*", "");

    String[] lines = text.split("\n");
    StringBuilder cleaned = new StringBuilder();
    for (String line : lines) {
      if (line.trim().startsWith("@")) continue;
      cleaned.append(line).append("\n");
    }
    return cleaned.toString().strip();
  }

  private static int memberRank(Completion completion) {
    return switch (completion.kind()) {
      case METHOD, FIELD, PROPERTY, VARIABLE -> 0;
      case KEYWORD, SNIPPET -> 1;
      case CLASS, CONSTRUCTOR -> 2;
    };
  }

  private static Completion typeCompletion(Class<?> type) {
    return typeCompletion(type, false, List.of());
  }

  private static Completion typeCompletion(Class<?> type, boolean fullyQualified) {
    return typeCompletion(type, fullyQualified, List.of());
  }

  private static Completion typeCompletion(Class<?> type, boolean fullyQualified, List<TextEdit> additionalEdits) {
    String pkg = type.getPackage() == null ? "" : type.getPackage().getName();
    String docs = "```groovy\n" + (fullyQualified ? type.getName() : type.getSimpleName()) + "\n```\n\n"
      + (pkg.isEmpty() ? "" : "`" + pkg + "`");
    return new Completion(type.getSimpleName(), CompletionKind.CLASS, type.getName(), docs,
      fullyQualified ? type.getName() : type.getSimpleName(),
      type.getName(), List.of(), additionalEdits);
  }

  private static Completion function(String name, String returnType, String documentation) {
    return new Completion(name, CompletionKind.METHOD, name + "()", documentation, name + "()", returnType, List.of(), List.of());
  }

  private static int importInsertLine(String source) {
    int lastImportEnd = -1;
    Matcher matcher = IMPORT.matcher(source);
    while (matcher.find()) lastImportEnd = matcher.end();
    if (lastImportEnd >= 0) {
      String afterLastImport = source.substring(lastImportEnd);
      int newline = afterLastImport.indexOf('\n');
      return newline >= 0
        ? (int) source.substring(0, lastImportEnd + newline + 1).lines().count()
        : (int) source.substring(0, lastImportEnd).lines().count();
    }
    int classLine = classLine(source).line();
    return Math.max(0, classLine);
  }

  private static Signature signature(Method method) {
    List<Parameter> parameters = Arrays.stream(method.getParameters())
      .map(parameter -> new Parameter(parameter.getName(), parameter.getParameterizedType().getTypeName())).toList();
    String label = method.getName() + "(" + String.join(", ", parameters.stream()
      .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
    return new Signature(label, method.getGenericReturnType().getTypeName(), parameters);
  }

  private static Symbol symbol(ClassNode type) {
    List<Symbol> children = new ArrayList<>();
    for (FieldNode field : type.getFields()) {
      children.add(new Symbol(field.getName(), SymbolKind.FIELD, field.getType().getName(), range(field), List.of()));
    }
    for (MethodNode method : type.getMethods()) {
      if (method.getDeclaringClass() != type) continue;
      children.add(new Symbol(method.getName(), method.isStaticConstructor() ? SymbolKind.CONSTRUCTOR : SymbolKind.METHOD,
        method.getReturnType().getName(), range(method), List.of()));
    }
    return new Symbol(type.getNameWithoutPackage(), SymbolKind.CLASS, type.getSuperClass().getName(), range(type), children);
  }

  private static Range range(org.codehaus.groovy.ast.ASTNode node) {
    int startLine = Math.max(0, node.getLineNumber() - 1);
    int startColumn = Math.max(0, node.getColumnNumber() - 1);
    int endLine = Math.max(startLine, node.getLastLineNumber() - 1);
    int endColumn = Math.max(0, node.getLastColumnNumber() - 1);
    return new Range(new Position(startLine, startColumn), new Position(endLine, endColumn));
  }

  private static Position classLine(String source) {
    Matcher matcher = Pattern.compile("(?m)^\\s*(?:public\\s+)?class\\s+").matcher(source);
    if (!matcher.find()) return new Position(0, 0);
    return new Position(Math.max(0, (int) source.substring(0, matcher.start()).lines().count() - 1), 0);
  }

  private static int offset(String text, Position position) {
    int line = 0;
    int offset = 0;
    while (line < position.line() && offset < text.length()) if (text.charAt(offset++) == '\n') line++;
    return Math.min(text.length(), offset + position.column());
  }

  private static String receiverExpression(String prefix) {
    int dot = prefix.length() - 1;
    while (dot >= 0 && Character.isWhitespace(prefix.charAt(dot))) dot--;
    if (dot < 0 || prefix.charAt(dot) != '.') return null;
    int end = dot;
    int depth = 0;
    int index = dot - 1;
    for (; index >= 0; index--) {
      char character = prefix.charAt(index);
      if (character == ')') depth++;
      else if (character == '(') depth--;
      if (depth < 0 || depth == 0 && ";{}=,+-*/!?:\n".indexOf(character) >= 0) break;
    }
    String receiver = prefix.substring(index + 1, end).strip();
    return receiver.isEmpty() ? null : receiver;
  }

  private static List<String> splitChain(String expression) {
    List<String> parts = new ArrayList<>();
    int depth = 0;
    int start = 0;
    for (int index = 0; index < expression.length(); index++) {
      char character = expression.charAt(index);
      if (character == '(' || character == '[' || character == '<') depth++;
      else if (character == ')' || character == ']' || character == '>') depth--;
      else if (character == '.' && depth == 0) {
        parts.add(expression.substring(start, index).strip());
        start = index + 1;
      }
    }
    parts.add(expression.substring(start).strip());
    return parts.stream().filter(part -> !part.isEmpty()).toList();
  }

  private static URI classUri(String fqn) {
    String path = fqn.replace('.', '/') + ".java";
    return URI.create("file:///" + path);
  }

  private static Optional<Location> findScriptMember(String source, String name, URI uri) {
    Matcher fieldMatch = Pattern.compile(
      "(?m)^\\s*(?:public|protected|private|static|final|transient|volatile|def|var|\\s)*\\s+\\S+\\s+" + Pattern.quote(name) + "\\s*[=;]")
      .matcher(source);
    if (fieldMatch.find()) {
      int line = (int) source.substring(0, fieldMatch.start()).lines().count() - 1;
      return Optional.of(new Location(uri, new Range(new Position(Math.max(0, line), 0), new Position(Math.max(0, line), 0))));
    }
    Matcher methodMatch = Pattern.compile(
      "(?m)^\\s*(?:public|protected|private|static|final|abstract|synchronized|native|strictfp|def|var|void|\\s)*\\s+\\S+\\s+" + Pattern.quote(name) + "\\s*\\(")
      .matcher(source);
    if (methodMatch.find()) {
      int line = (int) source.substring(0, methodMatch.start()).lines().count() - 1;
      return Optional.of(new Location(uri, new Range(new Position(Math.max(0, line), 0), new Position(Math.max(0, line), 0))));
    }
    return Optional.empty();
  }

  private static int findDeclarationLine(String source, String name) {
    Matcher explicit = EXPLICIT_VARIABLE.matcher(source);
    while (explicit.find()) {
      if (explicit.group(2).equals(name)) {
        int line = (int) source.substring(0, explicit.start()).lines().count() - 1;
        return Math.max(0, line);
      }
    }
    Matcher inferred = INFERRED_VARIABLE.matcher(source);
    while (inferred.find()) {
      if (inferred.group(1).equals(name)) {
        int line = (int) source.substring(0, inferred.start()).lines().count() - 1;
        return Math.max(0, line);
      }
    }
    return 0;
  }

  private static String wordAt(String source, int offset) {
    int start = Math.min(offset, source.length());
    int end = start;
    while (start > 0 && Character.isJavaIdentifierPart(source.charAt(start - 1))) start--;
    while (end < source.length() && Character.isJavaIdentifierPart(source.charAt(end))) end++;
    return source.substring(start, end);
  }

  private static String rawType(String type) {
    return type.replaceFirst("<.*", "").replace("[]", "").strip();
  }

  private static String simpleName(String type) {
    int generic = type.indexOf('<');
    String suffix = generic < 0 ? "" : type.substring(generic);
    String raw = generic < 0 ? type : type.substring(0, generic);
    int separator = Math.max(raw.lastIndexOf('.'), raw.lastIndexOf('$'));
    return (separator < 0 ? raw : raw.substring(separator + 1)) + suffix;
  }

  private record ParsedDocument(URI uri, boolean valid, List<ScriptDiagnostic> diagnostics, List<Symbol> symbols) {}

  private record ResolvedType(Class<?> type, Type genericType, boolean staticOnly) {}
}
