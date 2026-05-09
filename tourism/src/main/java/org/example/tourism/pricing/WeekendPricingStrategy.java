package org.example.tourism.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 *
 * Implements weekend-based pricing where weekend nights cost more.
 * This is the default pricing strategy.
 */
@Component("weekendPricing")
public class WeekendPricingStrategy implements PricingStrategy {

    private static final BigDecimal WEEKEND_MULTIPLIER = new BigDecimal("1.2");
    private static final BigDecimal WEEKDAY_MULTIPLIER = new BigDecimal("1.0");

    @Override
    public BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);

        for (int i = 0; i < days; i++) {
            LocalDate date = checkIn.plusDays(i);
            BigDecimal dailyPrice = basePrice;

            DayOfWeek day = date.getDayOfWeek();
            if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                dailyPrice = dailyPrice.multiply(WEEKEND_MULTIPLIER);
            }

            totalPrice = totalPrice.add(dailyPrice);
        }

        return totalPrice;
    }

    @Override
    public String getStrategyName() {
        return "Weekend Pricing Strategy";
    }
}