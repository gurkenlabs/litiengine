package de.gurkenlabs.utiliti.view.components;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;

/** Serves only bundled editor assets on a tokenized loopback origin. */
final class MonacoResourceServer implements AutoCloseable {
  private static final String MONACO_ROOT = "META-INF/resources/webjars/monaco-editor/0.55.1/";
  private static final String EDITOR_ROOT = "de/gurkenlabs/utiliti/script-editor/";
  private static final Map<String, String> CONTENT_TYPES = Map.of(
    "html", "text/html; charset=utf-8",
    "js", "text/javascript; charset=utf-8",
    "css", "text/css; charset=utf-8",
    "json", "application/json; charset=utf-8",
    "ttf", "font/ttf",
    "svg", "image/svg+xml");

  private final HttpServer server;
  private final String token = UUID.randomUUID().toString().replace("-", "");

  MonacoResourceServer() throws IOException {
    this.server = HttpServer.create(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 0);
    this.server.createContext("/" + this.token + "/", this::serve);
    this.server.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
    this.server.start();
  }

  String editorUrl() {
    return "http://127.0.0.1:" + this.server.getAddress().getPort() + "/" + this.token + "/editor/index.html";
  }

  private void serve(HttpExchange exchange) throws IOException {
    try (exchange) {
      if (!InetAddress.getLoopbackAddress().equals(exchange.getRemoteAddress().getAddress())
        || !"GET".equals(exchange.getRequestMethod())) {
        send(exchange, 403, "Forbidden".getBytes(StandardCharsets.UTF_8));
        return;
      }
      String prefix = "/" + this.token + "/";
      String path = exchange.getRequestURI().getPath();
      if (!path.startsWith(prefix) || path.contains("..")) {
        send(exchange, 404, new byte[0]);
        return;
      }
      String relative = path.substring(prefix.length());
      String resource = relative.startsWith("monaco/")
        ? MONACO_ROOT + relative.substring("monaco/".length())
        : relative.startsWith("editor/") ? EDITOR_ROOT + relative.substring("editor/".length()) : null;
      if (resource == null) {
        send(exchange, 404, new byte[0]);
        return;
      }
      InputStream input = MonacoResourceServer.class.getClassLoader().getResourceAsStream(resource);
      if (input == null && !resource.endsWith(".js")) {
        String fallbackResource = resource + ".js";
        InputStream fallbackInput = MonacoResourceServer.class.getClassLoader().getResourceAsStream(fallbackResource);
        if (fallbackInput != null) {
          resource = fallbackResource;
          input = fallbackInput;
        }
      }
      if (input == null) {
        send(exchange, 404, new byte[0]);
        return;
      }
      try (InputStream stream = input) {
        String extension = resource.contains(".") ? resource.substring(resource.lastIndexOf('.') + 1) : "js";
        exchange.getResponseHeaders().set("Content-Type", CONTENT_TYPES.getOrDefault(extension, "application/octet-stream"));
        exchange.getResponseHeaders().set("Cache-Control", relative.startsWith("monaco/")
          ? "public, max-age=31536000, immutable" : "no-store");
        exchange.getResponseHeaders().set("Content-Security-Policy",
          "default-src 'self'; script-src 'self' 'unsafe-eval'; style-src 'self' 'unsafe-inline'; "
            + "font-src 'self' data:; img-src 'self' data:; worker-src 'self' blob:; connect-src 'self'");
        send(exchange, 200, stream.readAllBytes());
      }
    }
  }

  private static void send(HttpExchange exchange, int status, byte[] content) throws IOException {
    exchange.sendResponseHeaders(status, content.length);
    exchange.getResponseBody().write(content);
  }

  @Override
  public void close() {
    this.server.stop(0);
  }
}
