package org.example.tourism.catalog.hotel.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class HotelSearchResponseDto {
    private Long id;
    private String name;
    private String city;
    private String country;
    private Integer starRating;
    private String mainImageUrl;
    private Double averageRating;
    private Integer reviewCount;
    private BigDecimal priceFrom;
    private Boolean available;
}