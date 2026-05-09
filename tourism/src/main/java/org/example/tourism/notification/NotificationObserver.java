package org.example.tourism.notification;

/**
 * DESIGN PATTERN: OBSERVER (Observer Interface)
 *
 * All notification handlers implement this interface.
 * When a new event occurs, all registered observers are notified.
 * This allows adding new notification channels (SMS, push notifications, etc.)
 * without modifying existing code.
 */
public interface NotificationObserver {

    /**
     * Called when a notification event occurs
     * @param event The notification event
     */
    void onNotificationEvent(NotificationEvent event);

    /**
     * Returns the types of events this observer is interested in
     * @return Array of event types
     */
    NotificationEvent.EventType[] getInterestedEvents();
}