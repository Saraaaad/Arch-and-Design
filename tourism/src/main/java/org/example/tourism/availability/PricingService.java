package org.example.tourism.availability;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface PricingService {
    BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut);
}