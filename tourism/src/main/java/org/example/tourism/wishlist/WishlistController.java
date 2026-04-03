package org.example.tourism.wishlist;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.example.tourism.catalog.hotel.dto.HotelSearchResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/wishlist")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "User wishlist management endpoints")
public class WishlistController {

    private final WishlistService wishlistService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Add hotel to wishlist", description = "Authenticated users can add hotels to their wishlist")
    public ResponseEntity<Void> addToWishlist(@RequestParam Long hotelId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        wishlistService.addToWishlist(userId, hotelId);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @DeleteMapping("/{hotelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Remove hotel from wishlist", description = "Authenticated users can remove hotels from their wishlist")
    public ResponseEntity<Void> removeFromWishlist(@PathVariable Long hotelId, Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        wishlistService.removeFromWishlist(userId, hotelId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/me")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get my wishlist", description = "Get all hotels in the authenticated user's wishlist")
    public ResponseEntity<List<HotelSearchResponseDto>> getMyWishlist(Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(wishlistService.getUserWishlist(userId));
    }

    @GetMapping("/me/check/{hotelId}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Check if hotel is in wishlist", description = "Check if a specific hotel is in the user's wishlist")
    public ResponseEntity<WishlistCheckResponse> checkInWishlist(
            @PathVariable Long hotelId,
            Authentication authentication) {
        Long userId = getUserIdFromAuth(authentication);
        return ResponseEntity.ok(wishlistService.isInWishlist(userId, hotelId));
    }

    @GetMapping("/count/{hotelId}")
    @Operation(summary = "Get wishlist count for a hotel", description = "Get number of users who added this hotel to wishlist")
    public ResponseEntity<Long> getWishlistCount(@PathVariable Long hotelId) {
        return ResponseEntity.ok(wishlistService.getWishlistCount(hotelId));
    }

    private Long getUserIdFromAuth(Authentication authentication) {
        Jwt jwt = (Jwt) authentication.getPrincipal();
        return jwt.getClaim("userId");
    }
}