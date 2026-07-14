package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.environment.tilemap.xml.Tileset;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.litiengine.resources.SpritesheetResource;
import de.gurkenlabs.utiliti.controller.Controller;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.FileDrop;
import de.gurkenlabs.utiliti.model.Style;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
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
  private final AssetPanel assetPanel;
  private final AssetTree assetTree;
  private final JTextField searchField;
  private final JSlider zoomSlider;
  private final JLabel titleLabel;
  private final JLabel summaryLabel;
  private final JScrollPane scrollPane;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    UI.configureSplitPane(this);
    this.setResizeWeight(0.0);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);
    this.assetPanel.setChangedCallback(this::updateSummary);

    this.assetTree.setBorder(javax.swing.BorderFactory.createEmptyBorder(
      Style.SPACE_MEDIUM, Style.SPACE_SMALL, Style.SPACE_MEDIUM, Style.SPACE_SMALL));

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setOpaque(false);
    leftPanel.setBorder(null);
    leftPanel.add(assetTree, BorderLayout.CENTER);
    this.setLeftComponent(leftPanel);

    new FileDrop(
      assetPanel,
      files -> {
        List<Path> droppedImages = new ArrayList<>();
        List<Path> droppedAnimations = new ArrayList<>();
        for (Path file : files) {
          if (ImageFormat.isSupported(file)) {
            droppedImages.add(file);
          } else if (file.getFileName() != null
            && file.getFileName().toString().toLowerCase(Locale.ROOT).endsWith(".json")) {
            droppedAnimations.add(file);
          }
        }

        if (!droppedImages.isEmpty()) {
          Editor.instance().importSpriteSheets(droppedImages.toArray(new Path[0]));
        }
        if (!droppedAnimations.isEmpty()) {
          Editor.instance().importAnimations(droppedAnimations.toArray(new Path[0]));
        }
      });

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
    this.searchField.putClientProperty(DarkTextUI.KEY_DEFAULT_TEXT, "Search assets...");
    this.searchField.setToolTipText("Search assets...");
    this.searchField.getAccessibleContext().setAccessibleName("Search assets");
    this.searchField.getAccessibleContext().setAccessibleDescription("Filter the visible assets by name");
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

    RoundedSearchBox searchBox = new RoundedSearchBox(this.searchField, 240);
    searchBox.setMinimumSize(new Dimension(80, Style.CONTROL_HEIGHT));
    searchBox.getClearButton().addActionListener(e -> {
      searchField.setText("");
      assetPanel.setFilterText("");
      updateSummary();
    });

    this.titleLabel = new JLabel("Resources");
    this.titleLabel.setForeground(Style.text());
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(Font.BOLD));

    this.summaryLabel = new JLabel();
    this.summaryLabel.setForeground(Style.mutedText());
    this.summaryLabel.setFont(this.summaryLabel.getFont().deriveFont(
      Math.max(10f, this.summaryLabel.getFont().getSize2D() - 1f)));

    JPanel heading = new JPanel(new BorderLayout(0, 1));
    heading.setOpaque(false);
    heading.add(this.titleLabel, BorderLayout.NORTH);
    heading.add(this.summaryLabel, BorderLayout.SOUTH);

    this.zoomSlider = new JSlider(96, 150, Editor.preferences().getAssetCardSize());
    this.zoomSlider.setPreferredSize(new Dimension(100, Style.CONTROL_HEIGHT));
    this.zoomSlider.setOpaque(false);
    this.zoomSlider.setToolTipText("Asset card size");
    this.zoomSlider.getAccessibleContext().setAccessibleName("Asset card size");
    this.zoomSlider.getAccessibleContext().setAccessibleDescription("Adjust the size of cards in the asset grid");
    this.zoomSlider.addChangeListener(e -> {
      assetPanel.setCardSize(this.zoomSlider.getValue());
      Editor.preferences().setAssetCardSize(this.zoomSlider.getValue());
    });

    boolean initialCompact = Editor.preferences().isCompactMode();
    JToggleButton densityToggle = Style.iconToggleButton(initialCompact ? new ListIcon() : new GridIcon(), initialCompact);
    densityToggle.setToolTipText("Compact asset list");
    densityToggle.getAccessibleContext().setAccessibleName("Compact asset list");
    densityToggle.getAccessibleContext().setAccessibleDescription("Toggle between the compact list and card grid");
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

    JPanel tools = new JPanel(new FlowLayout(FlowLayout.TRAILING, Style.SPACE_SMALL, 0));
    tools.setOpaque(false);
    tools.add(densityToggle);
    tools.add(this.zoomSlider);

    JPanel topBar = new JPanel(new BorderLayout(Style.SPACE_LARGE, 0));
    topBar.setOpaque(false);
    topBar.setBorder(javax.swing.BorderFactory.createEmptyBorder(
      Style.SPACE_SMALL, Style.SPACE_MEDIUM, Style.SPACE_SMALL, Style.SPACE_MEDIUM));
    topBar.add(heading, BorderLayout.WEST);
    topBar.add(searchBox, BorderLayout.CENTER);
    topBar.add(tools, BorderLayout.EAST);

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setOpaque(false);
    rightPanel.add(topBar, BorderLayout.NORTH);

    this.scrollPane =
      new JScrollPane(
        assetPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    this.scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    this.scrollPane.getVerticalScrollBar().setBlockIncrement(48);
    this.scrollPane.setBorder(null);
    this.scrollPane.getViewport().setBackground(Style.background());
    rightPanel.add(this.scrollPane, BorderLayout.CENTER);

    this.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(
      Editor.preferences().getAssetsSplitter() != 0
        ? Editor.preferences().getAssetsSplitter()
        : 200);

    this.setRightComponent(rightPanel);
    this.assetTree.selectDefault();
    updateSummary();
  }

  public AssetTree getAssetTree() {
    return this.assetTree;
  }

  @Override
  public void updateUI() {
    super.updateUI();
    if (this.titleLabel != null) {
      this.titleLabel.setForeground(Style.text());
    }
    if (this.summaryLabel != null) {
      this.summaryLabel.setForeground(Style.mutedText());
    }
    if (this.scrollPane != null) {
      this.scrollPane.getViewport().setBackground(Style.background());
    }
  }

  @Override
  public void refresh() {
    this.assetTree.forceUpdate();
    this.searchField.setText("");
    updateSummary();
  }

  public void updateSummary() {
    if (this.titleLabel != null) {
      String breadcrumb = this.assetTree.getCurrentBreadcrumb();
      AssetPanelItem focused = this.assetPanel.getFocusedItem();
      this.titleLabel.setText(breadcrumb);
      if (focused != null) {
        String details = focused.getDetailsSummary();
        String tooltip = focused.getName();
        if (!details.isBlank()) {
          tooltip += " - " + details;
        }
        this.titleLabel.setToolTipText(tooltip);
      } else {
        this.titleLabel.setToolTipText(breadcrumb);
      }
    }
    if (this.summaryLabel != null) {
      int visible = this.assetPanel.getVisibleItemCount();
      int total = this.assetPanel.getTotalItemCount();
      String count = visible == total ? total + " assets" : visible + " of " + total + " assets";
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
    } else if (this.assetPanel.getCurrentType() != AssetPanel.AssetType.TILESET
      && this.assetPanel.getCurrentType() != AssetPanel.AssetType.SPRITESHEET) {
      UI.hideAssetInspector();
    }
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
