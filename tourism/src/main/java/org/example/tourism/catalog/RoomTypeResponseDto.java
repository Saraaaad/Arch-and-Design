package org.example.tourism.catalog;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class RoomTypeResponseDto {
    private Long id;
    private String name;
    private Integer capacity;
    private BigDecimal basePrice;
    private String amenities;
    private Long hotelId;
}