package de.gurkenlabs.utiliti.mcp;

import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.Content;
import io.modelcontextprotocol.spec.McpSchema.GetPromptResult;
import io.modelcontextprotocol.spec.McpSchema.LoggingLevel;
import io.modelcontextprotocol.spec.McpSchema.LoggingMessageNotification;
import io.modelcontextprotocol.spec.McpSchema.Prompt;
import io.modelcontextprotocol.spec.McpSchema.PromptMessage;
import io.modelcontextprotocol.spec.McpSchema.ReadResourceResult;
import io.modelcontextprotocol.spec.McpSchema.Resource;
import io.modelcontextprotocol.spec.McpSchema.ResourceTemplate;
import io.modelcontextprotocol.spec.McpSchema.Role;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema.TextResourceContents;
import io.modelcontextprotocol.spec.McpSchema.Tool;
import io.modelcontextprotocol.spec.McpSchema.ToolAnnotations;
import jakarta.json.Json;
import jakarta.json.JsonArray;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonNumber;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonString;
import jakarta.json.JsonValue;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.SwingUtilities;

/**
 * Adapts the existing utiLITI domain handlers to the official MCP Java SDK types.
 *
 * <p>Protocol parsing, sessions, transport behavior, and JSON-RPC responses remain owned by the
 * SDK. This class only converts domain handler inputs and outputs.
 */
final class McpSdkAdapter {
  private static final Logger log = Logger.getLogger(McpSdkAdapter.class.getName());

  private final McpServer owner;
  private final Set<String> semanticToolNames = new HashSet<>();
  private final Set<String> registeredSessions = ConcurrentHashMap.newKeySet();

  McpSdkAdapter(McpServer owner) {
    this.owner = owner;
  }

  List<McpServerFeatures.SyncToolSpecification> tools() {
    List<McpServerFeatures.SyncToolSpecification> specifications = new ArrayList<>();

    // Level A: Semantic agent tools (registered first for deterministic ordering)
    for (JsonValue value : McpSemanticToolRegistry.getSemanticToolsList().getJsonArray("tools")) {
      JsonObject definition = value.asJsonObject();
      Tool tool = toTool(definition);
      semanticToolNames.add(definition.getString("name"));
      specifications.add(McpServerFeatures.SyncToolSpecification.builder()
          .tool(tool)
          .callHandler((exchange, request) ->
              callTool(exchange, request.name(), request.arguments()))
          .build());
    }

    // Level B: Editor primitive tools
    for (JsonValue value : McpToolHandler.getToolsList().getJsonArray("tools")) {
      JsonObject definition = value.asJsonObject();
      Tool tool = toTool(definition);
      specifications.add(McpServerFeatures.SyncToolSpecification.builder()
          .tool(tool)
          .callHandler((exchange, request) ->
              callTool(exchange, request.name(), request.arguments()))
          .build());
    }
    return specifications;
  }

  List<McpServerFeatures.SyncResourceSpecification> resources() {
    List<McpServerFeatures.SyncResourceSpecification> specifications = new ArrayList<>();
    JsonArray definitions = McpResourceHandler.getResourcesList().getJsonArray("resources");
    for (JsonValue value : definitions) {
      JsonObject definition = value.asJsonObject();
      Resource resource = Resource.builder(
              definition.getString("uri"), definition.getString("name"))
          .description(definition.getString("description", null))
          .mimeType(definition.getString("mimeType", "application/json"))
          .build();
      specifications.add(new McpServerFeatures.SyncResourceSpecification(
          resource, (exchange, request) -> readResource(request.uri())));
    }
    return specifications;
  }

  List<McpServerFeatures.SyncResourceTemplateSpecification> resourceTemplates() {
    List<McpServerFeatures.SyncResourceTemplateSpecification> specifications =
        new ArrayList<>();
    JsonArray definitions =
        McpResourceHandler.getResourceTemplatesList().getJsonArray("resourceTemplates");
    for (JsonValue value : definitions) {
      JsonObject definition = value.asJsonObject();
      ResourceTemplate template = ResourceTemplate.builder(
              definition.getString("uriTemplate"), definition.getString("name"))
          .description(definition.getString("description", null))
          .mimeType(definition.getString("mimeType", "application/json"))
          .build();
      specifications.add(new McpServerFeatures.SyncResourceTemplateSpecification(
          template, (exchange, request) -> readResource(request.uri())));
    }
    return specifications;
  }

  List<McpServerFeatures.SyncPromptSpecification> prompts() {
    List<McpServerFeatures.SyncPromptSpecification> specifications = new ArrayList<>();
    JsonArray definitions = McpPromptHandler.getPromptsList().getJsonArray("prompts");
    for (JsonValue value : definitions) {
      JsonObject definition = value.asJsonObject();
      Prompt prompt = Prompt.builder(definition.getString("name"))
          .description(definition.getString("description", null))
          .arguments(List.of())
          .build();
      specifications.add(new McpServerFeatures.SyncPromptSpecification(
          prompt, (exchange, request) -> getPrompt(request.name())));
    }
    return specifications;
  }

