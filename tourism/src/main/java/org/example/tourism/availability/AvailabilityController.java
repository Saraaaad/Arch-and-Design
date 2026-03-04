package org.example.tourism.availability;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/availability")
@RequiredArgsConstructor
@Tag(name = "Availability", description = "Endpoints for checking availability and pricing")
public class AvailabilityController {

    private final AvailabilityService availabilityService;

    @GetMapping("/check")
    @Operation(summary = "Check availability and price for a room type")
    public AvailabilityResponseDto checkAvailability(
            @RequestParam Long hotelId,
            @RequestParam Long roomTypeId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut,
            @RequestParam Integer guests) {
        return availabilityService.checkAvailability(hotelId, roomTypeId, checkIn, checkOut, guests);
    }
}