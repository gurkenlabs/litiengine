package de.gurkenlabs.utiliti.view.renderers;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.Component;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import org.junit.jupiter.api.Test;

class SceneGraphRendererTest {

  @Test
  void preferredWidthDoesNotDependOnTreeWidth() {
    SceneGraphRenderer renderer = new SceneGraphRenderer();
    DefaultMutableTreeNode node = new DefaultMutableTreeNode();
    JTree tree = new JTree(node);

    tree.setSize(300, 200);
    Component narrow = renderer.getTreeCellRendererComponent(tree, node, false, false, true, 0, false);
    int narrowWidth = narrow.getPreferredSize().width;

    tree.setSize(600, 200);
    Component wide = renderer.getTreeCellRendererComponent(tree, node, false, false, true, 0, false);
    int wideWidth = wide.getPreferredSize().width;

    assertEquals(narrowWidth, wideWidth);
    assertTrue(wideWidth < tree.getWidth());
  }
}
