package com.ninja_squad.spring.events;

/**
 * The moment when an event-observing method is called.
 * @author JB Nizet
 */
public enum EventMoment {
    /**
     * Indicates that the method must be called when the event is fired.
     */
    IN_PROGRESS,

    /**
     * Indicates that the method must be called after the current transaction has been committed successfully.
     * If there is no transaction when the event is fired, or if the transaction is not committed, the observing
     * method is not called.
     */
    AFTER_COMMIT,

    /**
     * Indicates that the method must be called after the current transaction has been rollbacked.
     * If there is no transaction when the event is fired, or if the transaction is not rollbacked, the observing
     * method is not called.
     */
    AFTER_ROLLBACK;
}
