package de.gurkenlabs.utiliti.mcp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.utiliti.controller.Editor;
import io.modelcontextprotocol.client.McpClient;
import io.modelcontextprotocol.client.transport.HttpClientStreamableHttpTransport;
import io.modelcontextprotocol.spec.McpSchema;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonObject;
import jakarta.json.JsonValue;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class McpServerTest {
  private static int port;
  private static String endpoint;
  private static int previousPort;
  private static boolean previouslyEnabled;
  private static final String INITIALIZE =
      """
      {"jsonrpc":"2.0","id":1,"method":"initialize","params":{
        "protocolVersion":"2025-11-25",
        "capabilities":{},
        "clientInfo":{"name":"utiliti-test","version":"1.0"}
      }}
      """;

  @BeforeAll
  public static void setUp() throws Exception {
    previousPort = Editor.preferences().getMcpPort();
    previouslyEnabled = Editor.preferences().isMcpEnabled();
    port = availableLoopbackPort();
    endpoint = "http://localhost:" + port + "/mcp";
    Editor.preferences().setMcpPort(port);
    Editor.preferences().setMcpEnabled(true);
    McpServer.instance().start();
    assertTrue(McpServer.instance().isRunning(), "The isolated MCP test server failed to start");
  }

  @AfterAll
  public static void tearDown() {
    McpServer.instance().stop();
    Editor.preferences().setMcpPort(previousPort);
    Editor.preferences().setMcpEnabled(previouslyEnabled);
  }

  @Test
  void serverBindsToLoopback() {
    assertTrue(McpServer.instance().isRunning());
    assertEquals(port, McpServer.instance().getPort());
    assertNotNull(McpServer.instance().getAddress());
    assertTrue(McpServer.instance().getAddress().getAddress().isLoopbackAddress());
  }

  @Test
  void initializesOfficialSdkSession() throws Exception {
    McpResponse initialized = initialize();

    assertEquals(200, initialized.status());
    assertNotNull(initialized.sessionId());
    JsonObject result = initialized.body().getJsonObject("result");
    assertEquals("2025-11-25", result.getString("protocolVersion"));
    assertEquals("utiliti", result.getJsonObject("serverInfo").getString("name"));
    assertTrue(result.getJsonObject("capabilities").containsKey("tools"));
    assertTrue(result.getJsonObject("capabilities").containsKey("resources"));
    assertTrue(result.getJsonObject("capabilities").containsKey("prompts"));
  }

  @Test
  void connectsWithOfficialSdkClient() {
    var transport = HttpClientStreamableHttpTransport.builder(endpoint)
        .connectTimeout(Duration.ofSeconds(5))
        .build();
    try (var client = McpClient.sync(transport)
        .clientInfo(McpSchema.Implementation.builder("utiliti-sdk-test", "1.0").build())
        .initializationTimeout(Duration.ofSeconds(5))
        .requestTimeout(Duration.ofSeconds(5))
        .build()) {
      var initialized = client.initialize();
      assertEquals("2025-11-25", initialized.protocolVersion());
      assertEquals("utiliti", initialized.serverInfo().name());
      assertNotNull(client.ping());
      assertTrue(client.listTools().tools().stream()
          .anyMatch(tool -> "get-property-docs".equals(tool.name())));
      assertEquals(9, client.listResources().resources().size());
      assertEquals(6, client.listPrompts().prompts().size());
    }
  }

  @Test
  void unsupportedNewerProtocolFallsBackToSdkVersion() throws Exception {
    McpResponse initialized = post(
        INITIALIZE.replace("2025-11-25", "2026-07-28"), null, null, null);
    assertEquals(200, initialized.status());
    assertEquals(
        "2025-11-25",
        initialized.body().getJsonObject("result").getString("protocolVersion"));
  }

  @Test
  void handlesInitializedNotificationAndPing() throws Exception {
    McpResponse initialized = initialize();
    McpResponse notification = post(
        "{\"jsonrpc\":\"2.0\",\"method\":\"notifications/initialized\"}",
        initialized.sessionId(),
        null,
        null);
    assertEquals(202, notification.status());
    assertNull(notification.body());

    McpResponse ping = request(
        initialized.sessionId(), "{\"jsonrpc\":\"2.0\",\"id\":2,\"method\":\"ping\"}");
    assertEquals(200, ping.status());
    assertTrue(ping.body().getJsonObject("result").isEmpty());
  }

  @Test
  void exposesCompleteToolCatalogWithoutManageTools() throws Exception {
    JsonObject response = request(
            initialize().sessionId(),
            "{\"jsonrpc\":\"2.0\",\"id\":3,\"method\":\"tools/list\",\"params\":{}}")
        .body();
    Set<String> names = toolNames(response);

    assertFalse(names.contains("manage-tools"));
    assertTrue(names.contains("create-sprite-animation"));
    assertTrue(names.contains("add-creature"));
    assertTrue(names.contains("get-property-docs"));
    assertTrue(names.contains("get-tiles-info"));

    JsonObject loadProject = response.getJsonObject("result").getJsonArray("tools").stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "load-project".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    assertEquals("object", loadProject.getJsonObject("inputSchema").getString("type"));
    assertTrue(loadProject.getJsonObject("inputSchema").getJsonArray("required")
        .contains(Json.createValue("path")));
    assertTrue(loadProject.containsKey("outputSchema"));
    assertTrue(loadProject.containsKey("annotations"));

    JsonArray tools = response.getJsonObject("result").getJsonArray("tools");
    JsonObject createMap = tools.stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "create-map".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    JsonObject createMapTilesets = createMap
        .getJsonObject("inputSchema")
        .getJsonObject("properties")
        .getJsonObject("tilesets");
    assertEquals("array", createMapTilesets.getString("type"));
    assertEquals("string", createMapTilesets.getJsonObject("items").getString("type"));
    assertTrue(createMapTilesets.getBoolean("uniqueItems"));

    JsonObject saveProject = tools.stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "save-project".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    assertTrue(saveProject.getJsonObject("annotations").getBoolean("destructiveHint"));

    JsonObject canvasSnapshot = tools.stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "get-canvas-snapshot".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    assertFalse(canvasSnapshot.getJsonObject("annotations").getBoolean("readOnlyHint"));

    JsonObject getTilesInfo = tools.stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "get-tiles-info".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    assertTrue(getTilesInfo.getJsonObject("annotations").getBoolean("readOnlyHint"));
    JsonObject queries =
        getTilesInfo
            .getJsonObject("inputSchema")
            .getJsonObject("properties")
            .getJsonObject("queries");
    assertEquals(512, queries.getInt("maxItems"));
    JsonArray requiredQueries =
        queries.getJsonObject("items").getJsonArray("required");
    assertTrue(requiredQueries.contains(Json.createValue("layer")));
    assertTrue(requiredQueries.contains(Json.createValue("x")));
    assertTrue(requiredQueries.contains(Json.createValue("y")));

    JsonObject paintTerrain = tools.stream()
        .map(JsonValue::asJsonObject)
        .filter(tool -> "paint-terrain".equals(tool.getString("name")))
        .findFirst()
        .orElseThrow();
    JsonObject paintSchema = paintTerrain.getJsonObject("inputSchema");
    JsonObject paintProperties = paintSchema.getJsonObject("properties");
    JsonArray paintRequired = paintSchema.getJsonArray("required");
    for (String selector : List.of("tileset", "set", "terrain", "layer")) {
      assertTrue(paintProperties.containsKey(selector));
      assertTrue(paintRequired.contains(Json.createValue(selector)));
    }
  }

  @Test
  void validatesToolInputsBeforeCallingDomainHandler() throws Exception {
    JsonObject response = request(
            initialize().sessionId(),
            """
            {"jsonrpc":"2.0","id":4,"method":"tools/call",
             "params":{"name":"load-project","arguments":{}}}
            """)
        .body();

    JsonObject result = response.getJsonObject("result");
    assertTrue(result.getBoolean("isError"));
    assertTrue(result.getJsonArray("content").getJsonObject(0).getString("text")
        .contains("validation"));
  }

  @Test
  void returnsStructuredToolContentAndTracksActionStatus() throws Exception {
    JsonObject response = request(
            initialize().sessionId(),
            """
            {"jsonrpc":"2.0","id":5,"method":"tools/call",
             "params":{"name":"get-logs","arguments":{"level":"all","limit":10}}}
            """)
        .body();

    JsonObject result = response.getJsonObject("result");
    assertFalse(result.getBoolean("isError"));
    assertTrue(result.containsKey("content"));
    assertTrue(result.containsKey("structuredContent"));
    assertEquals(
        "get-logs", McpServer.instance().getActionStatus().toolName());
    assertEquals(
        McpServer.ActionState.SUCCEEDED,
        McpServer.instance().getActionStatus().state());
  }

  @Test
  void returnsJsonRpcErrorForUnknownTool() throws Exception {
    JsonObject response = request(
            initialize().sessionId(),
            """
            {"jsonrpc":"2.0","id":6,"method":"tools/call",
             "params":{"name":"missing-tool","arguments":{}}}
            """)
        .body();

    assertFalse(response.containsKey("result"));
    assertEquals(-32602, response.getJsonObject("error").getInt("code"));
  }

  @Test
  void listsAndReadsResourcesAndTemplates() throws Exception {
    String sessionId = initialize().sessionId();
    JsonObject resources = request(
            sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":7,\"method\":\"resources/list\",\"params\":{}}")
        .body();
    JsonObject templates = request(
            sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":8,\"method\":\"resources/templates/list\",\"params\":{}}")
        .body();
    JsonObject read = request(
            sessionId,
            """
            {"jsonrpc":"2.0","id":9,"method":"resources/read",
             "params":{"uri":"uti://editor/property-docs"}}
            """)
        .body();

    assertEquals(9, resources.getJsonObject("result").getJsonArray("resources").size());
    assertEquals(
        2, templates.getJsonObject("result").getJsonArray("resourceTemplates").size());
    String text = read.getJsonObject("result")
        .getJsonArray("contents")
        .getJsonObject(0)
        .getString("text");
    assertTrue(text.contains("hitpoints"));
    assertTrue(text.contains("CREATURE"));
  }

  @Test
  void listsAndGetsPrompts() throws Exception {
    String sessionId = initialize().sessionId();
    JsonObject list = request(
            sessionId,
            "{\"jsonrpc\":\"2.0\",\"id\":10,\"method\":\"prompts/list\",\"params\":{}}")
        .body();
    JsonObject prompt = request(
            sessionId,
            """
            {"jsonrpc":"2.0","id":11,"method":"prompts/get",
             "params":{"name":"build-dungeon-room","arguments":{}}}
            """)
        .body();

    assertEquals(6, list.getJsonObject("result").getJsonArray("prompts").size());
    JsonObject message =
        prompt.getJsonObject("result").getJsonArray("messages").getJsonObject(0);
    assertEquals("user", message.getString("role"));
    assertFalse(message.getJsonObject("content").getString("text").isBlank());
  }

  @Test
  void tracksConnectedClientLifecycle() {
    McpServer server = McpServer.instance();
    server.registerClient("test_session_1", "test-client", "v1.0");
    assertEquals(1, server.getConnectedClientCount());
    assertTrue(server.getConnectedClients().stream().anyMatch(c -> "test-client".equals(c.name())));

    server.disconnectClient("test_session_1");
    assertEquals(0, server.getConnectedClientCount());
  }

  @Test
  void requiresSessionForPostInitializationRequests() throws Exception {
    McpResponse response = post(
        "{\"jsonrpc\":\"2.0\",\"id\":12,\"method\":\"tools/list\",\"params\":{}}",
        null,
        null,
        null);
    assertEquals(400, response.status());
  }

  @Test
  void deletesSession() throws Exception {
    String sessionId = initialize().sessionId();
    HttpURLConnection connection = connection("DELETE");
    connection.setRequestProperty("Mcp-Session-Id", sessionId);
    connection.setRequestProperty("MCP-Protocol-Version", "2025-11-25");
    assertEquals(200, connection.getResponseCode());
    connection.disconnect();

    McpResponse response = request(
        sessionId, "{\"jsonrpc\":\"2.0\",\"id\":13,\"method\":\"ping\"}");
    assertEquals(404, response.status());
  }

  @Test
  void rejectsHostileOriginAndAllowsLoopbackPreflight() throws Exception {
    assertEquals(403, rawInitializeStatus(
        "localhost:" + port, "https://attacker.example"));

    String preflight = rawPreflightResponse().toLowerCase();
    assertTrue(preflight.startsWith("http/1.1 204"));
    assertTrue(preflight.contains(
        "access-control-allow-origin: http://localhost:" + port));
  }

  @Test
  void rejectsHostileHostHeader() throws Exception {
    assertEquals(421, rawInitializeStatus("attacker.example", null));
  }

  @Test
  void rejectsInvalidJsonAndMissingAcceptHeader() throws Exception {
    McpResponse invalidJson = post("{invalid", null, null, null);
    assertEquals(400, invalidJson.status());

    McpResponse missingAccept = post(INITIALIZE, null, null, "");
    assertEquals(400, missingAccept.status());
  }

  @Test
  void legacySseEndpointIsGoneAndStreamableGetRequiresSession() throws Exception {
    HttpURLConnection legacy = (HttpURLConnection)
        URI.create("http://localhost:" + port + "/sse").toURL().openConnection();
    legacy.setRequestProperty("Accept", "text/event-stream");
    assertEquals(404, legacy.getResponseCode());
    legacy.disconnect();

    HttpURLConnection nested = (HttpURLConnection)
        URI.create("http://localhost:" + port + "/nested/mcp").toURL().openConnection();
    nested.setRequestProperty("Accept", "text/event-stream");
    assertEquals(404, nested.getResponseCode());
    nested.disconnect();

    HttpURLConnection get = connection("GET");
    get.setRequestProperty("Accept", "text/event-stream");
    assertEquals(400, get.getResponseCode());
    get.disconnect();
  }

  @Test
  void opensAndInterruptsStreamableSseConnection() throws Exception {
    String sessionId = initialize().sessionId();
    String request = "GET /mcp HTTP/1.1\r\n"
        + "Host: localhost:" + port + "\r\n"
        + "Accept: text/event-stream\r\n"
        + "Mcp-Session-Id: " + sessionId + "\r\n"
        + "MCP-Protocol-Version: 2025-11-25\r\n"
        + "Connection: close\r\n\r\n";
    try (Socket stream = new Socket("127.0.0.1", port)) {
      stream.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
      stream.getOutputStream().flush();
    }

    assertEquals(
        200,
        request(sessionId, "{\"jsonrpc\":\"2.0\",\"id\":14,\"method\":\"ping\"}")
            .status());
  }

  @Test
  void supportsConcurrentClientSessions() throws Exception {
    try (var executor = Executors.newFixedThreadPool(4)) {
      List<Callable<Boolean>> calls = new ArrayList<>();
      for (int i = 0; i < 4; i++) {
        int id = 20 + i;
        calls.add(() -> {
          String sessionId = initialize().sessionId();
          JsonObject response = request(
                  sessionId,
                  "{\"jsonrpc\":\"2.0\",\"id\":" + id
                      + ",\"method\":\"resources/list\",\"params\":{}}")
              .body();
          return response.getJsonObject("result").getJsonArray("resources").size() == 9;
        });
      }
      assertTrue(executor.invokeAll(calls).stream().allMatch(future -> {
        try {
          return future.get();
        } catch (Exception e) {
          return false;
        }
      }));
    }
  }

  @Test
  void restartsGracefully() throws Exception {
    McpServer.instance().stop();
    assertFalse(McpServer.instance().isRunning());
    McpServer.instance().start();
    assertTrue(McpServer.instance().isRunning());
    assertEquals(200, initialize().status());
  }

  private static McpResponse initialize() throws Exception {
    return post(INITIALIZE, null, null, null);
  }

  private static McpResponse request(String sessionId, String body) throws Exception {
    return post(body, sessionId, null, null);
  }

  private static McpResponse post(
      String body, String sessionId, String origin, String acceptOverride) throws Exception {
    HttpURLConnection connection = connection("POST");
    connection.setDoOutput(true);
    connection.setRequestProperty("Content-Type", "application/json");
    connection.setRequestProperty(
        "Accept",
        acceptOverride == null ? "application/json, text/event-stream" : acceptOverride);
    if (sessionId != null) {
      connection.setRequestProperty("Mcp-Session-Id", sessionId);
      connection.setRequestProperty("MCP-Protocol-Version", "2025-11-25");
    }
    if (origin != null) {
      connection.setRequestProperty("Origin", origin);
    }

    try (OutputStream output = connection.getOutputStream()) {
      output.write(body.getBytes(StandardCharsets.UTF_8));
    }

    int status = connection.getResponseCode();
    String responseSessionId = connection.getHeaderField("Mcp-Session-Id");
    InputStream input = status >= 400 ? connection.getErrorStream() : connection.getInputStream();
    JsonObject responseBody = readResponseBody(input, connection.getContentType());
    connection.disconnect();
    return new McpResponse(status, responseBody, responseSessionId);
  }

  private static JsonObject readResponseBody(InputStream input, String contentType)
      throws Exception {
    if (input == null) {
      return null;
    }
    if (contentType != null && contentType.startsWith("text/event-stream")) {
      try (BufferedReader reader =
          new BufferedReader(new InputStreamReader(input, StandardCharsets.UTF_8))) {
        String line;
        while ((line = reader.readLine()) != null) {
          if (line.startsWith("data:")) {
            String json = line.substring("data:".length()).trim();
            JsonObject message =
                Json.createReader(new java.io.StringReader(json)).readObject();
            if (message.containsKey("id")
                || message.containsKey("result")
                || message.containsKey("error")) {
              return message;
            }
          }
        }
        return null;
      }
    }
    try (InputStream stream = input) {
      String text = new String(stream.readAllBytes(), StandardCharsets.UTF_8);
      if (text.isBlank() || !text.stripLeading().startsWith("{")) {
        return null;
      }
      return Json.createReader(new java.io.StringReader(text)).readObject();
    }
  }

  private static HttpURLConnection connection(String method) throws Exception {
    URL url = URI.create(endpoint).toURL();
    HttpURLConnection connection = (HttpURLConnection) url.openConnection();
    connection.setRequestMethod(method);
    connection.setConnectTimeout(5000);
    connection.setReadTimeout(5000);
    return connection;
  }

  private static int rawInitializeStatus(String host, String origin) throws Exception {
    byte[] body = INITIALIZE.getBytes(StandardCharsets.UTF_8);
    StringBuilder request = new StringBuilder()
        .append("POST /mcp HTTP/1.1\r\n")
        .append("Host: ").append(host).append("\r\n")
        .append("Content-Type: application/json\r\n")
        .append("Accept: application/json, text/event-stream\r\n");
    if (origin != null) {
      request.append("Origin: ").append(origin).append("\r\n");
    }
    request.append("Content-Length: ").append(body.length).append("\r\n")
        .append("Connection: close\r\n\r\n")
        .append(INITIALIZE);

    try (Socket socket = new Socket("127.0.0.1", port)) {
      socket.getOutputStream().write(request.toString().getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();
      try (BufferedReader reader = new BufferedReader(
          new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8))) {
        String statusLine = reader.readLine();
        assertNotNull(statusLine);
        return Integer.parseInt(statusLine.split(" ")[1]);
      }
    }
  }

  private static String rawPreflightResponse() throws Exception {
    String request = "OPTIONS /mcp HTTP/1.1\r\n"
        + "Host: localhost:" + port + "\r\n"
        + "Origin: http://localhost:" + port + "\r\n"
        + "Access-Control-Request-Method: POST\r\n"
        + "Connection: close\r\n\r\n";
    try (Socket socket = new Socket("127.0.0.1", port)) {
      socket.getOutputStream().write(request.getBytes(StandardCharsets.UTF_8));
      socket.getOutputStream().flush();
      return new String(socket.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
    }
  }

  private static Set<String> toolNames(JsonObject response) {
    Set<String> names = new HashSet<>();
    for (JsonValue value : response.getJsonObject("result").getJsonArray("tools")) {
      names.add(value.asJsonObject().getString("name"));
    }
    return names;
  }

  private static int availableLoopbackPort() throws Exception {
    try (ServerSocket socket = new ServerSocket(0, 0, InetAddress.getLoopbackAddress())) {
      return socket.getLocalPort();
    }
  }

  private record McpResponse(int status, JsonObject body, String sessionId) {}
}
