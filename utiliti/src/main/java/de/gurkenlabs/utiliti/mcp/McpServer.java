package de.gurkenlabs.utiliti.mcp;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.GameListener;
import de.gurkenlabs.utiliti.controller.Editor;
import io.modelcontextprotocol.server.McpServerFeatures;
import io.modelcontextprotocol.server.McpSyncServer;
import io.modelcontextprotocol.server.transport.DefaultServerTransportSecurityValidator;
import io.modelcontextprotocol.server.transport.HttpServletStreamableServerTransportProvider;
import io.modelcontextprotocol.spec.McpSchema.ServerCapabilities;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.URI;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import org.apache.catalina.Context;
import org.apache.catalina.LifecycleException;
import org.apache.catalina.Wrapper;
import org.apache.catalina.connector.Connector;
import org.apache.catalina.startup.Tomcat;
import org.apache.tomcat.util.descriptor.web.FilterDef;
import org.apache.tomcat.util.descriptor.web.FilterMap;

/** Owns the lifecycle of the local, SDK-backed utiLITI MCP server. */
public class McpServer implements GameListener {
  public static final String ENDPOINT = "/mcp";
  private static final long COMPLETED_ACTION_DISPLAY_NANOS =
      TimeUnit.MILLISECONDS.toNanos(1500);
  private static final Logger log = Logger.getLogger(McpServer.class.getName());

  private static McpServer instance;
  private final ConcurrentSkipListMap<Long, String> activeActions =
      new ConcurrentSkipListMap<>();
  private final ConcurrentHashMap<String, ConnectedClient> connectedClients =
      new ConcurrentHashMap<>();
  private final AtomicLong actionSequence = new AtomicLong();

  private Tomcat tomcat;
  private HttpServletStreamableServerTransportProvider transportProvider;
  private McpSyncServer sdkServer;
  private InetSocketAddress address;
  private int port;
  private volatile boolean running;
  private volatile String lastAction;
  private volatile ActionState lastActionState = ActionState.IDLE;
  private volatile long lastActionCompletedAt;

  private McpServer() {
    Game.addGameListener(this);
  }

  public static synchronized McpServer instance() {
    if (instance == null) {
      instance = new McpServer();
    }
    return instance;
  }

  public synchronized void start() {
    if (running) {
      return;
    }

    port = Editor.preferences().getMcpPort();
    if (port < 1 || port > 65535) {
      log.log(Level.SEVERE, "Failed to start utiLITI MCP Server: invalid port {0}", port);
      return;
    }

    try {
      suppressExpectedCancellationWarnings();
      transportProvider = createTransportProvider(port);
      McpSdkAdapter catalog = new McpSdkAdapter(this);
      sdkServer = io.modelcontextprotocol.server.McpServer.sync(transportProvider)
          .serverInfo("utiliti", Game.info().getVersion())
          .instructions(
              "Use the available tools, resources, and prompts to inspect and edit the active "
                  + "utiLITI project. Destructive tools modify editor state and should be "
                  + "confirmed by the client.")
          .capabilities(ServerCapabilities.builder()
              .tools(false)
              .resources(false, false)
              .prompts(false)
              .build())
          .tools(catalog.tools())
          .resources(catalog.resources())
          .resourceTemplates(catalog.resourceTemplates())
          .prompts(catalog.prompts())
          .requestTimeout(Duration.ofSeconds(30))
          .immediateExecution(true)
          .build();

      tomcat = createTomcat(transportProvider, port);
      tomcat.start();
      address = new InetSocketAddress(InetAddress.getLoopbackAddress(), port);
      running = true;
      log.info("utiLITI MCP Server started on http://localhost:" + port + ENDPOINT);
    } catch (Exception e) {
      cleanupServerResources();
      log.log(Level.SEVERE, "Failed to start utiLITI MCP Server on port " + port, e);
    }
  }

  /**
   * The SDK logs client-side request aborts as warnings when it has no cancellation-notification
   * handler. Abort notifications are expected during client retries and do not affect server state.
   */
  static void suppressExpectedCancellationWarnings() {
    Logger sdkLogger = Logger.getLogger("io.modelcontextprotocol.spec.McpStreamableServerSession");
    java.util.logging.Filter existingFilter = sdkLogger.getFilter();
    sdkLogger.setFilter(record -> (existingFilter == null || existingFilter.isLoggable(record))
        && !isExpectedCancellationWarning(record));
  }

