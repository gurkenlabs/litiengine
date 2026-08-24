package de.gurkenlabs.litiengine.scripting;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.Environment;
import de.gurkenlabs.litiengine.graphics.ICamera;
import de.gurkenlabs.litiengine.scripting.ui.ScriptUiOverlay;
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
import java.util.HashSet;
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
  private static final Pattern IMPORT = Pattern.compile("(?m)^\\s*import\\s+(?:static\\s+)?([\\w.$]+(?:\\.\\*)?)(?:\\s+as\\s+([A-Za-z_$][\\w$]*))?\\s*;?\\s*$");
  private static final Set<String> KEYWORDS = Set.of(
    "abstract", "boolean", "break", "byte", "case", "catch", "char", "class", "const", "continue",
    "default", "do", "double", "else", "enum", "extends", "final", "finally", "float", "for", "goto",
    "if", "implements", "import", "instanceof", "int", "interface", "long", "native", "new", "package",
    "private", "protected", "public", "return", "short", "static", "strictfp", "super", "switch",
    "synchronized", "this", "throw", "throws", "transient", "try", "void", "volatile", "while", "var"
  );

  private record CachedMembers(List<Completion> all, List<Completion> staticOnly) {}
  private static final Map<Class<?>, CachedMembers> MEMBER_CACHE = new ConcurrentHashMap<>();
  private static final Map<Class<?>, Completion> TYPE_COMPLETION_CACHE = new ConcurrentHashMap<>();

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

  private static String currentWord(String prefix) {
    if (prefix == null || prefix.isEmpty()) return "";
    int i = prefix.length() - 1;
    while (i >= 0 && (Character.isJavaIdentifierPart(prefix.charAt(i)) || prefix.charAt(i) == '@')) {
      i--;
    }
    return prefix.substring(i + 1);
  }

  private static boolean matchesPrefix(String label, String prefix) {
    if (prefix == null || prefix.isEmpty()) return true;
    if (label.regionMatches(true, 0, prefix, 0, prefix.length())) return true;
    if (prefix.length() <= 4) {
      int pIdx = 0;
      for (int i = 0; i < label.length() && pIdx < prefix.length(); i++) {
        char c = label.charAt(i);
        if (Character.isUpperCase(c) && Character.toLowerCase(c) == Character.toLowerCase(prefix.charAt(pIdx))) {
          pIdx++;
        }
      }
      if (pIdx == prefix.length()) return true;
    }
    return label.toLowerCase(java.util.Locale.ROOT).contains(prefix.toLowerCase(java.util.Locale.ROOT));
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

    Matcher annotParamMatcher = Pattern.compile("(?s)@([A-Za-z0-9_$]+)\\s*\\(([^)]*)$").matcher(prefix);
    boolean annotationContext = prefix.matches("(?s).*@\\s*[A-Za-z0-9_$]*$");
    boolean constructorContext = prefix.matches("(?s).*\\bnew(?:\\s+[\\w.$]*)?$");
    boolean insideMethodArgs = findUnclosedParen(prefix) >= 0;

    if (annotParamMatcher.find()) {
      String annotName = annotParamMatcher.group(1);
      String paramsContent = annotParamMatcher.group(2);
      this.addAnnotationAttributeCompletions(result, annotName, paramsContent, source);
      return result;
    } else if (type != null) {
      addMembers(result, type);
    } else if (annotationContext) {
      this.addAnnotationCompletions(result, source, importedFqns, importInsertLine);
    } else if (constructorContext) {

      Optional<Class<?>> expectedParamType = this.inferExpectedParameterType(prefix, document.definition(), variables, source);
      expectedParamType.ifPresent(expectedType -> {
        List<TextEdit> edits = importedFqns.contains(expectedType.getName()) ? List.of()
          : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + expectedType.getName() + ";\n"));
        result.add(typeCompletionWithEdits(expectedType, true, edits));
        if (expectedType.isInterface() || Modifier.isAbstract(expectedType.getModifiers())) {
          result.add(anonymousClassCompletion(expectedType, edits));
        }
      });

      for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
        List<TextEdit> edits = importedFqns.contains(engineType.getName()) ? List.of()
          : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + engineType.getName() + ";\n"));
        result.add(typeCompletionWithEdits(engineType, true, edits));
      }
      for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
        if (importedFqns.contains(projectType.getName())) continue;
        List<TextEdit> edits = List.of(new TextEdit(
          new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
          "import " + projectType.getName() + ";\n"));
        result.add(typeCompletionWithEdits(projectType, true, edits));
      }
    } else {
      String word = currentWord(prefix);
      if (insideMethodArgs) {
        this.addParameterCompletions(result, prefix, document.definition(), variables, source, importedFqns, importInsertLine);
      }
      addScriptDeclaredMembers(result, source);
      result.add(new Completion("globals", CompletionKind.FIELD, "ScriptGlobals",
        "Direct access to global shared game state map (`globals.put(...)`, `globals.get(...)`, `globals.onChanged(...)`).",
        "globals", ScriptGlobals.class.getName(), List.of(), List.of()));
      addScriptScope(result, document.definition(), importedFqns, importInsertLine);
      ResolvedType scriptType = this.scriptType(document.definition(), source);
      if (scriptType != null) {
        addMembers(result, scriptType);
      }
      this.hostType(document.definition(), source).ifPresent(host -> addMembers(result, new ResolvedType(host, null, false)));
      if (!insideMethodArgs && (word.startsWith("@") || word.startsWith("Script") || word.startsWith("Prop"))) {
        this.addAnnotationCompletions(result, source, importedFqns, importInsertLine);
      }

      KEYWORDS.stream().sorted().forEach(keyword -> {
        if (word.isEmpty() || matchesPrefix(keyword, word)) {
          result.add(new Completion(
            keyword, CompletionKind.KEYWORD, "Java keyword", "", keyword, null, List.of(), List.of()));
        }
      });

      this.importedTypes(source).values().stream().distinct().sorted(Comparator.comparing(Class::getSimpleName))
        .forEach(imported -> {
          if (word.isEmpty() || matchesPrefix(imported.getSimpleName(), word)) {
            result.add(getCachedTypeCompletion(imported));
          }
        });

      for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
        if (word.isEmpty() || matchesPrefix(engineType.getSimpleName(), word)) {
          List<TextEdit> edits = importedFqns.contains(engineType.getName()) ? List.of()
            : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
              "import " + engineType.getName() + ";\n"));
          result.add(typeCompletionWithEdits(engineType, false, edits));
        }
      }
      for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
        if (importedFqns.contains(projectType.getName())) continue;
        if (word.isEmpty() || matchesPrefix(projectType.getSimpleName(), word)) {
          List<TextEdit> edits = List.of(new TextEdit(
            new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + projectType.getName() + ";\n"));
          result.add(typeCompletionWithEdits(projectType, false, edits));
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

    ParsedDocument parsed = parse(document);
    List<ScriptDiagnostic> allDiags = new ArrayList<>(parsed.diagnostics());
    if (diagnostics != null) allDiags.addAll(diagnostics);

    int targetLine = range.start().line();
    boolean lineHasDiagnostic = allDiags.stream().anyMatch(d -> d.line() - 1 == targetLine);

    Set<String> missingSymbols = new java.util.LinkedHashSet<>();
    if (lineHasDiagnostic && !word.isBlank() && Character.isJavaIdentifierStart(word.charAt(0)) && !KEYWORDS.contains(word)) {
      missingSymbols.add(word);
    }

    for (ScriptDiagnostic diag : allDiags) {
      if (diag.message() == null) continue;
      Matcher matcher = Pattern.compile("symbol:\\s*(?:variable|class|type|package)\\s+([A-Za-z_$][\\w$]*)").matcher(diag.message());
      if (matcher.find()) {
        missingSymbols.add(matcher.group(1));
      }
      Matcher resolveMatcher = Pattern.compile("([A-Za-z_$][\\w$]*)\\s+cannot be resolved").matcher(diag.message());
      if (resolveMatcher.find()) {
        missingSymbols.add(resolveMatcher.group(1));
      }
      Matcher pkgMatcher = Pattern.compile("package\\s+([A-Za-z_$][\\w$]*)\\s+does not exist").matcher(diag.message());
      if (pkgMatcher.find()) {
        missingSymbols.add(pkgMatcher.group(1));
      }
      Matcher annotMatcher = Pattern.compile("@([A-Za-z_$][\\w$]*)").matcher(diag.message());
      if (annotMatcher.find()) {
        missingSymbols.add(annotMatcher.group(1));
      }
    }

    missingSymbols.removeIf(s -> s.isBlank() || KEYWORDS.contains(s));


    Set<String> importedFqns = this.importedTypes(source).values().stream().map(Class::getName).collect(java.util.stream.Collectors.toSet());
    int insertLine = importInsertLine(source);
    Range insertRange = new Range(new Position(insertLine, 0), new Position(insertLine, 0));

    List<CodeAction> actions = new ArrayList<>();
    String[] lines = source.split("\r?\n", -1);
    List<DeclaredScriptField> declaredFields = scanDeclaredFields(source);

    DeclaredScriptField targetField = declaredFields.stream()

      .filter(f -> f.line() == targetLine || f.annotationLine() == targetLine || (f.line() >= range.start().line() && f.line() <= range.end().line()))
      .findFirst().orElse(null);

    if (targetField != null) {
      if (!targetField.hasScriptProperty()) {
        List<TextEdit> propEdits = new ArrayList<>();
        if (targetField.annotationRange() != null && targetField.annotationLine() >= 0 && lines[targetField.annotationLine()].trim().matches("^@\\s*[A-Za-z0-9_$]*$")) {
          propEdits.add(new TextEdit(targetField.annotationRange(), targetField.indentation() + "@ScriptProperty"));
        } else {
          propEdits.add(new TextEdit(new Range(new Position(targetField.line(), 0), new Position(targetField.line(), 0)),
            targetField.indentation() + "@ScriptProperty\n"));
        }
        if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
          propEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
        }
        actions.add(new CodeAction("Add '@ScriptProperty' to field '" + targetField.name() + "'", "quickfix", propEdits));

        List<TextEdit> propParamEdits = new ArrayList<>();
        String snippet = targetField.indentation() + "@ScriptProperty(name = \"" + targetField.name() + "\", description = \"\")";
        if (targetField.annotationRange() != null && targetField.annotationLine() >= 0 && lines[targetField.annotationLine()].trim().matches("^@\\s*[A-Za-z0-9_$]*$")) {
          propParamEdits.add(new TextEdit(targetField.annotationRange(), snippet));
        } else {
          propParamEdits.add(new TextEdit(new Range(new Position(targetField.line(), 0), new Position(targetField.line(), 0)),
            snippet + "\n"));
        }
        if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
          propParamEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
        }
        actions.add(new CodeAction("Add '@ScriptProperty(name = \"" + targetField.name() + "\", description = \"...\")' to field '" + targetField.name() + "'", "quickfix", propParamEdits));
      } else if (!targetField.hasAnnotationParams() && targetField.annotationRange() != null) {
        actions.add(new CodeAction("Configure '@ScriptProperty' attributes for '" + targetField.name() + "'", "refactor",
          List.of(new TextEdit(targetField.annotationRange(), targetField.indentation() + "@ScriptProperty(name = \"" + targetField.name() + "\", description = \"\")"))));
      }
    }

    if (targetLine >= 0 && targetLine < lines.length) {
      String lineStr = lines[targetLine];
      Matcher atLineMatcher = Pattern.compile("^(\\s*)@\\s*([A-Za-z0-9_$]*)").matcher(lineStr);
      if (atLineMatcher.find()) {
        String indent = atLineMatcher.group(1);
        String annotName = atLineMatcher.group(2);
        Range annotReplaceRange = new Range(new Position(targetLine, atLineMatcher.start()), new Position(targetLine, atLineMatcher.end()));

        if (!annotName.equals("ScriptProperty") && actions.stream().noneMatch(a -> a.title().contains("@ScriptProperty"))) {
          List<TextEdit> propEdits = new ArrayList<>();
          propEdits.add(new TextEdit(annotReplaceRange, indent + "@ScriptProperty"));
          if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            propEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptProperty'", "quickfix", propEdits));

          List<TextEdit> propParamEdits = new ArrayList<>();
          propParamEdits.add(new TextEdit(annotReplaceRange, indent + "@ScriptProperty(name = \"" + annotName.toLowerCase(java.util.Locale.ROOT) + "\", description = \"\")"));
          if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            propParamEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptProperty(name = \"...\", description = \"...\")'", "quickfix", propParamEdits));
        }

        if (!annotName.equals("ScriptInfo") && actions.stream().noneMatch(a -> a.title().contains("@ScriptInfo"))) {
          List<TextEdit> infoEdits = new ArrayList<>();
          infoEdits.add(new TextEdit(annotReplaceRange, indent + "@ScriptInfo"));
          if (!importedFqns.contains(ScriptInfo.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            infoEdits.add(new TextEdit(insertRange, "import " + ScriptInfo.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptInfo'", "quickfix", infoEdits));
        }
      }
    }

    for (DeclaredScriptField field : declaredFields) {
      if (!field.hasScriptProperty() && (targetField == null || !field.name().equals(targetField.name()))) {
        List<TextEdit> edits = new ArrayList<>();
        edits.add(new TextEdit(new Range(new Position(field.line(), 0), new Position(field.line(), 0)),
          field.indentation() + "@ScriptProperty\n"));
        if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
          edits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
        }
        actions.add(new CodeAction("Convert field '" + field.name() + "' to '@ScriptProperty'", "refactor", edits));
      }
    }

    for (String symbol : missingSymbols) {
      // 1. Check if the symbol is used as an annotation (@symbol)
      boolean isAnnotationUsage = false;
      Range symbolRange = range;
      for (int i = 0; i < lines.length; i++) {
        String lineStr = lines[i];
        Matcher atMatcher = Pattern.compile("@\\s*(" + Pattern.quote(symbol) + ")\\b").matcher(lineStr);
        if (atMatcher.find()) {
          isAnnotationUsage = true;
          Position startPos = new Position(i, atMatcher.start(1));
          Position endPos = new Position(i, atMatcher.end(1));
          symbolRange = new Range(startPos, endPos);
          break;
        }
      }

      if (isAnnotationUsage || symbol.startsWith("Script") || symbol.startsWith("Prop") || symbol.equalsIgnoreCase("Property")) {
        // Offer @ScriptProperty
        if (actions.stream().noneMatch(a -> a.title().contains("@ScriptProperty"))) {
          List<TextEdit> propEdits = new ArrayList<>();
          propEdits.add(new TextEdit(symbolRange, "ScriptProperty"));
          if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            propEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptProperty'", "quickfix", propEdits));

          // Offer @ScriptProperty with parameters snippet/template
          List<TextEdit> propParamEdits = new ArrayList<>();
          propParamEdits.add(new TextEdit(symbolRange, "ScriptProperty(name = \"" + symbol.toLowerCase(java.util.Locale.ROOT) + "\", description = \"\")"));
          if (!importedFqns.contains(ScriptProperty.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            propParamEdits.add(new TextEdit(insertRange, "import " + ScriptProperty.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptProperty(name = \"...\", description = \"...\")'", "quickfix", propParamEdits));
        }

        // Offer @ScriptInfo
        if (actions.stream().noneMatch(a -> a.title().contains("@ScriptInfo"))) {
          List<TextEdit> infoEdits = new ArrayList<>();
          infoEdits.add(new TextEdit(symbolRange, "ScriptInfo"));
          if (!importedFqns.contains(ScriptInfo.class.getName()) && !isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) {
            infoEdits.add(new TextEdit(insertRange, "import " + ScriptInfo.class.getName() + ";\n"));
          }
          actions.add(new CodeAction("Change to '@ScriptInfo'", "quickfix", infoEdits));
        }

        // Offer @Override if symbol is close
        if ("Override".toLowerCase(java.util.Locale.ROOT).contains(symbol.toLowerCase(java.util.Locale.ROOT)) || symbol.equalsIgnoreCase("Over")) {
          actions.add(new CodeAction("Change to '@Override'", "quickfix", List.of(new TextEdit(symbolRange, "Override"))));
        }
      }

      // 2. Exact match candidates for import
      List<Class<?>> exactCandidates = new ArrayList<>();
      for (Class<?> type : EngineTypeCatalog.publicTypes()) {
        if (type.getSimpleName().equals(symbol)) exactCandidates.add(type);
      }
      for (Class<?> type : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
        if (type.getSimpleName().equals(symbol) && !exactCandidates.contains(type)) exactCandidates.add(type);
      }

      for (Class<?> candidate : exactCandidates) {
        if (importedFqns.contains(candidate.getName())) continue;
        actions.add(new CodeAction(
          "Import '" + candidate.getName() + "'",
          "quickfix",
          List.of(new TextEdit(insertRange, "import " + candidate.getName() + ";\n"))
        ));
      }

      // 3. Prefix / fuzzy match candidates for "Change to '...'"
      if (exactCandidates.isEmpty()) {
        List<Class<?>> fuzzyCandidates = new ArrayList<>();
        String lowerSymbol = symbol.toLowerCase(java.util.Locale.ROOT);
        for (Class<?> type : EngineTypeCatalog.publicTypes()) {
          String lowerName = type.getSimpleName().toLowerCase(java.util.Locale.ROOT);
          if (lowerName.startsWith(lowerSymbol) || (lowerSymbol.length() >= 3 && lowerName.contains(lowerSymbol))) {
            if (!fuzzyCandidates.contains(type)) fuzzyCandidates.add(type);
          }
        }
        for (Class<?> type : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
          String lowerName = type.getSimpleName().toLowerCase(java.util.Locale.ROOT);
          if (lowerName.startsWith(lowerSymbol) || (lowerSymbol.length() >= 3 && lowerName.contains(lowerSymbol))) {
            if (!fuzzyCandidates.contains(type)) fuzzyCandidates.add(type);
          }
        }
        for (Class<?> candidate : fuzzyCandidates.stream().limit(5).toList()) {
          List<TextEdit> edits = new ArrayList<>();
          edits.add(new TextEdit(symbolRange, candidate.getSimpleName()));
          if (!importedFqns.contains(candidate.getName()) && !isPackageWildcardImported(candidate.getPackageName(), source)) {
            edits.add(new TextEdit(insertRange, "import " + candidate.getName() + ";\n"));
          }
          actions.add(new CodeAction(
            "Change to '" + candidate.getSimpleName() + "' (" + candidate.getPackageName() + ")",
            "quickfix",
            edits
          ));
        }
      }
    }
    actions.addAll(this.abstractMethodCodeActions(document, parsed));
    actions.addAll(this.syntaxErrorCodeActions(document, parsed, diagnostics));
    return actions;
  }

  private List<CodeAction> syntaxErrorCodeActions(Document document, ParsedDocument parsed, List<ScriptDiagnostic> externalDiagnostics) {
    List<CodeAction> actions = new ArrayList<>();
    String source = document.text();
    String[] lines = source.split("\r?\n", -1);

    List<ScriptDiagnostic> allDiagnostics = new ArrayList<>(parsed.diagnostics());
    if (externalDiagnostics != null) allDiagnostics.addAll(externalDiagnostics);

    for (ScriptDiagnostic diag : allDiagnostics) {
      if (diag.message() == null) continue;
      int diagLine = Math.max(1, Math.min(lines.length, diag.line()));
      String lineContent = lines[diagLine - 1];

      if (diag.message().contains("'{' or ';' expected") || diag.message().contains("illegal start of type")) {
        int lineLen = lineContent.length();
        Position endOfLine = new Position(diagLine - 1, lineLen);
        Range endRange = new Range(endOfLine, endOfLine);

        actions.add(new CodeAction(
          "Add method body '{\n}'",
          "quickfix",
          List.of(new TextEdit(endRange, " {\n    // TODO: implement\n  }"))
        ));
        actions.add(new CodeAction(
          "Add ';'",
          "quickfix",
          List.of(new TextEdit(endRange, ";"))
        ));
      } else if (diag.message().contains("';' expected")) {
        int lineLen = lineContent.length();
        Position endOfLine = new Position(diagLine - 1, lineLen);
        Range endRange = new Range(endOfLine, endOfLine);

        actions.add(new CodeAction(
          "Insert ';'",
          "quickfix",
          List.of(new TextEdit(endRange, ";"))
        ));
      } else if (diag.message().contains("')' expected")) {
        int lineLen = lineContent.length();
        Position endOfLine = new Position(diagLine - 1, lineLen);
        Range endRange = new Range(endOfLine, endOfLine);

        actions.add(new CodeAction(
          "Insert ')'",
          "quickfix",
          List.of(new TextEdit(endRange, ")"))
        ));
      } else if (diag.message().contains("'}' expected") || diag.message().contains("reached end of file while parsing")) {
        int lastLine = Math.max(0, lines.length - 1);
        int lastLineLen = lines[lastLine].length();
        Position endOfFile = new Position(lastLine, lastLineLen);
        Range eofRange = new Range(endOfFile, endOfFile);

        actions.add(new CodeAction(
          "Insert missing '}'",
          "quickfix",
          List.of(new TextEdit(eofRange, "\n}"))
        ));
      } else if (diag.message().contains("missing return statement")) {
        int lineLen = lineContent.length();
        Position endOfLine = new Position(diagLine - 1, lineLen);
        Range endRange = new Range(endOfLine, endOfLine);

        actions.add(new CodeAction(
          "Add 'return null;'",
          "quickfix",
          List.of(new TextEdit(endRange, "\n    return null;"))
        ));
      } else if (diag.message().contains("cyclic") || diag.message().contains("inheritance") || diag.message().contains("Vererbung")) {
        String cls = null;
        Matcher extendsM = Pattern.compile("class\\s+([A-Za-z_$][\\w$]*)\\s+extends\\s+([A-Za-z_$][\\w$]*)").matcher(lineContent);
        if (extendsM.find() && extendsM.group(1).equals(extendsM.group(2))) {
          cls = extendsM.group(1);
        } else {
          Matcher cyclicMatcher = Pattern.compile("(?:cyclic inheritance involving|zyklische Vererbung bei)\\s+([A-Za-z_$][\\w$.]*)").matcher(diag.message());
          if (cyclicMatcher.find()) {
            cls = cyclicMatcher.group(1);
            if (cls.contains(".")) cls = cls.substring(cls.lastIndexOf('.') + 1);
          }
        }
        if (cls != null) {
          Pattern extendsPattern = Pattern.compile("(\\bclass\\s+[A-Za-z_$][\\w$]*\\s+extends\\s+)(" + Pattern.quote(cls) + ")(\\b)");
          Matcher lineMatcher = extendsPattern.matcher(lineContent);
          if (lineMatcher.find()) {
            Position startPos = new Position(diagLine - 1, lineMatcher.start(2));
            Position endPos = new Position(diagLine - 1, lineMatcher.end(2));
            actions.add(new CodeAction(
              "Qualify superclass 'de.gurkenlabs.litiengine.scripting." + cls + "'",
              "quickfix",
              List.of(new TextEdit(new Range(startPos, endPos), "de.gurkenlabs.litiengine.scripting." + cls))
            ));
          }
        }
      } else if (diag.message().contains("should be declared in a file named")) {
        Matcher fileMatcher = Pattern.compile("class\\s+([A-Za-z_$][\\w$]*)\\s+is public,\\s+should be declared in a file named\\s+([A-Za-z_$][\\w$]*)\\.java").matcher(diag.message());
        String expectedClassName = null;
        String classInCode = null;
        if (fileMatcher.find()) {
          classInCode = fileMatcher.group(1);
          expectedClassName = fileMatcher.group(2);
        } else if (document.definition() != null) {
          expectedClassName = document.definition().getImplementation();
          Matcher declM = Pattern.compile("public\\s+class\\s+([A-Za-z_$][\\w$]*)").matcher(lineContent);
          if (declM.find()) classInCode = declM.group(1);
        }
        if (classInCode != null && expectedClassName != null) {
          Pattern classDeclPattern = Pattern.compile("(\\bpublic\\s+class\\s+)(" + Pattern.quote(classInCode) + ")(\\b)");
          Matcher lineMatcher = classDeclPattern.matcher(lineContent);
          if (lineMatcher.find()) {
            Position startPos = new Position(diagLine - 1, lineMatcher.start(2));
            Position endPos = new Position(diagLine - 1, lineMatcher.end(2));
            actions.add(new CodeAction(
              "Rename class in editor to '" + expectedClassName + "'",
              "quickfix",
              List.of(new TextEdit(new Range(startPos, endPos), expectedClassName))
            ));
          }
        }
      }
    }
    return actions;
  }

  private List<CodeAction> abstractMethodCodeActions(Document document, ParsedDocument parsed) {
    List<CodeAction> actions = new ArrayList<>();
    String source = document.text();

    Pattern classPattern = Pattern.compile("(?m)^\\s*(?:public|protected|private|static|final)*\\s*class\\s+([A-Za-z_$][\\w$]*)(?:\\s+extends\\s+([A-Za-z_$][\\w$.<>]+))?(?:\\s+implements\\s+([A-Za-z_$][\\w$,\\s.<>]+))?\\s*\\{");
    Matcher matcher = classPattern.matcher(source);
    while (matcher.find()) {
      String superClass = matcher.group(2);
      String implementsList = matcher.group(3);

      List<String> contracts = new ArrayList<>();
      if (superClass != null) contracts.add(superClass.replaceAll("<.*>", "").strip());
      if (implementsList != null) {
        for (String item : implementsList.split(",")) {
          contracts.add(item.replaceAll("<.*>", "").strip());
        }
      }

      for (String contract : contracts) {
        Optional<Class<?>> resolvedContract = this.resolveType(contract, source);
        if (resolvedContract.isEmpty()) continue;
        Class<?> contractClass = resolvedContract.get();

        List<Method> missingMethods = new ArrayList<>();
        for (Method m : contractClass.getMethods()) {
          if (Modifier.isAbstract(m.getModifiers()) || contractClass.isInterface()) {
            Pattern mPattern = Pattern.compile("\\b" + Pattern.quote(m.getName()) + "\\s*\\(");
            if (!mPattern.matcher(source).find()) {
              missingMethods.add(m);
            }
          }
        }

        if (!missingMethods.isEmpty()) {
          int insertPos = findClassClosingBrace(source, matcher.start());
          Position insertPosition = positionAt(source, insertPos);
          Range insertRange = new Range(insertPosition, insertPosition);

          StringBuilder sb = new StringBuilder();
          for (Method m : missingMethods) {
            sb.append("  @Override\n  public ");
            sb.append(simpleName(m.getGenericReturnType().getTypeName())).append(" ");
            sb.append(m.getName()).append("(");
            java.lang.reflect.Parameter[] params = m.getParameters();
            for (int i = 0; i < params.length; i++) {
              if (i > 0) sb.append(", ");
              sb.append(simpleName(params[i].getParameterizedType().getTypeName())).append(" ")
                .append(params[i].isNamePresent() ? params[i].getName() : "arg" + i);
            }
            sb.append(") {\n    // TODO: implement\n  }\n\n");
          }

          actions.add(new CodeAction(
            "Implement abstract methods for '" + contractClass.getSimpleName() + "'",
            "quickfix",
            List.of(new TextEdit(insertRange, sb.toString()))
          ));
        }
      }
    }
    return actions;
  }

  private static int findClassClosingBrace(String source, int classStart) {
    int openBrace = source.indexOf('{', classStart);
    if (openBrace < 0) return source.length();
    int depth = 1;
    for (int i = openBrace + 1; i < source.length(); i++) {
      char c = source.charAt(i);
      if (c == '{') depth++;
      else if (c == '}') {
        depth--;
        if (depth == 0) return i;
      }
    }
    return source.length();
  }

  private static Position positionAt(String source, int offset) {
    int line = 0;
    int col = 0;
    for (int i = 0; i < Math.min(offset, source.length()); i++) {
      if (source.charAt(i) == '\n') {
        line++;
        col = 0;
      } else {
        col++;
      }
    }
    return new Position(line, col);
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

    Optional<Class<?>> resolved = this.resolveType(word, text);
    if (resolved.isPresent()) {
      Class<?> cls = resolved.get();
      String pkg = cls.getPackage() == null ? "" : cls.getPackage().getName();
      String docs = "```java\n" + cls.getName() + "\n```";
      String description = ScriptDocumentation.get(cls);
      if (!description.isBlank()) {
        docs += "\n\n" + description;
      } else {
        docs += "\n\n### " + cls.getSimpleName() + "\n";
        if (cls.isInterface()) {
          docs += "Interface in package `" + pkg + "`.\n";
        } else if (cls.isEnum()) {
          docs += "Enum in package `" + pkg + "`.\n";
        } else {
          docs += "Class in package `" + pkg + "`.\n";
        }
        Class<?> superCls = cls.getSuperclass();
        if (superCls != null && superCls != Object.class) {
          docs += "\n**Extends:** `" + superCls.getSimpleName() + "`\n";
        }
        Class<?>[] interfaces = cls.getInterfaces();
        if (interfaces.length > 0) {
          docs += "\n**Implements:** " + Arrays.stream(interfaces).map(i -> "`" + i.getSimpleName() + "`").reduce((a, b) -> a + ", " + b).orElse("") + "\n";
        }
      }
      return Optional.of(new Hover(docs, null));
    }

    List<Method> candidateMethods = new ArrayList<>();
    String declaringClass = null;

    String prefix = text.substring(0, off);
    String receiver = receiverExpression(prefix);
    if (receiver != null) {
      Map<String, Class<?>> variables = this.variables(document, text);
      ResolvedType receiverType = this.resolveExpression(receiver, document.definition(), variables, text);
      if (receiverType != null) {
        candidateMethods = Arrays.stream(receiverType.type().getMethods())
          .filter(m -> m.getName().equals(word))
          .sorted(Comparator.comparingInt(Method::getParameterCount))
          .toList();
        if (!candidateMethods.isEmpty()) {
          declaringClass = candidateMethods.get(0).getDeclaringClass().getName();
        } else {
          Field field = Arrays.stream(receiverType.type().getFields())
            .filter(f -> f.getName().equals(word)).findFirst().orElse(null);
          if (field != null) {
            String docs = "```java\n" + simpleName(field.getGenericType().getTypeName()) + " " + field.getName() + "\n```\n\nDeclared in `" + field.getDeclaringClass().getName() + "`";
            String attrDoc = ScriptDocumentation.getAttributeDoc(word);
            if (!attrDoc.isBlank()) {
              docs += "\n\n" + attrDoc;
            }
            return Optional.of(new Hover(docs, null));
          }
        }
      }
    }

    if (candidateMethods.isEmpty()) {
      ResolvedType scriptType = this.scriptType(document.definition());
      if (scriptType != null) {
        candidateMethods = Arrays.stream(scriptType.type().getMethods())
          .filter(m -> m.getName().equals(word))
          .sorted(Comparator.comparingInt(Method::getParameterCount))
          .toList();
        if (!candidateMethods.isEmpty()) {
          declaringClass = candidateMethods.get(0).getDeclaringClass().getName();
        }
      }
    }

    if (!candidateMethods.isEmpty()) {
      StringBuilder sigs = new StringBuilder("```java\n");
      for (Method m : candidateMethods) {
        sigs.append(formatMethodSignature(m)).append("\n");
      }
      sigs.append("```\n\nDeclared in `").append(declaringClass != null ? declaringClass : candidateMethods.get(0).getDeclaringClass().getName()).append("`");
      String methodDoc = ScriptDocumentation.getMethodDoc(word);
      if (!methodDoc.isBlank()) {
        sigs.append("\n\n").append(methodDoc);
      }
      return Optional.of(new Hover(sigs.toString(), null));
    }

    String methodDoc = ScriptDocumentation.getMethodDoc(word);
    if (!methodDoc.isBlank()) {
      return Optional.of(new Hover("**" + word + "**\n\n" + methodDoc, null));
    }

    String attrDoc = ScriptDocumentation.getAttributeDoc(word);
    if (!attrDoc.isBlank()) {
      return Optional.of(new Hover(attrDoc, null));
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

  private static final int MAX_PARSE_CACHE_SIZE = 32;
  private final Map<String, ParsedDocument> parseCache = new ConcurrentHashMap<>();

  private ParsedDocument parse(Document document) {
    if (document == null || document.text() == null) {
      return new ParsedDocument(null, true, List.of(), List.of());
    }

    String cacheKey = (document.uri() == null ? "" : document.uri().toString()) + ":" + document.version() + ":" + document.text().hashCode();
    ParsedDocument cached = this.parseCache.get(cacheKey);
    if (cached != null) return cached;

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) return new ParsedDocument(document.uri(), true, List.of(), List.of());

    DiagnosticCollector<JavaFileObject> collector = new DiagnosticCollector<>();
    String filename;
    if (document.uri() != null && document.uri().isAbsolute() && !"string".equalsIgnoreCase(document.uri().getScheme())
        && !"inmemory".equalsIgnoreCase(document.uri().getScheme())) {
      try {
        filename = Path.of(document.uri()).getFileName().toString();
      } catch (Exception ignored) {
        String className = document.definition() != null && document.definition().getImplementation() != null && !document.definition().getImplementation().isBlank()
            ? document.definition().getImplementation() : extractClassName(document.text());
        filename = className + ".java";
      }
    } else {
      String className = document.definition() != null && document.definition().getImplementation() != null && !document.definition().getImplementation().isBlank()
          ? document.definition().getImplementation() : extractClassName(document.text());
      filename = className + ".java";
    }
    SimpleJavaFileObject file = new SimpleJavaFileObject(URI.create("string:///" + filename), JavaFileObject.Kind.SOURCE) {
      @Override public CharSequence getCharContent(boolean ignoreEncodingErrors) { return document.text(); }
    };

    List<String> options = new ArrayList<>();
    List<String> classpathEntries = this.buildCompilerClasspath();
    if (!classpathEntries.isEmpty()) {
      options.add("-classpath");
      options.add(String.join(java.io.File.pathSeparator, classpathEntries));
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
          diag.getMessage(java.util.Locale.ENGLISH)
        ));
      }
    }
    ParsedDocument result = new ParsedDocument(document.uri(), diagnostics.isEmpty(), List.copyOf(diagnostics), List.of());
    if (this.parseCache.size() > MAX_PARSE_CACHE_SIZE) {
      this.parseCache.clear();
    }
    this.parseCache.put(cacheKey, result);
    return result;
  }

  private Map<String, Class<?>> variables(Document document, String source) {
    Map<String, Class<?>> variables = new LinkedHashMap<>(this.importedTypes(source));
    ScriptDefinition definition = document == null ? null : document.definition();
    this.hostType(definition, source).ifPresent(type -> variables.put("host", type));
    variables.put("context", ScriptContext.class);
    variables.put("environment", Environment.class);
    variables.put("globals", ScriptGlobals.class);

    if (source == null || source.isBlank()) return variables;

    Set<String> keywords = KEYWORDS;

    // 1. var x = (Type) ...
    Matcher varCastMatcher = Pattern.compile("(?m)\\bvar\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*\\(\\s*([A-Za-z0-9_$]+)\\s*\\)").matcher(source);
    while (varCastMatcher.find()) {
      String varName = varCastMatcher.group(1);
      String typeName = varCastMatcher.group(2);
      if (!keywords.contains(varName)) {
        this.resolveType(typeName, source).ifPresent(type -> variables.put(varName, type));
      }
    }

    // 2. var x = new Type(...)
    Matcher varNewMatcher = Pattern.compile("(?m)\\bvar\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*new\\s+([A-Za-z0-9_$]+)\\s*\\(").matcher(source);
    while (varNewMatcher.find()) {
      String varName = varNewMatcher.group(1);
      String typeName = varNewMatcher.group(2);
      if (!keywords.contains(varName)) {
        this.resolveType(typeName, source).ifPresent(type -> variables.put(varName, type));
      }
    }

    // 3. var x = host() / environment() / context()
    Matcher varCallMatcher = Pattern.compile("(?m)\\bvar\\s+([A-Za-z_$][\\w$]*)\\s*=\\s*(host|environment|context)\\s*\\(\\s*\\)").matcher(source);
    while (varCallMatcher.find()) {
      String varName = varCallMatcher.group(1);
      String call = varCallMatcher.group(2);
      if (!keywords.contains(varName)) {
        if ("host".equals(call)) this.hostType(definition, source).ifPresent(type -> variables.put(varName, type));
        else if ("environment".equals(call)) variables.put(varName, Environment.class);
        else if ("context".equals(call)) variables.put(varName, ScriptContext.class);
      }
    }

    // 4. Type varName = ... or Type varName; or (Type varName, ...) or for (Type varName : ...)
    Matcher typedVarMatcher = Pattern.compile("(?m)\\b([A-Za-z0-9_$]+)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=[^;\\n]+|;|,|\\)|:)").matcher(source);
    while (typedVarMatcher.find()) {
      String typeName = typedVarMatcher.group(1);
      String varName = typedVarMatcher.group(2);
      if (!keywords.contains(varName) && !keywords.contains(typeName) && !"var".equals(typeName) && !"void".equals(typeName) && !"return".equals(typeName)) {
        this.resolveType(typeName, source).ifPresent(type -> variables.putIfAbsent(varName, type));
      }
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
      current = resolveMember(current, chain.get(index), source, this.workspace.classLoader(), definition);
      if (current == null) return null;
    }
    return current;
  }

  private ResolvedType resolveRoot(String root, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    if (root == null || root.isBlank()) return null;
    root = root.strip();

    // Check for cast: (Janitor)host() or (Janitor) host()
    Matcher castMatcher = Pattern.compile("^\\(\\s*([A-Za-z0-9_$]+)\\s*\\)\\s*(.*)$").matcher(root);
    if (castMatcher.matches()) {
      String castTypeName = castMatcher.group(1);
      Optional<Class<?>> castType = this.resolveType(castTypeName, source);
      if (castType.isPresent()) return new ResolvedType(castType.get(), null, false);
    }

    // Check for parenthesized expression: (expr)
    if (root.startsWith("(") && root.endsWith(")")) {
      return resolveRoot(root.substring(1, root.length() - 1).strip(), definition, variables, source);
    }

    if (root.endsWith("()")) {
      String methodName = root.substring(0, root.length() - 2);
      if (methodName.equals("host")) return this.hostType(definition, source).map(t -> new ResolvedType(t, null, false)).orElse(null);
      if (methodName.equals("context")) return new ResolvedType(ScriptContext.class, null, false);
      if (methodName.equals("environment")) return new ResolvedType(Environment.class, null, false);
      if (methodName.equals("globals")) return new ResolvedType(ScriptGlobals.class, null, false);
      if (methodName.equals("ui")) return new ResolvedType(ScriptUiOverlay.class, null, false);
      if (methodName.equals("camera")) return new ResolvedType(ICamera.class, null, false);

      ResolvedType scriptType = this.scriptType(definition, source);
      if (scriptType != null && scriptType.type() != null) {
        for (Method m : scriptType.type().getMethods()) {
          if (m.getName().equals(methodName)) {
            return new ResolvedType(m.getReturnType(), m.getGenericReturnType(), false);
          }
        }
      }
    }
    if (root.equals("this") || root.equals("super")) {
      ResolvedType scriptType = this.scriptType(definition, source);
      if (scriptType != null) return scriptType;
      return this.hostType(definition, source).map(t -> new ResolvedType(t, null, false)).orElse(null);
    }
    Class<?> varType = variables.get(root);
    if (varType != null) return new ResolvedType(varType, null, false);
    return this.resolveType(root, source).map(t -> new ResolvedType(t, null, true)).orElse(null);
  }

  private ResolvedType resolveMember(ResolvedType receiver, String memberCall, String source, ClassLoader loader, ScriptDefinition definition) {
    String memberName = memberCall.endsWith("()") ? memberCall.substring(0, memberCall.length() - 2) : memberCall;
    Class<?> type = receiver.type();
    if (ScriptContext.class.isAssignableFrom(type) || AbstractScript.class.isAssignableFrom(type)) {
      if (memberName.equals("host")) {
        return this.hostType(definition, source).map(t -> new ResolvedType(t, null, false)).orElse(new ResolvedType(Object.class, null, false));
      }
      if (memberName.equals("ui")) {
        return new ResolvedType(ScriptUiOverlay.class, null, false);
      }
      if (memberName.equals("camera")) {
        return new ResolvedType(ICamera.class, null, false);
      }
    }
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
    result.add(function("ui", ScriptUiOverlay.class.getSimpleName(), "Returns the ScriptUiOverlay service for floating text and banners."));
    result.add(function("camera", "ICamera", "Returns the active camera controller for panning, zooming, and shaking."));

    List<TextEdit> gameEdits = importedFqns != null && importedFqns.contains("de.gurkenlabs.litiengine.Game") ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import de.gurkenlabs.litiengine.Game;\n"));
    result.add(new Completion("Game", CompletionKind.CLASS, "de.gurkenlabs.litiengine.Game",
      "The central LITIENGINE static entry point for game systems.", "Game", "Game", List.of(), gameEdits));
  }

  private static void addMembers(List<Completion> result, ResolvedType receiver) {
    if (receiver == null || receiver.type() == null) return;
    CachedMembers cached = MEMBER_CACHE.computeIfAbsent(receiver.type(), JavaLanguageService::buildMembers);
    result.addAll(receiver.staticOnly() ? cached.staticOnly() : cached.all());
  }

  private static CachedMembers buildMembers(Class<?> type) {
    List<Completion> all = new ArrayList<>();
    List<Completion> statics = new ArrayList<>();
    String owner = simpleName(type.getName());

    Set<String> seenMethods = new HashSet<>();
    Set<String> seenFields = new HashSet<>();

    for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
      for (Method method : c.getDeclaredMethods()) {
        int mod = method.getModifiers();
        if (Modifier.isPrivate(mod) || method.isSynthetic()) continue;
        String sigKey = method.getName() + Arrays.toString(method.getParameterTypes());
        if (!seenMethods.add(sigKey)) continue;

        List<Parameter> parameters = extractParameters(method);
        String detail = method.getName() + "(" + String.join(", ", parameters.stream()
          .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
        String docs = "```java\n" + simpleName(method.getGenericReturnType().getTypeName()) + " " + owner + "." + detail + "\n```\n\n"
          + "Declared in `" + method.getDeclaringClass().getName() + "`";
        String methodDoc = ScriptDocumentation.getMethodDoc(method.getName());
        if (!methodDoc.isBlank()) {
          docs += "\n\n" + methodDoc;
        }
        String insertText;
        if (parameters.isEmpty()) {
          insertText = method.getName() + "()";
        } else {
          List<String> placeholders = new ArrayList<>();
          for (int i = 0; i < parameters.size(); i++) {
            placeholders.add("${" + (i + 1) + ":" + parameters.get(i).name() + "}");
          }
          insertText = method.getName() + "(" + String.join(", ", placeholders) + ")";
        }
        Completion completion = new Completion(method.getName(), CompletionKind.METHOD, detail, docs, insertText,
          method.getGenericReturnType().getTypeName(), parameters, List.of());
        all.add(completion);
        if (Modifier.isStatic(method.getModifiers())) {
          statics.add(completion);
        }
      }

      for (Field field : c.getDeclaredFields()) {
        int mod = field.getModifiers();
        if (Modifier.isPrivate(mod) || field.isSynthetic()) continue;
        if (!seenFields.add(field.getName())) continue;

        String docs = "```java\n" + simpleName(field.getGenericType().getTypeName()) + " " + owner + "." + field.getName() + "\n```\n\n"
          + "Declared in `" + field.getDeclaringClass().getName() + "`";
        Completion completion = new Completion(field.getName(), CompletionKind.FIELD, field.getGenericType().getTypeName(), docs,
          field.getName(), field.getGenericType().getTypeName(), List.of(), List.of());
        all.add(completion);
        if (Modifier.isStatic(field.getModifiers())) {
          statics.add(completion);
        }
      }
    }
    return new CachedMembers(List.copyOf(all), List.copyOf(statics));
  }


  private record DeclaredScriptField(int line, String indentation, String type, String name, boolean hasScriptProperty, boolean hasAnnotationParams, int annotationLine, Range annotationRange) {}

  private static List<DeclaredScriptField> scanDeclaredFields(String source) {
    List<DeclaredScriptField> fields = new ArrayList<>();
    String[] lines = source.split("\r?\n", -1);
    Pattern fieldPattern = Pattern.compile(
      "^(\\s*)(?:(?:public|protected|private|static|final|transient|volatile)\\s+)*([A-Za-z0-9_$<>]+(?:\\[\\])?)\\s+([A-Za-z_$][\\w$]*)\\s*(?:=[^;]+)?;");
    Set<String> keywords = Set.of("if", "while", "for", "switch", "catch", "new", "super", "this", "return", "class", "interface", "enum", "record", "import", "package");

    for (int i = 0; i < lines.length; i++) {
      String line = lines[i];
      if (line.contains("(") || line.contains(" class ") || line.contains(" interface ") || line.contains(" enum ") || line.contains(" record ")) continue;
      Matcher m = fieldPattern.matcher(line);
      if (m.find()) {
        String indent = m.group(1);
        String type = m.group(2);
        String name = m.group(3);
        if (keywords.contains(name) || keywords.contains(type)) continue;

        boolean hasProp = false;
        boolean hasParams = false;
        int annotLine = -1;
        Range annotRange = null;

        for (int k = i - 1; k >= Math.max(0, i - 4); k--) {
          String prev = lines[k].trim();
          if (prev.isEmpty()) continue;
          if (prev.startsWith("@")) {
            annotLine = k;
            int atIdx = lines[k].indexOf('@');
            annotRange = new Range(new Position(k, atIdx), new Position(k, lines[k].length()));
            if (prev.contains("ScriptProperty")) {
              hasProp = true;
              hasParams = prev.contains("name") || prev.contains("description") || prev.contains("value");
            }
          } else {
            break;
          }
        }
        if (line.trim().startsWith("@")) {
          int atIdx = line.indexOf('@');
          int afterAnnot = line.indexOf(' ', atIdx);
          if (afterAnnot > atIdx) {
            String annotText = line.substring(atIdx, afterAnnot);
            annotLine = i;
            annotRange = new Range(new Position(i, atIdx), new Position(i, afterAnnot));
            if (annotText.contains("ScriptProperty")) {
              hasProp = true;
              hasParams = annotText.contains("name") || annotText.contains("description");
            }
          }
        }

        fields.add(new DeclaredScriptField(i, indent, type, name, hasProp, hasParams, annotLine, annotRange));
      }
    }
    return fields;
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
    return this.hostType(definition, null);
  }

  private Optional<Class<?>> hostType(ScriptDefinition definition, String source) {
    if (source != null && !source.isBlank()) {
      Matcher genericMatcher = Pattern.compile("(?m)\\bextends\\s+(?:EntityScript|CreatureScript|AbstractScript|PropScript)\\s*<\\s*([A-Za-z0-9_$]+)\\s*>").matcher(source);
      if (genericMatcher.find()) {
        Optional<Class<?>> fromExtends = this.resolveType(genericMatcher.group(1), source);
        if (fromExtends.isPresent()) return fromExtends;
      }
      if (Pattern.compile("(?m)\\bextends\\s+CreatureScript\\b").matcher(source).find()) {
        return Optional.of(de.gurkenlabs.litiengine.entities.Creature.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EnvironmentScript\\b").matcher(source).find()) {
        return Optional.of(de.gurkenlabs.litiengine.environment.Environment.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+GameScript\\b").matcher(source).find()) {
        return Optional.of(de.gurkenlabs.litiengine.Game.class);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EntityScript\\b").matcher(source).find()) {
        return Optional.of(de.gurkenlabs.litiengine.entities.Entity.class);
      }
      Matcher targetMatcher = Pattern.compile("(?m)@ScriptInfo\\s*\\([^)]*target\\s*=\\s*([A-Za-z0-9_$]+)\\.class").matcher(source);
      if (targetMatcher.find()) {
        Optional<Class<?>> fromAnnotation = this.resolveType(targetMatcher.group(1), source);
        if (fromAnnotation.isPresent()) return fromAnnotation;
      }
    }
    if (definition != null && definition.getTargetType() != null && !definition.getTargetType().isBlank()) {
      try {
        return Optional.of(Class.forName(definition.getTargetType(), false, this.workspace.classLoader()));
      } catch (ClassNotFoundException | LinkageError ignored) {
        Optional<Class<?>> resolved = this.resolveType(definition.getTargetType(), "");
        if (resolved.isPresent()) return resolved;
      }
    }
    if (definition != null && definition.getHost() != null) {
      return switch (definition.getHost()) {
        case ENTITY -> Optional.of(de.gurkenlabs.litiengine.entities.Creature.class);
        case ENVIRONMENT -> Optional.of(de.gurkenlabs.litiengine.environment.Environment.class);
        case GAME -> Optional.of(de.gurkenlabs.litiengine.Game.class);
      };
    }
    return Optional.empty();
  }

  private ResolvedType scriptType(ScriptDefinition definition, String source) {
    if (source != null && !source.isBlank()) {
      if (Pattern.compile("(?m)\\bextends\\s+CreatureScript\\b").matcher(source).find()) {
        return new ResolvedType(CreatureScript.class, null, false);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EnvironmentScript\\b").matcher(source).find()) {
        return new ResolvedType(EnvironmentScript.class, null, false);
      }
      if (Pattern.compile("(?m)\\bextends\\s+EntityScript\\b").matcher(source).find()) {
        return new ResolvedType(EntityScript.class, null, false);
      }
      if (Pattern.compile("(?m)\\bextends\\s+GameScript\\b").matcher(source).find()) {
        return new ResolvedType(GameScript.class, null, false);
      }
      if (Pattern.compile("(?m)\\bextends\\s+AbstractScript\\b").matcher(source).find()) {
        return new ResolvedType(AbstractScript.class, null, false);
      }
    }
    if (definition != null && definition.getHost() != null) {
      return switch (definition.getHost()) {
        case ENTITY -> new ResolvedType(CreatureScript.class, null, false);
        case ENVIRONMENT -> new ResolvedType(EnvironmentScript.class, null, false);
        case GAME -> new ResolvedType(GameScript.class, null, false);
      };
    }
    return new ResolvedType(AbstractScript.class, null, false);
  }

  private ResolvedType scriptType(ScriptDefinition definition) {
    return this.scriptType(definition, null);
  }

  private List<String> buildCompilerClasspath() {
    List<String> classpathEntries = new ArrayList<>();
    String processClasspath = System.getProperty("java.class.path");
    if (processClasspath != null && !processClasspath.isBlank()) {
      classpathEntries.addAll(List.of(processClasspath.split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))));
    }
    if (this.workspace.projectClasspath() != null) {
      this.workspace.projectClasspath().stream().filter(Objects::nonNull).map(Path::toString).forEach(classpathEntries::add);
    }
    for (ClassLoader cl = this.workspace.classLoader(); cl != null; cl = cl.getParent()) {
      if (cl instanceof java.net.URLClassLoader ucl) {
        for (java.net.URL url : ucl.getURLs()) {
          try {
            classpathEntries.add(Path.of(url.toURI()).toString());
          } catch (Exception ignored) {
            String path = url.getPath();
            if (path != null && !path.isBlank()) {
              classpathEntries.add(path);
            }
          }
        }
      }
    }
    if (this.workspace.projectRoot() != null) {
      List<Path> commonDirs = List.of(
        Path.of("build", "classes", "java", "main"),
        Path.of("build", "classes", "kotlin", "main"),
        Path.of("build", "classes", "groovy", "main"),
        Path.of("target", "classes"),
        Path.of("bin", "main"),
        Path.of("bin"),
        Path.of("out", "production", "main"),
        Path.of("out", "production", "classes")
      );
      for (Path commonDir : commonDirs) {
        Path resolved = this.workspace.projectRoot().resolve(commonDir);
        if (Files.isDirectory(resolved)) {
          classpathEntries.add(resolved.toAbsolutePath().toString());
        }
      }
    }
    return classpathEntries.stream()
      .filter(entry -> entry != null && !entry.isBlank())
      .distinct()
      .toList();
  }

  private Map<String, Class<?>> importedTypes(String source) {
    if (source == null || source.isBlank()) return Map.of();
    Map<String, Class<?>> imports = new LinkedHashMap<>();
    Matcher matcher = IMPORT.matcher(source);
    ClassLoader loader = this.workspace.classLoader();
    while (matcher.find()) {
      String fqn = matcher.group(1);
      String alias = matcher.group(2);
      if (fqn.endsWith(".*")) {
        String pkg = fqn.substring(0, fqn.length() - 2);
        for (Class<?> type : EngineTypeCatalog.typesInPackage(pkg, loader)) {
          imports.put(type.getSimpleName(), type);
        }
      } else {
        try {
          Class<?> type = Class.forName(fqn, false, loader);
          imports.put(alias == null ? type.getSimpleName() : alias, type);
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

  private static int findUnclosedParen(String text) {
    int depth = 0;
    for (int i = text.length() - 1; i >= 0; i--) {
      char c = text.charAt(i);
      if (c == ')') depth++;
      else if (c == '(') {
        if (depth == 0) return i;
        depth--;
      }
    }
    return -1;
  }

  private static int countTopLevelCommas(String argList) {
    int depth = 0;
    int count = 0;
    for (int i = 0; i < argList.length(); i++) {
      char c = argList.charAt(i);
      if (c == '(' || c == '[' || c == '{') depth++;
      else if (c == ')' || c == ']' || c == '}') depth--;
      else if (c == ',' && depth == 0) count++;
    }
    return count;
  }

  private record CallContext(String methodName, int argIndex, String currentArgPrefix, ResolvedType receiverType) {}

  private Optional<CallContext> extractCallContext(String prefix, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    int openParen = findUnclosedParen(prefix);
    if (openParen < 0) return Optional.empty();
    String callPrefix = prefix.substring(0, openParen).trim();
    Matcher matcher = Pattern.compile("([A-Za-z_$][\\w$]*)$").matcher(callPrefix);
    if (!matcher.find()) return Optional.empty();
    String methodName = matcher.group(1);
    String argList = prefix.substring(openParen + 1);
    int argIndex = countTopLevelCommas(argList);
    String currentArgPrefix = argList.contains(",") ? argList.substring(argList.lastIndexOf(',') + 1).trim() : argList.trim();
    String receiverPrefix = callPrefix.substring(0, matcher.start(1));
    String receiverExpr = receiverExpression(receiverPrefix);
    ResolvedType receiverType = receiverExpr == null ? this.scriptType(definition, source)
      : this.resolveExpression(receiverExpr, definition, variables, source);
    if (receiverType == null || receiverType.type() == null) return Optional.empty();
    return Optional.of(new CallContext(methodName, argIndex, currentArgPrefix, receiverType));
  }

  private void addParameterCompletions(List<Completion> result, String prefix, ScriptDefinition definition, Map<String, Class<?>> variables, String source, Set<String> importedFqns, int importInsertLine) {
    Optional<CallContext> callCtxOpt = extractCallContext(prefix, definition, variables, source);
    if (callCtxOpt.isEmpty()) return;
    CallContext ctx = callCtxOpt.get();

    List<Method> matchingMethods = Arrays.stream(ctx.receiverType().type().getMethods())
      .filter(m -> m.getName().equals(ctx.methodName()))
      .sorted(Comparator.comparingInt(Method::getParameterCount))
      .toList();

    // 1. If at start of argument list: offer full parameter placeholder snippets for each overload
    if (ctx.argIndex() == 0 && ctx.currentArgPrefix().isEmpty()) {
      for (Method m : matchingMethods) {
        if (m.getParameterCount() > 0) {
          List<Parameter> params = extractParameters(m);
          List<String> placeholders = new ArrayList<>();
          List<String> labels = new ArrayList<>();
          for (int i = 0; i < params.size(); i++) {
            labels.add(params.get(i).name());
            placeholders.add("${" + (i + 1) + ":" + params.get(i).name() + "}");
          }
          String label = String.join(", ", labels);
          String snippet = String.join(", ", placeholders);
          String detail = ctx.methodName() + "(" + label + ") parameters";
          result.add(new Completion(label, CompletionKind.SNIPPET, detail,
            "Inserts parameter placeholders for `" + formatMethodSignature(m) + "`",
            snippet, simpleName(m.getGenericReturnType().getTypeName()), List.of(), List.of()));
        }
      }
    }

    // 2. Context-aware value suggestions for the active argument at argIndex:
    for (Method m : matchingMethods) {
      if (m.getParameterCount() > ctx.argIndex()) {
        Class<?> paramType = m.getParameterTypes()[ctx.argIndex()];

        if (paramType == java.awt.Color.class) {
          List<TextEdit> colorEdits = importedFqns.contains("java.awt.Color") ? List.of()
            : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)), "import java.awt.Color;\n"));
          for (String cName : List.of("RED", "GREEN", "BLUE", "YELLOW", "WHITE", "BLACK", "ORANGE", "CYAN", "MAGENTA", "GRAY", "DARK_GRAY", "LIGHT_GRAY", "PINK")) {
            result.add(new Completion("Color." + cName, CompletionKind.FIELD, "Color", "Standard Color constant", "Color." + cName, "Color", List.of(), colorEdits));
          }
          result.add(new Completion("new Color(r, g, b)", CompletionKind.SNIPPET, "Custom Color", "Creates a custom RGB color", "new Color(${1:255}, ${2:0}, ${3:0})", "Color", List.of(), colorEdits));
        } else if (paramType == de.gurkenlabs.litiengine.Direction.class) {
          for (String dName : List.of("UP", "DOWN", "LEFT", "RIGHT", "UNDEFINED")) {
            result.add(new Completion("Direction." + dName, CompletionKind.FIELD, "Direction", "Cardinal direction constant", "Direction." + dName, "Direction", List.of(), List.of()));
          }
        } else if (paramType == java.awt.geom.Point2D.class) {
          result.add(new Completion("host().getCenter()", CompletionKind.METHOD, "Point2D", "Center coordinate of host entity", "host().getCenter()", "Point2D", List.of(), List.of()));
          result.add(new Completion("host().getLocation()", CompletionKind.METHOD, "Point2D", "Location of host entity", "host().getLocation()", "Point2D", List.of(), List.of()));
          result.add(new Completion("new Point2D.Double(x, y)", CompletionKind.SNIPPET, "Point2D", "Creates a new Point2D coordinate", "new Point2D.Double(${1:0}, ${2:0})", "Point2D", List.of(), List.of()));
        } else if (de.gurkenlabs.litiengine.entities.IEntity.class.isAssignableFrom(paramType)) {
          result.add(new Completion("host()", CompletionKind.METHOD, simpleName(paramType.getName()), "The current host entity", "host()", simpleName(paramType.getName()), List.of(), List.of()));
        } else if (paramType == java.awt.Font.class) {
          result.add(new Completion("new Font(name, style, size)", CompletionKind.SNIPPET, "Font", "Creates a custom Font", "new Font(${1:\"Arial\"}, Font.PLAIN, ${2:12})", "Font", List.of(), List.of()));
        }

        for (var entry : variables.entrySet()) {
          if (paramType.isAssignableFrom(entry.getValue())) {
            result.add(new Completion(entry.getKey(), CompletionKind.VARIABLE, entry.getValue().getSimpleName(), "Local variable", entry.getKey(), entry.getValue().getSimpleName(), List.of(), List.of()));
          }
        }
      }
    }
  }

  private Optional<Class<?>> inferExpectedParameterType(String prefix, ScriptDefinition definition, Map<String, Class<?>> variables, String source) {
    return extractCallContext(prefix, definition, variables, source)
      .flatMap(ctx -> Arrays.stream(ctx.receiverType().type().getMethods())
        .filter(m -> m.getName().equals(ctx.methodName()) && m.getParameterCount() > ctx.argIndex())
        .map(m -> m.getParameterTypes()[ctx.argIndex()])
        .findFirst());
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

  private static Completion getCachedTypeCompletion(Class<?> type) {
    return TYPE_COMPLETION_CACHE.computeIfAbsent(type, t -> typeCompletion(t, false, List.of()));
  }

  private static Completion typeCompletion(Class<?> type) {
    return getCachedTypeCompletion(type);
  }

  private static Completion typeCompletionWithEdits(Class<?> type, boolean fullyQualified, List<TextEdit> additionalEdits) {
    if (!fullyQualified && additionalEdits.isEmpty()) {
      return getCachedTypeCompletion(type);
    }
    Completion base = getCachedTypeCompletion(type);
    return new Completion(
      base.label(),
      base.kind(),
      base.detail(),
      base.documentation(),
      fullyQualified ? type.getName() : base.insertText(),
      base.returnType(),
      base.parameters(),
      additionalEdits
    );
  }

  private static Completion typeCompletion(Class<?> type, boolean fullyQualified, List<TextEdit> additionalEdits) {
    String pkg = type.getPackage() == null ? "" : type.getPackage().getName();
    String docStr = ScriptDocumentation.get(type);
    String docs = "```java\n" + (fullyQualified ? type.getName() : type.getSimpleName()) + "\n```\n\n"
      + (docStr.isBlank() ? (pkg.isEmpty() ? "" : "`" + pkg + "`") : docStr);
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

  private static String formatParamName(java.lang.reflect.Parameter param, int index, int total, String methodName) {
    String rawName = param.getName();
    if (rawName != null && !rawName.startsWith("arg") && !rawName.isBlank()) {
      return rawName;
    }
    String typeName = simpleName(param.getParameterizedType().getTypeName());
    return switch (typeName) {
      case "String" -> {
        if (methodName.contains("Text") || methodName.contains("Message") || methodName.contains("Banner")) yield index == 0 ? "text" : "subtitle";
        if (methodName.contains("Sound") || methodName.contains("Audio")) yield "soundName";
        if (methodName.contains("Prop") || methodName.contains("Sprite")) yield "spriteSheet";
        yield "name";
      }
      case "Point2D" -> methodName.contains("Pan") || methodName.contains("Move") ? "target" : "location";
      case "Color" -> "color";
      case "Font" -> "font";
      case "IEntity", "Entity" -> "entity";
      case "Creature" -> "creature";
      case "Environment" -> "environment";
      case "Direction" -> "direction";
      case "Sound" -> "sound";
      case "Graphics2D" -> "g";
      case "int" -> {
        if (methodName.contains("Pan") || methodName.contains("Shake") || methodName.contains("Duration")) yield "durationTicks";
        if (methodName.contains("Zoom") || methodName.contains("Delay") || methodName.contains("Time") || methodName.contains("Text") || methodName.contains("Banner")) yield "durationMs";
        if (index == 0 && total >= 2) yield "x";
        if (index == 1 && total >= 2) yield "y";
        yield "value";
      }
      case "double", "float" -> {
        if (methodName.contains("Zoom")) yield "targetZoom";
        if (methodName.contains("Shake")) yield "intensity";
        if (methodName.contains("Velocity") || methodName.contains("Speed")) yield "velocity";
        if (methodName.contains("Angle")) yield "angleDegrees";
        if (index == 0 && total >= 2) yield "x";
        if (index == 1 && total >= 2) yield "y";
        yield "value";
      }
      case "boolean" -> "enabled";
      default -> {
        if (!typeName.isEmpty() && Character.isUpperCase(typeName.charAt(0))) {
          yield Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
        }
        yield "arg" + index;
      }
    };
  }

  private static List<Parameter> extractParameters(Method method) {
    java.lang.reflect.Parameter[] rawParams = method.getParameters();
    List<Parameter> parameters = new ArrayList<>();
    for (int i = 0; i < rawParams.length; i++) {
      String pName = formatParamName(rawParams[i], i, rawParams.length, method.getName());
      parameters.add(new Parameter(pName, rawParams[i].getParameterizedType().getTypeName()));
    }
    return parameters;
  }

  private static Signature signature(Method method) {
    List<Parameter> parameters = extractParameters(method);
    String label = simpleName(method.getGenericReturnType().getTypeName()) + " " + method.getName() + "(" + String.join(", ", parameters.stream()
      .map(parameter -> simpleName(parameter.type()) + " " + parameter.name()).toList()) + ")";
    return new Signature(label, method.getGenericReturnType().getTypeName(), parameters);
  }

  private static String formatMethodSignature(Method method) {
    return signature(method).label();
  }

  private void addAnnotationCompletions(List<Completion> result, String source, Set<String> importedFqns, int importInsertLine) {
    List<TextEdit> propEdits = (importedFqns.contains(ScriptProperty.class.getName()) || isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import " + ScriptProperty.class.getName() + ";\n"));

    result.add(new Completion("ScriptProperty", CompletionKind.PROPERTY, ScriptProperty.class.getName(),
      "Exports this field to the utiLITI inspector for live configuration and map persistence.",
      "ScriptProperty", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("@ScriptProperty", CompletionKind.PROPERTY, ScriptProperty.class.getName(),
      "Exports this field to the utiLITI inspector for live configuration and map persistence.",
      "@ScriptProperty", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("ScriptProperty(...)", CompletionKind.SNIPPET, "@ScriptProperty(name = \"...\", description = \"...\")",
      "Snippet for @ScriptProperty with configurable metadata attributes.",
      "ScriptProperty(name = \"${1:name}\", description = \"${2:description}\")", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("@ScriptProperty(...)", CompletionKind.SNIPPET, "@ScriptProperty(name = \"...\", description = \"...\")",
      "Snippet for @ScriptProperty with configurable metadata attributes.",
      "@ScriptProperty(name = \"${1:name}\", description = \"${2:description}\")", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("scriptproperty", CompletionKind.SNIPPET, "Property field template",
      "Generates an annotated @ScriptProperty field.",
      "@ScriptProperty\nprivate ${1:int} ${2:propertyName};\n", ScriptProperty.class.getName(), List.of(), propEdits));

    result.add(new Completion("prop", CompletionKind.SNIPPET, "Property field template",
      "Generates an annotated @ScriptProperty field.",
      "@ScriptProperty\nprivate ${1:int} ${2:propertyName};\n", ScriptProperty.class.getName(), List.of(), propEdits));

    List<TextEdit> infoEdits = (importedFqns.contains(ScriptInfo.class.getName()) || isPackageWildcardImported("de.gurkenlabs.litiengine.scripting", source)) ? List.of()
      : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
        "import " + ScriptInfo.class.getName() + ";\n"));

    result.add(new Completion("ScriptInfo", CompletionKind.CLASS, ScriptInfo.class.getName(),
      "Declares the script identifier, host type, and target entity class.",
      "ScriptInfo", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("@ScriptInfo", CompletionKind.CLASS, ScriptInfo.class.getName(),
      "Declares the script identifier, host type, and target entity class.",
      "@ScriptInfo", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("ScriptInfo(...)", CompletionKind.SNIPPET, "@ScriptInfo(id = \"...\", host = ...)",
      "Snippet for @ScriptInfo declaration.",
      "ScriptInfo(id = \"${1:id}\", host = ScriptHostType.${2|GAME,ENVIRONMENT,ENTITY|})", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("@ScriptInfo(...)", CompletionKind.SNIPPET, "@ScriptInfo(id = \"...\", host = ...)",
      "Snippet for @ScriptInfo declaration.",
      "@ScriptInfo(id = \"${1:id}\", host = ScriptHostType.${2|GAME,ENVIRONMENT,ENTITY|})", ScriptInfo.class.getName(), List.of(), infoEdits));

    result.add(new Completion("Override", CompletionKind.CLASS, "java.lang.Override",
      "Indicates that a method declaration is intended to override a method declaration in a supertype.",
      "Override", "java.lang.Override", List.of(), List.of()));

    result.add(new Completion("@Override", CompletionKind.CLASS, "java.lang.Override",
      "Indicates that a method declaration is intended to override a method declaration in a supertype.",
      "@Override", "java.lang.Override", List.of(), List.of()));

    result.add(new Completion("Deprecated", CompletionKind.CLASS, "java.lang.Deprecated",
      "Marks the annotated element as deprecated.",
      "Deprecated", "java.lang.Deprecated", List.of(), List.of()));

    result.add(new Completion("SuppressWarnings", CompletionKind.CLASS, "java.lang.SuppressWarnings",
      "Suppresses compiler warnings in the annotated element.",
      "SuppressWarnings(\"${1:all}\")", "java.lang.SuppressWarnings", List.of(), List.of()));

    for (Class<?> engineType : EngineTypeCatalog.publicTypes()) {
      if (engineType.isAnnotation() && !engineType.equals(ScriptProperty.class) && !engineType.equals(ScriptInfo.class)) {
        List<TextEdit> edits = (importedFqns.contains(engineType.getName()) || engineType.getPackageName().equals("java.lang") || isPackageWildcardImported(engineType.getPackageName(), source)) ? List.of()
          : List.of(new TextEdit(new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + engineType.getName() + ";\n"));
        result.add(typeCompletion(engineType, false, edits));
      }
    }

    for (Class<?> projectType : EngineTypeCatalog.projectTypes(this.workspace.classLoader())) {
      if (projectType.isAnnotation() && !importedFqns.contains(projectType.getName())) {
        List<TextEdit> edits = isPackageWildcardImported(projectType.getPackageName(), source) ? List.of()
          : List.of(new TextEdit(
            new Range(new Position(importInsertLine, 0), new Position(importInsertLine, 0)),
            "import " + projectType.getName() + ";\n"));
        result.add(typeCompletion(projectType, false, edits));
      }
    }
  }

  private void addAnnotationAttributeCompletions(List<Completion> result, String annotName, String paramsContent, String source) {
    int lastComma = paramsContent.lastIndexOf(',');
    String clause = lastComma >= 0 ? paramsContent.substring(lastComma + 1).strip() : paramsContent.strip();
    if (clause.contains("=")) {
      if (clause.startsWith("required") || clause.contains("required")) {
        result.add(new Completion("true", CompletionKind.KEYWORD, "boolean", "Required property", "true", "boolean", List.of(), List.of()));
        result.add(new Completion("false", CompletionKind.KEYWORD, "boolean", "Optional property", "false", "boolean", List.of(), List.of()));
      }
      return;
    }
    String word = currentWord(clause);

    if (annotName.equals("ScriptProperty") || annotName.endsWith(".ScriptProperty")) {
      List<Completion> attrs = List.of(
        new Completion("defaultValue", CompletionKind.PROPERTY, "String - Initial fallback value", "Initial fallback value when not overridden in utiLITI", "defaultValue = \"$1\"", "String", List.of(), List.of()),
        new Completion("name", CompletionKind.PROPERTY, "String - Inspector label", "Display label shown in the utiLITI inspector", "name = \"$1\"", "String", List.of(), List.of()),
        new Completion("description", CompletionKind.PROPERTY, "String - Help tooltip", "Help description tooltip shown on hover in utiLITI", "description = \"$1\"", "String", List.of(), List.of()),
        new Completion("category", CompletionKind.PROPERTY, "String - Inspector category", "Inspector section / grouping header (default: \"Script\")", "category = \"$1\"", "String", List.of(), List.of()),
        new Completion("min", CompletionKind.PROPERTY, "double - Minimum numeric bound", "Minimum numeric value boundary for sliders/spinners", "min = $1", "double", List.of(), List.of()),
        new Completion("max", CompletionKind.PROPERTY, "double - Maximum numeric bound", "Maximum numeric value boundary for sliders/spinners", "max = $1", "double", List.of(), List.of()),
        new Completion("unit", CompletionKind.PROPERTY, "String - Unit label", "Unit label shown next to the input (e.g. \"px\", \"sec\", \"%\")", "unit = \"$1\"", "String", List.of(), List.of()),
        new Completion("required", CompletionKind.PROPERTY, "boolean - Mandatory flag", "Flags the property as mandatory in the inspector", "required = true", "boolean", List.of(), List.of())
      );
      for (Completion attr : attrs) {
        if (word.isEmpty() || matchesPrefix(attr.label(), word)) {
          result.add(attr);
        }
      }
      return;
    }

    if (annotName.equals("ScriptInfo") || annotName.endsWith(".ScriptInfo")) {
      List<Completion> attrs = List.of(
        new Completion("name", CompletionKind.PROPERTY, "String - Script name", "Display name of the script", "name = \"$1\"", "String", List.of(), List.of()),
        new Completion("description", CompletionKind.PROPERTY, "String - Script description", "Summary description of what the script does", "description = \"$1\"", "String", List.of(), List.of()),
        new Completion("author", CompletionKind.PROPERTY, "String - Author", "Author or creator of the script", "author = \"$1\"", "String", List.of(), List.of()),
        new Completion("version", CompletionKind.PROPERTY, "String - Version", "Semantic version of the script", "version = \"$1\"", "String", List.of(), List.of()),
        new Completion("category", CompletionKind.PROPERTY, "String - Category", "Category grouping for the script", "category = \"$1\"", "String", List.of(), List.of())
      );
      for (Completion attr : attrs) {
        if (word.isEmpty() || matchesPrefix(attr.label(), word)) {
          result.add(attr);
        }
      }
      return;
    }

    this.resolveType(annotName, source).ifPresent(annotClass -> {
      if (annotClass.isAnnotation()) {
        for (Method m : annotClass.getDeclaredMethods()) {
          if (word.isEmpty() || matchesPrefix(m.getName(), word)) {
            String insert = m.getReturnType().equals(String.class) ? m.getName() + " = \"$1\"" : m.getName() + " = $1";
            result.add(new Completion(m.getName(), CompletionKind.PROPERTY, m.getReturnType().getSimpleName(),
              "Annotation attribute `" + m.getName() + "`", insert, m.getReturnType().getSimpleName(), List.of(), List.of()));
          }
        }
      }
    });
  }


  private static boolean isPackageWildcardImported(String pkg, String source) {
    if (pkg == null || pkg.isBlank() || source == null) return false;
    Matcher matcher = IMPORT.matcher(source);
    while (matcher.find()) {
      String fqn = matcher.group(1);
      if (fqn != null && fqn.equals(pkg + ".*")) return true;
    }
    return false;
  }

  private static int memberRank(Completion completion) {
    if (completion.kind() == CompletionKind.SNIPPET && completion.detail() != null && completion.detail().contains("parameters")) return -5;
    if (completion.label().startsWith("Color.") || completion.label().startsWith("Direction.")) return -4;
    if (completion.label().equals("host()")) return -3;
    if (completion.label().startsWith("@ScriptProperty") || completion.label().equals("prop")) return 10;
    if (completion.label().startsWith("@ScriptInfo")) return 11;
    return switch (completion.kind()) {
      case METHOD, FIELD, PROPERTY, VARIABLE -> 0;
      case SNIPPET -> 1;
      case KEYWORD -> 2;
      case CLASS, CONSTRUCTOR -> 3;
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
    int memberStart = end;
    while (memberStart > 0 && Character.isJavaIdentifierPart(text.charAt(memberStart - 1))) memberStart--;
    if (memberStart > 0 && text.charAt(memberStart - 1) == '.') end = memberStart;
    if (end <= 0 || text.charAt(end - 1) != '.') return null;

    int start = end - 1;
    int parenthesisDepth = 0;
    while (start > 0) {
      char current = text.charAt(start - 1);
      if (current == ')') {
        parenthesisDepth++;
        start--;
      } else if (current == '(') {
        if (parenthesisDepth == 0) break;
        parenthesisDepth--;
        start--;
      } else if (Character.isJavaIdentifierPart(current) || current == '.') {
        start--;
      } else {
        break;
      }
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

  @Override
  public List<TextEdit> rename(Document document, Position position, String newName) {
    if (document == null || document.text() == null || newName == null || newName.isBlank()) return List.of();
    String source = document.text();
    int off = offset(source, position);
    String targetWord = wordAt(source, off);
    if (targetWord.isBlank() || !targetWord.matches("[A-Za-z_$][\\w$]*") || KEYWORDS.contains(targetWord)) {
      return List.of();
    }

    String replacement = newName.trim();
    if (!replacement.matches("[A-Za-z_$][\\w$]*") || KEYWORDS.contains(replacement)) {
      return List.of();
    }

    List<TextEdit> edits = new ArrayList<>();
    String[] lines = source.split("\\R", -1);
    Pattern pattern = Pattern.compile("\\b" + Pattern.quote(targetWord) + "\\b");

    for (int l = 0; l < lines.length; l++) {
      String line = lines[l];
      Matcher matcher = pattern.matcher(line);
      while (matcher.find()) {
        int startCol = matcher.start();
        int endCol = matcher.end();
        edits.add(new TextEdit(
          new Range(new Position(l, startCol), new Position(l, endCol)),
          replacement
        ));
      }
    }
    return edits;
  }

  @Override
  public String format(Document document) {
    if (document == null || document.text() == null) return "";
    String source = document.text();
    if (source.isBlank()) return source;
    String[] lines = source.split("\\R", -1);
    StringBuilder result = new StringBuilder();
    int indentLevel = 0;
    boolean inBlockComment = false;
    boolean lastWasEmpty = false;

    for (String rawLine : lines) {
      String line = rawLine.stripTrailing();
      String code = line.strip();

      if (code.isEmpty()) {
        if (!lastWasEmpty && result.length() > 0) {
          result.append("\n");
          lastWasEmpty = true;
        }
        continue;
      }

      lastWasEmpty = false;

      if (inBlockComment) {
        result.append("  ".repeat(Math.max(0, indentLevel))).append(code).append("\n");
        if (code.contains("*/")) inBlockComment = false;
        continue;
      }

      if (code.startsWith("/*") && !code.contains("*/")) {
        inBlockComment = true;
      }

      int closingBracesAtStart = 0;
      for (int i = 0; i < code.length(); i++) {
        char c = code.charAt(i);
        if (c == '}') closingBracesAtStart++;
        else if (!Character.isWhitespace(c)) break;
      }

      int lineIndent = Math.max(0, indentLevel - closingBracesAtStart);
      String formattedCode = formatLineSpacing(code);

      result.append("  ".repeat(lineIndent)).append(formattedCode).append("\n");

      int openBraces = countOccurrences(code, '{');
      int closeBraces = countOccurrences(code, '}');
      indentLevel = Math.max(0, indentLevel + openBraces - closeBraces);
    }

    return result.toString().stripTrailing() + "\n";
  }

  private static String formatLineSpacing(String line) {
    if (line.startsWith("//") || line.startsWith("/*") || line.startsWith("*")) return line;
    return line.replaceAll("\\s*\\{", " {");
  }

  private static int countOccurrences(String str, char ch) {
    int count = 0;
    boolean inString = false;
    char quote = 0;
    for (int i = 0; i < str.length(); i++) {
      char c = str.charAt(i);
      if (inString) {
        if (c == quote && (i == 0 || str.charAt(i - 1) != '\\')) inString = false;
      } else if (c == '"' || c == '\'') {
        inString = true;
        quote = c;
      } else if (c == ch) {
        count++;
      }
    }
    return count;
  }

  private static String extractClassName(String source) {
    if (source == null || source.isBlank()) return "Script";
    var matcher = java.util.regex.Pattern.compile("(?m)^\\s*(?:public\\s+)?class\\s+([A-Za-z_$][\\w$]*)").matcher(source);
    return matcher.find() ? matcher.group(1) : "Script";
  }

  private record ParsedDocument(URI uri, boolean valid, List<ScriptDiagnostic> diagnostics, List<Symbol> symbols) {}
  private record ResolvedType(Class<?> type, Type genericType, boolean staticOnly) {}
}
