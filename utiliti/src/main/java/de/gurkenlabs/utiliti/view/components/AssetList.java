package de.gurkenlabs.utiliti.view.components;

import com.github.weisj.darklaf.ui.text.DarkTextUI;
import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.utiliti.controller.Controller;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.FileDrop;
import de.gurkenlabs.utiliti.model.Style;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
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
import javax.swing.JButton;
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
  private final JLabel titleLabel;
  private final JLabel summaryLabel;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.setBorder(null);
    this.setDividerSize(6);
    this.setResizeWeight(0.0);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);
    this.assetPanel.setChangedCallback(this::updateSummary);

    this.assetTree.setBackground(Style.COLOR_SURFACE);
    this.assetTree.setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 6, 8, 6));

    JPanel leftPanel = new JPanel(new BorderLayout());
    leftPanel.setOpaque(true);
    leftPanel.setBackground(Style.COLOR_SURFACE);
    leftPanel.setBorder(javax.swing.BorderFactory.createMatteBorder(0, 0, 0, 1, Style.COLOR_BORDER));
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

    JToggleButton densityToggle = new JToggleButton(new GridIcon());
    densityToggle.setPreferredSize(new Dimension(32, 28));
    densityToggle.setToolTipText("Toggle compact list / card grid");
    densityToggle.addActionListener(e -> {
      assetPanel.setCompact(densityToggle.isSelected());
      densityToggle.setIcon(densityToggle.isSelected() ? new ListIcon() : new GridIcon());
      updateSummary();
    });

    JButton clearSearch = new JButton(Icons.CROSS_8);
    clearSearch.setBorderPainted(false);
    clearSearch.setContentAreaFilled(false);
    clearSearch.setOpaque(false);
    clearSearch.setMargin(new Insets(2, 2, 2, 2));
    clearSearch.setPreferredSize(new Dimension(24, 28));
    clearSearch.setToolTipText("Clear search");
    clearSearch.addActionListener(e -> {
      searchField.setText("");
      assetPanel.setFilterText("");
      updateSummary();
    });

    JPanel searchBox = new JPanel(new BorderLayout(8, 0)) {
      @Override
      protected void paintComponent(Graphics g) {
        Graphics2D g2 = (Graphics2D) g.create();
        try {
          g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
          g2.setColor(Style.COLOR_SURFACE);
          g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
          g2.setColor(Style.COLOR_BORDER);
          g2.drawRoundRect(0, 0, getWidth() - 1, getHeight() - 1, 10, 10);
        } finally {
          g2.dispose();
        }
        super.paintComponent(g);
      }
    };
    searchBox.setOpaque(false);
    searchBox.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 10, 0, 4));
    searchBox.setPreferredSize(new Dimension(250, 30));
    JLabel searchIcon = new JLabel(Icons.SEARCH_16);
    searchIcon.setPreferredSize(new Dimension(16, 30));
    searchBox.add(searchIcon, BorderLayout.WEST);
    searchBox.add(this.searchField, BorderLayout.CENTER);
    searchBox.add(clearSearch, BorderLayout.EAST);

    JPanel titleBar = new JPanel(new BorderLayout());
    titleBar.setOpaque(false);
    this.titleLabel = new JLabel("Resources");
    this.titleLabel.setForeground(Style.COLOR_TEXT);
    this.titleLabel.setFont(this.titleLabel.getFont().deriveFont(java.awt.Font.BOLD));
    titleBar.add(this.titleLabel, BorderLayout.WEST);

    JPanel tools = new JPanel(new FlowLayout(FlowLayout.TRAILING, 6, 0));
    tools.setOpaque(false);
    this.summaryLabel = new JLabel();
    this.summaryLabel.setForeground(Style.COLOR_SUBTEXT);
    tools.add(this.summaryLabel);
    tools.add(searchBox);
    tools.add(densityToggle);
    JSlider zoomSlider = new JSlider(96, 150, 118);
    zoomSlider.setPreferredSize(new Dimension(110, 28));
    zoomSlider.setOpaque(false);
    zoomSlider.setToolTipText("Asset card size");
    zoomSlider.addChangeListener(e -> assetPanel.setCardSize(zoomSlider.getValue()));
    tools.add(zoomSlider);

    JPanel topBar = new JPanel(new BorderLayout(8, 0));
    topBar.setOpaque(true);
    topBar.setBackground(Style.COLOR_BG);
    topBar.setBorder(javax.swing.BorderFactory.createCompoundBorder(
        javax.swing.BorderFactory.createMatteBorder(0, 0, 1, 0, Style.COLOR_BORDER),
        javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10)));
    topBar.add(titleBar, BorderLayout.WEST);
    topBar.add(tools, BorderLayout.EAST);

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.setOpaque(true);
    rightPanel.setBackground(Style.COLOR_BG);
    rightPanel.add(topBar, BorderLayout.NORTH);

    JScrollPane scrollPane =
      new JScrollPane(
        assetPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setBlockIncrement(48);
    scrollPane.setBorder(null);
    scrollPane.getViewport().setBackground(Style.COLOR_BG);
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    this.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(
      Editor.preferences().getMainSplitterPosition() != 0
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
  public void refresh() {
    this.assetTree.forceUpdate();
    this.searchField.setText("");
    updateSummary();
  }

  public void updateSummary() {
    if (this.titleLabel != null) {
      String breadcrumb = this.assetTree.getCurrentBreadcrumb();
      AssetPanelItem focused = this.assetPanel.getFocusedItem();
      if (focused != null) {
        String details = focused.getDetailsSummary();
        breadcrumb += "  ›  " + focused.getName();
        if (!details.isBlank()) {
          breadcrumb += "  •  " + details;
        }
      }
      this.titleLabel.setText(breadcrumb);
    }
    if (this.summaryLabel != null) {
      int visible = this.assetPanel.getVisibleItemCount();
      int total = this.assetPanel.getTotalItemCount();
      String count = visible == total ? total + " assets" : visible + " of " + total + " assets";
      this.summaryLabel.setText(count);
    }
  }

  private static final class GridIcon implements Icon {
    @Override public int getIconWidth() { return 16; }
    @Override public int getIconHeight() { return 16; }
    @Override public void paintIcon(java.awt.Component c, Graphics g, int x, int y) {
      Graphics2D g2 = (Graphics2D) g.create();
      g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
      g2.setColor(Style.COLOR_TEXT);
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
      g2.setColor(Style.COLOR_TEXT);
      for (int row = 0; row < 3; row++) {
        int yy = y + 3 + row * 5;
        g2.fillRoundRect(x + 1, yy, 3, 3, 2, 2);
        g2.drawLine(x + 7, yy + 1, x + 15, yy + 1);
      }
      g2.dispose();
    }
  }
}
