package de.gurkenlabs.litiengine.scripting;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/// Owns a collection of registrations and releases them together.
public final class Subscriptions implements AutoCloseable {
  private static final Logger log = Logger.getLogger(Subscriptions.class.getName());
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
      } catch (RuntimeException e) {
        log.log(Level.WARNING, "Could not close script subscription.", e);
      } finally {
        this.subscriptions.remove(subscription);
      }
    }
  }
}
