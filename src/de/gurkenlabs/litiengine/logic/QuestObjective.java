package de.gurkenlabs.litiengine.logic;

import java.util.List;

import de.gurkenlabs.litiengine.entities.IEntity;
import de.gurkenlabs.tiled.tmx.IMapObject;

public class QuestObjective {
  private String description;
  private boolean completed;
  private int objectiveNumber;
  private Quest quest;
  private ObjectiveType objectiveType;
  private List<IMapObject> objectiveAreas;
  private List<IEntity> objectiveEntities;
  private boolean failed;

  public QuestObjective(final Quest quest, final int objectiveNumber, List<IMapObject> mapObjects, List<IEntity> entities, ObjectiveType objectiveType, String description) {
    this.quest = quest;
    this.objectiveNumber = objectiveNumber;
    this.objectiveType = objectiveType;
    this.objectiveAreas = mapObjects;
    this.objectiveEntities = entities;

  }

  public String getDescription() {
    return this.description;
  }

  public boolean isCompleted() {
    return this.completed;
  }

  protected void complete() {
    this.completed = true;
  }

  public int getObjectiveNumber() {
    return this.objectiveNumber;
  }

  public Quest getQuest() {
    return this.quest;
  }

  public List<IMapObject> getObjectiveAreas() {
    return this.objectiveAreas;
  }

  public ObjectiveType getObjectiveType() {
    return this.objectiveType;
  }

  public List<IEntity> getObjectiveEntities() {
    return this.objectiveEntities;
  }

  public boolean isFailed() {
    return this.failed;
  }

  public void fail() {
    this.failed = true;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setObjectiveNumber(int objectiveNumber) {
    this.objectiveNumber = objectiveNumber;
  }

  public void setQuest(Quest quest) {
    this.quest = quest;
  }

  public void setObjectiveType(ObjectiveType objectiveType) {
    this.objectiveType = objectiveType;
  }

  public void setObjectiveAreas(List<IMapObject> objectiveAreas) {
    this.objectiveAreas = objectiveAreas;
  }

  public void setObjectiveEntities(List<IEntity> entities) {
    this.objectiveEntities = entities;
  }

  public void reset() {
    this.failed = false;
    this.completed = false;
  }
}