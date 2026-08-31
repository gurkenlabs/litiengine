package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.Game;
import org.junit.jupiter.api.Test;

class ScriptLanguageServiceIntegrationTest {
  @Test
  void registersTheJavaSemanticProviderOnTheEditorClasspath() {
    assertTrue(Game.scripts().getProviders().stream()
      .anyMatch(provider -> provider.language().equalsIgnoreCase("java")));
  }

  @Test
  void servesMonacoEditorAssetsOverLoopbackServer() throws Exception {
    try (MonacoResourceServer server = new MonacoResourceServer()) {
      String baseUrl = server.editorUrl().replace("/editor/index.html", "");
      java.net.http.HttpClient client = java.net.http.HttpClient.newHttpClient();
      for (String path : java.util.List.of("/editor/index.html", "/editor/bootstrap.js", "/editor/editor.js", "/editor/editor.css", "/monaco/min/vs/loader.js", "/monaco/min/vs/editor/editor.main.js", "/monaco/min/vs/loader")) {
        java.net.http.HttpRequest request = java.net.http.HttpRequest.newBuilder(java.net.URI.create(baseUrl + path)).GET().build();
        java.net.http.HttpResponse<String> response = client.send(request, java.net.http.HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Failed to serve " + path);
        assertFalse(response.body().isEmpty(), "Empty response for " + path);
      }
    }
  }
}
