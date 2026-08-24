package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.scripting.ScriptDefinition;
import de.gurkenlabs.litiengine.scripting.ScriptLanguageService;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.debug.ScriptDebugSnapshot;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GraphicsEnvironment;
import java.awt.KeyboardFocusManager;
import java.awt.event.HierarchyEvent;
import java.beans.PropertyChangeListener;
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
import javax.swing.BorderFactory;
import javax.swing.JButton;
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
  private static final String READINESS_PROBE = """
      (() => {
        if (window.utilitiStartupError && window.utilitiReportStartupError) {
          window.utilitiReportStartupError(window.utilitiStartupError);
        }
        if (window.cefQuery
            && window.utilitiEditor
            && typeof window.utilitiEditor.receive === 'function'
            && !window.utilitiReadySent) {
          window.utilitiReadySent = true;
          window.cefQuery({
            request: JSON.stringify({ method: 'ready', payload: {} }),
            onSuccess: () => { window.utilitiEditorReady = true; },
            onFailure: (_code, message) => {
              window.utilitiReadySent = false;
              if (window.utilitiReportStartupError) window.utilitiReportStartupError(message);
            }
          });
        }
      })()
      """;

  private final CardLayout cards = new CardLayout();
  private final JPanel browserContainer = new JPanel(new BorderLayout());
  private final JLabel fallbackLabel = new JLabel("Starting Monaco...", SwingConstants.CENTER);
  private final JButton retryButton = new JButton("Retry Loading Editor");
  private final PropertyChangeListener focusOwnerListener = event -> {
    Component owner = event.getNewValue() instanceof Component component ? component : null;
    if (shouldRelinquishEditorFocus(owner, this.browserContainer)) {
      this.relinquishEditorFocus();
    }
  };
  private int retryCount;
  private MonacoResourceServer resources;
  private Consumer<String> changeListener = ignored -> {};
  private Runnable saveListener = () -> {};
  private Consumer<ScriptLanguageService.Analysis> analysisListener = ignored -> {};
  private Runnable readyListener = () -> {};
  private Consumer<ScriptLanguageService.Position> cursorListener = ignored -> {};
  private Consumer<List<Integer>> breakpointListener = ignored -> {};
  private Consumer<String> debugCommandListener = ignored -> {};
  private Consumer<DefinitionTarget> definitionListener = ignored -> {};
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
  private javax.swing.Timer readinessTimer;
  private volatile boolean ready;
  private volatile boolean closed;
  private volatile String unavailableReason;
  private volatile String startupError;
  private boolean started;
  private List<Integer> breakpointLines = List.of();
  private int executionLine;
  private List<ScriptDebugSnapshot.Variable> debugVariables = List.of();
  private int pendingRevealLine;
  private int pendingRevealColumn;
  private boolean focusOwnerListenerRegistered;

  MonacoScriptEditor() throws IOException {
    super();
    this.setBackground(Style.COLOR_BG);
    this.browserContainer.setOpaque(true);
    this.browserContainer.setBackground(Style.COLOR_BG);
    this.setLayout(this.cards);
    this.add(this.browserContainer, EDITOR);

    JPanel fallbackPanel = new JPanel(new BorderLayout(0, 12));
    fallbackPanel.setOpaque(true);
    fallbackPanel.setBackground(Style.COLOR_BG);

    fallbackPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

    this.fallbackLabel.setFont(Style.getDefaultFont().deriveFont(12f));
    this.fallbackLabel.setForeground(Style.mutedText());
    this.fallbackLabel.setHorizontalAlignment(SwingConstants.CENTER);

    this.retryButton.setFocusable(false);
    Style.styleButton(this.retryButton, Style.ButtonVariant.SECONDARY);
    this.retryButton.setPreferredSize(new Dimension(160, 26));
    this.retryButton.addActionListener(e -> this.retryLoading());

    JPanel center = new JPanel(new FlowLayout(FlowLayout.CENTER, 0, 0));
    center.setOpaque(false);
    center.add(this.retryButton);

    this.addHierarchyListener(e -> {
      if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0 && this.isShowing()) {
        this.ensureStarted();
        this.notifyMoved();
      }
    });

    fallbackPanel.add(this.fallbackLabel, BorderLayout.CENTER);
    fallbackPanel.add(center, BorderLayout.SOUTH);

    this.add(fallbackPanel, FALLBACK);
    this.cards.show(this, FALLBACK);
  }

  @Override
  public void addNotify() {
    super.addNotify();
    if (this.isShowing()) {
      this.ensureStarted();
    }
  }

  void ensureStarted() {
    if (this.started) return;
    if (SwingUtilities.isEventDispatchThread()) {
      this.start();
    } else {
      SwingUtilities.invokeLater(this::start);
    }
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

  void onBreakpointsChanged(Consumer<List<Integer>> listener) {
    this.breakpointListener = listener == null ? ignored -> {} : listener;
  }

  void onDebugCommand(Consumer<String> listener) {
    this.debugCommandListener = listener == null ? ignored -> {} : listener;
  }

  void onDefinition(Consumer<DefinitionTarget> listener) {
    this.definitionListener = listener == null ? ignored -> {} : listener;
  }

  void onUnavailable(Consumer<String> listener) {
    this.unavailableListener = listener == null ? ignored -> {} : listener;
    if (this.unavailableReason != null) this.unavailableListener.accept(this.unavailableReason);
  }

  synchronized void open(Path path, String content, ScriptDefinition definition) {
    this.ensureStarted();
    this.path = path;
    this.definition = definition == null ? null : new ScriptDefinition(definition);
    this.uri = path == null ? URI.create("inmemory://script/" + (this.definition == null ? "untitled" : this.definition.getId())) : path.toUri();
    this.text = Objects.requireNonNullElse(content, "");
    this.version++;
    this.pendingRevealLine = 0;
    this.pendingRevealColumn = 0;
    this.replaceLanguageService();
    if (this.ready) this.sendOpen();
  }

  void closeModel(URI modelUri) {
    if (this.ready && modelUri != null) {
      this.send("closeModel", Json.createObjectBuilder().add("uri", modelUri.toString()).build());
    }
  }

  private boolean currentThemeDark = Editor.preferences().getTheme() == Style.Theme.DARK;

  void setTheme(boolean dark) {
    this.currentThemeDark = dark;
    if (this.ready) this.send("theme", Json.createObjectBuilder().add("dark", dark).build());
  }

  void focusEditor() {
    if (this.browser != null) this.browser.setFocus(true);
    if (this.ready) this.send("focus", Json.createObjectBuilder().build());
  }

  private void relinquishEditorFocus() {
    if (this.browser != null) this.browser.setFocus(false);
    if (this.ready) this.send("blur", Json.createObjectBuilder().build());
  }

  static boolean shouldRelinquishEditorFocus(Component focusOwner, Component browserHost) {
    return focusOwner != null
      && browserHost != null
      && focusOwner != browserHost
      && !SwingUtilities.isDescendingFrom(focusOwner, browserHost);
  }

  void revealPosition(int line, int column) {
    this.pendingRevealLine = Math.max(1, line);
    this.pendingRevealColumn = Math.max(1, column);
    if (this.ready) this.sendPendingReveal();
  }

  void revealLine(int line) {
    revealPosition(line, 1);
  }

  void setDebugState(List<Integer> breakpoints, int executionLine,
                     List<ScriptDebugSnapshot.Variable> variables) {
    this.breakpointLines = breakpoints == null ? List.of() : breakpoints.stream().filter(line -> line != null && line > 0).distinct().sorted().toList();
    this.executionLine = Math.max(0, executionLine);
    this.debugVariables = variables == null ? List.of() : List.copyOf(variables);
    if (!this.ready) return;
    JsonArrayBuilder lines = Json.createArrayBuilder();
    this.breakpointLines.forEach(lines::add);
    this.send("debugState", Json.createObjectBuilder().add("breakpoints", lines).add("executionLine", this.executionLine).build());
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
    browserUI.setBackground(Style.COLOR_BG);
    browserUI.setVisible(true);
    this.browserContainer.add(browserUI, BorderLayout.CENTER);
    this.browserContainer.revalidate();
    this.browserContainer.repaint();
    if (!this.focusOwnerListenerRegistered) {
      KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .addPropertyChangeListener("permanentFocusOwner", this.focusOwnerListener);
      this.focusOwnerListenerRegistered = true;
    }
    this.cards.show(this, EDITOR);
    this.timeoutTimer = new javax.swing.Timer(30000, event -> {

      ((javax.swing.Timer) event.getSource()).stop();
      if (!this.ready) {
        String detail = this.startupError;
        this.unavailable(detail == null || detail.isBlank()
          ? "Monaco did not finish loading. The embedded browser did not complete its editor handshake."
          : "Monaco failed to start: " + detail);
      }
    });
    this.timeoutTimer.setRepeats(false);
    this.timeoutTimer.start();
    this.readinessTimer = new javax.swing.Timer(200, event -> this.probeReadiness());
    this.readinessTimer.setInitialDelay(50);
    this.readinessTimer.start();
  }

  private void probeReadiness() {
    if (this.ready || this.closed || this.browser == null || this.resources == null) {
      this.stopReadinessTimer();
      return;
    }
    this.browser.executeJavaScript(READINESS_PROBE, this.resources.editorUrl(), 0);
  }

  private void stopReadinessTimer() {
    if (this.readinessTimer != null) {
      this.readinessTimer.stop();
      this.readinessTimer = null;
    }
  }

  public void retryLoading() {
    this.unavailableReason = null;
    this.startupError = null;
    this.ready = false;
    if (this.timeoutTimer != null) {
      this.timeoutTimer.stop();
      this.timeoutTimer = null;
    }
    this.stopReadinessTimer();

    this.cards.show(this, EDITOR);

    if (this.browser != null && this.resources != null) {
      this.browser.loadURL(this.resources.editorUrl());
      this.timeoutTimer = new javax.swing.Timer(30000, event -> {
        ((javax.swing.Timer) event.getSource()).stop();
        if (!this.ready) {
          String detail = this.startupError;
          this.unavailable(detail == null || detail.isBlank()
            ? "Monaco did not finish loading. The embedded browser did not complete its editor handshake."
            : "Monaco failed to start: " + detail);
        }
      });
      this.timeoutTimer.setRepeats(false);
      this.timeoutTimer.start();
      this.readinessTimer = new javax.swing.Timer(200, event -> this.probeReadiness());
      this.readinessTimer.setInitialDelay(50);
      this.readinessTimer.start();
    } else {
      this.start();
    }
  }

  private void unavailable(String reason) {
    if (this.retryCount == 0 && this.browser != null && this.resources != null) {
      this.retryCount++;
      log.log(Level.INFO, "Retrying Monaco editor loading automatically (attempt {0})...", this.retryCount);
      this.retryLoading();
      return;
    }
    if (this.unavailableReason != null) return;
    if (this.timeoutTimer != null) {
      this.timeoutTimer.stop();
      this.timeoutTimer = null;
    }
    this.stopReadinessTimer();
    this.unavailableReason = reason;
    this.ready = false;
    log.log(Level.WARNING, "Monaco unavailable: {0}", reason);
    this.fallbackLabel.setText("<html><center>" + reason + "</center></html>");
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
        builder.addJcefArgs(
            "--disable-extensions",
            "--disable-background-networking",
            "--disable-sync",
            "--disable-default-apps",
            "--disable-component-update"
        );
        builder.setProgressHandler((progress, percentage) -> {



          switch (progress) {
            case DOWNLOADING -> {
              if (percentage >= 0) {
                log.log(Level.INFO, "Downloading script editor runtime (CEF): {0}%", String.format("%.0f", percentage));
              }
            }
            case EXTRACTING -> {
              if (percentage >= 0) {
                log.log(Level.INFO, "Extracting script editor runtime (CEF): {0}%", String.format("%.0f", percentage));
              }
            }
            case INITIALIZED -> log.log(Level.INFO, "Script editor runtime (CEF) initialized.");
            default -> log.log(Level.FINE, "Script editor runtime: {0}", progress);
          }
        });
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
        boolean firstReady = !this.ready;
        this.ready = true;
        this.unavailableReason = null;
        this.startupError = null;
        if (this.timeoutTimer != null) {
          this.timeoutTimer.stop();
          this.timeoutTimer = null;
        }
        this.stopReadinessTimer();
        this.send("theme", Json.createObjectBuilder().add("dark", this.currentThemeDark).build());
        if (this.uri != null) {
          SwingUtilities.invokeLater(this::sendOpen);
        }
        if (firstReady) {
          SwingUtilities.invokeLater(() -> this.cards.show(this, EDITOR));
          SwingUtilities.invokeLater(this.readyListener);
        }
        yield success(Json.createObjectBuilder().build());
      }
      case "startupError" -> {
        String detail = payload == null ? "Unknown JavaScript startup error"
          : payload.getString("detail", "Unknown JavaScript startup error");
        if (!this.ready) {
          this.startupError = detail;
          SwingUtilities.invokeLater(() -> this.unavailable("Monaco failed to start: " + detail));
        }
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
      case "breakpoints" -> {
        List<Integer> lines = payload.getJsonArray("lines").getValuesAs(value -> ((jakarta.json.JsonNumber) value).intValue());
        SwingUtilities.invokeLater(() -> this.breakpointListener.accept(List.copyOf(lines)));
        yield success(Json.createObjectBuilder().build());
      }
      case "switchWorkspaceMode" -> {
        String mode = payload.getString("mode", "cycle");
        SwingUtilities.invokeLater(() -> {
          if ("map".equalsIgnoreCase(mode)) {
            UI.showMapWorkspace();
          } else if ("script".equalsIgnoreCase(mode)) {
            UI.showScriptWorkspace();
          } else {
            UI.cycleWorkspaceMode();
          }
        });
        yield success(Json.createObjectBuilder().build());
      }
      case "debugCommand" -> {
        String command = payload.getString("command", "");
        SwingUtilities.invokeLater(() -> this.debugCommandListener.accept(command));
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
    JsonArrayBuilder lines = Json.createArrayBuilder();
    this.breakpointLines.forEach(lines::add);
    this.send("open", Json.createObjectBuilder()
      .add("uri", this.uri.toString()).add("text", this.text)
      .add("language", this.definition == null ? "java" : this.definition.getLanguage())
      .add("breakpoints", lines).add("executionLine", this.executionLine).build());
    this.sendPendingReveal();
  }

  private void sendPendingReveal() {
    if (!this.ready || this.pendingRevealLine <= 0) return;
    this.send("revealLine", Json.createObjectBuilder()
      .add("line", this.pendingRevealLine).add("column", this.pendingRevealColumn).build());
    this.pendingRevealLine = 0;
    this.pendingRevealColumn = 0;
  }

  static String readinessProbeScript() {
    return READINESS_PROBE;
  }

  private JsonObject complete(ScriptLanguageService.Position position) {
    JsonArrayBuilder completions = Json.createArrayBuilder();
    if (this.languageService != null) for (ScriptLanguageService.Completion item : this.languageService.complete(this.document(), position)) {
      JsonObjectBuilder obj = Json.createObjectBuilder()
        .add("label", item.label())
        .add("kind", item.kind().name())
        .add("detail", Objects.requireNonNullElse(item.detail(), ""))
        .add("insertText", Objects.requireNonNullElse(item.insertText(), item.label()))
        .add("returnType", Objects.requireNonNullElse(item.returnType(), ""));
      String docs = item.documentation();
      if (docs != null && !docs.isBlank()) {
        obj.add("documentation", docs);
      }
      if (!item.parameters().isEmpty()) {
        JsonArrayBuilder parameters = Json.createArrayBuilder();
        item.parameters().forEach(parameter -> parameters.add(Json.createObjectBuilder()
          .add("name", parameter.name()).add("type", parameter.type())));
        obj.add("parameters", parameters);
      }
      if (!item.additionalEdits().isEmpty()) {
        JsonArrayBuilder additionalEdits = Json.createArrayBuilder();
        item.additionalEdits().forEach(edit -> additionalEdits.add(Json.createObjectBuilder()
          .add("startLine", edit.range().start().line()).add("startColumn", edit.range().start().column())
          .add("endLine", edit.range().end().line()).add("endColumn", edit.range().end().column())
          .add("text", edit.text())));
        obj.add("additionalTextEdits", additionalEdits);
      }
      completions.add(obj);
    }
    return success(Json.createObjectBuilder().add("items", completions).build());
  }


  private JsonObject hover(ScriptLanguageService.Position position) {
    Optional<ScriptLanguageService.Hover> hover = this.languageService == null
      ? Optional.empty() : this.languageService.hover(this.document(), position);
    String runtime = runtimeHover(this.text, position, this.debugVariables);
    String language = hover.map(ScriptLanguageService.Hover::markdown).orElse("");
    String markdown = runtime.isBlank() ? language
      : language.isBlank() ? runtime : runtime + "\n\n---\n\n" + language;
    return success(Json.createObjectBuilder().add("markdown", markdown).build());
  }

  static String runtimeHover(String source, ScriptLanguageService.Position position,
                             List<ScriptDebugSnapshot.Variable> variables) {
    if (source == null || position == null || variables == null || variables.isEmpty()) return "";
    String[] lines = source.split("\\R", -1);
    if (position.line() >= lines.length) return "";
    String line = lines[position.line()];
    if (line.isEmpty()) return "";
    int cursor = Math.min(position.column(), line.length() - 1);
    if (!Character.isJavaIdentifierPart(line.charAt(cursor)) && cursor > 0
        && Character.isJavaIdentifierPart(line.charAt(cursor - 1))) cursor--;
    if (!Character.isJavaIdentifierPart(line.charAt(cursor))) return "";
    int start = cursor;
    int end = cursor + 1;
    while (start > 0 && Character.isJavaIdentifierPart(line.charAt(start - 1))) start--;
    while (end < line.length() && Character.isJavaIdentifierPart(line.charAt(end))) end++;
    String identifier = line.substring(start, end);
    Optional<ScriptDebugSnapshot.Variable> exact = variables.stream()
      .filter(variable -> identifier.equals(variable.name())).findFirst();
    ScriptDebugSnapshot.Variable variable = exact.orElseGet(() -> variables.stream()
      .filter(candidate -> ("this." + identifier).equals(candidate.name())).findFirst().orElse(null));
    if (variable == null) return "";
    return "**" + markdown(variable.name()) + "** = `" + markdown(variable.value())
      + "`\n\nType: `" + markdown(variable.type()) + "`";
  }

  private static String markdown(String value) {
    return Objects.requireNonNullElse(value, "").replace("\\", "\\\\")
        .replace("\r", "\\r").replace("\n", "\\n").replace("`", "\\`");
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
    Optional<DefinitionTarget> target = loc.flatMap(this::projectDefinitionTarget);
    target.ifPresent(value -> SwingUtilities.invokeLater(() -> this.definitionListener.accept(value)));
    if (target.isPresent()) {
      return success(Json.createObjectBuilder().add("uri", "").add("line", 0).add("column", 0).build());
    }
    if (loc.map(ScriptLanguageService.Location::uri)
        .map(URI::getScheme).filter("class"::equalsIgnoreCase).isPresent()) {
      return success(Json.createObjectBuilder().add("uri", "").add("line", 0).add("column", 0).build());
    }
    return success(loc.map(l -> Json.createObjectBuilder()
      .add("uri", l.uri() == null ? "" : l.uri().toString())
      .add("line", l.range().start().line()).add("column", l.range().start().column()).build())
      .orElse(Json.createObjectBuilder().add("uri", "").add("line", 0).add("column", 0).build()));
  }

  private Optional<DefinitionTarget> projectDefinitionTarget(ScriptLanguageService.Location location) {
    if (location == null || location.uri() == null) return Optional.empty();
    Path source = null;
    String className = classNameFromUri(location.uri());
    if (className != null) {
      source = Editor.instance().getProjectCodeIntegration().findSource(className);
    } else if ("file".equalsIgnoreCase(location.uri().getScheme())) {
      try {
        source = Path.of(location.uri()).toAbsolutePath().normalize();
      } catch (Exception ignored) {
        return Optional.empty();
      }
    }
    if (source == null || !java.nio.file.Files.isRegularFile(source)) return Optional.empty();
    try {
      source = source.toRealPath();
      Path projectRoot = Editor.instance().getProjectModel() == null
        ? null : Editor.instance().getProjectModel().projectRoot();
      if (projectRoot == null || !source.startsWith(projectRoot.toRealPath())) return Optional.empty();
    } catch (IOException ignored) {
      return Optional.empty();
    }
    ScriptLanguageService.Position position = className == null
      ? location.range().start() : typeDeclarationPosition(source, className, location.range().start());
    return Optional.of(new DefinitionTarget(source, className, position.line(), position.column()));
  }

  static ScriptLanguageService.Position typeDeclarationPosition(
      Path source, String className, ScriptLanguageService.Position fallback) {
    if (source == null || className == null) return fallback;
    try {
      String text = java.nio.file.Files.readString(source);
      String simpleName = className.substring(Math.max(className.lastIndexOf('.'), className.lastIndexOf('$')) + 1);
      var matcher = java.util.regex.Pattern.compile(
        "\\b(?:class|interface|enum|record|trait|object)\\s+(" + java.util.regex.Pattern.quote(simpleName) + ")\\b")
        .matcher(text);
      if (!matcher.find()) return fallback;
      int line = 0;
      int lineStart = 0;
      for (int index = 0; index < matcher.start(1); index++) {
        if (text.charAt(index) == '\n') {
          line++;
          lineStart = index + 1;
        }
      }
      return new ScriptLanguageService.Position(line, matcher.start(1) - lineStart);
    } catch (IOException ignored) {
      return fallback;
    }
  }

  static String classNameFromUri(URI target) {
    if (target == null || !"class".equalsIgnoreCase(target.getScheme())) return null;
    String path = Objects.requireNonNullElse(target.getPath(), "");
    while (path.startsWith("/")) path = path.substring(1);
    if (!path.endsWith(".java")) return null;
    return path.substring(0, path.length() - ".java".length()).replace('/', '.').replace('\\', '.');
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

  record DefinitionTarget(Path path, String className, int line, int column) {}

  @Override
  public synchronized void close() {
    this.closed = true;
    this.ready = false;
    if (this.focusOwnerListenerRegistered) {
      KeyboardFocusManager.getCurrentKeyboardFocusManager()
        .removePropertyChangeListener("permanentFocusOwner", this.focusOwnerListener);
      this.focusOwnerListenerRegistered = false;
    }
    if (this.timeoutTimer != null) {
      this.timeoutTimer.stop();
      this.timeoutTimer = null;
    }
    this.stopReadinessTimer();
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

    @Override
    public void onLoadEnd(CefBrowser browser, CefFrame frame, int httpStatusCode) {
      if (frame != null && frame.isMain()) {
        browser.executeJavaScript(
          "if (typeof window.monaco !== 'undefined') { window.cefQuery({ request: JSON.stringify({ method: 'ready', payload: {} }) }); }",
          frame.getURL(), 0);
      }
    }

    @Override
    public void onLoadError(CefBrowser browser, CefFrame frame, ErrorCode errorCode, String errorText, String failedUrl) {
      SwingUtilities.invokeLater(() -> unavailable("Monaco page load failed (" + errorCode + "): " + errorText));
    }
  }
}
