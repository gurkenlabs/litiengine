package de.gurkenlabs.utiliti.swing;

import javax.swing.tree.DefaultMutableTreeNode;

public class CustomMutableTreeNode extends DefaultMutableTreeNode {
  private static final long serialVersionUID = 1L;

  private String name;
  private int index;

  public CustomMutableTreeNode(String name, int index) {
    this.name = name;
    this.index = index;
  }
  
  public void setName(String name) {
    this.name = name;
  }
  
  public int getIndex() {
    return index;
  }
  
  @Override
  public String toString() {
    return this.name;
  }
}