  private CallToolResult callTool(
      McpSyncServerExchange exchange, String name, Map<String, Object> arguments) {
    return withApplicationClassLoader(() -> callToolWithApplicationClassLoader(
        exchange, name, arguments));
  }

  private CallToolResult callToolWithApplicationClassLoader(
      McpSyncServerExchange exchange, String name, Map<String, Object> arguments) {
    long actionId = owner.beginAction(name);
    boolean succeeded = false;
    registerClientIfNeeded(exchange);
    sendToolLog(exchange, LoggingLevel.INFO, "Running tool: " + name);
    try {
      JsonObject result;
      if (semanticToolNames.contains(name)) {
        // Level A: route to semantic handler (stateless, batch-capable)
        result = onEdt(() -> McpSemanticHandler.handleSemanticTool(name, toJsonObject(arguments)));
      } else {
        // Level B: route to editor primitive handler
        result = McpToolHandler.handleCallTool(name, toJsonObject(arguments));
      }
      succeeded = result.getBoolean("success", true);

      List<Content> content = new ArrayList<>();
      content.add(TextContent.builder(result.toString()).build());
      addImageContent(content, result);

      sendToolLog(
          exchange,
          succeeded ? LoggingLevel.INFO : LoggingLevel.ERROR,
          (succeeded ? "Completed tool: " : "Tool failed: ") + name);
      return CallToolResult.builder()
          .content(content)
          .structuredContent(toJava(result))
          .isError(!succeeded)
          .build();
    } finally {
      owner.finishAction(actionId, name, succeeded);
    }
  }

  private static ReadResourceResult readResource(String uri) {
    JsonObject result = withApplicationClassLoader(
        () -> onEdt(() -> McpResourceHandler.handleReadResource(uri)));
    if (result.containsKey("error")) {
      throw McpError.RESOURCE_NOT_FOUND.apply(uri);
    }
    TextResourceContents contents =
        TextResourceContents.builder(uri, result.toString())
            .mimeType("application/json")
            .build();
    return ReadResourceResult.builder(List.of(contents)).build();
  }

  private static GetPromptResult getPrompt(String name) {
    JsonObject result =
        withApplicationClassLoader(() -> McpPromptHandler.handleGetPrompt(name));
    if (result.containsKey("error")) {
      throw McpError.builder(McpSchema.ErrorCodes.INVALID_PARAMS)
          .message(result.getString("error"))
          .build();
    }

    List<PromptMessage> messages = new ArrayList<>();
    for (JsonValue value : result.getJsonArray("messages")) {
      JsonObject message = value.asJsonObject();
      JsonObject content = message.getJsonObject("content");
      Role role = "assistant".equalsIgnoreCase(message.getString("role", "user"))
          ? Role.ASSISTANT
          : Role.USER;
      messages.add(PromptMessage.builder(
              role, TextContent.builder(content.getString("text", "")).build())
          .build());
    }
    return GetPromptResult.builder(messages)
        .description(result.getString("description", null))
        .build();
  }

  private static Tool toTool(JsonObject definition) {
    JsonObject annotationsJson = definition.getJsonObject("annotations");
    ToolAnnotations annotations = annotationsJson != null
        ? ToolAnnotations.builder()
            .title(annotationsJson.getString("title", null))
            .readOnlyHint(annotationsJson.getBoolean("readOnlyHint", false))
            .destructiveHint(annotationsJson.getBoolean("destructiveHint", true))
            .idempotentHint(annotationsJson.getBoolean("idempotentHint", false))
            .openWorldHint(annotationsJson.getBoolean("openWorldHint", false))
            .build()
        : ToolAnnotations.builder().build();

    @SuppressWarnings("unchecked")
    Map<String, Object> inputSchema =
        (Map<String, Object>) toJava(definition.getJsonObject("inputSchema"));
    JsonObject outputSchemaJson = definition.getJsonObject("outputSchema");
    @SuppressWarnings("unchecked")
    Map<String, Object> outputSchema =
        outputSchemaJson != null ? (Map<String, Object>) toJava(outputSchemaJson) : null;

    return Tool.builder(definition.getString("name"), inputSchema)
        .title(annotationsJson != null ? annotationsJson.getString("title", null) : null)
        .description(definition.getString("description", null))
        .outputSchema(outputSchema)
        .annotations(annotations)
        .build();
  }

  private static void addImageContent(List<Content> content, JsonObject toolResult) {
    if (!toolResult.containsKey("filePath")) {
      return;
    }
    try {
      Path path = Path.of(toolResult.getString("filePath"));
      String mimeType = Files.probeContentType(path);
      if (mimeType == null && path.getFileName().toString().toLowerCase().endsWith(".png")) {
        mimeType = "image/png";
      }
      if (mimeType == null || !mimeType.startsWith("image/")) {
        return;
      }
      content.add(McpSchema.ImageContent.builder(
              Base64.getEncoder().encodeToString(Files.readAllBytes(path)), mimeType)
          .build());
    } catch (Exception e) {
      log.log(Level.FINE, "Could not attach snapshot image content", e);
    }
  }

