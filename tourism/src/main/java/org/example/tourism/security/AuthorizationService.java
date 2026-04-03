package org.example.tourism.security;

import org.example.tourism.booking.BookingResponseDto;
import org.example.tourism.booking.BookingService;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component("authz")
public class AuthorizationService {

    private final BookingService bookingService;

    public AuthorizationService(BookingService bookingService) {
        this.bookingService = bookingService;
    }

    public boolean isSelf(Long userId, Authentication authentication) {
        if (authentication == null || userId == null) return false;

        Object principal = authentication.getPrincipal();
        if (!(principal instanceof Jwt jwt)) return false;

        Long authenticatedUserId = jwt.getClaim("userId");
        return authenticatedUserId != null && authenticatedUserId.equals(userId);
    }

    public boolean isSelfOrAdmin(Long userId, Authentication authentication) {
        if (authentication == null) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        return isSelf(userId, authentication);
    }

    public boolean isBookingOwner(Long bookingId, Authentication authentication) {
        if (authentication == null || bookingId == null) return false;

        try {
            BookingResponseDto booking = bookingService.getBooking(bookingId);
            return isSelf(booking.getUserId(), authentication);
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isBookingOwnerOrAdmin(Long bookingId, Authentication authentication) {
        if (authentication == null) return false;

        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin) return true;

        return isBookingOwner(bookingId, authentication);
    }

    public boolean isHotelManager(Authentication authentication) {
        if (authentication == null) return false;

        return authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_HOTEL_MANAGER"));
    }
}