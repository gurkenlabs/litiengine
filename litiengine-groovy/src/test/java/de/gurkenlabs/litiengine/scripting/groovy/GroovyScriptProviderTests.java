package de.gurkenlabs.litiengine.scripting.groovy;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.entities.Creature;
import de.gurkenlabs.litiengine.scripting.GameScript;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptException;
import de.gurkenlabs.litiengine.scripting.ScriptHostType;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class GroovyScriptProviderTests {
  @TempDir Path tempDirectory;

  @Test
  void compilesAProjectScriptIntoFreshInstances() throws Exception {
    Path source = this.tempDirectory.resolve("ExampleScript.groovy");
    Files.writeString(source, """
      import de.gurkenlabs.litiengine.scripting.GameScript
      class ExampleScript extends GameScript { }
      """);
    ScriptDefinition definition = new ScriptDefinition("example", "groovy", source.toString(), "ExampleScript", ScriptHostType.GAME);

    try (var compiled = new GroovyScriptProvider().compile(definition, source.toUri().toURL(), getClass().getClassLoader())) {
      assertEquals(GameScript.class, compiled.implementationType().getSuperclass());
      assertInstanceOf(GameScript.class, compiled.create());
      assertFalse(compiled.create() == compiled.create());
    }
  }

  @Test
  void reportsSourceLocationsForCompilationErrors() throws Exception {
    Path source = this.tempDirectory.resolve("Broken.groovy");
    Files.writeString(source, "class Broken extends");
    ScriptDefinition definition = new ScriptDefinition("broken", "groovy", source.toString(), "Broken", ScriptHostType.GAME);

    ScriptException exception = assertThrows(ScriptException.class,
      () -> new GroovyScriptProvider().compile(definition, source.toUri().toURL(), getClass().getClassLoader()));

    assertFalse(exception.getDiagnostics().isEmpty());
    assertEquals("broken", exception.getDiagnostics().getFirst().scriptId());
  }

  @Test
  void completesMembersOfTheConcreteEntityHost() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    String source = "class Example { void update() { host(). } }";
    int column = source.indexOf("host().") + "host().".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      assertTrue(service.complete(document, new ScriptLanguageService.Position(0, column)).stream()
        .anyMatch(completion -> completion.label().equals("getLocation")));
    }
  }

  @Test
  void completesAllPublicEngineTypesAfterNew() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    String source = "class Example { void update() { host().addListener(new  } }";
    int column = source.indexOf("new ") + "new ".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      assertTrue(service.complete(document, new ScriptLanguageService.Position(0, column)).stream()
        .anyMatch(completion -> completion.label().equals("EntityListener")
          && completion.insertText().equals("de.gurkenlabs.litiengine.entities.EntityListener")));
    }
  }
  void completesGroovyPropertiesOnEntityHost() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    String source = "class Example { void update() { host(). } }";
    int column = source.indexOf("host().") + "host().".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      List<ScriptLanguageService.Completion> completions = service.complete(document, new ScriptLanguageService.Position(0, column));
      assertTrue(completions.stream().anyMatch(completion -> completion.label().equals("center")
        && completion.kind() == ScriptLanguageService.CompletionKind.PROPERTY));
      assertTrue(completions.stream().anyMatch(completion -> completion.label().equals("velocity")
        && completion.kind() == ScriptLanguageService.CompletionKind.PROPERTY));
    }
  }

  @Test
  void completesUnqualifiedCustomTargetType() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    definition.setTargetType("Creature");
    String source = "class Example { void update() { host(). } }";
    int column = source.indexOf("host().") + "host().".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      assertTrue(service.complete(document, new ScriptLanguageService.Position(0, column)).stream()
        .anyMatch(completion -> completion.label().equals("getCenter")));
    }
  }

  @Test
  void completesGenericClassInferredReturnTypes() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    String line0 = "import de.gurkenlabs.litiengine.entities.behavior.DefaultScriptBehaviorController";
    String line1 = "class Example { void update() { host().getController(DefaultScriptBehaviorController.class). } }";
    String source = line0 + "\n" + line1;
    int column = line1.indexOf("class).") + "class).".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      assertTrue(service.complete(document, new ScriptLanguageService.Position(1, column)).stream()
        .anyMatch(completion -> completion.label().equals("getScriptController")));
    }
  }

  @Test
  void completesScriptLocalMethods() {
    ScriptDefinition definition = new ScriptDefinition("creature", "groovy", "Example.groovy", "Example", ScriptHostType.ENTITY);
    definition.setTargetType(Creature.class.getName());
    String source = "class Example { protected void scream() {} void update() { scr } }";
    int column = source.indexOf("scr") + "scr".length();
    ScriptLanguageService.Document document = new ScriptLanguageService.Document(this.tempDirectory.resolve("Example.groovy").toUri(),
      source, 1, definition);

    try (ScriptLanguageService service = new GroovyScriptProvider().createLanguageService(
      new ScriptLanguageService.Workspace(this.tempDirectory, getClass().getClassLoader(), java.util.Map.of())).orElseThrow()) {
      List<ScriptLanguageService.Completion> completions = service.complete(document, new ScriptLanguageService.Position(0, column));
      assertTrue(completions.stream().anyMatch(completion -> completion.label().equals("scream")
        && completion.kind() == ScriptLanguageService.CompletionKind.METHOD), "Should complete script-declared method 'scream()'");
    }
  }
}
