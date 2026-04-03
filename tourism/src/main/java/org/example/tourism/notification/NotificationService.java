package org.example.tourism.notification;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Service
@Slf4j
public class NotificationService {

    private static final String RESET = "\u001B[0m";
    private static final String GREEN = "\u001B[32m";
    private static final String YELLOW = "\u001B[33m";
    private static final String BLUE = "\u001B[34m";
    private static final String PURPLE = "\u001B[35m";
    private static final String CYAN = "\u001B[36m";

    public void sendBookingConfirmation(String email, Long bookingId, String details) {
        String subject = String.format("Booking Confirmed #%d - Your stay is confirmed!", bookingId);

        String[] parts = details.split(", ");

        if (parts.length < 5) {
            log.warn("Invalid details format: {}", details);
            return;
        }
        String hotelId = parts[0].split(": ")[1];
        String checkIn = parts[1].split(": ")[1];
        String checkOut = parts[2].split(": ")[1];
        String total = parts[3].split(": ")[1];
        String transaction = parts[4].split(": ")[1];

        String message = String.format("""
                ╔══════════════════════════════════════════════════════════╗
                ║                 BOOKING CONFIRMATION                     ║
                ╠══════════════════════════════════════════════════════════╣
                ║  Dear Guest,                                             ║
                ║                                                          ║
                ║  Your booking has been successfully confirmed!           ║
                ║                                                          ║
                ║  ┌────────────────────────────────────────────────────┐ ║
                ║  │                   BOOKING DETAILS                   │ ║
                ║  ├────────────────────────────────────────────────────┤ ║
                ║  │  Booking ID:    #%d                                  │ ║
                ║  │  Hotel ID:      %s                                  │ ║
                ║  │  Check-in:      %s                                  │ ║
                ║  │  Check-out:     %s                                  │ ║
                ║  │  Total Amount:  %s                                  │ ║
                ║  │  Transaction:   %s                                  │ ║
                ║  └────────────────────────────────────────────────────┘ ║
                ║                                                          ║
                ║  Thank you for choosing our service!                     ║
                ║  We look forward to hosting you!                         ║
                ║                                                          ║
                ║  Tourism Hotel Booking Team                              ║
                ╚══════════════════════════════════════════════════════════╝
                """, bookingId, hotelId, checkIn, checkOut, total, transaction);

        log.info(GREEN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        log.info(GREEN + "║                    EMAIL NOTIFICATION                        ║" + RESET);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(GREEN + "║  " + CYAN + "To:      " + YELLOW + "%-45s" + GREEN + "║", email);
        log.info(GREEN + "║  " + CYAN + "Subject: " + YELLOW + "%-45s" + GREEN + "║", subject);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);

        String[] messageLines = message.split("\n");
        for (String line : messageLines) {
            if (line.contains("BOOKING CONFIRMATION") || line.contains("BOOKING DETAILS")) {
                log.info(PURPLE + line + RESET);
            } else if (line.contains("Booking ID:") || line.contains("Hotel ID:") ||
                    line.contains("Check-in:") || line.contains("Check-out:") ||
                    line.contains("Total Amount:") || line.contains("Transaction:")) {
                log.info(CYAN + line + RESET);
            } else if (line.contains("Thank you") || line.contains("look forward")) {
                log.info(YELLOW + line + RESET);
            } else {
                log.info(BLUE + line + RESET);
            }
        }

        log.info(GREEN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        log.info(GREEN + "    Email sent successfully at: " + java.time.LocalDateTime.now() + RESET);
    }

    public void sendBookingCancellation(String email, Long bookingId) {
        String subject = String.format("Booking Cancelled #%d", bookingId);

        String message = String.format("""
                ╔══════════════════════════════════════════════════════════╗
                ║                 BOOKING CANCELLATION                     ║
                ╠══════════════════════════════════════════════════════════╣
                ║  Dear Guest,                                             ║
                ║                                                          ║
                ║  Your booking #%d has been cancelled as requested.      ║
                ║                                                          ║
                ║  If you have any questions, please contact our           ║
                ║  support team.                                           ║
                ║                                                          ║
                ║  Tourism Hotel Booking Team                              ║
                ╚══════════════════════════════════════════════════════════╝
                """, bookingId);

        log.info(YELLOW + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        log.info(YELLOW + "║                 CANCELLATION NOTIFICATION                     ║" + RESET);
        log.info(YELLOW + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(YELLOW + "║  " + CYAN + "To:      " + YELLOW + "%-45s" + YELLOW + "║", email);
        log.info(YELLOW + "║  " + CYAN + "Subject: " + YELLOW + "%-45s" + YELLOW + "║", subject);
        log.info(YELLOW + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        log.info(message);
    }
    public void sendNotification(String email, String subject, String message) {
        log.info(GREEN + "╔════════════════════════════════════════════════════════════════╗" + RESET);
        log.info(GREEN + "║                    EMAIL NOTIFICATION                        ║" + RESET);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(GREEN + "║  " + CYAN + "To:      " + YELLOW + "%-45s" + GREEN + "║", email);
        log.info(GREEN + "║  " + CYAN + "Subject: " + YELLOW + "%-45s" + GREEN + "║", subject);
        log.info(GREEN + "╠════════════════════════════════════════════════════════════════╣" + RESET);
        log.info(BLUE + message + RESET);
        log.info(GREEN + "╚════════════════════════════════════════════════════════════════╝" + RESET);
        log.info(GREEN + "    Email sent at: " + java.time.LocalDateTime.now() + RESET);
    }
}