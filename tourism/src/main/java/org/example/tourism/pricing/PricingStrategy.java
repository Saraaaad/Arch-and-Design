package org.example.tourism.pricing;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DESIGN PATTERN: STRATEGY
 *
 * This interface defines the contract for different pricing strategies.
 * Instead of hardcoding pricing logic, we can swap strategies dynamically
 * based on season, demand, or promotions. This makes the system extensible
 * without modifying existing code (Open/Closed Principle).
 */
public interface PricingStrategy {

    /**
     * Calculate total price for a stay
     * @param basePrice Base price per night
     * @param checkIn Check-in date
     * @param checkOut Check-out date
     * @return Total price
     */
    BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut);

    /**
     * Get the strategy name for logging/tracking
     * @return Strategy name
     */
    String getStrategyName();
}