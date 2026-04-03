package org.example.tourism.booking;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CreateBookingRequest {
    @NotNull(message = "Hotel ID is required")
    private Long hotelId;

    @NotNull(message = "Room Type ID is required")
    private Long roomTypeId;

    @NotNull(message = "Check-in date is required")
    @FutureOrPresent(message = "Check-in date must be today or in the future")
    private LocalDate checkInDate;

    @NotNull(message = "Check-out date is required")
    @Future(message = "Check-out date must be in the future")
    private LocalDate checkOutDate;

    @Positive(message = "Number of guests must be positive")
    private Integer guests;

}