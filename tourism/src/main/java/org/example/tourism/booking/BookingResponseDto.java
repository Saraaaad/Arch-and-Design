package org.example.tourism.booking;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class BookingResponseDto {
    private Long id;
    private Long hotelId;
    private Long roomTypeId;
    private Long userId;
    private LocalDate checkInDate;
    private LocalDate checkOutDate;
    private BigDecimal totalPrice;
    private BookingStatus status;
    private Integer guests;
    private LocalDateTime createdAt;
}