package de.gurkenlabs.utiliti.view.components;

import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.utiliti.controller.Controller;
import de.gurkenlabs.utiliti.controller.Editor;
import de.gurkenlabs.utiliti.controller.FileDrop;
import de.gurkenlabs.utiliti.model.Icons;
import java.awt.BorderLayout;
import java.awt.Insets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class AssetList extends JSplitPane implements Controller {
  private final AssetPanel assetPanel;
  private final AssetTree assetTree;
  private final JTextField searchField;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);

    this.setLeftComponent(assetTree);

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
    this.searchField = new JTextField();
    this.searchField.putClientProperty("JTextField.search", true);
    this.searchField.setToolTipText("Search assets...");
    this.searchField.setMargin(new Insets(2, 4, 2, 4));
    this.searchField.getDocument().addDocumentListener(new DocumentListener() {
      @Override public void insertUpdate(DocumentEvent e) { filter(); }
      @Override public void removeUpdate(DocumentEvent e) { filter(); }
      @Override public void changedUpdate(DocumentEvent e) { filter(); }
      private void filter() {
        assetPanel.setFilterText(searchField.getText());
      }
    });

    JPanel rightPanel = new JPanel(new BorderLayout());
    rightPanel.add(searchField, BorderLayout.NORTH);

    JScrollPane scrollPane =
      new JScrollPane(
        assetPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setBlockIncrement(48);
    rightPanel.add(scrollPane, BorderLayout.CENTER);

    this.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(
      Editor.preferences().getMainSplitterPosition() != 0
        ? Editor.preferences().getAssetsSplitter()
        : 200);

    this.setRightComponent(rightPanel);
  }

  public AssetTree getAssetTree() {
    return this.assetTree;
  }

  @Override
  public void refresh() {
    this.assetTree.forceUpdate();
    this.searchField.setText("");
  }
}
