package org.example.tourism.availability;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

@Service
public class PricingServiceImpl implements PricingService {

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
}