  static boolean isExpectedCancellationWarning(LogRecord record) {
    if (record == null || record.getLevel().intValue() < Level.WARNING.intValue()) {
      return false;
    }
    String message = record.getMessage();
    return message != null
        && message.contains("No handler registered for notification method:")
        && message.contains("notifications/cancelled");
  }

  public synchronized void stop() {
    if (!running && tomcat == null && sdkServer == null) {
      return;
    }

    running = false;
    try {
      cleanupServerResources();
      log.info("utiLITI MCP Server stopped");
    } catch (Exception e) {
      log.log(Level.WARNING, "Error stopping utiLITI MCP Server", e);
    }
  }

  public boolean isRunning() {
    return running;
  }

  public int getPort() {
    return port;
  }

  InetSocketAddress getAddress() {
    return address;
  }

  public ActionStatus getActionStatus() {
    Map.Entry<Long, String> active = activeActions.lastEntry();
    if (active != null) {
      return new ActionStatus(ActionState.RUNNING, active.getValue());
    }
    if (lastAction != null
        && System.nanoTime() - lastActionCompletedAt < COMPLETED_ACTION_DISPLAY_NANOS) {
      return new ActionStatus(lastActionState, lastAction);
    }
    return new ActionStatus(ActionState.IDLE, null);
  }

  long beginAction(String name) {
    long actionId = actionSequence.incrementAndGet();
    activeActions.put(actionId, name);
    return actionId;
  }

  void finishAction(long actionId, String name, boolean succeeded) {
    activeActions.remove(actionId);
    lastAction = name;
    lastActionState = succeeded ? ActionState.SUCCEEDED : ActionState.FAILED;
    lastActionCompletedAt = System.nanoTime();
  }

  public void registerClient(String name, String version) {
    if (name == null || name.isBlank()) {
      return;
    }
    String key = name.trim() + (version != null && !version.isBlank() ? ":" + version.trim() : "");
    connectedClients.putIfAbsent(key, new ConnectedClient(key, name, version, Instant.now()));
  }

  public void registerClient(String sessionId, String name, String version) {
    if (sessionId == null || sessionId.isBlank()) {
      registerClient(name, version);
      return;
    }
    connectedClients.putIfAbsent(sessionId,
        new ConnectedClient(sessionId, name, version, Instant.now()));
  }

  void unregisterClient(String sessionId) {
    if (sessionId != null) {
      connectedClients.remove(sessionId);
    }
  }

  public Collection<ConnectedClient> getConnectedClients() {
    return Collections.unmodifiableCollection(connectedClients.values());
  }

  public int getConnectedClientCount() {
    return connectedClients.size();
  }

  public void disconnectClient(String sessionId) {
    unregisterClient(sessionId);
    log.info("Disconnected MCP client session: " + sessionId);
  }

  @Override
  public void terminated() {
    stop();
  }

  private static HttpServletStreamableServerTransportProvider createTransportProvider(int port) {
    List<String> allowedHosts =
        List.of("localhost:" + port, "127.0.0.1:" + port, "[::1]:" + port);
    List<String> allowedOrigins =
        List.of(
            "http://localhost:" + port,
            "http://127.0.0.1:" + port,
            "http://[::1]:" + port);

    return HttpServletStreamableServerTransportProvider.builder()
        // Streamable HTTP does not require a permanent GET stream. The SDK keep-alive scheduler
        // also targets POST-only and disconnected sessions, producing an unbounded warning loop.
        .mcpEndpoint(ENDPOINT)
        .securityValidator(DefaultServerTransportSecurityValidator.builder()
            .allowedHosts(allowedHosts)
            .allowedOrigins(allowedOrigins)
            .build())
        .build();
  }

