package org.example.tourism.catalog.shared;

import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ReviewResponseDto {
    private Long id;
    private Long hotelId;
    private String hotelName;
    private Long userId;
    private String userName;
    private Integer rating;
    private String title;
    private String comment;
    private LocalDate stayDate;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Integer helpfulCount;
    private Boolean verifiedBooking;
}