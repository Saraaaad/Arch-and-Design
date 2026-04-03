package org.example.tourism.wishlist;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class WishlistCheckResponse {
    private boolean inWishlist;
    private Long wishlistId;
}