package org.example.tourism.availability;

import org.example.tourism.pricing.PricingStrategyContext;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PricingServiceImpl implements PricingService {

    // DESIGN PATTERN: STRATEGY
    // Instead of hardcoding pricing logic, we delegate to the PricingStrategyContext
    // This allows us to switch between different pricing strategies (weekend, seasonal, etc.)
    // without modifying this service. New pricing strategies can be added by simply
    // creating new implementations of PricingStrategy.
    private final PricingStrategyContext pricingStrategyContext;

    public PricingServiceImpl(PricingStrategyContext pricingStrategyContext) {
        this.pricingStrategyContext = pricingStrategyContext;
    }

    @Override
    public BigDecimal calculateTotalPrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        // Auto-select strategy based on season
        if (isPeakSeason(checkIn, checkOut)) {
            pricingStrategyContext.useSeasonalPricing();
        } else {
            pricingStrategyContext.useWeekendPricing();
        }

        return pricingStrategyContext.calculatePrice(basePrice, checkIn, checkOut);
    }

    private boolean isPeakSeason(LocalDate checkIn, LocalDate checkOut) {
        java.time.Month startMonth = checkIn.getMonth();
        java.time.Month endMonth = checkOut.getMonth();

        return startMonth == java.time.Month.JUNE || startMonth == java.time.Month.JULY ||
                startMonth == java.time.Month.AUGUST || startMonth == java.time.Month.DECEMBER ||
                endMonth == java.time.Month.JUNE || endMonth == java.time.Month.JULY ||
                endMonth == java.time.Month.AUGUST || endMonth == java.time.Month.DECEMBER;
    }
}