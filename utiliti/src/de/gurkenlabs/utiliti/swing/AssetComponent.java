package de.gurkenlabs.utiliti.swing;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import de.gurkenlabs.litiengine.graphics.ImageFormat;
import de.gurkenlabs.utiliti.Program;
import de.gurkenlabs.utiliti.components.EditorScreen;
import de.gurkenlabs.utiliti.components.Controller;

@SuppressWarnings("serial")
public class AssetComponent extends JSplitPane implements Controller {
  private AssetPanel assetPanel;
  private AssetTree assetTree;

  public AssetComponent() {
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
        EditorScreen.instance().importSpriteSheets(droppedImages.toArray(new File[droppedImages.size()]));
      }
    });

    JScrollPane scrollPane = new JScrollPane(assetPanel);

    this.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> Program.preferences().setAssetsSplitter(this.getDividerLocation()));
    this.setDividerLocation(Program.preferences().getMainSplitterPosition() != 0 ? Program.preferences().getAssetsSplitter() : 200);

    this.setRightComponent(scrollPane);
  }
  
  public AssetTree getAssetTree() {
    return this.assetTree;
  }
  
  public void refresh() {
    this.assetTree.forceUpdate();
  }
}
