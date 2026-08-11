package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.net.URI;
import java.nio.file.Path;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import jakarta.json.Json;
import jakarta.json.JsonArrayBuilder;
import jakarta.json.JsonObject;
import jakarta.json.JsonObjectBuilder;
import jakarta.json.JsonReader;
import me.friwi.jcefmaven.CefAppBuilder;
import org.cef.CefApp;
import org.cef.CefClient;
import org.cef.CefSettings;
import org.cef.browser.CefBrowser;
import org.cef.browser.CefFrame;
import org.cef.browser.CefMessageRouter;
import org.cef.callback.CefQueryCallback;
import org.cef.handler.CefMessageRouterHandlerAdapter;
import org.cef.handler.CefDisplayHandler;
import org.cef.handler.CefLoadHandler;
import org.cef.network.CefRequest;

/** A single Monaco/JCEF surface shared by all open script models. */
final class MonacoScriptEditor extends JPanel implements AutoCloseable {
  private static final Logger log = Logger.getLogger(MonacoScriptEditor.class.getName());
  private static final String EDITOR = "editor";
  private static final String FALLBACK = "fallback";
  private static final AtomicReference<CompletableFuture<CefApp>> CEF = new AtomicReference<>();
  private static final java.util.concurrent.atomic.AtomicInteger CEF_REFS = new java.util.concurrent.atomic.AtomicInteger();

  private final CardLayout cards = new CardLayout();
  private final JPanel browserContainer = new JPanel(new BorderLayout());
  private final JLabel fallback = new JLabel("Starting Monaco...", SwingConstants.CENTER);
  private MonacoResourceServer resources;
  private Consumer<String> changeListener = ignored -> {};
  private Runnable saveListener = () -> {};
  private Consumer<ScriptLanguageService.Analysis> analysisListener = ignored -> {};
  private Runnable readyListener = () -> {};
  private Consumer<ScriptLanguageService.Position> cursorListener = ignored -> {};
  private Consumer<String> unavailableListener = ignored -> {};
  private ScriptDefinition definition;
  private URI uri;
  private Path path;
  private volatile String text = "";
  private volatile long version;
  private volatile ScriptLanguageService languageService;
  private CefClient client;
  private CefBrowser browser;
  private CefMessageRouter router;
  private javax.swing.Timer timeoutTimer;
  private volatile boolean ready;
  private volatile boolean closed;
  private volatile String unavailableReason;
  private boolean started;

  MonacoScriptEditor() throws IOException {
    super();
    this.setLayout(this.cards);
    this.add(this.browserContainer, EDITOR);
    this.fallback.setForeground(Style.mutedText());
    this.add(this.fallback, FALLBACK);
    this.cards.show(this, FALLBACK);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    this.start();
  }

  private void start() {
    if (this.started) return;
    this.started = true;
    if (GraphicsEnvironment.isHeadless() || Boolean.getBoolean("utiliti.monaco.disabled")) {
      this.unavailable("Monaco is disabled for this runtime.");
      return;
    }
    try {
      this.resources = new MonacoResourceServer();
    } catch (IOException error) {
      this.unavailable("Could not start Monaco's loopback asset server: " + error.getMessage());
      log.log(Level.WARNING, "Could not start the Monaco resource server", error);
      return;
    }
    initializeCef().whenComplete((app, error) -> SwingUtilities.invokeLater(() -> {
      if (this.closed) return;
      if (error != null) {
        this.unavailable("JCEF did not start: " + rootMessage(error));
        log.log(Level.WARNING, "Could not initialize the bundled Monaco editor", error);
        return;
      }
      this.attachBrowser(app);
    }));
  }

  boolean isReady() {
    return this.ready;
  }

  boolean isUnavailable() {
    return this.unavailableReason != null;
  }

  String unavailableReason() {
    return this.unavailableReason;
  }

  void onChanged(Consumer<String> listener) {
    this.changeListener = listener == null ? ignored -> {} : listener;
  }

  void onSave(Runnable listener) {
    this.saveListener = listener == null ? () -> {} : listener;
  }

  void onAnalysis(Consumer<ScriptLanguageService.Analysis> listener) {
    this.analysisListener = listener == null ? ignored -> {} : listener;
  }

  void onReady(Runnable listener) {
    this.readyListener = listener == null ? () -> {} : listener;
  }

