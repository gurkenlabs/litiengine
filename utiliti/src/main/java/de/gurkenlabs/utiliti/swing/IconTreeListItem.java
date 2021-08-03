package de.gurkenlabs.utiliti.swing;

import javax.swing.Icon;

public class IconTreeListItem {
  private final Object userObject;
  private final Icon icon;

  public IconTreeListItem(Object userObject) {
    this.userObject = userObject;
    this.icon = null;
  }

  public IconTreeListItem(Object userObject, Icon icon) {
    this.userObject = userObject;
    this.icon = icon;
  }

  public Object getUserObject() {
    return this.userObject;
  }

  public Icon getIcon() {
    return this.icon;
  }

  @Override
  public String toString() {
    return this.getUserObject().toString();
  }
}
