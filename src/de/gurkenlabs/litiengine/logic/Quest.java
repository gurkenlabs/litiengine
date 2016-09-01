package de.gurkenlabs.litiengine.logic;

import java.util.List;

import de.gurkenlabs.litiengine.IGameLoop;
import de.gurkenlabs.litiengine.IUpdateable;

public class Quest implements IUpdateable {
  private List<QuestObjective> objectives;
  private List<Quest> subQuests;
  private boolean completed;
  private boolean failed;
  private int questNumber;

  public Quest(final int questNumber, List<QuestObjective> objectives) {
    this.questNumber = questNumber;
    this.objectives = objectives;
  }

  public Quest(final int questNumber, List<QuestObjective> objectives, List<Quest> subQuests) {
    this(questNumber, objectives);
    this.subQuests = subQuests;
  }

  public List<QuestObjective> getObjectives() {
    return this.objectives;
  }

  public List<Quest> getSubQuests() {
    return this.subQuests;
  }

  public boolean isCompleted() {
    return this.completed;
  }

  public void setCompleted(boolean completed) {
    this.completed = completed;
  }

  public boolean isFailed() {
    return this.failed;
  }

  public void setFailed(boolean failed) {
    this.failed = failed;
  }

  public int getQuestNumber() {
    return questNumber;
  }

  @Override
  public void update(IGameLoop loop) {
    this.completed = this.getObjectives().stream().allMatch(val -> val.isFulfilled() == true);
    this.failed = this.getObjectives().stream().anyMatch(val -> val.isFailed() == true);

  }

}
