package org.example.tourism.pricing;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DESIGN PATTERN: STRATEGY (Context)
 *
 * The context maintains a reference to the current pricing strategy
 * and delegates the pricing calculation to it. This allows runtime
 * switching of pricing algorithms based on business needs.
 */
@Component
public class PricingStrategyContext {

    private final PricingStrategy weekendPricing;
    private final PricingStrategy seasonalPricing;

    // Default strategy
    private PricingStrategy currentStrategy;

    public PricingStrategyContext(
            @Qualifier("weekendPricing") PricingStrategy weekendPricing,
            @Qualifier("seasonalPricing") PricingStrategy seasonalPricing) {
        this.weekendPricing = weekendPricing;
        this.seasonalPricing = seasonalPricing;
        this.currentStrategy = weekendPricing; // Default strategy
    }

    /**
     * Switch to a different pricing strategy at runtime
     */
    public void setStrategy(PricingStrategy strategy) {
        this.currentStrategy = strategy;
    }

    /**
     * Use weekend pricing strategy
     */
    public void useWeekendPricing() {
        this.currentStrategy = weekendPricing;
    }

    /**
     * Use seasonal pricing strategy
     */
    public void useSeasonalPricing() {
        this.currentStrategy = seasonalPricing;
    }

    /**
     * Calculate price using the current strategy
     */
    public BigDecimal calculatePrice(BigDecimal basePrice, LocalDate checkIn, LocalDate checkOut) {
        return currentStrategy.calculateTotalPrice(basePrice, checkIn, checkOut);
    }

    /**
     * Get current strategy name for logging
     */
    public String getCurrentStrategyName() {
        return currentStrategy.getStrategyName();
    }
}