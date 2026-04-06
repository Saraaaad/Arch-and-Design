package org.example.tourism.unit.availability;

import org.example.tourism.availability.PricingServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PricingServiceImplTest {

    private PricingServiceImpl pricingService;

    @BeforeEach
    void setUp() {
        pricingService = new PricingServiceImpl();
    }

    @Test
    void calculateTotalPrice_ShouldCalculateCorrectPrice_ForWeekdaysOnly() {
        // Given
        BigDecimal basePrice = new BigDecimal("100.00");
        LocalDate checkIn = LocalDate.of(2024, 1, 8);  // Monday
        LocalDate checkOut = LocalDate.of(2024, 1, 12); // Friday (4 nights)

        // When
        BigDecimal result = pricingService.calculateTotalPrice(basePrice, checkIn, checkOut);

        // Then
        assertThat(result).isEqualTo(new BigDecimal("400.00"));
    }

    @Test
    void calculateTotalPrice_ShouldCalculateCorrectPrice_ForWeekendsOnly() {
        // Given
        BigDecimal basePrice = new BigDecimal("100.00");
        LocalDate checkIn = LocalDate.of(2024, 1, 12); // Friday
        LocalDate checkOut = LocalDate.of(2024, 1, 15); // Monday (3 nights)

        // When
        BigDecimal result = pricingService.calculateTotalPrice(basePrice, checkIn, checkOut);

        // Then
        // Fri: $100, Sat: $120, Sun: $120 = $340
        assertThat(result).isEqualByComparingTo("340.00");    }

    @Test
    void calculateTotalPrice_ShouldCalculateCorrectPrice_ForMixedWeekdaysAndWeekends() {
        // Given
        BigDecimal basePrice = new BigDecimal("100.00");
        LocalDate checkIn = LocalDate.of(2024, 1, 11); // Thursday
        LocalDate checkOut = LocalDate.of(2024, 1, 15); // Monday (4 nights)

        // When
        BigDecimal result = pricingService.calculateTotalPrice(basePrice, checkIn, checkOut);

        // Then
        assertThat(result).isEqualByComparingTo("440.00");    }

    @Test
    void calculateTotalPrice_ShouldReturnZero_WhenCheckInEqualsCheckOut() {
        // Given
        BigDecimal basePrice = new BigDecimal("100.00");
        LocalDate date = LocalDate.now();

        // When
        BigDecimal result = pricingService.calculateTotalPrice(basePrice, date, date);

        // Then
        assertThat(result).isEqualTo(BigDecimal.ZERO);
    }
}