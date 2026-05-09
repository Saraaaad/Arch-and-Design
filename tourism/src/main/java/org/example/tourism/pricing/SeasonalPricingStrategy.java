package org.example.tourism.pricing;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Month;
import java.time.temporal.ChronoUnit;

/**
 * DESIGN PATTERN: STRATEGY (Concrete Strategy)
 *
 * Implements seasonal pricing where summer and holiday months cost more.
 * This can be swapped in during peak seasons without changing any other code.
 */
@Component("seasonalPricing")
public class SeasonalPricingStrategy implements PricingStrategy {

    private static final BigDecimal PEAK_SEASON_MULTIPLIER = new BigDecimal("1.5");
    private static final BigDecimal REGULAR_MULTIPLIER = new BigDecimal("1.0");

    @Override
    public BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        BigDecimal totalPrice = BigDecimal.ZERO;
        long days = ChronoUnit.DAYS.between(checkIn, checkOut);

        for (int i = 0; i < days; i++) {
            LocalDate date = checkIn.plusDays(i);
            BigDecimal dailyPrice = basePrice;

            if (isPeakSeason(date)) {
                dailyPrice = dailyPrice.multiply(PEAK_SEASON_MULTIPLIER);
            }

            totalPrice = totalPrice.add(dailyPrice);
        }

        return totalPrice;
    }

    private boolean isPeakSeason(LocalDate date) {
        Month month = date.getMonth();
        // Peak season: June, July, August, December
        return month == Month.JUNE || month == Month.JULY ||
                month == Month.AUGUST || month == Month.DECEMBER;
    }

    @Override
    public String getStrategyName() {
        return "Seasonal Pricing Strategy";
    }
}