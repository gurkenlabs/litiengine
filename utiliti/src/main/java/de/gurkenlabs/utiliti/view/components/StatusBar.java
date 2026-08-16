package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.Game;
import de.gurkenlabs.litiengine.environment.tilemap.IMap;
import de.gurkenlabs.litiengine.input.Input;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.tool.PointerTool;
import de.gurkenlabs.utiliti.controller.tool.ToolManager;
import de.gurkenlabs.utiliti.mcp.McpServer;
import de.gurkenlabs.utiliti.mcp.McpServer.ActionState;
import de.gurkenlabs.utiliti.mcp.McpServer.ActionStatus;
import de.gurkenlabs.utiliti.mcp.McpServer.ConnectedClient;
import de.gurkenlabs.utiliti.model.Icons;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.HeadlessException;
import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

public final class StatusBar extends JPanel {
  static final int FPS_TOLERANCE = 1;
  static final double FPS_WARNING_RATIO = 0.9;
  static final Color FPS_WARNING_COLOR = new Color(220, 180, 70);

  private final JLabel stateLabel = new JLabel();
  private final JLabel mcpLabel = createMcpBadge();
  private final JLabel toolLabel = new JLabel();
  private final JLabel positionLabel = new JLabel();
  private final JLabel tileLabel = new JLabel();
  private final JLabel gridLabel = new JLabel();
  private final JLabel snapLabel = new JLabel();
  private final JLabel fpsLabel = new JLabel(formatFps(0, 0));
  private final Timer updateTimer;
  private final Timer animationTimer;
  private final List<JPanel> separatorLines = new ArrayList<>();
  private int currentFps;

