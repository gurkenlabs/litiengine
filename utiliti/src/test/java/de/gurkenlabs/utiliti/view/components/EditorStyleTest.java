package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.ImageIcon;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.plaf.UIResource;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(SwingTestSuite.class)
class EditorStyleTest {

  @Test
  void toolbarButtonsUseCompactFocusableContract() {

    JButton button = Style.iconButton(new ImageIcon(new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB)));

    assertTrue(button.isFocusable());
    assertEquals(Style.TOOLBAR_BUTTON_SIZE, button.getPreferredSize().width);
    assertEquals(Style.TOOLBAR_BUTTON_SIZE, button.getPreferredSize().height);
    assertEquals(Style.ButtonVariant.TOOLBAR, button.getClientProperty("Editor.buttonVariant"));

    button.setToolTipText("Add layer");
    assertEquals("Add layer", button.getAccessibleContext().getAccessibleName());
  }

  @Test
  void enabledDestructiveButtonsUseRedForeground() {
    BufferedImage source = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    source.setRGB(0, 0, java.awt.Color.WHITE.getRGB());
    JButton button = Style.iconButton(new ImageIcon(source));
    Style.styleButton(button, Style.ButtonVariant.DESTRUCTIVE);
    button.setSize(button.getPreferredSize());
    BufferedImage image = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);

    button.paint(image.getGraphics());

    assertEquals(Style.COLOR_RED, button.getForeground());
    BufferedImage iconImage = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
    button.getIcon().paintIcon(button, iconImage.getGraphics(), 0, 0);
    assertEquals(Style.COLOR_RED.getRGB(), iconImage.getRGB(0, 0));

    button.setEnabled(false);
    button.paint(image.getGraphics());

    assertEquals(Style.COLOR_DISABLED_TEXT, button.getForeground());
  }

  @Test
  void destructiveIconsRenderAtDisplayScale() {
    double[] renderedScale = new double[2];
    Icon source = new Icon() {
      @Override
      public void paintIcon(java.awt.Component component, java.awt.Graphics graphics, int x, int y) {
        java.awt.Graphics2D graphics2D = (java.awt.Graphics2D) graphics;
        renderedScale[0] = graphics2D.getTransform().getScaleX();
        renderedScale[1] = graphics2D.getTransform().getScaleY();
        graphics2D.fillRect(x, y, getIconWidth(), getIconHeight());
      }

      @Override
      public int getIconWidth() {
        return 16;
      }

      @Override
      public int getIconHeight() {
        return 16;
      }
    };
    JButton button = Style.iconButton(source);
    Style.styleButton(button, Style.ButtonVariant.DESTRUCTIVE);
    BufferedImage image = new BufferedImage(32, 48, BufferedImage.TYPE_INT_ARGB);
    java.awt.Graphics2D graphics = image.createGraphics();
    graphics.scale(2, 3);

    button.getIcon().paintIcon(button, graphics, 0, 0);
    graphics.dispose();

    assertEquals(2.0, renderedScale[0]);
    assertEquals(3.0, renderedScale[1]);
    assertEquals(button.getForeground().getRGB(), image.getRGB(24, 36));
  }

  @Test
  void textButtonTooltipReplacesGenericAccessibleName() {
    JButton button = Style.textButton("+");

    assertEquals("Add", button.getAccessibleContext().getAccessibleName());
    button.setToolTipText("Add terrain set");

    assertEquals("Add terrain set", button.getAccessibleContext().getAccessibleName());
  }

  @Test
  void searchBoxUsesCompactControlHeight() {
    RoundedSearchBox searchBox = new RoundedSearchBox(new JTextField(), 200);

    assertEquals(Style.CONTROL_HEIGHT, searchBox.getPreferredSize().height);
    assertTrue(searchBox.getClearButton().isFocusable());
    assertTrue(Style.mutedText() instanceof UIResource);
  }

  @Test
  void workspaceModeButtonsAlignWithDockedPanelAndUseSharedStyle() {
    JPanel rail = (JPanel) UI.initWorkspaceModeBar();
    JToggleButton mapButton = (JToggleButton) rail.getComponent(0);

    assertEquals(0, rail.getInsets().top);
    assertEquals(Style.SPACE_MEDIUM, rail.getInsets().left);
    assertEquals(
        mapButton.getPreferredSize().width + Style.SPACE_MEDIUM,
        rail.getPreferredSize().width);
    assertEquals(Style.SPACE_MEDIUM, rail.getComponent(1).getPreferredSize().height);
    assertEquals(Style.ButtonVariant.TOOLBAR, mapButton.getClientProperty("Editor.buttonVariant"));
    assertFalse(mapButton.isBorderPainted());
  }

  @Test
  void expandableCardHeaderSupportsKeyboardToggle() {
    ExpandableCard card = new ExpandableCard("General", new JPanel(), true);
    JPanel header = (JPanel) card.getComponent(0);
    JPanel content = (JPanel) card.getComponent(1);

    assertEquals(Style.SPACE_SMALL, card.getInsets().bottom);

    card.setInspectorContentInsets();

    assertEquals(Style.SPACE_MEDIUM, content.getInsets().left);
    assertEquals(Style.SPACE_MEDIUM, content.getInsets().right);

    card.setSize(160, 80);
    card.doLayout();
    BufferedImage image = new BufferedImage(160, 80, BufferedImage.TYPE_INT_ARGB);
    Graphics2D graphics = image.createGraphics();
    graphics.setColor(Color.MAGENTA);
    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
    card.paint(graphics);
    graphics.dispose();

    assertEquals(Style.surface().getRGB(), image.getRGB(80, 75));
    assertEquals(Color.MAGENTA.getRGB(), image.getRGB(80, 79));

    header.getActionMap().get("toggle").actionPerformed(null);

    assertFalse(card.isExpanded());
    assertTrue(header.isFocusable());

    card.setTitle("Transform");
    assertEquals("Transform", header.getAccessibleContext().getAccessibleName());
  }

  @Test
  void resourceTreeUsesNeutralWorkspaceColors() {
    AssetTree tree = new AssetTree(new AssetPanel());
    Object node = tree.getModel().getChild(tree.getModel().getRoot(), 0);
    JComponent cell = (JComponent) tree.getCellRenderer().getTreeCellRendererComponent(
        tree, node, true, true, false, 0, true);

    assertFalse(cell.isOpaque());
    assertTrue(cell.getBorder() instanceof EmptyBorder);
    assertEquals("None", tree.getClientProperty("JTree.lineStyle"));
    assertEquals(Style.assetExplorerBackground(), tree.getBackground());
    assertFalse(tree.isOpaque());
  }

  @Test
  void inspectorDividerReservesFullHeightInspectorSpace() {
    int divider = UI.initialInspectorDivider(1920, 300, 380, 380, 0);

    assertEquals(380, 1920 - divider);
  }

  @Test
  void sceneGraphWidthKeepsCommandStripVisible() {
    assertEquals(340, UI.constrainSceneGraphWidth(260));
    assertEquals(400, UI.constrainSceneGraphWidth(400));
    assertEquals(480, UI.constrainSceneGraphWidth(520));
  }

  @Test
  void splitPaneDividerIsAnInvisibleDragTarget() {
    JSplitPane splitPane = new JSplitPane();

    UI.configureSplitPane(splitPane);

    BasicSplitPaneUI splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
    assertNull(splitPane.getBorder());
    assertEquals(Style.background(), splitPane.getBackground());
    assertNull(splitPaneUI.getDivider().getBorder());

    splitPane.updateUI();

    splitPaneUI = (BasicSplitPaneUI) splitPane.getUI();
    assertNull(splitPane.getBorder());
    assertNull(splitPaneUI.getDivider().getBorder());

    splitPaneUI.getDivider().setSize(8, 24);
    BufferedImage dividerImage = new BufferedImage(8, 24, BufferedImage.TYPE_INT_ARGB);
    Graphics2D dividerGraphics = dividerImage.createGraphics();
    dividerGraphics.setColor(Color.MAGENTA);
    dividerGraphics.fillRect(0, 0, dividerImage.getWidth(), dividerImage.getHeight());
    splitPaneUI.getDivider().paint(dividerGraphics);
    dividerGraphics.dispose();

    assertEquals(Style.background().getRGB(), dividerImage.getRGB(4, 12));
  }

  @Test
  void inspectorDividerTranslatesPersistedViewportPosition() {
    int divider = UI.initialInspectorDivider(1920, 300, 380, 380, 1200);

    assertEquals(1504, divider);
  }

  @Test
  void assetPanelDividerKeepsHeightWithinBounds() {
    assertEquals(576, UI.constrainBottomDivider(1000, 4, 100));
    assertEquals(716, UI.constrainBottomDivider(1000, 4, 900));
    assertEquals(700, UI.constrainBottomDivider(1000, 4, 700));
  }

  @Test
  void themeSwitchReplacesDarkOverridesAndKeepsCompactMetrics() {
    Style.Theme original = Editor.preferences().getTheme();
    try {
      UI.setTheme(Style.Theme.DARK);
      Object darkBackground = UIManager.get("Panel.background");

      UI.setTheme(Style.Theme.LIGHT);

      assertNotEquals(darkBackground, UIManager.get("Panel.background"));
      assertEquals(Style.CONTROL_HEIGHT, UIManager.get("Button.minimumHeight"));
    } finally {
      UI.setTheme(original);
    }
  }

  @Test
  void customInspectorSurfacesRefreshAcrossThemes() {
    Style.Theme original = Editor.preferences().getTheme();
    TagPanel tags = new TagPanel();
    TilesetEditorPanel tilesetEditor = new TilesetEditorPanel();
    try {
      UI.setTheme(Style.Theme.LIGHT);
      SwingUtilities.updateComponentTreeUI(tags);
      SwingUtilities.updateComponentTreeUI(tilesetEditor);

      UI.setTheme(Style.Theme.DARK);
      SwingUtilities.updateComponentTreeUI(tags);
      SwingUtilities.updateComponentTreeUI(tilesetEditor);

      assertEquals(Style.inputBackground(), tags.getBackground());
      assertEquals(Style.background(), tilesetEditor.getBackground());
    } finally {
      UI.setTheme(original);
      tilesetEditor.dispose();
    }
  }

  @Test
  void disabledButtonsUseGrayVectorIcons() {
    BufferedImage source = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = source.createGraphics();
    g.setColor(Color.WHITE);
    g.fillRect(0, 0, 16, 16);
    g.dispose();

    JButton button = Style.iconButton(new ImageIcon(source));
    assertTrue(button.getDisabledIcon() instanceof Style.DisabledVectorIcon);

    button.setEnabled(false);
    BufferedImage rendered = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    button.getDisabledIcon().paintIcon(button, rendered.getGraphics(), 0, 0);

    assertEquals(Style.disabledIconColor().getRGB(), rendered.getRGB(8, 8));
  }

  @Test
  void dynamicIconChangeUpdatesDisabledIcon() {
    BufferedImage icon1 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    BufferedImage icon2 = new BufferedImage(16, 16, BufferedImage.TYPE_INT_ARGB);
    JButton button = Style.iconButton(new ImageIcon(icon1));

    Icon initialDisabled = button.getDisabledIcon();
    assertTrue(initialDisabled instanceof Style.DisabledVectorIcon);

    button.setIcon(new ImageIcon(icon2));
    Icon updatedDisabled = button.getDisabledIcon();
    assertTrue(updatedDisabled instanceof Style.DisabledVectorIcon);
    assertEquals(icon2, ((ImageIcon) ((Style.DisabledVectorIcon) updatedDisabled).getDelegate()).getImage());
  }

  @Test
  void dumpDarklafTreeDiagnostics() {
    UI.setTheme(Style.Theme.DARK);

    System.out.println("=== DARKLAF DIAGNOSTICS DUMP ===");
    System.out.println("Darklaf version: 3.1.1");
    System.out.println("Tree.background: " + toHex(UIManager.getColor("Tree.background")));
    System.out.println("Tree.textBackground: " + toHex(UIManager.getColor("Tree.textBackground")));
    System.out.println("Tree.selectionBackground: " + toHex(UIManager.getColor("Tree.selectionBackground")));
    System.out.println("Viewport.background: " + toHex(UIManager.getColor("Viewport.background")));
    System.out.println("Panel.background: " + toHex(UIManager.getColor("Panel.background")));

    SceneGraph sceneGraph = new SceneGraph();
    JTree sceneTree = findChildTree(sceneGraph);
    System.out.println("\nScene Graph:");
    System.out.println("  component class: " + (sceneTree != null ? sceneTree.getClass().getName() : "null"));
    System.out.println("  UI delegate: " + (sceneTree != null ? sceneTree.getUI().getClass().getName() : "null"));
    System.out.println("  opaque: " + (sceneTree != null ? sceneTree.isOpaque() : "null"));
    System.out.println("  component background: " + (sceneTree != null ? toHex(sceneTree.getBackground()) : "null"));

    ScriptWorkspacePanel scriptWorkspace = new ScriptWorkspacePanel();
    JTree scriptTree = findChildTree(scriptWorkspace);
    System.out.println("\nScripts Tree:");
    System.out.println("  component class: " + (scriptTree != null ? scriptTree.getClass().getName() : "null"));
    System.out.println("  UI delegate: " + (scriptTree != null ? scriptTree.getUI().getClass().getName() : "null"));
    System.out.println("  opaque: " + (scriptTree != null ? scriptTree.isOpaque() : "null"));
    System.out.println("  component background: " + (scriptTree != null ? toHex(scriptTree.getBackground()) : "null"));

    ScriptAssetTree scriptAssetTree = new ScriptAssetTree(new AssetPanel());
    System.out.println("\nScripts Categories:");
    System.out.println("  component class: " + scriptAssetTree.getClass().getName());
    System.out.println("  UI delegate: " + scriptAssetTree.getUI().getClass().getName());
    System.out.println("  opaque: " + scriptAssetTree.isOpaque());
    System.out.println("  component background: " + toHex(scriptAssetTree.getBackground()));
    System.out.println("=================================");
  }

  @Test
  void renderedTreePixelsMatchDarkBackground() {
    UI.setTheme(Style.Theme.DARK);

    SceneGraph sceneGraph = new SceneGraph();
    sceneGraph.setSize(240, 400);
    sceneGraph.doLayout();
    JTree sceneTree = findChildTree(sceneGraph);
    assertNotNull(sceneTree);
    sceneTree.setSize(240, 400);

    BufferedImage image = new BufferedImage(240, 400, BufferedImage.TYPE_INT_ARGB);
    Graphics2D g = image.createGraphics();
    g.setColor(Style.background());
    g.fillRect(0, 0, 240, 400);
    sceneTree.paint(g);
    g.dispose();

    // Check pixel at empty space (e.g., x=10, y=300) vs unselected row space (e.g., x=220, y=10)
    int emptyColor = image.getRGB(10, 350) & 0xFFFFFF;
    int rowColor = image.getRGB(220, 10) & 0xFFFFFF;
    int expected = Style.background().getRGB() & 0xFFFFFF;

    assertEquals(Integer.toHexString(expected), Integer.toHexString(emptyColor));
    assertEquals(Integer.toHexString(expected), Integer.toHexString(rowColor));
  }

  private static void assertNotNull(Object obj) {
    org.junit.jupiter.api.Assertions.assertNotNull(obj);
  }

  private static JTree findChildTree(java.awt.Container container) {
    for (java.awt.Component comp : container.getComponents()) {
      if (comp instanceof JTree tree) return tree;
      if (comp instanceof java.awt.Container childContainer) {
        JTree tree = findChildTree(childContainer);
        if (tree != null) return tree;
      }
    }
    return null;
  }

  private static String toHex(Color color) {
    if (color == null) return "null";
    return String.format("#%02X%02X%02X (alpha=%d)", color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
  }
}





