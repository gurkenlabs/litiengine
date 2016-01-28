package de.gurkenlabs.litiengine;

public interface ITimeToLive {
  long getAliveTime();

  int getTimeToLive();

  boolean timeToLiveReached();
}
