package org.example.tourism.availability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Endpoints for checking availability and pricing")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/check")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'GUEST')")
    @Operation(summary = "Check availability and price for a room type",
            description = "Any authenticated user can check availability")
    public AvailabilityResponseDto checkAvailability(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guests) {
        return availabilityService.checkAvailability(hotelId, roomTypeId, checkIn, checkOut, guests);
    }
}