  void onCursor(Consumer<ScriptLanguageService.Position> listener) {
    this.cursorListener = listener == null ? ignored -> {} : listener;
  }

  void onUnavailable(Consumer<String> listener) {
    this.unavailableListener = listener == null ? ignored -> {} : listener;
    if (this.unavailableReason != null) this.unavailableListener.accept(this.unavailableReason);
  }

  synchronized void open(Path path, String content, ScriptDefinition definition) {
    this.path = path;
    this.definition = definition == null ? null : new ScriptDefinition(definition);
    this.uri = path == null ? URI.create("inmemory://script/" + (this.definition == null ? "untitled" : this.definition.getId())) : path.toUri();
    this.text = Objects.requireNonNullElse(content, "");
    this.version++;
    this.replaceLanguageService();
    if (this.ready) this.sendOpen();
  }

  void closeModel(URI modelUri) {
    if (this.ready && modelUri != null) {
      this.send("closeModel", Json.createObjectBuilder().add("uri", modelUri.toString()).build());
    }
  }

  void setTheme(boolean dark) {
    if (this.ready) this.send("theme", Json.createObjectBuilder().add("dark", dark).build());
  }

  void focusEditor() {
    if (this.ready) this.send("focus", Json.createObjectBuilder().build());
  }

  void revealPosition(int line, int column) {
    if (this.ready) this.send("revealLine", Json.createObjectBuilder().add("line", line).add("column", column).build());
  }

  void revealLine(int line) {
    revealPosition(line, 1);
  }

  void insertText(String text) {
    if (this.ready && text != null && !text.isEmpty()) {
      this.send("insertText", Json.createObjectBuilder().add("text", text).build());
    }
  }

  void triggerFormat() {
    if (this.ready) this.send("triggerFormat", Json.createObjectBuilder().build());
  }

  void notifyMoved() {
    if (this.browser == null || this.resources == null) return;
    Runnable resize = () -> {
      if (this.browser != null && this.resources != null) {
        java.awt.Component ui = this.browser.getUIComponent();
        if (ui != null) {
          ui.revalidate();
          ui.repaint();
        }
        this.browserContainer.revalidate();
        this.browserContainer.repaint();
        this.browser.executeJavaScript("typeof window.editor !== 'undefined' && typeof window.editor.layout === 'function' && window.editor.layout()", this.resources.editorUrl(), 0);
      }
    };
    SwingUtilities.invokeLater(resize);
    javax.swing.Timer timer = new javax.swing.Timer(100, e -> resize.run());
    timer.setRepeats(false);
    timer.start();
  }

  private void attachBrowser(CefApp app) {
    CEF_REFS.incrementAndGet();
    this.client = app.createClient();
    this.router = CefMessageRouter.create();
    this.router.addHandler(new BridgeHandler(), true);
    this.client.addMessageRouter(this.router);
    this.client.addDisplayHandler(new ConsoleHandler());
    this.client.addLoadHandler(new LoadHandler());
    this.browser = this.client.createBrowser(this.resources.editorUrl(), false, false);
    java.awt.Component browserUI = this.browser.getUIComponent();
    browserUI.setVisible(true);
    this.browserContainer.add(browserUI, BorderLayout.CENTER);
    this.browserContainer.revalidate();
    this.browserContainer.repaint();
    this.cards.show(this, EDITOR);
    this.timeoutTimer = new javax.swing.Timer(30000, event -> {
      ((javax.swing.Timer) event.getSource()).stop();
      if (!this.ready) this.unavailable("Monaco did not finish loading. Check the application output for JavaScript or JCEF errors.");
    });
    this.timeoutTimer.setRepeats(false);
    this.timeoutTimer.start();
  }

  private void unavailable(String reason) {
    if (this.unavailableReason != null) return;
    if (this.timeoutTimer != null) {
      this.timeoutTimer.stop();
      this.timeoutTimer = null;
    }
    this.unavailableReason = reason;
    this.ready = false;
    this.fallback.setText("Monaco unavailable: " + reason);
    this.cards.show(this, FALLBACK);
    SwingUtilities.invokeLater(() -> this.unavailableListener.accept(reason));
  }

