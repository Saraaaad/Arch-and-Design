package org.example.tourism.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * DESIGN PATTERN: OBSERVER (Concrete Observer)
 *
 * Handles sending email notifications for various events.
 * Currently logs to console but can be easily extended to send real emails.
 */
@Component
@Slf4j
public class EmailNotificationObserver implements NotificationObserver {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String CYAN = "\u001B[36m";

    @Override
    public void onNotificationEvent(NotificationEvent event) {
        log.info(GREEN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        log.info(GREEN + "║                    EMAIL NOTIFICATION                        ║" + RESET);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(GREEN + "║  " + CYAN + "To:      " + YELLOW + "%-45s" + GREEN + "║", event.getUserEmail());
        log.info(GREEN + "║  " + CYAN + "Event:   " + YELLOW + "%-45s" + GREEN + "║", event.getEventType());
        log.info(GREEN + "║  " + CYAN + "Message: " + YELLOW + "%-45s" + GREEN + "║", event.getMessage());
        log.info(GREEN + "╚════════════════════════════════════════════════════════════════╝" + RESET);

        // In production, this would send actual emails via SMTP
        switch (event.getEventType()) {
            case BOOKING_CONFIRMED:
                sendBookingConfirmationEmail(event);
                break;
            case BOOKING_CANCELLED:
                sendCancellationEmail(event);
                break;
            case PAYMENT_REFUNDED:
                sendRefundEmail(event);
                break;
            default:
                sendGenericEmail(event);
        }
    }

    @Override
    public NotificationEvent.EventType[] getInterestedEvents() {
        // This observer is interested in ALL event types
        return NotificationEvent.EventType.values();
    }

    private void sendBookingConfirmationEmail(NotificationEvent event) {
        log.info(GREEN + "    Booking confirmation email sent for booking #{}" + RESET,
                event.getRelatedEntityId());
    }

    private void sendCancellationEmail(NotificationEvent event) {
        log.info(YELLOW + "    Cancellation email sent for booking #{}" + RESET,
                event.getRelatedEntityId());
    }

    private void sendRefundEmail(NotificationEvent event) {
        log.info(GREEN + "    Refund confirmation email sent for payment #{}" + RESET,
                event.getRelatedEntityId());
    }

    private void sendGenericEmail(NotificationEvent event) {
        log.info(GREEN + "    Notification email sent to {}" + RESET, event.getUserEmail());
    }
}