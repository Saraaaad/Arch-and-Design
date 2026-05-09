package org.example.tourism.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: OBSERVER (Concrete Observer)
 *
 * Logs all notification events to the application log.
 * Useful for auditing and debugging.
 */
@Component
@Slf4jpublic
class LoggingNotificationObserver implements NotificationObserver {

    @Override
    public void onNotificationEvent(NotificationEvent event) {
        log.info("NOTIFICATION EVENT: type={}, userId={}, entityId={}, message={}, timestamp={}",
                event.getEventType(),
                event.getUserId(),
                event.getRelatedEntityId(),
                event.getMessage(),
                event.getTimestamp());
    }

    @Override
    public NotificationEvent.EventType[] getInterestedEvents() {
        return NotificationEvent.EventType.values();
    }
}