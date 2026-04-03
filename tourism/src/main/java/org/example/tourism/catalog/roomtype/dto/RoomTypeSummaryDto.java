package org.example.tourism.catalog.roomtype.dto;

import lombok.Data;
import lombok.Builder;
import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class RoomTypeSummaryDto {
    private Long id;
    private String name;
    private Integer capacity;
    private BigDecimal basePrice;
    private String bedType;
    private List<String> amenities;
    private String imageUrl;
    private Boolean available;
}