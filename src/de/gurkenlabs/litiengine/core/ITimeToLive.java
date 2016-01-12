package de.gurkenlabs.litiengine.core;

public interface ITimeToLive {
  int getAliveTime();

  int getTimeToLive();

  boolean timeToLiveReached();
}
