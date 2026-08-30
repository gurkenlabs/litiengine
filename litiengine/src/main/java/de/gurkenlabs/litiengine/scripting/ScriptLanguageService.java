package de.gurkenlabs.litiengine.scripting;

import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Editor-independent semantic tooling for a scripting language.
 *
 * <p>Implementations analyze source text only and must never execute a script. Positions are
 * zero-based so the API maps directly to editor and language-server protocols.
 */
public interface ScriptLanguageService extends AutoCloseable {
  Analysis analyze(Document document);

  List<Completion> complete(Document document, Position position);

  default Optional<Hover> hover(Document document, Position position) {
    return Optional.empty();
  }

  default Optional<SignatureHelp> signatureHelp(Document document, Position position) {
    return Optional.empty();
  }

  default Optional<Location> definition(Document document, Position position) {
    return Optional.empty();
  }

  default List<CodeAction> codeActions(Document document, Range range, List<ScriptDiagnostic> diagnostics) {
    return List.of();
  }

  default String format(Document document) {
    return document.text();
  }

  default List<TextEdit> rename(Document document, Position position, String newName) {
    return List.of();
  }

  @Override
  default void close() {}

  record Workspace(Path projectRoot, ClassLoader classLoader, java.util.Collection<Path> projectClasspath, Map<String, String> options) {
    public Workspace(Path projectRoot, ClassLoader classLoader, Map<String, String> options) {
      this(projectRoot, classLoader, List.of(), options);
    }

    public Workspace {
      projectClasspath = projectClasspath == null ? List.of() : List.copyOf(projectClasspath);
      options = options == null ? Map.of() : Map.copyOf(options);
    }
  }

  record Document(URI uri, String text, long version, ScriptDefinition definition) {
    public Document {
      text = text == null ? "" : text;
      definition = definition == null ? null : new ScriptDefinition(definition);
    }
  }

  record Position(int line, int column) {
    public Position {
      if (line < 0 || column < 0) throw new IllegalArgumentException("Script positions must not be negative.");
    }
  }

  record Range(Position start, Position end) {}

  record Analysis(List<ScriptDiagnostic> diagnostics, List<Symbol> symbols, List<SemanticToken> tokens) {
    public Analysis {
      diagnostics = diagnostics == null ? List.of() : List.copyOf(diagnostics);
      symbols = symbols == null ? List.of() : List.copyOf(symbols);
      tokens = tokens == null ? List.of() : List.copyOf(tokens);
    }
  }

  enum CompletionKind { CLASS, CONSTRUCTOR, METHOD, FIELD, PROPERTY, VARIABLE, KEYWORD, SNIPPET }

  record Completion(String label, CompletionKind kind, String detail, String documentation, String insertText,
                    String returnType, List<Parameter> parameters, List<TextEdit> additionalEdits) {
    public Completion {
      parameters = parameters == null ? List.of() : List.copyOf(parameters);
      additionalEdits = additionalEdits == null ? List.of() : List.copyOf(additionalEdits);
    }
  }

  record Parameter(String name, String type) {}

  record Hover(String markdown, Range range) {}

  record SignatureHelp(List<Signature> signatures, int activeSignature, int activeParameter) {
    public SignatureHelp {
      signatures = signatures == null ? List.of() : List.copyOf(signatures);
    }
  }

  record Signature(String label, String documentation, List<Parameter> parameters) {
    public Signature {
      parameters = parameters == null ? List.of() : List.copyOf(parameters);
    }
  }

  enum SymbolKind { CLASS, FIELD, METHOD, CONSTRUCTOR, PROPERTY, VARIABLE }

  record Symbol(String name, SymbolKind kind, String detail, Range range, List<Symbol> children) {
    public Symbol {
      children = children == null ? List.of() : List.copyOf(children);
    }
  }

  record Location(URI uri, Range range) {}

  record SemanticToken(Range range, String type, List<String> modifiers) {
    public SemanticToken {
      modifiers = modifiers == null ? List.of() : List.copyOf(modifiers);
    }
  }

  record TextEdit(Range range, String text) {}

  record CodeAction(String title, String kind, List<TextEdit> edits) {
    public CodeAction {
      edits = edits == null ? List.of() : List.copyOf(edits);
    }
  }
}
