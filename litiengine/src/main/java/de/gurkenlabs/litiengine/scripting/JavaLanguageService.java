package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
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
import javax.tools.Diagnostic;
import javax.tools.DiagnosticCollector;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.SimpleJavaFileObject;
import javax.tools.ToolProvider;

/** Language service providing Monaco intellisense (completion, hover, diagnostics, definition) for Java scripts. */
public class JavaLanguageService implements ScriptLanguageService {
  private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+([\\w.$]+)(?:\\s+as\\s+([A-Za-z_$][\\w$]*))?\\s*;?\\s*$");
  private static final Set<String> KEYWORDS = Set.of(
    "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto",
    "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
    "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var"
  );

  private final Workspace workspace;
  private ParsedDocument lastValid;

  public JavaLanguageService(Workspace workspace) {
    this.workspace = Objects.requireNonNull(workspace);
  }

  @Override
  public Analysis analyze(Document document) {
    ParsedDocument parsed = parse(document);
    return new Analysis(parsed.diagnostics(), parsed.symbols(), List.of());
  }

  @Override
  public List<Completion> complete(Document document, Position position) {
    int offset = offset(document.text(), position);
    String prefix = document.text().substring(0, offset);
    String source = document.text();
    String receiver = receiverExpression(prefix);
    Map<String, Class<?>> variables = this.variables(document, prefix);
    ResolvedType type = receiver == null ? null : this.resolveExpression(receiver, document.definition(), variables, source);
    Set<String> importedFqns = this.importedTypes(source).values().stream().map(Class::getName).collect(java.util.stream.Collectors.toSet());
    int importInsertLine = importInsertLine(source);
    List<Completion> result = new ArrayList<>();

    boolean constructorContext = prefix.matches("(?s).*\\bnew(?:\\s+[\\w.$]*)?$");

    if (type != null) {
      addMembers(result, type);
    } else if (constructorContext) {
      Optional<Class<?>> expectedParamType = this.inferExpectedParameterType(prefix, document.definition(), variables, source);
      expectedParamType.ifPresent(expectedType -> {
        List<TextEdit> edits = importedFqns.contains(expectedType.getName()) ? List.of()
          : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + expectedType.getName() + ";\n"));
        result.add(typeCompletion(expectedType, true, edits));
        if (expectedType.isInterface() || Modifier.isAbstract(expectedType.getModifiers())) {
          result.add(anonymousClassCompletion(expectedType, edits));
        }
      });

