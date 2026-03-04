package org.example.tourism.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Booking", description = "Endpoints for managing bookings")
public class BookingController {

    private final BookingService bookingService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a new booking")
    public BookingResponseDto createBooking(@Valid @RequestBody BookingRequestDto bookingRequestDto) {
        return bookingService.createBooking(bookingRequestDto);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a booking by ID")
    public BookingResponseDto getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @PostMapping("/{id}/confirm")
    @Operation(summary = "Confirm a booking")
    public BookingResponseDto confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/cancel")
    @Operation(summary = "Cancel a booking")
    public BookingResponseDto cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get all bookings for a user")
    public List<BookingResponseDto> getUserBookings(@PathVariable Long userId) {
        return bookingService.getUserBookings(userId);
    }
}