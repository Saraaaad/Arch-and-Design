package org.example.tourism.wishlist;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class WishlistResponseDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private String hotelCity;
    private Integer hotelStarRating;
    private String hotelImageUrl;
    private LocalDateTime createdAt;
}