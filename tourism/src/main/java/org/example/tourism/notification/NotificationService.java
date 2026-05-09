package org.example.tourism.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * DESIGN PATTERN: OBSERVER
 *
 * NOTE: This original NotificationService is now DEPRECATED in favor of the
 * Observer pattern implementation (NotificationEventPublisher + Observers).
 *
 * It's kept for backward compatibility but new code should use the Observer pattern:
 * - NotificationEventPublisher.publishEvent() to send notifications
 * - EmailNotificationObserver handles email formatting
 * - LoggingNotificationObserver handles logging
 *
 * This demonstrates how the Observer pattern DECOUPLES notification logic
 * from business logic. Services no longer need to know HOW notifications
 * are sent - they just publish events.
 */
@Service
@Slf4j
@Deprecated
public class NotificationService {

    // Keep existing methods for backward compatibility
    // but delegate to the Observer pattern internally

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    public void sendBookingConfirmation(String email, Long bookingId, String details) {
        // DESIGN PATTERN: OBSERVER - This method is kept for backward compatibility
        // New code should use NotificationEventPublisher.publishEvent()
        String subject = String.format("Booking Confirmed #%d - Your stay is confirmed!", bookingId);
        sendNotification(email, subject, details);
    }

    public void sendBookingCancellation(String email, Long bookingId) {
        // DESIGN PATTERN: OBSERVER - Legacy wrapper
        String subject = String.format("Booking Cancelled #%d", bookingId);
        String message = String.format("Your booking #%d has been cancelled as requested.", bookingId);
        sendNotification(email, subject, message);
    }

    public void sendNotification(String email, String subject, String message) {
        log.info(GREEN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        log.info(GREEN + "║                    EMAIL NOTIFICATION (LEGACY)                ║" + RESET);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(GREEN + "║  " + CYAN + "To:      " + YELLOW + "%-45s" + GREEN + "║", email);
        log.info(GREEN + "║  " + CYAN + "Subject: " + YELLOW + "%-45s" + GREEN + "║", subject);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(BLUE + message + RESET);
        log.info(GREEN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
    }
}