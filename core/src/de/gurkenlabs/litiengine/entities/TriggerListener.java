package de.gurkenlabs.litiengine.entities;

/** This listener provides callbacks for when a {@code Trigger} gets activated or deactivated. */
public interface TriggerListener
    extends TriggerActivatedListener, TriggerDeactivatedListener, TriggerActivatingCondition {}
