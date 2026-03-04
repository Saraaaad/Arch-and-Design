package org.example.tourism.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class NotificationService {

    public void sendNotification(String email, String subject, String message) {

        log.info("EMAIL NOTIFICATION - To: {}, Subject: {}, Message: {}", email, subject, message);
    }

    public void sendBookingConfirmation(String email, Long bookingId, String details) {
        sendNotification(email, "Booking Confirmed #" + bookingId,
                "Your booking " + bookingId + " has been confirmed. " + details);
    }

    public void sendBookingCancellation(String email, Long bookingId) {
        sendNotification(email, "Booking Cancelled #" + bookingId,
                "Your booking " + bookingId + " has been cancelled.");
    }
}