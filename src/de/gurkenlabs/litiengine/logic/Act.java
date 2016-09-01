package de.gurkenlabs.litiengine.logic;

import java.util.List;

public class Act {
  private List<Quest> quests;
  private boolean completed;

  public Act() {
    // TODO Auto-generated constructor stub
  }

  public void addQuest(Quest q) {
    this.getQuests().add(q);
  }

  public void setQuest(Quest q, int questNumber) {
    this.getQuests().set(questNumber, q);
  }

  public List<Quest> getQuests() {
    return this.quests;
  }

  public boolean isCompleted() {
    return completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

}