  private static String rootMessage(Throwable error) {
    Throwable cause = error;
    while (cause.getCause() != null) cause = cause.getCause();
    return Objects.requireNonNullElse(cause.getMessage(), cause.getClass().getSimpleName());
  }

  private static CompletableFuture<CefApp> initializeCef() {
    CompletableFuture<CefApp> existing = CEF.get();
    if (existing != null) return existing;
    CompletableFuture<CefApp> created = CompletableFuture.supplyAsync(() -> {
      try {
        CefAppBuilder builder = new CefAppBuilder();
        builder.setInstallDir(Path.of(System.getProperty("user.home"), ".litiengine", "jcef-146").toFile());
        builder.getCefSettings().windowless_rendering_enabled = false;
        return builder.build();
      } catch (Exception error) {
        throw new IllegalStateException("Could not initialize JCEF", error);
      }
    });
    return CEF.compareAndSet(null, created) ? created : CEF.get();
  }

  private void replaceLanguageService() {
    if (this.languageService != null) this.languageService.close();
    this.languageService = this.definition == null ? null
      : Game.scripts().createLanguageService(this.definition.getLanguage()).orElse(null);
  }

  private ScriptLanguageService.Document document() {
    return new ScriptLanguageService.Document(this.uri, this.text, this.version, this.definition);
  }

  private void send(String method, JsonObject payload) {
    if (this.closed || this.browser == null || this.resources == null) return;
    try {
      JsonObject message = Json.createObjectBuilder().add("method", method).add("payload", payload).build();
      String base64 = java.util.Base64.getEncoder().encodeToString(message.toString().getBytes(java.nio.charset.StandardCharsets.UTF_8));
      this.browser.executeJavaScript("window.utilitiEditor && window.utilitiEditor.receive('" + base64 + "')", this.resources.editorUrl(), 0);
    } catch (Exception error) {
      log.log(Level.FINE, "Could not send message to Monaco editor: " + error.getMessage(), error);
    }
  }

  private synchronized JsonObject handle(JsonObject request) {
    String method = request.getString("method", "");
    JsonObject payload = request.getJsonObject("payload");
    return switch (method) {
      case "ready" -> {
        this.ready = true;
        this.unavailableReason = null;
        if (this.timeoutTimer != null) {
          this.timeoutTimer.stop();
          this.timeoutTimer = null;
        }
        SwingUtilities.invokeLater(() -> this.cards.show(this, EDITOR));
        if (this.uri != null) this.sendOpen();
        SwingUtilities.invokeLater(this.readyListener);
        yield success(Json.createObjectBuilder().build());
      }
      case "change" -> {
        StringBuilder updated = new StringBuilder(this.text);
        List<JsonObject> changes = payload.getJsonArray("changes").getValuesAs(JsonObject.class).stream()
          .sorted((left, right) -> Integer.compare(right.getInt("offset"), left.getInt("offset"))).toList();
        for (JsonObject change : changes) {
          int start = Math.max(0, Math.min(updated.length(), change.getInt("offset")));
          int end = Math.max(start, Math.min(updated.length(), start + change.getInt("length")));
          updated.replace(start, end, change.getString("text", ""));
        }
        this.text = updated.toString();
        this.version++;
        String textSnapshot = this.text;
        SwingUtilities.invokeLater(() -> this.changeListener.accept(textSnapshot));
        yield success(Json.createObjectBuilder().build());
      }
      case "save" -> {
        SwingUtilities.invokeLater(this.saveListener);
        yield success(Json.createObjectBuilder().build());
      }
      case "cursor" -> {
        ScriptLanguageService.Position position = position(payload);
        SwingUtilities.invokeLater(() -> this.cursorListener.accept(position));
        yield success(Json.createObjectBuilder().build());
      }
      case "analyze" -> this.analyze();
      case "complete" -> this.complete(position(payload));
      case "hover" -> this.hover(position(payload));
      case "signature" -> this.signature(position(payload));
      case "definition" -> this.definition(position(payload));
      case "codeActions" -> this.codeActions(payload);
      case "symbols" -> this.symbols();
      case "format" -> this.format();
      case "rename" -> this.rename(payload);
      default -> failure("Unknown editor bridge method: " + method);
    };
  }