  private static Tomcat createTomcat(
      HttpServletStreamableServerTransportProvider transportProvider, int port) {
    Tomcat embeddedTomcat = new Tomcat();
    embeddedTomcat.setPort(port);
    String baseDir = System.getProperty("java.io.tmpdir");
    embeddedTomcat.setBaseDir(baseDir);

    Context context = embeddedTomcat.addContext("", baseDir);
    Wrapper wrapper = context.createWrapper();
    wrapper.setName("mcpServlet");
    wrapper.setServlet(transportProvider);
    wrapper.setLoadOnStartup(1);
    wrapper.setAsyncSupported(true);
    context.addChild(wrapper);
    context.addServletMappingDecoded(ENDPOINT, "mcpServlet");

    FilterDef filterDefinition = new FilterDef();
    filterDefinition.setFilterName("mcpCorsFilter");
    filterDefinition.setFilter(new LoopbackCorsFilter(port));
    filterDefinition.setAsyncSupported("true");
    context.addFilterDef(filterDefinition);

    FilterMap filterMapping = new FilterMap();
    filterMapping.setFilterName("mcpCorsFilter");
    filterMapping.addURLPattern(ENDPOINT);
    filterMapping.setDispatcher("REQUEST");
    filterMapping.setDispatcher("ASYNC");
    context.addFilterMap(filterMapping);

    Connector connector = embeddedTomcat.getConnector();
    connector.setPort(port);
    connector.setProperty("address", InetAddress.getLoopbackAddress().getHostAddress());
    connector.setAsyncTimeout(Duration.ofSeconds(30).toMillis());
    return embeddedTomcat;
  }

  private void cleanupServerResources() {
    activeActions.clear();
    connectedClients.clear();
    lastAction = null;
    lastActionState = ActionState.IDLE;
    address = null;

    if (sdkServer != null) {
      try {
        sdkServer.closeGracefully();
      } catch (Exception e) {
        log.log(Level.FINE, "Error closing MCP SDK server", e);
      } finally {
        sdkServer = null;
      }
    }

    if (tomcat != null) {
      try {
        tomcat.stop();
      } catch (LifecycleException e) {
        log.log(Level.FINE, "Error stopping embedded MCP server", e);
      }
      try {
        tomcat.destroy();
      } catch (LifecycleException e) {
        log.log(Level.FINE, "Error destroying embedded MCP server", e);
      } finally {
        tomcat = null;
      }
    }

    transportProvider = null;
  }

  public enum ActionState {
    IDLE,
    RUNNING,
    SUCCEEDED,
    FAILED
  }

  public record ActionStatus(ActionState state, String toolName) {}

  public record ConnectedClient(
      String sessionId, String name, String version, Instant connectedAt) {}

  private static final class LoopbackCorsFilter implements Filter {
    private final int port;

    private LoopbackCorsFilter(int port) {
      this.port = port;
    }

    @Override
    public void init(FilterConfig filterConfig) {
      // No filter state to initialize.
    }

    @Override
    public void doFilter(
        ServletRequest request, ServletResponse response, FilterChain chain)
        throws IOException, ServletException {
      if (!(request instanceof HttpServletRequest httpRequest)
          || !(response instanceof HttpServletResponse httpResponse)) {
        chain.doFilter(request, response);
        return;
      }

      String origin = httpRequest.getHeader("Origin");
      if (origin != null && !origin.isBlank()) {
        if (!isAllowedOrigin(origin, port)) {
          httpResponse.sendError(HttpServletResponse.SC_FORBIDDEN, "Forbidden origin");
          return;
        }
        httpResponse.setHeader("Access-Control-Allow-Origin", origin);
        httpResponse.setHeader("Vary", "Origin");
      }

      httpResponse.setHeader(
          "Access-Control-Allow-Headers",
          "Content-Type, Accept, MCP-Protocol-Version, Mcp-Session-Id, Last-Event-ID");
      httpResponse.setHeader("Access-Control-Expose-Headers", "Mcp-Session-Id");
      httpResponse.setHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, OPTIONS");

      if ("OPTIONS".equalsIgnoreCase(httpRequest.getMethod())) {
        httpResponse.setStatus(HttpServletResponse.SC_NO_CONTENT);
        return;
      }

      chain.doFilter(request, response);
    }

    @Override
    public void destroy() {
      // No filter state to destroy.
    }
  }

  private static boolean isAllowedOrigin(String origin, int port) {
    try {
      URI uri = URI.create(origin);
      String host = uri.getHost();
      return "http".equalsIgnoreCase(uri.getScheme())
          && uri.getPort() == port
          && host != null
          && ("localhost".equalsIgnoreCase(host)
              || "127.0.0.1".equals(host)
              || "::1".equals(host)
              || "0:0:0:0:0:0:0:1".equals(host));
    } catch (IllegalArgumentException e) {
      return false;
    }
  }
}