  private static void sendToolLog(
      McpSyncServerExchange exchange, LoggingLevel level, String message) {
    try {
      exchange.loggingNotification(
          LoggingMessageNotification.builder(level, message).logger("utiLITI").build());
    } catch (Exception e) {
      log.log(Level.FINEST, "Could not send MCP logging notification", e);
    }
  }

  private void registerClientIfNeeded(McpSyncServerExchange exchange) {
    try {
      String sessionId = exchange.sessionId();
      if (sessionId != null && registeredSessions.add(sessionId)) {
        McpSchema.Implementation clientInfo = exchange.getClientInfo();
        String name = clientInfo != null ? clientInfo.name() : "unknown";
        String version = clientInfo != null ? clientInfo.version() : "";
        owner.registerClient(sessionId, name, version);
        log.info("MCP Client connected: " + name + " (" + version + ")");
      }
    } catch (Throwable e) {
      log.log(Level.WARNING, "Could not register MCP client session", e);
    }
  }


  private static JsonObject toJsonObject(Map<String, Object> values) {
    if (values == null || values.isEmpty()) {
      return JsonValue.EMPTY_JSON_OBJECT;
    }
    JsonValue converted = toJsonValue(values);
    return converted.asJsonObject();
  }

  private static JsonValue toJsonValue(Object value) {
    if (value == null) {
      return JsonValue.NULL;
    }
    if (value instanceof JsonValue jsonValue) {
      return jsonValue;
    }
    if (value instanceof Map<?, ?> map) {
      JsonObjectBuilder builder = Json.createObjectBuilder();
      for (Map.Entry<?, ?> entry : map.entrySet()) {
        builder.add(String.valueOf(entry.getKey()), toJsonValue(entry.getValue()));
      }
      return builder.build();
    }
    if (value instanceof Iterable<?> iterable) {
      JsonArrayBuilder builder = Json.createArrayBuilder();
      for (Object item : iterable) {
        builder.add(toJsonValue(item));
      }
      return builder.build();
    }
    if (value instanceof Boolean bool) {
      return bool ? JsonValue.TRUE : JsonValue.FALSE;
    }
    if (value instanceof Number number) {
      try {
        return Json.createValue(new BigDecimal(number.toString()));
      } catch (NumberFormatException e) {
        return Json.createValue(number.doubleValue());
      }
    }
    return Json.createValue(String.valueOf(value));
  }

  private static Object toJava(JsonValue value) {
    return switch (value.getValueType()) {
      case OBJECT -> {
        Map<String, Object> result = new LinkedHashMap<>();
        for (Map.Entry<String, JsonValue> entry : value.asJsonObject().entrySet()) {
          result.put(entry.getKey(), toJava(entry.getValue()));
        }
        yield result;
      }
      case ARRAY -> {
        List<Object> result = new ArrayList<>();
        for (JsonValue item : value.asJsonArray()) {
          result.add(toJava(item));
        }
        yield result;
      }
      case STRING -> ((JsonString) value).getString();
      case NUMBER -> {
        JsonNumber number = (JsonNumber) value;
        yield number.isIntegral() ? number.longValue() : number.bigDecimalValue();
      }
      case TRUE -> true;
      case FALSE -> false;
      case NULL -> null;
    };
  }

  private static <T> T onEdt(Supplier<T> supplier) {
    if (SwingUtilities.isEventDispatchThread()) {
      return supplier.get();
    }

    AtomicReference<T> result = new AtomicReference<>();
    AtomicReference<RuntimeException> failure = new AtomicReference<>();
    try {
      SwingUtilities.invokeAndWait(() -> {
        Thread thread = Thread.currentThread();
        ClassLoader previousClassLoader = thread.getContextClassLoader();
        try {
          thread.setContextClassLoader(McpSdkAdapter.class.getClassLoader());
          result.set(supplier.get());
        } catch (RuntimeException e) {
          failure.set(e);
        } finally {
          thread.setContextClassLoader(previousClassLoader);
        }
      });
    } catch (Exception e) {
      throw new IllegalStateException("Could not read editor state on the event dispatch thread", e);
    }
    if (failure.get() != null) {
      throw failure.get();
    }
    return result.get();
  }

  private static <T> T withApplicationClassLoader(Supplier<T> supplier) {
    Thread thread = Thread.currentThread();
    ClassLoader previousClassLoader = thread.getContextClassLoader();
    try {
      thread.setContextClassLoader(McpSdkAdapter.class.getClassLoader());
      return supplier.get();
    } finally {
      thread.setContextClassLoader(previousClassLoader);
    }
  }
}