  private JsonObject analyze() {
    if (this.languageService == null) return success(Json.createObjectBuilder().add("diagnostics", Json.createArrayBuilder()).build());
    ScriptLanguageService.Analysis analysis = this.languageService.analyze(this.document());
    SwingUtilities.invokeLater(() -> this.analysisListener.accept(analysis));
    JsonArrayBuilder diagnostics = Json.createArrayBuilder();
    analysis.diagnostics().forEach(item -> diagnostics.add(Json.createObjectBuilder()
      .add("severity", item.severity().name()).add("line", Math.max(1, item.line()))
      .add("column", Math.max(1, item.column())).add("message", item.message())));
    return success(Json.createObjectBuilder().add("diagnostics", diagnostics).build());
  }

  private void sendOpen() {
    this.send("open", Json.createObjectBuilder()
      .add("uri", this.uri.toString()).add("text", this.text)
      .add("language", this.definition == null ? "java" : this.definition.getLanguage()).build());
  }

  private JsonObject complete(ScriptLanguageService.Position position) {
    JsonArrayBuilder completions = Json.createArrayBuilder();
    if (this.languageService != null) for (ScriptLanguageService.Completion item : this.languageService.complete(this.document(), position)) {
      JsonArrayBuilder parameters = Json.createArrayBuilder();
      item.parameters().forEach(parameter -> parameters.add(Json.createObjectBuilder()
        .add("name", parameter.name()).add("type", parameter.type())));
      JsonArrayBuilder additionalEdits = Json.createArrayBuilder();
      item.additionalEdits().forEach(edit -> additionalEdits.add(Json.createObjectBuilder()
        .add("startLine", edit.range().start().line()).add("startColumn", edit.range().start().column())
        .add("endLine", edit.range().end().line()).add("endColumn", edit.range().end().column())
        .add("text", edit.text())));
      String docs = item.documentation();
      if (docs == null || docs.isBlank()) docs = item.detail() != null ? item.detail() : "";
      completions.add(Json.createObjectBuilder().add("label", item.label()).add("kind", item.kind().name())
        .add("detail", Objects.requireNonNullElse(item.detail(), ""))
        .add("documentation", docs)
        .add("insertText", Objects.requireNonNullElse(item.insertText(), item.label()))
        .add("returnType", Objects.requireNonNullElse(item.returnType(), ""))
        .add("parameters", parameters).add("additionalTextEdits", additionalEdits));
    }
    return success(Json.createObjectBuilder().add("items", completions).build());
  }

  private JsonObject hover(ScriptLanguageService.Position position) {
    Optional<ScriptLanguageService.Hover> hover = this.languageService == null
      ? Optional.empty() : this.languageService.hover(this.document(), position);
    return success(Json.createObjectBuilder().add("markdown", hover.map(ScriptLanguageService.Hover::markdown).orElse("")).build());
  }

  private JsonObject signature(ScriptLanguageService.Position position) {
    Optional<ScriptLanguageService.SignatureHelp> help = this.languageService == null
      ? Optional.empty() : this.languageService.signatureHelp(this.document(), position);
    JsonArrayBuilder signatures = Json.createArrayBuilder();
    help.ifPresent(value -> value.signatures().forEach(signature -> {
      JsonArrayBuilder params = Json.createArrayBuilder();
      signature.parameters().forEach(param -> params.add(Json.createObjectBuilder()
        .add("label", param.name()).add("documentation", param.type())));
      signatures.add(Json.createObjectBuilder()
        .add("label", signature.label()).add("documentation", Objects.requireNonNullElse(signature.documentation(), ""))
        .add("parameters", params));
    }));
    return success(Json.createObjectBuilder().add("signatures", signatures)
      .add("activeSignature", help.map(ScriptLanguageService.SignatureHelp::activeSignature).orElse(0))
      .add("activeParameter", help.map(ScriptLanguageService.SignatureHelp::activeParameter).orElse(0)).build());
  }

  private JsonObject definition(ScriptLanguageService.Position position) {
    if (this.languageService == null) return success(Json.createObjectBuilder().add("uri", "").add("line", 0).add("column", 0).build());
    Optional<ScriptLanguageService.Location> loc = this.languageService.definition(this.document(), position);
    return success(loc.map(l -> Json.createObjectBuilder()
      .add("uri", l.uri() == null ? "" : l.uri().toString())
      .add("line", l.range().start().line()).add("column", l.range().start().column()).build())
      .orElse(Json.createObjectBuilder().add("uri", "").add("line", 0).add("column", 0).build()));
  }

