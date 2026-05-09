package org.example.tourism.notification;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: OBSERVER (Initialization)
 *
 * Registers all observers when the application starts.
 * New observers can be added here without modifying the publisher.
 */
@Component
public class ObserverInitializer {

    private final NotificationEventPublisher publisher;
    private final EmailNotificationObserver emailObserver;
    private final LoggingNotificationObserver loggingObserver;

    public ObserverInitializer(NotificationEventPublisher publisher,
                               EmailNotificationObserver emailObserver,
                               LoggingNotificationObserver loggingObserver) {
        this.publisher = publisher;
        this.emailObserver = emailObserver;
        this.loggingObserver = loggingObserver;
    }

    @PostConstruct
    public void initialize() {
        publisher.registerObserver(emailObserver);
        publisher.registerObserver(loggingObserver);
    }
}