  public StatusBar() {
    this.animationTimer = new Timer(50, event -> {
      McpServer server = McpServer.instance();
      if (server.isRunning() && server.getActionStatus().state() == ActionState.RUNNING) {
        this.mcpLabel.repaint();
      } else {
        ((Timer) event.getSource()).stop();
      }
    });

    setLayout(new BoxLayout(this, BoxLayout.X_AXIS));
    setOpaque(true);
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));

    Font font = new Font(
        Style.FONTNAME_CONSOLE,
        Font.PLAIN,
        Math.max(10, Math.round(11 * Editor.preferences().getUiScale())));
    for (JLabel label : new JLabel[] {
        this.stateLabel, this.mcpLabel, this.toolLabel, this.positionLabel, this.tileLabel,
        this.gridLabel, this.snapLabel, this.fpsLabel}) {
      label.setFont(font);
    }

    add(this.stateLabel);
    add(separator());
    add(this.mcpLabel);
    add(separator());
    add(this.toolLabel);
    add(separator());
    add(this.positionLabel);
    add(separator());
    add(this.tileLabel);
    add(separator());
    add(this.gridLabel);
    add(separator());
    add(this.snapLabel);
    add(Box.createHorizontalGlue());
    add(this.fpsLabel);

    this.updateTimer = new Timer(100, event -> updateValues());
    Game.window().getRenderComponent().onFpsChanged(
        fps -> SwingUtilities.invokeLater(() -> {
          this.currentFps = fps;
          this.fpsLabel.setText(formatFps(fps, Game.metrics().getEstimatedMaxFramesPerSecond()));
          this.fpsLabel.setForeground(fpsColor(fps, Editor.preferences().getEditorFpsCap()));
        }));
    refreshTheme();
    updateValues();
  }

  static String formatFps(int fps, int maxFps) {
    return fps + " FPS  |  " + maxFps + " MAX";
  }

  static Color fpsColor(int fps, int configuredFps) {
    if (fps >= configuredFps - FPS_TOLERANCE) {
      return Style.COLOR_GREEN;
    }
    return fps >= Math.ceil(configuredFps * FPS_WARNING_RATIO)
        ? FPS_WARNING_COLOR
        : Style.COLOR_RED;
  }

  @Override public void addNotify() {
    super.addNotify();
    this.updateTimer.start();
  }

  @Override public void removeNotify() {
    this.updateTimer.stop();
    this.animationTimer.stop();
    super.removeNotify();
  }

  void refreshTheme() {
    setBackground(Style.background());
    setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createMatteBorder(1, 0, 0, 0, Style.border()),
        BorderFactory.createEmptyBorder(2, 8, 2, 8)));
    this.stateLabel.setForeground(Style.text());
    this.mcpLabel.setForeground(mcpColor(
        McpServer.instance().isRunning(), McpServer.instance().getActionStatus()));
    this.toolLabel.setForeground(Style.text());
    this.positionLabel.setForeground(Style.mutedText());
    this.tileLabel.setForeground(Style.mutedText());
    this.gridLabel.setForeground(Style.mutedText());
    this.snapLabel.setForeground(Style.mutedText());
    this.fpsLabel.setForeground(fpsColor(this.currentFps, Editor.preferences().getEditorFpsCap()));
    for (JPanel separatorLine : this.separatorLines) {
      separatorLine.setBackground(Style.border());
    }
  }

  void updateValues() {
    String currentStatus = Editor.instance().getCurrentStatus();
    this.stateLabel.setText(currentStatus == null || currentStatus.isBlank()
        ? Resources.strings().get("status_ready")
        : currentStatus);
    McpServer mcpServer = McpServer.instance();
    ActionStatus mcpAction = mcpServer.getActionStatus();
    if (mcpServer.isRunning()) {
      this.mcpLabel.setToolTipText(mcpTooltip(mcpServer.getPort(), mcpAction));
      this.mcpLabel.setForeground(mcpColor(true, mcpAction));
      this.mcpLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
      if (mcpAction.state() == ActionState.RUNNING) {
        if (!this.animationTimer.isRunning()) {
          this.animationTimer.start();
        }
      } else {
        if (this.animationTimer.isRunning()) {
          this.animationTimer.stop();
        }
      }
    } else {
      this.mcpLabel.setToolTipText("MCP Server is disabled or stopped");
      this.mcpLabel.setForeground(Style.mutedText());
      this.mcpLabel.setCursor(Cursor.getDefaultCursor());
      if (this.animationTimer.isRunning()) {
        this.animationTimer.stop();
      }
    }
    this.mcpLabel.repaint();
    this.toolLabel.setText(getToolDisplayName());

    IMap map = Game.world() != null && Game.world().environment() != null
        ? Game.world().environment().getMap()
        : null;
    if (map == null) {
      this.positionLabel.setText(Resources.strings().get("status_position", "--", "--"));
      this.tileLabel.setText(Resources.strings().get("status_tile", "--", "--"));
      this.gridLabel.setText(Resources.strings().get("status_grid", "--"));
    } else {
      this.positionLabel.setText(Resources.strings().get(
          "status_position",
          Integer.toString((int) Input.mouse().getMapLocation().getX()),
          Integer.toString((int) Input.mouse().getMapLocation().getY())));
      this.tileLabel.setText(Resources.strings().get(
          "status_tile",
          Integer.toString(Input.mouse().getTile().x),
          Integer.toString(Input.mouse().getTile().y)));
      String gridSize = map.getTileWidth() == map.getTileHeight()
          ? map.getTileWidth() + " px"
          : map.getTileWidth() + "x" + map.getTileHeight();
      this.gridLabel.setText(Resources.strings().get("status_grid", gridSize));
    }
    this.snapLabel.setText(Editor.preferences().snapToGrid()
        ? Resources.strings().get(
            "status_snapOn", Integer.toString(Editor.preferences().getSnapDivision()))
        : Resources.strings().get("status_snapOff"));
  }

  static Color mcpColor(boolean running, ActionStatus action) {
    if (!running) {
      return Style.mutedText();
    }
    if (action == null) {
      return Style.COLOR_GREEN;
    }
    return switch (action.state()) {
      case RUNNING -> Style.COLOR_ORANGE;
      case FAILED -> Style.COLOR_RED;
      case IDLE, SUCCEEDED -> Style.COLOR_GREEN;
    };
  }

  static String mcpTooltip(int port, ActionStatus action) {
    String hint = ". Click to view connected clients.";
    if (action == null || action.state() == ActionState.IDLE || action.toolName() == null) {
      return "MCP Server listening on port " + port + hint;
    }
    String tool = formatMcpAction(action.toolName());
    return switch (action.state()) {
      case RUNNING -> "MCP action in progress: " + tool + hint;
      case SUCCEEDED -> "MCP action completed: " + tool + hint;
      case FAILED -> "MCP action failed: " + tool + hint;
      case IDLE -> "MCP Server listening on port " + port + hint;
    };
  }

  public static JLabel createMcpBadge() {
    JLabel label = new JLabel(new McpStatusIcon());
    Dimension mcpIndicatorSize = new Dimension(62, 16);
    label.setPreferredSize(mcpIndicatorSize);
    label.setMinimumSize(new Dimension(44, 16));
    label.setMaximumSize(new Dimension(64, 16));
    label.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    label.getAccessibleContext().setAccessibleName("MCP server status");
    label.getAccessibleContext().setAccessibleDescription("Shows MCP activity and connected clients");
    label.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent event) {
        if (SwingUtilities.isLeftMouseButton(event) && McpServer.instance().isRunning()) {
          showMcpPanel(label);
        }
      }
    });
    Timer timer = new Timer(50, event -> {
      McpServer server = McpServer.instance();
      boolean running = server.isRunning();
      ActionStatus action = server.getActionStatus();
      label.setToolTipText(mcpTooltip(server.getPort(), action));
      if (running && action.state() == ActionState.RUNNING) {
        label.repaint();
      }
    });
    timer.start();
    return label;
  }

  public static void showMcpPanel(Component invoker) {
    McpServer server = McpServer.instance();
    if (!server.isRunning()) {
      return;
    }

    int port = server.getPort();
    String endpoint = "http://localhost:" + port + McpServer.ENDPOINT;
    JPopupMenu menu = new JPopupMenu();
    menu.setBackground(Style.surface());
    menu.setBorder(BorderFactory.createCompoundBorder(
        BorderFactory.createLineBorder(Style.border(), 1),
        BorderFactory.createEmptyBorder(4, 0, 4, 0)));

    // --- Server status header ---
    JPanel header = new JPanel();
    header.setLayout(new BoxLayout(header, BoxLayout.Y_AXIS));
    header.setOpaque(false);
    header.setAlignmentX(Component.LEFT_ALIGNMENT);
    header.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

    JPanel statusRow = new JPanel();
    statusRow.setLayout(new BoxLayout(statusRow, BoxLayout.X_AXIS));
    statusRow.setOpaque(false);
    statusRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel dotLabel = new JLabel(new GreenDotIcon(7));
    statusRow.add(dotLabel);

    JLabel serverLabel = new JLabel("MCP Server");
    serverLabel.setForeground(Style.text());
    serverLabel.setFont(serverLabel.getFont().deriveFont(Font.BOLD));
    statusRow.add(serverLabel);
    statusRow.add(Box.createHorizontalGlue());
    JLabel runningLabel = new JLabel("Running");
    runningLabel.setForeground(Style.COLOR_GREEN);
    runningLabel.setFont(runningLabel.getFont().deriveFont(Font.BOLD));
    statusRow.add(runningLabel);
    header.add(statusRow);

    JPanel urlRow = new JPanel();
    urlRow.setLayout(new BoxLayout(urlRow, BoxLayout.X_AXIS));
    urlRow.setOpaque(false);
    urlRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    urlRow.setBorder(BorderFactory.createEmptyBorder(2, 0, 0, 0));
    JLabel urlLabel = new JLabel(endpoint);
    urlLabel.setForeground(Style.mutedText());
    urlLabel.setFont(new Font(Style.FONTNAME_CONSOLE, Font.PLAIN,
        Math.max(10, Math.round(11 * Editor.preferences().getUiScale()))));
    urlRow.add(urlLabel);
    urlRow.add(Box.createHorizontalGlue());

    JLabel copyBtn = new JLabel(Icons.COPY_16);
    copyBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    copyBtn.setToolTipText("Copy URL");
    copyBtn.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        try {
          Toolkit.getDefaultToolkit().getSystemClipboard().setContents(
              new StringSelection(endpoint), null);
        } catch (HeadlessException | IllegalStateException ex) {
          // clipboard unavailable
        }
      }
    });
    urlRow.add(copyBtn);
    header.add(urlRow);
    menu.add(header);
    menu.addSeparator();

    // --- Connected clients section ---
    var clients = server.getConnectedClients();
    JPanel clientsHeader = new JPanel();
    clientsHeader.setLayout(new BoxLayout(clientsHeader, BoxLayout.X_AXIS));
    clientsHeader.setOpaque(false);
    clientsHeader.setAlignmentX(Component.LEFT_ALIGNMENT);
    clientsHeader.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));
    JLabel clientsLabel = new JLabel(
        "CONNECTED CLIENTS (" + clients.size() + ")");
    clientsLabel.setForeground(Style.mutedText());
    clientsLabel.setFont(clientsLabel.getFont().deriveFont(Font.BOLD,
        Math.max(9f, 10f * Editor.preferences().getUiScale())));
    clientsHeader.add(clientsLabel);
    clientsHeader.add(Box.createHorizontalGlue());

    JLabel refreshBtn = new JLabel(Icons.LOOP_16);
    refreshBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
    refreshBtn.setToolTipText("Refresh");
    refreshBtn.addMouseListener(new MouseAdapter() {
      @Override public void mouseClicked(MouseEvent e) {
        menu.setVisible(false);
        SwingUtilities.invokeLater(() -> showMcpPanel(invoker));
      }
    });
    clientsHeader.add(refreshBtn);
    menu.add(clientsHeader);

    if (clients.isEmpty()) {
      JPanel emptyRow = new JPanel();
      emptyRow.setLayout(new BoxLayout(emptyRow, BoxLayout.X_AXIS));
      emptyRow.setOpaque(false);
      emptyRow.setAlignmentX(Component.LEFT_ALIGNMENT);
      emptyRow.setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));
      JLabel emptyLabel = new JLabel("No clients connected");
      emptyLabel.setForeground(Style.mutedText());
      emptyRow.add(emptyLabel);
      emptyRow.add(Box.createHorizontalGlue());
      menu.add(emptyRow);
    } else {
      for (ConnectedClient client : clients) {
        menu.addSeparator();
        menu.add(clientEntry(client));
      }
    }

    menu.show(invoker, 0, invoker.getHeight());
  }

  private static JPanel clientEntry(ConnectedClient client) {
    JPanel panel = new JPanel();
    panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
    panel.setOpaque(false);
    panel.setAlignmentX(Component.LEFT_ALIGNMENT);
    panel.setBorder(BorderFactory.createEmptyBorder(4, 12, 4, 12));

    JPanel nameRow = new JPanel();
    nameRow.setLayout(new BoxLayout(nameRow, BoxLayout.X_AXIS));
    nameRow.setOpaque(false);
    nameRow.setAlignmentX(Component.LEFT_ALIGNMENT);

    JLabel dotLabel = new JLabel(new GreenDotIcon(6));
    nameRow.add(dotLabel);

    JLabel nameLabel = new JLabel(client.name() != null ? client.name() : "unknown");
    nameLabel.setForeground(Style.text());
    nameLabel.setFont(nameLabel.getFont().deriveFont(Font.BOLD));
    nameRow.add(nameLabel);
    nameRow.add(Box.createHorizontalGlue());
    panel.add(nameRow);

    JPanel versionRow = new JPanel();
    versionRow.setLayout(new BoxLayout(versionRow, BoxLayout.X_AXIS));
    versionRow.setOpaque(false);
    versionRow.setAlignmentX(Component.LEFT_ALIGNMENT);
    versionRow.setBorder(BorderFactory.createEmptyBorder(1, 14, 0, 0));
    String versionText = (client.version() != null && !client.version().isEmpty()
        ? client.version() : "unknown");
    JLabel versionLabel = new JLabel(versionText);
    versionLabel.setForeground(Style.mutedText());
    versionLabel.setFont(versionLabel.getFont().deriveFont(
        Math.max(10f, 11f * Editor.preferences().getUiScale())));
    versionRow.add(versionLabel);
    versionRow.add(Box.createHorizontalGlue());
    panel.add(versionRow);

    return panel;
  }

  private static String formatMcpAction(String toolName) {
    StringBuilder result = new StringBuilder();
    for (String part : toolName.split("-")) {
      if (!result.isEmpty()) {
        result.append(' ');
      }
      if (!part.isEmpty()) {
        result.append(Character.toUpperCase(part.charAt(0))).append(part.substring(1));
      }
    }
    return result.toString();
  }

  private static String getToolDisplayName() {
    if (ToolManager.instance().getActiveTool() == null) {
      return Resources.strings().get("tool_select");
    }
    if (!(ToolManager.instance().getActiveTool() instanceof PointerTool)) {
      var layer = ToolManager.instance().getActiveTileLayer();
      String layerName = layer != null && layer.getName() != null && !layer.getName().isBlank()
          ? layer.getName()
          : Resources.strings().get("status_noTileLayer");
      return ToolManager.instance().getActiveTool().getName() + "  |  " + layerName;
    }
    return switch (Editor.instance().getMapComponent().getTransformMode()) {
      case NONE -> Resources.strings().get("tool_select");
      case MOVE -> Resources.strings().get("tool_move");
      case RESIZE -> Resources.strings().get("tool_resize");
      case CREATE -> Resources.strings().get("tool_create");
    };
  }

  private JPanel separator() {
    JPanel wrapper = new JPanel();
    wrapper.setOpaque(false);
    wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.X_AXIS));
    wrapper.setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

    JPanel line = new JPanel();
    line.setPreferredSize(new Dimension(1, 12));
    line.setMaximumSize(new Dimension(1, 12));
    line.setBackground(Style.border());
    this.separatorLines.add(line);
    wrapper.add(line);
    return wrapper;
  }

  private static final class McpStatusIcon implements Icon {
    private static final int HEIGHT = 16;

    @Override public int getIconWidth() {
      int count = McpServer.instance().getConnectedClientCount();
      return count > 0 ? 52 : 36;
    }
    @Override public int getIconHeight() { return HEIGHT; }

    @Override public void paintIcon(java.awt.Component component, Graphics graphics, int x, int y) {
      Graphics2D g = (Graphics2D) graphics.create();
      try {
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
        McpServer server = McpServer.instance();
        ActionStatus action = server.getActionStatus();
        Color dotColor = mcpColor(server.isRunning(), action);
        int clientCount = server.getConnectedClientCount();

        g.setFont(component.getFont().deriveFont(Font.BOLD, 9f));
        FontMetrics fm = g.getFontMetrics();

        int textWidth = fm.stringWidth("MCP");
        int contentWidth = textWidth + 12; // "MCP" + dot space
        if (clientCount > 0) {
          contentWidth += 6 + fm.stringWidth(String.valueOf(clientCount));
        }

        int pillWidth = contentWidth + 10; // 5px left & right padding

        // Background pill
        int fillAlpha = action.state() == ActionState.RUNNING
            ? 38 + (int) (38 * (1 + Math.sin(System.nanoTime() / 120_000_000.0)) / 2)
            : 28;
        g.setColor(new Color(dotColor.getRed(), dotColor.getGreen(), dotColor.getBlue(), fillAlpha));
        g.fillRoundRect(x, y, pillWidth - 1, HEIGHT - 1, 8, 8);
        g.setColor(dotColor);
        g.drawRoundRect(x, y, pillWidth - 1, HEIGHT - 1, 8, 8);

        // "MCP" text
        g.drawString("MCP", x + 5, y + 11);

        // Status dot
        int dotX = x + 5 + textWidth + 5;
        g.fillOval(dotX, y + 5, 5, 5);

        // Client count badge (only rendered when > 0)
        if (clientCount > 0) {
          String countStr = String.valueOf(clientCount);
          int countX = dotX + 8;
          g.drawString(countStr, countX, y + 11);
        }
      } finally {
        g.dispose();
      }
    }
  }

  private static final class GreenDotIcon implements Icon {
    private final int size;

    private GreenDotIcon(int size) {
      this.size = size;
    }

    @Override public int getIconWidth() { return this.size + 4; }
    @Override public int getIconHeight() { return this.size; }

    @Override public void paintIcon(Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.COLOR_GREEN);
      int drawY = y + Math.max(0, (c.getHeight() - this.size) / 2);
      g2.fillOval(x, drawY, this.size, this.size);
      g2.dispose();
    }
  }
}