  private JsonObject codeActions(JsonObject payload) {
    if (this.languageService == null) return success(Json.createObjectBuilder().add("actions", Json.createArrayBuilder()).build());
    ScriptLanguageService.Range range = new ScriptLanguageService.Range(
      new ScriptLanguageService.Position(Math.max(0, payload.getInt("startLine", 1) - 1), Math.max(0, payload.getInt("startColumn", 1) - 1)),
      new ScriptLanguageService.Position(Math.max(0, payload.getInt("endLine", 1) - 1), Math.max(0, payload.getInt("endColumn", 1) - 1)));
    List<ScriptLanguageService.CodeAction> actions = this.languageService.codeActions(this.document(), range, List.of());
    JsonArrayBuilder actionArray = Json.createArrayBuilder();
    actions.forEach(action -> {
      JsonArrayBuilder edits = Json.createArrayBuilder();
      action.edits().forEach(edit -> edits.add(Json.createObjectBuilder()
        .add("startLine", edit.range().start().line()).add("startColumn", edit.range().start().column())
        .add("endLine", edit.range().end().line()).add("endColumn", edit.range().end().column())
        .add("text", edit.text())));
      actionArray.add(Json.createObjectBuilder().add("title", action.title()).add("kind", action.kind()).add("edits", edits));
    });
    return success(Json.createObjectBuilder().add("actions", actionArray).build());
  }

  private JsonObject symbols() {
    if (this.languageService == null) return success(Json.createObjectBuilder().add("symbols", Json.createArrayBuilder()).build());
    ScriptLanguageService.Analysis analysis = this.languageService.analyze(this.document());
    JsonArrayBuilder symbolArray = Json.createArrayBuilder();
    analysis.symbols().forEach(symbol -> symbolArray.add(buildSymbol(symbol)));
    return success(Json.createObjectBuilder().add("symbols", symbolArray).build());
  }

  private JsonObject format() {
    if (this.languageService == null) return success(Json.createObjectBuilder().add("text", Objects.requireNonNullElse(this.text, "")).build());
    String formatted = this.languageService.format(this.document());
    return success(Json.createObjectBuilder().add("text", Objects.requireNonNullElse(formatted, "")).build());
  }

  private JsonObject rename(JsonObject payload) {
    ScriptLanguageService.Position position = position(payload);
    String newName = payload.getString("newName", "");
    if (this.languageService == null || newName.isBlank()) {
      return success(Json.createObjectBuilder().add("edits", Json.createArrayBuilder().build()).build());
    }
    List<ScriptLanguageService.TextEdit> edits = this.languageService.rename(this.document(), position, newName);
    JsonArrayBuilder editsArr = Json.createArrayBuilder();
    for (ScriptLanguageService.TextEdit edit : edits) {
      editsArr.add(Json.createObjectBuilder()
        .add("startLine", edit.range().start().line())
        .add("startColumn", edit.range().start().column())
        .add("endLine", edit.range().end().line())
        .add("endColumn", edit.range().end().column())
        .add("newText", edit.text()));
    }
    return success(Json.createObjectBuilder().add("edits", editsArr.build()).build());
  }

  private static JsonObject buildSymbol(ScriptLanguageService.Symbol symbol) {
    JsonArrayBuilder children = Json.createArrayBuilder();
    symbol.children().forEach(child -> children.add(buildSymbol(child)));
    return Json.createObjectBuilder().add("name", symbol.name()).add("kind", symbol.kind().name())
      .add("detail", Objects.requireNonNullElse(symbol.detail(), ""))
      .add("startLine", symbol.range().start().line()).add("startColumn", symbol.range().start().column())
      .add("endLine", symbol.range().end().line()).add("endColumn", symbol.range().end().column())
      .add("children", children.build()).build();
  }

  private static ScriptLanguageService.Position position(JsonObject payload) {
    return new ScriptLanguageService.Position(Math.max(0, payload.getInt("line", 1) - 1),
      Math.max(0, payload.getInt("column", 1) - 1));
  }

  private static JsonObject success(JsonObject value) {
    return Json.createObjectBuilder().add("ok", true).add("value", value).build();
  }

