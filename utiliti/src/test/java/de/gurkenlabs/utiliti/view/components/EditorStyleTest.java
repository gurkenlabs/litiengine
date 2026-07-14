package de.gurkenlabs.utiliti.view.components;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.gurkenlabs.litiengine.test.SwingTestSuite;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.controller.Editor;
import java.awt.image.BufferedImage;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.UIManager;
import javax.swing.SwingUtilities;
import javax.swing.plaf.UIResource;
import javax.swing.border.EmptyBorder;
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
  void expandableCardHeaderSupportsKeyboardToggle() {
    ExpandableCard card = new ExpandableCard("General", new JPanel(), true);
    JPanel header = (JPanel) card.getComponent(0);

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

    assertTrue(cell.isOpaque());
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
  void inspectorDividerTranslatesPersistedViewportPosition() {
    int divider = UI.initialInspectorDivider(1920, 300, 380, 380, 1200);

    assertEquals(1504, divider);
  }

  @Test
  void assetPanelDividerKeepsHeightWithinBounds() {
    assertEquals(576, UI.constrainBottomDivider(1000, 4, 100));
    assertEquals(816, UI.constrainBottomDivider(1000, 4, 900));
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

      assertEquals(Style.raisedSurface(), tags.getBackground());
      assertEquals(Style.background(), tilesetEditor.getBackground());
    } finally {
      UI.setTheme(original);
      tilesetEditor.dispose();
    }
  }
}
