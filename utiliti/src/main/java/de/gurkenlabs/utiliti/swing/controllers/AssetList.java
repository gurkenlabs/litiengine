package de.gurkenlabs.utiliti.swing.controllers;

import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.utiliti.components.Controller;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.AssetPanel;
import de.gurkenlabs.utiliti.swing.AssetTree;
import de.gurkenlabs.utiliti.swing.FileDrop;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ScrollPaneConstants;

public class AssetList extends JSplitPane implements Controller {
  private final AssetPanel assetPanel;
  private final AssetTree assetTree;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);

    this.setLeftComponent(assetTree);

    new FileDrop(
      assetPanel,
      files -> {
        List<Path> droppedImages = new ArrayList<>();
        for (Path file : files) {
          // handle dropped image
          if (ImageFormat.isSupported(file)) {
            droppedImages.add(file);
          }
        }

        if (!droppedImages.isEmpty()) {
          Editor.instance().importSpriteSheets(droppedImages.toArray(new Path[0]));
        }
      });

    JScrollPane scrollPane =
      new JScrollPane(
        assetPanel,
        ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
        ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
    scrollPane.getVerticalScrollBar().setUnitIncrement(16);
    scrollPane.getVerticalScrollBar().setBlockIncrement(48);

    this.addPropertyChangeListener(
      JSplitPane.DIVIDER_LOCATION_PROPERTY,
      evt -> Editor.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(
      Editor.preferences().getMainSplitterPosition() != 0
        ? Editor.preferences().getAssetsSplitter()
        : 200);

    this.setRightComponent(scrollPane);
  }

  public AssetTree getAssetTree() {
    return this.assetTree;
  }

  @Override
  public void refresh() {
    this.assetTree.forceUpdate();
  }
}
