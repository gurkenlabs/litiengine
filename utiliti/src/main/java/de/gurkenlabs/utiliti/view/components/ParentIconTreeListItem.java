package de.gurkenlabs.utiliti.view.components;

import java.util.function.Supplier;
import javax.swing.Icon;

public class ParentIconTreeListItem extends IconTreeListItem {

  private final Supplier<Integer> childCount;

  public ParentIconTreeListItem(Object userObject, Icon icon, Supplier<Integer> childCount) {
    super(userObject, icon);
    this.childCount = childCount;
  }

  @Override
  public String toString() {
    return childCount.get() + " " + super.toString();
  }
}
