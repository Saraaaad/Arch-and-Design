package org.example.tourism.availability;

import java.time.LocalDate;

public interface AvailabilityService {
    AvailabilityResponseDto checkAvailability(Long hotelId, Long roomTypeId, LocalDate checkIn, LocalDate checkOut, Integer guests);
}