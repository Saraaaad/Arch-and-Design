package org.example.tourism.catalog.roomtype.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;

@Data
@Builder
public class RoomTypeAvailabilityDto {
    private Long id;
    private String name;
    private Integer capacity;
    private BigDecimal pricePerNight;
    private BigDecimal totalPrice;
    private String bedType;
    private String amenities;
    private Boolean refundable;
}