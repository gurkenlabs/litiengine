package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.resources.Resources;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Controller;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.FileDrop;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.JToggleButton;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AssetList extends JSplitPane implements Controller {
  private static final int RESOURCE_EXPLORER_MIN_WIDTH = 180;
  private static final int RESOURCE_EXPLORER_MAX_WIDTH = 320;
  private static final int RESOURCE_EXPLORER_DEFAULT_WIDTH = 220;
  private static final int ASSET_CONTENT_MIN_WIDTH = 360;
  private static final int SEARCH_WIDTH = 340;
  private final AssetPanel assetPanel;
  private final AssetTree assetTree;
  private final JTextField searchField;
  private final JSlider zoomSlider;
  private final JLabel summaryLabel;
  private final JPanel toolbar;
  private final JScrollPane scrollPane;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    UI.configureSplitPane(this);
    this.setResizeWeight(0.0);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);
    this.assetPanel.setChangedCallback(this::updateSummary);

    this.assetTree.setBorder(javax.swing.BorderFactory.createEmptyBorder(
      Style.SPACE_SMALL, Style.SPACE_SMALL, Style.SPACE_SMALL, Style.SPACE_SMALL));

    JPanel leftPanel = new JPanel(new BorderLayout()) {
      @Override
      public void updateUI() {
        super.updateUI();
        setBackground(Style.assetExplorerBackground());
      }
    };
    leftPanel.setOpaque(true);
    leftPanel.setBorder(null);
    leftPanel.setMinimumSize(new Dimension(RESOURCE_EXPLORER_MIN_WIDTH, 0));
    leftPanel.setPreferredSize(new Dimension(RESOURCE_EXPLORER_DEFAULT_WIDTH, 0));
    leftPanel.setMaximumSize(new Dimension(RESOURCE_EXPLORER_MAX_WIDTH, Integer.MAX_VALUE));
    leftPanel.add(assetTree, BorderLayout.CENTER);
    this.setLeftComponent(leftPanel);

    new FileDrop(assetPanel, files -> Editor.instance().importResources(files));

    // Search field
    this.searchField = new JTextField() {
      @Override
      public void updateUI() {
        super.updateUI();
        setBorder(javax.swing.BorderFactory.createEmptyBorder());
        setOpaque(false);
        putClientProperty("JComponent.outline", "none");
      }

      @Override
      protected void paintBorder(Graphics g) {
        // The parent search box owns the only visible border.
      }
    };
    this.searchField.putClientProperty(
      DarkTextUI.KEY_DEFAULT_TEXT, Resources.strings().get("assetlist_search_placeholder"));
    this.searchField.setToolTipText(Resources.strings().get("assetlist_search_placeholder"));
    this.searchField.getAccessibleContext().setAccessibleName(
      Resources.strings().get("assetlist_search"));
    this.searchField.getAccessibleContext().setAccessibleDescription(
      Resources.strings().get("assetlist_search_description"));
    this.searchField.setBorder(javax.swing.BorderFactory.createEmptyBorder());
    this.searchField.setOpaque(false);
    this.searchField.putClientProperty("JComponent.outline", "none");
    this.searchField.setMargin(new Insets(0, 0, 0, 0));
    this.searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) { filter(); }
      @Override public void removeUpdate(DocumentEvent e) { filter(); }
      @Override public void changedUpdate(DocumentEvent e) { filter(); }
      private void filter() {
        assetPanel.setFilterText(searchField.getText());
        updateSummary();
      }
    });

    RoundedSearchBox searchBox = new RoundedSearchBox(this.searchField, SEARCH_WIDTH);
    searchBox.setMinimumSize(new Dimension(200, Style.CONTROL_HEIGHT));
    searchBox.setMaximumSize(new Dimension(SEARCH_WIDTH, Style.CONTROL_HEIGHT));
    searchBox.getClearButton().addActionListener(e -> {
      searchField.setText("");
      assetPanel.setFilterText("");
      updateSummary();
    });

    this.summaryLabel = new JLabel();
    this.summaryLabel.setForeground(Style.mutedText());
    this.summaryLabel.setFont(this.summaryLabel.getFont().deriveFont(
      Math.max(10f, this.summaryLabel.getFont().getSize2D() - 1f)));

    this.zoomSlider = new JSlider(96, 150, Editor.preferences().getAssetCardSize());
    this.zoomSlider.setPreferredSize(new Dimension(100, Style.CONTROL_HEIGHT));
    this.zoomSlider.setOpaque(false);
    this.zoomSlider.setToolTipText(Resources.strings().get("assetlist_card_size"));
    this.zoomSlider.getAccessibleContext().setAccessibleName(
      Resources.strings().get("assetlist_card_size"));
    this.zoomSlider.getAccessibleContext().setAccessibleDescription(
      Resources.strings().get("assetlist_card_size_description"));
    this.zoomSlider.addChangeListener(e -> {
      assetPanel.setCardSize(this.zoomSlider.getValue());
      Editor.preferences().setAssetCardSize(this.zoomSlider.getValue());
    });

    boolean initialCompact = Editor.preferences().isCompactMode();
    JToggleButton densityToggle = Style.iconToggleButton(initialCompact ? new ListIcon() : new GridIcon(), initialCompact);
    densityToggle.setToolTipText(Resources.strings().get("assetlist_compact"));
    densityToggle.getAccessibleContext().setAccessibleName(Resources.strings().get("assetlist_compact"));
    densityToggle.getAccessibleContext().setAccessibleDescription(
      Resources.strings().get("assetlist_compact_description"));
    densityToggle.addActionListener(e -> {
      assetPanel.setCompact(densityToggle.isSelected());
      densityToggle.setIcon(densityToggle.isSelected() ? new ListIcon() : new GridIcon());
      this.zoomSlider.setEnabled(!densityToggle.isSelected());
      Editor.preferences().setCompactMode(densityToggle.isSelected());
      updateSummary();
    });
    assetPanel.setCardSize(Editor.preferences().getAssetCardSize());
    assetPanel.setCompact(initialCompact);
    this.zoomSlider.setEnabled(!initialCompact);

    this.toolbar = new JPanel(new FlowLayout(
      FlowLayout.TRAILING, Style.SPACE_MEDIUM, Style.SPACE_MEDIUM));
    this.toolbar.setOpaque(false);
    this.toolbar.add(this.summaryLabel);
    this.toolbar.add(searchBox);
    this.toolbar.add(densityToggle);
    this.toolbar.add(this.zoomSlider);

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setOpaque(false);
    rightPanel.setMinimumSize(new Dimension(ASSET_CONTENT_MIN_WIDTH, 0));
    rightPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE));

    this.scrollPane =
      new JScrollPane(
        assetPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    this.scrollPane.getVerticalScrollBar().setBlockIncrement(48);
    this.scrollPane.setBorder(null);
    this.scrollPane.getViewport().setBackground(Style.assetExplorerBackground());
    rightPanel.add(this.scrollPane, BorderLayout.CENTER);

    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
      int location = Math.max(
        RESOURCE_EXPLORER_MIN_WIDTH,
        Math.min(RESOURCE_EXPLORER_MAX_WIDTH, this.getDividerLocation()));
      if (location != this.getDividerLocation()) {
        this.setDividerLocation(location);
      } else {
        Editor.preferences().setAssetsSplitter(location);
      }
    });
    int preferredDivider = Editor.preferences().getAssetsSplitter() != 0
      ? Editor.preferences().getAssetsSplitter()
      : RESOURCE_EXPLORER_DEFAULT_WIDTH;
    this.setDividerLocation(Math.max(
      RESOURCE_EXPLORER_MIN_WIDTH,
      Math.min(RESOURCE_EXPLORER_MAX_WIDTH, preferredDivider)));

    this.setRightComponent(rightPanel);
    this.assetTree.selectDefault();
    updateSummary();
  }

  public AssetTree getAssetTree() {
    return this.assetTree;
  }

  public JPanel getToolbar() {
    return this.toolbar;
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.summaryLabel != null) {
      this.summaryLabel.setForeground(Style.mutedText());
    }
    if (this.scrollPane != null) {
      this.scrollPane.getViewport().setBackground(Style.assetExplorerBackground());
    }
  }

  @Override
  public void refresh() {
    this.assetTree.forceUpdate();
    this.searchField.setText("");
    updateSummary();
  }

  public void updateSummary() {
    if (this.summaryLabel != null) {
      int visible = this.assetPanel.getVisibleItemCount();
      int total = this.assetPanel.getTotalItemCount();
      String count = visible == total
        ? Resources.strings().get("assetlist_asset_count", total)
        : Resources.strings().get("assetlist_filtered_asset_count", visible, total);
      this.summaryLabel.setText(count);
    }
    updateAssetInspector();
  }

  private void updateAssetInspector() {
    AssetPanelItem focused = this.assetPanel.getFocusedItem();
    Object origin = focused != null ? focused.getOrigin() : null;
    if (origin instanceof Tileset tileset) {
      UI.showTilesetInspector(tileset);
    } else if (origin instanceof SpritesheetResource spritesheetResource) {
      UI.showSpriteInspector(spritesheetResource);
    } else if (!hasAssetInspectorTarget(origin)) {
      UI.hideAssetInspector();
    }
  }

  static boolean hasAssetInspectorTarget(Object origin) {
    return origin instanceof Tileset || origin instanceof SpritesheetResource;
  }

  private static final class GridIcon implements Icon {
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
    @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.text());
      for (int row = 0; row < 2; row++) {
        for (int col = 0; col < 2; col++) {
          g2.drawRoundRect(x + col * 8 + 1, y + row * 8 + 1, 5, 5, 2, 2);
        }
      }
      g2.dispose();
    }
  }

  private static final class ListIcon implements Icon {
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
    @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.text());
      for (int row = 0; row < 3; row++) {
        int yy = y + 3 + row * 5;
        g2.fillRoundRect(x + 1, yy, 3, 3, 2, 2);
        g2.drawLine(x + 7, yy + 1, x + 15, yy + 1);
      }
      g2.dispose();
    }
  }
}
