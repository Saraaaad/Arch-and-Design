package org.example.tourism.catalog.roomtype.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.List;

@Data
public class RoomTypeResponseDto {
    private Long id;
    private String name;
    private String description;
    private Integer capacity;
    private BigDecimal basePrice;
    private String bedType;
    private Integer roomSize;
    private List<String> amenities;
    private List<String> imageUrls;
    private Integer totalRooms;
    private Boolean smokingAllowed;
    private String view;
    private String boardBasis;
    private Boolean refundable;
    private String cancellationPolicy;
    private Long hotelId;
}