      for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
        List<TextEdit> edits = importedFqns.contains(engineType.getName()) ? List.of()
          : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + engineType.getName() + ";\n"));
        result.add(typeCompletion(engineType, true, edits));
      }
      for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
        if (importedFqns.contains(projectType.getName())) continue;
        List<TextEdit> edits = List.of(new TextEdit(
          new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
          "import " + projectType.getName() + ";\n"));
        result.add(typeCompletion(projectType, true, edits));
      }
    } else {
      addScriptDeclaredMembers(result, source);
      result.add(new Completion("globals", CompletionKind.FIELD, "ScriptGlobals",
        "Direct access to global shared game state map (`globals.put(...)`, `globals.get(...)`, `globals.onChanged(...)`).",
        "globals", ScriptGlobals.class.getName(), List.of(), List.of()));
      addScriptScope(result, document.definition(), importedFqns, importInsertLine);
      this.hostType(document.definition()).ifPresent(host -> addMembers(result, new ResolvedType(host, null, false)));

      KEYWORDS.stream().sorted().forEach(keyword -> result.add(new Completion(
        keyword, CompletionKind.KEYWORD, "Java keyword", "", keyword, null, List.of(), List.of())));

      this.importedTypes(source).values().stream().distinct().sorted(Comparator.comparing(Class::getSimpleName))
        .forEach(imported -> result.add(typeCompletion(imported)));

      String currentWord = wordAt(source, offset);
      boolean isUpper = !currentWord.isEmpty() && Character.isUpperCase(currentWord.charAt(0));

      if (isUpper || result.size() < 15) {
        for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
          List<TextEdit> edits = importedFqns.contains(engineType.getName()) ? List.of()
            : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
              "import " + engineType.getName() + ";\n"));
          result.add(typeCompletion(engineType, false, edits));
        }
        for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
          if (importedFqns.contains(projectType.getName())) continue;
          List<TextEdit> edits = List.of(new TextEdit(
            new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + projectType.getName() + ";\n"));
          result.add(typeCompletion(projectType, false, edits));
        }
      }
    }
    return result.stream().collect(java.util.stream.Collectors.toMap(
      completion -> completion.kind() + ":" + completion.label() + ":" + completion.detail(), completion -> completion,
      (left, right) -> left, LinkedHashMap::new)).values().stream()
      .sorted(Comparator.comparing(JavaLanguageService::memberRank)
        .thenComparing(Comparator.comparing(Completion::label, String.CASE_INSENSITIVE_ORDER)))
      .toList();
  }

  @Override
  public List<CodeAction> codeActions(Document document, Range range, List<ScriptDiagnostic> diagnostics) {
    String source = document.text();
    int off = offset(source, range.start());
    String word = wordAt(source, off);
    if (word.isBlank()) {
      off = offset(source, range.end());
      word = wordAt(source, off);
    }

    Set<String> missingSymbols = new java.util.HashSet<>();
    if (!word.isBlank() && Character.isJavaIdentifierStart(word.charAt(0))) {
      missingSymbols.add(word);
    }

    ParsedDocument parsed = parse(document);
    for (ScriptDiagnostic diag : parsed.diagnostics()) {
      if (diag.message() != null && diag.message().contains("cannot find symbol")) {
        Matcher matcher = Pattern.compile("symbol:\\s*(?:variable|class|type)\\s+([A-Za-z_$][\\w$]*)").matcher(diag.message());
        if (matcher.find()) {
          missingSymbols.add(matcher.group(1));
        }
      }
    }

    Set<String> importedFqns = this.importedTypes(source).values().stream().map(Class::getName).collect(java.util.stream.Collectors.toSet());
    int insertLine = importInsertLine(source);
    Range insertRange = new Range(new Position(insertLine, 0), new Position(insertLine, 0));

    List<CodeAction> actions = new ArrayList<>();
    for (String symbol : missingSymbols) {
      List<Class<?>> candidates = new ArrayList<>();
      for (Class<?> type : EngineTypeCatalog.publicTypes()) {
        if (type.getSimpleName().equals(symbol)) candidates.add(type);
      }
      for (Class<?> type : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
        if (type.getSimpleName().equals(symbol) && !candidates.contains(type)) candidates.add(type);
      }

      for (Class<?> candidate : candidates) {
        if (importedFqns.contains(candidate.getName())) continue;
        actions.add(new CodeAction(
          "Import '" + candidate.getName() + "'",
          "quickfix",
          List.of(new TextEdit(insertRange, "import " + candidate.getName() + ";\n"))
        ));
      }
    }
    return actions;
  }

  @Override
  public Optional<Hover> hover(Document document, Position position) {
    String text = document.text();
    int off = offset(text, position);
    String word = wordAt(text, off);
    if (word.isBlank()) return Optional.empty();

    if ("globals".equals(word)) {
      return Optional.of(new Hover("**globals**  `ScriptGlobals`\n\nGlobal shared game state store (`put`, `get`, `onChanged`).", null));
    }

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
          String sig = simpleName(method.getGenericReturnType().getTypeName()) + " " + word + "("
            + Arrays.stream(method.getParameters())
              .map(p -> simpleName(p.getParameterizedType().getTypeName()) + " " + p.getName())
              .reduce((a, b) -> a + ", " + b).orElse("") + ")";
          return Optional.of(new Hover("```java\n" + sig + "\n```\n\nDeclared in `" + method.getDeclaringClass().getName() + "`", null));
        }
      }
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
      .map(JavaLanguageService::signature).toList();
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

    Optional<Class<?>> type = this.resolveType(word, text);
    if (type.isPresent()) return Optional.of(new Location(classUri(type.get().getName()), new Range(new Position(0, 0), new Position(0, 0))));

    return Optional.empty();
  }

  private ParsedDocument parse(Document document) {
    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) return new ParsedDocument(document.uri(), true, List.of(), List.of());

    DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
    String filename = document.uri() == null ? "Script.java" : Path.of(document.uri()).getFileName().toString();
    SimpleJavaFileObject file = new SimpleJavaFileObject(URI.create("string:///" + filename), JavaFileObject.Kind.SOURCE) {
      @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return document.text(); }
    };

    List<String> options = new ArrayList<>();
    String classpath = System.getProperty("java.class.path");
    if (classpath != null && !classpath.isBlank()) {
      options.add("-classpath");
      options.add(classpath);
    }

    JavaCompiler.CompilationTask task = compiler.getTask(null, null, collector, options, null, List.of(file));
    task.call();

    List<ScriptDiagnostic> diagnostics = new ArrayList<>();
    for (Diagnostic<? extends JavaFileObject> diag : collector.getDiagnostics()) {
      if (diag.getKind() == Diagnostic.Kind.ERROR) {
        diagnostics.add(new ScriptDiagnostic(
          ScriptDiagnostic.Severity.ERROR,
          document.definition() == null ? null : document.definition().getId(),
          document.uri() == null ? null : document.uri().toString(),
          (int) diag.getLineNumber(),
          (int) diag.getColumnNumber(),
          diag.getMessage(null)
        ));
      }
    }
    return new ParsedDocument(document.uri(), diagnostics.isEmpty(), List.copyOf(diagnostics), List.of());
  }

  private Map<String, Class<?>> variables(Document document, String source) {
    Map<String, Class<?>> variables = new LinkedHashMap<>(this.importedTypes(source));
    ScriptDefinition definition = document.definition();
    this.hostType(definition).ifPresent(type -> variables.put("host", type));
    variables.put("context", ScriptContext.class);
    variables.put("environment", Environment.class);
    variables.put("globals", ScriptGlobals.class);
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
    if (root.endsWith("()")) {
      String methodName = root.substring(0, root.length() - 2);
      if (methodName.equals("host")) return this.hostType(definition).map(t -> new ResolvedType(t, null, false)).orElse(null);
      if (methodName.equals("context")) return new ResolvedType(ScriptContext.class, null, false);
      if (methodName.equals("environment")) return new ResolvedType(Environment.class, null, false);
      if (methodName.equals("globals")) return new ResolvedType(ScriptGlobals.class, null, false);
    }
    Class<?> varType = variables.get(root);
    if (varType != null) return new ResolvedType(varType, null, false);
    return this.resolveType(root, source).map(t -> new ResolvedType(t, null, true)).orElse(null);
  }

  private ResolvedType resolveMember(ResolvedType receiver, String memberCall, String source, ClassLoader loader) {
    String memberName = memberCall.endsWith("()") ? memberCall.substring(0, memberCall.length() - 2) : memberCall;
    Class<?> type = receiver.type();
    for (Method m : type.getMethods()) {
      if (m.getName().equals(memberName)) {
        return new ResolvedType(m.getReturnType(), m.getGenericReturnType(), false);
      }
    }
    try {
      Field f = type.getField(memberName);
      return new ResolvedType(f.getType(), f.getGenericType(), false);
    } catch (NoSuchFieldException ignored) {}
    return null;
  }

  private static void addScriptScope(List<Completion> result, ScriptDefinition definition, Set<String> importedFqns, int importInsertLine) {
    String host = definition == null || definition.getTargetType() == null ? "Object" : simpleName(definition.getTargetType());
    result.add(function("host", host, "The typed object controlled by this script."));
    result.add(function("context", ScriptContext.class.getSimpleName(), "The current script attachment context."));
    result.add(function("environment", Environment.class.getSimpleName(), "The host's current environment."));
    result.add(function("globals", ScriptGlobals.class.getSimpleName(), "Direct access to the global shared state store."));

    List<TextEdit> gameEdits = importedFqns != null && importedFqns.contains("de.gurkenlabs.litiengine.Game") ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import de.gurkenlabs.litiengine.Game;\n"));
    result.add(new Completion("Game", CompletionKind.CLASS, "de.gurkenlabs.litiengine.Game",
      "The central LITIENGINE static entry point for game systems.", "Game", "Game", List.of(), gameEdits));
  }

  private static void addMembers(List<Completion> result, ResolvedType receiver) {
    String owner = simpleName(receiver.type().getName());
    for (Method method : receiver.type().getMethods()) {
      if (receiver.staticOnly() && !Modifier.isStatic(method.getModifiers())) continue;
      List<Parameter> parameters = Arrays.stream(method.getParameters())
        .map(parameter -> new Parameter(parameter.getName(), parameter.getParameterizedType().getTypeName())).toList();
      String detail = method.getName() + "(" + String.join(", ", parameters.stream()
        .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
      String docs = "```java\n" + simpleName(method.getGenericReturnType().getTypeName()) + " " + owner + "." + detail + "\n```\n\n"
        + "Declared in `" + method.getDeclaringClass().getName() + "`";
      result.add(new Completion(method.getName(), CompletionKind.METHOD, detail, docs, method.getName() + "()",
        method.getGenericReturnType().getTypeName(), parameters, List.of()));
    }
    for (Field field : receiver.type().getFields()) {
      if (receiver.staticOnly() && !Modifier.isStatic(field.getModifiers())) continue;
      String docs = "```java\n" + simpleName(field.getGenericType().getTypeName()) + " " + owner + "." + field.getName() + "\n```\n\n"
        + "Declared in `" + field.getDeclaringClass().getName() + "`";
      result.add(new Completion(field.getName(), CompletionKind.FIELD, field.getGenericType().getTypeName(), docs,
        field.getName(), field.getGenericType().getTypeName(), List.of(), List.of()));
    }
  }

  private static void addScriptDeclaredMembers(List<Completion> result, String source) {
    Pattern methodPattern = Pattern.compile(
      "\\b(?:public|protected|private|static|final|abstract|synchronized|native|strictfp|void|[A-Za-z0-9_$<>]+)\\s+([A-Za-z_$][\\w$]*)\\s*\\(([^)]*)\\)");
    Matcher methodMatcher = methodPattern.matcher(source);
    Set<String> reservedKeywords = Set.of("if", "while", "for", "switch", "catch", "new", "super", "this", "return", "class", "import", "package");
    while (methodMatcher.find()) {
      String mName = methodMatcher.group(1);
      if (reservedKeywords.contains(mName)) continue;
      String params = methodMatcher.group(2).strip();
      String detail = mName + "(" + params + ")";
      String doc = "```java\n" + detail + "\n```\n\nMethod declared in this script";
      result.add(new Completion(mName, CompletionKind.METHOD, detail, doc, mName + "()", "void", List.of(), List.of()));
    }

    Pattern fieldPattern = Pattern.compile(
      "\\b(?:public|protected|private|static|final|transient|volatile|[A-Za-z0-9_$<>]+)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=[^;\\n]+|;)");
    Matcher fieldMatcher = fieldPattern.matcher(source);
    while (fieldMatcher.find()) {
      String fName = fieldMatcher.group(1);
      if (reservedKeywords.contains(fName)) continue;
      String doc = "```java\n" + fName + "\n```\n\nField declared in this script";
      result.add(new Completion(fName, CompletionKind.VARIABLE, fName, doc, fName, "Object", List.of(), List.of()));
    }
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
    ClassLoader loader = this.workspace.classLoader();
    while (matcher.find()) {
      String fqn = matcher.group(1);
      String alias = matcher.group(2);
      try {
        Class<?> type = Class.forName(fqn, false, loader);
        imports.put(alias == null ? type.getSimpleName() : alias, type);
      } catch (ClassNotFoundException | LinkageError ignored) {
      }
    }
    return imports;
  }

  private Optional<Class<?>> resolveType(String name, String source) {
    String raw = rawType(name);
    Class<?> imported = this.importedTypes(source).get(raw);
    if (imported != null) return Optional.of(imported);
    Optional<Class<?>> catalogType = EngineTypeCatalog.findType(raw, this.workspace.classLoader());
    if (catalogType.isPresent()) return catalogType;
    try {
      return Optional.of(Class.forName(raw, false, this.workspace.classLoader()));
    } catch (ClassNotFoundException | LinkageError ignored) {}
    return Optional.empty();
  }

  private static URI classUri(String fqn) {
    return URI.create("class:///" + fqn.replace('.', '/') + ".java");
  }

  private Optional<Class<?>> inferExpectedParameterType(String prefix, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    Matcher matcher = Pattern.compile("(?s)([A-Za-z_$][\\w$]*)\\s*\\(([^()]*)$").matcher(prefix);
    if (!matcher.find()) return Optional.empty();
    String methodName = matcher.group(1);
    String argList = matcher.group(2);
    int argIndex = (int) argList.chars().filter(c -> c == ',').count();
    String callPrefix = prefix.substring(0, matcher.start(1));
    String receiverExpr = receiverExpression(callPrefix);
    ResolvedType receiverType = receiverExpr == null ? this.scriptType(definition)
      : this.resolveExpression(receiverExpr, definition, variables, source);
    if (receiverType == null || receiverType.type() == null) return Optional.empty();
    for (Method method : receiverType.type().getMethods()) {
      if (method.getName().equals(methodName) && method.getParameterCount() > argIndex) {
        return Optional.of(method.getParameterTypes()[argIndex]);
      }
    }
    return Optional.empty();
  }

  private static Completion anonymousClassCompletion(Class<?> type, List<TextEdit> edits) {
    String name = type.getSimpleName();
    Method[] methods = type.getDeclaredMethods();
    StringBuilder body = new StringBuilder();
    body.append(name).append("() {\n");
    for (Method method : methods) {
      if (Modifier.isAbstract(method.getModifiers()) || type.isInterface()) {
        body.append("  @Override\n  public ");
        body.append(simpleName(method.getGenericReturnType().getTypeName())).append(" ");
        body.append(method.getName()).append("(");
        java.lang.reflect.Parameter[] params = method.getParameters();
        for (int i = 0; i < params.length; i++) {
          if (i > 0) body.append(", ");
          body.append(simpleName(params[i].getParameterizedType().getTypeName())).append(" ").append(params[i].isNamePresent() ? params[i].getName() : "arg" + i);
        }
        body.append(") {\n    // TODO: implement\n  }\n");
      }
    }
    body.append("}");
    return new Completion(name + " () { ... }", CompletionKind.SNIPPET, "Anonymous " + name + " implementation",
      "Creates an inline anonymous implementation of `" + type.getName() + "`.", body.toString(), name, List.of(), edits);
  }

  private static Completion typeCompletion(Class<?> type) {
    return typeCompletion(type, false, List.of());
  }

  private static Completion typeCompletion(Class<?> type, boolean fullyQualified, List<TextEdit> additionalEdits) {
    String pkg = type.getPackage() == null ? "" : type.getPackage().getName();
    String docs = "```java\n" + (fullyQualified ? type.getName() : type.getSimpleName()) + "\n```\n\n" + (pkg.isEmpty() ? "" : "`" + pkg + "`");
    return new Completion(type.getSimpleName(), CompletionKind.CLASS, type.getName(), docs,
      fullyQualified ? type.getName() : type.getSimpleName(), type.getName(), List.of(), additionalEdits);
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
      return newline >= 0 ? (int) source.substring(0, lastImportEnd + newline + 1).lines().count()
        : (int) source.substring(0, lastImportEnd).lines().count();
    }
    return 0;
  }

  private static Signature signature(Method method) {
    List<Parameter> parameters = Arrays.stream(method.getParameters())
      .map(parameter -> new Parameter(parameter.getName(), parameter.getParameterizedType().getTypeName())).toList();
    String label = method.getName() + "(" + String.join(", ", parameters.stream()
      .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
    return new Signature(label, method.getGenericReturnType().getTypeName(), parameters);
  }

  private static int memberRank(Completion completion) {
    return switch (completion.kind()) {
      case METHOD, FIELD, PROPERTY, VARIABLE -> 0;
      case KEYWORD, SNIPPET -> 1;
      case CLASS, CONSTRUCTOR -> 2;
    };
  }

  private static int offset(String text, Position position) {
    String[] lines = text.split("\n", -1);
    int offset = 0;
    for (int index = 0; index < Math.min(position.line(), lines.length); index++) {
      offset += lines[index].length() + 1;
    }
    return Math.min(offset + Math.max(0, position.column()), text.length());
  }

  private static String receiverExpression(String text) {
    int end = text.length();
    while (end > 0 && Character.isWhitespace(text.charAt(end - 1))) end--;
    if (end <= 0 || text.charAt(end - 1) != '.') return null;
    int start = end - 1;
    while (start > 0) {
      char current = text.charAt(start - 1);
      if (Character.isJavaIdentifierPart(current) || current == '.' || current == '(' || current == ')') start--;
      else break;
    }
    String expr = text.substring(start, end - 1).strip();
    return expr.isEmpty() ? null : expr;
  }

  private static List<String> splitChain(String expression) {
    List<String> result = new ArrayList<>();
    StringBuilder current = new StringBuilder();
    int parens = 0;
    for (int index = 0; index < expression.length(); index++) {
      char character = expression.charAt(index);
      if (character == '(') parens++;
      else if (character == ')') parens--;
      if (character == '.' && parens == 0) {
        if (!current.isEmpty()) result.add(current.toString().strip());
        current.setLength(0);
      } else {
        current.append(character);
      }
    }
    if (!current.isEmpty()) result.add(current.toString().strip());
    return result;
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
