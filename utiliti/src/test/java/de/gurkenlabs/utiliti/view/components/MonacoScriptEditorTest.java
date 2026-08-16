package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.JTextField;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class MonacoScriptEditorTest {
  @Test
  void classUrisResolveToFullyQualifiedNames() {
    assertEquals("de.gurkenlabs.game.Janitor",
      MonacoScriptEditor.classNameFromUri(URI.create("class:///de/gurkenlabs/game/Janitor.java")));
  }

  @Test
  void typeDefinitionNavigationTargetsTheDeclaration(@TempDir Path tempDir) throws Exception {
    Path source = tempDir.resolve("Janitor.java");
    Files.writeString(source, "package game;\n\npublic class Janitor {\n}\n");

    ScriptLanguageService.Position position = MonacoScriptEditor.typeDeclarationPosition(
      source, "game.Janitor", new ScriptLanguageService.Position(0, 0));

    assertEquals(new ScriptLanguageService.Position(2, 13), position);
  }

  @Test
  void readinessProbeWaitsForTheEditorReceiver() {
    String probe = MonacoScriptEditor.readinessProbeScript();

    assertTrue(probe.contains("window.utilitiEditor"));
    assertTrue(probe.contains("typeof window.utilitiEditor.receive === 'function'"));
  }

  @Test
  void browserRelinquishesFocusWhenANativeControlBecomesFocused() {
    JPanel browserHost = new JPanel();
    JPanel browserChild = new JPanel();
    browserHost.add(browserChild);

    assertFalse(MonacoScriptEditor.shouldRelinquishEditorFocus(browserHost, browserHost));
    assertFalse(MonacoScriptEditor.shouldRelinquishEditorFocus(browserChild, browserHost));
    assertTrue(MonacoScriptEditor.shouldRelinquishEditorFocus(new JTextField(), browserHost));
  }

  @Test
  void runtimeHoverShowsPausedLocalVariable() {
    String hover = MonacoScriptEditor.runtimeHover(
        "int result = count + 1;", new ScriptLanguageService.Position(0, 14),
        List.of(new ScriptDebugSnapshot.Variable("count", "int", "41")));

    assertEquals("**count** = `41`\n\nType: `int`", hover);
  }

  @Test
  void runtimeHoverResolvesInstanceFieldBySimpleName() {
    String hover = MonacoScriptEditor.runtimeHover(
        "health -= damage;", new ScriptLanguageService.Position(0, 2),
        List.of(new ScriptDebugSnapshot.Variable("this.health", "int", "100")));

    assertEquals("**this.health** = `100`\n\nType: `int`", hover);
  }

  @Test
  void runtimeHoverIgnoresIdentifiersOutsidePausedScope() {
    String hover = MonacoScriptEditor.runtimeHover(
        "missing++;", new ScriptLanguageService.Position(0, 2), List.of());

    assertEquals("", hover);
  }

  @Test
  void runtimeHoverPrefersLocalThatShadowsAFieldAndEscapesMarkdown() {
    String hover = MonacoScriptEditor.runtimeHover(
        "name.length();", new ScriptLanguageService.Position(0, 1),
        List.of(
            new ScriptDebugSnapshot.Variable("this.name", "String", "field"),
            new ScriptDebugSnapshot.Variable("name", "String", "local`\nvalue")));

    assertEquals("**name** = `local\\`\\nvalue`\n\nType: `String`", hover);
  }

  @Test
  void editorRegistersDebuggerCommandsWhileNativeBrowserHasFocus() throws Exception {
    try (var source = MonacoScriptEditor.class.getResourceAsStream("/de/gurkenlabs/utiliti/script-editor/editor.js")) {
      String script = new String(source.readAllBytes(), StandardCharsets.UTF_8);

      assertEquals(7, script.split("query\\('debugCommand'", -1).length - 1);
    }
  }

  @Test
  void editorSupportsExplicitFocusRelease() throws Exception {
    try (var source = MonacoScriptEditor.class.getResourceAsStream("/de/gurkenlabs/utiliti/script-editor/editor.js")) {
      String script = new String(source.readAllBytes(), StandardCharsets.UTF_8);

      assertTrue(script.contains("method === 'blur'"));
      assertTrue(script.contains("active.blur()"));
    }
  }

  @Test
  void editorReportsLoaderFailuresAndSupportsReadinessRetries() throws Exception {
    try (var source = MonacoScriptEditor.class.getResourceAsStream("/de/gurkenlabs/utiliti/script-editor/editor.js");
         var bootstrap = MonacoScriptEditor.class.getResourceAsStream("/de/gurkenlabs/utiliti/script-editor/bootstrap.js")) {
      String script = new String(source.readAllBytes(), StandardCharsets.UTF_8);
      String startup = new String(bootstrap.readAllBytes(), StandardCharsets.UTF_8);

      assertEquals(2, script.split("utilitiReportStartupError", -1).length - 1);
      assertEquals(2, startup.split("addEventListener", -1).length - 1);
    }
  }
}
