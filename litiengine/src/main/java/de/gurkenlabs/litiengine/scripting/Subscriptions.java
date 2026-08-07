package de.gurkenlabs.litiengine.scripting;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

/** Owns a collection of registrations and releases them together. */
public final class Subscriptions implements AutoCloseable {
  private final Collection<Subscription> subscriptions = ConcurrentHashMap.newKeySet();

  public <T extends Subscription> T add(T subscription) {
    if (subscription != null) this.subscriptions.add(subscription);
    return subscription;
  }

  public boolean remove(Subscription subscription) {
    return this.subscriptions.remove(subscription);
  }

  @Override
  public void close() {
    for (Subscription subscription : this.subscriptions) {
      try {
        subscription.close();
      } finally {
        this.subscriptions.remove(subscription);
      }
    }
  }
}
