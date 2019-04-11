package de.gurkenlabs.litiengine;

public interface IGameLoop extends ILoop {

  public int perform(int delay, Runnable action);

  public void updateExecutionTime(int index, long ticks);

  public float getTimeScale();

  public int getUpdateRate();

  public void setTimeScale(float timeScale);
}