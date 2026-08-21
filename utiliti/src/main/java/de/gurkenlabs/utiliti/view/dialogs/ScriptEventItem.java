package de.gurkenlabs.utiliti.view.dialogs;

/** Represents a single searchable event, lifecycle hook, or API item in {@link ScriptEventExplorerDialog}. */
public record ScriptEventItem(
    String name,
    String category,
    String hostType,
    String description,
    String codeSnippet) {

  @Override
  public String toString() {
    return this.name;
  }
}
