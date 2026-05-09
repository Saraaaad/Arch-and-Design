package org.example.tourism.notification;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

/**
 * DESIGN PATTERN: OBSERVER (Subject/Publisher)
 *
 * Manages observers and publishes notification events to all registered observers.
 * This decouples the notification logic from business logic - services just
 * publish events and don't need to know how notifications are handled.
 */
@Component
public class NotificationEventPublisher {

    private final List<NotificationObserver> observers = new ArrayList<>();

    /**
     * Register an observer to receive notifications
     */
    public void registerObserver(NotificationObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    /**
     * Remove an observer
     */
    public void removeObserver(NotificationObserver observer) {
        observers.remove(observer);
    }

    /**
     * Publish an event to all interested observers
     */
    public void publishEvent(NotificationEvent event) {
        for (NotificationObserver observer : observers) {
            for (NotificationEvent.EventType eventType : observer.getInterestedEvents()) {
                if (eventType == event.getEventType()) {
                    observer.onNotificationEvent(event);
                    break;
                }
            }
        }
    }
}