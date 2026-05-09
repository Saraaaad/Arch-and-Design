package org.example.tourism.notification;

import java.time.LocalDateTime;

/**
 * DESIGN PATTERN: OBSERVER
 *
 * This is the event object that gets passed to observers.
 * It encapsulates all information about a notification event.
 */
public class NotificationEvent {

    public enum EventType {
        BOOKING_CREATED,
        BOOKING_CONFIRMED,
        BOOKING_CANCELLED,
        PAYMENT_COMPLETED,
        PAYMENT_REFUNDED,
        REVIEW_ADDED,
        USER_REGISTERED
    }

    private final EventType eventType;
    private final Long userId;
    private final String userEmail;
    private final Long relatedEntityId;
    private final String message;
    private final LocalDateTime timestamp;

    public NotificationEvent(EventType eventType, Long userId, String userEmail,
                             Long relatedEntityId, String message) {
        this.eventType = eventType;
        this.userId = userId;
        this.userEmail = userEmail;
        this.relatedEntityId = relatedEntityId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }

    public EventType getEventType() { return eventType; }
    public Long getUserId() { return userId; }
    public String getUserEmail() { return userEmail; }
    public Long getRelatedEntityId() { return relatedEntityId; }
    public String getMessage() { return message; }
    public LocalDateTime getTimestamp() { return timestamp; }
}