package de.gurkenlabs.litiengine.scripting.groovy;

import de.gurkenlabs.litiengine.scripting.CompiledScript;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptDiagnostic;
import de.gurkenlabs.litiengine.scripting.ScriptException;
import de.gurkenlabs.litiengine.scripting.ScriptInstance;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import de.gurkenlabs.litiengine.scripting.ScriptProvider;
import groovy.lang.GroovyClassLoader;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import org.codehaus.groovy.control.MultipleCompilationErrorsException;
import org.codehaus.groovy.control.messages.Message;
import org.codehaus.groovy.control.messages.SyntaxErrorMessage;

/** Compiles trusted Groovy project scripts against the public LITIENGINE API. */
public final class GroovyScriptProvider implements ScriptProvider {
  @Override
  public String language() {
    return "groovy";
  }

  @Override
  public Optional<ScriptLanguageService> createLanguageService(ScriptLanguageService.Workspace workspace) {
    return Optional.of(new GroovyLanguageService(workspace));
  }

  @Override
  public CompiledScript compile(ScriptDefinition definition, URL source, ClassLoader parent) throws ScriptException {
    if (source == null) throw new ScriptException("A Groovy source URL is required.");
    GroovyClassLoader classLoader = new GroovyClassLoader(parent);
    try {
      String text;
      try (var stream = source.openStream()) {
        text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      }
      Class<?> parsedType = classLoader.parseClass(text, source.toExternalForm());
      Class<?> type = parsedType.getName().equals(definition.getImplementation())
        ? parsedType : classLoader.loadClass(definition.getImplementation());
      Class<? extends ScriptInstance> scriptType = type.asSubclass(ScriptInstance.class);
      scriptType.getConstructor();
      return new GroovyCompiledScript(definition, classLoader, scriptType);
    } catch (MultipleCompilationErrorsException e) {
      close(classLoader);
      throw new ScriptException("Groovy compilation failed for " + definition.getId() + ".", e, diagnostics(definition, e));
    } catch (IOException | ClassNotFoundException | ClassCastException | NoSuchMethodException e) {
      close(classLoader);
      throw new ScriptException("Could not compile Groovy script " + definition.getId() + ".", e);
    }
  }

  private static List<ScriptDiagnostic> diagnostics(ScriptDefinition definition, MultipleCompilationErrorsException exception) {
    List<ScriptDiagnostic> diagnostics = new ArrayList<>();
    for (Message message : exception.getErrorCollector().getErrors()) {
      if (message instanceof SyntaxErrorMessage syntaxMessage) {
        var syntax = syntaxMessage.getCause();
        diagnostics.add(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, definition.getId(), definition.getSource(),
          syntax.getStartLine(), syntax.getStartColumn(), syntax.getOriginalMessage()));
      }
    }
    if (diagnostics.isEmpty()) {
      diagnostics.add(new ScriptDiagnostic(ScriptDiagnostic.Severity.ERROR, definition.getId(), definition.getSource(), -1, -1,
        exception.getMessage()));
    }
    return List.copyOf(diagnostics);
  }

  private static void close(GroovyClassLoader classLoader) {
    try {
      classLoader.close();
    } catch (IOException ignored) {
      // The failed generation has no live instances; there is nothing else to recover here.
    }
  }

  private record GroovyCompiledScript(
    ScriptDefinition definition,
    GroovyClassLoader classLoader,
    Class<? extends ScriptInstance> implementationType) implements CompiledScript {

    @Override
    public ScriptInstance create() throws ScriptException {
      try {
        return this.implementationType.getConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new ScriptException("Could not instantiate Groovy script " + this.definition.getId() + ".", e);
      }
    }

    @Override
    public void close() throws IOException {
      this.classLoader.close();
    }
  }
}
