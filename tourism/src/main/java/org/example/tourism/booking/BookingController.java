package org.example.tourism.booking;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
@Tag(name = "Bookings", description = "Booking management endpoints")
public class BookingController {

    private final BookingService bookingService;


    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER', 'GUEST')")
    public BookingResponseDto createBooking(
            @Valid @RequestBody CreateBookingRequest request,
            Authentication authentication) {

        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");

        BookingRequestDto bookingRequest = new BookingRequestDto();
        bookingRequest.setHotelId(request.getHotelId());
        bookingRequest.setRoomTypeId(request.getRoomTypeId());
        bookingRequest.setUserId(userId);  // Set it here
        bookingRequest.setCheckInDate(request.getCheckInDate());
        bookingRequest.setCheckOutDate(request.getCheckOutDate());
        bookingRequest.setGuests(request.getGuests());

        return bookingService.createBooking(bookingRequest);
    }

    @GetMapping("/{id}")
    @PreAuthorize("@authz.isBookingOwnerOrAdmin(#id, authentication)")
    @Operation(summary = "Get booking by ID")
    public BookingResponseDto getBooking(@PathVariable Long id) {
        return bookingService.getBooking(id);
    }

    @PostMapping("/{id}/confirm")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Confirm a booking", description = "Admin only")
    public BookingResponseDto confirmBooking(@PathVariable Long id) {
        return bookingService.confirmBooking(id);
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("@authz.isBookingOwnerOrAdmin(#id, authentication)")
    @Operation(summary = "Cancel a booking")
    public BookingResponseDto cancelBooking(@PathVariable Long id) {
        return bookingService.cancelBooking(id);
    }

    @GetMapping("/user/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my bookings", description = "Get all bookings for the authenticated user")
    public List<BookingResponseDto> getMyBookings(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        Long userId = jwt.getClaim("userId");
        return bookingService.getUserBookings(userId);
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or @authz.isSelf(#userId, authentication)")
    @Operation(summary = "Get bookings for a specific user", description = "User themselves or Admin only")
    public List<BookingResponseDto> getUserBookings(@PathVariable Long userId) {
        return bookingService.getUserBookings(userId);
    }

    @GetMapping("/hotel/{hotelId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'HOTEL_MANAGER')")
    @Operation(summary = "Get all bookings for a hotel", description = "Admin or Hotel Manager only")
    public List<BookingResponseDto> getHotelBookings(@PathVariable Long hotelId) {
        return bookingService.getHotelBookings(hotelId);
    }
}