  private static JsonObject failure(String message) {
    return Json.createObjectBuilder().add("ok", false).add("error", message).build();
  }

  @Override
  public synchronized void close() {
    this.closed = true;
    this.ready = false;
    if (this.timeoutTimer != null) {
      this.timeoutTimer.stop();
      this.timeoutTimer = null;
    }
    if (this.languageService != null) {
      this.languageService.close();
      this.languageService = null;
    }
    if (this.client != null && this.router != null) {
      this.client.removeMessageRouter(this.router);
    }
    if (this.router != null) { this.router.dispose(); this.router = null; }
    if (this.browser != null) { this.browser.close(true); this.browser = null; }
    if (this.client != null) { this.client.dispose(); this.client = null; }
    if (this.resources != null) { this.resources.close(); this.resources = null; }
    if (CEF_REFS.get() > 0 && CEF_REFS.decrementAndGet() <= 0) {
      shutdownCef();
    }
  }

  public static synchronized void shutdownCef() {
    CEF_REFS.set(0);
    CompletableFuture<CefApp> future = CEF.getAndSet(null);
    if (future != null) {
      try {
        if (future.isDone() && !future.isCompletedExceptionally()) {
          CefApp app = future.getNow(null);
          if (app != null) {
            app.dispose();
          }
        }
      } catch (Throwable error) {
        log.log(Level.FINE, "Error disposing CEF app future", error);
      }
    }
    try {
      if (CefApp.getState() != CefApp.CefAppState.NONE && CefApp.getState() != CefApp.CefAppState.TERMINATED) {
        CefApp.getInstance().dispose();
      }
    } catch (Throwable error) {
      log.log(Level.FINE, "Error disposing CefApp instance", error);
    }
  }

  static {
    Runtime.getRuntime().addShutdownHook(new Thread(() -> {
      try {
        shutdownCef();
      } catch (Throwable ignored) {
      }
    }, "Monaco-CEF-Shutdown"));
  }

  private final class BridgeHandler extends CefMessageRouterHandlerAdapter {
    @Override
    public boolean onQuery(CefBrowser browser, org.cef.browser.CefFrame frame, long queryId, String request,
                           boolean persistent, CefQueryCallback callback) {
      try (JsonReader reader = Json.createReader(new java.io.StringReader(request))) {
        callback.success(MonacoScriptEditor.this.handle(reader.readObject()).toString());
      } catch (Throwable error) {
        String msg = error.getMessage();
        if (msg == null || msg.isBlank()) msg = error.getClass().getSimpleName();
        log.log(Level.WARNING, "Error processing Monaco bridge query", error);
        callback.failure(500, msg);
      }
      return true;
    }
  }

  private final class ConsoleHandler implements CefDisplayHandler {
    @Override public void onAddressChange(CefBrowser browser, CefFrame frame, String url) {}
    @Override public void onTitleChange(CefBrowser browser, String title) {}
    @Override public void onFullscreenModeChange(CefBrowser browser, boolean fullscreen) {}
    @Override public boolean onTooltip(CefBrowser browser, String text) { return false; }
    @Override public void onStatusMessage(CefBrowser browser, String value) {}
    @Override public boolean onCursorChange(CefBrowser browser, int cursorType) { return false; }

    @Override
    public boolean onConsoleMessage(CefBrowser browser, CefSettings.LogSeverity severity, String message, String source,
                                    int line) {
      if (message != null && (message.contains("Canceled") || message.contains("ResizeObserver") || message.contains("SetupDiGetDeviceProperty"))) {
        log.log(Level.FINE, "Monaco console {0} at {1}:{2}: {3}", new Object[] {severity, source, line, message});
        return false;
      }
      log.log(Level.WARNING, "Monaco console {0} at {1}:{2}: {3}",
        new Object[] {severity, source, line, message});
      return false;
    }
  }

  private final class LoadHandler implements CefLoadHandler {
    @Override public void onLoadingStateChange(CefBrowser browser, boolean loading, boolean canGoBack, boolean canGoForward) {}
    @Override public void onLoadStart(CefBrowser browser, CefFrame frame, CefRequest.TransitionType transitionType) {}
    @Override public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {}

    @Override
    public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
      SwingUtilities.invokeLater(() -> unavailable("Monaco page load failed (" + errorCode + "): " + errorText));
    }
  }
}
