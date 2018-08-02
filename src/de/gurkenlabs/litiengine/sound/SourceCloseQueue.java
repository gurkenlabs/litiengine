package de.gurkenlabs.litiengine.sound;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.sound.sampled.SourceDataLine;

import de.gurkenlabs.litiengine.Game;

final class SourceCloseQueue implements Runnable {
  private final Queue<SourceDataLine> queue = new ConcurrentLinkedQueue<>();
  
  private volatile boolean isRunning = true;

  void enqueue(final SourceDataLine clip) {
    this.queue.add(clip);
  }

  @Override
  public void run() {
    while (this.isRunning) {
      this.closeAllSoundSources();
      try {
        Thread.sleep(1000 / Game.getConfiguration().client().getUpdaterate());
      } catch (final InterruptedException e) {
        break;
      }
    }

    this.closeAllSoundSources();
  }

  void terminate() {
    this.isRunning = false;
  }

  private void closeAllSoundSources() {
    if (this.queue.isEmpty()) {
      return;
    }

    while (this.queue.peek() != null) {
      final SourceDataLine clip = this.queue.poll();
      clip.stop();
      clip.flush();
      clip.close();
    }
  }
}