package de.gurkenlabs.utiliti.swing.controllers;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import de.gurkenlabs.litiengine.resources.ImageFormat;
import de.gurkenlabs.utiliti.components.Controller;
import de.gurkenlabs.utiliti.components.Editor;
import de.gurkenlabs.utiliti.swing.AssetPanel;
import de.gurkenlabs.utiliti.swing.AssetTree;
import de.gurkenlabs.utiliti.swing.FileDrop;

@SuppressWarnings("serial")
public class AssetList extends JSplitPane implements Controller {
  private AssetPanel assetPanel;
  private AssetTree assetTree;

  public AssetList() {
    super(JSplitPane.HORIZONTAL_SPLIT);
    this.assetPanel = new AssetPanel();
    this.assetTree = new AssetTree(this.assetPanel);

    this.setLeftComponent(assetTree);

    new FileDrop(assetPanel, files -> {
      List<File> droppedImages = new ArrayList<>();
      for (File file : files) {
        // handle dropped image
        if (ImageFormat.isSupported(file)) {
          droppedImages.add(file);
        }
      }

      if (!droppedImages.isEmpty()) {
        Editor.instance().importSpriteSheets(droppedImages.toArray(new File[droppedImages.size()]));
      }
    });

    JScrollPane scrollPane = new JScrollPane(assetPanel, JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);

    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Editor.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(Editor.preferences().getMainSplitterPosition() != 0 ? Editor.preferences().getAssetsSplitter() : 200);

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
