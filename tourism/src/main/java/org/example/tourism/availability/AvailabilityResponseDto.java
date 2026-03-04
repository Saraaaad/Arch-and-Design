package org.example.tourism.availability;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AvailabilityResponseDto {
    private Long hotelId;
    private Long roomTypeId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private boolean available;
    private BigDecimal totalPrice;
}